package math.algebra;

import java.math.BigInteger;

public class Power extends Container{
	Container base,expo;
	
	public Power(Container base,Container expo) {
		this.base = base;
		this.expo = expo;
	}
	

	@Override
	public void print() {
		
		if(expo instanceof Power) {
			Power expoPower = (Power)expo;
			if(expoPower.base instanceof IntC && expoPower.expo instanceof IntC) {
				IntC expoPowerBase = (IntC)expoPower.base;
				IntC expoPowerExpo = (IntC)expoPower.expo;
				if(expoPowerExpo.value.equals(BigInteger.valueOf(-1))) {
					if(expoPowerBase.value.equals(BigInteger.TWO)) {
						System.out.print("sqrt(");
						base.print();
						System.out.print(')');
						return;
					}else if(expoPowerBase.value.equals(BigInteger.valueOf(3))) {
						System.out.print("cbrt(");
						base.print();
						System.out.print(')');
						return;
					}
				}
			}
		}
		
		boolean prBase = false;
		if(base instanceof Sum) prBase = true;
		else if(base instanceof Product) prBase = true;
		else if(base instanceof Power) prBase = true;
		else if(base instanceof IntC) {
			IntC baseI = (IntC)base;
			if(baseI.value.signum() == -1) prBase = true;
		}
		
		boolean div = false;
		
		if(expo instanceof IntC) {
			IntC expoInt = (IntC)expo;
			if(expoInt.value.equals(BigInteger.valueOf(-1))) div = true;
		}
		
		if(div) {
			System.out.print("1/");
			if(base instanceof IntC) prBase = false;
			
			if(prBase) System.out.print('(');
			base.print();
			if(prBase) System.out.print(')');
		}else if(!div) {
			
			if(prBase) System.out.print('(');
			base.print();
			if(prBase) System.out.print(')');
		
			System.out.print('^');
			
			boolean prExpo = false;
			
			if(expo instanceof Sum) prExpo = true;
			else if(expo instanceof Product) prExpo = true;
			else if(expo instanceof Power) {
				Power expoPower = (Power)expo;
				if(expoPower.expo instanceof IntC)
					if( ((IntC)expoPower.expo).value.equals(BigInteger.valueOf(-1)) ) prExpo = true;
			}
			
			if(prExpo) System.out.print('(');
			expo.print();
			if(prExpo) System.out.print(')');
		}
	}
	@Override
	public void classicPrint() {
		System.out.print('(');
		base.classicPrint();
		System.out.print(")^(");
		expo.classicPrint();
		System.out.print(')');
	}
	@Override
	public boolean containsVar(String name) {
		return base.containsVar(name) || expo.containsVar(name);
	}
	@Override
	public boolean equalStruct(Container other) {
		if(other instanceof Power) {
			Power otherPower = (Power)other;
			return otherPower.base.equalStruct(this.base) && otherPower.expo.equalStruct(this.expo);
		}
		return false;
	}

	@Override
	public Container copy() {
		return new Power(this.base.copy(),this.expo.copy());
	}

	@Override
	public boolean constant() {
		return base.constant() && expo.constant();
	}
	
	Container expoZeroOrOne() {
		if(expo instanceof IntC) {
			BigInteger v = ((IntC)expo).value;
			if(v.equals(BigInteger.ZERO)) return new IntC(1);
			if(v.equals(BigInteger.ONE)) return base.copy();
		}
		return this.copy();
	}
	
	Container baseZeroOrOne() {
		if(base instanceof IntC) {
			BigInteger v = ((IntC)base).value;
			if(v.equals(BigInteger.ZERO)) return new IntC(0);
			if(v.equals(BigInteger.ONE)) return new IntC(1);
		}
		return this.copy();
	}
	
	Container powerIntC() {
		if(!constant()) return this.copy();
		if(base instanceof IntC && expo instanceof IntC) {
			BigInteger expoVal = ((IntC)expo).value;
			if(expoVal.equals(BigInteger.valueOf(-1))) return this;
			BigInteger baseVal = ((IntC)base).value;
			
			boolean negExpo = expoVal.signum() == -1;
			if(negExpo) expoVal = expoVal.multiply(BigInteger.valueOf(-1));
			
			BigInteger res = IntArith.power(baseVal, expoVal);
			if(negExpo) return new Power(new IntC(res),new IntC(-1));
			else return new IntC(res);
			
		}else if(base instanceof IntC && expo instanceof Power) {
			
			Power expoPower = (Power)expo;
			
			if(expoPower.base instanceof IntC && expoPower.expo instanceof IntC) {
				if(((IntC)expoPower.expo).value.equals(BigInteger.valueOf(-1))) {
					
					BigInteger root = ((IntC)expoPower.base).value;
					BigInteger baseValue = ((IntC)base).value;
					
					if(!IntArith.Prime.isPrime(baseValue)) {
						Product factors = IntArith.Prime.primeFactor(baseValue);
						if(factors != null) {
							boolean modified = false;
							Product out = new Product();
							Product in = new Product();
							for(int j = 0;j<factors.containers.size();j++) {
								Container c = factors.containers.get(j);
								Power cPow = (Power)c;
								BigInteger factorBase = ((IntC)cPow.base).value;
								BigInteger factorExpo = ((IntC)cPow.expo).value;
								if(factorExpo.mod(root).equals(BigInteger.ZERO)) {
									modified = true;
									out.add(new Power(  new IntC(factorBase),new IntC(factorExpo.divide(root))));
								}else if(factorExpo.compareTo(root) == 1){
									modified = true;
									factors.add(new Power(new IntC(factorBase),new IntC(factorExpo.subtract(root))));
									out.add(new IntC(factorBase));
								}else {
									in.add(c);
								}
								
							}
							
							out.add(new Power(in,new Power(new IntC(root),new IntC(-1))));
							if(modified) {
								return out.simplify();
							}
						}
					}
					
				}
			}
			
		}else if(base instanceof IntC && expo instanceof Product) {
			
			Product expoProd = (Product)expo;
			
			if(expoProd.containers.size() == 2) {
				
				IntC intRef = null;
				Power powerRef = null;
				
				if(expoProd.containers.get(0) instanceof IntC && expoProd.containers.get(1) instanceof Power) {
					intRef = (IntC)expoProd.containers.get(0);
					powerRef = (Power)expoProd.containers.get(1);
				}else if(expoProd.containers.get(1) instanceof IntC && expoProd.containers.get(0) instanceof Power) {
					intRef = (IntC)expoProd.containers.get(1);
					powerRef = (Power)expoProd.containers.get(0);
				}
				
				if(powerRef.base instanceof IntC && powerRef.expo instanceof IntC) {
					if(((IntC)powerRef.expo).value.equals(BigInteger.valueOf(-1))) {
						
						Container pwr = new Power(base.copy(),powerRef.copy()).simplify();
						if(pwr instanceof IntC) return new Power(pwr,intRef).simplify();
						
					}
				}
				
			}
			
		}
		return this.copy();
	}
	
	public Container baseContainsPower() {
		if(!(base instanceof Power)) return this.copy();
		Power basePower = (Power)base;
		Product newExpo = new Product();
		newExpo.add(this.expo.copy());
		newExpo.add(basePower.expo.copy());
		return new Power(basePower.base,newExpo.simplify());
	}
	
	public Container distributeExpo() {
		if(!(base instanceof Product)) return this.copy();
		Product baseProduct = (Product)base;
		
		Product newProd = new Product();
		for(Container c:baseProduct.containers) {
			newProd.add(new Power(c.copy(),expo.copy()));
		}
		
		return newProd.simplify();
	}
	
	public Container expoTrick() {
		
		Product expoProductCopy = null;
		if(expo instanceof Product)
			expoProductCopy = (Product)expo.copy();
		else {
			expoProductCopy = new Product();
			expoProductCopy.add(expo.copy());
		}
		
		boolean hasLogInExpo = false;
		
		for(Container c:expoProductCopy.containers) {
			if(c instanceof Power) {
				Power cPower = (Power)c;
				if (cPower.base instanceof Log) hasLogInExpo = true;
			}else if(c instanceof Log) hasLogInExpo = true;
		}
		
		if(!hasLogInExpo) return this.copy();
		
		expoProductCopy.add(new Log(base.copy()));
		
		Container simpleExpoProductCopy = expoProductCopy.simplify();
		if(simpleExpoProductCopy.equalStruct(expoProductCopy)) return this.copy();
		return new Power(new E(),simpleExpoProductCopy);
	}
	
	public Container baseEExpoWithLog() {
		if(!(base instanceof E))return this.copy();
		if(!(expo instanceof Product || expo instanceof Log)) return this.copy();
		if(expo instanceof Product) {
			Product expoProduct = (Product)expo.copy();
			Log log = null;
			boolean found = false;
			for(int i = 0;i<expoProduct.containers.size();i++) {
				Container temp = expoProduct.containers.get(i);
				if(temp instanceof Log) {
					log = (Log)temp;
					expoProduct.containers.remove(i);
					found = true;
					break;
				}
			}
			if(!found) return this.copy();
			return new Power(log.container,expoProduct.alone());
		}else {
			return ((Log)expo).container.copy();
		}
		
	}
	
	public Container baseIntExpoProductWithFrac() {
		
		Power modible = (Power)this.copy();
		
		if(modible.expo instanceof Product && modible.base instanceof IntC) {
			Product expoProd = (Product)modible.expo;
			
			boolean moreThanInts = true;
			
			if(this.constant()) {
				moreThanInts = false;
				for(Container c:expoProd.containers) {
					boolean justANumber = false;
					if(c instanceof IntC) {
						justANumber = true;
					}else if(c instanceof Power) {
						Power cPow = (Power)c;
						if(cPow.base instanceof IntC && cPow.expo instanceof IntC) {
							justANumber = true;
						}
					}
					
					if(!justANumber) {
						moreThanInts = true;
						break;
					}
					
				}
			}
			
			if(!moreThanInts) return modible;
			
			BigInteger num = BigInteger.ONE;
			BigInteger den = BigInteger.ONE;
			
			for(int i = 0;i < expoProd.containers.size();i++) {
				Container c = expoProd.containers.get(i);
				if(c instanceof IntC) {
					num = ((IntC)c).value;
					expoProd.containers.remove(i);
					i--;
				}else if(c instanceof Power) {
					
					Power cPow = (Power)c;
					
					if(cPow.base instanceof IntC && cPow.expo instanceof IntC) {
						if( ((IntC)cPow.expo).value.equals(BigInteger.valueOf(-1))) {
							den = ((IntC)cPow.base).value;
							expoProd.containers.remove(i);
							i--;
						}
					}
					
				}
			}
			
			BigInteger base = ((IntC)modible.base).value;
			
			Product frac = new Product();
			frac.add(new IntC(num));
			frac.add(new Power(new IntC(den),new IntC(-1)));
			
			Container check = new Power(new IntC(base),frac).simplify();
			
			if(check instanceof IntC) {
				if( ((IntC)check).value.compareTo( base ) == -1) {
					return new Power(check,modible.expo).simplify();
				}else return this.copy();
			}
			
		}
		
		return modible;
	}
	
	public Container trigConvert() {
		
		BigInteger value = BigInteger.ZERO;
		
		if(expo instanceof IntC) {
			value = ((IntC)expo).value;
		}
		
		if(base instanceof Sin) {
			if(value.equals(BigInteger.TWO)) {
				Container theta = ((Sin)base).container;
				Product frac = new Product();
				frac.add(new Power(new IntC(2),new IntC(-1)));
				Sum sm = new Sum();
				sm.add(new IntC(1));
				Product pr = new Product();
				pr.add(new IntC(-1));
				Product pr2 = new Product();
				pr2.add(theta);
				pr2.add(new IntC(2));
				pr.add(new Cos(pr2));
				sm.add(pr);
				frac.add(sm);
				return frac.simplify();
			}
		}else if(base instanceof Cos) {
			if(value.equals(BigInteger.TWO)) {
				Container theta = ((Cos)base).container;
				Product frac = new Product();
				frac.add(new Power(new IntC(2),new IntC(-1)));
				Sum sm = new Sum();
				sm.add(new IntC(1));
				Product pr = new Product();
				pr.add(theta);
				pr.add(new IntC(2));
				sm.add(new Cos(pr));
				frac.add(sm);
				return frac.simplify();
			}
		}
		
		return this.copy();
	}
	@Override
	public boolean containsVars() {
		return base.containsVars() || expo.containsVars();
	}
	
	public Container baseReduction() {
		if(base instanceof IntC && this.containsVars()) {
			IntC baseInt = (IntC)base;
			Power newPow = IntArith.toPower(baseInt.value);
			Product pr = new Product();
			pr.add(newPow.expo);
			pr.add(expo.copy());
			return new Power(newPow.base,pr.simplify());
		}else return this.copy();
	}
	
	public Container desumIfSimpler() {
		if(!(expo instanceof Sum)) return this.copy();
		Product out = new Product();
		Sum expoSum = (Sum)expo.copy();
		int count = 0;
		
		for(int i = 0;i<expoSum.containers.size();i++) {
			Container temp = expoSum.containers.get(i);
			if(temp.equalStruct(new IntC(1))) continue;
			Power original = new Power(base.copy(),temp.copy());
			Container simpler = original.simplify();
			if(!original.equalStruct(simpler)) {
				out.add(simpler);
				count++;
				expoSum.containers.remove(i);
				i--;
			}
		}
		out.add(new Power(base.copy(),expoSum));
		
		if(count == 0) return this.copy();
		
		return out.simplify();
	}
	
	@Override
	public Container simplify() {
		
		if(showSteps) {
			System.out.println("simplifying power");
			classicPrint();
			System.out.println();
		}
		
		Container current = new Power(this.base.simplify(),this.expo.simplify());
		
		
		if(!(current instanceof Power)) return current;
		current = ((Power)current).baseContainsPower();//(a^b)^c -> a^(b*c)
		
		if(!(current instanceof Power)) return current;
		current = ((Power)current).expoTrick();//a^b -> e^(ln(a)*b) allows logs to cancel out 
		
		if(!(current instanceof Power)) return current;
		current = ((Power)current).distributeExpo();//(x*y)^z -> x^z*y^z
		
		if(!(current instanceof Power)) return current;
		current = ((Power)current).baseEExpoWithLog();//e^(ln(a)*b) -> a^b reverses expo trick
		
		if(!(current instanceof Power)) return current;
		current = ((Power)current).desumIfSimpler();
		
		if(!(current instanceof Power)) return current;
		current = ((Power)current).baseIntExpoProductWithFrac();//4^(x/2) -> 2^x
		
		if(!(current instanceof Power)) return current;
		current = ((Power)current).baseReduction();//4^x -> 2^(2*x)
		
		if(!(current instanceof Power)) return current;
		current = ((Power)current).expoZeroOrOne();//x^0 -> 1 x^1 -> x
		
		if(!(current instanceof Power)) return current;
		current = ((Power)current).baseZeroOrOne();//0^x -> 0 1^x -> 1
		
		if(!(current instanceof Power)) return current;
		current = ((Power)current).trigConvert();//sin(x)^2 -> (1-cos(2*x))/2
		
		if(!(current instanceof Power)) return current;
		current = ((Power)current).powerIntC();//2^3 -> 8 also 8^(1/3) -> 2
		
		return current;
	}
	@Override
	public double approx() {
		return Math.pow(base.approx(), expo.approx());
	}
	
}
