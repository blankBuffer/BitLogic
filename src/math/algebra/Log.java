package math.algebra;

import java.math.BigInteger;

public class Log extends Container{
	Container container;
	public Log(Container container) {
		this.container = container;
	}
	@Override
	public void print() {
		System.out.print("ln(");
		container.print();
		System.out.print(')');
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
	public Container copy() {
		return new Log(this.container.copy());
	}
	@Override
	public boolean constant() {
		return this.container.constant();
	}
	
	public Container powerToProduct() {
		if(this.container instanceof Power) {
			Power innerPow = (Power)container;
			Product pr = new Product();
			pr.add(new Log(innerPow.base.copy()));
			pr.add(innerPow.expo.copy());
			return pr.simplify();
		}
		return this.copy();
	}
	
	public Container containsE() {
		if(this.container instanceof E ) return new IntC(1);
		else if(container instanceof Power) {
			Power contPow = (Power)container;
			if(contPow.base instanceof E) {
				return contPow.expo;
			}
		}
		return this.copy();
	}
	
	public Container valueOne() {
		if(!(this.container instanceof IntC)) return this.copy();
		if(((IntC)this.container).value.equals(BigInteger.ONE)) return new IntC(0);
		return this.copy();
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
