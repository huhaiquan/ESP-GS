package model;

public class BaseStation {
	private Location loc = new Location();
	private double radius = 0;
	
	public BaseStation(){
		
	}
	
	public BaseStation(BaseStation s) {		
		this.loc = s.getLocation();
		this.radius = s.getRadius();
	}
	
	public BaseStation(Location loc,double radius) {
		this.loc = loc;
		this.radius = radius;
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
	
	
	/**
	 * @return the radius
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

}
