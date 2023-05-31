package cas.programming;

import cas.Cas;
import cas.base.CasInfo;
import cas.base.Expr;
import cas.base.Func;
import cas.base.FunctionsLoader;
import cas.lang.Ask;

/*
 * the stack editor is a basic calculator interface based on rpn with an algebraic and word based extension
 * it takes commands as strings and stores an internal state which can be accessed
 */

public class StackEditor extends Cas {

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
		expr = stackSequence.get(index);
		expr.flags.simple = false;
		Thread compute = new Thread("compute") {
			@Override
			public void run() {
				Expr.RECURSION_SAFETY = 0;
				long oldTime = System.nanoTime();
				expr = expr.simplify(currentCasInfo);// Substitutes the variables and}
				long delta = System.nanoTime() - oldTime;
				createAlert("took " + delta / 1000000.0 + " ms to compute");
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
		if(expr != null) stackSequence.set(index, expr);
	}

	public Func stackSequence = sequence(),stackOld = sequence();

	public int size() {
		return stackSequence.size();
	}

	public Expr last() {
		if (size() < 1) {
			createAlert("the stack is empty");
			return null;
		}
		return stackSequence.get(stackSequence.size()-1);
	}

	public Expr sLast() {
		if (size() < 2) {
			createAlert("need more elements");
			return null;
		}
		return stackSequence.get(size() - 2);
	}
	
	public String colorize(String in){
		String out = "";
		
		
		
		return out;
	}

	public String getStackAsString(){
		String out = "";
		out+="STACK\n";
		String[] lines = new String[size()];
		for (int i = 0; i < size(); i++) lines[i] = (i + 1) + ": "+stackSequence.get(i)+"  --->  "+stackSequence.get(i).convertToFloat(exprSet()).toString(8);
		int longestLine = 0;
		for (int i = 0; i < size(); i++) longestLine = Math.max(longestLine, lines[i].length());
		String upperBar = "",lowerBar = "";
		for (int i = 0; i < longestLine;i++){
			upperBar+="▀";
			lowerBar+="▄";
		}
		
		out+="▛"+upperBar+"▜\n";
		for (int i = 0; i < size(); i++) {
			//lines[i] = colorize(lines[i]);
			out+=lines[i]+"\n";
		}
		out+="▙"+lowerBar+"▟\n";
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
		stackSequence.clear();
	}

	public void negate() {
		if (last() == null)
			return;
		stackSequence.set(size() - 1, neg(last()));
	}

	public void add() {
		if (sLast() == null)
			return;
		if (sLast().isType("sum")) {
			sLast().add(last());
			stackSequence.remove(size() - 1);
		} else {
			Expr sum = sum(sLast(), last());
			stackSequence.remove(size() - 1);
			stackSequence.set(size() - 1, sum);
		}
	}

	public void subtract() {
		if (sLast() == null)
			return;
		if (sLast().isType("sum")) {
			sLast().add(neg(last()));
			stackSequence.remove(size() - 1);
		} else {
			Expr sub = sub(sLast(), last());
			stackSequence.remove(size() - 1);
			stackSequence.set(size() - 1, sub);
		}
	}

	public void multiply() {
		if (sLast() == null)
			return;
		if (sLast().isType("prod")) {
			sLast().add(last());
			stackSequence.remove(size() - 1);
		} else {
			Expr prod = prod(sLast(), last());
			stackSequence.remove(size() - 1);
			stackSequence.set(size() - 1, prod);
		}
	}
	
	public void dot() {
		if (sLast() == null)
			return;
		if (sLast().isType("dot")) {
			sLast().add(last());
			stackSequence.remove(size() - 1);
		} else {
			Expr dot = dot(sLast(), last());
			stackSequence.remove(size() - 1);
			stackSequence.set(size() - 1, dot);
		}
	}
	
	public void and(){
		if (sLast() == null)
			return;
		if (sLast().isType("and")) {
			sLast().add(last());
			stackSequence.remove(size() - 1);
		} else {
			Expr and = and(sLast(), last());
			stackSequence.remove(size() - 1);
			stackSequence.set(size() - 1, and);
		}
	}
	public void or(){
		if (sLast() == null)
			return;
		if (sLast().isType("or")) {
			sLast().add(last());
			stackSequence.remove(size() - 1);
		} else {
			Expr or = or(sLast(), last());
			stackSequence.remove(size() - 1);
			stackSequence.set(size() - 1, or);
		}
	}
	public void not(){
		if (last() == null)
			return;
		stackSequence.set(size() - 1, not(last()));
	}

	public void exponent() {
		if (sLast() == null)
			return;
		Expr pow = power(sLast(), last());
		stackSequence.remove(size() - 1);
		stackSequence.set(size() - 1, pow);
	}

	public void pop() {
		if (last() == null)
			return;
		stackSequence.remove(size() - 1);
	}

	public void divide() {
		if (sLast() == null)
			return;

		stackSequence.set(size() - 2, div(sLast(), stackSequence.get(size() - 1)));
		stackSequence.remove(size() - 1);
	}
	
	public void becomes() {
		if (sLast() == null)
			return;

		stackSequence.set(size() - 2, becomes(sLast(), stackSequence.get(size() - 1)));
		stackSequence.remove(size() - 1);
	}
	
	public void swap() {
		if (sLast() == null)
			return;
		Expr temp = last();
		stackSequence.set(size() - 1, sLast());
		stackSequence.set(size() - 2, temp);
	}

	public void equ() {
		if (sLast() == null)
			return;
		stackSequence.set(size() - 2, equ(sLast(), last()));
		stackSequence.remove(size() - 1);
	}
	public void equGreater() {
		if (sLast() == null)
			return;
		stackSequence.set(size() - 2, equGreater(sLast(), last()));
		stackSequence.remove(size() - 1);
	}
	public void equLess() {
		if (sLast() == null)
			return;
		stackSequence.set(size() - 2, equLess(sLast(), last()));
		stackSequence.remove(size() - 1);
	}

	public void createSet() {
		stackSequence.add(exprSet());
	}

	public void addToSet() {
		if (sLast() == null)
			return;
		sLast().add(last());
		stackSequence.remove(size() - 1);
	}
	
	public void createSequence() {
		stackSequence.add(sequence());
	}

	public void addToSequence() {
		if (sLast() == null)
			return;
		sLast().add(last());
		stackSequence.remove(size() - 1);
	}

	public void breakApart() {
		if (last() == null)
			return;
		Expr temp = last();
		stackSequence.remove(size() - 1);
		for (int i = 0; i < temp.size(); i++)
			stackSequence.add(temp.get(i));
	}

	public void duplicate() {
		if (last() == null)
			return;
		stackSequence.add(last().copy());
	}

	public void roll() {
		if (last() == null)
			return;
		Expr temp = last();
		for (int i = size() - 1; i > 0; i--) {
			stackSequence.set(i, stackSequence.get(i - 1));
		}
		stackSequence.set(0, temp);
	}
	
	public void addAll() {
		Func sum = sum();
		
		for(int i = 0;i<size();i++) {
			sum.add(stackSequence.get(i));
		}
		stackSequence.clear();
		stackSequence.add(sum);
	}
	
	public void multAll() {
		Func prod = prod();
		
		for(int i = 0;i<size();i++) {
			prod.add(stackSequence.get(i));
		}
		stackSequence.clear();
		stackSequence.add(prod);
	}
	
	public void undo() {
		Func tempSequence = stackSequence;
		stackSequence = stackOld;
		stackOld = tempSequence;
	}
	
	public void define() {
		if (sLast() == null)
			return;

		stackSequence.set(size() - 2, define(sLast(), stackSequence.get(size() - 1)));
		stackSequence.remove(size() - 1);
	}

	public int command(String command) {

		if (command.isEmpty()) {
			createAlert("you typed nothing");
			return INPUT_ERROR;
		}
		try {
			if (!command.equals("undo")) {
				stackOld = (Func) stackSequence.copy();
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
				result(stackSequence.size()-1);
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
			} else if (command.equals(":=")) {
				define();
			} else if (command.equals("{")) {
				createSet();
			} else if (command.equals("}")) {
				addToSet();
			}else if (command.equals("[")) {
				createSequence();
			} else if (command.equals("]")) {
				addToSequence();
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
						stackSequence.add(stackSequence.get(val).copy());
					} else createAlert("invalid index");
				} else if (command.equals("del") || command.equals("pop")) {
					val--;
					if (val > -1 && val < size()) {
						stackSequence.remove(val);
					} else createAlert("invalid index");
				} else if (command.equals("swap")) {
					val--;
					if (val > -1 && val < size()) {
						Expr temp = stackSequence.get(val);
						stackSequence.set(val, last());
						stackSequence.set(size() - 1, temp);
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
			}else if(FunctionsLoader.isFunc(command)) {
				int numberOfParams = FunctionsLoader.getExpectedParams(command);
				if(stackSequence.size() >= numberOfParams) {
					Expr[] paramsList = new Expr[numberOfParams];
					int startPoint = stackSequence.size()-numberOfParams;
					for(int i = startPoint;i<stackSequence.size();i++) {
						paramsList[i-startPoint] = stackSequence.get(i);
					}
					
					Expr newStackItem = FunctionsLoader.getFuncByName(command, paramsList);
					
					for(int i = stackSequence.size()-1;i>=startPoint;i--) {
						stackSequence.remove(i);
					}
					
					stackSequence.add(newStackItem);
				}
			}else {
			
				long oldTime = System.nanoTime();
				Expr convertedQ = Ask.ask(command);

				long delta = System.nanoTime() - oldTime;
				System.out.println("took " + delta / 1000000.0 + " ms to understand");

				System.out.println("meaning: " + convertedQ);
				if (convertedQ != null) {
					stackSequence.add(convertedQ);
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
