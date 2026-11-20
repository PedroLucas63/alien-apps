package br.ufrn.dimap;

import br.ufrn.dimap.configuration.sources.EnvConfigurationSource;
import br.ufrn.dimap.configuration.sources.JsonConfigurationSource;
import br.ufrn.dimap.hosting.AlienAppBuilder;
import br.ufrn.dimap.service_discovery.hosting.ServiceDiscoveryClientModule;
import br.ufrn.dimap.service_discovery.transport.UdpDiscoveryProtocol;
import br.ufrn.dimap.services.AlphaService;
import br.ufrn.dimap.services.IAlphaService;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        var appBuilder = new AlienAppBuilder();

        loadConfiguration(appBuilder);
        appBuilder.addConfigurationSource(new EnvConfigurationSource());


        appBuilder.addModule(new ServiceDiscoveryClientModule(UdpDiscoveryProtocol.class));
        appBuilder.getServices().registerEternal(IAlphaService.class, AlphaService.class);
        appBuilder.getRoutes().autoRegister();

        var app = appBuilder.build();
        String port = appBuilder.getConfiguration().tryGet("PORT").orElse("8080");
        System.out.println("Alpha (HTTP) rodando na porta " + port + "...");

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
