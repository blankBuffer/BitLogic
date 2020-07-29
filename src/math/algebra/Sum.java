package math.algebra;

import java.math.BigInteger;
import java.util.ArrayList;

public class Sum extends List{
	
	public Sum(ArrayList<Container> containers) {
		this.containers = containers;
	}
	public Sum() {
	}
	

	@Override
	public String toString(String modif) {
		for(int i = containers.size()-1;i>-1;i--) {
			Container temp = containers.get(i);
			
			boolean minus = false;
			
			if(temp instanceof Product) {
				Product tempProd = (Product)temp;
				for(Container c:tempProd.containers) {
					if(c instanceof IntC) {
						IntC cInt = (IntC)c;
						if(cInt.value.signum() == -1) {
							minus = !minus;
						}
						
					}
				}
			}
			if(temp instanceof IntC) {
				IntC tempInt = (IntC)temp;
				if(tempInt.value.signum() == -1) {
					minus = true;
					temp = new IntC(tempInt.value.multiply(BigInteger.valueOf(-1)));
				}
				
			}
			
			
			if(minus && temp instanceof Product) {
				temp = temp.clone();
				
				Product tempProd = (Product)temp;
				for(int j = 0;j<tempProd.containers.size();j++) {
					Container c = tempProd.containers.get(j);
					if(c instanceof IntC) {
						IntC cInt = (IntC)c;
						if(cInt.value.signum() == -1) {
							tempProd.containers.remove(j);
							BigInteger newVal = cInt.value.abs();
							if(!newVal.equals(BigInteger.ONE)){
								tempProd.containers.add(new IntC(newVal));
								j--;
							}
						}
						
					}
				}
				
			}
			
			
			if(minus) modif+="-";
			
			if(i != containers.size()-1&& !minus) modif+="+";
			
			boolean pr = false;
			if(temp instanceof Sum) pr = true;
			
			
			if(pr) modif+="(";
			modif+=temp.toString();
			if(pr) modif+=")";
			
			
			
		}
		return modif;
	}
	
	@Override
	public void classicPrint() {
		System.out.print('(');
		for(Container c:containers) {
			c.classicPrint();
			System.out.print("+");
		}
		System.out.print(')');
	}

	@Override
	public boolean equalStruct(Container other) {
		if(other instanceof Sum) return equalList(other);
		return false;
	}

	@Override
	public Container clone() {
		ArrayList<Container> listCopy = new ArrayList<Container>();
		for(Container c:this.containers) listCopy.add(c.clone());
		return new Sum(listCopy);
	}
	
	public Container addIntC() {
		Sum newSum = (Sum)this.clone();
		BigInteger num = BigInteger.ZERO;
		BigInteger den = BigInteger.ONE;
		for(int i = 0;i<newSum.containers.size();i++) {
			Container temp = newSum.containers.get(i);
			if(temp instanceof IntC) {
				IntC tempIntC = (IntC)temp;
				newSum.containers.remove(i);
				num=tempIntC.value.multiply(den).add(num);
				i--;
			}else if(temp instanceof Power) {
				Power tempPower = (Power)temp;
				if(tempPower.base instanceof IntC && tempPower.expo instanceof IntC) {
					BigInteger expo = ((IntC)tempPower.expo).value;
					if(expo.equals(BigInteger.valueOf(-1))) {
						BigInteger base = ((IntC)tempPower.base).value;
						num = num.multiply(base).add(den);
						den=base.multiply(den);
						newSum.containers.remove(i);
						i--;
						
					}
				}
			}else if(temp instanceof Product) {
				Product tempProduct = (Product)temp;
				if(tempProduct.containers.size() == 2) {
					
					IntC intc = null;
					Power power = null;
					boolean found = false;
					if(tempProduct.containers.get(0) instanceof IntC && tempProduct.containers.get(1) instanceof Power) {
						found = true;
						intc = (IntC)tempProduct.containers.get(0);
						power = (Power)tempProduct.containers.get(1);
					}else if(tempProduct.containers.get(1) instanceof IntC && tempProduct.containers.get(0) instanceof Power) {
						found = true;
						intc = (IntC)tempProduct.containers.get(1);
						power = (Power)tempProduct.containers.get(0);
					}
					if(found) {
						
						if(power.base instanceof IntC && power.expo instanceof IntC) {
							if(((IntC)power.expo).value.equals(BigInteger.valueOf(-1))) {
								BigInteger num2 = intc.value;
								BigInteger den2 = ((IntC)power.base).value;
								
								num = num2.multiply(den).add(num.multiply(den2));
								den=den2.multiply(den);
								
								newSum.containers.remove(i);
								i--;
								
							}
						}
						
					}
					
				}
				
			}
			
			
			boolean neg = (num.signum()==-1) != (den.signum()==-1);
			num = num.abs();
			den = den.abs();
			
			BigInteger gcd = num.gcd(den);
			num = num.divide(gcd);
			den = den.divide(gcd);
			
			if(neg) num=num.multiply(BigInteger.valueOf(-1));
			
			
		}
		
		if(num.equals(BigInteger.ZERO)) return newSum;
		else if(num.equals(BigInteger.valueOf(-1)) && !den.equals(BigInteger.ONE)) newSum.add(new Power(new IntC(den.multiply(BigInteger.valueOf(-1))),new IntC(-1)));
		else if(!num.equals(BigInteger.ZERO) && den.equals(BigInteger.ONE)) newSum.add(new IntC(num));
		else if(num.equals(BigInteger.ONE) && !den.equals(BigInteger.ONE)) newSum.add(new Power(new IntC(den),new IntC(-1)));
		else if(!num.equals(BigInteger.ONE) && !den.equals(BigInteger.ONE)) {
			Product frac = new Product();
			frac.add(new IntC(num));
			frac.add(new Power(new IntC(den),new IntC(-1)));
			newSum.add(frac);
		}
		
		return newSum;
	}
	
	public Container merge() {
		Sum newSum = (Sum)this.clone();
		for(int i = 0;i<newSum.containers.size();i++) {
			Container temp = newSum.containers.get(i);
			if(temp instanceof Sum) {
				Sum tempSum = (Sum)temp;
				newSum.containers.remove(i);
				for(Container c:tempSum.containers) newSum.containers.add(c);
				i--;
			}
		}
		return newSum;
	}
	
	public Container alone() {
		int length = this.containers.size();
		if(length == 1) return this.containers.get(0).clone();
		if(length == 0) return new IntC(0);
		return this.clone();
	}
	
	public Container combineProduct() {
		if(containers.size()<2) return this.clone();
		boolean justInts = true;
		for(Container c:containers) {
			if(!(c instanceof IntC)) {
				justInts = false;
				break;
			}
		}
		if(justInts) return this.clone();
		
		Sum modSum = (Sum)this.clone();
		Sum newSum = new Sum();
		for(int i = 0;i<modSum.containers.size();i++) {
			Sum sum = new Sum();
			Container temp = modSum.containers.get(i);
			Container original = temp.clone();
			if(temp instanceof Product) {
				Product tempProduct = (Product)temp;
				Product productForSum = new Product();
				for(int m = 0;m<tempProduct.containers.size();m++) {
					Container c = tempProduct.containers.get(m);
					if(c instanceof IntC) {
						productForSum.add(c);
						tempProduct.containers.remove(m);
						m--;
					}
				}
				sum.add(productForSum);
				temp = tempProduct.alone();
			}else sum.add(new IntC(1));
			boolean found = false;
			for(int j = i+1;j < modSum.containers.size();j++) {
				Container compare = modSum.containers.get(j).clone();
				if(temp.equalStruct(compare)) {
					modSum.containers.remove(j);
					sum.add(new IntC(1));
					found = true;
					j--;
				}else if(compare instanceof Product) {
					Product compareProduct = (Product)compare;
					Product constantParts = new Product();
					for(int m = 0;m<compareProduct.containers.size();m++) {
						Container c = compareProduct.containers.get(m);
						if(c instanceof IntC) {
							constantParts.add(c);
							compareProduct.containers.remove(m);
							m--;
						}
					}
					compare = compareProduct.alone();
					if(temp.equalStruct(compare)) {
						modSum.containers.remove(j);
						found = true;
						sum.add(constantParts);
						j--;
					}
					
				}
			}
			
			if(found) {
				Product newProduct = new Product();
				newProduct.add(temp);
				newProduct.add(sum);
				newSum.add(newProduct.simplify());
			}else {
				newSum.add(original);
			}
			
		}
		return newSum;
	}
	
	public Container logCompression() {
		Sum modible = (Sum)this.clone();
		
		Product pr = new Product();
		Log log = new Log(pr);
		
		int count = 0;
		
		for(int i = 0;i<modible.containers.size();i++) {
			Container c = modible.containers.get(i);
			if(c instanceof Log) {
				Log cLog = (Log)c;
				pr.add(cLog.container);
				modible.containers.remove(c);
				i--;
				count++;
			}
		}
		
		if(count == 1) return this.clone();
		
		if(pr.containers.size()>0) {
			modible.add(log);
			return modible.simplify();
		}
		
		return modible;
	}
	
	public Container combineFractions() {
		Sum modible = (Sum)this.clone();
		boolean frac = false;
		for(Container c:modible.containers) {
			if(c instanceof Product) {
				Product cProd = (Product)c;
				for(Container c2:cProd.containers) {
					if(c2 instanceof Power) {
						Power c2Power = (Power)c2;
						if(c2Power.expo instanceof IntC) {
							if(((IntC)c2Power.expo).value.equals(BigInteger.valueOf(-1))) {
								frac = true;
								break;
							}
						}
					}
				}
			}else if(c instanceof Power) {
				Power cPower = (Power)c;
				if(cPower.expo instanceof IntC) {
					if(((IntC)cPower.expo).value.equals(BigInteger.valueOf(-1))) {
						frac = true;
						break;
					}
				}
			}
		}
		if(frac) {
			Product den = new Product();
			Sum num = new Sum();
			for(Container c:modible.containers) {
				if(c instanceof Product) {
					Product cProd = (Product)c;
					Product numTemp = new Product();
					Product denTemp = new Product();
					for(Container c2:cProd.containers) {
						boolean found = false;
						if(c2 instanceof Power) {
							Power c2Power = (Power)c2;
							if(c2Power.expo instanceof IntC) {
								if( ((IntC)c2Power.expo).value.equals(BigInteger.valueOf(-1)) ) {
									found = true;
									denTemp.add(c2Power.base);
								}
							}
						}
						if(!found) numTemp.add(c2);
					}
					Sum newNum = new Sum();
					{
						Product pr = new Product();
						pr.add(num);
						pr.add(denTemp.clone());
						newNum.add(pr);
					}
					{
						Product pr = new Product();
						pr.add(numTemp.clone());
						pr.add(den.clone());
						newNum.add(pr);
					}
					den.add(denTemp.clone());
					num = newNum;
				}else if(c instanceof Power) {
					boolean inverse = false;
					Power cPower = (Power)c;
					if(cPower.expo instanceof IntC) {
						if(((IntC)cPower.expo).value.equals(BigInteger.valueOf(-1))){
							Sum newNum = new Sum();
							newNum.add(den.clone());
							{
								Product pr = new Product();
								pr.add(num);
								pr.add(cPower.base);
								newNum.add(pr);
							}
							num = newNum;
							den.add(cPower.base.clone());
							inverse = true;
						}
					}
					if(!inverse) {
						Product numPart = new Product();
						numPart.add(c);
						numPart.add(den.clone());
						num.add(numPart);
					}
				}else {
					Product numPart = new Product();
					numPart.add(c);
					numPart.add(den.clone());
					num.add(numPart);
				}
			}
			Product out = new Product();
			out.add(num);
			out.add(new Power(den,new IntC(-1)));
			//
			
			Container simpler = out.simplify();

			//
			return simpler;
		}
		return modible;
	}
	
	public Container factorOut() {
		if(containers.size()<2) return this.clone();
		if(!this.containsVars()) return this.clone();
		//
		Sum modible = (Sum)this.clone();
		Product factoredParts = new Product();
		//
		
		//
		Product firstElement = null;
		Container first = modible.containers.get(0).clone();
		if(first instanceof Product) firstElement = (Product)first;
		else {
			Product pr = new Product();
			pr.add(first);
			firstElement = pr;
		}
		//
		outer:for(Container c:firstElement.containers) {
			
			if(c instanceof IntC) {
				//all terms negative factored intC
				//apply gcd to current variable
				boolean allNeg = true;
				BigInteger gcd = ((IntC) c).value;
				for(int i = 1;i<modible.containers.size();i++) {
					Container temp = modible.containers.get(i);
					Product tempProd = null;
					if(temp instanceof Product)
						tempProd = (Product)temp;
					else {
						tempProd = new Product();
						tempProd.add(temp);
					}
					boolean foundNum = false;
					for(Container c2:tempProd.containers ) {
						if(c2 instanceof IntC) {
							foundNum = true;
							if(((IntC)c2).value.signum() == 1)
								allNeg = false;
							gcd = gcd.gcd(((IntC)c2).value);
							continue;
						}
					}
					if(!foundNum || (gcd.equals(BigInteger.valueOf(1))&&!allNeg))
						continue outer;
				}
				if(allNeg)gcd = gcd.multiply(BigInteger.valueOf(-1));
				factoredParts.add(new IntC(gcd));
				for(Container temp:modible.containers) {
					Product prTemp = null;
					if(temp instanceof Product) prTemp = (Product)temp;
					else {
						Product pr = new Product();
						pr.add(temp);
						prTemp = pr;
					}
					for(Container c2:prTemp.containers) {
						if(c2 instanceof IntC)
							((IntC)c2).value = ((IntC)c2).value.divide(gcd);
					}
				}
				
			}else {
				
				Power cPow = null;
				
				boolean neg = false;
				
				if(c instanceof Power) {
					Power temp = (Power)c;;
					if(temp.expo instanceof IntC)
						cPow = (Power)c.clone();
					else cPow = new Power(c.clone(),new IntC(1));
				}else cPow = new Power(c.clone(),new IntC(1));
				
				if(((IntC)cPow.expo).value.signum() == -1) 
					neg = true;
				
				BigInteger smallestExpo = ((IntC)cPow.expo).value;
				
				//the sign of all exponents should be the same
				
				for(int i = 1;i<modible.containers.size();i++) {
					Container temp = modible.containers.get(i);
					Product tempProd = null;
					if(temp instanceof Product)
						tempProd = (Product)temp;
					else {
						tempProd = new Product();
						tempProd.add(temp);
					}
					
					boolean foundSim = false;
					for(Container c2:tempProd.containers) {
						Power c2Pow = null;
						
						if(c2 instanceof Power) {
							Power tempC2Pow = (Power)c2;
							if(tempC2Pow.expo instanceof IntC ) {
								c2Pow = tempC2Pow;
							}else c2Pow = new Power(c2,new IntC(1));
						}else c2Pow = new Power(c2,new IntC(1));
						
						if(c2Pow.base.equalStruct(cPow.base)) {
							if(((IntC)c2Pow.expo).value.signum()==-1 == neg)
							foundSim = true;
							if(smallestExpo.abs().compareTo(((IntC)c2Pow.expo).value.abs()) == 1) {
								smallestExpo = ((IntC)c2Pow.expo).value;
							}
							break;
						}
					}
					if(!foundSim) continue outer;
				}
				
				cPow.expo = new IntC(smallestExpo);
				
				
				//removal
				for(int i = 0;i<modible.containers.size();i++) {
					Container temp = modible.containers.get(i);
					Product tempProd = null;
					if(temp instanceof Product)
						tempProd = (Product)temp;
					else {
						tempProd = new Product();
						tempProd.add(temp);
					}
					
					for(int j = 0;j<tempProd.containers.size();j++) {
						Container c2 = tempProd.containers.get(j);
						Power c2Pow = null;
						
						if(c2 instanceof Power) {
							Power tempC2Pow = (Power)c2;
							if(tempC2Pow.expo instanceof IntC ) {
								c2Pow = tempC2Pow;
							}else c2Pow = new Power(c2,new IntC(1));
						}else c2Pow = new Power(c2,new IntC(1));
						
						if(c2Pow.base.equalStruct(cPow.base)) {
							
							((IntC)c2Pow.expo).value = ((IntC)c2Pow.expo).value.subtract(smallestExpo);
							tempProd.containers.set(j, c2Pow);
							break;
						}
					}
					modible.containers.set(i, tempProd);
				}
				//
				factoredParts.add(cPow);
			}
			
		}
		
		Product pr = new Product();
		pr.add(modible);
		pr.add(factoredParts);

		
		if(factoredParts.containers.size()>0)
			return pr.simplify();
		else return modible;
	}
	
	public Container simplify() {
		if(showSteps) {
			System.out.println("simplifying sum");
			classicPrint();
			System.out.println();
		}
		
		Container current = null;
		Sum temp = new Sum();
		for(Container c:this.containers) {
			Container simplePart = c.simplify();
			temp.add(simplePart);
		}
		current = temp;
		
		
		if(!(current instanceof Sum)) return current;
		current = ((Sum)current).merge();//(x+y)+z -> x+y+z
		
		if(!(current instanceof Sum)) return current;
		current = ((Sum)current).logCompression();// ln(x)+ln(y) -> ln(x*y)
		
		if(!(current instanceof Sum)) return current;
		current = ((Sum)current).combineProduct();// 5*x+3*x -> 8*x also handles constants 5^(1/2)-4*5^(1/2)->-3*5^(1/2)
		
		if(!(current instanceof Sum)) return current;
		current = ((Sum)current).addIntC();//adds numeric fractions and integers together
		
		if(!(current instanceof Sum)) return current;
		current = ((Sum)current).combineFractions();//adds fractions with variables a/b+c/d -> (c*a+b^2)/b/c
		
		if(!(current instanceof Sum)) return current;
		current = ((Sum)current).factorOut();//a*x^2+b*x^2 ->x^2*(a+b) does it only with variables
		
		if(!(current instanceof Sum)) return current;
		current = ((Sum)current).alone();//empty sum
		
		return current;
	}
	@Override
	public double approx() {
		double sum = 0;
		for(Container c:containers)sum+=c.approx();
		return sum;
	}
}
