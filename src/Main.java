import cas.*;
import ui.UI;

public class Main extends QuickMath{
	
	public static final String VERSION = "1.4.3";
	
	public static void fancyIntro() {
		String img = ""
				+ "▛▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▜\n"
				+ "▌ ▛▀▀▄ ▀▛▘ ▀▛▘   ▌   ▗▛▜▖ ▗▛▜▖ ▀▛▘ ▗▛▜▖ ▐\n"
				+ "▌ ▌  ▟  ▌   ▌    ▌   ▛  ▜ ▛  ▀  ▌  ▛  ▀ ▐\n"
				+ "▌ ▌▀▀▄  ▌   ▌ ██ ▌   ▌  ▐ ▌ ▄▄  ▌  ▌    ▐\n"
				+ "▌ ▌  ▐  ▌   ▌    ▌   ▙  ▟ ▙ ▘▟  ▌  ▙  ▄ ▐\n"
				+ "▌ ▙▄▄▀ ▄▙▖  ▌    ▙▄▄ ▝▙▟▘ ▝▙▟▘ ▄▙▖ ▝▙▟▘ ▐\n"
				+ "▙▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▟\n";
		System.out.println(img);
	}
	
	static void testRegion() {
		//System.out.println(Interpreter.isProbablyExpr("q+3"));
	}
	
	public static void main(String[] args) {
		
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
		System.out.println("Benjamin Currie @2021 v "+VERSION+" , java runtime version: "+System.getProperty("java.version"));
		
		if(UI.CLEAR_TERM) UI.clearTerm();
		fancyIntro();
		
		testRegion();
		
		//showMemoryUsage.start();
		if(gui == 1) UI.startGraphicalInterface();
		else if(gui == 2) UI.startCommandLineInterface();
	}
	
	static Thread showMemoryUsage = new Thread("mem-use-visual"){
		@Override
		public void run() {
			while(true) {
				try {
					Thread.sleep(100);
					long freeMem = Runtime.getRuntime().freeMemory();
					long mem = Runtime.getRuntime().totalMemory();
					long percent = Math.round(((double)(mem-freeMem)/mem)*100.0);
					for(int i = 0;i<50;i++){
						if(i<percent/2){
							System.out.print("*");
						}else{
							System.out.print("-");
						}
					}
					System.out.println();
				} catch (Exception e) {
					System.out.println("stopping memory usage thread");
					break;
				}
			}
		}
	};

}
