package com.samuelweller.LocationService;

import com.samuelweller.Location.KnownLocation;

public class DailySummaryObj {

	public String name;
	public double ms;
	public int h, m, s;
	
	
	public DailySummaryObj(KnownLocation kl, double time) {
		this.name = kl.getName();
		this.ms = time;
		
		System.out.println(this.ms);
		
		// Convert time to h:m:s
		this.h = (int) Math.floor(time/(60*60));
		this.m = (int) Math.floor((time - this.h*60*60)/60);
		this.s = (int) Math.floor(time - this.h*60*60 - this.m*60);
	}
	
	public String toString() {
		return("Name: " + this.name + " - " + this.h + " hours, " + this.m + " mins, " + this.s + " seconds");
	}
	
}
