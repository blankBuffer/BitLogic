package ui;
import cas.*;

public class StackEditor extends cas.QuickMath {

	private static Expr expr = null;
	private String alerts = "";
	
	
	public void createAlert(String string){
		alerts = "alert: "+string+"\n";
	}

	void result() {
		if (last() == null)
			return;
		expr = last();
		Thread compute = new Thread() {
			@Override
			public void run() {
				long oldTime = System.nanoTime();
				long startingInstructionCount = Expr.ruleCallCount;
				expr = expr.replace(currentDefs.getVars()).simplify(currentSettings);// Substitutes the variables and}
				
				long delta = System.nanoTime() - oldTime;
				createAlert("took " + delta / 1000000.0 + " ms to compute, "+(Expr.ruleCallCount-startingInstructionCount)+" intructions called");
			}
		};
		compute.start();
		Thread stuckCheck = new Thread() {
			@Override
			public void run() {
				try {
					while (compute.isAlive()) {
						sleep(1000);
						if (compute.isAlive())
							System.out.println("thinking...");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		stuckCheck.start();
		try {
			compute.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(expr != null) stack.set(size() - 1, expr);
	}

	Settings currentSettings = new Settings();
	public Defs currentDefs = new Defs();

	public ExprList stack = new ExprList();
	public ExprList stackOld = new ExprList();

	public int size() {
		return stack.size();
	}

	public Expr last() {
		if (size() < 1) {
			createAlert("the stack is empty");
			return null;
		}
		return stack.get(stack.size()-1);
	}

	public Expr sLast() {
		if (size() < 2) {
			createAlert("need more elements");
			return null;
		}
		return stack.get(size() - 2);
	}

	public String getStackAsString(){
		String out = "";
		out+="STACK\n";
		out+="▛▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▜\n";
		for (int i = 0; i < size(); i++) {
			out+=(i + 1) + ": "+stack.get(i)+"\n";
		}
		out+="▙▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▟\n";
		out+=alerts;
		alerts = "";
		return out;
	}
	public void printStack() {
		System.out.print(getStackAsString());
	}

	void clear() {
		stack.clear();
	}

	void negate() {
		if (last() == null)
			return;
		stack.set(size() - 1, neg(last()));
	}

	void add() {
		if (sLast() == null)
			return;
		if (sLast() instanceof Sum) {
			sLast().add(last());
			stack.remove(size() - 1);
		} else {
			Expr sum = sum(sLast(), last());
			stack.remove(size() - 1);
			stack.set(size() - 1, sum);
		}
	}

	void subtract() {
		if (sLast() == null)
			return;
		if (sLast() instanceof Sum) {
			sLast().add(neg(last()));
			stack.remove(size() - 1);
		} else {
			Expr sub = sub(sLast(), last());
			stack.remove(size() - 1);
			stack.set(size() - 1, sub);
		}
	}

	void multiply() {
		if (sLast() == null)
			return;
		if (sLast() instanceof Prod) {
			sLast().add(last());
			stack.remove(size() - 1);
		} else {
			Expr prod = prod(sLast(), last());
			stack.remove(size() - 1);
			stack.set(size() - 1, prod);
		}
	}
	
	void and(){
		if (sLast() == null)
			return;
		if (sLast() instanceof And) {
			sLast().add(last());
			stack.remove(size() - 1);
		} else {
			Expr and = and(sLast(), last());
			stack.remove(size() - 1);
			stack.set(size() - 1, and);
		}
	}
	void or(){
		if (sLast() == null)
			return;
		if (sLast() instanceof Or) {
			sLast().add(last());
			stack.remove(size() - 1);
		} else {
			Expr or = or(sLast(), last());
			stack.remove(size() - 1);
			stack.set(size() - 1, or);
		}
	}
	void not(){
		if (last() == null)
			return;
		stack.set(size() - 1, not(last()));
	}
	
	void limit(){
		if (size() < 3)
			return;
		Limit out = new Limit(stack.get(size()-4),(Var)sLast(),last());
		for(int i = 0;i<3;i++){
			stack.remove(size()-1);
		}
		stack.add( out);
	}

	void exponent() {
		if (sLast() == null)
			return;
		Expr pow = pow(sLast(), last());
		stack.remove(size() - 1);
		stack.set(size() - 1, pow);
	}

	void pop() {
		if (last() == null)
			return;
		stack.remove(size() - 1);
	}

	void ln() {
		if (last() == null)
			return;
		stack.set(size() - 1, ln(last()));
	}

	void log() {
		if (sLast() == null)
			return;
		if(last().equals(e())){
			stack.set(size() - 1, ln(sLast()));
			stack.remove(size()-2);
		}else{
			stack.set(size() - 1, div(ln(sLast()), ln(last())));
			stack.remove(size()-2);
		}
	}

	void factor() {
		if (last() == null)
			return;
		stack.set(size() - 1, factor(last()));
	}

	void distr() {
		if (last() == null)
			return;
		stack.set(size() - 1, distr(last()));
	}

	void sqrt() {
		if (last() == null)
			return;
		stack.set(size() - 1, sqrt(last()));
	}

	void similar() {
		if (sLast() == null)
			return;
		stack.add(bool(Rule.strictSimilarStruct(sLast(),last())));
	}
	
	void fastSimilar() {
		if (sLast() == null)
			return;
		stack.add(bool(Rule.fastSimilarStruct(sLast(),last())));
	}

	void divide() {
		if (sLast() == null)
			return;

		stack.set(size() - 2, div(sLast(), stack.get(size() - 1)));
		stack.remove(size() - 1);
	}

	void inverse() {
		if (last() == null)
			return;
		stack.set(size() - 1, inv(last()));
	}

	void swap() {
		if (sLast() == null)
			return;
		Expr temp = last();
		stack.set(size() - 1, sLast());
		stack.set(size() - 2, temp);
	}

	void diff() {
		if (sLast() == null)
			return;
		if (!(last() instanceof Var)) {
			createAlert("the second element needs to be a variable");
			return;
		}
		stack.set(size() - 2, diff(sLast(), (Var) last()));
		stack.remove(size() - 1);
	}

	void integrate() {
		if (sLast() == null)
			return;
		if (!(last() instanceof Var)) {
			createAlert("the second element needs to be a variable");
			return;
		}
		stack.set(size() - 2, integrate(sLast(), (Var) last()));
		stack.remove(size() - 1);
	}

	void integrateOver() {
		if (stack.size() < 4) {
			createAlert("need more elements");
			return;
		}
		if (!(last() instanceof Var)) {
			createAlert("the last element needs to be a variable");
			return;
		}
		stack.set(size() - 4, integrateOver(stack.get(size() - 4), stack.get(size() - 3), sLast(), (Var) last()));
		for(int i = 0;i<3;i++) {
			stack.remove(stack.size()-1);
		}
	}

	void solve() {
		if (sLast() == null)
			return;
		if (!(last() instanceof Var)) {
			createAlert("the second element needs to be a variable");
			return;
		} else if (!(sLast() instanceof Equ)) {
			createAlert("the first element needs to be an equation");
			return;
		}
		stack.set(size() - 2, solve((Equ) sLast(), (Var) last()));
		stack.remove(size() - 1);
	}

	void equ() {
		if (sLast() == null)
			return;
		stack.set(size() - 2, equ(sLast(), last()));
		stack.remove(size() - 1);
	}

	void createList() {
		stack.add(new ExprList());
	}

	void addToList() {
		if (sLast() == null)
			return;
		sLast().add(last());
		stack.remove(size() - 1);
	}

	void breakApart() {
		if (last() == null)
			return;
		Expr temp = last();
		stack.remove(size() - 1);
		for (int i = 0; i < temp.size(); i++)
			stack.add(temp.get(i));
	}

	void duplicate() {
		if (last() == null)
			return;
		stack.add(last().copy());
	}

	void roll() {
		if (last() == null)
			return;
		Expr temp = last();
		for (int i = size() - 1; i > 0; i--) {
			stack.set(i, stack.get(i - 1));
		}
		stack.set(0, temp);
	}

	void sin() {
		if (last() == null)
			return;
		stack.set(size() - 1, sin(last()));
	}

	void cos() {
		if (last() == null)
			return;
		stack.set(size() - 1, cos(last()));
	}

	void tan() {
		if (last() == null)
			return;
		stack.set(size() - 1, tan(last()));
	}

	void sinh() {
		if (last() == null)
			return;
		stack.set(size() - 1, sinh(last()));
	}

	void cosh() {
		if (last() == null)
			return;
		stack.set(size() - 1, cosh(last()));
	}

	void tanh() {
		if (last() == null)
			return;
		stack.set(size() - 1, tanh(last()));
	}

	void atan() {
		if (last() == null)
			return;
		stack.set(size() - 1, atan(last()));
	}
	void asin() {
		if (last() == null)
			return;
		stack.set(size() - 1, asin(last()));
	}
	void acos() {
		if (last() == null)
			return;
		stack.set(size() - 1, acos(last()));
	}
	
	void lambertW(){
		if (last() == null)
			return;
		stack.set(size() - 1, lambertW(last()));
	}
	
	void mathML() {
		if (last() == null)
			return;
		stack.set(size() - 1, var("\""+generateMathML(last())+"\"" ) );
	}

	void approx() {
		if (sLast() == null)
			return;
		stack.set(size() - 2, approx(sLast(), (ExprList) last()));
		stack.remove(stack.size() - 1);
	}

	public int command(String command) {

		if (command.isEmpty()) {
			createAlert("you typed nothing");
			return 0;
		}
		try {
			if (!command.equals("undo")) {
				stackOld.clear();
				for (int i = 0; i < size(); i++) {
					stackOld.add(stack.get(i).copy());
				}
			}
			if (command.equals("+")) {
				add();
			} else if (command.equals("--")) {
				negate();
			} else if (command.equals("and") || command.equals("&")) {
				and();
			} else if (command.equals("or") || command.equals("|")) {
				or();
			} else if (command.equals("not") || command.equals("~")) {
				not();
			} else if (command.equals("-")) {
				subtract();
			} else if (command.equals("limit")) {
				limit();
			}else if (command.equals("*")) {
				multiply();
			} else if (command.equals("^")) {
				exponent();
			} else if (command.equals("r") || (command.equals("result"))) {// get result
				result();
			} else if (command.equals("p") || command.equals("pop")) {// pop element
				pop();
			} else if (command.equals("ln") || command.equals("log")) {
				log();
			} else if (command.equals("sqrt")) {
				sqrt();
			} else if (command.equals("similar")) {
				similar();
			}	else if (command.equals("fastSimilar")) {
				fastSimilar();
			} else if (command.equals("swap")) {
				swap();
			} else if (command.equals("inv")) {
				inverse();
			} else if (command.equals("/")) {
				divide();
			} else if (command.equals("diff")) {
				diff();
			} else if (command.equals("integrate")) {
				integrate();
			} else if (command.equals("perfect-power")) {
				stack.add(perfectPower((Num) last()));
			} else if (command.equals("prime-factor")) {
				stack.add(primeFactor((Num) last()));
			} else if (command.equals("distr")) {
				distr();
			} else if (command.equals("solve")) {
				solve();
			} else if (command.equals("=")) {
				equ();
			} else if (command.equals("[")) {
				createList();
			} else if (command.equals("]")) {
				addToList();
			} else if (command.equals("factor")) {
				factor();
			} else if (command.equals("break")) {
				breakApart();
			} else if (command.equals("save")) {
				Expr.saveExpr(sLast(), last().toString());
				stack.remove(size() - 1);
			} else if (command.equals("open")) {
				stack.set(size() - 1, Expr.openExpr(((Var) last()).name));
			} else if (command.equals("clear")) {
				clear();
			} else if (command.equals("sort")) {
				last().sort();
				stack.set(size() - 1, last());// update visuals
			}else if (command.equals("tree")) {
				stack.add(var(last().toStringTree(0)));
			}else if (command.equals("complexity")) {
				stack.add(num(last().complexity()));
			}else if (command.equals("dup")) {
				duplicate();
			} else if (command.equals("undo")) {
				stack.clear();
				for (int i = 0; i < stackOld.size(); i++) {
					stack.add(stackOld.get(i));
				}
			} else if (command.equals("roll")) {
				roll();
			} else if (command.equals("sin")) {
				sin();
			} else if (command.equals("cos")) {
				cos();
			} else if (command.equals("tan")) {
				tan();
			} else if (command.equals("sinh")) {
				sinh();
			} else if (command.equals("cosh")) {
				cosh();
			} else if (command.equals("tanh")) {
				tanh();
			} else if (command.equals("atan")) {
				atan();
			}else if (command.equals("asin")) {
				asin();
			}else if (command.equals("acos")) {
				acos();
			}else if (command.equals("lambertW")) {
				lambertW();
			}else if (command.equals("mathML")) {
				mathML();
			}else if (command.equals("quit") || command.equals("exit") || command.equals("close")) {
				return -1;
			} else if (command.equals("approx")) {
				approx();
			} else if (command.equals("integrateOver")) {
				integrateOver();
			} else if (command.equals("poly")) {
				Var v = var("x");
				partialFrac(last(), v, currentSettings);
			} else if (command.equals("hash")) {
				stack.add(num(last().hashCode()));
			} else if (command.equals("equal")) {
				stack.add(new BoolState(last().equals(sLast())));
			} else if(command.equals("exactlyEqual")){
				stack.add(new BoolState(last().exactlyEquals(sLast())));
			}
			else if (command.contains(":") && !command.contains(" ")) {
				String[] parts = command.split(":");
				command = parts[0];

				if (command.equals("dup")) {
					int index = Integer.parseInt(parts[1]) - 1;
					if (index > -1 && index < size()) {
						stack.add(stack.get(index).copy());
					} else
						createAlert("invalid index");
				} else if (command.equals("remove")) {
					int index = Integer.parseInt(parts[1]) - 1;
					if (index > -1 && index < size()) {
						stack.remove(index);
					} else
						createAlert("invalid index");
				} else if (command.equals("swap")) {
					int index = Integer.parseInt(parts[1]) - 1;
					if (index > -1 && index < size()) {
						Expr temp = stack.get(index);
						stack.set(index, last());
						stack.set(size() - 1, temp);
					} else
						createAlert("invalid index");
				}
			} else {
				long oldTime = System.nanoTime();
				Expr convertedQ = Ask.ask(command, currentDefs, currentSettings);

				long delta = System.nanoTime() - oldTime;
				System.out.println("took " + delta / 1000000.0 + " ms to understand");

				System.out.println("meaning: " + convertedQ);
				if (convertedQ != null) {
					stack.add(convertedQ);
				}

			}
		} catch (Exception e) {
			createAlert("An error has occured\nreason: " + e.getMessage());
			e.printStackTrace();
		}
		return 0;
	}
}
