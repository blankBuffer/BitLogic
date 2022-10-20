import cas.*;

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
		if(!passes) System.out.println("expected: "+expected);
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
			
		}
		
		if(verbose) System.out.println("passed arithmetic test: "+passes);
		return passes;
	}
	
	public boolean runAllTests(boolean verbose){
		boolean passes = true;
		if(verbose) System.out.println("running all tests");
		Cas.load();
		CasInfo casInfo = new CasInfo();
		passes = passes & arithmeticTest(casInfo);
		
		if(verbose) System.out.println("passed all tests: "+passes);
		return passes;
	}
}
