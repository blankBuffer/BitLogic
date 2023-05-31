package cas.matrix;

import cas.Cas;
import cas.base.CasInfo;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.primitive.Sequence;

public class Mat{
	
	public static Func.FuncLoader matLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			Rule make2d = new Rule("force it to be 2d") {
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
					Func mat = (Func)e;
					
					Func innerSequence = Sequence.cast( mat.get() );
					
					for(int i = 0;i < innerSequence.size();i++) {
						if(!(innerSequence.get(i).typeName().equals("sequence"))) {
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
		Func mat = Cas.mat();
		Func allRows = Cas.sequence();
		for(int i = 0;i<rows;i++) {
			Func row = Cas.sequence();
			for(int j = 0;j<cols;j++) row.add(Cas.num(0));
			
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
		Func out = Cas.sequence();
		
		for(int i = 0;i<rows(mat);i++) {
			out.add( getRow(mat,i).get(col) );
		}
		
		return out;
	}
	
	public static Expr getElement(Func mat,int row,int col) {
		return mat.get().get(row).get(col);
	}
	
	public static void setElement(Func mat,int row,int col,Expr e) {
		mat.get().flags.simple = false;
		mat.flags.simple = false;
		mat.get().get(row).set(col, e);
	}
	
	
	public static boolean correctFormat(Func mat) {
		if(!(mat.get().typeName().equals("sequence"))) return false;
		Func innerSequence = (Func)mat.get();
		if(innerSequence.size() == 0) return false;
		for(int i = 0;i<innerSequence.size();i++) {
			if(!(innerSequence.get(i).typeName().equals("sequence") && getRow(mat,i).size() == cols(mat)  )) return false;
		}
		
		return true;
	}
	
}
