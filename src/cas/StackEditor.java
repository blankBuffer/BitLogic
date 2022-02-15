package cas;

public class StackEditor extends QuickMath {

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

	void divide() {
		if (sLast() == null)
			return;

		stack.set(size() - 2, div(sLast(), stack.get(size() - 1)));
		stack.remove(size() - 1);
	}

	void swap() {
		if (sLast() == null)
			return;
		Expr temp = last();
		stack.set(size() - 1, sLast());
		stack.set(size() - 2, temp);
	}

	void equ() {
		if (sLast() == null)
			return;
		stack.set(size() - 2, equ(sLast(), last()));
		stack.remove(size() - 1);
	}
	
	void greater() {
		if (sLast() == null)
			return;
		stack.set(size() - 2, equGreater(sLast(), last()));
		stack.remove(size() - 1);
	}
	
	void less() {
		if (sLast() == null)
			return;
		stack.set(size() - 2, equLess(sLast(), last()));
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
	
	void undo() {
		ExprList temp = stack;
		stack = stackOld;
		stackOld = temp;
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
			if(command.equals("undo")){
				undo();
			}else if (command.equals("+")) {
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
			} else if (command.equals("*")) {
				multiply();
			} else if (command.equals("/")) {
				divide();
			} else if (command.equals("^")) {
				exponent();
			} else if (command.equals("r") || (command.equals("result"))) {// get result
				result();
			} else if (command.equals("p") || command.equals("pop")) {// pop element
				pop();
			} else if (command.equals("swap")) {
				swap();
			} else if (command.equals("=")) {
				equ();
			} else if (command.equals(">")) {
				greater();
			} else if (command.equals("<")) {
				less();
			} else if (command.equals("[")) {
				createList();
			} else if (command.equals("]")) {
				addToList();
			} else if (command.equals("break")) {
				breakApart();
			} else if (command.equals("clear")) {
				clear();
			}else if (command.equals("dup")) {
				duplicate();
			} else if (command.equals("roll")) {
				roll();
			} else if (command.equals("quit") || command.equals("exit") || command.equals("close")) {
				return -1;
			}else if (command.contains(":") && !command.contains(" ")) {
				String[] parts = command.split(":");
				command = parts[0];

				if (command.equals("dup")) {
					int index = Integer.parseInt(parts[1]) - 1;
					if (index > -1 && index < size()) {
						stack.add(stack.get(index).copy());
					} else
						createAlert("invalid index");
				} else if (command.equals("del") || command.equals("pop")) {
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
					} else {
						createAlert("invalid index");
					}
				}
			}else if(SimpleFuncs.isFunc(command)) {
				int numberOfParams = SimpleFuncs.getExpectectedParams(command, currentDefs);
				if(stack.size() >= numberOfParams) {
					Expr[] paramsList = new Expr[numberOfParams];
					int startPoint = stack.size()-numberOfParams;
					for(int i = startPoint;i<stack.size();i++) {
						paramsList[i-startPoint] = stack.get(i);
					}
					
					Expr newStackItem = SimpleFuncs.getFuncByName(command, currentDefs, paramsList);
					
					for(int i = stack.size()-1;i>=startPoint;i--) {
						stack.remove(i);
					}
					
					stack.add(newStackItem);
				}
			}else {
			
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