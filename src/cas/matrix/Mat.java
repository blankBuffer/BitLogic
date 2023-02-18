package cas.matrix;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.primitive.Sequence;

public class Mat extends Expr{
	
	public Mat(){}//
	public Mat(Func sequenceExpr) {
		add(sequenceExpr);
	}
	
	public Mat(int rows, int cols) {
		Func allRows = sequence();
		for(int i = 0;i<rows;i++) {
			Func row = sequence();
			for(int j = 0;j<cols;j++) row.add(num(0));
			
			allRows.add(row);
		}
		add(allRows);
	}
	public int cols() {
		return get().get().size();
	}
	
	public int rows() {
		return get().size();
	}
	
	public Func getRow(int row) {//returns sequence
		return (Func)get().get(row);
	}
	
	public Func getCol(int col) {//returns sequence
		Func out = sequence();
		
		for(int i = 0;i<rows();i++) {
			out.add( getRow(i).get(col) );
		}
		
		return out;
	}
	
	public Expr getElement(int row,int col) {
		return get().get(row).get(col);
	}
	
	public void setElement(int row,int col,Expr e) {
		get().flags.simple = false;
		flags.simple = false;
		get().get(row).set(col, e);
	}
	
	public boolean correctFormat() {
		if(!(get().typeName().equals("sequence"))) return false;
		Func innerSequence = (Func)get();
		if(innerSequence.size() == 0) return false;
		for(int i = 0;i<innerSequence.size();i++) {
			if(!(innerSequence.get(i).typeName().equals("sequence") && getRow(i).size() == cols()  )) return false;
		}
		
		return true;
	}
	
	static Rule make2d = new Rule("force it to be 2d") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Mat mat = (Mat)e;
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

	static Rule mainSequenceRule = null;
	
	public static void loadRules(){
		mainSequenceRule = new Rule(new Rule[]{
				make2d
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
		return "mat";
	}
	@Override
	public String help() {
		return "matrix expression\n"
				+ "examples\n"
				+ "2*mat({{1,2},{3,4}})->mat({{2,4},{6,8}})\n"
				+ "(mat({{1,3},{5,8}})).(mat({{8,2},{1,5}}))->mat({{11,17},{48,50}})";
	}
}
