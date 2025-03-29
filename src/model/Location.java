package model;

import utils.EarthDistance;

public class Location {
	private double lat = 0;
	private double lng = 0;

	/**
	 * @return the lat
	 */
	public double getLat() {
		return lat;
	}
	/**
	 * @param lat the lat to set
	 */
	public void setLat(double lat) {
		this.lat = lat;
	}
	/**
	 * @return the lng
	 */
	public double getLng() {
		return lng;
	}
	/**
	 * @param lng the lng to set
	 */
	public void setLng(double lng) {
		this.lng = lng;
	}
	
	public Location() {
		
	}
	public Location(double lat,double lng) {
		this.lat = lat;
		this.lng = lng;
	}
	
	public Location(Location l) {
		this.lat = l.getLat();
		this.lng = l.getLng();
	}
	
	public Location(double[] l) {
		this.lat = l[0];
		this.lng = l[1];
	}
	
	
	public static double getDistance(Location l1,Location l2) {
		return EarthDistance.calculate(l1.getLat(), l1.getLng(), l2.getLat(), l2.getLng()); //meter
	}

}
