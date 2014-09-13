package org.mcjug.aameetingmanager.jsonobjects;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import com.google.gson.annotations.SerializedName;

public class MeetingsMessage {

	public class MeetingDetails {

		private String address;
		@SerializedName("created_date")
		private Date createdDate;
		private String creator;
		@SerializedName("day_of_week")
		private long dayOfWeek;
		private String description;
		private long distance;
		@SerializedName("end_time")
		private String endTime;
		private long id;
		@SerializedName("internal_type")
		private String internalType;
		@SerializedName("lat")
		private double latitude;
		@SerializedName("long")
		private double longitude;
		private String name;
		@SerializedName("start_time")
		private String startTime;
		private List<Long> types = new ArrayList<Long>();
		public String getAddress() {
			return address;
		}
		public Date getCreatedDate() {
			return createdDate;
		}
		public String getCreator() {
			return creator;
		}
		public long getDayOfWeek() {
			return dayOfWeek;
		}
		public String getDescription() {
			return description;
		}
		public long getDistance() {
			return distance;
		}
		public String getEndTime() {
			return endTime;
		}
		public long getId() {
			return id;
		}
		public String getInternalType() {
			return internalType;
		}
		public double getLatitude() {
			return latitude;
		}
		public double getLongitude() {
			return longitude;
		}
		public String getName() {
			return name;
		}
		public String getStartTime() {
			return startTime;
		}
		public List<Long> getTypes() {
			return types;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public void setCreatedDate(Date createdDate) {
			this.createdDate = createdDate;
		}
		public void setCreator(String creator) {
			this.creator = creator;
		}
		public void setDayOfWeek(long dayOfWeek) {
			this.dayOfWeek = dayOfWeek;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public void setDistance(long distance) {
			this.distance = distance;
		}
		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}
		public void setId(long id) {
			this.id = id;
		}
		public void setInternalType(String internalType) {
			this.internalType = internalType;
		}
		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}
		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}
		public void setTypes(List<Long> types) {
			this.types = types;
		}

		public MeetingDetails(String address, Date createdDate, String creator,
				long dayOfWeek, String description, long distance,
				String endTime, long id, String internalType, double latitude,
				double longitude, String name, String startTime,
				List<Long> types) {
			super();
			this.address = address;
			this.createdDate = createdDate;
			this.creator = creator;
			this.dayOfWeek = dayOfWeek;
			this.description = description;
			this.distance = distance;
			this.endTime = endTime;
			this.id = id;
			this.internalType = internalType;
			this.latitude = latitude;
			this.longitude = longitude;
			this.name = name;
			this.startTime = startTime;
			this.types = types;
		}       

		@Override
		public String toString() {
			if (id > 0) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
				//SimpleDateFormat meetingTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);

				StringBuilder sb = new StringBuilder();
				sb.append('{');
				sb.append(String.format("\"address\": \"%s\", ", address));
				sb.append(String.format("\"created_date\": \"%s\", ", dateFormat.format(createdDate)));
				sb.append(String.format("\"creator\": \"%s\", ", creator));
				sb.append(String.format("\"dayOfWeek\": \"%d\", ", dayOfWeek));
				sb.append(String.format("\"description\": \"%s\", ", description));
				sb.append(String.format("\"distance\": \"%d\", ", distance));
				//sb.append(String.format("\"end_time\": \"%s\", ", meetingTimeFormat.format(endTime)));
				sb.append(String.format("\"end_time\": \"%s\", ", endTime));
				sb.append(String.format("\"id\": \"%d\", ", id));
				sb.append(String.format("\"internalType\": \"%s\", ", internalType));
				sb.append(String.format("\"lat\": \"%s\", ", latitude));
				sb.append(String.format("\"long\": \"%s\", ", longitude));
				sb.append(String.format("\"name\": \"%s\", ", name));
				//sb.append(String.format("\"start_time\": \"%s\", ", meetingTimeFormat.format(startTime)));
				sb.append(String.format("\"start_time\": \"%s\", ", startTime));
				if (types == null) {
					sb.append("\"types\": [] ");
				}
				else
					sb.append(String.format("\"types\": %s ", Arrays.toString(types.toArray())));

				sb.append('}');
				return sb.toString();

			}
			return "id == 0";
		}

	}

	public class Meta {

		public int limit;
		public String next;
		public int offset;
		public String previous;
		@SerializedName("total_count")
		public int totalCount;

		public Meta (int limit, String next, int offset, String previous, int totalCount ){
			//this.objects = objects;
			this.limit = limit;
			this.next = next;
			this.offset = offset;
			this.previous = previous;
			this.totalCount = totalCount;
		}

		@Override
		public String toString() {
			// \"meta\": {\"limit\": 20, \"next\": null, \"offset\": 0, \"previous\": null, \"total_count\": 1}
			StringBuilder sb = new StringBuilder();
			sb.append("{\"limit\": " + limit);
			sb.append(", \"next\": " + next);
			sb.append(", \"offset\": " + offset);
			sb.append(", \"previous\": " + previous);
			sb.append(", \"total_count\": " + totalCount + "} ");
			return sb.toString();
		}
	}

	public final Meta meta;
	public final List<MeetingDetails> objects;

	public MeetingsMessage  (Meta meta, List<MeetingDetails> objects) {
		this.objects = objects;
		this.meta = meta;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");

		if (meta != null) {
			sb.append("\"meta\": " + meta);
		}
		if (objects != null) {
			sb.append(", \"objects\": [ ");
			String separator = "";
			for (MeetingDetails item : objects) {
				sb.append(separator);
				sb.append(item);
				separator = ", ";
			}
			sb.append("]");
		}
		sb.append("}");
		return sb.toString();
	}       

	public String firstShortMessage () {
		String value = "";
		if (objects != null) {
			if (objects.size() > 0) {
				MeetingDetails item = objects.get(0);
				if (item != null) {                                     
					value = item.name;
				}
			}
		}
		return value;
	}

}
