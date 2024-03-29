package cas.primitive;

import static cas.Cas.*;
import cas.base.Expr;
import cas.base.Func;

/*
 * this is a list of items where the order MATTERS and CAN have repeats
 */
public class Sequence{
	
	public static Func.FuncLoader sequenceLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.toStringMethod = new Func.ToString() {
				
				@Override
				public String generateString(Func owner) {
					String out = "";
					out+="[";
					for(int i = 0;i<owner.size();i++) {
						out+=owner.get(i);
						if(i != owner.size()-1) out+=",";
					}
					out+="]";
					return out;
				}
			};
		}
	};
	
	public static Func cast(Expr e) {
		if(e == null) return sequence();
		if(e.isType("sequence")) return (Func)e;
		if(e instanceof Params || e.isType("set")) {
			Func out = sequence();
			for(int i = 0;i<e.size();i++) {
				out.add(e.get(i));
			}
			return out;
		}
		return sequence(e);
	}
}
