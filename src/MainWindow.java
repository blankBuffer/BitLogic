import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import cas.*;

public class MainWindow extends JFrame{

	private static final long serialVersionUID = -3880026026104218593L;
	JFrame saveWindow,openWindow;
	StackEditor currentStackEditor = new StackEditor();
	
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
		
		topMenu.setLayout(new FlowLayout());
		topMenu.add(saveStackButton);
		topMenu.add(openStackButton);
		topMenu.add(helpButton);
		
		return topMenu;
	}
	
	
	JButton createControlButton(String text,String command) {
		JButton b = new JButton(text);
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentStackEditor.command(command);
			}
		});
		return b;
	}
	
	JPanel createControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.setBackground(Color.GRAY);
		
		controlPanel.setLayout(new BoxLayout(controlPanel,BoxLayout.Y_AXIS));
		
		controlPanel.add(createControlButton("clear","clear"));
		controlPanel.add(createControlButton("duplicate","dup"));
		controlPanel.add(createControlButton("swap","swap"));
		controlPanel.add(createControlButton("pop/delete","pop"));
		controlPanel.add(createControlButton("break apart","break"));
		controlPanel.add(createControlButton("roll","roll"));
		controlPanel.add(createControlButton("undo","undo"));
		controlPanel.add(createControlButton("result","result"));
		controlPanel.add(createControlButton("show graph","plot"));
		
		
		return controlPanel;
	}
	
	JPanel createConstructPanel() {
		JPanel constructPanel = new JPanel();
		constructPanel.setBackground(Color.GRAY);
		
		constructPanel.setLayout(new BoxLayout(constructPanel,BoxLayout.Y_AXIS));
		
		constructPanel.add(createControlButton("[]+[]","+"));
		constructPanel.add(createControlButton("[]-[]","-"));
		constructPanel.add(createControlButton("[]*[]","*"));
		constructPanel.add(createControlButton("[]^[]","^"));
		constructPanel.add(createControlButton("[]/[]","/"));
		constructPanel.add(createControlButton("-[]","--"));
		constructPanel.add(createControlButton("1/[]","inv"));
		constructPanel.add(createControlButton("√[]","sqrt"));
		constructPanel.add(createControlButton("ln[]","ln"));
		constructPanel.add(createControlButton("sin","sin"));
		constructPanel.add(createControlButton("cos","cos"));
		constructPanel.add(createControlButton("tan","tan"));
		constructPanel.add(createControlButton("∂[expr]/∂[var]","diff"));
		constructPanel.add(createControlButton("∫(expr,var)","integrate"));
		constructPanel.add(createControlButton("∫(min,max,expr,var)","integrateOver"));
		constructPanel.add(createControlButton("[]=[]","="));
		constructPanel.add(createControlButton("solve(equ,var)","solve"));
		
		return constructPanel;
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
		stackEditorPanel.add(entryArea,BorderLayout.SOUTH);
		JScrollPane scrollBars = new JScrollPane(stackView);
		stackEditorPanel.add(scrollBars);
		
		return stackEditorPanel;
	}
	
	MainWindow(){
		super("Ben's Tool Box");
		
		//adding elements
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(2,2));
		panel.add(createTopMenu(),BorderLayout.NORTH);
		panel.add(createStackEditor(),BorderLayout.CENTER);
		panel.add(createControlPanel(),BorderLayout.EAST);
		panel.add(createConstructPanel(),BorderLayout.WEST);
		add(panel);
		setSize(600,600);
		setLocationRelativeTo(null);
		//
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		saveWindow = new SaveStackWindow(this);
		openWindow = new OpenStackWindow(this);
	}

}
