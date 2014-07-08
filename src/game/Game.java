package game;

import entities.*;
import entities.Point2D;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.lang.System;

abstract class Game extends JFrame implements Runnable, KeyListener, MouseListener, MouseMotionListener {
	
	// Game loop
	private Thread gameLoop;
	
	// Objects list
	private LinkedList<AnimatedSprite> _sprites;
	public LinkedList<AnimatedSprite> sprites() { return _sprites; }
	
	// Graphics rendering
	private BufferedImage backBuffer;
	private Graphics2D g2d;
	private int screenWidth, screenHeight;
	
	// Input controls
	private Point2D mousePos = new Point2D(0,0);
	private boolean mouseButtons[] = new boolean[4];
	
	// frame rate and timing
	private int _frameCount = 0;
	private int _frameRate = 0;
	private int desiredFrameRate;
	private long startTime = System.currentTimeMillis();
	
	// Game state
	private boolean _gamePaused = false;
	public boolean gamePaused() { return _gamePaused; }
	public void pauseGame() { _gamePaused = true; }
	public void resumeGame() { _gamePaused = false; }
	
	// Abstract methods subclasses will implement for their specific games
	abstract void gameStartup();
	abstract void gameTimeUpdate();
	abstract void gameRefreshScreen();
	abstract void gameShutdown();
	abstract void gameKeyDown(int keyCode);
	abstract void gameKeyUp(int keyCode);
	abstract void gameMouseDown();
	abstract void gameMouseUp();
	abstract void gameMouseMove();
	abstract void spriteUpdate(AnimatedSprite sprite);
	abstract void spriteDraw(AnimatedSprite sprite);
	abstract void spriteDying(AnimatedSprite sprite);
	abstract void spriteCollision(AnimatedSprite spr1, AnimatedSprite spr2);
	abstract void checkInput();			// ADDED 
	
	public Graphics2D graphics() { return g2d; }
	public int frameRate() { return _frameRate; }
	
	public boolean mouseButton(int btn) { return mouseButtons[btn]; }
	public Point2D mousePosition() { return mousePos; }
	
	
	public Game(int frameRate, int width, int height) {
		super("GALACTIC WAR");
		this.desiredFrameRate = frameRate;
		this.screenWidth = width;
		this.screenHeight = height;
		init();		// ADDED TO SIMULATE THE APPLET START
	}
	
	public void init() {
		backBuffer = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
		g2d = backBuffer.createGraphics();
		
		_sprites = new LinkedList<AnimatedSprite>();
		
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
				
		gameStartup();
	}
	
	public void paint(Graphics g) {
		g2d.clearRect(0, 0, this.screenWidth, this.screenHeight); // ADDED
		
		_frameCount++;
		if(System.currentTimeMillis() > startTime + 1000) {
			startTime = System.currentTimeMillis();
			_frameRate = _frameCount;
			_frameCount = 0;
			purgeSprites();
		}
		
		gameRefreshScreen();
		
		if(!gamePaused()) {
			drawSprites();
		}

		g.drawImage(backBuffer, 0, 0, this);
	}	
	
	public void start() {		
		// ADDED FOR BOOT
		// Show the screen
		setSize(this.screenWidth, this.screenHeight);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Fire in the hole
		gameLoop = new Thread(this);
		gameLoop.start();
	}
	
	public void run() {
		Thread t = Thread.currentThread();
		while(t == gameLoop) {
			try {
				Thread.sleep(1000/desiredFrameRate);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			if (!gamePaused()) {
				updateSprites();
				testCollisions();
			}
			
			gameTimeUpdate();
			repaint();
		}
	}
	
	public void stop() {
		gameLoop = null;
		gameShutdown();
	}
	
	/** KEYLISTENERS **/
	public void keyTyped(KeyEvent k) {}
	public void keyPressed(KeyEvent k) {
		gameKeyDown(k.getKeyCode());
	}
	public void keyReleased(KeyEvent k) {
		gameKeyUp(k.getKeyCode());
	}
	
	/** MOUSELISTENERS **/
	private void checkButtons(MouseEvent e) {
		switch(e.getButton()) {
		case MouseEvent.BUTTON1:
			mouseButtons[1]=true;
			mouseButtons[2]=false;
			mouseButtons[3]=false;
			break;
		case MouseEvent.BUTTON2:
			mouseButtons[2]=true;
			mouseButtons[1]=false;
			mouseButtons[3]=false;
			break;
		case MouseEvent.BUTTON3:
			mouseButtons[3]=true;
			mouseButtons[2]=false;
			mouseButtons[1]=false;	
			break;
		}
	}
	
	public void mousePressed(MouseEvent e) {
		checkButtons(e);
		mousePos.setX(e.getX());
		mousePos.setY(e.getY());
		gameMouseDown();
	}
	
	public void mouseReleased(MouseEvent e) {
		checkButtons(e);
		mousePos.setX(e.getX());
		mousePos.setY(e.getY());
		gameMouseUp();
	}
	
	public void mouseDragged(MouseEvent e) {
		checkButtons(e);
		mousePos.setX(e.getX());
		mousePos.setY(e.getY());
		gameMouseDown();
		gameMouseMove();
	}
	
	public void mouseEntered(MouseEvent e) {
		mousePos.setX(e.getX());
		mousePos.setY(e.getY());
		gameMouseMove();
	}
	
	public void mouseExited(MouseEvent e) {
		mousePos.setX(e.getX());
		mousePos.setY(e.getY());
		gameMouseMove();
	}
	
	public void mouseClicked(MouseEvent e) {
		
	}
	
	public void mouseMoved(MouseEvent e) {
		
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
	
	/** UPDATE **/
	protected void updateSprites() {
		for(int n=0;n<_sprites.size();n++){
			AnimatedSprite spr = (AnimatedSprite)_sprites.get(n);
			if(spr.alive()) {
				spr.updatePosition();
				spr.updateRotation();
				spr.updateAnimation();
				spriteUpdate(spr);
				spr.updateLifetime();
				if(!spr.alive()) {
					spriteDying(spr);
				}
			}
		}
	}
	
	protected void testCollisions() {
		for (int first=0; first < _sprites.size(); first++) {
			AnimatedSprite spr1 = (AnimatedSprite)_sprites.get(first);
			if(spr1.alive()) {
				for (int second=0; second < _sprites.size(); second++) {
					if (first != second) {
						AnimatedSprite spr2 = (AnimatedSprite)_sprites.get(second);
						if(spr2.alive()) {
							if (spr2.collidesWith(spr1)) {
								spriteCollision(spr1, spr2);
								break;
							} else {
								spr1.setCollided(false);
							}
						}
					}
				}
			}
		}
	}
	
	protected void drawSprites() {
		for (int n=0;n<_sprites.size();n++) {
			AnimatedSprite spr = (AnimatedSprite)_sprites.get(n);
			if(spr.alive()) {
				spr.updateFrame();
				spr.transform();
				spr.draw();
				spriteDraw(spr);
			}
		}
	}
	
	private void purgeSprites() {
		for (int n=0;n<_sprites.size();n++) {
			AnimatedSprite spr = (AnimatedSprite)_sprites.get(n);
			if(!spr.alive()) {
				_sprites.remove(n);
			}
		}
	}
	
	
	
}
