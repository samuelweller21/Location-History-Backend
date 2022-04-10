package com.samuelweller.Location;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class KnownLocation implements Serializable {

	private static final long serialVersionUID = 1112434620260534679L;
	private String name, description;
	private double lng, lat, radius;

	public KnownLocation(String name, double lng, double lat, double radius) {
		super();
		this.name = name;
		this.lng = lng;
		this.lat = lat;
		this.radius = radius;
	}
	
	public KnownLocation(String name, String description, double lat, double lng) {
		super();
		this.name = name;
		this.description = description;
		this.lng = lng;
		this.lat = lat;
	}
	
}
