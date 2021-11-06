package cas;

public class ComplexFloat {
	public double real = 0.0,imag = 0.0;
	public ComplexFloat(double real,double imag) {
		this.real = real;
		this.imag = imag;
	}
	public void set(ComplexFloat other) {
		this.real = other.real;
		this.imag = other.imag;
	}
	@Override
	public String toString(){
		if(real != 0.0 && imag != 0.0) {
			return Double.toString(real)+"+"+Double.toString(imag)+"*i";
		}else if(imag == 0.0) {
			return Double.toString(real);
		}else if(real == 0.0){
			return Double.toString(imag)+"*i";
		}
		return Double.toString(real);
	}
	
	public static ComplexFloat sin(ComplexFloat in) {
		return new ComplexFloat(Math.sin(in.real)*Math.cosh(in.imag),Math.cos(in.real)*Math.sinh(in.imag));
	}
	public static ComplexFloat cos(ComplexFloat in) {
		return new ComplexFloat(Math.cos(in.real)*Math.cosh(in.imag),-Math.sin(in.real)*Math.sinh(in.imag));
	}
	public static ComplexFloat tan(ComplexFloat in) {
		return div(sin(in),cos(in));
	}
	public static ComplexFloat add(ComplexFloat a,ComplexFloat b) {
		return new ComplexFloat(a.real+b.real,a.imag+b.imag);
	}
	public static ComplexFloat sub(ComplexFloat a, ComplexFloat b) {
		return new ComplexFloat(a.real-b.real,a.imag-b.imag);
	}
	public static ComplexFloat mult(ComplexFloat a,ComplexFloat b) {
		return new ComplexFloat(a.real*b.real-a.imag*b.imag,a.real*b.imag+a.imag*b.real);
	}
	public static ComplexFloat div(ComplexFloat a,ComplexFloat b) {
		double divisor = b.real*b.real+b.imag*b.imag;
		return new ComplexFloat( (a.imag*b.imag+b.real*a.real)/divisor ,(a.imag*b.real-b.imag*a.real)/divisor );
	}
	public static ComplexFloat ln(ComplexFloat in) {
		return new ComplexFloat(0.5*Math.log(in.real*in.real+in.imag*in.imag),Math.atan2(in.imag,in.real));
	}
	public static ComplexFloat exp(ComplexFloat in) {
		double scaler = Math.exp(in.real);
		return new ComplexFloat(Math.cos(in.imag)*scaler,Math.sin(in.imag)*scaler);
	}
	public static ComplexFloat pow(ComplexFloat base,ComplexFloat expo) {
		if(base.real == 0.0 && base.imag == 0.0) return new ComplexFloat(0,0);
		return exp(mult(ln(base),expo));
	}
	public static ComplexFloat atan(ComplexFloat in) {
		ComplexFloat iz = mult(new ComplexFloat(0,1),in);
		return mult(new ComplexFloat(0,0.5),sub(ln(sub(new ComplexFloat(1,0),iz)),ln(add(new ComplexFloat(1,0),iz)) ));
	}
}
