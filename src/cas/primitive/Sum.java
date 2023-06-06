package cas.primitive;

import cas.base.CasInfo;
import cas.base.ComplexFloat;
import cas.base.Expr;
import cas.base.Func;
import cas.base.Rule;
import cas.calculus.Limit;
import cas.matrix.Mat;

import static cas.Cas.*;

import cas.Algorithms;

public class Sum{
	
	public static Func.FuncLoader sumLoader = new Func.FuncLoader() {
		
		@Override
		public void load(Func owner) {
			owner.behavior.commutative = true;
			
			Rule pythagOnePlusTanSqr = new Rule("1+tan(x)^2->cos(x)^-2","one plus tangent squared");
			
			Rule sumWithInf = new Rule("sum with infinity"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func sum = (Func)e;
					
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
			
			Rule trigExpandElements = new Rule("trig expand elements"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func sum = (Func)e;
					if(sum.containsType("sin")){
						for(int i = 0;i < sum.size();i++){
							sum.set(i, Algorithms.trigExpand(sum.get(i),casInfo));
						}
					}
					return sum;
				}
			};
			
			Rule complexPythagIden = new Rule("pythagorean identity"){//sin(x)^2+cos(x)^2 = 1 and a*sin(x)^2+a*cos(x)^2=a
				Expr sinsqr,cossqr;
				
				@Override
				public void init(){
					sinsqr = createExpr("sin(x)^2");
					cossqr = createExpr("cos(x)^2");	
				}
				
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func sum = (Func)e;
					
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
						}else if(current.isType("prod")) {
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
									if(other.isType("prod")) {
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
			
			Rule addLogs = new Rule("add logarithms"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func sum = (Func)e;
					
					Algorithms.IndexSet indexSet = new Algorithms.IndexSet();
					Algorithms.IndexSet indexOfProdWithLog = new Algorithms.IndexSet();
					
					for(int i = 0;i < sum.size();i++) {
						if(sum.get(i).isType("ln") && !(sum.get(i).get().isType("sum")) && !(sum.get(i).get().isType("abs") && sum.get(i).get().get().isType("sum"))  ) indexSet.ints.add(i);
						else if(sum.get(i).isType("prod")) {
							Func innerProd = (Func)sum.get(i);
							int innerLogCount = 0;
							boolean onlyConstantsOutside = true;
							
							for(int j = 0;j<innerProd.size();j++) {
								if(innerProd.get(j).isType("ln")) {
									if(!(innerProd.get(j).get().isType("sum")) && !(innerProd.get(j).get().isType("abs") && innerProd.get(j).get().get().isType("sum"))) {
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
							Func nonLogProd = prod();
							for(int j = 0;j < prod.size();j++) {
								if(!(prod.get(j).isType("ln"))) {
									nonLogProd.add(prod.get(j));
									prod.remove(j);
									j--;
								}
							}
							Expr log = prod.get(0);
							
							Expr newInnerPow = power(log.get(),nonLogProd).simplify(casInfo);

							log.set(0, newInnerPow);
							sum.set(i,log);
							
						}
						//now merge
						Func innerProd = prod();
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
			
			Rule distrSubProds = new Rule("distribute sub products"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func sum = (Func)e;
					
					for(int i = 0;i<sum.size();i++) {
						if(sum.get(i).isType("prod")) {
							sum.set(i,  distr(sum.get(i)).simplify(casInfo));
						}
					}
					
					return sum;
				}
			};
			
			Rule sumContainsSum = new Rule("sum contains sum"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func sum = (Func)e;
					for(int i = 0;i<sum.size();i++) {
						Expr current = sum.get(i);
						if(current.isType("sum")) {
							for(int j = 0;j<current.size();j++) sum.add(current.get(j));
							sum.remove(i);//delete from list to remove duplicates
							i--;//shift back after deletion
						}
					}
					return sum;
				}
			};
			
			Rule addLikeTerms = new Rule("add like terms"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func sum = (Func)e;
					
					for(int i = 0;i<sum.size();i++) {
						Expr current = sum.get(i).copy();//make sure its copy as we don't want to modify the real object
						if(Limit.isInf(current)) continue;
						
						Expr coef = num(1);//coefficient
						
						if(current.isType("prod") || current.isType("div")) {//if its a product
							Func partsSequence = Algorithms.seperateCoef(current);
							coef = partsSequence.get(0);
							current = partsSequence.get(1);
						}
						
						boolean foundSame = false;
						for(int j = i+1;j < sum.size();j++) {//the i+1 is more efficient than 0 
							
							Expr toComp = sum.get(j).copy();//expression to compare to
							if(Limit.isInf(toComp)) continue;
							
							Expr toCompCoef = num(1);
							
							if(toComp.isType("prod") || toComp.isType("div")) {
								Func partsSequence = Algorithms.seperateCoef(toComp);
								toCompCoef = partsSequence.get(0);
								toComp = partsSequence.get(1);
							}
							
							if(current.equals(toComp)) {
								sum.remove(j);
								j--;
								foundSame = true;
								coef = sum(coef,toCompCoef).simplify(casInfo);
							}
							
						}
						if(foundSame) {
							if(current.isType("prod")) {//if its a product still just add the coefficient
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
			
			Rule addIntegersAndFractions = new Rule("add integers and fractions"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func sum = (Func)e;
					Num total = Num.ZERO;
					Expr totalFrac = null;
					
					for (int i = 0;i<sum.size();i++) {
						if(sum.get(i) instanceof Num) {
							Num temp = (Num)sum.get(i);
							total = total.addNum(temp);
							sum.remove(i);
							i--;
						}else if(sum.get(i).isType("div") && Div.isNumerical((Func)sum.get(i))) {
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
						totalFrac = Div.addFracs((Func)totalFrac, div(total,num(1)));
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
			
			Rule alone = new Rule("alone sum"){
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func sum = (Func)e;
					
					if(sum.size() == 1) {//if a sum is only one element 
						return sum.get(0);
					}else if(sum.size() == 0) {//if the sum is empty return 0
						return num(0);
					}
					return sum;
				}
			};
			
			Rule basicPythagIden = new Rule("basic pythagorean identity") {
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
					Func sum = (Func)e;
					
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
			
			Rule sumWithMatrix = new Rule("sum with matrix") {//sum of each element
				@Override
				public Expr applyRuleToExpr(Expr e,CasInfo casInfo){
					Func sum = (Func)e;
					if(sum.containsType("mat")) {
						Func matriciesSum = sum();
						Func nonMatriciesSum = sum();
						
						for(int i = 0;i<sum.size();i++) {
							if(sum.get(i).isType("mat")) {
								matriciesSum.add(sum.get(i));
							}else {
								nonMatriciesSum.add(sum.get(i));
							}
						}
						
						if(matriciesSum.size()>0) {
							Func totalMat = (Func)matriciesSum.get(0);
							for(int i = 1;i<matriciesSum.size();i++) {
								Func otherMat = (Func)matriciesSum.get(i);
								
								for(int row = 0;row<Mat.rows(totalMat);row++) {
									for(int col = 0;col<Mat.cols(totalMat);col++) {
										
										Func elsum = Sum.cast(Mat.getElement(totalMat,row, col));
										elsum.add(Mat.getElement(otherMat,row, col));
										
										Mat.setElement(totalMat,row, col, elsum  );
										
									}
									
								}
							}
							
							for(int row = 0;row<Mat.rows(totalMat);row++) {
								for(int col = 0;col<Mat.cols(totalMat);col++) {
									
									Func elsum = Sum.cast(Mat.getElement(totalMat,row, col));
									elsum.add(nonMatriciesSum);
									
									Mat.setElement(totalMat,row, col, elsum  );
									
								}
								
							}
							
							return totalMat.simplify(casInfo);
						}
					}
					return sum;
				}
				
			};
			
			owner.behavior.rule = new Rule(new Rule[] {
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
			
			owner.behavior.toStringMethod = new Func.ToString() {
				@Override
				public String generateString(Func owner) {
					String out = "";
					if(owner.size() < 2) {
						out+="sum(";
						for(int i = 0;i<owner.size();i++) out+=owner.get(i);
						out+=")";
						return out;
					}
					
					
					for(int i = 0;i < owner.size();i++) {
						out+=owner.get(i).toString();
						boolean useNothing = false;
						
						if(i!=owner.size()-1) {
							Expr next = owner.get(i+1);
							if(next instanceof Num) {
								Num numCatsed  = (Num)next;
								if(numCatsed.getRealValue().signum()==-1) useNothing = true;
							}else if(next.isType("prod")){
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
						
						if(i != owner.size()-1) {
							if(!useNothing) out+='+';
						}
					}
					return out;
				}
			};
			
			owner.behavior.toFloat = new Func.FloatFunc() {
				@Override
				public ComplexFloat convertToFloat(Func varDefs, Func owner) {
					ComplexFloat total = new ComplexFloat(0,0);
					for(int i = 0;i<owner.size();i++) total =ComplexFloat.add(total, owner.get(i).convertToFloat(varDefs));
					return total;
				}
			};
		}
	};
	
	public static Func cast(Expr e) {
		if(e.isType("sum")) {
			return (Func)e;
		}
		Func out = sum();
		out.add(e);
		return out;
	}
	
	public static Expr unCast(Expr e) {
		if(e.isType("sum")) {
			Func castedSum = (Func)e;
			if(castedSum.size() == 0) {
				return num(0);
			}else if(castedSum.size() == 1) {
				return castedSum.get();
			}else {
				return castedSum;
			}
		}
		return e;
	}
	
	public static Func combineSums(Func sumA,Func sumB) {//creates new sum object
		Func outSum = sum();
		for(int i = 0;i<sumA.size();i++) {
			outSum.add(sumA.get(i).copy());
		}
		for(int i = 0;i<sumB.size();i++) {
			outSum.add(sumB.get(i).copy());
		}
		return outSum;
	}
	
	//like the sum(a,b) function but handles it better, avoids sums in sums
	public static Func combine(Expr a,Expr b) {//returns sum
		Func aCastedSum = Sum.cast(a),bCastedSum = Sum.cast(b);
		return Sum.combineSums(aCastedSum, bCastedSum);
	}
}
