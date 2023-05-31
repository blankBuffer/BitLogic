package cas.primitive;

import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;

public class Greater{
	
	public static Func.FuncLoader greaterLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.toStringMethod = new Func.ToString() {
				
				@Override
				public String generateString(Func owner) {
					String out = "";
					out+=getLeftSide(owner).toString();
					out+='>';
					out+=getRightSide(owner).toString();
					return out;
				}
			};
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return getRightSide(owner).convertToFloat(varDefs);//usually the solution is on the right side of the equation
				}
			};
		}
	};
	
	public static Expr getLeftSide(Func greater) {
		return greater.get(0);
	}
	public static Expr getRightSide(Func greater) {
		return greater.get(1);
	}
	
	public static void setLeftSide(Func greater,Expr expr) {
		greater.set(0,expr);
	}
	public static void setRightSide(Func greater,Expr expr) {
		greater.set(1,expr);
	}
}
