package cas.base;

import cas.calculus.Limit;
import cas.primitive.Var;

public class Func extends Expr{

	public Behavior behavior;
	public FuncLoader loader = null;
	
	public static abstract class FloatFunc{
		public abstract ComplexFloat convertToFloat(Func varDefs,Func owner);
	}
	
	public static abstract class FuncLoader{
		public abstract void load(Func owner);
	}
	
	public static abstract class ToString{
		public abstract String generateString(Func owner);
	}
	
	public static class Behavior{
		public Rule rule = null;
		public Rule doneRule = null;
		public FloatFunc toFloat = null;
		public ToString toStringMethod = null;
		public String name = null;
		public int numOfParams = 0;
		public boolean commutative = false;//a+b = b+a
		public boolean simplifyChildren = true;
		public String helpMessage = null;
	}
	
	void init(){
	}
	
	public Func(String name,int numberOfParams){
		behavior = new Behavior();
		this.behavior.numOfParams = numberOfParams;
		this.behavior.name = name;
		init();
	}
	public Func(String name,int numberOfParams,FuncLoader loader){
		this.loader = loader;
		behavior = new Behavior();
		this.behavior.numOfParams = numberOfParams;
		this.behavior.name = name;
		init();
	}
	public Func(String name,Expr... params){
		for(Expr e:params) {
			add(e);
		}
		behavior = new Behavior();
		this.behavior.numOfParams = params.length;
		this.behavior.name = name;
		init();
	}
	
	public Func(){
		init();
	}
	
	@Override
	public String toString() {
		if(behavior.toStringMethod == null){
			String out = "";
			out+=behavior.name+"(";
			
			for(int i = 0;i<size();i++) {
				out+=get(i);
				if(i!=size()-1) out+=",";
			}
			out+=")";
			return out;
		}else{
			return behavior.toStringMethod.generateString(this);
		}
	}
	
	@Override
	public Expr copy() {
		Func out = new Func();
		for(int i = 0;i<size();i++){
			out.add(get(i).copy());
		}
		out.behavior = behavior;
		out.flags.set(flags);
		return out;
	}
	
	public static FloatFunc nothingFunc = new Func.FloatFunc() {//return whatever is inside
		@Override
		public ComplexFloat convertToFloat(Func varDefs, Func owner) {
			return owner.get().convertToFloat(varDefs);
		}
	};
	
	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		if(behavior.toFloat != null) {
			return behavior.toFloat.convertToFloat(varDefs,this);
		}
		return new ComplexFloat(0,0);
	}
	@Override
	public Rule getRule() {
		return behavior.rule;
	}
	
	@Override
	public Rule getDoneRule() {
		return behavior.doneRule;
	}

	@Override
	public String typeName() {
		return behavior.name;
	}
	
	//power specific functions
	public void setBase(Expr base) {
		if(!isType("power")) throw new RuntimeException(typeName()+": is not a power");
		set(0, base);
	}
	public void setExpo(Expr expo) {
		if(!isType("power")) throw new RuntimeException(typeName()+": is not a power");
		set(1, expo);
	}
	
	public Expr getBase() {
		assert isType("power") : "expected a power";
		return get(0);
	}
	public Expr getExpo() {
		assert isType("power") : "expected a power";
		return get(1);
	}
	
	public Expr getNumer() {
		assert isType("div") : "expected a div";
		return get();
	}
	public Expr getDenom() {
		assert isType("div") : "expected a div";
		return get(1);
	}
	
	public void setNumer(Expr e) {
		set(0,e);
	}
	
	public void setDenom(Expr e) {
		set(1,e);
	}
	
	@Override
	public Var getVar(){
		if(isType("diff") || isType("integrate") || isType("solve")){
			return (Var) get(1);
		}else if(isType("integrateOver")){
			return (Var) get(3);
		}else if(isType("limit")) {
			return Limit.getVar(this);
		}
		
		throw new RuntimeException(typeName()+"invalid type for getVar()");
	}
	
	public Expr getComparison(){
		if(isType("solve")){
			return get();
		}
		
		throw new RuntimeException(typeName()+"invalid type for getComparison()");
	}
	
	@Override
	public boolean isCommutative(){
		return behavior.commutative;
	}

	@Override
	public String help() {
		if(behavior.helpMessage == null) {
			return "no help message";
		}else {
			return behavior.helpMessage;
		}
	}
	
}
