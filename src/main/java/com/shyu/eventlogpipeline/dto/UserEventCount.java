package com.shyu.eventlogpipeline.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEventCount {

	private int userId;
	private long eventCount;

}
