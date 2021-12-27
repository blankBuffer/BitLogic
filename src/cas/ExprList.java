package cas;

public class ExprList extends Expr{

	private static final long serialVersionUID = 4631446864313039932L;
	
	public ExprList(){
		commutative = true;
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		removeRepeats((ExprList)toBeSimplified);
		toBeSimplified = alone((ExprList)toBeSimplified);
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	static void removeRepeats(ExprList list) {
		for(int i = 0;i<list.size();i++) {
			for(int j = i+1;j<list.size();j++) {
				if(list.get(i).equalStruct(list.get(j))) {
					list.remove(j);
					j--;
				}
			}
		}
	}
	
	static Expr alone(ExprList list) {
		if(list.size() == 1) {//if its only one element 
			return list.get(0);
		}else if(list.size() == 0) {//if its empty return 0
			return num(0);
		}
		return list;
	}

	@Override
	public String toString() {
		String out = "";
		out+='[';
		for(int i = 0;i<size();i++) {
			out+=get(i).toString();
			if(i != size()-1) out+=',';
		}
		out+=']';
		return out;
	}
	
	public static ExprList cast(Expr e) {
		if(e instanceof ExprList) return (ExprList)e;
		ExprList out = new ExprList();
		out.add(e);
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

}
