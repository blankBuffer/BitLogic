package math.algebra;

import java.math.BigInteger;
import java.util.ArrayList;

public class Product extends List{
	
	public Product(ArrayList<Container> containers) {
		this.containers = containers;
	}
	public Product() {
	}

	@Override
	public String toString(String modif) {
		
		boolean minus = false;
		
		Product modible = this;
		
		
		for(int i = 0;i<modible.containers.size();i++) {
			Container temp = modible.containers.get(i);
			if(temp instanceof IntC) {
				IntC tempInt = (IntC)temp;
				if(tempInt.value.equals(BigInteger.valueOf(-1))) {
					minus = !minus;
				}
			}
		}
		
		if(minus) {
			modible = (Product)this.clone();
			for(int i = 0;i<modible.containers.size();i++) {
				Container temp = modible.containers.get(i);
				if(temp instanceof IntC) {
					IntC tempInt = (IntC)temp;
					if(tempInt.value.equals(BigInteger.valueOf(-1))) {
						modible.containers.remove(i);
						i--;
					}
				}
			}
			modif+="-";
		}
		
		if(modible.containers.size()>0) {
			
			int indexToMoveToBack = -1;
			for(int i = 0;i<modible.containers.size();i++) {
				//find non fraction and put it in the last element in modible
				Container temp = modible.containers.get(i);
				if(temp instanceof Power) {
					Power tempPow = (Power)temp;
					if(tempPow.expo instanceof IntC) {
						IntC tempPowExpo = (IntC)tempPow.expo;
						if(tempPowExpo.value.equals(BigInteger.valueOf(-1))) {
							continue;
						}else {
							indexToMoveToBack = i;
							break;
						}
					}else {
						indexToMoveToBack = i;
						break;
					}
				}else {
					indexToMoveToBack = i;
					break;
				}
			}
			
			if(indexToMoveToBack != -1) {
				Container mem = modible.containers.get(indexToMoveToBack);
				modible.containers.remove(indexToMoveToBack);
				modible.containers.add(mem);
			}
			
			if(modible.containers.get(modible.containers.size()-1) instanceof Power) {
				Power first = (Power)modible.containers.get(modible.containers.size()-1);
				if(first.expo instanceof IntC) {
					if(((IntC)first.expo).value.equals(BigInteger.valueOf(-1))) modif+="1";
				}
			}
		}
		
		for(int i = modible.containers.size()-1;i>-1;i--) {
			
			Container temp = modible.containers.get(i);
			
			boolean div = false;
			if(temp instanceof Power) {
				Power tempPower = (Power)temp;
				if(tempPower.expo instanceof IntC) {
					if( ((IntC)tempPower.expo).value.equals(BigInteger.valueOf(-1)) ) {
						div = true;
						temp = tempPower.base;
					}
				}
			}
			
			
			if(div) modif+="/";
			else if(i != modible.containers.size()-1) modif+="*";
			
			
			boolean pr = false;
			if(temp instanceof Sum) pr = true;
			else if(temp instanceof Product) pr = true;
			
			if(pr)modif+="(";
			modif+=temp.toString();
			if(pr)modif+=")";
			
		}
		return modif;
	}
	@Override
	public void classicPrint() {
		System.out.print('(');
		for(Container c:containers) {
			c.classicPrint();
			System.out.print("*");
		}
		System.out.print(')');
	}

	@Override
	public boolean equalStruct(Container other) {
		if(other instanceof Product) return equalList(other);
		return false;
	}

	@Override
	public Container clone() {
		ArrayList<Container> listCopy = new ArrayList<Container>();
		for(Container c:this.containers) listCopy.add(c.clone());
		return new Product(listCopy);
	}
	
	public Container multiplyIntC() {
		Product newProduct = (Product)this.clone();
		BigInteger prod = BigInteger.ONE;
		BigInteger inverseProd = BigInteger.ONE;
		for(int i = 0;i<newProduct.containers.size();i++) {
			Container temp = newProduct.containers.get(i);
			if(temp instanceof IntC) {
				IntC tempIntC = (IntC)temp;
				newProduct.containers.remove(i);
				prod = prod.multiply(tempIntC.value);
				i--;
			}else if(temp instanceof Power) {
				Power tempPower = (Power)temp;
				if(tempPower.base instanceof IntC && tempPower.expo instanceof IntC) {
					IntC tempPowerExpo = (IntC)tempPower.expo;
					if(tempPowerExpo.value.equals(BigInteger.valueOf(-1))) {
						IntC tempPowerBase = (IntC)tempPower.base;
						newProduct.containers.remove(i);
						inverseProd=inverseProd.multiply(tempPowerBase.value);
						i--;
					}
				}
			}
		}
		Container outObj = newProduct;
		if(prod.equals(BigInteger.ZERO)) return new IntC(0);
		if(prod.equals(BigInteger.valueOf(-1)) && !inverseProd.equals(BigInteger.ONE)) {
			newProduct.add(new Power(new IntC(inverseProd.multiply(BigInteger.valueOf(-1))),new IntC(-1)));
			return outObj;
		}
		if(!prod.equals(BigInteger.ONE) && !inverseProd.equals(BigInteger.ONE)) {
			Container frac = IntArith.simpleFrac(prod, inverseProd);
			newProduct.add(frac);
			outObj = newProduct.merge();
			return outObj;
		}
		if(!prod.equals(BigInteger.ONE)) newProduct.add(new IntC(prod));
		if(!inverseProd.equals(BigInteger.ONE)) newProduct.add(new Power(new IntC(inverseProd),new IntC(-1)));
		return outObj;
	}
	
	public Container merge() {
		Product newProduct = (Product)this.clone();
		for(int i = 0;i<newProduct.containers.size();i++) {
			Container temp = newProduct.containers.get(i);
			if(temp instanceof Product) {
				Product tempProduct = (Product)temp;
				newProduct.containers.remove(i);
				for(Container c:tempProduct.containers) newProduct.containers.add(c);
				i--;
			}
		}
		return newProduct;
	}
	
	public Container alone() {
		int length = this.containers.size();
		if(length == 1) return this.containers.get(0).clone();
		if(length == 0) return new IntC(1);
		return this.clone();
	}
	
	public Container addPowersInProduct() {
		Product oldProduct = (Product)this.clone();
		Product newProd = new Product();
		for(int i = 0;i<oldProduct.containers.size();i++) {
			Sum expoSum = new Sum();
			Container currentElement = oldProduct.containers.get(i);
			Container original = currentElement;
			
			if(!(currentElement instanceof Power))
				currentElement = new Power(currentElement,new IntC(1));
			
			Power currentElementPower = (Power)currentElement;
			currentElement = currentElementPower.base;
			expoSum.add(currentElementPower.expo);
			boolean found = false;
			for(int j = i+1;j<oldProduct.containers.size();j++) {
				Container currentCompare = oldProduct.containers.get(j);
				if(!(currentCompare instanceof Power)) currentCompare = new Power(currentCompare,new IntC(1));
				Power currentComparePower = (Power)currentCompare;
				if(currentComparePower.base.equalStruct(currentElement)) {
					expoSum.add(currentComparePower.expo);
					oldProduct.containers.remove(j);
					found = true;
					j--;
				}
			}
			if(found) {
				Container simpler = new Power(currentElement,expoSum).simplify();
				if(simpler instanceof IntC) {
					if(((IntC)simpler).value.equals(BigInteger.ONE)) continue;
				}
			
				newProd.add(simpler);
			}else newProd.add(original);
		}
		return newProd;
	}
	
	public Container expoSameIntCBase(){
		
		Product modible = (Product)this.clone();
		
		Product out = new Product();
		
		for(int i = 0;i < modible.containers.size();i++) {
			Container c = modible.containers.get(i);
			
			if(c instanceof Power) {
				Power cPow = (Power)c;
				if(cPow.base instanceof IntC) {
					BigInteger val = ((IntC)cPow.base).value;
					Container compare = cPow.expo;
					
					for(int j = i+1;j<modible.containers.size();j++) {
						Container temp = modible.containers.get(j);
						
						if(temp instanceof Power) {
							Power tempPower = (Power)temp;
							
							if(tempPower.base instanceof IntC) {
								BigInteger tempVal = ((IntC)tempPower.base).value;
								if(tempPower.expo.equalStruct(compare)) {
									modible.containers.remove(j);
									val=val.multiply(tempVal);
									j--;
								}
								
							}
							
						}
						
						
					}
					
					out.add(new Power(new IntC(val),compare).simplify());
					
				}else out.add(c);
			}else out.add(c);
			
			
		}
		return out;
	}
	
	public Container divideToPower() {
		//get numerator and denominator
		
		BigInteger num = BigInteger.ONE,den = BigInteger.ONE;
		
		for(Container c:containers) {
			if(c instanceof IntC)
				num = num.multiply(((IntC)c).value);
			else if(c instanceof Power) {
				Power cPow = (Power)c;
				if(cPow.base instanceof IntC && cPow.expo instanceof IntC) {
					if(((IntC)cPow.expo).value.equals(BigInteger.valueOf(-1)) )
						den = den.multiply(((IntC)cPow.base).value);
				}
			}
		}
		
		//go through each element if base is intc and expo contains vars see if num or den can be divided by it

		if(!(num.equals(BigInteger.ONE) && den.equals(BigInteger.ONE))) {
			Product modible = (Product)this.clone();
			
			for(int i = 0;i<modible.containers.size();i++) {
				Container temp = modible.containers.get(i);
				
				if(temp instanceof IntC) {
					modible.containers.remove(i);
					i--;
					continue;
				}else if(temp instanceof Power) {
					Power tempPow = (Power)temp;
					if(!(tempPow.expo instanceof IntC)) {
						
						if(tempPow.base instanceof IntC) {
							BigInteger base = ((IntC)tempPow.base).value;
							
							boolean didSomething = false;
							BigInteger count = BigInteger.ZERO;
							if(base.signum() == 1) {
								
								
								while(num.mod(base).equals(BigInteger.ZERO) && !num.equals(BigInteger.ZERO)) {
									num = num.divide(base);
									didSomething = true;
									count = count.add(BigInteger.ONE);
								}
							
							
								while(den.mod(base).equals(BigInteger.ZERO) && !den.equals(BigInteger.ZERO)) {
									den = den.divide(base);
									didSomething = true;
									count = count.add(BigInteger.valueOf(-1));
								}
								
							}
							if(didSomething) {
								Sum sm = new Sum();
								sm.add(tempPow.expo);
								sm.add(new IntC(count));
								tempPow.expo = sm.simplify();
							}
							
						}
						
					}else {
						if(tempPow.expo instanceof IntC) {
							if(((IntC)tempPow.expo).value.equals(BigInteger.valueOf(-1))&&tempPow.base instanceof IntC) {
								modible.containers.remove(i);
								i--;
								continue;
							}
						}
					}
					
				}
				
			}
			if(!num.equals(BigInteger.ONE))
				modible.add(new IntC(num));
			if(!den.equals(BigInteger.ONE))
				modible.add(new Power(new IntC(den),new IntC(-1)));
			return modible;
		}
		
		return this.clone();
	}
	
	public Container distribute() {
		//check for a sum
		//if(this.containsVars()) return this.clone();
		
		boolean foundAtLeastOne = false;
		for(Container c:this.containers) {
			if(c instanceof Sum) {
				foundAtLeastOne = true;
				break;
			}
		}
		if(!foundAtLeastOne) return this.clone();
		
			
		Product modible = (Product)this.clone();
		Sum sum = null;
		
		for(Container c:modible.containers) {
			if(c instanceof Sum) {
				sum = (Sum)c;
			}
		}
		
		modible.containers.remove(sum);
		
		
		boolean nevermind = false;
		for(Container c:modible.containers) {
			if(c instanceof Power) {
				Power cPow = (Power)c;
				if(cPow.expo instanceof IntC) {
					IntC cPowInt = (IntC)cPow.expo;
					if(cPowInt.value.equals(BigInteger.valueOf(-1))) {
						nevermind = true;
						break;
					}
				}
			}
		}
		if(nevermind) {
			modible.add(sum);
			return modible;
		}
		
		
		for(int i = 0;i<sum.containers.size();i++) {
			Product temp = null;
			if(sum.containers.get(i) instanceof Product) {
				temp = (Product)sum.containers.get(i);
			}else {
				temp = new Product();
				temp.add(sum.containers.get(i));
			}
			
			temp.add(modible.clone());
			sum.containers.set(i, temp);
		}
		return sum.simplify();
		
	}
	
	public Container seperateFraction() {
		//check if it has a sum as numerator and rest is denominator
		
		Product modible = (Product)clone();
		int count = 0;
		
		Sum sm = null;
		for(Container c:modible.containers) {
			if(c instanceof Sum) {
				sm = (Sum)c;
				count++;
			}else if(c instanceof Power){
				Power cPow = (Power)c;
				if((cPow.expo instanceof IntC)) {
					if(!((IntC)cPow.expo).value.equals(BigInteger.valueOf(-1))) return modible;
				}else return modible;
			}else return modible;
		}
		if(count>1 || count == 0) return modible;
		
		modible.containers.remove(sm);
		//distribute denominator to each element
		//simplify every object in the sum
		//return sum
		for(int i = 0;i<sm.containers.size();i++) {
			Container c = sm.containers.get(i);
			Product pr = null;
			if(c instanceof Product) {
				pr = (Product)c;
			}else {
				pr = new Product();
				pr.add(c);
			}
			pr.add(modible.clone());
			
			Container replace = pr.seperateFraction();
			replace = replace.simplify();
			
			if(replace instanceof Product) replace = ((Product)replace).seperateFraction();
			sm.containers.set(i, replace);
		}
		
		return sm.merge();
	}
	
	public Container factorSums() {
		Product modible = (Product)clone();
		for(int i = 0;i<modible.containers.size();i++) {
			Container c = modible.containers.get(i);
			if(c instanceof Sum) {
				Container repl = ((Sum)c).factorOut();
				modible.containers.set(i,repl);
				
			}
		}
		return modible;
	}
	
	@Override
	public Container simplify() {
		
		if(showSteps) {
			System.out.println("simplifying product");
			classicPrint();
			System.out.println();
		}
		
		Container current = null;
		Product temp = new Product();
		for(Container c:this.containers) {
			Container simplePart = c.simplify();
			temp.add(simplePart);
		}
		current = temp;
		
		if(!(current instanceof Product)) return current;
		current = ((Product)current).factorSums();
		
		if(!(current instanceof Product)) return current;
		current = ((Product)current).merge();//(a*b)*c -> a*b*c
		
		if(!(current instanceof Product)) return current;
		current = ((Product)current).expoSameIntCBase();//2^(x)*2^(y)
		
		if(!(current instanceof Product)) return current;
		current = ((Product)current).addPowersInProduct();//x^2*x^3->x^5
		
		if(!(current instanceof Product)) return current;
		current = ((Product)current).divideToPower();//12*2^x->2^(x+2)*3 also multiples constants
		
		if(!(current instanceof Product)) return current;
		current = ((Product)current).multiplyIntC();//changes (-1*(3)^(-1)*) -> (-3)^(-1). removes useless ones
		
		if(!(current instanceof Product)) return current;
		current = ((Product)current).alone();//empty product
		
		return current;
	}
	@Override
	public double approx() {
		double prod = 1;
		for(Container c:containers)prod*=c.approx();
		return prod;
	}
}
