package math.algebra;

public class Cos extends Trig {
	Container container;

	public Cos() {
		init();
	}

	public Cos(Container container) {
		init();
		this.container = container;
	}

	@Override
	public String toString(String modif) {
		modif+="cos(";
		modif+=container.toString();
		modif+=")";
		return modif;
	}
	
	@Override
	public void classicPrint() {
		System.out.print("cos(");
		container.classicPrint();
		System.out.print(')');
	}

	@Override
	public boolean equalStruct(Container other) {
		if (other instanceof Cos) {
			Cos otherCos = (Cos) other;
			return otherCos.container.equalStruct(this.container);
		}
		return false;
	}

	@Override
	public Container clone() {
		return new Cos(container.clone());
	}

	@Override
	public boolean constant() {
		return container.constant();
	}

	public Container unitCircle() {
		if (container.equalStruct(zero) || container.equalStruct(twoPi))
			return new IntC(1);
		if (container instanceof Pi)
			return new IntC(-1);
		if (container.equalStruct(piOver2) || container.equalStruct(threePiOver2))
			return new IntC(0);
		if (container.equalStruct(piOver4) || container.equalStruct(sevenPiOver4))
			return new Power(new IntC(2), new Power(new IntC(-2), new IntC(-1)));
		if (container.equalStruct(piOver3) || container.equalStruct(fivePiOver3))
			return new Power(new IntC(2), new IntC(-1));
		if (container.equalStruct(piOver6) || container.equalStruct(elevenPiOver6)) {
			Product prod = new Product();
			prod.add(new Power(new IntC(2), new IntC(-1)));
			prod.add(new Power(new IntC(3), new Power(new IntC(2), new IntC(-1))));
			return prod;
		}
		if (container.equalStruct(fivePiOver6) || container.equalStruct(sevenPiOver6)) {
			Product prod = new Product();
			prod.add(new Power(new IntC(-2), new IntC(-1)));
			prod.add(new Power(new IntC(3), new Power(new IntC(2), new IntC(-1))));
			return prod;
		}
		if (container.equalStruct(threePiOver4) || container.equalStruct(fivePiOver4)) {
			Product prod = new Product();
			prod.add(new Power(new IntC(2), new Power(new IntC(-2), new IntC(-1))));
			prod.add(new IntC(-1));
			return prod;
		}
		if (container.equalStruct(twoPiOver3) || container.equalStruct(fourPiOver3))
			return new Power(new IntC(-2), new IntC(-1));
		return this.clone();
	}

	public Container even() {
		if (container instanceof Product) {
			Product containerProduct = (Product) container;
			Product out = new Product();
			for (Container c : containerProduct.containers) {
				if (c instanceof IntC)
					out.add(new IntC(((IntC) c).value.abs()));
				else if (c instanceof Power) {
					Power cPower = (Power) c;
					if (cPower.base instanceof IntC)
						out.add(new Power(new IntC(((IntC) cPower.base).value.abs()), cPower.expo.clone()));
					else
						out.add(c.clone());
				} else
					out.add(c.clone());
			}
			return new Cos(out.simplify());
		} else if (container instanceof Power) {
			Power cPower = (Power) container;
			Product out = new Product();
			if (cPower.base instanceof IntC)
				out.add(new Power(new IntC(((IntC) cPower.base).value.abs()), cPower.expo.clone()));
			else
				out.add(cPower.clone());
			return new Cos(out.simplify());
		} else if (container instanceof IntC) {
			return new Cos(new IntC(((IntC) container).value.abs()));
		} else
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

				return new Cos(out.simplify());
			}

		}
		return this.clone();
	}
	
public Container sumSep() {
		
		Cos modible = (Cos)this.clone();
		
		if(modible.container instanceof Sum) {
			//try to simplify a term in the sum
			Sum containerSum = (Sum)modible.container;
			
			for(Container c:containerSum.containers) {
				if(!c.constant()) continue;
				if(!c.containsVar("π")) continue;
				if(c.containsVar("e")) continue;
				Cos cos = new Cos(c.clone());
				Container simpCos = cos.simplify();
				if(!simpCos.equalStruct(cos)) {
					
					Container simpSin = new Sin(c.clone());
					
					containerSum.containers.remove(c);
					
					Sin remainSin = new Sin(containerSum.clone());
					Cos remainCos = new Cos(containerSum.clone());
					
					Sum sm = new Sum();
					Product pr1 = new Product();
					pr1.add(remainCos);
					pr1.add(simpCos);
					Product pr2 = new Product();
					pr2.add(new IntC(-1));
					pr2.add(remainSin);
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
	public boolean containsVars() {
		return container.containsVars();
	}

	@Override
	public Container simplify() {
		
		Container current = new Cos(this.container.simplify());
		
		if(current instanceof Cos) {
			Cos currentCos = (Cos)current;
			if(currentCos.container instanceof Product) currentCos.container = ((Product)currentCos.container).seperateFraction();
		}

		if (!(current instanceof Cos))
			return current;
		current = ((Cos) current).even();

		if (!(current instanceof Cos))
			return current;
		current = ((Cos) current).periodicUnitCircle();

		if (!(current instanceof Cos))
			return current;
		current = ((Cos) current).unitCircle();

		if (!(current instanceof Cos))
			return current;
		current = ((Cos) current).sumSep();
		
		return current;
	}
	@Override
	public boolean containsVar(String name) {
		return container.containsVar(name);
	}

	@Override
	public double approx() {
		return Math.cos(container.approx());
	}

}
