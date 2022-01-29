package com.samuelweller.RESTController;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samuelweller.AWS.S3.AWSService;
import com.samuelweller.Location.KnownLocation;
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

	@GetMapping(path = "/nextLocation/{user}/{timestamp}")
	public Location getNextLocation(@PathVariable String user, @PathVariable long timestamp) {
		return AWS.getLocations(user).setCurrentLocation(timestamp).getNextLocation();
	}

	@GetMapping(path = "/nextDay/{user}/{timestamp}")
	public Location getNextDay(@PathVariable String user, @PathVariable long timestamp) {
		return AWS.getLocations(user).setCurrentLocation(timestamp).moveDay().getCurrentLocation();
	}
	
	// Mass return
	
	@GetMapping(path = "/getLocationsThisWeek/{timestamp}")
	public List<Location> getLocationsThisWeek(@PathVariable long timestamp) {
		// To do
		return null;
	}
	
	@GetMapping(path = "/getKnownLocations/{user}")
	public List<KnownLocation> getKnownLocations(@PathVariable String user) {
		return AWS.getKnownLocations(user);
	}
	
	// Posts
	
	@PostMapping(path = "/addKnownLocation/{user}/{name}/{lng}/{lat}")
	public void addKnownLocation(@PathVariable String user, @PathVariable String name, @PathVariable double lng, @PathVariable double lat) {
		AWS.addKnownLocation(user, new KnownLocation(user, lng, lat));
	}
	
	@PostMapping(path = "/removeKnownLocation/{user}/{name}/{lng}/{lat}")
	public void removeKnownLocation(@PathVariable String user, @PathVariable String name, @PathVariable double lng, @PathVariable double lat) {
		AWS.removeKnownLocation(user, new KnownLocation(user, lng, lat));
	}
	
}
