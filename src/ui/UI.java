package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cas.Rule;
import cas.programming.StackEditor;

public class UI {
	
	public static final String VERSION = "1.6.21";
	public static final String CRED = "Benjamin Currie @2021-2022 v "+VERSION+" , java runtime version: "+System.getProperty("java.version");
	
	public static final String fancyIntro() {
		String img = ""
				+ "▛▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▜\n"
				+ "▌ ▛▀▀▄ ▀▛▘ ▀▛▘   ▌   ▗▛▜▖ ▗▛▜▖ ▀▛▘ ▗▛▜▖ ▐\n"
				+ "▌ ▌  ▟  ▌   ▌    ▌   ▛  ▜ ▛  ▀  ▌  ▛  ▀ ▐\n"
				+ "▌ ▌▀▀▄  ▌   ▌ ██ ▌   ▌  ▐ ▌ ▄▄  ▌  ▌    ▐\n"
				+ "▌ ▌  ▐  ▌   ▌    ▌   ▙  ▟ ▙ ▘▟  ▌  ▙  ▄ ▐\n"
				+ "▌ ▙▄▄▀ ▄▙▖  ▌    ▙▄▄ ▝▙▟▘ ▝▙▟▘ ▄▙▖ ▝▙▟▘ ▐\n"
				+ "▙▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▟\n";
		return img+CRED;
	}
	public static void clearTerm() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
	
	public static void startCommandLineInterface(boolean clearTerm) {
		System.out.println(UI.fancyIntro());
		System.out.println("type \"quit\" and hit ENTER to quit");
		Rule.loadRules();
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
			
			if(clearTerm) clearTerm();
			
			int result = editor.command(s);
			editor.printStack();
			if(result == StackEditor.QUIT) break;
		}
		
	}
	
	@SuppressWarnings("unused")
	public static void startGraphicalInterface() {
		System.out.println(UI.fancyIntro());
		new MainWindow();
	}
	
}
