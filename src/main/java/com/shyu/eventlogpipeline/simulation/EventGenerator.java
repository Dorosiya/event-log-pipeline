package com.shyu.eventlogpipeline.simulation;

import com.shyu.eventlogpipeline.domain.EventLog;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EventGenerator {

	private static final String COURSE_VIEW = "course_view";
	private static final String COURSE_ENROLL = "course_enroll";
	private static final String LESSON_START = "lesson_start";
	private static final String LESSON_COMPLETE = "lesson_complete";
	private static final String QUESTION_CREATE = "question_create";

	private static final String SUCCESS = "success";
	private static final String BUSINESS_FAILURE = "business_failure";
	private static final String SYSTEM_FAILURE = "system_failure";

	private static final String[] DEVICE_TYPES = {"mobile", "desktop", "tablet"};
	private static final String[] COUNTRIES = {"KR", "US", "JP", "VN"};

	private final SimulationProperties simulationProperties;
	private final Random random;
	private long sessionSequence = 0;

	public EventGenerator(SimulationProperties simulationProperties) {
		this.simulationProperties = simulationProperties;
		this.random = new Random(simulationProperties.getSeed());
	}

	public List<EventLog> generateBatch(int runNumber) {
		List<EventLog> eventLogs = new ArrayList<>();

		while (eventLogs.size() < simulationProperties.getBatchSize()) {
			addSessionEvents(eventLogs, runNumber);
		}

		return eventLogs;
	}

	private void addSessionEvents(List<EventLog> eventLogs, int runNumber) {
		String sessionId = "session-" + runNumber + "-" + sessionSequence;
		int userId = random.nextInt(1000) + 1;
		int courseId = random.nextInt(120) + 1;
		String deviceType = randomDeviceType();
		String country = randomCountry();
		LocalDateTime createdAt = simulatedCreatedAt(runNumber);
		sessionSequence++;

		EventOutcome courseViewOutcome = createOutcome(COURSE_VIEW);
		if (!addEventIfRoom(eventLogs, createEvent(sessionId, userId, COURSE_VIEW, "/courses/{id}", deviceType, country, courseId, null, createdAt, courseViewOutcome))) {
			return;
		}
		if (!SUCCESS.equals(courseViewOutcome.getEventStatus())) {
			return;
		}

		if (random.nextDouble() >= 0.58) {
			return;
		}

		EventOutcome enrollOutcome = createOutcome(COURSE_ENROLL);
		if (!addEventIfRoom(eventLogs, createEvent(sessionId, userId, COURSE_ENROLL, "/courses/{id}/enroll", deviceType, country, courseId, coursePrice(courseId), createdAt.plusMinutes(5), enrollOutcome))) {
			return;
		}
		if (!SUCCESS.equals(enrollOutcome.getEventStatus())) {
			return;
		}

		if (random.nextDouble() >= 0.82) {
			return;
		}

		EventOutcome lessonStartOutcome = createOutcome(LESSON_START);
		if (!addEventIfRoom(eventLogs, createEvent(sessionId, userId, LESSON_START, "/lessons/start", deviceType, country, courseId, null, createdAt.plusMinutes(25), lessonStartOutcome))) {
			return;
		}
		if (!SUCCESS.equals(lessonStartOutcome.getEventStatus())) {
			return;
		}

		if (random.nextDouble() < 0.62) {
			EventOutcome questionCreateOutcome = createOutcome(QUESTION_CREATE);
			addEventIfRoom(eventLogs, createEvent(sessionId, userId, QUESTION_CREATE, "/questions/create", deviceType, country, courseId, null, createdAt.plusMinutes(35), questionCreateOutcome));
		}

		if (random.nextDouble() >= 0.76) {
			return;
		}

		EventOutcome lessonCompleteOutcome = createOutcome(LESSON_COMPLETE);
		addEventIfRoom(eventLogs, createEvent(sessionId, userId, LESSON_COMPLETE, "/lessons/complete", deviceType, country, courseId, null, createdAt.plusMinutes(55), lessonCompleteOutcome));
	}

	private boolean addEventIfRoom(List<EventLog> eventLogs, EventLog eventLog) {
		if (eventLogs.size() >= simulationProperties.getBatchSize()) {
			return false;
		}
		eventLogs.add(eventLog);
		return true;
	}

	private EventLog createEvent(
			String sessionId,
			int userId,
			String eventType,
			String endpoint,
			String deviceType,
			String country,
			Integer courseId,
			BigDecimal price,
			LocalDateTime createdAt,
			EventOutcome eventOutcome
	) {
		EventLog eventLog = new EventLog();
		eventLog.setSessionId(sessionId);
		eventLog.setUserId(userId);
		eventLog.setEventType(eventType);
		eventLog.setEventStatus(eventOutcome.getEventStatus());
		eventLog.setFailureReason(eventOutcome.getFailureReason());
		eventLog.setHttpStatus(eventOutcome.getHttpStatus());
		eventLog.setEndpoint(endpoint);
		eventLog.setDeviceType(deviceType);
		eventLog.setCountry(country);
		eventLog.setCourseId(courseId);
		eventLog.setPrice(price);
		eventLog.setLatencyMs(latencyFor(eventType, eventOutcome.getEventStatus()));
		eventLog.setCreatedAt(createdAt);
		eventLog.setEventId(UUID.randomUUID().toString());
		return eventLog;
	}

	private String randomDeviceType() {
		double value = random.nextDouble();
		if (value < 0.62) {
			return DEVICE_TYPES[0];
		}
		if (value < 0.90) {
			return DEVICE_TYPES[1];
		}
		return DEVICE_TYPES[2];
	}

	private String randomCountry() {
		double value = random.nextDouble();
		if (value < 0.70) {
			return COUNTRIES[0];
		}
		if (value < 0.85) {
			return COUNTRIES[1];
		}
		if (value < 0.95) {
			return COUNTRIES[2];
		}
		return COUNTRIES[3];
	}

	private LocalDateTime simulatedCreatedAt(int runNumber) {
		int hour = (runNumber * 2 + random.nextInt(4)) % 24;
		int minute = random.nextInt(60);
		int second = random.nextInt(60);
		return LocalDateTime.now().minusDays(1).withHour(hour).withMinute(minute).withSecond(second).withNano(0);
	}

	private EventOutcome createOutcome(String eventType) {
		String eventStatus = randomEventStatus(eventType);
		int httpStatus = httpStatusFor(eventStatus);
		String failureReason = failureReasonFor(eventType, eventStatus);
		return new EventOutcome(eventStatus, failureReason, httpStatus);
	}

	private String randomEventStatus(String eventType) {
		double value = random.nextDouble();

		if (COURSE_VIEW.equals(eventType)) {
			if (value < 0.98) {
				return SUCCESS;
			}
			return SYSTEM_FAILURE;
		}

		if (COURSE_ENROLL.equals(eventType)) {
			if (value < 0.70) {
				return SUCCESS;
			}
			if (value < 0.90) {
				return BUSINESS_FAILURE;
			}
			return SYSTEM_FAILURE;
		}

		if (LESSON_START.equals(eventType)) {
			if (value < 0.85) {
				return SUCCESS;
			}
			if (value < 0.93) {
				return BUSINESS_FAILURE;
			}
			return SYSTEM_FAILURE;
		}

		if (LESSON_COMPLETE.equals(eventType)) {
			if (value < 0.88) {
				return SUCCESS;
			}
			if (value < 0.95) {
				return BUSINESS_FAILURE;
			}
			return SYSTEM_FAILURE;
		}

		if (value < 0.82) {
			return SUCCESS;
		}
		if (value < 0.95) {
			return BUSINESS_FAILURE;
		}
		return SYSTEM_FAILURE;
	}

	private int httpStatusFor(String eventStatus) {
		if (SUCCESS.equals(eventStatus)) {
			return 200;
		}
		if (BUSINESS_FAILURE.equals(eventStatus)) {
			return 409;
		}
		if (random.nextDouble() < 0.65) {
			return 500;
		}
		return 504;
	}

	private String failureReasonFor(String eventType, String eventStatus) {
		if (SUCCESS.equals(eventStatus)) {
			return null;
		}

		if (COURSE_VIEW.equals(eventType)) {
			return randomFrom("cdn_error", "service_unavailable");
		}

		if (COURSE_ENROLL.equals(eventType)) {
			if (BUSINESS_FAILURE.equals(eventStatus)) {
				return randomFrom("payment_declined", "coupon_invalid", "already_enrolled");
			}
			return randomFrom("payment_gateway_timeout", "db_lock");
		}

		if (LESSON_START.equals(eventType)) {
			if (BUSINESS_FAILURE.equals(eventStatus)) {
				return randomFrom("not_enrolled", "course_expired");
			}
			return randomFrom("cdn_error", "timeout");
		}

		if (LESSON_COMPLETE.equals(eventType)) {
			if (BUSINESS_FAILURE.equals(eventStatus)) {
				return "session_expired";
			}
			return randomFrom("cdn_error", "timeout");
		}

		if (BUSINESS_FAILURE.equals(eventStatus)) {
			return randomFrom("question_limit_exceeded", "duplicate_question");
		}
		return randomFrom("timeout", "db_lock");
	}

	private int latencyFor(String eventType, String eventStatus) {
		int baseLatencyMs = 110;
		if (COURSE_ENROLL.equals(eventType)) {
			baseLatencyMs = 320;
		} else if (LESSON_START.equals(eventType) || LESSON_COMPLETE.equals(eventType)) {
			baseLatencyMs = 210;
		} else if (QUESTION_CREATE.equals(eventType)) {
			baseLatencyMs = 190;
		}

		int jitter = random.nextInt(140);
		if (SYSTEM_FAILURE.equals(eventStatus)) {
			return baseLatencyMs + jitter + 450;
		}
		if (BUSINESS_FAILURE.equals(eventStatus)) {
			return baseLatencyMs + jitter + 120;
		}
		return baseLatencyMs + jitter;
	}

	private BigDecimal coursePrice(int courseId) {
		BigDecimal[] prices = {
				BigDecimal.valueOf(39000),
				BigDecimal.valueOf(59000),
				BigDecimal.valueOf(79000),
				BigDecimal.valueOf(99000),
				BigDecimal.valueOf(129000)
		};
		return prices[courseId % prices.length];
	}

	private String randomFrom(String... values) {
		List<String> items = Arrays.asList(values);
		return items.get(random.nextInt(items.size()));
	}

	private static class EventOutcome {

		private final String eventStatus;
		private final String failureReason;
		private final int httpStatus;

		private EventOutcome(String eventStatus, String failureReason, int httpStatus) {
			this.eventStatus = eventStatus;
			this.failureReason = failureReason;
			this.httpStatus = httpStatus;
		}

		public String getEventStatus() {
			return eventStatus;
		}

		public String getFailureReason() {
			return failureReason;
		}

		public int getHttpStatus() {
			return httpStatus;
		}
	}
}
