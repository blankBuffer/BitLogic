package cas.primitive;

import java.math.BigInteger;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.Algorithms;
import cas.calculus.Limit;
import cas.matrix.Mat;

import static cas.Cas.*;

public class Div{
	
	public static Func.FuncLoader divLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.helpMessage = "Division operator.\n"
					+ "Examples a/b, 1/3+1/5\n"
					+ "Special syntax, inv(3) gets parsed as 1/3";
			
			owner.behavior.rule = new Rule(new Rule[]{
					divWithEpsilon,
					factorChildren,
					trigExpandElements,
					reduceTrigFraction,
					divContainsDiv,
					expandRoots,
					cancelOutTerms,
					absInDenom,
					transferNegative,
					reSimpNumerAndDenom,//to reverse expandRoots process
					rationalize,
					rationalize2,
					reduceFraction,
					overOne,
					divWithMatrix,
					zeroInNum
			},"main sequence");
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.div(owner.getNumer().convertToFloat(varDefs), owner.getDenom().convertToFloat(varDefs));
				}
			};
			owner.behavior.toStringMethod = new Func.ToString() {
				@Override
				public String generateString(Func owner) {
					String out = "";
					boolean numerNeedsParen = owner.getNumer().isType("prod") || owner.getNumer().isType("sum") || owner.getNumer().isType("div");
					boolean denomNeedsParen = owner.getDenom().isType("prod") || owner.getDenom().isType("sum") || owner.getDenom().isType("div");
					
					if(numerNeedsParen) out += "(";
					out+=owner.getNumer().toString();
					if(numerNeedsParen) out += ")";
					
					out+="/";
					
					if(denomNeedsParen) out += "(";
					out+=owner.getDenom().toString();
					if(denomNeedsParen) out += ")";
					
					return out;
				}
			};
			
		}
	};
	
	public static Rule overOne = new Rule("a/1->a","divide by one");
	public static Rule zeroInNum = new Rule("0/a->0","zero in numerator");
	
	static Rule divWithEpsilon = new Rule("divisions with epsilon"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func div = (Func)e;
			
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
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func div = (Func)e;
			
			boolean prodInTrig = false;
			boolean nonProdInTrig = false;
			if(div.getNumer().isType("prod")){
				prodInTrig |= Prod.foundProdInTrigInProd((Func)div.getNumer());
				nonProdInTrig |= Prod.foundNonProdInTrigInProd((Func)div.getNumer());
			}else if(div.getNumer().isType("sin") || div.getNumer().isType("cos") || div.getNumer().isType("tan")){
				if(div.getNumer().get().isType("prod")){
					prodInTrig = true;
				}else{
					nonProdInTrig = true;
				}
			}
			if(div.getDenom().isType("prod")){
				prodInTrig |= Prod.foundProdInTrigInProd((Func)div.getDenom());
				nonProdInTrig |= Prod.foundNonProdInTrigInProd((Func)div.getDenom());
			}else if(div.getDenom().isType("sin") || div.getDenom().isType("cos") || div.getDenom().isType("tan")){
				if(div.getDenom().get().isType("prod")){
					prodInTrig = true;
				}else{
					nonProdInTrig = true;
				}
			}
			
			if(prodInTrig && nonProdInTrig){
				
				div.setNumer(Algorithms.trigExpand(div.getNumer(),casInfo));
				div.setDenom(Algorithms.trigExpand(div.getDenom(),casInfo));
			}
			
			return div;
		}
	};
	static Rule cancelOutTerms = new Rule("cancel out terms"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func div = (Func)e;
			Func numerProd = Prod.cast(div.getNumer()), denomProd = Prod.cast(div.getDenom());
			
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
					Func numerPower = Power.cast(numerTerm);
					Func denomPower =  Power.cast(denomTerm);
					
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
						
					}else if(numerPower.getExpo().equals(denomPower.getExpo()) && Algorithms.isPositiveRealNum(numerPower.getBase()) && Algorithms.isPositiveRealNum(denomPower.getBase())){//both denoms are the same
						Expr resTest = div(numerPower.getBase(),denomPower.getBase()).simplify(casInfo);
						if(resTest instanceof Num) {//10^x/2^x -> 5^x
							numerProd.set(i, power(resTest,numerPower.getExpo()));
							denomProd.remove(j);
							j--;
							continue inner;
						}else if(resTest.isType("div")) {//2^x/10^x -> 1/5^x
							if(((Func)resTest).getNumer().equals(Num.ONE)) {
								denomProd.set(j,power( ((Func)resTest).getDenom() ,numerPower.getExpo()));
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
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func div = (Func)e;
			boolean numerIsDiv = div.getNumer().isType("div");
			boolean denomIsDiv = div.getDenom().isType("div");
			
			if(numerIsDiv && denomIsDiv) {
				Func numerDiv = (Func)div.getNumer();
				Func denomDiv = (Func)div.getDenom();
				
				Expr newNumer = prod(numerDiv.getNumer(), denomDiv.getDenom());
				Expr newDenom = prod(numerDiv.getDenom(), denomDiv.getNumer());
				
				
				div.setNumer(newNumer.simplify(casInfo));
				div.setDenom(newDenom.simplify(casInfo));
			}else if(numerIsDiv) {
				Func numerDiv = (Func)div.getNumer();
				
				Expr newDenom = prod(numerDiv.getDenom(),div.getDenom());
				Expr newNumer = numerDiv.getNumer();
				
				div.setNumer(newNumer.simplify(casInfo));
				div.setDenom(newDenom.simplify(casInfo));
			}else if(denomIsDiv) {
				
				Func denomDiv = (Func)div.getDenom();
				
				Expr newNumer = prod(div.getNumer(),denomDiv.getDenom());
				Expr newDenom = denomDiv.getNumer();
				
				div.setNumer(newNumer.simplify(casInfo));
				div.setDenom(newDenom.simplify(casInfo));
			}
			return div;
		}
	};
	static Rule reduceFraction = new Rule("cancel out terms"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func div = (Func)e;
			
			//get numerator
			Num numer = num(1);
			int indexOfNumer = -1;//index if the numerator is a product
			boolean numerIsProd = false;
			boolean numerIsNum = false;
			
			if(div.getNumer() instanceof Num) {
				numer = (Num)div.getNumer();
				numerIsNum = true;
			}else if(div.getNumer().isType("prod")) {
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
			}else if(div.getDenom().isType("prod")) {
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
				
				if(numer.signum() == -1 && denom.signum() == -1) negate = true;//numerator and denominator negative
				else if(numer.equals(Num.NEG_ONE) && !denom.equals(Num.ONE)) negate = true;//transfer to negative to denominator
				else if(denom.equals(Num.NEG_ONE)) negate = true;//transfer negative to numerator
				else if(!numer.strangeAbs().equals(Num.ONE) && !denom.strangeAbs().equals(Num.ONE) && denom.signum() == -1) negate = true;//numerator negative takes priority
				
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
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			for(int i = 0;i<e.size();i++){
				e.set(i, factor(e.get(i)).simplify(casInfo));
			}
			return e;
		}
	};
	
	static Rule reduceTrigFraction = new Rule("reducing trig fraction") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func div = (Func)e;
			
			Func numerProd = Prod.cast(div.getNumer());
			Func denomProd = Prod.cast(div.getDenom());
			
			Func newNumerProd = prod();
			Func newDenomProd = prod();
			
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
							BigInteger tanCountBI = sinCount.getRealValue().abs().min(cosCount.getRealValue().abs());
							BigInteger sumCount = sinCount.getRealValue().add(cosCount.getRealValue());
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
						if(tanCount.getRealValue().signum() == 1) {
							newNumerProd.add(  Power.unCast(power(tan(var),tanCount))  );
						}else {
							newDenomProd.add(  Power.unCast(power(tan(var),tanCount.negate()))  );
						}
					}
					if(!sinCount.equals(Num.ZERO)) {
						if(sinCount.getRealValue().signum() == 1) {
							newNumerProd.add(  Power.unCast(power(sin(var),sinCount))  );
						}else {
							newDenomProd.add(  Power.unCast(power(sin(var),sinCount.negate()))  );
						}
					}
					if(!cosCount.equals(Num.ZERO)) {
						if(cosCount.getRealValue().signum() == 1) {
							newNumerProd.add(  Power.unCast(power(cos(var),cosCount))  );
						}else {
							newDenomProd.add(  Power.unCast(power(cos(var),cosCount.negate())) );
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
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func div = (Func)e;
			
			Func numerProd = Prod.cast( div.getNumer() );
			Func denomProd = Prod.cast( div.getDenom() );
			
			outer:for(int i = 0;i < denomProd.size();i++) {
				if(denomProd.get(i).isType("abs")) {
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
	
	static Rule rationalize = new Rule("a/b^(m/n)->a*b^((n-m)/n)/b","isType(b,num)&isType(m,num)&comparison(b>0)","rationalize denom");
	static Rule rationalize2 = new Rule("a/(k*b^(m/n))->a*b^((n-m)/n)/(k*b)","isType(b,num)&isType(k,num)&isType(m,num)&comparison(b>0)","rationalize denom");
	
	static Rule divWithMatrix = new Rule("divisions with a matrix") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func div = (Func)e;
			
			boolean numerIsMat = div.getNumer().isType("mat");
			boolean denomIsMat = div.getDenom().isType("mat");
			
			if(numerIsMat && denomIsMat) {
				Func nMat = (Func)div.getNumer();
				Func dMat = (Func)div.getDenom();
				
				for(int row = 0;row < Mat.rows(nMat);row++) {
					for(int col = 0;col < Mat.cols(nMat);col++) {
						
						Mat.setElement(nMat,row, col, div( Mat.getElement(nMat,row, col), Mat.getElement(dMat,row, col) ) );
						
					}
				}
				
				return nMat.simplify(casInfo);
			}else if(numerIsMat) {
				Func nMat = (Func)div.getNumer();
				
				for(int row = 0;row < Mat.rows(nMat);row++) {
					for(int col = 0;col < Mat.cols(nMat);col++) {
						
						Mat.setElement(nMat,row, col, div( Mat.getElement(nMat,row, col), div.getDenom() ) );
						
					}
				}
				
				return nMat.simplify(casInfo);
			}else if(denomIsMat) {
				Func dMat = (Func)div.getDenom();
				
				for(int row = 0;row < Mat.rows(dMat);row++) {
					for(int col = 0;col < Mat.cols(dMat);col++) {
						
						Mat.setElement(dMat,row, col, div(div.getNumer() , Mat.getElement(dMat,row, col)) );
						
					}
				}
				
				return dMat.simplify(casInfo);
			}
			
			return div;
		}
	};
	
	static Rule expandRoots = new Rule("expand roots") {
		Expr rootForm;
		@Override
		public void init() {
			rootForm = createExpr("a^(b/c)");
		}
		
		public boolean prodInForm(Func prod,CasInfo casInfo) {//also factors the root base
			boolean inForm = false;
			for(int i = 0;i<prod.size();i++) {
				if(Rule.fastSimilarExpr(rootForm, prod.get(i))) {
					Func pow = (Func)prod.get(i);
					
					if(pow.getBase().isType("sum")) {
						pow.setBase(factor(pow.getBase()).simplify(casInfo));
					}
					
					if(pow.getBase().isType("prod")) inForm = true;
				}
			}
			return inForm;
		}
		public Func expandRoots(Func prod,CasInfo casInfo) {//returns prod
			Func outProd = prod();
			for(int i = 0;i<prod.size();i++) {
				Expr current = prod.get(i);
				if(Rule.fastSimilarExpr(rootForm, current) && current.get().isType("prod")) {
					
					Expr expo = ((Func)current).getExpo();
					Func baseProd = (Func)prod.get(i).get();
					for(int j = 0;j<baseProd.size();j++) {
						outProd.add(power(baseProd.get(j),expo).simplify(casInfo));
					}
					
				}else {
					outProd.add(prod.get(i));
				}
			}
			return outProd;
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func div = (Func)e;
			
			Func numerProd = Prod.cast(div.getNumer());
			Func denomProd = Prod.cast(div.getDenom());
			
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
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func div = (Func)e;
			
			div.setNumer(div.getNumer().simplify(casInfo));
			div.setDenom(div.getDenom().simplify(casInfo));
			
			return div;
		}
	};
	
	static Rule transferNegative = new Rule("transfer the invalid negative to the other side of fraction") {
		Expr negativeRootNum;
		Expr negativeRootNumCond;
		
		@Override
		public void init() {
			negativeRootNum = createExpr("a^(b/c)");
			negativeRootNumCond = createExpr("isType(a,num)&comparison(a<0)");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func div = (Func)e;
			
			Func numerProd = Prod.cast(div.getNumer());
			Func denomProd = Prod.cast(div.getDenom());
			
			outer:for(int i = 0;i<numerProd.size();i++) {//numer variant
				if(Rule.similarWithCondition(negativeRootNum, numerProd.get(i), negativeRootNumCond)) {
					Func numerRoot = (Func)numerProd.get(i);
					
					for(int j = 0;j<denomProd.size();j++) {
						Expr compare = denomProd.get(j);
						if(compare.isType("power") && ((Func)compare).getExpo().equals(numerRoot.getExpo()) && (((Func)compare).getBase().containsVars() || Algorithms.isNegativeRealNum(((Func)compare).getBase()) ) ) {
							Func denomPow = (Func)denomProd.get(j);
							denomPow.setBase(neg(denomPow.getBase()));
							numerRoot.setBase( numerRoot.getBase().strangeAbs(casInfo) );
							continue outer;
						}
					}
					
				}
			}
			
			outer:for(int i = 0;i<denomProd.size();i++) {//denom variant
				if(Rule.similarWithCondition(negativeRootNum, denomProd.get(i), negativeRootNumCond)) {
					Func denomRoot = (Func)denomProd.get(i);
					
					for(int j = 0;j<numerProd.size();j++) {
						Expr compare = numerProd.get(j);
						if(compare.isType("power") && ((Func)compare).getExpo().equals(denomRoot.getExpo()) && (((Func)compare).getBase().containsVars() || Algorithms.isNegativeRealNum(((Func)compare).getBase()) ) ) {
							Func numerPow = (Func)numerProd.get(j);
							numerPow.setBase(neg(numerPow.getBase()));
							denomRoot.setBase( denomRoot.getBase().strangeAbs(casInfo) );
							continue outer;
						}
					}
					
				}
			}
			
			return div;
		}
	};
	
	public static boolean isNumerical(Func frac) {//returns if its just numbers
		return frac.getNumer() instanceof Num && frac.getDenom() instanceof Num;
	}
	
	public static boolean isNumericalAndReal(Func frac) {
		assert frac.isType("div") : "expected a div";
		return isNumerical(frac) && !((Num)frac.getNumer()).isComplex() && !((Num)frac.getDenom()).isComplex();
	}
	
	public static Func ratioOfUnitCircle(Func frac) {//2*pi/3 -> 2/3, if it does not fit form a*pi/b then return null
		assert frac.isType("div") : "expected a div";
		
		if(frac.getDenom() instanceof Num && !((Num)frac.getDenom()).isComplex()) {
			if(frac.getNumer().isType("prod") && frac.getNumer().size() == 2) {
				Func numerProdCopy = (Func)frac.getNumer().copy();
				for(int i = 0;i<2;i++) {
					if(numerProdCopy.get(i).equals(Var.PI)) {
						numerProdCopy.remove(i);
						break;
					}
				}
				if(numerProdCopy.size() == 1 && numerProdCopy.get() instanceof Num && !((Num)numerProdCopy.get()).isComplex()) {
					return div(numerProdCopy.get(),frac.getDenom().copy());
				}
			}else if(frac.getNumer().equals(Var.PI)) {
				return div(num(1),frac.getDenom().copy());
			}
		}
		return null;
	}
	
	//a and b are divs and returns a div
	public static Func addFracs(Func a,Func b) {//combines fraction, does not reduce/simplify answer, creates new object
		// I tried to make it so it's a little efficient and not always just doing (a*d+c*b)/(b*d)
		
		
		if(a.getNumer().equals(Num.ZERO)) {
			return (Func)b.copy();
		}else if(b.getNumer().equals(Num.ZERO)) {
			return (Func)a.copy();
		}
		
		if(a.getDenom().equals(b.getDenom())) {//if they have the same denominator
			return div(Sum.combine(a.getNumer(), b.getNumer()),a.getDenom().copy());
		}
		//a/b + c/d = (a*d+c*b)/(b*d)
		Expr newDenom = Prod.combine(a.getDenom(), b.getDenom());
		Expr newNumer = sum( prod(a.getNumer().copy(),b.getDenom().copy()) , prod(b.getNumer().copy(),a.getDenom().copy()) );
		
		return div(newNumer,newDenom);
	}
	
	public static Func mixedFraction(Func frac) {//the fractional part of the sum will always be positive
		assert frac.isType("div") : "expected a div";
		if(Div.isNumericalAndReal(frac)) {
			Num a = (Num)frac.getNumer();
			Num b = (Num)frac.getDenom();
			
			if(b.negative()) {
				a = a.negate();
				b = b.negate();
			}
			
			Num newNumer = num(a.getRealValue().mod(b.getRealValue()));
			Num outer = a.divideNum(b.getRealValue());
			if(a.negative()) outer = outer.addNum(Num.NEG_ONE);
			
			if(outer.equals(Num.ZERO)) return null;
			
			return sum(outer,div(newNumer,b));
			
			
		}
		return null;
	}
	
	public static Func cast(Expr e) {
		if(e.isType("div")) {
			return (Func)e;
		}
		return div(e,num(1));
	}
	
	public static Expr unCast(Expr e) {
		if(e.isType("div")) {
			Func casted = (Func)e;
			if(casted.getDenom().equals(Num.ONE)) {
				return casted.getNumer();
			}
		}
		return e;
	}
}
