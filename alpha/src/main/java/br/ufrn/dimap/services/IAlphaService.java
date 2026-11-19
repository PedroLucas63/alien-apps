package br.ufrn.dimap.services;

import br.ufrn.dimap.dtos.CreateUserRequest;
import br.ufrn.dimap.dtos.UpdateUserRequest;
import br.ufrn.dimap.dtos.UserFileMetadata;
import br.ufrn.dimap.models.Result;
import br.ufrn.dimap.models.User;
import br.ufrn.dimap.types.HttpContext;

import java.io.File;
import java.util.List;

public interface IAlphaService {
    List<User> getAllUsers();
    Result<User> getUser(int id);
    Result<User> createUser(CreateUserRequest request);
    Result<User> updateUser(int id, UpdateUserRequest request);
    boolean deleteUser(int id);

    Result<List<UserFileMetadata>> listFilesByUser(int userId);
    Result<UserFileMetadata> saveFile(int userId, String fileName, HttpContext context, boolean overwrite);
    Result<File> GetFile(int userId, String fileName);
    Result<Boolean> deleteFile(int userId, String fileName);
}
