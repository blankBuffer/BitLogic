package math.algebra;

import java.util.ArrayList;

public abstract class List extends Container{
	public ArrayList<Container> containers;
	public List() {
		containers = new ArrayList<Container>();
	}
	public List(ArrayList<Container> containers) {
		this.containers = containers;
	}
	
	public void add(Container c) {
		containers.add(c);
	}
	public boolean equalList(Container other) {
		//order does not matter
		if(other instanceof List) {
			List otherList = (List)other;
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
	public abstract String toString(String modif);
	@Override
	public void classicPrint() {
		System.out.print('[');
		for(int i = 0;i<containers.size();i++) {
			containers.get(i).classicPrint();
			if(i<containers.size()-1) System.out.print(',');
		}
		System.out.print(']');
	}
	@Override
	public boolean equalStruct(Container other) {
		if(other instanceof Sum || other instanceof Product) return false;
		return equalList(other);
	}
	@Override
	public abstract Container clone();
	@Override
	public boolean constant() {
		for(Container c:containers) {
			if(!c.constant()) return false;
		}
		return true;
	}
	public Container alone() {
		int length = this.containers.size();
		if(length == 1) return this.containers.get(0).clone();
		return this.clone();
	}
	@Override
	public boolean containsVars() {
		for(Container c:containers) {
			if(c.containsVars()) return true;
		}
		return false;
	}
	@Override
	public abstract Container simplify();
	@Override
	public boolean containsVar(String name) {
		for(Container c:containers) {
			if(c.containsVar(name)) return true;
		}
		return false;
	}
	@Override
	public double approx() {
		System.out.println("lists can't become floats");
		return 0;
	}
}
