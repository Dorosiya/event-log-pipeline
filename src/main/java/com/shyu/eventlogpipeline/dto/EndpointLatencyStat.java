package com.shyu.eventlogpipeline.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EndpointLatencyStat {

	private String endpoint;
	private long eventCount;
	private long failureCount;
	private double averageLatencyMs;

}
