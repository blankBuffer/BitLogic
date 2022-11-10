package cas.primitive;
import cas.*;

public class Gcd extends Expr{
	public Gcd() {
	}//
	
	@Override
	public boolean isCommutative(){
		return true;
	}
	
	static Rule gcdRule = new Rule("get the greatest common divisor") {
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
			Gcd gcd = (Gcd)e;
			
			if(gcd.size() == 1) return gcd.get();
			
			for(int i = 0;i<gcd.size();i++) {
				if(gcd.get(i) instanceof Sum) {
					Sum subSum = (Sum)gcd.get(i);
					Gcd subGcd = new Gcd();
					
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
	
	static Rule mainSequenceRule = null;
	
	public static void loadRules(){
		mainSequenceRule = new Rule(new Rule[]{
			gcdRule
		},"main sequence");
		mainSequenceRule.init();
	}
	
	@Override
	public Rule getRule() {
		return mainSequenceRule;
	}

	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		return new ComplexFloat(0,0);
	}

	@Override
	public String typeName() {
		return "gcd";
	}

	@Override
	public String help() {
		return "gcd(a,b) is the greatest common divisor computer\n"
				+ "examples\n"
				+ "gcd(4*x^2*y,2*x*y)->2*x*y\n"
				+ "gcd(10,4)->2";
	}

}
