package cas.lang;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cas.*;
import cas.primitive.*;
import cas.programming.*;
import cas.matrix.*;

public class Interpreter extends Cas{
	
	public static Expr SUCCESS = var("done!");
	
	public static Expr createExpr(String string){
		try {
			ArrayList<String> tokens = generateTokens(string);
			return createExprFromTokens(tokens,0);
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Expr createExprWithThrow(String string) throws Exception{
		ArrayList<String> tokens = generateTokens(string);
		return createExprFromTokens(tokens,0);
	}
	
	static Expr createExprFromToken(String token) throws Exception {
		ArrayList<String> tokens = new ArrayList<String>();
		tokens.add(token);
		return createExprFromTokens(tokens,0);
	}
	
	static void errors(ArrayList<String> tokens) throws Exception {
		//System.out.println(tokens);
		if(tokens == null) throw new Exception("missing tokens");
		
		String initToken = tokens.get(0);
		if(initToken.equals("+") || initToken.equals("*") || initToken.equals("/") || initToken.equals("^") || initToken.equals(",")) throw new Exception("starting with invalid token");//should not start with any of these
		
		String lastToken = tokens.get(tokens.size()-1);
		if(lastToken.equals("+") || lastToken.equals("-") || lastToken.equals("*") || lastToken.equals("/") || lastToken.equals("^") || lastToken.equals(",")) throw new Exception("ending with invalid token");//should not end with any of these
		
	}
	static Pattern isOperatorPattern = Pattern.compile("[+\\-*^/,=><!;:\\~\\&\\|\\[\\]\\{\\}\\(\\)\\.\\?]");
	public static boolean isOperator(String string) {
		Matcher m = isOperatorPattern.matcher(string);
		return m.matches();
	}
	
	static Pattern isProbablyExprPattern = Pattern.compile("pi|i|e|.*[(0-9)(+\\-*^/,=><!;:\\~\\&\\|\\[\\]\\{\\}\\(\\)\\.\\?].*");
	public static boolean isProbablyExpr(String string) {
		Matcher m = isProbablyExprPattern.matcher(string);
		return m.matches();
	}
	
	static Pattern containsOperatorsPattern = Pattern.compile(".*[+\\-*/^,=><!;:\\~\\&\\|\\[\\]\\{\\}\\(\\)\\.\\?].*");
	public static boolean containsOperators(String string) {
		Matcher m = containsOperatorsPattern.matcher(string);
		return m.matches();
	}
	
	public static boolean isLeftBracket(char ch) {
		return ch == '[' || ch == '{' || ch == '(';
	}
	public static boolean isRightBracket(char ch) {
		return ch == ']' || ch == '}' || ch == ')';
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
		if(result < 0) throw new Exception("missing bracket: '"+endingChar+"' try "+s+endingChar);
		if(count > 0) {
			String rewrite = s.substring(0,result)+s.substring(result+1,s.length());
			throw new Exception("too many brackets: '"+endingChar+"' try "+rewrite);
		}
		return result;
	}
	
	static ArrayList<String> groupTokens(ArrayList<String> tokens,int startIndex,int endIndex){//end index not included
		ArrayList<String> out = new ArrayList<String>();
		for(int i = startIndex;i<endIndex;i++) out.add(tokens.get(i));
		return out;
	}
	
	static int findToken(ArrayList<String> tokens,String token,int startPoint) {
		for(int i = startPoint;i<tokens.size();i++) if(tokens.get(i).equals(token)) return i;
		return -1;
	}
	
	static ArrayList<ArrayList<String>> splitTokensIntoGroups(ArrayList<String> tokens,String splitterToken){
		ArrayList<ArrayList<String>> groups = new ArrayList<ArrayList<String>>();
		
		int indexOfLastSplitterToken = -1;
		
		while(indexOfLastSplitterToken != tokens.size()) {
			int indexOfNextSplitterToken = findToken(tokens,splitterToken,indexOfLastSplitterToken+1);
			if(indexOfNextSplitterToken == -1) indexOfNextSplitterToken = tokens.size();
			
			groups.add( groupTokens(tokens,indexOfLastSplitterToken+1,indexOfNextSplitterToken) );
			indexOfLastSplitterToken = indexOfNextSplitterToken;
		}
		
		return groups;
	}
	
	static String combineTokensIntoString(ArrayList<String> tokens,int startIndex,int endIndex) {
		String out = "";
		for(int i = startIndex;i<endIndex;i++) {
			out+=tokens.get(i);
		}
		return out;
	}
	
	public static void removeTokens(ArrayList<String> tokens,int startIndex,int endIndex) {
		for(int i = endIndex-1;i>=startIndex;i--) {
			tokens.remove(i);
		}
	}
	
	public static void combineTokens(ArrayList<String> tokens,int startIndex,int endIndex) {
		String newToken = combineTokensIntoString(tokens,startIndex,endIndex);
		removeTokens(tokens,startIndex+1,endIndex);
		tokens.set(startIndex, newToken);
	}
	
	public static void groupFloatingPointTokens(ArrayList<String> tokens) {
		if(tokens.contains(".")) {
			for(int i = 0;i<tokens.size()-1;i++){//floating point reading
				if(i+6<tokens.size()+1 && tokens.get(i).equals("-") && tokens.get(i+1).matches("([0-9])+") && tokens.get(i+2).equals(".") && tokens.get(i+3).matches("([0-9])*(e|E)") && tokens.get(i+4).equals("-") && tokens.get(i+5).matches("([0-9])+")) {
					combineTokens(tokens,i,i+6);
					tokens.add(i, "+");
				}
				if(i+5<tokens.size()+1 && tokens.get(i).matches("([0-9])+") && tokens.get(i+1).equals(".") && tokens.get(i+2).matches("([0-9])*(e|E)") && tokens.get(i+3).equals("-") && tokens.get(i+4).matches("([0-9])+")) {
					combineTokens(tokens,i,i+5);
				}
				if(i+4<tokens.size()+1 && tokens.get(i).equals("-") && tokens.get(i+1).matches("([0-9])+") && tokens.get(i+2).equals(".") && tokens.get(i+3).matches("([0-9])*(e|E)?([0-9])*")) {
					combineTokens(tokens,i,i+4);
					tokens.add(i, "+");
				}
				if(i+3<tokens.size()+1 && tokens.get(i).matches("([0-9])+") && tokens.get(i+1).equals(".") && tokens.get(i+2).matches("([0-9])*(e|E)?([0-9])*")) {
					combineTokens(tokens,i,i+3);
				}
				
				if(i+2<tokens.size()+1 && tokens.get(i).equals(".") && tokens.get(i+1).matches("([0-9])+")){
					combineTokens(tokens,i,i+2);
				}
				if(i+3<tokens.size()+1 && tokens.get(i).equals("-") && tokens.get(i+1).equals(".") && tokens.get(i+2).matches("([0-9])+")){
					combineTokens(tokens,i,i+3);
				}
			}
		}
	}
	
	private static ArrayList<String> multiSymbolOperators = new ArrayList<String>();
	static {
		multiSymbolOperators.add("->");
		multiSymbolOperators.add(":=");
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
	
	static Expr createExprFromTokens(ArrayList<String> tokens,int rec) throws Exception{
		if(tokens.size() == 0) return null;
		if(rec > 40){
			throw new Exception("recursion in expression reading");
		}
		//System.out.println("Received: "+tokens);
		errors(tokens);
		
		makeMultiSymbolOperators(tokens);
		groupFloatingPointTokens(tokens);
		
		if(tokens.size() == 1) {//reading basics
			String string = tokens.get(0);
			
			if(string.matches("[0-9]+")){
				return num(string);
			}
			if(string.matches("[\\-]?[0-9]*(([\\.][0-9]*)|([\\.][0-9]*[e|E][\\-]?[0-9]+))")) {
				return floatExpr(string);
			}
			if(!containsOperators(string)){//variable
				String lowered = string.toLowerCase();
				if(string.equals("i")) return num(0,1);
				else if(lowered.equals("true")) return bool(true);
				else if(lowered.equals("false")) return bool(false);
				else if(lowered.equals("degree")) return div(pi(),num(180));
				else{//variables
					return var(string);
				}
			}
			if( isLeftBracket(string.charAt(0)) ) {//token is in brackets
				
				String subPart = string.substring(1,string.length()-1);
				
				ArrayList<String> subTokens = generateTokens(subPart);
				Expr outExpr = createExprFromTokens(subTokens,rec+1);
				
				if(string.startsWith("(")) {
					return outExpr;
				}else if(string.startsWith("[")){
					if(outExpr instanceof Params) return Sequence.cast(outExpr);
					else if(outExpr == null) return sequence();
					return sequence(outExpr);
				}else if(string.startsWith("{")){
					if(outExpr instanceof Params) return ExprList.cast(outExpr);
					else if(outExpr == null) return exprList();
					return exprList(outExpr);
				}
				
			}
		}
		
		if(tokens.size() == 2) {//reading functions	
			String op = tokens.get(0);
			if(op.matches("[a-zA-Z]+") && !tokens.get(1).equals("!")) {
				if(op.isBlank()) throw new Exception("confusing parenthesis");
				Expr params = Params.cast( createExprFromToken(tokens.get(1)));
				
				
				Expr[] paramsArray = new Expr[params.size()];
				
				for(int i = 0;i<params.size();i++) {
					paramsArray[i] = params.get(i);
				}
				
				
				Expr f = SimpleFuncs.getFuncByName(op,paramsArray);
				return f;
			}
			
		}
		
		if(tokens.contains(";")) {
			Script scr = new Script();
			int indexOfLastComma = 0;
			
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals(";")) {
					
					ArrayList<String> tokenSet = new ArrayList<String>();
					for(int j = indexOfLastComma;j<i;j++)  tokenSet.add(tokens.get(j));
					scr.add(createExprFromTokens(tokenSet,rec+1));
					indexOfLastComma = i+1;
				}
			}
			
			return scr;
			
		}
		if(tokens.contains(":=")) {
			int indexOfAssign = tokens.indexOf(":=");
			ArrayList<String> leftSide = groupTokens(tokens,0,indexOfAssign);
			ArrayList<String> rightSide = groupTokens(tokens,indexOfAssign+1,tokens.size());
			
			return define(createExprFromTokens(leftSide,rec+1),createExprFromTokens(rightSide,rec+1));
			
		}
		if(tokens.contains(",")) {
			Params params = new Params();
			ArrayList<ArrayList<String>> tokenGroups = splitTokensIntoGroups(tokens,",");
			for(ArrayList<String> group:tokenGroups) params.add( createExprFromTokens(group,rec+1) );
			return params;
			
		}
		if(tokens.contains("->")) {
			ArrayList<ArrayList<String>> tokenGroups = splitTokensIntoGroups(tokens,"->");
			Expr leftSide = createExprFromTokens(tokenGroups.get(0),rec+1);
			Expr rightSide = createExprFromTokens(tokenGroups.get(1),rec+1);
			return becomes(leftSide,rightSide);
		}
		
		if(tokens.contains("?") && tokens.contains(":")) {//ternary case
			
			int questionMarkIndex = tokens.indexOf("?");
			ArrayList<String> toBeEvaled = groupTokens(tokens,0,questionMarkIndex);
			int colonIndex = tokens.indexOf(":");
			ArrayList<String> ifTrue = groupTokens(tokens,questionMarkIndex+1,colonIndex);
			ArrayList<String> ifFalse = groupTokens(tokens,colonIndex+1,tokens.size());
			return ternary( createExprFromTokens(toBeEvaled,rec+1),createExprFromTokens(ifTrue,rec+1),createExprFromTokens(ifFalse,rec+1) );
		}
		
		if(tokens.contains("=") || tokens.contains(">") || tokens.contains("<")) {//is equation
			
			int indexOfEq = tokens.indexOf("=");
			int indexOfGreater = tokens.indexOf(">");
			int indexOfLess = tokens.indexOf("<");
			int indexOfEquToken = Math.max(Math.max(indexOfEq, indexOfLess),indexOfGreater);
			
			char symbol = tokens.get(indexOfEquToken).charAt(0);
			Expr leftSide = createExprFromTokens(groupTokens(tokens,0,indexOfEquToken),rec+1);
			Expr rightSide = createExprFromTokens(groupTokens(tokens,indexOfEquToken+1,tokens.size()),rec+1);
			
			if(symbol == '=') return equ(leftSide,rightSide);
			if(symbol == '>') return equGreater(leftSide,rightSide);
			if(symbol == '<') return equLess(leftSide,rightSide);
			
		}
		
		if(tokens.contains("|")){
			Func or = or();
			ArrayList<ArrayList<String>> tokenGroups = splitTokensIntoGroups(tokens,"|");
			for(ArrayList<String> group:tokenGroups) or.add( createExprFromTokens(group,rec+1) );
			return or;
		}
		if(tokens.contains("&")){
			Func and = and();
			ArrayList<ArrayList<String>> tokenGroups = splitTokensIntoGroups(tokens,"&");
			for(ArrayList<String> group:tokenGroups) and.add( createExprFromTokens(group,rec+1) );
			return and;
		}
		if(tokens.get(0).equals("~")){
			tokens.remove(0);
			return not(createExprFromTokens(tokens,rec+1));
		}
		
		boolean isSum = false;
		for(int i = 0;i<tokens.size();i++) {
			String token = tokens.get(i);
			if(token.equals("+") || token.equals("-")  &&   (i==0 ? true : ! (tokens.get(i-1).equals("*")||tokens.get(i-1).equals("/")||tokens.get(i-1).equals("^")) )   ) {//avoids error of misinterpreting x*-2*5 as a sum
				isSum = true;
				break;
			}
		}
		if(isSum) {
			tokens.add("+");
			Expr sum = new Sum();
			int indexOfLastAdd = 0;
			boolean nextIsSub = false;
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals("+") || token.equals("-")) {
					if(i != 0) {
						if(tokens.get(i-1).equals("^") || tokens.get(i-1).equals("/") || tokens.get(i-1).equals("*")) continue;//avoids error created by e^-x+x where the negative is actually in the exponent same things with x+y/-2
						ArrayList<String> tokenSet = groupTokens(tokens,indexOfLastAdd,i);
						
						if(nextIsSub) {
							Expr toBeAdded = createExprFromTokens(tokenSet,rec+1);
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
						}else {
							sum.add(createExprFromTokens(tokenSet,rec+1));
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
		}
		
		if(tokens.contains(".")) {
			Dot dot = new Dot();
			ArrayList<ArrayList<String>> tokenGroups = splitTokensIntoGroups(tokens,".");
			for(ArrayList<String> group:tokenGroups) dot.add( createExprFromTokens(group,rec+1) );
			return dot;
		}
		if(tokens.contains("*") || tokens.contains("/")) {
			tokens.add("*");
			Expr numerProd = new Prod();
			Expr denomProd = new Prod();
			int indexOfLastProd = 0;
			boolean nextDiv = false;
			for(int i = 0;i<tokens.size();i++) {
				String token = tokens.get(i);
				if(token.equals("*") || token.equals("/")) {
					ArrayList<String> tokenSet = groupTokens(tokens,indexOfLastProd,i);
					
					if(nextDiv) denomProd.add(createExprFromTokens(tokenSet,rec+1));
					else numerProd.add(createExprFromTokens(tokenSet,rec+1));
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
		}
		if(tokens.contains("^")) {
			int indexOfPowToken = tokens.indexOf("^");
			Expr base = createExprFromTokens(groupTokens(tokens,0,indexOfPowToken),rec+1);
			Expr expo = createExprFromTokens(groupTokens(tokens,indexOfPowToken+1,tokens.size()),rec+1);
			
			return power(base,expo);
		}
		if(tokens.get(tokens.size()-1).equals("!")) {
			return gamma( sum(createExprFromTokens(groupTokens(tokens,0,tokens.size()-1),rec+1),num(1)) );
		}
		
		throw new Exception("unrecognized format:"+tokens);
	}
	
	
	private static char[] basicOperators = new char[] {
		'*','+','-','^','/',',','=','!',':',';','&','|','~','<','>','.','?'
	};
	private static boolean isBasicOperator(char c) {
		for(char o:basicOperators) {
			if(o == c) return true;
		}
		return false;
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
			}else if(isLeftBracket(currentChar)) {
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
		
		return tokens;
	}
}
