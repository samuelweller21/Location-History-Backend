package com.samuelweller.RESTController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samuelweller.AWS.S3.AWSService;
import com.samuelweller.Location.KnownLocation;
import com.samuelweller.Location.Location;
import com.samuelweller.LocationService.DailySummary;
import com.samuelweller.LocationService.DailySummaryObj;
import com.samuelweller.LocationService.LL;

@RestController
@CrossOrigin(origins="http://localhost:4200")
public class RESTController {
	
	@Autowired
	AWSService AWS;
	
	ObjectMapper mapper = new ObjectMapper();
	
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
	
	@GetMapping(path = "/previousLocation/{user}/{timestamp}")
	public Location getPreviousLocation(@PathVariable String user, @PathVariable long timestamp) {
		return AWS.getLocations(user).setCurrentLocation(timestamp).getPreviousLocation();
	}

	@GetMapping(path = "/previousDay/{user}/{timestamp}")
	public Location getPreviousDay(@PathVariable String user, @PathVariable long timestamp) {
		System.out.println(AWS.getLocations(user).setCurrentLocation(timestamp).getAllLocationsOnCurrentDay());
		return AWS.getLocations(user).setCurrentLocation(timestamp).moveDay(-1).getCurrentLocation();
	}
	
	@GetMapping(path = "/firstDate/{user}")
	public Date getFirstDate(@PathVariable String user) {
		return AWS.getLocations(user).getFirstDate();
	}
	
	@GetMapping(path = "/lastDate/{user}")
	public Date getLastDate(@PathVariable String user) {
		return AWS.getLocations(user).getLastDate();
	}
	
	@GetMapping(path = "/firstLastDates/{user}")
	public List<String> getFirstLastDates(@PathVariable String user) {
		List<String> dates = new ArrayList<String>();
		dates.add(new SimpleDateFormat("yyyy-MM-dd").format(AWS.getLocations(user).getFirstDate()));
		dates.add(new SimpleDateFormat("yyyy-MM-dd").format(AWS.getLocations(user).getLastDate()));
		System.out.println(dates);
		return dates;
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
	
	@GetMapping(path = "/getDailySummary/{user}/{date}")
	public List<DailySummaryObj> getDailySummary(@PathVariable String user, @PathVariable Date date) {
		System.out.println("##############################");
		System.out.println("Receieved daily summary request:");
		LL locsOnDay = AWS.getLocations(user);
		List<KnownLocation> kls = AWS.getKnownLocations(user);
		List<DailySummaryObj> ds = DailySummary.getDailySummary(locsOnDay, kls, date);
		System.out.println("##############################");
		return ds;
	}
	
	@GetMapping(path = "/getColours/{user}")
	public List<Boolean> getColours(@PathVariable String user) {
		System.out.println("Getting colours");
		List<Date> datesInRange = new ArrayList<>();
		LL locations = AWS.getLocations(user);
		Calendar calendar = getCalendarWithoutTime(locations.getFirstDate());
		Calendar endCalendar = getCalendarWithoutTime(locations.getLastDate());

		while (calendar.before(endCalendar)) {
			Date result = calendar.getTime();
			datesInRange.add(result);
			calendar.add(Calendar.DATE, 1);
		}
		
		List<Boolean> colours = new ArrayList();

		for (int i = 0; i < datesInRange.size(); i++) {
			colours.add((locations.getAllLocationsOnDate(datesInRange.get(i)).getLocations().size() != 0) ? true : false);
		}
		
		return colours;
	}
	
	//Util for above
	
	private static Calendar getCalendarWithoutTime(Date date) {
		  Calendar calendar = new GregorianCalendar();
		  calendar.setTime(date);
		  calendar.set(Calendar.HOUR, 0);
		  calendar.set(Calendar.HOUR_OF_DAY, 0);
		  calendar.set(Calendar.MINUTE, 0);
		  calendar.set(Calendar.SECOND, 0);
		  calendar.set(Calendar.MILLISECOND, 0);
		  return calendar;
		}
	
	// Posts
	
	@PostMapping(path = "/getLocationOnDate/{user}")
	public Location getLocationOnDate(@PathVariable String user, @RequestBody String json) {
		System.out.println("##############################");
		System.out.println("Receieved get location on date request:");
		
		// Reverse as date reads in american date
		String strDate = json.substring(12,15) + json.substring(9,12) + json.substring(15,19);
		Date date = new Date(strDate);
		System.out.println("##############################");
		return AWS.getLocations(user).getFirstLocationOnDate(date);
	}
	
	@PostMapping(path = "/getLocationsOnDate/{user}")
	public List<Location> getLocationsOnDate(@PathVariable String user, @RequestBody String json) {
		System.out.println("##############################");
		System.out.println("Receieved get locations on date request:");
		
		// Reverse as date reads in american date
		String strDate = json.substring(12,15) + json.substring(9,12) + json.substring(15,19);
		Date date = new Date(strDate);
		System.out.println("##############################");
		return AWS.getLocations(user).getAllLocationsOnDate(date).getLocations();
	}
	
	@PostMapping(path = "/addKnownLocation/{user}/{name}/{lat}/{lng}/{radius}/{description}")
	public void addKnownLocation(@PathVariable String user, @PathVariable String name,
			@PathVariable double lat, 
			@PathVariable double lng,
			@PathVariable double radius,
			@PathVariable String description) {
		AWS.addKnownLocation(user, new KnownLocation(name, lng, lat, radius, description));
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
