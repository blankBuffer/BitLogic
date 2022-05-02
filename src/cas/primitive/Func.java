package cas.primitive;

import java.io.Serializable;

import cas.ComplexFloat;
import cas.Expr;

public class Func extends Expr{

	private static final long serialVersionUID = -3146654431684411030L;
	public String name;
	public int numberOfParams;
	
	void init(){
	}
	
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
	public Sequence ruleSequence = sequence();
	public FloatFunc toFloatFunc = null;
	public String helpMessage = "no help message";
	
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
		out.toFloatFunc = toFloatFunc;
		return out;
	}
	
	public static abstract class FloatFunc implements Serializable{
		private static final long serialVersionUID = 7831115405347619209L;

		public abstract ComplexFloat convertToFloat(ExprList varDefs,Func owner);
	}
	
	public static FloatFunc nothingFunc = new Func.FloatFunc() {//return whatever is inside
		private static final long serialVersionUID = 1L;

		@Override
		public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
			return owner.get().convertToFloat(varDefs);
		}
	};
	
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		if(toFloatFunc != null) {
			return toFloatFunc.convertToFloat(varDefs,this);
		}
		return new ComplexFloat(0,0);
	}
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}

	@Override
	public String typeName() {
		return name;
	}

	@Override
	public String help() {
		return helpMessage;
	}
	
}
