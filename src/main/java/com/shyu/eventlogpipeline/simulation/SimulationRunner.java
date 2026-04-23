package com.shyu.eventlogpipeline.simulation;

import com.shyu.eventlogpipeline.domain.EventLog;
import com.shyu.eventlogpipeline.dto.AnalysisResult;
import com.shyu.eventlogpipeline.service.AnalysisService;
import com.shyu.eventlogpipeline.service.ChartService;
import com.shyu.eventlogpipeline.service.EventLogService;
import com.shyu.eventlogpipeline.service.SummaryReportService;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimulationRunner implements ApplicationRunner {

	private final SimulationProperties simulationProperties;
	private final EventGenerator eventGenerator;
	private final EventLogService eventLogService;
	private final AnalysisService analysisService;
	private final ChartService chartService;
	private final SummaryReportService summaryReportService;
	private final ConfigurableApplicationContext applicationContext;

	public SimulationRunner(
			SimulationProperties simulationProperties,
			EventGenerator eventGenerator,
			EventLogService eventLogService,
			AnalysisService analysisService,
			ChartService chartService,
			SummaryReportService summaryReportService,
			ConfigurableApplicationContext applicationContext
	) {
		this.simulationProperties = simulationProperties;
		this.eventGenerator = eventGenerator;
		this.eventLogService = eventLogService;
		this.analysisService = analysisService;
		this.chartService = chartService;
		this.summaryReportService = summaryReportService;
		this.applicationContext = applicationContext;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!simulationProperties.isEnabled()) {
			log.info("Simulation is disabled.");
			return;
		}

		eventLogService.createTable();
		eventLogService.clear();

		runSimulation();

		AnalysisResult analysisResult = analysisService.analyze();
		chartService.createCharts(analysisResult);
		summaryReportService.createSummary(analysisResult);

		log.info("Simulation completed. totalEvents={}", eventLogService.countAll());
		SpringApplication.exit(applicationContext, () -> 0);
	}

	private void runSimulation() {
		for (int runCount = 1; runCount <= simulationProperties.getMaxRuns(); runCount++) {
			List<EventLog> eventLogs = eventGenerator.generateBatch(runCount);
			eventLogService.saveAll(eventLogs);
			log.info(
					"Simulation run {}/{} saved {} events.",
					runCount,
					simulationProperties.getMaxRuns(),
					eventLogs.size()
			);
			sleepIfNeeded(runCount);
		}
	}

	private void sleepIfNeeded(int runCount) {
		if (runCount >= simulationProperties.getMaxRuns()) {
			return;
		}

		try {
			Thread.sleep(Duration.ofSeconds(simulationProperties.getIntervalSeconds()).toMillis());
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Simulation was interrupted.", exception);
		}
	}
}
