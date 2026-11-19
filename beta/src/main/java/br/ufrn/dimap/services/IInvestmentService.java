package br.ufrn.dimap.services;

import br.ufrn.dimap.dtos.InvestmentRequest;
import br.ufrn.dimap.dtos.InvestmentResponse;
import br.ufrn.dimap.models.Result;

public interface IInvestmentService {
    Result<InvestmentResponse> calculate(InvestmentRequest request);
}
