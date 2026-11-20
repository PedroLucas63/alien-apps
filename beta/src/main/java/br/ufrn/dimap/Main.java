package br.ufrn.dimap;

import br.ufrn.dimap.configuration.sources.EnvConfigurationSource;
import br.ufrn.dimap.hosting.AlienAppBuilder;
import br.ufrn.dimap.service_discovery.hosting.ServiceDiscoveryClientModule;
import br.ufrn.dimap.service_discovery.transport.UdpDiscoveryProtocol;
import br.ufrn.dimap.services.IInvestmentService;
import br.ufrn.dimap.services.InvestmentService;

public class Main {
    public static void main(String[] args) {
        var appBuilder = new AlienAppBuilder();

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
}
