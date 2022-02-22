package cas;
import java.math.BigInteger;

public class Tan extends Expr{
	
	private static final long serialVersionUID = -2282985074053649819L;

	static Rule containsInverse = new Rule("tan(atan(x))->x","contains inverse",Rule.VERY_EASY);
	static Rule tanOfArcsin = new Rule("tan(asin(x))->x/sqrt(1-x^2)","tan of arcsin",Rule.UNCOMMON);
	static Rule tanOfArccos = new Rule("tan(acos(x))->sqrt(1-x^2)/x","tan of arccos",Rule.UNCOMMON);
	
	Tan(){}//
	public Tan(Expr a) {
		add(a);
	}
	
	public static Rule unitCircle = new Rule("unit circle for tan",Rule.TRICKY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Tan tan = (Tan)e;
			
			Var pi = pi();
			BigInteger three = BigInteger.valueOf(3),six = BigInteger.valueOf(6),four = BigInteger.valueOf(4),five = BigInteger.valueOf(5),twelve = BigInteger.valueOf(12);
			
			Expr innerExpr = tan.get();
			if(innerExpr.equals(num(BigInteger.ZERO)) || innerExpr.equals(pi)) {
				return num(0);
			}if(innerExpr instanceof Div && innerExpr.contains(pi())){
				Div frac = ((Div)innerExpr).ratioOfUnitCircle();
				
				if(frac!=null) {
					BigInteger numer = ((Num)frac.getNumer()).realValue,denom = ((Num)frac.getDenom()).realValue;
					
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
							return neg(tan(div(prod(pi(),num(numer)),num(denom)).simplify(Settings.normal)));
						}
						return tan(div(prod(pi(),num(numer)),num(denom)).simplify(Settings.normal));
					}
					
				}
			}else if(innerExpr instanceof Sum){
				for(int i = 0;i<innerExpr.size();i++) {
					if(innerExpr.get(i) instanceof Div && !innerExpr.get(i).containsVars() && innerExpr.get(i).contains(pi)) {
						
						Div frac = ((Div)innerExpr.get(i)).ratioOfUnitCircle();
						
						if(frac!=null) {
							
							BigInteger numer = ((Num)frac.getNumer()).realValue,denom = ((Num)frac.getDenom()).realValue;
							
							numer = numer.mod(denom);//to do this we take the mod
							
							innerExpr.set(i,  div(prod(num(numer),pi()),num(denom)) );
							tan.set(0, innerExpr.simplify(Settings.normal));
							
						}
						
					}
				}
			}
			
			return tan;
		}
		
	};
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.tan(get().convertToFloat(varDefs));
	}
	static Sequence ruleSequence = null;
	public static void loadRules(){
		ruleSequence = sequence(
				containsInverse,
				tanOfArcsin,
				tanOfArccos,
				StandardRules.oddFunction,
				StandardRules.distrInner,
				unitCircle
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	Sequence getRuleSequence() {
		return ruleSequence;
	}

}
