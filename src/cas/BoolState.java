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
		if(other instanceof BoolState) return equalStruct(other);
		return false;
	}

	@Override
	public int hashCode() {
		if(state) return -926360784;
		return 971308753;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		if(state == true) return new ComplexFloat(1,0);
		return new ComplexFloat(0,0);
	}

}
