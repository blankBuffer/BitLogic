package cas;

public class Sum extends Expr{
	
	private static final long serialVersionUID = 2026808885890783719L;
	
	static Equ pythagOneMinusSinSqr = (Equ)createExpr("1-sin(x)^2=cos(x)^2");
	static Equ pythagOneMinusCosSqr = (Equ)createExpr("1-cos(x)^2=sin(x)^2");
	static Equ pythagOnePlusTanSqr = (Equ)createExpr("1+tan(x)^2=cos(x)^-2");
	
	public Sum() {
		commutative = true;
	}
	
	@Override
	public Expr simplify(Settings settings) {
		
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		toBeSimplified.simplifyChildren(settings);//simplify all the sub expressions
		
		distrSubProds((Sum)toBeSimplified,settings);
		
		pythagIden((Sum)toBeSimplified,settings);//Variants of sin(x)^2+cos(x)^2
		
		sumContainsSum((Sum)toBeSimplified);//sums contains a sum
		
		trigExpandElements((Sum)toBeSimplified,settings);//expands double angle to allow things to cancel out
		
		addLogs((Sum)toBeSimplified,settings);
		
		addIntegersAndFractions((Sum)toBeSimplified);//1+2 = 3
		
		addLikeTerms((Sum)toBeSimplified,settings);//x+x = 2*x
		
		addIntegersAndFractions((Sum)toBeSimplified);//1+2 = 3
		
		toBeSimplified = alone((Sum)toBeSimplified);//alone sum is 0
		
		if(toBeSimplified instanceof Sum) toBeSimplified = toBeSimplified.modifyFromExample(pythagOneMinusSinSqr, settings);
		
		if(toBeSimplified instanceof Sum) toBeSimplified = toBeSimplified.modifyFromExample(pythagOneMinusCosSqr, settings);
		
		if(toBeSimplified instanceof Sum) toBeSimplified = toBeSimplified.modifyFromExample(pythagOnePlusTanSqr, settings);
		
		toBeSimplified.flags.simple = true;
		
		return toBeSimplified;
	}
	
	static void trigExpandElements(Sum sum,Settings settings){//expands double angle to allow things to cancel out
		if(sum.containsType(Sin.class)){
			for(int i = 0;i < sum.size();i++){
				sum.set(i, trigExpand(sum.get(i),settings));
			}
		}
	}
	
	static Expr sinsqr = createExpr("sin(x)^2"),cossqr = createExpr("cos(x)^2");
	
	static void pythagIden(Sum sum,Settings settings) {//sin(x)^2+cos(x)^2 = 1 and a*sin(x)^2+a*cos(x)^2=a
		if(!sum.containsType(Sin.class)) return;
		
		outer:for(int i = 0;i<sum.size();i++) {
			Expr current = sum.get(i);
			if(!(current.containsType(Sin.class) || current.containsType(Cos.class))) continue;
			if(sinsqr.fastSimilarStruct(current)) {
				Expr var = current.get(0).get(0);
				for(int j = 0;j<sum.size();j++) {
					if(j==i) continue;
					Expr other = sum.get(j); 
					if(cossqr.fastSimilarStruct(other)) {
						Expr otherVar = other.get(0).get(0);
						
						if(otherVar.equalStruct(var)) {
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
					if(sinsqr.fastSimilarStruct(current.get(j))) {
						index = j;
						type = "sin";
						break;
					}else if(cossqr.fastSimilarStruct(current.get(j))) {
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
								if(type == "cos" && sinsqr.fastSimilarStruct(other.get(k))) {
									index = k;
									break;
								}else if(type == "sin" && cossqr.fastSimilarStruct(other.get(k))) {
									index = k;
									break;
								}
							}
							if(index != -1) {
								Expr otherVar = other.get(index).get().get();
								Expr otherCoef = other.copy();
								otherCoef.remove(index);
								
								if(var.equalStruct(otherVar) && coef.equalStruct(otherCoef)) {
									sum.set(i, coef.simplify(settings));
									sum.remove(j);
									continue outer;
								}
							}
							
						}
					}
					
				}
				
			}
			
		}
	}
	
	static void addLogs(Sum sum, Settings settings) {
		IndexSet indexSet = new IndexSet();
		IndexSet indexOfProdWithLog = new IndexSet();
		
		for(int i = 0;i < sum.size();i++) {
			if(sum.get(i) instanceof Log) indexSet.ints.add(i);
			else if(sum.get(i) instanceof Prod) {
				Prod innerProd = (Prod)sum.get(i);
				int innerLogCount = 0;
				for(int j = 0;j<innerProd.size();j++) {
					if(innerProd.get(j) instanceof Log) {
						innerLogCount++;
					}
				}
				if(innerLogCount == 1) {
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
					if(!(prod.get(j) instanceof Log)) {
						nonLog.add(prod.get(j));
						prod.remove(j);
						j--;
					}
				}
				Expr log = prod.get(0);
				
				Expr newInnerPow = pow(log.get(),nonLog).simplify(settings);
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
			sum.add(ln(innerProd).simplify(settings));
		}
		
		
		
	}
	
	static void distrSubProds(Sum sum,Settings settings) {//x+y*(z+w) -> x+y*z+y*w
		for(int i = 0;i<sum.size();i++) {
			if(sum.get(i) instanceof Prod) {
				sum.set(i,  distr(sum.get(i)).simplify(settings));
			}
		}
	}
	
	static void sumContainsSum(Sum sum) {
		for(int i = 0;i<sum.size();i++) {
			Expr current = sum.get(i);
			if(current instanceof Sum) {
				for(int j = 0;j<current.size();j++) sum.add(current.get(j));
				sum.remove(i);//delete from list to remove duplicates
				i--;//shift back after deletion
			}
		}
	}
	
	static void addLikeTerms(Sum sum,Settings settings) {//x+x -> 2*x
		for(int i = 0;i<sum.size();i++) {
			Expr current = sum.get(i).copy();//make sure its copy as we don't want to modify the real object
			Num coef = num(1);//coefficient
			
			if(current instanceof Prod) {//if its a product
				for(int j = 0;j<current.size();j++) {//look at each part of product
					Expr partOfProd = current.get(j);
					if(partOfProd instanceof Num) {//if its a number
						coef = ((Num)partOfProd);
						current.remove(j);
						current = current.simplify(settings);//simplify so that it does not stay a product if it becomes alone
						break;
					}
				}
			}
			
			boolean foundSame = false;
			for(int j = i+1;j < sum.size();j++) {//the i+1 is more efficient than 0 
				
				Expr toComp = sum.get(j).copy();//expression to compare to
				Num toCompCoef = num(1);
				
				if(toComp instanceof Prod) {
					for(int k = 0;k<toComp.size();k++) {//look at each part of product
						Expr partOfProd = toComp.get(k);
						if(partOfProd instanceof Num) {//if its a number
							toCompCoef = ((Num)partOfProd);
							toComp.remove(k);
							toComp = toComp.simplify(settings);//simplify so that it does not stay a product if it becomes alone
							break;
						}
					}
				}
				
				if(current.equalStruct(toComp)) {
					sum.remove(j);
					j--;
					foundSame = true;
					coef = (Num)sum(coef,toCompCoef).simplify(settings);//must be num
				}
				
			}
			if(foundSame) {
				if(current instanceof Prod) {//if its a product still just add the coefficient
					current.add(coef);
				}else {//if not just make a new product
					current = prod(current,coef);
				}
				current = current.simplify(settings);//this has to be done in case of 0*x must become zero
				sum.set(i, current);//replace the sum element with the new combine like term version
			}
			
		}
	}
	
	static void addIntegersAndFractions(Sum sum) {
		
		Num total = Num.ZERO;
		Expr totalFrac = null;
		
		for (int i = 0;i<sum.size();i++) {
			if(sum.get(i) instanceof Num) {
				Num temp = (Num)sum.get(i);
				total = total.addNum(temp);
				sum.remove(i);
				i--;
			}else if(sum.get(i) instanceof Div && ((Div)sum.get(i)).isNumerical()) {
				if(totalFrac == null) {
					totalFrac = sum.get(i);
				}else {
					totalFrac = Div.addFracs((Div)totalFrac, ((Div)sum.get(i)));
				}
				sum.remove(i);
				i--;
			}
		}
		
		if(totalFrac != null) {
			totalFrac = Div.addFracs((Div)totalFrac, div(total,num(1)));
			totalFrac = totalFrac.simplify(Settings.normal);
			sum.add(totalFrac);
		}else {
			if(!total.equalStruct(Num.ZERO)) {
				sum.add(total);
			}
		}
		
	}
	
	static Expr alone(Sum sum) {
		if(sum.size() == 1) {//if a sum is only one element 
			return sum.get(0);
		}else if(sum.size() == 0) {//if the sum is empty return 0
			return num(0);
		}
		return sum;
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
					if(numCatsed.realValue.signum()==-1) useNothing = true;
				}else if(next instanceof Prod){
					Num numCasted = null;
					for(int j = 0;j<next.size();j++) {
						if(next.get(j) instanceof Num) {
							numCasted = (Num)next.get(j);
							break;
						}
					}
					if(numCasted != null) {
						if(numCasted.realValue.signum()==-1) useNothing = true;
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
	public ComplexFloat convertToFloat(ExprList varDefs) {
		ComplexFloat total = new ComplexFloat(0,0);
		for(int i = 0;i<size();i++) total =ComplexFloat.add(total, get(i).convertToFloat(varDefs));
		return total;
	}

}
