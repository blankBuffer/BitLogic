package cas;

import java.math.BigInteger;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.base.StandardRules;
import cas.primitive.*;

public class Trig {
	public static Func.FuncLoader sinLoader = new Func.FuncLoader(){
		@Override
		public void load(Func owner) {
			
			Rule sinOfArctan = new Rule("sin(atan(x))->x/sqrt(1+x^2)","sin of arctan");
			Rule sinOfAsin = new Rule("sin(asin(x))->x","sin contains inverse");
			Rule sinOfAcos = new Rule("sin(acos(x))->sqrt(1-x^2)","sin of arccos");
			
			Rule sinOfEpsilon = new Rule("sin(epsilon)->epsilon","sin of epsion");
			
			Rule unitCircle = new Rule("unit circle for sine"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func sin = (Func) e;
					
					BigInteger three = BigInteger.valueOf(3),six = BigInteger.valueOf(6),four = BigInteger.valueOf(4),twelve = BigInteger.valueOf(12);
					
					Expr innerExpr = sin.get();
					if(innerExpr.equals(num(0))) {
						return num(0);
					}else if(innerExpr.equals(Var.PI)) {
						return num(0);
					}else if(innerExpr.typeName().equals("prod") && innerExpr.size() == 2) {
						if(innerExpr.get(1).equals(Var.PI) && isRealNum(innerExpr.get(0))) {
							return num(0);
						}else if(innerExpr.get(0).equals(Var.PI) && isRealNum(innerExpr.get(1))) {
							return num(0);
						}
					}
					if(innerExpr.typeName().equals("div") && innerExpr.contains(Var.PI)){
						Func frac = Div.ratioOfUnitCircle((Func)innerExpr);
						
						if(frac != null) {
							BigInteger numer = ((Num)frac.getNumer()).getRealValue(),denom = ((Num)frac.getDenom()).getRealValue();//getting numerator and denominator
							
							numer = numer.mod(denom.multiply(BigInteger.TWO));//restrict to the main circle
							int negate = 1;
							
							if(numer.compareTo(denom) == 1) {//if the numerator is greater than the denominator
								negate = -negate;
								numer = numer.mod(denom);//if we go past the top part of the circle we can flip it back to the top and keep track of the negative
							}
							
							if(numer.compareTo(denom.divide(BigInteger.TWO)) == 1) {//if we are past the first part of the quarter circle, we can restrict it further
								numer = denom.subtract(numer);//basically reflecting across the y axis
							}
							
							if(numer.equals(BigInteger.ONE) && denom.equals(BigInteger.TWO)) return num(negate);
							else if(numer.equals(BigInteger.ONE) && denom.equals(three)) return div(sqrt(num(3)),num(2*negate));
							else if(numer.equals(BigInteger.ONE) && denom.equals(six)) return inv(num(2*negate));
							else if(numer.equals(BigInteger.ONE) && denom.equals(four)) return div(sqrt(num(2)),num(2*negate));
							else if(numer.equals(BigInteger.ONE) && denom.equals(twelve)) return div(sub(sqrt(num(2)),sqrt(num(6))),num(-4));
							else if(numer.equals(BigInteger.ZERO)) return num(0);
							else {
								if(negate == -1) {
									return neg(sin(div(prod(pi(),num(numer)),num(denom)).simplify(CasInfo.normal)));
								}
								return sin(div(prod(pi(),num(numer)),num(denom)).simplify(CasInfo.normal));
							}
							
							
						}
						
					}else if(innerExpr.typeName().equals("sum")) {//sin(x-pi/4) can be turned into sin(x+7*pi/4) because sin has symmetry
						for(int i = 0;i<innerExpr.size();i++) {
							if(innerExpr.get(i).typeName().equals("div") && !innerExpr.get(i).containsVars() && innerExpr.get(i).contains(Var.PI)) {
								
								Func frac = Div.ratioOfUnitCircle((Func)innerExpr.get(i));
								
								if(frac!=null) {
									BigInteger numer = ((Num)frac.getNumer()).getRealValue(),denom = ((Num)frac.getDenom()).getRealValue();
									
									if(denom.signum() == -1){
										denom = denom.negate();
										numer = numer.negate();
									}
									
									numer = numer.mod(denom.multiply(BigInteger.TWO));//to do this we take the mod
									
									if(numer.equals(BigInteger.ONE) && denom.equals(BigInteger.TWO)) {//sin(x+pi/2) = cos(x)
										innerExpr.remove(i);
										return cos(innerExpr).simplify(CasInfo.normal);
									}else if(numer.equals(three) && denom.equals(BigInteger.TWO)) {
										innerExpr.remove(i);
										return neg(cos(innerExpr.simplify(CasInfo.normal)));
									}
									
									innerExpr.set(i,  div(prod(num(numer),pi()),num(denom)) );
									sin.set(0, innerExpr.simplify(CasInfo.normal));
									
								}
								
							}
						}
					}
					return sin;
				}
				
			};
			
			owner.behavior.rule = new Rule(new Rule[]{
				sinOfEpsilon,
				sinOfArctan,
				sinOfAsin,
				sinOfAcos,
				StandardRules.oddFunction,
				StandardRules.distrInner,
				unitCircle
			},"main sequence");
			
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.sin(owner.get().convertToFloat(varDefs));
				}
			};
		}
	};
	
	public static Func.FuncLoader cosLoader = new Func.FuncLoader() {
		@Override
		public void load(Func owner) {
			Rule cosOfArctan = new Rule("cos(atan(x))->1/sqrt(1+x^2)","cos or arctan");
			Rule cosOfArcsin = new Rule("cos(asin(x))->sqrt(1-x^2)","cos of arcsin");
			Rule cosOfArccos = new Rule("cos(acos(x))->x","cos of arccos");
			
			Rule cosOfEpsilon = new Rule("cos(epsilon)->1-epsilon","cos of epsilon");
			
			Rule cosOfAbs = new Rule("cos(abs(x))->cos(x)","cos of abs");
			
			Rule unitCircle = new Rule("unit circle for cos"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func cos = (Func)e;
					BigInteger three = BigInteger.valueOf(3),six = BigInteger.valueOf(6),four = BigInteger.valueOf(4),twelve = BigInteger.valueOf(12);
					Expr innerExpr = distr(cos.get()).simplify(casInfo);
					
					Expr out = cos;
					if(innerExpr.equals(num(0))) {
						out = num(1);
					}else if(innerExpr.equals(Var.PI)) {
						out = num(-1);
					}else if(innerExpr.typeName().equals("prod") && innerExpr.size() == 2) {
						if(innerExpr.get(1).equals(Var.PI) && isRealNum(innerExpr.get(0)) ) {
							return ((Num)innerExpr.get(0)).getRealValue().mod(BigInteger.TWO).equals(BigInteger.ZERO) ? num(1) : num(-1);
						}else if(innerExpr.get(0).equals(Var.PI) && isRealNum(innerExpr.get(1))) {
							return ((Num)innerExpr.get(1)).getRealValue().mod(BigInteger.TWO).equals(BigInteger.ZERO) ? num(1) : num(-1);
						}
					}if(innerExpr.typeName().equals("div") && innerExpr.contains(Var.PI)){
						Func frac = Div.ratioOfUnitCircle((Func)innerExpr);
						
						if(frac!=null) {
							
							BigInteger numer = ((Num)frac.getNumer()).getRealValue(),denom = ((Num)frac.getDenom()).getRealValue();
							
							numer = numer.mod(denom.multiply(BigInteger.TWO));//restrict to whole circle
							int negate = 1;
							
							if(numer.compareTo(denom) == 1) {//if we are past the top half flip over x axis
								numer = BigInteger.TWO.multiply(denom).subtract(numer);
							}
							
							if(numer.compareTo(denom.divide(BigInteger.TWO)) == 1) {//if we are past quarter circle, reflect over y axis and flip sign
								numer = denom.subtract(numer);
								negate = -negate;
							}
							
							if(numer.equals(BigInteger.ONE) && denom.equals(BigInteger.TWO)) out = num(0);
							else if(numer.equals(BigInteger.ONE) && denom.equals(three)) out = inv(num(2*negate));
							else if(numer.equals(BigInteger.ONE) && denom.equals(six)) out = div(sqrt(num(3)),num(2*negate));
							else if(numer.equals(BigInteger.ONE) && denom.equals(four)) out = div(sqrt(num(2)),num(2*negate));
							else if(numer.equals(BigInteger.ONE) && denom.equals(twelve)) return div(sum(sqrt(num(6)),sqrt(num(2))),num(4));
							else if(numer.equals(BigInteger.ZERO)) out = num(negate);
							else {
								//make it into the sin version for canonical form
								out = prod(  num(negate),   sin(sum( div(prod(pi(),num(numer)),num(denom)) ,div(pi(),num(2))))   ).simplify(casInfo);
								//
								
							}
							
							
						}
						
					}else if(innerExpr.typeName().equals("sum")) {//cos(x-pi/4) can be turned into sin(x+7*pi/4) because sin has symmetry
						for(int i = 0;i<innerExpr.size();i++) {
							if(innerExpr.get(i).typeName().equals("div") && !innerExpr.get(i).containsVars() && innerExpr.get(i).contains(Var.PI)) {
								Func frac = Div.ratioOfUnitCircle((Func)innerExpr.get(i));
								
								if(frac!=null) {
									//make it into the sin version for canonical form
									out = sin(sum( innerExpr ,div(pi(),num(2)))).simplify(casInfo);
									//
								}
								
							}
						}
					}
					return out;
				}
			};
			owner.behavior.rule = new Rule(new Rule[]{
					cosOfEpsilon,
					cosOfArctan,
					cosOfArcsin,
					cosOfArccos,
					cosOfAbs,
					StandardRules.evenFunction,
					StandardRules.distrInner,
					unitCircle
			},"main sequence");
			
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.cos(owner.get().convertToFloat(varDefs));
				}
			};
		}
	};
	
	public static Func.FuncLoader tanLoader = new Func.FuncLoader(){
		@Override
		public void load(Func owner) {
			
			Rule containsInverse = new Rule("tan(atan(x))->x","contains inverse");
			Rule tanOfArcsin = new Rule("tan(asin(x))->x/sqrt(1-x^2)","tan of arcsin");
			Rule tanOfArccos = new Rule("tan(acos(x))->sqrt(1-x^2)/x","tan of arccos");
			
			Rule unitCircle = new Rule("unit circle for tan"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func tan = (Func)e;
					
					BigInteger three = BigInteger.valueOf(3),six = BigInteger.valueOf(6),four = BigInteger.valueOf(4),five = BigInteger.valueOf(5),twelve = BigInteger.valueOf(12);
					
					Expr innerExpr = tan.get();
					if(innerExpr.equals(num(BigInteger.ZERO)) || innerExpr.equals(Var.PI)) {
						return num(0);
					}else if(innerExpr.typeName().equals("prod") && innerExpr.size() == 2) {
						if(innerExpr.get(1).equals(Var.PI) && isRealNum(innerExpr.get(0))) {
							return num(0);
						}else if(innerExpr.get(0).equals(Var.PI) && isRealNum(innerExpr.get(1))) {
							return num(0);
						}
					}if(innerExpr.typeName().equals("div") && innerExpr.contains(Var.PI)){
						Func frac = Div.ratioOfUnitCircle((Func)innerExpr);
						
						if(frac!=null) {
							BigInteger numer = ((Num)frac.getNumer()).getRealValue(),denom = ((Num)frac.getDenom()).getRealValue();
							
							numer = numer.mod(denom);//we take the mod since it repeats in circle
							
							
							if(numer.equals(BigInteger.ONE) && denom.equals(four)) return num(1);
							else if(numer.equals(BigInteger.ONE) && denom.equals(three)) return sqrt(num(3));
							else if(numer.equals(BigInteger.ONE) && denom.equals(twelve)) return sub(num(2),sqrt(num(3)));
							else if(numer.equals(BigInteger.ONE) && denom.equals(six)) return div(sqrt(num(3)),num(3));
							else if(numer.equals(BigInteger.TWO) && denom.equals(three)) return neg(sqrt(num(3)));
							else if(numer.equals(three) && denom.equals(four)) return num(-1);
							else if(numer.equals(five) && denom.equals(six)) return div(sqrt(num(3)),num(-3));
							else if(numer.equals(BigInteger.ZERO)) return num(0);
							else {
								int negate = 1;
								if(numer.compareTo(denom.divide(BigInteger.TWO)) == 1) {
									numer = denom.subtract(numer);
									negate = -1;
								}
								
								if(negate == -1) {
									return neg(tan(div(prod(pi(),num(numer)),num(denom)).simplify(CasInfo.normal)));
								}
								return tan(div(prod(pi(),num(numer)),num(denom)).simplify(CasInfo.normal));
							}
							
						}
					}else if(innerExpr.typeName().equals("sum")){
						for(int i = 0;i<innerExpr.size();i++) {
							if(innerExpr.get(i).typeName().equals("div") && !innerExpr.get(i).containsVars() && innerExpr.get(i).contains(Var.PI)) {
								
								Func frac = Div.ratioOfUnitCircle((Func)innerExpr.get(i));
								
								if(frac!=null) {
									
									BigInteger numer = ((Num)frac.getNumer()).getRealValue(),denom = ((Num)frac.getDenom()).getRealValue();
									
									numer = numer.mod(denom);//to do this we take the mod
									
									innerExpr.set(i,  div(prod(num(numer),pi()),num(denom)) );
									tan.set(0, innerExpr.simplify(CasInfo.normal));
									
								}
								
							}
						}
					}
					
					return tan;
				}
				
			};
			
			owner.behavior.rule = new Rule(new Rule[]{
					containsInverse,
					tanOfArcsin,
					tanOfArccos,
					StandardRules.oddFunction,
					StandardRules.distrInner,
					unitCircle
			},"main sequence");
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.tan(owner.get().convertToFloat(varDefs));
				}
			};
		}
		
	};
	
	public static Func.FuncLoader atanLoader = new Func.FuncLoader() {
		@Override
		public void load(Func owner) {
			
			Rule containsInverse = new Rule("atan(tan(x))->x","arctan of tan");
			
			Rule inverseUnitCircle = new Rule(new Rule[]{
					new Rule("atan(0)->0","arctan of zero"),
					new Rule("atan(1)->pi/4","arctan of one"),
					new Rule("atan(sqrt(3))->pi/3","arctan of root 3"),
					new Rule("atan(sqrt(3)/3)->pi/6","arctan of root 3 over 3"),
					new Rule("atan(inf)->pi/2-epsilon","arctan of infinity"),
			},"asin unit circle");
			
			owner.behavior.rule = new Rule(new Rule[]{
					StandardRules.trigCompressInner,
					StandardRules.oddFunction,
					containsInverse,
					inverseUnitCircle
			},"main sequence");
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.atan(owner.get().convertToFloat(varDefs));
				}
			};
		}
	};
	
	public static Func.FuncLoader acosLoader = new Func.FuncLoader() {
		Rule containsInverse = new Rule("acos(cos(x))->x","acos contains inverse");
		Rule containsSin = new Rule("acos(sin(x))->-x+pi/2","acos contains inverse");
		
		Rule negativeInner = new Rule("arccos of negative value"){
			@Override
			public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
				Func acos = (Func)e;
				if(acos.get().negative()){
					Expr result = sum(neg(acos( neg(acos.get()).simplify(casInfo) )),pi());
					return result.simplify(casInfo);
				}
				return acos;
			}
		};
		
		Rule inverseUnitCircle = new Rule(new Rule[]{
				new Rule("acos(0)->pi/2","arccos of zero"),
				new Rule("acos(1)->0","arccos of one"),
				new Rule("acos(sqrt(2)/2)->pi/4","arccos of root 2 over 2"),
				new Rule("acos(1/2)->pi/3","arccos of a half"),
				new Rule("acos(sqrt(3)/2)->pi/6","arccos of root 3 over 2"),
		},"unit circle for arccos");
		
		Rule arccosWithSqrt = new Rule(new Rule[]{
				new Rule("acos(sqrt(a*x+b)/c)->asin((c^2-2*a*x-2*b)/c^2)/2+pi/4","arcsin with square root"),
				new Rule("acos(sqrt(x+b)/c)->asin((c^2-2*x-2*b)/c^2)/2+pi/4","arcsin with square root"),
				new Rule("acos(sqrt(a*x+b))->asin(1-2*a*x-2*b)/2+pi/4","arcsin with square root"),
				new Rule("acos(sqrt(x+b))->asin(1-2*x-2*b)/2+pi/4","arcsin with square root"),
		},"arcsin with square root");
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule(new Rule[]{
					StandardRules.trigCompressInner,
					negativeInner,
					arccosWithSqrt,
					containsInverse,
					containsSin,
					inverseUnitCircle
			},"main sequence");
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.acos(owner.get().convertToFloat(varDefs));
				}
			};
		}
	};
	
	public static Func.FuncLoader asinLoader = new Func.FuncLoader() {
		Rule asinSinCase = new Rule("asin(sin(x))->x","arcsin of the sin");
		Rule asinCosCase = new Rule("asin(cos(x))->-x+pi/2","arcsin of cosine");

		Rule inverseUnitCircle = new Rule(new Rule[]{
				new Rule("asin(0)->0","arcsin of zero"),
				new Rule("asin(1)->pi/2","arcsin of one"),
				new Rule("asin(sqrt(2)/2)->pi/4","arcsin of root 2 over 2"),
				new Rule("asin(1/2)->pi/6","arcsin of a half"),
				new Rule("asin(sqrt(3)/2)->pi/3","arcsin of root 3 over 2"),
		},"asin unit circle");
		
		Rule arcsinWithSqrt = new Rule(new Rule[]{
				new Rule("asin(sqrt(a*x+b)/c)->asin((c^2-2*a*x-2*b)/c^2)/-2+pi/4","arcsin with square root"),
				new Rule("asin(sqrt(x+b)/c)->asin((c^2-2*x-2*b)/c^2)/-2+pi/4","arcsin with square root"),
				new Rule("asin(sqrt(a*x+b))->asin(1-2*a*x-2*b)/-2+pi/4","arcsin with square root"),
				new Rule("asin(sqrt(x+b))->asin(1-2*x-2*b)/-2+pi/4","arcsin with square root"),
		},"arcsin with square root");
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule(new Rule[]{
					StandardRules.trigCompressInner,
					StandardRules.oddFunction,
					arcsinWithSqrt,
					asinSinCase,
					asinCosCase,
					inverseUnitCircle
			},"main sequence");
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.asin(owner.get().convertToFloat(varDefs));
				}
			};
			
		}
	};
}
