package cas;
import java.math.BigInteger;

public class Num extends Expr{
	
	private static final long serialVersionUID = -8648260475281043831L;
	public BigInteger realValue,imagValue;
	
	public static final Num ZERO = num(0),ONE = num(1),TWO = num(2),I = num(0,1), NEG_ONE = num(-1,0);//be carful not to modify these
	
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
	
	public void set(Num other) {
		realValue = other.realValue;
		imagValue = other.imagValue;
	}
	
	@Override
	public Expr simplify(Settings settings) {//nothing to be simplified
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
		}else if(this.equalStruct(ZERO)) {
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

	@Override
	public String toString() {
		if(!realValue.equals(BigInteger.ZERO) && !imagValue.equals(BigInteger.ZERO)) {
			if(imagValue.equals(BigInteger.ONE)) {
				return "("+realValue.toString()+"+i)";
			}
			return "(" +realValue.toString()+"+"+imagValue.toString()+"*i)";
		}else if(realValue.equals(BigInteger.ZERO) && !imagValue.equals(BigInteger.ZERO)) {
			if(imagValue.equals(BigInteger.ONE)) return "i";
			return "("+imagValue.toString()+"*i)";
		}else {
			return realValue.toString();
		}
	}
	@Override
	public Expr copy() {
		return new Num(realValue,imagValue);
	}
	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Num) {
			Num otherCasted = (Num)other;
			return otherCasted.realValue.equals(realValue) && otherCasted.imagValue.equals(imagValue);
		}
		return false;
	}
	@Override
	boolean similarStruct(Expr other,boolean checked) {
		if(other instanceof Num) return equalStruct(other);
		return false;
	}
	@Override
	public int hashCode() {
		return realValue.intValue()+230487349;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(realValue.doubleValue(),imagValue.doubleValue());
	}

}
