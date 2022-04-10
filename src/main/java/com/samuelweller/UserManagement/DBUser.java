package com.samuelweller.UserManagement;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
public class DBUser {

	@Id
	@GeneratedValue
	private int id;
	private String username, password;
	
	public DBUser(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
}
