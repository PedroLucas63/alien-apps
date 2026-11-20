package br.ufrn.dimap;

import br.ufrn.dimap.api_gateway.hosting.ApiGatewayModule;
import br.ufrn.dimap.api_gateway.transport.HttpRedirectProtocol;
import br.ufrn.dimap.hosting.AlienAppBuilder;
import br.ufrn.dimap.service_discovery.transport.UdpDiscoveryProtocol;

public class Main {
    public static void main(String[] args) {
        var appBuilder = new AlienAppBuilder();

        appBuilder.addModule(new ApiGatewayModule(UdpDiscoveryProtocol.class, HttpRedirectProtocol.class));

        var app = appBuilder.build();
        String port = appBuilder.getConfiguration().tryGet("PORT").orElse("8080");
        System.out.println("ApiGateway (HTTP) rodando na porta " + port + "...");

        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        app.start();
    }
}
