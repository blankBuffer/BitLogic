package cas;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;

import graphics.Plot;

public class Ask extends QuickMath{
	
	static final boolean DEBUG = true;
	static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm:ss a MM/dd/yyyy");
	
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
		
		Expr getResponse(@SuppressWarnings("unused") ArrayList<String> tokens){//can be override to get custom response
			return def;
		}
		
		public Expr hasResult(ArrayList<String> tokens) {
			if(tokens.size()>wordLimit) return null;
			int min = 0;
			outer:for(String key:requiredKeys) {//tokens need to have same order as required keys
				
				for(int i = min;i<tokens.size();i++){
					if(tokens.get(i).equals(key)){
						min = i;
						continue outer;
					}
				}
				
				return null;
			}
			return getResponse(tokens);
		}
	}
	
	static ArrayList<Integer> indexOfExpressions(ArrayList<String> tokens) {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		for(int i = 0;i<tokens.size();i++) {
			if(tokens.get(i).isBlank()) continue;
			String s = tokens.get(i);
			if(Interpreter.isProbablyExpr(s)) {
				indexes.add(i);
			}
		}
		
		return indexes;
	}
	static HashMap<String, String> replacement = new HashMap<String,String>();
	static HashMap<String, String> phraseReplacement = new HashMap<String, String>();
	static Set<String> phraseReplacementKeys = phraseReplacement.keySet();
	static ArrayList<String> functionNames = new ArrayList<String>();
	
	static {
		replacement.put("plus", "+");
		replacement.put("gives", "+");
		replacement.put("gave", "+");
		replacement.put("give", "+");
		
		replacement.put("minus", "-");
		replacement.put("takes", "-");
		replacement.put("took", "-");
		replacement.put("takes", "-");
		replacement.put("take", "-");
		
		replacement.put("squared","^2");
		replacement.put("cubed","^3");
		
		replacement.put("times", "*");
		replacement.put("multiplied", "*");
		replacement.put("over", "/");
		replacement.put("per", "/");
		replacement.put("divided", "/");
		replacement.put("cents", "dollar/100");
		replacement.put("power", "^");
		replacement.put("equals", "=");
		replacement.put("equal", "=");
		replacement.put("factorial", "!");
		
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
		
		
		phraseReplacement.put("to the", "^");//example , 2 to the 3
		phraseReplacement.put("square root", "sqrt");
		phraseReplacement.put("cube root", "cbrt");
		
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
		
		questions.add(new Question(new String[] {"earth","acceleration"},Double.toString(Unit.EARTH_GRAVITY),8));
		questions.add(new Question(new String[] {"acceleration","earth"},Double.toString(Unit.EARTH_GRAVITY),8));
		
		questions.add(new Question(new String[] {"time"},nullExpr,7){
			@Override
			Expr getResponse(ArrayList<String> tokens) {
				int offset = TimeZone.getDefault().getRawOffset()/(1000);
				for(String token:tokens){
					int offsetHours = 0,offsetMinutes = 0;
					boolean set = false;
					
					if(token.matches("GMT[+-][0-9]{2}:[0-9]{2}")){
						offsetHours = Integer.valueOf(token.substring(3,6));
						offsetMinutes = Integer.valueOf(token.substring(7,9));
						set = true;
					}else if(token.matches("GMT[+-][0-9]{1}:[0-9]{2}")){
						offsetHours = Integer.valueOf(token.substring(3,5));
						offsetMinutes = Integer.valueOf(token.substring(6,8));
						set = true;
					}else if(token.matches("GMT[+-][0-9]+")){
						offsetHours = Integer.valueOf(token.substring(3));
						set = true;
					}
					if(set){
						int negative = offsetHours < 0 ? -1 : 1;
						offset = (Math.abs(offsetHours)*60*60+offsetMinutes*60)*negative;
						break;
					}
				}
				Date date = new Date(); //; java.util.Date object
				Instant instant = date.toInstant();
				ZonedDateTime zonedDateTime = instant.atZone(ZoneOffset.ofTotalSeconds(offset));
				
				DecimalFormat df = new DecimalFormat("00");
				String offsetStr = (offset>=0 ? "+":"")+ df.format(offset/(60*60))+":"+df.format(Math.abs((offset/60)%60));
				return var(timeFormat.format(zonedDateTime)+" GMT"+offsetStr);
			}
		});
		//randomness
		questions.add(new Question(new String[] {"random","from"},nullExpr,10){
			@Override
			Expr getResponse(ArrayList<String> tokens) {
				int indexOfMin = indexOfExpr(0,tokens.size(),tokens);
				int indexOfMax = indexOfExpr(indexOfMin+1,tokens.size(),tokens);
				if(indexOfMin != -1 && indexOfMax != -1){
					long min = Long.parseLong(tokens.get(indexOfMin));
					long max = Long.parseLong(tokens.get(indexOfMax));
					long range = max-min;
					
					return num((int)Math.floor(Math.random()*(range+1)+min));
				}
				return null;
			}
		});
		questions.add(new Question(new String[] {"flip","coin"},nullExpr,5){
			@Override
			Expr getResponse(ArrayList<String> tokens) {
				String ans = Math.random()>0.5?"heads":"tails";
				return var(ans);
			}
		});
		questions.add(new Question(new String[] {"roll"},nullExpr,7){
			@Override
			Expr getResponse(ArrayList<String> tokens) {
				int indexOfExpr = indexOfExpr(0,tokens.size(),tokens);
				if(indexOfExpr != -1){
					String sidesQuantity = tokens.get(indexOfExpr);
					sidesQuantity = sidesQuantity.substring(0,sidesQuantity.indexOf('*'));
					long sides = Long.parseLong(sidesQuantity);
					
					return num((int)Math.floor(Math.random()*sides+1.0));
				}
				return num((int)Math.floor(Math.random()*6+1.0));
			}
		});
		//definitions
		
		questions.add(new Question(new String[] {"number"},"It is a labeling system to abstractly quantify things",5));
		
		//geometric definitions
		questions.add(new Question(new String[] {"polygon"},"a 2d shape that is closed and made up of a finite number of sides and verticies",5));
		questions.add(new Question(new String[] {"triangle"},"a polygon with three sides",5));
		questions.add(new Question(new String[] {"square"},"a polygon with four sides",5));
		questions.add(new Question(new String[] {"pentagon"},"a polygon with five sides",5));
		questions.add(new Question(new String[] {"hexagon"},"a polygon with six sides",5));
		
		functionNames.add("sin");
		functionNames.add("cos");
		functionNames.add("tan");
		functionNames.add("asin");
		functionNames.add("acos");
		functionNames.add("atan");
		functionNames.add("sqrt");
		functionNames.add("cbrt");
		functionNames.add("ln");
		functionNames.add("gamma");
		functionNames.add("approx");
		functionNames.add("mathML");
		
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
			if(Interpreter.isProbablyExpr(tokens.get(i))) {
				return i;
			}
		}
		return -1;
	}
	
	static void applyWordReplacement(ArrayList<String> tokens){
		for(int i = 0;i<tokens.size();i++) {
			String s = tokens.get(i);
			String repl = replacement.get(s);
			if(repl != null) {
				tokens.set(i, repl);
			}
		}
	}
	
	static void removeDuplicateOperators(ArrayList<String> tokens){
		for(int i = 1;i<tokens.size();i++) {
			if(tokens.get(i).equals(tokens.get(i-1))) {
				tokens.remove(i);
				i--;
			}
		}
	}
	
	static void combineTrailingOperatorTokens(ArrayList<String> tokens){//'2^ 3' -> '2^3'
		for(int i = 0;i<tokens.size()-1;i++) {
			String s = tokens.get(i);
			boolean functionalForm = tokens.get(i+1).charAt(0) == '(';
			char leftEndChar = s.charAt(s.length()-1);
			boolean leftHasOperator = leftEndChar != ')' && Interpreter.isOperator(String.valueOf(leftEndChar));
			char rightStartChar = tokens.get(i+1).charAt(0);
			boolean rightHasOperator = Interpreter.isOperator(String.valueOf(rightStartChar)) && rightStartChar != '[';
			
			if( (leftHasOperator || rightHasOperator)&&!functionalForm || functionalForm&&functionNames.contains(s) ) {
				tokens.set(i,tokens.get(i)+tokens.get(i+1));
				tokens.remove(i+1);
				i--;
			}
			
		}
	}
	
	static void applyFunctionKeywords(ArrayList<String> tokens){
		for(int i = tokens.size()-1;i>=0;i--) {
			if(functionNames.contains(tokens.get(i))) {//function reading
				int indexOfExpr = i+1;
				if(tokens.get(i+1).equals("of")){
					tokens.remove(i+1);
				}
				tokens.set(i, tokens.get(i)+"("+tokens.get(indexOfExpr)+")");
				tokens.remove(indexOfExpr);
			}
		}
	}
	
	static ArrayList<String> bannedNumberNounPairs = new ArrayList<String>();//three dollar -> 3*dollar is fine but 3 from is not 3*from
	static void numberNounPair(ArrayList<String> tokens){//four apples -> 4*apple
		
		if(bannedNumberNounPairs.size() == 0){//'3 dollars' -> 3*dollars but '3 to' is not 3*to
			bannedNumberNounPairs.add("from");
			bannedNumberNounPairs.add("to");
			bannedNumberNounPairs.add("and");
			bannedNumberNounPairs.add("with");
			bannedNumberNounPairs.add("with");
			bannedNumberNounPairs.add("respect");
		}
		
		for(int i = 0;i<tokens.size()-1;i++) {
			if(Character.isDigit(tokens.get(i).charAt(0))) {
				if(!Interpreter.isOperator(tokens.get(i+1)) && !bannedNumberNounPairs.contains(tokens.get(i+1))) {
					String stripped = tokens.get(i+1);
					if(!Interpreter.isProbablyExpr(tokens.get(i+1)) && stripped.length() > 1 && stripped.charAt(stripped.length()-1) == 's') {
						stripped = stripped.substring(0, stripped.length()-1);//strips the 's' apples -> apple
					}
					tokens.set(i, tokens.get(i)+"*"+stripped);
					tokens.remove(i+1);
				}
			}
		}
	}
	
	static HashMap<String,String> ordinalReplacement = new HashMap<String,String>();
	static void fractionalReading(ArrayList<String> tokens){
		if(ordinalReplacement.size() == 0){
			ordinalReplacement.put("zeroth","0");
			ordinalReplacement.put("whole","1");
			ordinalReplacement.put("halve","2");
			ordinalReplacement.put("half","2");
			ordinalReplacement.put("third","3");
			ordinalReplacement.put("fourth","4");
			ordinalReplacement.put("fifth","5");
			ordinalReplacement.put("sixth","6");
			ordinalReplacement.put("seventh","7");
			ordinalReplacement.put("eighth","8");
			ordinalReplacement.put("ninth","9");
			ordinalReplacement.put("tenth","10");	
		}
		
		for(int i = 1;i<tokens.size();i++){
			//two thirds
			String token = tokens.get(i);
			if(token.charAt(token.length()-1) == 's'){
				token = tokens.get(i).substring(0, tokens.get(i).length()-1);//remove trailing s. thirds -> third
			}
			if(ordinalReplacement.containsKey(token)){
				if(tokens.get(i-1).equals("a")) tokens.set(i-1, "1");
				if(Interpreter.isProbablyExpr(tokens.get(i-1))){
					String numberVersion = ordinalReplacement.get(token);
					tokens.set(i-1, tokens.get(i-1)+"/"+numberVersion);
					tokens.remove(i);
					i--;
				}
			}
		}
	}
	
	static void specialFunctions(ArrayList<String> tokens){
		
		//special cases
		for(int i = tokens.size()-1;i>=0;i--) {
			if(tokens.get(i).equals("diff") || tokens.get(i).equals("integrate") || tokens.get(i).equals("solve")) {
				
				int indexOfExpr = indexOfExpr(i,3,tokens);
				if(indexOfExpr != -1) {
					
					String v = "x";
					
					String min = null,max = null;
					
					
					for(int j = i;j<i+10&&j<tokens.size()-3;j++) {
						if(tokens.get(j).equals("from")) {
							min = tokens.get(j+1);
							max = tokens.get(j+3);
							
							for(int k = 3;k>=0;k--){
								tokens.remove(j+k);
							}
							break;
						}
					}
					
					
					for(int j = i;j<i+10&&j<tokens.size()-2;j++) {//find name of variable to be used
						if(tokens.get(j).equals("respect")) {
							
							v = tokens.get(j+2);
							
							for(int k = 2;k>=0;k--){
								tokens.remove(j+k);
							}
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
				
				
			}
		}
		
	}
	
	static void unitReading(ArrayList<String> tokens){
		for(int i = 0;i<tokens.size();i++){
			String token = tokens.get(i);
			if(token.contains("*")){
				String[] parts = token.split("\\*");
				if(parts.length == 2 && (Unit.unitNames.contains(parts[1]) || token.contains("degree"))){
					try {
						String fromUnit = parts[1];
						if(fromUnit.equals("degree")){
							fromUnit = tokens.get(i+1);
						}
						String toUnit = "";
						
						for(int j = i+2;j<tokens.size();j++){
							if(Unit.unitNames.contains(tokens.get(j))){
								toUnit = tokens.get(j);
							}
						}
						tokens.set(i, "conv("+parts[0]+","+fromUnit+","+toUnit+")");
						
						tokens.remove(i+2);
						tokens.remove(i+1);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	static void reformulate(ArrayList<String> tokens) {
		
		applyWordReplacement(tokens);
		removeDuplicateOperators(tokens);
		applyFunctionKeywords(tokens);
		fractionalReading(tokens);
		numberNounPair(tokens);
		combineTrailingOperatorTokens(tokens);
		specialFunctions(tokens);
		
		unitReading(tokens);
		
		//System.out.println(tokens);
		
	}
	
	static String specialPhraseReplacement(String in){
		String out = in;
		for(String key:phraseReplacementKeys){
			out = out.replace(key,phraseReplacement.get(key));
		}
		
		return out;
	}
	
	static Expr goThroughProgrammedResponses(ArrayList<String> tokens){
		for(Question q:questions) {
			Expr res = q.hasResult(tokens);
			if(res != null) {
				return res;
			}
		}
		return null;
	}
	
	public static Expr ask(String question,Defs defs,Settings settings) throws Exception {
		
		boolean endsInQuestionMark = question.charAt(question.length()-1) == '?';
		
		if(endsInQuestionMark) question = question.substring(0, question.length()-1);
		
		question = specialPhraseReplacement(question);
		
		String[] tokensArray = question.split(" ");
		ArrayList<String> tokens = new ArrayList<String>();
		for(String s:tokensArray) {
			if(!s.isEmpty()) tokens.add(s);
		}
		//
		
		reformulate(tokens);
		
		if(DEBUG) System.out.println(tokens);
		
		Expr maybeResponse = goThroughProgrammedResponses(tokens);
		if(maybeResponse != null){
			return maybeResponse;
		}
		
		if(tokens.get(tokens.size()-1).equals("test")) {
			return var("working!");
		}
		
		ArrayList<Integer> indexes = indexOfExpressions(tokens);
		
		//rpn like english
		
		if(tokens.contains("add") || tokens.contains("sum")) {
			Sum out = new Sum();
			for (int i:indexes) {
				Expr toBeAdded = Interpreter.createExprWithThrow(tokens.get(i),defs,settings);
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
				Expr toBeAdded = Interpreter.createExprWithThrow(tokens.get(i),defs,settings);
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
		
		if(tokens.contains("plot") || tokens.contains("graph")) {
			Expr exprToPlot = Interpreter.createExprWithThrow(tokens.get(indexes.get(0)),defs,settings);
			Plot.PlotWindowParams windowParams = null;
			
			if(indexes.size() == 2) {
				ExprList params = (ExprList)Interpreter.createExprWithThrow(tokens.get(indexes.get(1)),defs,settings);
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
		
		if(tokens.contains("about") || tokens.contains("creator")) {
			return var("Benjamin Currie @2021");
		}
		
		if(tokens.size() == 1) {
			return Interpreter.createExprWithThrow(tokens.get(0),defs,settings);
		}
		
		//last resort
		if(indexes.size() == 1) {
			return Interpreter.createExprWithThrow(tokens.get(indexes.get(0)),defs,settings);
		}
		if(tokens.contains("why")) {
			return var("I don't know, 'why' questions are not my thing");
		}
		return var("I don't know");
	}
}
