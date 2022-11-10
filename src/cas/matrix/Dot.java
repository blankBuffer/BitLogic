package cas.matrix;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;
import cas.primitive.Func;
import cas.primitive.Prod;
import cas.primitive.Sequence;
import cas.primitive.Sum;
import cas.primitive.Var;

public class Dot extends Expr{
	public Dot(){}//
	
	static Rule matrixMult = new Rule("matrix multiplication") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Dot dot = (Dot)e;
			
			if(dot.get(0) instanceof Mat) {
				Mat total = (Mat)dot.get(0);
				
				for(int i = 1;i < dot.size();i++) {
					Mat other = (Mat)dot.get(i);
					
					Mat newMat = mat(total.rows(),other.cols());
					
					for(int row = 0; row < newMat.rows();row++) {
						for(int col = 0; col < newMat.cols();col++) {
							Sequence totalRow = total.getRow(row);
							Sequence otherCol = other.getCol(col);
							
							Sum sum = new Sum();
							for(int j = 0;j<totalRow.size();j++) {
								sum.add( prod(totalRow.get(j),otherCol.get(j)) );
							}
							newMat.setElement(row, col, sum);
							
						}
					}
					
					total = newMat;
				}
				return total.simplify(casInfo);
			}
			
			return dot;
		}
	};
	
	static Rule dotContainsDot = new Rule("dot contains dot") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Dot dot = (Dot)e;
			
			for(int i = 0;i<dot.size();i++) {
				if(dot.get(i) instanceof Dot) {
					Dot subDot = (Dot)dot.get(i);
					dot.remove(i);
					for(int j = 0;j<subDot.size();j++) dot.add(i+j, subDot.get(j));
					
					i+=subDot.size()-1;
				}
			}
			
			return dot;
		}
		
	};
	
	static Rule mainSequenceRule = null;

	public static void loadRules(){
		mainSequenceRule = new Rule(new Rule[]{
				dotContainsDot,
				matrixMult
		},"main sequence");
		mainSequenceRule.init();
	}
	
	@Override
	public Rule getRule() {
		return mainSequenceRule;
	}
	
	@Override
	public String toString() {
		String out = "";
		for(int i = 0;i<size();i++) {
			boolean paren = !(get(i) instanceof Var) && (get(i) instanceof Mat) && !(get(i) instanceof Prod) && !(get(i) instanceof Func);
			if(paren) out+="(";
			out+=get(i);
			if(paren) out+=")";
			if(i != size()-1) out += ".";
		}
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		return new ComplexFloat(0,0);
	}
	
	@Override
	public String typeName() {
		return "dot";
	}

	@Override
	public String help() {
		return ". opertator\n"
				+ "examples\n"
				+ "(mat({{1,3},{5,8}})).(mat({{8,2},{1,5}}))->mat({{11,17},{48,50}})\n"
				+ "transpose(x.y)->transpose(y).transpose(x)";
	}
}
