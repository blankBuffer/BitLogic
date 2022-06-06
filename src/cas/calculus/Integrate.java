package cas.calculus;

import java.math.BigInteger;

import cas.*;
import cas.primitive.*;
import cas.trig.*;
import cas.bool.*;

public class Integrate extends Expr{

	private static final long serialVersionUID = 5071855237530369367L;
	
	/*
	 * creates all the combinations for a u substitution where u is a linear equation
	 */
	private static Rule[] autoGenerateCases(String before,String after,String name) {
		Rule[] cases = new Rule[4];
		
		String case0 = before.replace("lin", "x")+"->"+after.replace("lin", "x");
		cases[0] = new Rule(case0,name);
		String case1 = before.replace("lin", "(b*x)")+"->("+after.replace("lin", "(b*x)")+")/b";
		cases[1] = new Rule(case1,"~contains(b,x)",name);
		String case2 = before.replace("lin", "(a+b*x)")+"->("+after.replace("lin", "(a+b*x)")+")/b";
		cases[2] = new Rule(case2,"~contains({a,b},x)",name);
		String case3 = before.replace("lin", "(x+a)")+"->"+after.replace("lin", "(x+a)");
		cases[3] = new Rule(case3,"~contains(a,x)",name);
		
		Rule.initRules(cases);
		return cases;
	}
	
	static Rule polynomial = new Rule(new Rule[] {
			new Rule("integrate(1,x)->x","integral of a 1"),
			new Rule("integrate(x,x)->x^2/2","integral of the variable"),
			new Rule(autoGenerateCases("integrate(1/lin,x)","ln(lin)","integral of inverse"),"integral of the inverse"),
			new Rule(autoGenerateCases("integrate(lin^n,x)","lin^(n+1)/(n+1)","reverse power rule"),"reverse power rule"),
			new Rule(autoGenerateCases("integrate(1/lin^n,x)","-1/(lin^(n-1)*(n-1))","reverse inverse power rule"),"reverse inverse power rule"),
	},"polynomial integration rules");
	
	static Rule logCases = new Rule(autoGenerateCases("integrate(ln(lin),x)","ln(lin)*lin-lin","integral of the natural log"),"integral of the natural log");
	
	static Rule basicTrig = new Rule(new Rule[] {
			new Rule(autoGenerateCases("integrate(sin(lin),x)","-cos(lin)","integral of sin"),"integral of the sin"),
			new Rule(autoGenerateCases("integrate(cos(lin),x)","sin(lin)","integral of cos"),"integral of the cos"),
			new Rule(autoGenerateCases("integrate(tan(lin),x)","-ln(cos(lin))","integral of the tan"),"integral of the tan"),
			new Rule(autoGenerateCases("integrate(1/sin(lin),x)","ln(1-cos(lin))-ln(sin(lin))","integral of 1 over sin"),"integral of 1 over sin"),
			new Rule(autoGenerateCases("integrate(1/cos(lin),x)","ln(1+sin(lin))-ln(cos(lin))","integral of 1 over cos"),"integral of 1 over cos"),
			new Rule(autoGenerateCases("integrate(1/tan(lin),x)","ln(sin(lin))","integral of 1 over tan"),"integral of 1 over tan"),
			
	},"basic trigonometric integration rules");
	
	static Rule singleTrigPower = new Rule(new Rule[] {
			new Rule("integrate(sin(x)^n,x)->-sin(x)^(n-1)*cos(x)/n+(n-1)/n*integrate(sin(x)^(n-2),x)","eval(n>1)&~contains(n,x)","integral of sin to the n"),
			new Rule("integrate(1/sin(x)^n,x)->-sin(x)^(1-n)*cos(x)/(n-1)+(n-2)*integrate(1/sin(x)^(n-2),x)/(n-1)","eval(n>1)&~contains(n,x)","integral of 1 over sin to the n"),
			new Rule("integrate(cos(x)^n,x)->cos(x)^(n-1)*sin(x)/n+(n-1)/n*integrate(cos(x)^(n-2),x)","eval(n>1)&~contains(n,x)","integral of cos to the n"),
			new Rule("integrate(1/cos(x)^n,x)->cos(x)^(1-n)*sin(x)/(n-1)+(n-2)*integrate(1/cos(x)^(n-2),x)/(n-1)","eval(n>1)&~contains(n,x)","integral of 1 over cos to the n"),
			new Rule("integrate(tan(x)^n,x)->tan(x)^(n-1)/(n-1)-integrate(tan(x)^(n-2),x)","eval(n>1)&~contains(n,x)","integral of tan to the n"),
			new Rule("integrate(1/tan(x)^n,x)->tan(x)^(1-n)/(1-n)-integrate(1/tan(x)^(n-2),x)","eval(n>1)&~contains(n,x)","integral of 1 over tan to the n"),
	},"power of a trig function");
	
	static Rule absPolynomial = new Rule(new Rule[] {
			new Rule("integrate(abs(x),x)->x*abs(x)/2","integral of the absolute value"),
			new Rule("integrate(abs(x)*x,x)->x^2*abs(x)/3","integral of product with the absolute value"),
			new Rule("integrate(abs(x)*x^n,x)->(x^(n+1)*abs(x))/(n+2)","~contains(n,x)","integral of the power of absolute value"),
	},"polynomials with absolute values");
	
	static Rule inverseTrig = new Rule(new Rule[] {
			new Rule("integrate(atan(x),x)->x*atan(x)+ln(x^2+1)/-2","integral of arctan"),
			new Rule("integrate(asin(x),x)->x*asin(x)+sqrt(1-x^2)","integral of arcsin"),
			new Rule("integrate(acos(x),x)->x*acos(x)-sqrt(1-x^2)","integral of arccos"),
	},"integral of the inverse trigonometric functions");
	
	static Rule sinTanProd = new Rule(new Rule[] {
			new Rule("integrate(sin(x)*tan(x),x)->-sin(x)+-ln(cos(x))+ln(sin(x)+1)","integral of sin times tan"),
			new Rule("integrate(1/(tan(x)*sin(x)),x)->-1/sin(x)","integral of 1 over tan times sin"),
			
	},"sin tan product integrals");
	
	static Rule tanOverCos = new Rule(new Rule[] {
			new Rule("integrate(tan(x)/cos(x),x)->1/cos(x)","integral of tan over cos"),
			new Rule("integrate(cos(x)/tan(x),x)->ln(1-cos(x))/2-ln(1+cos(x))/2+cos(x)","integral of cos over tan"),
	},"tan over cos integrals");
	
	static Rule specialSinCosProd = new Rule(new Rule[] {
			new Rule("integrate(sin(a)*cos(b),x)->integrate(sin(a+b),x)/2+integrate(sin(a-b),x)/2","eval(degree(a,x)=1)&eval(degree(b,x)=1)","integral of sin cos product"),
			new Rule("integrate(sin(a)*sin(b),x)->integrate(cos(a-b),x)/2-integrate(cos(a+b),x)/2","eval(degree(a,x)=1)&eval(degree(b,x)=1)","integral of sin sin product"),
			new Rule("integrate(cos(a)*cos(b),x)->integrate(cos(a+b),x)/2+integrate(cos(a-b),x)/2","eval(degree(a,x)=1)&eval(degree(b,x)=1)","integral of cos cos product"),
	},"product of two trigonometric functions with different linear equations");
	
	static Rule loopingIntegrals = new Rule(new Rule[] {
			new Rule("integrate(sin(a*x)*b^(c*x),x)->c*ln(b)*sin(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)-a*cos(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)","~contains({a,b,c},x)","integral of looping sine"),
			new Rule("integrate(sin(a*x)*b^x,x)->ln(b)*sin(a*x)*b^x/(a^2+ln(b)^2)-a*cos(a*x)*b^x/(a^2+ln(b)^2)","~contains({a,b},x)","integral of looping sine"),
			new Rule("integrate(sin(x)*b^(c*x),x)->c*ln(b)*sin(x)*b^(c*x)/(1+c^2*ln(b)^2)-cos(x)*b^(c*x)/(1+c^2*ln(b)^2)","~contains({b,c},x)","integral of looping sine"),
			new Rule("integrate(sin(x)*b^x,x)->ln(b)*sin(x)*b^x/(1+ln(b)^2)-cos(x)*b^x/(1+ln(b)^2)","~contains(b,x)","integral of looping sine"),
			
			new Rule("integrate(sin(x+k)*b^x,x)->ln(b)*sin(x+k)*b^x/(1+ln(b)^2)-cos(x+k)*b^x/(1+ln(b)^2)","~contains({k,b},x)","integral of looping sine"),
			new Rule("integrate(sin(a*x+k)*b^x,x)->ln(b)*sin(a*x+k)*b^x/(a^2+ln(b)^2)-a*cos(a*x+k)*b^x/(a^2+ln(b)^2)","~contains({a,k,b},x)","integral of looping sine"),
			
			
			new Rule("integrate(cos(a*x)*b^(c*x),x)->a*sin(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)+c*ln(b)*cos(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)","~contains({a,b,c},x)","integral of looping cosine"),
			new Rule("integrate(cos(a*x)*b^x,x)->a*sin(a*x)*b^x/(a^2+ln(b)^2)+ln(b)*cos(a*x)*b^x/(a^2+ln(b)^2)","~contains({a,b},x)","integral of looping cosine"),
			new Rule("integrate(cos(x)*b^(c*x),x)->sin(x)*b^(c*x)/(1+c^2*ln(b)^2)+c*ln(b)*cos(x)*b^(c*x)/(1+c^2*ln(b)^2)","~contains({b,c},x)","integral of looping cosine"),
			new Rule("integrate(cos(x)*b^x,x)->sin(x)*b^x/(1+ln(b)^2)+ln(b)*cos(x)*b^x/(1+ln(b)^2)","~contains(b,x)","integral of looping cosine"),
			
			new Rule("integrate(cos(x+k)*b^x,x)->sin(x+k)*b^x/(1+ln(b)^2)+ln(b)*cos(x+k)*b^x/(1+ln(b)^2)","~contains({k,b},x)","integral of looping cosine"),
			new Rule("integrate(cos(a*x+k)*b^x,x)->a*sin(a*x+k)*b^x/(a^2+ln(b)^2)+ln(b)*cos(a*x+k)*b^x/(a^2+ln(b)^2)","~contains({a,k,b},x)","integral of looping cosine"),
	},"looping integrals");
	
	static Rule recursivePowerOverSqrt = new Rule(new Rule[] {
		new Rule("integrate(x^n/sqrt(a*x+b),x)->(2*x^n*sqrt(a*x+b))/(a*(2*n+1))-(2*n*b*integrate(x^(n-1)/sqrt(a*x+b),x))/(a*(2*n+1))","~contains({n,a,b},x)","power over sqrt"),
		new Rule("integrate(x^n/sqrt(x+b),x)->(2*x^n*sqrt(x+b))/(2*n+1)-(2*n*b*integrate(x^(n-1)/sqrt(x+b),x))/(2*n+1)","~contains({n,b},x)","power over sqrt"),
		new Rule("integrate(x/sqrt(a*x+b),x)->(2*x*sqrt(a*x+b))/(a*3)-(2*b*integrate(1/sqrt(a*x+b),x))/(a*3)","~contains({a,b},x)","power over sqrt"),
		new Rule("integrate(x/sqrt(x+b),x)->(2*x*sqrt(x+b))/3-(2*b*integrate(1/sqrt(x+b),x))/3","~contains(b,x)","power over sqrt"),
	},"power over sqrt");
	
	static Rule recursiveInvPowerOverSqrt = new Rule(new Rule[] {
			new Rule("integrate(1/(sqrt(a*x+b)*x^n),x)->(-sqrt(a*x+b))/((n-1)*b*x^(n-1))-(a*(2*n-3)*integrate(1/(sqrt(a*x+b)*x^(n-1)),x))/(2*b*(n-1))","~contains({a,b,n},x)","power over sqrt"),
			new Rule("integrate(1/(sqrt(a*x+b)*x),x)->ln(1-sqrt(a*x+b)/sqrt(b))/sqrt(b)-ln(1+sqrt(a*x+b)/sqrt(b))/sqrt(b)","~contains({a,b},x)&(eval(b>0)|allowComplexNumbers())","power over sqrt"),
			new Rule("integrate(1/(sqrt(x+b)*x^n),x)->(-sqrt(x+b))/((n-1)*b*x^(n-1))-((2*n-3)*integrate(1/(sqrt(x+b)*x^(n-1)),x))/(2*b*(n-1))","~contains({b,n},x)","power over sqrt"),
			new Rule("integrate(1/(sqrt(x+b)*x),x)->ln(1-sqrt(x+b)/sqrt(b))/sqrt(b)-ln(1+sqrt(x+b)/sqrt(b))/sqrt(b)","~contains(b,x)&(eval(b>0)|allowComplexNumbers())","power over sqrt"),
	},"1 over power times sqrt");
	
	static Rule integralForArcsin = new Rule(new Rule[]{
			new Rule("integrate(1/sqrt(a-x^2),x)->asin(x/sqrt(a))","~contains(a,x)","simple integral leading to arcsin"),
			new Rule("integrate(1/sqrt(a+b*x^2),x)->asin((sqrt(-b)*x)/sqrt(a))/sqrt(-b)","(eval(b<0)|allowComplexNumbers())&~contains({a,b},x)","simple integral leading to arcsin"),
	},"integrals leading to arcsin");
	
	static Rule sinCosProdReduction = new Rule(new Rule[] {
			new Rule("integrate(sin(x)^m*cos(x)^n,x)->sin(x)^(m+1)*cos(x)^(n-1)/(m+n)+(n-1)/(m+n)*integrate(sin(x)^m*cos(x)^(n-2),x)","eval(n>m)&isType(m,num)&isType(n,num)","trig reduction formula"),
			new Rule("integrate(sin(x)^m*cos(x)^n,x)->-sin(x)^(m-1)*cos(x)^(n+1)/(m+n)+(m-1)/(m+n)*integrate(sin(x)^(m-2)*cos(x)^n,x)","(eval(m>n)|eval(m=n))&isType(m,num)&isType(n,num)","trig reduction formula"),
			new Rule("integrate(1/(sin(x)^m*cos(x)^n),x)->1/((n-1)*sin(x)^(m-1)*cos(x)^(n-1))+(m+n-2)/(n-1)*integrate(1/(sin(x)^m*cos(x)^(n-2)),x)","eval(n>m)&isType(m,num)&isType(n,num)","trig reduction formula"),
			new Rule("integrate(1/(sin(x)^m*cos(x)^n),x)->-1/((m-1)*sin(x)^(m-1)*cos(x)^(n-1))+(m+n-2)/(m-1)*integrate(1/(sin(x)^(m-2)*cos(x)^n),x)","(eval(m>n)|eval(m=n))&isType(m,num)&isType(n,num)","trig reduction formula"),
	},"product of sin and cos powers");
	
	static Rule sinTanProdReduction = new Rule(new Rule[] {
			new Rule("integrate(sin(x)^m*tan(x)^n,x)->sin(x)^m*tan(x)^(n-1)/(n-1)-(n+m-1)/(n-1)*integrate(sin(x)^m*tan(x)^(n-2),x)","isType(m,num)&isType(n,num)","trig reduction formula"),
			new Rule("integrate(1/(sin(x)^m*tan(x)^n),x)->-1/((n+m-1)*tan(x)^(n-1)*sin(x)^m)-(n-1)/(n+m-1)*integrate(1/(sin(x)^m*tan(x)^(n-2)),x)","isType(m,num)&isType(n,num)","trig reduction formula"),
	},"product of sin and tan powers");
	
	static Rule tanOverCosReduction = new Rule(new Rule[] {
			new Rule("integrate(tan(x)^m/cos(x)^n,x)->tan(x)^(m-1)/((n+m-1)*cos(x)^n)-(m-1)/(n+m-1)*integrate(tan(x)^(m-2)/cos(x)^n,x)","isType(m,num)&isType(n,num)","trig reduction formula"),
			new Rule("integrate(cos(x)^m/tan(x)^n,x)->-cos(x)^m/((n-1)*tan(x)^(n-1))-(m+n-1)/(n-1)*integrate(cos(x)^m/tan(x)^(n-2),x)","isType(m,num)&isType(n,num)","trig reduction formula"),
			
			new Rule("integrate(tan(x)^m/cos(x),x)->tan(x)^(m-1)/(m*cos(x))-(m-1)/m*integrate(tan(x)^(m-2)/cos(x),x)","isType(m,num)","trig reduction formula"),
			new Rule("integrate(cos(x)/tan(x)^n,x)->-cos(x)/((n-1)*tan(x)^(n-1))-n/(n-1)*integrate(cos(x)/tan(x)^(n-2),x)","isType(n,num)","trig reduction formula"),
	},"tan power over cos power");
	
	static Rule inverseQuadratic = new Rule("inverse quadratic"){//robust
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
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			
			Integrate integ = (Integrate)e;
			
			if(integ.get() instanceof Div && !((Div)integ.get()).getNumer().contains(integ.getVar())) {
				Expr denom = ((Div)integ.get()).getDenom();
				Sequence poly = polyExtract(denom, integ.getVar(), casInfo);
				if(poly != null && poly.size() == 3) {
					Expr c = poly.get(0),b = poly.get(1),a = poly.get(2);
					ExprList subTable = exprList(equ(var("0a"),a),equ(var("0b"),b),equ(var("0c"),c),equ(var("x"),integ.getVar()));
					
					Expr check = this.check.replace(subTable).simplify(casInfo);
					
					subTable.add(equ(var("0k"),check));
					if(!check.negative()) {//check is negative if the quadratic contains no roots thus using arctan
						return this.arctanCase.replace(subTable).simplify(casInfo);
					}
					//otherwise use logarithms
					return this.logCase.replace(subTable).simplify(casInfo);
				}
			}
			return integ;
			
		}
		
	};
	
	// these are the reverse process of diff(atan(x^n),x) -> (n-1)*x^(n-1)/(x^(2*n)+1) 
	static Rule inverseQuadraticUSub = new Rule(new Rule[] {
			new Rule("integrate(x^a/(x^b+c),x)->atan(x^(a+1)/sqrt(c))/((a+1)*sqrt(c))","eval(b/(a+1)=2)&(eval(c>0)|allowComplexNumbers())&~contains({a,b,c},x)","inverse quadratic with u sub"),
			new Rule("integrate(x^a/(d*x^b+c),x)->atan((x^(a+1)*sqrt(d))/sqrt(c))/((a+1)*sqrt(d*c))","eval(b/(a+1)=2)&(eval(c*d>0)|allowComplexNumbers())&~contains({a,b,c,d},x)","inverse quadratic with u sub"),
			
			new Rule("integrate(x/(x^b+c),x)->atan(x^2/sqrt(c))/(2*sqrt(c))","eval(b/2=2)&(eval(c>0)|allowComplexNumbers())&~contains({b,c},x)","inverse quadratic with u sub"),
			new Rule("integrate(x/(d*x^b+c),x)->atan((x^2*sqrt(d))/sqrt(c))/(2*sqrt(d*c))","eval(b/2=2)&(eval(c*d>0)|allowComplexNumbers())&~contains({b,c,d},x)","inverse quadratic with u sub"),
	},"reverse to arctan with power");
	
	static Rule inverseQuadraticToNReduction = new Rule("1 over quadratic to the n") {
		private static final long serialVersionUID = 1L;
		
		Expr ans;
		Expr invPow;
		
		@Override
		public void init() {
			ans = createExpr("(-2*a*x-b)/((n-1)*(b^2-4*a*c)*(a*x^2+b*x+c)^(n-1))-2*(2*n-3)*a/((n-1)*(b^2-4*a*c))*integrate(1/(a*x^2+b*x+c)^(n-1),x)");
			invPow = createExpr("1/q^n");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Integrate integ = (Integrate)e;
			
			if( !Rule.fastSimilarExpr(invPow,integ.get() )) return integ;
			Power denom = (Power)(((Div)integ.get()).getDenom());
			
			if(!isPositiveRealNum(denom.getExpo())) return integ;
			Num n = (Num)denom.getExpo();
			
			Sequence coef = polyExtract(denom.getBase() ,integ.getVar(),casInfo);
			if(coef == null || coef.size() != 3) return integ;
			ExprList equs = exprList(  equ(var("a"),coef.get(2)) , equ(var("b"),coef.get(1)) , equ(var("c"),coef.get(0)) , equ(var("n"),n) , equ(var("x"),integ.getVar()) );
			
			return ans.replace(equs).simplify(casInfo);
		}
		
	};
	
	static Rule integrationByParts = new Rule("integration by parts"){
		private static final long serialVersionUID = 1L;
		final int OKAY = 0,GOOD = 1,GREAT = 2,BEST = 3;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
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
					Expr newIntegral = integrate(innerDiv,integ.getVar()).simplify(casInfo);
					//newIntegral.println();
					if(!newIntegral.containsType("integrate")) {
						Expr out = sub(prod(newIntegral,best),integrate(prod(newIntegral.copy(),diff(best.copy(),integ.getVar())),integ.getVar()));
						return out.simplify(casInfo);
					}
				}
				
			}
			return integ;
		}
	};
	
	static Rule integrationByPartsSpecial = new Rule("special integration by parts"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Integrate integ = (Integrate)e;
			if(integ.get() instanceof Div && !integ.get().containsType("integrate")) {
				Div innerDiv = (Div)integ.get().copy();
				if(innerDiv.getDenom() instanceof Power) {
					Power denomPower = (Power)innerDiv.getDenom();
					Div expo = Div.cast(denomPower.getExpo());
					if(expo.isNumericalAndReal() && isPlainPolynomial(denomPower.getBase(),integ.getVar()) && degree(denomPower.getBase(),integ.getVar()).equals(BigInteger.ONE) ) {
						if( ((Num)expo.getNumer()).realValue.compareTo( ((Num)expo.getDenom()).realValue )  == 1) {//make sure the fraction is greater than 1
							Expr integralOfDenom = integrate(inv(denomPower),integ.getVar()).simplify(casInfo);
							Expr derivativeOfNumer = diff(innerDiv.getNumer(),integ.getVar()).simplify(casInfo);
							if(!(derivativeOfNumer instanceof Div && ((Div)derivativeOfNumer).getDenom().contains(integ.getVar())  )) {
								Expr out = sub(prod(innerDiv.getNumer(),integralOfDenom),integrate( prod(derivativeOfNumer,integralOfDenom.copy()) ,integ.getVar()));
								return out.simplify(casInfo);
							}
						}
					}
				}
			}
			return integ;
		}
	};
	
	static Rule specialUSub = new Rule("special u sub, f(x)*diff(f(x),x)"){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
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
				resToCheck = resToCheck.simplify(casInfo);
				if(!resToCheck.contains(integ.getVar())) {
					Div res = div(prod(pow(testExpr,num(2)),resToCheck),num(2));
					return res.simplify(casInfo);
				}
				
			}
			
			
			return integ;
		}
	};
	
	static Rule normalUSub = new Rule("normal u sub"){
		private static final long serialVersionUID = 1L;
		
		public Expr getNextInnerFunction(Expr e,Var v) {
			if(e.size()>0 && e.contains(v)){
				if(e instanceof Power && !((Power)e).getBase().contains(v) && ((Power)e).getExpo() instanceof Prod) {//if in the form of a^(b*x) return a^x
					return pow(((Power)e).getBase(),v);
				}
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
		
		public int countNegatives(Expr e) {//counts the number of negative numbers in the expression
			if(e instanceof Num) return (e.negative() ? 1 : 0);
			int sum = 0;
			
			for(int i = 0;i<e.size();i++) {
				sum+=countNegatives(e.get(i));
			}
			
			return sum;
		}
		public Expr leastNegative(ExprList exprs) {
			Expr least = exprs.get(0);
			int leastCount = countNegatives(exprs.get(0));
			
			for(int i = 1;i<exprs.size();i++) {
				Expr current = exprs.get(i);
				int count = countNegatives(current);
				if(count < leastCount) {
					 leastCount = count;
					 least = current;
				}
			}
			
			return least;
		}
		
		Var uSubVar;
		
		@Override
		public void init(){
			uSubVar = var("0u");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
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
				boolean logCase = !div(casted.getNumer(),diff(casted.getDenom() ,integ.getVar())).simplify(casInfo).contains(integ.getVar());
				
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
						
						Expr diffObj = diff(u,(Var)integ.getVar().copy()).simplify(casInfo);
						if(diffObj.containsType("diff")) return integ;
						
						diffObj = diffObj.replace(eq);//it is possible for derivative to contain u
						Expr before = div(integ.get().replace(eq),diffObj);
						Expr newExpr = before.simplify(casInfo);
						if(!newExpr.contains(integ.getVar())) {//no solve needed
							newExpr = integrate(newExpr,uSubVar).simplify(casInfo);
							if(!newExpr.containsType("integrate")) {
								Expr out = newExpr.replace(equ(uSubVar,u));
								return out.simplify(casInfo);
							}
						}else {//oof we need to solve for x
							Expr solved = solve(equ(uSubVar,u),integ.getVar()).simplify(casInfo);
							if(solved instanceof ExprList) {
								solved = leastNegative((ExprList)solved);//choose the solution with the least amount of negatives
							}
							if(!(solved instanceof Solve)) {
								solved = ((Equ)solved).getRightSide();
								newExpr = integrate(newExpr.replace(equ(integ.getVar(),solved)),uSubVar);
								newExpr = newExpr.simplify(casInfo);
								if(!newExpr.containsType("integrate")) {
									Expr out = newExpr.replace(equ(uSubVar,u)).simplify(casInfo);
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
	static Rule partialFraction = new Rule("partial fractions"){
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Integrate integ = (Integrate)e;
			
			integ.set(0, partialFrac(integ.get(), integ.getVar(), casInfo) );
			if(integ.get() instanceof Sum){
				Expr out = StandardRules.linearOperator.applyRuleToExpr(integ, casInfo);
				return out;
			}
			return integ;
		}
	
	};
	
	static Rule polyDiv = new Rule("polynomial division"){
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Integrate integ = (Integrate)e;
			
			integ.set(0, polyDiv(integ.get(), integ.getVar(), casInfo) );
			if(integ.get() instanceof Sum){
				return StandardRules.linearOperator.applyRuleToExpr(integ, casInfo);
			}
			
			return integ;
		}
	
	};
	
	static Rule psudoTrigSub = new Rule(new Rule[]{
			
			//x^n/sqrt(1-x^2) generalization
			new Rule("integrate(x^n/sqrt(a+b*x^2),x)->a^(n/2)/(-b)^((n+1)/2)*subst(integrate(sin(0k)^n,0k),0k=asin(sqrt(-b/a)*x))","(eval(b<0)&eval(a>0)&(eval(-b<a)|eval(-b=a))|allowComplexNumbers())&~contains({a,b},x)&isType(n,num)","trig sub"),
			//sqrt(1-x^2)/x^n generalization
			new Rule("integrate(sqrt(a+b*x^2)/x^n,x)->(-b)^((n-1)/2)/a^((n-2)/2)*subst(integrate(1/sin(0k)^n,0k)-integrate(1/sin(0k)^(n-2),0k),0k=asin(sqrt(-b/a)*x))","(eval(b<0)&eval(a>0)&(eval(-b<a)|eval(-b=a))|allowComplexNumbers())&~contains({a,b},x)&eval(n>1)&isType(n,num)","trig sub"),
			
			//sqrt(1+x^2)/x^n generalization
			new Rule("integrate(sqrt(a*x^2+b)/x^n,x)->a^((n-1)/2)/b^((n-2)/2)*subst(integrate(1/(cos(0k)^3*tan(0k)^n),0k),0k=atan(sqrt(a/b)*x))","~contains({a,b},x)&(eval(a>0)&eval(b>0)|allowComplexNumbers())&isType(n,num)","trig sub"),
			new Rule("integrate(sqrt(x^2+b)/x^n,x)->1/b^((n-2)/2)*subst(integrate(1/(cos(0k)^3*tan(0k)^n),0k),0k=atan(x/sqrt(b)))","~contains(b,x)&(eval(b>0)|allowComplexNumbers())&isType(n,num)","trig sub"),
			
			//x^n/sqrt(1+x^2) generalization
			new Rule("integrate(x^n/sqrt(a*x^2+b),x)->b^(n/2)/a^((n+1)/2)*subst(integrate(tan(0k)^n/cos(0k),0k),0k=atan(sqrt(a/b)*x))","~contains({a,b},x)&(eval(a>0)&eval(b>0)|allowComplexNumbers())&isType(n,num)","trig sub"),
			new Rule("integrate(x^n/sqrt(x^2+b),x)->b^(n/2)*subst(integrate(tan(0k)^n/cos(0k),0k),0k=atan(x/sqrt(b)))","~contains(b,x)&(eval(b>0)|allowComplexNumbers())&isType(n,num)","trig sub"),
			
			//sqrt(x^2-1)/x^n generalization
			new Rule("integrate(sqrt(a*x^2+b)/x^n,x)->a^((n-1)/2)/(-b)^((n-2)/2)*subst(integrate(cos(0k)^(n-3)*sin(0k)^2,0k),0k=acos(sqrt(-b)/(sqrt(a)*x)))","~contains({a,b},x)&(eval(a>0)&(eval(-b<a)|eval(-b=a))|allowComplexNumbers())&isType(n,num)","trig sub"),
			new Rule("integrate(sqrt(x^2+b)/x^n,x)->1/(-b)^((n-2)/2)*subst(integrate(cos(0k)^(n-3)*sin(0k)^2,0k),0k=acos(sqrt(-b)/x))","~contains(b,x)&(eval(-b<1)|eval(b=-1)|allowComplexNumbers())&isType(n,num)","trig sub"),
			
			//x^n/sqrt(x^2-1) generalization
			new Rule("integrate(x^n/sqrt(a*x^2+b),x)->(-b)^(n/2)/a^((n+1)/2)*subst(integrate(1/cos(0t)^(n+1),0t),0t=acos(sqrt(-b)/(sqrt(a)*x)))","~contains({a,b},x)&(eval(a>0)&(eval(-b<a)|eval(-b=a))|allowComplexNumbers())&isType(n,num)",""),
			new Rule("integrate(x^n/sqrt(x^2+b),x)->(-b)^(n/2)*subst(integrate(1/cos(0t)^(n+1),0t),0t=acos(sqrt(-b)/x))","~contains(b,x)&(eval(-b<1)|eval(b=-1)|allowComplexNumbers())&isType(n,num)",""),
			
			new Rule("integrate(1/sqrt(a+b*x^2),x)->ln(x*sqrt(b)+sqrt(a+b*x^2))/(2*sqrt(b))-ln(sqrt(a+b*x^2)-x*sqrt(b))/(2*sqrt(b))","(eval(a>0)&eval(b>0)|allowComplexNumbers())&~contains({a,b},x)","trig sub"),
			new Rule("integrate(1/sqrt(a+x^2),x)->ln(x+sqrt(a+x^2))/2-ln(sqrt(a+x^2)-x)/2","(eval(a>0)|allowComplexNumbers())&~contains(a,x)","trig sub"),
			
	},"psudo trig substitution");
	
	static Rule sqrtOfQuadratic = new Rule("square root has quadratic") {
		private static final long serialVersionUID = 1L;
		Expr resultPos,resultNeg;
		Expr check;
		
		@Override
		public void init() {
			resultPos = createExpr("(b+2*a*x)*sqrt(a*x^2+b*x+c)/(4*a)+(4*a*c-b^2)*(ln(2*sqrt(a)*sqrt(a*x^2+b*x+c)+2*a*x+b)-ln(2))/(8*a^(3/2))");
			resultNeg = createExpr("(b+2*a*x)*sqrt(a*x^2+b*x+c)/(4*a)+(4*a*c-b^2)*asin((2*a*x+b)/sqrt(b^2-4*a*c))/(8*(-a)^(3/2))");
			check = createExpr("eval(a>0)");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Integrate integ = (Integrate)e;
			if(Rule.fastSimilarExpr(sqrtObj, integ.get())) {
				//System.out.println(e);
				Sequence coefs = polyExtract(e.get().get(),integ.getVar(),casInfo);
				if(coefs == null) return integ;
				if(coefs.size() == 3) {
					ExprList equs = exprList( equ(var("c"),coefs.get(0)) , equ(var("b"),coefs.get(1)) , equ(var("a"),coefs.get(2)) , equ(var("x"),integ.getVar()) );
					boolean aPositive = check.replace(equs).simplify(casInfo).equals(BoolState.TRUE);
					Expr out;
					if(aPositive) {
						out = resultPos.replace(equs).simplify(casInfo);
					}else {
						out = resultNeg.replace(equs).simplify(casInfo);
					}
					//System.out.println(out);
					return out;
				}
				
			}
			return integ;
		}
	};
	
	static Rule fullExpandInner = new Rule("full expansion") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Integrate integ = (Integrate)e;
			integ.set(0, SimpleFuncs.fullExpand.applyRuleToExpr(integ.get(), casInfo) );
			return integ;
		}
		
	};
	
	static Rule weierstrassSub = new Rule("weierstrass substitution") {
		private static final long serialVersionUID = 1L;
		
		public Expr getSinOrCosInner(Expr e) {//search tree for inside of sin or cos
			if(e instanceof Sin || e instanceof Cos) return e.get();
			for(int i = 0;i<e.size();i++) {
				Expr inner = getSinOrCosInner(e.get(i));
				if(inner != null) return inner;
			}
			return null;
		}
		ExprList subs;
		Expr addedDeriv;
		@Override
		public void init() {
			subs = (ExprList)createExpr("[sin(0a)=(2*0t)/(1+0t^2),cos(0a)=(1-0t^2)/(1+0t^2)]");
			addedDeriv = createExpr("2/(0t^2+1)");
		}
		
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Integrate integ = (Integrate)e;
			if(!( (integ.containsType("sin") || integ.containsType("cos")) && integ.containsType("sum") && !(integ.get() instanceof Sum) )) return integ;//needs sin or cos
			
			Expr innerTrig = getSinOrCosInner(integ.get());
			if(innerTrig == null) return integ;
			
			Equ equ = equ(var("0a"),innerTrig);
			ExprList subs = (ExprList) this.subs.replace(equ);
			Expr addedDeriv = this.addedDeriv.replace(equ);
			
			Expr newInner = div(prod(integ.get().replace(subs),addedDeriv),diff(innerTrig,integ.getVar()));
			newInner = newInner.simplify(casInfo);
			
			if(newInner.contains(integ.getVar()) || containsTrig(newInner)) return integ;
			
			Expr integRes = integrate(newInner,var("0t")).simplify(casInfo);
			if(!integRes.containsType("integrate")) {
				Expr out = integRes.replace(equ(var("0t"),tan( div(innerTrig,num(2)) ))).simplify(casInfo);
				return out;
			}
			
			return integ;
		}
		
	};
	
	static Rule shouldExpand = new Rule("should expand") {//there is a power with trig function in base sum. Should expand as it saves alot of time
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Integrate integ = (Integrate)e;
			if(integ.get() instanceof Power) {
				Power inner = (Power)integ.get();
				
				if(inner.getExpo() instanceof Num && inner.getBase() instanceof Sum && containsTrig(inner.getBase()) ) {
					integ.set(0, SimpleFuncs.fullExpand.applyRuleToExpr(integ.get(), casInfo) );
				}
			}
			return integ;
		}
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				
				StandardRules.pullOutConstants,
				polynomial,
				logCases,
				inverseQuadratic,
				inverseQuadraticUSub,
				absPolynomial,
				
				basicTrig,
				singleTrigPower,
				inverseTrig,
				
				specialSinCosProd,
				
				sinCosProdReduction,
				sinTanProdReduction,
				tanOverCosReduction,
				
				sinTanProd,
				tanOverCos,
				
				shouldExpand,
				
				inverseQuadraticToNReduction,
				
				loopingIntegrals,
				
				sqrtOfQuadratic,
				recursivePowerOverSqrt,
				recursiveInvPowerOverSqrt,
				
				
				
				psudoTrigSub,
				integralForArcsin,
				
				partialFraction,
				polyDiv,
				StandardRules.distrInner,
				specialUSub,
				integrationByParts,
				normalUSub,
				integrationByPartsSpecial,
				weierstrassSub,
				fullExpandInner,
				StandardRules.linearOperator
				
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence(){
		return ruleSequence;
	}
	
	static Rule postProcessing = new Rule("after integration cleanup") {
		private static final long serialVersionUID = 1L;
		
		Expr applyAbs(Expr e,CasInfo casInfo) {
			if(e instanceof Ln && !(e.get() instanceof Abs)) return ln(abs(e.get())).simplify(casInfo);
			for(int i = 0;i<e.size();i++) {
				e.set(i,applyAbs(e.get(i),casInfo));
			}
			return e;
		}
		
		@Override
		public Expr applyRuleToExpr(Expr expr,CasInfo casInfo) {
			
			if(expr.containsType("integrate") || expr.contains(var("0u"))) return expr;//obviously in the middle of processing if you encounter u
			
			if(!casInfo.allowComplexNumbers()) {
				expr = applyAbs(expr,casInfo);
			}
			expr = distr(expr.simplify(casInfo)).simplify(casInfo);
			
			if(expr instanceof Sum) {//remove any trailing constants
				for(int i = 0;i<expr.size();i++) {
					if(!expr.get(i).containsVars()) {
						expr.remove(i);
						i--;
					}
				}
			}
			
			return Sum.unCast(expr);
		}
	};
	
	@Override
	public Rule getDoneRule() {
		return postProcessing;
	}
	
	public Integrate(){}//
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

	@Override
	public String typeName() {
		return "integrate";
	}

	@Override
	public String help() {
		return "integrate(function,variable) the integration computer\n"
				+ "examples\n"
				+ "integrate(sin(2*x+1),x)->cos(2*x+1)/-2\n"
				+ "integrate(x^n,x)->x^(n+1)/(n+1)";
	}
}
