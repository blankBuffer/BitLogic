package math.algebra;

import java.math.BigInteger;

public class Sin extends Trig{

	public Sin() {
		init();
	}

	public Sin(Container container) {
		init();
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
	
	public Container unitCircle() {
		if (container.equalStruct(zero) || container instanceof Pi || container.equalStruct(twoPi))
			return new IntC(0);
		if (container.equalStruct(piOver6) || container.equalStruct(fivePiOver6))
			return new Power(new IntC(2), new IntC(-1));
		if (container.equalStruct(piOver4) || container.equalStruct(threePiOver4))
			return new Power(new IntC(2), new Power(new IntC(-2), new IntC(-1)));
		if (container.equalStruct(piOver3) || container.equalStruct(twoPiOver3)) {
			Product prod = new Product();
			prod.add(new Power(new IntC(2), new IntC(-1)));
			prod.add(new Power(new IntC(3), new Power(new IntC(2), new IntC(-1))));
			return prod;
		}
		if (container.equalStruct(piOver2))
			return new IntC(1);
		if (container.equalStruct(sevenPiOver6) || container.equalStruct(elevenPiOver6))
			return new Power(new IntC(-2), new IntC(-1));
		if (container.equalStruct(fivePiOver4) || container.equalStruct(sevenPiOver4)) {
			Product prod = new Product();
			prod.add(new Power(new IntC(2), new Power(new IntC(-2), new IntC(-1))));
			prod.add(new IntC(-1));
			return prod;
		}
		if (container.equalStruct(fourPiOver3) || container.equalStruct(fivePiOver3)) {
			Product prod = new Product();
			prod.add(new Power(new IntC(-2), new IntC(-1)));
			prod.add(new Power(new IntC(3), new Power(new IntC(2), new IntC(-1))));
			return prod;
		}
		if (container.equalStruct(threePiOver2))
			return new IntC(-1);
		return this.clone();
	}

	public Container periodicUnitCircle() {
		double approx = container.approx();
		if (Math.abs(approx) > Math.PI * 2 && container.constant() && container instanceof Product) {

			Product containerProduct = (Product) container;

			boolean hasPi = false;

			for (Container c : containerProduct.containers) {
				if (c instanceof Pi) {
					hasPi = true;
					break;
				}
			}

			if (hasPi) {
				Sum out = new Sum();
				out.add(container.clone());
				Product prod = new Product();
				int over = (int) (container.approx() / (Math.PI * 2));
				prod.add(new IntC(over));
				prod.add(new Pi());
				prod.add(new IntC(-2));
				out.add(prod);

				return new Sin(out.simplify());
			}

		}
		return this.clone();
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
					out.add(new Sin(new Power(new IntC(value.multiply(BigInteger.valueOf(-1))), cPower.expo.clone())));
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
	
	public Container sumSep() {
		
		Sin modible = (Sin)this.clone();
		
		if(modible.container instanceof Sum) {
			//try to simplify a term in the sum
			Sum containerSum = (Sum)modible.container;
			
			for(Container c:containerSum.containers) {
				if(!c.constant()) continue;
				if(!c.containsVar("π")) continue;
				if(c.containsVar("e")) continue;
				Sin sin = new Sin(c.clone());
				Container simpSin = sin.simplify();
				if(!simpSin.equalStruct(sin)) {
					
					Container simpCos = new Cos(c.clone());
					
					containerSum.containers.remove(c);
					
					Sin remainSin = new Sin(containerSum.clone());
					Cos remainCos = new Cos(containerSum.clone());
					
					Sum sm = new Sum();
					Product pr1 = new Product();
					pr1.add(remainSin);
					pr1.add(simpCos);
					Product pr2 = new Product();
					pr2.add(remainCos);
					pr2.add(simpSin);
					sm.add(pr1);
					sm.add(pr2);
					
					return sm.simplify();
					
				}
				
			}
		}
		
		return modible;
	}
	@Override
	public Container simplify() {
		
		Container current = new Sin(this.container.simplify());
		
		
		if(current instanceof Sin) {
			Sin currentSin = (Sin)current;
			if(currentSin.container instanceof Product) currentSin.container = ((Product)currentSin.container).seperateFraction();
		}
		
		if (!(current instanceof Sin))
			return current;
		current = ((Sin) current).odd();

		if (!(current instanceof Sin))
			return current;
		current = ((Sin) current).periodicUnitCircle();

		if (!(current instanceof Sin))
			return current;
		current = ((Sin) current).unitCircle();
		
		if (!(current instanceof Sin))
			return current;
		current = ((Sin) current).sumSep();

		return current;
	}
	@Override
	public double approx() {
		return Math.sin(container.approx());
	}
}
