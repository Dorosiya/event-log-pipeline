package com.shyu.eventlogpipeline.service;

import com.shyu.eventlogpipeline.domain.EventLog;
import com.shyu.eventlogpipeline.mapper.EventLogMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventLogService {

	private final EventLogMapper eventLogMapper;

	public EventLogService(EventLogMapper eventLogMapper) {
		this.eventLogMapper = eventLogMapper;
	}

	public void createTable() {
		eventLogMapper.dropEventLogTable();
		eventLogMapper.createEventLogTable();
	}

	public void clear() {
		eventLogMapper.truncateEventLogTable();
	}

	@Transactional
	public void saveAll(List<EventLog> eventLogs) {
		if (eventLogs.isEmpty()) {
			return;
		}
		eventLogMapper.insertEvents(eventLogs);
	}

	public int countAll() {
		return eventLogMapper.countAll();
	}
}
