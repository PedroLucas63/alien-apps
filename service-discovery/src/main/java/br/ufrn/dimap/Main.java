package br.ufrn.dimap;

import br.ufrn.dimap.configuration.sources.JsonConfigurationSource;
import br.ufrn.dimap.hosting.AlienAppBuilder;
import br.ufrn.dimap.service_discovery.hosting.ServiceDiscoveryServerModule;
import br.ufrn.dimap.transport.UdpBeamProtocol;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        var appBuilder = new AlienAppBuilder();

        Path configPath = resolveConfigPath();
        if (configPath != null && Files.exists(configPath)) {
            appBuilder.addConfigurationSource(new JsonConfigurationSource(configPath.toString()));
        }

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

    private static Path resolveConfigPath() {
        Path localPath = Path.of(System.getProperty("user.dir"), "appsettings.json");
        if (Files.exists(localPath)) {
            return localPath;
        }
        URL resource = Main.class.getClassLoader().getResource("appsettings.json");
        if (resource == null || !"file".equalsIgnoreCase(resource.getProtocol())) {
            return null;
        }
        try {
            return Path.of(resource.toURI());
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
