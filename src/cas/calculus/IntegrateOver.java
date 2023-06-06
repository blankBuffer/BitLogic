package cas.calculus;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.primitive.Equ;
import cas.primitive.FloatExpr;
import cas.primitive.Var;

import static cas.Cas.*;

public class IntegrateOver{
	
	public static Func.FuncLoader integrateOverLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			
			owner.behavior.helpMessage = "Integration over a domain.\n"
					+ "The frist two parameters are the min and max domain the third parameter is the expression and the last is the variable.\n"
					+ "Example, integrateOver(1,2,x^2,x) returns 7/3\n"
					+ "Example, integrateOver(0,1,x*sqrt(1+x),x) returns (4*sqrt(2))/15+4/15";
			
			Rule definiteIntegral = new Rule("integral with bounds"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func defInt = (Func)e;
					
					Expr indefInt = integrate(getExpr(defInt),defInt.getVar()).simplify(casInfo);
					
					if(!indefInt.containsType("integrate")) {
						return sub(indefInt.replace(equ(  defInt.getVar() , getMax(defInt) )),indefInt.replace(equ(  defInt.getVar() , getMin(defInt) ))).simplify(casInfo);
					}
					
					return defInt;
				}
			};
			
			owner.behavior.rule = new Rule(new Rule[]{
				definiteIntegral		
			},"main sequence");
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					int n = 32;//should be an even number
					ComplexFloat sum = new ComplexFloat(0,0);
					ComplexFloat min = getMin(owner).convertToFloat(varDefs),max = getMax(owner).convertToFloat(varDefs);
					ComplexFloat step = ComplexFloat.div( ComplexFloat.sub(max, min),new ComplexFloat(n,0));
					
					Func vDefEqu = equ(owner.getVar(),floatExpr(min));
					Func varDefs2 = (Func) varDefs.copy();
					
					for(int i = 0;i < varDefs2.size();i++) {
						Func tempEqu = (Func)varDefs2.get(i);
						Var v = (Var)Equ.getLeftSide(tempEqu);
						if(v.equals(owner.getVar())) {
							varDefs2.remove(i);
							break;
						}
					}
					varDefs2.add(vDefEqu);
					
					sum = ComplexFloat.add(sum, getExpr(owner).convertToFloat(varDefs2));
					((FloatExpr)Equ.getRightSide(vDefEqu)).value = ComplexFloat.add(((FloatExpr)Equ.getRightSide(vDefEqu)).value, step);
					
					for(int i = 1;i<n;i++) {
						sum=ComplexFloat.add(sum,  ComplexFloat.mult( new ComplexFloat(((i%2)*2+2),0) ,getExpr(owner).convertToFloat(varDefs2)) );
						((FloatExpr)Equ.getRightSide(vDefEqu)).value = ComplexFloat.add(((FloatExpr)Equ.getRightSide(vDefEqu)).value, step);
					}
					
					sum=ComplexFloat.add(sum, getExpr(owner).convertToFloat(varDefs2));
					
					return ComplexFloat.mult(sum,ComplexFloat.mult(step, new ComplexFloat(1.0/3.0,0)));
				}
			};
		}
	};
	
	public static Expr getMin(Func integrateOver) {
		return integrateOver.get(0);
	}
	public static Expr getMax(Func integrateOver) {
		return integrateOver.get(1);
	}
	public static Expr getExpr(Func integrateOver) {
		return integrateOver.get(2);
	}

}
