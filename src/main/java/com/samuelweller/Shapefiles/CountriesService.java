package com.samuelweller.Shapefiles;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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

	public CountriesService() {
		// Get shapefile
		File file = null;
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
			this.collection = featureSource.getFeatures();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getCountry(Location l) {
		return CountriesService.getCountry(l.getLat(), l.getLng());
	}

	public static String getCountry(double lat, double lng) {
		SimpleFeatureIterator iterator = collection.features();
	        
        while (iterator.hasNext()) {
        	SimpleFeature feature = iterator.next();
        	Geometry g = (Geometry) feature.getDefaultGeometry();
        	
        	GeometryFactory factory 
            = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
        	
        	if (g.contains(factory.createPoint(new Coordinate(lat, lng)))) {
        		return (feature.getAttribute("COUNTRYAFF").toString());
        	} 
        }
        
        return ("No country found");
	}
}
