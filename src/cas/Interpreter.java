package cas;
import java.io.IOException;
import java.util.ArrayList;

public class Interpreter extends QuickMath{
	
	public static Expr SUCCESS = var("done!");
	
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
		if(initToken.equals("+") || initToken.equals("*") || initToken.equals("/") || initToken.equals("^") || initToken.equals(",")) throw new Exception("starting with invalid token");;//should not start with any of these
		
		String lastToken = tokens.get(tokens.size()-1);
		if(lastToken.equals("+") || lastToken.equals("-") || lastToken.equals("*") || lastToken.equals("/") || lastToken.equals("^") || lastToken.equals(",")) throw new Exception("ending with invalid token");;//should not end with any of these
		
		for(int i = 1;i<tokens.size();i++) {//should not contain two of the same token in a row
			if(tokens.get(i).equals(tokens.get(i-1))) {
				throw new Exception("found duplicate token");
			}
		}
		
	}
	
	public static boolean isOperator(String string) {
		if(string.equals("+")) return true;
		else if(string.equals("(")) return true;
		else if(string.equals(")")) return true;
		else if(string.equals("{")) return true;
		else if(string.equals("}")) return true;
		else if(string.equals("-")) return true;
		else if(string.equals("^")) return true;
		else if(string.equals("/")) return true;
		else if(string.equals("*")) return true;
		else if(string.equals(",")) return true;
		else if(string.equals("[")) return true;
		else if(string.equals("]")) return true;
		else if(string.equals("=")) return true;
		else if(string.equals("!")) return true;
		
		return false;
	}
	
	public static boolean containsOperators(String string) {
		for(char c:string.toCharArray()) {
			if(c == '+') return true;
			else if(c == '(') return true;
			else if(c == ')') return true;
			else if(c == '{') return true;
			else if(c == '}') return true;
			else if(c == '-') return true;
			else if(c == '^') return true;
			else if(c == '/') return true;
			else if(c == '*') return true;
			else if(c == ',') return true;
			else if(c == '[') return true;
			else if(c == ']') return true;
			else if(c == '=') return true;
			else if(c == '!') return true;
		}
		return false;
	}
	
	static void printTokens(ArrayList<String> tokens) {//for debugging
		for(int i = 0;i<tokens.size();i++) {
			System.out.print(tokens.get(i));
			System.out.print('\\');
		}
		System.out.println();
	}
	
	static Expr createExprFromTokens(ArrayList<String> tokens,Defs defs,Settings settings) throws Exception{
		
		errors(tokens);
		
		if(tokens.size() == 1) {
			String string = tokens.get(0);
			if(string.isEmpty()) return null;
			Expr num = null;
			try {
				if(string.contains(".")) num = floatExpr(string);
				else num = num(string);
			}catch(Exception e) {}
			
			if(num != null){
				return num;
			}else if(!containsOperators(string)){
				if(string.equals("i")) return num(0,1);
				else if(string.equals("pi")) return pi();
				else if(string.equals("e")) return e();
				else {//variables
					return var(string);
				}
			}else if(string.charAt(0) == '[') {
				if(string.equals("[]")) return new ExprList();
				Expr expr = createExprFromToken(string.substring(1, string.length()-1),defs,settings);
				if(expr instanceof ExprList) return expr;
				else {
					ExprList temp = new ExprList();
					temp.add(expr);
					return temp;
				}
			}else {
				return createExprFromTokens(generateTokens(tokens.get(0)),defs,settings);
			}
		}else if(tokens.size() == 2) {
			if(tokens.get(0) == "-") {
				return neg(createExpr(tokens.get(1),defs,settings));
			}else if(!tokens.get(1).equals("!")){
				String op = tokens.get(0);
				if(op.isBlank()) throw new Exception("confusing parenthesis");
				Expr paramsTemp = createExprFromToken(tokens.get(1),defs,settings);
				ExprList params;
				if(paramsTemp instanceof ExprList) params = (ExprList)paramsTemp;
				else {
					params = new ExprList();
					params.add(paramsTemp);
				}
				
				Exception wrongParams = new Exception("wrong number of parameters");
				if(!(params instanceof ExprList)) {
					ExprList temp = new ExprList();
					temp.add(params);
					params = temp;
				}
				if(op.equals("diff")) {
					if(params.size() != 2) throw wrongParams;
					return diff(params.get(0),(Var)params.get(1));
				}else if(op.equals("integrate")) {
					if(params.size() != 2) throw wrongParams;
					return integrate(params.get(0),(Var)params.get(1));
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
				}else if(op.equals("atan")) {
					if(params.size() != 1) throw wrongParams;
					return atan(params.get(0));
				}else if(op.equals("ln") || op.equals("log")) {
					if(params.size() != 1) throw wrongParams;
					return ln(params.get(0));
				}else if(op.equals("exp")) {
					if(params.size() != 1) throw wrongParams;
					return exp(params.get(0));
				}else if(op.equals("gamma")) {
					if(params.size() != 1) throw wrongParams;
					return gamma(params.get(0));
				}else if(op.equals("integrateOver")) {
					if(params.size() != 4) throw wrongParams;
					return integrateOver(params.get(0),params.get(1),params.get(2),(Var)params.get(3));
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
					else return approx(params.get(0),(ExprList)params.get(1));
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
				}
				else if(op.equals("defineFunc")) {
					String name = ((Var)params.get(0)).name;
					ExprList functionParams = null;
					if(params.get(1) instanceof ExprList) {
						functionParams = (ExprList)params.get(1);
					}else {
						functionParams = new ExprList();
						functionParams.add(paramsTemp.get(1));
					}
					for(int i = 0;i<functionParams.size();i++) {
						functionParams.set(i, equ(functionParams.get(i),functionParams.get(i).copy()));
					}
					Func f = func(name ,functionParams ,params.get(2).simplify(Settings.normal ));
					f.example = true;
					defs.addFunc( name , f);
					return SUCCESS;
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
		boolean isSum = false,isProd = false,isList = false,isFactorial = false;
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
			}else {
				lastWasOperator = false;
			}
		}
		
		if(isList) {
			Expr list = new ExprList();
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
							}else sum.add(neg(toBeAdded));
						}
						else {
							sum.add(createExprFromTokens(tokenSet,defs,settings));
						}
					}
					indexOfLastAdd = i+1;
					nextIsSub = token.equals("-");
				}
			}
			ArrayList<String> tokenSet = new ArrayList<String>();
			for(int j = indexOfLastAdd;j<tokens.size();j++) {
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
				}else sum.add(neg(toBeAdded));
			}else sum.add(createExprFromTokens(tokenSet,defs,settings));
			
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
			}else {
				return div(numerProd,denomProd);
			}
		}else if(indexOfPowToken != -1) {
			ArrayList<String> baseTokens = new ArrayList<String>();
			for(int i = 0;i<indexOfPowToken;i++) baseTokens.add(tokens.get(i));
			Expr base = createExprFromTokens(baseTokens,defs,settings);
			
			ArrayList<String> expoTokens = new ArrayList<String>();
			for(int i = indexOfPowToken+1;i<tokens.size();i++) expoTokens.add(tokens.get(i));
			Expr expo = createExprFromTokens(expoTokens,defs,settings);
			
			return pow(base,expo);
		}else if(isFactorial) {
			return gamma(sum(createExpr(tokens.get(0),defs,settings),num(1)));
		}
		throw new Exception("unrecognized format:"+tokens);
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
				if(string.charAt(i) == '*') {
					String subString = string.substring(lastIndex, i);
					if(!subString.isEmpty())tokens.add(subString);
					tokens.add("*");
					lastIndex = i+1;
				}else if(string.charAt(i) == '+') {
					String subString = string.substring(lastIndex, i);
					if(!subString.isEmpty())tokens.add(subString);
					tokens.add("+");
					lastIndex = i+1;
				}else if(string.charAt(i) == '-') {
					String subString = string.substring(lastIndex, i);
					if(!subString.isEmpty())tokens.add(subString);
					tokens.add("-");
					lastIndex = i+1;
				}else if(string.charAt(i) == '^') {
					String subString = string.substring(lastIndex, i);
					if(!subString.isEmpty())tokens.add(subString);
					tokens.add("^");
					lastIndex = i+1;
				}else if(string.charAt(i) == '/') {
					String subString = string.substring(lastIndex, i);
					if(!subString.isEmpty())tokens.add(subString);
					tokens.add("/");
					lastIndex = i+1;
				}else if(string.charAt(i) == ',') {
					String subString = string.substring(lastIndex, i);
					if(!subString.isEmpty())tokens.add(subString);
					tokens.add(",");
					lastIndex = i+1;
				}else if(string.charAt(i) == '=') {
					String subString = string.substring(lastIndex, i);
					if(!subString.isEmpty())tokens.add(subString);
					tokens.add("=");
					lastIndex = i+1;
				}else if(string.charAt(i) == '!') {
					String subString = string.substring(lastIndex, i);
					if(!subString.isEmpty())tokens.add(subString);
					tokens.add("!");
					lastIndex = i+1;
				}else if(string.charAt(i) == ')') {
					tokens.add(string.substring(lastIndex+1, i));
					lastIndex = i+1;
				}else if(string.charAt(i) == '}') {
					tokens.add(string.substring(lastIndex, i+1));
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
		return tokens;
	}
}
