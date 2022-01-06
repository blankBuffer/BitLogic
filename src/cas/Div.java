package cas;

import java.math.BigInteger;

public class Div extends Expr{
	
	private static final long serialVersionUID = -1262460519269095855L;
	public static Equ overOne = (Equ)createExpr("a/1=a");
	public static Equ zeroInNum = (Equ)createExpr("0/a=0");

	Div(){}//
	public Div(Expr num,Expr den){
		add(num);
		add(den);
	}
	
	public Expr getNumer() {
		return get();
	}
	public Expr getDenom() {
		return get(1);
	}
	
	public void setNumer(Expr e) {
		set(0,e);
	}
	
	public void setDenom(Expr e) {
		set(1,e);
	}

	@Override
	public Expr simplify(Settings settings) {
		
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		((Div)toBeSimplified).setNumer(factor(((Div)toBeSimplified).getNumer()).simplify(settings));//factor numerator
		((Div)toBeSimplified).setDenom(factor(((Div)toBeSimplified).getDenom()).simplify(settings));//factor denominator
		
		toBeSimplified = trigExpandElements.applyRuleToExpr(toBeSimplified, settings);
		
		divContainsDiv((Div)toBeSimplified,settings);//(a/b)/(c/d) -> (a*d)/(b*c)
		
		cancelOutTerms((Div)toBeSimplified,settings);
		
		reduceFraction((Div)toBeSimplified);//2/4 -> 1/2
		
		toBeSimplified = toBeSimplified.modifyFromExample( overOne , settings);//x/1 -> x
		toBeSimplified = toBeSimplified.modifyFromExample( zeroInNum , settings);//x/1 -> x
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	static Rule trigExpandElements = new Rule("trig expand elements div",Rule.TRICKY){
		@Override
		public void init(){
			example = "sin(2*x)/sin(x)=(2*sin(x)*cos(x))/sin(x)";
		}
		@Override
		public Expr applyRuleToExpr(Expr e,Settings settings){
			Div div = null;
			if(e instanceof Div){
				div = (Div)e;
			}else{
				return e;
			}
			
			boolean prodInTrig = false;
			boolean nonProdInTrig = false;
			if(div.getNumer() instanceof Prod){
				prodInTrig |= Prod.foundProdInTrigInProd((Prod)div.getNumer());
				nonProdInTrig |= Prod.foundNonProdInTrigInProd((Prod)div.getNumer());
			}else if(div.getNumer() instanceof Sin || div.getNumer() instanceof Cos || div.getNumer() instanceof Tan){
				if(div.getNumer().get() instanceof Prod){
					prodInTrig = true;
				}else{
					nonProdInTrig = true;
				}
			}
			if(div.getDenom() instanceof Prod){
				prodInTrig |= Prod.foundProdInTrigInProd((Prod)div.getDenom());
				nonProdInTrig |= Prod.foundNonProdInTrigInProd((Prod)div.getDenom());
			}else if(div.getDenom() instanceof Sin || div.getDenom() instanceof Cos || div.getDenom() instanceof Tan){
				if(div.getDenom().get() instanceof Prod){
					prodInTrig = true;
				}else{
					nonProdInTrig = true;
				}
			}
			
			if(prodInTrig && nonProdInTrig){
				Expr original = div.copy();
				
				div.setNumer(trigExpand(div.getNumer(),settings));
				div.setDenom(trigExpand(div.getDenom(),settings));
				
				verboseMessage(original,div);
			}
			
			return div;
		}
	};
	
	static void cancelOutTerms(Div div,Settings settings) {
		
		Prod numerProd = Prod.cast(div.getNumer()), denomProd = Prod.cast(div.getDenom());
		
		outer:for(int i = 0;i < numerProd.size();i++) {
			
			inner:for(int j = 0;j < denomProd.size();j++) {
				
				Expr numerTerm = numerProd.get(i);
				Expr denomTerm = denomProd.get(j);
				
				if(numerTerm instanceof Num) {
					continue outer;
				}else if(denomTerm instanceof Num) {
					continue inner;
				}
				
				if(numerTerm.equalStruct(denomTerm)) {
					numerProd.remove(i);
					denomProd.remove(j);
					i--;
					continue outer;
				}
				Power numerPower = Power.cast(numerTerm);
				Power denomPower =  Power.cast(denomTerm);
				
				if(numerPower.getBase().equalStruct(denomPower.getBase())) {//both bases are the same x^2/x^3
					Expr newExpo = sub(numerPower.getExpo(),denomPower.getExpo()).simplify(settings);
					if(newExpo.negative()) {
						newExpo = neg(newExpo).simplify(settings);//flip it
						denomPower.setExpo(newExpo);
						denomProd.set(j, denomPower.simplify(settings));
						numerProd.remove(i);
						i--;
						continue outer;
					}
					numerPower.setExpo(newExpo);
					numerProd.set(i, numerPower.simplify(settings));
					denomProd.remove(j);
					j--;
					continue inner;
					
				}else if(numerPower.getExpo().equalStruct(denomPower.getExpo()) && numerPower.getBase() instanceof Num && denomPower.getBase() instanceof Num){//both denoms are the same
					Expr resTest = div(numerPower.getBase(),denomPower.getBase()).simplify(settings);
					if(resTest instanceof Num) {//10^x/2^x -> 5^x
						numerProd.set(i, pow(resTest,numerPower.getExpo()));
						denomProd.remove(j);
						j--;
						continue inner;
					}else if(resTest instanceof Div) {//2^x/10^x -> 1/5^x
						if(((Div)resTest).getNumer().equalStruct(Num.ONE)) {
							denomProd.set(i, pow( ((Div)resTest).getDenom() ,numerPower.getExpo()));
							numerProd.remove(i);
							i--;
							continue outer;
						}
					}
				}
				
			}
			
		}
		
			
		div.setNumer(Prod.unCast(numerProd));
		div.setDenom(Prod.unCast(denomProd));
	}
	
	static void divContainsDiv(Div div,Settings settings) {//all 3 cases (a/b)/(c/d) or (a/b)/c or a/(b/c)
		boolean numerIsDiv = div.getNumer() instanceof Div;
		boolean denomIsDiv = div.getDenom() instanceof Div;
		
		if(numerIsDiv && denomIsDiv) {
			Div numerDiv = (Div)div.getNumer();
			Div denomDiv = (Div)div.getDenom();
			
			Expr newNumer = prod(numerDiv.getNumer(), denomDiv.getDenom());
			Expr newDenom = prod(numerDiv.getDenom(), denomDiv.getNumer());
			
			
			div.setNumer(newNumer.simplify(settings));
			div.setDenom(newDenom.simplify(settings));
		}else if(numerIsDiv) {
			Div numerDiv = (Div)div.getNumer();
			
			Expr newDenom = prod(numerDiv.getDenom(),div.getDenom());
			Expr newNumer = numerDiv.getNumer();
			
			div.setNumer(newNumer.simplify(settings));
			div.setDenom(newDenom.simplify(settings));
		}else if(denomIsDiv) {
			
			Div denomDiv = (Div)div.getDenom();
			
			Expr newNumer = prod(div.getNumer(),denomDiv.getDenom());
			Expr newDenom = denomDiv.getNumer();
			
			div.setNumer(newNumer.simplify(settings));
			div.setDenom(newDenom.simplify(settings));
		}
	}
	
	static void reduceFraction(Div div) {//rationalizes complex fraction and reduces it
		//get numerator
		Num numer = num(1);
		int indexOfNumer = -1;//index if the numerator is a product
		boolean numerIsProd = false;
		boolean numerIsNum = false;
		
		if(div.getNumer() instanceof Num) {
			numer = (Num)div.getNumer();
			numerIsNum = true;
		}else if(div.getNumer() instanceof Prod) {
			for(int i = 0;i<div.getNumer().size();i++) {
				if(div.getNumer().get(i) instanceof Num) {
					numerIsProd = true;
					indexOfNumer = i;
					numer = (Num)div.getNumer().get(i);
					break;
				}
			}
		}
		//get denominator
		
		Num denom = null;
		int indexOfDenom = -1;
		boolean denomIsProd = false;
		
		if(div.getDenom() instanceof Num) {
			denom = (Num)div.getDenom();
		}else if(div.getDenom() instanceof Prod) {
			for(int i = 0;i<div.getDenom().size();i++) {
				if(div.getDenom().get(i) instanceof Num) {
					denomIsProd = true;
					indexOfDenom = i;
					denom = (Num)div.getDenom().get(i);
					break;
				}
			}
		}
		
		if(denom != null) {
			//rationalize
			
			if(denom.isComplex()) {
				numer = numer.multNum(denom.complexConj());
				denom = denom.multNum(denom.complexConj());
			}
			
			//reduce by gcd
			
			BigInteger gcd = numer.gcd().gcd(denom.gcd());
			numer = numer.divideNum(gcd);
			denom = denom.divideNum(gcd);
			
			//rules involving negatives
			
			boolean negate = false;
			
			if(numer.signum() == -1 && denom.signum() == -1) negate = true;
			else if(numer.equalStruct(Num.NEG_ONE) && !denom.equalStruct(Num.ONE)) negate = true;
			else if(denom.equalStruct(Num.NEG_ONE)) negate = true;
			
			if(negate) {
				numer = numer.negate();
				denom = denom.negate();
			}
			
			//applying result
			if(numerIsNum) {
				div.setNumer(numer);
			}else if(numerIsProd){
				if(numer.equalStruct(Num.ONE)) {
					if(indexOfNumer != -1) {
						div.getNumer().remove(indexOfNumer);
					}
				}else {
					if(indexOfNumer != -1) {
						div.getNumer().set(indexOfNumer,numer);
					}else {
						div.getNumer().add(numer);
					}
				}
				
				if(div.getNumer().size() == 1) {
					div.setNumer(div.getNumer().get());
				}
			}else if(!numer.equalStruct(Num.ONE)){
				div.setNumer(prod(numer,div.getNumer()));
			}
			
			if(!denomIsProd) {
				div.setDenom(denom);
			}else {
				if(denom.equalStruct(Num.ONE)) {
					if(indexOfDenom != -1) {
						div.getDenom().remove(indexOfDenom);
					}
				}else {
					if(indexOfDenom != -1) {
						div.getDenom().set(indexOfDenom,denom);
					}else {
						div.getDenom().add(denom);
					}
				}
				
				if(div.getDenom().size() == 1) {
					div.setDenom(div.getDenom().get());
				}
			}
			
		}
	}
	
	boolean isNumerical() {//returns if its just numbers
		return getNumer() instanceof Num && getDenom() instanceof Num;
	}
	
	boolean isNumericalAndReal() {
		return isNumerical() && !((Num)getNumer()).isComplex() && !((Num)getDenom()).isComplex();
	}
	
	Div ratioOfUnitCircle() {//2*pi/3 -> 2/3, if it does not fit form a*pi/b then return null
		if(getDenom() instanceof Num && !((Num)getDenom()).isComplex()) {
			if(getNumer() instanceof Prod && getNumer().size() == 2) {
				Prod numerProdCopy = (Prod)getNumer().copy();
				for(int i = 0;i<2;i++) {
					if(numerProdCopy.get(i) instanceof Pi) {
						numerProdCopy.remove(i);
						break;
					}
				}
				if(numerProdCopy.size() == 1 && numerProdCopy.get() instanceof Num && !((Num)numerProdCopy.get()).isComplex()) {
					return div(numerProdCopy.get(),getDenom().copy());
				}
			}else if(getNumer() instanceof Pi) {
				return div(num(1),getDenom().copy());
			}
		}
		return null;
	}

	@Override
	public String toString() {
		String out = "";
		boolean numerNeedsParen = getNumer() instanceof Prod || getNumer() instanceof Sum || getNumer() instanceof Div;
		boolean denomNeedsParen = getDenom() instanceof Prod || getDenom() instanceof Sum || getDenom() instanceof Div;
		
		if(numerNeedsParen) out += "(";
		out+=getNumer().toString();
		if(numerNeedsParen) out += ")";
		
		out+="/";
		
		if(denomNeedsParen) out += "(";
		out+=getDenom().toString();
		if(denomNeedsParen) out += ")";
		
		return out;
	}
	
	public static Div addFracs(Div a,Div b) {//combines fraction, does not reduce/simplify answer, creates new object
		// I tried to make it so it's a little efficient and not always just doing (a*d+c*b)/(b*d)
		
		
		if(a.getNumer().equalStruct(Num.ZERO)) {
			return (Div)b.copy();
		}else if(b.getNumer().equalStruct(Num.ZERO)) {
			return (Div)a.copy();
		}
		
		if(a.getDenom().equalStruct(b.getDenom())) {//if they have the same denominator
			return div(Sum.combine(a.getNumer(), b.getNumer()),a.getDenom().copy());
		}
		//a/b + c/d = (a*d+c*b)/(b*d)
		Expr newDenom = Prod.combine(a.getDenom(), b.getDenom());
		Expr newNumer = sum( prod(a.getNumer().copy(),b.getDenom().copy()) , prod(b.getNumer().copy(),a.getDenom().copy()) );
		
		return div(newNumer,newDenom);
	}
	
	public static Sum mixedFraction(Div f) {//the fractional part of the sum will always be positive
		if(f.isNumericalAndReal()) {
			Num a = (Num)f.getNumer();
			Num b = (Num)f.getDenom();
			
			if(b.negative()) {
				a = a.negate();
				b = b.negate();
			}
			
			Num newNumer = num(a.realValue.mod(b.realValue));
			Num outer = a.divideNum(b.realValue);
			if(a.negative()) outer = outer.addNum(Num.NEG_ONE);
			
			if(outer.equalStruct(Num.ZERO)) return null;
			
			return sum(outer,div(newNumer,b));
			
			
		}
		return null;
	}
	
	public static Div cast(Expr e) {
		if(e instanceof Div) {
			return (Div)e;
		}
		return div(e,num(1));
	}
	
	public static Expr unCast(Expr e) {
		if(e instanceof Div) {
			Div casted = (Div)e;
			if(casted.getDenom().equalStruct(Num.ONE)) {
				return casted.getNumer();
			}
		}
		return e;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.div(getNumer().convertToFloat(varDefs), getDenom().convertToFloat(varDefs));
	}

}
