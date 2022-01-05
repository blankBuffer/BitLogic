package cas;

import java.util.ArrayList;

public class Func extends Expr{

	private static final long serialVersionUID = -3146654431684411030L;
	String name;
	boolean example = false;
	boolean simplifyChildren = true;
	
	ExprList getVars() {
		return (ExprList)get();
	}
	Expr getExpr() {
		return get(1);
	}
	
	public Expr getParameter(int i){
		return ((Equ)getVars().get(i)).getRightSide();
	}
	public int parametersSize(){
		return getVars().size();
	}
	public void setParameter(int i,Expr e){
		((Equ)getVars().get(i)).setRightSide(e);
	}
	
	void init(){
	}
	
	Func(){}//
	public Func(String name){
		this.name = name;
		init();
	}
	public Func(String name,ExprList vars){
		this.name = name;
		add(vars);
		init();
	}
	public Func(String name,int numberOfParams){
		this.name = name;
		ExprList vars = new ExprList();
		for(int i = 0;i<numberOfParams;i++){
			String varName = "p"+i;//honestly pointless but whatever
			vars.add(equ(var(varName),var(varName)));
			
		}
		add(vars);
		init();
	}
	public Func(String name,ExprList vars,Expr expr) {
		this.name = name;
		add(vars);
		add(expr);
		init();
	}
	
	public Func(String name,Equ e,Expr expr) {
		this.name = name;
		ExprList vars = new ExprList();
		vars.add(e);
		add(vars);
		add(expr);
		init();
	}

	public ArrayList<Rule> rules = new ArrayList<Rule>();
	
	@Override
	public Expr simplify(Settings settings) {
		if(size()>1){
			return getExpr().replace(getVars()).simplify(settings);
		}
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		if(simplifyChildren){
			Func current = (Func)toBeSimplified;
			for(int i = 0;i<current.parametersSize();i++) {
				Expr temp = current.getParameter(i);
				if(!temp.flags.simple) {
					temp = temp.simplify(settings);
					current.setParameter(i, temp);
				}
			}
		}
		for(Rule r:rules){
			toBeSimplified = r.applyRuleToExpr(toBeSimplified, settings);
		}
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	public static int printMode = 0;
	
	@Override
	public String toString() {
		String out = "";
		out+=name+"(";
		ExprList vars = getVars();
		
		for(int i = 0;i<vars.size();i++) {
			out+=getParameter(i);
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
		Func out = new Func(name);
		for(int i = 0;i<size();i++){
			out.add(get(i).copy());
		}
		out.rules = rules;
		out.flags.set(flags);
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return getExpr().replace(getVars()).convertToFloat(varDefs);
	}
	
}
