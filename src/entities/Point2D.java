package entities;

import java.awt.*;

public class Point2D extends Point {

	final static long serialVersionUID = 0;
	
	public double x;
	public double y;
	public int id;
	
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
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
}
