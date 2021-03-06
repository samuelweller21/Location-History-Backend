package com.samuelweller.LocationHistoryViewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.samuelweller.AWS.S3.AWSService;
import com.samuelweller.Mail.EmailServiceImpl;

//docker run --detach --env MYSQL_ROOT_PASSWORD=sweller --env MYSQL_USER=sweller --env MYSQL_PASSWORD=sweller --env MYSQL_DATABASE=test --name mysql --publish 3306:3306 mysql:5.7
//\connect sweller@localhost:3306

@EnableCaching
@SpringBootApplication()
@ComponentScan(basePackages={"com.samuelweller"})
@EnableJpaRepositories(basePackages= {"com.samuelweller"})
@EntityScan("com.samuelweller")  
public class LocationHistoryViewerApplication extends SpringBootServletInitializer implements CommandLineRunner {
	
	@Autowired
	AWSService AWS;
	
	@Autowired
	EmailServiceImpl es;
	
	public static void main(String[] args) {
		
		SpringApplication.run(LocationHistoryViewerApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {

		AWS.TESTcreateObject();
		
		// Send test email
		
		//es.sendSimpleMessage("samuelweller21@hotmail.com", "Testing", "Hello, this is a test email \n \n And this is on a new line");
	}

}
