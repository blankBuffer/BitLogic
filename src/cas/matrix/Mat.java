package cas.matrix;

import cas.base.CasInfo;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.primitive.Sequence;
import static cas.Cas.*;

public class Mat{
	
	public static Func.FuncLoader matLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.helpMessage = "Matrix data identifier.\n"
					+ "The mat function tells the CAS that the data is representing a matrix.\n"
					+ "For example mat([[1,2,3],[4,5,6]]) represents a matrix with 2 rows and 3 columns.";
			
			Rule make2d = new Rule("force it to be 2d") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func mat = (Func)e;
					
					Func innerSequence = Sequence.cast( mat.get() );
					
					for(int i = 0;i < innerSequence.size();i++) {
						if(!(innerSequence.get(i).isType("sequence"))) {
							innerSequence.set(i, Sequence.cast(innerSequence.get(i)));
						}
					}
					
					if(innerSequence.size() == 0) innerSequence.add(sequence());
					
					mat.set(0, innerSequence);
					
					return mat;
				}
			};
			
			owner.behavior.rule = new Rule(new Rule[] {
					make2d
			},"main sequence");
		}
	};
	
	public static Func generateMat(int rows, int cols) {
		Func mat = mat();
		Func allRows = sequence();
		for(int i = 0;i<rows;i++) {
			Func row = sequence();
			for(int j = 0;j<cols;j++) row.add(num(0));
			
			allRows.add(row);
		}
		mat.add(allRows);
		
		return mat;
	}
	public static int cols(Func mat) {
		return mat.get().get().size();
	}
	
	public static int rows(Func mat) {
		return mat.get().size();
	}
	
	public static Func getRow(Func mat,int row) {//returns sequence
		return (Func)mat.get().get(row);
	}
	
	public static Func getCol(Func mat,int col) {//returns sequence
		Func out = sequence();
		
		for(int i = 0;i<rows(mat);i++) {
			out.add( getRow(mat,i).get(col) );
		}
		
		return out;
	}
	
	public static Expr getElement(Func mat,int row,int col) {
		return mat.get().get(row).get(col);
	}
	
	public static void setElement(Func mat,int row,int col,Expr e) {
		mat.get().get(row).set(col, e);
	}
	
	
	public static boolean correctFormat(Func mat) {
		if(!(mat.get().isType("sequence"))) return false;
		Func innerSequence = (Func)mat.get();
		if(innerSequence.size() == 0) return false;
		for(int i = 0;i<innerSequence.size();i++) {
			if(!(innerSequence.get(i).isType("sequence") && getRow(mat,i).size() == cols(mat)  )) return false;
		}
		
		return true;
	}
	
}
