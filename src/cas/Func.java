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
	
	@Override
	public Expr simplify(Settings settings) {		
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		if(USE_CACHE){
			Expr cacheOut = cached.get(this);
			if(cacheOut != null){
				return cacheOut.copy();
			}
		}
		
		Class<? extends Func> originalType = this.getClass();
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
		
		ExprList ruleSequence = getRuleSequence();
		
		if(ruleSequence != null){
			
			for (int i = 0;i<ruleSequence.size();i++){
				Rule rule = (Rule)ruleSequence.get(i);
				toBeSimplified = rule.applyRuleToExpr(toBeSimplified, settings);
				if(!toBeSimplified.getClass().equals(originalType)) break;
			}
		}
		
		toBeSimplified.flags.simple = true;//result is simplified and should not be simplified again
		
		if(USE_CACHE && cached.size()<CACHE_SIZE) cached.put(copy(), toBeSimplified.copy());
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
	public ExprList ruleSequence = new ExprList();
	@Override
	public Expr copy() {
		Func out = new Func(name);
		for(int i = 0;i<size();i++){
			out.add(get(i).copy());
		}
		out.simplifyChildren = simplifyChildren;
		out.ruleSequence = ruleSequence;
		out.flags.set(flags);
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return getExpr().replace(getVars()).convertToFloat(varDefs);
	}
	@Override
	ExprList getRuleSequence() {
		return ruleSequence;
	}
	
}
