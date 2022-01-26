package cas;

public class ObjectExpr extends Expr{//behaves like variables

	private static final long serialVersionUID = 5555283885953176775L;
	public Object object = null;
	
	public ObjectExpr(Object o) {
		this.object = o;
	}

	@Override
	public Expr copy() {
		return new ObjectExpr(object);
	}

	@Override
	public String toString() {
		return object.toString();
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof ObjectExpr){
			return ((ObjectExpr)other).object == object;//only comparing if same object
		}
		return false;
	}

	@Override
	public int hashCode() {
		return object.hashCode()+876041978;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof ObjectExpr) return equals(other);
		return false;
	}

	@Override
	ExprList getRuleSequence() {
		return null;
	}
	
}
