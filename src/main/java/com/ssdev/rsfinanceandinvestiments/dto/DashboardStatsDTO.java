package com.ssdev.rsfinanceandinvestiments.dto;

//package com.ssdev.rsfinanceandinvestiments.dto;

import java.math.BigDecimal;

public class DashboardStatsDTO {
    private long totalCustomers;
    private BigDecimal totalPaid;
    private BigDecimal totalPending;

    public DashboardStatsDTO(long totalCustomers, BigDecimal totalPaid, BigDecimal totalPending) {
        this.totalCustomers = totalCustomers;
        this.totalPaid = totalPaid;
        this.totalPending = totalPending;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public BigDecimal getTotalPending() {
        return totalPending;
    }
}
