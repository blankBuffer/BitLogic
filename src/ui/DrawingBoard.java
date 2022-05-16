package ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DrawingBoard extends JFrame{
	private static final long serialVersionUID = 5450950810919398177L;
	
	static int TILE_SIZE = 256;
	int panX = 0,panY = 0;
	int ERASER_SIZE = 42;
	int LINE_THICKNESS = 3;
	
	private Color BACKGROUND_COLOR = Color.black,FOREGROUND_COLOR = Color.white;
	private Color paintColor = FOREGROUND_COLOR;
	
	public DrawingBoard() {
		super("infinite drawing board");
		
		UI.WINDOW_COUNT++;
		
		setSize(900, 600);
		this.setMinimumSize(new Dimension(900,400));
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new FlowLayout());
		
		JButton clearButton = new JButton("clear");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {clear();}
		});
		controlsPanel.add(clearButton);
		
		JColorChooser colorChooser = new JColorChooser(paintColor);
		JButton changeColor = new JButton("change color");
		changeColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame colorWindow = new JFrame("choose a color");
				JButton okButton = new JButton("choose");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						paintColor = colorChooser.getColor();
					}
				});
				colorWindow.setLayout(new BorderLayout());
				colorWindow.setSize(400, 400);
				colorWindow.add(colorChooser,BorderLayout.CENTER);
				colorWindow.add(okButton,BorderLayout.SOUTH);
				colorWindow.setVisible(true);
			}
		});
		controlsPanel.add(changeColor);
		
		{//panning buttons
			JButton moveLeftButton = new JButton("←");
			moveLeftButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					panRight(40);
				}
			});
			controlsPanel.add(moveLeftButton);
			
			JButton moveRightButton = new JButton("→");
			moveRightButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					panLeft(40);
				}
			});
			controlsPanel.add(moveRightButton);
			
			JButton moveUpButton = new JButton("↑");
			moveUpButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					panDown(40);
				}
			});
			controlsPanel.add(moveUpButton);
			
			JButton moveDownButton = new JButton("↓");
			moveDownButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					panUp(40);
				}
			});
			controlsPanel.add(moveDownButton);
		}
		
		controlsPanel.add(new JLabel("set pen size"));
		
		JSlider penSizeSlider = new JSlider(1,20,LINE_THICKNESS);
		penSizeSlider.setSnapToTicks(true);
		penSizeSlider.setMajorTickSpacing(2);
		penSizeSlider.setPaintTicks(true);
		penSizeSlider.setPaintLabels(true);
		penSizeSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				LINE_THICKNESS = penSizeSlider.getValue();
			}
		});
		controlsPanel.add(penSizeSlider);
		
		JButton saveButton = new JButton("save image");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveImage();
			}
		});
		controlsPanel.add(saveButton);
		
		mainPanel.add(controlsPanel,BorderLayout.NORTH);
		
		JPanel drawingPanel = new JPanel() {
			private static final long serialVersionUID = -1233355333293061090L;
			@Override
			public void paintComponent(Graphics g) {
				g.setColor(BACKGROUND_COLOR);
				g.fillRect(0, 0, getWidth(), getHeight());
				
				paintZones(g);//draw the paint zones
			}
		};
		
		mainPanel.add(drawingPanel,BorderLayout.CENTER);
		
		this.add(mainPanel);
		this.setVisible(true);
		
		drawingPanel.setFocusable(true);
		
		drawingPanel.addKeyListener(new KeyListener() {
			
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
					clear();
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
		
		drawingPanel.addMouseListener(new MouseListener() {
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
				while(DrawingBoard.this.isVisible()) {
					mouse = drawingPanel.getMousePosition();
					if(mousePressed && mouse != null && pMouse != null) {
						drawingPanel.requestFocus();
						if(buttonNumber == 1) {//left click mouse used for pen
							drawLine();
						}else {//right click mouse used for eraser
							eraser();
						}
					}
					pMouse = mouse;
					
					if(arrowUp) {
						panDown(5);
					}
					if(arrowDown) {
						panUp(5);
					}
					if(arrowLeft) {
						panRight(5);
					}
					if(arrowRight) {
						panLeft(5);
					}
					
					drawingPanel.repaint();
					try {
						Thread.sleep(1000/60);
					} catch (InterruptedException e) {
					}
					
				}
				UI.WINDOW_COUNT--;
				DrawingBoard.this.dispose();//free window
			}
		};
		repaintThread.start();
	}
	
	public void panLeft(int amount) {
		panX-=amount;
		if(pMouse != null) pMouse.x-=5;
	}
	public void panRight(int amount) {
		panX+=amount;
		if(pMouse != null) pMouse.x+=amount;
	}
	public void panUp(int amount) {
		panY-=amount;
		if(pMouse != null) pMouse.y-=amount;
	}
	public void panDown(int amount) {
		panY+=amount;
		if(pMouse != null) pMouse.y+=amount;
	}
	
	public void clear() {
		zones.clear();
		panX = 0;
		panY = 0;
	}
	
	class ImageZone{//a region the can be draw on that has position
		BufferedImage image;
		int x,y;
		
		ImageZone(int x,int y){
			image = new BufferedImage(TILE_SIZE,TILE_SIZE,BufferedImage.TYPE_INT_RGB);
			this.x = x;
			this.y = y;
			Graphics g = image.createGraphics();
			g.setColor(BACKGROUND_COLOR);
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
		Stroke roundStroke = new BasicStroke(LINE_THICKNESS,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
		ImageZone[] possibleZones = {
				getZone(pMouse.x-panX,pMouse.y-panY),
				getZone(mouse.x-panX,mouse.y-panY),
				
				getZone(mouse.x-panX-LINE_THICKNESS/2,mouse.y-panY-LINE_THICKNESS/2),
				getZone(mouse.x-panX+LINE_THICKNESS/2,mouse.y-panY-LINE_THICKNESS/2),
				getZone(mouse.x-panX-LINE_THICKNESS/2,mouse.y-panY+LINE_THICKNESS/2),
				getZone(mouse.x-panX+LINE_THICKNESS/2,mouse.y-panY+LINE_THICKNESS/2),
		};
		
		outer:for(int i = 0;i<5;i++) {
			ImageZone zone = possibleZones[i];
			
			//check if the zone has already been covered
			for(int j = 0;j<i;j++) if(possibleZones[i] == possibleZones[j]) continue outer;
			
			int shiftX = zone.x+panX,shiftY = zone.y+panY;
			
			Graphics2D g = zone.image.createGraphics();
			g.setColor(paintColor);
			g.setStroke(roundStroke);
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
			g.setColor(BACKGROUND_COLOR);
			g.fillOval(mouse.x-shiftX-ERASER_SIZE/2, mouse.y-shiftY-ERASER_SIZE/2,ERASER_SIZE,ERASER_SIZE);
			g.dispose();
		}
	}
	
	void saveImage() {
		String fileName = JOptionPane.showInputDialog(this, "choose file name");
		if(fileName != null) {
			fileName="saves/"+fileName+".jpg";
			int xMin = 0,xMax = 0,yMin = 0,yMax = 0;
			for(ImageZone zone:zones) {
				xMin = Math.min(xMin, zone.x);
				xMax = Math.max(xMax, zone.x);
				
				yMin = Math.min(yMin, zone.y);
				yMax = Math.max(yMax, zone.y);
			}
			int width=xMax-xMin+TILE_SIZE,height=yMax-yMin+TILE_SIZE;
			BufferedImage outImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
			Graphics g = outImage.createGraphics();
			g.setColor(BACKGROUND_COLOR);
			g.fillRect(0, 0, width, height);
			
			for(int x = xMin;x<=xMax;x+=TILE_SIZE) {
				for(int y = yMin;y<=yMax;y+=TILE_SIZE) {
					for(ImageZone zone:zones) {
						if(zone.containsPoint(x, y)) {
							g.drawImage(zone.image,x-xMin,y-yMin,TILE_SIZE,TILE_SIZE,null);
							continue;
						}
					}
				}
			}
			
			g.dispose();
			try {
				ImageIO.write(outImage, "jpg", new File(fileName));
				System.out.println("image saved!");
			} catch (Exception e) {
				System.err.println("could not write image!");
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public Color getBackground() {
		return BACKGROUND_COLOR;
	}
	@Override
	public Color getForeground() {
		return FOREGROUND_COLOR;
	}
}
