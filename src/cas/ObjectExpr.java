package cas;

public class ObjectExpr extends Expr{//behaves like variables

	private static final long serialVersionUID = 5555283885953176775L;
	public Object object = null;
	
	public ObjectExpr(Object o) {
		this.object = o;
	}

	@Override
	public Expr simplify(Settings settings) {
		return copy();
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
	public boolean equalStruct(Expr other) {
		if(other instanceof ObjectExpr){
			return ((ObjectExpr)other).object == object;//only comparing if same object
		}
		return false;
	}

	@Override
	public long generateHash() {
		return object.hashCode()+8760341908762341234L;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		return true;
	}
	
}
