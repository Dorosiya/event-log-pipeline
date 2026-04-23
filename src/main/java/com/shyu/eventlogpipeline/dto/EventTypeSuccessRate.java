package com.shyu.eventlogpipeline.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventTypeSuccessRate {

	private String eventType;
	private long successCount;
	private long totalCount;
	private double successRate;

}
