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
	public void print() {
		
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
			modible = (Product)this.copy();
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
			System.out.print("-");
		}
		
		if(modible.containers.size()>0) {
			if(modible.containers.get(modible.containers.size()-1) instanceof Power) {
				Power p = (Power)modible.containers.get(modible.containers.size()-1);
				if(p.expo instanceof IntC) {
					if( ((IntC)p.expo).value.equals(BigInteger.valueOf(-1)) ) {
						Container obj = p;
						modible.containers.remove(modible.containers.size()-1);
						modible.containers.add(0, obj);
					}
				}
				
			}
			
			if(modible.containers.get(modible.containers.size()-1) instanceof Power) {
				Power first = (Power)modible.containers.get(modible.containers.size()-1);
				if(first.expo instanceof IntC) {
					if(((IntC)first.expo).value.equals(BigInteger.valueOf(-1))) System.out.print('1');
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
			
			
			if(div) System.out.print('/');
			else if(i != modible.containers.size()-1) System.out.print('*');
			
			
			boolean pr = false;
			if(temp instanceof Sum) pr = true;
			else if(temp instanceof Product) pr = true;
			
			if(pr)System.out.print('(');
			temp.print();
			if(pr)System.out.print(')');
			
		}
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
	public Container copy() {
		ArrayList<Container> listCopy = new ArrayList<Container>();
		for(Container c:this.containers) listCopy.add(c.copy());
		return new Product(listCopy);
	}
	
	public Container multiplyIntC() {
		Product newProduct = (Product)this.copy();
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
		Product newProduct = (Product)this.copy();
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
		if(length == 1) return this.containers.get(0).copy();
		if(length == 0) return new IntC(1);
		return this.copy();
	}
	
	public Container addPowersInProduct() {
		Product oldProduct = (Product)this.copy();
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
	
	
	public Container distribute() {
		
		
		
		return this.copy();
		
	}
	
	public Container expoSameIntCBase(){
		
		Product modible = (Product)this.copy();
		
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
	
	public Container logFrac() {
		
		ArrayList<Container> num = new ArrayList<Container>();
		ArrayList<Container> den = new ArrayList<Container>();
		
		for(Container c:containers) {
			if(c instanceof Power) {
				Power cPow = (Power)c;
				if(cPow.expo instanceof IntC) {
					if(((IntC)cPow.expo).value.equals(BigInteger.valueOf(-1)) ) {
						den.add(cPow.base.copy());
					}else num.add(c.copy());
				}else num.add(c.copy());
			}else num.add(c.copy());
		}
		
		Product newProd = new Product();
		
		//try to cancel out logs
		outer:for(int i = 0;i<num.size();i++) {
			Container numObj = num.get(i);
			
			if(numObj instanceof Log) {
				
				Log numObjLog = (Log)numObj;
				
				
				Power numToPow = null;
				if(numObjLog.container instanceof IntC)
					numToPow = IntArith.toPower(((IntC)numObjLog.container).value);
				
				if(numToPow!=null) {
					for(int j = 0;j<den.size();j++) {
						Container denObj = den.get(j);
						
						if(denObj instanceof Log) {
							
							Log denObjLog = (Log)denObj;
							
							Power denToPow = null;
							if(denObjLog.container instanceof IntC) 
								denToPow = IntArith.toPower(((IntC)denObjLog.container).value);
							
							if(denToPow!=null) {
								
								if(numToPow.base.equalStruct(denToPow.base)) {
									newProd.add(numToPow.expo);
									newProd.add(new Power(denToPow.expo,new IntC(-1)));
									den.remove(j);
									
									
									continue outer;
								}
								
							}
							
						}
						
					}
				}
				
				
				
			}
			
			newProd.add(numObj);
			
		}
		for(Container c:den) {
			newProd.add(new Power(c,new IntC(-1)));
		}
		
		return newProd;
		
	}
	
	public Container divideToPower() {
		//get numerator and denominator
		
		BigInteger num = BigInteger.ONE,den = BigInteger.ONE;
		
		for(Container c:containers) {
			if(c instanceof IntC)
				num = ((IntC)c).value;
			else if(c instanceof Power) {
				Power cPow = (Power)c;
				if(cPow.base instanceof IntC && cPow.expo instanceof IntC) {
					if(((IntC)cPow.expo).value.equals(BigInteger.valueOf(-1)) )
						den = ((IntC)cPow.base).value;
				}
			}
		}
		
		//go through each element if base is intc and expo contains vars see if num or den can be divided by it

		if(!(num.equals(BigInteger.ONE) && den.equals(BigInteger.ONE))) {
			Product modible = (Product)this.copy();
			
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
								while(num.mod(base).equals(BigInteger.ZERO)) {
									num = num.divide(base);
									didSomething = true;
									count = count.add(BigInteger.ONE);
								}
							
							
								while(den.mod(base).equals(BigInteger.ZERO)) {
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
		
		return this.copy();
	}
	
	public Container fracNumSimp() {
		
		return this.copy();
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
		current = ((Product)current).merge();//(a*b)*c -> a*b*c
		
		if(!(current instanceof Product)) return current;
		current = ((Product)current).logFrac();//ln(100)/ln(10) ->2 | ln(x^y)/ln(x) -> y//needs rewrite
		
		if(!(current instanceof Product)) return current;
		current = ((Product)current).expoSameIntCBase();//2^(x)*2^(y)
		
		if(!(current instanceof Product)) return current;
		current = ((Product)current).addPowersInProduct();//x^2*x^3->x^5
		
		if(!(current instanceof Product)) return current;
		current = ((Product)current).divideToPower();//12*2^x->2^(x+2)*3
		
		if(!(current instanceof Product)) return current;
		current = ((Product)current).distribute();//5*(a+b) -> 5*a+5*b
		
		//cancel integers in fractions
		if(!(current instanceof Product)) return current;
		current = ((Product)current).multiplyIntC();//2*5->10
		//remove ones
		
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
