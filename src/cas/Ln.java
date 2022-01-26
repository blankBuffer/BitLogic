package cas;
import java.math.BigInteger;


public class Ln extends Expr{
	
	
	private static final long serialVersionUID = 8168024064884459716L;
	static Rule log1To0 = new Rule("ln(1)=0","log of 1",Rule.VERY_EASY);
	static Rule logETo1 = new Rule("ln(e)=1","log of e",Rule.VERY_EASY);
	static Rule powToProd = new Rule("ln(a^b)=b*ln(a)","log of power",Rule.EASY);
	static Rule lnOfEpsilon = new Rule("ln(epsilon)=-inf","log of epsilon",Rule.EASY);
	static Rule lnOfInf = new Rule("ln(inf)=inf","log of infinity",Rule.EASY);
	
	static Rule lnOfEpsilonSum = new Rule("log of sum with epsilon",Rule.UNCOMMON){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Ln ln = null;
			if(e instanceof Ln){
				ln = (Ln)e;
			}else{
				return e;
			}
			
			if(ln.get() instanceof Sum){
				Expr inner = ln.get();
				short direction = Limit.getDirection(inner);
				
				if(direction != Limit.NONE){
					ln.set(0, Limit.stripDirection(inner));
					return Limit.applyDirection(e, direction);
				}
				
			}
			
			return ln;
		}
	};
	
	Ln(){}//
	public Ln(Expr e){
		add(e);
	}
	
	static Rule logOfPerfectPower = new Rule("log of a perfect power",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Ln log = (Ln)e;
			
			if(log.get() instanceof Num) {// example log(25) -> 2*ln(5)
				Num casted = (Num)log.get();
				Power perfectPower = perfectPower(casted);
				if(((Num)perfectPower.getExpo()).realValue.equals(BigInteger.ONE)) return log;
				
				log.set(0, perfectPower);
			}else if(log.get() instanceof Prod) {//ln(8*x) -> ln(2^3*x) , this will be reverted in later steps
				Prod innerProd = (Prod)log.get();
				for(int i = 0;i<innerProd.size();i++) {
					if(innerProd.get(i) instanceof Num) {
						Num casted = (Num)innerProd.get(i);
						
						Power perfectPower = perfectPower(casted);
						if(((Num)perfectPower.getExpo()).realValue.equals(BigInteger.ONE)) continue;
						innerProd.set(i, perfectPower);
						
					}
				}
				
			}
			return log;
		}
	};

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.ln(get().convertToFloat(varDefs));
	}
	
	static ExprList ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = exprList(
				log1To0,
				logETo1,
				lnOfEpsilon,
				lnOfInf,
				lnOfEpsilonSum,
				logOfPerfectPower,
				powToProd
		);
	}
	
	@Override
	ExprList getRuleSequence() {
		return ruleSequence;
	}

}
