package cas;

public class IntegrateOver extends Expr {

	private static final long serialVersionUID = -418375860392765107L;
	
	Expr getMin() {
		return get(0);
	}
	Expr getMax() {
		return get(1);
	}
	Expr getExpr() {
		return get(2);
	}
	
	@Override
	public Var getVar() {
		return (Var)get(3);
	}
	
	IntegrateOver(){}//
	public IntegrateOver(Expr min,Expr max,Expr e,Var v){
		add(min);
		add(max);
		add(e);
		add(v);
	}
	
	static Rule definiteIntegral = new Rule("integral with bounds",Rule.EASY){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			IntegrateOver defInt = (IntegrateOver)e;
			
			Expr indefInt = integrate(defInt.getExpr(),defInt.getVar()).simplify(settings);
			
			if(!indefInt.containsType("integrate")) {
				return sub(indefInt.replace(equ(  defInt.getVar() , defInt.getMax() )),indefInt.replace(equ(  defInt.getVar() , defInt.getMin() ))).simplify(settings);
			}
			
			return defInt;
		}
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				definiteIntegral		
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	Sequence getRuleSequence() {
		return ruleSequence;
	}
	
	@Override
	public String toString() {
		String out = "";
		out+="integrateOver(";
		out+=getMin().toString();
		out+=',';
		out+=getMax().toString();
		out+=',';
		out+=getExpr().toString();
		out+=',';
		out+=getVar().toString();
		out+=')';
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {//using simpson's rule
		int n = 32;//should be an even number
		ComplexFloat sum = new ComplexFloat(0,0);
		ComplexFloat min = getMin().convertToFloat(varDefs),max = getMax().convertToFloat(varDefs);
		ComplexFloat step = ComplexFloat.div( ComplexFloat.sub(max, min),new ComplexFloat(n,0));
		
		Equ vDef = equ(getVar(),floatExpr(min));
		ExprList varDefs2 = (ExprList) varDefs.copy();
		
		for(int i = 0;i < varDefs2.size();i++) {
			Equ temp = (Equ)varDefs2.get(i);
			Var v = (Var)temp.getLeftSide();
			if(v.equals(getVar())) {
				varDefs2.remove(i);
				break;
			}
		}
		varDefs2.add(vDef);
		
		sum = ComplexFloat.add(sum, getExpr().convertToFloat(varDefs2));
		((FloatExpr)vDef.getRightSide()).value = ComplexFloat.add(((FloatExpr)vDef.getRightSide()).value, step);
		
		for(int i = 1;i<n;i++) {
			sum=ComplexFloat.add(sum,  ComplexFloat.mult( new ComplexFloat(((i%2)*2+2),0) ,getExpr().convertToFloat(varDefs2)) );
			((FloatExpr)vDef.getRightSide()).value = ComplexFloat.add(((FloatExpr)vDef.getRightSide()).value, step);
		}
		
		sum=ComplexFloat.add(sum, getExpr().convertToFloat(varDefs2));
		
		return ComplexFloat.mult(sum,ComplexFloat.mult(step, new ComplexFloat(1.0/3.0,0)));
		
	}

}
