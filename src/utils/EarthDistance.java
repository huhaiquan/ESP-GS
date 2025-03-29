package utils;

public final class EarthDistance {
	
	public static final double EARTH_RADIUS = 6378137;

    public static double calculate(double lat1, double lng1, double lat2, double lng2) {
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(
				Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		return s; // meter
	}

    public static double rad(double d){
	    return d * Math.PI / 180.0;
	}
}
