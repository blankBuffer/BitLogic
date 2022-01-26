package cas;
import java.math.BigInteger;
import java.util.ArrayList;

public class Solve extends Expr{
	
	private static final long serialVersionUID = 8530995692151126277L;
	static Equ logCase = equ( createExpr("ln(a)=b") , createExpr("a=e^b") );//ln(a)=b -> a=e^b
	static Equ rootCase = equ( createExpr("m^n=a") , createExpr("m=a^inv(n)")  );//m^n=a -> m=a^(1/n)
	static Equ expoCase = equ( createExpr("m^n=a")   ,createExpr("n=ln(a)/ln(m)")  );//m^n=a -> n=ln(a)/ln(m)
	static Equ baseAndExpoHaveVar = equ( createExpr("m^n=a")   ,createExpr("n*ln(m)=ln(a)")  );
	static Equ divCase1 = equ(createExpr("a/b=y"),createExpr("a=y*b"));
	static Equ divCase2 = equ(createExpr("a/b=y"),createExpr("b=a/y"));
	static Equ divCase3 = equ(createExpr("a/b=y"),createExpr("a-y*b=0"));
	
	static ArrayList<Equ> caseTable = new ArrayList<Equ>();
	static{//make rare case table
		//root problems
		Expr rareCase1Ans = createExpr("x=(k^2-a)^2/(4*k^2)");
		caseTable.add(equ( createExpr("sqrt(x)-sqrt(x+a)=k") , rareCase1Ans ));
		caseTable.add(equ( createExpr("sqrt(x+a)-sqrt(x)=k") , rareCase1Ans ));
		caseTable.add(equ( createExpr("sqrt(x+a)+sqrt(x)=k") , rareCase1Ans ));
		
		Expr rareCase2Ans = createExpr("[x=(sqrt(4*a+4*y+1)+2*y+1)/2,x=(-sqrt(4*a+4*y+1)+2*y+1)/2]");
		caseTable.add(equ( createExpr("x+sqrt(x+a)=y") , rareCase2Ans ));
		caseTable.add(equ( createExpr("x-sqrt(x+a)=y") , rareCase2Ans ));
		Expr rareCase2Ans2 = createExpr("[x=(sqrt(4*a-4*y+1)-2*y+1)/2,x=(-sqrt(4*a-4*y+1)-2*y+1)/2]");
		caseTable.add(equ( createExpr("sqrt(x+a)-x=y") , rareCase2Ans2 ));
		
		Expr rareCase3Ans = createExpr("[x=(sqrt(-4*a*b+4*a*y^2+4*b*z^2+m^2+4*m*y*z)+m+2*y*z)/(2*(y^2-b)),x=(-sqrt(-4*a*b+4*a*y^2+4*b*z^2+m^2+4*m*y*z)+m+2*y*z)/(2*(y^2-b))]");
		caseTable.add(equ( createExpr("y*x+sqrt(a+m*x+b*x^2)=z") , rareCase3Ans ));
		caseTable.add(equ( createExpr("y*x-sqrt(a+m*x+b*x^2)=z") , rareCase3Ans ));
		Expr rareCase3Ans2 = createExpr("[x=(sqrt(-4*a*b+4*a*y^2+4*b*z^2+m^2+4*m*y*z)+m+2*y*z)/(2*(b-y^2)),x=(-sqrt(-4*a*b+4*a*y^2+4*b*z^2+m^2+4*m*y*z)+m+2*y*z)/(2*(b-y^2))]");
		caseTable.add(equ( createExpr("-y*x+sqrt(a+m*x+b*x^2)=z") , rareCase3Ans2 ));
		
		Expr rareCase4Ans = createExpr("[x=(-sqrt(-4*a*b*k^4+4*a*k^2*y^2+4*b*k^2*z^2+k^4*m^2+4*k^2*m*y*z)-k^2*m-2*y*z)/(2*(b*k^2-y^2)),x=(sqrt(-4*a*b*k^4+4*a*k^2*y^2+4*b*k^2*z^2+k^4*m^2+4*k^2*m*y*z)-k^2*m-2*y*z)/(2*(b*k^2-y^2))]");
		caseTable.add(equ( createExpr("y*x+k*sqrt(a+m*x+b*x^2)=z") , rareCase4Ans ));
		caseTable.add(equ( createExpr("y*x-k*sqrt(a+m*x+b*x^2)=z") , rareCase4Ans ));
		Expr rareCase4Ans2 = createExpr("[x=(-sqrt(-4*a*b*k^4+4*a*k^2*y^2+4*b*k^2*z^2+k^4*m^2+4*k^2*m*y*z)-k^2*m-2*y*z)/(-2*(b*k^2-y^2)),x=(sqrt(-4*a*b*k^4+4*a*k^2*y^2+4*b*k^2*z^2+k^4*m^2+4*k^2*m*y*z)-k^2*m-2*y*z)/(-2*(b*k^2-y^2))]");
		caseTable.add(equ( createExpr("-y*x+k*sqrt(a+m*x+b*x^2)=z") , rareCase4Ans2 ));
		//special
		caseTable.add(equ( createExpr("a^b=1") , createExpr("[b=0,a=1]") ));
		caseTable.add(equ( createExpr("sin(x)-cos(x)=0") , createExpr("x=pi/4") ));
		caseTable.add(equ( createExpr("a*sin(x)+b*cos(x)=y") , createExpr("x=acos(y/sqrt(a^2+b^2))+atan(a/b)") ));
		caseTable.add(equ( createExpr("sin(x)+b*cos(x)=y") , createExpr("x=acos(y/sqrt(1+b^2))+atan(1/b)") ));
		caseTable.add(equ( createExpr("a*sin(x)+cos(x)=y") , createExpr("x=acos(y/sqrt(a^2+1))+atan(a)") ));
		caseTable.add(equ( createExpr("sin(x)+cos(x)=y") , createExpr("x=acos(y/sqrt(2))+pi/4") ));
		//lambert w function
		caseTable.add(equ( createExpr("x*ln(x)=y") , createExpr("x=e^w(y)") ));
		caseTable.add(equ( createExpr("x*a^x=y") , createExpr("x=w(y*ln(a))/ln(a)") ));
		caseTable.add(equ( createExpr("x*a^(b*x)=y") , createExpr("x=w(y*b*ln(a))/(b*ln(a))") ));
		caseTable.add(equ( createExpr("x^n*a^x=y") , createExpr("x=n*w(y^(1/n)*ln(a)/n)/ln(a)") ));
		caseTable.add(equ( createExpr("x^n*a^(b*x)=y") , createExpr("x=n*w(y^(1/n)*ln(a)*b/n)/(b*ln(a))") ));
		caseTable.add(equ( createExpr("x+a^x=y") , createExpr("x=y-w(ln(a)*a^y)/ln(a)") ));
		caseTable.add(equ( createExpr("b*x+a^x=y") , createExpr("x=(ln(a)*y-b*w((ln(a)*a^(y/b))/b))/(b*ln(a))") ));
		
		//subtract same type of function
		{
			caseTable.add(equ( createExpr("sin(a)-sin(y)=0") , createExpr("a-y=0") ));
			caseTable.add(equ( createExpr("cos(a)-cos(y)=0") , createExpr("a-y=0") ));
			caseTable.add(equ( createExpr("tan(a)-tan(y)=0") , createExpr("a-y=0") ));
			caseTable.add(equ( createExpr("asin(a)-asin(y)=0") , createExpr("a-y=0") ));
			caseTable.add(equ( createExpr("acos(a)-acos(y)=0") , createExpr("a-y=0") ));
			caseTable.add(equ( createExpr("atan(a)-atan(y)=0") , createExpr("a-y=0") ));
			caseTable.add(equ( createExpr("ln(a)-ln(y)=0") , createExpr("a-y=0") ));
			caseTable.add(equ( createExpr("a^m-a^y=0") , createExpr("m-y=0") ));
			caseTable.add(equ( createExpr("m^a-y^a=0") , createExpr("m-y=0") ));
			//negative versions
		}
		//solving inverses
		caseTable.add(equ( createExpr("sin(x)=y") , createExpr("x=asin(y)") ));
		caseTable.add(equ( createExpr("cos(x)=y") , createExpr("x=acos(y)") ));
		caseTable.add(equ( createExpr("tan(x)=y") , createExpr("x=atan(y)") ));
		caseTable.add(equ( createExpr("asin(x)=y") , createExpr("x=sin(y)") ));
		caseTable.add(equ( createExpr("acos(x)=y") , createExpr("x=cos(y)") ));
		caseTable.add(equ( createExpr("atan(x)=y") , createExpr("x=tan(y)") ));
		caseTable.add(equ( createExpr("w(x)=y") , createExpr("x=y*e^y") ));
	}
	
	Solve(){}//
	public Solve(Equ e,Var v){
		add(e);
		add(v);
	}
	
	@Override
	public Var getVar() {
		return (Var)get(1);
	}
	
	Equ getEqu() {
		return (Equ)get();
	}
	
	void setEqu(Equ e) {
		set(0,e);
	}
	
	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		{//first move everything to the left side
			moveToLeftSide((Solve)toBeSimplified);
		}
		
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		Solve castedSolve = (Solve)toBeSimplified;
		
		Expr originalProblem = castedSolve.getEqu().getLeftSide().copy();
		
		Equ oldEqu = null;
		
		outer:while(!castedSolve.getEqu().equals(oldEqu)) {
			oldEqu = (Equ)castedSolve.getEqu().copy();
				
			if( !castedSolve.getEqu().getRightSide().equals(Num.ZERO)) castedSolve.getEqu().setLeftSide( distr(castedSolve.getEqu().getLeftSide()).simplify(settings));//distribute, we don't do it if right side is zero because we can use technique
				
			sumMoveNonImportantToRightSide(castedSolve,settings);//x+a+b=c -> x=c-a-b
			
			castedSolve.getEqu().setLeftSide( factor(castedSolve.getEqu().getLeftSide()).simplify(settings) );//factor
			
			if(castedSolve.getEqu().getRightSide().equals(Num.ZERO) && castedSolve.getEqu().getLeftSide() instanceof Prod) {//if left side is a product and right side is 0 each part of the product is an equation
				ExprList repl = new ExprList();
				
				Prod leftProd = (Prod)castedSolve.getEqu().getLeftSide();
				
				for(int i = 0;i<leftProd.size();i++) {
					if(leftProd.get(i).contains(castedSolve.getVar())) {
						repl.add(solve(equ(leftProd.get(i),num(0)),castedSolve.getVar()));
					}
				}
				
				toBeSimplified = repl.simplify(settings);
				break;
			}
			
			
			//special cases ///////
			Expr.ModifyFromExampleResult result = null;
			for(Equ rareCase:caseTable){
				result = castedSolve.getEqu().modifyFromExampleSpecific(rareCase,settings);
				if(result.expr instanceof ExprList){
					for(int i = 0;i<result.expr.size();i++){
						result.expr.set(i, solve((Equ)result.expr.get(i),castedSolve.getVar()) );
					}
					toBeSimplified = result.expr.simplify(settings);
					break outer;
				}
				castedSolve.setEqu((Equ)result.expr);
				if(result.success) break;
			}
			
			//////	
				
			prodMoveNonImportantToRightSide(castedSolve,settings);//x*a*b=c -> x=c*inv(a)*inv(b)
			
			if(castedSolve.getEqu().getLeftSide() instanceof Div){
				Div castedDiv = (Div)castedSolve.getEqu().getLeftSide();
				boolean numerHasVar = castedDiv.getNumer().contains(castedSolve.getVar());
				boolean denomHasVar = castedDiv.getDenom().contains(castedSolve.getVar());
				
				if(numerHasVar && !denomHasVar) {
					castedSolve.setEqu((Equ) castedSolve.getEqu().modifyFromExample(divCase1, settings));
				}else if(!numerHasVar && denomHasVar){
					castedSolve.setEqu((Equ) castedSolve.getEqu().modifyFromExample(divCase2, settings));
				}else if(numerHasVar && denomHasVar){
					castedSolve.setEqu((Equ) castedSolve.getEqu().modifyFromExample(divCase3, settings));
				}
			}
			
			{//quadratic solve
				Equ eq = castedSolve.getEqu();
				if(eq.getLeftSide() instanceof Prod) {
					Prod pr = (Prod)eq.getLeftSide();
					if(pr.size() == 2) {
						Sum sumPart = null;
						Expr varPart = null;
						for(int i = 0;i<2;i++) {
							if(pr.get(i) instanceof Sum) {
								if(sumPart != null) break;
								sumPart = (Sum)pr.get(i);
							}else {
								varPart = pr.get(i);
							}
						}
						if(sumPart!=null && varPart != null) {//I'm not going to use poly extract because i want it to solve more than the generic quadratic like sin(x)^2+sin(x)+2=0
							Expr a = new Sum(),b = new Sum();
							for(int i = 0;i<sumPart.size();i++) {
								if(!sumPart.get(i).contains(varPart)) {
									b.add(sumPart.get(i));
								}else {
									if(sumPart.get(i).equals(varPart)) a.add(num(1));
									else if(sumPart.get(i) instanceof Prod) {
										Prod subProd = (Prod)sumPart.get(i);
										Prod p = new Prod();
										for(int j = 0;j<subProd.size();j++) {
											if(!subProd.get(j).equals(varPart)) {
												p.add(subProd.get(j));
											}
										}
										a.add(p);
									}
								}
							}
							if(!a.contains(castedSolve.getVar()) && !b.contains(castedSolve.getVar())) {
								Expr c = eq.getRightSide();
								
								ExprList out = new ExprList();
								out.add(     solve(equ(varPart,prod(sub(sqrt(  sum(pow(b,num(2)),prod(num(4),a,c))) , b ),inv(num(2)),inv(a))),castedSolve.getVar())     );
								out.add(     solve(equ(varPart,prod(sum(sqrt(  sum(pow(b,num(2)),prod(num(4),a,c))) , b ),inv(num(-2)),inv(a))),castedSolve.getVar())    );
								
								toBeSimplified = out.simplify(settings);
								
								break;
							}
							
						}
					}
				}
				
			}
				
			castedSolve.setEqu( (Equ)castedSolve.getEqu().modifyFromExample(logCase,settings)  );//ln(x)=b -> x=e^b
				
			
			//rules involving powers
			if(castedSolve.getEqu().getLeftSide() instanceof Power) {
				Power castedPower = (Power)castedSolve.getEqu().getLeftSide();
				if(!castedPower.getExpo().contains( castedSolve.getVar() )) {//case 1, only base has x
					castedSolve.setEqu( (Equ)castedSolve.getEqu().modifyFromExample(rootCase,settings)  );
					if(castedPower.getExpo() instanceof Num) {
						Num num = (Num)castedPower.getExpo();
						if(num.realValue.mod(BigInteger.TWO).equals(BigInteger.ZERO) && !num.isComplex()) {//if exponent is divisible by two then there are two solutions
							ExprList repl = new ExprList();//result has more than one solution so stored in a list
							repl.add(castedSolve.copy());
							castedSolve.getEqu().setRightSide(neg(castedSolve.getEqu().getRightSide()));
							repl.add(castedSolve);
							toBeSimplified = repl.simplify(settings);
							break;
						}
					}
				}else if(!castedPower.getBase().contains( castedSolve.getVar() )) {//expo case variable only in exponent
					castedSolve.setEqu( (Equ)castedSolve.getEqu().modifyFromExample(expoCase,settings));
				}else{//generic problem:  x^(b*x)=a -> ln(x^(b*x))=ln(a) -> x*ln(x)=ln(a)/b -> ln(x)*e^ln(x)=ln(a)/b -> ln(x)=w(ln(a)/b)
					castedSolve.setEqu( (Equ)castedSolve.getEqu().modifyFromExample(baseAndExpoHaveVar,settings));
				}
			}
			if(toBeSimplified instanceof Solve) {
				if(castedSolve.getEqu().getLeftSide().equals(castedSolve.getVar())) toBeSimplified = castedSolve.getEqu();//solved !!!
			}
		}
		
		if(toBeSimplified instanceof Solve){//last resort
			{//integer tests
				ExprList sol = new ExprList();
				BigInteger mag = BigInteger.valueOf(128);
				Expr[] nums = new Expr[257+3];
				int index = 0;
				for(BigInteger i = mag.negate();i.compareTo(mag)!=1;i=i.add(BigInteger.ONE)){
					Num iV = num(i);
					nums[index]=iV;
					index++;
				}
				nums[257+0] = Num.I;
				nums[257+1] = pi();
				nums[257+2] = e();
				
				for(Expr testValue:nums){
					ComplexFloat test = originalProblem.replace(equ(castedSolve.getVar(),testValue)).convertToFloat(new ExprList());
					if(Math.abs(test.real)<1){
						Expr testZero = originalProblem.replace(equ(castedSolve.getVar(),testValue)).simplify(settings);
						if(testZero.equals(num(0))){//double check
							sol.add(equ(castedSolve.getVar().copy(),testValue));
						}
					}
				}
				if(sol.size()>0){
					toBeSimplified = sol.simplify(settings);
				}
			}
		}
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}
	
	static void moveToLeftSide(Solve solve) {
		if(!solve.getEqu().getRightSide().equals(Num.ZERO)) {
			solve.getEqu().setLeftSide( sub(solve.getEqu().getLeftSide(),solve.getEqu().getRightSide()) );
			solve.getEqu().setRightSide(num(0));
		}
	}
	
	static void sumMoveNonImportantToRightSide(Solve solve,Settings settings) {
		if(solve.getEqu().getLeftSide() instanceof Sum) {
			Sum leftSideSum = (Sum)solve.getEqu().getLeftSide();
			Sum newRightSide = new Sum();
			newRightSide.add(solve.getEqu().getRightSide());
			for(int i = 0;i<leftSideSum.size();i++) {
				if(!leftSideSum.get(i).contains(solve.getVar())) {
					newRightSide.add(neg(leftSideSum.get(i)));
					leftSideSum.remove(i);
					i--;
				}
			}
			solve.getEqu().setRightSide(newRightSide.simplify(settings));
			solve.getEqu().setLeftSide(leftSideSum.simplify(settings));
		}
	}
	static void prodMoveNonImportantToRightSide(Solve solve,Settings settings) {
		if(solve.getEqu().getLeftSide() instanceof Prod) {
			Prod leftSideProd = (Prod)solve.getEqu().getLeftSide();
			Prod newRightSide = new Prod();
			newRightSide.add(solve.getEqu().getRightSide());
			for(int i = 0;i<leftSideProd.size();i++) {
				if(!leftSideProd.get(i).contains(solve.getVar())) {
					newRightSide.add(inv(leftSideProd.get(i)));
					leftSideProd.remove(i);
					i--;
				}
			}
			solve.getEqu().setRightSide(newRightSide.simplify(settings));
			solve.getEqu().setLeftSide(leftSideProd.simplify(settings));
		}
	}
	
	@Override
	public String toString() {
		String out = "";
		out+="solve(";
		out+=getEqu().toString();
		out+=',';
		out+=getVar().toString();
		out+=')';
		return out;
	}
	
	public double INITIAL_GUESS = 1;
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {//newton's method
		
		FloatExpr guess = floatExpr(INITIAL_GUESS);
		Expr expr = sub(getEqu().getLeftSide(),getEqu().getRightSide());
		expr = sub(getVar(),div(expr,diff(expr,getVar())));
		ExprList varDefs2 = (ExprList) varDefs.copy();
		varDefs2.add(equ(getVar(),guess));
		
		for(int i = 0;i<16;i++) guess.value = expr.convertToFloat(varDefs2);
		
		return guess.value;
	}
	
	
	
	/////////
	private static double polyOut(double[] poly,double x) {
		double out = 0;
		double pow = 1;
		for(int i = 0;i<poly.length;i++) {
			out += pow*poly[i];
			pow*=x;
		}
		return out;
	}
	static double small = 0.000000000000001;
	private static double secantSolve(double leftBound,double rightBound,double[] poly) {
		double root = 0;
		int count = 0;
		while(count<128) {
			double fL = polyOut(poly,leftBound);
			double fR = polyOut(poly,rightBound);
			if(Math.signum(fL) == Math.signum(fR)) return Double.NaN;//need opposite sign
			double newRoot = -fL*(rightBound-leftBound)/( fR-fL)+leftBound;
			double delta = Math.abs(newRoot-root);
			if(delta<Math.abs(poly[poly.length-1]*small)) {
				return newRoot;
			}
			root = newRoot;
			
			double fRoot = polyOut(poly,root);
			if(Math.signum(fL) <= 0) {
				if(Math.signum(fRoot) <= 0) {
					leftBound = root;
				}else {
					rightBound = root;
				}
			}else {
				if(Math.signum(fRoot) <= 0) {
					rightBound = root;
				}else {
					leftBound = root;
				}
			}
			count++;
		}
		return root;
	}
	private static double newtonSolve(double[] poly,double[] deriv) {
		double guess = 0;
		int count = 0;
		while(count<128) {
			double newGuess = guess-polyOut(poly,guess)/polyOut(deriv,guess);
			double delta = Math.abs(newGuess-guess);
			if(delta<Math.abs(poly[poly.length-1]*small)) {
				return newGuess;
			}
			guess = newGuess;
			count++;
		}
		return guess;
	}
	public static ArrayList<Double> polySolve(ExprList poly) {//an algorithm i came up with to solve all roots of a polynomial
		//init polyArray
		double[] base = new double[poly.size()];
		for(int i = 0;i<poly.size();i++) {
			base[i] = ((Num)poly.get(i)).realValue.doubleValue();
		}
		double[][] table = new double[poly.size()-1][];
		
		table[0] = base;
		
		for(int i = 1;i<table.length;i++) {
			double[] derivative = new double[table[i-1].length-1];
			for(int j = 0;j<derivative.length;j++) {
				derivative[j] = table[i-1][j+1]*(j+1.0);
			}
			table[i] = derivative;
		}
		ArrayList<Double> bounds = null;
		ArrayList<Double> solutions = new ArrayList<Double>();
		//add linear solution
		double[] lin = table[table.length-1];
		solutions.add( (-lin[0])/(lin[1]) );
		
		//recursive solving through the table
		
		for(int i = table.length-2;i>=0;i--) {
			double[] currentPoly = table[i];
			int currentDegree = currentPoly.length-1;
			bounds = solutions;
			solutions = new ArrayList<Double>();
			
			if(bounds.size() == 0 && currentDegree%2==1) {//special case
				solutions.add( newtonSolve(currentPoly,table[i+1]) );
				continue;
			}
			
			//determine if there are solutions to the left or right
			boolean hasLeftSolution = false;
			boolean hasRightSolution = false;
			int leftSign = (int)Math.signum(polyOut( currentPoly,bounds.get(0)));
			int rightSign = (int)Math.signum(polyOut( currentPoly,bounds.get(bounds.size()-1)));
			if(currentDegree%2 == 0) {
				hasLeftSolution = leftSign != (int)Math.signum(currentPoly[currentPoly.length-1]);
			}else {
				hasLeftSolution = leftSign == (int)Math.signum(currentPoly[currentPoly.length-1]);
			}
			hasRightSolution = rightSign != (int)Math.signum(currentPoly[currentPoly.length-1]);
			//find the outer bound lines
			if(hasLeftSolution) {
				double search = bounds.get(0);
				double step = 1.0;
				while((int)Math.signum(polyOut( currentPoly,search)) == leftSign) {
					search -= step;
					step *=2.0;
				}
				bounds.add(0, search);
			}
			if(hasRightSolution) {
				double search = bounds.get(bounds.size()-1);
				double step = 1.0;
				while((int)Math.signum(polyOut( currentPoly,search)) == rightSign) {
					search += step;
					step *= 2.0;
				}
				bounds.add(search);
			}
			//
			
			for(int j = 0;j<bounds.size()-1;j++) {
				double leftBound = bounds.get(j);
				double rightBound = bounds.get(j+1);
				double solution = secantSolve(leftBound,rightBound,currentPoly);
				if(!Double.isNaN(solution)) {
					solutions.add(solution);
				}
			}
		}
		
		return solutions;
	}
	@Override
	ExprList getRuleSequence() {
		// TODO Auto-generated method stub
		return null;
	}

}
