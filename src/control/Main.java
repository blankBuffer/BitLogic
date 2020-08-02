package control;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import math.algebra.Container;
import math.algebra.IntArith;
import math.booleanAlgebra.BoolContainer;

public class Main {
	public static Scanner in = new Scanner(System.in);
	public static long updateCount = 0;
	public volatile static boolean runningGui = false;
	
	public static void init() {
		
		JFrame f; 
	    
	    JTextField ta; 
	  
        // create a new frame to store text field and button 
        f = new JFrame("BitLogic"); 
        
        // create a label to display text 
        ta = new JTextField();
        ta.setSize(600,50);
        ta.setLocation(0, 0);
        ta.setFont(new Font("TimesRoman", Font.PLAIN, 24));
  
        // create a new button 
        JButton simp = new JButton("simplify"); 
        simp.setLocation(0, 250);
        simp.setSize(150, 50);
        
        simp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        
		       	Container m = cmd.Interpreter.stringToContainer( ta.getText() );
		        ta.setText(m.simplify().toString());
		        
			}
        });
        
        JButton boolsimp = new JButton("bool simplify"); 
        boolsimp.setLocation(150, 250);
        boolsimp.setSize(150, 50);
        
        boolsimp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        
		       	BoolContainer m = cmd.Interpreter.stringToBoolContainer( ta.getText() );
		        ta.setText(m.simplify().toString());
		        
			}
        });
        
        JButton closeButton = new JButton("exit");
        closeButton.setLocation(600-150, 250);
        closeButton.setSize(150, 50);
        
        closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
		        System.exit(0);
			}
        });
        
        
        JPanel p = new JPanel(); 
        p.setLayout(null);
        p.setBackground(new Color(32,32,32));
  
        p.add(ta);
        p.add(simp);
        p.add(boolsimp);
        p.add(closeButton);
  
        //f.setUndecorated(true);
        f.add(p);
        
        f.setResizable(false);
        f.setSize(600, 300); 
        f.setUndecorated(true);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        IntArith.Prime.init();
	}
	
	public static void main(String[] args) {
		Main.init();
	}

}
