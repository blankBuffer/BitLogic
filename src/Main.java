import java.util.Scanner;

import javax.swing.JOptionPane;

import cas.*;

public class Main extends QuickMath{
	
	public static boolean GUI = true,CLEAR_TERM = false;
	public static final String VERSION = "1.4.1";
	
	static void createMessege(String messege) {
		if(Main.GUI) {
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
	
	static void startCommandLineInterface() {
		StackEditor editor = new StackEditor();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		while(true) {
			System.out.print("> ");
			String s = scanner.nextLine();
			
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
	
	public static void main(String[] args) {
		System.out.println("Benjamin Currie @2021 v "+VERSION+" , java runtime version: "+System.getProperty("java.version"));
		
		for(String arg:args) {
			if(arg.equals("no-gui")) GUI = false;
			if(arg.equals("clear-term")) CLEAR_TERM = true;
		}
		
		
		if(!GUI) startCommandLineInterface();
		else new MainWindow();
		
	}
	

}
