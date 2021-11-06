package cas;

public class Diff extends Expr{
	
	private static final long serialVersionUID = -4094192362094389130L;
	
	static Equ baseCase = (Equ)createExpr("diff(x,x)=1");
	static Equ logCase = (Equ)createExpr("diff(ln(a),x)=diff(a,x)/a");
	static Equ powCase = (Equ)createExpr("diff(a^b,x)=a^b*diff(ln(a)*b,x)");
	static Equ sinCase = (Equ)createExpr("diff(sin(a),x)=cos(a)*diff(a,x)");
	static Equ cosCase = (Equ)createExpr("diff(cos(a),x)=-sin(a)*diff(a,x)");
	static Equ tanCase = (Equ)createExpr("diff(tan(a),x)=(cos(a)^-2)*diff(a,x)");
	static Equ atanCase = (Equ)createExpr("diff(atan(a),x)=diff(a,x)/(a^2+1)");
	static Equ divCase = (Equ)createExpr("diff(a/b,x)=(diff(a,x)*b-a*diff(b,x))/(b^2)");
	
	public Diff(Expr e,Var v){
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
		if(!get().contains(getVar())) toBeSimplified = num(0);
		if(toBeSimplified instanceof Diff) toBeSimplified = extractOutConstants((Diff)toBeSimplified,settings);
		if(toBeSimplified instanceof Diff) {
			Expr get = toBeSimplified.get();
			
			if(get instanceof Var) toBeSimplified = toBeSimplified.modifyFromExample(baseCase,settings);
			else if(get instanceof Log) toBeSimplified = toBeSimplified.modifyFromExample(logCase,settings);
			else if(get instanceof Power) toBeSimplified = toBeSimplified.modifyFromExample(powCase,settings);
			else if(get instanceof Sin) toBeSimplified = toBeSimplified.modifyFromExample(sinCase,settings);
			else if(get instanceof Cos) toBeSimplified = toBeSimplified.modifyFromExample(cosCase,settings);
			else if(get instanceof Tan) toBeSimplified = toBeSimplified.modifyFromExample(tanCase,settings);
			else if(get instanceof Atan) toBeSimplified = toBeSimplified.modifyFromExample(atanCase,settings);
			else if(get instanceof Div) toBeSimplified = toBeSimplified.modifyFromExample(divCase,settings);
			else if(get instanceof Sum) {//derivative of sum becomes sum of derivatives
				for(int i = 0;i<get.size();i++) get.set(i, diff(get.get(i),(Var)(getVar().copy())));
				toBeSimplified = get;
				toBeSimplified = toBeSimplified.simplify(settings);
			}else if(get instanceof Prod) {//diff(a*b*c,x) -> diff(a,x)*b*c+a*diff(b,x)*c+a*b*diff(c,x)
				Expr repl = new Sum();
				for(int i = 0;i<get.size();i++) {
					Prod p = (Prod)get.copy();
					p.set(i, diff(p.get(i),(Var)getVar().copy()));
					repl.add(p);
				}
				toBeSimplified = repl.simplify(settings);
			}
		}
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	Expr extractOutConstants(Diff d,Settings settings) {
		if(d.get() instanceof Prod) {
			Prod innerProd = (Prod)d.get();
			
			Expr repl = new Prod();
			Prod replInnerProd = new Prod();
			boolean foundNonVar = false;
			
			for(int i = 0;i<innerProd.size();i++) {
				if(!innerProd.get(i).contains(d.getVar())) {
					repl.add(innerProd.get(i));
					foundNonVar = true;
				}
				else replInnerProd.add(innerProd.get(i));
			}
			
			if(!foundNonVar) return d;
			
			repl.add(diff(replInnerProd,d.getVar()));
			repl = repl.simplify(settings);
			return repl;
		}
		return d;
	}

	@Override
	public Expr copy() {
		Diff out = new Diff(get().copy(),(Var)getVar().copy());
		out.flags.set(flags);
		return out;
	}

	@Override
	public String toString() {
		String out = "";
		out+="diff(";
		out+=get().toString();
		out+=",";
		out+=getVar().toString();
		out+=")";
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof Diff) return get().equalStruct(other.get()) && getVar().equalStruct( ((Diff)other).getVar() );
		return false;
	}

	@Override
	boolean similarStruct(Expr other,boolean checked) {
		
		if(!checked) if(checkForMatches(other) == false) return false;
		
		if(other instanceof Diff) {
			return get().fastSimilarStruct(other.get()) && getVar().fastSimilarStruct( ((Diff)other).getVar());
		}
		return false;
	}

	@Override
	public long generateHash() {
		return (get().generateHash()+71938*getVar().generateHash())-2971540562596037251L;
	}

	@Override
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
		}
		Expr mainPart = get().replace(equs);
		Var v = (Var)getVar().replace(equs);
		return diff(mainPart,v);
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		ComplexFloat delta = new ComplexFloat(1.0/(double)Integer.MAX_VALUE,1.0/(double)Integer.MAX_VALUE);
		ComplexFloat y0 = get().convertToFloat(varDefs);
		
		ExprList varDefs2 = (ExprList) varDefs.copy();
		
		for(int i = 0;i < varDefs2.size();i++) {
			Equ temp = (Equ)varDefs2.get(i);
			Var v = (Var)temp.getLeftSide();
			if(v.equalStruct(getVar())) {
				((FloatExpr)temp.getRightSide()).value.set(ComplexFloat.add(((FloatExpr)temp.getRightSide()).value, delta));
				break;
			}
		}
		ComplexFloat y1 = get().convertToFloat(varDefs2);
		
		return ComplexFloat.div((ComplexFloat.sub(y1, y0)),delta);
	}

	
}
