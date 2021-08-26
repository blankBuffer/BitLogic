import java.util.Scanner;

import javax.swing.JOptionPane;

import cas.*;

public class Main extends QuickMath{
	
	public static boolean GUI = true;
	
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
	
	static void startCommandLineInterface() {
		StackEditor editor = new StackEditor();
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		while(true) {
			System.out.print("> ");
			String s = scanner.nextLine();
			if(s.equals("GUI")) {
				GUI = true;
				break;
			}
			editor.command(s);
			editor.printStack();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("Benjamin Currie @2021 v 1.3.0 , java runtime version: "+System.getProperty("java.version"));
		
		for(String arg:args) {
			if(arg.equals("no-gui")) GUI = false;
		}
		
		if(!GUI) startCommandLineInterface();
		new MainWindow();
		
		
	}
	

}
