package math.booleanAlgebra;

import java.util.ArrayList;

public class BoolState extends BoolContainer{
	boolean value = false;
	
	public BoolState(boolean value) {
		this.value = value;
	}
	public BoolState() {
	}
	
	@Override
	public String toString(String modif) {
		return modif+=value;
	}

	@Override
	public boolean equalStruct(BoolContainer other) {
		if(other instanceof BoolState) {
			return value == ((BoolState)other).value;
		}
		return false;
	}

	@Override
	public BoolContainer copy() {
		return new BoolState(value);
	}

	@Override
	public BoolContainer toSOP() {
		return copy();
	}
	@Override
	public void getVars(ArrayList<BoolVar> varList) {}

}
