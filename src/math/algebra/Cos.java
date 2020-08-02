package math.algebra;

import java.math.BigInteger;

public class Cos extends Trig {
	Container container;

	public Cos() {
	}

	public Cos(Container container) {
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
	

	@Override
	public boolean containsVars() {
		return container.containsVars();
	}
	
	public Container unitCircle() {
		if(!constant()) return clone();
		IntC zero = new IntC(0);
		if(container.equalStruct(zero)) return new IntC(1);
		if(container instanceof Pi) return new IntC(-1);
		if(container instanceof Product) {
			
			Product prCon = (Product)container.clone();
			Sum sm = new Sum();
			sm.add(prCon);
			Product tpr = new Product();
			tpr.add(new Pi());
			tpr.add(new Power(new IntC(2),new IntC(-1)));
			sm.add(tpr);
			Container mer = sm.simplify();
			
			Sin sin = new Sin(mer);
			Container sinSimp = sin.simplify();
			if(!sinSimp.equals(sin)) {
				return sinSimp;
			}else return clone();
			
		}
		
		return clone();
	}
	
	public Container basicShift() {
		if(!(container instanceof Sum)) return clone();
		Sum smCast = (Sum)container;
		outer:for(Container c:smCast.containers) {
			Product cPr = null;
			
			if(c instanceof Product)cPr = (Product)c;
			else {
				cPr = new Product();
				cPr.add(c);
			}
			if(cPr.constant() && cPr.containsVar("π")) {
				for(Container c2:cPr.containers) {
					if(c2 instanceof IntC) {
						continue;
					}
					if(c2 instanceof Power) {
						Power c2Pow = (Power)c2;
						if(c2Pow.base instanceof IntC && c2Pow.expo instanceof IntC) {
							if(((IntC)c2Pow.expo).value.equals(BigInteger.valueOf(-1))) {
								continue;
							}
						}
					}
					if(c2 instanceof Pi) {
						continue;
					}
					continue outer;
				}
				this.convertToSin().classicPrint();
				return this.convertToSin().simplify();
				
			}
			
		}
		return clone();
	}
	
	public Container convertToSin() {
		Product pr = new Product();
		pr.add(new Pi());
		pr.add(new Power(new IntC(2),new IntC(-1)));
		Sum sm = new Sum();
		sm.add(container.clone());
		sm.add(pr);
		return new Sin(sm);
	}

	@Override
	public Container simplify() {
		
		Container current = new Cos(this.container.simplify());
		
		if (current instanceof Cos){
			Cos currentCos = (Cos)current;
			if(currentCos.container instanceof Sum) currentCos.container = ((Sum)currentCos.container).factorOut();
		}

		if (!(current instanceof Cos))
			return current;
		current = ((Cos) current).even();
		
		if(current instanceof Cos) {
			Cos currentCos = (Cos)current;
			if(currentCos.container instanceof Product) currentCos.container = ((Product)currentCos.container).seperateFraction();
			if(currentCos.container instanceof Product) currentCos.container = ((Product)currentCos.container).distribute();
		}
		
		if (!(current instanceof Cos))
			return current;
		current = ((Cos) current).basicShift();
		
		if (!(current instanceof Cos))
			return current;
		current = ((Cos) current).unitCircle();

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
