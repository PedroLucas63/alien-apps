package br.ufrn.dimap;

import br.ufrn.dimap.configuration.sources.EnvConfigurationSource;
import br.ufrn.dimap.configuration.sources.JsonConfigurationSource;
import br.ufrn.dimap.hosting.AlienAppBuilder;
import br.ufrn.dimap.service_discovery.hosting.ServiceDiscoveryClientModule;
import br.ufrn.dimap.service_discovery.transport.UdpDiscoveryProtocol;
import br.ufrn.dimap.services.IInvestmentService;
import br.ufrn.dimap.services.InvestmentService;

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
        appBuilder.addConfigurationSource(new EnvConfigurationSource());

        appBuilder.addModule(new ServiceDiscoveryClientModule(UdpDiscoveryProtocol.class));
        appBuilder.getServices().registerEternal(IInvestmentService.class, InvestmentService.class);
        appBuilder.getRoutes().autoRegister();

        var app = appBuilder.build();
        String port = appBuilder.getConfiguration().tryGet("PORT").orElse("5002");
        System.out.println("Beta (HTTP) rodando na porta " + port + "...");

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
