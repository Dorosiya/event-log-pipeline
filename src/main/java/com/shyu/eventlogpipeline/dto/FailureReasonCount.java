package com.shyu.eventlogpipeline.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FailureReasonCount {

	private String failureReason;
	private long failureCount;

}
