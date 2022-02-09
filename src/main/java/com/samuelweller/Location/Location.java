package com.samuelweller.Location;

import java.io.Serializable;

import com.samuelweller.JSONParsing.GoogleJSONMapper;

public class Location implements Serializable {

	private static final long serialVersionUID = 7648747914493611668L;
	
	private double lng;
	private double lat;
	private long timestamp;
	
	public Location(double lng, double lat, long timestamp) {
		this.lng = lng;
		this.lat = lat;
		this.timestamp = timestamp;
	}
	
	public Location(GoogleJSONMapper json) {
		this.lng = json.getLongitudeE7();
		this.lat = json.getLatitudeE7();
		this.timestamp = (long) json.getTimestampMs();
	}

	@Override
	public String toString() {
		return "Location [lng=" + lng + ", lat=" + lat + ", timestamp=" + timestamp + "]";
	}

	public long getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}


	public double getLng() {
		return lng;
	}


	public void setLng(double lng) {
		this.lng = lng;
	}


	public double getLat() {
		return lat;
	}


	public void setLat(double lat) {
		this.lat = lat;
	}
	
}
