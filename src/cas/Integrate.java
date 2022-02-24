package cas;

import java.math.BigInteger;

public class Integrate extends Expr{

	private static final long serialVersionUID = 5071855237530369367L;
	
	static Rule zeroCase = new Rule("integrate(0,x)->0","integral of zero",Rule.EASY);
	static Rule oneCase = new Rule("integrate(1,x)->x","integral of one",Rule.EASY);
	static Rule varCase = new Rule("integrate(x,x)->x^2/2","integral of variable",Rule.EASY);
	static Rule invRule = new Rule("integral of the inverse",Rule.EASY){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
					new Rule("integrate(1/x,x)->ln(x)","integral of inverse",Rule.EASY),
					new Rule("integrate(1/(b*x),x)->ln(b*x)/b","~contains(b,x)","integral of inverse",Rule.EASY),
					new Rule("integrate(1/(x+a),x)->ln(x+a)","~contains(a,x)","integral of inverse",Rule.EASY),
					new Rule("integrate(1/(a+b*x),x)->ln(a+b*x)/b","~contains({a,b},x)","integral of inverse",Rule.EASY),
			};
			Rule.initRules(cases);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, settings);
			}
			return e;
		}
	};
	static Rule logCase = new Rule("integrate(ln(x),x)->ln(x)*x-x","integral of the log",Rule.UNCOMMON);
	
	static Rule absCase = new Rule("integrate(abs(x),x)->x*abs(x)/2","integral of the absolute value",Rule.UNCOMMON);
	
	static Rule cosCase = new Rule("integrate(cos(x),x)->sin(x)","integral of the cosine",Rule.EASY);
	static Rule cosPowerCase = new Rule("integrate(cos(x)^n,x)->cos(x)^(n-1)*sin(x)/n+(n-1)/n*integrate(cos(x)^(n-2),x)","eval(n>1)&~contains(n,x)","integral of cos to the n",Rule.DIFFICULT);
	static Rule cosInvPowerCase = new Rule("integrate(1/cos(x)^n,x)->cos(x)^(1-n)*sin(x)/(n-1)+(n-2)*integrate(1/cos(x)^(n-2),x)/(n-1)","eval(n>1)&~contains(n,x)","integral of 1 over cos to the n",Rule.DIFFICULT);
	
	static Rule sinCase = new Rule("integrate(sin(x),x)->-cos(x)","integral of the sin",Rule.EASY);
	static Rule sinPowerCase = new Rule("integrate(sin(x)^n,x)->-sin(x)^(n-1)*cos(x)/n+(n-1)/n*integrate(sin(x)^(n-2),x)","eval(n>1)&~contains(n,x)","integral of sin to the n",Rule.DIFFICULT);
	static Rule sinInvPowerCase = new Rule("integrate(1/sin(x)^n,x)->-sin(x)^(1-n)*cos(x)/(n-1)+(n-2)*integrate(1/sin(x)^(n-2),x)/(n-1)","eval(n>1)&~contains(n,x)","integral of 1 over sin to the n",Rule.DIFFICULT);
	
	static Rule tanCase = new Rule("integrate(tan(x),x)->-ln(cos(x))","integral of tan",Rule.UNCOMMON);
	static Rule tanPowerCase = new Rule("integrate(tan(x)^n,x)->tan(x)^(n-1)/(n-1)-integrate(tan(x)^(n-2),x)","eval(n>1)&~contains(n,x)","integral of tan to the n",Rule.DIFFICULT);
	static Rule tanInvPowerCase = new Rule("integrate(1/tan(x)^n,x)->tan(x)^(1-n)/(1-n)-integrate(1/tan(x)^(n-2),x)","eval(n>1)&~contains(n,x)","integral of 1 over tan to the n",Rule.DIFFICULT);
	
	static Rule atanCase = new Rule("integrate(atan(x),x)->x*atan(x)+ln(x^2+1)/-2","integral of arctan",Rule.UNCOMMON);
	
	static Rule cscCase = new Rule("integrate(1/sin(x),x)->ln(1-cos(x))-ln(sin(x))","integral of 1 over sin",Rule.UNCOMMON);
	static Rule secCase = new Rule("integrate(1/cos(x),x)->ln(1+sin(x))-ln(cos(x))","integral of 1 over cos",Rule.UNCOMMON);
	static Rule cotCase = new Rule("integrate(1/tan(x),x)->ln(sin(x))","integral of 1 over tan",Rule.UNCOMMON);
	
	static Rule cotCscCase = new Rule("integrate(1/(tan(x)*sin(x)),x)->-1/sin(x)","integral of 1 over tan times sin",Rule.UNCOMMON);
	
	static Rule arcsinCase = new Rule("integrate(asin(x),x)->x*asin(x)+sqrt(1-x^2)","integral of arcsin",Rule.UNCOMMON);
	
	static Rule sinCosProdCase = new Rule("integrate(sin(a)*cos(b),x)->integrate(sin(a+b),x)/2+integrate(sin(a-b),x)/2","integral of sin cos product",Rule.UNCOMMON);
	static Rule sinSinProdCase = new Rule("integrate(sin(a)*sin(b),x)->integrate(cos(a-b),x)/2-integrate(cos(a+b),x)/2","integral of sin cos product",Rule.UNCOMMON);
	static Rule cosCosProdCase = new Rule("integrate(cos(a)*cos(b),x)->integrate(cos(a+b),x)/2+integrate(cos(a-b),x)/2","integral of sin cos product",Rule.UNCOMMON);
	
	static Rule loopingIntegrals = new Rule("looping integrals",Rule.UNCOMMON) {
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		@Override
		public void init(){
			cases = new Rule[]{
					new Rule("integrate(sin(a*x)*b^(c*x),x)->c*ln(b)*sin(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)-a*cos(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)","~contains({a,b,c},x)","integral of looping sine",Rule.UNCOMMON),
					new Rule("integrate(sin(a*x)*b^x,x)->ln(b)*sin(a*x)*b^x/(a^2+ln(b)^2)-a*cos(a*x)*b^x/(a^2+ln(b)^2)","~contains({a,b},x)","integral of looping sine",Rule.UNCOMMON),
					new Rule("integrate(sin(x)*b^(c*x),x)->c*ln(b)*sin(x)*b^(c*x)/(1+c^2*ln(b)^2)-cos(x)*b^(c*x)/(1+c^2*ln(b)^2)","~contains({b,c},x)","integral of looping sine",Rule.UNCOMMON),
					new Rule("integrate(sin(x)*b^x,x)->ln(b)*sin(x)*b^x/(1+ln(b)^2)-cos(x)*b^x/(1+ln(b)^2)","~contains(b,x)","integral of looping sine",Rule.UNCOMMON),
					
					new Rule("integrate(sin(x+k)*b^x,x)->ln(b)*sin(x+k)*b^x/(1+ln(b)^2)-cos(x+k)*b^x/(1+ln(b)^2)","~contains({k,b},x)","integral of looping sine",Rule.UNCOMMON),
					new Rule("integrate(sin(a*x+k)*b^x,x)->ln(b)*sin(a*x+k)*b^x/(a^2+ln(b)^2)-a*cos(a*x+k)*b^x/(a^2+ln(b)^2)","~contains({a,k,b},x)","integral of looping sine",Rule.UNCOMMON),
					
					
					new Rule("integrate(cos(a*x)*b^(c*x),x)->a*sin(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)+c*ln(b)*cos(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)","~contains({a,b,c},x)","integral of looping cosine",Rule.UNCOMMON),
					new Rule("integrate(cos(a*x)*b^x,x)->a*sin(a*x)*b^x/(a^2+ln(b)^2)+ln(b)*cos(a*x)*b^x/(a^2+ln(b)^2)","~contains({a,b},x)","integral of looping cosine",Rule.UNCOMMON),
					new Rule("integrate(cos(x)*b^(c*x),x)->sin(x)*b^(c*x)/(1+c^2*ln(b)^2)+c*ln(b)*cos(x)*b^(c*x)/(1+c^2*ln(b)^2)","~contains({b,c},x)","integral of looping cosine",Rule.UNCOMMON),
					new Rule("integrate(cos(x)*b^x,x)->sin(x)*b^x/(1+ln(b)^2)+ln(b)*cos(x)*b^x/(1+ln(b)^2)","~contains(b,x)","integral of looping cosine",Rule.UNCOMMON),
					
					new Rule("integrate(cos(x+k)*b^x,x)->sin(x+k)*b^x/(1+ln(b)^2)+ln(b)*cos(x+k)*b^x/(1+ln(b)^2)","~contains({k,b},x)","integral of looping cosine",Rule.UNCOMMON),
					new Rule("integrate(cos(a*x+k)*b^x,x)->a*sin(a*x+k)*b^x/(a^2+ln(b)^2)+ln(b)*cos(a*x+k)*b^x/(a^2+ln(b)^2)","~contains({a,k,b},x)","integral of looping cosine",Rule.UNCOMMON),
					
					
					
					new Rule("integrate(x*abs(x),x)->abs(x)*x^2/3","signed quadratic integral",Rule.UNCOMMON),
			};
			Rule.initRules(cases);
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
					new Rule("integrate(x^n/sqrt(a*x+b),x)->(2*x^n*sqrt(a*x+b))/(a*(2*n+1))-(2*n*b*integrate(x^(n-1)/sqrt(a*x+b),x))/(a*(2*n+1))","~contains({n,a,b},x)","power over sqrt",Rule.UNCOMMON),
					new Rule("integrate(x^n/sqrt(x+b),x)->(2*x^n*sqrt(x+b))/(2*n+1)-(2*n*b*integrate(x^(n-1)/sqrt(x+b),x))/(2*n+1)","~contains({n,b},x)","power over sqrt",Rule.UNCOMMON),
					new Rule("integrate(x/sqrt(a*x+b),x)->(2*x*sqrt(a*x+b))/(a*3)-(2*b*integrate(1/sqrt(a*x+b),x))/(a*3)","~contains({a,b},x)","power over sqrt",Rule.UNCOMMON),
					new Rule("integrate(x/sqrt(x+b),x)->(2*x*sqrt(x+b))/3-(2*b*integrate(1/sqrt(x+b),x))/3","~contains(b,x)","power over sqrt",Rule.UNCOMMON),
			};
			Rule.initRules(cases);
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
					new Rule("integrate(1/(sqrt(a*x+b)*x^n),x)->(-sqrt(a*x+b))/((n-1)*b*x^(n-1))-(a*(2*n-3)*integrate(1/(sqrt(a*x+b)*x^(n-1)),x))/(2*b*(n-1))","~contains({a,b,n},x)","power over sqrt",Rule.UNCOMMON),
					new Rule("integrate(1/(sqrt(a*x+b)*x),x)->ln(1-sqrt(a*x+b)/sqrt(b))/sqrt(b)-ln(1+sqrt(a*x+b)/sqrt(b))/sqrt(b)","~contains({a,b},x)","power over sqrt",Rule.UNCOMMON),
					new Rule("integrate(1/(sqrt(x+b)*x^n),x)->(-sqrt(x+b))/((n-1)*b*x^(n-1))-((2*n-3)*integrate(1/(sqrt(x+b)*x^(n-1)),x))/(2*b*(n-1))","~contains({b,n},x)","power over sqrt",Rule.UNCOMMON),
					new Rule("integrate(1/(sqrt(x+b)*x),x)->ln(1-sqrt(x+b)/sqrt(b))/sqrt(b)-ln(1+sqrt(x+b)/sqrt(b))/sqrt(b)","~contains(b,x)","power over sqrt",Rule.UNCOMMON),
			};
			Rule.initRules(cases);
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
		
		Rule powerRule,powerRule2,powerRule3;
		Rule inversePowerRule,inversePowerRule2, inversePowerRule3;
		Rule exponentRule;
		
		@Override
		public void init(){
			powerRule = new Rule("integrate(x^n,x)->x^(n+1)/(n+1)","~contains(n,x)","integral of polynomial",Rule.EASY);
			powerRule.init();
			powerRule2 = new Rule("integrate((x+a)^n,x)->(x+a)^(n+1)/(n+1)","~contains({a,n},x)","integral of polynomial",Rule.EASY);
			powerRule2.init();
			powerRule3 = new Rule("integrate((b*x+a)^n,x)->(b*x+a)^(n+1)/((n+1)*b)","~contains({a,b,n},x)","integral of polynomial",Rule.EASY);
			powerRule3.init();
			
			inversePowerRule = new Rule("integrate(1/x^n,x)->-1/(x^(n-1)*(n-1))","~contains(n,x)","integral of 1 over a polynomial",Rule.EASY);
			inversePowerRule.init();
			inversePowerRule2 = new Rule("integrate(1/(x+a)^n,x)->-1/((x+a)^(n-1)*(n-1))","~contains({a,n},x)","integral of 1 over a polynomial",Rule.EASY);
			inversePowerRule2.init();
			inversePowerRule3 = new Rule("integrate(1/(b*x+a)^n,x)->-1/((b*x+a)^(n-1)*(n-1)*b)","~contains({a,b,n},x)","integral of 1 over a polynomial",Rule.EASY);
			inversePowerRule3.init();
			
			exponentRule = new Rule("integrate(n^x,x)->n^x/ln(n)","~contains(n,x)","integral of exponential",Rule.UNCOMMON);
			exponentRule.init();
			
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			
			Integrate integ = (Integrate)e;
			
			if(integ.get() instanceof Power){
				
				Expr out = integ;
				out = powerRule.applyRuleToExpr(out, settings);
				out = powerRule2.applyRuleToExpr(out, settings);
				out = powerRule3.applyRuleToExpr(out, settings);
				out = exponentRule.applyRuleToExpr(out, settings);
				return out;
				
			}
			if(integ.get() instanceof Div){
				Div innerDiv = (Div)integ.get();
				
				if(innerDiv.getDenom() instanceof Power){
					
					Expr out = integ;
					out = inversePowerRule.applyRuleToExpr(out, settings);
					out = inversePowerRule2.applyRuleToExpr(out, settings);
					out = inversePowerRule3.applyRuleToExpr(out, settings);
					return out;
						
				}
				
			}
			
			return integ;
		}
	};
	
	static Rule inverseQuadratic = new Rule("inverse quadratic",Rule.TRICKY){//robust
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
				Sequence poly = polyExtract(denom, integ.getVar(), settings);
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
	
	/*
	 * these are the reverse process of diff(atan(x^n),x) -> (n-1)*x^(n-1)/(x^(2*n)+1)
	 */
	
	static Rule inverseQuadraticSimple = new Rule("integrate(x^a/(x^b+c),x)->atan(x^(a+1)/sqrt(c))/((a+1)*sqrt(c))","eval(b/(a+1)=2)&eval(c>0)&~contains({a,b,c},x)","inverse quadratic with u sub",Rule.UNCOMMON);
	static Rule inverseQuadraticSimple2 = new Rule("integrate(x^a/(d*x^b+c),x)->atan((x^(a+1)*sqrt(d))/sqrt(c))/((a+1)*sqrt(d*c))","eval(b/(a+1)=2)&eval(c*d>0)&~contains({a,b,c,d},x)","inverse quadratic with u sub",Rule.UNCOMMON);
	
	static Rule inverseQuadraticToNReduction = new Rule("1 over quadratic to the n",Rule.VERY_DIFFICULT) {
		private static final long serialVersionUID = 1L;
		
		Expr ans;
		Expr invPow;
		
		@Override
		public void init() {
			ans = createExpr("(-2*a*x-b)/((n-1)*(b^2-4*a*c)*(a*x^2+b*x+c)^(n-1))-2*(2*n-3)*a/((n-1)*(b^2-4*a*c))*integrate(1/(a*x^2+b*x+c)^(n-1),x)");
			invPow = createExpr("1/q^n");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Integrate integ = (Integrate)e;
			
			if( !Rule.fastSimilarStruct(invPow,integ.get() )) return integ;
			Power denom = (Power)(((Div)integ.get()).getDenom());
			
			if(!isPositiveRealNum(denom.getExpo())) return integ;
			Num n = (Num)denom.getExpo();
			
			Sequence coef = polyExtract(denom.getBase() ,integ.getVar(),settings);
			if(coef == null || coef.size() != 3) return integ;
			ExprList equs = exprList(  equ(var("a"),coef.get(2)) , equ(var("b"),coef.get(1)) , equ(var("c"),coef.get(0)) , equ(var("n"),n) , equ(var("x"),integ.getVar()) );
			
			return ans.replace(equs).simplify(settings);
		}
		
	};
	
	static Rule integralForArcsin = new Rule("integrals leading to arcsin",Rule.TRICKY){
		private static final long serialVersionUID = 1L;
		
		Rule[] cases;
		
		@Override
		public void init(){
			cases = new Rule[]{
					new Rule("integrate(1/sqrt(a-x^2),x)->asin(x/sqrt(a))","~contains(a,x)","simple integral leading to arcsin",Rule.EASY),
					new Rule("integrate(1/sqrt(a+b*x^2),x)->asin((sqrt(-b)*x)/sqrt(a))/sqrt(-b)","eval(b<0)&~contains({a,b},x)","simple integral leading to arcsin",Rule.EASY),
			};
			Rule.initRules(cases);
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
			if(!integ.get().containsType("integrate") && !isPolynomialUnstrict(integ.get(),integ.getVar())) {
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
					if(!newIntegral.containsType("integrate")) {
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
			if(integ.get() instanceof Div && !integ.get().containsType("integrate")) {
				Div innerDiv = (Div)integ.get().copy();
				if(innerDiv.getDenom() instanceof Power) {
					Power denomPower = (Power)innerDiv.getDenom();
					Div expo = Div.cast(denomPower.getExpo());
					if(expo.isNumericalAndReal() && isPlainPolynomial(denomPower.getBase(),integ.getVar()) && degree(denomPower.getBase(),integ.getVar()).equals(BigInteger.ONE) ) {
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
			
			if(integ.contains(uSubVar) || integ.get().containsType("integrate") || isPolynomialUnstrict(integ.get(),integ.getVar())) return integ;
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
						if(diffObj.containsType("diff")) return integ;
						
						diffObj = diffObj.replace(eq);//it is possible for derivative to contain u
						Expr before = div(integ.get().replace(eq),diffObj);
						Expr newExpr = before.simplify(settings);
						if(!newExpr.contains(integ.getVar())) {//no solve needed
							newExpr = integrate(newExpr,uSubVar).simplify(settings);
							if(!newExpr.containsType("integrate")) {
								Expr out = newExpr.replace(equ(uSubVar,u));
								return out.simplify(settings);
							}
						}else {//oof we need to solve for x
							Expr solved = solve(equ(uSubVar,u),integ.getVar()).simplify(settings);
							if(solved instanceof ExprList) {
								solved = solved.get(solved.size()-1);//last element is usually positive
							}
							if(!(solved instanceof Solve)) {
								solved = ((Equ)solved).getRightSide();
								newExpr = integrate(newExpr.replace(equ(integ.getVar(),solved)),uSubVar);
								newExpr = newExpr.simplify(settings);
								if(!newExpr.containsType("integrate")) {
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
					new Rule("integrate(x^2/sqrt(a+b*x^2),x)->a*asin((x*sqrt(-b))/sqrt(a))/(2*(-b)^(3/2))+x*sqrt(a-(-b)*x^2)/(2*b)","eval(b<0)&~contains({a,b},x)","trig sub",Rule.VERY_DIFFICULT),
					
					new Rule("integrate(sqrt(a+b*x^2)/x^2,x)->b*asin(sqrt(-b)*x/sqrt(a))/sqrt(-b)-sqrt(a+b*x^2)/x","eval(b<0)&~contains({a,b},x)","trig sub",Rule.VERY_DIFFICULT),
					
					new Rule("integrate(sqrt(a+b*x^2),x)->a*asin(x*sqrt(-b)/sqrt(a))/(2*sqrt(-b))+x*sqrt(a+b*x^2)/2","eval(b<0)&~contains({a,b},x)","trig sub",Rule.VERY_DIFFICULT),
					
					new Rule("integrate(sqrt(a+b*x^2),x)->x*sqrt(b)*sqrt(a+b*x^2)/(2*sqrt(b))+a*ln(sqrt(a+b*x^2)+x*sqrt(b))/(2*sqrt(b))","eval(b>0)&~contains({a,b},x)","trig sub",Rule.CHALLENGING),
					new Rule("integrate(sqrt(a+x^2),x)->x*sqrt(a+x^2)/2+a*ln(sqrt(a+x^2)+x)/2","eval(b>0)&~contains(a,x)","trig sub",Rule.CHALLENGING),
					
					new Rule("integrate(1/sqrt(a+b*x^2),x)->ln(x*sqrt(b)+sqrt(a+b*x^2))/(2*sqrt(b))-ln(sqrt(a+b*x^2)-x*sqrt(b))/(2*sqrt(b))","eval(a>0)&eval(b>0)&~contains({a,b},x)","trig sub",Rule.CHALLENGING),
					new Rule("integrate(1/sqrt(a+x^2),x)->ln(x+sqrt(a+x^2))/2-ln(sqrt(a+x^2)-x)/2","eval(a>0)&~contains(a,x)","trig sub",Rule.CHALLENGING),
					
					new Rule("integrate(sqrt(x^2+a)/x^4,x)->-(x^2+a)^(3/2)/(3*a*x^3)","~contains(a,x)","trig sub",Rule.VERY_DIFFICULT),
					new Rule("integrate(sqrt(b*x^2+a)/x^4,x)->-(b*x^2+a)^(3/2)/(3*a*x^3)","~contains({a,b},x)","trig sub",Rule.VERY_DIFFICULT),
					
					new Rule("integrate(sqrt(x^2+a)/x^3,x)->-sqrt(x^2+a)/(2*x^2)+atan(sqrt(x^2+a)/sqrt(-a))/(2*sqrt(-a))","eval(a<0)&~contains(a,x)","trig sub",Rule.VERY_DIFFICULT),
					new Rule("integrate(sqrt(b*x^2+a)/x^3,x)->-sqrt(b*x^2+a)/(2*x^2)+atan(sqrt(b*x^2+a)/sqrt(-a))/(2*sqrt(-a))","eval(a<0)&~contains({a,b},x)","trig sub",Rule.VERY_DIFFICULT),
					
			};
			Rule.initRules(cases);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			for(Rule r:cases){
				e = r.applyRuleToExpr(e, settings);
			}
			
			return e;
		}
	};
	
	static Rule sqrtOfQuadratic = new Rule("square root has quadratic",Rule.DIFFICULT) {
		private static final long serialVersionUID = 1L;
		Expr result;
		Expr check;
		
		@Override
		public void init() {
			result = createExpr("(b+2*a*x)*sqrt(a*x^2+b*x+c)/(4*a)+(4*a*c-b^2)*(ln(2*sqrt(a)*sqrt(a*x^2+b*x+c)+2*a*x+b)-ln(2))/(8*a^(3/2))");
			check = createExpr("eval(a>0)");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Integrate integ = (Integrate)e;
			
			if(Rule.fastSimilarStruct(sqrtObj, integ.get())) {
				Sequence coefs = polyExtract(e.get().get(),integ.getVar(),settings);
				
				if(coefs.size() == 3) {
					ExprList equs = exprList( equ(var("c"),coefs.get(0)) , equ(var("b"),coefs.get(1)) , equ(var("a"),coefs.get(2)) );
					if(check.replace(equs).simplify(settings).equals(BoolState.TRUE)) {
						return result.replace(equs).simplify(settings);
					}
				}
				
			}
			
			return integ;
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
	
	static Rule sinCosProdPowersCase = new Rule("product of sin and cos powers",Rule.UNCOMMON) {
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
				
				Sequence polyCoef = polyExtract(sinPow.getBase().get(),integ.getVar(),settings);
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
	
	static Rule weierstrassSub = new Rule("weierstrass substitution",Rule.TRICKY) {
		private static final long serialVersionUID = 1L;
		
		public Expr getSinOrCosInner(Expr e) {//search tree for inside of sin or cos
			if(e instanceof Sin || e instanceof Cos) return e.get();
			for(int i = 0;i<e.size();i++) {
				Expr inner = getSinOrCosInner(e.get(i));
				if(inner != null) return inner;
			}
			return null;
		}
		String[] trigTypes;
		ExprList subs;
		Expr addedDeriv;
		@Override
		public void init() {
			trigTypes = new String[] {"sin","cos","tan","asin","acos","atan"};
			subs = (ExprList)createExpr("[sin(0a)=(2*0t)/(1+0t^2),cos(0a)=(1-0t^2)/(1+0t^2)]");
			addedDeriv = createExpr("2/(0t^2+1)");
		}
		boolean containsTrig(Expr e) {
			for(String type:trigTypes) {
				if(e.containsType(type)) return true;
			}
			return false;
		}
		
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Integrate integ = (Integrate)e;
			if(!( (integ.containsType("sin") || integ.containsType("cos")) && integ.containsType("sum") )) return integ;//needs sin or cos
			
			Expr innerTrig = getSinOrCosInner(integ.get());
			if(innerTrig == null) return integ;
			
			Equ equ = equ(var("0a"),innerTrig);
			ExprList subs = (ExprList) this.subs.replace(equ);
			Expr addedDeriv = this.addedDeriv.replace(equ);
			
			Expr newInner = div(prod(integ.get().replace(subs),addedDeriv),diff(innerTrig,integ.getVar()));
			newInner = newInner.simplify(settings);
			
			if(newInner.contains(integ.getVar()) || containsTrig(newInner)) return integ;
			System.out.println(newInner);
			Expr integRes = integrate(newInner,var("0t")).simplify(settings);
			if(!integRes.containsType("integrate")) {
				Expr out = integRes.replace(equ(var("0t"),tan( div(innerTrig,num(2)) ))).simplify(settings);
				return out;
			}
			
			return integ;
		}
		
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				
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
				absCase,
				
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
				sinSinProdCase,
				cosCosProdCase,
				
				sinCosProdPowersCase,
				
				cotCscCase,
				inverseQuadraticToNReduction,
				atanCase,
				
				loopingIntegrals,
				
				cscCase,
				secCase,
				cotCase,
				
				compressRoots,
				sqrtOfQuadratic,
				arcsinCase,
				recursivePowerOverSqrt,
				recursiveInvPowerOverSqrt,
				
				weierstrassSub,
				
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
	public Sequence getRuleSequence(){
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
	
	
	final private double smallRandNum = Math.sqrt(1.9276182763);//just a random number thats small
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return integrateOver(floatExpr(smallRandNum),getVar(),get(),getVar()).convertToFloat(varDefs);
	}

}
