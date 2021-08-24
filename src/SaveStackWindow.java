import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

import cas.*;

public class SaveStackWindow extends JFrame{

	private static final long serialVersionUID = 164095131110049102L;
	
	SaveStackWindow(MainWindow mainWindow){
		super("save");
		setSize(200, 100);
		setLocationRelativeTo(null);
		
		//file name field
		JTextField fileNameField = new JTextField();
		fileNameField.setText("fileName");
		
		JPanel bottomPart = new JPanel();
		//open button
		JButton saveButton = new JButton("save");
		saveButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ExprList exprList = new ExprList();
				for(int i = 0;i< mainWindow.currentStackEditor.stack.size();i++) exprList.add( mainWindow.currentStackEditor.stack.get(i));
				try {
					Expr.saveExpr(exprList, fileNameField.getText());
				} catch (IOException e1) {}
				setVisible(false);
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
		bottomPart.add(saveButton);
		bottomPart.add(cancelButton);
		bottomPart.setLayout(new FlowLayout());
		
		setLayout(new BorderLayout());
		add(fileNameField,BorderLayout.NORTH);
		add(bottomPart,BorderLayout.SOUTH);
	}
	
}
