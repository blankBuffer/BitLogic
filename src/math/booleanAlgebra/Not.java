package math.booleanAlgebra;

import java.util.ArrayList;

public class Not extends BoolContainer{
	BoolContainer container;
	
	public Not(BoolContainer container) {
		this.container = container;
	}
	@Override
	public String toString(String modif) {
		modif+=('!');
		modif+=container.toString();
		return modif;
	}

	@Override
	public boolean equalStruct(BoolContainer other) {
		if(other instanceof Not) {
			return this.container.equalStruct(((Not)other).container);
		}
		return false;
	}

	@Override
	public BoolContainer copy() {
		return new Not(container.copy());
	}
	
	public BoolContainer containsNot() {
		if(!(container instanceof Not)) return this.copy();
		Not containerNot = (Not)container;
		return containerNot.container.copy();
	}
	
	public BoolContainer stateFlip() {
		if(container instanceof BoolState) {
			return new BoolState( !((BoolState)container).value );
		}
		return this.copy();
	}
	
	public BoolContainer andToOr() {
		if(this.container instanceof And) {
			And and = (And)this.container.copy();
			Or out = new Or();
			for(BoolContainer c:and.containers) {
				out.add(new Not(c));
			}
			return out.toSOP();
		}
		return this.copy();
	}
	
	public BoolContainer orToAnd() {
		if(this.container instanceof Or) {
			Or or = (Or)this.container.copy();
			And out = new And();
			for(BoolContainer c:or.containers) {
				out.add(new Not(c));
			}
			return out.toSOP();
		}
		return this.copy();
	}
	
	@Override
	public BoolContainer toSOP() {
		BoolContainer current = new Not(this.container.toSOP());
		
		if(!(current instanceof Not)) return current;
		current = ((Not)current).containsNot();
		
		if(!(current instanceof Not)) return current;
		current = ((Not)current).andToOr();
		
		if(!(current instanceof Not)) return current;
		current = ((Not)current).orToAnd();
		
		if(!(current instanceof Not)) return current;
		current = ((Not)current).stateFlip();
		
		
		return current;
	}
	@Override
	public void getVars(ArrayList<BoolVar> varList) {
		container.getVars(varList);
	}
}
