package org.mcjug.aameetingmanager.meeting;

import java.util.Date;

public class Meeting {
	private int id;
	
	private String name;
	private String description;
	private String creator;
	private Date startTime;
	private Date endTime;
	private String timeRange;
	private String address;
	private String distance;
	private int dayOfWeekIdx;
	private double latitude;
	private double longitude;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}	
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public int getDayOfWeekIdx() {
		return dayOfWeekIdx;
	}

	public void setDayOfWeekIdx(int dayOfWeekIdx) {
		this.dayOfWeekIdx = dayOfWeekIdx;
	}

	public String getTimeRange() {
		return timeRange;
	}
	
	public void setTimeRange(String timeRange) {
		this.timeRange = timeRange;
	}	
	
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getDistance() {
		return distance;
	}
	
	public void setDistance(String distance) {
		this.distance = distance;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
