package com.samuelweller.LocationHistoryViewer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

import com.samuelweller.AWS.S3.AWSService;
import com.samuelweller.Location.KnownLocation;

//docker run --detach --env MYSQL_ROOT_PASSWORD=sweller --env MYSQL_USER=sweller --env MYSQL_PASSWORD=sweller --env MYSQL_DATABASE=test --name mysql --publish 3306:3306 mysql:5.7

@EnableCaching
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@ComponentScan("com.samuelweller")
public class LocationHistoryViewerApplication implements CommandLineRunner {
	
	@Autowired
	AWSService AWS;
	
	public static void main(String[] args) {
		
		SpringApplication.run(LocationHistoryViewerApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {

//		String json = Files.readString(Path.of(this.getClass().getResource("/static/locations.json").toURI()));
//		long currentTimeMillis = System.currentTimeMillis();
//		List<Location> parse = JSONParser.parse(json);
//		System.out.println(parse.size());
//		System.out.println("Time taken to parse alone: " + (System.currentTimeMillis() - currentTimeMillis)/1000 + "s");
//	    
		AWS.TESTcreateObject();
	
	}

}
