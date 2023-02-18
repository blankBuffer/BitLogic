package cas.matrix;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
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
							Func totalRowSequence = total.getRow(row);
							Func otherColSequence = other.getCol(col);
							
							Func sum = sum();
							for(int j = 0;j<totalRowSequence.size();j++) {
								sum.add( prod(totalRowSequence.get(j),otherColSequence.get(j)) );
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
			boolean paren = !(get(i) instanceof Var) && (get(i) instanceof Mat) && !(get(i).typeName().equals("prod")) && !(get(i) instanceof Func);
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
