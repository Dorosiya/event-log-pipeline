package com.shyu.eventlogpipeline.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SummaryStats {

	private long totalEvents;
	private long totalSessions;
	private long successfulEnrollments;
	private long completedLessons;
	private long questionCreateCount;
	private BigDecimal totalRevenue;
	private long businessFailureCount;
	private long systemFailureCount;
	private double failureRate;
	private double averageLatencyMs;
	private String topFailureReason;

}
