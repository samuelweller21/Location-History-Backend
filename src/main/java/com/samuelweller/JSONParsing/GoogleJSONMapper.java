package com.samuelweller.JSONParsing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleJSONMapper {

	private long deviceTag;
	private int accuracy;
	private double timestampMs;
	private double latitudeE7;
	private double longitudeE7;
	private String source;
	
	@Override
	public String toString() {
		return "GoogleJSONMapper [deviceTag=" + deviceTag + ", accuracy=" + accuracy
				+ ", timestampMs=" + timestampMs + ", latitudeE7=" + latitudeE7 + ", longitudeE7=" + longitudeE7
				+ ", source=" + source + "]";
	}
	
	public GoogleJSONMapper divideGeoAndTimestamp() {
		this.latitudeE7 = this.latitudeE7/10000000;
		this.longitudeE7 = this.longitudeE7/10000000;
		this.timestampMs = this.timestampMs/1000;
		return this;
	}

	public GoogleJSONMapper() {
		
	}

	public long getDeviceTag() {
		return deviceTag;
	}

	public void setDeviceTag(long deviceTag) {
		this.deviceTag = deviceTag;
	}

	public int getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}

	public double getTimestampMs() {
		return timestampMs;
	}

	public void setTimestampMs(double timestampMS) {
		this.timestampMs = timestampMS;
	}

	public double getLatitudeE7() {
		return latitudeE7;
	}

	public void setLatitudeE7(double latitudeE7) {
		this.latitudeE7 = latitudeE7;
	}

	public double getLongitudeE7() {
		return longitudeE7;
	}

	public void setLongitudeE7(double longitudeE7) {
		this.longitudeE7 = longitudeE7;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
}
