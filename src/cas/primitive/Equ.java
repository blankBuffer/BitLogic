package cas.primitive;

import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;

public class Equ{
	public static Func.FuncLoader equLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.toStringMethod = new Func.ToString() {
				
				@Override
				public String generateString(Func owner) {
					String out = "";
					out+=getLeftSide(owner).toString();
					out+='=';
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
	
	
	public static Expr getLeftSide(Func equ) {
		assert equ.typeName().equals("equ") : "expected a equ";
		return equ.get(0);
	}
	public static Expr getRightSide(Func equ) {
		assert equ.typeName().equals("equ") : "expected a equ";
		return equ.get(1);
	}
	
	public static void setLeftSide(Func equ,Expr expr) {
		equ.set(0,expr);
	}
	public static void setRightSide(Func equ,Expr expr) {
		equ.set(1,expr);
	}
}
