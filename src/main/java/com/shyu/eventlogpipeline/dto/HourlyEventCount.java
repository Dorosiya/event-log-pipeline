package com.shyu.eventlogpipeline.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HourlyEventCount {

	private String eventHour;
	private long eventCount;

}
