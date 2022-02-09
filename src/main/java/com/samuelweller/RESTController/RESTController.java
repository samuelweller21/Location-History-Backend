package com.samuelweller.RESTController;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	
	@PostMapping(path = "/addKnownLocation/{user}/{name}/{lat}/{lng}/{radius}")
	public void addKnownLocation(@PathVariable String user, @PathVariable String name, 
			@PathVariable double lat, 
			@PathVariable double lng,
			@PathVariable double radius) {
		AWS.addKnownLocation(user, new KnownLocation(name, lng, lat, radius));
		System.out.println("Created " + name + " in " + user);
	}
	
	@PostMapping(path = "/removeKnownLocation/{user}/{name}")
	public ResponseEntity<String> removeKnownLocation(@PathVariable String user, @PathVariable String name) {
		List<KnownLocation> kl = AWS.getKnownLocations(user);
		KnownLocation toDelete = kl.stream().filter(loc -> loc.getName().equals(name)).findFirst().get();
		System.out.println(toDelete);
		AWS.removeKnownLocation(user, toDelete);
		System.out.println("Deleted " + toDelete + " from " + user);
		return ResponseEntity.status(HttpStatus.OK).body("Deleted object");
	}
	
}
