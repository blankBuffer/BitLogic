package cas.primitive;
import cas.*;

public class Gcd extends Expr{
	private static final long serialVersionUID = -4998956713872579420L;

	public Gcd() {
		commutative = true;
	}//
	
	static Rule gcdRule = new Rule("get the greatest common divisor") {
		private static final long serialVersionUID = 1L;
		
		Expr smallGcd(Expr a,Expr b) {//gcd of just two elements
			Prod subGcd = new Prod();
			
			Sequence aSep = seperateCoef(a);
			a =	aSep.get(1);
			Sequence bSep = seperateCoef(b);
			b =	bSep.get(1);
			
			Div fracCoefA = Div.cast(aSep.get());
			Div fracCoefB = Div.cast(bSep.get());
			
			subGcd.add(div( num(((Num)fracCoefA.getNumer()).gcd().gcd(((Num)fracCoefB.getNumer()).gcd())) ,num(((Num)fracCoefA.getDenom()).gcd().gcd(((Num)fracCoefB.getDenom()).gcd())) ));
			
			if(a instanceof Div || b instanceof Div) {
				Div aDiv = Div.cast(a);
				Div bDiv = Div.cast(b);
				
				return Div.unCast(div( smallGcd(aDiv.getNumer(),bDiv.getNumer()),smallGcd(aDiv.getDenom(),bDiv.getDenom()) ));
			}
			
			a = Prod.cast(a);
			b = Prod.cast(b);
			
			for(int i = 0;i<a.size();i++) {
				Expr aExpr = a.get(i);
				Power aPow = Power.cast(aExpr);
				for(int j = 0;j<b.size();j++) {
					Expr bExpr = b.get(j);
					Power bPow = Power.cast(bExpr);
					if(aPow.getBase().equals(bPow.getBase())) {
						
						if(aPow.getExpo().equals(bPow.getExpo())) {
							subGcd.add(Power.unCast(aPow));
						}else if( isPositiveRealNum(aPow.getExpo()) && isPositiveRealNum(bPow.getExpo()) ){
							subGcd.add(Power.unCast( pow(aPow.getBase(), num(((Num)aPow.getExpo()).realValue.min(((Num)bPow.getExpo()).realValue))) ));
						}else if(aPow.getExpo() instanceof Div || bPow.getExpo() instanceof Div) {
							Div aExpo = Div.cast(aPow.getExpo());
							Div bExpo = Div.cast(bPow.getExpo());
							
							if(aExpo.isNumericalAndReal() && bExpo.isNumericalAndReal()) {
								int comparison = ((Num)aExpo.getNumer()).realValue.multiply(((Num)bExpo.getDenom()).realValue).compareTo(((Num)bExpo.getNumer()).realValue.multiply(((Num)aExpo.getDenom()).realValue));
								
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
			
			return Prod.unCast(subGcd);
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Gcd gcd = (Gcd)e;
			
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
	
	static Sequence ruleSequence;
	
	public static void loadRules() {
		ruleSequence = sequence(gcdRule);
	}

	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

	@Override
	public String typeName() {
		return "gcd";
	}

}
