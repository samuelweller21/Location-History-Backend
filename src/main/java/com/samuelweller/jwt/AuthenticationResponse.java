package com.samuelweller.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
public class AuthenticationResponse {

	private final String jwt;
	
}
