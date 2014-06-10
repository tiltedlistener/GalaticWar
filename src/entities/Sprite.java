package entities;

import java.awt.*;
import javax.swing.*;

public class Sprite extends Object {
	
	private ImageEntity entity;
	protected Point2D pos;
	protected Point2D vel;
	protected double rotRate;
	protected int currentState;
	
	public Sprite(JFrame a, Graphics2D g2d) {
		entity = new ImageEntity(a);
		entity.setGraphics(g2d);
		entity.setAlive(false);
		pos = new Point2D(0,0);
		vel = new Point2D(0,0);
		rotRate = 0.0;
		currentState = 0;
	}
	
	public void load(String filename) {
		entity.load(filename);
	}
	
	public void transform() {
		entity.setX(pos.x);
		entity.setY(pos.y);
		entity.transform();
	}
	
	public void draw() {
		entity.g2d.drawImage(entity.getImage(), entity.at, entity.frame);
	}
	
	public void drawBounds(Color c) {
		entity.g2d.setColor(c);
		entity.g2d.draw(getBounds());
	}
	
	public void updatePosition() {
		pos.x += vel.x;
		pos.y += vel.y;
	}
	
	public double rotationRate() {
		return rotRate;
	}
	
	public void setRotationRate(double rotation) {
		rotRate = rotation;
	}
	
	public void updateRotation() {
		setFaceAngle(faceAngle() + rotRate);
		if(faceAngle() < 0) {
			setFaceAngle(360 - rotRate);
		} else if (faceAngle() > 360) {
			setFaceAngle(rotRate);
		}
	}
	
	public int state() {
		return currentState;
	}
	
	public void setState(int state) {
		currentState = state;
	}
	
	public Rectangle getBounds() {
		return entity.getBounds();
	}
	
	public Point2D position() {
		return pos;
	}
	
	public void setPosition(Point2D pos) {
		this.pos = pos;
	}
	
	public Point2D velocity() {
		return vel;
	}
	
	public void setVelocity(Point2D vel) {
		this.vel = vel;
	}
	
	public Point2D center() {
		int x = (int)entity.getCenterX();
		int y = (int)entity.getCenterY();
		return(new Point2D(x,y));
	}
	
	public boolean alive() {
		return entity.isAlive();
	}
	
	public void setAlive(boolean alive) {
		entity.setAlive(alive);
	}
	
	public double faceAngle() {
		return entity.getFaceAngle();
	}
	
	public void setFaceAngle(double angle) {
		entity.setFaceAngle(angle);
	}
	
	public void setFaceAngle(float angle) {
		entity.setFaceAngle((double) angle);
	}
	
	public void setFaceAngle(int angle) {
		entity.setFaceAngle((double) angle);
	}
	
	public double moveAngle() {
		return entity.getMoveAngle();
	}
	
	public void setMoveAngle(double angle) {
		entity.setMoveAngle(angle);
	}
	
	public void setMoveAngle(float angle) {
		entity.setMoveAngle((double) angle);
	}
	
	public void setMoveAngle(int angle) {
		entity.setMoveAngle((double) angle);
	}
	
	public int imageWidth() {
		return entity.width();
	}
	
	public int imageHeight() {
		return entity.height();
	}
	
	public boolean collidesWith(Rectangle rect) {
		return (rect.intersects(getBounds()));
	}
	
	public boolean collidesWith(Sprite sprite) {
		return (getBounds().intersects(sprite.getBounds()));
	}

	public boolean collidesWith(Point point) {
		return (getBounds().contains(point.x, point.y));
	}
	
	public JFrame frame() {
		return entity.frame;
	}
	
	public Graphics2D graphics() { 
		return entity.g2d;
	}

	public Image image() { 
		return entity.image;
	}
	
	public void setImage(Image image) {
		entity.setImage(image);
	}

}
