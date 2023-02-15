import cas.*;
import cas.base.CasInfo;
import cas.lang.Ask;
import test.Tester;
import ui.UI;

/*
 * The main entry class for the bitlogic program
 */
public class Main extends Cas{
	
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
	static final int DEFAULT_START_MODE = GRAPHICAL_MODE;
	
	/*
	 * Program flags are not case sensitive and minus '-' and '_' are ignored
	 */
	public static void main(String[] args) {
		
		
		int startMode = DEFAULT_START_MODE;
		boolean clearTerm = false;//flag for startCommandLineInterface
		
		for(int i = 0;i<args.length;i++) {
			String arg = (args[i]).toLowerCase().replaceAll("[-_]", "");
			
			if(arg.equals("gui")) startMode = GRAPHICAL_MODE;
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
