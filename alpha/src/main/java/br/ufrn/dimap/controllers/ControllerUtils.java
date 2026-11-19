package br.ufrn.dimap.controllers;

import br.ufrn.dimap.factories.HttpResponseFactory;
import br.ufrn.dimap.models.Result;
import br.ufrn.dimap.types.HttpResponse;

final class ControllerUtils {

    private ControllerUtils() {
    }

    static HttpResponse toResponse(Result<?> result, HttpResponseFactory responseFactory) {
        return switch (result.getStatusCode()) {
            case 200 -> responseFactory.ok(result.getValue());
            case 201 -> responseFactory.created(result.getValue());
            case 204 -> responseFactory.noContent();
            case 404 -> responseFactory.notFound(result.getMessage());
            case 409 -> responseFactory.conflict(result.getMessage());
            default -> responseFactory.badRequest(result.getMessage());
        };
    }
}
