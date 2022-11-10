package cas.primitive;

import cas.*;
import cas.calculus.Limit;
import cas.matrix.Mat;

public class Sum extends Expr{
	
	static Rule pythagOnePlusTanSqr = new Rule("1+tan(x)^2->cos(x)^-2","one plus tangent squared");
	
	public Sum() {
	}
	
	@Override
	public boolean isCommutative(){
		return true;
	}
	
	/*
	public static Rule  = new Rule("",Rule){
		private static final long serialVersionUID = 1L;

		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Sum sum = (Sum)e;
		}
	};
	 */
	
	public static Rule sumWithInf = new Rule("sum with infinity"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Sum sum = (Sum)e;
			
			boolean hasPosInf = false;
			boolean hasNegInf = false;
			
			for(int i = 0;i<sum.size();i++){
				if(sum.get(i).equals(Var.INF)){
					hasPosInf = true;
				}else if(sum.get(i).equals(Var.NEG_INF)){
					hasNegInf = true;
				}
				if(hasPosInf && hasNegInf) break;
				
			}
			
			if(hasPosInf && !hasNegInf){
				sum.clear();
				sum.add(inf());
			} else if(!hasPosInf && hasNegInf){
				sum.clear();
				sum.add(neg(inf()));
			}else if(hasPosInf && hasNegInf && sum.size() != 2){
				sum.clear();
				sum.add(inf());
				sum.add(neg(inf()));
			}
			
			return sum;
		}
	};
	
	public static Rule trigExpandElements = new Rule("trig expand elements"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Sum sum = (Sum)e;
			if(sum.containsType("sin")){
				for(int i = 0;i < sum.size();i++){
					sum.set(i, trigExpand(sum.get(i),casInfo));
				}
			}
			return sum;
		}
	};
	
	public static Rule complexPythagIden = new Rule("pythagorean identity"){//sin(x)^2+cos(x)^2 = 1 and a*sin(x)^2+a*cos(x)^2=a
		Expr sinsqr,cossqr;
		
		@Override
		public void init(){
			sinsqr = createExpr("sin(x)^2");
			cossqr = createExpr("cos(x)^2");	
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Sum sum = (Sum)e;
			
			if(!sum.containsType("sin")) return e;
			
			outer:for(int i = 0;i<sum.size();i++) {
				Expr current = sum.get(i);
				if(!(current.containsType("sin") || current.containsType("cos"))) continue;
				if(fastSimilarExpr(sinsqr,current)) {
					Expr var = current.get(0).get(0);
					for(int j = 0;j<sum.size();j++) {
						if(j==i) continue;
						Expr other = sum.get(j); 
						if(fastSimilarExpr(cossqr,other)) {
							Expr otherVar = other.get(0).get(0);
							
							if(otherVar.equals(var)) {
								int min = Math.min(i, j);
								int max = Math.max(i, j);
								sum.remove(max);
								sum.remove(min);
								
								sum.add(num(1));
								i=min-1;
								continue outer;
							}
							
						}
					}
				}else if(current instanceof Prod) {
					int index = -1;
					String type = null;
					for(int j = 0;j < current.size();j++) {
						if(fastSimilarExpr(sinsqr,current.get(j))) {
							index = j;
							type = "sin";
							break;
						}else if(fastSimilarExpr(cossqr,current.get(j))) {
							index = j;
							type = "cos";
							break;
						}
					}
					if(index != -1) {
						Expr var = current.get(index).get().get();
						Expr coef = current.copy();
						coef.remove(index);
						
						for(int j = i+1;j < sum.size();j++) {
							Expr other = sum.get(j);
							if(other instanceof Prod) {
								index = -1;
								for(int k = 0;k < other.size();k++) {
									if(type == "cos" && fastSimilarExpr(sinsqr,other.get(k))) {
										index = k;
										break;
									}else if(type == "sin" && fastSimilarExpr(cossqr,other.get(k))) {
										index = k;
										break;
									}
								}
								if(index != -1) {
									Expr otherVar = other.get(index).get().get();
									Expr otherCoef = other.copy();
									otherCoef.remove(index);
									
									if(var.equals(otherVar) && coef.equals(otherCoef)) {
										sum.set(i, coef.simplify(casInfo));
										sum.remove(j);
										continue outer;
									}
								}
								
							}
						}
						
					}
					
				}
				
			}
			return sum;
		}
	};
	
	public static Rule addLogs = new Rule("add logarithms"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Sum sum = (Sum)e;
			
			IndexSet indexSet = new IndexSet();
			IndexSet indexOfProdWithLog = new IndexSet();
			
			for(int i = 0;i < sum.size();i++) {
				if(sum.get(i).typeName().equals("ln") && !(sum.get(i).get() instanceof Sum) && !(sum.get(i).get().typeName().equals("abs") && sum.get(i).get().get() instanceof Sum)  ) indexSet.ints.add(i);
				else if(sum.get(i) instanceof Prod) {
					Prod innerProd = (Prod)sum.get(i);
					int innerLogCount = 0;
					boolean onlyConstantsOutside = true;
					
					for(int j = 0;j<innerProd.size();j++) {
						if(innerProd.get(j).typeName().equals("ln")) {
							if(!(innerProd.get(j).get() instanceof Sum) && !(innerProd.get(j).get().typeName().equals("abs") && innerProd.get(j).get().get() instanceof Sum)) {
								innerLogCount++;
							}
						}else {
							if(innerProd.get(j).containsVars())onlyConstantsOutside = false;
						}
					}
					if(innerLogCount == 1 && onlyConstantsOutside) {//we want it to constuct a product polynomial
						indexSet.ints.add(i);
						indexOfProdWithLog.ints.add(i);
					}
				}
			}
			
			if(indexSet.ints.size() > 1) {//turn x*ln(y) -> ln(y^x)
				for(Integer index:indexOfProdWithLog.ints) {
					int i = index.intValue();
					Expr prod = sum.get(i);
					Prod nonLog = new Prod();
					for(int j = 0;j < prod.size();j++) {
						if(!(prod.get(j).typeName().equals("ln"))) {
							nonLog.add(prod.get(j));
							prod.remove(j);
							j--;
						}
					}
					Expr log = prod.get(0);
					
					Expr newInnerPow = power(log.get(),nonLog).simplify(casInfo);

					log.set(0, newInnerPow);
					sum.set(i,log);
					
				}
				//now merge
				Prod innerProd = new Prod();
				for(int j = indexSet.ints.size()-1;j >= 0;j--) {
					int i = indexSet.ints.get(j);
					Expr log = sum.get(i);
					innerProd.add(log.get());
					sum.remove(i);
				}
				sum.add(ln(innerProd).simplify(casInfo));
			}
			return sum;
		}
	};
	
	public static Rule distrSubProds = new Rule("distribute sub products"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Sum sum = (Sum)e;
			
			for(int i = 0;i<sum.size();i++) {
				if(sum.get(i) instanceof Prod) {
					sum.set(i,  distr(sum.get(i)).simplify(casInfo));
				}
			}
			
			return sum;
		}
	};
	
	public static Rule sumContainsSum = new Rule("sum contains sum"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Sum sum = (Sum)e;
			for(int i = 0;i<sum.size();i++) {
				Expr current = sum.get(i);
				if(current instanceof Sum) {
					for(int j = 0;j<current.size();j++) sum.add(current.get(j));
					sum.remove(i);//delete from list to remove duplicates
					i--;//shift back after deletion
				}
			}
			return sum;
		}
	};
	
	public static Rule addLikeTerms = new Rule("add like terms"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Sum sum = (Sum)e;
			
			for(int i = 0;i<sum.size();i++) {
				Expr current = sum.get(i).copy();//make sure its copy as we don't want to modify the real object
				if(Limit.isInf(current)) continue;
				
				Expr coef = num(1);//coefficient
				
				if(current instanceof Prod || current.typeName().equals("div")) {//if its a product
					Sequence parts = seperateCoef(current);
					coef = parts.get(0);
					current = parts.get(1);
				}
				
				boolean foundSame = false;
				for(int j = i+1;j < sum.size();j++) {//the i+1 is more efficient than 0 
					
					Expr toComp = sum.get(j).copy();//expression to compare to
					if(Limit.isInf(toComp)) continue;
					
					Expr toCompCoef = num(1);
					
					if(toComp instanceof Prod || toComp.typeName().equals("div")) {
						Sequence parts = seperateCoef(toComp);
						toCompCoef = parts.get(0);
						toComp = parts.get(1);
					}
					
					if(current.equals(toComp)) {
						sum.remove(j);
						j--;
						foundSame = true;
						coef = sum(coef,toCompCoef).simplify(casInfo);
					}
					
				}
				if(foundSame) {
					if(current instanceof Prod) {//if its a product still just add the coefficient
						current.add(coef);
					}else {//if not just make a new product
						current = prod(current,coef);
					}
					current = current.simplify(casInfo);//this has to be done in case of 0*x must become zero
					sum.set(i, current);//replace the sum element with the new combine like term version
				}
				
			}
			
			return sum;
		}
	};
	
	public static Rule addIntegersAndFractions = new Rule("add integers and fractions"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Sum sum = (Sum)e;
			Num total = Num.ZERO;
			Expr totalFrac = null;
			
			for (int i = 0;i<sum.size();i++) {
				if(sum.get(i) instanceof Num) {
					Num temp = (Num)sum.get(i);
					total = total.addNum(temp);
					sum.remove(i);
					i--;
				}else if(sum.get(i).typeName().equals("div") && Div.isNumerical((Func)sum.get(i))) {
					if(totalFrac == null) {
						totalFrac = sum.get(i);
					}else {
						totalFrac = Div.addFracs((Func)totalFrac, ((Func)sum.get(i)));
					}
					sum.remove(i);
					i--;
				}
			}
			
			if(totalFrac != null) {
				totalFrac = Div.addFracs((Func)totalFrac, Cas.div(total,num(1)));
				totalFrac = totalFrac.simplify(CasInfo.normal);
				sum.add(totalFrac);
			}else {
				if(!total.equals(Num.ZERO)) {
					sum.add(total);
				}
			}
			return sum;
		}
	};
	
	public static Rule alone = new Rule("alone sum"){
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Sum sum = (Sum)e;
			
			if(sum.size() == 1) {//if a sum is only one element 
				return sum.get(0);
			}else if(sum.size() == 0) {//if the sum is empty return 0
				return num(0);
			}
			return sum;
		}
	};
	
	public static Rule basicPythagIden = new Rule("basic pythagorean identity") {
		Expr sinSqrTemplate,cosSqrTemplate,sinSqrProdTemplate,cosSqrProdTemplate;
		
		@Override
		public void init() {
			sinSqrTemplate = createExpr("sin(x)^2");
			cosSqrTemplate = createExpr("cos(x)^2");
			sinSqrProdTemplate = createExpr("a*sin(x)^2");
			cosSqrProdTemplate = createExpr("a*cos(x)^2");
		}
		
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Sum sum = (Sum)e;
			
			outer:for(int i = 0;i<sum.size();i++) {
				Expr current = sum.get(i);
				if(Rule.fastSimilarExpr(sinSqrTemplate, current )) {
					Expr var = current.get().get();
					
					for(int j = 0;j < sum.size();j++) {
						if(j == i) continue;
						if(sum.get(j).equals(Num.NEG_ONE)) {
							sum.remove(Math.max(i, j));
							sum.set(Math.min(i, j), neg(power(cos(var),num(2))));
							i--;
							continue outer;
						}
					}
				}else if(Rule.fastSimilarExpr(cosSqrTemplate, current )) {
					Expr var = current.get().get();
					
					for(int j = 0;j < sum.size();j++) {
						if(j == i) continue;
						if(sum.get(j).equals(Num.NEG_ONE)) {
							sum.remove(Math.max(i, j));
							sum.set(Math.min(i, j), neg(power(sin(var),num(2))));
							i--;
							continue outer;
						}
					}
				}else if(Rule.fastSimilarExpr(sinSqrProdTemplate, sum.get(i))) {
					Func equsSet = Rule.getEqusFromTemplate(sinSqrProdTemplate, sum.get(i));
					Expr a = Rule.getExprByName(equsSet, "a");
					Expr x = Rule.getExprByName(equsSet, "x");
					
					Expr negA = neg(a).simplify(casInfo);
					
					for(int j = 0;j < sum.size();j++) {
						if(j == i) continue;
						if(sum.get(j).equals(negA)) {
							sum.remove(Math.max(i, j));
							sum.set(Math.min(i, j), prod(negA,power(cos(x),num(2))).simplify(casInfo) );
							i--;
							continue outer;
						}
					}
				}else if(Rule.fastSimilarExpr(cosSqrProdTemplate, sum.get(i))) {
					Func equsSet = Rule.getEqusFromTemplate(cosSqrProdTemplate, sum.get(i));
					Expr a = Rule.getExprByName(equsSet, "a");
					Expr x = Rule.getExprByName(equsSet, "x");
					
					Expr negA = neg(a).simplify(casInfo);
					
					for(int j = 0;j < sum.size();j++) {
						if(j == i) continue;
						if(sum.get(j).equals(negA)) {
							sum.remove(Math.max(i, j));
							sum.set(Math.min(i, j), prod(negA,power(sin(x),num(2))).simplify(casInfo) );
							i--;
							continue outer;
						}
					}
				}
			}
			
			return sum;
		}
	};
	
	static Rule sumWithMatrix = new Rule("sum with matrix") {//sum of each element
		@Override
		public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
			Sum sum = (Sum)e;
			if(sum.containsType("mat")) {
				Sum matricies = new Sum();
				Sum nonMatricies = new Sum();
				
				for(int i = 0;i<sum.size();i++) {
					if(sum.get(i) instanceof Mat) {
						matricies.add(sum.get(i));
					}else {
						nonMatricies.add(sum.get(i));
					}
				}
				
				if(matricies.size()>0) {
					Mat total = (Mat)matricies.get(0);
					for(int i = 1;i<matricies.size();i++) {
						Mat other = (Mat)matricies.get(i);
						
						for(int row = 0;row<total.rows();row++) {
							for(int col = 0;col<total.cols();col++) {
								
								Sum elsum = Sum.cast(total.getElement(row, col));
								elsum.add(other.getElement(row, col));
								
								total.setElement(row, col, elsum  );
								
							}
							
						}
					}
					
					for(int row = 0;row<total.rows();row++) {
						for(int col = 0;col<total.cols();col++) {
							
							Sum elsum = Sum.cast(total.getElement(row, col));
							elsum.add(nonMatricies);
							
							total.setElement(row, col, elsum  );
							
						}
						
					}
					
					return total.simplify(casInfo);
				}
			}
			return sum;
		}
		
	};
	
	static Rule mainSequenceRule = null;

	public static void loadRules(){
		mainSequenceRule = new Rule(new Rule[]{
				sumWithInf,
				distrSubProds,
				basicPythagIden,//1-sin(x)^2=cos(x)^2 cases
				complexPythagIden,//Variants of sin(x)^2+cos(x)^2
				sumContainsSum,//sums contains a sum
				trigExpandElements,
				addLogs,
				addIntegersAndFractions,//1+2 = 3
				addLikeTerms,//x+x = 2*x
				addIntegersAndFractions,//1+2 = 3
				sumWithMatrix,
				alone,//alone sum is 0
				pythagOnePlusTanSqr
		},"main sequence");
		mainSequenceRule.init();
	}
	
	@Override
	public Rule getRule() {
		return mainSequenceRule;
	}
	
	@Override
	public String toString() {
		String out = "";
		if(size() < 2) out+="alone sum:";
		for(int i = 0;i < size();i++) {
			out+=get(i).toString();
			boolean useNothing = false;
			
			if(i!=size()-1) {
				Expr next = get(i+1);
				if(next instanceof Num) {
					Num numCatsed  = (Num)next;
					if(numCatsed.getRealValue().signum()==-1) useNothing = true;
				}else if(next instanceof Prod){
					Num numCasted = null;
					for(int j = 0;j<next.size();j++) {
						if(next.get(j) instanceof Num) {
							numCasted = (Num)next.get(j);
							break;
						}
					}
					if(numCasted != null) {
						if(numCasted.getRealValue().signum()==-1) useNothing = true;
					}
				}
			}
			
			if(i != size()-1) {
				if(!useNothing) out+='+';
			}
		}
		return out;
	}
	
	public static Sum cast(Expr e) {
		if(e instanceof Sum) {
			return (Sum)e;
		}
		Sum out = new Sum();
		out.add(e);
		return out;
	}
	
	public static Expr unCast(Expr e) {
		if(e instanceof Sum) {
			Sum casted = (Sum)e;
			if(casted.size() == 0) {
				return num(0);
			}else if(casted.size() == 1) {
				return casted.get();
			}else {
				return casted;
			}
		}
		return e;
	}
	
	public static Sum combineSums(Sum a,Sum b) {//creates new sum object
		Sum out = new Sum();
		for(int i = 0;i<a.size();i++) {
			out.add(a.get(i).copy());
		}
		for(int i = 0;i<b.size();i++) {
			out.add(b.get(i).copy());
		}
		return out;
	}
	
	public static Sum combine(Expr a,Expr b) {//like the sum(a,b) function but handles it better, avoids sums in sums
		Sum aCasted = Sum.cast(a),bCasted = Sum.cast(b);
		return Sum.combineSums(aCasted, bCasted);
	}
	@Override
	public ComplexFloat convertToFloat(Func varDefs) {
		ComplexFloat total = new ComplexFloat(0,0);
		for(int i = 0;i<size();i++) total =ComplexFloat.add(total, get(i).convertToFloat(varDefs));
		return total;
	}

	@Override
	public String typeName() {
		return "sum";
	}

	@Override
	public String help() {
		return "+ operator\n"
				+ "examples\n"
				+ "2+3->5\n"
				+ "x+x+y->2*x+y";
	}
}
