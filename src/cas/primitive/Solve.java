package cas.primitive;
import java.math.BigInteger;
import java.util.ArrayList;

import cas.*;

public class Solve extends Expr{
	
	private static final long serialVersionUID = 8530995692151126277L;
	
	public Solve(){}//
	public Solve(Equ e,Var v){
		add(e);
		add(v);
	}
	
	public Solve(ExprList equs,ExprList vars) {
		add(equs);
		add(vars);
	}
	
	@Override
	public Var getVar() {
		return (Var)get(1);
	}
	
	public ExprList getVars() {
		return (ExprList)get(1);
	}
	
	Equ getEqu() {
		return (Equ)get();
	}
	
	ExprList getEqus() {
		return (ExprList)get();
	}
	
	void setEqu(Equ e) {
		set(0,e);
	}
	
	void setEqus(ExprList equs) {
		set(0,equs);
	}
	
	public boolean singleEq() {
		return get() instanceof Equ;
	}
	public boolean manyEqus() {
		return get() instanceof ExprList;
	}
	
	static Sequence ruleSequence;
	
	static Rule solveSingleEqCase = new Rule("solved equation") {
		private static final long serialVersionUID = 1L;
		
		Sequence loopedSequence;
		
		Rule moveNonVarPartsInProd;
		Rule moveNonVarPartsInSum;
		Rule moveToLeftSide;
		Rule distrLeftSide;
		Rule factorLeftSide;
		Rule rightSideZeroCase;
		Rule powerCase;
		Rule inverseFunctionCase;
		Rule fractionalCase;
		Rule quadraticCase;
		Rule lambertWCases;
		Rule sinCosSumCase;
		Rule rootCases;
		Rule subtractiveZero;
		Rule absCase;
		
		public Expr goThroughEquCases(Expr e,CasInfo casInfo,Rule[] cases) {
			Solve solve = (Solve)e;
			Var v = solve.getVar();
			for(Rule rule:cases) {
				Expr result = rule.applyRuleToExpr(solve.getEqu(), casInfo);
				if(result instanceof Equ) {
					solve.setEqu( (Equ)result );
				}else if(result instanceof ExprList) {
					for(int i = 0;i<result.size();i++) result.set(i, solve((Equ)result.get(i),v)  );
					return result.simplify(casInfo);
				}
			}
			return solve;
		}
		
		@Override
		public void init() {
			
			absCase = new Rule("solve(abs(m)=n,x)->[solve(m=-n,x),solve(m=n,x)]","solving absolute value");
			
			subtractiveZero = new Rule("subtracting two same functions") {
				private static final long serialVersionUID = 1L;
				
				@Override
				public void init() {
					cases = new Rule[] {
							new Rule("m^a-y^a=0->m-y=0","subtracting powers, same exponent"),
							new Rule("a^m-a^y=0->m-y=0","subtracting powers, same base"),
							
							new Rule("ln(a)-ln(y)=0->a-y=0","subtracting logs"),
							new Rule("sin(a)-sin(y)=0->a-y=0","subtracting sins"),
							new Rule("cos(a)-cos(y)=0->a-y=0","subtracting cos"),
							new Rule("tan(a)-tan(y)=0->a-y=0","subtracting tans"),
							new Rule("asin(a)-asin(y)=0->a-y=0","subtracting asins"),
							new Rule("acos(a)-acos(y)=0->a-y=0","subtracting acoss"),
							new Rule("atan(a)-atan(y)=0->a-y=0","subtracting atans"),
					};
					Rule.initRules(cases);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					return goThroughEquCases(e,casInfo,cases);
				}
			};
			
			rootCases = new Rule("solving with roots") {
				private static final long serialVersionUID = 1L;
				
				@Override
				public void init() {
					cases = new Rule[] {
							new Rule("sqrt(a)+sqrt(b)=c->4*a*b-expand((c^2-b-a)^2)=0","sum of linear square roots"),
							new Rule("m*sqrt(a)+n*sqrt(b)=c->4*a*b*m^2*n^2-expand((c^2-b*n^2-a*m^2)^2)=0","sum of linear square roots"),
							
							new Rule("m*sqrt(a)+sqrt(b)=c->4*a*b*m^2-expand((c^2-b-a*m^2)^2)=0","sum of linear square roots"),
							
							new Rule("a+sqrt(b)=c->b-expand((c-a)^2)=0","root sum"),
							new Rule("a+k*sqrt(b)=c->k^2*b-expand((c-a)^2)=0","root sum"),
					};
					Rule.initRules(cases);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					return goThroughEquCases(e,casInfo,cases);
				}
				
			};
			
			sinCosSumCase = new Rule("solving equations with summed sin and cos") {
				private static final long serialVersionUID = 1L;
				
				@Override
				public void init() {
					cases = new Rule[] {
							new Rule("sin(x)+cos(x)=y->x=acos(y/sqrt(2))+pi/4","basic case of summed sin and cos"),
							new Rule("a*sin(x)+b*cos(x)=y->x=acos(y/sqrt(a^2+b^2))+atan(a/b)","hard case of summed sin and cos"),
							new Rule("sin(x)+b*cos(x)=y->x=acos(y/sqrt(1+b^2))+atan(1/b)","hard case of summed sin and cos"),
							new Rule("a*sin(x)+cos(x)=y->x=acos(y/sqrt(a^2+1))+atan(a)","hard case of summed sin and cos"),
					};
					Rule.initRules(cases);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					return goThroughEquCases(e,casInfo,cases);
				}
			};
			
			lambertWCases = new Rule("solving with lambert w") {
				private static final long serialVersionUID = 1L;
				
				@Override
				public void init() {
					cases = new Rule[] {
							new Rule("x*ln(x)=y->x=e^lambertW(y)","basic case of lambert w"),
							new Rule("x*a^x=y->x=lambertW(y*ln(a))/ln(a)","hard case of lambert w"),
							new Rule("x*a^(b*x)=y->x=lambertW(y*b*ln(a))/(b*ln(a))","hard case of lambert w"),
							new Rule("x^n*a^x=y->x=n*lambertW(y^(1/n)*ln(a)/n)/ln(a)","hard case of lambert w"),
							new Rule("x^n*a^(b*x)=y->x=n*lambertW(y^(1/n)*ln(a)*b/n)/(b*ln(a))","hard case of lambert w"),
							new Rule("x+a^x=y->x=y-lambertW(ln(a)*a^y)/ln(a)",""),
							new Rule("b*x+a^x=y->x=(ln(a)*y-b*lambertW((ln(a)*a^(y/b))/b))/(b*ln(a))","hard case of lambert w"),
					};
					Rule.initRules(cases);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					return goThroughEquCases(e,casInfo,cases);
				}
			};
			
			Expr quadAns = createExpr("{x=(-b+sqrt(b^2+4*a*c))/(2*a),x=(-b-sqrt(b^2+4*a*c))/(2*a)}");
			
			quadraticCase = new Rule("solving quadratics") {
				private static final long serialVersionUID = 1L;
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Solve solve = (Solve)e;
					
					Equ equ = solve.getEqu();
					Var v = solve.getVar();
					
					if(equ.getLeftSide() instanceof Prod) {
						Prod leftSide = (Prod)equ.getLeftSide();
						if(leftSide.size() == 2) {
							Expr varPart = null,sumPart = null;
							
							if(leftSide.get(0) instanceof Sum) {
								sumPart = leftSide.get(0);
								varPart = leftSide.get(1);
							}else if(leftSide.get(1) instanceof Sum) {
								sumPart = leftSide.get(1);
								varPart = leftSide.get(0);
							}
							
							if(varPart == null || sumPart == null) return solve;
								
							Expr constantParts = new Sum();
							Expr variableParts = new Sum();
							
							for(int i = 0;i<sumPart.size();i++) {
								if(sumPart.get(i).contains(varPart)) {
									Expr test = div(sumPart.get(i),varPart).simplify(casInfo);
									if(test.contains(varPart)) return solve;
									variableParts.add(test);
								}else {
									constantParts.add(sumPart.get(i));
								}
							}
							
							if(!(variableParts.size() > 0 && constantParts.size() > 0)) return solve;
							
							Expr a = Sum.unCast(variableParts);
							Expr b = Sum.unCast(constantParts);
							Expr c = equ.getRightSide();
							
							//a*x^2+b*x=c
							//x = -b+sqrt(b^2+4*a*c)/2a
							
							Expr answer = quadAns.replace( exprList( equ(var("a"),a),equ(var("b"),b),equ(var("c"),c),equ(var("x"),varPart) ) );
							
							for(int i = 0;i<answer.size();i++) {
								answer.set(i, solve( (Equ)answer.get(i) ,v));
							}
							
							return answer.simplify(casInfo);
						}
					}
					
					return solve;
				}
				
			};
			
			fractionalCase = new Rule("fraction case for solve") {
				private static final long serialVersionUID = 1L;
				Rule mainCase;
				@Override
				public void init() {
					mainCase = new Rule("a/b=c->a-c*b=0","div case for solve");
					mainCase.init();
				}
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Solve solve = (Solve)e;
					solve.setEqu( (Equ)mainCase.applyRuleToExpr(solve.getEqu(), casInfo) );
					return solve;
				}
			};
			
			inverseFunctionCase = new Rule("inverse function case") {
				private static final long serialVersionUID = 1L;
				@Override
				public void init() {
					cases = new Rule[] {
							new Rule("ln(a)=b->a=e^b","log case for solve"),
							
							new Rule("sin(a)=b->{a=asin(b),a=pi-asin(b)}","sin case for solve"),
							new Rule("cos(a)=b->{a=acos(b),a=-acos(b)}","cos case for solve"),
							new Rule("tan(a)=b->{a=atan(b),a=atan(b)-pi}","tan case for solve"),
							
							new Rule("asin(a)=b->a=sin(b)","tan case for solve"),
							new Rule("acos(a)=b->a=cos(b)","tan case for solve"),
							new Rule("atan(a)=b->a=tan(b)","tan case for solve"),
							
							new Rule("lambertW(a)=b->a=b*e^b","tan case for solve"),
					};
					Rule.initRules(cases);
				}
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					return goThroughEquCases(e,casInfo,cases);
				}
			};
			
			powerCase = new Rule("power case for solving") {
				private static final long serialVersionUID = 1L;
				
				Rule rootCase,expoCase,baseAndExpoHaveVar;
				
				@Override
				public void init() {
					rootCase = new Rule("m^n=a->m=a^inv(n)","root case for solve");
					rootCase.init();
					expoCase = new Rule("m^n=a->n=ln(a)/ln(m)","expo case for solve");
					expoCase.init();
					baseAndExpoHaveVar = new Rule("m^n=a->n*ln(m)=ln(a)","preperation for lambert w solve");
					baseAndExpoHaveVar.init();
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Solve solve = (Solve)e;
					
					Var v = solve.getVar();
					
					if(solve.getEqu().getLeftSide() instanceof Power) {
						Power pow = (Power)solve.getEqu().getLeftSide();
						
						boolean baseHasVar = pow.getBase().contains(v);
						boolean expoHasVar = pow.getExpo().contains(v);
						
						if(baseHasVar && !expoHasVar) {
							Div frac = Div.cast(pow.getExpo());
							
							boolean plusMinus = frac.isNumericalAndReal() && ((Num)frac.getNumer()).realValue.mod(BigInteger.TWO).equals(BigInteger.ZERO);
							
							Equ newEqu = (Equ)rootCase.applyRuleToExpr(solve.getEqu(), casInfo);
							
							if(plusMinus) {
								Equ newEquNeg = (Equ)newEqu.copy();
								newEquNeg.setRightSide(neg(newEquNeg.getRightSide()));
								return exprList( solve(newEquNeg,v), solve(newEqu,v) ).simplify(casInfo);
							}
							solve.setEqu(newEqu);
						}else if(!baseHasVar && expoHasVar) {
							solve.setEqu((Equ)expoCase.applyRuleToExpr(solve.getEqu(), casInfo));
						}else if(baseHasVar && expoHasVar) {
							solve.setEqu((Equ)baseAndExpoHaveVar.applyRuleToExpr(solve.getEqu(), casInfo));
						}
						
					}
					
					return solve;
				}
			};
			
			factorLeftSide = new Rule("factor left side") {
				private static final long serialVersionUID = 1L;
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Solve solve = (Solve)e;
					if(solve.getEqu().getLeftSide() instanceof Sum) {
						solve.getEqu().setLeftSide(factor(solve.getEqu().getLeftSide()).simplify(casInfo));
					}
					return solve;
				}
			};
			
			rightSideZeroCase = new Rule("right side is zero") {
				private static final long serialVersionUID = 1L;
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Solve solve = (Solve)e;
					
					Var v = solve.getVar();
					
					if(solve.getEqu().getRightSide().equals(Num.ZERO) && solve.getEqu().getLeftSide() instanceof Prod) {
						ExprList solutions = new ExprList();
						Prod leftSide = (Prod)solve.getEqu().getLeftSide();
						
						for(int i = 0;i<leftSide.size();i++) {
							Expr current = leftSide.get(i);
							if(current.contains(v)) {
								Expr solution = solve( equ(current,num(0)), v).simplify(casInfo);
								if(!(solution instanceof Solve)) {
									solutions.add(solution);
								}
							}
						}
						if(solutions.size()>0) {
							return solutions;
						}
					}
					
					return solve;
				}
			};
			
			distrLeftSide = new Rule("distribute left side") {
				private static final long serialVersionUID = 1L;
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Solve solve = (Solve)e;
					
					if(solve.getEqu().getLeftSide() instanceof Prod) {
						solve.getEqu().setLeftSide(distr(solve.getEqu().getLeftSide()).simplify(casInfo));
					}
					
					return solve;
				}
			};
			
			moveToLeftSide = new Rule("solve(a=b,c)->solve(a-b=0,c)","move everything to the left side");
			
			moveNonVarPartsInProd = new Rule("move non var parts to the right side (prod)") {
				private static final long serialVersionUID = 1L;

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Solve solve = (Solve)e;
					
					Var v = solve.getVar();
					
					if(solve.getEqu().getLeftSide() instanceof Prod) {
						Prod leftSide = (Prod)solve.getEqu().getLeftSide();
						
						Prod rightSide = Prod.cast(solve.getEqu().getRightSide());
						for(int i = 0;i<leftSide.size();i++) {
							if(!leftSide.get(i).contains(v)) {
								Expr temp = leftSide.get(i);
								
								leftSide.remove(i);
								rightSide.add(inv(temp));
								i--;
							}
						}
						
						solve.getEqu().setLeftSide(Prod.unCast(leftSide));
						solve.getEqu().setRightSide(rightSide.simplify(casInfo));
					}
					
					return solve;
				}
			};
			
			moveNonVarPartsInSum = new Rule("move non var parts to the right side (sum)") {
				private static final long serialVersionUID = 1L;

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Solve solve = (Solve)e;
					
					Var v = solve.getVar();
					
					if(solve.getEqu().getLeftSide() instanceof Sum) {
						Sum leftSide = (Sum)solve.getEqu().getLeftSide();
						
						Sum rightSide = Sum.cast(solve.getEqu().getRightSide());
						for(int i = 0;i<leftSide.size();i++) {
							if(!leftSide.get(i).contains(v)) {
								Expr temp = leftSide.get(i);
								leftSide.remove(i);
								rightSide.add(neg(temp));
								i--;
							}
						}
						
						solve.getEqu().setLeftSide(Sum.unCast(leftSide));
						solve.getEqu().setRightSide(rightSide.simplify(casInfo));
					}
					
					return solve;
				}
			};
			
			loopedSequence = sequence(
				rightSideZeroCase,
				moveToLeftSide,
				distrLeftSide,
				moveNonVarPartsInSum,
				factorLeftSide,
				moveNonVarPartsInProd,
				absCase,
				quadraticCase,
				powerCase,
				inverseFunctionCase,
				lambertWCases,
				sinCosSumCase,
				rootCases,
				subtractiveZero,
				fractionalCase
			);
			Rule.initRules(loopedSequence);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Expr oldState = null;
			Solve solve = (Solve)e;
			if(!solve.singleEq()) return e;
			
			while(!solve.getEqu().equals(oldState)) {
				oldState = solve.getEqu().copy();
				for(int i = 0;i<loopedSequence.size();i++) {
					e = ((Rule)loopedSequence.get(i)).applyRuleToExpr(solve, casInfo);
					if(!(e instanceof Solve)) break;
					solve = (Solve)e;
					if(solve.getEqu().getLeftSide().equals(solve.getVar()) && !solve.getEqu().getRightSide().contains(solve.getVar())) {
						return solve.getEqu();
					}
				}
			}
			return e;
		}
	};
	
	static Rule solveSetCase = new Rule("solve a set of equations") {
		private static final long serialVersionUID = 1L;
		
		void removeAnEq(ExprList equs,Var v,CasInfo casInfo,Sequence removed) {//remove an equation reducing the problem
			for(int i = 0;i<equs.size();i++) {
				Expr solution = solve((Equ)equs.get(i),v).simplify(casInfo);
				if(solution instanceof ExprList) solution = solution.get();
				else if(solution instanceof Solve) continue;
				
				removed.add(solution);
				equs.remove(i);
				
				for(int j = 0;j<equs.size();j++) equs.set(j,equs.get(j).replace((Equ)solution));
				
				return;
			}
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Solve solve = (Solve)e;
			
			if(!solve.manyEqus()) return solve;
			
			ExprList vars = solve.getVars();
			
			ExprList reduced = solve.getEqus();
			Sequence removed = new Sequence();//keep reducing problem
			for(int i = 0;i<vars.size();i++) {
				Var v = (Var)vars.get(i);
				removeAnEq(reduced,v,casInfo,removed);
			}
			ExprList variableSolutions = new ExprList();
			
			//work backwards
			for(int i = removed.size()-1;i>=0;i--) {
				Equ currentEq = (Equ)(removed.get(i).replace(variableSolutions).simplify(casInfo));
				variableSolutions.add(currentEq);
			}
			
			return variableSolutions;//done
		}
	};
	
	public static void loadRules() {
		ruleSequence = sequence(
				solveSingleEqCase,
				solveSetCase
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}
	
	public double INITIAL_GUESS = 1;
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {//newton's method
		FloatExpr guess = floatExpr(INITIAL_GUESS);
		if(singleEq()){
			Expr expr = sub(getEqu().getLeftSide(),getEqu().getRightSide());
			expr = sub(getVar(),div(expr,diff(expr,getVar())));
			ExprList varDefs2 = exprList(equ(getVar(),guess));
		
			for(int i = 0;i<16;i++) guess.value = expr.convertToFloat(varDefs2);
		}
		return guess.value;
	}
	
	
	
	///////// polynomial solving
	private static double polyOut(double[] poly,double x) {
		double out = 0;
		double pow = 1;
		for(int i = 0;i<poly.length;i++) {
			out += pow*poly[i];
			pow*=x;
		}
		return out;
	}
	static final double small = 0.000000000000001;
	static final int LOOP_LIMIT = 128;
	private static double secantSolve(double leftBound,double rightBound,double[] poly) {
		double root = 0;
		int count = 0;
		while(count<LOOP_LIMIT) {
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
		while(count<LOOP_LIMIT) {
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
	public static ArrayList<Double> polySolve(Sequence poly) {//an algorithm i came up with to solve all roots of a polynomial
		if(poly.size() == 1) return new ArrayList<Double>();
		//init polyArray
		
		double[] base = new double[poly.size()];
		for(int i = 0;i<poly.size();i++) {
			base[i] = poly.get(i).convertToFloat(exprList()).real;
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
				double step = 0.75;
				int count = 0;
				while((int)Math.signum(polyOut( currentPoly,search)) == leftSign && count < LOOP_LIMIT) {
					search -= step;
					step *=2.0;
					count++;
				}
				bounds.add(0, search);
			}
			if(hasRightSolution) {
				double search = bounds.get(bounds.size()-1);
				double step = 0.75;
				int count = 0;
				while((int)Math.signum(polyOut( currentPoly,search)) == rightSign && count < LOOP_LIMIT) {
					search += step;
					step *= 2.0;
					count++;
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
	public String typeName() {
		return "solve";
	}
	@Override
	public String help() {
		return "solve(equation,variable) the algebraic solver\n"
				+ "examples\n"
				+ "solve(x^x=2,x)->x=ln(2)/lambertW(ln(2))\n"
				+ "solve(x^2=2,x)->{x=-sqrt(2),x=sqrt(2)}";
	}

}
