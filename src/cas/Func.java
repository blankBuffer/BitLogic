package cas;

public class Func extends Expr{

	private static final long serialVersionUID = -3146654431684411030L;
	String name;
	int numberOfParams;
	
	void init(){
	}
	
	Func(){}//
	public Func(String name,int numberOfParams){
		this.name = name;
		this.numberOfParams = numberOfParams;
		init();
	}
	public Func(String name,Expr... params){
		this.name = name;
		for(Expr e:params) {
			add(e);
		}
		init();
	}
	
	@Override
	public String toString() {
		String out = "";
		out+=name+"(";
		
		for(int i = 0;i<size();i++) {
			out+=get(i);
			if(i!=size()-1) out+=",";
		}
		out+=")";
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
		out.numberOfParams = numberOfParams;
		out.flags.set(flags);
		return out;
	}
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}
	@Override
	ExprList getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public String typeName() {
		return name;
	}
	
}
