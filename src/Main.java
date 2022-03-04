import java.io.File;
import java.util.Scanner;

import cas.*;
import cas.lang.Ask;
import ui.UI;

public class Main extends QuickMath{
	
	public static void runScript(String fileName,boolean verbose) {
		long startingInstructionCount = Expr.ruleCallCount;
		long oldTime = System.nanoTime();
		Scanner sc;
		int currentLine = 0;
		try {
			sc = new Scanner(new File(fileName));
			CasInfo casInfo = new CasInfo();
			System.out.println("running "+fileName+" test script...");
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				currentLine++;
				if(line.startsWith("#")) continue;
				if(verbose) System.out.print(line+" -> ");
				Expr response = null;
				
				response = Ask.ask(line);
				
				if(response != null) {
					if(verbose) System.out.print(response+" -> ");
					response = response.simplify(casInfo);
					if(verbose)System.out.println(response);
				}
			}
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("fail at line: "+currentLine);
			return;
		}
		long delta = System.nanoTime() - oldTime;
		System.out.println("took " + delta / 1000000.0 + " ms to finish script!");
		System.out.println((Expr.ruleCallCount-startingInstructionCount)+" - instructions called");
	}
	
	static void testRegion() {
		//runScript("bitLogicTest.bl",true);
	}
	
	public static void main(String[] args) {
		int gui = 1;
		boolean clearTerm = false;
		
		for(int i = 0;i<args.length;i++) {
			String arg = args[i];
			if(arg.equals("gui")) gui = 1;
			else if(arg.equals("no-gui")) gui = 2;
			else if(arg.equals("clear-term")) clearTerm = true;
			else if(arg.equals("-s")) {
				runScript(args[i+1], true);
				i++;
			}
		}
		
		testRegion();
		
		if(gui == 1) UI.startGraphicalInterface();
		else if(gui == 2) UI.startCommandLineInterface(clearTerm);
		
		
	}

}
