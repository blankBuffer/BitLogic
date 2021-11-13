package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

public class UI {
	public static boolean GUI = false;
	public static boolean CLEAR_TERM = false;
	public static void createMessege(String messege) {
		if(GUI) {
			new Thread() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null,messege);
				}
			}.start();
		}else {
			System.out.println("messege: "+messege);
		}
	}
	
	static void clearTerm() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
	
	public static void startCommandLineInterface() {
		GUI = false;
		StackEditor editor = new StackEditor();
		BufferedReader bf = new BufferedReader(new
		        InputStreamReader(System.in));
		while(true) {
			System.out.print("> ");
			String s = null;
			try {
				s = bf.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(CLEAR_TERM) clearTerm();
			
			if(s.equals("GUI")) {
				GUI = true;
				new MainWindow(editor);
				continue;
			}
			editor.command(s);
			editor.printStack();
		}
		
	}
	
	public static void startGraphicalInterface() {
		GUI = true;
		new MainWindow();
	}
	
}
