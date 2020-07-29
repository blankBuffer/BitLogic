package math.algebra;

import java.math.BigInteger;

public class Log extends Container{
	Container container;
	public Log(Container container) {
		this.container = container;
	}
	@Override
	public String toString(String modif) {
		modif+="ln(";
		modif+=container.toString();
		modif+=")";
		return modif;
	}
	@Override
	public void classicPrint() {
		System.out.print("ln(");
		container.classicPrint();
		System.out.print(')');
	}
	@Override
	public boolean equalStruct(Container other) {
		if(other instanceof Log) {
			Log otherLog = (Log)other;
			return otherLog.container.equalStruct(this.container);
		}
		return false;
	}
	@Override
	public Container clone() {
		return new Log(this.container.clone());
	}
	@Override
	public boolean constant() {
		return this.container.constant();
	}
	
	public Container powerToProduct() {
		if(this.container instanceof Power) {
			Power innerPow = (Power)container;
			Product pr = new Product();
			pr.add(new Log(innerPow.base.clone()));
			pr.add(innerPow.expo.clone());
			return pr.simplify();
		}
		return this.clone();
	}
	
	public Container containsE() {
		if(this.container instanceof E ) return new IntC(1);
		else if(container instanceof Power) {
			Power contPow = (Power)container;
			if(contPow.base instanceof E) {
				return contPow.expo;
			}
		}
		return this.clone();
	}
	
	public Container valueOne() {
		if(!(this.container instanceof IntC)) return this.clone();
		if(((IntC)this.container).value.equals(BigInteger.ONE)) return new IntC(0);
		return this.clone();
	}
	@Override
	public boolean containsVars() {
		return container.containsVars();
	}
	@Override
	public boolean containsVar(String name) {
		return container.containsVar(name);
	}
	@Override
	public Container simplify() {
		
		if(showSteps) {
			System.out.println("simplifying logarithm");
			classicPrint();
			System.out.println();
		}
		
		Container current = new Log(this.container.simplify());
		
		if(!(current instanceof Log)) return current;
		current = ((Log)current).valueOne();//ln(1) -> 0
		
		if(!(current instanceof Log)) return current;
		current = ((Log)current).powerToProduct();
		
		if(!(current instanceof Log)) return current;
		current = ((Log)current).containsE();//ln(e^x)->x
		
		return current;
	}
	@Override
	public double approx() {
		return Math.log(container.approx());
	}
}
