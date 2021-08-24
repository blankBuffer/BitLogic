package cas;
import java.io.IOException;
import java.util.ArrayList;

public class Interpreter extends QuickMath{
	
	private static Expr expr = null;//used for running the r() on own thread for avoiding getting stuck
	
	public static Expr createExpr(String string){
		string = string.replaceAll(" ", "");//remove spaces
		try {
			ArrayList<String> tokens = generateTokens(string);
			return createExprFromTokens(tokens);
		}catch(Exception e) {
			System.err.println(e);
			return null;
		}
	}
	
	static Expr createExprFromToken(String token) throws Exception {
		ArrayList<String> tokens = new ArrayList<String>();
		tokens.add(token);
		return createExprFromTokens(tokens);
	}
	
	static void errors(ArrayList<String> tokens) throws Exception {
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
	
	static boolean containsOperators(String string) {
		for(char c:string.toCharArray()) {
			if(c == '+') return true;
			else if(c == '-') return true;
			else if(c == '^') return true;
			else if(c == '(') return true;
			else if(c == '/') return true;
			else if(c == '*') return true;
			else if(c == ',') return true;
			else if(c == '[') return true;
			else if(c == '=') return true;
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
	
	static Expr createExprFromTokens(ArrayList<String> tokens) throws Exception{
		
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
				if(string.equals("i")) return i();
				else if(string.equals("pi")) return pi();
				else if(string.equals("e")) return e();
				else return var(string);
			}else if(string.charAt(0) == '[') {
				if(string.equals("[]")) return new ExprList();
				Expr expr = createExprFromToken(string.substring(1, string.length()-1));
				if(expr instanceof ExprList) return expr;
				else {
					ExprList temp = new ExprList();
					temp.add(expr);
					return temp;
				}
			}else {
				return createExprFromTokens(generateTokens(tokens.get(0)));
			}
		}else if(tokens.size() == 2) {
			if(tokens.get(0) == "-") {
				String string = tokens.get(1);
				Expr num = null;
				try {
					if(string.contains(".")) num = floatExpr(string);
					else num = num(string);
				}catch(Exception e) {}
				
				if(num != null) {
					if(num instanceof Num) ((Num)num).value = ((Num)num).value.negate();
					else ((FloatExpr)num).value = -((FloatExpr)num).value;
					return num;
				}
			}else {
				String op = tokens.get(0);
				if(op.isBlank()) throw new Exception("confusing parenthesis");
				Expr params = createExprFromToken(tokens.get(1));
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
				}else if(op.equals("sqrt")) {
					if(params.size() != 1) throw wrongParams;
					return sqrt(params.get(0));
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
				}else if(op.equals("integrateOver")) {
					if(params.size() != 4) throw wrongParams;
					return integrateOver(params.get(0),params.get(1),params.get(2),(Var)params.get(3));
				}else if(op.equals("solve")) {
					if(params.size() != 2) throw wrongParams;
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
				}
				else if(op.equals("open")) {
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
						return var("done!");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else if(op.equals("r")) {
					if(params.size() != 1) throw wrongParams;
					expr = params.get(0);
					Thread compute = new Thread() {
						@Override
						public void run() {
							expr = expr.simplify(Settings.normal);
						}
					};
					compute.start();
					Thread stuckCheck = new Thread() {
						@Override
						public void run() {
							try {
								while(compute.isAlive()) {
									sleep(1000);
									if(compute.isAlive()) System.out.println("thinking...");
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
					return expr;
				}
				else throw new Exception("function \""+op+"\" is not part of the system");
				
			}
		}
		
		boolean isSum = false,isProd = false,isList = false;
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
					list.add(createExprFromTokens(tokenSet));
					indexOfLastComma = i+1;
				}
			}
			
			ArrayList<String> tokenSet = new ArrayList<String>();
			for(int j = indexOfLastComma;j<tokens.size();j++)  tokenSet.add(tokens.get(j));
			list.add(createExprFromTokens(tokenSet));
			
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
			return equ(createExprFromTokens(leftSideTokens),createExprFromTokens(rightSideTokens));
		}else if(isSum) {
			Expr sum = new Sum();
			int indexOfLastAdd = 0;
			boolean nextIsSub = false;
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals("+") || token.equals("-")) {
					if(i != 0) {
						if(tokens.get(i-1).equals("^")) continue;//avoids error created by e^-x+x where the negative is actually in the exponent
						ArrayList<String> tokenSet = new ArrayList<String>();
						for(int j = indexOfLastAdd;j<i;j++) {
							tokenSet.add(tokens.get(j));
						}
						if(nextIsSub) {
							Expr toBeAdded = createExprFromTokens(tokenSet);
							if(toBeAdded instanceof Prod) {
								boolean foundNum = false;
								for(int k = 0;k<toBeAdded.size();k++) {
									if(toBeAdded.get(k) instanceof Num) {
										Num casted = (Num)toBeAdded.get(k);
										casted.value = casted.value.negate();
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
							sum.add(createExprFromTokens(tokenSet));
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
				Expr toBeAdded = createExprFromTokens(tokenSet);
				if(toBeAdded instanceof Prod) {
					boolean foundNum = false;
					for(int k = 0;k<toBeAdded.size();k++) {
						if(toBeAdded.get(k) instanceof Num) {
							Num casted = (Num)toBeAdded.get(k);
							casted.value = casted.value.negate();
							foundNum = true;
							break;
						}
					}
					if(!foundNum) {
						toBeAdded.add(num(-1));
					}
					sum.add(toBeAdded);
				}else sum.add(neg(toBeAdded));
			}else sum.add(createExprFromTokens(tokenSet));
			
			if(sum.size() == 1) return sum.get();
			return sum;
		}else if(isProd) {
			Expr prod = new Prod();
			int indexOfLastProd = 0;
			boolean nextDiv = false;
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals("*") || token.equals("/")) {
					ArrayList<String> tokenSet = new ArrayList<String>();
					for(int j = indexOfLastProd;j<i;j++) {
						tokenSet.add(tokens.get(j));
					}
					if(nextDiv) prod.add(inv(createExprFromTokens(tokenSet)));
					else prod.add(createExprFromTokens(tokenSet));
					if(token.equals("/")) nextDiv = true;
					else nextDiv = false;
					
					indexOfLastProd = i+1;
				}
			}
			ArrayList<String> tokenSet = new ArrayList<String>();
			for(int j = indexOfLastProd;j<tokens.size();j++) {
				tokenSet.add(tokens.get(j));
			}
			if(nextDiv) prod.add(inv(createExprFromTokens(tokenSet)));
			else prod.add(createExprFromTokens(tokenSet));
			
			if(prod.get(0).equalStruct(num(1))) prod.remove(0);
			if(prod.size() == 1) return prod.get();
			
			return prod;
		}else if(indexOfPowToken != -1) {
			ArrayList<String> baseTokens = new ArrayList<String>();
			for(int i = 0;i<indexOfPowToken;i++) baseTokens.add(tokens.get(i));
			Expr base = createExprFromTokens(baseTokens);
			
			ArrayList<String> expoTokens = new ArrayList<String>();
			for(int i = indexOfPowToken+1;i<tokens.size();i++) expoTokens.add(tokens.get(i));
			Expr expo = createExprFromTokens(expoTokens);
			
			return pow(base,expo);
		}
		
		return null;
	}
	
	static ArrayList<String> generateTokens(String string) throws Exception{//splits up a string into its relevant subsections and removes parentheses
		ArrayList<String> tokens = new ArrayList<String>();
		
		int count = 0;
		int lastIndex = 0;
		for(int i = 0;i < string.length();i++) {
			if(string.charAt(i) == '(' || string.charAt(i) == '[') count++;
			else if(string.charAt(i) == ')' || string.charAt(i) == ']') count--;
			
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
				}else if(string.charAt(i) == ')') {
					tokens.add(string.substring(lastIndex+1, i));
					lastIndex = i+1;
				}else if(string.charAt(i) == ']') {
					tokens.add(string.substring(lastIndex, i+1));
					lastIndex = i+2;
				}
				
				
			}else if(count == 1 && (string.charAt(i) == '(')) {//this is important for detecting sin(x) into [sin,x]
				String subString = string.substring(lastIndex, i);
				if(!subString.isEmpty())tokens.add(subString);
				lastIndex = i;
			}
		}
		if(count != 0) throw new Exception("could not find parenthesis");
		if(lastIndex < string.length()+1) {
			String subString = string.substring(lastIndex, string.length());
			if(!subString.isEmpty())tokens.add(subString);
		}
		return tokens;
	}
}
