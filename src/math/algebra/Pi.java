package math.algebra;

public class Pi extends Var{
	public Pi() {
		super("π");
	}

	@Override
	public boolean equalStruct(Container other) {
		return other instanceof Pi;
	}

	@Override
	public Container copy() {
		return new Pi();
	}
	@Override
	public boolean constant() {
		return true;
	}
	@Override
	public Container simplify() {
		return new Pi();
	}
	@Override
	public double approx() {
		return Math.PI;
	}
}
