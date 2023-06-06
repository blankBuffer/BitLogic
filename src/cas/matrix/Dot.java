package cas.matrix;

import cas.base.CasInfo;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;

import static cas.Cas.*;

public class Dot{
	
	public static Func.FuncLoader dotLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			
			owner.behavior.helpMessage = "Dot multiplication.\n"
					+ "Used to do dot procucts on vectors and to multiply matricies.\n"
					+ "Example of multiplying matricies, mat([[1,2],[3,4]]).mat([[5,6],[7,8]]) returns mat([[19,22],[43,50]])\n"
					+ "Example, transpose(A.B) returns transpose(B).transpose(A)";
			
			Rule matrixMult = new Rule("matrix multiplication") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func dot = (Func)e;
					
					if(dot.get(0).isType("mat")) {
						Func totalMat = (Func)dot.get(0);
						
						for(int i = 1;i < dot.size();i++) {
							Func otherMat = (Func)dot.get(i);
							
							Func newMat = Mat.generateMat(Mat.rows(totalMat),Mat.cols(otherMat));
							
							for(int row = 0; row < Mat.rows(newMat);row++) {
								for(int col = 0; col < Mat.cols(newMat);col++) {
									Func totalRowSequence = Mat.getRow(totalMat,row);
									Func otherColSequence = Mat.getCol(otherMat,col);
									
									Func sum = sum();
									for(int j = 0;j<totalRowSequence.size();j++) {
										sum.add( prod(totalRowSequence.get(j),otherColSequence.get(j)) );
									}
									Mat.setElement(newMat,row, col, sum);
									
								}
							}
							
							totalMat = newMat;
						}
						return totalMat.simplify(casInfo);
					}
					
					return dot;
				}
			};
			
			Rule dotContainsDot = new Rule("dot contains dot") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func dot = (Func)e;
					
					for(int i = 0;i<dot.size();i++) {
						if(dot.get(i).isType("dot")) {
							Func subDot = (Func)dot.get(i);
							dot.remove(i);
							for(int j = 0;j<subDot.size();j++) dot.add(i+j, subDot.get(j));
							
							i+=subDot.size()-1;
						}
					}
					
					return dot;
				}
				
			};
			
			owner.behavior.rule = new Rule(new Rule[] {
					dotContainsDot,
					matrixMult
			},"main sequence");
			
			owner.behavior.toStringMethod = new Func.ToString() {
				
				@Override
				public String generateString(Func owner) {
					String out = "";
					for(int i = 0;i<owner.size();i++) {
						Expr currentExpr = owner.get(i);
						
						String currentExprType = currentExpr.typeName();
						boolean paren = !(
									currentExprType.equals("var") ||
									currentExprType.equals("mat") ||
									currentExpr instanceof Func && ((Func)currentExpr).behavior.toStringMethod == null
								);
						
						if(paren) out+="(";
						out+=currentExpr;
						if(paren) out+=")";
						if(i != owner.size()-1) out += ".";
					}
					return out;
				}
			};
		}
	};
}
