package com.samuelweller.config;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import software.amazon.awssdk.regions.Region;

@Configuration
@ConfigurationProperties(prefix = "domain")
public class ApplicationConfig {

	public static final String DEFAULT_AWS_BUCKET_NAME = "bucketawstest";
	public static final String DEFAULT_AWS_ACCESS_KEY = "AKIAX5YHYTYZB2KSUQ2C";
	public static final String DEFAULT_AWS_SECRET_KEY = "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";
	public static final Region DEFAULT_AWS_REGION = Region.EU_WEST_2;
	public static final int DEFAULT_MINIMUM_ACCURACY = 50;
	public static final int DEFAULT_MINIMUM_NEW_LOCATION_DIST = 50;
	
	private String address;
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	@Bean
	public Caffeine caffeineConfig() {
	    return Caffeine.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES);
	}
	
	@Bean
	public CacheManager cacheManager(Caffeine caffeine) {
	    CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
	    caffeineCacheManager.setCaffeine(caffeine);
	    return caffeineCacheManager;
	}

}
