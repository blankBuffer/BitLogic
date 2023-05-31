package cas.primitive;

import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;

public class Less{
	
	public static Func.FuncLoader lessLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.toStringMethod = new Func.ToString() {
				
				@Override
				public String generateString(Func owner) {
					String out = "";
					out+=getLeftSide(owner).toString();
					out+='<';
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
	
	public static Expr getLeftSide(Func less) {
		return less.get(0);
	}
	public static Expr getRightSide(Func less) {
		return less.get(1);
	}
	
	public static void setLeftSide(Func less,Expr expr) {
		less.set(0,expr);
	}
	public static void setRightSide(Func less,Expr expr) {
		less.set(1,expr);
	}
}
