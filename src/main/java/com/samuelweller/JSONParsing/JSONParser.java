package com.samuelweller.JSONParsing;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samuelweller.Distance.DS;
import com.samuelweller.Location.Location;
import com.samuelweller.config.ApplicationConfig;

public class JSONParser {

	public static List<Location> parse(String rawJson) {
		
		// Parse json
		
		JSONObject json = new JSONObject(rawJson);
		JSONArray jsonArray = json.getJSONArray("locations");
		
		// Could potentially improve this by doing together ??
		
		// Map to objects
		
		List<GoogleJSONMapper> parsedJson = null;
		
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
	    	parsedJson = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<GoogleJSONMapper>>(){});
	    } catch (Exception e) {
	    	System.out.println("Probably failed conversion - Unsure how");
	    	e.printStackTrace();
	    }
	    
	    // Clean raw data
	    
	    return JSONParser.clean(parsedJson);
		
	}
	
	public static List<Location> clean(List<GoogleJSONMapper> raw) {
		
		// Ensure all accuracy is within application config value and format timestamp and geo values
		
		List<GoogleJSONMapper> clean = raw.stream()
				.filter(o -> o.getAccuracy() < ApplicationConfig.DEFAULT_MINIMUM_ACCURACY)
				.map(o -> o.divideGeoAndTimestamp())
				.collect(Collectors.toList());
		
		// It's possible a user might have multiple devices.  For now, return all from the one which has the most location data
		
		List<Long> ids = clean.stream().map(o -> o.getDeviceTag()).distinct().collect(Collectors.toList());
		List<Long> counts = new ArrayList(); 
		
		for (int i = 0; i < ids.size(); i++) {
			long id = ids.get(i);
			counts.add(clean.stream().filter(o -> o.getDeviceTag() == id).count());
		}
		
		Long max_count = counts.stream().max((i,j) -> i.compareTo(j)).get();
		long max_id = ids.get(counts.indexOf(max_count));
		
		clean = clean.stream()
				.filter(o -> o.getDeviceTag() == max_id)
				.collect(Collectors.toList());
		
		// Remove values when the user is just in one location e.g. asleep
		
		double prev = 0;
		List<Location> locations = new ArrayList();
		locations.add(new Location(clean.get(0)));
		
		for (int i = 1; i < (clean.size()-1); i++) {
			
			// If new day - add
			
			Date date = Date.from(Instant.ofEpochMilli(locations.get(locations.size()-1).getTimestamp()*1000));
			LocalDate ldate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			long day_before = ldate.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
			long day_after = ldate.atStartOfDay().plusDays(1L).toEpochSecond(ZoneOffset.UTC);
			
//			System.out.println("TS: " + clean.get(i).getTimestampMs());
//			System.out.println("Day after: " + day_after);
			
			if (clean.get(i).getTimestampMs() > day_after) {
				locations.add(new Location(clean.get(i)));
			}
			
			// Location is further than app min distance
			
			if (DS.getDistance(clean.get(i+1).getLatitudeE7(), 
					locations.get(locations.size()-1).getLat(), 
					clean.get(i+1).getLongitudeE7(), 
					locations.get(locations.size()-1).getLng(), 0, 0) > ApplicationConfig.DEFAULT_MINIMUM_NEW_LOCATION_DIST) {
				
				// If we have moved since the last then also add the last timestamp from the previous place
				
				if (locations.get(locations.size()-1).getTimestamp() != (long) clean.get(i).getTimestampMs()) {
					locations.add(new Location(clean.get(i)));
				}
				
				// Otherwise just add the next value
				
				locations.add(new Location(clean.get(i+1)));
			}
			
		}
		
		return locations;
	}
	
}
