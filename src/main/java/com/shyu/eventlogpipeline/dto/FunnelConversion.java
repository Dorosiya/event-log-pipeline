package com.shyu.eventlogpipeline.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FunnelConversion {

	private String stepName;
	private long sessionCount;
	private double conversionRate;

}
