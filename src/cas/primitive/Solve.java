package cas.primitive;
import java.math.BigInteger;
import java.util.ArrayList;

import cas.*;
import cas.bool.BoolState;

public class Solve{
	
	public static Func.FuncLoader solveLoader = new Func.FuncLoader() {
		
		public double INITIAL_GUESS = 1;
		
		@Override
		public void load(Func owner) {
			
			Rule solveSingleEqCase = new Rule("solved equation") {
				Sequence loopedSequence;
				
				Rule moveToLeftSide;
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
				
				@Override
				public void init() {
					
					absCase = new Rule("solve(abs(m)=n,x)->singleSolutionMode()?solve(m=n,x):{solve(m=-n,x),solve(m=n,x)}","solving absolute value");
					
					subtractiveZero = new Rule("subtracting two same functions") {
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
					
					quadraticCase = new Rule("solving quadratics") {
						Expr quadAns = null,quadSingleAns = null;
						
						@Override
						public void init(){
							
							quadAns = createExpr("{x=(-b+sqrt(b^2+4*a*c))/(2*a),x=(-b-sqrt(b^2+4*a*c))/(2*a)}");
							quadSingleAns = createExpr("x=(-b+sqrt(b^2+4*a*c))/(2*a)");
							
						}
						
						@Override
						public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
							Func solve = (Func)e;
							
							Equ equ = getEqu(solve);
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
									
									Expr answer = null;
									
									if(!casInfo.singleSolutionMode()) {
										answer = quadAns.replace( exprList( equ(var("a"),a),equ(var("b"),b),equ(var("c"),c),equ(var("x"),varPart) ) );
										for(int i = 0;i<answer.size();i++) {
											answer.set(i, solve( (Equ)answer.get(i) ,v));
										}
										
										return answer.simplify(casInfo);
									}
									
									//if we are in single solution mode
									answer = quadSingleAns.replace( exprList( equ(var("a"),a),equ(var("b"),b),equ(var("c"),c),equ(var("x"),varPart) ) );
									setEqu(solve,(Equ) answer.simplify(casInfo));
								}
							}
							
							return solve;
						}
						
					};
					
					fractionalCase = new Rule("fraction case for solve") {
						Rule mainCase;
						@Override
						public void init() {
							mainCase = new Rule("a/b=c->a-c*b=0","div case for solve");
							mainCase.init();
						}
						@Override
						public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
							Func solve = (Func)e;
							setEqu(solve, (Equ)mainCase.applyRuleToExpr(getEqu(solve), casInfo) );
							return solve;
						}
					};
					
					inverseFunctionCase = new Rule("inverse function case") {
						@Override
						public void init() {
							cases = new Rule[] {
									new Rule("ln(a)=b->a=e^b","log case for solve"),
									
									new Rule("sin(a)=b->singleSolutionMode()?a=asin(b):{a=asin(b),a=pi-asin(b)}","sin case for solve"),
									new Rule("cos(a)=b->singleSolutionMode()?a=acos(b):{a=acos(b),a=-acos(b)}","cos case for solve"),
									new Rule("tan(a)=b->singleSolutionMode()?a=atan(b):{a=atan(b),a=atan(b)-pi}","tan case for solve"),
									
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
							Func solve = (Func)e;
							
							Var v = solve.getVar();
							
							if(getEqu(solve).getLeftSide().typeName().equals("power")) {
								Func pow = (Func)getEqu(solve).getLeftSide();
								
								boolean baseHasVar = pow.getBase().contains(v);
								boolean expoHasVar = pow.getExpo().contains(v);
								
								if(baseHasVar && !expoHasVar) {
									Func frac = Div.cast(pow.getExpo());
									
									boolean plusMinus = !casInfo.singleSolutionMode() && Div.isNumericalAndReal(frac) && ((Num)frac.getNumer()).getRealValue().mod(BigInteger.TWO).equals(BigInteger.ZERO);
									
									Equ newEqu = (Equ)rootCase.applyRuleToExpr(getEqu(solve), casInfo);
									
									if(plusMinus) {
										Equ newEquNeg = (Equ)newEqu.copy();
										newEquNeg.setRightSide(neg(newEquNeg.getRightSide()));
										return exprList( solve(newEquNeg,v), solve(newEqu,v) ).simplify(casInfo);
									}
									setEqu(solve,newEqu);
								}else if(!baseHasVar && expoHasVar) {
									setEqu(solve,(Equ)expoCase.applyRuleToExpr(getEqu(solve), casInfo));
								}else if(baseHasVar && expoHasVar) {
									setEqu(solve,(Equ)baseAndExpoHaveVar.applyRuleToExpr(getEqu(solve), casInfo));
								}
								
							}
							
							return solve;
						}
					};
					
					rightSideZeroCase = new Rule("right side is zero") {
						@Override
						public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
							Func solve = (Func)e;
							
							Var v = solve.getVar();
							
							if(getEqu(solve).getRightSide().equals(Num.ZERO) && getEqu(solve).getLeftSide() instanceof Prod) {
								ExprList solutions = new ExprList();
								Prod leftSide = (Prod)getEqu(solve).getLeftSide();
								
								for(int i = 0;i<leftSide.size();i++) {
									Expr current = leftSide.get(i);
									if(current.contains(v)) {
										Expr solution = solve( equ(current,num(0)), v).simplify(casInfo);
										if(!(solution.typeName().equals("solve"))) {
											solutions.add(solution);
										}
									}
								}
								if(solutions.size()>0) {
									if(!casInfo.singleSolutionMode()) return solutions;
									return solutions.get();
								}
							}
							
							return solve;
						}
					};
					
					moveToLeftSide = new Rule("solve(a=b,c)->solve(a-b=0,c)","move everything to the left side");
					
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
					Func solve = (Func)e;
					if(!singleEq(solve)) return e;
					
					outer:while(!getEqu(solve).equals(oldState)) {
						oldState = getEqu(solve).copy();
						for(int i = 0;i<loopedSequence.size();i++) {
							e = ((Rule)loopedSequence.get(i)).applyRuleToExpr(solve, casInfo);
							if(!(e.typeName().equals("solve"))) break outer;
							solve = (Func)e;
							if(getEqu(solve).getLeftSide().equals(solve.getVar()) && !getEqu(solve).getRightSide().contains(solve.getVar())) {
								return getEqu(solve);
							}
						}
					}
					return e;
				}
			};
			
			owner.behavior.rule = new Rule(new Rule[]{
				solveSingleEqCase,
				solveSetCase,
				comparisonSolve
			},"main sequence");
			owner.behavior.rule.init();
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				
				@Override
				public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
					FloatExpr guess = Cas.floatExpr(INITIAL_GUESS);
					if(singleEq(owner)){
						Expr expr = Cas.sub(getEqu(owner).getLeftSide(),getEqu(owner).getRightSide());
						expr = Cas.sub(owner.getVar(),Cas.div(expr,Cas.diff(expr,owner.getVar())));
						ExprList varDefs2 = Cas.exprList(Cas.equ(owner.getVar(),guess));
					
						for(int i = 0;i<16;i++) guess.value = expr.convertToFloat(varDefs2);
					}
					return guess.value;
				}
			};
			
		}
	};
	
	static ExprList getVars(Func solve){
		return (ExprList) solve.get(1);
	}
	
	static void setComparison(Func solve,Expr c){
		solve.set(0,c);
	}
	
	static void flipComparison(Func solve){
		Expr comp = solve.getComparison();
		if(comp instanceof Less){
			setComparison(solve,Cas.equGreater( Cas.getLeftSideGeneric(comp) , Cas.getRightSideGeneric(comp) ));
		}else if(comp instanceof Greater){
			setComparison(solve,Cas.equLess( Cas.getLeftSideGeneric(comp) , Cas.getRightSideGeneric(comp) ));
		}
	}
	
	
	static Equ getEqu(Func solve) {
		return (Equ)solve.get();
	}
	
	static ExprList getEqus(Func solve) {
		return (ExprList)solve.get();
	}
	
	static void setEqu(Func solve,Equ e) {
		solve.set(0,e);
	}
	
	void setEqus(Func solve,ExprList equs) {
		solve.set(0,equs);
	}
	
	public static boolean singleEq(Func solve) {
		return solve.get() instanceof Equ;
	}
	public static boolean manyEqus(Func solve) {
		return solve.get() instanceof ExprList;
	}
	
	public static boolean comparisonEq(Func solve){
		Expr in = solve.get();
		return in instanceof Greater || in instanceof Less;
	}
	
	static Sequence ruleSequence;
	
	public static Expr goThroughEquCases(Expr e,CasInfo casInfo,Rule[] cases) {
		Func solve = (Func)e;
		Var v = solve.getVar();
		for(Rule rule:cases) {
			Expr result = rule.applyRuleToExpr(solve.get(), casInfo);
			if(result instanceof ExprList) {
				for(int i = 0;i<result.size();i++){
					if(result instanceof Equ) result.set(i, Cas.solve((Equ)result.get(i),v));
					if(result instanceof Less) result.set(i, Cas.solve((Less)result.get(i),v)  );
					if(result instanceof Greater) result.set(i, Cas.solve((Greater)result.get(i),v)  );
				}
				return result.simplify(casInfo);
			}else{
				solve.set(0, result );
			}
		}
		return solve;
	}
	
	static Rule solveSetCase = new Rule("solve a set of equations") {
		void removeAnEq(ExprList equs,Var v,CasInfo casInfo,Sequence removed) {//remove an equation reducing the problem
			CasInfo singleSolutionModeCasInfo = new CasInfo(casInfo);
			singleSolutionModeCasInfo.setSingleSolutionMode(true);
			
			for(int i = 0;i<equs.size();i++) {
				Expr solution = solve((Equ)equs.get(i),v).simplify(singleSolutionModeCasInfo);
				if(solution instanceof ExprList) solution = solution.get();
				else if(solution.typeName().equals("solve")) continue;
				
				removed.add(solution);
				equs.remove(i);
				
				for(int j = 0;j<equs.size();j++) equs.set(j,equs.get(j).replace((Equ)solution));
				
				return;
			}
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func solve = (Func)e;
			solve.sort();//always solve in the same way regardless of input order
			
			if(!manyEqus(solve)) return solve;
			
			ExprList vars = getVars(solve);
			
			ExprList reduced = (ExprList) getEqus(solve).copy();
			Sequence removed = new Sequence();//keep reducing problem
			for(int i = 0;i<vars.size();i++) {
				Var v = (Var)vars.get(i);
				int oldSize = reduced.size();
				removeAnEq(reduced,v,casInfo,removed);
				if(oldSize == reduced.size()) return e;//did not reduce, stop solve
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
	
	public static Rule comparisonSolve = new Rule("solve a comparison"){
		Sequence loopedSequence;
		
		Rule moveToLeftSide;
		Rule powerCase;
		
		@Override
		public void init(){
			moveToLeftSide = new Rule("move everything to the left side"){
				@Override
				public void init(){
					cases = new Rule[]{
							new Rule("x>y->x-y>0","move everything to the left side"),
							new Rule("x<y->x-y<0","move everything to the left side"),
					};
					Rule.initRules(cases);
				}
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					return goThroughEquCases(e,casInfo,cases);
				}
			};
			
			powerCase = new Rule("reversing powers"){
				Rule expReversal;
				
				@Override
				public void init(){
					expReversal = new Rule(new Rule[]{
							new Rule("solve(a^b>c,d)->solve(b>ln(c)/ln(a),d)","exponential comparison reversal"),
							new Rule("solve(a^b<c,d)->solve(b<ln(c)/ln(a),d)","exponential comparison reversal"),
					},"exponential comparison reversal");
					expReversal.init();
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func solve = (Func)e;
					
					Var v = solve.getVar();
					if(getLeftSideGeneric(solve.getComparison()).typeName().equals("power")){
						Func leftSide = (Func)getLeftSideGeneric(solve.getComparison());
						
						boolean baseHasVar = leftSide.getBase().contains(v), expoHasVar = leftSide.getExpo().contains(v);
						
						if(baseHasVar && !expoHasVar){
							
							setLeftSideGeneric(solve.getComparison(),leftSide.getBase());
							setRightSideGeneric(solve.getComparison(), power(getRightSideGeneric(solve.getComparison()),inv(leftSide.getExpo())).simplify(casInfo) );
							
							if( !leftSide.getExpo().containsVars() && comparison(equLess(leftSide.getExpo(),num(0))).simplify(casInfo).equals(BoolState.TRUE) ){
								flipComparison(solve);
							}
							
							Func frac = Div.cast(leftSide.getExpo());
							
							boolean twoParts = !casInfo.singleSolutionMode() && Div.isNumericalAndReal(frac) && ((Num)frac.getNumer()).getRealValue().mod(BigInteger.TWO).equals(BigInteger.ZERO);
							if(twoParts){
								Func negSolve = (Func)solve.copy();
								flipComparison(negSolve);
								setRightSideGeneric(negSolve.getComparison(),neg(getRightSideGeneric(negSolve.getComparison())).simplify(casInfo));
								Expr out = exprList(solve,negSolve);
								return out.simplify(casInfo);
							}
						}else if(!baseHasVar && expoHasVar){
							solve = (Func) expReversal.applyRuleToExpr(solve, casInfo);
						}
						
					}
					
					return solve;
				}
			};
			
			loopedSequence = sequence(
					moveToLeftSide,
					distrLeftSide,
					moveNonVarPartsInSum,
					factorLeftSide,
					moveNonVarPartsInProd,
					powerCase
			);
			Rule.initRules(loopedSequence);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Expr oldState = null;
			Func solve = (Func)e;
			if(!comparisonEq(solve)) return e;
			
			outer:while(!solve.getComparison().equals(oldState)) {
				oldState = solve.getComparison().copy();
				for(int i = 0;i<loopedSequence.size();i++) {
					e = ((Rule)loopedSequence.get(i)).applyRuleToExpr(solve, casInfo);
					if(!(e.typeName().equals("solve"))) break outer;
					solve = (Func)e;
					if(getLeftSideGeneric(solve.getComparison()).equals(solve.getVar()) && !getRightSideGeneric(solve.getComparison()).contains(solve.getVar())) {
						return solve.getComparison();
					}
				}
			}
			return e;
		}
	};
	
	//
	
	static Rule moveNonVarPartsInSum = new Rule("move non var parts to the right side (sum)"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func solve = (Func)e;
			Var v = solve.getVar();
			
			if(getLeftSideGeneric(solve.getComparison()) instanceof Sum){
				Sum leftSide = (Sum)getLeftSideGeneric(solve.getComparison());
				Sum rightSide = Sum.cast(getRightSideGeneric(solve.getComparison()));
				
				for(int i = 0;i < leftSide.size();i++){
					Expr current = leftSide.get(i);
					
					if(!current.contains(v)){
						leftSide.remove(i);
						rightSide.add(neg(current));
						i--;
					}
				}
				
				setLeftSideGeneric(solve.getComparison(),Sum.unCast(leftSide));
				setRightSideGeneric(solve.getComparison(),rightSide.simplify(casInfo));
			}
			
			return solve;
		}
	};
	
	static Rule moveNonVarPartsInProd = new Rule("move non var parts to the right side (prod)"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func solve = (Func)e;
			Var v = solve.getVar();
			
			if(getLeftSideGeneric(solve.getComparison()) instanceof Prod){
				Prod leftSide = (Prod)getLeftSideGeneric(solve.getComparison());
				Prod rightSide = Prod.cast(getRightSideGeneric(solve.getComparison()));
				
				boolean flip = false;
				boolean isComp = solve.getComparison() instanceof Less || solve.getComparison() instanceof Greater;
				
				for(int i = 0;i < leftSide.size();i++){
					Expr current = leftSide.get(i);
					
					if(isComp && !current.containsVars() && comparison(equLess(current,num(0))).simplify(casInfo).equals(BoolState.TRUE) ){
						flip = !flip;
					}
					
					if(!current.contains(v)){
						leftSide.remove(i);
						rightSide.add(inv(current));
						i--;
					}
				}
				
				setLeftSideGeneric(solve.getComparison(),Sum.unCast(leftSide));
				setRightSideGeneric(solve.getComparison(),rightSide.simplify(casInfo));
				if(flip) flipComparison(solve);
				
			}
			
			return solve;
		}
	};
	
	static Rule factorLeftSide = new Rule("factor left side") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func solve = (Func)e;
			if(getLeftSideGeneric(solve.getComparison()) instanceof Sum) {
				setLeftSideGeneric(solve.getComparison(), factor(getLeftSideGeneric(solve.getComparison())).simplify(casInfo));
			}
			return solve;
		}
	};
	static Rule distrLeftSide = new Rule("factor left side") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func solve = (Func)e;
			if(getLeftSideGeneric(solve.getComparison()) instanceof Sum) {
				setLeftSideGeneric(solve.getComparison(), distr(getLeftSideGeneric(solve.getComparison())).simplify(casInfo));
			}
			return solve;
		}
	};
	
	
	
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
			base[i] = poly.get(i).convertToFloat(Cas.exprList()).real;
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
