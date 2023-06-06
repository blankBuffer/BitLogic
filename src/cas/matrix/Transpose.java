package cas.matrix;

import cas.base.CasInfo;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;

import static cas.Cas.*;

public class Transpose{
	
	public static Func.FuncLoader transposeLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.helpMessage = "Transpose a matrix, swaps rows and columns.\n"
					+ "Example, transpose(mat([[1,2],[3,4]])) returns mat([[1,3],[2,4]])";
			
			Rule transContainsTrans = new Rule("transpose contains transpose"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					if(e.get().isType("transpose")) {
						return e.get().get();
					}
					return e;
				}
			};
			Rule transOfSum = new Rule("transpose of a sum"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					if(e.get().isType("sum")) {
						Func innerSum = (Func)e.get();
						for(int i = 0;i<innerSum.size();i++) {
							innerSum.set(i, transpose(innerSum.get(i)) );
						}
						return innerSum.simplify(casInfo);
					}
					return e;
				}
			};
			Rule transOfProd = new Rule("transpose of a product"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					if(e.get().isType("prod")) {
						Func innerProd = (Func)e.get();
						for(int i = 0;i<innerProd.size();i++) {
							innerProd.set(i, transpose(innerProd.get(i)) );
						}
						return innerProd.simplify(casInfo);
					}
					return e;
				}
			};
			Rule transOfDotMult = new Rule("transpose of dot multiply"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					if(e.get().isType("dot")) {
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
					if(e.get().isType("mat")) {
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
