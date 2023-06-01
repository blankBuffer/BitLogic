package cas.algebra;
import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.primitive.Div;
import cas.primitive.Num;
import cas.primitive.Power;
import cas.primitive.Prod;

public class Gcd{
	
	public static Func.FuncLoader gcdLoader = new Func.FuncLoader() {
		@Override
		public void load(Func owner) {
			owner.behavior.helpMessage = "Get the greatest common diviser amongst a set of expressions.\n"
					+ "For example, gcd(9,6) returns 3\n"
					+ "It can do more than numbers like, gcd(8*x^2,6*x^3) returns 2*x^2\n";
			
			owner.behavior.commutative = true;
			
			Rule gcdRule = new Rule("get the greatest common divisor") {
				Expr smallGcd(Expr a,Expr b) {//gcd of just two elements
					Func subGcdProd = prod();
					
					Func aSepSequence = seperateCoef(a);
					a =	aSepSequence.get(1);
					Func bSepSequence = seperateCoef(b);
					b =	bSepSequence.get(1);
					
					Func fracCoefA = Div.cast(aSepSequence.get());
					Func fracCoefB = Div.cast(bSepSequence.get());
					
					subGcdProd.add(Div.unCast(div( num(((Num)fracCoefA.getNumer()).gcd().gcd(((Num)fracCoefB.getNumer()).gcd())) ,num(((Num)fracCoefA.getDenom()).gcd().gcd(((Num)fracCoefB.getDenom()).gcd())) )));
					
					if(a.isType("div") || b.isType("div")) {
						Func aDiv = Div.cast(a);
						Func bDiv = Div.cast(b);
						
						return Div.unCast(div( smallGcd(aDiv.getNumer(),bDiv.getNumer()),smallGcd(aDiv.getDenom(),bDiv.getDenom()) ));
					}
					
					a = Prod.cast(a);
					b = Prod.cast(b);
					
					for(int i = 0;i<a.size();i++) {
						Expr aExpr = a.get(i);
						Func aPow = Power.cast(aExpr);
						for(int j = 0;j<b.size();j++) {
							Expr bExpr = b.get(j);
							Func bPow = Power.cast(bExpr);
							if(aPow.getBase().equals(bPow.getBase())) {
								
								if(aPow.getExpo().equals(bPow.getExpo())) {
									subGcdProd.add(Power.unCast(aPow));
								}else if( isPositiveRealNum(aPow.getExpo()) && isPositiveRealNum(bPow.getExpo()) ){
									subGcdProd.add(Power.unCast( power(aPow.getBase(), num(((Num)aPow.getExpo()).getRealValue().min(((Num)bPow.getExpo()).getRealValue()))) ));
								}else if(aPow.getExpo().isType("div") || bPow.getExpo().isType("div")) {
									Func aExpo = Div.cast(aPow.getExpo());
									Func bExpo = Div.cast(bPow.getExpo());
									
									if(Div.isNumericalAndReal(aExpo) && Div.isNumericalAndReal(bExpo)) {
										int comparison = ((Num)aExpo.getNumer()).getRealValue().multiply(((Num)bExpo.getDenom()).getRealValue()).compareTo(((Num)bExpo.getNumer()).getRealValue().multiply(((Num)aExpo.getDenom()).getRealValue()));
										
										if(comparison == -1) {
											subGcdProd.add(a.get(i));
										}else if(comparison == 1) {
											subGcdProd.add(b.get(j));
										}
										
									}
									
								}
								
							}
						}
					}
					//remove ones
					for(int i = 0;i<subGcdProd.size();i++) {
						if(subGcdProd.get(i).equals(Num.ONE)) {
							subGcdProd.remove(i);
							i--;
						}
					}
					return Prod.unCast(subGcdProd);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func gcd = (Func)e;
					
					if(gcd.size() == 0) return Num.ONE;
					else if(gcd.size() == 1) return gcd.get();
					
					for(int i = 0;i<gcd.size();i++) {
						if(gcd.get(i).isType("sum")) {
							Func subSum = (Func)gcd.get(i);
							Func subGcd = gcd();
							
							for(int j = 0;j<subSum.size();j++) {
								subGcd.add(subSum.get(j));
							}
							
							gcd.set(i, subGcd.simplify(casInfo));
						}
					}
					
					Expr currentGcd = gcd.get(0);
					for(int i = 1;i<gcd.size();i++) {
						currentGcd = smallGcd(currentGcd,gcd.get(i));
						if(currentGcd.equals(Num.ONE)) break;
					}
					return currentGcd.simplify(casInfo);
				}
				
			};
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					if(owner.size() == 0) {
						return ComplexFloat.ONE;
					}else {
						ComplexFloat currentGcd = owner.get().convertToFloat(varDefs);
						for(int i = 1;i<owner.size();i++) {
							currentGcd = ComplexFloat.gcd(currentGcd, owner.get(i).convertToFloat(varDefs));
						}
						return currentGcd;
					}
				}
			};
			
			owner.behavior.rule = new Rule(new Rule[] {
					gcdRule
			},"main sequence");
		}
	};

}
