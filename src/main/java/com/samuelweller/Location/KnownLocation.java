package com.samuelweller.Location;

import java.io.Serializable;

public class KnownLocation implements Serializable {

	private String name, description;;
	private double lng, lat;
	
	public KnownLocation(String name, double lng, double lat) {
		super();
		this.name = name;
		this.lng = lng;
		this.lat = lat;
	}
	
	public KnownLocation(String name, String description, double lat, double lng) {
		super();
		this.name = name;
		this.description = description;
		this.lng = lng;
		this.lat = lat;
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
