package com.samuelweller.jwt;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.samuelweller.UserManagement.DBUser;
import com.samuelweller.UserManagement.UserRepoImpl;

@Service
public class LHVUserDetailsService implements UserDetailsService {

	@Autowired
	UserRepoImpl userRepo;
	
	@PostConstruct
	public void postConstruct() {
		userRepo.addUser(new DBUser("sweller", "sweller"));
		userRepo.addUser(new DBUser("samuelweller21@hotmail.com", "testPW"));
		userRepo.addUser(new DBUser("samuelweller22@hotmail.com", "testPW123"));
		userRepo.addUser(new DBUser("samuelweller23@hotmail.com", "testPW332"));
		userRepo.addUser(new DBUser("samuelweller24@hotmail.com", "testPW222"));
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
 		return new User(userRepo.findUser(username).get(0).getUsername(), userRepo.findUser(username).get(0).getPassword(), new ArrayList());
	}

}
