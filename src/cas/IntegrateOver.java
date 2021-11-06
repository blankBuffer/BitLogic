package cas;

public class IntegrateOver extends Expr {

	private static final long serialVersionUID = -418375860392765107L;
	
	Expr getMin() {
		return get(0);
	}
	Expr getMax() {
		return get(1);
	}
	Expr getExpr() {
		return get(2);
	}
	Var getVar() {
		return (Var)get(3);
	}
	
	public IntegrateOver(Expr min,Expr max,Expr e,Var v){
		add(min);
		add(max);
		add(e);
		add(v);
	}

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		toBeSimplified.simplifyChildren(settings);
		
		Expr test = integrate(((IntegrateOver)toBeSimplified).getExpr(),((IntegrateOver)toBeSimplified).getVar()).simplify(settings);
		if(!test.containsType(Integrate.class)) {
			toBeSimplified = sub(test.replace(equ(  ((IntegrateOver)toBeSimplified).getVar() , ((IntegrateOver)toBeSimplified).getMax() )),test.replace(equ(  ((IntegrateOver)toBeSimplified).getVar() , ((IntegrateOver)toBeSimplified).getMin() ))).simplify(settings);
		}
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}

	@Override
	public Expr copy() {
		IntegrateOver out = new IntegrateOver(getMin().copy(),getMax().copy(),getExpr().copy(),(Var)getVar().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="integrateOver(";
		out+=getMin().toString();
		out+=',';
		out+=getMax().toString();
		out+=',';
		out+=getExpr().toString();
		out+=',';
		out+=getVar().toString();
		out+=')';
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof IntegrateOver) {
			for(int i = 0;i<size();i++) {
				if(!get(i).equalStruct(other.get(i))) return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public long generateHash() {
		return getMin().generateHash()*2837+getMax().generateHash()*57123+getExpr().generateHash()*27651+getVar().generateHash()*2763;
	}

	@Override
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		Expr min = getMin().replace(equs);
		Expr max = getMax().replace(equs);
		Expr mainPart = getExpr().replace(equs);
		Var v = (Var)getVar().replace(equs);
		return integrateOver(min,max,mainPart,v);
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {//using simpson's rule
		int n = 32;//should be an even number
		ComplexFloat sum = new ComplexFloat(0,0);
		ComplexFloat min = getMin().convertToFloat(varDefs),max = getMax().convertToFloat(varDefs);
		ComplexFloat step = ComplexFloat.div( ComplexFloat.sub(max, min),new ComplexFloat(n,0));
		
		Equ vDef = equ(getVar(),floatExpr(min));
		ExprList varDefs2 = (ExprList) varDefs.copy();
		
		for(int i = 0;i < varDefs2.size();i++) {
			Equ temp = (Equ)varDefs2.get(i);
			Var v = (Var)temp.getLeftSide();
			if(v.equalStruct(getVar())) {
				varDefs2.remove(i);
				break;
			}
		}
		varDefs2.add(vDef);
		
		sum = ComplexFloat.add(sum, getExpr().convertToFloat(varDefs2));
		((FloatExpr)vDef.getRightSide()).value = ComplexFloat.add(((FloatExpr)vDef.getRightSide()).value, step);
		
		for(int i = 1;i<n;i++) {
			sum=ComplexFloat.add(sum,  ComplexFloat.mult( new ComplexFloat(((i%2)*2+2),0) ,getExpr().convertToFloat(varDefs2)) );
			((FloatExpr)vDef.getRightSide()).value = ComplexFloat.add(((FloatExpr)vDef.getRightSide()).value, step);
		}
		
		sum=ComplexFloat.add(sum, getExpr().convertToFloat(varDefs2));
		
		return ComplexFloat.mult(sum,ComplexFloat.mult(step, new ComplexFloat(1.0/3.0,0)));
		
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(!checked) if(checkForMatches(other) == false) return false;
		
		if(other instanceof IntegrateOver) {
			for(int i = 0;i<size();i++) {
				if(!get(i).fastSimilarStruct(other.get(i))) return false;
			}
			return true;
		}
		return false;
	}

}
