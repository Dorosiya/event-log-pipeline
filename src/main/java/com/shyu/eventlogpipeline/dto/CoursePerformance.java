package com.shyu.eventlogpipeline.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoursePerformance {

	private int courseId;
	private long successfulEnrollments;
	private long completedLessons;
	private long questionCreates;
	private double completionRate;

}
