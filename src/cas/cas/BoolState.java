package cas;
import java.util.ArrayList;

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
	public Expr replace(ArrayList<Equ> equs) {
		for(Equ e:equs) if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		return copy();
	}

	@Override
	public long generateHash() {
		if(state) return 2;
		return 1;
	}

	@Override
	public double convertToFloat(ExprList varDefs) {
		if(state == true) return 1;
		else return 0;
	}

}
