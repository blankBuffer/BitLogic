package math.algebra;

import java.util.ArrayList;

public class Var extends Container{
	public String name;
	public Container container = null;
	
	public static ArrayList<Var> definedVars = new ArrayList<Var>();
	
	public static void deleteVar(String nameC) {
		for(Var v:definedVars) {
			if(v.name.equals(nameC)) {
				definedVars.remove(v);
				v.container = null;
				break;
			}
		}
	}
	
	public Var(String name) {
		this.name = name;
		
		boolean found = false;
		for(Var v:definedVars) {
			if(v.name.equals(name)) {
				this.container = v.container;
				found = true;
				break;
			}
		}
		if(!found) definedVars.add(this);
	}
	
	public Var(String name,Container c) {
		this.name = name;
		container = c;
		
		boolean found = false;
		for(Var v:definedVars) {
			if(v.name.equals(name)) {
				v.container = c;
				this.container = v.container;
				found = true;
				break;
			}
		}
		if(!found) definedVars.add(this);
	}
	@Override
	public boolean containsVars() {
		if(container != null) return container.containsVars();
		return true;
	}
	@Override
	public void print() {
		System.out.print(name);
	}
	@Override
	public void classicPrint() {
		print();
	}
	@Override
	public boolean equalStruct(Container other) {
		if(other instanceof Var) {
			Var otherVar = (Var)other;
			return otherVar.name.equals(this.name);
		}
		return false;
	}
	@Override
	public Container copy() {
		return this;
	}
	@Override
	public boolean constant() {
		if(container != null) {
			return container.constant();
		}
		return false;
	}
	@Override
	public boolean containsVar(String name) {
		return name.contentEquals(this.name);
	}
	@Override
	public Container simplify() {
		if(container != null) {
			return container.simplify();
		}
		return this.copy();
	}
	@Override
	public double approx() {
		return 0;
	}
}
