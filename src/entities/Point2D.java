package entities;

import java.awt.*;

public class Point2D extends Point {

	public double x;
	public double y;
	
	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double X() {
		return x;
	}
	
	public double Y() {
		return y;
	}
	
}
