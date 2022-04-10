package com.samuelweller.UserManagement;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;


public class UserRepoImpl {

	
	@Autowired
	UserRepo userRepo;
	
	public List<DBUser> findUser(String username) {
		return userRepo.findByUsername(username);
	}
	
	public boolean addUser(DBUser user) {
		if (userRepo.findByUsername(user.getUsername()).size() > 0) {
			return false;
		} else {
			userRepo.save(user);
			return true;
		}
	}
	
	public void deleteUser(String username) {
		userRepo.delete(userRepo.findByUsername(username).get(0));
	}
	
}
