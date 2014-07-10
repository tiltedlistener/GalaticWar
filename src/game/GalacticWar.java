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
	
	// Game States
	final int GAME_MENU = 0;
	final int GAME_RUNNING = 1;
	final int GAME_OVER = 2;
	
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
	final int SPRITE_POWERUP_SHIELD = 300;
	final int SPRITE_POWERUP_HEALTH = 301;
	final int SPRITE_POWERUP_250 = 302;
	final int SPRITE_POWERUP_500 = 303;
	final int SPRITE_POWERUP_1000 = 304;
	final int SPRITE_POWERUP_GUN = 305;
	
	// Images
	ImageEntity background;
	ImageEntity bulletImage;
	ImageEntity[] bigAsteroids;
	ImageEntity[] medAsteroids;
	ImageEntity[] smlAsteroids;
	ImageEntity[] tnyAsteroids;
	ImageEntity[] explosions;
	ImageEntity[] shipImage;
	ImageEntity[] barImage;
	ImageEntity barFrame;
	ImageEntity powerupShield;
	ImageEntity powerupHealth;
	ImageEntity powerup250;
	ImageEntity powerup500;
	ImageEntity powerup1000;
	ImageEntity powerupGun;
	
	// Health and Shield 
	int health = 20;
	int shield = 20;
	int score = 0;
	int highscore = 0;
	int firepower = 1;
	int gameState = GAME_MENU;
	
	// Support
	AffineTransform identity;
	Random rand;
	
	// Dev support
	boolean showBounds = false;
	boolean collisionTesting = true;
	long collisionTimer = 0;
	
	// Keyboard
	boolean keyDown, keyUp, keyLeft, keyRight, keyFire, keyShield;	
	
	public GalacticWar() { 		
		super(FRAMERATE, SCREENWIDTH, SCREENHEIGHT);
	}
	
	void gameStartup() {
		
		// Set variables
		identity = new AffineTransform();
		rand = new Random();
		bigAsteroids = new ImageEntity[5];
		medAsteroids = new ImageEntity[2];
		smlAsteroids = new ImageEntity[3];
		tnyAsteroids = new ImageEntity[4];
		explosions = new ImageEntity[2];
		shipImage = new ImageEntity[3];
		barImage = new ImageEntity[2];
		
		// Health/Shield
		barFrame = new ImageEntity(this);
		barFrame.load("images/barframe.png");
		barImage[0] = new ImageEntity(this);
		barImage[0].load("images/bar_health.png");
		barImage[1] = new ImageEntity(this);
		barImage[1].load("images/bar_shield.png");
		
		// Power ups
		powerupShield = new ImageEntity(this);
		powerupShield.load("images/powerup_shield2.png");
		powerupHealth = new ImageEntity(this);
		powerupHealth.load("images/powerup_cola.png");
		powerup250 = new ImageEntity(this);
		powerup250.load("images/powerup_250.png");
		powerup500 = new ImageEntity(this);
		powerup500.load("images/powerup_500.png");
		powerup1000 = new ImageEntity(this);
		powerup1000.load("images/powerup_1000.png");
		powerupGun = new ImageEntity(this);
		powerupGun.load("images/powerup_gun.png");
		
		// What it is
		background = new ImageEntity(this);
		background.load("images/bluespace.png");
		
		
		
		// Ship forms
		shipImage[0] = new ImageEntity(this);
		shipImage[0].load("images/spaceship.png");
		shipImage[1] = new ImageEntity(this);
		shipImage[1].load("images/ship_thrust.png");
		shipImage[2] = new ImageEntity(this);
		shipImage[2].load("images/ship_shield.png");		
		
		AnimatedSprite ship = new AnimatedSprite(this, graphics());
		ship.setSpriteType(SPRITE_SHIP);
		ship.setImage(shipImage[0].getImage());
		ship.setFrameWidth(ship.imageWidth());
		ship.setFrameHeight(ship.imageHeight());
		ship.setPosition(new Point2D(CENTERX, CENTERY));
		ship.setAlive(true);
		ship.setState(STATE_EXPLODING);
		collisionTimer = System.currentTimeMillis();
		sprites().add(ship);
		
		// Bullets
		bulletImage = new ImageEntity(this);
		bulletImage.load("images/plasmashot.png");
		
		// Explosions
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
		
		pauseGame();
		start();
		
	}
	
	/**
	 * GAME STATE
	 */
	public void gameShutdown() {
		// No sound in my version at the moment
	}
	
	public void gameTimeUpdate() {
		checkInput();
		if (!gamePaused() && sprites().size() == 1) {
			resetGame();
			gameState = GAME_OVER;
		}
	}
	
	private void resetGame() {
		AnimatedSprite ship = (AnimatedSprite) sprites().get(0);
		sprites().clear();
		ship.setPosition(new Point2D(SCREENWIDTH/2, SCREENHEIGHT/2));
		ship.setAlive(true);
		ship.setState(STATE_EXPLODING);
		collisionTimer = System.currentTimeMillis();
		ship.setVelocity(new Point2D(0,0));
		sprites().add(ship);
		for(int i=0;i<ASTEROIDS;i++) {
			createAsteroid();
		}
		
		health = 20;
		shield = 20;
		score = 0;
		firepower = 2;
	}
	
	public void gameRefreshScreen() {
		Graphics2D g2d = graphics();
		
		// AnimatedSprite ship = (AnimatedSprite)sprites().get(0);
		g2d.drawImage(background.getImage(), 0,0, SCREENWIDTH - 1, SCREENHEIGHT - 1, this);
		
        if (gameState == GAME_MENU) {
            g2d.setFont(new Font("Verdana", Font.BOLD, 36));
            g2d.setColor(Color.BLACK);
            g2d.drawString("GALACTIC WAR", 252, 202);
            g2d.setColor(new Color(200,30,30));
            g2d.drawString("GALACTIC WAR", 250, 200);

            int x = 270, y = 15;
            g2d.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 20));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("CONTROLS:", x, ++y*20);
            g2d.drawString("ROTATE - Left/Right Arrows", x+20, ++y*20);
            g2d.drawString("THRUST - Up Arrow", x+20, ++y*20);
            g2d.drawString("SHIELD - Shift key (no scoring)", x+20, ++y*20);
            g2d.drawString("FIRE - Ctrl key", x+20, ++y*20);

            g2d.setColor(Color.WHITE);
            g2d.drawString("POWERUPS INCREASE FIREPOWER!", 240, 480);

            g2d.setFont(new Font("Ariel", Font.BOLD, 24));
            g2d.setColor(Color.ORANGE);
            g2d.drawString("Press ENTER to start", 280, 570);
        }
        else if (gameState == GAME_RUNNING) {
            //draw health/shield bars and meters
            g2d.drawImage(barFrame.getImage(), SCREENWIDTH - 132, 18, this);
            for (int n = 0; n < health; n++) {
                int dx = SCREENWIDTH - 130 + n * 5;
                g2d.drawImage(barImage[0].getImage(), dx, 20, this);
            }
            g2d.drawImage(barFrame.getImage(), SCREENWIDTH - 132, 33, this);
            for (int n = 0; n < shield; n++) {
                int dx = SCREENWIDTH - 130 + n * 5;
                g2d.drawImage(barImage[1].getImage(), dx, 35, this);
            }

            //draw the bullet upgrades
            for (int n = 0; n < firepower; n++) {
                int dx = SCREENWIDTH - 220 + n * 13;
                g2d.drawImage(powerupGun.getImage(), dx, 17, this);
            }

            //display the score
            g2d.setFont(new Font("Verdana", Font.BOLD, 24));
            g2d.setColor(Color.WHITE);
            g2d.drawString("" + score, 20, 40);
            g2d.setColor(Color.RED);
            g2d.drawString("" + highscore, 350, 40);
        }
        else if (gameState == GAME_OVER) {
            g2d.setFont(new Font("Verdana", Font.BOLD, 36));
            g2d.setColor(new Color(200, 30, 30));
            g2d.drawString("GAME OVER", 270, 200);

            g2d.setFont(new Font("Arial", Font.CENTER_BASELINE, 24));
            g2d.setColor(Color.ORANGE);
            g2d.drawString("Press ENTER to restart", 260, 500);
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
		case SPRITE_POWERUP_SHIELD:	
		case SPRITE_POWERUP_HEALTH:	
		case SPRITE_POWERUP_250:	
		case SPRITE_POWERUP_500:
		case SPRITE_POWERUP_1000:	
		case SPRITE_POWERUP_GUN:
			warp(sprite);
			double rot = sprite.rotationRate();
			if(sprite.faceAngle() > 350) {
				sprite.setRotationRate(rot * -1);
				sprite.setFaceAngle(350);
			} else if (sprite.faceAngle() < 10) {
				sprite.setRotationRate(rot * -1);
				sprite.setFaceAngle(10);
			}
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
				bumpScore(5);
				spr1.setAlive(false);
				spr2.setAlive(false);
				breakAsteroid(spr2);
			}
			break;
		case SPRITE_SHIP:
			if(isAsteroid(spr2.spriteType())) {
				if(spr1.state() == STATE_NORMAL) {
					if (keyShield) {
						shield -=1;
					} else {
						collisionTimer = System.currentTimeMillis();
						spr1.setVelocity(new Point2D(0, 0));
						double x = spr1.position().X() - 10;
						double y = spr1.position().Y() - 10;
						startBigExplosion(new Point2D(x, y));
						spr1.setState(STATE_EXPLODING);
						health -=1;
						if(health < 0){
							gameState = GAME_OVER;
						}
						firepower--;
						if(firepower < 1) firepower = 1;
					}
					spr2.setAlive(false);
					breakAsteroid(spr2);
				} else if(spr1.state() == STATE_EXPLODING){
					if(collisionTimer + 3000 < System.currentTimeMillis()) {
						spr1.setState(STATE_NORMAL);
					}
				}
			}
			break;
        case SPRITE_POWERUP_SHIELD:
            if (spr2.spriteType()==SPRITE_SHIP) {
                shield += 5;
                if (shield > 20) shield = 20;
                spr1.setAlive(false);
            }
            break;

        case SPRITE_POWERUP_HEALTH:
            if (spr2.spriteType()==SPRITE_SHIP) {
                health += 5;
                if (health > 20) health = 20;
                spr1.setAlive(false);
            }
            break;

        case SPRITE_POWERUP_250:
            if (spr2.spriteType()==SPRITE_SHIP) {
                bumpScore(250);
                spr1.setAlive(false);
            }
            break;

        case SPRITE_POWERUP_500:
            if (spr2.spriteType()==SPRITE_SHIP) {
                bumpScore(500);
                spr1.setAlive(false);
            }
            break;

        case SPRITE_POWERUP_1000:
            if (spr2.spriteType()==SPRITE_SHIP) {
                bumpScore(1000);
                spr1.setAlive(false);
            }
            break;

        case SPRITE_POWERUP_GUN:
            if (spr2.spriteType()==SPRITE_SHIP) {
                firepower++;
                if (firepower > 5) firepower = 5;
                spr1.setAlive(false);
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
		case KeyEvent.VK_SHIFT:
            if ((!keyUp) && (shield > 0))
                keyShield = true;
            else
                keyShield = false;
            break;

        case KeyEvent.VK_ENTER:
            if (gameState == GAME_MENU) {
                resetGame();
                resumeGame();
                gameState = GAME_RUNNING;
            }
            else if (gameState == GAME_OVER) {
                resetGame();
                resumeGame();
                gameState = GAME_RUNNING;
            }
            break;

        case KeyEvent.VK_ESCAPE:
            if (gameState == GAME_RUNNING) {
                pauseGame();
                gameState = GAME_OVER;
            }
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
		case KeyEvent.VK_SHIFT:
			keyShield = false;
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
		int n = rand.nextInt(100);
		if(n > 12) return;
		
		AnimatedSprite spr = new AnimatedSprite(this, graphics());
		spr.setRotationRate(8);
		spr.setPosition(sprite.position());
		double velx = rand.nextDouble();
		double vely = rand.nextDouble();
		spr.setVelocity(new Point2D(velx, vely));
		spr.setLifespan(500);
		spr.setAlive(true);
		
		//customize the sprite based on powerup type
        switch(rand.nextInt(6)) {
        case 0:
            //create a new shield powerup sprite
            spr.setImage(powerupShield.getImage());
            spr.setSpriteType(SPRITE_POWERUP_SHIELD);
            sprites().add(spr);
            break;

        case 1:
            //create a new health powerup sprite
            spr.setImage(powerupHealth.getImage());
            spr.setSpriteType(SPRITE_POWERUP_HEALTH);
            sprites().add(spr);
            break;

        case 2:
            //create a new 250-point powerup sprite
            spr.setImage(powerup250.getImage());
            spr.setSpriteType(SPRITE_POWERUP_250);
            sprites().add(spr);
            break;

        case 3:
            //create a new 500-point powerup sprite
            spr.setImage(powerup500.getImage());
            spr.setSpriteType(SPRITE_POWERUP_500);
            sprites().add(spr);
            break;

        case 4:
            //create a new 1000-point powerup sprite
            spr.setImage(powerup1000.getImage());
            spr.setSpriteType(SPRITE_POWERUP_1000);
            sprites().add(spr);
            break;

        case 5:
            //create a new gun powerup sprite
            spr.setImage(powerupGun.getImage());
            spr.setSpriteType(SPRITE_POWERUP_GUN);
            sprites().add(spr);
            break;

        }
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
		if (gameState != GAME_RUNNING) return;
		
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
		} else if(keyShield) {
			ship.setImage(shipImage[2].getImage());
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
        //create the new bullet sprite
        AnimatedSprite[] bullets = new AnimatedSprite[6];

        switch(firepower) {
        case 1:
            bullets[0] = stockBullet();
            sprites().add(bullets[0]);
            break;

        case 2:
            bullets[0] = stockBullet();
            adjustDirection(bullets[0], -4);
            sprites().add(bullets[0]);

            bullets[1] = stockBullet();
            adjustDirection(bullets[1], 4);
            sprites().add(bullets[1]);

            break;

        case 3:
            bullets[0] = stockBullet();
            adjustDirection(bullets[0], -4);
            sprites().add(bullets[0]);

            bullets[1] = stockBullet();
            sprites().add(bullets[1]);

            bullets[2] = stockBullet();
            adjustDirection(bullets[2], 4);
            sprites().add(bullets[2]);

            break;

        case 4:
            bullets[0] = stockBullet();
            adjustDirection(bullets[0], -5);
            sprites().add(bullets[0]);

            bullets[1] = stockBullet();
            adjustDirection(bullets[1], 5);
            sprites().add(bullets[1]);

            bullets[2] = stockBullet();
            adjustDirection(bullets[2], -10);
            sprites().add(bullets[2]);

            bullets[3] = stockBullet();
            adjustDirection(bullets[3], 10);
            sprites().add(bullets[3]);

            break;

        case 5:
            bullets[0] = stockBullet();
            adjustDirection(bullets[0], -6);
            sprites().add(bullets[0]);

            bullets[1] = stockBullet();
            adjustDirection(bullets[1], 6);
            sprites().add(bullets[1]);

            bullets[2] = stockBullet();
            adjustDirection(bullets[2], -15);
            sprites().add(bullets[2]);

            bullets[3] = stockBullet();
            adjustDirection(bullets[3], 15);
            sprites().add(bullets[3]);

            bullets[4] = stockBullet();
            adjustDirection(bullets[4], -60);
            sprites().add(bullets[4]);

            bullets[5] = stockBullet();
            adjustDirection(bullets[5], 60);
            sprites().add(bullets[5]);
            break;
        }
	}
	
	private void adjustDirection(AnimatedSprite sprite, double angle) {
		angle = sprite.faceAngle() + angle;
		if (angle < 0 ) angle += 360;
		else if (angle > 360) angle -= 360;
		sprite.setFaceAngle(angle);
		sprite.setMoveAngle(sprite.faceAngle() - 90);
		angle = sprite.moveAngle();
		double svx = calcAngleMoveX(angle) * BULLET_SPEED;
		double svy = calcAngleMoveY(angle) * BULLET_SPEED;
		sprite.setVelocity(new Point2D(svx, svy));
	}
	
	private AnimatedSprite stockBullet() {
		AnimatedSprite ship = (AnimatedSprite) sprites().get(0);
		AnimatedSprite bul = new AnimatedSprite(this, graphics());
		bul.setAlive(true);
		bul.setImage(bulletImage.getImage());
		bul.setFrameWidth(bulletImage.width());
		bul.setFrameHeight(bulletImage.height());
		bul.setSpriteType(SPRITE_BULLET);
		bul.setLifespan(90);
		bul.setFaceAngle(ship.faceAngle());
		bul.setMoveAngle(ship.faceAngle() - 90);
		
		double angle = bul.moveAngle();
		double svx = calcAngleMoveX(angle) * BULLET_SPEED;
		double svy = calcAngleMoveY(angle) * BULLET_SPEED;
		bul.setVelocity(new Point2D(svx, svy));
		
		double x = ship.center().X() - bul.imageWidth()/2;
		double y = ship.center().Y() - bul.imageHeight()/2;
		bul.setPosition(new Point2D(x, y));
		
		return bul;
	}
	
	/**
	 * Scores
	 */
	public void bumpScore(int howmuch) {
		score+= howmuch;
		if (score > highscore) {
			highscore = score;
		}
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
