package cas;

import java.util.ArrayList;

public class Acos extends Expr{
	
	private static final long serialVersionUID = 3855238699397076495L;

	Acos(){}
	public Acos(Expr expr) {
		add(expr);
	}
	
	static Rule containsInverse = new Rule("acos(cos(x))=x","acos contains inverse",Rule.EASY);
	static Rule containsSin = new Rule("acos(sin(x))=-x+pi/2","acos contains inverse",Rule.UNCOMMON);
	
	static Rule trigCompressInner = new Rule("trig compress inner",Rule.TRICKY){
		@Override
		public void init(){
			example = "acos(2*sin(x)*cos(x))=acos(sin(2*x))";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Acos acos = null;
			if(e instanceof Acos){
				acos = (Acos)e;
			}else{
				return e;
			}
			
			Acos result = (Acos)acos.copy();
			result.set(0, trigCompress(result.get(),settings));
			if(!result.equalStruct(acos)){
				verboseMessage(e,result);
				return result;
			}
			
			return acos;
		}
		
	};
	
	static Rule negativeInner = new Rule("arccos of negative value",Rule.UNCOMMON){
		@Override
		public void init(){
			example = "acos(-x)=-acos(x)+pi";
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Acos acos = null;
			if(e instanceof Acos){
				acos = (Acos)e;
			}else{
				return e;
			}
			
			if(acos.get().negative()){
				Expr result = sum(neg(acos(acos.get().abs(settings))),pi()).simplify(settings);
				verboseMessage(e,result);
				return result;
			}
			return acos;
		}
	};
	
	static Rule unitCircle = new Rule("unit circle for arccos",Rule.UNCOMMON){
		ArrayList<Equ> unitCircleTable;
		
		@Override
		public void init(){
			example = "acos(0)=pi/2";
			unitCircleTable = new ArrayList<Equ>();
			unitCircleTable.add((Equ)createExpr("acos(0)=pi/2"));
			unitCircleTable.add((Equ)createExpr("acos(1)=0"));
			unitCircleTable.add((Equ)createExpr("acos(sqrt(2)/2)=pi/4"));
			unitCircleTable.add((Equ)createExpr("acos(1/2)=pi/3"));
			unitCircleTable.add((Equ)createExpr("acos(sqrt(3)/2)=pi/6"));
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Acos acos = null;
			if(e instanceof Acos){
				acos = (Acos)e;
			}else{
				return e;
			}
			
			Expr result = acos;
			for(int i = 0;i<unitCircleTable.size();i++) {
				result = acos.modifyFromExample(unitCircleTable.get(i), settings);
				if(!(result instanceof Acos)){
					verboseMessage(e,result);
					break;
				}
			}
			return result;
		}
		
	};
	
	static Rule[] ruleSequence = {
			trigCompressInner,
			negativeInner,
			containsInverse,
			containsSin,
			unitCircle,
	};
	
	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		for (Rule r:ruleSequence){
			toBeSimplified = r.applyRuleToExpr(toBeSimplified, settings);
		}
		
		toBeSimplified.flags.simple = true;//result is simplified and should not be simplified again
		return toBeSimplified;
	}
	
	@Override
	public String toString() {
		return "acos("+get()+")";
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.acos(get().convertToFloat(varDefs));
	}
}
