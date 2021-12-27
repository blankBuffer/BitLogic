package cas;

public class And extends Expr{

	private static final long serialVersionUID = 8729081482954093557L;
	
	public And(){
		commutative = true;
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify all the sub expressions
		
		
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}

	@Override
	public String toString() {
		String out = "";
		if(size() < 2) out+="alone and:";
		for(int i = 0;i<size();i++){
			out+=get(i);
			if(i!=size()-1) out+="&";
		}
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		// TODO Auto-generated method stub
		return null;
	}
}
