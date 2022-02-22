package cas;

public class Abs extends Expr{
	private static final long serialVersionUID = 2865185687344371868L;
	Abs() {}//
	
	public Abs(Expr e) {
		add(e);
	}
	
	static Rule absOfPower = new Rule("abs(a^b)->abs(a)^b","abs of a power",Rule.EASY);
	
	static Rule absOfProd = new Rule("contains product",Rule.UNCOMMON) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Abs abs = (Abs)e;
			
			if(abs.get() instanceof Prod) {
				Prod innerProd = (Prod)abs.get();
				
				for(int i = 0;i<innerProd.size();i++) {
					innerProd.set(i, abs(innerProd.get(i)) );
				}
				
				return innerProd.simplify(settings);
			}
			
			return abs;
		}
	};
	
	static Rule absOfDiv = new Rule("abs(a/b)->abs(a)/abs(b)","abs of a division",Rule.UNCOMMON);
	
	static Rule absOfNum = new Rule("abs of a number",Rule.VERY_EASY) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings) {
			Abs abs = (Abs)e;
			
			if(abs.get() instanceof Num) {
				Num num = (Num)abs.get();
				
				if(num.isComplex()) {
					return sqrt( num(  num.realValue.pow(2).add(num.imagValue.pow(2))  ) ).simplify(settings);
				}
				return num(num.realValue.abs());
			}
			
			return abs;
		}
	};

	static Sequence ruleSequence;
	public static void loadRules(){
		ruleSequence = sequence(
				absOfNum,
				absOfPower,
				absOfProd,
				absOfDiv
		);
		Rule.initRules(ruleSequence);
	}
	@Override
	Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.mag(get().convertToFloat(varDefs));
	}

}
