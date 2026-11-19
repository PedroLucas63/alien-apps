package br.ufrn.dimap.controllers;

import br.ufrn.dimap.annotations.binding.Body;
import br.ufrn.dimap.annotations.binding.Param;
import br.ufrn.dimap.annotations.routing.*;
import br.ufrn.dimap.dtos.CreateUserRequest;
import br.ufrn.dimap.dtos.UpdateUserRequest;
import br.ufrn.dimap.factories.HttpResponseFactory;
import br.ufrn.dimap.models.User;
import br.ufrn.dimap.services.IAlphaService;
import br.ufrn.dimap.types.HttpResponse;

import java.util.List;

@Controller("users")
public class UsersController {
    private final IAlphaService alphaService;
    private final HttpResponseFactory responseFactory;

    public UsersController(IAlphaService alphaService, HttpResponseFactory responseFactory) {
        this.alphaService = alphaService;
        this.responseFactory = responseFactory;
    }

    @MapGet
    public List<User> getAll() {
        return alphaService.getAllUsers();
    }

    @MapGet("/{id}")
    public HttpResponse getById(@Param("id") int id) {
        return ControllerUtils.toResponse(alphaService.getUser(id), responseFactory);
    }

    @MapPost
    public HttpResponse create(@Body CreateUserRequest request) {
        return ControllerUtils.toResponse(alphaService.createUser(request), responseFactory);
    }

    @MapPut("/{id}")
    public HttpResponse update(@Param("id") int id, @Body UpdateUserRequest request) {
        return ControllerUtils.toResponse(alphaService.updateUser(id, request), responseFactory);
    }

    @MapDelete("/{id}")
    public HttpResponse delete(@Param("id") int id) {
        var result = alphaService.deleteUser(id);

        if (result) {
            return responseFactory.noContent();
        } else {
            return responseFactory.notFound("Usuário não encontrado");
        }
    }

}
