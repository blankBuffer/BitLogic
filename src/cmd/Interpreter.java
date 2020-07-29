package cmd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import math.algebra.*;
import math.booleanAlgebra.*;

public class Interpreter {
	
	static Container ans = null;
	
	public static void interpret(){
		IntArith.Prime.init();
		ArrayList<String> inputHistory = new ArrayList<String>();
		while(true) {
			try {
				System.out.println(":::::::::::::::::::::::::::::");
				
				String line = BasicConstructor.in.nextLine();
				line = line.replaceAll(" ", "");
				inputHistory.add(line);
				if(inputHistory.size()>16) {
					inputHistory.remove(0);
				}
				
				String[] parts  = line.split(":");

				String command = parts[0].toLowerCase();
				if(command.equals("quit")||command.equals("exit")||command.equals("close")||command.equals("done")||command.equals("stop")) System.exit(0);
				else if(command.equals("bool")) {
					BoolContainer c = stringToBoolContainer(parts[1]);
					long startTime = System.nanoTime();
					BoolContainer bsimp = c.simplify();
					long endTime = System.nanoTime();
					System.out.println("calculation time: "+(endTime-startTime)/1000000.0+" ms");
					bsimp.print();
					System.out.println();
				}
				else if(command.equals("help")) {
					if(parts.length == 1) {
						System.out.println("This is calculator program that does not just handle numerical");
						System.out.println("calculations. It can do algebraic simplification and variable asignment");
						System.out.println("the format of commands is (command name):(text for command) ");
						System.out.println("list of commands:");
						System.out.println("help, bool ,prime , exit, list_vars, reset, classic, classic_bool, solve");
						System.out.println("type \"help:(command name)\" for details on command");
					}else {
						parts[1] = parts[1].toLowerCase();
						if(parts[1].equals("help")) System.out.println("you think you're clever!");
						else if(parts[1].equals("bool")) {
							System.out.println("simplifies boolean statements to a reduced SOP form. | or, & and, ! not");
							System.out.println("Example:\nbool:!x|x&y\nIt will reduce this to a simpler form");
						}
						else if(parts[1].equals("prime")) System.out.println("tells whether that number is prime");
						else if(parts[1].equals("exit")) System.out.println("exits the program");
						else if(parts[1].equals("list_vars")) System.out.println("shows all the variables made");
						else if(parts[1].equals("reset")) System.out.println("deletes all definitions");
						else if(parts[1].equals("classic")) System.out.println("runs the classic mode");
						else if(parts[1].equals("classic_bool")) System.out.println("runs the classic bool mode");
						else if(parts[1].equals("solve")) {
							System.out.println("attempts to solve an equation");
							System.out.println("Example:\nsolve:y=x^2,x\nthis will solve for x");
						}
					}
				}else if(command.equals("prime")) {
					BigInteger value = new BigInteger(parts[1]);
					System.out.println(IntArith.Prime.isPrime(value));
				}else if(command.equals("list_vars")) {
					for(Var v:Var.definedVars) {
						if(v.container==null) {
							System.out.print(v.name+": undefined");
						}else {
							System.out.print(v.name+"=");
							System.out.println(v.container);
						}
						System.out.println();
					}
				}else if(command.equals("reset")) {
					Var.definedVars.clear();
					System.out.println("done");
				}else if(command.equals("classic")) {
					BasicConstructor.basicProg();
				}else if(command.equals("classic_bool")) {
					BasicConstructor.boolBasicProg();
				}else if(command.equals("delete")) {
					Var.deleteVar(parts[1]);
				}else if(command.equals("solve")) {
					Container[] conts = new Container[2];
					String[] param = parts[1].split(",");
					String[] sides = param[0].split("=");
					for(int i = 0;i<2;i++) conts[i] = stringToContainer(sides[i]);
					Truth eqn = new Truth(conts[0],conts[1]);
					long startTime = System.nanoTime();
					ArrayList<Truth> sols = eqn.solve(param[1]);
					long endTime = System.nanoTime();
					System.out.println("calculation time: "+(endTime-startTime)/1000000.0+" ms");
					for(Truth t:sols) {
						t.print();
						System.out.println();
					}
					
				}else if(command.equals("tutorial")) {
					System.out.println("to add 5 and 3 type\n5+3\n");
					System.out.println("to multiply 5 and 3 type\n5*3\n");
					System.out.println("to simplify x*x to x^2 type\nx*x\n");
					System.out.println("to find sin(pi/3) on unit circle type\nsin(pi/3)\n");
					System.out.println("to asign x to 3 type\nx=3\n");
				}
				else {
					if(command.equals("")) {
						System.out.println("no command given. Type help for commands");
					}else if(command.contains("=")) {
						String[] def = command.split("=");
						new Var(def[0],stringToContainer(def[1]));
					}else {
						Container c = stringToContainer(command);
						//c.classicPrint();
						if(c.constant()) System.out.println("precalc aprox: "+c.approx());
						long startTime = System.nanoTime();
						Container csimp = c.simplify();
						long endTime = System.nanoTime();
						System.out.println("calculation time: "+(endTime-startTime)/1000000.0+" ms");
						ans = csimp;
						System.out.println("simple: "+csimp);
						if(csimp.constant()) {
							System.out.println("afercalc aprox: "+csimp.approx());
						}
					}
				}
			}catch(Exception | StackOverflowError e) {
				e.printStackTrace();
				FileOutputStream fos;
				try {
					Path currentRelativePath = Paths.get("");
					fos = new FileOutputStream(new File(currentRelativePath.toAbsolutePath().toString()+"/_log.txt"));
					PrintStream ps = new PrintStream(fos);
					e.printStackTrace(ps);
					for(String s:inputHistory) {
						fos.write((s+'\n').getBytes());
					}
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				} catch (IOException e3) {
					e3.printStackTrace();
				}
			}
		}
	}
	
	
	
	public static Container stringToContainer(String text) {
		if(text.length()>0)	return new Token(text).convert();
		else return new IntC(0);
	}
	
	public static BoolContainer stringToBoolContainer(String text) {
		if(text.length()>0)	return new Token(text).convertBool();
		else return new BoolState(false);
	}
	
	static class Token{
		
		ArrayList<Token> tokens = new ArrayList<Token>();
		String text;
		boolean surounded;
		boolean basicOp = false;
		
		public void print() {
			if(tokens.size()>0 ) for(Token t:tokens) t.print();
			else System.out.println(text);
		}
		
		Token(String text) {
			this.text = text;
			surounded = true;
		}
		Token(String text,boolean surounded) {
			this.text = text;
			this.surounded = surounded;
		}
		Token(String text,boolean surounded,boolean op) {
			this.text = text;
			this.surounded = surounded;
			this.basicOp = op;
		}
		
		public Container convert() {
			seperate();
			removeTrash();
			return tokensToContainer();
		}
		
		public BoolContainer convertBool() {
			seperate();
			removeTrash();
			return boolTokensToContainer();
		}
		
		public BoolContainer boolTokensToContainer() {
			
			BoolContainer lastCont = null;
			Or orObj = new Or();
			And andObj = null;
			
			String lastOp = "or#";
			
			for(int i = tokens.size()-1;i>-1;i--) {
				Token t = tokens.get(i);
				if(t.surounded) {
					lastCont = t.convertBool();
					continue;
				}
				
				if(t.text.equals("not#")) {
					lastCont = new Not(lastCont);
					continue;
				}
				
				if(!t.basicOp) {
					String textLower = t.text.toLowerCase();
					if(textLower.equals("0") || textLower.equals("false")) {
						lastCont = new BoolState(false);
					}else if(textLower.equals("1") || textLower.equals("true")) {
						lastCont = new BoolState(true);
					}else {
						lastCont = new BoolVar(t.text);
					}
					continue;
				}
				
				if(t.text.equals("or#") || t.text.equals("sum#")) {
					if(lastOp == "and#") {
						andObj.add(lastCont);
						lastCont = andObj;
					}
					orObj.add(lastCont);
					andObj = null;
					lastOp = "or#";
				}else if(t.text.equals("and#") || t.text.equals("prod#")) {
					if(andObj == null) andObj = new And();
					andObj.add(lastCont);
					lastOp = "and#";
				}
				
			}
			
			if(lastOp == "and#") {
				andObj.add(lastCont);
				lastCont = andObj;
				lastOp = "or#";
			}
			if(lastOp == "or#") {
				orObj.add(lastCont);
			}
			
			return orObj;
		}
		
		Container tokensToContainer() {

			Container lastCont = null;
			
			Sum sumObj = new Sum();
			Product prodObj = null;
			Container expo = null;
			
			String lastOp = "sum#";
			boolean sub = false;
			
			for(int i = tokens.size()-1;i>-1;i--) {
				Token t = tokens.get(i);
				
				String lower = t.text.toLowerCase();
				if(lower.equals("ln")||lower.equals("log")) {
					lastCont = new Log(lastCont);
					continue;
				}
				if(lower.equals("sin")) {
					lastCont = new Sin(lastCont);
					continue;
				}
				if(lower.equals("cos")) {
					lastCont = new Cos(lastCont);
					continue;
				}
				if(lower.equals("sqrt")) {
					lastCont = new Power(lastCont,new Power(new IntC(2),new IntC(-1)));
					continue;
				}
				if(lower.equals("cbrt")) {
					lastCont = new Power(lastCont,new Power(new IntC(3),new IntC(-1)));
					continue;
				}
				//
				if(!t.basicOp) {
					if(sub) {
						tokens.add(i+1, new Token("sum#",false,true));
						sub = false;
						i+=2;
						continue;
					}
					if(t.surounded) {
						lastCont = t.convert();
						continue;
					}else {
						try {
							BigInteger value = new BigInteger(t.text);
							lastCont = new IntC(value);
							continue;
						}catch(Exception e) {
							//special constants
							if(lower.equals("e")) {
								lastCont = new E();
								continue;
							}
							if(lower.equals("pi")||lower.equals("π")) {
								lastCont = new Pi();
								continue;
							}
							if(lower.equals("ans")) {
								lastCont = ans.clone();
								continue;
							}
							//
							lastCont = new Var(t.text);
							continue;
						}
					}
				}
				sub = false;
				if(expo!=null) {
					lastCont = new Power(lastCont,expo);
					expo = null;
				}
				
				if(t.text.equals("sub#")) {
					Product pr = new Product();
					pr.add(new IntC(-1));
					pr.add(lastCont);
					lastCont = pr;
					sub = true;
					continue;
				}
				
				if(t.text.equals("sum#")) {
					
					if(lastOp == "prod#") {
						prodObj.add(lastCont);
						sumObj.add(prodObj);
					}else {
						sumObj.add(lastCont);
					}
					prodObj = null;
					lastOp = "sum#";
				}else if(t.text.equals("prod#")||t.text.equals("div#")) {
					if(prodObj == null) prodObj = new Product();
					if(t.text.equals("prod#")) prodObj.add(lastCont);
					else prodObj.add(new Power(lastCont,new IntC(-1)));
					lastOp = "prod#";
				}else if(t.text.equals("pow#")) {
					expo = lastCont;
				}
			}
			{
				if(expo!=null) {
					lastCont = new Power(lastCont,expo);
					expo = null;
				}
				if(lastOp == "prod#") {
					prodObj.add(lastCont);
					sumObj.add(prodObj);
				}else {
					sumObj.add(lastCont);
				}
				prodObj = null;
				lastOp = "sum#";
			}
			
			return sumObj.alone();
		}
		
		void removeTrash() {
			for(int i = 0;i<tokens.size();i++) {
				Token temp = tokens.get(i);
				if(temp.text.equals("+") || temp.text.equals("*") || temp.text.equals("^") || temp.text.equals("/") || temp.text.equals("-") || temp.text.equals("") || temp.text.equals("!") || temp.text.equals("|") || temp.text.equals("&")) {
					//remove unneeded lines
					tokens.remove(i);
					i--;
					continue;
				}
				if(!temp.surounded) {
					//remove 
					temp.text = temp.text.replaceAll("\\+","");
					temp.text = temp.text.replaceAll("\\*","");
					temp.text = temp.text.replaceAll("·","");
					temp.text = temp.text.replaceAll("\\^","");
					temp.text = temp.text.replaceAll("/","");
					temp.text = temp.text.replaceAll("\\-","");
					temp.text = temp.text.replaceAll("!","");
					temp.text = temp.text.replaceAll("\\|","");
					temp.text = temp.text.replaceAll("\\&","");
				}
			}
		}
		void seperate() {
			
			int[] level = new int[text.length()];
			int currentLevel = 0;
			for(int i = 0;i<text.length();i++) {
				char c = text.charAt(i);
				if(c == '(') currentLevel++;
				level[i] = currentLevel;
				if(c == ')') currentLevel--;
			}
			int indexOfLastFlip = 0;
			currentLevel = level[0];
			for(int i = 0;i<text.length();i++) {
				if((level[i]==0 && currentLevel!=0)||(level[i]!=0 && currentLevel==0)) {
					if(currentLevel != 0) tokens.add(new Token(text.substring(indexOfLastFlip+1, i-1),true));
					else tokens.add(new Token(text.substring(indexOfLastFlip, i),false));
					indexOfLastFlip = i;
				}
				if((text.charAt(i)=='+'||text.charAt(i)=='*'||text.charAt(i)=='·'||text.charAt(i)=='^'||text.charAt(i)=='/' || text.charAt(i)=='-' || text.charAt(i)=='!' || text.charAt(i)=='|' || text.charAt(i)=='&')&&level[i]==0) {
					tokens.add(new Token(text.substring(indexOfLastFlip, i),false));
					if(text.charAt(i)=='+') tokens.add(new Token("sum#",false,true));
					else if(text.charAt(i)=='*' || text.charAt(i)=='·') tokens.add(new Token("prod#",false,true));
					else if(text.charAt(i)=='^') tokens.add(new Token("pow#",false,true));
					else if(text.charAt(i)=='/') tokens.add(new Token("div#",false,true));
					else if(text.charAt(i)=='-') tokens.add(new Token("sub#",false,true));
					else if(text.charAt(i)=='!') tokens.add(new Token("not#",false,true));
					else if(text.charAt(i)=='|') tokens.add(new Token("or#",false,true));
					else if(text.charAt(i)=='&') tokens.add(new Token("and#",false,true));
					
					indexOfLastFlip = i;
				}
				currentLevel = level[i];
			}
			if(level[text.length()-1] != 0) tokens.add(new Token(text.substring(indexOfLastFlip+1,text.length()-1),true));
			else tokens.add(new Token(text.substring(indexOfLastFlip,text.length()),false));
		}
		
	}
}
