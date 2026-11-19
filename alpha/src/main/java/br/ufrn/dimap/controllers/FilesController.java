package br.ufrn.dimap.controllers;

import br.ufrn.dimap.annotations.binding.Param;
import br.ufrn.dimap.annotations.routing.Controller;
import br.ufrn.dimap.annotations.routing.MapDelete;
import br.ufrn.dimap.annotations.routing.MapGet;
import br.ufrn.dimap.annotations.routing.MapPost;
import br.ufrn.dimap.annotations.routing.MapPut;
import br.ufrn.dimap.dtos.UserFileMetadata;
import br.ufrn.dimap.factories.HttpResponseFactory;
import br.ufrn.dimap.models.Result;
import br.ufrn.dimap.services.IAlphaService;
import br.ufrn.dimap.types.HttpContext;
import br.ufrn.dimap.types.HttpResponse;

import java.io.File;
import java.util.List;

@Controller("files")
public class FilesController {
    private final IAlphaService alphaService;
    private final HttpResponseFactory responseFactory;

    public FilesController(IAlphaService alphaService, HttpResponseFactory responseFactory) {
        this.alphaService = alphaService;
        this.responseFactory = responseFactory;
    }

    @MapGet("/{userId}")
    public HttpResponse listFiles(@Param("userId") int userId) {
        Result<List<UserFileMetadata>> result = alphaService.listFilesByUser(userId);
        return ControllerUtils.toResponse(result, responseFactory);
    }

    @MapPost("/{userId}")
    public HttpResponse uploadFile(@Param("userId") int userId, HttpContext context) {
        String filename = context.request != null
                ? context.request.getHeaders().tryGetHeader("X-File-Name").orElse("")
                : "";

        Result<UserFileMetadata> result = alphaService.saveFile(userId, filename, context, false);
        return ControllerUtils.toResponse(result, responseFactory);
    }

    @MapPut("/{userId}/{filename}")
    public HttpResponse updateFile(
            @Param("userId") int userId,
            @Param("filename") String filename,
            HttpContext context
    ) {
        Result<UserFileMetadata> result = alphaService.saveFile(userId, filename, context, true);
        return ControllerUtils.toResponse(result, responseFactory);
    }

    @MapGet("/{userId}/{filename}")
    public HttpResponse downloadFile(@Param("userId") int userId, @Param("filename") String filename) {
        Result<File> result = alphaService.GetFile(userId, filename);
        return ControllerUtils.toResponse(result, responseFactory);
    }

    @MapDelete("/{userId}/{filename}")
    public HttpResponse deleteFile(@Param("userId") int userId, @Param("filename") String filename) {
        Result<Boolean> result = alphaService.deleteFile(userId, filename);
        return ControllerUtils.toResponse(result, responseFactory);
    }
}
