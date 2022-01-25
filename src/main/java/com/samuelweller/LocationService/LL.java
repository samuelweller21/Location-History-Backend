package com.samuelweller.LocationService;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;

import com.samuelweller.Distance.DS;
import com.samuelweller.Location.Location;

//Location List
public class LL {
	
	public final double NEW_LOCATION_DIST = 50; //metres
	
	// To do: Add unit tests
	
	private List<Location> locations;
	private Location current_location;
	
	// Constructors

	public LL(List<Location> locations) {
		this.locations = locations;
	}
	
	public LL(List<Location> locations, Location current_location) {
		this.locations = locations;
		this.current_location = current_location;
	}
	
	// Clone method
	
	public LL clone() {
		return new LL(this.locations, this.current_location);
	}
	
	// Getters
	
	public List<Location> getLocations() {
		return this.locations;
	}
	
	public Location getCurrentLocation() {
		return this.current_location;
	}
	
	public int getCurrentIndex() {
		// Could do a binary search
		return this.locations.indexOf(this.current_location);
	}
	
	public Location getNextLocation(double..._distance) {
		
		// Optional parameters - number of advancements
		double distance = _distance.length > 0 ? _distance[0] : this.NEW_LOCATION_DIST;
		
		// Set current index
		int new_index = this.getCurrentIndex() + 1;
		
		// Increment until we get to a new location outside distance
		while (DS.getDistance(this.locations.get(new_index), current_location) < distance) {
			new_index++;
		}
		
		return this.locations.get(new_index);
		
	}
	
	public LL getLocationsThisWeek() {
		//To do
		return null;
	}
	
	// Chain Methods
	
	public LL setCurrentLocation(long timestamp) {
		// To do: Implement a binary search
		this.current_location = this.locations.stream().filter(l -> l.getTimestamp() == timestamp).findAny().get();
		return this.clone();
	}
	
	public LL getAllLocationsOnCurrentDay() {
		
		// Get start and end timestamps
		Date temp_date = Date.from(Instant.ofEpochMilli(current_location.getTimestamp()));
		long day_before = temp_date.getTime();
		long day_after = DateUtils.addDays(temp_date, 1).getTime();
		
		// Filter locations
		this.locations = this.locations.stream().filter(l -> l.getTimestamp() > day_before && l.getTimestamp() < day_after).collect(Collectors.toList());
		
		return this.clone();
	}
	
	public LL getAllLocationsWithinXDays(int x) {
		// Get start and end timestamps
		Date temp_day = Date.from(Instant.ofEpochMilli(this.current_location.getTimestamp()));
		long day_before = DateUtils.addDays(temp_day, -x).getTime();
		long day_after = DateUtils.addDays(temp_day, x).getTime();
		
		// Filter locations
		this.locations = this.locations.stream().filter(l -> l.getTimestamp() > day_before && l.getTimestamp() < day_after).collect(Collectors.toList());
		
		return this;
	}

	//To Do: Get all locations this week/month/year
	
	public LL moveCurrentLocation(int..._n) {
		
		// Optional parameters - number of advancements
		int n = _n.length > 0 ? _n[0] : 1;

		// To do: What to do if it runs into boundaries ? Surely not throw error? Maybe print warning but jusy carry on 'as best as I can'
		
		// Advance location
		this.current_location = this.locations.get(this.getCurrentIndex() + n);
		
		return this.clone();
	}
	
	public LL moveDay(int..._n) {

		// Optional parameters - number of advancements
		int n = _n.length > 0 ? _n[0] : 1;
		
		// Get start and end timestamps
		Date temp_day = Date.from(Instant.ofEpochMilli(this.current_location.getTimestamp()));
		long moved_day = DateUtils.addDays(temp_day, n).getTime();
		
		// To do: What to do if it runs into boundaries ? Surely not throw error? Maybe print warning but just carry on 'as best as I can'
		
		this.current_location = this.locations.stream().filter(l -> l.getTimestamp() > moved_day).findFirst().get();
				
		return this.clone();
	}
	
	public LL moveNewLocation(double..._distance) {
		
		// Optional parameters - number of advancements
		double distance = _distance.length > 0 ? _distance[0] : this.NEW_LOCATION_DIST;
		
		// Set current index
		int new_index = this.getCurrentIndex() + 1;
		
		// Increment until we get to a new location outside distance
		while (DS.getDistance(this.locations.get(new_index), current_location) < distance) {
			new_index++;
		}
		
		this.current_location = this.locations.get(new_index);
		
		return this.clone();
		
	}
	
	
}
