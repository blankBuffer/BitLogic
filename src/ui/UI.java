package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cas.Cas;
import cas.programming.StackEditor;
import test.Tester;

public class UI {
	
	public static final String VERSION = "1.7.17";
	public static final String CRED = "Benjamin Currie @2021-2023 java runtime version: "+System.getProperty("java.version");
	
	static int WINDOW_COUNT = 0;
	
	public static final String fancyIntro() {
		String img = ""
				+ "▛▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▜\n"
				+ "  ▛▀▀▄ ▀▛▘ ▀▛▘  ▌   ▗▛▜▖ ▗▛▜▖ ▀▛▘ ▗▛▜▖    V "+VERSION+"\n"
				+ "  ▌  ▟  ▌   ▌   ▌   ▛  ▜ ▛  ▀  ▌  ▛  ▀\n"
				+ "  ▌▀▀▄  ▌   ▌ █ ▌   ▌  ▐ ▌ ▄▄  ▌  ▌    ▗▛▀▜▖  █  ▗▛▀▚\n"
				+ "  ▌  ▐  ▌   ▌   ▌   ▙  ▟ ▙ ▘▟  ▌  ▙  ▄ ▐     ▟▄▙  ▀▚▄\n"
				+ "  ▙▄▄▀ ▄▙▖  ▌   ▙▄▄ ▝▙▟▘ ▝▙▟▘ ▄▙▖ ▝▙▟▘ ▝▙▄▟▘▟▘ ▝▙ ▚▄▟▘\n"
				+ "▙▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▟\n";
		return img+CRED+"\n"+"GNU LGPL V 2.1";
	}
	public static void clearTerm() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
	
	public static void startCommandLineInterface(boolean clearTerm) {
		System.out.println("starting stack read print loop...");
		
		System.out.println(UI.fancyIntro());
		System.out.println("type \"quit\" and hit ENTER to quit");
		
		Cas.load();
		
		StackEditor editor = new StackEditor();
		BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
		
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
	
	public static void startTest() {
		Tester tester = new Tester();
		tester.runAllTests(true);
	}
	
	public static void startGraphicalInterface() {
		System.out.println(UI.fancyIntro());
		new AppChooser();
	}
	
}
