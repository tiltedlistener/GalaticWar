package entities;

import java.awt.*;
import javax.swing.*;

public class AnimatedSprite {
	protected JFrame frame;
	protected Graphics2D g2d;
	protected ImageEntity image;
	public boolean alive;
	public boolean collided;
	public Point2D position;
	public Point2D velocity;
	public double rotationRate;
	public int currentState;
	public int currentFrame, totalFrames;
	public int animationDirection;
	public int frameCount, frameDelay;
	public int frameWidth, frameHeight, columns;
	public double moveAngle, faceAngle;
	
	public int spriteType;
	public int spriteState;
	public int lifespan;
	
	public AnimatedSprite(JFrame _frame, Graphics2D _g2d) {
		frame = _frame;
		g2d = _g2d;
		image = null;
		alive = true;
		position = new Point2D(0,0);
		velocity = new Point2D(0,0);
		rotationRate = 0.0;
		currentState = 0;
		currentFrame = 0;
		totalFrames = 1;
		animationDirection = 1;
		frameCount = 0;
		frameDelay = 0;
		frameWidth = 0;
		frameHeight = 0;
		columns = 1;
		moveAngle = 0.0;
		faceAngle = 0.0;
	}
	
	public JFrame getJFrame() { return frame; }
	public Graphics2D getGraphics() { return g2d; }
	public void setGraphics(Graphics2D _g2d) { g2d = _g2d; }
	public void setImage(ImageEntity _image) { image = _image; }
	
	public int getWidth() {
		if (image != null) {
			return image.width();
		} else {
			return 0;
		}
	}
	
	public int getHeight() {
		if (image != null) {
			return image.height();
		} else {
			return 0;
		}
	}
	
	/**
	 * Frame methods
	 */
	public void setFrameWidth(int frameWidth) {
		this.frameWidth = frameWidth;
	}
	
	public void setFrameHeight(int frameHeight) {
		this.frameHeight = frameHeight;
	}
	public int frameWidth() { return frameWidth; }
	public int frameHeight() { return frameHeight; }
	
	public void setColumns(int columns) {
		this.columns = columns;
	}
	
	public void setTotalFrames(int frames) {
		this.totalFrames = frames;
	}
	
	/**
	 * Position relations
	 */
	public Point2D center() {
		return new Point2D(getCenterX(), getCenterY());
	}
	
	public double getCenterX() {
		return position.x + getWidth() / 2;
	}
	
	public double getCenterY() {
		return position.y + getHeight() / 2;
	}
	
	public Point getCenter() {
		int x = (int)getCenterX();
		int y = (int)getCenterY();
		return (new Point(x, y));
	}
	
	public Rectangle getBounds() {
		return (new Rectangle((int)position.x, (int)position.y, getWidth(), getHeight()));
	}
	
	public void load(String filename, int _columns, int _totalFrames, int _width, int _height) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Image tempImage = tk.getImage(filename);
		while(tempImage.getWidth(frame) <= 0);
		image.setImage(tempImage);
		columns = _columns;
		totalFrames = _totalFrames;
		frameWidth = _width;
		frameHeight = _height;
	}
	
	public void draw() {
		int fx = (currentFrame % columns) * frameWidth;
		int fy = (currentFrame / columns) * frameHeight;
		g2d.drawImage(image.getImage(), (int)position.X(), (int)position.Y(), (int)position.X() + frameWidth, (int)position.Y() + frameHeight, fx, fy, fx+frameWidth, fy+frameHeight, getJFrame());
	}	
	
	public void updateFrame() {
		if (totalFrames > 1) {
			frameCount++;
			if(frameCount > frameDelay) {
				frameCount = 0;
				currentFrame += animationDirection;
				if(currentFrame > totalFrames - 1) {
					currentFrame = 0;
				} else if (currentFrame < 0) {
					currentFrame = totalFrames - 1;
				}
			}		
		}
	}
	
	public void transform() {
		image.transform();
	}
	
	public void update() {
		position.x += velocity.x;
		position.y += velocity.y;
		
		if (rotationRate > 0.0) {
			faceAngle += rotationRate;
			if(faceAngle < 0) {
				faceAngle = 360 - rotationRate;
			} else if (faceAngle > 360) {
				faceAngle = rotationRate;
			}
		}
		
		if (totalFrames > 1) {
			frameCount++;
			if(frameCount > frameDelay) {
				frameCount = 0;
				currentFrame += animationDirection;
				if(currentFrame > totalFrames - 1) {
					currentFrame = 0;
				} else if (currentFrame < 0) {
					currentFrame = totalFrames - 1;
				}
			}		
		}
	}
	
	public void updatePosition() {
		position.x += velocity.x;
		position.y += velocity.y;

	}
	
	public void updateRotation() {
		if (rotationRate > 0.0) {
			faceAngle += rotationRate;
			if(faceAngle < 0) {
				faceAngle = 360 - rotationRate;
			} else if (faceAngle > 360) {
				faceAngle = rotationRate;
			}
		}
	}
	
	public void updateAnimation() {
		if (totalFrames > 1) {
			frameCount++;
			if(frameCount > frameDelay) {
				frameCount = 0;
				currentFrame += animationDirection;
				if(currentFrame > totalFrames - 1) {
					currentFrame = 0;
				} else if (currentFrame < 0) {
					currentFrame = totalFrames - 1;
				}
			}		
		}
	}
	
	public void setLifespan(int span) {
		lifespan = span;
	}
	
	public void updateLifetime() {
		lifespan -= 1;
	}
	
	public void drawBounds(Color c) {
		g2d.setColor(c);
		g2d.draw(getBounds());
	}
	
	public boolean collidesWith(Rectangle rect) {
		return (rect.intersects(getBounds()));
	}
	
	public boolean collidesWith(AnimatedSprite sprite) {
		return (getBounds().intersects(sprite.getBounds()));
	}
	
	public boolean collidesWith(Sprite sprite) {
		return (getBounds().intersects(sprite.getBounds()));
	}

	public boolean collidesWith(Point point) {
		return (getBounds().contains(point.x, point.y));
	}
	
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
	public boolean alive() {
		return this.alive;
	}
	
	public void setFrameDelay(int delay) {
		frameDelay = delay;
	}
	
	public void setCurrentFrame(int frame) {
		this.currentFrame = frame;
	}
	
	public int totalFrames() {
		return frameCount;
	}
	
	public int currentFrame() {
		return this.currentFrame;
	}
	
	/**
	 * Position and Motion variables
	 */
	public void setPosition(Point2D point) {
		position = point;
	}
	
	public Point2D position() {
		return position;
	}
	
	public void setVelocity(Point2D vel) {
		this.velocity = vel;
	}
	public Point2D velocity() { return this.velocity; }
	
	public void setFaceAngle(double faceAngle) {
		this.faceAngle = faceAngle;
	}

	public void setMoveAngle(double moveAngle) {
		this.moveAngle = moveAngle;
	}
	
	public double moveAngle() { return this.moveAngle; }
	public double faceAngle() { return this.faceAngle; }
	
	/**
	 * Game State
	 */
	public void setSpriteType(int type) {
		spriteType = type;
	}
	
	public int spriteType() {
		return spriteType;
	}
	
	public void setState(int state) {
		spriteState = state;
	}
	public int state() { return this.spriteState; }
	
	public void setCollided(boolean collided) {
		this.collided = collided;
	}
	public boolean collided() { return this.collided; }
}
