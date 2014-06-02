package entities;

import java.awt.Shape;

public class VectorEntity extends BaseGameEntity {
	private Shape shape; 
	
	public Shape getShape() {
		return shape;
	}
	
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	
	public VectorEntity() {
		setShape(null);
	}
}
