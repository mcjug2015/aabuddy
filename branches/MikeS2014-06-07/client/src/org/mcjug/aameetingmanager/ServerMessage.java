package org.mcjug.aameetingmanager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.gson.annotations.SerializedName;

public class ServerMessage {

	public class ServerMessageItem {

		@SerializedName("created_date")
		public final Date createdDate;
		public final int id;
		@SerializedName("is_active")
		public final boolean isActive;
		public final String message;
		@SerializedName("resource_uri")
		public final String resourceUri;
		@SerializedName("short_message")
		public final String shortMessage;
		@SerializedName("updated_date")
		public final Date updatedDate;

		public ServerMessageItem (Date createdDate, int id, boolean isActive, String message, String resourceUri, String shortMessage, Date updatedDate) {
			this.createdDate = createdDate;
			this.id = id;
			this.isActive = isActive;
			this.message = message;
			this.resourceUri = resourceUri;
			this.shortMessage = shortMessage;
			this.updatedDate = updatedDate;
		}

		@Override
		public String toString() {
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append(String.format("\"created_date\": \"%s\", ", dateformat.format(createdDate)));
			sb.append(String.format("\"id\": %s, ", id));
			sb.append(String.format("\"is_active\": %b, ", isActive));
			sb.append(String.format("\"message\": \"%s\", ", message));
			sb.append(String.format("\"resource_uri\": \"%s\", ", resourceUri));
			sb.append(String.format("\"short_message\": \"%s\", ", shortMessage));
			sb.append(String.format("\"updated_date\": \"%s\"", dateformat.format(updatedDate)));
			sb.append('}');
			return sb.toString();
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
			sb.append(", \"total_count\": " + totalCount + "}");
			return sb.toString();
		}		
	}

	public final Meta meta;
	public final List<ServerMessageItem> objects;

	public ServerMessage (Meta meta, List<ServerMessageItem> objects) {
		this.objects = objects;
		this.meta = meta;
	}

	public String firstShortMessage () {
		String value = "";
		if (objects != null) {
			if (objects.size() > 0) {
				ServerMessageItem item = objects.get(0);
				if (item != null) {					
					return (item.shortMessage);
				}
			}
		}
		return value;
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
			for (ServerMessageItem item : objects) {
				sb.append(separator);
				sb.append(item);
				separator = ", ";
			}
			sb.append("]");
		}
		sb.append("}");
		return sb.toString();
	}	

}
