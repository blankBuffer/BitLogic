package cas.calculus;

import java.math.BigInteger;

import cas.*;
import cas.primitive.*;
import cas.bool.*;

public class Integrate{
	
	public static Func.FuncLoader integrateLoader = new Func.FuncLoader() {
		
		/*
		 * creates all the combinations for a u substitution where u is a linear equation
		 */
		Rule[] autoGenerateCases(String before,String after,String name) {
			Rule[] cases = new Rule[4];
			
			String case0 = before.replace("lin", "x")+"->"+after.replace("lin", "x");
			cases[0] = new Rule(case0,name);
			String case1 = before.replace("lin", "(b*x)")+"->("+after.replace("lin", "(b*x)")+")/b";
			cases[1] = new Rule(case1,"~contains(b,x)",name);
			String case2 = before.replace("lin", "(a+b*x)")+"->("+after.replace("lin", "(a+b*x)")+")/b";
			cases[2] = new Rule(case2,"~contains([a,b],x)",name);
			String case3 = before.replace("lin", "(x+a)")+"->"+after.replace("lin", "(x+a)");
			cases[3] = new Rule(case3,"~contains(a,x)",name);
			
			return cases;
		}
		
		final private double smallRandNum = Math.sqrt(1.9276182763);//just a random number thats small
		
		@Override
		public void load(Func owner) {
			
			Rule polynomial = new Rule(new Rule[] {
					new Rule("integrate(1,x)->x","integral of a 1"),
					new Rule("integrate(x,x)->x^2/2","integral of the variable"),
					new Rule(autoGenerateCases("integrate(1/lin,x)","ln(lin)","integral of inverse"),"integral of the inverse"),
					new Rule(autoGenerateCases("integrate(lin^n,x)","lin^(n+1)/(n+1)","reverse power rule"),"reverse power rule"),
					new Rule(autoGenerateCases("integrate(1/lin^n,x)","-1/(lin^(n-1)*(n-1))","reverse inverse power rule"),"reverse inverse power rule"),
			},"polynomial integration rules");
			
			Rule logCases = new Rule(autoGenerateCases("integrate(ln(lin),x)","ln(lin)*lin-lin","integral of the natural log"),"integral of the natural log");
			
			Rule basicTrig = new Rule(new Rule[] {
					new Rule(autoGenerateCases("integrate(sin(lin),x)","-cos(lin)","integral of sin"),"integral of the sin"),
					new Rule(autoGenerateCases("integrate(cos(lin),x)","sin(lin)","integral of cos"),"integral of the cos"),
					new Rule(autoGenerateCases("integrate(tan(lin),x)","-ln(cos(lin))","integral of the tan"),"integral of the tan"),
					new Rule(autoGenerateCases("integrate(1/sin(lin),x)","ln(1-cos(lin))-ln(sin(lin))","integral of 1 over sin"),"integral of 1 over sin"),
					new Rule(autoGenerateCases("integrate(1/cos(lin),x)","ln(1+sin(lin))-ln(cos(lin))","integral of 1 over cos"),"integral of 1 over cos"),
					new Rule(autoGenerateCases("integrate(1/tan(lin),x)","ln(sin(lin))","integral of 1 over tan"),"integral of 1 over tan"),
					
			},"basic trigonometric integration rules");
			
			Rule singleTrigPower = new Rule(new Rule[] {
					new Rule("integrate(sin(x)^n,x)->-sin(x)^(n-1)*cos(x)/n+(n-1)/n*integrate(sin(x)^(n-2),x)","comparison(n>1)&~contains(n,x)","integral of sin to the n"),
					new Rule("integrate(1/sin(x)^n,x)->-sin(x)^(1-n)*cos(x)/(n-1)+(n-2)*integrate(1/sin(x)^(n-2),x)/(n-1)","comparison(n>1)&~contains(n,x)","integral of 1 over sin to the n"),
					new Rule("integrate(cos(x)^n,x)->cos(x)^(n-1)*sin(x)/n+(n-1)/n*integrate(cos(x)^(n-2),x)","comparison(n>1)&~contains(n,x)","integral of cos to the n"),
					new Rule("integrate(1/cos(x)^n,x)->cos(x)^(1-n)*sin(x)/(n-1)+(n-2)*integrate(1/cos(x)^(n-2),x)/(n-1)","comparison(n>1)&~contains(n,x)","integral of 1 over cos to the n"),
					new Rule("integrate(tan(x)^n,x)->tan(x)^(n-1)/(n-1)-integrate(tan(x)^(n-2),x)","comparison(n>1)&~contains(n,x)","integral of tan to the n"),
					new Rule("integrate(1/tan(x)^n,x)->tan(x)^(1-n)/(1-n)-integrate(1/tan(x)^(n-2),x)","comparison(n>1)&~contains(n,x)","integral of 1 over tan to the n"),
			},"power of a trig function");
			
			Rule absPolynomial = new Rule(new Rule[] {
					new Rule("integrate(abs(x),x)->x*abs(x)/2","integral of the absolute value"),
					new Rule("integrate(abs(x)*x,x)->x^2*abs(x)/3","integral of product with the absolute value"),
					new Rule("integrate(abs(x)*x^n,x)->(x^(n+1)*abs(x))/(n+2)","~contains(n,x)","integral of the power of absolute value"),
			},"polynomials with absolute values");
			
			Rule inverseTrig = new Rule(new Rule[] {
					new Rule("integrate(atan(x),x)->x*atan(x)+ln(x^2+1)/-2","integral of arctan"),
					new Rule("integrate(asin(x),x)->x*asin(x)+sqrt(1-x^2)","integral of arcsin"),
					new Rule("integrate(acos(x),x)->x*acos(x)-sqrt(1-x^2)","integral of arccos"),
			},"integral of the inverse trigonometric functions");
			
			Rule sinTanProd = new Rule(new Rule[] {
					new Rule("integrate(sin(x)*tan(x),x)->-sin(x)+-ln(cos(x))+ln(sin(x)+1)","integral of sin times tan"),
					new Rule("integrate(1/(tan(x)*sin(x)),x)->-1/sin(x)","integral of 1 over tan times sin"),
					
			},"sin tan product integrals");
			
			Rule tanOverCos = new Rule(new Rule[] {
					new Rule("integrate(tan(x)/cos(x),x)->1/cos(x)","integral of tan over cos"),
					new Rule("integrate(cos(x)/tan(x),x)->ln(1-cos(x))/2-ln(1+cos(x))/2+cos(x)","integral of cos over tan"),
			},"tan over cos integrals");
			
			Rule specialSinCosProd = new Rule(new Rule[] {
					new Rule("integrate(sin(a)*cos(b),x)->integrate(sin(a+b),x)/2+integrate(sin(a-b),x)/2","comparison(degree(a,x)=1)&comparison(degree(b,x)=1)","integral of sin cos product"),
					new Rule("integrate(sin(a)*sin(b),x)->integrate(cos(a-b),x)/2-integrate(cos(a+b),x)/2","comparison(degree(a,x)=1)&comparison(degree(b,x)=1)","integral of sin sin product"),
					new Rule("integrate(cos(a)*cos(b),x)->integrate(cos(a+b),x)/2+integrate(cos(a-b),x)/2","comparison(degree(a,x)=1)&comparison(degree(b,x)=1)","integral of cos cos product"),
			},"product of two trigonometric functions with different linear equations");
			
			Rule loopingIntegrals = new Rule(new Rule[] {
					new Rule("integrate(sin(a*x)*b^(c*x),x)->c*ln(b)*sin(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)-a*cos(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)","~contains([a,b,c],x)","integral of looping sine"),
					new Rule("integrate(sin(a*x)*b^x,x)->ln(b)*sin(a*x)*b^x/(a^2+ln(b)^2)-a*cos(a*x)*b^x/(a^2+ln(b)^2)","~contains([a,b],x)","integral of looping sine"),
					new Rule("integrate(sin(x)*b^(c*x),x)->c*ln(b)*sin(x)*b^(c*x)/(1+c^2*ln(b)^2)-cos(x)*b^(c*x)/(1+c^2*ln(b)^2)","~contains([b,c],x)","integral of looping sine"),
					new Rule("integrate(sin(x)*b^x,x)->ln(b)*sin(x)*b^x/(1+ln(b)^2)-cos(x)*b^x/(1+ln(b)^2)","~contains(b,x)","integral of looping sine"),
					
					new Rule("integrate(sin(x+k)*b^x,x)->ln(b)*sin(x+k)*b^x/(1+ln(b)^2)-cos(x+k)*b^x/(1+ln(b)^2)","~contains([k,b],x)","integral of looping sine"),
					new Rule("integrate(sin(a*x+k)*b^x,x)->ln(b)*sin(a*x+k)*b^x/(a^2+ln(b)^2)-a*cos(a*x+k)*b^x/(a^2+ln(b)^2)","~contains([a,k,b],x)","integral of looping sine"),
					
					
					new Rule("integrate(cos(a*x)*b^(c*x),x)->a*sin(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)+c*ln(b)*cos(a*x)*b^(c*x)/(a^2+c^2*ln(b)^2)","~contains([a,b,c],x)","integral of looping cosine"),
					new Rule("integrate(cos(a*x)*b^x,x)->a*sin(a*x)*b^x/(a^2+ln(b)^2)+ln(b)*cos(a*x)*b^x/(a^2+ln(b)^2)","~contains([a,b],x)","integral of looping cosine"),
					new Rule("integrate(cos(x)*b^(c*x),x)->sin(x)*b^(c*x)/(1+c^2*ln(b)^2)+c*ln(b)*cos(x)*b^(c*x)/(1+c^2*ln(b)^2)","~contains([b,c],x)","integral of looping cosine"),
					new Rule("integrate(cos(x)*b^x,x)->sin(x)*b^x/(1+ln(b)^2)+ln(b)*cos(x)*b^x/(1+ln(b)^2)","~contains(b,x)","integral of looping cosine"),
					
					new Rule("integrate(cos(x+k)*b^x,x)->sin(x+k)*b^x/(1+ln(b)^2)+ln(b)*cos(x+k)*b^x/(1+ln(b)^2)","~contains([k,b],x)","integral of looping cosine"),
					new Rule("integrate(cos(a*x+k)*b^x,x)->a*sin(a*x+k)*b^x/(a^2+ln(b)^2)+ln(b)*cos(a*x+k)*b^x/(a^2+ln(b)^2)","~contains([a,k,b],x)","integral of looping cosine"),
			},"looping integrals");
			
			Rule recursivePowerOverSqrt = new Rule(new Rule[] {
				new Rule("integrate(x^n/sqrt(a*x+b),x)->(2*x^n*sqrt(a*x+b))/(a*(2*n+1))-(2*n*b*integrate(x^(n-1)/sqrt(a*x+b),x))/(a*(2*n+1))","~contains([n,a,b],x)","power over sqrt"),
				new Rule("integrate(x^n/sqrt(x+b),x)->(2*x^n*sqrt(x+b))/(2*n+1)-(2*n*b*integrate(x^(n-1)/sqrt(x+b),x))/(2*n+1)","~contains([n,b],x)","power over sqrt"),
				new Rule("integrate(x/sqrt(a*x+b),x)->(2*x*sqrt(a*x+b))/(a*3)-(2*b*integrate(1/sqrt(a*x+b),x))/(a*3)","~contains([a,b],x)","power over sqrt"),
				new Rule("integrate(x/sqrt(x+b),x)->(2*x*sqrt(x+b))/3-(2*b*integrate(1/sqrt(x+b),x))/3","~contains(b,x)","power over sqrt"),
			},"power over sqrt");
			
			Rule recursiveInvPowerOverSqrt = new Rule(new Rule[] {
					new Rule("integrate(1/(sqrt(a*x+b)*x^n),x)->(-sqrt(a*x+b))/((n-1)*b*x^(n-1))-(a*(2*n-3)*integrate(1/(sqrt(a*x+b)*x^(n-1)),x))/(2*b*(n-1))","~contains([a,b,n],x)","power over sqrt"),
					new Rule("integrate(1/(sqrt(a*x+b)*x),x)->ln(1-sqrt(a*x+b)/sqrt(b))/sqrt(b)-ln(1+sqrt(a*x+b)/sqrt(b))/sqrt(b)","~contains([a,b],x)&(comparison(b>0)|allowComplexNumbers())","power over sqrt"),
					new Rule("integrate(1/(sqrt(x+b)*x^n),x)->(-sqrt(x+b))/((n-1)*b*x^(n-1))-((2*n-3)*integrate(1/(sqrt(x+b)*x^(n-1)),x))/(2*b*(n-1))","~contains([b,n],x)","power over sqrt"),
					new Rule("integrate(1/(sqrt(x+b)*x),x)->ln(1-sqrt(x+b)/sqrt(b))/sqrt(b)-ln(1+sqrt(x+b)/sqrt(b))/sqrt(b)","~contains(b,x)&(comparison(b>0)|allowComplexNumbers())","power over sqrt"),
			},"1 over power times sqrt");
			
			Rule integralForArcsin = new Rule(new Rule[]{
					new Rule("integrate(1/sqrt(a-x^2),x)->asin(x/sqrt(a))","~contains(a,x)","simple integral leading to arcsin"),
					new Rule("integrate(1/sqrt(a+b*x^2),x)->asin((sqrt(-b)*x)/sqrt(a))/sqrt(-b)","(comparison(b<0)|allowComplexNumbers())&~contains([a,b],x)","simple integral leading to arcsin"),
			},"integrals leading to arcsin");
			
			Rule sinCosProdReduction = new Rule(new Rule[] {
					new Rule("integrate(sin(x)^m*cos(x)^n,x)->sin(x)^(m+1)*cos(x)^(n-1)/(m+n)+(n-1)/(m+n)*integrate(sin(x)^m*cos(x)^(n-2),x)","comparison(n>m)&isType(m,num)&isType(n,num)","trig reduction formula"),
					new Rule("integrate(sin(x)^m*cos(x)^n,x)->-sin(x)^(m-1)*cos(x)^(n+1)/(m+n)+(m-1)/(m+n)*integrate(sin(x)^(m-2)*cos(x)^n,x)","(comparison(m>n)|comparison(m=n))&isType(m,num)&isType(n,num)","trig reduction formula"),
					new Rule("integrate(1/(sin(x)^m*cos(x)^n),x)->1/((n-1)*sin(x)^(m-1)*cos(x)^(n-1))+(m+n-2)/(n-1)*integrate(1/(sin(x)^m*cos(x)^(n-2)),x)","comparison(n>m)&isType(m,num)&isType(n,num)","trig reduction formula"),
					new Rule("integrate(1/(sin(x)^m*cos(x)^n),x)->-1/((m-1)*sin(x)^(m-1)*cos(x)^(n-1))+(m+n-2)/(m-1)*integrate(1/(sin(x)^(m-2)*cos(x)^n),x)","(comparison(m>n)|comparison(m=n))&isType(m,num)&isType(n,num)","trig reduction formula"),
			},"product of sin and cos powers");
			
			Rule sinTanProdReduction = new Rule(new Rule[] {
					new Rule("integrate(sin(x)^m*tan(x)^n,x)->sin(x)^m*tan(x)^(n-1)/(n-1)-(n+m-1)/(n-1)*integrate(sin(x)^m*tan(x)^(n-2),x)","isType(m,num)&isType(n,num)","trig reduction formula"),
					new Rule("integrate(1/(sin(x)^m*tan(x)^n),x)->-1/((n+m-1)*tan(x)^(n-1)*sin(x)^m)-(n-1)/(n+m-1)*integrate(1/(sin(x)^m*tan(x)^(n-2)),x)","isType(m,num)&isType(n,num)","trig reduction formula"),
			},"product of sin and tan powers");
			
			Rule tanOverCosReduction = new Rule(new Rule[] {
					new Rule("integrate(tan(x)^m/cos(x)^n,x)->tan(x)^(m-1)/((n+m-1)*cos(x)^n)-(m-1)/(n+m-1)*integrate(tan(x)^(m-2)/cos(x)^n,x)","isType(m,num)&isType(n,num)","trig reduction formula"),
					new Rule("integrate(cos(x)^m/tan(x)^n,x)->-cos(x)^m/((n-1)*tan(x)^(n-1))-(m+n-1)/(n-1)*integrate(cos(x)^m/tan(x)^(n-2),x)","isType(m,num)&isType(n,num)","trig reduction formula"),
					
					new Rule("integrate(tan(x)^m/cos(x),x)->tan(x)^(m-1)/(m*cos(x))-(m-1)/m*integrate(tan(x)^(m-2)/cos(x),x)","isType(m,num)","trig reduction formula"),
					new Rule("integrate(cos(x)/tan(x)^n,x)->-cos(x)/((n-1)*tan(x)^(n-1))-n/(n-1)*integrate(cos(x)/tan(x)^(n-2),x)","isType(n,num)","trig reduction formula"),
			},"tan power over cos power");
			
			// these are the reverse process of diff(atan(x^n),x) -> (n-1)*x^(n-1)/(x^(2*n)+1) 
			Rule inverseQuadraticUSub = new Rule(new Rule[] {
					new Rule("integrate(x^a/(x^b+c),x)->atan(x^(a+1)/sqrt(c))/((a+1)*sqrt(c))","comparison(b/(a+1)=2)&(comparison(c>0)|allowComplexNumbers())&~contains([a,b,c],x)","inverse quadratic with u sub"),
					new Rule("integrate(x^a/(d*x^b+c),x)->atan((x^(a+1)*sqrt(d))/sqrt(c))/((a+1)*sqrt(d*c))","comparison(b/(a+1)=2)&(comparison(c*d>0)|allowComplexNumbers())&~contains([a,b,c,d],x)","inverse quadratic with u sub"),
					
					new Rule("integrate(x/(x^b+c),x)->atan(x^2/sqrt(c))/(2*sqrt(c))","comparison(b/2=2)&(comparison(c>0)|allowComplexNumbers())&~contains([b,c],x)","inverse quadratic with u sub"),
					new Rule("integrate(x/(d*x^b+c),x)->atan((x^2*sqrt(d))/sqrt(c))/(2*sqrt(d*c))","comparison(b/2=2)&(comparison(c*d>0)|allowComplexNumbers())&~contains([b,c,d],x)","inverse quadratic with u sub"),
			},"reverse to arctan with power");
			
			Rule inverseQuadraticToNReduction = new Rule("1 over quadratic to the n") {
				Expr ans;
				Expr invPow;
				
				@Override
				public void init() {
					ans = createExpr("(-2*a*x-b)/((n-1)*(b^2-4*a*c)*(a*x^2+b*x+c)^(n-1))-2*(2*n-3)*a/((n-1)*(b^2-4*a*c))*integrate(1/(a*x^2+b*x+c)^(n-1),x)");
					invPow = createExpr("1/q^n");
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func integ = (Func)e;
					
					if( !Rule.fastSimilarExpr(invPow,integ.get() )) return integ;
					Func denomPow = (Func)(((Func)integ.get()).getDenom());
					
					if(!isPositiveRealNum(denomPow.getExpo())) return integ;
					Num n = (Num)denomPow.getExpo();
					
					Sequence coef = polyExtract(denomPow.getBase() ,integ.getVar(),casInfo);
					if(coef == null || coef.size() != 3) return integ;
					Func equsSet = exprSet(  equ(var("a"),coef.get(2)) , equ(var("b"),coef.get(1)) , equ(var("c"),coef.get(0)) , equ(var("n"),n) , equ(var("x"),integ.getVar()) );
					
					return ans.replace(equsSet).simplify(casInfo);
				}
				
			};
			
			Rule integrationByParts = new Rule("integration by parts"){
				boolean isTypeExtended(Expr e,String typeName) {//is that type or a power with base of that type
					if(e.typeName().equals(typeName)) {
						return true;
					}else if(e.typeName().equals("power")) {
						Func casted = (Func)e;
						
						return casted.getExpo() instanceof Num && casted.getBase().typeName().equals(typeName);
					}
					return false;
				}
				
				final int OKAY = 0,GOOD = 1,GREAT = 2,BEST = 3;

				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func integ = (Func)e;
					if(!integ.get().containsType("integrate") && !isPolynomialUnstrict(integ.get(),integ.getVar())) {
						Func innerDiv = Div.cast(integ.get().copy());
						Prod innerProd = Prod.cast(innerDiv.getNumer());
						innerDiv.setNumer(innerProd);
						boolean denomIsFunc = innerDiv.getDenom().contains(integ.getVar());
						int bestIndex = -1;
						int confidence = -1;
						
						int fractionalPowerCount = 0;
						
						for(int i = 0;i < innerProd.size();i++) {
							int currentConfidence = -1;
							
							Expr current = innerProd.get(i);
							
							if(isTypeExtended(current,"ln") && !denomIsFunc){
								currentConfidence = BEST;
							}else if(isTypeExtended(current,"atan") || isTypeExtended(current,"asin") || isTypeExtended(current,"acos")){
								currentConfidence = GREAT;
							}else if(!denomIsFunc){//polynomial
								Func pow = Power.cast(current);
								if(isPlainPolynomial(pow.getBase(),integ.getVar()) && pow.getBase().contains(integ.getVar())) {
									if(isPositiveRealNum(pow.getExpo())){
										currentConfidence = GOOD;
									}else{//root
										//System.out.println(pow);
										Func frac = Div.cast(pow.getExpo());
										if(frac!=null && Div.isNumericalAndReal(frac)) {
											if(!degree(pow.getBase(),integ.getVar()).equals(BigInteger.ONE)) return integ;//dangerous because square root does not have linear term, derivative of a square root with non linear base only increase the complexity
											if(((Num)frac.getNumer()).getRealValue().signum() == 1) {
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
			
			Rule integrationByPartsSpecial = new Rule("special integration by parts"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func integ = (Func)e;
					if(integ.get().typeName().equals("div") && !integ.get().containsType("integrate")) {
						Func innerDiv = (Func)integ.get().copy();
						if(innerDiv.getDenom().typeName().equals("power")) {
							Func denomPower = (Func)innerDiv.getDenom();
							Func expo = Div.cast(denomPower.getExpo());
							if(Div.isNumericalAndReal(expo) && isPlainPolynomial(denomPower.getBase(),integ.getVar()) && degree(denomPower.getBase(),integ.getVar()).equals(BigInteger.ONE) ) {
								if( ((Num)expo.getNumer()).getRealValue().compareTo( ((Num)expo.getDenom()).getRealValue() )  == 1) {//make sure the fraction is greater than 1
									Expr integralOfDenom = integrate(inv(denomPower),integ.getVar()).simplify(casInfo);
									Expr derivativeOfNumer = diff(innerDiv.getNumer(),integ.getVar()).simplify(casInfo);
									if(!(derivativeOfNumer.typeName().equals("div") && ((Func)derivativeOfNumer).getDenom().contains(integ.getVar())  )) {
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
			
			Rule specialUSub = new Rule("special u sub, f(x)*diff(f(x),x)"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func integ = (Func)e;
						
					Func innerDiv = Div.cast(integ.get().copy());
					Prod innerProd = Prod.cast(innerDiv.getNumer());
					innerDiv.setNumer(innerProd);
					for(int i = 0;i<innerProd.size();i++) {
						Func divCopy = (Func)innerDiv.copy();
						
						Expr testExpr = divCopy.getNumer().get(i);
						if(!testExpr.contains(integ.getVar())) continue;
						
						divCopy.getNumer().remove(i);
						Expr resToCheck = div(divCopy,diff(testExpr,(Var)integ.getVar().copy()));
						resToCheck = resToCheck.simplify(casInfo);
						if(!resToCheck.contains(integ.getVar())) {
							Func res = div(prod(power(testExpr,num(2)),resToCheck),num(2));
							return res.simplify(casInfo);
						}
						
					}
					
					
					return integ;
				}
			};
			
			Rule normalUSub = new Rule("normal u sub"){
				public Expr getNextInnerFunction(Expr e,Var v) {
					if(e.size()>0 && e.contains(v)){
						if(e.typeName().equals("power") && !((Func)e).getBase().contains(v) && ((Func)e).getExpo() instanceof Prod) {//if in the form of a^(b*x) return a^x
							return power(((Func)e).getBase(),v);
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
				
				Var uSubVar;
				
				@Override
				public void init(){
					uSubVar = var("0u");
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func integ = (Func)e;
					
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
					}else if(integ.get().typeName().equals("div")) {
						Func castedDiv =  ((Func)integ.get());
						boolean logCase = !div(castedDiv.getNumer(),diff(castedDiv.getDenom() ,integ.getVar())).simplify(casInfo).contains(integ.getVar());
						
						if(logCase) {
							u = castedDiv.getDenom();
						}else if(!castedDiv.getNumer().contains(integ.getVar())) {
							u = castedDiv.getDenom();
						}else if(!castedDiv.getDenom().contains(integ.getVar())){
							u = castedDiv.getNumer();
						}else {
							u = castedDiv.getNumer().complexity() > castedDiv.getDenom().complexity() ? castedDiv.getNumer() : castedDiv.getDenom();
						}
					}else {
						u = integ.get();
					}
					
					if(u != null) {
						while(true) {//try normal u and innermost u sub
							if(!u.equals(integ.getVar())) {
								Func eq = equ(u,uSubVar);//im calling it 0u since variables normally can't start with number
								
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
									CasInfo singleSolutionModeCasInfo = new CasInfo(casInfo);
									singleSolutionModeCasInfo.setSingleSolutionMode(true);
									Expr solved = solve(equ(uSubVar,u),integ.getVar()).simplify(singleSolutionModeCasInfo);
									if(solved.typeName().equals("set")) {//just in case single solution mode accidently returns a list
										solved = solved.get();
									}
									if(!(solved.typeName().equals("solve"))) {
										solved = Equ.getRightSide(((Func)solved));
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
			Rule partialFraction = new Rule("partial fractions"){
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func integ = (Func)e;
					
					integ.set(0, partialFrac(integ.get(), integ.getVar(), casInfo) );
					if(integ.get() instanceof Sum){
						Expr out = StandardRules.linearOperator.applyRuleToExpr(integ, casInfo);
						return out;
					}
					return integ;
				}
			
			};
			
			Rule polyDiv = new Rule("polynomial division"){
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func integ = (Func)e;
					
					integ.set(0, polyDiv(integ.get(), integ.getVar(), casInfo) );
					if(integ.get() instanceof Sum){
						return StandardRules.linearOperator.applyRuleToExpr(integ, casInfo);
					}
					
					return integ;
				}
			
			};
			
			Rule psudoTrigSub = new Rule(new Rule[]{
					
					//x^n/sqrt(1-x^2) generalization
					new Rule("integrate(x^n/sqrt(a+b*x^2),x)->a^(n/2)/(-b)^((n+1)/2)*subst(integrate(sin(0k)^n,0k),0k=asin(sqrt(-b/a)*x))","(comparison(b<0)&comparison(a>0)&(comparison(-b<a)|comparison(-b=a))|allowComplexNumbers())&~contains([a,b],x)&isType(n,num)","trig sub"),
					//sqrt(1-x^2)/x^n generalization
					new Rule("integrate(sqrt(a+b*x^2)/x^n,x)->(-b)^((n-1)/2)/a^((n-2)/2)*subst(integrate(1/sin(0k)^n,0k)-integrate(1/sin(0k)^(n-2),0k),0k=asin(sqrt(-b/a)*x))","(comparison(b<0)&comparison(a>0)&(comparison(-b<a)|comparison(-b=a))|allowComplexNumbers())&~contains([a,b],x)&comparison(n>1)&isType(n,num)","trig sub"),
					
					//sqrt(1+x^2)/x^n generalization
					new Rule("integrate(sqrt(a*x^2+b)/x^n,x)->a^((n-1)/2)/b^((n-2)/2)*subst(integrate(1/(cos(0k)^3*tan(0k)^n),0k),0k=atan(sqrt(a/b)*x))","~contains([a,b],x)&(comparison(a>0)&comparison(b>0)|allowComplexNumbers())&isType(n,num)","trig sub"),
					new Rule("integrate(sqrt(x^2+b)/x^n,x)->1/b^((n-2)/2)*subst(integrate(1/(cos(0k)^3*tan(0k)^n),0k),0k=atan(x/sqrt(b)))","~contains(b,x)&(comparison(b>0)|allowComplexNumbers())&isType(n,num)","trig sub"),
					
					//x^n/sqrt(1+x^2) generalization
					new Rule("integrate(x^n/sqrt(a*x^2+b),x)->b^(n/2)/a^((n+1)/2)*subst(integrate(tan(0k)^n/cos(0k),0k),0k=atan(sqrt(a/b)*x))","~contains([a,b],x)&(comparison(a>0)&comparison(b>0)|allowComplexNumbers())&isType(n,num)","trig sub"),
					new Rule("integrate(x^n/sqrt(x^2+b),x)->b^(n/2)*subst(integrate(tan(0k)^n/cos(0k),0k),0k=atan(x/sqrt(b)))","~contains(b,x)&(comparison(b>0)|allowComplexNumbers())&isType(n,num)","trig sub"),
					
					//sqrt(x^2-1)/x^n generalization
					new Rule("integrate(sqrt(a*x^2+b)/x^n,x)->a^((n-1)/2)/(-b)^((n-2)/2)*subst(integrate(cos(0k)^(n-3)*sin(0k)^2,0k),0k=acos(sqrt(-b)/(sqrt(a)*x)))","~contains([a,b],x)&(comparison(a>0)&(comparison(-b<a)|comparison(-b=a))|allowComplexNumbers())&isType(n,num)","trig sub"),
					new Rule("integrate(sqrt(x^2+b)/x^n,x)->1/(-b)^((n-2)/2)*subst(integrate(cos(0k)^(n-3)*sin(0k)^2,0k),0k=acos(sqrt(-b)/x))","~contains(b,x)&(comparison(-b<1)|comparison(b=-1)|allowComplexNumbers())&isType(n,num)","trig sub"),
					
					//x^n/sqrt(x^2-1) generalization
					new Rule("integrate(x^n/sqrt(a*x^2+b),x)->(-b)^(n/2)/a^((n+1)/2)*subst(integrate(1/cos(0t)^(n+1),0t),0t=acos(sqrt(-b)/(sqrt(a)*x)))","~contains([a,b],x)&(comparison(a>0)&(comparison(-b<a)|comparison(-b=a))|allowComplexNumbers())&isType(n,num)",""),
					new Rule("integrate(x^n/sqrt(x^2+b),x)->(-b)^(n/2)*subst(integrate(1/cos(0t)^(n+1),0t),0t=acos(sqrt(-b)/x))","~contains(b,x)&(comparison(-b<1)|comparison(b=-1)|allowComplexNumbers())&isType(n,num)",""),
					
					new Rule("integrate(1/sqrt(a+b*x^2),x)->ln(x*sqrt(b)+sqrt(a+b*x^2))/(2*sqrt(b))-ln(sqrt(a+b*x^2)-x*sqrt(b))/(2*sqrt(b))","(comparison(a>0)&comparison(b>0)|allowComplexNumbers())&~contains([a,b],x)","trig sub"),
					new Rule("integrate(1/sqrt(a+x^2),x)->ln(x+sqrt(a+x^2))/2-ln(sqrt(a+x^2)-x)/2","(comparison(a>0)|allowComplexNumbers())&~contains(a,x)","trig sub"),
					
			},"psudo trig substitution");
			
			Rule sqrtOfQuadratic = new Rule("square root has quadratic") {
				Expr resultPos,resultNeg;
				Expr check;
				
				@Override
				public void init() {
					resultPos = createExpr("(b+2*a*x)*sqrt(a*x^2+b*x+c)/(4*a)+(4*a*c-b^2)*(ln(2*sqrt(a)*sqrt(a*x^2+b*x+c)+2*a*x+b)-ln(2))/(8*a^(3/2))");
					resultNeg = createExpr("(b+2*a*x)*sqrt(a*x^2+b*x+c)/(4*a)+(4*a*c-b^2)*asin((2*a*x+b)/sqrt(b^2-4*a*c))/(8*(-a)^(3/2))");
					check = createExpr("comparison(a>0)");
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func integ = (Func)e;
					if(Rule.fastSimilarExpr(sqrtObj, integ.get())) {
						//System.out.println(e);
						Sequence coefs = polyExtract(e.get().get(),integ.getVar(),casInfo);
						if(coefs == null) return integ;
						if(coefs.size() == 3) {
							Func equsSet = exprSet( equ(var("c"),coefs.get(0)) , equ(var("b"),coefs.get(1)) , equ(var("a"),coefs.get(2)) , equ(var("x"),integ.getVar()) );
							boolean aPositive = check.replace(equsSet).simplify(casInfo).equals(BoolState.TRUE);
							Expr out;
							if(aPositive) {
								out = resultPos.replace(equsSet).simplify(casInfo);
							}else {
								out = resultNeg.replace(equsSet).simplify(casInfo);
							}
							//System.out.println(out);
							return out;
						}
						
					}
					return integ;
				}
			};
			
			Rule fullExpandInner = new Rule("full expansion") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func integ = (Func)e;
					integ.set(0, SimpleFuncs.fullExpand.applyRuleToExpr(integ.get(), casInfo) );
					return integ;
				}
				
			};
			
			Rule weierstrassSub = new Rule("weierstrass substitution") {
				public Expr getSinOrCosInner(Expr e) {//search tree for inside of sin or cos
					if(e.typeName().equals("sin") || e.typeName().equals("cos")) return e.get();
					for(int i = 0;i<e.size();i++) {
						Expr inner = getSinOrCosInner(e.get(i));
						if(inner != null) return inner;
					}
					return null;
				}
				Func subsSet;
				Expr addedDeriv;
				@Override
				public void init() {
					subsSet = (Func)createExpr("{sin(0a)=(2*0t)/(1+0t^2),cos(0a)=(1-0t^2)/(1+0t^2)}");
					addedDeriv = createExpr("2/(0t^2+1)");
				}
				
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func integ = (Func)e;
					if(!( (integ.containsType("sin") || integ.containsType("cos")) && integ.containsType("sum") && !(integ.get() instanceof Sum) )) return integ;//needs sin or cos
					
					Expr innerTrig = getSinOrCosInner(integ.get());
					if(innerTrig == null) return integ;
					
					Func equ = equ(var("0a"),innerTrig);
					Func subsSet = (Func) this.subsSet.replace(equ);
					Expr addedDeriv = this.addedDeriv.replace(equ);
					
					Expr newInner = div(prod(integ.get().replace(subsSet),addedDeriv),diff(innerTrig,integ.getVar()));
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
			
			Rule shouldExpand = new Rule("should expand") {//there is a power with trig function in base sum. Should expand as it saves alot of time
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func integ = (Func)e;
					if(integ.get().typeName().equals("power")) {
						Func inner = (Func)integ.get();
						
						if(inner.getExpo() instanceof Num && inner.getBase() instanceof Sum && containsTrig(inner.getBase()) ) {
							integ.set(0, SimpleFuncs.fullExpand.applyRuleToExpr(integ.get(), casInfo) );
						}
					}
					return integ;
				}
			};
			
			Rule linearOverQuadratic = new Rule("linear over quadratic integration") {
				Expr kPosCase;
				Expr k;
				Expr kNegCase;
				
				@Override
				public void init() {
					k = createExpr("0b^2-4*0a*0c");
					kPosCase = createExpr("ln(0a*x^2+0b*x+0c)/(2*0a)+0b*ln(sqrt(0k)+2*0a*x+0b)/(2*0a*sqrt(0k))-0b*ln(sqrt(0k)-2*0a*x-0b)/(2*0a*sqrt(0k))");
					kNegCase = createExpr("ln(0a*x^2+0b*x+0c)/(2*0a)-0b*atan((2*0a*x+0b)/sqrt(-0k))/(0a*sqrt(-0k))");
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func integ = (Func)e;
					
					Var v = integ.getVar();
					if(!(integ.get().typeName().equals("div"))) return e;
					
					Func innerDiv = (Func)integ.get();
					if(!( innerDiv.getNumer().equals(v) && degree(innerDiv.getDenom(),v).equals(BigInteger.TWO) )) return e;
					Sequence poly = polyExtract(innerDiv.getDenom(),v,casInfo);
					if(poly == null) return e;
					Expr c = poly.get(0),b = poly.get(1),a = poly.get(2);
					Func subTableSet = exprSet(equ(var("0a"),a),equ(var("0b"),b),equ(var("0c"),c),equ(var("x"),v));

					Expr k = this.k.replace(subTableSet).simplify(casInfo);
					
					subTableSet.add(equ(var("0k"),k));
					
					if(comparison(equGreater(k,num(0))).simplify(casInfo).equals(BoolState.TRUE)) {
						return this.kPosCase.replace(subTableSet).simplify(casInfo);
					}
					
					return this.kNegCase.replace(subTableSet).simplify(casInfo);
				}
				
			};
			
			Rule inverseQuadratic = new Rule("inverse quadratic"){//robust
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
					
					Func integ = (Func)e;
					
					if(integ.get().typeName().equals("div") && !((Func)integ.get()).getNumer().contains(integ.getVar())) {
						Expr denom = ((Func)integ.get()).getDenom();
						Sequence poly = polyExtract(denom, integ.getVar(), casInfo);
						if(poly != null && poly.size() == 3) {
							Expr c = poly.get(0),b = poly.get(1),a = poly.get(2);
							Func subTableSet = exprSet(equ(var("0a"),a),equ(var("0b"),b),equ(var("0c"),c),equ(var("x"),integ.getVar()));
							
							Expr check = this.check.replace(subTableSet).simplify(casInfo);
							
							subTableSet.add(equ(var("0k"),check));
							if(!check.negative()) {//check is negative if the quadratic contains no roots thus using arctan
								return this.arctanCase.replace(subTableSet).simplify(casInfo);
							}
							//otherwise use logarithms
							return this.logCase.replace(subTableSet).simplify(casInfo);
						}
					}
					return integ;
					
				}
				
			};
			
			owner.behavior.rule = new Rule(new Rule[]{
					
					StandardRules.pullOutConstants,
					polynomial,
					logCases,
					linearOverQuadratic,
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
			},"main sequence");
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return Cas.integrateOver(Cas.floatExpr(smallRandNum),owner.getVar(),owner.get(),owner.getVar()).convertToFloat(varDefs);
				}
			};
			
			owner.behavior.doneRule = new Rule("after integration cleanup") {
				Expr applyAbs(Expr e,CasInfo casInfo) {
					if(e.typeName().equals("ln") && !(e.get().typeName().equals("abs"))) return ln(abs(e.get())).simplify(casInfo);
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
		}
	};
}
