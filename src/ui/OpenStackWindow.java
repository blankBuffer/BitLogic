package ui;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

import cas.*;

class OpenStackWindow extends JFrame{

	private static final long serialVersionUID = 164095131110049102L;
	
	OpenStackWindow(MainWindow mainWindow){
		super("open");
		setSize(200, 100);
		setLocationRelativeTo(null);
		
		//file name field
		JTextField fileNameField = new JTextField();
		fileNameField.setText("fileName");
		
		JPanel bottomPart = new JPanel();
		//open button
		JButton openButton = new JButton("open");
		openButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					setVisible(false);
					ExprList exprList = (ExprList)Expr.openExpr(fileNameField.getText());
					for(int i = 0;i<exprList.size();i++) mainWindow.currentStackEditor.stack.addElement(exprList.get(i));
					
				} catch (IOException e1) {} catch (ClassNotFoundException e1) {}
			}
			
		});
		
		//cancel button
		JButton cancelButton = new JButton("cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
			
		});
		bottomPart.add(openButton);
		bottomPart.add(cancelButton);
		bottomPart.setLayout(new FlowLayout());
		
		setLayout(new BorderLayout());
		add(fileNameField,BorderLayout.NORTH);
		add(bottomPart,BorderLayout.SOUTH);
	}
	
}
