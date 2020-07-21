package math.algebra;

public abstract class Container {
	public static boolean showSteps = false;
	
	boolean simplified = false;
	public abstract void print();
	public abstract void classicPrint();
	public abstract boolean equalStruct(Container other);
	public abstract Container copy();
	public abstract boolean constant();
	public abstract Container simplify();
	public abstract double approx();
	public abstract boolean containsVars();
	public abstract boolean containsVar(String name);
}
