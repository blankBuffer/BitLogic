package cas.primitive;

import cas.base.Expr;
import cas.base.Func;

/*
 * the become expression type is what's used to create rule mappings, the arrow -> shows how the left argument "becomes"
 * the right argument
 */

public class Becomes{

	public static Func.FuncLoader becomesLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.helpMessage = "Becomes operator.\n"
					+ "Can be used to represent a transformation or aproaching a value like with limits.\n"
					+ "Example of aproaching syntax, limit(sin(x)/x,x->0) returns 1\n"
					+ "Example of transformation, diff(atan(a),x)->diff(a,x)/(a^2+1)";
			
			owner.behavior.toStringMethod = new Func.ToString() {
				
				@Override
				public String generateString(Func owner) {
					return getLeftSide(owner)+"->"+getRightSide(owner);
				}
			};
		}
	};
	
	public static Expr getLeftSide(Func becomes) {
		assert becomes.isType("becomes") : "expected a becomes";
		return becomes.get(0);
	}
	public static Expr getRightSide(Func becomes) {
		assert becomes.isType("becomes") : "expected a becomes";
		return becomes.get(1);
	}
	
	public static void setLeftSide(Func becomes,Expr expr) {
		assert becomes.isType("becomes") : "expected a becomes";
		becomes.set(0,expr);
	}
	public static void setRightSide(Func becomes,Expr expr) {
		assert becomes.isType("becomes") : "expected a becomes";
		becomes.set(1,expr);
	}
}
