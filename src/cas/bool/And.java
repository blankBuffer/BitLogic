package cas.bool;

import cas.ComplexFloat;
import cas.Expr;
import cas.Cas;
import cas.Rule;
import cas.CasInfo;
import cas.primitive.ExprList;
import cas.primitive.Func;

public class And{
	
	public static Func.FuncLoader andLoader = new Func.FuncLoader() {
		@Override
		public void load(Func owner) {
			
			Rule andContainsAnd = new Rule("and contains and"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func and = (Func)e;
					for(int i = 0;i<and.size();i++){
						if(and.get(i).typeName().equals("and")){
							Func subAnd = (Func)and.get(i);
							and.remove(i);
							i--;
							for(int j = 0;j<subAnd.size();j++){
								and.add(subAnd.get(j));
							}
						}
					}
					return and;
				}
				
			};
			
			Rule nullRule = new Rule("null rule"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func and = (Func)e;
					for(int i = 0;i<and.size();i++){
						if(and.get(i).equals(BoolState.FALSE)){
							Expr result = bool(false);
							return result;
						}
					}
					return and;
				}
			};
			
			Rule removeTrues = new Rule("remove trues"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func and = (Func)e;
					for(int i = 0;i<and.size();i++){
						if(and.get(i).equals(BoolState.TRUE)){
							and.remove(i);
							i--;
						}
					}
					return and;
				}
			};
			
			Rule complement = new Rule("has complement"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func and = (Func)e;
					for(int i = 0;i<and.size();i++){
						Expr current = and.get(i);
						Expr complement = current.typeName().equals("not") ? current.get() : not(current);
						
						for(int j = i+1;j<and.size();j++){
							Expr other = and.get(j);
							
							if(other.equals(complement)){
								Expr result = bool(false);
								return result;
							}
						}
						
					}
					return and;
				}
			};
			
			Rule removeDuplicates = new Rule("remove duplicates"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func and = (Func)e;
					for(int i = 0;i<and.size();i++){
						Expr current = and.get(i);
						
						for(int j = i+1;j<and.size();j++){
							Expr other = and.get(j);
							
							if(other.equals(current)){
								and.remove(j);
								j--;
							}
						}
						
					}
					return and;
				}
			};
			
			Rule distribute = new Rule("and contains or"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func and = (Func)e;
					for(int i = 0;i<and.size();i++){
						if(and.get(i).typeName().equals("or")){
							Func subOr = (Func)and.get(i).copy();
							Func remAnd = (Func)and.copy();
							remAnd.remove(i);
							
							for(int j = 0;j<subOr.size();j++){
								Expr subExpr = subOr.get(j);
								
								if(subExpr.typeName().equals("and")){
									Func remCopyAnd = (Func)remAnd.copy();
									for(int k = 0;k<subExpr.size();k++){
										remCopyAnd.add(subExpr.get(k));
									}
									subOr.set(j, remCopyAnd);
								}else{
									Func remCopyAnd = (Func)remAnd.copy();
									remCopyAnd.add(subExpr);
									subOr.set(j, remCopyAnd);
								}
								
								
							}
							
							Expr result = subOr.simplify(casInfo);
							return result;
						}
					}
					return and;
				}
			};
			
			Rule aloneAnd = new Rule("and has one element"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func and = (Func)e;
					Expr result = null;
					if(and.size() == 0){
						result = bool(true);
					}else if(and.size() == 1){
						result = and.get();
					}
					if(result != null){
						return result;
					}
					return and;
				}
			};
			
			owner.behavior.commutative = true;
			
			owner.behavior.rule = new Rule(new Rule[]{
				andContainsAnd,
				removeDuplicates,
				nullRule,
				removeTrues,
				complement,
				distribute,
				aloneAnd
			},"main sequence");
			owner.behavior.rule.init();
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(ExprList varDefs, Func owner) {
					boolean state = true;
					for(int i = 0;i<owner.size();i++){
						state &= Math.abs(owner.get(i).convertToFloat(varDefs).real)>0.5;
					}
					double res = state ? 1.0 : 0.0;
					return new ComplexFloat(res,0);
				}
			};
			
			owner.behavior.toStringMethod = new Func.ToString() {
				@Override
				public String generateString(Func owner) {
					String out = "";
					if(owner.size() < 2) out+="alone and:";
					for(int i = 0;i<owner.size();i++){
						boolean paren = owner.get(i).typeName().equals("or") || owner.get(i).typeName().equals("and");
						if(paren) out+="(";
						out+=owner.get(i);
						if(paren) out+=")";
						if(i!=owner.size()-1) out+="&";
					}
					return out;
				}
			};
		}
	};
	
	public static Func cast(Expr e){
		if(e.typeName().equals("and")){
			return (Func)e;
		}
		return Cas.and(e);
	}

	public static Expr unCast(Expr e) {
		if(e.typeName().equals("and") && e.size() == 1) {
			return e.get();
		}
		return e;
	}
}
