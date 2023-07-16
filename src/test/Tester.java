package test;
import java.io.File;
import java.util.Random;
import java.util.Scanner;

import cas.base.CasInfo;
import cas.base.Expr;
import cas.base.FunctionsLoader;
import cas.base.StandardRules;
import cas.lang.Ask;
import cas.lang.Interpreter;
import cas.lang.MetaLang;
import cas.lang.Unit;
import cas.primitive.Var;

import static cas.Cas.*;

/*
 * The giant tester file
 * This tests things from parsing to simplification and more
 * Tester is a non static class, you must create Tester object first before running tests
 * the runScript does not require an instance
 */
public class Tester {
	
	/*
	 * basically simplifies every line in the file.
	 * Currently used mostly for testing with the bitLogicTest.bl file
	 * Lines that start with '#' are comment lines and are not run
	 * In verbose mode it shows every before and after computation
	 */
	public static void runScript(String fileName,boolean verbose) {
		load();
		
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
	
	boolean verbose = true;
	
	public void setVerbose(boolean choice){
		verbose = choice;
	}
	
	private boolean testCase(Expr testExpr,Expr expected,CasInfo casInfo){
		boolean passes = true;
		if(verbose) testExpr.print();
		System.out.print(" -> ");
		testExpr = testExpr.simplify(casInfo);
		passes = passes & testExpr.equals(expected);
		if(verbose) testExpr.print();
		if(verbose) System.out.println(" : "+passes);
		if(!passes) System.out.println("expected: "+expected+" got :"+testExpr);
		return passes;
	}
	
	public boolean arithmeticTest(CasInfo casInfo){
		boolean passes = true;
		
		{//powers
			//2^3 -> 8
			passes = passes & 
					testCase(power(num(2),num(3)),num(8),casInfo);
			//2^-3 -> 1/8
			passes = passes & 
					testCase(power(num(2),num(-3)),inv(num(8)),casInfo);
			//(-2)^3 -> -8
			passes = passes & 
					testCase(power(num(-2),num(3)),num(-8),casInfo);
			//(-2)^4 -> 16
			passes = passes & 
					testCase(power(num(-2),num(4)),num(16),casInfo);
		}
		{//sums
			//3+4 -> 7
			passes = passes & 
					testCase(sum(num(3),num(4)),num(7),casInfo);
			//3-4 -> -1
			passes = passes & 
					testCase(sum(num(3),num(-4)),num(-1),casInfo);
			//3+4+(7+9) -> 23
			passes = passes & 
					testCase(sum(num(3),num(4),sum(num(7),num(9))),num(23),casInfo);
		}
		{//products
			//6*3 -> 18
			passes = passes &
					testCase(prod(num(6),num(3)),num(18),casInfo);
			//-6*3 -> -18
			passes = passes &
					testCase(prod(num(-6),num(3)),num(-18),casInfo);
			//(-6)*(-3) -> 18
			passes = passes &
					testCase(prod(num(-6),num(-3)),num(18),casInfo);
			//3*4*(7*9) -> 756
			passes = passes & 
					testCase(prod(num(3),num(4),prod(num(7),num(9))),num(756),casInfo);
		}
		
		{//ratios
			//6/3 -> 2
			passes = passes &
					testCase(div(num(6), num(3)),num(2),casInfo);
			//6/-3 -> -2
			passes = passes &
					testCase(div(num(6), num(-3)),num(-2),casInfo);
			//-6/3 -> -2
			passes = passes &
					testCase(div(num(-6), num(3)),num(-2),casInfo);
			//3/6 -> 1/2
			passes = passes &
					testCase(div(num(3), num(6)),inv(num(2)),casInfo);
			
			//here the denominator must hold the negative to avoid negative ones
			
			//3/(-6) -> 1/-2
			passes = passes &
					testCase(div(num(3), num(-6)),inv(num(-2)),casInfo);
			//(-3)/6 -> 1/-2
			passes = passes &
					testCase(div(num(-3), num(6)),inv(num(-2)),casInfo);
			//2/-3 -> -2/3     here numerator negative takes priority
			passes = passes &
					testCase(div(num(2), num(-3)),div(num(-2), num(3)),casInfo);
			//-2/3 -> -2/3     verifying
			passes = passes &
					testCase(div(num(-2), num(3)),div(num(-2), num(3)),casInfo);
			
		}
		
		if(verbose) System.out.println("passed arithmetic test: "+passes);
		return passes;
	}
	
	public boolean basicAlgebraTest(CasInfo casInfo) {
		boolean passes = true;
		
		{//powers
			//x^0 -> 1
			passes = passes &
					testCase(power(var("x"), num(0)),num(1),casInfo);
			//x^1 -> x
			passes = passes &
					testCase(power(var("x"), num(1)),var("x"),casInfo);
			//(2^y)^z -> 2^(y*z)
			passes = passes &
					testCase(power(power(num(2), var("y")), var("z")),power(num(2), prod(var("y"),var("z"))),casInfo);
			//sqrt(x^2) -> abs(x)
			passes = passes &
					testCase(sqrt(power(var("x"), num(2))),abs(var("x")),casInfo);
			
		}
		{//sums
			//x+x -> 2*x
			passes = passes &
					testCase(sum(var("x"),var("x")),prod(num(2),var("x")),casInfo);
			//2*x+x -> 3*x
			passes = passes &
					testCase(sum(prod(num(2),var("x")),var("x")),prod(num(3),var("x")),casInfo);
			//x+2*x -> 3*x
			passes = passes &
					testCase(sum(var("x"),prod(num(2),var("x"))),prod(num(3),var("x")),casInfo);
			//3*x+2*x -> 5*x
			passes = passes &
					testCase(sum(prod(num(3),var("x")),prod(num(2),var("x"))),prod(num(5),var("x")),casInfo);
			
		}
		
		if(verbose) System.out.println("passed basic algebra test: "+passes);
		return passes;
	}
	
	private boolean testParse(String toParse,Expr expected) {
		boolean passed = true;
		
		Expr whatGotParsed = null;
		try {
			whatGotParsed = createExpr(toParse);
			passed = whatGotParsed.equals(expected);
		}catch(Exception e) {
			passed &= false;
		}
		
		if(verbose) System.out.println("parsed "+toParse+" : "+passed);
		if(!passed) System.out.println("failed to parse '"+toParse+"' correctly");
		
		if(!passed && whatGotParsed != null) {
			String treeStructure = whatGotParsed.toStringTree(0);
			System.out.println(treeStructure);
		}
		
		return passed;
	}
	
	public boolean parseTest() {
		boolean passes = true;
		
		{//arithmetic
			passes = passes & testParse("16",num(16));
			passes = passes & testParse("-71",num(-71));
			passes = passes & testParse("x",var("x"));
			passes = passes & testParse("2+3",sum(num(2),num(3)));
			passes = passes & testParse("-2+3",sum(num(-2),num(3)));
			passes = passes & testParse("2-3",sum(num(2), num(-3)));
			passes = passes & testParse("-2-3",sum(num(-2), num(-3)));
			passes = passes & testParse("-2+3",sum(num(-2), num(3)));
			passes = passes & testParse("2*3",prod(num(2),num(3)));
			passes = passes & testParse("2*-3",prod(num(2),num(-3)));
			passes = passes & testParse("-2*3",prod(num(-2),num(3)));
			passes = passes & testParse("-2*-3",prod(num(-2),num(-3)));
			passes = passes & testParse("2^3",power(num(2), num(3)));
			passes = passes & testParse("(-2)^3",power(num(-2), num(3)));
			passes = passes & testParse("-2^3",neg(power(num(2), num(3))));
			passes = passes & testParse("2^-3",power(num(2), num(-3)));
			passes = passes & testParse("2/3",div(num(2), num(3)));
			passes = passes & testParse("-2/3",neg(div(num(2), num(3))));
			passes = passes & testParse("2/-3",div(num(2), num(-3)));
		}
		
		{//boolean parsing
			passes = passes & testParse("x|y&~z",or(var("x"),and(var("y"),not(var("z")))));
			passes = passes & testParse("comparison(2>3)?x:y", ternary(comparison(equGreater(num(2),num(3))),var("x"),var("y")) );
		}
		
		{//functions
			passes = passes & testParse("sin(x)",sin(var("x")));
			passes = passes & testParse("integrate(sin(3*x),x)",integrate(sin(prod(num(3),var("x"))), var("x")));
		}
		
		{//other
			passes = passes & testParse("x:=5",define(var("x"),num(5)));
			passes = passes & testParse("f(x,y):=x+y", define(getFunction("f",var("x"),var("y")),sum(var("x"),var("y"))) );
		}
		
		{//parenthesis
			passes = passes & testParse("2*(x+y)",prod(num(2),sum(var("x"),var("y"))));
		}
		
		if(verbose) System.out.println("passed parsing test: "+passes);
		return passes;
	}
	
	public boolean runAllTests(boolean verbose){
		boolean passes = true;
		if(verbose) System.out.println("running all tests");
		
		
		MetaLang.init();//load the meta language
		Interpreter.init();//load bit logic standard syntax
		FunctionsLoader.load();//load functions into memory
		FunctionsLoader.FUNCTION_UNLOCKED = true;//on the fly function generation now permitted since everything is loaded
		Var.init();//initialize var specific stuff
		
		CasInfo casInfo = new CasInfo();
		
		passes = passes & parseTest();
		passes = passes & arithmeticTest(casInfo);
		passes = passes & basicAlgebraTest(casInfo);
		
		StandardRules.loadRules();//load additional shared rules
		
		Unit.init();//initialize unit conversion information
		
		Expr.random = new Random(761234897);//initialize random variable
		
		Ask.loadBasicQuestions();//load Q and A file
		
		if(verbose) System.out.println("passed all tests: "+passes);
		return passes;
		
	}
}
