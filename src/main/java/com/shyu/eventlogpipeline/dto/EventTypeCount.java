package com.shyu.eventlogpipeline.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventTypeCount {

	private String eventType;
	private long eventCount;

}
