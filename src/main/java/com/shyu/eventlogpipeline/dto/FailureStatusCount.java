package com.shyu.eventlogpipeline.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FailureStatusCount {

	private String eventStatus;
	private long failureCount;

}
