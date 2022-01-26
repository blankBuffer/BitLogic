import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import cas.*;
import ui.UI;

public class Main extends QuickMath{
	
	public static final String VERSION = "1.5.0";
	
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
	static void mod(Integer a){
		a++;
	}
	
	public static void runScript(String fileName,boolean verbose) {
		long startingInstructionCount = Expr.ruleCallCount;
		long oldTime = System.nanoTime();
		Scanner sc;
		try {
			sc = new Scanner(new File(fileName));
			System.out.println("running "+fileName+" test script...");
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				if(line.startsWith("#")) continue;
				if(verbose) System.out.print(line+" -> ");
				Expr response = null;
				try {
					response = Ask.ask(line,Defs.blank,Settings.normal);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(verbose && response != null) System.out.print(response+" -> ");
				response = response.replace(Defs.blank.getVars()).simplify(Settings.normal);
				if(verbose && response != null) System.out.println(response);
			}
			
		} catch (FileNotFoundException e) {
			if(verbose) e.printStackTrace();
			return;
		}
		long delta = System.nanoTime() - oldTime;
		System.out.println("took " + delta / 1000000.0 + " ms to finish script!");
		System.out.println((Expr.ruleCallCount-startingInstructionCount)+" - instructions called");
	}
	
	static void testRegion() {
		runScript("bitLogicTest.bl",false);
	}
	
	public static void main(String[] args) {
		
		int gui = 0;
		
		for(int i = 0;i<args.length;i++) {
			String arg = args[i];
			if(arg.equals("gui")) gui = 1;
			else if(arg.equals("no-gui")) gui = 2;
			else if(arg.equals("clear-term")) UI.CLEAR_TERM = true;
			else if(arg.equals("-s")) {
				runScript(args[i+1], true);
				i++;
			}
		}
		
		if(UI.CLEAR_TERM) UI.clearTerm();
		System.out.println("Benjamin Currie @2021 v "+VERSION+" , java runtime version: "+System.getProperty("java.version"));
		fancyIntro();
		
		Rule.loadRules();
		
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
