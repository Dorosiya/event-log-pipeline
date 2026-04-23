package com.shyu.eventlogpipeline.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QualityCheckResult {

	private String checkName;
	private long issueCount;

}
