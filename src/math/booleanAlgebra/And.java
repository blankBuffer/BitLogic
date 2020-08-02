package math.booleanAlgebra;

import java.util.ArrayList;

public class And extends BoolList{
	public And(ArrayList<BoolContainer> containers) {
		this.containers = containers;
	}
	public And() {
	}
	@Override
	public String toString(String modif) {
		modif+=('(');
		for(int i = 0;i<containers.size();i++) {
			modif+=containers.get(i).toString();
			if(i<containers.size()-1) modif+=("&");
		}
		modif+=(')');
		return modif;
	}
	@Override
	public BoolContainer copy() {
		ArrayList<BoolContainer> listCopy = new ArrayList<BoolContainer>();
		for(BoolContainer c:this.containers) listCopy.add(c.copy());
		return new And(listCopy);
	}
	@Override
	public boolean equalStruct(BoolContainer other) {
		if(!(other instanceof And)) return false;
		return this.equalList(other);
	}
	@Override
	public BoolContainer alone() {
		int length = this.containers.size();
		if(length == 1) return this.containers.get(0).copy();
		if(length == 0) return new BoolState(true);
		return this.copy();
	}
	public BoolContainer oppositeToZero() {
		BoolList modible = (BoolList)this.copy();
		for(int i = 0;i <modible.containers.size();i++) {
			BoolContainer temp = modible.containers.get(i);
			if(temp instanceof Not) {
				temp = ((Not)temp).container;
				for(int j = i+1;j < modible.containers.size();j++) {
					BoolContainer compare = modible.containers.get(j);
					if(compare.equalStruct(temp)) return new BoolState(false);
				}
			}else {
				for(int j = i+1;j < modible.containers.size();j++) {
					BoolContainer compare = modible.containers.get(j);
					if(compare instanceof Not) {
						compare = ((Not)compare).container;
						if(compare.equalStruct(temp)) return new BoolState(false);
					}
					
				}
			}
		}
		return modible;
	}
	public BoolContainer stateRules() {
		And modible = (And)this.copy();
		for(int i = 0;i < modible.containers.size();i++) {
			BoolContainer temp = modible.containers.get(i);
			if(temp instanceof BoolState) {
				boolean value = ((BoolState)temp).value;
				if(value == false) return new BoolState(false);
				else {
					modible.containers.remove(i);
					i--;
				}
			}
		}
		return modible;
	}
	public BoolContainer merge() {
		And modible = (And)this.copy();
		for(int i = 0;i<modible.containers.size();i++) {
			BoolContainer temp = modible.containers.get(i);
			if(temp instanceof And) {
				And tempAnd = (And)temp;
				modible.containers.remove(i);
				for(BoolContainer c:tempAnd.containers) {
					modible.add(c);
				}
				i--;
			}
		}
		return modible;
	}
	public BoolContainer distribute() {
		boolean found = false;
		Or or = null;
		And modible = (And)this.copy();
		for(BoolContainer c: modible.containers) {
			if(c instanceof Or) {
				or = (Or)c;
				modible.containers.remove(c);
				found = true;
				break;
			}
		}
		if(!found) return modible;
		
		Or out = new Or();
		
		for(int i = 0;i< or.containers.size();i++) {
			And p = new And();
			p.add(or.containers.get(i));
			p.add(modible.copy());
			out.add(p);
		}
		
		return out.toSOP();
	}
	@Override
	public BoolContainer toSOP() {
		And temp = new And();
		for(BoolContainer c:this.containers) {
			BoolContainer simplePart = c.toSOP();
			temp.add(simplePart);
		}
		BoolContainer current = temp;
		
		if(!(current instanceof And)) return current;
		current = ((And)current).merge();
		
		if(!(current instanceof And)) return current;
		current = ((And)current).distribute();
		
		if(!(current instanceof And)) return current;
		current = ((And)current).removeDuplicates();
		
		if(!(current instanceof And)) return current;
		current = ((And)current).oppositeToZero();
		
		if(!(current instanceof And)) return current;
		current = ((And)current).stateRules();
		
		if(!(current instanceof And)) return current;
		current = ((And)current).alone();
		
		return current;
	}
}
