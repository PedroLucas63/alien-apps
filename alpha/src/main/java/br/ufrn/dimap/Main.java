package br.ufrn.dimap;

import br.ufrn.dimap.configuration.sources.EnvConfigurationSource;
import br.ufrn.dimap.hosting.AlienAppBuilder;
import br.ufrn.dimap.service_discovery.hosting.ServiceDiscoveryClientModule;
import br.ufrn.dimap.service_discovery.transport.UdpDiscoveryProtocol;
import br.ufrn.dimap.services.AlphaService;
import br.ufrn.dimap.services.IAlphaService;

public class Main {
    public static void main(String[] args) {
        var appBuilder = new AlienAppBuilder();

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
}
