package ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class DrawingBoard extends JFrame{
	private static final long serialVersionUID = 5450950810919398177L;
	
	static int TILE_SIZE = 256;
	int panX = 0,panY = 0;
	int ERASER_SIZE = 42;
	int LINE_THICKNESS = 3;
	MainWindow mainWindowRef = null;
	
	class ImageZone{//a region the can be draw on that has position
		BufferedImage image;
		int x,y;
		
		ImageZone(int x,int y){
			image = new BufferedImage(TILE_SIZE,TILE_SIZE,BufferedImage.TYPE_INT_RGB);
			this.x = x;
			this.y = y;
			Graphics g = image.createGraphics();
			g.setColor(mainWindowRef.BACKGROUND_COLOR);
			g.fillRect(0, 0, TILE_SIZE, TILE_SIZE);
			g.dispose();
		}
		
		boolean inFrame() {//is the zone is contained in the window
			return x+TILE_SIZE+panX >= 0 &&
					y+TILE_SIZE+panY >= 0 &&
					x+panX < getWidth() &&
					y+panY < getHeight();
		}
		boolean containsPoint(int xp,int yp) {//check if a point is within the zone
			return xp >= x && yp >= y && xp < x+TILE_SIZE && yp < y+TILE_SIZE;
		}
		
		void paint(Graphics g) {//draw the image zone
			g.drawImage(image,x+panX,y+panY,TILE_SIZE,TILE_SIZE,null );
			//g.setColor(Color.blue);
			//g.drawRect(x+panX,y+panY,TILE_SIZE,TILE_SIZE);
		}
		
		@Override
		public String toString() {
			return "x:"+x+" y:"+y;
		}
	}
	
	ArrayList<ImageZone> zones = new ArrayList<ImageZone>();//collection of all drawing zones
	
	ImageZone getZone(int x,int y) {//look for a zone in a particular location
		for(ImageZone zone:zones) {
			if(zone.containsPoint(x, y)) {
				return zone;
			}
		}
		//none found so make a new one
		ImageZone zone = new ImageZone((Integer.divideUnsigned(x, TILE_SIZE))*TILE_SIZE,(Integer.divideUnsigned(y, TILE_SIZE))*TILE_SIZE);
		zones.add(zone);
		return zone;
	}
	
	void paintZones(Graphics g) {
		for(ImageZone zone:zones) {
			if(zone.inFrame()) {
				zone.paint(g);
			}
		}
	}
	
	boolean arrowUp = false,arrowDown = false,
			arrowLeft = false,arrowRight = false;
	
	boolean mousePressed = false;
	int buttonNumber = 0;
	
	Point mouse = null,pMouse = null;
	
	void drawLine() {
		ImageZone mainZone = getZone(mouse.x-panX,mouse.y-panY);
		ImageZone edgeZone = getZone(pMouse.x-panX,pMouse.y-panY);
		
		if(mainZone != edgeZone) {//the line goes over two regions
			
			int shiftX = mainZone.x+panX,shiftY = mainZone.y+panY;
			Graphics2D g = mainZone.image.createGraphics();
			
			g.setStroke(new BasicStroke(LINE_THICKNESS));
			g.setColor(mainWindowRef.FOREGROUND_COLOR);
			g.drawLine(mouse.x-shiftX, mouse.y-shiftY, pMouse.x-shiftX, pMouse.y-shiftY);
			g.dispose();
			
			shiftX = edgeZone.x+panX;
			shiftY = edgeZone.y+panY;
			
			g = edgeZone.image.createGraphics();
			
			g.setStroke(new BasicStroke(LINE_THICKNESS));
			g.setColor(mainWindowRef.FOREGROUND_COLOR);
			g.drawLine(mouse.x-shiftX, mouse.y-shiftY, pMouse.x-shiftX, pMouse.y-shiftY);
			g.dispose();
			
		}else {//same region
			Graphics2D g = mainZone.image.createGraphics();
			
			g.setStroke(new BasicStroke(LINE_THICKNESS));
			g.setColor(mainWindowRef.FOREGROUND_COLOR);
			int shiftX = mainZone.x+panX,shiftY = mainZone.y+panY;
			g.drawLine(mouse.x-shiftX, mouse.y-shiftY, pMouse.x-shiftX, pMouse.y-shiftY);
			g.dispose();
		}
	}
	
	void eraser() {
		//the eraser can go over many regions
		ImageZone[] possibleZones = {
				getZone(mouse.x-panX,mouse.y-panY),
				
				
				getZone(mouse.x-panX-ERASER_SIZE/2,mouse.y-panY-ERASER_SIZE/2),
				getZone(mouse.x-panX+ERASER_SIZE/2,mouse.y-panY-ERASER_SIZE/2),
				getZone(mouse.x-panX-ERASER_SIZE/2,mouse.y-panY+ERASER_SIZE/2),
				getZone(mouse.x-panX+ERASER_SIZE/2,mouse.y-panY+ERASER_SIZE/2),
		};
		
		outer:for(int i = 0;i<5;i++) {
			ImageZone zone = possibleZones[i];
			
			//check if the zone has already been covered
			for(int j = 0;j<i;j++) if(possibleZones[i] == possibleZones[j]) continue outer;
			
			int shiftX = zone.x+panX,shiftY = zone.y+panY;
			
			Graphics g = zone.image.createGraphics();
			g.setColor(mainWindowRef.BACKGROUND_COLOR);
			g.fillOval(mouse.x-shiftX-ERASER_SIZE/2, mouse.y-shiftY-ERASER_SIZE/2,ERASER_SIZE,ERASER_SIZE);
			g.dispose();
		}
	}
	
	public DrawingBoard(MainWindow mainWindow) {
		super("infinite drawing board");
		mainWindowRef = mainWindow;
		this.setSize(600, 600);
		this.setAlwaysOnTop(mainWindow.KEEP_WINDOW_ON_TOP);
		
		JPanel panel = new JPanel() {
			private static final long serialVersionUID = -1233355333293061090L;
			@Override
			public void paintComponent(Graphics g) {
				g.setColor(mainWindowRef.BACKGROUND_COLOR);
				g.fillRect(0, 0, getWidth(), getHeight());
				
				paintZones(g);//draw the paint zones
			}
		};
		
		this.add(panel);
		this.setVisible(true);
		
		panel.setFocusable(true);
		panel.setFocusTraversalKeysEnabled(true);
		
		panel.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_UP) {
					arrowUp = true;
				}
				if(e.getKeyCode() == KeyEvent.VK_DOWN) {
					arrowDown = true;
				}
				if(e.getKeyCode() == KeyEvent.VK_LEFT) {
					arrowLeft = true;
				}
				if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
					arrowRight = true;
				}
				if(e.getKeyChar() == 'c') {
					zones.clear();
					panX = 0;
					panY = 0;
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_UP) {
					arrowUp = false;
				}
				if(e.getKeyCode() == KeyEvent.VK_DOWN) {
					arrowDown = false;
				}
				if(e.getKeyCode() == KeyEvent.VK_LEFT) {
					arrowLeft = false;
				}
				if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
					arrowRight = false;
				}
			}
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
		});
		
		panel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			@Override
			public void mouseExited(MouseEvent e) {
			}
			@Override
			public void mousePressed(MouseEvent e) {
				mousePressed = true;
				buttonNumber = e.getButton();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mousePressed = false;
			}
			
		});
		
		Thread repaintThread = new Thread() {
			@Override
			public void run() {
				while(DrawingBoard.this.isVisible() && mainWindowRef.isVisible()) {
					mouse = panel.getMousePosition();
					if(mousePressed && mouse != null && pMouse != null) {
						if(buttonNumber == 1) {//left click mouse used for pen
							drawLine();
						}else {//right click mouse used for eraser
							eraser();
						}
					}
					pMouse = mouse;
					
					if(arrowUp) {
						panY+=5;
						if(pMouse != null) pMouse.y+=5;
					}
					if(arrowDown) {
						panY-=5;
						if(pMouse != null) pMouse.y-=5;
					}
					if(arrowLeft) {
						panX+=5;
						if(pMouse != null) pMouse.x+=5;
					}
					if(arrowRight) {
						panX-=5;
						if(pMouse != null) pMouse.x-=5;
					}
					
					panel.repaint();
					try {
						Thread.sleep(1000/60);
					} catch (InterruptedException e) {
					}
					
				}
				DrawingBoard.this.dispose();//free window
			}
		};
		repaintThread.start();
	}
}
