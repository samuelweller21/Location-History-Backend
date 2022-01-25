package com.samuelweller.LocationHistoryViewer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samuelweller.AWS.S3.AWSService;
import com.samuelweller.JSONParsing.GoogleJSONMapper;
import com.samuelweller.JSONParsing.JSONParser;
import com.samuelweller.Location.Location;

//docker run --detach --env MYSQL_ROOT_PASSWORD=sweller --env MYSQL_USER=sweller --env MYSQL_PASSWORD=sweller --env MYSQL_DATABASE=test --name mysql --publish 3306:3306 mysql:5.7

@EnableCaching
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@ComponentScan("com.samuelweller")
public class LocationHistoryViewerApplication implements CommandLineRunner {
	
	public static void main(String[] args) {
		
		SpringApplication.run(LocationHistoryViewerApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {

		String json = Files.readString(Path.of(this.getClass().getResource("/static/locations.json").toURI()));
		long currentTimeMillis = System.currentTimeMillis();
		List<Location> parse = JSONParser.parse(json);
		System.out.println(parse.size());
		System.out.println("Time taken to parse alone: " + (System.currentTimeMillis() - currentTimeMillis)/1000 + "s");
	    
		AWSService AWS = new AWSService();
		AWS.TESTcreateObject();
	}

}
