package br.ufrn.dimap;

import br.ufrn.dimap.hosting.AlienAppBuilder;
import br.ufrn.dimap.service_discovery.hosting.ServiceDiscoveryServerModule;
import br.ufrn.dimap.transport.UdpBeamProtocol;

public class Main {
    public static void main(String[] args) {
        var appBuilder = new AlienAppBuilder();

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
}
