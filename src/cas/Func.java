package cas;

public class Func extends Expr{

	private static final long serialVersionUID = -3146654431684411030L;
	String name;
	boolean example = false;
	
	ExprList getVars() {
		return (ExprList)get();
	}
	Expr getExpr() {
		return get(1);
	}
	
	public Func(String name,ExprList vars,Expr expr) {
		this.name = name;
		add(vars);
		add(expr);
	}
	
	public Func(String name,Equ e,Expr expr) {
		this.name = name;
		ExprList vars = new ExprList();
		vars.add(e);
		add(vars);
		add(expr);
	}

	@Override
	public Expr simplify(Settings settings) {
		return getExpr().replace(getVars()).simplify(settings);
	}

	@Override
	public Expr copy() {
	Func out = new Func(name,(ExprList)getVars().copy(),getExpr().copy());
		out.flags.set(flags);
		return out;
	}
	
	public static int printMode = 0;
	
	@Override
	public String toString() {
		String out = "";
		out+=name+"(";
		ExprList vars = getVars();
		
		for(int i = 0;i<vars.size();i++) {
			out+=((Equ)vars.get(i)).getRightSide();
			if(i!=vars.size()-1) out+=",";
		}
		if(example) {
			out+=")=";
			out+=getExpr();
		}else out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Func) {
			Func otherCasted = (Func)other;
			return otherCasted.getVars().equalStruct(getVars()) && otherCasted.getExpr().equalStruct(getExpr());
		}
		return false;
	}

	@Override
	public long generateHash() {
		return getVars().generateHash()*18643+getExpr().generateHash()*1982+923468723L;
	}

	@Override
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		ExprList vars = (ExprList) getVars().replace(equs);
		Expr expr = getExpr().replace(equs);
		return func(name,vars,expr);
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return getExpr().replace(getVars()).convertToFloat(varDefs);
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(!checked) if(checkForMatches(other) == false) return false;
		
		if(other instanceof Func) {
			for(int i = 0;i<size();i++) {
				if(!get(i).fastSimilarStruct(other.get(i))) return false;
			}
			return true;
		}
		return false;
	}
	
}
