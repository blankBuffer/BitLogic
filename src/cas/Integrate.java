package cas;

import java.util.ArrayList;

public class Integrate extends Expr{

	private static final long serialVersionUID = 5071855237530369367L;
	
	static boolean showSteps = false;
	
	static Equ zeroCase =  (Equ)createExpr("integrate(0,x)=0");
	static Equ varCase = (Equ)createExpr("integrate(x,x)=x^2/2");
	static Equ invRule = (Equ)createExpr("integrate(inv(x),x)=ln(x)");
	static Equ powerRule = (Equ)createExpr("integrate(x^n,x)=x^(n+1)/(n+1)");
	static Equ exponentRule = (Equ)createExpr("integrate(n^x,x)=n^x/ln(n)");
	static Equ logCase = (Equ)createExpr("integrate(ln(x),x)=ln(x)*x-x");
	static Equ cosCase = (Equ)createExpr("integrate(cos(x),x)=sin(x)");
	static Equ sinCase = (Equ)createExpr("integrate(sin(x),x)=-cos(x)");
	
	public Integrate(Expr e,Var v){
		add(e);
		add(v);
	}
	
	Var getVar() {
		return (Var)get(1);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		toBeSimplified.simplifyChildren(settings);
		
		
		settings = new Settings(settings);//create new settings object with same parameters
		settings.factor = false;//disable factoring for faster processing
		
		toBeSimplified = toBeSimplified.modifyFromExample(zeroCase,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = noVarCase((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = pullOutConstants((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = toBeSimplified.modifyFromExample(varCase,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = toBeSimplified.modifyFromExample(invRule,settings);
		if(toBeSimplified instanceof Integrate) {
			Integrate casted = (Integrate)toBeSimplified;
			if(casted.get() instanceof Power) {
				Power p = (Power)casted.get();
				if(p.getExpo().contains(casted.getVar()) ^ p.getBase().contains(casted.getVar())) {//either but not both
					toBeSimplified = toBeSimplified.modifyFromExample(powerRule,settings);
					toBeSimplified = toBeSimplified.modifyFromExample(exponentRule,settings);
				}
			}
		}
		if(toBeSimplified instanceof Integrate) toBeSimplified = toBeSimplified.modifyFromExample(logCase,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = toBeSimplified.modifyFromExample(cosCase,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = toBeSimplified.modifyFromExample(sinCase,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = arctanCase((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = polyDiv((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = specialUSub((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = normalUSub((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified.set(0, distr(((Integrate)toBeSimplified).get()).simplify(settings));
		if(toBeSimplified instanceof Integrate) toBeSimplified = ibp((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = ibpSpecial((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = integralSum((Integrate)toBeSimplified,settings);
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	Expr arctanCase(Integrate integ,Settings settings) {
		if(invObj.fastSimilarStruct(integ.get())) {
			Expr denom = integ.get().get();
			ExprList poly = polyExtract(denom, integ.getVar(), settings);
			if(poly != null) {
				Expr c = poly.get(0),b = poly.get(1),a = poly.get(2);
				
				Expr check = sub(prod(num(4),a,c),pow(b,num(2))).simplify(settings);
				if(!check.negative()) {
					check = pow(check,inv(num(-2)));
					return prod(num(2),check,atan(prod(check, sum(prod(num(2),a,integ.getVar()),b) )) ).simplify(settings);
				}
			}
		}
		return integ;
	}
	
	Expr polyDiv(Integrate integ,Settings settings) {//polynomial division
		Expr out = integ;
		if(integ.get() instanceof Prod) {
			Expr[] frac = extractFrac((Prod)integ.get());
			Expr oldDen = frac[1];
			
			frac[0] = distr(frac[0]).simplify(settings);
			
			settings = new Settings(settings);
			settings.powExpandMode = true;
			frac[1] = distr(  inv(frac[1])  ).simplify(settings);
			settings.powExpandMode = true;
			
			ExprList numPoly = polyExtract(frac[0],integ.getVar(),settings);
			ExprList denPoly = polyExtract(frac[1],integ.getVar(),settings);
			
			if(numPoly != null && denPoly != null && numPoly.size()>=denPoly.size()) {
				
				ExprList[] result = polyDiv(numPoly,denPoly,settings);
				Expr outPart =  exprListToPoly(result[0],integ.getVar(),settings);
				Expr remainPart =  prod(exprListToPoly(result[1],integ.getVar(),settings),oldDen);
				
				integ.set(0, sum(outPart,remainPart));
				out = integralSum((Integrate)integ,settings);
			}
			
		}
		
		return out;
	}

	Expr ibpSpecial(Integrate integ,Settings settings) {
		if(integ.get() instanceof Prod) {
			Prod innerProd = (Prod)integ.get().copy();
			int bestIndex = -1;
			for(int i = 0;i < innerProd.size();i++) {
				if(innerProd.get(i) instanceof Power) {
					Power pow = (Power)innerProd.get(i);
					if(pow.getBase().contains(integ.getVar())) {
						
						Num[] frac = extractNormalFrac(pow.getExpo());
						if(frac!=null) {
							if(frac[0].value.abs().compareTo(frac[1].value) == 1 && frac[0].value.signum() == -1) {
								bestIndex = i;
								break;
							}
						}
						
					}
				}
			}
			if(bestIndex != -1) {
				Expr bestExpr = innerProd.get(bestIndex);
				Expr iBest = integrate(bestExpr ,(Var)integ.getVar().copy()).simplify(settings);
				if(!iBest.containsIntegrals()) {
					innerProd.remove(bestIndex);
					
					Expr out = sub(prod(innerProd,iBest),integrate( prod(diff(innerProd.copy(),(Var)integ.getVar()),iBest.copy()) ,(Var)integ.getVar().copy()));
					if(showSteps) {
						System.out.println(integ+":integration by parts, integrate:"+bestExpr+":result:"+out);
					}
					return out.simplify(settings);
					
				}
				
			}
		}
		return integ;
	}
	
	Expr ibp(Integrate integ,Settings settings) {
		
		if(integ.get() instanceof Prod) {
			Prod innerProd = (Prod)integ.get().copy();
			int bestIndex = -1;
			for(int i = 0;i < innerProd.size();i++) {
				if(innerProd.get(i) instanceof Power) {
					Power pow = (Power)innerProd.get(i);
					if(pow.getBase().contains(integ.getVar())) {
						
						Num[] frac = extractNormalFrac(pow.getExpo());
						if(frac!=null ) {
							if(frac[0].value.signum() == 1) {
								bestIndex = i;
								break;
							}
						}
						
					}
				}else if(innerProd.get(i).equalStruct(integ.getVar())) {
					bestIndex = i;
					break;
				}
			}
			for(int i = 0;i < innerProd.size();i++) {
				if(innerProd.get(i) instanceof Log) {//log and inverse trigonometric
					bestIndex = i;
					break;
				}
			}
			if(bestIndex != -1) {
				Expr best = innerProd.get(bestIndex);
				innerProd.remove(bestIndex);
				Expr newIntegral = integrate(innerProd,(Var)integ.getVar()).simplify(settings);
				if(!newIntegral.containsIntegrals()) {
					Expr out = sub(prod(newIntegral,best),integrate(prod(newIntegral.copy(),diff(best.copy(),integ.getVar())),(Var)integ.getVar()));
					if(showSteps) {
						System.out.println(integ+":integration by parts, diff:"+best+":result:"+out);
					}
					return out.simplify(settings);
				}
			}
			
		}
		return integ;
	}
	
	private static Var uSubVar = var("0u");
	
	Expr normalUSub(Integrate integ,Settings settings) {
		if(integ.contains(uSubVar)) return integ;
		Expr u = null;
		if(integ.get() instanceof Prod) {
			Prod innerProd = (Prod)integ.get();
			int highestNestDepth = 0;
			int indexOfHighestComplexity = 0;
			for(int i = 0;i<innerProd.size();i++) {
				if(innerProd.get(i) instanceof Sum) continue;//skip sums because thats usually du
				int current = innerProd.get(i).nestDepth();
				if(current > highestNestDepth) {
					highestNestDepth = current;
					indexOfHighestComplexity = i;
				}
			}
			u = innerProd.get(indexOfHighestComplexity);
		}else {
			u = integ.get().copy();
		}
		
		if(u != null) {
			while(true) {//try normal u and innermost u sub
				if(!u.equalStruct(integ.getVar())) {
					Equ eq = equ(u,uSubVar);//im calling it 0u since variables normally can't start with number
					Expr diffObj = diff(u,(Var)integ.getVar().copy()).simplify(settings);
					diffObj = diffObj.replace(eq);//it is possible for derivative to contain u
					Expr newExpr = div(integ.get().replace(eq),diffObj).simplify(settings);
					if(!newExpr.contains(integ.getVar())) {//no solve needed
						newExpr = integrate(newExpr,uSubVar).simplify(settings);
						if(!newExpr.containsIntegrals()) {
							Expr out = newExpr.replace(equ(uSubVar,u));
							if(showSteps) {
								System.out.println(integ+":u sub:"+eq+":result:"+out);
							}
							return out.simplify(settings);
						}
					}else {//oof we need to solve for x
						Expr solved = solve(equ(uSubVar,u),integ.getVar()).simplify(settings);
						if(solved instanceof ExprList) solved = solved.get();
						if(!(solved instanceof Solve)) {
							solved = ((Equ)solved).getRightSide();
							newExpr = integrate(newExpr.replace(equ(integ.getVar(),solved)),uSubVar);
							newExpr = newExpr.simplify(settings);
							
							if(!newExpr.containsIntegrals()) {
								Expr out = newExpr.replace(equ(uSubVar,u)).simplify(settings);
								if(showSteps) {
									System.out.println(integ+":u sub with solve:"+eq+":result:"+out);
								}
								return out;
							}
							
						}
					}
					
				}
				Expr newU = u.getNextInnerFunction(integ.getVar());//we slowly work are way in
				if(newU.equalStruct(u)) break;
				else u = newU;
			}
		}
		
		return integ;
	}
	
	Expr specialUSub(Integrate integ,Settings settings) {
		if(integ.get() instanceof Prod) {
			Prod innerProd = (Prod)integ.get();
			
			for(int i = 0;i<innerProd.size();i++) {
				Prod prodCopy = (Prod)innerProd.copy();
				
				Expr testExpr = prodCopy.get(i);
				prodCopy.remove(i);
				Expr resToCheck = div(prodCopy,diff(testExpr,(Var)integ.getVar().copy()));
				resToCheck = resToCheck.simplify(settings);
				if(!resToCheck.contains((Var)integ.getVar())) {
					Prod res = new Prod();
					res.add(pow(testExpr,num(2)));
					res.add(resToCheck);
					res.add(inv(num(2)));
					if(showSteps) {
						System.out.println(integ+":special u sub, let u be:"+testExpr+":result:"+res);
					}
					return res.simplify(settings);
				}
				
			}
			
		}
		
		return integ;
	}
	
	Expr integralSum(Integrate integ,Settings settings) {
		if(integ.get() instanceof Sum) {
			if(showSteps) {
				System.out.println(integ+":seperation");
			}
			Expr innerSum = integ.get();
			for(int i = 0;i<innerSum.size();i++) innerSum.set(i, integrate(innerSum.get(i),integ.getVar()));
			return innerSum.simplify(settings);
		}
		return integ;
	}
	
	Expr noVarCase(Integrate integ,Settings settings) {
		if(!integ.get().contains(integ.getVar())) {
			return prod(integ.get(),integ.getVar()).simplify(settings);
		}
		return integ;
	}
	
	Expr pullOutConstants(Integrate integ,Settings settings) {
		if(integ.get() instanceof Prod) {
			Prod res = new Prod();
			Expr innerProd = integ.get();
			for(int i = 0;i<innerProd.size();i++) {
				if(!innerProd.get(i).contains(integ.getVar())) {
					res.add(innerProd.get(i));
					innerProd.remove(i);
					i--;
				}
			}
			if(res.size()>0) {
				if(showSteps) {
					System.out.println(integ+":pulling constants");
				}
				res.add(integ);
				return res.simplify(settings);
			}else return integ;
			
		}
		return integ;
	}

	@Override
	public Expr copy() {
		Integrate out = new Integrate(get().copy(),(Var)getVar().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="integrate(";
		out+=get().toString();
		out+=',';
		out+=getVar().toString();
		out+=')';
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Integrate) return get().equalStruct(other.get()) && getVar().equalStruct( ((Integrate)other).getVar() );
		return false;
	}

	@Override
	public long generateHash() {
		return (get().generateHash()+92617*getVar().generateHash())-1730361936502638193L;
	}

	@Override
	public Expr replace(ArrayList<Equ> equs) {
		for(Equ e:equs) if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		Expr mainPart = get().replace(equs);
		Var v = (Var)getVar().replace(equs);
		return integrate(mainPart,v);
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(!checked) if(checkForMatches(other) == false) return false;
		
		if(other instanceof Integrate) {
			return get().fastSimilarStruct(other.get()) && getVar().fastSimilarStruct( ((Integrate)other).getVar());
		}
		return false;
	}

	@Override
	public double convertToFloat(ExprList varDefs) {
		return integrateOver(num(0),getVar(),get(),getVar()).convertToFloat(varDefs);
	}

}
