package math.algebra;

import java.math.BigInteger;

public class IntC extends Container{
	public BigInteger value = BigInteger.ZERO;
	public IntC(long value) {
		this.value =  BigInteger.valueOf(value);
	}
	public IntC(BigInteger value) {
		this.value =  value;
	}
	public IntC() {
	}
	@Override
	public String toString(String modif) {
		modif+=value.toString();
		return modif;
	}
	@Override
	public void classicPrint() {
		System.out.print(value);
	}
	@Override
	public boolean equalStruct(Container other) {
		if(other instanceof IntC) {
			IntC otherIntC = (IntC)other;
			return otherIntC.value.equals(this.value);
		}
		return false;
	}
	@Override
	public Container clone() {
		return new IntC(this.value);
	}
	@Override
	public boolean constant() {
		return true;
	}
	@Override
	public boolean containsVars() {
		return false;
	}
	@Override
	public Container simplify() {
		return this.clone();
	}
	@Override
	public boolean containsVar(String name) {
		return false;
	}
	@Override
	public double approx() {
		return value.doubleValue();
	}
}
