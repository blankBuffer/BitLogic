import cas.*;
import cas.base.CasInfo;
import cas.base.Expr;
import cas.lang.Interpreter2;
import cas.lang.MetaLang;

public class Tester {
	
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
					testCase(Cas.power(Cas.num(2),Cas.num(3)),Cas.num(8),casInfo);
			//2^-3 -> 1/8
			passes = passes & 
					testCase(Cas.power(Cas.num(2),Cas.num(-3)),Cas.inv(Cas.num(8)),casInfo);
			//(-2)^3 -> -8
			passes = passes & 
					testCase(Cas.power(Cas.num(-2),Cas.num(3)),Cas.num(-8),casInfo);
			//(-2)^4 -> 16
			passes = passes & 
					testCase(Cas.power(Cas.num(-2),Cas.num(4)),Cas.num(16),casInfo);
		}
		{//sums
			//3+4 -> 7
			passes = passes & 
					testCase(Cas.sum(Cas.num(3),Cas.num(4)),Cas.num(7),casInfo);
			//3-4 -> -1
			passes = passes & 
					testCase(Cas.sum(Cas.num(3),Cas.num(-4)),Cas.num(-1),casInfo);
			//3+4+(7+9) -> 23
			passes = passes & 
					testCase(Cas.sum(Cas.num(3),Cas.num(4),Cas.sum(Cas.num(7),Cas.num(9))),Cas.num(23),casInfo);
		}
		{//products
			//6*3 -> 18
			passes = passes &
					testCase(Cas.prod(Cas.num(6),Cas.num(3)),Cas.num(18),casInfo);
			//-6*3 -> -18
			passes = passes &
					testCase(Cas.prod(Cas.num(-6),Cas.num(3)),Cas.num(-18),casInfo);
			//(-6)*(-3) -> 18
			passes = passes &
					testCase(Cas.prod(Cas.num(-6),Cas.num(-3)),Cas.num(18),casInfo);
			//3*4*(7*9) -> 756
			passes = passes & 
					testCase(Cas.prod(Cas.num(3),Cas.num(4),Cas.prod(Cas.num(7),Cas.num(9))),Cas.num(756),casInfo);
		}
		
		{//ratios
			//6/3 -> 2
			passes = passes &
					testCase(Cas.div(Cas.num(6), Cas.num(3)),Cas.num(2),casInfo);
			//6/-3 -> -2
			passes = passes &
					testCase(Cas.div(Cas.num(6), Cas.num(-3)),Cas.num(-2),casInfo);
			//-6/3 -> -2
			passes = passes &
					testCase(Cas.div(Cas.num(-6), Cas.num(3)),Cas.num(-2),casInfo);
			//3/6 -> 1/2
			passes = passes &
					testCase(Cas.div(Cas.num(3), Cas.num(6)),Cas.inv(Cas.num(2)),casInfo);
			
			//here the denominator must hold the negative to avoid negative ones
			
			//3/(-6) -> 1/-2
			passes = passes &
					testCase(Cas.div(Cas.num(3), Cas.num(-6)),Cas.inv(Cas.num(-2)),casInfo);
			//(-3)/6 -> 1/-2
			passes = passes &
					testCase(Cas.div(Cas.num(-3), Cas.num(6)),Cas.inv(Cas.num(-2)),casInfo);
			//2/-3 -> -2/3     here numerator negative takes priority
			passes = passes &
					testCase(Cas.div(Cas.num(2), Cas.num(-3)),Cas.div(Cas.num(-2), Cas.num(3)),casInfo);
			//-2/3 -> -2/3     verifying
			passes = passes &
					testCase(Cas.div(Cas.num(-2), Cas.num(3)),Cas.div(Cas.num(-2), Cas.num(3)),casInfo);
			
		}
		
		if(verbose) System.out.println("passed arithmetic test: "+passes);
		return passes;
	}
	
	public boolean basicAlgebraTest(CasInfo casInfo) {
		boolean passes = true;
		
		{//powers
			//x^0 -> 1
			passes = passes &
					testCase(Cas.power(Cas.var("x"), Cas.num(0)),Cas.num(1),casInfo);
			//x^1 -> x
			passes = passes &
					testCase(Cas.power(Cas.var("x"), Cas.num(1)),Cas.var("x"),casInfo);
			//(2^y)^z -> 2^(y*z)
			passes = passes &
					testCase(Cas.power(Cas.power(Cas.num(2), Cas.var("y")), Cas.var("z")),Cas.power(Cas.num(2), Cas.prod(Cas.var("y"),Cas.var("z"))),casInfo);
			//sqrt(x^2) -> abs(x)
			passes = passes &
					testCase(Cas.sqrt(Cas.power(Cas.var("x"), Cas.num(2))),Cas.abs(Cas.var("x")),casInfo);
			
		}
		{//sums
			//x+x -> 2*x
			passes = passes &
					testCase(Cas.sum(Cas.var("x"),Cas.var("x")),Cas.prod(Cas.num(2),Cas.var("x")),casInfo);
			//2*x+x -> 3*x
			passes = passes &
					testCase(Cas.sum(Cas.prod(Cas.num(2),Cas.var("x")),Cas.var("x")),Cas.prod(Cas.num(3),Cas.var("x")),casInfo);
			//x+2*x -> 3*x
			passes = passes &
					testCase(Cas.sum(Cas.var("x"),Cas.prod(Cas.num(2),Cas.var("x"))),Cas.prod(Cas.num(3),Cas.var("x")),casInfo);
			//3*x+2*x -> 5*x
			passes = passes &
					testCase(Cas.sum(Cas.prod(Cas.num(3),Cas.var("x")),Cas.prod(Cas.num(2),Cas.var("x"))),Cas.prod(Cas.num(5),Cas.var("x")),casInfo);
			
		}
		
		if(verbose) System.out.println("passed basic algebra test: "+passes);
		return passes;
	}
	
	private boolean testParse(String toParse,Expr expected) {
		boolean passed = Cas.createExpr(toParse).equals(expected);
		if(verbose) System.out.println("parsed "+toParse+" : "+passed);
		if(!passed) System.out.println("failed to parse '"+toParse+"' correctly");
		return passed;
	}
	
	public boolean parseTest() {
		boolean passes = true;
		
		{//arithmetic
			passes = passes & testParse("16",Cas.num(16));
			passes = passes & testParse("-71",Cas.num(-71));
			passes = passes & testParse("x",Cas.var("x"));
			passes = passes & testParse("2+3",Cas.sum(Cas.num(2),Cas.num(3)));
			passes = passes & testParse("-2+3",Cas.sum(Cas.num(-2),Cas.num(3)));
			passes = passes & testParse("2-3",Cas.sum(Cas.num(2), Cas.num(-3)));
			passes = passes & testParse("-2-3",Cas.sum(Cas.num(-2), Cas.num(-3)));
			passes = passes & testParse("-2+3",Cas.sum(Cas.num(-2), Cas.num(3)));
			passes = passes & testParse("2*3",Cas.prod(Cas.num(2),Cas.num(3)));
			passes = passes & testParse("2*-3",Cas.prod(Cas.num(2),Cas.num(-3)));
			passes = passes & testParse("-2*3",Cas.prod(Cas.num(-2),Cas.num(3)));
			passes = passes & testParse("-2*-3",Cas.prod(Cas.num(-2),Cas.num(-3)));
			passes = passes & testParse("2^3",Cas.power(Cas.num(2), Cas.num(3)));
			passes = passes & testParse("(-2)^3",Cas.power(Cas.num(-2), Cas.num(3)));
			passes = passes & testParse("-2^3",Cas.neg(Cas.power(Cas.num(2), Cas.num(3))));
			passes = passes & testParse("2^-3",Cas.power(Cas.num(2), Cas.num(-3)));
			passes = passes & testParse("2/3",Cas.div(Cas.num(2), Cas.num(3)));
			passes = passes & testParse("-2/3",Cas.div(Cas.num(-2), Cas.num(3)));
			passes = passes & testParse("2/-3",Cas.div(Cas.num(2), Cas.num(-3)));
		}
		
		{//functions
			passes = passes & testParse("sin(x)",Cas.sin(Cas.var("x")));
			passes = passes & testParse("integrate(sin(3*x),x)",Cas.integrate(Cas.sin(Cas.prod(Cas.num(3),Cas.var("x"))), Cas.var("x")));
		}
		
		if(verbose) System.out.println("passed parsing test: "+passes);
		return passes;
	}
	
	public boolean runAllTests(boolean verbose){
		boolean passes = true;
		if(verbose) System.out.println("running all tests");
		
		SimpleFuncs.functionsConstructor();
		MetaLang.init();
		Interpreter2.init();
		
		/*
		
		passes = passes & parseTest();
		
		Cas.load();
		CasInfo casInfo = new CasInfo();
		passes = passes & arithmeticTest(casInfo);
		passes = passes & basicAlgebraTest(casInfo);
		
		if(verbose) System.out.println("passed all tests: "+passes);
		*/
		return passes;
		
	}
}
