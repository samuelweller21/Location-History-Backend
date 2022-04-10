package com.samuelweller.UserManagement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<DBUser, Integer> {

	List<DBUser> findByUsername(String user);
	
}
