package entities;

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;

public class AnimatedSprite extends Sprite{
	
    private ImageEntity animImage;
    BufferedImage tempImage;
    Graphics2D tempSurface;
    private int currFrame, totFrames;
    private int animDir;
    private int frCount, frDelay;
    private int frWidth, frHeight;
    private int cols;
	
	public AnimatedSprite(JFrame _frame, Graphics2D _g2d) {
		super(_frame, _g2d);
        animImage = new ImageEntity(_frame);
        currFrame = 0;
        totFrames = 0;
        animDir = 1;
        frCount = 0;
        frDelay = 0;
        frWidth = 0;
        frHeight = 0;
        cols = 0;
	}
	
    public void load(String filename, int columns, int rows,
            int width, int height)
        {
            //load the tiled animation bitmap
            animImage.load(filename);
            setColumns(columns);
            setTotalFrames(columns * rows);
            setFrameWidth(width);
            setFrameHeight(height);

            //frame image is passed to parent class for drawing
            tempImage = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
            tempSurface = tempImage.createGraphics();
            super.setImage(tempImage);
        }

        public int currentFrame() { return currFrame; }
        public void setCurrentFrame(int frame) { currFrame = frame; }

        public int frameWidth() { return frWidth; }
        public void setFrameWidth(int width) { frWidth = width; }

        public int frameHeight() { return frHeight; }
        public void setFrameHeight(int height) { frHeight = height; }

        public int totalFrames() { return totFrames; }
        public void setTotalFrames(int total) { totFrames = total; }

        public int animationDirection() { return animDir; }
        public void setAnimationDirection(int dir) { animDir = dir; }

        public int frameDelay() { return frDelay; }
        public void setFrameDelay(int delay) { frDelay = delay; }

        public int columns() { return cols; }
        public void setColumns(int num) { cols = num; }

        public Image getAnimImage() { return animImage.getImage(); }
        public void setAnimImage(Image image) { animImage.setImage(image); }

        public void updateAnimation() {
            frCount += 1;
            if (frCount > frDelay) {
                frCount = 0;
                //update the animation frame
                currFrame += animDir;
                if (currFrame > totFrames - 1) {
                    currFrame = 0;
                }
                else if (currFrame < 0) {
                    currFrame = totFrames - 1;
                }
            }
        }

        public void updateFrame() {
            if (totFrames > 0) {
                //calculate the current frame's X and Y position
                int frameX = (currentFrame() % columns()) * frameWidth();
                int frameY = (currentFrame() / columns()) * frameHeight();

                if (tempImage == null) {
                    tempImage = new BufferedImage(frameWidth(), frameHeight(),
                                                  BufferedImage.TYPE_INT_ARGB);
                    tempSurface = tempImage.createGraphics();
                }

                //copy the frame onto the temp image
                if (animImage.getImage() != null) {
                    tempSurface.drawImage(animImage.getImage(), 0, 0, frameWidth() - 1,
                    frameHeight() - 1, frameX, frameY,
                    frameX + frameWidth(),
                    frameY + frameHeight(), animImage.getFrame());
                }
                //pass the temp image on to the parent class and draw it
                super.setImage(tempImage);
            }
        }
}
