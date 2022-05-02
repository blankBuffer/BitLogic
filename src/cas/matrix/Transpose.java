package cas.matrix;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;
import cas.primitive.ExprList;
import cas.primitive.Prod;
import cas.primitive.Sequence;
import cas.primitive.Sum;

public class Transpose extends Expr{
	private static final long serialVersionUID = 197607904901661059L;
	
	public Transpose(){}//
	
	public Transpose(Expr e) {
		add(e);
	}

	static Rule transContainsTrans = new Rule("transpose contains transpose"){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			if(e.get() instanceof Transpose) {
				return e.get().get();
			}
			return e;
		}
	};
	static Rule transOfSum = new Rule("tranpose of sum"){
		private static final long serialVersionUID = 1L;
		
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
		private static final long serialVersionUID = 1L;
		
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
		private static final long serialVersionUID = 1L;
		
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
		private static final long serialVersionUID = 1L;
		
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
	
	static Sequence ruleSequence;
	
	public static void loadRules() {
		ruleSequence = sequence(
				transContainsTrans,
				transOfSum,
				transOfProd,
				transOfDotMult,
				transOfMatrix
				
		);
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
