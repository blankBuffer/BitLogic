package cas.primitive;
import java.math.BigInteger;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;

public class Num extends Expr{
	
	private BigInteger realValue,imagValue;
	
	public static final Num ZERO = new Num(0,false),ONE = new Num(1,false),TWO = new Num(2,false),I = new Num(0,1,false), NEG_ONE = new Num(-1,0,false), NEG_I = new Num(0,-1,false);
	
	private void setFlags() {
		flags.simple = true;
		flags.sorted = true;
	}
	
	public Num(long num) {
		realValue = BigInteger.valueOf(num);
		imagValue = BigInteger.ZERO;
		setFlags();
	}
	public Num(long real,long imag) {
		realValue = BigInteger.valueOf(real);
		imagValue = BigInteger.valueOf(imag);
		setFlags();
	}
	public Num(String num) {
		realValue = new BigInteger(num);
		imagValue = BigInteger.ZERO;
		setFlags();
	}
	public Num(String real,String imag) {
		realValue = new BigInteger(real);
		imagValue = new BigInteger(imag);
		setFlags();
	}
	public Num(BigInteger num) {
		realValue = num;
		imagValue = BigInteger.ZERO;
		setFlags();
	}
	public Num(BigInteger real,BigInteger imag) {
		realValue = real;
		imagValue = imag;
		setFlags();
	}
	
	
	
	
	public Num(long num,boolean mutable) {
		realValue = BigInteger.valueOf(num);
		imagValue = BigInteger.ZERO;
		flags.mutable = mutable;
		setFlags();
	}
	public Num(long real,long imag,boolean mutable) {
		realValue = BigInteger.valueOf(real);
		imagValue = BigInteger.valueOf(imag);
		flags.mutable = mutable;
		setFlags();
	}
	public Num(String num,boolean mutable) {
		realValue = new BigInteger(num);
		imagValue = BigInteger.ZERO;
		flags.mutable = mutable;
		setFlags();
	}
	public Num(String real,String imag,boolean mutable) {
		realValue = new BigInteger(real);
		imagValue = new BigInteger(imag);
		flags.mutable = mutable;
		setFlags();
	}
	public Num(BigInteger num,boolean mutable) {
		realValue = num;
		imagValue = BigInteger.ZERO;
		flags.mutable = mutable;
		setFlags();
	}
	public Num(BigInteger real,BigInteger imag,boolean mutable) {
		realValue = real;
		imagValue = imag;
		flags.mutable = mutable;
		setFlags();
	}
	
	public void setValue(Num other) {
		realValue = other.realValue;
		imagValue = other.imagValue;
	}
	
	public void setRealValue(BigInteger realValue){
		if(flags.mutable == false) throw new RuntimeException("expression is not mutable!");
		this.realValue = realValue;
	}
	
	public void setImagValue(BigInteger imagValue){
		if(flags.mutable == false) throw new RuntimeException("expression is not mutable!");
		this.imagValue = imagValue;
	}
	
	public BigInteger getRealValue(){
		return realValue;
	}
	
	public BigInteger getImagValue(){
		return imagValue;
	}
	
	@Override
	public Expr simplify(CasInfo casInfo) {//nothing to be simplified
		return copy();
	}
	
	public boolean isComplex() {
		return !imagValue.equals(BigInteger.ZERO);
	}
	
	public Num addNum(Num other) {
		BigInteger newReal = realValue.add(other.realValue);
		BigInteger newImag = imagValue.add(other.imagValue);
		return new Num(newReal,newImag);
	}
	
	public Num subNum(Num other) {
		BigInteger newReal = realValue.subtract(other.realValue);
		BigInteger newImag = imagValue.subtract(other.imagValue);
		return new Num(newReal,newImag);
	}
	
	public Num multNum(Num other) {
		BigInteger newReal = realValue.multiply(other.realValue).subtract(imagValue.multiply(other.imagValue));
		BigInteger newImag = realValue.multiply(other.imagValue).add(imagValue.multiply(other.realValue));
		return new Num(newReal,newImag);
	}
	
	public Num divideNum(BigInteger b) {
		return new Num(realValue.divide(b),imagValue.divide(b));
	}
	
	public short signum() {//its considered negative if the real component of the number is negative or if the real part is zero and the complex part is negative
		if(realValue.signum() == -1) {
			return -1;
		}else if(realValue.equals(BigInteger.ZERO) && imagValue.signum() == -1) {
			return -1;
		}else if(this.equals(ZERO)) {
			return 0;
		}else {
			return 1;
		}
	}
	
	public Num strangeAbs() {//not absolute value in the normal sense
		if(signum() == -1) {
			return new Num(realValue.negate(),imagValue.negate());
		}
		return (Num) copy();
	}
	
	public boolean isDivisible(BigInteger b) {
		return realValue.mod(b) == BigInteger.ZERO && imagValue.mod(b) == BigInteger.ZERO;
	}
	
	public Num negate() {
		return new Num(realValue.negate(),imagValue.negate());
	}
	
	public BigInteger gcd() {
		return realValue.gcd(imagValue);
	}
	
	public Num complexConj() {
		return new Num(realValue,imagValue.negate());
	}
	
	public Num pow(BigInteger exponent) {
		
		if(exponent.signum() == -1) {
			System.err.println("negative exponent");
		}
		
		if(exponent.equals(BigInteger.ZERO)) {
			return ONE;
		}else if(exponent.equals(BigInteger.ONE)) {
			return (Num)copy();
		}else {
			
			if(exponent.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
				return this.multNum(this).pow(exponent.divide(BigInteger.TWO));
			}
			return this.multNum(this).pow(exponent.subtract(BigInteger.ONE).divide(BigInteger.TWO)).multNum(this);
			
			
		}
	}

	private BigInteger bigIntNegOne = BigInteger.valueOf(-1);
	@Override
	public String toString() {
		
		String out = null;
		if(!realValue.equals(BigInteger.ZERO) && !imagValue.equals(BigInteger.ZERO)) {
			if(imagValue.equals(BigInteger.ONE)) out = "("+realValue.toString()+"+i)";
			else if(imagValue.equals(bigIntNegOne)) out = "("+realValue.toString()+"-i)";
			else if(imagValue.signum() == -1) out = "(" +realValue.toString()+"-"+imagValue.negate().toString()+"*i)";
			else out = "(" +realValue.toString()+"+"+imagValue.toString()+"*i)";
		}else if(realValue.equals(BigInteger.ZERO) && !imagValue.equals(BigInteger.ZERO)) {
			if(imagValue.equals(BigInteger.ONE)) out = "i";
			else if(equals(Num.NEG_I)) out = "-i";
			else out = "("+imagValue.toString()+"*i)";
		}else {
			out = realValue.toString();
		}
		
		return out;
	}
	@Override
	public Expr copy() {
		return new Num(realValue,imagValue);
	}
	@Override
	public boolean equals(Object other) {
		if(other instanceof Num) {
			Num otherCasted = (Num)other;
			return otherCasted.realValue.equals(realValue) && otherCasted.imagValue.equals(imagValue);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return realValue.intValue()+230487349;
	}
	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		return new ComplexFloat(realValue.doubleValue(),imagValue.doubleValue());
	}

	@Override
	public Rule getRule() {
		return null;
	}

	@Override
	public String typeName() {
		return "num";
	}

	@Override
	public String help() {
		return "number expression\n"
				+ "examples\n"
				+ "2+3->5\n"
				+ "x+2*x->3*x";
	}
}
