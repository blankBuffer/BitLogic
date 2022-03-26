package cas.calculus;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;
import cas.StandardRules;
import cas.primitive.Equ;
import cas.primitive.ExprList;
import cas.primitive.FloatExpr;
import cas.primitive.Prod;
import cas.primitive.Sequence;
import cas.primitive.Sum;
import cas.primitive.Var;

public class Diff extends Expr{
	
	private static final long serialVersionUID = -4094192362094389130L;
	
	static Rule baseCase = new Rule("diff(x,x)->1","derivative of x");
	static Rule logCase = new Rule("diff(ln(a),x)->diff(a,x)/a","derivative of log");
	static Rule rootCase = new Rule("diff(a^(k/n),x)->(diff(a,x)*k)/(a^((-k+n)/n)*n)","~contains({k,n},x)","derivative of root");
	static Rule invRootCase = new Rule("diff(1/a^(k/n),x)->(-diff(a,x)*k)/(a^((k+n)/n)*n)","~contains({k,n},x)","derivative of root");
	static Rule powCase = new Rule("diff(a^b,x)->a^b*diff(ln(a)*b,x)","power rule");
	static Rule sinCase = new Rule("diff(sin(a),x)->cos(a)*diff(a,x)","derivative of sine");
	static Rule cosCase = new Rule("diff(cos(a),x)->-sin(a)*diff(a,x)","derivative of cosine");
	static Rule tanCase = new Rule("diff(tan(a),x)->diff(a,x)/cos(a)^2","derivative of tangent");
	static Rule atanCase = new Rule("diff(atan(a),x)->diff(a,x)/(a^2+1)","derivative of arctan");
	static Rule asinCase = new Rule("diff(asin(a),x)->diff(a,x)/sqrt(1-a^2)","derivative of arctan");
	static Rule acosCase = new Rule("diff(acos(a),x)->(-diff(a,x))/sqrt(1-a^2)","derivative of arctan");
	static Rule divCase = new Rule("diff(a/b,x)->(diff(a,x)*b-a*diff(b,x))/(b^2)","derivative of division");
	static Rule lambertWCase = new Rule("diff(lambertW(a),x)->lambertW(a)/(a*lambertW(a)+a)*diff(a,x)","derivative of division");
	static Rule absCase = new Rule("diff(abs(a),x)->(abs(a)*diff(a,x))/a","derivative of absolute values");
	
	static Rule constant = new Rule("derivative of a constant"){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Diff d = (Diff)e;
			if(!d.get().contains(d.getVar())){
				Expr out = num(0);
				return out;
			}
			return d;
		}
		
	};
	static Rule derivOfProd = new Rule("derivative of a product"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Diff d = (Diff)e;
			if(d.get() instanceof Prod){
				Prod inner = (Prod)d.get();
				
				Sum out = new Sum();
				for(int i = 0;i<inner.size();i++){
					Expr copy = inner.copy();
					copy.set(i, diff(copy.get(i),d.getVar()));
					out.add(copy);
				}
				return out.simplify(casInfo);
				
			}
			return d;
		}
		
	};
	
	static Sequence ruleSequence = null;
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}
	
	public static void loadRules(){
		ruleSequence = sequence(
				constant,
				StandardRules.pullOutConstants,
				baseCase,
				StandardRules.linearOperator,
				derivOfProd,
				logCase,
				rootCase,
				invRootCase,
				powCase,
				sinCase,
				cosCase,
				tanCase,
				atanCase,
				asinCase,
				acosCase,
				divCase,
				lambertWCase,
				absCase
			);
		Rule.initRules(ruleSequence);
	}
	
	public Diff(){}//
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
		
		ComplexFloat equRightSideVal = null;
		Expr expr = get();
		
		for(int i = 0;i < varDefs.size();i++) {//search for definition
			Equ equ = (Equ)varDefs.get(i);
			Var v = (Var)equ.getLeftSide();
			if(v.equals(getVar())) {
				equRightSideVal = ((FloatExpr)equ.getRightSide()).value;//found!
				break;
			}
		}
		if(equRightSideVal == null) return new ComplexFloat(0,0);
		ComplexFloat delta = new ComplexFloat(Math.abs(equRightSideVal.real)/Short.MAX_VALUE,0);
		
		ComplexFloat y0 = expr.convertToFloat(varDefs);
		equRightSideVal.set( ComplexFloat.add(equRightSideVal, delta) );//add delta
		ComplexFloat y1 = expr.convertToFloat(varDefs);
		
		ComplexFloat slope = ComplexFloat.div((ComplexFloat.sub(y1, y0)),delta);
		return slope;
	}
	
	@Override
	public String typeName() {
		return "diff";
	}
}
