package cas.primitive;

import java.math.BigInteger;

import cas.ComplexFloat;
import cas.Expr;
import cas.Rule;
import cas.CasInfo;
import cas.calculus.Limit;
import cas.matrix.Mat;
import cas.trig.Cos;
import cas.trig.Sin;
import cas.trig.Tan;

public class Div extends Expr{
	
	private static final long serialVersionUID = -1262460519269095855L;
	public static Rule overOne = new Rule("a/1->a","divide by one");
	public static Rule zeroInNum = new Rule("0/a->0","zero in numerator");

	public Div(){}//
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
	
	static Rule divWithEpsilon = new Rule("divisions with epsilon"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Div div = null;
			if(e instanceof Div){
				div = (Div)e;
			}else{
				return e;
			}
			if(!Limit.zeroOrEpsilon(div.getDenom())){
				if(Limit.isEpsilon(div.getNumer())){
					return prod(div.getNumer(),div.getDenom()).simplify(casInfo);
				}else if(Limit.isInf(div.getNumer())){
					return prod(div.getNumer(),div.getDenom()).simplify(casInfo);
				}
			}
			if(!Limit.zeroOrEpsilon(div.getNumer())){
				if(Limit.isEpsilon(div.getDenom()) ){
					if(div.getDenom().equals(Var.EPSILON) ){
						return prod(inf(),div.getNumer()).simplify(casInfo);
					}
					return prod(num(-1),inf(),div.getNumer()).simplify(casInfo);
				}else if(Limit.isInf(div.getDenom())){
					if(div.getDenom().equals(Var.INF) ){
						return prod(epsilon(),div.getNumer()).simplify(casInfo);
					}
					return prod(num(-1),epsilon(),div.getNumer()).simplify(casInfo);
				}
			}
			
			if(!div.getDenom().contains(Var.INF)) {//h/(m+epsilon) -> h/m-epsilon
				short direction = Limit.getDirection(div.getDenom());
				if(direction != Limit.NONE) {
					
					div.setDenom(Limit.stripDirection(div.getDenom()));
					
					direction = (short) -direction;
					
					return Limit.applyDirection(div.simplify(casInfo), direction);
				}
			}
			
			return div;
		}
	};
	
	static Rule trigExpandElements = new Rule("trig expand elements div"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
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
				
				div.setNumer(trigExpand(div.getNumer(),casInfo));
				div.setDenom(trigExpand(div.getDenom(),casInfo));
			}
			
			return div;
		}
	};
	static Rule cancelOutTerms = new Rule("cancel out terms"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Div div = (Div)e;
			
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
					
					if(numerTerm.equals(denomTerm)) {
						numerProd.remove(i);
						denomProd.remove(j);
						i--;
						continue outer;
					}
					Power numerPower = Power.cast(numerTerm);
					Power denomPower =  Power.cast(denomTerm);
					
					if(numerPower.getBase().equals(denomPower.getBase())) {//both bases are the same x^2/x^3
						Expr newExpo = sub(numerPower.getExpo(),denomPower.getExpo()).simplify(casInfo);
						if(newExpo.negative()) {
							newExpo = neg(newExpo).simplify(casInfo);//flip it
							denomPower.setExpo(newExpo);
							denomProd.set(j, denomPower.simplify(casInfo));
							numerProd.remove(i);
							i--;
							continue outer;
						}
						numerPower.setExpo(newExpo);
						numerProd.set(i, numerPower.simplify(casInfo));
						denomProd.remove(j);
						j--;
						continue inner;
						
					}else if(numerPower.getExpo().equals(denomPower.getExpo()) && numerPower.getBase() instanceof Num && denomPower.getBase() instanceof Num){//both denoms are the same
						Expr resTest = div(numerPower.getBase(),denomPower.getBase()).simplify(casInfo);
						if(resTest instanceof Num) {//10^x/2^x -> 5^x
							numerProd.set(i, pow(resTest,numerPower.getExpo()));
							denomProd.remove(j);
							j--;
							continue inner;
						}else if(resTest instanceof Div) {//2^x/10^x -> 1/5^x
							if(((Div)resTest).getNumer().equals(Num.ONE)) {
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
			
			return div;
		
		}
	};
	static Rule divContainsDiv = new Rule("cancel out terms"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Div div = (Div)e;
			boolean numerIsDiv = div.getNumer() instanceof Div;
			boolean denomIsDiv = div.getDenom() instanceof Div;
			
			if(numerIsDiv && denomIsDiv) {
				Div numerDiv = (Div)div.getNumer();
				Div denomDiv = (Div)div.getDenom();
				
				Expr newNumer = prod(numerDiv.getNumer(), denomDiv.getDenom());
				Expr newDenom = prod(numerDiv.getDenom(), denomDiv.getNumer());
				
				
				div.setNumer(newNumer.simplify(casInfo));
				div.setDenom(newDenom.simplify(casInfo));
			}else if(numerIsDiv) {
				Div numerDiv = (Div)div.getNumer();
				
				Expr newDenom = prod(numerDiv.getDenom(),div.getDenom());
				Expr newNumer = numerDiv.getNumer();
				
				div.setNumer(newNumer.simplify(casInfo));
				div.setDenom(newDenom.simplify(casInfo));
			}else if(denomIsDiv) {
				
				Div denomDiv = (Div)div.getDenom();
				
				Expr newNumer = prod(div.getNumer(),denomDiv.getDenom());
				Expr newDenom = denomDiv.getNumer();
				
				div.setNumer(newNumer.simplify(casInfo));
				div.setDenom(newDenom.simplify(casInfo));
			}
			return div;
		}
	};
	static Rule reduceFraction = new Rule("cancel out terms"){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Div div = (Div)e;
			
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
				else if(numer.equals(Num.NEG_ONE) && !denom.equals(Num.ONE)) negate = true;
				else if(denom.equals(Num.NEG_ONE)) negate = true;
				
				if(negate) {
					numer = numer.negate();
					denom = denom.negate();
				}
				
				//applying result
				if(numerIsNum) {
					div.setNumer(numer);
				}else if(numerIsProd){
					if(numer.equals(Num.ONE)) {
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
				}else if(!numer.equals(Num.ONE)){
					div.setNumer(prod(numer,div.getNumer()));
				}
				
				if(!denomIsProd) {
					div.setDenom(denom);
				}else {
					if(denom.equals(Num.ONE)) {
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
			return div;
		}
	};
	
	static Rule factorChildren = new Rule("factor sub expression"){
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			for(int i = 0;i<e.size();i++){
				e.set(i, factor(e.get(i)).simplify(casInfo));
			}
			return e;
		}
	};
	
	static Rule reduceTrigFraction = new Rule("reducing trig fraction") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Div div = (Div)e;
			
			Prod numerProd = Prod.cast(div.getNumer());
			Prod denomProd = Prod.cast(div.getDenom());
			
			Prod newNumerProd = new Prod();
			Prod newDenomProd = new Prod();
			
			for(int i = 0;i<numerProd.size();i++) {
				Prod.TermInfo termInfo = Prod.getTermInfo(numerProd.get(i));
				if(termInfo != null) {
					Expr var = termInfo.var;
					
					Num sinCount = num(0);
					Num cosCount = num(0);
					if(termInfo.typeName.equals("sin")) {
						sinCount = sinCount.addNum(termInfo.expo);
					}else if(termInfo.typeName.equals("cos")) {
						cosCount = cosCount.addNum(termInfo.expo);
					}else if(termInfo.typeName.equals("tan")) {
						sinCount = sinCount.addNum(termInfo.expo);
						cosCount = cosCount.subNum(termInfo.expo);
					}
					numerProd.remove(i);
					i--;
					//filter out numerator
					for(int j = 0;j<numerProd.size();j++) {
						Prod.TermInfo otherTermInfo = Prod.getTermInfo(numerProd.get(j));
						if(otherTermInfo != null && otherTermInfo.var.equals(var)) {
							
							if(otherTermInfo.typeName.equals("sin")) {
								sinCount = sinCount.addNum(otherTermInfo.expo);
							}else if(otherTermInfo.typeName.equals("cos")) {
								cosCount = cosCount.addNum(otherTermInfo.expo);
							}else if(otherTermInfo.typeName.equals("tan")) {
								sinCount = sinCount.addNum(otherTermInfo.expo);
								cosCount = cosCount.subNum(otherTermInfo.expo);
							}
							
							numerProd.remove(j);
							j--;
						}
					}
					//filter out denominator
					for(int j = 0;j<denomProd.size();j++) {
						Prod.TermInfo otherTermInfo = Prod.getTermInfo(denomProd.get(j));
						if(otherTermInfo != null && otherTermInfo.var.equals(var)) {
							
							if(otherTermInfo.typeName.equals("sin")) {
								sinCount = sinCount.subNum(otherTermInfo.expo);
							}else if(otherTermInfo.typeName.equals("cos")) {
								cosCount = cosCount.subNum(otherTermInfo.expo);
							}else if(otherTermInfo.typeName.equals("tan")) {
								sinCount = sinCount.subNum(otherTermInfo.expo);
								cosCount = cosCount.addNum(otherTermInfo.expo);
							}
							
							denomProd.remove(j);
							j--;
						}
					}
					//
					
					//move around calculation
					Num tanCount = num(0);
					if(!sinCount.equals(Num.ZERO) && !cosCount.equals(Num.ZERO)) {
						if(sinCount.signum() == 1 ^ cosCount.signum() == 1) {
							BigInteger tanCountBI = sinCount.realValue.abs().min(cosCount.realValue.abs());
							BigInteger sumCount = sinCount.realValue.add(cosCount.realValue);
							if(sinCount.negative()) {
								tanCountBI = tanCountBI.negate();
							}
							
							if(sumCount.signum() == 1 ^ sinCount.negative()) {
								sinCount = num(sumCount);
								cosCount = num(0);
							}else {
								sinCount = num(0);
								cosCount = num(sumCount);
							}
							
							tanCount = num(tanCountBI);
						}
					}
					//re add back
					if(!tanCount.equals(Num.ZERO)) {
						if(tanCount.realValue.signum() == 1) {
							newNumerProd.add(  Power.unCast(pow(tan(var),tanCount))  );
						}else {
							newDenomProd.add(  Power.unCast(pow(tan(var),tanCount.negate()))  );
						}
					}
					if(!sinCount.equals(Num.ZERO)) {
						if(sinCount.realValue.signum() == 1) {
							newNumerProd.add(  Power.unCast(pow(sin(var),sinCount))  );
						}else {
							newDenomProd.add(  Power.unCast(pow(sin(var),sinCount.negate()))  );
						}
					}
					if(!cosCount.equals(Num.ZERO)) {
						if(cosCount.realValue.signum() == 1) {
							newNumerProd.add(  Power.unCast(pow(cos(var),cosCount))  );
						}else {
							newDenomProd.add(  Power.unCast(pow(cos(var),cosCount.negate())) );
						}
					}
					
				}
			}
			
			Expr newNumer = Prod.unCast(Prod.combine(numerProd, newNumerProd));
			Expr newDenom = Prod.unCast(Prod.combine(denomProd, newDenomProd));
			
			div.setNumer(newNumer);
			div.setDenom(newDenom);
			
			return div;
		}
	};
	
	/*
	 * x/abs(x) -> abs(x)/x
	 */
	static Rule absInDenom = new Rule("denominator has an absolute value") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Div div = (Div)e;
			
			Prod numerProd = Prod.cast( div.getNumer() );
			Prod denomProd = Prod.cast( div.getDenom() );
			
			outer:for(int i = 0;i < denomProd.size();i++) {
				if(denomProd.get(i) instanceof Abs) {
					Expr inner = denomProd.get(i).get();
					for(int j = 0;j<numerProd.size();j++) {
						if(numerProd.get(j).equals(inner)) {
							
							denomProd.set(i, inner);
							numerProd.set(j, abs(numerProd.get(j)));
							continue outer;
							
						}
					}
				}
			}
			
			div.setNumer(Prod.unCast(numerProd));
			div.setDenom(Prod.unCast(denomProd));
			
			return div;
		}
	};
	
	static Rule rationalize = new Rule("a/b^(m/n)->a*b^((n-m)/n)/b","isType(b,num)&isType(m,num)","rationalize denom");
	static Rule rationalize2 = new Rule("a/(k*b^(m/n))->a*b^((n-m)/n)/(k*b)","isType(b,num)&isType(k,num)&isType(m,num)","rationalize denom");
	
	static Rule divWithMatrix = new Rule("divisions with a matrix") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Div div = (Div)e;
			
			boolean numerIsMat = div.getNumer() instanceof Mat;
			boolean denomIsMat = div.getDenom() instanceof Mat;
			
			if(numerIsMat && denomIsMat) {
				Mat nMat = (Mat)div.getNumer();
				Mat dMat = (Mat)div.getDenom();
				
				for(int row = 0;row < nMat.rows();row++) {
					for(int col = 0;col < nMat.cols();col++) {
						
						nMat.setElement(row, col, div( nMat.getElement(row, col), dMat.getElement(row, col) ) );
						
					}
				}
				
				return nMat.simplify(casInfo);
			}else if(numerIsMat) {
				Mat nMat = (Mat)div.getNumer();
				
				for(int row = 0;row < nMat.rows();row++) {
					for(int col = 0;col < nMat.cols();col++) {
						
						nMat.setElement(row, col, div( nMat.getElement(row, col), div.getDenom() ) );
						
					}
				}
				
				return nMat.simplify(casInfo);
			}else if(denomIsMat) {
				Mat dMat = (Mat)div.getDenom();
				
				for(int row = 0;row < dMat.rows();row++) {
					for(int col = 0;col < dMat.cols();col++) {
						
						dMat.setElement(row, col, div(div.getNumer() , dMat.getElement(row, col)) );
						
					}
				}
				
				return dMat.simplify(casInfo);
			}
			
			return div;
		}
	};
	
	static Rule expandRoots = new Rule("expand roots") {
		private static final long serialVersionUID = 1L;
		
		Expr rootForm;
		@Override
		public void init() {
			rootForm = createExpr("a^(b/c)");
		}
		
		public boolean prodInForm(Prod prod,CasInfo casInfo) {//also factors the root base
			boolean inForm = false;
			for(int i = 0;i<prod.size();i++) {
				if(Rule.fastSimilarExpr(rootForm, prod.get(i))) {
					Power pow = (Power)prod.get(i);
					
					if(pow.getBase() instanceof Sum) {
						pow.setBase(factor(pow.getBase()).simplify(casInfo));
					}
					
					if(pow.getBase() instanceof Prod) inForm = true;
				}
			}
			return inForm;
		}
		public Prod expandRoots(Prod prod,CasInfo casInfo) {
			Prod out = new Prod();
			for(int i = 0;i<prod.size();i++) {
				Expr current = prod.get(i);
				if(Rule.fastSimilarExpr(rootForm, current) && current.get() instanceof Prod) {
					
					Expr expo = ((Power)current).getExpo();
					Prod base = (Prod)prod.get(i).get();
					for(int j = 0;j<base.size();j++) {
						out.add(pow(base.get(j),expo).simplify(casInfo));
					}
					
				}else {
					out.add(prod.get(i));
				}
			}
			return out;
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Div div = (Div)e;
			
			Prod numerProd = Prod.cast(div.getNumer());
			Prod denomProd = Prod.cast(div.getDenom());
			
			boolean inForm = prodInForm(numerProd,casInfo) | prodInForm(denomProd,casInfo);//but use single | operator because we don't want short circuit evaluation
		
			if(!inForm) return div;
			
			numerProd = expandRoots(numerProd,casInfo);
			denomProd = expandRoots(denomProd,casInfo);
			
			div.setNumer(Prod.unCast(numerProd));
			div.setDenom(Prod.unCast(denomProd));
			
			return div;
		}
	};
	
	static Rule reSimpNumerAndDenom = new Rule("simplify numerator and denominator again") {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Div div = (Div)e;
			
			div.setNumer(div.getNumer().simplify(casInfo));
			div.setDenom(div.getDenom().simplify(casInfo));
			
			return div;
		}
	};
	
	static Sequence ruleSequence = null;
	
	public static void loadRules(){
		ruleSequence = sequence(
				divWithEpsilon,
				factorChildren,
				trigExpandElements,
				reduceTrigFraction,
				divContainsDiv,
				absInDenom,
				expandRoots,
				cancelOutTerms,
				reSimpNumerAndDenom,//to reverse expandRoots process
				rationalize,
				rationalize2,
				reduceFraction,
				overOne,
				divWithMatrix,
				zeroInNum
		);
		Rule.initRules(ruleSequence);
	}
	
	@Override
	public Sequence getRuleSequence() {
		return ruleSequence;
	}
	public boolean isNumerical() {//returns if its just numbers
		return getNumer() instanceof Num && getDenom() instanceof Num;
	}
	
	public boolean isNumericalAndReal() {
		return isNumerical() && !((Num)getNumer()).isComplex() && !((Num)getDenom()).isComplex();
	}
	
	public Div ratioOfUnitCircle() {//2*pi/3 -> 2/3, if it does not fit form a*pi/b then return null
		if(getDenom() instanceof Num && !((Num)getDenom()).isComplex()) {
			if(getNumer() instanceof Prod && getNumer().size() == 2) {
				Prod numerProdCopy = (Prod)getNumer().copy();
				for(int i = 0;i<2;i++) {
					if(numerProdCopy.get(i).equals(Var.PI)) {
						numerProdCopy.remove(i);
						break;
					}
				}
				if(numerProdCopy.size() == 1 && numerProdCopy.get() instanceof Num && !((Num)numerProdCopy.get()).isComplex()) {
					return div(numerProdCopy.get(),getDenom().copy());
				}
			}else if(getNumer().equals(Var.PI)) {
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
		
		
		if(a.getNumer().equals(Num.ZERO)) {
			return (Div)b.copy();
		}else if(b.getNumer().equals(Num.ZERO)) {
			return (Div)a.copy();
		}
		
		if(a.getDenom().equals(b.getDenom())) {//if they have the same denominator
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
			
			if(outer.equals(Num.ZERO)) return null;
			
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
			if(casted.getDenom().equals(Num.ONE)) {
				return casted.getNumer();
			}
		}
		return e;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return ComplexFloat.div(getNumer().convertToFloat(varDefs), getDenom().convertToFloat(varDefs));
	}
	
	@Override
	public String typeName() {
		return "div";
	}
}
