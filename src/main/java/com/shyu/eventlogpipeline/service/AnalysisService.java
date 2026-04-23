package com.shyu.eventlogpipeline.service;

import com.shyu.eventlogpipeline.dto.AnalysisResult;
import com.shyu.eventlogpipeline.mapper.EventLogMapper;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

	private final EventLogMapper eventLogMapper;

	public AnalysisService(EventLogMapper eventLogMapper) {
		this.eventLogMapper = eventLogMapper;
	}

	public AnalysisResult analyze() {
		return new AnalysisResult(
				eventLogMapper.selectEventTypeCounts(),
				eventLogMapper.selectHourlyEventCounts(),
				eventLogMapper.selectFunnelConversions(),
				eventLogMapper.selectUserEventCounts(),
				eventLogMapper.selectEventTypeSuccessRates(),
				eventLogMapper.selectFailureStatusCounts(),
				eventLogMapper.selectFailureReasonCounts(),
				eventLogMapper.selectEndpointLatencyStats(),
				eventLogMapper.selectCoursePerformances(),
				eventLogMapper.selectQualityCheckResults(),
				eventLogMapper.selectSummaryStats()
		);
	}
}
