package org.mcjug.aameetingmanager.meeting;

public class MeetingType {	
	String id;
	String name;
	String shortName;
	String description;
	String resourceUri;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getResourceUri() {
		return resourceUri;
	}
	public void setResourceUri(String resourceUri) {
		this.resourceUri = resourceUri;
	}
}
