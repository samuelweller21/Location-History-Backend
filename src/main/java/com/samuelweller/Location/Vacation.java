package com.samuelweller.Location;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Vacation {

	public int startIndex, endIndex;
	public Date startDate, endDate;
	public String countryName;
	
}
