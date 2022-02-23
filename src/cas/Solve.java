package cas;
import java.math.BigInteger;
import java.util.ArrayList;

public class Solve extends Expr{
	
	private static final long serialVersionUID = 8530995692151126277L;
	
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
	
	static Sequence ruleSequence;
	
	static Rule solvedCase = new Rule("solved equation",Rule.VERY_EASY) {
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
		
		public Expr goThroughEquCases(Expr e,Settings settings,Rule[] cases) {
			Solve solve = (Solve)e;
			Var v = solve.getVar();
			for(Rule rule:cases) {
				Expr result = rule.applyRuleToExpr(solve.getEqu(), settings);
				if(result instanceof Equ) {
					solve.setEqu( (Equ)result );
				}else if(result instanceof ExprList) {
					for(int i = 0;i<result.size();i++) result.set(i, solve((Equ)result.get(i),v)  );
					return result.simplify(settings);
				}
			}
			return solve;
		}
		
		@Override
		public void init() {
			
			absCase = new Rule("solve(abs(m)=n,x)->[solve(m=-n,x),solve(m=n,x)]","solving absolute value",Rule.EASY);
			
			subtractiveZero = new Rule("subtracting two same functions",Rule.UNCOMMON) {
				private static final long serialVersionUID = 1L;
				
				Rule[] cases;
				@Override
				public void init() {
					cases = new Rule[] {
							new Rule("m^a-y^a=0->m-y=0","subtracting powers, same exponent",Rule.UNCOMMON),
							new Rule("a^m-a^y=0->m-y=0","subtracting powers, same base",Rule.UNCOMMON),
							
							new Rule("ln(a)-ln(y)=0->a-y=0","subtracting logs",Rule.UNCOMMON),
							new Rule("sin(a)-sin(y)=0->a-y=0","subtracting sins",Rule.UNCOMMON),
							new Rule("cos(a)-cos(y)=0->a-y=0","subtracting cos",Rule.UNCOMMON),
							new Rule("tan(a)-tan(y)=0->a-y=0","subtracting tans",Rule.UNCOMMON),
							new Rule("asin(a)-asin(y)=0->a-y=0","subtracting asins",Rule.UNCOMMON),
							new Rule("acos(a)-acos(y)=0->a-y=0","subtracting acoss",Rule.UNCOMMON),
							new Rule("atan(a)-atan(y)=0->a-y=0","subtracting atans",Rule.UNCOMMON),
					};
					Rule.initRules(cases);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					return goThroughEquCases(e,settings,cases);
				}
			};
			
			rootCases = new Rule("solving with roots",Rule.VERY_DIFFICULT) {
				private static final long serialVersionUID = 1L;
				
				Rule[] cases;
				@Override
				public void init() {
					cases = new Rule[] {
							new Rule("sqrt(x)-sqrt(x+a)=k->x=(k^2-a)^2/(4*k^2)","sum of linear square roots",Rule.DIFFICULT),
							new Rule("sqrt(x+a)-sqrt(x)=k->x=(k^2-a)^2/(4*k^2)","sum of linear square roots",Rule.DIFFICULT),
							new Rule("sqrt(x+a)+sqrt(x)=k->x=(k^2-a)^2/(4*k^2)","sum of linear square roots",Rule.DIFFICULT),
							
							new Rule("x+sqrt(x+a)=y->[x=(sqrt(4*a+4*y+1)+2*y+1)/2,x=(-sqrt(4*a+4*y+1)+2*y+1)/2]","sum of linear square roots",Rule.DIFFICULT),
							new Rule("x-sqrt(x+a)=y->[x=(sqrt(4*a+4*y+1)+2*y+1)/2,x=(-sqrt(4*a+4*y+1)+2*y+1)/2]","sum of linear square roots",Rule.DIFFICULT),
							new Rule("sqrt(x+a)-x=y->[x=(sqrt(4*a-4*y+1)-2*y+1)/2,x=(-sqrt(4*a-4*y+1)-2*y+1)/2]","sum of linear square roots",Rule.DIFFICULT),
							
							new Rule("y*x+sqrt(a+m*x+b*x^2)=z->[x=(sqrt(-4*a*b+4*a*y^2+4*b*z^2+m^2+4*m*y*z)+m+2*y*z)/(2*(y^2-b)),x=(-sqrt(-4*a*b+4*a*y^2+4*b*z^2+m^2+4*m*y*z)+m+2*y*z)/(2*(y^2-b))]","linear plus square root of quadratic",Rule.VERY_DIFFICULT),
							new Rule("y*x-sqrt(a+m*x+b*x^2)=z->[x=(sqrt(-4*a*b+4*a*y^2+4*b*z^2+m^2+4*m*y*z)+m+2*y*z)/(2*(y^2-b)),x=(-sqrt(-4*a*b+4*a*y^2+4*b*z^2+m^2+4*m*y*z)+m+2*y*z)/(2*(y^2-b))]","linear plus square root of quadratic",Rule.VERY_DIFFICULT),
							new Rule("-y*x+sqrt(a+m*x+b*x^2)=z->[x=(sqrt(-4*a*b+4*a*y^2+4*b*z^2+m^2+4*m*y*z)+m+2*y*z)/(2*(b-y^2)),x=(-sqrt(-4*a*b+4*a*y^2+4*b*z^2+m^2+4*m*y*z)+m+2*y*z)/(2*(b-y^2))]","linear plus square root of quadratic",Rule.VERY_DIFFICULT),
							
							new Rule("y*x+k*sqrt(a+m*x+b*x^2)=z->[x=(-sqrt(-4*a*b*k^4+4*a*k^2*y^2+4*b*k^2*z^2+k^4*m^2+4*k^2*m*y*z)-k^2*m-2*y*z)/(2*(b*k^2-y^2)),x=(sqrt(-4*a*b*k^4+4*a*k^2*y^2+4*b*k^2*z^2+k^4*m^2+4*k^2*m*y*z)-k^2*m-2*y*z)/(2*(b*k^2-y^2))]","linear plus square root of quadratic",Rule.VERY_DIFFICULT),
							new Rule("y*x-k*sqrt(a+m*x+b*x^2)=z->[x=(-sqrt(-4*a*b*k^4+4*a*k^2*y^2+4*b*k^2*z^2+k^4*m^2+4*k^2*m*y*z)-k^2*m-2*y*z)/(2*(b*k^2-y^2)),x=(sqrt(-4*a*b*k^4+4*a*k^2*y^2+4*b*k^2*z^2+k^4*m^2+4*k^2*m*y*z)-k^2*m-2*y*z)/(2*(b*k^2-y^2))]","linear plus square root of quadratic",Rule.VERY_DIFFICULT),
							new Rule("-y*x+k*sqrt(a+m*x+b*x^2)=z->[x=(-sqrt(-4*a*b*k^4+4*a*k^2*y^2+4*b*k^2*z^2+k^4*m^2+4*k^2*m*y*z)-k^2*m-2*y*z)/(-2*(b*k^2-y^2)),x=(sqrt(-4*a*b*k^4+4*a*k^2*y^2+4*b*k^2*z^2+k^4*m^2+4*k^2*m*y*z)-k^2*m-2*y*z)/(-2*(b*k^2-y^2))]","linear plus square root of quadratic",Rule.VERY_DIFFICULT),
							
					};
					Rule.initRules(cases);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					return goThroughEquCases(e,settings,cases);
				}
				
			};
			
			sinCosSumCase = new Rule("solving equations with summed sin and cos",Rule.UNCOMMON) {
				private static final long serialVersionUID = 1L;
				
				Rule[] cases;
				@Override
				public void init() {
					cases = new Rule[] {
							new Rule("sin(x)+cos(x)=y->x=acos(y/sqrt(2))+pi/4","basic case of summed sin and cos",Rule.UNCOMMON),
							new Rule("a*sin(x)+b*cos(x)=y->x=acos(y/sqrt(a^2+b^2))+atan(a/b)","hard case of summed sin and cos",Rule.UNCOMMON),
							new Rule("sin(x)+b*cos(x)=y->x=acos(y/sqrt(1+b^2))+atan(1/b)","hard case of summed sin and cos",Rule.UNCOMMON),
							new Rule("a*sin(x)+cos(x)=y->x=acos(y/sqrt(a^2+1))+atan(a)","hard case of summed sin and cos",Rule.UNCOMMON),
					};
					Rule.initRules(cases);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					return goThroughEquCases(e,settings,cases);
				}
			};
			
			lambertWCases = new Rule("solving with lambert w",Rule.UNCOMMON) {
				private static final long serialVersionUID = 1L;
				
				Rule[] cases;
				@Override
				public void init() {
					cases = new Rule[] {
							new Rule("x*ln(x)=y->x=e^lambertW(y)","basic case of lambert w",Rule.UNCOMMON),
							new Rule("x*a^x=y->x=lambertW(y*ln(a))/ln(a)","hard case of lambert w",Rule.UNCOMMON),
							new Rule("x*a^(b*x)=y->x=lambertW(y*b*ln(a))/(b*ln(a))","hard case of lambert w",Rule.UNCOMMON),
							new Rule("x^n*a^x=y->x=n*lambertW(y^(1/n)*ln(a)/n)/ln(a)","hard case of lambert w",Rule.UNCOMMON),
							new Rule("x^n*a^(b*x)=y->x=n*lambertW(y^(1/n)*ln(a)*b/n)/(b*ln(a))","hard case of lambert w",Rule.UNCOMMON),
							new Rule("x+a^x=y->x=y-lambertW(ln(a)*a^y)/ln(a)","",Rule.UNCOMMON),
							new Rule("b*x+a^x=y->x=(ln(a)*y-b*lambertW((ln(a)*a^(y/b))/b))/(b*ln(a))","hard case of lambert w",Rule.UNCOMMON),
					};
					Rule.initRules(cases);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					return goThroughEquCases(e,settings,cases);
				}
			};
			
			quadraticCase = new Rule("",Rule.TRICKY) {
				private static final long serialVersionUID = 1L;
				
				Expr ans;
				@Override
				public void init() {
					ans = createExpr("[x=(-b+sqrt(b^2+4*a*c))/2*a,x=(-b-sqrt(b^2+4*a*c))/2*a]");
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
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
									Expr test = div(sumPart.get(i),varPart).simplify(settings);
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
							
							Expr answer = ans.replace( exprList( equ(var("a"),a),equ(var("b"),b),equ(var("c"),c),equ(var("x"),varPart) ) );
							
							for(int i = 0;i<answer.size();i++) {
								answer.set(i, solve( (Equ)answer.get(i) ,v));
							}
							
							return answer.simplify(settings);
						}
					}
					
					return solve;
				}
				
			};
			
			fractionalCase = new Rule("fraction case for solve",Rule.EASY) {
				private static final long serialVersionUID = 1L;
				Rule mainCase;
				@Override
				public void init() {
					mainCase = new Rule("a/b=c->a-c*b=0","div case for solve",Rule.EASY);
					mainCase.init();
				}
				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					Solve solve = (Solve)e;
					solve.setEqu( (Equ)mainCase.applyRuleToExpr(solve.getEqu(), settings) );
					return solve;
				}
			};
			
			inverseFunctionCase = new Rule("inverse function case",Rule.EASY) {
				private static final long serialVersionUID = 1L;
				Rule[] cases;
				@Override
				public void init() {
					cases = new Rule[] {
							new Rule("ln(a)=b->a=e^b","log case for solve",Rule.EASY),
							
							new Rule("sin(a)=b->[a=asin(b),a=pi-asin(b)]","sin case for solve",Rule.EASY),
							new Rule("cos(a)=b->[a=acos(b),a=-acos(b)]","cos case for solve",Rule.EASY),
							new Rule("tan(a)=b->[a=atan(b),a=atan(b)-pi]","tan case for solve",Rule.EASY),
							
							new Rule("asin(a)=b->a=sin(b)","tan case for solve",Rule.EASY),
							new Rule("acos(a)=b->a=cos(b)","tan case for solve",Rule.EASY),
							new Rule("atan(a)=b->a=tan(b)","tan case for solve",Rule.EASY),
							
							new Rule("lambertW(a)=b->a=b*e^b","tan case for solve",Rule.EASY),
					};
					Rule.initRules(cases);
				}
				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					return goThroughEquCases(e,settings,cases);
				}
			};
			
			powerCase = new Rule("power case for solving",Rule.EASY) {
				private static final long serialVersionUID = 1L;
				
				Rule rootCase,expoCase,baseAndExpoHaveVar;
				
				@Override
				public void init() {
					rootCase = new Rule("m^n=a->m=a^inv(n)","root case for solve",Rule.EASY);
					rootCase.init();
					expoCase = new Rule("m^n=a->n=ln(a)/ln(m)","expo case for solve",Rule.EASY);
					expoCase.init();
					baseAndExpoHaveVar = new Rule("m^n=a->n*ln(m)=ln(a)","preperation for lambert w solve",Rule.UNCOMMON);
					baseAndExpoHaveVar.init();
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					Solve solve = (Solve)e;
					
					Var v = solve.getVar();
					
					if(solve.getEqu().getLeftSide() instanceof Power) {
						Power pow = (Power)solve.getEqu().getLeftSide();
						
						boolean baseHasVar = pow.getBase().contains(v);
						boolean expoHasVar = pow.getExpo().contains(v);
						
						if(baseHasVar && !expoHasVar) {
							Div frac = Div.cast(pow.getExpo());
							
							boolean plusMinus = frac.isNumericalAndReal() && ((Num)frac.getNumer()).realValue.mod(BigInteger.TWO).equals(BigInteger.ZERO);
							
							Equ newEqu = (Equ)rootCase.applyRuleToExpr(solve.getEqu(), settings);
							
							if(plusMinus) {
								Equ newEquNeg = (Equ)newEqu.copy();
								newEquNeg.setRightSide(neg(newEquNeg.getRightSide()));
								return exprList( solve(newEquNeg,v), solve(newEqu,v) ).simplify(settings);
							}
							solve.setEqu(newEqu);
						}else if(!baseHasVar && expoHasVar) {
							solve.setEqu((Equ)expoCase.applyRuleToExpr(solve.getEqu(), settings));
						}else if(baseHasVar && expoHasVar) {
							solve.setEqu((Equ)baseAndExpoHaveVar.applyRuleToExpr(solve.getEqu(), settings));
						}
						
					}
					
					return solve;
				}
			};
			
			factorLeftSide = new Rule("factor left side",Rule.EASY) {
				private static final long serialVersionUID = 1L;
				
				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					Solve solve = (Solve)e;
					if(solve.getEqu().getLeftSide() instanceof Sum) {
						solve.getEqu().setLeftSide(factor(solve.getEqu().getLeftSide()).simplify(settings));
					}
					return solve;
				}
			};
			
			rightSideZeroCase = new Rule("right side is zero",Rule.EASY) {
				private static final long serialVersionUID = 1L;
				
				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					Solve solve = (Solve)e;
					
					Var v = solve.getVar();
					
					if(solve.getEqu().getRightSide().equals(Num.ZERO) && solve.getEqu().getLeftSide() instanceof Prod) {
						ExprList solutions = new ExprList();
						Prod leftSide = (Prod)solve.getEqu().getLeftSide();
						
						for(int i = 0;i<leftSide.size();i++) {
							Expr current = leftSide.get(i);
							if(current.contains(v)) {
								Expr solution = solve( equ(current,num(0)), v).simplify(settings);
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
			
			distrLeftSide = new Rule("distribute left side",Rule.EASY) {
				private static final long serialVersionUID = 1L;
				
				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					Solve solve = (Solve)e;
					
					if(solve.getEqu().getLeftSide() instanceof Prod) {
						solve.getEqu().setLeftSide(distr(solve.getEqu().getLeftSide()).simplify(settings));
					}
					
					return solve;
				}
			};
			
			moveToLeftSide = new Rule("move everything to the left side",Rule.VERY_EASY) {
				private static final long serialVersionUID = 1L;
				
				Rule[] cases;
				
				@Override
				public void init() {
					cases = new Rule[] {
							new Rule("a=b->a-b=0","move everything to the left side",Rule.VERY_EASY),
							new Rule("a>b->a-b>0","move everything to the left side",Rule.VERY_EASY),
							new Rule("a<b->a-b<0","move everything to the left side",Rule.VERY_EASY),
					};
					Rule.initRules(cases);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					return goThroughEquCases(e,settings,cases);
				}
				
			};
			
			moveNonVarPartsInProd = new Rule("move non var parts to the right side (prod)",Rule.EASY) {
				private static final long serialVersionUID = 1L;

				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
					Solve solve = (Solve)e;
					
					Var v = solve.getVar();
					
					if(solve.getEqu().getLeftSide() instanceof Prod) {
						Prod leftSide = (Prod)solve.getEqu().getLeftSide();
						
						Prod rightSide = Prod.cast(solve.getEqu().getRightSide());
						for(int i = 0;i<leftSide.size();i++) {
							if(!leftSide.get(i).contains(v)) {
								Expr temp = leftSide.get(i);
								
								if(!temp.containsVars()) {
									if(eval(equLess(temp,num(0))).simplify(settings).equals(BoolState.TRUE)) {
										solve.getEqu().type = -solve.getEqu().type;
									}
								}
								
								leftSide.remove(i);
								rightSide.add(inv(temp));
								i--;
							}
						}
						
						solve.getEqu().setLeftSide(Prod.unCast(leftSide));
						solve.getEqu().setRightSide(rightSide.simplify(settings));
					}
					
					return solve;
				}
			};
			
			moveNonVarPartsInSum = new Rule("move non var parts to the right side (sum)",Rule.EASY) {
				private static final long serialVersionUID = 1L;

				@Override
				public Expr applyRuleToExpr(Expr e,Settings settings){
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
						solve.getEqu().setRightSide(rightSide.simplify(settings));
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
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Expr oldState = null;
			Solve solve = (Solve)e;
			while(!solve.getEqu().equals(oldState)) {
				oldState = solve.getEqu().copy();
				for(int i = 0;i<loopedSequence.size();i++) {
					e = ((Rule)loopedSequence.get(i)).applyRuleToExpr(solve, settings);
					if(!(e instanceof Solve)) break;
					solve = (Solve)e;
				}
				if(e instanceof Solve) {
					solve = (Solve)e;
					if(solve.getEqu().getLeftSide().equals(solve.getVar())) {
						return solve.getEqu();
					}
				}
			}
			return e;
		}
	};
	
	public static void loadRules() {
		ruleSequence = sequence(
				solvedCase
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	Sequence getRuleSequence() {
		return ruleSequence;
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
		ExprList varDefs2 = exprList(equ(getVar(),guess));
		
		for(int i = 0;i<16;i++) guess.value = expr.convertToFloat(varDefs2);
		
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

}
