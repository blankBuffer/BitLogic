import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import cas.*;

public class MainWindow extends JFrame{

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
		stackEditorPanel.setBackground(Color.LIGHT_GRAY);
		
		JList<Expr> stackView =  new JList<Expr>(currentStackEditor.stack);
		stackView.setBackground(new Color(200,220,255));
		//entry area
		JTextField entryArea = new JTextField(30);
		entryArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				currentStackEditor.command(entryArea.getText());
				entryArea.setText(null);
				
			}
		});
		//stack view
		stackView.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Expr selected = stackView.getSelectedValue();
				if(selected != null) entryArea.setText(selected.toString());
			}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
		});
		
		stackEditorPanel.setLayout(new BorderLayout());
		stackEditorPanel.add(stackView);
		stackEditorPanel.add(entryArea,BorderLayout.NORTH);
		JScrollPane scrollBars = new JScrollPane(stackView);
		stackEditorPanel.add(scrollBars);
		
		return stackEditorPanel;
	}
	
	JPanel createDefsView() {
		JPanel defsPanel = new JPanel();
		
		JList<Func> funcDefsList =  new JList<Func>(currentStackEditor.currentDefs.functionsArrayList);
		funcDefsList.setBackground(new Color(255,220,220));
		JList<Equ> varsDefsList =  new JList<Equ>(currentStackEditor.currentDefs.varsArrayList);
		varsDefsList.setBackground(new Color(255,220,220));
		defsPanel.add(funcDefsList);
		defsPanel.add(varsDefsList);
		
		defsPanel.setLayout(new BoxLayout(defsPanel,BoxLayout.Y_AXIS));
		defsPanel.add(new JLabel("functions"));
		defsPanel.add(new JScrollPane(funcDefsList));
		defsPanel.add(new JLabel("variables"));
		defsPanel.add(new JScrollPane(varsDefsList));
		
		
		return defsPanel;
	}
	
	void init() {//adding elements
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(2,2));
		panel.add(createTopMenu(),BorderLayout.NORTH);
		panel.add(createStackEditor(),BorderLayout.CENTER);
		panel.add(createDefsView(),BorderLayout.WEST);
		add(panel);
		setSize(600,600);
		setLocationRelativeTo(null);
		//
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		saveWindow = new SaveStackWindow(this);
		openWindow = new OpenStackWindow(this);
	}
	
	MainWindow(){
		super("Ben's Tool Box / BitLogic "+Main.VERSION);
		currentStackEditor = new StackEditor();
		init();
	}
	
	MainWindow(StackEditor stackEditor){
		super("Ben's Tool Box / BitLogic "+Main.VERSION);
		this.currentStackEditor = stackEditor;
		init();
		
	}

}
