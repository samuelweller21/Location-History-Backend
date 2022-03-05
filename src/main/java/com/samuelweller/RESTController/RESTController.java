package com.samuelweller.RESTController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samuelweller.AWS.S3.AWSService;
import com.samuelweller.Location.KnownLocation;
import com.samuelweller.Location.Location;
import com.samuelweller.Location.Vacation;
import com.samuelweller.LocationService.DailySummary;
import com.samuelweller.LocationService.DailySummaryObj;
import com.samuelweller.LocationService.LL;
import com.samuelweller.jwt.AuthenticationRequest;
import com.samuelweller.jwt.AuthenticationResponse;
import com.samuelweller.jwt.JWTUtil;
import com.samuelweller.jwt.LHVUserDetailsService;

@RestController
@CrossOrigin(origins="http://localhost:4200")
@ComponentScan(basePackages = "com.samuelweller")
public class RESTController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	AWSService AWS;
	
	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private LHVUserDetailsService userDetailservice;
	
	@Autowired
	private JWTUtil jwtTokenUtil;
	
	// Authentication
	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
		
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				authenticationRequest.getUsername(), 
				authenticationRequest.getPassword()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Wrong username/password");
		}
		
		final UserDetails userDetails = userDetailservice
				.loadUserByUsername(authenticationRequest.getUsername());
		
		final String jwt = jwtTokenUtil.generateToken(userDetails);
		
		return ResponseEntity.ok(new AuthenticationResponse(jwt));
	}
	
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
	
	// Could easily cache
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
	
	@GetMapping(path = "/getAllLocations/{user}")
	public List<Location> getAllLocations(@PathVariable String user) {
		System.out.println("Returning all location ... this may take a while");
		return AWS.getAllLocations(user);
	}
	
//	@GetMapping(path = "/getCountries")
//	public List<String> getCountries() {
//		return CountriesService.getCountries();		
//	}
	
	@GetMapping(path = "/getVacations/{user}/{homeCountry}") 
	public List<Vacation> getVacations(@PathVariable String user, @PathVariable String homeCountry) {
		return AWS.buildVacations(user, homeCountry);
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
