package com.samuelweller.RESTController;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.RequestHeader;
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
import com.samuelweller.Mail.EmailServiceImpl;
import com.samuelweller.UserManagement.DBUser;
import com.samuelweller.UserManagement.UserRepoImpl;
import com.samuelweller.jwt.AuthenticationRequest;
import com.samuelweller.jwt.AuthenticationResponse;
import com.samuelweller.jwt.JWTUtil;
import com.samuelweller.jwt.LHVUserDetailsService;

@RestController
@CrossOrigin(origins="http://localhost:4200")
@ComponentScan(basePackages = "com.samuelweller")
public class RESTController {
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	AWSService AWS;
	
	@Autowired
	UserRepoImpl userRepo;
	
	ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private LHVUserDetailsService userDetailservice;
	
	@Autowired
	private JWTUtil jwtTokenUtil;
	
	@Autowired
	EmailServiceImpl ES;
	
	@Value("${domain.address}")
	String domainAddress;
	
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
	
	@RequestMapping(value = "/createUser", method = RequestMethod.POST)
	public ResponseEntity<?> createUser(@RequestBody String raw) throws Exception {
		
		JSONObject json = new JSONObject(raw);
		
		String jwt = json.getString("jwt");
		if (jwtTokenUtil.isTokenExpired(jwt)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Link expired");
		} else if (!jwtTokenUtil.extractUsername(jwt).equals(json.getString("username"))) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usernames did not match");
		}
		
		DBUser user = new DBUser(json.getString("username"), json.getString("password"));
		
		if (userRepo.addUser(user)) {
			return ResponseEntity.ok(new AuthenticationResponse("Created user"));
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Could not create user - you probably already have an account using that email");
		}
		
	}
	
	@RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
	public ResponseEntity<?> updatePassword(@RequestBody String raw) throws Exception {
		
		JSONObject json = new JSONObject(raw);
		
		String jwt = json.getString("jwt");
		if (jwtTokenUtil.isTokenExpired(jwt)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Link expired");
		} else if (!jwtTokenUtil.extractUsername(jwt).equals(json.getString("username"))) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usernames did not match");
		}
		
		//Check if they already have an account
		if (userRepo.findUser(json.getString("username")).size() == 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You do not have an account");
		}
		
		DBUser user = new DBUser(json.getString("username"), json.getString("password"));
		
		userRepo.deleteUser(json.getString("username"));
		
		if (userRepo.addUser(user)) {
			return ResponseEntity.ok(new AuthenticationResponse("Changed password"));
		} else {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Could not change password for some reason");
		}
		
	}
	
	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public ResponseEntity<?> test(@RequestHeader("Authorization") String jwt) {
		return ResponseEntity.ok(new AuthenticationResponse("Ok"));
	}
	
	@RequestMapping(value = "/deleteUser", method = RequestMethod.POST)
	public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String jwt) throws Exception {
		
		String username = jwtTokenUtil.extractUsername(jwt.substring(7));
		
		userRepo.deleteUser(username);
		
		return ResponseEntity.ok(new AuthenticationResponse("Deleted user - " + username));
		
	}
	
	@RequestMapping(value = "/createUserGetJWT", method = RequestMethod.POST)
	public ResponseEntity<String> createUserGetJWT(@RequestBody String raw) {
		System.out.println(raw);
		JSONObject json = new JSONObject(raw);
		System.out.println(json.getString("email"));
		
		//Check if they already have an account
		if (userRepo.findUser(json.getString("email")).size() > 0) {
			System.out.println("User already exists");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You already have an account");
		}
		
		System.out.println(jwtTokenUtil.createCreateAccountToken(json.getString("email")));
		
		System.out.println(domainAddress);
		
		// Send email
		ES.sendSimpleMessage(json.getString("email"), "Welcome to LHV", "Hi there,\n\nThanks for joining us! To finish creating your account please go "
				+ "to this link:\n\n" + domainAddress + "/emailconfirmation/" + jwtTokenUtil.createCreateAccountToken(json.getString("email")) + "\n\nThe LHV Team");
		
		System.out.println(jwtTokenUtil.createCreateAccountToken(json.getString("email")));
		
		
		return ResponseEntity.status(HttpStatus.OK).body("Check your email");
	}
		
	@RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
	public ResponseEntity<String> resetPassword(@RequestBody String raw) {
		System.out.println(raw);
		JSONObject json = new JSONObject(raw);
		System.out.println(json.getString("email"));
		
		//Check if they already have an account
		if (userRepo.findUser(json.getString("email")).size() == 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You do not have an account yet");
		}
		
		System.out.println(jwtTokenUtil.createCreateAccountToken(json.getString("email")));
		
		System.out.println(domainAddress);
		
		// Send email
		ES.sendSimpleMessage(json.getString("email"), "LHV Password Reset", "Hi there,\n\nYou've requested a password reset. Head to the link below to change your password "
				+ "to this link:\n\n" + domainAddress + "/resetPassword/" + jwtTokenUtil.createCreateAccountToken(json.getString("email")) + "\n\nThe LHV Team");
		
		System.out.println(jwtTokenUtil.createCreateAccountToken(json.getString("email")));
		
		
		return ResponseEntity.status(HttpStatus.OK).body("Check your email");
	}
	
	
	@RequestMapping(value = "/createUserGetUsername", method = RequestMethod.POST)
	public ResponseEntity<String> createUserGetUsername(@RequestBody String raw) {
		JSONObject json = new JSONObject(raw);
		System.out.println("Username: " + jwtTokenUtil.extractUsername(json.getString("jwt")));
		if (jwtTokenUtil.isTokenExpired(json.getString("jwt"))) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sorry, that wasn't valid");
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(jwtTokenUtil.extractUsername(json.getString("jwt")));
		}
	}
	
	@GetMapping(value = "/getUsername")
	public String getUsername(@RequestHeader("Authorization") String jwt) throws Exception {
		return jwtTokenUtil.extractUsername(jwt.substring(7));
		
	}
	
	// Movement
	
	@GetMapping(path = "/location")
	public Location getLocation(@RequestHeader("Authorization") String jwt) {
		String username = jwtTokenUtil.extractUsername(jwt.substring(7));
		return AWS.getLocations(username).getLocations().get(1000);
	}

	@GetMapping(path = "/nextLocation/{timestamp}")
	public Location getNextLocation(@RequestHeader("Authorization") String jwt, @PathVariable long timestamp) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		return AWS.getLocations(user).setCurrentLocation(timestamp).getNextLocation();
	}

	@GetMapping(path = "/nextDay/{timestamp}")
	public Location getNextDay(@PathVariable long timestamp, @RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		return AWS.getLocations(user).setCurrentLocation(timestamp).moveDay().getCurrentLocation();
	}
	
	@GetMapping(path = "/previousLocation/{timestamp}")
	public Location getPreviousLocation( @PathVariable long timestamp, @RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		return AWS.getLocations(user).setCurrentLocation(timestamp).getPreviousLocation();
	}

	@GetMapping(path = "/previousDay/{timestamp}")
	public Location getPreviousDay(@PathVariable long timestamp, @RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		System.out.println(AWS.getLocations(user).setCurrentLocation(timestamp).getAllLocationsOnCurrentDay());
		return AWS.getLocations(user).setCurrentLocation(timestamp).moveDay(-1).getCurrentLocation();
	}
	
	@GetMapping(path = "/firstDate")
	public Date getFirstDate( @RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		return AWS.getLocations(user).getFirstDate();
	}
	
	@GetMapping(path = "/lastDate")
	public Date getLastDate(@RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		return AWS.getLocations(user).getLastDate();
	}
	
	@GetMapping(path = "/firstLastDates")
	public List<String> getFirstLastDates(@RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		List<String> dates = new ArrayList<String>();
		dates.add(new SimpleDateFormat("yyyy-MM-dd").format(AWS.getLocations(user).getFirstDate()));
		dates.add(new SimpleDateFormat("yyyy-MM-dd").format(AWS.getLocations(user).getLastDate()));
		System.out.println(dates);
		return dates;
	}
	
	// Mass return
	
	@GetMapping(path = "/getLocationsThisWeek/{timestamp}")
	public List<Location> getLocationsThisWeek(@PathVariable long timestamp, @RequestHeader("Authorization") String jwt) {
		// To do
		return null;
	}
	
	@GetMapping(path = "/getKnownLocations")
	public List<KnownLocation> getKnownLocations(@RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		return AWS.getKnownLocations(user);
	}
	
	@GetMapping(path = "/getDailySummary/{date}")
	public List<DailySummaryObj> getDailySummary(@PathVariable Date date, @RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		System.out.println("##############################");
		System.out.println("Receieved daily summary request:");
		LL locsOnDay = AWS.getLocations(user);
		List<KnownLocation> kls = AWS.getKnownLocations(user);
		List<DailySummaryObj> ds = DailySummary.getDailySummary(locsOnDay, kls, date);
		System.out.println("##############################");
		return ds;
	}
	
	// Could easily cache
	@GetMapping(path = "/getColours")
	public List<Boolean> getColours(@RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		System.out.println(user);
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
	
	@GetMapping(path = "/getAllLocations")
	public List<Location> getAllLocations(@RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		System.out.println("Returning all location ... this may take a while");
		return AWS.getAllLocations(user);
	}
	
//	@GetMapping(path = "/getCountries")
//	public List<String> getCountries() {
//		return CountriesService.getCountries();		
//	}
	
	@GetMapping(path = "/getVacations/{homeCountry}") 
	public List<Vacation> getVacations(@PathVariable String homeCountry, @RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
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
	
	@PostMapping(path = "/getLocationOnDate")
	public Location getLocationOnDate(@RequestBody String json, @RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		System.out.println("##############################");
		System.out.println("Receieved get location on date request:");
		
		// Reverse as date reads in american date
		String strDate = json.substring(12,15) + json.substring(9,12) + json.substring(15,19);
		Date date = new Date(strDate);
		System.out.println(json);
		System.out.println(strDate);
		System.out.println("##############################");
		return AWS.getLocations(user).getFirstLocationOnDate(date);
	}
	
	@PostMapping(path = "/getAllLocationsFromTo")
	public List<Location> getAllLocationsFromTo(@RequestBody String json, @RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		System.out.println("##############################");
		System.out.println("Receieved get all locations from/to date request:");
		
		System.out.println(json);
		
		// Reverse as date reads in american date
		String strDate1 = json.substring(9,19);
		Date date1 = Date.from(LocalDate.parse(strDate1).atStartOfDay(ZoneId.systemDefault()).toInstant());
		String strDate2 = json.substring(36,46);
		Date date2 = Date.from(LocalDate.parse(strDate2).atStartOfDay(ZoneId.systemDefault()).toInstant());
		
		System.out.println(strDate1);
		System.out.println(strDate2);
		
		System.out.println(date1);
		System.out.println(date2);
		
		System.out.println("##############################");
		return AWS.getLocations(user).getLocationsFromTo(date1, date2);
	}
	
	@PostMapping(path = "/getLocationsOnDate")
	public List<Location> getLocationsOnDate(@RequestBody String json, @RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		System.out.println("##############################");
		System.out.println("Receieved get locations on date request:");
		
		// Reverse as date reads in american date
		String strDate = json.substring(12,15) + json.substring(9,12) + json.substring(15,19);
		Date date = new Date(strDate);
		
		System.out.println(json);
		System.out.println(strDate);
		System.out.println("##############################");
		return AWS.getLocations(user).getAllLocationsOnDate(date).getLocations();
	}
	
	@PostMapping(path = "/addKnownLocation/{name}/{lat}/{lng}/{radius}/{description}")
	public void addKnownLocation(@PathVariable String name,
			@PathVariable double lat, 
			@PathVariable double lng,
			@PathVariable double radius,
			@PathVariable String description,
			@RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		AWS.addKnownLocation(user, new KnownLocation(name, description, lng, lat, radius));
		System.out.println("Created " + name + " in " + user);
	}
	
	@PostMapping(path = "/removeKnownLocation/{name}")
	public ResponseEntity<String> removeKnownLocation(@PathVariable String name, @RequestHeader("Authorization") String jwt) {
		String user = jwtTokenUtil.extractUsername(jwt.substring(7));
		List<KnownLocation> kl = AWS.getKnownLocations(user);
		KnownLocation toDelete = kl.stream().filter(loc -> loc.getName().equals(name)).findFirst().get();
		System.out.println(toDelete);
		AWS.removeKnownLocation(user, toDelete);
		System.out.println("Deleted " + toDelete + " from " + user);
		return ResponseEntity.status(HttpStatus.OK).body("Deleted object");
	}
	
}
