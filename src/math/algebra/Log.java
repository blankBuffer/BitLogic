package math.algebra;

import java.math.BigInteger;

public class Log extends Container{
	Container container;
	public Log(Container container) {
		this.container = container;
	}
	public Log() {
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
	
	public Container removePowers() {
		if(!(container instanceof Product || container instanceof Power || container instanceof IntC)) return clone();
		if(container instanceof IntC) {
			IntC cI = (IntC)container;
			if(IntArith.Prime.isPrime(cI.value)) return clone();
		}
		boolean allOnes = true;
		Sum res = new Sum();
		Product pr = null;
		Product thePlainLogPr = new Product();
		
		if(container instanceof Product) pr = (Product)container.clone();
		else {
			pr = new Product();
			pr.add(container.clone());
		}
		for(Container c:pr.containers) {
			if(c instanceof IntC) {
				IntC cI = (IntC)c;
				Power toPow = IntArith.toPower(cI.value);
				if(!((IntC)toPow.expo).value.equals(BigInteger.ONE) ) {
					Product tpr = new Product();
					tpr.add(new Log(toPow.base));
					tpr.add(toPow.expo);
					res.add(tpr);
					allOnes = false;
				}else thePlainLogPr.add(c);
			}else if(c instanceof Power) {
				Power cPow = (Power)c;
				Product tpr = new Product();
				tpr.add(new Log(cPow.base));
				tpr.add(cPow.expo);
				res.add(tpr);
				allOnes = false;
			}else thePlainLogPr.add(c);
		}
		if(thePlainLogPr.containers.size()>0) res.add(new Log(thePlainLogPr));
		if(allOnes) return clone();
		Container fres = res.alone();
		if(fres instanceof Sum) fres = fres.simplify();
		return fres;
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
		current = ((Log)current).removePowers();
		
		if(!(current instanceof Log)) return current;
		current = ((Log)current).containsE();//ln(e^x)->x
		
		return current;
	}
	@Override
	public double approx() {
		return Math.log(container.approx());
	}
}
