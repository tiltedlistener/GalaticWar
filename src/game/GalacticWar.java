package game;

import entities.*;
import entities.Point2D;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.lang.System;

public class GalacticWar extends Game {
	
	final static long serialVersionUID = 0;

	public static void main(String[] args) {
		new GalacticWar();
	}
	
	// Main Graphics variables
	static int FRAMERATE = 60;
	static int SCREENWIDTH = 800;
	static int SCREENHEIGHT = 600;
	
	static int CENTERX = SCREENWIDTH / 2;
	static int CENTERY = SCREENHEIGHT / 2;
	
	// Sprite states
	final static int STATE_NORMAL = 0;
	final static int STATE_COLLIDED = 1;
	final static int STATE_EXPLODING = 2;
	
	// Game objects
	final static int ASTEROIDS = 10;	
	final static int BULLET_SPEED = 4;
	final static double ACCELERATION = 0.05;
	final static double SHIPROTATION = 5.0;
	
	// Sprite types
	final int SPRITE_SHIP = 1;
	final int SPRITE_BULLET = 100;
	final int SPRITE_EXPLOSION = 200;
	final int SPRITE_ASTEROID_BIG = 10;
	final int SPRITE_ASTEROID_MEDIUM = 11;
	final int SPRITE_ASTEROID_SMALL = 12;
	final int SPRITE_ASTEROID_TINY = 13;
	
	// Images
	ImageEntity background;
	ImageEntity bulletImage;
	ImageEntity[] bigAsteroids;
	ImageEntity[] medAsteroids;
	ImageEntity[] smlAsteroids;
	ImageEntity[] tnyAsteroids;
	ImageEntity[] explosions;
	ImageEntity[] shipImage;
	
	// Support
	AffineTransform identity;
	Random rand;
	
	// Dev support
	boolean showBounds = false;
	boolean collisionTesting = true;
	long collisionTimer = 0;
	
	// Keyboard
	boolean keyDown, keyUp, keyLeft, keyRight, keyFire;	
	
	public GalacticWar() { 		
		super(FRAMERATE, SCREENWIDTH, SCREENHEIGHT);
	}
	
	void gameStartup() {
		identity = new AffineTransform();
		rand = new Random();
		bigAsteroids = new ImageEntity[5];
		medAsteroids = new ImageEntity[2];
		smlAsteroids = new ImageEntity[3];
		tnyAsteroids = new ImageEntity[4];
		explosions = new ImageEntity[2];
		shipImage = new ImageEntity[2];
		
		background = new ImageEntity(this);
		background.load("images/space.png");
			
		shipImage[0] = new ImageEntity(this);
		shipImage[0].load("images/spaceship.png");
		shipImage[1] = new ImageEntity(this);
		shipImage[1].load("images/ship_thrust.png");
		
		AnimatedSprite ship = new AnimatedSprite(this, graphics());
		ship.setSpriteType(SPRITE_SHIP);
		ship.setImage(shipImage[0].getImage());
		ship.setFrameWidth(ship.imageWidth());
		ship.setFrameHeight(ship.imageHeight());
		ship.setPosition(new Point2D(CENTERX, CENTERY));
		ship.setAlive(true);
		ship.setState(STATE_NORMAL);
		sprites().add(ship);
		
		bulletImage = new ImageEntity(this);
		bulletImage.load("images/laser.png");
		
		explosions[0] = new ImageEntity(this);
		explosions[0].load("images/explosion.png");
		explosions[1] = new ImageEntity(this);
		explosions[1].load("images/explosion2.png");	
		
		
		
		for(int n=0; n<5;n++) {
			bigAsteroids[n] = new ImageEntity(this);
			String tempName = "asteroid" + (n+1) + ".png";
			bigAsteroids[n].load("images/" + tempName);
		}

		for(int n=0; n<2;n++) {
			medAsteroids[n] = new ImageEntity(this);
			String tempName = "medium" + (n+1) + ".png";
			medAsteroids[n].load("images/" + tempName);
		}
		
		for(int n=0; n<3;n++) {
			smlAsteroids[n] = new ImageEntity(this);
			String tempName = "small" + (n+1) + ".png";
			smlAsteroids[n].load("images/" + tempName);
		}
		
		for(int n=0; n<4;n++) {
			tnyAsteroids[n] = new ImageEntity(this);
			String tempName = "tiny" + (n+1) + ".png";
			tnyAsteroids[n].load("images/" + tempName);
		}
	
		for(int n=0;n<ASTEROIDS;n++) {
			createAsteroid();
		}

		start();
	}
	
	public void gameShutdown() {
		
	}
	
	public void gameTimeUpdate() {
		checkInput();
	}
	
	public void gameRefreshScreen() {
		Graphics2D g2d = graphics();
		
		// AnimatedSprite ship = (AnimatedSprite)sprites().get(0);
		g2d.drawImage(background.getImage(), 0,0, SCREENWIDTH - 1, SCREENHEIGHT - 1, this);
		
		// UNUSED from the book - dispensing with the status printing and therefore the ship object above
	}
	
	/**
	 * SPRITE METHODS
	 */
	public void spriteUpdate(AnimatedSprite sprite) {
		switch(sprite.spriteType()) {
		case SPRITE_SHIP:
			warp(sprite);
			break;
		case SPRITE_BULLET:
			warp(sprite);
			break;
		case SPRITE_EXPLOSION:
			if(sprite.currentFrame() == sprite.totalFrames() -1) {
				sprite.setAlive(false);
			}
			break;
		case SPRITE_ASTEROID_BIG:
		case SPRITE_ASTEROID_MEDIUM:
		case SPRITE_ASTEROID_SMALL:
		case SPRITE_ASTEROID_TINY:
			warp(sprite);
			break;
		}
	}
	
	public void spriteDraw(AnimatedSprite sprite) {
		if(showBounds) {
			if(sprite.collided()) {
				sprite.drawBounds(Color.RED);		// TODO - this currently isn't firing though some collisions are
			} else {
				sprite.drawBounds(Color.BLUE);
			}
		}
	}
	
	public void spriteDying(AnimatedSprite sprite) {
		// STUB
	}
	
	public void spriteCollision(AnimatedSprite spr1, AnimatedSprite spr2) {
		if (!collisionTesting) return;
		
		switch (spr1.spriteType()) {
		case SPRITE_BULLET: 
			if(isAsteroid(spr2.spriteType())) {
				spr1.setAlive(false);
				spr2.setAlive(false);
				breakAsteroid(spr2);
			}
			break;
		case SPRITE_SHIP:
			if(isAsteroid(spr2.spriteType())) {
				if(spr1.state() == STATE_NORMAL) {
					collisionTimer = System.currentTimeMillis();
					spr1.setVelocity(new Point2D(0, 0));
					double x = spr1.position().X() - 10;
					double y = spr1.position().Y() - 10;
					startBigExplosion(new Point2D(x, y));
					spr1.setState(STATE_EXPLODING);
					spr2.setAlive(false);
					breakAsteroid(spr2);
				} else if(spr1.state() == STATE_EXPLODING){
					if(collisionTimer + 3000 < System.currentTimeMillis()) {
						spr1.setState(STATE_NORMAL);
					}
				}
			}
			break;
		}
	}
	
	
	/**
	 * KEYBOARD AND MOUSE EVENTS
	 */
	public void gameKeyDown(int keyCode) {
		
		switch(keyCode) {
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
			break;
		}
		
		// I have removed the optional turn on bounds and collision testing
	}
	
	public void gameKeyUp(int keyCode) {

		switch(keyCode) {
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
			fireBullets();
			break;
		}
	}
	
	public void gameMouseDown() {}
	public void gameMouseUp() {}
	public void gameMouseMove() {}

	
	/**
	 * BREAK UP ASTEROIDS
	 */
	private void breakAsteroid(AnimatedSprite sprite) {
		switch(sprite.spriteType()) {
		case SPRITE_ASTEROID_BIG:
			spawnAsteroid(sprite);
			spawnAsteroid(sprite);
			spawnAsteroid(sprite);
			startBigExplosion(sprite.position());
			break;
		case SPRITE_ASTEROID_MEDIUM:
			spawnAsteroid(sprite);
			spawnAsteroid(sprite);
			spawnAsteroid(sprite);
			startSmallExplosion(sprite.position());
			break;
		case SPRITE_ASTEROID_SMALL:
			spawnAsteroid(sprite);
			spawnAsteroid(sprite);
			spawnAsteroid(sprite);
			startSmallExplosion(sprite.position());
			break;
		case SPRITE_ASTEROID_TINY:
			spawnPowerup(sprite);
			startSmallExplosion(sprite.position());
			break;
		}
	}
	
	private void spawnAsteroid(AnimatedSprite sprite) {
		AnimatedSprite ast = new AnimatedSprite(this, graphics());
		ast.setAlive(true);
		
		int w = sprite.getBounds().width;
		int h = sprite.getBounds().height;
		
		double x = sprite.position().X() + w/2 + rand.nextInt(20) - 40;
		double y = sprite.position().Y() + h/2 + rand.nextInt(20) - 40;
		ast.setPosition(new Point2D(x,y));
		
		ast.setFaceAngle(rand.nextInt(360));
		ast.setMoveAngle(rand.nextInt(360));
		ast.setRotationRate(rand.nextDouble());
		
		double ang = ast.moveAngle() - 90;
		double velx = calcAngleMoveX(ang);
		double vely = calcAngleMoveY(ang);
		ast.setVelocity(new Point2D(velx, vely));
		
		int i = 0;				// I've dispensed with having random asteroids, so here, I just set a baseline 
		switch(sprite.spriteType()) {
		case SPRITE_ASTEROID_BIG:
			ast.setSpriteType(SPRITE_ASTEROID_MEDIUM);
			ast.setImage(medAsteroids[i].getImage());
			ast.setFrameWidth(medAsteroids[i].width());
			ast.setFrameHeight(medAsteroids[i].height());
			break;
		case SPRITE_ASTEROID_MEDIUM:
			ast.setSpriteType(SPRITE_ASTEROID_SMALL);		
			ast.setImage(smlAsteroids[i].getImage());
			ast.setFrameWidth(smlAsteroids[i].width());
			ast.setFrameHeight(smlAsteroids[i].height());
			break;
		case SPRITE_ASTEROID_SMALL:
			ast.setSpriteType(SPRITE_ASTEROID_TINY);
			ast.setImage(tnyAsteroids[i].getImage());
			ast.setFrameWidth(tnyAsteroids[i].width());
			ast.setFrameHeight(tnyAsteroids[i].height());
			break;					
		}
		
		sprites().add(ast);
	}
		
	
	private void spawnPowerup(AnimatedSprite sprite) {
		
	}
	
	private void createAsteroid() {
		AnimatedSprite ast = new AnimatedSprite(this, graphics());
		ast.setAlive(true);
		ast.setSpriteType(SPRITE_ASTEROID_BIG);
	
		int i = 0;		
		ast.setImage(bigAsteroids[i].getImage());
		ast.setFrameWidth(bigAsteroids[i].width());
		ast.setFrameHeight(bigAsteroids[i].height());
		
		double x = rand.nextInt(SCREENWIDTH - 128);
		double y = rand.nextInt(SCREENHEIGHT - 128);
		ast.setPosition(new Point2D(x,y));
		
		ast.setFaceAngle(rand.nextInt(360));
		ast.setMoveAngle(rand.nextInt(360));
		ast.setRotationRate(rand.nextDouble());
		
		double ang = ast.moveAngle() - 90;
		double velx = calcAngleMoveX(ang);
		double vely = calcAngleMoveY(ang);
		ast.setVelocity(new Point2D(velx, vely));
		
		sprites().add(ast);
	}
	
	private boolean isAsteroid(int spriteType) {
		switch(spriteType) {
		case SPRITE_ASTEROID_BIG:
		case SPRITE_ASTEROID_MEDIUM:
		case SPRITE_ASTEROID_SMALL:
		case SPRITE_ASTEROID_TINY:
			return true;
		default: 
			return false;
		}
	}
		
	/**
	 * Process Keys
	 */
	public void checkInput() {
		AnimatedSprite ship = (AnimatedSprite)sprites().get(0);
		
		if(keyLeft) {
			ship.setFaceAngle(ship.faceAngle() - SHIPROTATION);
			if(ship.faceAngle() < 0) {
				ship.setFaceAngle(360 - SHIPROTATION);
			}
		}
		else if(keyRight) {
			ship.setFaceAngle(ship.faceAngle() + SHIPROTATION);
			if(ship.faceAngle() > 360) {
				ship.setFaceAngle(SHIPROTATION);
			}
		}
		
		if (keyUp) {
			ship.setImage(shipImage[1].getImage());
			applyThrust();
		} else {
			ship.setImage(shipImage[0].getImage());
		}
		
	}
	
	public void applyThrust() {
		AnimatedSprite ship = (AnimatedSprite)sprites().get(0);
		ship.setMoveAngle(ship.faceAngle() - 90);
		
		double velx = ship.velocity().X();
		velx+= calcAngleMoveX(ship.moveAngle()) * ACCELERATION;
		
		double vely = ship.velocity().Y();
		vely+= calcAngleMoveY(ship.moveAngle()) * ACCELERATION;
		
		if (velx < -10) {
			velx = -10;
		} else if (velx > 10) {
			velx = 10;
		}
		
		if (vely < -10) {
			vely = -10;
		} else if (vely > 10) {
			vely = 10;
		}
		
		ship.setVelocity(new Point2D(velx, vely));
	}
	
	public void fireBullets() {
		AnimatedSprite ship = (AnimatedSprite)sprites().get(0);
		
		AnimatedSprite bullet = new AnimatedSprite(this, graphics());
		bullet.setImage(bulletImage.getImage());
		bullet.setFrameWidth(bulletImage.width());
		bullet.setFrameHeight(bulletImage.height());
		bullet.setSpriteType(SPRITE_BULLET);
		bullet.setAlive(true);
		bullet.setLifespan(200);
		bullet.setFaceAngle(ship.faceAngle());
		bullet.setMoveAngle(ship.faceAngle() - 90);
		
		double x = ship.center().X() - bullet.imageWidth() / 2;
		double y = ship.center().Y() - bullet.imageHeight() / 2;
		bullet.setPosition(new Point2D(x, y));
		
		double angle = bullet.moveAngle();
		double svx = calcAngleMoveX(angle) * BULLET_SPEED;
		double svy = calcAngleMoveY(angle) * BULLET_SPEED;
		bullet.setVelocity(new Point2D(svx, svy));
		
		sprites().add(bullet);
	}
	
	/**
	 * Draw methods
	 */
	public void warp(AnimatedSprite spr) {
		int w = spr.frameWidth() -1;
		int h = spr.frameHeight() -1;
	
		if (spr.position().X() < 0-w) {
			spr.position().setX(SCREENWIDTH);
		} else if (spr.position().X() > SCREENWIDTH) {
			spr.position().setX(0-w);
		}
		
		if (spr.position().Y() < 0-h) {
			spr.position().setY(SCREENHEIGHT);
		} else if (spr.position().Y() > SCREENHEIGHT) {
			spr.position().setY(0-h);
		}
	}
	
	public void startBigExplosion(Point2D pos) {
		AnimatedSprite expl = new AnimatedSprite(this, graphics());
		expl.setSpriteType(SPRITE_EXPLOSION);
		expl.setAlive(true);
		expl.setAnimImage(explosions[0].getImage());
		expl.setTotalFrames(16);
        expl.setColumns(4);
        expl.setFrameWidth(96);
        expl.setFrameHeight(96);
        expl.setFrameDelay(2);
		expl.setPosition(pos);
		
		sprites().add(expl);
	}
	
	public void startSmallExplosion(Point2D pos) {
		AnimatedSprite expl = new AnimatedSprite(this, graphics());
		expl.setSpriteType(SPRITE_EXPLOSION);
		expl.setAlive(true);
		expl.setAnimImage(explosions[1].getImage());
		expl.setTotalFrames(8);
        expl.setColumns(4);
        expl.setFrameWidth(40);
        expl.setFrameHeight(40);
        expl.setFrameDelay(2);
		expl.setPosition(pos);
		
		sprites().add(expl);
	}

}
