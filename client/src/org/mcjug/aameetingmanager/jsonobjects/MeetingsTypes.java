package org.mcjug.aameetingmanager.jsonobjects;

import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class MeetingsTypes  {
	
	public class MeetingTypeDetails implements Comparable<MeetingTypeDetails> {

		public final String description;
		public final int id;
		@SerializedName("resource_uri")
		public final String resourceUri;
		@SerializedName("short_name")
		public final String shortName;
		public final String name;


		public MeetingTypeDetails (String description, int id, String message, String resourceUri, String shortName, String name) {
			this.description = description;
			this.id = id;
			this.resourceUri = resourceUri;
			this.shortName = shortName;
			this.name = name;
		}

		public int compareTo(MeetingTypeDetails compareTypeDetails) {
			return (this.name.compareTo(compareTypeDetails.name));
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append(String.format("\"description\": \"%s\", ", description));
			sb.append(String.format("\"id\": %s, ", id));
			sb.append(String.format("\"resource_uri\": \"%s\", ", resourceUri));
			sb.append(String.format("\"short_name\": \"%s\", ", shortName));
			sb.append(String.format("\"name\": \"%s\", ", name));
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
	public final List<MeetingTypeDetails> objects;

	public MeetingsTypes (Meta meta, List<MeetingTypeDetails> objects) {
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
			for (MeetingTypeDetails item : objects) {
				sb.append(separator);
				sb.append(item);
				separator = ", ";
			}
			sb.append("]");
		}
		sb.append("}");
		return sb.toString();
	}       

	public void sortList () {
		Collections.sort(objects);
	}
	
	public CharSequence[] allNames () {
		int arraySize = this.objects.size();
		CharSequence[] strArray = new CharSequence[arraySize];
		for (int i = 0 ; i < arraySize ; i++) {
			strArray[i] = objects.get(i).name;
		}
		return strArray;
	}
	
	public CharSequence[] allShortNames () {
		int arraySize = this.objects.size();
		CharSequence[] strArray = new CharSequence[arraySize];
		for (int i = 0 ; i < arraySize ; i++) {
			strArray[i] = objects.get(i).shortName;
		}
		return strArray;
	}

	public String firstShortMessage () {
		String value = "";
		if (objects != null) {
			if (objects.size() > 0) {
				MeetingTypeDetails item = objects.get(0);
				if (item != null) {                                     
					value = item.name;
				}
			}
		}
		return value;
	}
	
	

}
