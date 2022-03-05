package com.samuelweller.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samuelweller.Location.Location;

public interface TestRepo extends JpaRepository<Location, Integer> {
	List<Location> findById(int timestamp);
	
}
