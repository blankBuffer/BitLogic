package cas.matrix;

import cas.base.CasInfo;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;

public class Transpose{
	
	public static Func.FuncLoader transposeLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			Rule transContainsTrans = new Rule("transpose contains transpose"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					if(e.get().typeName().equals("transpose")) {
						return e.get().get();
					}
					return e;
				}
			};
			Rule transOfSum = new Rule("tranpose of sum"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					if(e.get().typeName().equals("sum")) {
						Func innerSum = (Func)e.get();
						for(int i = 0;i<innerSum.size();i++) {
							innerSum.set(i, transpose(innerSum.get(i)) );
						}
						return innerSum.simplify(casInfo);
					}
					return e;
				}
			};
			Rule transOfProd = new Rule("tranpose of product"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					if(e.get().typeName().equals("prod")) {
						Func innerProd = (Func)e.get();
						for(int i = 0;i<innerProd.size();i++) {
							innerProd.set(i, transpose(innerProd.get(i)) );
						}
						return innerProd.simplify(casInfo);
					}
					return e;
				}
			};
			Rule transOfDotMult = new Rule("tranpose of dot multiply"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					if(e.get().typeName().equals("dot")) {
						Func innerDotMult = (Func)e.get();
						Func outDot = dot();
						for(int i = innerDotMult.size()-1;i>=0;i--) {
							outDot.add(transpose(innerDotMult.get(i)) );
						}
						return outDot.simplify(casInfo);
					}
					return e;
				}
			};
			Rule transOfMatrix = new Rule("matrix transpose"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					if(e.get().typeName().equals("mat")) {
						Func mat = (Func)e.get();
						
						Func newRowsSequence = sequence();
						for(int i = 0;i<Mat.cols(mat);i++) {
							newRowsSequence.add(Mat.getCol(mat,i));
						}
						
						return mat(newRowsSequence);
					}
					return e;
				}
			};
			
			owner.behavior.rule = new Rule(new Rule[]{
				transContainsTrans,
				transOfSum,
				transOfProd,
				transOfDotMult,
				transOfMatrix
			},"main sequence");
		}
	};
}
