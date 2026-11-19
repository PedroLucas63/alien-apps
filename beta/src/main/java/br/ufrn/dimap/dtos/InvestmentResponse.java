package br.ufrn.dimap.dtos;

import java.math.BigDecimal;

public record InvestmentResponse(
        BigDecimal totalInvested,
        BigDecimal totalYield,
        BigDecimal totalAmount,
        BigDecimal monthlyYieldFromPoint
) {
}
