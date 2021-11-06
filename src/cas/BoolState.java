package cas;

public class BoolState extends Expr{

	private static final long serialVersionUID = 4155968576732862760L;
	boolean state = false;
	
	public BoolState(boolean state){
		this.state = state;
		flags.simple = true;
		flags.sorted = true;
	}
	
	@Override
	public Expr simplify(Settings settings) {
		return copy();
	}

	@Override
	public Expr copy() {
		return new BoolState(state);
	}

	@Override
	public String toString() {
		return Boolean.toString(state);
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof BoolState) {
			BoolState otherCasted = (BoolState)other;
			if(otherCasted.state == state) return true;
		}
		return false;
	}

	@Override
	boolean similarStruct(Expr other,boolean checked) {
		if(other instanceof BoolState) return equalStruct((BoolState)other);
		else return false;
	}

	@Override
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		return copy();
	}

	@Override
	public long generateHash() {
		if(state) return 2;
		return 1;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		if(state == true) return new ComplexFloat(1,0);
		else return new ComplexFloat(0,0);
	}

}
