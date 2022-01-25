package com.samuelweller.Location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class KnownLocations {

	public static ArrayList<KnownLocation> KNOWN_LOCATIONS;
	
	public KnownLocations() throws IOException {
		BufferedReader csvReader = new BufferedReader(
				new InputStreamReader(this.getClass().getClassLoader().getResource("static/locations.csv").openStream()));
		CSVParser parser = new CSVParser(csvReader, CSVFormat.RFC4180);
		List<CSVRecord> list = parser.getRecords();
		KNOWN_LOCATIONS = new ArrayList<KnownLocation>();
		for (int i = 1; i < list.size(); i++) {
			List row = list.get(i).toList();
			KnownLocation next = new KnownLocation(
					row.get(0).toString(),
					row.get(1).toString(),
					Double.parseDouble(row.get(2).toString()),
					Double.parseDouble(row.get(3).toString()));
			KNOWN_LOCATIONS.add(next);
		}
	}
	
}
