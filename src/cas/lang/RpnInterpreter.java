package cas.lang;

import cas.base.Expr;
import cas.lang.ParseMachine.ObjectBuilder;

public class RpnInterpreter {
	/*
	 * 2 3 4 + 2 * sin( is the same thing as sin((2+3+4)*2)
	 */
	
	private static boolean loaded = false;
	
	static ObjectBuilder exprBuilder = null;
	
	public static Expr createExpr(String toParse) {
		return (Expr)exprBuilder.build(toParse);
	}
	
	public static void test() {
		init();
		
		String testText = "267 2 * x + sin( y =>";
		
		Expr testOut = createExpr(testText);
		
		testOut.println();
		
	}
	
	public static void init() {
		if(loaded) return;
		
		exprBuilder = new ObjectBuilder();
		
		try {
			exprBuilder.setLang("resources/rpn_syntax.pm");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		loaded = true;
	}
}
