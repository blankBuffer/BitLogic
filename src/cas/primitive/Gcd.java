package cas.primitive;
import cas.*;

public class Gcd{
	
	public static Func.FuncLoader gcdLoader = new Func.FuncLoader() {
		@Override
		public void load(Func owner) {
			owner.behavior.commutative = true;
			
			Rule gcdRule = new Rule("get the greatest common divisor") {
				Expr smallGcd(Expr a,Expr b) {//gcd of just two elements
					Prod subGcd = new Prod();
					
					Sequence aSep = seperateCoef(a);
					a =	aSep.get(1);
					Sequence bSep = seperateCoef(b);
					b =	bSep.get(1);
					
					Func fracCoefA = Div.cast(aSep.get());
					Func fracCoefB = Div.cast(bSep.get());
					
					subGcd.add(Div.unCast(div( num(((Num)fracCoefA.getNumer()).gcd().gcd(((Num)fracCoefB.getNumer()).gcd())) ,num(((Num)fracCoefA.getDenom()).gcd().gcd(((Num)fracCoefB.getDenom()).gcd())) )));
					
					if(a.typeName().equals("div") || b.typeName().equals("div")) {
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
									subGcd.add(Power.unCast(aPow));
								}else if( isPositiveRealNum(aPow.getExpo()) && isPositiveRealNum(bPow.getExpo()) ){
									subGcd.add(Power.unCast( power(aPow.getBase(), num(((Num)aPow.getExpo()).getRealValue().min(((Num)bPow.getExpo()).getRealValue()))) ));
								}else if(aPow.getExpo().typeName().equals("div") || bPow.getExpo().typeName().equals("div")) {
									Func aExpo = Div.cast(aPow.getExpo());
									Func bExpo = Div.cast(bPow.getExpo());
									
									if(Div.isNumericalAndReal(aExpo) && Div.isNumericalAndReal(bExpo)) {
										int comparison = ((Num)aExpo.getNumer()).getRealValue().multiply(((Num)bExpo.getDenom()).getRealValue()).compareTo(((Num)bExpo.getNumer()).getRealValue().multiply(((Num)aExpo.getDenom()).getRealValue()));
										
										if(comparison == -1) {
											subGcd.add(a.get(i));
										}else if(comparison == 1) {
											subGcd.add(b.get(j));
										}
										
									}
									
								}
								
							}
						}
					}
					//remove ones
					for(int i = 0;i<subGcd.size();i++) {
						if(subGcd.get(i).equals(Num.ONE)) {
							subGcd.remove(i);
							i--;
						}
					}
					return Prod.unCast(subGcd);
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func gcd = (Func)e;
					
					if(gcd.size() == 0) return Num.ONE;
					else if(gcd.size() == 1) return gcd.get();
					
					for(int i = 0;i<gcd.size();i++) {
						if(gcd.get(i) instanceof Sum) {
							Sum subSum = (Sum)gcd.get(i);
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
