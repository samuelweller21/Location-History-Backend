package com.samuelweller.RESTController;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.samuelweller.AWS.S3.AWSService;
import com.samuelweller.Location.Location;

@RestController
@CrossOrigin(origins="http://localhost:4200")
public class RESTController {
	
	@Autowired
	AWSService AWS;
	
	// Movement
	
	@GetMapping(path = "/location")
	public Location getLocation() {
		return AWS.getLocations("sweller").getLocations().get(1000);
	}

	@GetMapping(path = "/nextLocation/{timestamp}")
	public Location getNextLocation(@PathVariable long timestamp) {
		return AWS.getLocations("sweller").setCurrentLocation(timestamp).getNextLocation();
	}

	@GetMapping(path = "/nextDay/{timestamp}")
	public Location getNextDay(@PathVariable long timestamp) {
		return AWS.getLocations("sweller").setCurrentLocation(timestamp).moveDay().getCurrentLocation();
	}
	
	// Mass return
	
	@GetMapping(path = "/getLocationsThisWeek/{timestamp}")
	public ArrayList<Location> getLocationsThisWeek(@PathVariable long timestamp) {
		// To do
		return null;
	}
}
