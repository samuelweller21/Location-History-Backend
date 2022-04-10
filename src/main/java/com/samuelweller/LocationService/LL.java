package com.samuelweller.LocationService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;

import com.samuelweller.Distance.DS;
import com.samuelweller.Location.Location;
import com.samuelweller.Location.Vacation;
import com.samuelweller.Shapefiles.CountriesService;

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
	
	public Location getPreviousLocation(double..._distance) {
		
		// Optional parameters - number of advancements
		double distance = _distance.length > 0 ? _distance[0] : this.NEW_LOCATION_DIST;
		
		// Set current index
		int new_index = this.getCurrentIndex() - 1;
		
		// Increment until we get to a new location outside distance
		while (DS.getDistance(this.locations.get(new_index), current_location) < distance) {
			new_index--;
		}
		
		return this.locations.get(new_index);
		
	}
	
	public LL getLocationsThisWeek() {
		//To do
		return null;
	}
	
	public Date getFirstDate() {
		System.out.println("Got first date request");
		return (Date.from(Instant.ofEpochMilli(1000*this.locations.get(0).getTimestamp())));
	}
	
	public Date getLastDate() {
		System.out.println("Got last date request");
		return (Date.from(Instant.ofEpochMilli(1000*this.locations.get(this.locations.size()-1).getTimestamp())));
	}
	
	// Chain Methods
	
	public LL setCurrentLocation(long timestamp) {
		// To do: Implement a binary search
		this.current_location = this.locations.stream().filter(l -> l.getTimestamp() == timestamp).findAny().get();
		return this.clone();
	}
	
	public LL getAllLocationsOnDate(Date date) {
		
		// First clone LL object
		LL ll = this.clone();
		
		LocalDate ldate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		long day_before = ldate.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
		long day_after = ldate.atStartOfDay().plusDays(1L).toEpochSecond(ZoneOffset.UTC);
		
		// Filter locations
		ll.locations = ll.locations.stream().filter(l -> l.getTimestamp() > day_before && l.getTimestamp() < day_after).collect(Collectors.toList());
	
		return ll;
	}
	
	public List<Location> getAllLocations() {
		List<Location> list = new ArrayList();
		for (int i = 0; i < this.getLocations().size(); i++) {
			boolean toAdd = true;
			Location current = this.getLocations().get(i);
			for (int j = 0; j < list.size(); j++) {
				if (DS.getDistance(list.get(j), current) < 100) {
					toAdd = false;
				}
			}
			if (toAdd) {
				list.add(current);
			}
		}
		return list;
	}
	
	public String getCurrentCountry() {
		return CountriesService.getCountry(this.current_location);
	}
	
	public Location getFirstLocationOnDate(Date date) {
		
		// First clone LL object
		LL ll = this.clone();
		
		LocalDate ldate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		long day_before = ldate.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
		
		// Filter locations
		Optional<Location> first = ll.locations.stream().filter(l -> l.getTimestamp() > day_before).findFirst(); 
		System.out.println(first.get());
		if (first.isPresent()) {
			return first.get();
		} else {
			System.out.println("There's a null");
			return null;
		}
	}
	
public List<Location> getLocationsFromTo(Date startDate, Date endDate) {
		
		// First clone LL object
		LL ll = this.clone();
		
		LocalDate ldate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		long startTime = ldate.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
		
		LocalDate edate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		long endTime = edate.plusDays(1L).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
		
		
		// Filter locations
		List<Location> all = ll.locations.stream().filter(l -> l.getTimestamp() > startTime && l.getTimestamp() < endTime).collect(Collectors.toList()); 
		
		return all;
	}
	
	public LL getAllLocationsOnCurrentDay() {
		
		LL ll = this.clone();
		
		//  FIX!
		
		// Get start and end timestamps
		Date temp_date = Date.from(Instant.ofEpochMilli(current_location.getTimestamp()));
		long day_before = temp_date.getTime()/1000;
		System.out.println(day_before);
		long day_after = DateUtils.addDays(temp_date, 1).getTime()/1000;
		
		// Filter locations
		ll.locations = ll.locations.stream().filter(l -> l.getTimestamp() > day_before && l.getTimestamp() < day_after).collect(Collectors.toList());
		
		return ll;
	}
	
	public LL getAllLocationsWithinXDays(int x) {
		
		LL ll = this.clone();
		
		// Get start and end timestamps
		Date temp_day = Date.from(Instant.ofEpochMilli(this.current_location.getTimestamp()));
		long day_before = DateUtils.addDays(temp_day, -x).getTime();
		long day_after = DateUtils.addDays(temp_day, x).getTime();
		
		// Filter locations
		ll.locations = ll.locations.stream().filter(l -> l.getTimestamp() > day_before && l.getTimestamp() < day_after).collect(Collectors.toList());
		
		return ll;
	}

	//To Do: Get all locations this week/month/year
	
	@Override
	public String toString() {
		
		if (this.locations.size() > 100) {
			return "LL too long";
		} else {
			return "LL [NEW_LOCATION_DIST=" + NEW_LOCATION_DIST + ", locations=" + locations + ", current_location="
				+ current_location + "]";
		}
	}

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
		
		// Start of next/nth day
		Date temp_day = Date.from(Instant.ofEpochMilli(1000*this.current_location.getTimestamp()));
		LocalDate ldate = temp_day.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		long day_before = ldate.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
		Date temp_day2 = Date.from(Instant.ofEpochMilli(day_before));
		long moved_day = temp_day2.getTime() + ((long) (n*60*60*24));
		
		// To do: What to do if it runs into boundaries ? Surely not throw error? Maybe print warning but just carry on 'as best as I can'
		
		this.current_location = this.locations.stream().filter(l -> l.getTimestamp() > moved_day).findFirst().get();
				
		System.out.println(this.current_location.getTimestamp());
		
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
