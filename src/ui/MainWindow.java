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
	
	static void setButtonTheme(JButton b){
		b.setOpaque(true);
		b.setBorderPainted(false);
		b.setBackground(new Color(100,100,100));
		b.setForeground(Color.white);
	}
	
	JPanel createTopMenu() {
		JPanel topMenu = new JPanel();
		topMenu.setBackground(Color.DARK_GRAY);
		
		//save stack Button
		JButton saveStackButton = new JButton("save stack");
		saveStackButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveWindow.setVisible(true);
			}
		});
		setButtonTheme(saveStackButton);

		//open stack button
		JButton openStackButton = new JButton("open stack");
		openStackButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openWindow.setVisible(true);
			}
			
		});
		setButtonTheme(openStackButton);
		//open help menu
		JButton helpButton = new JButton("help");
		helpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Help();
			}
			
		});
		setButtonTheme(helpButton);
		
		JButton plotButton = new JButton("plot/graph");
		plotButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				currentStackEditor.command("plot");
			}
			
		});
		setButtonTheme(plotButton);
		
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
		JTextField entryArea = new JTextField("enter expression here",30);
		stackEditorPanel.setBackground(Color.LIGHT_GRAY);
		//stack view
		JPanel stackView = new JPanel();
		stackView.setLayout(null);
		JList<Expr> stackViewList = new JList<Expr>(currentStackEditor.stack);
		stackViewList.setFont(font);
		stackViewList.setBackground(Color.DARK_GRAY);
		stackViewList.setForeground(Color.LIGHT_GRAY);
		stackViewList.setSelectionBackground(Color.DARK_GRAY);
		stackViewList.setSelectionForeground(Color.LIGHT_GRAY);
		
		stackView.setBackground(new Color(100,100,100));
		JScrollPane scrollPane = new JScrollPane(stackViewList);
		
		int cellHeight = font.getSize()*2;
		stackViewList.setFixedCellHeight(cellHeight);
		currentStackEditor.stack.addListDataListener(new ListDataListener(){
			void reset(){
				scrollPane.setLocation(0,0);
				int stackSize = currentStackEditor.size();
				scrollPane.setSize(stackView.getWidth(),Math.max(Math.min(cellHeight*stackSize,stackView.getHeight()),cellHeight*2));
				scrollPane.setLocation(0,stackView.getHeight()-scrollPane.getHeight());
			}
			@Override
			public void intervalAdded(ListDataEvent e) {
				reset();
			}
			@Override
			public void intervalRemoved(ListDataEvent e) {
				reset();
			}
			@Override
			public void contentsChanged(ListDataEvent e) {
				reset();
			}
		});
		
		stackView.add(scrollPane);
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
		JLabel variablesLabel = new JLabel("variables");
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
		setSize(1024,768);
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
