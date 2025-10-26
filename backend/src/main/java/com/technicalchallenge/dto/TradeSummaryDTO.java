package com.technicalchallenge.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeSummaryDTO {
    private LocalDate asOf; //Summaries 'as of' this date
    private Map<String, Long> tradeCountByStatus; // Stores the number of trades (type Long) for each status (type String)
    private Map<String, BigDecimal> totalNotionalByCurrency; // Stores the total notional amount (type BigDecimal) for each currency (type String)
    private List<TypeCptyRow> breakdownByTypeAndCounterparty; // Breakdown of trades by type and counterparty(detailed view)
    private Map<String, BigDecimal> netExposureByCurrency; // risk-style summary
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeCptyRow {
        private String tradeType;
        private String counterparty;
        private Long tradeCount;
        private BigDecimal totalNotional;
    }
}

