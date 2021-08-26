package cas;
import java.math.BigInteger;
import java.util.ArrayList;
public class Log extends Expr{
	
	
	private static final long serialVersionUID = 8168024064884459716L;
	static Equ rule0 = equ( ln(num(1)) , num(0) );// ln(1) -> 0
	static Equ rule1 = equ( ln(e()) , num(1) );// ln(e) -> 1
	static Equ rule2 = equ( ln(pow( var("a"),var("b") )) , prod(var("b"),ln(var("a"))) );//ln(a^b) -> b*ln(a)
	
	public Log(Expr e){
		add(e);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		toBeSimplified = toBeSimplified.modifyFromExample(rule0,settings);
		toBeSimplified = toBeSimplified.modifyFromExample(rule1,settings);
		
		if(toBeSimplified instanceof Log) logOfPerfectPower((Log)toBeSimplified);//ln(8) ->ln(2^3)
		
		toBeSimplified = toBeSimplified.modifyFromExample(rule2,settings);
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}
	
	void logOfPerfectPower(Log log) {
		if(log.get() instanceof Num) {
			Num casted = (Num)log.get();
			Power perfectPower = perfectPower(casted);
			if(((Num)perfectPower.getExpo()).value.equals(BigInteger.ONE)) return;
			
			log.set(0, perfectPower);
		}
	}

	@Override
	public Expr copy() {
		Log out = new Log(get().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="ln(";
		out+=get().toString();
		out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Log) {
			if(other.get().equalStruct(get())) return true;
		}
		return false;
	}

	@Override
	boolean similarStruct(Expr other,boolean checked) {
		
		if(other instanceof Log) {
			if(!checked) if(checkForMatches(other) == false) return false;
			if(get().fastSimilarStruct(other.get())) return true;
		}
		return false;
	}

	@Override
	public Expr replace(ArrayList<Equ> equs) {
		for(Equ e:equs) if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		return new Log(get().replace(equs));
	}

	@Override
	public long generateHash() {
		long childHash = get().generateHash();
		return childHash+2873468283392719732L;//just a random numbers
	}

	@Override
	public double convertToFloat(ExprList varDefs) {
		return Math.log(get().convertToFloat(varDefs));
	}

}
