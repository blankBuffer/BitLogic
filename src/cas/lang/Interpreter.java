package cas.lang;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cas.*;
import cas.bool.*;
import cas.primitive.*;
import cas.programming.Script;
import cas.matrix.*;

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
	
	public static Expr createExprWithThrow(String string) throws Exception {
		return createExprWithThrow(string,Defs.blank,Settings.normal);
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
	static Pattern isOperatorPattern = Pattern.compile("[+\\-*^/,=><!;:\\~\\&\\|\\[\\]\\{\\}\\(\\)\\.]");
	public static boolean isOperator(String string) {
		Matcher m = isOperatorPattern.matcher(string);
		return m.matches();
	}
	
	static Pattern isProbablyExprPattern = Pattern.compile("pi|i|e|.*[(0-9)(+\\-*^/,=><!;:\\~\\&\\|\\[\\]\\{\\}\\(\\)\\.].*");
	public static boolean isProbablyExpr(String string) {
		Matcher m = isProbablyExprPattern.matcher(string);
		return m.matches();
	}
	
	static Pattern containsOperatorsPattern = Pattern.compile(".*[+\\-*/^,=><!;:\\~\\&\\|\\[\\]\\{\\}\\(\\)\\.].*");
	public static boolean containsOperators(String string) {
		Matcher m = containsOperatorsPattern.matcher(string);
		return m.matches();
	}
	
	static Expr createExprFromTokens(ArrayList<String> tokens,Defs defs,Settings settings) throws Exception{
		if(tokens.size() == 0) return null;
		//System.out.println("recieved: "+tokens);
		errors(tokens);
		
		if(tokens.contains(".")) {
			for(int i = 0;i<tokens.size()-2;i++){//floating point reading
				if(i+6<tokens.size()+1 && tokens.get(i).equals("-") && tokens.get(i+1).matches("([0-9])+") && tokens.get(i+2).equals(".") && tokens.get(i+3).matches("([0-9])*(e|E)") && tokens.get(i+4).equals("-") && tokens.get(i+5).matches("([0-9])+")) {
					String newToken = "-"+tokens.get(i+1)+"."+tokens.get(i+3)+tokens.get(i+4)+tokens.get(i+5);
					for(int j = 1;j<6;j++) tokens.remove(i);
					tokens.set(i, newToken);
				}
				if(i+5<tokens.size()+1 && tokens.get(i).matches("([0-9])+") && tokens.get(i+1).equals(".") && tokens.get(i+2).matches("([0-9])*(e|E)") && tokens.get(i+3).equals("-") && tokens.get(i+4).matches("([0-9])+")) {
					String newToken = tokens.get(i)+"."+tokens.get(i+2)+tokens.get(i+3)+tokens.get(i+4);
					for(int j = 1;j<5;j++) tokens.remove(i);
					tokens.set(i, newToken);
				}
				if(i+4<tokens.size()+1 && tokens.get(i).equals("-") && tokens.get(i+1).matches("([0-9])+") && tokens.get(i+2).equals(".") && tokens.get(i+3).matches("([0-9])*(e|E)?([0-9])*")) {
					String newToken = "-"+tokens.get(i+1)+"."+tokens.get(i+3);
					for(int j = 1;j<4;j++) tokens.remove(i);
					tokens.set(i, newToken);
				}
				if(i+3<tokens.size()+1 && tokens.get(i).matches("([0-9])+") && tokens.get(i+1).equals(".") && tokens.get(i+2).matches("([0-9])*(e|E)?([0-9])*")) {
					String newToken = tokens.get(i)+"."+tokens.get(i+2);
					for(int j = 1;j<3;j++) tokens.remove(i);
					tokens.set(i, newToken);
				}
					
			}
		}
		
		if(tokens.size() == 1) {
			String string = tokens.get(0);
			
			if(string.matches("[0-9]+")){
				return num(string);
			}else if(string.matches("[\\-]?[0-9]+((.[0-9]+)|(.[0-9]+[e|E][\\-]?[0-9]+))")) {
				return floatExpr(string);
			}else if(!containsOperators(string)){
				String lowered = string.toLowerCase();
				if(string.equals("i")) return num(0,1);
				else if(lowered.equals("true")) return bool(true);
				else if(lowered.equals("false")) return bool(false);
				else if(lowered.equals("degree")) return div(pi(),num(180));
				else{//variables
					return var(string);
				}
			}else {
				
				if( isBracket(string.charAt(0)) ) {
					
					String subPart = string.substring(1,string.length()-1);
					
					ArrayList<String> subTokens = generateTokens(subPart);
					Expr outExpr = createExprFromTokens(subTokens,defs,settings);
					
					if(string.startsWith("(")) {
						return outExpr;
					}else if(string.startsWith("{")){
						if(outExpr instanceof Params) return Sequence.cast(outExpr);
						else if(outExpr == null) return sequence();
						return sequence(outExpr);
					}else if(string.startsWith("[")){
						if(outExpr instanceof Params) return ExprList.cast(outExpr);
						else if(outExpr == null) return exprList();
						return exprList(outExpr);
					}
					
				}
				
			}
		}else if(tokens.size() == 2) {
			if(tokens.get(0).equals("-")) {//negate
				Expr expr = createExpr(tokens.get(1));
				if(expr instanceof Num) return ((Num) expr).negate();
				return neg(createExpr(tokens.get(1),defs,settings));
			}else if(!tokens.get(1).equals("!")){//functional notation
				String op = tokens.get(0);
				if(op.isBlank()) throw new Exception("confusing parenthesis");
				Expr params = Params.cast( createExprFromToken(tokens.get(1),defs,settings));
				
				if(op.equals("define")) {
					Expr def = params.get(0);
					
					if(def instanceof Becomes && ((Becomes)def).getLeftSide() instanceof Func) {
						Func f = (Func)((Becomes)def).getLeftSide().copy();
						f.ruleSequence.add(new Rule(((Becomes)def),"function definition",Rule.EASY));
						System.out.println(f.getRuleSequence());
						
						f.clear();
						defs.addFunc(f);
					}else {
						defs.addVar((Equ)def);
					}
					
					return var("done");
				}
				if(op.equals("delete")) {
					if(params.get(0) instanceof Func) {
						defs.removeFunc(params.get(0).typeName());
					}else {
						defs.removeVar(params.get(0).toString());
					}
					
					return var("done");
				}
				
				if(op.equals("defs")) {
					return var(defs.toString());
				}
				
				if(!op.equals("~")){
					Expr[] paramsArray = new Expr[params.size()];
					
					for(int i = 0;i<params.size();i++) {
						paramsArray[i] = params.get(i);
					}
					
					
					Expr f = SimpleFuncs.getFuncByName(op,defs,paramsArray);
					return f;
				}
				
			}
		}
		
		boolean isSum = false,
				isProdAndOrDiv = false,
				isParams = false,
				isFactorial = false,
				isScript = false,
				isAnd = false,
				isOr = false,
				isNot = false,
				isEqu = false,
				isArrow = false,
				isDot = false;
		
		int indexOfPowToken = -1,equCount = 0;
		boolean lastWasOperator = false;
		for(int i = 0;i<tokens.size();i++) {
			String token = tokens.get(i);
			
			if(token.equals(",")) {
				isParams = true;
				lastWasOperator = true;
			}else if(token.equals("=") || token.equals(">") || token.equals("<")) {
				isEqu = true;
				equCount++;
				lastWasOperator = true;
			}else if(token.equals("+") || (token.equals("-") && !lastWasOperator)) {//the last operator check avoids error of misinterpreting x*-2*5 as a sum
				isSum = true;
				lastWasOperator = true;
			}else if(token.equals("*") || token.equals("/")) {
				isProdAndOrDiv = true;
				lastWasOperator = true;
			}else if(token.equals(".")) {
				isDot = true;
				lastWasOperator = true;
			}else if(token.equals("^")) {
				if(indexOfPowToken == -1) indexOfPowToken = i;
				lastWasOperator = true;
			}else if(token.equals("!")) {
				isFactorial = true;
				lastWasOperator = true;
			}else if(token.equals(";")) {
				isScript = true;
				lastWasOperator = true;
			}else if(token.equals("&")){
				isAnd = true;
				lastWasOperator = true;
			}else if(token.equals("|")){
				isOr = true;
				lastWasOperator = true;
			}else if(token.equals("~")){
				isNot = true;
				lastWasOperator = true;
			}else if(token.equals("->")) {
				isArrow = true;
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
			
		}else if(isParams) {
			tokens.add(",");
			Params params = new Params();
			int indexOfLastComma = 0;
			
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals(",")) {
					
					ArrayList<String> tokenSet = new ArrayList<String>();
					for(int j = indexOfLastComma;j<i;j++)  tokenSet.add(tokens.get(j));
					params.add(createExprFromTokens(tokenSet,defs,settings));
					indexOfLastComma = i+1;
				}
			}
			
			return params;
			
		}else if(isArrow) {
			int indexOfArrowToken = tokens.indexOf("->");
			ArrayList<String> leftSideTokens = new ArrayList<String>();
			for(int i = 0;i<indexOfArrowToken;i++) {
				leftSideTokens.add(tokens.get(i));
			}
			ArrayList<String> rightSideTokens = new ArrayList<String>();
			for(int i = indexOfArrowToken+1;i<tokens.size();i++) {
				rightSideTokens.add(tokens.get(i));
			}
			
			Expr leftSide = createExprFromTokens(leftSideTokens,defs,settings);
			Expr rightSide = createExprFromTokens(rightSideTokens,defs,settings);
			
			return becomes(leftSide,rightSide);
		}else if(isEqu) {//is equation
			
			int indexOfEquToken = 0;
			int currentCount = 0;
			
			for(int i = 0; i < tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals("=") || token.equals(">") || token.equals("<")) {
					currentCount++;
				}
				if(currentCount == equCount/2+1 ) {
					indexOfEquToken = i;
					break;
				}
			}
			
			ArrayList<String> leftSideTokens = new ArrayList<String>();
			for(int i = 0;i<indexOfEquToken;i++) {
				leftSideTokens.add(tokens.get(i));
			}
			ArrayList<String> rightSideTokens = new ArrayList<String>();
			for(int i = indexOfEquToken+1;i<tokens.size();i++) {
				rightSideTokens.add(tokens.get(i));
			}
			char symbol = tokens.get(indexOfEquToken).charAt(0);
			
			Expr leftSide = createExprFromTokens(leftSideTokens,defs,settings);
			Expr rightSide = createExprFromTokens(rightSideTokens,defs,settings);
			
			if(symbol == '=') return equ(leftSide,rightSide);
			if(symbol == '>') return equGreater(leftSide,rightSide);
			if(symbol == '<') return equLess(leftSide,rightSide);
			
		}else if(isOr){
			tokens.add("|");
			Or or = new Or();
			int indexOfLastOr = 0;
			
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals("|")) {
					
					ArrayList<String> tokenSet = new ArrayList<String>();
					for(int j = indexOfLastOr;j<i;j++)  tokenSet.add(tokens.get(j));
					or.add(createExprFromTokens(tokenSet,defs,settings));
					indexOfLastOr = i+1;
				}
			}
			
			return or;
			
		}else if(isAnd){
			tokens.add("&");
			And and = new And();
			int indexOfLastAnd = 0;
			
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals("&")) {
					
					ArrayList<String> tokenSet = new ArrayList<String>();
					for(int j = indexOfLastAnd;j<i;j++)  tokenSet.add(tokens.get(j));
					and.add(createExprFromTokens(tokenSet,defs,settings));
					indexOfLastAnd = i+1;
				}
			}
			
			return and;
			
		}else if(isNot){
			tokens.remove(0);
			return not(createExprFromTokens(tokens,defs,settings));
		}else if(isSum) {
			tokens.add("+");
			Expr sum = new Sum();
			int indexOfLastAdd = 0;
			boolean nextIsSub = false;
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals("+") || token.equals("-")) {
					if(i != 0) {
						if(tokens.get(i-1).equals("^") || tokens.get(i-1).equals("/") || tokens.get(i-1).equals("*")) continue;//avoids error created by e^-x+x where the negative is actually in the exponent same things with x+y/-2
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
										Num negatedVersion = ((Num)toBeAdded.get(k)).negate();
										toBeAdded.set(k, negatedVersion);
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
		}else if(isDot) {
			tokens.add(".");
			Dot dot = new Dot();
			int indexOfLastAnd = 0;
			
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals(".")) {
					
					ArrayList<String> tokenSet = new ArrayList<String>();
					for(int j = indexOfLastAnd;j<i;j++)  tokenSet.add(tokens.get(j));
					dot.add(createExprFromTokens(tokenSet,defs,settings));
					indexOfLastAnd = i+1;
				}
			}
			
			return dot;
		}else if(isProdAndOrDiv) {
			tokens.add("*");
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
	
	
	private static char[] basicOperators = new char[] {
		'*','+','-','^','/',',','=','!',':',';','&','|','~','<','>','.'
	};
	private static boolean isBasicOperator(char c) {
		for(char o:basicOperators) {
			if(o == c) return true;
		}
		return false;
	}
	
	private static ArrayList<String> multiSymbolOperators = new ArrayList<String>();
	static {
		multiSymbolOperators.add("->");
	}
	private static void makeMultiSymbolOperators(ArrayList<String> tokens) {
		for(int i = 0;i < tokens.size();i++) {
			String fullToken = tokens.get(i);
			if(fullToken.isBlank() || fullToken.length()>1) continue; 
			
			char token = fullToken.charAt(0);
			if(!isBasicOperator(token)) continue;
			symbolLoop:for(String multiSymbol:multiSymbolOperators) {
				if(multiSymbol.charAt(0) == token) {
					for(int j = 1;j<multiSymbol.length();j++) {
						if(!(tokens.get(i+j).length() == 1 && multiSymbol.charAt(j) == tokens.get(i+j).charAt(0))) continue symbolLoop;
					}
					
					for(int j = i+multiSymbol.length()-1;j>i;j--) {
						tokens.remove(j);
					}
					tokens.set(i, multiSymbol);
					
				}
			}
			
		}
	}
	
	public static boolean isBracket(char ch) {
		return ch == '[' || ch == '{' || ch == '(';
	}
	public static int indexOfMatchingBracket(String s,int startIndex) throws Exception {
		char startingChar = s.charAt(startIndex);
		char endingChar = 0;
		if(startingChar == '[') endingChar = ']';
		else if(startingChar == '{') endingChar = '}';
		else if(startingChar == '(') endingChar = ')';
		
		int count = 1;
		
		int result = -1;
		
		for(int i = startIndex+1;i<s.length();i++) {
			char currentChar = s.charAt(i);
			if(currentChar == startingChar) count++;
			if(currentChar == endingChar) count--;
			
			if(count == 0 && result == -1) {
				result = i;
			}
		}
		if(result == -1) throw new Exception("missing bracket: '"+endingChar+"' try "+s+endingChar);
		if(count != 0) {
			String rewrite = s.substring(0,result)+s.substring(result+1,s.length());
			throw new Exception("too many brackets: '"+endingChar+"' try "+rewrite);
		}
		return result;
	}
	
	static ArrayList<String> generateTokens(String string) throws Exception{//splits up a string into its relevant subsections and removes parentheses	
		ArrayList<String> tokens = new ArrayList<String>();
		//split apart
		String currentToken = "";
		for(int i = 0;i<string.length();i++) {
			char currentChar = string.charAt(i);
			if(isBasicOperator(currentChar)) {
				if(!currentToken.isEmpty()) tokens.add(currentToken);
				currentToken = "";
				tokens.add(Character.toString(currentChar));
			}else if(isBracket(currentChar)) {
				if(!currentToken.isEmpty()) tokens.add(currentToken);
				currentToken = "";
				int endIndex = indexOfMatchingBracket(string,i);
				tokens.add( string.substring(i, endIndex+1 ) );
				i=endIndex;
			}else {
				currentToken+=currentChar;
			}
		}
		if(!currentToken.isEmpty()) tokens.add(currentToken);
		//combine multi-token operators
		makeMultiSymbolOperators(tokens);
		
		/*
		for(String s:tokens) {
			System.out.print(s);
			System.out.print(" ");
		}
		System.out.println();
		*/
		
		return tokens;
	}
}
