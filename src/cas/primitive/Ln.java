package cas.primitive;
import java.math.BigInteger;

import cas.Algorithms;
import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.calculus.Limit;

import static cas.Cas.*;

public class Ln{
	
	public static Func.FuncLoader lnLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.rule = new Rule(new Rule[]{
				log1To0,
				logETo1,
				lnOfEpsilon,
				lnOfInf,
				lnOfEpsilonSum,
				logOfInverse,
				logOfInverse2,
				logOfNegativeOrComplex,
				factorInnerReal,
				logWithSums,
				logOfPerfectPower,
				gcdExponent,
				powToProd
			},"main sequence");
			owner.behavior.rule.init();
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					return ComplexFloat.ln(owner.get().convertToFloat(varDefs));
				}
			};
		}
	};
	
	
	static Rule log1To0 = new Rule("ln(1)->0","log of 1");
	static Rule logETo1 = new Rule("ln(e)->1","log of e");
	static Rule powToProd = new Rule("ln(a^b)->b*ln(a)","log of power");
	static Rule lnOfEpsilon = new Rule("ln(epsilon)->-inf","log of epsilon");
	static Rule lnOfInf = new Rule("ln(inf)->inf","log of infinity");
	
	static Rule lnOfEpsilonSum = new Rule("log of sum with epsilon"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Func ln = null;
			if(e.isType("ln")){
				ln = (Func)e;
			}else{
				return e;
			}
			
			if(ln.get().isType("sum")){
				Expr inner = ln.get();
				short direction = Limit.getDirection(inner);
				
				if(direction != Limit.NONE){
					ln.set(0, Limit.stripDirection(inner));
					return Limit.applyDirection(e, direction);
				}
				
			}
			
			return ln;
		}
	};
	
	static Rule logOfPerfectPower = new Rule("log of a perfect power"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func log = (Func)e;
			
			if(log.get() instanceof Num) {// example log(25) -> 2*ln(5)
				Num casted = (Num)log.get();
				Func perfectPower = Algorithms.perfectPower(casted);
				if(((Num)perfectPower.getExpo()).getRealValue().equals(BigInteger.ONE)) return log;
				
				log.set(0, perfectPower);
			}else if(log.get().isType("prod")) {//ln(8*x) -> ln(2^3*x) , this will be reverted in later steps
				Func innerProd = (Func)log.get();
				for(int i = 0;i<innerProd.size();i++) {
					if(innerProd.get(i) instanceof Num) {
						Num casted = (Num)innerProd.get(i);
						
						Func perfectPower = Algorithms.perfectPower(casted);
						if(((Num)perfectPower.getExpo()).getRealValue().equals(BigInteger.ONE)) continue;
						innerProd.set(i, perfectPower);
						
					}
				}
				
			}
			return log;
		}
	};
	
	static Rule logOfInverse = new Rule("ln(1/x)->-ln(x)","log of inverse becomes negative log");
	static Rule logOfInverse2 = new Rule("ln((-1)/x)->-ln(-x)","log of inverse becomes negative log");
	
	
	static Rule logWithSums = new Rule("remove sums from within logs") {//the goal is to remove sums inside of logs if they are part of a product or division
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func log = (Func)e;
			
			if((log.get().isType("prod") || log.get().isType("div")) && (casInfo.allowComplexNumbers() || !log.get().negative()) ) {

				Func outSum = sum();
				Func div = Div.cast(log.get());
				
				Func prodNumer = Prod.cast(div.getNumer());
				Func prodDenom = Prod.cast(div.getDenom());
				
				for(int i = 0;i<prodNumer.size();i++) {
					Expr current = prodNumer.get(i);
					if(current.isType("sum") || current.isType("power") && ((Func)current).getBase().isType("sum") || current.isType("abs") && ((Func)current).get().isType("sum")) {
						if(!current.containsVars() && !current.convertToFloat(exprSet()).positiveAndReal()) continue;
						outSum.add(ln(current));
						prodNumer.remove(i);
						i--;
					}
				}
				for(int i = 0;i<prodDenom.size();i++) {
					Expr current = prodDenom.get(i);
					if(current.isType("sum") || current.isType("power") && ((Func)current).getBase().isType("sum") || current.isType("abs") && ((Func)current).get().isType("sum")) {
						if(!current.containsVars() && !current.convertToFloat(exprSet()).positiveAndReal()) continue;
						outSum.add(neg(ln(current)));
						prodDenom.remove(i);
						i--;
					}
				}
				
				if(outSum.size()>0) {
					div.setNumer(prodNumer);
					div.setDenom(prodDenom);
					log.set(0, div);
					
					outSum.add(log);
					return outSum.simplify(casInfo);
				}
			}
			
			return log;
		}
		
	};
	
	static Rule logOfNegativeOrComplex = new Rule("log of a negative or complex expression") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func log = (Func)e;
			
			if(casInfo.allowComplexNumbers()) {
				Func sepSequence = Algorithms.basicRealAndImagComponents(e.get(),casInfo);
				if(!sepSequence.get(0).equals(Num.ZERO) && !sepSequence.get(1).equals(Num.ZERO)) {
					//ln(a+b*i) -> ln(sqrt(a^2+b^2)*e^(i*atan(b/a))) -> ln(a^2+b^2)/2+i*atan(b/a)
					
					Expr out = sum(div(ln(sum(power(sepSequence.get(0),num(2)),power(sepSequence.get(1),num(2)))),num(2)),prod(num(0,1),atan(div(sepSequence.get(1),sepSequence.get(0)))));
					return out.simplify(casInfo);
				}
				
				if(log.get().negative()) {//ln(-x) -> ln(x)+pi*i
					return sum(ln(neg(log.get()).simplify(casInfo)),prod(pi(),num(0,1)));
				}
			}
			
			return log;
		}
	};
	
	/*
	 * cant factor complex roots because that would create recursion with ln(x+i) -> ln(x^2+1)+i*atan(1/x) -> ln((x-i)*(x+i))+i*atan(1/x) back to itself again
	 */
	static Rule factorInnerReal = new Rule("only factor real") {
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func log = (Func)e;
			
			CasInfo noComplex = new CasInfo(casInfo);
			noComplex.setAllowComplexNumbers(false);
			log.set(0, factor(log.get()).simplify(noComplex) );
			
			return log;
		}
	};
	
	public static Rule gcdExponent = new Rule("ln has common exponent that can be factored"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo) {
			Func log = (Func)e;
			if(log.get().isType("prod")) {
				Func innerProd = (Func)log.get();
				for(int i = 0;i<innerProd.size();i++) {
					boolean badForm = false;
					if(innerProd.get(i) instanceof Num) {
						Func ppower = Algorithms.perfectPower((Num)innerProd.get(i));
						if(ppower.getExpo().equals(Num.ONE)) badForm = true;	
						else innerProd.set(i,ppower);
					}
					badForm|=!(innerProd.get(i).isType("power"));
					
					if(badForm) {
						log.simplifyChildren(casInfo);
						return log;
					}
				}
				Expr gcd = gcd();
				for(int i = 0;i<innerProd.size();i++) {
					Func current = (Func)innerProd.get(i);
					
					gcd.add(current.getExpo());
					
				}
				gcd = gcd.simplify(casInfo);
				if(gcd.equals(Num.ONE)) {
					log.simplifyChildren(casInfo);
					return log;
				}
				for(int i = 0;i<innerProd.size();i++) {
					Func current = (Func)innerProd.get(i);
					current.setExpo(div(current.getExpo(),gcd));
				}
				innerProd.setSimpleSingleNode(false);
				return prod(gcd,ln(innerProd)).simplify(casInfo);
				
			}
			return log;
		}
	};
}
