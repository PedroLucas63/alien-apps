package br.ufrn.dimap;

import br.ufrn.dimap.configuration.sources.JsonConfigurationSource;
import br.ufrn.dimap.hosting.AlienAppBuilder;
import br.ufrn.dimap.service_discovery.hosting.ServiceDiscoveryServerModule;
import br.ufrn.dimap.transport.UdpBeamProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        var appBuilder = new AlienAppBuilder();

        loadConfiguration(appBuilder);

        appBuilder.addModule(new ServiceDiscoveryServerModule());

        int port = appBuilder.getConfiguration().tryGet("PORT")
                .map(value -> Integer.parseInt(value.trim()))
                .orElse(5000);
        appBuilder.useProtocol(dispatcher -> new UdpBeamProtocol(dispatcher, port));

        var app = appBuilder.build();
        System.out.println("ServiceDiscovery (UDP) rodando na porta " + port + "...");

        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        app.start();
    }

    private static void loadConfiguration(AlienAppBuilder appBuilder) {
        Path localPath = Path.of(System.getProperty("user.dir"), "appsettings.json");
        if (Files.exists(localPath)) {
            appBuilder.addConfigurationSource(new JsonConfigurationSource(localPath.toString()));
            return;
        }
        try (InputStream stream = Main.class.getClassLoader().getResourceAsStream("appsettings.json")) {
            if (stream != null) {
                appBuilder.addConfigurationSource(new JsonConfigurationSource(stream));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
