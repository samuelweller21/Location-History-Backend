package com.samuelweller.Shapefiles;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.feature.simple.SimpleFeature;

import com.samuelweller.Location.Location;

public class CountriesService {

	public static SimpleFeatureCollection collection;
	public static List<String> countries;
	public static boolean loaded = false;

	public static void loadData() {
		try {
		// Get shapefile
		File file = null;
		Set<String> set = new HashSet();
		try {
			file = new File(CountriesService.class.getClassLoader()
					.getResource("static/shapefiles/countries/World_Countries__Generalized_.shp").toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		FileDataStore store = null;
		try {
			store = FileDataStoreFinder.getDataStore(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SimpleFeatureSource featureSource = null;
		try {
			featureSource = store.getFeatureSource();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			CountriesService.collection = featureSource.getFeatures();
		} catch (IOException e) {
			e.printStackTrace();
		}
		SimpleFeatureIterator iterator = collection.features();
		while (iterator.hasNext()) {
        	SimpleFeature feature = iterator.next();
        	set.add("\"" + feature.getAttribute("COUNTRYAFF").toString() + "\"");	
        }
		iterator.close();
		store.dispose();
		CountriesService.countries = new ArrayList(set);
		CountriesService.loaded = true;
		} catch (Exception e) {
			System.out.println("Exception in load");
			e.printStackTrace();
		}
		
	}
	
	public static String getCountry(Location l) {
		if (!CountriesService.loaded) {
			CountriesService.loadData();
		}
		return CountriesService.getCountry(l.getLng(), l.getLat());
	}
	
	public static List<String> getCountries() {
		if (!CountriesService.loaded) {
			CountriesService.loadData();
		}
		return CountriesService.countries;
	}

	public static String getCountry(double lat, double lng) {
		try {
		if (!CountriesService.loaded) {
			CountriesService.loadData();
		}
		SimpleFeatureIterator iterator = collection.features();
	        
        while (iterator.hasNext()) {
        	SimpleFeature feature = iterator.next();
        	Geometry g = (Geometry) feature.getDefaultGeometry();
        	
        	GeometryFactory factory 
            = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
        	
        	if (g.contains(factory.createPoint(new Coordinate(lat, lng)))) {
                iterator.close();
        		return (feature.getAttribute("COUNTRYAFF").toString());
        	} 
        }
        
        iterator.close();
        
        return ("No country found");
		} catch (Exception e) {
			System.out.println("Error in getCountry");
			e.printStackTrace();
			return "";
		}
	}
}
