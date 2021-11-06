package cas;
import java.math.BigInteger;

public class Solve extends Expr{
	
	private static final long serialVersionUID = 8530995692151126277L;
	static Equ logCase = equ( createExpr("ln(a)=b") , createExpr("a=e^b") );//ln(a)=b -> a=e^b
	static Equ rootCase = equ( createExpr("m^n=a") , createExpr("m=a^inv(n)")  );//m^n=a -> m=a^(1/n)
	static Equ expoCase = equ( createExpr("m^n=a")   ,createExpr("n=ln(a)/ln(m)")  );//m^n=a -> n=ln(a)/ln(m)
	static Equ divCase1 = equ(createExpr("a/b=y"),createExpr("a=y*b"));
	static Equ divCase2 = equ(createExpr("a/b=y"),createExpr("b=a/y"));
	
	//Special case with variants involving roots
	static Expr rareCase1Ans = createExpr("x=(a+k^2)^2*k^-2/4-a");
	static Equ rareCase11 = equ( createExpr("sqrt(x)-sqrt(x+a)=k") , rareCase1Ans );
	static Equ rareCase12 = equ( createExpr("sqrt(x+a)-sqrt(x)=k") , rareCase1Ans );
	static Equ rareCase13 = equ( createExpr("sqrt(x+a)+sqrt(x)=k") , rareCase1Ans );
	
	public Solve(Equ e,Var v){
		add(e);
		add(v);
	}
	
	Var getVar() {
		return (Var)get(1);
	}
	
	Equ getEqu() {
		return (Equ)get();
	}
	
	void setEqu(Equ e) {
		set(0,e);
	}
	private static Num ZERO = num(0);//just for comparisons
	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		{//first move everything to the left side
			moveToLeftSide((Solve)toBeSimplified);
		}
		
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		Solve casted = (Solve)toBeSimplified;
		
		Equ oldEqu = null;
		
		while(!casted.getEqu().equalStruct(oldEqu)) {
			oldEqu = (Equ)casted.getEqu().copy();
				
			if( !casted.getEqu().getRightSide().equalStruct(ZERO)) casted.getEqu().setLeftSide( distr(casted.getEqu().getLeftSide()).simplify(settings));//distribute, we don't do it if right side is zero because we can use technique
				
			sumMoveNonImportantToRightSide(casted,settings);//x+a+b=c -> x=c-a-b
			
			casted.getEqu().setLeftSide( factor(casted.getEqu().getLeftSide()).simplify(settings) );//factor
			
			if(casted.getEqu().getRightSide().equalStruct(ZERO) && casted.getEqu().getLeftSide() instanceof Prod) {//if left side is a product and right side is 0 each part of the product is an equation
				ExprList repl = new ExprList();
				
				Prod leftProd = (Prod)casted.getEqu().getLeftSide();
				
				for(int i = 0;i<leftProd.size();i++) {
					if(leftProd.get(i).contains(casted.getVar())) {
						repl.add(solve(equ(leftProd.get(i),num(0)),casted.getVar()));
					}
				}
				
				toBeSimplified = repl.simplify(settings);
				break;
			}
			
			if(casted.nestDepth() >= rareCase13.getLeftSide().nestDepth()) {//for faster computation
				
				Expr.ModifyFromExampleResult result = casted.getEqu().modifyFromExampleSpecific(rareCase11,settings);
				casted.setEqu( (Equ) result.expr  );//sqrt(x)-sqrt(x+a)
				if(!result.success) {
					result = casted.getEqu().modifyFromExampleSpecific(rareCase12,settings);
					casted.setEqu( (Equ) result.expr  );//sqrt(x+a)-sqrt(x)
				}
				if(!result.success) {
					result = casted.getEqu().modifyFromExampleSpecific(rareCase13,settings);
					casted.setEqu( (Equ) result.expr  );//sqrt(x)+sqrt(x+a)
				}
				
			}
				
			prodMoveNonImportantToRightSide(casted,settings);//x*a*b=c -> x=c*inv(a)*inv(b)
			
			if(casted.getEqu().getLeftSide() instanceof Div){
				Div castedDiv = (Div)casted.getEqu().getLeftSide();
				boolean numerHasVar = castedDiv.getNumer().contains(casted.getVar());
				boolean denomHasVar = castedDiv.getDenom().contains(casted.getVar());
				
				if(numerHasVar && !denomHasVar) {
					casted.setEqu((Equ) casted.getEqu().modifyFromExample(divCase1, settings));
				}else if(!numerHasVar && denomHasVar){
					casted.setEqu((Equ) casted.getEqu().modifyFromExample(divCase2, settings));
				}
			}
			
			{
				Equ eq = casted.getEqu();
				if(eq.getLeftSide() instanceof Prod) {
					Prod pr = (Prod)eq.getLeftSide();
					if(pr.size() == 2) {
						Sum sumPart = null;
						Expr varPart = null;
						for(int i = 0;i<2;i++) {
							if(pr.get(i) instanceof Sum) {
								if(sumPart != null) break;
								sumPart = (Sum)pr.get(i);
							}else {
								varPart = pr.get(i);
							}
						}
						if(sumPart!=null && varPart != null) {//I'm not going to use poly extract because i want it to solve more than the generic quadratic like sin(x)^2+sin(x)+2=0
							Expr a = new Sum(),b = new Sum();
							for(int i = 0;i<sumPart.size();i++) {
								if(!sumPart.get(i).contains(varPart)) {
									b.add(sumPart.get(i));
								}else {
									if(sumPart.get(i).equalStruct(varPart)) a.add(num(1));
									else if(sumPart.get(i) instanceof Prod) {
										Prod subProd = (Prod)sumPart.get(i);
										Prod p = new Prod();
										for(int j = 0;j<subProd.size();j++) {
											if(!subProd.get(j).equalStruct(varPart)) {
												p.add(subProd.get(j));
											}
										}
										a.add(p);
									}
								}
							}
							if(!a.contains(casted.getVar()) && !b.contains(casted.getVar())) {
								Expr c = eq.getRightSide();
								
								ExprList out = new ExprList();
								out.add(     solve(equ(varPart,prod(sub(sqrt(  sum(pow(b,num(2)),prod(num(4),a,c))) , b ),inv(num(2)),inv(a))),casted.getVar())     );
								out.add(     solve(equ(varPart,prod(sum(sqrt(  sum(pow(b,num(2)),prod(num(4),a,c))) , b ),inv(num(-2)),inv(a))),casted.getVar())    );
								
								toBeSimplified = out.simplify(settings);
								
								break;
							}
							
						}
					}
				}
				
			}
				
			casted.setEqu( (Equ)casted.getEqu().modifyFromExample(logCase,settings)  );//ln(x)=b -> x=e^b
				
			
			//rules involving powers
			if(casted.getEqu().getLeftSide() instanceof Power) {
				Power castedPower = (Power)casted.getEqu().getLeftSide();
				if(!castedPower.getExpo().contains( casted.getVar() )) {//case 1, only base has x
					casted.setEqu( (Equ)casted.getEqu().modifyFromExample(rootCase,settings)  );
					if(castedPower.getExpo() instanceof Num) {
						Num num = (Num)castedPower.getExpo();
						if(num.realValue.mod(BigInteger.TWO).equals(BigInteger.ZERO) && !num.isComplex()) {//if exponent is divisible by two then there are two solutions
							ExprList repl = new ExprList();//result has more than one solution so stored in a list
							repl.add(casted.copy());
							casted.getEqu().setRightSide(neg(casted.getEqu().getRightSide()));
							repl.add(casted);
							toBeSimplified = repl.simplify(settings);
							break;
						}
					}
				}else if(!castedPower.getBase().contains( casted.getVar() )) {//expo case variable only in exponent
					casted.setEqu( (Equ)casted.getEqu().modifyFromExample(expoCase,settings));
				}
			}
			if(toBeSimplified instanceof Solve) {//solved !!!
				if(casted.getEqu().getLeftSide().equalStruct(casted.getVar())) toBeSimplified = casted.getEqu();
			}
		}
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	void moveToLeftSide(Solve solve) {
		if(!solve.getEqu().getRightSide().equalStruct(ZERO)) {
			solve.getEqu().setLeftSide( sub(solve.getEqu().getLeftSide(),solve.getEqu().getRightSide()) );
			solve.getEqu().setRightSide(num(0));
		}
	}
	
	void sumMoveNonImportantToRightSide(Solve solve,Settings settings) {
		if(solve.getEqu().getLeftSide() instanceof Sum) {
			Sum leftSideSum = (Sum)solve.getEqu().getLeftSide();
			Sum newRightSide = new Sum();
			newRightSide.add(solve.getEqu().getRightSide());
			for(int i = 0;i<leftSideSum.size();i++) {
				if(!leftSideSum.get(i).contains(solve.getVar())) {
					newRightSide.add(neg(leftSideSum.get(i)));
					leftSideSum.remove(i);
					i--;
				}
			}
			solve.getEqu().setRightSide(newRightSide.simplify(settings));
			solve.getEqu().setLeftSide(leftSideSum.simplify(settings));
		}
	}
	void prodMoveNonImportantToRightSide(Solve solve,Settings settings) {
		if(solve.getEqu().getLeftSide() instanceof Prod) {
			Prod leftSideProd = (Prod)solve.getEqu().getLeftSide();
			Prod newRightSide = new Prod();
			newRightSide.add(solve.getEqu().getRightSide());
			for(int i = 0;i<leftSideProd.size();i++) {
				if(!leftSideProd.get(i).contains(solve.getVar())) {
					newRightSide.add(inv(leftSideProd.get(i)));
					leftSideProd.remove(i);
					i--;
				}
			}
			solve.getEqu().setRightSide(newRightSide.simplify(settings));
			solve.getEqu().setLeftSide(leftSideProd.simplify(settings));
		}
	}

	@Override
	public Expr copy() {
		Solve out = new Solve((Equ)getEqu().copy(),(Var)getVar().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="solve(";
		out+=getEqu().toString();
		out+=',';
		out+=getVar().toString();
		out+=')';
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Solve) {
			Solve otherCasted = (Solve)other;
			return otherCasted.getEqu().equalStruct(getEqu()) && otherCasted.getVar().equalStruct(getVar());
		}
		return false;
	}

	@Override
	public long generateHash() {
		return (getEqu().generateHash()+91634*getVar().generateHash())-2834826016327861232L;
	}

	@Override
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		Equ equ = (Equ)getEqu().replace(equs);
		Var var = (Var)getVar().replace(equs);
		
		return solve(equ,var);
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof Solve) {
			if(!checked) if(checkForMatches(other) == false) return false;
			Solve otherCasted = (Solve)other;
			boolean similarEqu = false,similarVar = false;
			if(getEqu().fastSimilarStruct(otherCasted.getEqu())) similarEqu = true;
			if(getVar().fastSimilarStruct(otherCasted.getVar())) similarVar = true;
			
			if(similarEqu && similarVar) return true;
		}
		return false;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {//newton's method
		FloatExpr guess = floatExpr(1);
		Expr expr = sub(getEqu().getLeftSide(),getEqu().getRightSide());
		expr = sub(getVar(),div(expr,diff(expr,getVar())));
		ExprList varDefs2 = (ExprList) varDefs.copy();
		varDefs2.add(equ(getVar(),guess));
		
		for(int i = 0;i<16;i++) guess.value = expr.convertToFloat(varDefs2);
		
		return guess.value;
	}

}
