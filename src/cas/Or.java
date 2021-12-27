package cas;

public class Or extends Expr{
	
	private static final long serialVersionUID = 5003710279364491787L;

	public Or(){
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
		if(size() < 2) out+="alone or:";
		for(int i = 0;i<size();i++){
			out+=get(i);
			if(i!=size()-1) out+="|";
		}
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		// TODO Auto-generated method stub
		return null;
	}
}
