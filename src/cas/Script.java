package cas;

public class Script extends Expr{

	private static final long serialVersionUID = -3385077575450663182L;

	@Override
	public Expr simplify(Settings settings) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expr copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		String out = "";
		if(size() == 0) return "{emptyScript;}";
		out+='{';
		for(int i = 0;i<size();i++) {
			out+=get(i)+";";
		}
		out+='}';
		return out;
	}
	
	public static Script cast(Expr e) {
		if(e instanceof Script) {
			return (Script)e;
		}
		
		Script s = new Script();
		s.add(e);
		return s;
		
	}

	@Override
	public boolean equalStruct(Expr other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long generateHash() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		// TODO Auto-generated method stub
		return false;
	}

}
