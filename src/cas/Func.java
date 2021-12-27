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
	
	Func(){}//
	
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
	public Expr copy() {
		Func out = new Func(name,(ExprList)getVars().copy(),getExpr().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return getExpr().replace(getVars()).convertToFloat(varDefs);
	}
	
}
