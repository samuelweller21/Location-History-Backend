package com.samuelweller.Location;

import java.io.Serializable;

public class KnownLocation implements Serializable {

	private static final long serialVersionUID = 1112434620260534679L;
	private String name, description;;
	private double lng, lat, radius;
	
	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public KnownLocation(String name, double lng, double lat, double radius) {
		super();
		this.name = name;
		this.lng = lng;
		this.lat = lat;
		this.radius = radius;
	}
	
	public KnownLocation(String name, double lng, double lat, double radius, String description) {
		super();
		this.name = name;
		this.lng = lng;
		this.lat = lat;
		this.radius = radius;
		this.description = description;
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

	@Override
	public String toString() {
		return "KnownLocation [name=" + name + ", description=" + description + ", lng=" + lng + ", lat=" + lat
				+ ", radius=" + radius + "]";
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
