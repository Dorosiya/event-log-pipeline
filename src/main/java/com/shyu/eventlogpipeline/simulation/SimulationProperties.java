package com.shyu.eventlogpipeline.simulation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "simulation")
public class SimulationProperties {

	private boolean enabled;

	@Positive
	private int intervalSeconds;

	@Positive
	private int batchSize;

	@Positive
	private int maxRuns;

	@Min(0)
	private long seed;
}
