package game;

import entities.*;
import entities.Point2D;
import sound.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import java.lang.System;

public class GalacticWar extends JFrame implements Runnable, KeyListener {

	public static void main(String[] args) {
		new GalacticWar();
	}
	
	// Main Graphics variables
	static int SCREENWIDTH = 800;
	static int SCREENHEIGHT = 600;
	static int CENTERX = SCREENWIDTH / 2;
	static int CENTERY = SCREENHEIGHT / 2;
	
	// Sprite states
	static int SPRITE_NORMAL = 0;
	static int SPRITE_COLLIDED = 1;
	
	// Game objects
	static int ASTEROIDS = 10;	
	static int BULLETS = 10;
	static int BULLET_SPEED = 4;
	static double ACCELERATION = 0.05;
	
	// Screen entities
	Sprite ship;
	Sprite[] ast = new Sprite[ASTEROIDS];
	Sprite[] bullet = new Sprite[BULLETS];
	int currentBullet = 0;
	ImageEntity background;
	
	// Support
	AffineTransform identity = new AffineTransform();
	Random rand = new Random();
	
	// Sound 
	SoundClip shoot;
	SoundClip explode;
	
	// Graphics
	Thread gameloop;
	BufferedImage backBuffer;
	Graphics2D g2d;
	
	// Dev support
	boolean showBounds = false;
	boolean collisionTesting = false;
	
	// Keyboard
	boolean keyDown, keyUp, keyLeft, keyRight, keyFire;
	
	// Frame controls
	int frameCount = 0, frameRate = 0;
	long startTime = System.currentTimeMillis();
	
	public GalacticWar() { 		
		super("Galatic War");
		
		backBuffer = new BufferedImage(SCREENWIDTH, SCREENHEIGHT, BufferedImage.TYPE_INT_RGB);
		g2d = backBuffer.createGraphics();
		
		// Set Background
		background = new ImageEntity(this);
		background.load("images/space.png");
		
		// Initialize in-game objects
		ship = new Sprite(this, g2d);
		ship.load("images/spaceship.png");
		ship.setPosition(new Point2D(CENTERX, CENTERY));
		ship.setAlive(true);
		
		for(int i=0;i<BULLETS;i++) {
			bullet[i] = new Sprite(this, g2d);
			bullet[i].load("images/laser.png");
		}

		for(int j=0;j<ASTEROIDS;j++) {
			ast[j] = new Sprite(this, g2d);
			ast[j].setAlive(true);
			
			int i=rand.nextInt(5);
			ast[j].load("images/asteroid" + i + ".png");
			
			int x = rand.nextInt(SCREENWIDTH);
			int y = rand.nextInt(SCREENHEIGHT);
			ast[j].setPosition(new Point2D(x,y));
			
			ast[j].setFaceAngle(rand.nextInt(360));
			ast[j].setMoveAngle(rand.nextInt(360));
			ast[j].setRotationRate(rand.nextDouble());
			
			double ang = ast[j].moveAngle() - 90;			
			double velx = calcAngleMoveX(ang);
			double vely = calcAngleMoveY(ang);
			ast[j].setVelocity(new Point2D(velx, vely));
		}
		
		shoot = new SoundClip("sounds/laser.wav");
		explode = new SoundClip("sounds/explosion.wav");
		
		addKeyListener(this);
		start();
	
		setSize(SCREENWIDTH, SCREENHEIGHT);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	/** 
	 * THREAD METHODS
	 */
	public void start() {
		gameloop = new Thread(this);
		gameloop.start();
	}
	
	public void run() {
		Thread t = Thread.currentThread();
		while(t == gameloop) {
			try {
				Thread.sleep(20);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			gameUpdate(); 
			repaint();
		}
	}
	
	public void stop() {
		gameloop = null;
	}	
	
	/**
	 * GAME UPDATE METHODS
	 */
	private void gameUpdate() {
		checkInput();
		updateShip();
		updateBullets();
		updateAsteroids();
		if (collisionTesting) checkCollisions();
	}
	
	public void updateShip() {
		ship.updatePosition();
		double newx = ship.position().X();
		double newy = ship.position().Y();

		if(ship.position().X() < -10) {
			newx = SCREENWIDTH + 10;
		} else if (ship.position().X() > SCREENWIDTH + 10) {
			newx = -10;
		}

		if(ship.position().Y() < -10) {
			newy = SCREENHEIGHT + 10;
		} else if (ship.position().Y() > SCREENHEIGHT + 10) {
			newy = -10;
		}
		
		ship.setPosition(new Point2D(newx,newy));
		ship.setState(SPRITE_NORMAL);
	}
	
	public void updateBullets() {
		for (int n=0;n<BULLETS;n++) {
			if (bullet[n].alive()) {
				
				bullet[n].updatePosition();
				if(bullet[n].position().X() < 0 || bullet[n].position().X() > SCREENWIDTH) {
					bullet[n].setAlive(false);
				}

				bullet[n].updatePosition();
				if(bullet[n].position().Y() < 0 || bullet[n].position().Y() > SCREENHEIGHT) {
					bullet[n].setAlive(false);
				}
				
				bullet[n].setState(SPRITE_NORMAL);
			}
		}
	}
	
	public void updateAsteroids() {
		for (int n=0;n<ASTEROIDS;n++) {
			if (ast[n].alive()) {
				ast[n].updatePosition();
				ast[n].updateRotation();
				
				int w = ast[n].imageWidth()-1;
				int h = ast[n].imageHeight()-1;
				double newx = ast[n].position().X();
				double newy = ast[n].position().Y();
				
				if (ast[n].position().X() < -w) {
					newx = SCREENWIDTH + w;
				} else if (ast[n].position().X() > SCREENWIDTH + w) {
					newx = -w;
				}
				
				if (ast[n].position().Y() < -h) {
					newy = SCREENHEIGHT + h;
				} else if (ast[n].position().Y() > SCREENHEIGHT + h) {
					newy = -h;
				}
				
				ast[n].setPosition(new Point2D(newx, newy));
				ast[n].setState(SPRITE_NORMAL);
			}
		}	
	}	
	
	public void checkCollisions() {
		for(int m=0;m<ASTEROIDS;m++) {

			if (ast[m].alive()) {
				for(int n=0;n<BULLETS;n++) {
					if(bullet[n].alive()) {
						if (ast[m].collidesWith(bullet[n])){
							bullet[n].setState(SPRITE_COLLIDED);
							ast[m].setState(SPRITE_COLLIDED);
							explode.play();
						}
					}
				}
			}
			
			if(ship.collidesWith(ast[m])) {
				ast[m].setState(SPRITE_COLLIDED);
				ship.setState(SPRITE_COLLIDED);
				explode.play();
			}
		}
	}	
	
	/**
	 *  PAINT METHODS
	 */
	public void paint(Graphics g) {
		frameCount++;
		if(System.currentTimeMillis() > startTime + 1000) {
			startTime = System.currentTimeMillis();
			frameRate = frameCount;
			frameCount = 0;
		}
		
		g2d.setTransform(identity);
		g2d.setPaint(Color.black);
		g2d.fillRect(0, 0, SCREENWIDTH, SCREENHEIGHT);
		
		g2d.drawImage(background.getImage(), 0, 0, SCREENWIDTH - 1, SCREENHEIGHT - 1, this);
		
		drawShip();
		drawBullets();
		drawAsteroids();
		g.drawImage(backBuffer, 0, 0, this);
	}	
	
	public void drawShip() {
		ship.transform();
		ship.draw();
		
		if (showBounds) {
			if (ship.state() == SPRITE_COLLIDED) {
				ship.drawBounds(Color.RED);
			} else {
				ship.drawBounds(Color.BLUE);
			}
		}
	}
	
	public void drawBullets() {
		for (int n=0;n<BULLETS;n++) {
			if (bullet[n].alive()) {
				bullet[n].transform();
				bullet[n].draw();
				if (showBounds) {
					if (bullet[n].state() == SPRITE_COLLIDED) {
						bullet[n].drawBounds(Color.RED);
					} else {
						bullet[n].drawBounds(Color.BLUE);
					}
				}
			}
		}
	}
	
	public void drawAsteroids() {
		for(int n=0;n<ASTEROIDS;n++) {
			if (ast[n].alive()) {
				ast[n].transform();
				ast[n].draw();
				if (showBounds) {
					if (ast[n].state() == SPRITE_COLLIDED) {
						ast[n].drawBounds(Color.RED);
					} else {
						ast[n].drawBounds(Color.BLUE);
					}
				}
			}
		}
	}
	
	/**
	 * SHIP ACTIONS
	 */
	
	public void applyThrust() {
		ship.setMoveAngle(ship.faceAngle() - 90);
		double velx = ship.velocity().X();
		velx+= calcAngleMoveX(ship.moveAngle()) * ACCELERATION;
		double vely = ship.velocity().Y();
		vely+= calcAngleMoveY(ship.moveAngle()) * ACCELERATION;
		ship.setVelocity(new Point2D(velx, vely));
	}
	
	public void fireBullet() {
		currentBullet++;
		if(currentBullet > BULLETS -1) currentBullet = 0;
		bullet[currentBullet].setAlive(true);
		
		int w = bullet[currentBullet].imageWidth();
		int h = bullet[currentBullet].imageHeight();
		
		double x = ship.center().X() - w/2;
		double y = ship.center().Y() - w/2;
		bullet[currentBullet].setPosition(new Point2D(x,y));
		
		bullet[currentBullet].setFaceAngle(ship.faceAngle());
		bullet[currentBullet].setMoveAngle(ship.faceAngle() - 90);
		
		double angle = bullet[currentBullet].moveAngle();
		double svx = calcAngleMoveX(angle) * BULLET_SPEED;
		double svy = calcAngleMoveY(angle) * BULLET_SPEED;
		bullet[currentBullet].setVelocity(new Point2D(svx, svy));
		
		shoot.play();
	}
	
	/**
	 * MOTION SUPPORT
	 */
	public double calcAngleMoveX(double angle) {
		return (double) (Math.cos(angle * Math.PI / 180));
	}
	
	public double calcAngleMoveY(double angle) {
		return (double) (Math.sin(angle * Math.PI / 180));
	}
	
	/** KEYBOARD **/
	public void checkInput() {
		if (keyLeft) {
			ship.setFaceAngle(ship.faceAngle() - 5);
			if (ship.faceAngle() < 0) ship.setFaceAngle(360-5);
		} else if (keyRight) {
			ship.setFaceAngle(ship.faceAngle() + 5);
			if (ship.faceAngle() > 360) ship.setFaceAngle(5);
		}
		
		if (keyUp) {
			applyThrust();
		}
	}
	
	public void keyTyped(KeyEvent k) {}
	public void keyReleased(KeyEvent k) {
		int keyCode = k.getKeyCode();
		switch (keyCode) {
			case KeyEvent.VK_LEFT:
				keyLeft = false;
				break;
			case KeyEvent.VK_RIGHT:
				keyRight = false;
				break;
			case KeyEvent.VK_UP:
				keyUp = false;
				break;
			case KeyEvent.VK_SPACE:
				keyFire = false;
				break;
			case KeyEvent.VK_B: 
				showBounds = !showBounds;
				break;
			case KeyEvent.VK_C:
				collisionTesting = !collisionTesting;
				break;
		}
	}
	public void keyPressed(KeyEvent k) {
		int keyCode = k.getKeyCode();
		switch (keyCode) {
			case KeyEvent.VK_LEFT:
				keyLeft = true;
				break;
			case KeyEvent.VK_RIGHT:
				keyRight = true;
				break;
			case KeyEvent.VK_UP:
				keyUp = true;
				break;
			case KeyEvent.VK_SPACE:
				keyFire = true;
				fireBullet();
				break;
			case KeyEvent.VK_B: 
				showBounds = !showBounds;
				break;
			case KeyEvent.VK_C:
				collisionTesting = !collisionTesting;
				break;
		}
	}
	

}
