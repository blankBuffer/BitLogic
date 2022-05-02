package cas.lang;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;

import cas.*;
import cas.graphics.Plot;
import cas.primitive.*;

public class Ask extends QuickMath{
	
	static final boolean DEBUG = false;
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
	
	public static void loadBasicQuestions() {
		try {
			Scanner scanner = new Scanner(new File("resources/QnA.txt"));
			System.out.println("loading questions...");
			int questionsLoadedCount = 0;
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if(line.startsWith("#")) continue;
				
				int barIndex = line.indexOf('|');
				
				if(line.contains(":")) {
					int colonIndex = line.indexOf(':');
					
					String[] tokens = line.substring(0, colonIndex).split(",");
					for(int i = 0;i<tokens.length;i++) {
						tokens[i] = tokens[i].toLowerCase();
					}
					String output = line.substring(colonIndex+1, barIndex);
					output = output.replace('`', '\n');
					int wordLimit = Integer.valueOf(line.substring(barIndex+1));
					
					questions.add(new Question(tokens,output,wordLimit));
					questionsLoadedCount++;
				}else if(line.contains("\\")) {
					int slashIndex = line.indexOf('\\');
					
					String[] tokens = line.substring(0, slashIndex).split(",");
					for(int i = 0;i<tokens.length;i++) {
						tokens[i] = tokens[i].toLowerCase();
					}
					Expr output = createExpr(line.substring(slashIndex+1, barIndex));
					int wordLimit = Integer.valueOf(line.substring(barIndex+1));
					
					questions.add(new Question(tokens,output,wordLimit));
					questionsLoadedCount++;
				}
				
			}
			scanner.close();
			System.out.println("done loading "+questionsLoadedCount+" questions");
		} catch (FileNotFoundException e) {
			System.err.println("unable to load questions file");
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
	
	static {
		replacement.put("not", "~");
		replacement.put("negate", "~");
		replacement.put("or", "|");
		replacement.put("and", "&");
		
		replacement.put("plus", "+");
		replacement.put("gives", "+");
		replacement.put("gave", "+");
		replacement.put("give", "+");
		
		replacement.put("minus", "-");
		replacement.put("takes", "-");
		replacement.put("took", "-");
		replacement.put("takes", "-");
		replacement.put("take", "-");
		replacement.put("negative", "neg");
		
		replacement.put("has", "owns");
		
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
		
		replacement.put("derivative", "diff");
		replacement.put("differentiate", "diff");
		replacement.put("terms", "respect");
		replacement.put("matrix", "mat");
		
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
		
		replacement.put("graph", "plot");
		
		phraseReplacement.put("to the", "^");//example , 2 to the 3
		phraseReplacement.put("square root", "sqrt");
		phraseReplacement.put("cube root", "cbrt");
		phraseReplacement.put("absolute value", "abs");
		phraseReplacement.put("in terms", "terms");
		
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
		questions.add(new Question(new String[] {"plot"},nullExpr,7){
			@Override
			Expr getResponse(ArrayList<String> tokens) {
				Expr equation = null;
				Plot.PlotWindowParams plotWind = new Plot.PlotWindowParams();
				
				boolean _3d = false;
				int _3dTokenIndex = tokens.indexOf("3d");
				if(_3dTokenIndex != -1) {
					_3d = true;
					tokens.remove(_3dTokenIndex);
				}
				
				int indexOfExpr = indexOfExpr(0,tokens.size(),tokens);
				if(indexOfExpr != -1){
					equation = createExpr(tokens.get(indexOfExpr));
				}
				
				int windowTokenIndex = tokens.indexOf("window");
				if(windowTokenIndex != -1) {
					ExprList windowParams = (ExprList) createExpr(tokens.get(windowTokenIndex+1));
					plotWind.xMin = windowParams.get(0).convertToFloat(exprList()).real;
					plotWind.xMax = windowParams.get(1).convertToFloat(exprList()).real;
					plotWind.yMin = windowParams.get(2).convertToFloat(exprList()).real;
					plotWind.yMax = windowParams.get(3).convertToFloat(exprList()).real;
					
					if(windowParams.size()>4) {
						plotWind.zMin = windowParams.get(4).convertToFloat(exprList()).real;
						plotWind.zMax = windowParams.get(5).convertToFloat(exprList()).real;
					}
					
					if(windowParams.size()>6 ) {
						plotWind.zRot = windowParams.get(6).convertToFloat(exprList()).real;
						plotWind.xRot = windowParams.get(7).convertToFloat(exprList()).real;
					}
				}
				
				if(equation != null) {
					if(!_3d)
						return new ObjectExpr(Plot.renderGraph2D(sequence(equation), plotWind, new Dimension(400,400), new Color(64,64,64), new Color(255,255,255) ,1));
					return new ObjectExpr(Plot.renderGraph3D(sequence(equation), plotWind, new Dimension(400,400),new Color(64,64,64), new Color(255,255,255), 48));
				}
				return var("not sure what to plot?");
			}
		});
		//definitions
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
	static ArrayList<String> acceptableDupToken = new ArrayList<String>();
	static {
		acceptableDupToken.add("~");
		acceptableDupToken.add("-");
	}
	static void removeDuplicateOperators(ArrayList<String> tokens){
		for(int i = 1;i<tokens.size();i++) {
			if(acceptableDupToken.contains(tokens.get(i))) continue;
			if(tokens.get(i).equals(tokens.get(i-1))) {
				tokens.remove(i);
				i--;
			}
		}
	}
	
	static boolean oneParameterFunction(String funcName) {
		return SimpleFuncs.isFunc(funcName) && SimpleFuncs.getExpectectedParams(funcName) == 1;
	}
	
	static void combineTrailingOperatorTokens(ArrayList<String> tokens){//'2^ 3' -> '2^3'
		for(int i = 0;i<tokens.size()-1;i++) {
			String s = tokens.get(i);
			boolean functionalForm = tokens.get(i+1).charAt(0) == '(';
			char leftEndChar = s.charAt(s.length()-1);
			boolean leftHasOperator = leftEndChar != ')' && Interpreter.isOperator(String.valueOf(leftEndChar));
			char rightStartChar = tokens.get(i+1).charAt(0);
			boolean rightHasOperator = Interpreter.isOperator(String.valueOf(rightStartChar)) && rightStartChar != '[' && rightStartChar != '~';
			
			if( (leftHasOperator || rightHasOperator)&&!functionalForm || functionalForm && oneParameterFunction(s) ) {
				tokens.set(i,tokens.get(i)+tokens.get(i+1));
				tokens.remove(i+1);
				i--;
			}
			
		}
	}
	
	static void applyFunctionKeywords(ArrayList<String> tokens){
		for(int i = tokens.size()-1;i>=0;i--) {
			if(oneParameterFunction(tokens.get(i))) {//function reading
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
			//never add in because in short hand for inches
		}
		
		for(int i = 0;i<tokens.size()-1;i++) {
			if(tokens.get(i).matches("([0-9]+)|(neg\\([0-9]+\\))")) {
				if(!Interpreter.containsOperators(tokens.get(i+1)) && !bannedNumberNounPairs.contains(tokens.get(i+1))) {
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
				
				
			}else if(tokens.get(i).equals("approx")) {
				tokens.remove(i);
				int indexOfExpr = indexOfExpr(i,3,tokens);
				
				if(indexOfExpr != -1) {
					tokens.set(indexOfExpr, "approx("+tokens.get(indexOfExpr)+")");
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
					if(Interpreter.containsOperators(parts[0]) || Interpreter.containsOperators(parts[1])) return;
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
	
	static HashMap<String,String> wordToDig = new HashMap<String,String>();
	static {
		wordToDig.put("zero","0");
		wordToDig.put("one","1");
		wordToDig.put("two","2");
		wordToDig.put("three","3");
		wordToDig.put("four","4");
		wordToDig.put("five","5");
		wordToDig.put("six","6");
		wordToDig.put("seven","7");
		wordToDig.put("eight","8");
		wordToDig.put("nine","9");
		wordToDig.put("ten","10");
		wordToDig.put("eleven","11");
		wordToDig.put("twelve","12");
		wordToDig.put("thirteen","13");
		wordToDig.put("fourteen","14");
		wordToDig.put("fifteen","15");
		wordToDig.put("sixteen","16");
		wordToDig.put("seventeen","17");
		wordToDig.put("eighteen","18");
		wordToDig.put("nineteen","19");
		wordToDig.put("twenty","20");
		wordToDig.put("thirty","30");
		wordToDig.put("fourty","40");
		wordToDig.put("fifty","50");
		wordToDig.put("sixty","60");
		wordToDig.put("seventy","70");
		wordToDig.put("eighty","80");
		wordToDig.put("ninety","90");
		wordToDig.put("hundred","100");
		wordToDig.put("thousand","1000");
		wordToDig.put("million","1000000");
		wordToDig.put("billion","1000000000");
	}
	static void constructNumbers(ArrayList<String> tokens) {
		for(int i = 0;i<tokens.size();i++) {
			tokens.set(i,  wordToDig.getOrDefault(tokens.get(i), tokens.get(i)) );
		}
		for(int i = 0;i<tokens.size()-1;i++) {
			if(tokens.get(i).matches("[0-9]{2}") && tokens.get(i+1).matches("[1-9]")) {
				String newToken = tokens.get(i).charAt(0)+tokens.get(i+1);
				tokens.remove(i+1);
				tokens.set(i, newToken);
			}else if(tokens.get(i).matches("[1-9]") && tokens.get(i+1).matches("10[0]+")) {
				String newToken = tokens.get(i)+tokens.get(i+1).substring(1);
				tokens.remove(i+1);
				tokens.set(i, newToken);
			}
		}
		
		for(int i = 0;i<tokens.size()-1;i++) {
			if(tokens.get(i).matches("[1-9][0]{2}") && tokens.get(i+1).matches("[0-9]{2}")) {
				String newToken = tokens.get(i).charAt(0)+tokens.get(i+1);
				tokens.remove(i+1);
				tokens.set(i, newToken);
			}
		}
		
		for(int i = 0;i<tokens.size()-1;i++) {
			if(tokens.get(i).matches("[0-9]+") && tokens.get(i+1).matches("100[0]+")) {
				String newToken = tokens.get(i)+tokens.get(i+1).substring(1);
				tokens.remove(i+1);
				tokens.set(i, newToken);
			}
		}
		for(int i = 0;i<tokens.size()-1;i++) {
			if(tokens.get(i).matches("[0-9]+") && tokens.get(i+1).matches("[0-9]+")) {
				int sum = Integer.parseInt(tokens.get(i))+Integer.parseInt(tokens.get(i+1));
				tokens.remove(i+1);
				tokens.set(i, String.valueOf(sum));
			}
		}
		for(int i = 0;i<tokens.size()-1;i++) {
			if(tokens.get(i).equals("negative") && tokens.get(i+1).matches("[0-9]+")) {
				String newToken = "neg("+tokens.get(i+1)+")";
				tokens.remove(i+1);
				tokens.set(i, newToken);
			}
		}
	}
	
	static void nounOwnership(ArrayList<String> tokens) {
		for(int i = 0;i<tokens.size();i++) {
			String token = tokens.get(i);
			if(token.equals("owns") || token.equals("own")) {
				
				String items = "";
				int j = i+1;
				while(true) {
					items+=tokens.get(j);
					if(!(j+1 < tokens.size() && tokens.get(j+1).equals("&"))) break;
					items+="+";
					j+=2;
				}
				
				for(int k = j;k>i;k--) {
					tokens.remove(k);
				}
				
				String newToken = "owns("+tokens.get(i-1)+","+items+")";
				tokens.remove(i);
				tokens.set(i-1, newToken);
				
			}
		}
	}
	
	static void applyChooseKeyword(ArrayList<String> tokens) {
		for(int i = 0;i<tokens.size();i++) {
			if(tokens.get(i).equals("choose")) {
				tokens.set(i-1, "choose("+tokens.get(i-1)+","+tokens.get(i+1)+")");
				
				tokens.remove(i);
				tokens.remove(i);
				i--;
			}
		}
	}
	
	static void reformulate(ArrayList<String> tokens) {
		if(DEBUG){
			System.out.println("before reformulate");
			System.out.println(tokens);
		}
		constructNumbers(tokens);
		applyWordReplacement(tokens);
		removeDuplicateOperators(tokens);
		fractionalReading(tokens);
		applyChooseKeyword(tokens);
		numberNounPair(tokens);
		nounOwnership(tokens);
		combineTrailingOperatorTokens(tokens);
		applyFunctionKeywords(tokens);
		specialFunctions(tokens);
		combineTrailingOperatorTokens(tokens);
		
		unitReading(tokens);
		
		if(DEBUG){
			System.out.println("after reformulate");
			System.out.println(tokens);
		}
		
	}
	
	public static Expr ask(String question) throws Exception {
		
		if(!question.contains(" ")) return Interpreter.createExprWithThrow(question);
		
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
				Expr toBeAdded = Interpreter.createExprWithThrow(tokens.get(i));
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
				Expr toBeAdded = Interpreter.createExprWithThrow(tokens.get(i));
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
		//
		
		if(tokens.contains("about") || tokens.contains("creator")) {
			return var("Benjamin Currie @2021");
		}
		
		if(tokens.size() == 1) {
			return Interpreter.createExprWithThrow(tokens.get(0));
		}
		
		//last resort
		if(indexes.size() == 1) {
			return Interpreter.createExprWithThrow(tokens.get(indexes.get(0)));
		}
		if(tokens.contains("why")) {
			return var("I don't know, 'why' questions are not my thing");
		}
		return var("I don't know");
	}
}
