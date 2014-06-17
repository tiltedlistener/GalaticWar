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

public class GalacticWar extends Game {

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
	static int SPRITE_NORMAL = 0;
	static int SPRITE_COLLIDED = 1;
	static int SPRITE_EXPLODING = 2;
	
	// Game objects
	static int ASTEROIDS = 10;	
	static int BULLETS = 10;
	static int BULLET_SPEED = 4;
	static double ACCELERATION = 0.05;
	static double SHIPROTATION = 5.0;
	
	// Sprite types
	final int SPRITE_SHIP = 1;
	final int SPRITE_ASTEROID_BIG = 10;
	final int SPRITE_ASTEROID_MEDIUM = 11;
	final int SPRITE_ASTEROID_SMALL = 12;
	final int SPRITE_ASTEROID_TINY = 13;
	
	// Images
	ImageEntity background;
	ImageEntity bulletImage;
	ImageEntity[] bigAsteriods = new ImageEntity[5];
	ImageEntity[] medAsteriods = new ImageEntity[2];
	ImageEntity[] smlAsteriods = new ImageEntity[3];
	ImageEntity[] tnyAsteriods = new ImageEntity[4];
	ImageEntity[] explosions = new ImageEntity[2];
	ImageEntity[] shipImage = new ImageEntity[2];
		
	// Support
	AffineTransform identity = new AffineTransform();
	Random rand = new Random();
	
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
		background = new ImageEntity(this);
		background.load("images/space.png");
		
		shipImage[0] = new ImageEntity(this);
		shipImage[0].load("images/spaceship.png");
		shipImage[1] = new ImageEntity(this);
		shipImage[1].load("images/spaceship_thrust.png");
		
		AnimatedSprite ship = new AnimatedSprite(this, graphics());
		ship.setSpriteType(SPRITE_SHIP);
		ship.setImage(shipImage[0].getImage());
		ship.setFrameWidth(ship.imageWidth());
		ship.setFrameHeight(ship.imageHeight());
		ship.setPosition(new Point2D(CENTERX, CENTERY));
		ship.setAlive(true);
		ship.setState(SPRITE_NORMAL);
		sprites().add(ship);
		
		bulletImage = new ImageEntity(this);
		bulletImage.load("images/laser.png");
		
		explosions[0] = new ImageEntity(this);
		explosions[0].load("images/explosion.png");
		explosions[1] = new ImageEntity(this);
		explosions[1].load("images/explosion.png");	
		
		for(int n=0; n<5;n++) {
			bigAsteriods[n] = new ImageEntity(this);
			bigAsteriods[n].load("images/asteroid.png");
		}
		
		for(int n=0; n<2;n++) {
			medAsteriods[n] = new ImageEntity(this);
			medAsteriods[n].load("images/asteroidmedium.png");
		}
		
		for(int n=0; n<3;n++) {
			smlAsteriods[n] = new ImageEntity(this);
			smlAsteriods[n].load("images/asteroidsmall.png");
		}
		
		for(int n=0; n<4;n++) {
			tnyAsteriods[n] = new ImageEntity(this);
			tnyAsteriods[n].load("images/asteroidtiny.png");
		}
	
		for(int n=0;n<ASTEROIDS;n++) {
			createAsteroid();
		}
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
				sprite.drawBounds(Color.RED);
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
		case KeyEvent.VK_CONTROL:
			keyFire = true;
			break;
		}
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
		case KeyEvent.VK_CONTROL:
			keyFire = false;
			break;
		}
	}
	
	public void gameMouseDown() {}
	public void gameMouseUp() {}
	public void gameMouseMoved() {}

	
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
			startBigExplosion(sprite.position());
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
		
		
	}
	
	
}
