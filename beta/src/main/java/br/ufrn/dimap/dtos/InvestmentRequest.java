package br.ufrn.dimap.dtos;

public record InvestmentRequest(
        double monthlyValue,
        int months,
        double monthlyRatePercent,
        Double initialValue
) {
}
