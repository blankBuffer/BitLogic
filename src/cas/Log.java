package cas;
import java.math.BigInteger;


public class Log extends Expr{
	
	
	private static final long serialVersionUID = 8168024064884459716L;
	static Equ log1To0 = (Equ)createExpr("ln(1)=0");
	static Equ logETo1 = (Equ)createExpr("ln(e)=1");
	static Equ powToProd = (Equ)createExpr("ln(a^b)=b*ln(a)");
	
	public Log(Expr e){
		add(e);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);
		
		toBeSimplified = toBeSimplified.modifyFromExample(log1To0,settings);
		toBeSimplified = toBeSimplified.modifyFromExample(logETo1,settings);
		
		if(toBeSimplified instanceof Log) logOfPerfectPower((Log)toBeSimplified);//ln(8) ->ln(2^3)
		
		//if(toBeSimplified instanceof Log) toBeSimplified = factorExpo((Log)toBeSimplified,settings);
		
		if(toBeSimplified instanceof Log) toBeSimplified = toBeSimplified.modifyFromExample(powToProd,settings);
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}
	/*
	Expr factorExpo(Log log,Settings settings){
		if(log.get() instanceof Prod) {
			
			boolean allPowers = true;
			Prod innerProd = (Prod)log.get();
			for(int i = 0;i<innerProd.size();i++) {
				if(!(innerProd.get(i) instanceof Power)) {//needs to be all powers
					allPowers = false;
					break;
				}
			}
			
			Prod factors = new Prod();
			
			if(allPowers) {//only can factor common exponent integer if they are all powers
				//numerator factors
				Num leadingCoefExpo = innerProd.get().get(1).getCoefficient();//get(1) refers to exponent
				BigInteger numFactor = leadingCoefExpo.value.abs();
				boolean neg = leadingCoefExpo.negative();
				
				long hash = innerProd.get().generateHash();
				
				for(int i = 1;i<innerProd.size();i++) {
					Num coef = innerProd.get(i).get(1).getCoefficient();
					numFactor = coef.value.abs().gcd(numFactor);
					
					long currentHash = innerProd.get(i).generateHash();
					if(currentHash < hash) {
						hash = currentHash;
						neg = coef.negative();
					}
				}
				
				if(!numFactor.equals(BigInteger.ONE) && !neg) factors.add(num(numFactor));
				if(neg) factors.add(num(numFactor.negate()));
				
			}
			
			//(we don't want any fractional powers)
			ExprList denoms = new ExprList();
			BigInteger denomNum = BigInteger.ONE;
			//extracting denominators
			for(int i = 0;i<innerProd.size();i++) {
				if(innerProd.get(i) instanceof Power) {
					Power pow = (Power)innerProd.get(i);
					
					Prod prodExpo = null;
					if(pow.getExpo() instanceof Prod) prodExpo = (Prod)pow.getExpo();
					else {
						prodExpo = new Prod();
						prodExpo.add(pow.getExpo());
					}
					
					for(int j = 0;j<prodExpo.size();j++) {//going through exponent product
						
						if(!(prodExpo.get(j) instanceof Num) && !(prodExpo.get(j) instanceof Power)) {//skip numbers since we already did that
							prodExpo.set(j, pow(prodExpo.get(j),num(1)));
						}
						
						if(prodExpo.get(j) instanceof Power) {
							
							Power currentPow = (Power)prodExpo.get(j);
							
							if(invObj.fastSimilarStruct(currentPow) && currentPow.getBase() instanceof Num) {
					
								denomNum = gcm(denomNum,  ((Num)currentPow.getBase()).value  );
							}
							
							if( currentPow.getExpo() instanceof Num && currentPow.getExpo().negative()) {
								boolean alreadyExists = false;
								Num lowestExpo = (Num)( currentPow).getExpo();
								
								if(denoms.contains(currentPow)) alreadyExists = true;//trivial check
								else {//minimize the base of the current power in the list suppose current = x^-3 and denoms has x^-2. change item in denoms to x^-3
									for(int k = 0;k<denoms.size();k++) {
										Power denom = (Power) denoms.get(k);
										
										if(denom.getBase().equalStruct( currentPow.getBase())) {
											denom.setExpo(num( ((Num)denom.getExpo()).value.min( ((Num)currentPow.getExpo()).value )  ));
										}
										alreadyExists = true;
										
									}
								}
								
								if(!alreadyExists) {
									denoms.add(pow( currentPow.getBase(),lowestExpo));
								}
								
								
								
							}
							
						}
					}
					
				}
			}
			if(!denomNum.equals(BigInteger.ONE)) factors.add(inv(num(denomNum)));
			for(int i = 0;i<denoms.size();i++) factors.add(denoms.get(i));
			
			//factoring common exponents
			
			//
			
			if(factors.size()>0) {
				Expr out = prod(ln( pow(log.get(),inv(factors))),factors);
				return out.simplify(settings);
			}
			
			
		}
		return log;
	}
	*/
	
	void logOfPerfectPower(Log log) {
		if(log.get() instanceof Num) {// example log(25) -> 2*ln(5)
			Num casted = (Num)log.get();
			Power perfectPower = perfectPower(casted);
			if(((Num)perfectPower.getExpo()).realValue.equals(BigInteger.ONE)) return;
			
			log.set(0, perfectPower);
		}else if(log.get() instanceof Prod) {//ln(8*x) -> ln(2^3*x) , this will be reverted in later steps
			Prod innerProd = (Prod)log.get();
			for(int i = 0;i<innerProd.size();i++) {
				if(innerProd.get(i) instanceof Num) {
					Num casted = (Num)innerProd.get(i);
					
					Power perfectPower = perfectPower(casted);
					if(((Num)perfectPower.getExpo()).realValue.equals(BigInteger.ONE)) continue;
					innerProd.set(i, perfectPower);
					
				}
			}
			
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
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		return new Log(get().replace(equs));
	}

	@Override
	public long generateHash() {
		long childHash = get().generateHash();
		return childHash+2873468283392719732L;//just a random numbers
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.ln(get().convertToFloat(varDefs));
	}

}
