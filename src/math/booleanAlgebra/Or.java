package math.booleanAlgebra;

import java.util.ArrayList;

public class Or extends BoolList{
	public Or(ArrayList<BoolContainer> containers) {
		this.containers = containers;
	}
	public Or() {
	}
	@Override
	public String toString(String modif) {
		modif+=('(');
		for(int i = 0;i<containers.size();i++) {
			modif+=containers.get(i).toString();
			if(i<containers.size()-1) modif+=("|");
		}
		modif+=(')');
		return modif;
	}
	@Override
	public BoolContainer copy() {
		ArrayList<BoolContainer> listCopy = new ArrayList<BoolContainer>();
		for(BoolContainer c:this.containers) listCopy.add(c.copy());
		return new Or(listCopy);
	}
	@Override
	public boolean equalStruct(BoolContainer other) {
		if(!(other instanceof Or)) return false;
		return this.equalList(other);
	}
	@Override
	public BoolContainer alone() {
		int length = this.containers.size();
		if(length == 1) return this.containers.get(0).copy();
		if(length == 0) return new BoolState(false);
		return this.copy();
	}
	public BoolContainer oppositeToOne() {
		BoolList modible = (BoolList)this.copy();
		for(int i = 0;i <modible.containers.size();i++) {
			BoolContainer temp = modible.containers.get(i);
			if(temp instanceof Not) {
				temp = ((Not)temp).container;
				for(int j = i+1;j < modible.containers.size();j++) {
					BoolContainer compare = modible.containers.get(j);
					if(compare.equalStruct(temp)) return new BoolState(true);
				}
			}else {
				for(int j = i+1;j < modible.containers.size();j++) {
					BoolContainer compare = modible.containers.get(j);
					if(compare instanceof Not) {
						compare = ((Not)compare).container;
						if(compare.equalStruct(temp)) return new BoolState(true);
					}
					
				}
			}
		}
		return modible;
	}
	public BoolContainer stateRules() {
		Or modible = (Or)this.copy();
		for(int i = 0;i < modible.containers.size();i++) {
			BoolContainer temp = modible.containers.get(i);
			if(temp instanceof BoolState) {
				boolean value = ((BoolState)temp).value;
				if(value == true) return new BoolState(true);
				else {
					modible.containers.remove(i);
					i--;
				}
			}
		}
		return modible;
	}
	public BoolContainer merge() {
		Or modible = (Or)this.copy();
		for(int i = 0;i<modible.containers.size();i++) {
			BoolContainer temp = modible.containers.get(i);
			if(temp instanceof Or) {
				Or tempOr = (Or)temp;
				modible.containers.remove(i);
				for(BoolContainer c:tempOr.containers) {
					modible.add(c);
				}
				i--;
			}
		}
		return modible;
	}
	
	@Override
	public BoolContainer toSOP() {
		Or temp = new Or();
		for(BoolContainer c:this.containers) {
			BoolContainer simplePart = c.toSOP();
			temp.add(simplePart);
		}
		BoolContainer current = temp;
		
		if(!(current instanceof Or)) return current;
		current = ((Or)current).merge();
		
		if(!(current instanceof Or)) return current;
		current = ((Or)current).removeDuplicates();
		
		if(!(current instanceof Or)) return current;
		current = ((Or)current).oppositeToOne();
		
		if(!(current instanceof Or)) return current;
		current = ((Or)current).stateRules();
		
		if(!(current instanceof Or)) return current;
		current = ((Or)current).alone();
		
		return current;
	}
}
