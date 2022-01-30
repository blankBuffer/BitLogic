package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UI {
	
	public static final String VERSION = "1.5.2";
	
	public static boolean GUI = false;
	public static boolean CLEAR_TERM = false;
	static MainWindow mainWindow = null;
	
	public static String fancyIntro() {
		String img = ""
				+ "▛▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▜\n"
				+ "▌ ▛▀▀▄ ▀▛▘ ▀▛▘   ▌   ▗▛▜▖ ▗▛▜▖ ▀▛▘ ▗▛▜▖ ▐\n"
				+ "▌ ▌  ▟  ▌   ▌    ▌   ▛  ▜ ▛  ▀  ▌  ▛  ▀ ▐\n"
				+ "▌ ▌▀▀▄  ▌   ▌ ██ ▌   ▌  ▐ ▌ ▄▄  ▌  ▌    ▐\n"
				+ "▌ ▌  ▐  ▌   ▌    ▌   ▙  ▟ ▙ ▘▟  ▌  ▙  ▄ ▐\n"
				+ "▌ ▙▄▄▀ ▄▙▖  ▌    ▙▄▄ ▝▙▟▘ ▝▙▟▘ ▄▙▖ ▝▙▟▘ ▐\n"
				+ "▙▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▟\n";
		return img+"Benjamin Currie @2021-2022 v "+VERSION+" , java runtime version: "+System.getProperty("java.version");
	}
	public static void clearTerm() {
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
			
			int result = editor.command(s);
			editor.printStack();
			if(result == -1) break;
		}
		
	}
	
	public static void startGraphicalInterface() {
		GUI = true;
		mainWindow = new MainWindow();
	}
	
}
