 package math.booleanAlgebra;

import java.util.ArrayList;

public class BoolVar extends BoolContainer{
	String name;
	public BoolVar(String name) {
		this.name = name;
	}
	@Override
	public void print() {
		System.out.print(name);
	}
	@Override
	public boolean equalStruct(BoolContainer other) {
		if(other instanceof BoolVar) {
			BoolVar otherVar = (BoolVar)other;
			return otherVar.name.equals(this.name);
		}
		return false;
	}
	@Override
	public BoolContainer copy() {
		return new BoolVar(name);
	}
	@Override
	public BoolContainer toSOP() {
		return this.copy();
	}
	@Override
	public void getVars(ArrayList<BoolVar> varList) {
		for(BoolVar v:varList) if(v.equalStruct(this)) return;
		varList.add((BoolVar)this.copy());
	}
}
