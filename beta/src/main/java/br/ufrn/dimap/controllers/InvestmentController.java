package br.ufrn.dimap.controllers;

import br.ufrn.dimap.annotations.binding.Body;
import br.ufrn.dimap.annotations.binding.Query;
import br.ufrn.dimap.annotations.routing.Controller;
import br.ufrn.dimap.annotations.routing.MapGet;
import br.ufrn.dimap.annotations.routing.MapPost;
import br.ufrn.dimap.dtos.InvestmentRequest;
import br.ufrn.dimap.dtos.InvestmentResponse;
import br.ufrn.dimap.factories.HttpResponseFactory;
import br.ufrn.dimap.models.Result;
import br.ufrn.dimap.services.IInvestmentService;
import br.ufrn.dimap.types.HttpResponse;

@Controller("investment")
public class InvestmentController {

    private final IInvestmentService investmentService;
    private final HttpResponseFactory responseFactory;

    public InvestmentController(IInvestmentService investmentService, HttpResponseFactory responseFactory) {
        this.investmentService = investmentService;
        this.responseFactory = responseFactory;
    }

    @MapPost("/calculate")
    public HttpResponse calculatePost(@Body InvestmentRequest request) {
        return executeCalculation(request);
    }

    @MapGet("/calculate")
    public HttpResponse calculateGet(@Query InvestmentRequest request) {
        return executeCalculation(request);
    }

    private HttpResponse executeCalculation(InvestmentRequest request) {
        Result<InvestmentResponse> result = investmentService.calculate(request);
        return result.isSuccess()
                ? responseFactory.ok(result.getValue())
                : responseFactory.badRequest(result.getMessage());
    }
}
