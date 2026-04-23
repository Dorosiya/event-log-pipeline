package com.shyu.eventlogpipeline.mapper;

import com.shyu.eventlogpipeline.dto.CoursePerformance;
import com.shyu.eventlogpipeline.domain.EventLog;
import com.shyu.eventlogpipeline.dto.EndpointLatencyStat;
import com.shyu.eventlogpipeline.dto.EventTypeCount;
import com.shyu.eventlogpipeline.dto.EventTypeSuccessRate;
import com.shyu.eventlogpipeline.dto.FailureReasonCount;
import com.shyu.eventlogpipeline.dto.FailureStatusCount;
import com.shyu.eventlogpipeline.dto.FunnelConversion;
import com.shyu.eventlogpipeline.dto.HourlyEventCount;
import com.shyu.eventlogpipeline.dto.QualityCheckResult;
import com.shyu.eventlogpipeline.dto.SummaryStats;
import com.shyu.eventlogpipeline.dto.UserEventCount;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EventLogMapper {

	void createEventLogTable();

	void dropEventLogTable();

	void truncateEventLogTable();

	void insertEvents(@Param("eventLogs") List<EventLog> eventLogs);

	int countAll();

	List<EventTypeCount> selectEventTypeCounts();

	List<HourlyEventCount> selectHourlyEventCounts();

	List<FunnelConversion> selectFunnelConversions();

	List<UserEventCount> selectUserEventCounts();

	List<EventTypeSuccessRate> selectEventTypeSuccessRates();

	List<FailureStatusCount> selectFailureStatusCounts();

	List<FailureReasonCount> selectFailureReasonCounts();

	List<EndpointLatencyStat> selectEndpointLatencyStats();

	List<CoursePerformance> selectCoursePerformances();

	List<QualityCheckResult> selectQualityCheckResults();

	SummaryStats selectSummaryStats();
}
