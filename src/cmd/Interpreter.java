package cmd;

import java.math.BigInteger;
import java.util.ArrayList;
import math.algebra.*;
import math.booleanAlgebra.*;

public class Interpreter {
	
	static Container ans = null;
	
	public static Container stringToContainer(String text) {
		if(text.length()>0)	return new Token(text).convert();
		else return new IntC(0);
	}
	
	public static BoolContainer stringToBoolContainer(String text) {
		if(text.length()>0)	return new Token(text).convertBool();
		else return new BoolState(false);
	}
	
	private static class Token{
		
		ArrayList<Token> tokens = new ArrayList<Token>();
		String text;
		boolean surounded;
		boolean basicOp = false;
		
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
					temp.text = temp.text.replaceAll(",","");
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
				if(c == '(' || c == '[') currentLevel++;
				level[i] = currentLevel;
				if(c == ')' || c == ']') currentLevel--;
			}
			int indexOfLastFlip = 0;
			currentLevel = level[0];
			for(int i = 0;i<text.length();i++) {
				if((level[i]==0 && currentLevel!=0)||(level[i]!=0 && currentLevel==0)) {
					if(currentLevel != 0) tokens.add(new Token(text.substring(indexOfLastFlip+1, i-1),true));
					else tokens.add(new Token(text.substring(indexOfLastFlip, i),false));
					indexOfLastFlip = i;
				}
				if((text.charAt(i)=='+'||text.charAt(i)=='*'||text.charAt(i)=='^'||text.charAt(i)=='/' || text.charAt(i)=='-' || text.charAt(i)=='!' || text.charAt(i)=='|' || text.charAt(i)=='&' || text.charAt(i)==',' ) && level[i]==0) {
					tokens.add(new Token(text.substring(indexOfLastFlip, i),false));
					if(text.charAt(i)=='+') tokens.add(new Token("sum#",false,true));
					else if(text.charAt(i)=='*') tokens.add(new Token("prod#",false,true));
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
