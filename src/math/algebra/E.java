package math.algebra;

public class E extends Var{

	public E() {
		super("e");
	}

	@Override
	public boolean equalStruct(Container other) {
		return other instanceof E;
	}
	@Override
	public boolean constant() {
		return true;
	}
	@Override
	public Container clone() {
		return new E();
	}

	@Override
	public Container simplify() {
		return new E();
	}
	@Override
	public double approx() {
		return Math.E;
	}
}
