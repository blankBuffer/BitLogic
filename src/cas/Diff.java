package cas;

public class Diff extends Expr{
	
	private static final long serialVersionUID = -4094192362094389130L;
	
	static Rule baseCase = new Rule("diff(x,x)->1","derivative of x",Rule.VERY_EASY);
	static Rule logCase = new Rule("diff(ln(a),x)->diff(a,x)/a","derivative of log",Rule.EASY);
	static Rule powCase = new Rule("diff(a^b,x)->a^b*diff(ln(a)*b,x)","power rule",Rule.EASY);
	static Rule sinCase = new Rule("diff(sin(a),x)->cos(a)*diff(a,x)","derivative of sine",Rule.UNCOMMON);
	static Rule cosCase = new Rule("diff(cos(a),x)->-sin(a)*diff(a,x)","derivative of cosine",Rule.UNCOMMON);
	static Rule tanCase = new Rule("diff(tan(a),x)->diff(a,x)/cos(a)^2","derivative of tangent",Rule.UNCOMMON);
	static Rule atanCase = new Rule("diff(atan(a),x)->diff(a,x)/(a^2+1)","derivative of arctan",Rule.UNCOMMON);
	static Rule asinCase = new Rule("diff(asin(a),x)->diff(a,x)/sqrt(1-a^2)","derivative of arctan",Rule.UNCOMMON);
	static Rule acosCase = new Rule("diff(acos(a),x)->(-diff(a,x))/sqrt(1-a^2)","derivative of arctan",Rule.UNCOMMON);
	static Rule divCase = new Rule("diff(a/b,x)->(diff(a,x)*b-a*diff(b,x))/(b^2)","derivative of division",Rule.TRICKY);
	static Rule lambertWCase = new Rule("diff(lambertW(a),x)->lambertW(a)/(a*lambertW(a)+a)*diff(a,x)","derivative of division",Rule.TRICKY);
	
	static Rule constant = new Rule("derivative of a constant",Rule.VERY_EASY){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Diff d = (Diff)e;
			if(!d.get().contains(d.getVar())){
				Expr out = num(0);
				return out;
			}
			return d;
		}
		
	};
	static Rule derivOfProd = new Rule("derivative of a product",Rule.TRICKY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Diff d = (Diff)e;
			if(d.get() instanceof Prod){
				Prod inner = (Prod)d.get();
				
				Sum out = new Sum();
				for(int i = 0;i<inner.size();i++){
					Expr copy = inner.copy();
					copy.set(i, diff(copy.get(i),d.getVar()));
					out.add(copy);
				}
				return out.simplify(settings);
				
			}
			return d;
		}
		
	};
	
	static ExprList ruleSequence = null;
	
	@Override
	ExprList getRuleSequence() {
		return ruleSequence;
	}
	
	public static void loadRules(){
		ruleSequence = exprList(
				constant,
				StandardRules.pullOutConstants,
				baseCase,
				StandardRules.linearOperator,
				derivOfProd,
				logCase,
				powCase,
				sinCase,
				cosCase,
				tanCase,
				atanCase,
				asinCase,
				acosCase,
				divCase,
				lambertWCase
			);
		Rule.initRules(ruleSequence);
	}
	
	Diff(){}//
	public Diff(Expr e,Var v){
		add(e);
		add(v);
	}
	
	@Override
	public Var getVar() {
		return (Var)get(1);
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		ComplexFloat delta = new ComplexFloat(1.0/Integer.MAX_VALUE,1.0/Integer.MAX_VALUE);
		ComplexFloat y0 = get().convertToFloat(varDefs);
		
		ExprList varDefs2 = (ExprList) varDefs.copy();
		
		for(int i = 0;i < varDefs2.size();i++) {
			Equ temp = (Equ)varDefs2.get(i);
			Var v = (Var)temp.getLeftSide();
			if(v.equals(getVar())) {
				((FloatExpr)temp.getRightSide()).value.set(ComplexFloat.add(((FloatExpr)temp.getRightSide()).value, delta));
				break;
			}
		}
		ComplexFloat y1 = get().convertToFloat(varDefs2);
		
		return ComplexFloat.div((ComplexFloat.sub(y1, y0)),delta);
	}
	
}
