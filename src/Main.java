import java.io.File;
import java.util.Scanner;

import cas.*;
import cas.lang.Ask;
import ui.UI;

public class Main extends Cas{
	
	public static void runScript(String fileName,boolean verbose) {
		Rule.loadCompileSimplifyRules();
		long oldTime = System.nanoTime();
		Scanner sc = null;
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
			if(sc != null) sc.close();
			return;
		}
		long delta = System.nanoTime() - oldTime;
		System.out.println("took " + delta / 1000000.0 + " ms to finish script!");
	}
	
	public static void main(String[] args) {
		
		Tester tester = new Tester();
		tester.runAllTests(true);
		
		
		
		int gui = 1;
		boolean clearTerm = false;
		
		for(int i = 0;i<args.length;i++) {
			String arg = args[i];
			if(arg.equals("gui")) gui = 1;
			else if(arg.equals("no-gui")) gui = 2;
			else if(arg.equals("clear-term")) clearTerm = true;
			else if(arg.equals("-s")) {
				runScript(args[i+1], true);
				System.exit(0);
			}else if(arg.equals("-e")) {
				Rule.loadCompileSimplifyRules();
				try {
					String q = args[i+1];
					System.out.println("question: "+q);
					System.out.println(Ask.ask(q).simplify(CasInfo.normal));
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.exit(0);
			}
		}
		
		if(gui == 1) UI.startGraphicalInterface();
		else if(gui == 2) UI.startCommandLineInterface(clearTerm);
		
		
	}

}
