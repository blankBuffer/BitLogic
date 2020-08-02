package math.algebra;

import java.math.BigInteger;

public class Sin extends Trig{

	public Sin() {
	}

	public Sin(Container container) {
		this.container = container;
	}

	@Override
	public String toString(String modif) {
		modif+="sin(";
		modif+=container.toString();
		modif+=")";
		return modif;
	}
	
	@Override
	public void classicPrint() {
		System.out.print("sin(");
		container.classicPrint();
		System.out.print(')');
	}

	@Override
	public boolean equalStruct(Container other) {
		if (other instanceof Sin) {
			Sin otherSin = (Sin) other;
			return otherSin.container.equalStruct(this.container);
		}
		return false;
	}

	@Override
	public Container clone() {
		return new Sin(container.clone());
	}

	public Container odd() {
		if (container instanceof IntC) {
			BigInteger value = ((IntC) container).value;
			if (value.signum() == -1) {
				Product out = new Product();
				out.add(new IntC(-1));
				out.add(new Sin(new IntC(value.multiply(BigInteger.valueOf(-1)))));
				return out.simplify();
			}
		} else if (container instanceof Power) {
			Power cPower = (Power) container;
			if (cPower.base instanceof IntC) {
				BigInteger value = ((IntC) cPower.base).value;
				if (value.signum() == -1) {
					Product out = new Product();
					out.add(new IntC(-1));
					out.add(new Sin(new Power(new IntC(value.abs()), cPower.expo.clone())));
					return out.simplify();
				}
			}
		} else if (container instanceof Product) {
			int sign = 1;
			Product containerProduct = (Product) container;
			Product out = new Product();
			for (Container c : containerProduct.containers) {
				if (c instanceof IntC) {
					BigInteger value = ((IntC) c).value;
					if (value.signum() == -1)
						sign *= -1;
					out.add(new IntC(value.abs()));
				} else if (c instanceof Power) {
					Power cPower = (Power) c;
					if (cPower.base instanceof IntC) {
						BigInteger value = ((IntC) cPower.base).value;
						if (value.signum() == -1)
							sign *= -1;
						out.add(new Power(new IntC(value.abs()), cPower.expo.clone()));
					} else
						out.add(c.clone());
				} else
					out.add(c.clone());
			}
			if (sign == -1) {
				Product pr = new Product();
				pr.add(new IntC(-1));
				pr.add(new Sin(out));
				return pr.simplify();
			} else
				return new Sin(out.simplify());
		}
		return this.clone();
	}
	
	public Container unitCircle() {
		if(!constant()) return clone();
		IntC zero = new IntC(0);
		if(container.equalStruct(zero)) return zero;
		if(container instanceof Pi) return zero;
		if(container instanceof Product) {
			Product prCon = (Product)container.clone();
			if(prCon.containers.size() <= 3 && prCon.containsVar("π")) {
				
				BigInteger num = BigInteger.ONE, den = BigInteger.ONE;
				for(Container c:prCon.containers) {
					if(c instanceof IntC) num = ((IntC)c).value;
					else if(c instanceof Power) {
						Power pc = (Power)c;
						if(pc.expo instanceof IntC && pc.base instanceof IntC) {
							if(((IntC)pc.expo).value.equals(BigInteger.valueOf(-1))) {
								den = ((IntC)pc.base).value;
							}
						}
						
					}else if(!(c instanceof Pi)) return clone();
				}
				//the magic
				num = num.mod(den.multiply(BigInteger.TWO));
				
				boolean flip = false;
				if(num.compareTo(den) == 1) {
					num = num.mod(den);
					flip = true;
				}
				
				Container out = null;
				if(den.equals(BigInteger.ONE)) {
					out = new IntC(0);
				}else if(den.equals(BigInteger.TWO)) {
					out = new IntC(1);
				}else if(den.equals(BigInteger.valueOf(3))) {
					Product pr = new Product();
					pr.add(new Power(new IntC(3),new Power(new IntC(2),new IntC(-1))));
					pr.add(new Power(new IntC(2),new IntC(-1)));
					out = pr;
				}else if(den.equals(BigInteger.valueOf(4))) {
					out = new Power(new IntC(2),new Power(new IntC(-2),new IntC(-1)));
				}else if(den.equals(BigInteger.valueOf(6))) {
					out = new Power(new IntC(2),new IntC(-1));
				}else {
					Product pr = new Product();
					pr.add(new IntC(num));
					pr.add(new Power(new IntC(den),new IntC(-1)));
					pr.add(new Pi());
					out = new Sin(pr.simplify());
				}
				
				if(flip) {
					Product pr = new Product();
					pr.add(new IntC(-1));
					pr.add(out);
					return pr.simplify();
				}else {
					return out;
				}
				
				
			}
		}
		
		return clone();
	}
	
	public Container basicShift() {
		
		if(!(container instanceof Sum)) return clone();
		Sum smCont = (Sum)container.clone();
		
		outer:for(int i = 0;i<smCont.containers.size();i++) {
			Container c = smCont.containers.get(i);
			if(c.constant() && c.containsVar("π")) {
				Product cPr = null;
				if(c instanceof Product)cPr = (Product)c;
				else {
					cPr = new Product();
					cPr.add(c);
				}
				BigInteger num = BigInteger.ONE,den = BigInteger.ONE;
				for(Container c2:cPr.containers) {
					if(c2 instanceof IntC) {
						num = ((IntC)c2).value;
						continue;
					}
					if(c2 instanceof Power) {
						Power c2Pow = (Power)c2;
						if(c2Pow.base instanceof IntC && c2Pow.expo instanceof IntC) {
							if(((IntC)c2Pow.expo).value.equals(BigInteger.valueOf(-1))) {
								den = ((IntC)c2Pow.base).value;
								continue;
							}
						}
					}
					if(c2 instanceof Pi) {
						continue;
					}
					continue outer;
				}
				smCont.containers.remove(i);
				//
				
				num = num.mod(den.multiply(BigInteger.TWO));
				boolean flip = false;
				if(num.compareTo(den) == 1 || num.compareTo(den) == 0) {
					flip = true;
					num = num.mod(den);
				}
				//
				
				if(num.equals(BigInteger.ONE) && den.equals(BigInteger.TWO)) {
					if(!flip) {
						return new Cos(smCont).simplify();
					}
				}
				
				Product newFrac = new Product();
				newFrac.add(new IntC(num));
				newFrac.add(new Power(new IntC(den),new IntC(-1)));
				newFrac.add(new Pi());
				Container newFracSimp = newFrac.simplify();
				if(!(newFracSimp instanceof IntC)) smCont.containers.add(newFrac.simplify());
				
				if(flip) {
					Product pr = new Product();
					pr.add(new IntC(-1));
					pr.add(new Sin(smCont.alone()));
					return pr;
				}else {
					return new Sin(smCont.alone());
				}
				
				
			}
		}
		
		return clone();
	}
	
	@Override
	public Container simplify() {
		
		Container current = new Sin(this.container.simplify());
		
		if(current instanceof Sin) {
			Sin currentSin = (Sin)current;
			if(currentSin.container instanceof Sum) currentSin.container = ((Sum)currentSin.container).factorOut();
		}
		
		if (!(current instanceof Sin))
			return current;
		current = ((Sin) current).odd();
		
		if(current instanceof Sin) {
			Sin currentSin = (Sin)current;
			if(currentSin.container instanceof Product) currentSin.container = ((Product)currentSin.container).seperateFraction();
			if(currentSin.container instanceof Product) currentSin.container = ((Product)currentSin.container).distribute();
		}
		
		if (!(current instanceof Sin))
			return current;
		current = ((Sin) current).basicShift();
		
		if (!(current instanceof Sin))
			return current;
		current = ((Sin) current).unitCircle();

		return current;
	}
	@Override
	public double approx() {
		return Math.sin(container.approx());
	}
}
