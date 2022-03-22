package cas.programming;

import cas.Expr;
import cas.QuickMath;
import cas.CasInfo;
import cas.SimpleFuncs;
import cas.bool.And;
import cas.bool.Or;
import cas.lang.Ask;
import cas.matrix.Dot;
import cas.primitive.ExprList;
import cas.primitive.Prod;
import cas.primitive.Sequence;
import cas.primitive.Sum;

/*
 * the stack editor is a basic calculator interface based on rpn with an algebraic and word based extension
 * it takes commands as strings and stores an internal state which can be accessed
 */

public class StackEditor extends QuickMath {

	public static final int QUIT = -1;
	public static final int INPUT_ERROR = -2;
	public static final int FINISHED = 0;
	
	public CasInfo currentCasInfo = new CasInfo();
	
	private static Expr expr = null;
	private String alerts = "";
	
	
	public void createAlert(String string){
		alerts = "alert: "+string+"\n";
	}

	private static final int TIME_LIMIT = 8000;//3 seconds until the compute thread is killed
	/*
	 * computes the result of the last item on the stack. It runs on its own thread to prevent freezing
	 */
	public void result(int index) {
		if (last() == null)
			return;
		expr = stack.get(index);
		expr.flags.simple = false;
		Thread compute = new Thread("compute") {
			@Override
			public void run() {
				Expr.RECURSION_SAFETY = 0;
				long startingInstructionCount = Expr.ruleCallCount;
				long oldTime = System.nanoTime();
				expr = expr.simplify(currentCasInfo);// Substitutes the variables and}
				long delta = System.nanoTime() - oldTime;
				createAlert("took " + delta / 1000000.0 + " ms to compute, "+(Expr.ruleCallCount-startingInstructionCount)+" intructions called");
			}
		};
		Thread stuckCheck = new Thread("stuck check") {
			
			@Override
			public void run() {
				compute.start();
				long oldTime = System.nanoTime();
				while (compute.isAlive()) {
					long delta = System.nanoTime() - oldTime;
					if(delta / 1000000.0 > TIME_LIMIT) {
						System.err.println("stopping thread: "+compute.getName()+" because took too long to compute");
						compute.interrupt();
						return;
					}
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {}
				}
			}
		};
		stuckCheck.start();
		try {
			stuckCheck.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(expr != null) stack.set(index, expr);
	}

	public Sequence stack = new Sequence();
	public Sequence stackOld = new Sequence();

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
			out+=(i + 1) + ": "+stack.get(i)+"  --->  "+stack.get(i).convertToFloat(exprList())+"\n";
		}
		out+="▙▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▟\n";
		out+=alerts;
		alerts = "";
		return out;
	}
	
	public String getAlerts() {
		String out = alerts;
		alerts = "";
		return out;
	}
	
	public void printStack() {
		System.out.print(getStackAsString());
	}

	public void clear() {
		stack.clear();
	}

	public void negate() {
		if (last() == null)
			return;
		stack.set(size() - 1, neg(last()));
	}

	public void add() {
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

	public void subtract() {
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

	public void multiply() {
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
	
	public void dot() {
		if (sLast() == null)
			return;
		if (sLast() instanceof Dot) {
			sLast().add(last());
			stack.remove(size() - 1);
		} else {
			Expr dot = dot(sLast(), last());
			stack.remove(size() - 1);
			stack.set(size() - 1, dot);
		}
	}
	
	public void and(){
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
	public void or(){
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
	public void not(){
		if (last() == null)
			return;
		stack.set(size() - 1, not(last()));
	}

	public void exponent() {
		if (sLast() == null)
			return;
		Expr pow = pow(sLast(), last());
		stack.remove(size() - 1);
		stack.set(size() - 1, pow);
	}

	public void pop() {
		if (last() == null)
			return;
		stack.remove(size() - 1);
	}

	public void divide() {
		if (sLast() == null)
			return;

		stack.set(size() - 2, div(sLast(), stack.get(size() - 1)));
		stack.remove(size() - 1);
	}
	
	public void becomes() {
		if (sLast() == null)
			return;

		stack.set(size() - 2, becomes(sLast(), stack.get(size() - 1)));
		stack.remove(size() - 1);
	}
	
	public void swap() {
		if (sLast() == null)
			return;
		Expr temp = last();
		stack.set(size() - 1, sLast());
		stack.set(size() - 2, temp);
	}

	public void equ() {
		if (sLast() == null)
			return;
		stack.set(size() - 2, equ(sLast(), last()));
		stack.remove(size() - 1);
	}
	public void equGreater() {
		if (sLast() == null)
			return;
		stack.set(size() - 2, equGreater(sLast(), last()));
		stack.remove(size() - 1);
	}
	public void equLess() {
		if (sLast() == null)
			return;
		stack.set(size() - 2, equLess(sLast(), last()));
		stack.remove(size() - 1);
	}

	public void createList() {
		stack.add(new ExprList());
	}

	public void addToList() {
		if (sLast() == null)
			return;
		sLast().add(last());
		stack.remove(size() - 1);
	}

	public void breakApart() {
		if (last() == null)
			return;
		Expr temp = last();
		stack.remove(size() - 1);
		for (int i = 0; i < temp.size(); i++)
			stack.add(temp.get(i));
	}

	public void duplicate() {
		if (last() == null)
			return;
		stack.add(last().copy());
	}

	public void roll() {
		if (last() == null)
			return;
		Expr temp = last();
		for (int i = size() - 1; i > 0; i--) {
			stack.set(i, stack.get(i - 1));
		}
		stack.set(0, temp);
	}
	
	public void addAll() {
		Sum sum = new Sum();
		
		for(int i = 0;i<size();i++) {
			sum.add(stack.get(i));
		}
		stack.clear();
		stack.add(sum);
	}
	
	public void multAll() {
		Prod prod = new Prod();
		
		for(int i = 0;i<size();i++) {
			prod.add(stack.get(i));
		}
		stack.clear();
		stack.add(prod);
	}
	
	public void undo() {
		Sequence temp = stack;
		stack = stackOld;
		stackOld = temp;
	}

	public int command(String command) {

		if (command.isEmpty()) {
			createAlert("you typed nothing");
			return INPUT_ERROR;
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
			}else if (command.equals(".")) {
				dot();
			} else if (command.equals("/")) {
				divide();
			} else if (command.equals("^")) {
				exponent();
			} else if (command.equals("r") || (command.equals("result"))) {// get result
				result(stack.size()-1);
			} else if (command.equals("p") || command.equals("pop")) {// pop element
				pop();
			} else if (command.equals("swap")) {
				swap();
			} else if (command.equals("=")) {
				equ();
			} else if (command.equals(">")) {
				equGreater();
			} else if (command.equals("<")) {
				equLess();
			} else if (command.equals("->")) {
				becomes();
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
			}else if(command.equals("addAll")) {
				addAll();
			}else if(command.equals("multAll")) {
				multAll();
			}else if (command.equals("quit") || command.equals("exit") || command.equals("close")) {
				return QUIT;
			}else if (command.equals("sort")) {
				last().sort();
			}
			else if (command.matches("[a-zA-Z]+(:)((true|false)|([0-9]+))")) {
				String[] parts = command.split(":");
				command = parts[0];
				boolean isNum = parts[1].matches("[0-9]+");
				boolean isBool = parts[1].matches("(true|false)");
				
				int val = 0;
				boolean bool = false;
				if(isNum) val = Integer.parseInt(parts[1]);
				if(isBool) bool = Boolean.parseBoolean(parts[1]);
				
				if (command.equals("dup")) {
					val--;
					if (val > -1 && val < size()) {
						stack.add(stack.get(val).copy());
					} else createAlert("invalid index");
				} else if (command.equals("del") || command.equals("pop")) {
					val--;
					if (val > -1 && val < size()) {
						stack.remove(val);
					} else createAlert("invalid index");
				} else if (command.equals("swap")) {
					val--;
					if (val > -1 && val < size()) {
						Expr temp = stack.get(val);
						stack.set(val, last());
						stack.set(size() - 1, temp);
					} else createAlert("invalid index");
				}else if (command.equals("roll")) {
					if (size()>2) {
						val = Math.floorMod(val,size());
						for(int i = 0;i<val;i++) roll();
					} else createAlert("need more elements");
				}else if(command.equals("result")) {
					val--;
					if (val > -1 && val < size()) {
						result(val);
					}else createAlert("invalid index");
				}else if(command.equals("allowAbs")) {
					currentCasInfo.setAllowAbs(bool);
					createAlert("allowAbs="+bool);
				}else if(command.equals("allowComplexNumbers")) {
					currentCasInfo.setAllowComplexNumbers(bool);
					createAlert("allowComplexNumbers="+bool);
				}else {
					System.out.println("unknown stack commans: "+command);
				}
			}else if(SimpleFuncs.isFunc(command)) {
				int numberOfParams = SimpleFuncs.getExpectectedParams(command);
				if(stack.size() >= numberOfParams) {
					Expr[] paramsList = new Expr[numberOfParams];
					int startPoint = stack.size()-numberOfParams;
					for(int i = startPoint;i<stack.size();i++) {
						paramsList[i-startPoint] = stack.get(i);
					}
					
					Expr newStackItem = SimpleFuncs.getFuncByName(command, paramsList);
					
					for(int i = stack.size()-1;i>=startPoint;i--) {
						stack.remove(i);
					}
					
					stack.add(newStackItem);
				}
			}else {
			
				long oldTime = System.nanoTime();
				Expr convertedQ = Ask.ask(command);

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
			return INPUT_ERROR;
		}
		return FINISHED;
	}
}
