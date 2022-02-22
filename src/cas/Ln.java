package cas;
import java.math.BigInteger;


public class Ln extends Expr{
	
	
	private static final long serialVersionUID = 8168024064884459716L;
	static Rule log1To0 = new Rule("ln(1)->0","log of 1",Rule.VERY_EASY);
	static Rule logETo1 = new Rule("ln(e)->1","log of e",Rule.VERY_EASY);
	static Rule powToProd = new Rule("ln(a^b)->b*ln(a)","log of power",Rule.EASY);
	static Rule lnOfEpsilon = new Rule("ln(epsilon)->-inf","log of epsilon",Rule.EASY);
	static Rule lnOfInf = new Rule("ln(inf)->inf","log of infinity",Rule.EASY);
	
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
	
	static Rule logOfInverse = new Rule("ln(1/x)->-ln(x)","log of inverse becomes negative log",Rule.VERY_EASY);
	static Rule logOfInverse2 = new Rule("ln((-1)/x)->-ln(-x)","log of inverse becomes negative log",Rule.VERY_EASY);
	
	
	static Rule logWithSums = new Rule("remove sums from within logs",Rule.UNCOMMON) {//the goal is to remove sums inside of logs if they are part of a product or divisin
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Ln log = (Ln)e;
			
			if(log.get() instanceof Prod || log.get() instanceof Div) {

				Sum out = new Sum();
				Div div = Div.cast(log.get());
				
				Prod prodNumer = Prod.cast(div.getNumer());
				Prod prodDenom = Prod.cast(div.getDenom());
				
				for(int i = 0;i<prodNumer.size();i++) {
					if(prodNumer.get(i) instanceof Sum || prodNumer.get(i) instanceof Power && ((Power)prodNumer.get(i)).getBase() instanceof Sum) {
						out.add(ln(prodNumer.get(i)));
						prodNumer.remove(i);
						i--;
					}
				}
				for(int i = 0;i<prodDenom.size();i++) {
					if(prodDenom.get(i) instanceof Sum || prodDenom.get(i) instanceof Power && ((Power)prodDenom.get(i)).getBase() instanceof Sum) {
						out.add(neg(ln(prodNumer.get(i))));
						prodDenom.remove(i);
						i--;
					}
				}
				
				
				if(out.size()>0) {
					div.setNumer(prodNumer);
					div.setDenom(prodDenom);
					log.set(0, div);
					
					out.add(log);
					return out.simplify(settings);
				}
			}
			
			return log;
		}
		
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				log1To0,
				logETo1,
				lnOfEpsilon,
				lnOfInf,
				lnOfEpsilonSum,
				logOfInverse,
				logOfInverse2,
				StandardRules.factorInner,
				logWithSums,
				logOfPerfectPower,
				powToProd
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	Sequence getRuleSequence() {
		return ruleSequence;
	}
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.ln(get().convertToFloat(varDefs));
	}

}
