package cas.matrix;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;
import cas.primitive.Func;
import cas.primitive.Prod;
import cas.primitive.Sequence;
import cas.primitive.Sum;

public class Transpose extends Expr{
	public Transpose(){}//
	
	public Transpose(Expr e) {
		add(e);
	}

	static Rule transContainsTrans = new Rule("transpose contains transpose"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			if(e.get() instanceof Transpose) {
				return e.get().get();
			}
			return e;
		}
	};
	static Rule transOfSum = new Rule("tranpose of sum"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			if(e.get() instanceof Sum) {
				Sum innerSum = (Sum)e.get();
				for(int i = 0;i<innerSum.size();i++) {
					innerSum.set(i, transpose(innerSum.get(i)) );
				}
				return innerSum.simplify(casInfo);
			}
			return e;
		}
	};
	static Rule transOfProd = new Rule("tranpose of product"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			if(e.get() instanceof Prod) {
				Prod innerProd = (Prod)e.get();
				for(int i = 0;i<innerProd.size();i++) {
					innerProd.set(i, transpose(innerProd.get(i)) );
				}
				return innerProd.simplify(casInfo);
			}
			return e;
		}
	};
	static Rule transOfDotMult = new Rule("tranpose of dot multiply"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			if(e.get() instanceof Dot) {
				Dot innerDotMult = (Dot)e.get();
				Dot out = new Dot();
				for(int i = innerDotMult.size()-1;i>=0;i--) {
					out.add(transpose(innerDotMult.get(i)) );
				}
				return out.simplify(casInfo);
			}
			return e;
		}
	};
	static Rule transOfMatrix = new Rule("matrix transpose"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			if(e.get() instanceof Mat) {
				Mat mat = (Mat)e.get();
				
				Sequence newRows = new Sequence();
				for(int i = 0;i<mat.cols();i++) {
					newRows.add(mat.getCol(i));
				}
				
				return mat(newRows);
			}
			return e;
		}
	};
	
	static Rule mainSequenceRule = null;
	
	public static void loadRules(){
		mainSequenceRule = new Rule(new Rule[]{
			transContainsTrans,
			transOfSum,
			transOfProd,
			transOfDotMult,
			transOfMatrix
		},"main sequence");
		mainSequenceRule.init();
	}
	
	@Override
	public Rule getRule() {
		return mainSequenceRule;
	}
	
	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		return ComplexFloat.ZERO;
	}

	@Override
	public String typeName() {
		return "transpose";
	}

	@Override
	public String help() {
		return "transpose(x) is matrix transpose computer\n"
				+ "examples\n"
				+ "transpose(mat({{1,2},{3,4}}))->mat({{1,3},{2,4}})\n"
				+ "transpose(a.b)->transpose(b).transpose(a)";
	}
}
