package math.algebra;

public abstract class Container {
	public static boolean showSteps = false;
	
	boolean simplified = false;
	public abstract String toString(String modif);
	public abstract void classicPrint();
	public abstract boolean equalStruct(Container other);
	@Override
	public abstract Container clone();
	public abstract boolean constant();
	public abstract Container simplify();
	public abstract double approx();
	public abstract boolean containsVars();
	public abstract boolean containsVar(String name);
	@Override
	public String toString() {
		return toString("");
	}
}
