package cas;

import java.math.BigInteger;

public class Integrate extends Expr{

	private static final long serialVersionUID = 5071855237530369367L;
	
	static Rule zeroCase = new Rule("integrate(0,x)=0","integral of zero",Rule.EASY);
	static Rule oneCase = new Rule("integrate(1,x)=x","integral of one",Rule.EASY);
	static Rule varCase = new Rule("integrate(x,x)=x^2/2","integral of variable",Rule.EASY);
	static Rule invRule = new Rule("integral of the inverse",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
					new Rule("integrate(1/x,x)=ln(x)","integral of inverse",Rule.EASY),
					new Rule("integrate(1/(b*x),x)=ln(b*x)/b","integral of inverse",Rule.EASY),
					new Rule("integrate(1/(x+a),x)=ln(x+a)","integral of inverse",Rule.EASY),
					new Rule("integrate(1/(a+b*x),x)=ln(a+b*x)/b","integral of inverse",Rule.EASY),
			};
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, settings);
			}
			return e;
		}
	};
	static Rule logCase = new Rule("integrate(ln(x),x)=ln(x)*x-x","integral of the log",Rule.UNCOMMON);
	
	static Rule cosCase = new Rule("integrate(cos(x),x)=sin(x)","integral of the cosine",Rule.EASY);
	static Rule cosPowerCase = new Rule("integrate(cos(x)^n,x)=cos(x)^(n-1)*sin(x)/n+(n-1)/n*integrate(cos(x)^(n-2),x)","eval(n>1)","integral of cos to the n",Rule.DIFFICULT);
	static Rule cosInvPowerCase = new Rule("integrate(1/cos(x)^n,x)=cos(x)^(1-n)*sin(x)/(n-1)+(n-2)*integrate(1/cos(x)^(n-2),x)/(n-1)","eval(n>1)","integral of 1 over cos to the n",Rule.DIFFICULT);
	
	static Rule sinCase = new Rule("integrate(sin(x),x)=-cos(x)","integral of the sin",Rule.EASY);
	static Rule sinPowerCase = new Rule("integrate(sin(x)^n,x)=-sin(x)^(n-1)*cos(x)/n+(n-1)/n*integrate(sin(x)^(n-2),x)","eval(n>1)","integral of sin to the n",Rule.DIFFICULT);
	static Rule sinInvPowerCase = new Rule("integrate(1/sin(x)^n,x)=-sin(x)^(1-n)*cos(x)/(n-1)+(n-2)*integrate(1/sin(x)^(n-2),x)/(n-1)","eval(n>1)","integral of 1 over sin to the n",Rule.DIFFICULT);
	
	static Rule tanCase = new Rule("integrate(tan(x),x)=-ln(cos(x))","integral of tan",Rule.UNCOMMON);
	static Rule tanPowerCase = new Rule("integrate(tan(x)^n,x)=tan(x)^(n-1)/(n-1)-integrate(tan(x)^(n-2),x)","eval(n>1)","integral of tan to the n",Rule.DIFFICULT);
	static Rule tanInvPowerCase = new Rule("integrate(1/tan(x)^n,x)=tan(x)^(1-n)/(1-n)-integrate(1/tan(x)^(n-2),x)","eval(n>1)","integral of 1 over tan to the n",Rule.DIFFICULT);
	
	static Rule secSqr = new Rule("integrate(cos(x)^-2,x)=tan(x)","integral of 1 over cosine squared",Rule.UNCOMMON);
	static Rule atanCase = new Rule("integrate(atan(x),x)=x*atan(x)+ln(x^2+1)/-2","integral of arctan",Rule.UNCOMMON);
	
	static Rule cscCase = new Rule("integrate(1/sin(x),x)=ln(1-cos(x))-ln(sin(x))","integral of 1 over sin",Rule.UNCOMMON);
	static Rule secCase = new Rule("integrate(1/cos(x),x)=ln(1+sin(x))-ln(cos(x))","integral of 1 over cos",Rule.UNCOMMON);
	static Rule cotCase = new Rule("integrate(1/tan(x),x)=ln(sin(x))","integral of 1 over tan",Rule.UNCOMMON);
	static Rule arcsinCase = new Rule("integrate(asin(x),x)=x*asin(x)+sqrt(1-x^2)","integral of arcsin",Rule.UNCOMMON);
	
	static Rule loopingIntegrals = new Rule("looping integrals",Rule.UNCOMMON) {
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
					new Rule("integrate(sin(a*x)*b^(c*x),x)=c*ln(b)*sin(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)-a*cos(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)","integral of looping sine",Rule.UNCOMMON),
					new Rule("integrate(sin(a*x)*b^x,x)=ln(b)*sin(a*x)*b^x/(a^2+ln(b)^2)-a*cos(a*x)*b^x/(a^2+ln(b)^2)","integral of looping sine",Rule.UNCOMMON),
					new Rule("integrate(sin(x)*b^(c*x),x)=c*ln(b)*sin(x)*b^(c*x)/(1+c^2*ln(b)^2)-cos(x)*b^(c*x)/(1+c^2*ln(b)^2)","integral of looping sine",Rule.UNCOMMON),
					new Rule("integrate(sin(x)*b^x,x)=ln(b)*sin(x)*b^x/(1+ln(b)^2)-cos(x)*b^x/(1+ln(b)^2)","integral of looping sine",Rule.UNCOMMON),
					
					new Rule("integrate(sin(x+k)*b^x,x)=ln(b)*sin(x+k)*b^x/(1+ln(b)^2)-cos(x+k)*b^x/(1+ln(b)^2)","integral of looping sine",Rule.UNCOMMON),
					new Rule("integrate(sin(a*x+k)*b^x,x)=ln(b)*sin(a*x+k)*b^x/(a^2+ln(b)^2)-a*cos(a*x+k)*b^x/(a^2+ln(b)^2)","integral of looping sine",Rule.UNCOMMON),
					
					
					new Rule("integrate(cos(a*x)*b^(c*x),x)=a*sin(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)+c*ln(b)*cos(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)","integral of looping cosine",Rule.UNCOMMON),
					new Rule("integrate(cos(a*x)*b^x,x)=a*sin(a*x)*b^x/(a^2+ln(b)^2)+ln(b)*cos(a*x)*b^x/(a^2+ln(b)^2)","integral of looping cosine",Rule.UNCOMMON),
					new Rule("integrate(cos(x)*b^(c*x),x)=sin(x)*b^(c*x)/(1+c^2*ln(b)^2)+c*ln(b)*cos(x)*b^(c*x)/(1+c^2*ln(b)^2)","integral of looping cosine",Rule.UNCOMMON),
					new Rule("integrate(cos(x)*b^x,x)=sin(x)*b^x/(1+ln(b)^2)+ln(b)*cos(x)*b^x/(1+ln(b)^2)","integral of looping cosine",Rule.UNCOMMON),
					
					new Rule("integrate(cos(x+k)*b^x,x)=sin(x+k)*b^x/(1+ln(b)^2)+ln(b)*cos(x+k)*b^x/(1+ln(b)^2)","integral of looping cosine",Rule.UNCOMMON),
					new Rule("integrate(cos(a*x+k)*b^x,x)=a*sin(a*x+k)*b^x/(a^2+ln(b)^2)+ln(b)*cos(a*x+k)*b^x/(a^2+ln(b)^2)","integral of looping cosine",Rule.UNCOMMON),
			};
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, settings);
			}
			return e;
		}
	};
	
	static Rule recursivePowerOverSqrt = new Rule("power over sqrt",Rule.UNCOMMON){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
					new Rule("integrate(x^n/sqrt(a*x+b),x)=(2*x^n*sqrt(a*x+b))/(a*(2*n+1))-(2*n*b*integrate(x^(n-1)/sqrt(a*x+b),x))/(a*(2*n+1))","power over sqrt",Rule.UNCOMMON),
					new Rule("integrate(x^n/sqrt(x+b),x)=(2*x^n*sqrt(x+b))/(2*n+1)-(2*n*b*integrate(x^(n-1)/sqrt(x+b),x))/(2*n+1)","power over sqrt",Rule.UNCOMMON),
					new Rule("integrate(x/sqrt(a*x+b),x)=(2*x*sqrt(a*x+b))/(a*3)-(2*b*integrate(1/sqrt(a*x+b),x))/(a*3)","power over sqrt",Rule.UNCOMMON),
					new Rule("integrate(x/sqrt(x+b),x)=(2*x*sqrt(x+b))/3-(2*b*integrate(1/sqrt(x+b),x))/3","power over sqrt",Rule.UNCOMMON),
			};
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, settings);
			}
			return e;
		}
	};
	
	static Rule recursiveInvPowerOverSqrt = new Rule("1 over power times sqrt",Rule.UNCOMMON){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
					new Rule("integrate(1/(sqrt(a*x+b)*x^n),x)=(-sqrt(a*x+b))/((n-1)*b*x^(n-1))-(a*(2*n-3)*integrate(1/(sqrt(a*x+b)*x^(n-1)),x))/(2*b*(n-1))","power over sqrt",Rule.UNCOMMON),
					new Rule("integrate(1/(sqrt(a*x+b)*x),x)=ln(1-sqrt(a*x+b)/sqrt(b))/sqrt(b)-ln(1+sqrt(a*x+b)/sqrt(b))/sqrt(b)","power over sqrt",Rule.UNCOMMON),
					new Rule("integrate(1/(sqrt(x+b)*x^n),x)=(-sqrt(x+b))/((n-1)*b*x^(n-1))-((2*n-3)*integrate(1/(sqrt(x+b)*x^(n-1)),x))/(2*b*(n-1))","power over sqrt",Rule.UNCOMMON),
					new Rule("integrate(1/(sqrt(x+b)*x),x)=ln(1-sqrt(x+b)/sqrt(b))/sqrt(b)-ln(1+sqrt(x+b)/sqrt(b))/sqrt(b)","power over sqrt",Rule.UNCOMMON),
			};
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, settings);
			}
			return e;
		}
	};
	
	static Rule integralsWithPowers = new Rule("integral with powers",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		Rule powerRule;
		Rule inversePowerRule;
		Rule exponentRule;
		
		@Override
		public void init(){
			powerRule = new Rule("integrate(x^n,x)=x^(n+1)/(n+1)","integral of polynomial",Rule.EASY);
			inversePowerRule = new Rule("integrate(1/x^n,x)=-1/(x^(n-1)*(n-1))","integral of 1 over a polynomial",Rule.EASY);
			exponentRule = new Rule("integrate(n^x,x)=n^x/ln(n)","integral of exponential",Rule.UNCOMMON);
			
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			
			Integrate integ = (Integrate)e;
			Var v = integ.getVar();
			
			if(integ.get() instanceof Power){
				Power inner = (Power)integ.get();
				
				boolean baseHasVar = inner.getBase().contains(v),expoHasVar = inner.getExpo().contains(v);
				
				if(baseHasVar && !expoHasVar){
					return powerRule.applyRuleToExpr(integ, settings);
				}else if(!baseHasVar && expoHasVar){
					return exponentRule.applyRuleToExpr(integ, settings);
				}
				
			}
			if(integ.get() instanceof Div){
				Div innerDiv = (Div)integ.get();
				
				if(innerDiv.getDenom() instanceof Power){
					
					Power inner = (Power)innerDiv.getDenom();
					
					boolean baseHasVar = inner.getBase().contains(v),expoHasVar = inner.getExpo().contains(v);
					
					if(baseHasVar && !expoHasVar){
						return inversePowerRule.applyRuleToExpr(integ, settings);
					}
					
				}
				
			}
			
			return integ;
		}
	};
	
	static Rule inverseQuadratic = new Rule("inverse quadratic",Rule.TRICKY){
		private static final long serialVersionUID = 1L;
		
		Expr arctanCase;
		Expr check;
		Expr logCase;
		
		@Override
		public void init(){
			check = createExpr("4*0a*0c-0b^2");
			arctanCase = createExpr("(2*atan((2*0a*x+0b)/sqrt(0k)))/sqrt(0k)");//0k is defined as 4*0a*0c-0b^2
			logCase = createExpr("ln(sqrt(0b^2-4*0a*0c)*(0b+2*0a*x)-0b^2+4*0a*0c)/sqrt(0b^2-4*0a*0c)-ln(sqrt(0b^2-4*0a*0c)*(0b+2*0a*x)+0b^2-4*0a*0c)/sqrt(0b^2-4*0a*0c)");
		}

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			
			Integrate integ = (Integrate)e;
			
			if(integ.get() instanceof Div && !((Div)integ.get()).getNumer().contains(integ.getVar())) {
				Expr denom = ((Div)integ.get()).getDenom();
				ExprList poly = polyExtract(denom, integ.getVar(), settings);
				if(poly != null && poly.size() == 3) {
					Expr c = poly.get(0),b = poly.get(1),a = poly.get(2);
					ExprList subTable = exprList(equ(var("0a"),a),equ(var("0b"),b),equ(var("0c"),c),equ(var("x"),integ.getVar()));
					
					Expr check = this.check.replace(subTable).simplify(settings);
					
					subTable.add(equ(var("0k"),check));
					if(!check.negative()) {//check is negative if the quadratic contains no roots thus using arctan
						return this.arctanCase.replace(subTable).simplify(settings);
					}
					//otherwise use logarithms
					return this.logCase.replace(subTable).simplify(settings);
				}
			}
			return integ;
			
		}
		
	};
	
	static Rule inverseQuadraticSimple = new Rule("integrate(x^a/(x^b+c),x)=atan(x^(a+1)/sqrt(c))/((a+1)*sqrt(c))","eval(b/(a+1)=2)&eval(c>0)","inverse quadratic with u sub",Rule.UNCOMMON);
	static Rule inverseQuadraticSimple2 = new Rule("integrate(x^a/(d*x^b+c),x)=atan((x^(a+1)*sqrt(d))/sqrt(c))/((a+1)*sqrt(d*c))","eval(b/(a+1)=2)&eval(c*d>0)","inverse quadratic with u sub",Rule.UNCOMMON);
	
	static Rule integralForArcsin = new Rule("integrals leading to arcsin",Rule.TRICKY){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		
		@Override
		public void init(){
			cases = new Rule[]{
					new Rule("integrate(1/sqrt(a-x^2),x)=asin(x/sqrt(a))","simple integral leading to arcsin",Rule.EASY),
					new Rule("integrate(1/sqrt(a+b*x^2),x)=asin((sqrt(-b)*x)/sqrt(a))/sqrt(-b)","eval(b<0)","simple integral leading to arcsin",Rule.EASY),
			};
			
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, settings);
			}
			return e;
		}
	};
	
	static Rule integrationByParts = new Rule("integration by parts",Rule.CHALLENGING){
		private static final long serialVersionUID = 1L;
		final int OKAY = 0,GOOD = 1,GREAT = 2,BEST = 3;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Integrate integ = (Integrate)e;
			if(!integ.get().containsType(Integrate.class) && !isPolynomialUnstrict(integ.get(),integ.getVar())) {
				Div innerDiv = Div.cast(integ.get().copy());
				Prod innerProd = Prod.cast(innerDiv.getNumer());
				innerDiv.setNumer(innerProd);
				boolean denomIsFunc = innerDiv.getDenom().contains(integ.getVar());
				int bestIndex = -1;
				int confidence = -1;
				
				int fractionalPowerCount = 0;
				
				for(int i = 0;i < innerProd.size();i++) {
					int currentConfidence = -1;
					
					if(innerProd.get(i) instanceof Ln){
						currentConfidence = BEST;
					}else if(innerProd.get(i) instanceof Atan || innerProd.get(i) instanceof Asin || innerProd.get(i) instanceof Acos){
						currentConfidence = GREAT;
					}else if(!denomIsFunc){
						Power pow = Power.cast(innerProd.get(i));
						if(isPlainPolynomial(pow.getBase(),integ.getVar()) && pow.getBase().contains(integ.getVar())) {
							if(isPositiveRealNum(pow.getExpo())){
								currentConfidence = GOOD;
							}else{
								//System.out.println(pow);
								Div frac = Div.cast(pow.getExpo());
								if(frac!=null && frac.isNumericalAndReal()) {
									if(!degree(pow.getBase(),integ.getVar()).equals(BigInteger.ONE)) return integ;//dangerous because square root does not have linear term
									if(((Num)frac.getNumer()).realValue.signum() == 1) {
										fractionalPowerCount++;
										confidence = OKAY;
									}
								}
								
							}
						}
					}
					
					if(fractionalPowerCount>1) return integ;//dangerous because integrating more than one square root function is hard
					
					if(currentConfidence>confidence){
						confidence = currentConfidence;
						bestIndex = i;
						continue;
					}
				}
				if(bestIndex != -1) {
					Expr best = innerProd.get(bestIndex);
					innerProd.remove(bestIndex);
					Expr newIntegral = integrate(innerDiv,integ.getVar()).simplify(settings);
					//newIntegral.println();
					if(!newIntegral.containsType(Integrate.class)) {
						Expr out = sub(prod(newIntegral,best),integrate(prod(newIntegral.copy(),diff(best.copy(),integ.getVar())),integ.getVar()));
						return out.simplify(settings);
					}
				}
				
			}
			return integ;
		}
	};
	
	static Rule integrationByPartsSpecial = new Rule("special integration by parts",Rule.CHALLENGING){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Integrate integ = (Integrate)e;
			if(integ.get() instanceof Div && !integ.get().containsType(Integrate.class)) {
				Div innerDiv = (Div)integ.get().copy();
				if(innerDiv.getDenom() instanceof Power) {
					Power denomPower = (Power)innerDiv.getDenom();
					Div expo = Div.cast(denomPower.getExpo());
					if(expo.isNumericalAndReal() && isPlainPolynomial(denomPower.getBase(),integ.getVar())) {
						if( ((Num)expo.getNumer()).realValue.compareTo( ((Num)expo.getDenom()).realValue )  == 1) {//make sure the fraction is greater than 1
							Expr integralOfDenom = integrate(inv(denomPower),integ.getVar()).simplify(settings);
							Expr derivativeOfNumer = diff(innerDiv.getNumer(),integ.getVar()).simplify(settings);
							if(!(derivativeOfNumer instanceof Div && ((Div)derivativeOfNumer).getDenom().contains(integ.getVar())  )) {
								Expr out = sub(prod(innerDiv.getNumer(),integralOfDenom),integrate( prod(derivativeOfNumer,integralOfDenom.copy()) ,integ.getVar()));
								return out.simplify(settings);
							}
						}
					}
				}
			}
			return integ;
		}
	};
	
	static Rule specialUSub = new Rule("special u sub, f(x)*diff(f(x),x)",Rule.TRICKY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Integrate integ = (Integrate)e;
				
				
			Div innerDiv = Div.cast(integ.get().copy());
			Prod innerProd = Prod.cast(innerDiv.getNumer());
			innerDiv.setNumer(innerProd);
			for(int i = 0;i<innerProd.size();i++) {
				Div divCopy = (Div)innerDiv.copy();
				
				Expr testExpr = divCopy.getNumer().get(i);
				if(!testExpr.contains(integ.getVar())) continue;
				
				divCopy.getNumer().remove(i);
				Expr resToCheck = div(divCopy,diff(testExpr,(Var)integ.getVar().copy()));
				resToCheck = resToCheck.simplify(settings);
				if(!resToCheck.contains(integ.getVar())) {
					Div res = div(prod(pow(testExpr,num(2)),resToCheck),num(2));
					return res.simplify(settings);
				}
				
			}
			
			
			return integ;
		}
	};
	
	static Rule normalUSub = new Rule("normal u sub",Rule.TRICKY){
		private static final long serialVersionUID = 1L;
		
		public Expr getNextInnerFunction(Expr e,Var v) {
			if(e.size()>0 && e.contains(v)){
				Expr highest = null;
				long highestComplexity = 0;
				for(int i = 0;i<e.size();i++) {
					long current = e.get(i).complexity();
					if(current>highestComplexity && e.get(i).contains(v)) {
						highestComplexity = current;
						highest = e.get(i);
					}
				}
				return highest;
			}
			return e;
		}
		
		Var uSubVar;
		
		@Override
		public void init(){
			uSubVar = var("0u");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Integrate integ = (Integrate)e;
			
			if(integ.contains(uSubVar) || integ.get().containsType(Integrate.class) || isPolynomialUnstrict(integ.get(),integ.getVar())) return integ;
			Expr u = null;
			if(integ.get() instanceof Prod) {
				Prod innerProd = (Prod)integ.get();
				long highestComplexity = 0;
				int indexOfHighestComplexity = 0;
				for(int i = 0;i<innerProd.size();i++) {
					if(innerProd.get(i) instanceof Sum) continue;//skip sums because thats usually du
					long current = innerProd.get(i).complexity();
					if(current > highestComplexity) {
						highestComplexity = current;
						indexOfHighestComplexity = i;
					}
				}
				u = innerProd.get(indexOfHighestComplexity);
			}else if(integ.get() instanceof Div) {
				Div casted =  ((Div)integ.get());
				boolean logCase = !div(casted.getNumer(),diff(casted.getDenom() ,integ.getVar())).simplify(settings).contains(integ.getVar());
				
				if(logCase) {
					u = casted.getDenom();
				}else if(!casted.getNumer().contains(integ.getVar())) {
					u = casted.getDenom();
				}else if(!casted.getDenom().contains(integ.getVar())){
					u = casted.getNumer();
				}else {
					u = casted.getNumer().complexity() > casted.getDenom().complexity() ? casted.getNumer() : casted.getDenom();
				}
			}else {
				u = integ.get();
			}
			
			if(u != null) {
				while(true) {//try normal u and innermost u sub
					if(!u.equals(integ.getVar())) {
						Equ eq = equ(u,uSubVar);//im calling it 0u since variables normally can't start with number
						Expr diffObj = diff(u,(Var)integ.getVar().copy()).simplify(settings);
						diffObj = diffObj.replace(eq);//it is possible for derivative to contain u
						Expr before = div(integ.get().replace(eq),diffObj);
						Expr newExpr = before.simplify(settings);
						if(!newExpr.contains(integ.getVar())) {//no solve needed
							newExpr = integrate(newExpr,uSubVar).simplify(settings);
							if(!newExpr.containsType(Integrate.class)) {
								Expr out = newExpr.replace(equ(uSubVar,u));
								return out.simplify(settings);
							}
						}else {//oof we need to solve for x
							Expr solved = solve(equ(uSubVar,u),integ.getVar()).simplify(settings);
							if(solved instanceof ExprList) solved = solved.get();
							if(!(solved instanceof Solve)) {
								solved = ((Equ)solved).getRightSide();
								newExpr = integrate(newExpr.replace(equ(integ.getVar(),solved)),uSubVar);
								newExpr = newExpr.simplify(settings);
								if(!newExpr.containsType(Integrate.class)) {
									Expr out = newExpr.replace(equ(uSubVar,u)).simplify(settings);
									return out;
								}
								
							}
						}
						
					}
					Expr newU = getNextInnerFunction(u,integ.getVar());//we slowly work are way in
					if(newU.equals(u)) break;
					u = newU;
				}
			}
			
			return integ;
		}
		
	};
	static Rule partialFraction = new Rule("partial fractions",Rule.DIFFICULT){
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Integrate integ = (Integrate)e;
			
			integ.set(0, partialFrac(integ.get(), integ.getVar(), settings) );
			if(integ.get() instanceof Sum){
				Expr out = StandardRules.linearOperator.applyRuleToExpr(integ, settings);
				return out;
			}
			return integ;
		}
	
	};
	
	static Rule polyDiv = new Rule("polynomial division",Rule.CHALLENGING){
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Integrate integ = (Integrate)e;
			
			integ.set(0, polyDiv(integ.get(), integ.getVar(), settings) );
			if(integ.get() instanceof Sum){
				return StandardRules.linearOperator.applyRuleToExpr(integ, settings);
			}
			
			return integ;
		}
	
	};
	
	static Rule psudoTrigSub = new Rule("psudo trig substitution",Rule.DIFFICULT) {
		private static final long serialVersionUID = 1L;

		Rule[] cases;
		
		@Override
		public void init(){
			cases = new Rule[]{
					new Rule("integrate(x^2/sqrt(a-x^2),x)=a*asin(x/sqrt(a))/2-x*sqrt(a-x^2)/2","trig sub",Rule.DIFFICULT),
					new Rule("integrate(x^2/sqrt(a+b*x^2),x)=a*asin((x*sqrt(-b))/sqrt(a))/(2*(-b)^(3/2))+x*sqrt(a-(-b)*x^2)/(2*b)","eval(b<0)","trig sub",Rule.VERY_DIFFICULT),
					new Rule("integrate(sqrt(a-x^2),x)=a*asin(x/sqrt(a))/2+x*sqrt(a-x^2)/2","trig sub",Rule.VERY_DIFFICULT),
					new Rule("integrate(sqrt(a+b*x^2),x)=a*asin(x*sqrt(-b)/sqrt(a))/(2*sqrt(-b))+x*sqrt(a+b*x^2)/2","eval(b<0)","trig sub",Rule.VERY_DIFFICULT),
			};
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, settings);
			}
			
			return e;
		}
	};
	
	/*
	 * this rule makes integration more reliable for both other rules and for u substitutions
	 * for example with an example equation of x^3/sqrt(1-x^2) we do not want the sqrt(1-x^2) to be in the form of sqrt(1-x)*sqrt(1+x)
	 * while normally the ladder would be better for integration it creates more problems
	 */
	static Rule compressRoots = new Rule("compress roots",Rule.EASY) {//sqrt(a+b)*sqrt(a-b) -> sqrt(a^2-b^2)
		private static final long serialVersionUID = 1L;
		
		public Expr compressRoots(Expr e,Settings settings) {
			if(e instanceof Prod) {
				for(int i = 0;i<e.size();i++) {
					if(e.get(i) instanceof Power) {
						Power current = (Power)e.get(i);
						if(!(current.getExpo() instanceof Div)) continue;
						Div frac = (Div)current.getExpo();
						if(!frac.isNumericalAndReal()) continue;
						
						current.setBase(Prod.cast(current.getBase()));
						
						for(int j = i+1;j<e.size();j++) {
							if(e.get(j) instanceof Power) {
								Power otherPow = (Power)e.get(j);
								if(otherPow.getExpo().equals(current.getExpo())) {
									current.getBase().add(otherPow.getBase());
									e.remove(j);
									j--;
								}
								
							}
							
						}
						current.setBase(distr(current.getBase()).simplify(settings));
					}
				}
				e = Prod.unCast(e);
			}else {
				for(int i = 0;i<e.size();i++) {
					e.set(i,compressRoots(e.get(i),settings));
				}
			}
			return e;
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			e = compressRoots(e,settings);
			return e;
		}
		
	};
	
	static Rule fullExpandInner = new Rule("full expansion",Rule.TRICKY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Integrate integ = (Integrate)e;
			integ.set(0, SimpleFuncs.fullExpand.applyRuleToExpr(integ.get(), settings) );
			return integ;
		}
		
	};
	
	static Rule sinCosProdCase = new Rule("product of sin and cos powers",Rule.UNCOMMON) {
		private static final long serialVersionUID = 1L;
		
		Expr sinPowTemplate,cosPowTemplate;
		
		@Override
		public void init() {
			sinPowTemplate = createExpr("sin(x)^n");
			cosPowTemplate = createExpr("cos(x)^n");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Integrate integ = (Integrate)e;
			
			if(integ.get() instanceof Prod && integ.get().size() == 2) {
				Prod innerProd = (Prod)integ.get();
				Power sinPow = null;
				Power cosPow = null;
				//extraction
				for(int i = 0;i<2;i++) {
					if(Rule.fastSimilarStruct(sinPowTemplate, innerProd.get(i))) {
						sinPow = (Power)innerProd.get(i);
					}else if(Rule.fastSimilarStruct(cosPowTemplate, innerProd.get(i))) {
						cosPow = (Power)innerProd.get(i);
					}
				}
				if(sinPow == null || cosPow == null) return integ;
				boolean check = isPositiveRealNum(cosPow.getExpo()) && isPositiveRealNum(sinPow.getExpo()) && sinPow.getBase().get().equals(cosPow.getBase().get());
				if(!check) return integ;
				
				ExprList polyCoef = polyExtract(sinPow.getBase().get(),integ.getVar(),settings);
				if(polyCoef == null) return integ;
				Expr coef = null;
				if(polyCoef.size() == 2) {
					coef = polyCoef.get(1);
				}
				if(coef == null) return integ;
				
				Num m = num(((Num)cosPow.getExpo()).realValue.shiftRight(1));
				Num n = (Num)sinPow.getExpo();
				Expr needsExpand = prod(  pow(sub(num(1),pow(var("0u"),num(2))),m),pow(var("0u"),n)  );
				Expr expanded = SimpleFuncs.fullExpand.applyRuleToExpr(needsExpand, settings);
				if(!isPlainPolynomial(expanded, var("0u"))) return integ;
				
				Expr out = null;
				if(((Num)cosPow.getExpo()).realValue.mod(BigInteger.TWO).equals(BigInteger.ONE)) {//odd case
					//cos(x)^(2*m+1)*sin(x)^n = (1-sin(x)^2)^m*sin(x)^n*cos(x)
					//u=sin(x)
					//integrate(cos(x)^(2*m+1)*sin(x)^n,x) = integrate((1-u^2)^m*u^n,u)
					out = div(integrate(expanded,var("0u")).simplify(settings).replace(equ( var("0u") , sinPow.getBase() )),coef);
				}else {//even case
					//cos(x)^(2*m)*sin(x)^n = (1-sin(x)^2)^m*sin(x)^n
					//u=sin(x)
					//(1-u^2)*u^n
					out = div(integrate(expanded.replace(equ( var("0u") , sinPow.getBase() )),integ.getVar()).simplify(settings),coef);
				}
				return out.simplify(settings);
				
			}
			
			return integ;
		}
	};
	
	static ExprList ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = exprList(
				
				zeroCase,
				oneCase,
				StandardRules.pullOutConstants,
				varCase,
				invRule,
				integralsWithPowers,
				inverseQuadratic,
				inverseQuadraticSimple,
				inverseQuadraticSimple2,
				logCase,
				
				cosCase,
				cosPowerCase,
				cosInvPowerCase,
				
				sinCase,
				sinPowerCase,
				sinInvPowerCase,
				
				tanCase,
				tanPowerCase,
				tanInvPowerCase,
				
				sinCosProdCase,
				
				secSqr,
				atanCase,
				
				loopingIntegrals,
				
				cscCase,
				secCase,
				cotCase,
				arcsinCase,
				compressRoots,
				recursivePowerOverSqrt,
				recursiveInvPowerOverSqrt,
				psudoTrigSub,
				integralForArcsin,
				
				partialFraction,
				polyDiv,
				StandardRules.distrInner,
				specialUSub,
				integrationByParts,
				compressRoots,
				normalUSub,
				integrationByPartsSpecial,
				fullExpandInner,
				StandardRules.linearOperator
				
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public ExprList getRuleSequence(){
		return ruleSequence;
	}
	
	Integrate(){}//
	public Integrate(Expr e,Var v){
		add(e);
		add(v);
	}
	
	@Override
	public Var getVar() {
		return (Var)get(1);
	}
	
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return integrateOver(num(0),getVar(),get(),getVar()).convertToFloat(varDefs);
	}

}
