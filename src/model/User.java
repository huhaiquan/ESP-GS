package model;

public class User {
	private Location loc = new Location();
	
	public User() {
		
	}
	public User(User u) {
		this.loc = u.loc;
	}
	
	public User(Location location) {
		this.loc = location;
	}
	
	public User(double lat, double lng) {
		this.loc.setLat(lat);
		this.loc.setLng(lng);
	}
	
	/**
	 * @return the location
	 */
	public Location getLocation() {
		return loc;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Location loc) {
		this.loc = loc;
	}
	

}
