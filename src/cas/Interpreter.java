package cas;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Interpreter extends QuickMath{
	
	public static Expr SUCCESS = var("done!");
	
	public static void runScript(String fileName,boolean verbose) {
		long oldTime = System.nanoTime();
		Scanner sc;
		try {
			sc = new Scanner(new File(fileName));
			System.out.println("running "+fileName+" script...");
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				if(verbose) System.out.print(line+" -> ");
				Expr response = null;
				try {
					response = Ask.ask(line,Defs.blank,Settings.normal);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(verbose && response != null) System.out.print(response+" -> ");
				response = response.replace(Defs.blank.getVars()).simplify(Settings.normal);
				if(verbose && response != null) System.out.println(response);
			}
			
		} catch (FileNotFoundException e) {
			if(verbose) e.printStackTrace();
			System.err.println("fail!");
			return;
		}
		long delta = System.nanoTime() - oldTime;
		System.out.println("took " + delta / 1000000.0 + " ms to finish script!\n");
	}
	
	public static Expr createExpr(String string,Defs defs,Settings settings){
		string = string.replaceAll(" ", "");//remove spaces
		try {
			ArrayList<String> tokens = generateTokens(string);
			return createExprFromTokens(tokens,defs,settings);
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Expr createExprWithThrow(String string,Defs defs,Settings settings) throws Exception{
		string = string.replaceAll(" ", "");//remove spaces
		ArrayList<String> tokens = generateTokens(string);
		return createExprFromTokens(tokens,defs,settings);
	}
	
	public static Expr createExpr(String string) {
		return createExpr(string,Defs.blank,Settings.normal);
	}
	
	static Expr createExprFromToken(String token,Defs defs,Settings settings) throws Exception {
		ArrayList<String> tokens = new ArrayList<String>();
		tokens.add(token);
		return createExprFromTokens(tokens,defs,settings);
	}
	
	static void errors(ArrayList<String> tokens) throws Exception {
		//System.out.println(tokens);
		if(tokens == null) throw new Exception("missing tokens");
		
		String initToken = tokens.get(0);
		if(initToken.equals("+") || initToken.equals("*") || initToken.equals("/") || initToken.equals("^") || initToken.equals(",")) throw new Exception("starting with invalid token");//should not start with any of these
		
		String lastToken = tokens.get(tokens.size()-1);
		if(lastToken.equals("+") || lastToken.equals("-") || lastToken.equals("*") || lastToken.equals("/") || lastToken.equals("^") || lastToken.equals(",")) throw new Exception("ending with invalid token");//should not end with any of these
		
	}
	
	public static boolean isOperator(String string) {
		return string.matches("[+\\-*/^,=!;:\\[\\]\\{\\}\\(\\)]")&&!string.contains(" ");
	}
	
	public static boolean isProbablyExpr(String string) {
		return string.matches("pi|i|e|.*[(0-9)(+\\-*/^,=!;:\\[\\]\\{\\}\\(\\))].*")&&!string.contains(" ");
	}
	
	public static boolean containsOperators(String string) {
		return string.matches(".*[+\\-*/^,=!;:\\[\\]\\{\\}\\(\\)].*")&&!string.contains(" ");
	}
	
	static void printTokens(ArrayList<String> tokens) {//for debugging
		for(int i = 0;i<tokens.size();i++) {
			System.out.print(tokens.get(i));
			System.out.print('\\');
		}
		System.out.println();
	}
	
	static void removeEmptyTokens(ArrayList<String> tokens) {
		for(int i = 0;i<tokens.size();i++) {
			if(tokens.get(i).isBlank()) {
				tokens.remove(i);
				i--;
			}
		}
	}
	
	static Expr createExprFromTokens(ArrayList<String> tokens,Defs defs,Settings settings) throws Exception{
		
		removeEmptyTokens(tokens);
		
		errors(tokens);
		
		//System.out.println(tokens);
		
		if(tokens.size() == 1) {
			String string = tokens.get(0);
			if(string.isEmpty()) return null;
			Expr num = null;
			if(string.matches("[(0-9)(.)]+")){
				try {
					if(string.contains(".")) num = floatExpr(string);
					else num = num(string);
				}catch(Exception e) {}
			}
			
			if(num != null){
				return num;
			}else if(!containsOperators(string)){
				String lowered = string.toLowerCase();
				if(string.equals("i")) return num(0,1);
				else if(lowered.equals("pi")) return pi();
				else if(lowered.equals("e")) return e();
				else if(lowered.equals("true")) return bool(true);
				else if(lowered.equals("false")) return bool(false);
				else{//variables
					return var(string);
				}
			}else if(string.charAt(0) == '[') {
				if(string.equals("[]")) return new ExprList();
				return ExprList.cast(createExprFromToken(string.substring(1, string.length()-1),defs,settings));
			}else if(string.charAt(0) == '{') {
				if(string.equals("{}")) return new Script();
				Script script = Script.cast(createExprFromToken(string.substring(1, string.length()-1),defs,settings));
				return script;
			}else {
				return createExprFromTokens(generateTokens(tokens.get(0)),defs,settings);
			}
		}else if(tokens.size() == 2) {
			if(tokens.get(0).equals("-")) {
				Expr expr = createExpr(tokens.get(1));
				if(expr instanceof Num) return ((Num) expr).negate();
				return neg(createExpr(tokens.get(1),defs,settings));
			}else if(!tokens.get(1).equals("!")){
				String op = tokens.get(0);
				if(op.isBlank()) throw new Exception("confusing parenthesis");
				Expr params = ExprList.cast( createExprFromToken(tokens.get(1),defs,settings));
				
				Exception wrongParams = new Exception("wrong number of parameters");
				
				if(op.equals("diff")) {
					if(params.size() != 2) throw wrongParams;
					return diff(params.get(0),(Var)params.get(1));
				}else if(op.equals("integrate") || op.equals("int") || op.equals("integral") || op.equals("integrateOver")) {
					if(params.size() == 2) {
						return integrate(params.get(0),(Var)params.get(1));
					}else if(params.size() == 4) {
						return integrateOver(params.get(0),params.get(1),params.get(2),(Var)params.get(3));
					}else throw wrongParams;

				}else if(op.equals("sinh")) {
					if(params.size() != 1) throw wrongParams;
					return sinh(params.get(0));
				}else if(op.equals("cosh")) {
					if(params.size() != 1) throw wrongParams;
					return cosh(params.get(0));
				}else if(op.equals("tanh")) {
					if(params.size() != 1) throw wrongParams;
					return tanh(params.get(0));
				}else if(op.equals("sqrt")) {
					if(params.size() != 1) throw wrongParams;
					return sqrt(params.get(0));
				}else if(op.equals("cbrt")) {
					if(params.size() != 1) throw wrongParams;
					return cbrt(params.get(0));
				}else if(op.equals("sin")) {
					if(params.size() != 1) throw wrongParams;
					return sin(params.get(0));
				}else if(op.equals("cos")) {
					if(params.size() != 1) throw wrongParams;
					return cos(params.get(0));
				}else if(op.equals("tan")) {
					if(params.size() != 1) throw wrongParams;
					return tan(params.get(0));
				}else if(op.equals("atan") || op.equals("arctan")) {
					if(params.size() != 1) throw wrongParams;
					return atan(params.get(0));
				}else if(op.equals("asin") || op.equals("arcsin")) {
					if(params.size() != 1) throw wrongParams;
					return asin(params.get(0));
				}else if(op.equals("acos") || op.equals("arccos")) {
					if(params.size() != 1) throw wrongParams;
					return acos(params.get(0));
				}else if(op.equals("ln") || op.equals("log")) {
					if(params.size() != 1) throw wrongParams;
					return ln(params.get(0));
				}else if(op.equals("exp")) {
					if(params.size() != 1) throw wrongParams;
					return exp(params.get(0));
				}else if(op.equals("gamma")) {
					if(params.size() != 1) throw wrongParams;
					return gamma(params.get(0));
				}else if(op.equals("solve")) {
					if(params.size() != 2) throw wrongParams;
					if(!(params.get() instanceof Equ)) throw new Exception("the first parameter should be an equation");
					return solve((Equ)params.get(0),(Var)params.get(1));
				}else if(op.equals("inv")) {
					if(params.size() != 1) throw wrongParams;
					return inv(params.get(0));
				}else if(op.equals("approx")) {
					if(params.size() > 2 || params.size() == 0) throw wrongParams;
					if(params.size() == 1) return approx(params.get(0),new ExprList());
					return approx(params.get(0),(ExprList)params.get(1));
				}else if(op.equals("mathML")) {
					return var("\""+generateMathML(params.get(0))+"\"");
				}else if(op.equals("factor")) {
					if(params.size() != 1) throw wrongParams;
					return factor(params.get(0));
				}else if(op.equals("distr")) {
					if(params.size() != 1) throw wrongParams;
					return distr(params.get(0));
				}else if(op.equals("delete")) {
					String name = ((Var)params.get(0)).name;
					defs.removeVar(name);
					return SUCCESS;
				}else if(op.equals("deleteFunc")) {
					String name = ((Var)params.get(0)).name;
					defs.removeFunc(name);
					return SUCCESS;
				}else if(op.equals("clear")) {
					defs.clear();
					return SUCCESS;
				}else if(op.equals("showDefs")) {
					return var(defs.toString());
				}else if(op.equals("open")) {
					if(params.size() != 1) throw wrongParams;
					try {
						return Expr.openExpr(params.get(0).toString());
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else if(op.equals("runScript")) {
					if(params.size() != 2) throw wrongParams;
					runScript(params.get(0).toString(),((BoolState)params.get(1)).state);
					return SUCCESS;
				}else if(op.equals("save")) {
					if(params.size() != 2) throw wrongParams;
					try {
						Expr.saveExpr(params.get(0), params.get(1).toString());
						return SUCCESS;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else if(op.equals("define")) {
					String name = ((Var)params.get(0)).name;
					defs.addVar(name, params.get(1).simplify(Settings.normal));
					return SUCCESS;
				}else if(op.equals("r")) {//use is for modifyFromExample
					return params.get().simplify(settings);
				}else if(op.equals("defineFunc")) {
					String name = ((Var)params.get(0)).name;
					ExprList functionParams = null;
					if(params.get(1) instanceof ExprList) {
						functionParams = (ExprList)params.get(1);
					}else {
						functionParams = new ExprList();
						functionParams.add(params.get(1));
					}
					for(int i = 0;i<functionParams.size();i++) {
						functionParams.set(i, equ(functionParams.get(i),functionParams.get(i).copy()));
					}
					Func f = func(name ,functionParams ,params.get(2).simplify(Settings.normal ));
					f.example = true;
					defs.addFunc( name , f);
					return SUCCESS;
				}else if(op.equals("conv")){
					String fromUnit = params.get(1).toString();
					String toUnit = params.get(2).toString();
					return approx( Unit.conv(params.get(0), Unit.getUnit(fromUnit), Unit.getUnit(toUnit)) ,new ExprList());
				}else {
					Func f = defs.getFunc(op);
					if(f == null) {
						throw new Exception("function: \""+op+"\" is not defined");
					}
					ExprList vars = f.getVars();
					
					for(int i = 0;i<vars.size();i++) {
						Equ equ = (Equ)vars.get(i);
						equ.setRightSide(params.get(i));
					}
					
					return f;
				}
				
			}
		}
		boolean isSum = false,isProd = false,isList = false,isFactorial = false,isScript = false,isAssignment = false;
		int indexOfPowToken = -1,indexOfEquToken = -1;
		boolean lastWasOperator = false;
		for(int i = 0;i<tokens.size();i++) {
			String token = tokens.get(i);
			
			if(token.equals(",")) {
				isList = true;
				lastWasOperator = true;
			}else if(token.equals("=")) {
				indexOfEquToken = i;
				lastWasOperator = true;
			}else if(token.equals("+") || (token.equals("-") && !lastWasOperator)) {//the last operator check avoids error of misinterpreting x*-2*5 as a sum
				isSum = true;
				lastWasOperator = true;
			}else if(token.equals("*") || token.equals("/")) {
				isProd = true;
				lastWasOperator = true;
			}else if(token.equals("^")) {
				if(indexOfPowToken == -1) indexOfPowToken = i;
				lastWasOperator = true;
			}else if(token.equals("!")) {
				isFactorial = true;
				lastWasOperator = true;
			}else if(token.equals(":")) {
				isAssignment = true;
				lastWasOperator = true;
			}else if(token.equals(";")) {
				isScript = true;
				lastWasOperator = true;
			}else {
				lastWasOperator = false;
			}
		}
		if(isScript) {
			
			Script scr = new Script();
			int indexOfLastComma = 0;
			
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals(";")) {
					
					ArrayList<String> tokenSet = new ArrayList<String>();
					for(int j = indexOfLastComma;j<i;j++)  tokenSet.add(tokens.get(j));
					scr.add(createExprFromTokens(tokenSet,defs,settings));
					indexOfLastComma = i+1;
				}
			}
			
			return scr;
			
		}else if(isList) {
			ExprList list = new ExprList();
			int indexOfLastComma = 0;
			
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals(",")) {
					
					ArrayList<String> tokenSet = new ArrayList<String>();
					for(int j = indexOfLastComma;j<i;j++)  tokenSet.add(tokens.get(j));
					list.add(createExprFromTokens(tokenSet,defs,settings));
					indexOfLastComma = i+1;
				}
			}
			
			ArrayList<String> tokenSet = new ArrayList<String>();
			for(int j = indexOfLastComma;j<tokens.size();j++)  tokenSet.add(tokens.get(j));
			list.add(createExprFromTokens(tokenSet,defs,settings));
			
			return list;
			
		}else if(indexOfEquToken != -1) {
			ArrayList<String> leftSideTokens = new ArrayList<String>();
			for(int i = 0;i<indexOfEquToken;i++) {
				leftSideTokens.add(tokens.get(i));
			}
			ArrayList<String> rightSideTokens = new ArrayList<String>();
			for(int i = indexOfEquToken+1;i<tokens.size();i++) {
				rightSideTokens.add(tokens.get(i));
			}
			return equ(createExprFromTokens(leftSideTokens,defs,settings),createExprFromTokens(rightSideTokens,defs,settings));
		}else if(isSum) {
			tokens.add("+");
			Expr sum = new Sum();
			int indexOfLastAdd = 0;
			boolean nextIsSub = false;
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals("+") || token.equals("-")) {
					if(i != 0) {
						if(tokens.get(i-1).equals("^") || tokens.get(i-1).equals("/")) continue;//avoids error created by e^-x+x where the negative is actually in the exponent same things with x+y/-2
						ArrayList<String> tokenSet = new ArrayList<String>();
						for(int j = indexOfLastAdd;j<i;j++) {
							tokenSet.add(tokens.get(j));
						}
						if(nextIsSub) {
							Expr toBeAdded = createExprFromTokens(tokenSet,defs,settings);
							if(toBeAdded instanceof Prod) {
								boolean foundNum = false;
								for(int k = 0;k<toBeAdded.size();k++) {
									if(toBeAdded.get(k) instanceof Num) {
										Num casted = (Num)toBeAdded.get(k);
										casted.realValue = casted.realValue.negate();
										foundNum = true;
										break;
									}
								}
								if(!foundNum) {
									toBeAdded.add(num(-1));
								}
								sum.add(toBeAdded);
							}else if(toBeAdded instanceof Num) {
								sum.add( ((Num)toBeAdded).negate() );
							}else sum.add(neg(toBeAdded));
						}
						else {
							sum.add(createExprFromTokens(tokenSet,defs,settings));
						}
					}
					indexOfLastAdd = i;
					nextIsSub = false;
					boolean goingThroughOperators = true;
					for (int j = i;goingThroughOperators&&j<tokens.size();j++) {
						if(tokens.get(j).equals("-")) {
							nextIsSub=!nextIsSub;
						}
						if(j+1<tokens.size()) {
							if(!(tokens.get(j+1).equals("-") || tokens.get(j+1).equals("+"))) {
								goingThroughOperators = false;
							}
						}
						tokens.remove(j);
						j--;
					}
				}
			}
			if(sum.size() == 1) return sum.get();
			return sum;
		}else if(isProd) {
			Expr numerProd = new Prod();
			Expr denomProd = new Prod();
			int indexOfLastProd = 0;
			boolean nextDiv = false;
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals("*") || token.equals("/")) {
					ArrayList<String> tokenSet = new ArrayList<String>();
					for(int j = indexOfLastProd;j<i;j++) {
						tokenSet.add(tokens.get(j));
					}
					if(nextDiv) denomProd.add(createExprFromTokens(tokenSet,defs,settings));
					else numerProd.add(createExprFromTokens(tokenSet,defs,settings));
					if(token.equals("/")) nextDiv = true;
					else nextDiv = false;
					
					indexOfLastProd = i+1;
				}
			}
			
			ArrayList<String> tokenSet = new ArrayList<String>();
			for(int j = indexOfLastProd;j<tokens.size();j++) {
				tokenSet.add(tokens.get(j));
			}
			if(nextDiv) denomProd.add(createExprFromTokens(tokenSet,defs,settings));
			else {
				Expr test = createExprFromTokens(tokenSet,defs,settings);
				numerProd.add(test);
			}
			if(numerProd.size() == 1) numerProd = numerProd.get();
			if(denomProd.size() == 1) denomProd = denomProd.get();
			
			if(denomProd instanceof Prod && denomProd.size() == 0) {
				return numerProd;
			}
			return div(numerProd,denomProd);
		}else if(indexOfPowToken != -1) {
			ArrayList<String> baseTokens = new ArrayList<String>();
			for(int i = 0;i<indexOfPowToken;i++) baseTokens.add(tokens.get(i));
			Expr base = createExprFromTokens(baseTokens,defs,settings);
			
			ArrayList<String> expoTokens = new ArrayList<String>();
			for(int i = indexOfPowToken+1;i<tokens.size();i++) expoTokens.add(tokens.get(i));
			Expr expo = createExprFromTokens(expoTokens,defs,settings);
			
			return pow(base,expo);
		}else if(isFactorial) {
			Expr out = createExpr(tokens.get(0),defs,settings);
			for(int i = tokens.size()-1;i>=1;i--){
				if(tokens.get(i).equals("!")){
					out = gamma(sum(out,num(1)));
				}
			}
			return out;
		}
		throw new Exception("unrecognized format:"+tokens);
	}
	
	
	static char[] basicOperators = new char[] {
		'*','+','-','^','/',',','=','!',':',';'
	};
	private static boolean isBasicOperator(char c) {
		for(char o:basicOperators) {
			if(o == c) return true;
		}
		return false;
	}
	
	static ArrayList<String> generateTokens(String string) throws Exception{//splits up a string into its relevant subsections and removes parentheses	
		ArrayList<String> tokens = new ArrayList<String>();
		int count = 0;
		int lastIndex = 0;
		for(int i = 0;i < string.length();i++) {
			if(string.charAt(i) == '(' || string.charAt(i) == '[' || string.charAt(i) == '{') count++;
			else if(string.charAt(i) == ')' || string.charAt(i) == ']' || string.charAt(i) == '}') {
				count--;
				if(i != string.length()-1  && !containsOperators(Character.toString(  string.charAt(i+1) ))) {
					//throw new Exception("expected operator after ')'");
				}
			}
			
			if(count == 0) {
				if(isBasicOperator(string.charAt(i))) {
					String subString = string.substring(lastIndex, i);
					if(!subString.isEmpty())tokens.add(subString);
					tokens.add(String.valueOf(string.charAt(i)));
					lastIndex = i+1;
				}else if(string.charAt(i) == ')') {
					tokens.add(string.substring(lastIndex+1, i));
					lastIndex = i+1;
				}
			}else if(count == 1 && ((string.charAt(i) == '(') || string.charAt(i) == '{')) {//this is important for detecting sin(x) into [sin,x]
				String subString = string.substring(lastIndex, i);
				if(!subString.isEmpty())tokens.add(subString);
				lastIndex = i;
			}
		}
		if(count != 0) throw new Exception("missing ')'");
		if(lastIndex < string.length()+1) {
			String subString = string.substring(lastIndex, string.length());
			if(!subString.isEmpty())tokens.add(subString);
		}
		//strange case of 6x meaning 6*x
		
		
		
		
		return tokens;
	}
}
