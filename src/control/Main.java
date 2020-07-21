package control;

public class Main {
	
	public static long updateCount = 0;
	public volatile static boolean runningGui = false;
	
	public static void init() {
		System.out.println("Bit_Logic Version 0.55 Text_Mode.  Created By: Ben Currie");
		System.out.println("type \"help\" for commands or \"tutorial\" for a tutorial");
		
		cmd.Interpreter.interpret();
		
	}
	
	public static void main(String[] args) {
		Main.init();
	}

}
