package math.booleanAlgebra;
import java.util.ArrayList;

public class BoolList extends BoolContainer{
	public ArrayList<BoolContainer> containers;
	
	public BoolList(ArrayList<BoolContainer> containers) {
		this.containers = containers;
	}
	public BoolList() {
		containers = new ArrayList<BoolContainer>();
	}
	
	public void add(BoolContainer container) {
		containers.add(container);
	}

	@Override
	public String toString(String modif) {
		modif+=('[');
		for(int i = 0;i<containers.size();i++) {
			modif+=containers.get(i).toString();
			if(i<containers.size()-1) modif+=(',');
		}
		modif+=(']');
		return modif;
	}

	public boolean equalList(BoolContainer other) {
		//order does not matter
		if(other instanceof BoolList) {
			BoolList otherList = (BoolList)other;
			if(otherList.containers.size()!=this.containers.size()) return false;
			boolean[] usedIndex = new boolean[this.containers.size()];
			for(int i = 0;i < this.containers.size();i++) {
				boolean found = false;
				for(int j = 0;j < this.containers.size();j++) {
					if(usedIndex[j]) continue;
					if(this.containers.get(i).equalStruct(otherList.containers.get(j))) {
						usedIndex[j] = true;
						found = true;
						break;
					}
				}
				if(!found) return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean equalStruct(BoolContainer other) {
		if(other instanceof Or || other instanceof And) return false;
		return this.equalList(other);
	}

	@Override
	public BoolContainer copy() {
		ArrayList<BoolContainer> listCopy = new ArrayList<BoolContainer>();
		for(BoolContainer c:this.containers) listCopy.add(c.copy());
		return new BoolList(listCopy);
	}
	public BoolContainer alone() {
		int length = this.containers.size();
		if(length == 1) return this.containers.get(0).copy();
		return this.copy();
	}
	public BoolContainer removeDuplicates() {
		BoolList modible = (BoolList)this.copy();
		for(int i = 0;i <modible.containers.size();i++) {
			BoolContainer temp = modible.containers.get(i);
			for(int j = i+1;j<modible.containers.size();j++) {
				BoolContainer compare = modible.containers.get(j);
				
				if(compare.equalStruct(temp)) {
					modible.containers.remove(j);
					j--;
				}
				
			}
		}
		return modible;
	}
	@Override
	public BoolContainer toSOP() {
		BoolList temp = new BoolList();
		for(BoolContainer c:this.containers) {
			BoolContainer simplePart = c.toSOP();
			temp.add(simplePart);
		}
		BoolContainer current = temp;
		
		if(!(current instanceof BoolList)) return current;
		current = ((BoolList)current).alone();
		
		return current;
	}
	@Override
	public void getVars(ArrayList<BoolVar> varList) {
		for(BoolContainer c:containers) c.getVars(varList);
	}
}
