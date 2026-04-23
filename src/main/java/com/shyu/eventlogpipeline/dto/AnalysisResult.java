package com.shyu.eventlogpipeline.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class AnalysisResult {

	private final List<EventTypeCount> eventTypeCounts;
	private final List<HourlyEventCount> hourlyEventCounts;
	private final List<FunnelConversion> funnelConversions;
	private final List<UserEventCount> userEventCounts;
	private final List<EventTypeSuccessRate> eventTypeSuccessRates;
	private final List<FailureStatusCount> failureStatusCounts;
	private final List<FailureReasonCount> failureReasonCounts;
	private final List<EndpointLatencyStat> endpointLatencyStats;
	private final List<CoursePerformance> coursePerformances;
	private final List<QualityCheckResult> qualityCheckResults;
	private final SummaryStats summaryStats;

	public AnalysisResult(
			List<EventTypeCount> eventTypeCounts,
			List<HourlyEventCount> hourlyEventCounts,
			List<FunnelConversion> funnelConversions,
			List<UserEventCount> userEventCounts,
			List<EventTypeSuccessRate> eventTypeSuccessRates,
			List<FailureStatusCount> failureStatusCounts,
			List<FailureReasonCount> failureReasonCounts,
			List<EndpointLatencyStat> endpointLatencyStats,
			List<CoursePerformance> coursePerformances,
			List<QualityCheckResult> qualityCheckResults,
			SummaryStats summaryStats
	) {
		this.eventTypeCounts = eventTypeCounts;
		this.hourlyEventCounts = hourlyEventCounts;
		this.funnelConversions = funnelConversions;
		this.userEventCounts = userEventCounts;
		this.eventTypeSuccessRates = eventTypeSuccessRates;
		this.failureStatusCounts = failureStatusCounts;
		this.failureReasonCounts = failureReasonCounts;
		this.endpointLatencyStats = endpointLatencyStats;
		this.coursePerformances = coursePerformances;
		this.qualityCheckResults = qualityCheckResults;
		this.summaryStats = summaryStats;
	}

}
