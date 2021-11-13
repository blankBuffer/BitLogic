import cas.*;
import ui.UI;

public class Main extends QuickMath{
	
	public static final String VERSION = "1.4.2";
	
	public static void fancyIntro() {
		String img = ""
				+ "▛▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▜\n"
				+ "▌ ▛▀▀▄ ▀▛▘ ▀▛▘   ▌   ▗▛▜▖ ▗▛▜▖ ▀▛▘ ▗▛▜▖ ▐\n"
				+ "▌ ▌  ▐  ▌   ▌    ▌   ▛  ▜ ▛  ▀  ▌  ▛  ▀ ▐\n"
				+ "▌ ▌▄▄▀  ▌   ▌ ██ ▌   ▌  ▐ ▌ ▄▄  ▌  ▌    ▐\n"
				+ "▌ ▌  ▜  ▌   ▌    ▌   ▙  ▟ ▙ ▘▟  ▌  ▙  ▄ ▐\n"
				+ "▌ ▙▄▄▀ ▄▙▖  ▌    ▙▄▄ ▝▙▟▘ ▝▙▟▘ ▄▙▖ ▝▙▟▘ ▐\n"
				+ "▙▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▟\n";
		System.out.println(img);
	}
	
	public static void main(String[] args) {
		fancyIntro();
		System.out.println("Benjamin Currie @2021 v "+VERSION+" , java runtime version: "+System.getProperty("java.version"));
		
		int gui = 0;
		
		for(int i = 0;i<args.length;i++) {
			String arg = args[i];
			if(arg.equals("gui")) gui = 1;
			else if(arg.equals("no-gui")) gui = 2;
			else if(arg.equals("clear-term")) UI.CLEAR_TERM = true;
			else if(arg.equals("-s")) {
				Interpreter.runScript(args[i+1], true);
				i++;
			}
		}
		
		if(gui == 1) UI.startGraphicalInterface();
		else if(gui == 2) UI.startCommandLineInterface();
		
	}
	

}
