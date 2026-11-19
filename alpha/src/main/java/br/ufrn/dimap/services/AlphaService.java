package br.ufrn.dimap.services;

import br.ufrn.dimap.dtos.CreateUserRequest;
import br.ufrn.dimap.dtos.UpdateUserRequest;
import br.ufrn.dimap.dtos.UserFileMetadata;
import br.ufrn.dimap.models.Result;
import br.ufrn.dimap.models.User;
import br.ufrn.dimap.types.HttpContext;
import br.ufrn.dimap.types.HttpHeader;
import br.ufrn.dimap.types.HttpRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AlphaService implements IAlphaService {
    public static final int MINIMUM_AGE = 13;
    public static final int MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;
    private static final int BUFFER_SIZE = 8192;

    private final AtomicInteger nextId = new AtomicInteger(0);
    private final ConcurrentMap<Integer, User> users = new ConcurrentHashMap<>();
    private final Path storageRoot = Path.of(System.getProperty("user.dir"), "storage");

    @Override
    public List<User> getAllUsers() {
        return users.values()
                .stream()
                .sorted(Comparator.comparingInt(User::id))
                .toList();
    }

    @Override
    public Result<User> getUser(int id) {
        User user = users.get(id);
        if (user == null) {
            return Result.error("Usuário não encontrado.", 404);
        }
        return Result.of(user);
    }

    @Override
    public Result<User> createUser(CreateUserRequest request) {
        String validationError = validateUser(request.name(), request.age());
        if (validationError != null) {
            return Result.error(validationError, 400);
        }

        int id = nextId.incrementAndGet();
        User user = new User(id, request.name().trim(), request.age());
        users.put(id, user);
        return Result.of(user, 201);
    }

    @Override
    public Result<User> updateUser(int id, UpdateUserRequest request) {
        if (!users.containsKey(id)) {
            return Result.error("Usuário não encontrado.", 404);
        }

        String validationError = validateUser(request.name(), request.age());
        if (validationError != null) {
            return Result.error(validationError, 400);
        }

        User updated = new User(id, request.name().trim(), request.age());
        users.put(id, updated);
        return Result.of(updated);
    }

    @Override
    public boolean deleteUser(int id) {
        if (users.remove(id) == null) {
            return false;
        }

        deleteUserDirectory(id);
        return true;
    }

    @Override
    public Result<List<UserFileMetadata>> listFilesByUser(int userId) {
        if (!users.containsKey(userId)) {
            return Result.error("Usuário não encontrado.", 404);
        }

        Path directory = getUserDirectory(userId);
        if (Files.notExists(directory)) {
            return Result.of(List.of());
        }

        try (var paths = Files.list(directory)) {
            List<UserFileMetadata> files = paths
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .map(f -> new UserFileMetadata(
                            f.getName(),
                            f.length(),
                            Instant.ofEpochMilli(f.lastModified())
                    ))
                    .sorted(Comparator.comparing(UserFileMetadata::fileName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            return Result.of(files);
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao listar arquivos do usuário " + userId, e);
        }
    }

    @Override
    public Result<UserFileMetadata> saveFile(int userId, String fileName, HttpContext context, boolean overwrite) {
        if (!users.containsKey(userId)) {
            return Result.error("Usuário não encontrado.", 404);
        }

        String safeName = sanitizeFileName(fileName);
        if (safeName == null) {
            return Result.error("Nome de arquivo inválido.", 400);
        }

        HttpRequest request = context != null ? context.request : null;
        if (request == null) {
            return Result.error("Requisição inválida.", 400);
        }

        Optional<String> contentLengthHeader = request.getHeaders().tryGetHeader(HttpHeader.ContentLength);
        if (contentLengthHeader.isEmpty()) {
            return Result.error("Header Content-Length ausente ou inválido.", 400);
        }

        int length;
        try {
            length = Integer.parseInt(contentLengthHeader.get().trim());
        } catch (NumberFormatException e) {
            return Result.error("Header Content-Length ausente ou inválido.", 400);
        }

        if (length <= 0 || length > MAX_FILE_SIZE_BYTES) {
            return Result.error(
                    "Tamanho de arquivo não suportado. Máximo permitido: " + MAX_FILE_SIZE_BYTES + " bytes (10 MB).",
                    400
            );
        }

        Path userDir = getUserDirectory(userId);
        Path filePath = userDir.resolve(safeName);
        boolean exists = Files.exists(filePath);

        if (!overwrite && exists) {
            return Result.error("Arquivo já existente.", 409);
        }
        if (overwrite && !exists) {
            return Result.error("Arquivo não encontrado para atualização.", 404);
        }

        try {
            Files.createDirectories(userDir);
            copyFixedBytes(request.getBodyStream(), filePath, length);
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao salvar arquivo " + safeName, e);
        }

        File saved = filePath.toFile();
        UserFileMetadata metadata = new UserFileMetadata(
                saved.getName(),
                saved.length(),
                Instant.ofEpochMilli(saved.lastModified())
        );
        return Result.of(metadata, overwrite ? 200 : 201);
    }

    @Override
    public Result<File> GetFile(int userId, String fileName) {
        if (!users.containsKey(userId)) {
            return Result.error("Usuário não encontrado.", 404);
        }

        String safeName = sanitizeFileName(fileName);
        if (safeName == null) {
            return Result.error("Nome de arquivo inválido.", 400);
        }

        Path filePath = getUserDirectory(userId).resolve(safeName);
        if (!Files.isRegularFile(filePath)) {
            return Result.error("Arquivo não encontrado.", 404);
        }

        return Result.of(filePath.toFile(), 200);
    }

    @Override
    public Result<Boolean> deleteFile(int userId, String fileName) {
        if (!users.containsKey(userId)) {
            return Result.error("Usuário não encontrado.", 404);
        }

        String safeName = sanitizeFileName(fileName);
        if (safeName == null) {
            return Result.error("Nome de arquivo inválido.", 400);
        }

        Path filePath = getUserDirectory(userId).resolve(safeName);
        try {
            if (!Files.deleteIfExists(filePath)) {
                return Result.error("Arquivo não encontrado.", 404);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao remover arquivo " + safeName, e);
        }

        return Result.of(true, 204);
    }

    private void deleteUserDirectory(int userId) {
        Path directory = getUserDirectory(userId);
        if (Files.exists(directory)) {
            try (var paths = Files.walk(directory)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private Path getUserDirectory(int userId) {
        return storageRoot.resolve(Integer.toString(userId));
    }

    private static void copyFixedBytes(InputStream input, Path target, int length) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int remaining = length;
        try (OutputStream output = Files.newOutputStream(target)) {
            while (remaining > 0) {
                int read = input.read(buffer, 0, Math.min(buffer.length, remaining));
                if (read == -1) {
                    break;
                }
                output.write(buffer, 0, read);
                remaining -= read;
            }
        }
    }

    private static String validateUser(String name, int age) {
        if (name == null || name.isBlank()) {
            return "O nome do usuário é obrigatório.";
        }
        if (age < MINIMUM_AGE) {
            return "A idade mínima permitida é " + MINIMUM_AGE + " anos.";
        }
        return null;
    }

    private static String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        String name = Path.of(fileName.trim()).getFileName().toString();
        if (name.isBlank() || name.equals(".") || name.equals("..")) {
            return null;
        }
        return name;
    }
}
