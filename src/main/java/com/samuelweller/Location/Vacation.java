package com.samuelweller.Location;

import java.util.Date;

public class Vacation {

	public int startIndex, endIndex;
	public Date startDate, endDate;
	public String countryName;
	
	public Vacation(int start, int end, Date startDate, Date endDate, String name) {
		this.startIndex = start;
		this.endIndex = end;
		this.startDate = startDate;
		this.endDate = endDate;
		this.countryName = name;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	
}
