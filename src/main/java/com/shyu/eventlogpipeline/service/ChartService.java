package com.shyu.eventlogpipeline.service;

import com.shyu.eventlogpipeline.dto.AnalysisResult;
import com.shyu.eventlogpipeline.dto.CoursePerformance;
import com.shyu.eventlogpipeline.dto.EndpointLatencyStat;
import com.shyu.eventlogpipeline.dto.EventTypeCount;
import com.shyu.eventlogpipeline.dto.EventTypeSuccessRate;
import com.shyu.eventlogpipeline.dto.FailureReasonCount;
import com.shyu.eventlogpipeline.dto.FunnelConversion;
import com.shyu.eventlogpipeline.dto.HourlyEventCount;
import com.shyu.eventlogpipeline.dto.UserEventCount;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChartService {

	private static final Font CHART_TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 20);
	private static final Font CHART_AXIS_TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 15);
	private static final Font CHART_AXIS_TICK_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
	private static final Font CHART_LEGEND_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

	private static final String[] CHART_FILE_NAMES = {
			"event_type_count.png",
			"hourly_event_count.png",
			"funnel_conversion.png",
			"user_event_count.png",
			"event_success_rate.png",
			"failure_reason_distribution.png",
			"endpoint_latency.png",
			"course_completion_rate.png"
	};

	private final Path outputDirectory;

	public ChartService(@Value("${output.directory:output}") String outputDirectory) {
		this.outputDirectory = Paths.get(outputDirectory);
	}

	public void createCharts(AnalysisResult analysisResult) {
		try {
			Files.createDirectories(outputDirectory);
			deleteOldChartFiles();
			createEventTypeCountChart(analysisResult.getEventTypeCounts());
			createHourlyEventCountChart(analysisResult.getHourlyEventCounts());
			createFunnelConversionChart(analysisResult.getFunnelConversions());
			createUserEventCountChart(analysisResult.getUserEventCounts());
			createEventSuccessRateChart(analysisResult.getEventTypeSuccessRates());
			createFailureReasonChart(analysisResult.getFailureReasonCounts());
			createEndpointLatencyChart(analysisResult.getEndpointLatencyStats());
			createCourseCompletionRateChart(analysisResult.getCoursePerformances());
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to create chart files.", exception);
		}
	}

	private void deleteOldChartFiles() throws IOException {
		for (String chartFileName : CHART_FILE_NAMES) {
			Files.deleteIfExists(outputDirectory.resolve(chartFileName));
		}
	}

	private void createEventTypeCountChart(List<EventTypeCount> eventTypeCounts) throws IOException {
		List<String> categories = new ArrayList<>();
		List<Number> values = new ArrayList<>();

		for (EventTypeCount eventTypeCount : eventTypeCounts) {
			categories.add(toEventLabel(eventTypeCount.getEventType()));
			values.add(eventTypeCount.getEventCount());
		}

		saveChart("이벤트 유형별 건수", "이벤트 유형", "건수", categories, values, "event_type_count");
	}

	private void createHourlyEventCountChart(List<HourlyEventCount> hourlyEventCounts) throws IOException {
		List<String> categories = new ArrayList<>();
		List<Number> values = new ArrayList<>();

		for (HourlyEventCount hourlyEventCount : hourlyEventCounts) {
			categories.add(hourlyEventCount.getEventHour());
			values.add(hourlyEventCount.getEventCount());
		}

		saveChart("시간대별 이벤트 건수", "시간", "건수", categories, values, "hourly_event_count");
	}

	private void createFunnelConversionChart(List<FunnelConversion> funnelConversions) throws IOException {
		List<String> categories = new ArrayList<>();
		List<Number> values = new ArrayList<>();

		for (FunnelConversion funnelConversion : funnelConversions) {
			categories.add(toEventLabel(funnelConversion.getStepName()));
			values.add(funnelConversion.getConversionRate());
		}

		saveChart("퍼널 전환율", "단계", "전환율 (%)", categories, values, "funnel_conversion");
	}

	private void createUserEventCountChart(List<UserEventCount> userEventCounts) throws IOException {
		List<String> categories = new ArrayList<>();
		List<Number> values = new ArrayList<>();

		for (UserEventCount userEventCount : userEventCounts) {
			categories.add("사용자-" + userEventCount.getUserId());
			values.add(userEventCount.getEventCount());
		}

		saveChart("상위 사용자 이벤트 건수", "사용자", "이벤트 건수", categories, values, "user_event_count");
	}

	private void createEventSuccessRateChart(List<EventTypeSuccessRate> eventTypeSuccessRates) throws IOException {
		List<String> categories = new ArrayList<>();
		List<Number> values = new ArrayList<>();

		for (EventTypeSuccessRate eventTypeSuccessRate : eventTypeSuccessRates) {
			categories.add(toEventLabel(eventTypeSuccessRate.getEventType()));
			values.add(eventTypeSuccessRate.getSuccessRate());
		}

		saveChart("이벤트 유형별 성공률", "이벤트 유형", "성공률 (%)", categories, values, "event_success_rate");
	}

	private void createFailureReasonChart(List<FailureReasonCount> failureReasonCounts) throws IOException {
		List<String> categories = new ArrayList<>();
		List<Number> values = new ArrayList<>();

		for (FailureReasonCount failureReasonCount : failureReasonCounts) {
			categories.add(failureReasonCount.getFailureReason());
			values.add(failureReasonCount.getFailureCount());
		}

		saveChart("실패 사유 분포", "실패 사유", "건수", categories, values, "failure_reason_distribution");
	}

	private void createEndpointLatencyChart(List<EndpointLatencyStat> endpointLatencyStats) throws IOException {
		List<String> categories = new ArrayList<>();
		List<Number> values = new ArrayList<>();

		for (EndpointLatencyStat endpointLatencyStat : endpointLatencyStats) {
			categories.add(endpointLatencyStat.getEndpoint());
			values.add(endpointLatencyStat.getAverageLatencyMs());
		}

		saveChart("엔드포인트 평균 지연시간", "엔드포인트", "평균 지연시간 (ms)", categories, values, "endpoint_latency");
	}

	private void createCourseCompletionRateChart(List<CoursePerformance> coursePerformances) throws IOException {
		List<String> categories = new ArrayList<>();
		List<Number> values = new ArrayList<>();

		for (CoursePerformance coursePerformance : coursePerformances) {
			categories.add("강의-" + coursePerformance.getCourseId());
			values.add(coursePerformance.getCompletionRate());
		}

		saveChart("강의별 수강 완료율", "강의", "수강 완료율 (%)", categories, values, "course_completion_rate");
	}

	private void saveChart(
			String title,
			String xAxisTitle,
			String yAxisTitle,
			List<String> categories,
			List<Number> values,
			String fileName
	) throws IOException {
		if (categories.isEmpty()) {
			categories.add("없음");
			values.add(0);
		}

		CategoryChart chart = new CategoryChartBuilder()
				.width(1000)
				.height(650)
				.title(title)
				.xAxisTitle(xAxisTitle)
				.yAxisTitle(yAxisTitle)
				.build();

		chart.getStyler().setLegendVisible(false);
		chart.getStyler().setChartTitleFont(CHART_TITLE_FONT);
		chart.getStyler().setAxisTitleFont(CHART_AXIS_TITLE_FONT);
		chart.getStyler().setAxisTickLabelsFont(CHART_AXIS_TICK_FONT);
		chart.getStyler().setLegendFont(CHART_LEGEND_FONT);
		chart.getStyler().setXAxisLabelRotation(45);
		chart.addSeries("value", categories, values);
		BitmapEncoder.saveBitmap(
				chart,
				outputDirectory.resolve(fileName).toString(),
				BitmapEncoder.BitmapFormat.PNG
		);
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
}
