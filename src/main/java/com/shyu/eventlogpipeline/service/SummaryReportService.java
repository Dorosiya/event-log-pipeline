package com.shyu.eventlogpipeline.service;

import com.shyu.eventlogpipeline.dto.AnalysisResult;
import com.shyu.eventlogpipeline.dto.CoursePerformance;
import com.shyu.eventlogpipeline.dto.EndpointLatencyStat;
import com.shyu.eventlogpipeline.dto.EventTypeCount;
import com.shyu.eventlogpipeline.dto.EventTypeSuccessRate;
import com.shyu.eventlogpipeline.dto.FailureReasonCount;
import com.shyu.eventlogpipeline.dto.FailureStatusCount;
import com.shyu.eventlogpipeline.dto.FunnelConversion;
import com.shyu.eventlogpipeline.dto.QualityCheckResult;
import com.shyu.eventlogpipeline.dto.SummaryStats;
import com.shyu.eventlogpipeline.dto.UserEventCount;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SummaryReportService {

	private final Path outputDirectory;

	public SummaryReportService(@Value("${output.directory:output}") String outputDirectory) {
		this.outputDirectory = Paths.get(outputDirectory);
	}

	public void createSummary(AnalysisResult analysisResult) {
		try {
			Files.createDirectories(outputDirectory);
			Files.writeString(outputDirectory.resolve("summary.md"), createContent(analysisResult));
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to create summary report.", exception);
		}
	}

	private String createContent(AnalysisResult analysisResult) {
		SummaryStats summaryStats = analysisResult.getSummaryStats();
		StringBuilder builder = new StringBuilder();

		builder.append("# 이벤트 로그 파이프라인 요약\n\n");
		builder.append("## 개요\n\n");
		builder.append("- 전체 이벤트 수: ").append(summaryStats.getTotalEvents()).append("\n");
		builder.append("- 전체 세션 수: ").append(summaryStats.getTotalSessions()).append("\n");
		builder.append("- 성공한 수강 등록 수: ").append(summaryStats.getSuccessfulEnrollments()).append("\n");
		builder.append("- 수강 완료 수: ").append(summaryStats.getCompletedLessons()).append("\n");
		builder.append("- 질문 등록 수: ").append(summaryStats.getQuestionCreateCount()).append("\n");
		builder.append("- 총 매출: ").append(formatMoney(summaryStats.getTotalRevenue())).append("\n");
		builder.append("- 비즈니스 실패 건수: ").append(summaryStats.getBusinessFailureCount()).append("\n");
		builder.append("- 시스템 실패 건수: ").append(summaryStats.getSystemFailureCount()).append("\n");
		builder.append("- 전체 실패율: ").append(summaryStats.getFailureRate()).append("%\n");
		builder.append("- 평균 지연시간: ").append(summaryStats.getAverageLatencyMs()).append(" ms\n");
		builder.append("- 최다 실패 사유: ").append(nullToNone(summaryStats.getTopFailureReason())).append("\n\n");

		appendKeyFindings(builder, analysisResult);
		appendQualityChecks(builder, analysisResult);
		appendEventTypeCounts(builder, analysisResult);
		appendFunnelConversions(builder, analysisResult);
		appendUserEventCounts(builder, analysisResult);
		appendEventSuccessRates(builder, analysisResult);
		appendFailureStatusCounts(builder, analysisResult);
		appendFailureReasonCounts(builder, analysisResult);
		appendEndpointLatencies(builder, analysisResult);
		appendCoursePerformances(builder, analysisResult);

		return builder.toString();
	}

	private void appendKeyFindings(StringBuilder builder, AnalysisResult analysisResult) {
		builder.append("## 주요 인사이트\n\n");
		builder.append("- ").append(lowestSuccessRateFinding(analysisResult.getEventTypeSuccessRates())).append("\n");
		builder.append("- ").append(topFailureReasonFinding(analysisResult.getSummaryStats())).append("\n");
		builder.append("- ").append(endpointLatencyFinding(analysisResult.getEndpointLatencyStats())).append("\n");
		builder.append("- ").append(funnelDropFinding(analysisResult.getFunnelConversions())).append("\n\n");
	}

	private void appendQualityChecks(StringBuilder builder, AnalysisResult analysisResult) {
		builder.append("## 데이터 품질 점검\n\n");
		for (QualityCheckResult qualityCheckResult : analysisResult.getQualityCheckResults()) {
			String mark = qualityCheckResult.getIssueCount() == 0 ? "정상" : "확인 필요";
			builder.append("- ")
					.append(mark)
					.append(" - ")
					.append(toCheckLabel(qualityCheckResult.getCheckName()))
					.append(": ")
					.append(qualityCheckResult.getIssueCount())
					.append("\n");
		}
		builder.append("\n");
	}

	private void appendEventTypeCounts(StringBuilder builder, AnalysisResult analysisResult) {
		builder.append("## 이벤트 유형별 건수\n\n");
		builder.append("| 이벤트 유형 | 건수 |\n");
		builder.append("| --- | ---: |\n");
		for (EventTypeCount eventTypeCount : analysisResult.getEventTypeCounts()) {
			builder.append("| ")
					.append(toEventLabel(eventTypeCount.getEventType()))
					.append(" | ")
					.append(eventTypeCount.getEventCount())
					.append(" |\n");
		}
		builder.append("\n");
	}

	private void appendFunnelConversions(StringBuilder builder, AnalysisResult analysisResult) {
		builder.append("## 퍼널 전환율\n\n");
		builder.append("| 단계 | 세션 수 | 전환율 |\n");
		builder.append("| --- | ---: | ---: |\n");
		for (FunnelConversion funnelConversion : analysisResult.getFunnelConversions()) {
			builder.append("| ")
					.append(toEventLabel(funnelConversion.getStepName()))
					.append(" | ")
					.append(funnelConversion.getSessionCount())
					.append(" | ")
					.append(funnelConversion.getConversionRate())
					.append("% |\n");
		}
		builder.append("\n");
	}

	private void appendUserEventCounts(StringBuilder builder, AnalysisResult analysisResult) {
		builder.append("## 상위 사용자 이벤트 건수\n\n");
		builder.append("| 사용자 | 이벤트 건수 |\n");
		builder.append("| --- | ---: |\n");
		for (UserEventCount userEventCount : analysisResult.getUserEventCounts()) {
			builder.append("| ")
					.append("사용자-")
					.append(userEventCount.getUserId())
					.append(" | ")
					.append(userEventCount.getEventCount())
					.append(" |\n");
		}
		builder.append("\n");
	}

	private void appendEventSuccessRates(StringBuilder builder, AnalysisResult analysisResult) {
		builder.append("## 이벤트 유형별 성공률\n\n");
		builder.append("| 이벤트 유형 | 성공 건수 | 전체 건수 | 성공률 |\n");
		builder.append("| --- | ---: | ---: | ---: |\n");
		for (EventTypeSuccessRate eventTypeSuccessRate : analysisResult.getEventTypeSuccessRates()) {
			builder.append("| ")
					.append(toEventLabel(eventTypeSuccessRate.getEventType()))
					.append(" | ")
					.append(eventTypeSuccessRate.getSuccessCount())
					.append(" | ")
					.append(eventTypeSuccessRate.getTotalCount())
					.append(" | ")
					.append(eventTypeSuccessRate.getSuccessRate())
					.append("% |\n");
		}
		builder.append("\n");
	}

	private void appendFailureStatusCounts(StringBuilder builder, AnalysisResult analysisResult) {
		builder.append("## 실패 상태별 건수\n\n");
		builder.append("| 실패 상태 | 건수 |\n");
		builder.append("| --- | ---: |\n");
		for (FailureStatusCount failureStatusCount : analysisResult.getFailureStatusCounts()) {
			builder.append("| ")
					.append(toFailureStatusLabel(failureStatusCount.getEventStatus()))
					.append(" | ")
					.append(failureStatusCount.getFailureCount())
					.append(" |\n");
		}
		builder.append("\n");
	}

	private void appendFailureReasonCounts(StringBuilder builder, AnalysisResult analysisResult) {
		builder.append("## 실패 사유 분포\n\n");
		builder.append("| 실패 사유 | 건수 |\n");
		builder.append("| --- | ---: |\n");
		for (FailureReasonCount failureReasonCount : analysisResult.getFailureReasonCounts()) {
			builder.append("| ")
					.append(failureReasonCount.getFailureReason())
					.append(" | ")
					.append(failureReasonCount.getFailureCount())
					.append(" |\n");
		}
		builder.append("\n");
	}

	private void appendEndpointLatencies(StringBuilder builder, AnalysisResult analysisResult) {
		builder.append("## 엔드포인트 지연시간\n\n");
		builder.append("| 엔드포인트 | 이벤트 수 | 실패 수 | 평균 지연시간 |\n");
		builder.append("| --- | ---: | ---: | ---: |\n");
		for (EndpointLatencyStat endpointLatencyStat : analysisResult.getEndpointLatencyStats()) {
			builder.append("| ")
					.append(endpointLatencyStat.getEndpoint())
					.append(" | ")
					.append(endpointLatencyStat.getEventCount())
					.append(" | ")
					.append(endpointLatencyStat.getFailureCount())
					.append(" | ")
					.append(endpointLatencyStat.getAverageLatencyMs())
					.append(" ms |\n");
		}
		builder.append("\n");
	}

	private void appendCoursePerformances(StringBuilder builder, AnalysisResult analysisResult) {
		builder.append("## 강의별 성과\n\n");
		builder.append("| 강의 | 성공 수강 등록 수 | 수강 완료 수 | 질문 등록 수 | 수강 완료율 |\n");
		builder.append("| --- | ---: | ---: | ---: | ---: |\n");
		for (CoursePerformance coursePerformance : analysisResult.getCoursePerformances()) {
			builder.append("| ")
					.append("강의-")
					.append(coursePerformance.getCourseId())
					.append(" | ")
					.append(coursePerformance.getSuccessfulEnrollments())
					.append(" | ")
					.append(coursePerformance.getCompletedLessons())
					.append(" | ")
					.append(coursePerformance.getQuestionCreates())
					.append(" | ")
					.append(coursePerformance.getCompletionRate())
					.append("% |\n");
		}
		builder.append("\n");
	}

	private String lowestSuccessRateFinding(List<EventTypeSuccessRate> eventTypeSuccessRates) {
		EventTypeSuccessRate lowest = null;
		for (EventTypeSuccessRate eventTypeSuccessRate : eventTypeSuccessRates) {
			if (lowest == null || eventTypeSuccessRate.getSuccessRate() < lowest.getSuccessRate()) {
				lowest = eventTypeSuccessRate;
			}
		}
		if (lowest == null) {
			return "이벤트 성공률 데이터가 없습니다.";
		}
		return toEventLabel(lowest.getEventType()) + "의 성공률이 " + lowest.getSuccessRate() + "%로 가장 낮았습니다.";
	}

	private String topFailureReasonFinding(SummaryStats summaryStats) {
		String topFailureReason = nullToNone(summaryStats.getTopFailureReason());
		if ("없음".equals(topFailureReason)) {
			return "이번 실행에서는 실패 사유가 기록되지 않았습니다.";
		}
		return topFailureReason + "가 이번 실행에서 가장 많이 발생한 실패 사유였습니다.";
	}

	private String endpointLatencyFinding(List<EndpointLatencyStat> endpointLatencyStats) {
		EndpointLatencyStat highestLatency = null;
		for (EndpointLatencyStat endpointLatencyStat : endpointLatencyStats) {
			if (highestLatency == null || endpointLatencyStat.getAverageLatencyMs() > highestLatency.getAverageLatencyMs()) {
				highestLatency = endpointLatencyStat;
			}
		}
		if (highestLatency == null) {
			return "엔드포인트 지연시간 데이터가 없습니다.";
		}
		return highestLatency.getEndpoint() + "의 평균 지연시간이 " + highestLatency.getAverageLatencyMs() + " ms로 가장 높았습니다.";
	}

	private String funnelDropFinding(List<FunnelConversion> funnelConversions) {
		FunnelConversion previous = null;
		FunnelConversion largestDropStep = null;
		double largestDrop = -1.0;

		for (FunnelConversion funnelConversion : funnelConversions) {
			if (previous != null) {
				double drop = previous.getConversionRate() - funnelConversion.getConversionRate();
				if (drop > largestDrop) {
					largestDrop = drop;
					largestDropStep = funnelConversion;
				}
			}
			previous = funnelConversion;
		}

		if (largestDropStep == null) {
			return "퍼널 전환율 데이터가 없습니다.";
		}
		return toEventLabel(largestDropStep.getStepName()) + " 단계 직전에서 전환율이 " + largestDrop + "%p 하락해 가장 큰 이탈이 발생했습니다.";
	}

	private String formatMoney(BigDecimal value) {
		if (value == null) {
			return "0";
		}
		return value.toPlainString();
	}

	private String nullToNone(String value) {
		if (value == null || value.isBlank()) {
			return "없음";
		}
		return value;
	}

	private String toEventLabel(String eventType) {
		if ("course_view".equals(eventType)) {
			return "강의 조회";
		}
		if ("course_enroll".equals(eventType)) {
			return "수강 등록";
		}
		if ("lesson_start".equals(eventType)) {
			return "수강 시작";
		}
		if ("lesson_complete".equals(eventType)) {
			return "수강 완료";
		}
		if ("question_create".equals(eventType)) {
			return "질문 등록";
		}
		return eventType;
	}

	private String toFailureStatusLabel(String eventStatus) {
		if ("business_failure".equals(eventStatus)) {
			return "비즈니스 실패";
		}
		if ("system_failure".equals(eventStatus)) {
			return "시스템 실패";
		}
		if ("success".equals(eventStatus)) {
			return "성공";
		}
		return eventStatus;
	}

	private String toCheckLabel(String checkName) {
		if ("duplicate event_id count".equals(checkName)) {
			return "중복 event_id 건수";
		}
		if ("invalid event_type count".equals(checkName)) {
			return "정의되지 않은 event_type 건수";
		}
		if ("course_enroll with null price count".equals(checkName)) {
			return "course_enroll인데 price가 null인 건수";
		}
		if ("failure without reason count".equals(checkName)) {
			return "실패인데 failure_reason이 없는 건수";
		}
		if ("success with failure_reason count".equals(checkName)) {
			return "성공인데 failure_reason이 존재하는 건수";
		}
		if ("success with non-200 http_status count".equals(checkName)) {
			return "성공인데 http_status가 200이 아닌 건수";
		}
		if ("lesson_start without successful enrollment session count".equals(checkName)) {
			return "성공한 수강 등록 없이 lesson_start가 발생한 세션 수";
		}
		if ("lesson_complete without successful lesson_start session count".equals(checkName)) {
			return "성공한 lesson_start 없이 lesson_complete가 발생한 세션 수";
		}
		return checkName;
	}
}
