package cas.primitive;

import cas.Expr;

/*
 * the become expression type is what's used to create rule mappings, the arrow -> shows how the left argument "becomes"
 * the right argument
 */

public class Becomes{

	public static Func.FuncLoader becomesLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.toStringMethod = new Func.ToString() {
				
				@Override
				public String generateString(Func owner) {
					return getLeftSide(owner)+"->"+getRightSide(owner);
				}
			};
		}
	};
	
	public static Expr getLeftSide(Func becomes) {
		assert becomes.typeName().equals("becomes") : "expected a becomes";
		return becomes.get(0);
	}
	public static Expr getRightSide(Func becomes) {
		assert becomes.typeName().equals("becomes") : "expected a becomes";
		return becomes.get(1);
	}
	
	public static void setLeftSide(Func becomes,Expr expr) {
		assert becomes.typeName().equals("becomes") : "expected a becomes";
		becomes.flags.simple = false;
		becomes.set(0,expr);
	}
	public static void setRightSide(Func becomes,Expr expr) {
		assert becomes.typeName().equals("becomes") : "expected a becomes";
		becomes.flags.simple = false;
		becomes.set(1,expr);
	}
}
