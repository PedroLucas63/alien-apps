package br.ufrn.dimap.services;

import br.ufrn.dimap.dtos.InvestmentRequest;
import br.ufrn.dimap.dtos.InvestmentResponse;
import br.ufrn.dimap.models.Result;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class InvestmentService implements IInvestmentService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @Override
    public Result<InvestmentResponse> calculate(InvestmentRequest request) {
        if (request == null) {
            return Result.error("Requisição inválida.");
        }

        double rawInitial = request.initialValue() != null ? request.initialValue() : 0.0;
        if (rawInitial < 0) {
            return Result.error("O valor inicial não pode ser negativo.");
        }
        if (request.monthlyValue() < 0) {
            return Result.error("O valor mensal não pode ser negativo.");
        }
        if (request.months() <= 0) {
            return Result.error("A quantidade de meses deve ser maior que zero.");
        }
        if (request.monthlyRatePercent() < 0) {
            return Result.error("A porcentagem de rendimento mensal não pode ser negativa.");
        }

        BigDecimal initial = BigDecimal.valueOf(rawInitial);
        BigDecimal monthlyValue = BigDecimal.valueOf(request.monthlyValue());
        BigDecimal rate = BigDecimal.valueOf(request.monthlyRatePercent())
                .divide(ONE_HUNDRED, MathContext.DECIMAL128);

        BigDecimal balance = initial;
        BigDecimal totalInvested = initial;
        BigDecimal growthFactor = BigDecimal.ONE.add(rate);

        for (int i = 0; i < request.months(); i++) {
            balance = balance.multiply(growthFactor).add(monthlyValue);
            totalInvested = totalInvested.add(monthlyValue);
        }

        BigDecimal totalYield = balance.subtract(totalInvested);
        BigDecimal monthlyYieldFromPoint = balance.multiply(rate);

        InvestmentResponse response = new InvestmentResponse(
                totalInvested.setScale(2, RoundingMode.HALF_UP),
                totalYield.setScale(2, RoundingMode.HALF_UP),
                balance.setScale(2, RoundingMode.HALF_UP),
                monthlyYieldFromPoint.setScale(2, RoundingMode.HALF_UP)
        );
        return Result.of(response);
    }
}
