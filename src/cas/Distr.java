package cas;

public class Distr extends Expr{

	
	private static final long serialVersionUID = -1352926948237577310L;

	public Distr(Expr expr) {
		add(expr);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		toBeSimplified = toBeSimplified.get();
		
		toBeSimplified = generalDistr(toBeSimplified,settings);
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	Expr generalDistr(Expr expr,Settings settings) {//2*(x+y) -> 2*x+2*y
		if(expr instanceof Prod) {
			Expr needsExpand = null;
			Prod prod = null;
			for(int i = 0;i<expr.size();i++) {
				if(expr.get(i) instanceof Sum) {
					needsExpand = expr.get(i).copy();
					prod = (Prod)expr.copy();
					prod.remove(i);
					break;
				}
			}
			if(needsExpand != null) {
				if(needsExpand instanceof Sum) {
					for(int i = 0;i<needsExpand.size();i++) {
						needsExpand.set(i, distr(prod(prod,needsExpand.get(i))));
					}
				}else if(needsExpand instanceof Power) {
					needsExpand = distr(prod(prod,distr(needsExpand) ));
				}
				return needsExpand.simplify(settings);
			}
		}else if(expr instanceof Div) {//(x+y)/3 -> x/3+y/3
			Div casted = (Div)expr;
			
			if(casted.getNumer() instanceof Sum) {
				for (int i = 0;i < casted.getNumer().size();i++) {
					casted.getNumer().set(i, div(casted.getNumer().get(i),casted.getDenom().copy()));
				}
				return casted.getNumer().simplify(settings);
				
			}
			
		}
		return expr;
	}
	
	@Override
	public Expr copy() {
		Expr out = new Distr(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="distr(";
		out+=get();
		out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Distr) return get().equalStruct(other.get());
		return false;
	}

	@Override
	public long generateHash() {
		return get().generateHash()+8152371823985037265L;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return get().convertToFloat(varDefs);
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Distr) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}
}
