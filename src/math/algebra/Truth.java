package math.algebra;

import java.math.BigInteger;
import java.util.ArrayList;

public class Truth {
	Container left = null,right = null;
	public Truth(Container left,Container right) {
		this.left = left;
		this.right = right;
	}
	public void print() {
		left.print();
		System.out.print('=');
		right.print();
	}
	public Truth copy() {
		return new Truth(left.copy(),right.copy());
	}
	public Truth simplify() {
		return new Truth(left.simplify(),right.simplify());
	}
	public boolean equalStruct(Truth other) {
		if(other == null) return false;
		return other.left.equalStruct(this.left) && other.right.equalStruct(this.right);
	}
	public ArrayList<Truth> solve(String name) {
		Truth current = null;
		{//all one side //the left side
			Sum sum = new Sum();
			sum.add(left.copy());
			Product pr = new Product();
			pr.add(right);
			pr.add(new IntC(-1));
			sum.add(pr);
			current = new Truth(sum,new IntC(0));
		}
		ArrayList<Truth> solutions = new ArrayList<Truth>();
		
		Truth oldCurrent = null;
		
		simpleiso:while(current.left instanceof Sum || current.left instanceof Product || current.left instanceof Power) {
			//
			
			current.left = current.left.simplify();
			
			if(current.equalStruct(oldCurrent)) break simpleiso;
			oldCurrent  = current.copy();
			//
			
			if(current.left instanceof Sum) {
				Sum sum = (Sum)current.left;
				Sum rightSide = new Sum();
				rightSide.add(current.right);
				
				for(int i = 0;i<sum.containers.size();i++) {
					Container c = sum.containers.get(i);
					if(!c.containsVar(name)) {
						sum.containers.remove(i);
						Product pr = new Product();
						pr.add(new IntC(-1));
						pr.add(c);
						rightSide.add(pr);
						i--;
					}
				}
				current.right = rightSide;
				current.left = sum;
			}
			
			//
			
			if(current.left instanceof Product) {
				
				Product prod = (Product)current.left;
				Product rightSide = new Product();
				rightSide.add(current.right);
				
				for(int i = 0;i<prod.containers.size();i++) {
					Container c = prod.containers.get(i);
					if(!c.containsVar(name)) {
						prod.containers.remove(i);
						rightSide.add(new Power(c,new IntC(-1)));
						i--;
					}
				}
				current.right = rightSide;
				current.left = prod;
			}
			
			if(current.left instanceof Product) {
				current.right = current.right.simplify();
				boolean rightJustZero = false;
				if(current.right instanceof IntC) {
					if(((IntC)current.right).value.equals(BigInteger.ZERO)) {
						rightJustZero = true;
						Product prLeft = (Product)current.left;
						for(Container c:prLeft.containers) {
							Truth tr = new Truth(c.copy(),new IntC(0));
							ArrayList<Truth> list = tr.solve(name);
							for(Truth t:list) {
								solutions.add(t);
							}
						}
						return solutions;
					}
				}
				if(!rightJustZero){
					Product cL = (Product)current.left;
					if(cL.containers.size() == 2) {
						//look for x term
						Container xTerm = null;
						boolean found = false;
						
						Sum otherSection = null;
						
						for(Container c:cL.containers) {
							if(c.containsVar(name)) {
								if(c instanceof Sum) {
									otherSection = (Sum)c;
								}else {
									xTerm = c;
									found = true;
								}
							}
						}
						
						if(found && otherSection != null) {
							
							Sum nonSVarSm = new Sum();
							int varPartCount = 0;
							Container sVar = null;
							
							for(Container c:otherSection.containers) {
								if(!c.containsVar(name)) {
									nonSVarSm.add(c);
								}else {
									varPartCount++;
									sVar = c;
								}
							}
							
							if(varPartCount == 1) {
								varPartCount = 0;
								Product nonSVarPr = new Product();
								Container xTerm2 = null;
								
								if(sVar instanceof Product) {
									Product sVarProd = (Product)sVar;
									for(Container c:sVarProd.containers) {
										if(!c.containsVar(name)) {
											nonSVarPr.add(c);
										}else {
											xTerm2 = c;
											varPartCount++;
										}
									}
								}else {
									xTerm2 = sVar;
									varPartCount = 1;
								}
								
								if(varPartCount == 1 && xTerm.equalStruct(xTerm2)) {
									
									//
									Sum innerSum = new Sum();
									{
										Product pr = new Product();
										pr.add(new IntC(2));
										pr.add(nonSVarPr.copy());
										pr.add(xTerm.copy());
										innerSum.add(pr);
									}
									innerSum.add(nonSVarSm.copy());
									
									Sum outerSum = new Sum();
									outerSum.add(new Power(innerSum,new IntC(2)));
									{
										Product pr = new Product();
										pr.add(new IntC(-1));
										pr.add(new Power(nonSVarSm.copy(),new IntC(2)));
										outerSum.add(pr);
									}
									Product pr = new Product();
									pr.add(outerSum);
									pr.add(new Power(new IntC(4),new IntC(-1)));
									pr.add(new Power(nonSVarPr.copy(),new IntC(-1)));
									
									current.left = pr;
									
									//
								}
								
							}
							
							
						}
						
						
					}
				}
			}
			
			
			if(current.left instanceof Power) {
				
				Power pow = (Power)current.left;
				if(pow.expo.containsVar(name) && !pow.base.containsVar(name)) {
					current.left = pow.expo;
					Product pr = new Product();
					pr.add(new Log(current.right));
					pr.add(new Power(new Log(pow.base),new IntC(-1)));
					current.right = pr;
				}else if(!pow.expo.containsVar(name) && pow.base.containsVar(name)){
					boolean even = false;
					if(pow.expo instanceof IntC) {
						IntC expoInt = (IntC)pow.expo;
						if(expoInt.value.mod(BigInteger.TWO).equals(BigInteger.ZERO)) even = true;
					}
					if(!even){
						current.left = pow.base;
						current.right = new Power(current.right,new Power(pow.expo,new IntC(-1)));
					}else {
						Truth v1 = new Truth(pow.base.copy(),new Power(current.right,new Power(pow.expo,new IntC(-1))));
						Product pr = new Product();
						pr.add(new IntC(-1));
						pr.add(new Power(current.right,new Power(pow.expo,new IntC(-1))));
						Truth v2 = new Truth(pow.base.copy(),pr);
						
						
						ArrayList<Truth> soluV1 = v1.solve(name);
						ArrayList<Truth> soluV2 = v2.solve(name);
						
						
						for(Truth t:soluV1) {
							solutions.add(t);
						}
						for(Truth t:soluV2) {
							solutions.add(t);
						}
						
						return solutions;
					}
				}
			}
			
			
			if(current.left instanceof Log) {
				Log cLLog = (Log)current.left;
				current.right = new Power(new E(),current.right);
				current.left = cLLog.container;
			}
			
		}
		//
		
		current.right = current.right.simplify();
		//
		solutions.add(current);
		return solutions;
	}
}
