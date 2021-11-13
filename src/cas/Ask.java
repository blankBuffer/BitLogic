package cas;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;

import graphics.Plot;

public class Ask extends QuickMath{
	
	static final boolean DEBUG = false;
	
	static ArrayList<Question> questions = new ArrayList<Question>();
	static class Question{
		String[] requiredKeys;
		Expr def = null;
		int wordLimit;
		Question(String[] keys,String def,int wordLimit){
			requiredKeys = keys;
			this.def = var(def);
			this.wordLimit = wordLimit;
		}
		Question(String[] keys,Expr def,int wordLimit){
			requiredKeys = keys;
			this.def = def;
			this.wordLimit = wordLimit;
		}
		
		public Expr result(ArrayList<String> tokens) {
			if(tokens.size()>wordLimit) return null;
			for(String key:requiredKeys) {
				if(!tokens.contains(key)) return null;
			}
			return def;
		}
	}
	
	static boolean isExpr(String s) {
		if(s.length() == 1) return true;
		
		return Interpreter.containsOperators(s) || Character.isDigit(s.charAt(0));
	}
	
	static ArrayList<Integer> indexOfExpressions(ArrayList<String> tokens) {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		for(int i = 0;i<tokens.size();i++) {
			if(tokens.get(i).isBlank()) continue;
			String s = tokens.get(i);
			if(isExpr(s)) {
				indexes.add(i);
			}
		}
		
		return indexes;
	}
	static HashMap<String, String> replacement = new HashMap<String,String>();
	static ArrayList<String> ordinals = new ArrayList<String>();
	static ArrayList<String> bannedComboWords = new ArrayList<String>();//three dollar -> 3*dollar is fine but 3 from is not 3*from
	static ArrayList<String> uselessWords = new ArrayList<String>();
	static ArrayList<String> functionNames = new ArrayList<String>();
	
	static boolean initiated = false;
	static void init() {
		replacement.put("plus", "+");
		replacement.put("gives", "+");
		replacement.put("gave", "+");
		replacement.put("give", "+");
		
		replacement.put("minus", "-");
		replacement.put("takes", "-");
		replacement.put("took", "-");
		replacement.put("takes", "-");
		replacement.put("take", "-");
		
		replacement.put("times", "*");
		replacement.put("multiplied", "*");
		replacement.put("over", "/");
		replacement.put("divided", "/");
		replacement.put("cents", "dollar/100");
		replacement.put("power", "^");
		replacement.put("equals", "=");
		replacement.put("equal", "=");
		
		replacement.put("cuberoot","cbrt");
		replacement.put("squareroot","sqrt");
		replacement.put("sine","sin");
		replacement.put("tangent","tan");
		replacement.put("cosine","cos");
		replacement.put("arcsine","asin");
		replacement.put("arccosine","acos");
		replacement.put("arctangent","atan");
		replacement.put("logarithm","ln");
		replacement.put("log","ln");
		
		replacement.put("one", "1");
		replacement.put("two", "2");
		replacement.put("three", "3");
		replacement.put("four", "4");
		replacement.put("five", "5");
		replacement.put("six", "6");
		replacement.put("seven", "7");
		replacement.put("eight", "8");
		replacement.put("nine", "9");
		replacement.put("zero", "0");
		replacement.put("ten", "10");
		
		replacement.put("derivative", "diff");
		replacement.put("differentiate", "diff");
		replacement.put("terms", "respect");
		
		//solve
		replacement.put("solution","solve");
		replacement.put("roots","solve");
		replacement.put("root","solve");
		
		
		replacement.put("integral", "integrate");
		replacement.put("approximate", "approx");
		
		ordinals.add("zeroth");
		ordinals.add("first");
		ordinals.add("second");
		ordinals.add("third");
		ordinals.add("fourth");
		ordinals.add("fifth");
		ordinals.add("sixth");
		ordinals.add("seventh");
		ordinals.add("eighth");
		ordinals.add("ninth");
		ordinals.add("tenth");
		
		for(int i = 0;i<=10;i++) {
			String ordinal = ordinals.get(i);
			replacement.put(ordinal, Integer.toString(i));
		}
		
		
		replacement.put("1st", "1");
		replacement.put("2nd", "2");
		replacement.put("3rd", "3");
		replacement.put("4th", "4");
		replacement.put("5th", "5");
		replacement.put("6th", "6");
		replacement.put("7th", "7");
		replacement.put("8th", "8");
		replacement.put("9th", "9");
		replacement.put("0th", "0");
		replacement.put("10th", "10");
		
		uselessWords.add("by");
		uselessWords.add("in");
		uselessWords.add("the");
		uselessWords.add("away");
		uselessWords.add("will");
		uselessWords.add("has");
		uselessWords.add("with");
		
		//funny
		
		questions.add(new Question(new String[] {"meaning","life"},"that's a stupid question, you make the rules",10));
		questions.add(new Question(new String[] {"god"},"Ben Currie is my creator, so he is God",10));
		
		String bestSubjectResponse = "math is the best, and anyone who disagrees is mad";
		questions.add(new Question(new String[] {"favorite","subject"},bestSubjectResponse,10));
		questions.add(new Question(new String[] {"best","subject"},bestSubjectResponse,10));
		
		//basic questions
		
		questions.add(new Question(new String[] {"help"},"Supported Math functions\n"
				+ "Sin,Cos,Tan\n"
				+ "diff(#expr,#var) for derivative\n"
				+ "integrate(#expr,#var) for anti-derivatuve\n"
				+ "integrateOver(#left-bound,#right-bound,#expr,#var)\n"
				+ "solve(#eq,#var) for solving an equation\n"
				+ "distr(#expr) distributes an expression\n"
				+ "factor(#expr) to factor an expression\n"
				+ "approx(#expr,[#variable-defs-list]) to approximate expression",7));
		
		//physics
		questions.add(new Question(new String[] {"centripetal","acceleration"},div(pow(var("v"),num(2)),var("r")),6));
		questions.add(new Question(new String[] {"rotational","kinetic","energy"},div(prod(var("I"),pow(var("w"),num(2))),num(2)),6));
		questions.add(new Question(new String[] {"kinetic","energy"},div(prod(var("m"),pow(var("v"),num(2))),num(2)),6));
		questions.add(new Question(new String[] {"potential","energy"},sum(prod(var("m"),var("g"),var("h")),div(prod(var("k"),pow(var("x"),num(2))),num(2))),6));
		questions.add(new Question(new String[] {"momentum"},prod(var("m"),var("v")),6));
		
		Expr standardMomentOfInertia = prod(var("m"),pow(var("r"),num(2)));
		questions.add(new Question(new String[] {"moment","inertia","point"},standardMomentOfInertia,8));
		questions.add(new Question(new String[] {"moment","inertia","ring"},standardMomentOfInertia,8));
		questions.add(new Question(new String[] {"moment","inertia","hollow","cylinder"},standardMomentOfInertia,10));
		questions.add(new Question(new String[] {"moment","inertia","cylinder"},div(standardMomentOfInertia,num(2)),10));
		questions.add(new Question(new String[] {"moment","inertia","disk"},div(standardMomentOfInertia,num(2)),10));
		questions.add(new Question(new String[] {"moment","inertia","hollow","sphere"},div(prod(num(2),var("m"),pow(var("r"),num(2))),num("3")),10));
		questions.add(new Question(new String[] {"moment","inertia","sphere"},div(prod(num(2),var("m"),pow(var("r"),num(2))),num("5")),10));
		
		
		//definitions
		
		questions.add(new Question(new String[] {"number"},"It is a labeling system to abstractly quantify things",5));
		
		//geometric definitions
		questions.add(new Question(new String[] {"polygon"},"a 2d shape that is closed and made up of a finite number of sides and verticies",5));
		questions.add(new Question(new String[] {"triangle"},"a polygon with three sides",5));
		questions.add(new Question(new String[] {"square"},"a polygon with four sides",5));
		questions.add(new Question(new String[] {"pentagon"},"a polygon with five sides",5));
		questions.add(new Question(new String[] {"hexagon"},"a polygon with six sides",5));
		
		bannedComboWords.add("from");
		bannedComboWords.add("to");
		bannedComboWords.add("and");
		bannedComboWords.add("with");
		
		functionNames.add("sin");
		functionNames.add("cos");
		functionNames.add("tan");
		functionNames.add("asin");
		functionNames.add("acos");
		functionNames.add("atan");
		
		functionNames.add("sqrt");
		functionNames.add("cbrt");
		functionNames.add("ln");
		
		functionNames.add("approx");
		functionNames.add("mathML");
		
		initiated = true;
	}
	
	static String numeric(String s) {
		String out = replacement.get(s);
		if(Character.isDigit(s.charAt(0))) {
			return s;
		}else if(out != null && Character.isDigit(out.charAt(0))) {
			return out;
		}else {
			return null;
		}
	}
	
	static boolean isNumeric(String s) {
		String out = replacement.get(s);
		if(Character.isDigit(s.charAt(0))) {
			return true;
		}else if(out != null) {
			return Character.isDigit(out.charAt(0));
		}else {
			return false;
		}
	}
	
	static int indexOfExpr(int startSearchAtIndex,int limit,ArrayList<String> tokens) {
		for(int i = startSearchAtIndex;i<startSearchAtIndex+limit && i<tokens.size();i++) {
			if(isExpr(tokens.get(i))) {
				return i;
			}
		}
		return -1;
	}
	
	static void reformulate(ArrayList<String> tokens) {
		//replace operator words with operators, 5 plus 2 -> 5 + 2
		for(int i = 0;i<tokens.size();i++) {
			String s = tokens.get(i);
			String repl = replacement.get(s);
			if(repl != null) {
				tokens.set(i, repl);
			}
		}
		
		//special cases
		for(int i = tokens.size()-1;i>=0;i--) {
			if(tokens.get(i).equals("squared")) {
				tokens.set(i-1, tokens.get(i-1)+"^2");
				tokens.remove(i);
			}else if(tokens.get(i).equals("cubed")) {
				tokens.set(i-1, tokens.get(i-1)+"^3");
				tokens.remove(i);
			}else if(functionNames.contains(tokens.get(i))) {
				int indexOfExpr = indexOfExpr(i,3,tokens);
				tokens.set(i, tokens.get(i)+"("+tokens.get(indexOfExpr)+")");
				tokens.remove(indexOfExpr);
			}else if(tokens.get(i).equals("diff") || tokens.get(i).equals("integrate") || tokens.get(i).equals("solve")) {
				
				int indexOfExpr = indexOfExpr(i,3,tokens);
				if(indexOfExpr != -1) {
					
					String v = "x";
					
					String min = null,max = null;
					
					
					for(int j = i;j<i+10&&j<tokens.size()-3;j++) {
						if(tokens.get(j).equals("from")) {
							min = tokens.get(j+1);
							max = tokens.get(j+3);
							
							tokens.remove(j+3);
							tokens.remove(j+1);
							break;
						}
					}
					
					
					for(int j = i;j<i+10&&j<tokens.size()-2;j++) {
						if(tokens.get(j).equals("respect")) {
							
							v = tokens.get(j+2);
							tokens.remove(j+2);
							tokens.remove(j);
							break;
						}
					}
					
					String ex = tokens.get(indexOfExpr);
					if(tokens.get(i).equals("solve")) {
						if(!ex.contains("=")) {
							ex+="=0";
						}
					}
					
					if(tokens.get(i).equals("integrate") && min != null) {
						tokens.set(indexOfExpr,"integrateOver("+min+","+max+","+ex+","+v+")");
					}else tokens.set(indexOfExpr,tokens.get(i)+"("+ex+","+v+")");
					
					tokens.remove(i);
					
				}
				
				
			}else if( i<tokens.size()-1 && isNumeric(tokens.get(i))) {//fractional handling "one third" -> "1/3"
				String currentToken = tokens.get(i);
				String nextToken = tokens.get(i+1);
				if(nextToken.charAt(nextToken.length()-1)=='s') {
					nextToken = nextToken.substring(0,nextToken.length()-1);
				}
				if(ordinals.contains(nextToken)) {
					String repl = numeric(currentToken)+"/"+replacement.get(nextToken);
					tokens.remove(i+1);
					tokens.set(i, repl);
				}
			}
		}
		
		//remove duplicate operators
		
		for(int i = 1;i<tokens.size();i++) {
			if(tokens.get(i).equals(tokens.get(i-1))) {
				tokens.remove(i);
				i--;
			}
		}
		
		
		//combine with variable, example 300 dollars -> 300*dollar
		for(int i = 0;i<tokens.size()-1;i++) {
			if(Character.isDigit(tokens.get(i).charAt(0))) {
				if(!Interpreter.isOperator(tokens.get(i+1)) && !Character.isDigit(tokens.get(i+1).charAt(0)) && !bannedComboWords.contains(tokens.get(i+1))) {
					String stripped = tokens.get(i+1);
					if(stripped.length() > 1 && stripped.charAt(stripped.length()-1) == 's') {
						stripped = stripped.substring(0, stripped.length()-1);
					}
					tokens.set(i, tokens.get(i)+"*"+stripped);
					tokens.remove(i+1);
				}
			}
		}
		
		//combine with operators what,is,3,+,4 -> what,is,3+4
		
		for(int i = 1;i<tokens.size()-1;i++) {
			String s = tokens.get(i);
			if(Interpreter.isOperator(s)) {
				if(tokens.get(i-1).equals("to")) {//example "2 to power 3"
					tokens.remove(i-1);
					i--;
				}
				tokens.set(i-1,tokens.get(i-1)+tokens.get(i)+tokens.get(i+1));
				tokens.remove(i+1);
				tokens.remove(i);
				i--;
			}else if(s.equals("and") && tokens.get(i-1).contains("dollar")) {//case of two dollars and two cents, and is a plus in this context
				tokens.set(i-1, tokens.get(i-1)+"+"+tokens.get(i+1) );
				tokens.remove(i+1);
				tokens.remove(i);
				i--;
			}
			
		}
		
		if(Interpreter.isOperator(tokens.get(0))) tokens.remove(0);
		if(Interpreter.isOperator(tokens.get(tokens.size()-1))) tokens.remove(tokens.size()-1);
	}
	
	static void removeUselessWords(ArrayList<String> tokens) {
		for(int i = 0;i<tokens.size();i++) {
			if(uselessWords.contains(tokens.get(i))) {
				tokens.remove(i);
				i--;
			}
		}
		//remove commas after tokens
		for(int i = 0;i<tokens.size();i++) {
			String s = tokens.get(i);
			if(s.charAt(s.length()-1) == ','){
				s = s.substring(0, s.length()-1);
			}
			tokens.set(i, s);
		}
		
		
	}
	
	public static Expr ask(String question,Defs defs,Settings settings) {
		
		boolean endsInQuestionMark = question.charAt(question.length()-1) == '?';
		
		if(endsInQuestionMark) question = question.substring(0, question.length()-1);
		
		question = question.replace("to the", "^");//example , 2 to the 3
		question = question.replace("square root", "sqrt");
		question = question.replace("cube root", "cbrt");
		
		if(!initiated) init();
		
		String[] tokensArray = question.split(" ");
		ArrayList<String> tokens = new ArrayList<String>();
		for(String s:tokensArray) {
			tokens.add(s);
		}
		//
		
		removeUselessWords(tokens);
		
		
		reformulate(tokens);
		
		if(tokens.get(tokens.size()-1).equals("test")) {
			return var("working!");
		}
		
		if(DEBUG) System.out.println(tokens);
		
		ArrayList<Integer> indexes = indexOfExpressions(tokens);
		
		//rpn like english
		
		if(tokens.contains("add") || tokens.contains("sum")) {
			Sum out = new Sum();
			for (int i:indexes) {
				Expr toBeAdded = Interpreter.createExpr(tokens.get(i),defs,settings);
				if(toBeAdded instanceof ExprList) {
					for (int j = 0;j<toBeAdded.size();j++) {
						out.add(toBeAdded.get(j));
					}
				}else {
					out.add(toBeAdded);
				}
			}
			return out;
		}
		
		if(tokens.contains("multiply") || tokens.contains("product")) {
			Prod out = new Prod();
			for (int i:indexes) {
				Expr toBeAdded = Interpreter.createExpr(tokens.get(i),defs,settings);
				if(toBeAdded instanceof ExprList) {
					for (int j = 0;j<toBeAdded.size();j++) {
						out.add(toBeAdded.get(j));
					}
				}else {
					out.add(toBeAdded);
				}
			}
			return out;
		}
		
		if(tokens.contains("plot")) {
			Expr exprToPlot = Interpreter.createExpr(tokens.get(indexes.get(0)),defs,settings);
			Plot.PlotWindowParams windowParams = null;
			
			if(indexes.size() == 2) {
				ExprList params = (ExprList)Interpreter.createExpr(tokens.get(indexes.get(1)),defs,settings);
				double xMin = params.get(0).convertToFloat(null).real;
				double xMax = params.get(1).convertToFloat(null).real;
				double yMin = params.get(2).convertToFloat(null).real;
				double yMax = params.get(3).convertToFloat(null).real;
				windowParams = new Plot.PlotWindowParams(xMin,xMax , yMin, yMax);
				
			}else {
				windowParams = new Plot.PlotWindowParams();
			}
			
			Dimension imageSize = new Dimension(400,400);
			
			
			return new ObjectExpr(Plot.renderGraph(exprToPlot, windowParams, imageSize));
			
		}
		//
		
		if(tokens.contains("roll")) {
			return num((int)Math.floor(Math.random()*6.0+1));
		}
		
		if(tokens.contains("about") || tokens.contains("creator")) {
			return var("Benjamin Currie @2021");
		}
		
		for(Question q:questions) {
			Expr res = q.result(tokens);
			if(res != null) {
				return res;
			}
		}
		
		if(tokens.size() == 1) {
			return Interpreter.createExpr(tokens.get(0),defs,settings);
		}
		
		//last resort
		if(indexes.size() == 1) {
			return Interpreter.createExpr(tokens.get(indexes.get(0)),defs,settings);
		}
		if(tokens.contains("why")) {
			return var("I don't know, 'why' questions are not my thing");
		}
		return var("I don't know");
	}
}
