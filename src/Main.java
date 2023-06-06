import cas.*;
import cas.base.CasInfo;
import cas.lang.Ask;
import test.Tester;
import ui.UI;

/*
 * The main entry class for the bitlogic program
 */
public class Main{
	
	//program start modes
	
	/*
	 * start the program with swing UI
	 * this has the plot and fancy math graphical text etc
	 */
	static final int GRAPHICAL_MODE = 0b0;
	/*
	 * this mode is like a machine query with a mix of features
	 * it uses a mix of 
	 *     natural language processing 
	 *     RPN (reverse polish notation) with stack
	 *     function syntax
	 *     algebraic input (normal math notation)
	 */
	static final int TERMINAL_MODE = 0b1;
	
	/*
	 * This mode is used just for testing and debugging
	 */
	static final int TEST_MODE = 0b10;
	
	/*
	 * graphical mode is the default for now since simps will find that easy
	 * TODO add a preference file for start mode
	 */
	static final int DEFAULT_START_MODE = TERMINAL_MODE;
	
	/*
	 * Program flags are not case sensitive and minus '-' and '_' are ignored
	 */
	public static void main(String[] args) {
		
		
		int startMode = DEFAULT_START_MODE;
		boolean clearTerm = false;//flag for startCommandLineInterface
		
		for(int i = 0;i<args.length;i++) {
			String arg = (args[i]).toLowerCase().replaceAll("[-_]", "");
			
			if(arg.equals("h") || arg.equals("help")) {
				System.out.println("## Startup help menu ##");
				System.out.println();
				System.out.println("-gui #start in graphical mode");
				System.out.println("-no_gui #start in terminal mode");
				System.out.println("-term #start in terminal mode");
				System.out.println();
				System.out.println("-test -t #start in test mode");
				System.out.println();
				System.out.println("-clear_term -ct #every input refreshes screen in terminal mode");
				System.out.println();
				System.out.println("-execute \"MATH_EXPRESSION\" #exectues single math command");
				System.out.println("-e \"MATH_EXPRESSION\" #exectues single math command");
				System.out.println("#Just a warning that -execute is very slow to run!");
				System.out.println();
				System.out.println("-help -h #show startup help menu");
				return;
			}
			else if(arg.equals("gui")) startMode = GRAPHICAL_MODE;
			else if(arg.equals("nogui") || arg.equals("term")) startMode = TERMINAL_MODE;
			else if(arg.equals("t") || arg.equals("test")) startMode = TEST_MODE;
			
			else if(arg.equals("ct") || arg.equals("clearterm")) clearTerm = true;
			/*
			 * run file line by line for testing. Simply pass fileName
			 */
			else if(arg.equals("s") || arg.equals("script")) {
				String fileName = args[i+1];
				Tester.runScript(fileName, true);
				System.exit(0);
			}
			/*
			 * simplify a single expression
			 * example ./start e "5*7"
			 */
			else if(arg.equals("e") || arg.equals("execute")) {
				Cas.load();
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
		
		//start main user interfaces
		if(startMode == GRAPHICAL_MODE) UI.startGraphicalInterface();
		else if(startMode == TERMINAL_MODE) UI.startCommandLineInterface(clearTerm);
		else if(startMode == TEST_MODE) UI.startTest();
		
	}

}
