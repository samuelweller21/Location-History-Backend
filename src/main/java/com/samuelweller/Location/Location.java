package com.samuelweller.Location;

import java.io.Serializable;

import com.samuelweller.JSONParsing.GoogleJSONMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Location implements Serializable {

	private static final long serialVersionUID = 7648747914493611668L;
	
	private double lng;
	private double lat;
	private long timestamp;
	private double accuracy;

	public Location(double lng, double lat, long timestamp) {
		this.lng = lng;
		this.lat = lat;
		this.timestamp = timestamp;
	}
	
	public Location(KnownLocation kl) {
		this.lng = kl.getLng();
		this.lat = kl.getLat();
	}
	
	public Location(GoogleJSONMapper json) {
		this.lng = json.getLongitudeE7();
		this.lat = json.getLatitudeE7();
		this.accuracy = json.getAccuracy();
		this.timestamp = (long) json.getTimestampMs();
	}
	
}
