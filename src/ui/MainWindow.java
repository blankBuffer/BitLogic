package ui;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import cas.*;

class MainWindow extends JFrame{

	private static final long serialVersionUID = -3880026026104218593L;
	JFrame saveWindow,openWindow;
	StackEditor currentStackEditor;
	
	
	JPanel createTopMenu() {
		JPanel topMenu = new JPanel();
		topMenu.setBackground(Color.LIGHT_GRAY);
		
		//save stack Button
		JButton saveStackButton = new JButton("save stack");
		saveStackButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveWindow.setVisible(true);
			}
		});
		//open stack button
		JButton openStackButton = new JButton("open stack");
		openStackButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openWindow.setVisible(true);
			}
			
		});
		//open help menu
		JButton helpButton = new JButton("help");
		helpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Help();
			}
			
		});
		
		JButton plotButton = new JButton("plot/graph");
		plotButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				currentStackEditor.command("plot");
			}
			
		});
		
		topMenu.setLayout(new FlowLayout());
		topMenu.add(saveStackButton);
		topMenu.add(openStackButton);
		topMenu.add(helpButton);
		topMenu.add(plotButton);
		
		return topMenu;
	}
	
	
	JPanel createStackEditor() {
		JPanel stackEditorPanel = new JPanel();
		Font font = new Font(null,Font.PLAIN,20);
		JTextField entryArea = new JTextField(30);
		stackEditorPanel.setBackground(Color.LIGHT_GRAY);
		//stack view
		JComponent stackView =  new JComponent() {
			private static final long serialVersionUID = 1L;
			{//contructor
				currentStackEditor.stack.addListDataListener(new ListDataListener(){
					@Override
					public void intervalAdded(ListDataEvent e) {
						repaint();
					}
					@Override
					public void intervalRemoved(ListDataEvent e) {
						repaint();
					}
					@Override
					public void contentsChanged(ListDataEvent e) {
						repaint();
					}
				});
			}
			public void renderBackground(Graphics g) {
				g.setColor(Color.DARK_GRAY);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
			@Override
			public void paint(Graphics g) {
				renderBackground(g);
				g.setFont(font);
				g.setColor(Color.LIGHT_GRAY);
				int stackSize = currentStackEditor.stack.size();
				int elementHeight = font.getSize()*2;
				for(int i = 0;i<stackSize;i++) {
					int y = getHeight()-(stackSize-i)*elementHeight;
					g.drawString(currentStackEditor.stack.get(i).toString(), 0, y+elementHeight/2);
					g.drawLine(0, y, getWidth(),y);
				}
			}
		};
		stackView.setBackground(Color.DARK_GRAY);
		stackView.setForeground(Color.LIGHT_GRAY);
		
		//entry area
		entryArea.setBackground(Color.DARK_GRAY);
		entryArea.setForeground(Color.LIGHT_GRAY);
		entryArea.setCaretColor(Color.white);
		entryArea.setFont(font);
		entryArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentStackEditor.command(entryArea.getText());
				entryArea.setText(null);
				
			}
		});
		
		stackEditorPanel.setLayout(new BorderLayout());
		stackEditorPanel.add(stackView);
		stackEditorPanel.add(entryArea,BorderLayout.SOUTH);
		return stackEditorPanel;
	}
	
	JPanel createDefsView() {
		JPanel defsPanel = new JPanel();
		defsPanel.setBackground(Color.DARK_GRAY.darker());
		
		JList<Func> funcDefsList =  new JList<Func>(currentStackEditor.currentDefs.functionsArrayList);
		funcDefsList.setBackground(new Color(100,100,100));
		funcDefsList.setForeground(Color.WHITE);
		JList<Equ> varsDefsList =  new JList<Equ>(currentStackEditor.currentDefs.varsArrayList);
		varsDefsList.setBackground(new Color(100,100,100));
		varsDefsList.setForeground(Color.WHITE);
		defsPanel.add(funcDefsList);
		defsPanel.add(varsDefsList);
		
		defsPanel.setLayout(new BoxLayout(defsPanel,BoxLayout.Y_AXIS));
		JLabel functionsLabel = new JLabel("functions");
		functionsLabel.setForeground(Color.white);
		defsPanel.add(functionsLabel);
		defsPanel.add(new JScrollPane(funcDefsList));
		JLabel variablesLabel = new JLabel("functions");
		variablesLabel.setForeground(Color.white);
		defsPanel.add(variablesLabel);
		defsPanel.add(new JScrollPane(varsDefsList));
		
		
		return defsPanel;
	}
	
	void init() {//adding elements
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(2,2));
		panel.add(createTopMenu(),BorderLayout.NORTH);
		panel.add(createStackEditor(),BorderLayout.CENTER);
		panel.add(createDefsView(),BorderLayout.WEST);
		panel.setBackground(Color.DARK_GRAY);
		add(panel);
		setSize(600,600);
		setLocationRelativeTo(null);
		//
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		saveWindow = new SaveStackWindow(this);
		openWindow = new OpenStackWindow(this);
	}
	
	public MainWindow(){
		super("Ben's Tool Box / BitLogic ");
		currentStackEditor = new StackEditor();
		init();
	}
	
	public MainWindow(StackEditor stackEditor){
		super("Ben's Tool Box / BitLogic ");
		this.currentStackEditor = stackEditor;
		init();
		
	}

}
