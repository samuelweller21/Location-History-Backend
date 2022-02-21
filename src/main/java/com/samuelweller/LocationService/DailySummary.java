package com.samuelweller.LocationService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.samuelweller.Distance.DS;
import com.samuelweller.Location.KnownLocation;
import com.samuelweller.Location.Location;

public class DailySummary {

	public static List<DailySummaryObj> getDailySummary(LL ll, List<KnownLocation> kls, Date date) {

//		System.out.println("Running ds");
		
		List<Long> kl_times = new ArrayList();
		
		//Init
		
		for (int i = 0; i < kls.size(); i++) {
			kl_times.add(i, 0L);
		}
		
		List<Location> locations = ll.getAllLocationsOnDate(date).getLocations();
		
		if (locations.size() == 0) {	
			return null;
		}

		for (int l = 0; l < kls.size(); l++) {
			for (int t = 1; t < locations.size() - 1; t++) {
				if (DS.getDistance(locations.get(t), new Location(kls.get(l))) < kls.get(l).getRadius()) {
					kl_times.set(l,
							kl_times.get(l) + (locations.get(t + 1).getTimestamp() - locations.get(t).getTimestamp()));
				}
			}
		}
		
		LocalDate ldate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		long day_before = ldate.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
		long day_after = ldate.atStartOfDay().plusDays(1L).toEpochSecond(ZoneOffset.UTC);
		
		for (int l = 0; l < kls.size(); l++) {
			if (DS.getDistance(locations.get(0), new Location(kls.get(l))) < kls.get(l).getRadius()) {
				kl_times.set(l,
						kl_times.get(l) + (locations.get(0).getTimestamp() - day_before));
			}
		}
		
		for (int l = 0; l < kls.size(); l++) {
			if (DS.getDistance(locations.get(locations.size()-1), new Location(kls.get(l))) < kls.get(l).getRadius()) {
				kl_times.set(l,
						kl_times.get(l) + (day_after - locations.get(locations.size()-1).getTimestamp()));
			}
		}		
				
		List<DailySummaryObj> ds = new ArrayList();
		for (int i = 0;  i < kls.size(); i++) {
			ds.add(new DailySummaryObj(kls.get(i), kl_times.get(i)));
		}

		return ds;
	}

}
