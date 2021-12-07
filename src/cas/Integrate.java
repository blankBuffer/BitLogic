package cas;

public class Integrate extends Expr{

	private static final long serialVersionUID = 5071855237530369367L;
	
	static boolean showSteps = false;
	
	static Equ zeroCase =  (Equ)createExpr("integrate(0,x)=0");
	static Equ varCase = (Equ)createExpr("integrate(x,x)=x^2/2");
	static Equ invRule = (Equ)createExpr("integrate(inv(x),x)=ln(x)");
	static Equ powerRule = (Equ)createExpr("integrate(x^n,x)=x^(n+1)/(n+1)");
	static Equ inversePowerRule = (Equ)createExpr("integrate(1/x^n,x)=-1/(x^(n-1)*(n-1))");
	static Equ exponentRule = (Equ)createExpr("integrate(n^x,x)=n^x/ln(n)");
	static Equ logCase = (Equ)createExpr("integrate(ln(x),x)=ln(x)*x-x");
	static Equ cosCase = (Equ)createExpr("integrate(cos(x),x)=sin(x)");
	static Equ sinCase = (Equ)createExpr("integrate(sin(x),x)=-cos(x)");
	static Equ tanCase = (Equ)createExpr("integrate(tan(x),x)=-ln(cos(x))");
	static Equ secSqr = (Equ)createExpr("integrate(cos(x)^-2,x)=tan(x)");
	static Equ atanCase = (Equ)createExpr("integrate(atan(x),x)=x*atan(x)+ln(x^2+1)/-2");
	static Equ eToXTimesSinX = (Equ)createExpr("integrate(e^x*sin(x),x)=e^x*(sin(x)-cos(x))/2");
	static Equ eToXTimesCosX = (Equ)createExpr("integrate(e^x*cos(x),x)=e^x*(sin(x)+cos(x))/2");
	
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
		
		toBeSimplified = toBeSimplified.modifyFromExample(zeroCase,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = noVarCase((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = pullOutConstants((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = toBeSimplified.modifyFromExample(varCase,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = toBeSimplified.modifyFromExample(invRule,settings);
		toBeSimplified = toBeSimplified.modifyFromExample(inversePowerRule,settings);
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
		if(toBeSimplified instanceof Integrate) toBeSimplified = toBeSimplified.modifyFromExample(tanCase,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = toBeSimplified.modifyFromExample(secSqr,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = toBeSimplified.modifyFromExample(atanCase,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = toBeSimplified.modifyFromExample(eToXTimesSinX,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = toBeSimplified.modifyFromExample(eToXTimesCosX,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = inverseQuadratic((Integrate)toBeSimplified,settings);//integration of inverse quadratic
		if(toBeSimplified instanceof Integrate) toBeSimplified = polyDiv((Integrate)toBeSimplified,settings);
		//if(toBeSimplified instanceof Integrate) toBeSimplified = partialFraction((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = specialUSub((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = normalUSub((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified.set(0, distr(((Integrate)toBeSimplified).get()).simplify(settings));
		if(toBeSimplified instanceof Integrate) toBeSimplified = ibp((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = ibpSpecial((Integrate)toBeSimplified,settings);
		if(toBeSimplified instanceof Integrate) toBeSimplified = integralSum((Integrate)toBeSimplified,settings);
		
		//if(toBeSimplified instanceof Prod) toBeSimplified = distr(toBeSimplified).simplify(settings);
		if(toBeSimplified instanceof Sum) {//remove constants
			Sum casted = (Sum)toBeSimplified;
			for(int i = 0;i<casted.size();i++) {
				if(!casted.get(i).contains( getVar() )) {
					casted.remove(i);
					i--;
				}
			}
		}
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	Expr inverseQuadratic(Integrate integ,Settings settings) {
		if(integ.get() instanceof Div && !((Div)integ.get()).getNumer().contains(integ.getVar())) {
			Expr denom = ((Div)integ.get()).getDenom();
			ExprList poly = polyExtract(denom, integ.getVar(), settings);
			if(poly != null && poly.size() == 3) {
				Expr c = poly.get(0),b = poly.get(1),a = poly.get(2);
				
				Expr check = sub(prod(num(4),a,c),pow(b,num(2))).simplify(settings);
				if(!check.negative()) {
					check = pow(check,inv(num(-2)));
					return prod(num(2),check,atan(prod(check, sum(prod(num(2),a,integ.getVar()),b) )) ).simplify(settings);
				}
				Expr bsquare4ac = sqrt(sub(pow(b,num(2)),prod(num(4),a,c)));
				Expr twoaxpb = sum(prod(num(2),a,integ.getVar()),b);
				
				return div(ln( div( sub(twoaxpb,bsquare4ac) , sum(twoaxpb,bsquare4ac)  ) ),bsquare4ac).simplify(settings);
			}
		}
		return integ;
	}
	
	Expr arctanCase(Integrate integ,Settings settings) {
		if(integ.get() instanceof Div) {
			Expr denom = ((Div)integ.get()).getDenom();
			ExprList poly = polyExtract(denom, integ.getVar(), settings);
			if(poly != null && poly.size() == 3) {
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
	
	Expr partialFraction(Integrate integ,Settings settings) {
		integ.set(0, partialFrac(integ.get(), integ.getVar(), settings) );
		if(integ.get() instanceof Sum) {
			return integralSum(integ,settings);
		}
		return integ;
	}
	
	Expr polyDiv(Integrate integ,Settings settings) {//polynomial division
		integ.set(0, polyDiv(integ.get(), integ.getVar(), settings) );
		if(integ.get() instanceof Sum) {
			return integralSum(integ,settings);
		}
		return integ;
	}

	Expr ibpSpecial(Integrate integ,Settings settings) {
		if(integ.get() instanceof Div && !integ.get().containsType(Integrate.class)) {
			Div innerDiv = (Div)integ.get().copy();
			if(innerDiv.getDenom() instanceof Power) {
				Power denomPower = (Power)innerDiv.getDenom();
				Div expo = Div.cast(denomPower.getExpo());
				if(expo.isNumericalAndReal()) {
					if( ((Num)expo.getNumer()).realValue.compareTo( ((Num)expo.getDenom()).realValue )  == 1) {//make sure the fraction is greater than 1
						Expr integralOfDenom = integrate(inv(denomPower),integ.getVar()).simplify(settings);
						Expr derivativeOfNumer = diff(innerDiv.getNumer(),integ.getVar()).simplify(settings);
						Expr out = sub(prod(innerDiv.getNumer(),integralOfDenom),integrate( prod(derivativeOfNumer,integralOfDenom.copy()) ,integ.getVar()));
						return out.simplify(settings);
					}
				}
			}
		}
		return integ;
	}
	
	Expr ibp(Integrate integ,Settings settings) {
		if(integ.get() instanceof Prod && !integ.get().containsType(Integrate.class)) {
			Prod innerProd = (Prod)integ.get().copy();
			int bestIndex = -1;
			for(int i = 0;i < innerProd.size();i++) {
				if(innerProd.get(i) instanceof Power) {
					Power pow = (Power)innerProd.get(i);
					if(pow.getBase().contains(integ.getVar())) {
						if(pow.getExpo() instanceof Div) {
							
							Div frac = (Div)pow.getExpo();
							if(frac!=null && frac.isNumericalAndReal()) {
								if(((Num)frac.getNumer()).realValue.signum() == 1) {
									bestIndex = i;
									break;
								}
							}
							
						}else if(pow.getExpo() instanceof Num && !pow.getExpo().negative() && !((Num)pow.getExpo()).isComplex() ) {
							bestIndex = i;
							break;
						}
					}
				}else if(innerProd.get(i).equalStruct(integ.getVar())) {
					bestIndex = i;
					break;
				}
			}
			for(int i = 0;i < innerProd.size();i++) {
				if(innerProd.get(i) instanceof Log || innerProd.get(i) instanceof Atan) {//log and inverse trigonometric
					bestIndex = i;
					break;
				}
			}
			if(bestIndex != -1) {
				Expr best = innerProd.get(bestIndex);
				innerProd.remove(bestIndex);
				Expr newIntegral = integrate(innerProd,integ.getVar()).simplify(settings);
				if(!newIntegral.containsType(Integrate.class)) {
					Expr out = sub(prod(newIntegral,best),integrate(prod(newIntegral.copy(),diff(best.copy(),integ.getVar())),integ.getVar()));
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
		if(integ.contains(uSubVar) || integ.get().containsType(Integrate.class)) return integ;
		Expr u = null;
		if(integ.get() instanceof Prod) {
			Prod innerProd = (Prod)integ.get();
			long highestComplexity = 0;
			int indexOfHighestComplexity = 0;
			for(int i = 0;i<innerProd.size();i++) {
				if(innerProd.get(i) instanceof Sum) continue;//skip sums because thats usually du
				long current = innerProd.get(i).complexity();
				if(current > highestComplexity) {
					highestComplexity = current;
					indexOfHighestComplexity = i;
				}
			}
			u = innerProd.get(indexOfHighestComplexity);
		}else if(integ.get() instanceof Div) {
			Div casted =  ((Div)integ.get());
			boolean logCase = !div(casted.getNumer(),diff(casted.getDenom() ,integ.getVar())).simplify(settings).contains(integ.getVar());
			if(!casted.getNumer().contains(integ.getVar()) || logCase) {
				u = casted.getDenom();
			}else {
				u = casted.getNumer();
			}
		}else {
			u = integ.get();
		}
		
		if(u != null) {
			while(true) {//try normal u and innermost u sub
				
				if(!u.equalStruct(integ.getVar())) {
					Equ eq = equ(u,uSubVar);//im calling it 0u since variables normally can't start with number
					
					Expr diffObj = diff(u,(Var)integ.getVar().copy()).simplify(settings);
					diffObj = diffObj.replace(eq);//it is possible for derivative to contain u
					Expr before = div(integ.get().replace(eq),diffObj);
					Expr newExpr = before.simplify(settings);
					if(!newExpr.contains(integ.getVar())) {//no solve needed
						newExpr = integrate(newExpr,uSubVar).simplify(settings);
						if(!newExpr.containsType(Integrate.class)) {
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
							if(!newExpr.containsType(Integrate.class)) {
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
				u = newU;
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
				if(!resToCheck.contains(integ.getVar())) {
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
		Expr res = seperateByVar(integ.get(),integ.getVar());
		if(!res.get(0).equalStruct(Num.ONE)) {
			return prod(res.get(0),integrate(res.get(1),integ.getVar())).simplify(settings);
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
	boolean similarStruct(Expr other, boolean checked) {
		if(!checked) if(checkForMatches(other) == false) return false;
		
		if(other instanceof Integrate) {
			return get().fastSimilarStruct(other.get()) && getVar().fastSimilarStruct( ((Integrate)other).getVar());
		}
		return false;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return integrateOver(num(0),getVar(),get(),getVar()).convertToFloat(varDefs);
	}

}
