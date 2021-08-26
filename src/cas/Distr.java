package cas;

import java.math.BigInteger;
import java.util.ArrayList;

public class Distr extends Expr{

	
	private static final long serialVersionUID = -1352926948237577310L;

	public Distr(Expr expr) {
		add(expr);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		settings = new Settings(settings);
		settings.factor = false;
		toBeSimplified.simplifyChildren(settings);
		
		toBeSimplified = toBeSimplified.get();
		
		if(settings.distr) {
			toBeSimplified = generalDistr(toBeSimplified,settings);
			if(settings.powExpandMode) toBeSimplified = powExpand(toBeSimplified,settings);
		}
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	Expr generalDistr(Expr expr,Settings settings) {//2*(x+y) -> 2*x+2*y
		if(expr instanceof Prod) {
			Expr needsExpand = null;
			Prod prod = null;
			for(int i = 0;i<expr.size();i++) {
				if(expr.get(i) instanceof Sum || (settings.powExpandMode && expr.get(i) instanceof Power && ((Power)expr.get(i)).getBase() instanceof Sum &&  ((Power)expr.get(i)).getExpo() instanceof Num && !((Power)expr.get(i)).getExpo().negative() )) {
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
		}
		return expr;
	}
	
	public static Expr powExpand(Expr expr,Settings settings) {
		if(expr instanceof Power) {
			Power pow = (Power)expr;
			if(pow.getExpo() instanceof Num && pow.getBase() instanceof Sum && pow.getBase().size() == 2) {
				Num expo  = (Num)pow.getExpo();
				if(expo.value.compareTo(BigInteger.valueOf(8)) == -1 && expo.value.compareTo(BigInteger.ONE) == 1) {
					settings = new Settings(settings);
					settings.powExpandMode = true;
					settings.factor = false;
					settings.distr = true;
					
					int expoInt = expo.value.intValue();
					
					Prod p = new Prod();
					for(int i = 0;i<expoInt;i++) {
						p.add(pow.getBase());
					}
					Sum out = (Sum)distr(p).simplify(settings);
					
					settings.powExpandMode = false;
					for(int i = 0;i<out.size();i++) {
						if(out.get(i) instanceof Prod) {
							out.get(i).flags.simple = false;
							out.set(i, out.get(i).simplify(settings));
						}
					}
					return out;
				}
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
	public Expr replace(ArrayList<Equ> equs) {
		for(Equ e:equs) if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		return distr(get().replace(equs));
	}

	@Override
	public double convertToFloat(ExprList varDefs) {
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
