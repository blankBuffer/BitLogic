package cas.lang;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class ParseMachine {
	
	public static void test() {
		MetaLang.init();
		
		@SuppressWarnings("unused")
		ParseRule language = MetaLang.createParseStructureFromFile("resources/bitlogic_syntax.pm");
	}
	
	public static abstract class ParseAction{
		abstract void doAction(ParseNode parseNode);
	}
	
	static class MetaLang{
		
		static ParseRule metaLangDef = null;
		
		static HashMap<String,ParseAction> parseNodeTypeToInstruction = new HashMap<String,ParseAction>();
		
		static void init() {
			metaLangDef = new ParseRule(//meta syntax
					tokenRule("quote_sym","\\'"),
					new ParseRule(ParseRule.REMOVE_OPERATOR,"string",ruleNodeChar("'"),ruleNode(NODE_SEQUENCE),ruleNodeChar("'")),
					new ParseRule(DELETE,ruleNodeChar(" \n\t")),
					tokenRule("becomes","->"),
					new ParseRule("name",ruleNodeChar("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_")),
					new ParseRule(ParseRule.MERGE,"name",ruleNode("name"),ruleNode("name")),
					new ParseRule("rule_node",ruleNode("name"),ruleNodeChar("("),ruleNode(NODE_SEQUENCE),ruleNodeChar(")")),
					new ParseRule("data",ruleNode("name")),
					new ParseRule("data",ruleNode("string")),
					new ParseRule(ParseRule.REMOVE_OPERATOR,"rule_node_sequence",ruleNode("rule_node")),
					new ParseRule(ParseRule.MERGE,"rule_node_sequence",ruleNode("rule_node_sequence"),ruleNodeChar(","),ruleNode("rule_node_sequence")),
					new ParseRule("flags",contextRuleNodeChar(":|"),ruleNode("data")),
					new ParseRule(ParseRule.MERGE,"flags",ruleNode("flags"),ruleNodeChar("|"),ruleNode("flags")),
					new ParseRule("syntax_rule",ruleNodeChar("["),ruleNode("rule_node_sequence"),ruleNode("becomes"),ruleNode("data"),ruleNodeChar("]")),
					new ParseRule("syntax_rule",ruleNodeChar("["),ruleNode("rule_node_sequence"),ruleNode("becomes"),ruleNode("data"),ruleNodeChar(":"),ruleNode("flags"),ruleNodeChar("]")),
					
					new ParseRule(ParseRule.LOOP,
						new ParseRule("syntax_rule_sequence",ruleNode("syntax_rule"),ruleNodeChar(","),ruleNode("syntax_rule")),
						new ParseRule(ParseRule.MERGE,"syntax_rule_sequence",ruleNode("syntax_rule"),ruleNodeChar(","),ruleNode("syntax_rule_sequence")),
						new ParseRule(ParseRule.MERGE,"syntax_rule_sequence",ruleNode("syntax_rule_sequence"),ruleNodeChar(","),ruleNode("syntax_rule")),
						new ParseRule("syntax_rule",ruleNodeChar("["),ruleNode("syntax_rule_sequence"),ruleNodeChar("]")),
						new ParseRule("syntax_rule",ruleNodeChar("["),ruleNode("syntax_rule_sequence"),ruleNodeChar(":"),ruleNode("flags"),ruleNodeChar("]"))
					)
				);
			
			parseNodeTypeToInstruction.put("string", new ParseAction() {
				@Override
				public void doAction(ParseNode parseNode) {
					ParseNode param = parseNode.getNode(0);
					String out = "";
					for(int i = 0;i<param.size();i++) out += param.getNode(i).leaf_char;
					parseNode.object = out;
				}
			});
			parseNodeTypeToInstruction.put("name", new ParseAction() {

				@Override
				void doAction(ParseNode parseNode) {
					String out = "";
					for(int i = 0;i<parseNode.size();i++) out += parseNode.getNode(i).leaf_char;
					parseNode.object = out;
				}
				
			});
			parseNodeTypeToInstruction.put("rule_node", new ParseAction() {

				@Override
				void doAction(ParseNode parseNode) {
					String content = (String)(parseNode.getNode(2).getNode(0).getNode(0).object);
					String name = (String)(parseNode.getNode(0).object);
					
					if(name.equals("node")) {
						parseNode.object = ruleNode(content);
					}else if(name.equals("char")) {
						parseNode.object = ruleNodeChar(content);
					}
				}
			});
			parseNodeTypeToInstruction.put("syntax_rule", new ParseAction() {
				@Override
				void doAction(ParseNode parseNode) {
					if(parseNode.getNode(1).type.equals("syntax_rule_sequence")) {
						ParseNode srs = parseNode.getNode(1);
						
						int n_elements = (srs.size()+1)/2;
						
						ParseRule[] rules = new ParseRule[n_elements];
						
						for(int i = 0;i<n_elements;i++) {
							int index = (i)*2;
							
							rules[i] = (ParseRule)srs.getNode(index).object;
						}
						
						int flags = ParseRule.NONE;
						
						
						if(parseNode.size() == 5) {
							ParseNode flagsNode = parseNode.getNode(3); 
							
							flags = getFlags(flagsNode);
						}
						
						parseNode.object = new ParseRule(flags,rules);
						
					}else if(parseNode.getNode(1).type.equals("rule_node_sequence")) {
						ParseNode rns = parseNode.getNode(1);
						
						int n_elements = (rns.size()+1)/2;
						
						RuleNode[] ruleNodes = new RuleNode[n_elements];
						
						for(int i = 0;i<n_elements;i++) {
							int index = (i)*2;
							
							ruleNodes[i] = (RuleNode)rns.getNode(index).object;
							
						}
						
						String outType = (String)(parseNode.getNode(3).getNode(0).object);
						
						int flags = ParseRule.NONE;
						
						if(parseNode.size() == 7) {
							ParseNode flagsNode = parseNode.getNode(5); 
							
							flags = getFlags(flagsNode);
						}
						
						parseNode.object = new ParseRule(flags,outType,ruleNodes);
						
					}
				}
			});
			
		}
		
		public static void generateObjectsInNodes(ParseNode parseNode) {//this is for the meta lang
			
			
			
			for(int i = 0;i<parseNode.size();i++) {
				generateObjectsInNodes(parseNode.getNode(i));
			}
			
			ParseAction whatToDo = parseNodeTypeToInstruction.get(parseNode.type);
			if(whatToDo != null) whatToDo.doAction(parseNode);
		}
		
		public static int getFlags(ParseNode flagsNode) {
			int flags = ParseRule.NONE;
			for(int i = 0;i<flagsNode.size();i++) {
				String flagStr = (String)(flagsNode.getNode(i).getNode(0).object);
				
				if(flagStr.equals("MERGE")) {
					flags|=ParseRule.MERGE;
				}else if(flagStr.equals("LOOP")) {
					flags|=ParseRule.LOOP;
				}else if(flagStr.equals("REMOVE_OPERATOR")) {
					flags|=ParseRule.REMOVE_OPERATOR;
				}else if(flagStr.equals("NONE")) {
					//no code
				}else {
					System.err.println("Flag not recognized! : "+flagStr);
				}
				
			}
			
			return flags;
		}
		
		
		public static ParseRule createParseStructure(String parseMachineText) {
			ParseNode langParseTree = baseParse(parseMachineText,metaLangDef);
			
			generateObjectsInNodes(langParseTree);
			
			ParseRule out = null;
			try {
				out = (ParseRule)langParseTree.object;
			}catch(Exception e) {
				System.err.println("failed to parse language description, unexpected output!");
				System.err.println("Generation output...");
				System.out.println(langParseTree);
				return null;
			}
			
			if(out == null) {
				System.err.println("failed to parse language description, no output!");
				System.err.println("Generation output...");
				System.out.println(langParseTree);
				return null;
			}
			
			System.out.println("Syntax Definition loaded!");
			
			return out;
		}
		public static ParseRule createParseStructureFromFile(String fileName) {
			System.out.println("Loading Syntax Definition from : \""+fileName+"\"");
			
			String parseMachineText = "";
			
			File syntaxFile = new File(fileName);
			
			try {
				Scanner myReader = new Scanner(syntaxFile);
				while (myReader.hasNextLine()) {
					parseMachineText += myReader.nextLine();
					parseMachineText += "\n";
				}
				myReader.close();
			} catch (FileNotFoundException e) {
				System.err.println("syntax file not found!");
			}
			
			return createParseStructure(parseMachineText);
		}
	}
	
	
	
	public static ParseNode baseParse(String toParse,ParseRule parseRules) {
		ParseNode parseNode = breakUp(toParse);
		
		applyRuleToTree(parseNode,parseRules);
		
		if(parseNode.size() == 1) {
			parseNode = parseNode.getNode(0);
		}
		
		return parseNode;
	}
	
	static final String NO_TYPE = "NO_TYPE";
	static final String NODE_SEQUENCE = "NODE_SEQUENCE";
	static final String ANY = "ANY";
	static final String UNKNOWN_TYPE = "UNKNOWN";
	static final String CHAR = "CHAR";
	static final String NEG_CHAR = "NEG_CHAR";
	static final String ANY_CHAR = "ANY_CHAR";
	static final String PARSE_PARAM = "PARSE_PARAM";//parameter for a down parse
	
	static final String DELETE = "DELETE";//TODO
	
	static char EMPTY_CHAR = 'ε';//classic epsilon notation
	
	
	//operator types for down parse
	static final int OPERATOR = 0b0;
	static final int ENCAP = 0b1;//things like parenthesis
	//the operator is on the left and everything else is on the right
	static final int OPERATOR_LEFT = 0b10;
	static final int OPERATOR_RIGHT = 0b100;//opposite of operator left
	static final int NOT_OPERATOR = 0b1000;//not parse-able
	
	//easy construct
	static RuleNode ruleNode(String type) {
		return new RuleNode(type,false);
	}
	static RuleNode ruleNodeChar(String charClass) {
		return new RuleNode(charClass,CHAR,false);
	}
	static RuleNode ruleNodeNegChar(String charClass) {
		return new RuleNode(charClass,NEG_CHAR,false);
	}
	static RuleNode contextRuleNode(String type) {
		return new RuleNode(type,true);
	}
	static RuleNode contextRuleNodeChar(String charClass) {
		return new RuleNode(charClass,CHAR,true);
	}
	static RuleNode contextRuleNodeNegChar(String charClass) {
		return new RuleNode(charClass,NEG_CHAR,true);
	}
	static ParseRule tokenRule(String outName,String token) {
		RuleNode[] nodes = new RuleNode[token.length()];
		int i = 0;
		for(char c:token.toCharArray()) {
			nodes[i] = ruleNodeChar(String.valueOf(c));
			i++;
		}
		return new ParseRule(outName,nodes);
		
	}
	//
	
	public static class ParseNode{
		
		private char leaf_char;
		private String type = null;
		private ArrayList<ParseNode> childNodes = null;
		private boolean fullyParsed = false;
		
		private Object object = null;//what the node represents
		
		public Object getOutput() {
			return object;
		}
		public void setOutput(Object object) {
			this.object = object;
		}
		
		ParseNode(){
			this.type = UNKNOWN_TYPE;
			this.leaf_char = EMPTY_CHAR;
		}
		ParseNode(String type){
			this.type = type;
			this.leaf_char = EMPTY_CHAR;
		}
		ParseNode(char c){
			this.leaf_char = c;
			this.type = CHAR;
			this.fullyParsed = true;
		}
		ParseNode getNode(int i) {
			return childNodes.get(i);
		}
		void setNode(int i,ParseNode node) {
			childNodes.set(i,node);
		}
		void addNode(ParseNode node) {
			if(childNodes == null) childNodes = new ArrayList<ParseNode>();
			childNodes.add(node);
		}
		void removeNode(int i) {
			childNodes.remove(i);
		}
		
		ParseNode abstractRegion(int startIndex,int endIndex,String outType,boolean fullyParsed) {//end index is inclusive
			ParseNode paramNode = new ParseNode(outType);
			paramNode.fullyParsed = fullyParsed;
			for(int i = startIndex;i<=endIndex;i++) {
				paramNode.addNode(getNode(i));
			}
			for(int i = endIndex;i>startIndex;i--) {
				removeNode(i);
			}
			setNode(startIndex,paramNode);
			return paramNode;
		}
		
		int size() {
			if(childNodes != null) return childNodes.size();
			else return 0;
		}
		int endIndex() {
			return this.size()-1;
		}
		void merge() {
			if(childNodes != null) {
				ArrayList<ParseNode> repl = new ArrayList<ParseNode>();
				for(ParseNode child:childNodes) {
					if(child.type.equals(this.type)) {
						repl.addAll(child.childNodes);
					}else {
						repl.add(child);
					}
				}
				childNodes = repl;
			}
		}
		
		private String toString(int tab) {
			if(type == null) return "error";
			
			String out = "";
			for(int i = 0;i<tab;i++) out+="\t";
			if(type.equals(CHAR)) {
				out+="CHAR: "+leaf_char+" , fp: "+fullyParsed+" , object: {"+object+"}\n";
			}else {
				out+="type: "+type+" , fp: "+fullyParsed+" , object: {"+object+"}\n";
			}
			if(childNodes != null) {
				for(ParseNode child:childNodes) out+=child.toString(tab+1);
			}
			return out;
		}
		
		@Override
		public String toString() {
			return toString(0);
		}
	}
	
	private static class RuleNode{
		private String charClass = null;
		private String type = NO_TYPE;
		//context nodes are nodes on the left most or right most that are not converted to tree but are required to build tree
		private boolean contextBased = false;
		
		RuleNode(String charClass,String type,boolean contextBased){
			this.charClass = charClass;
			this.type = type;
			this.contextBased = contextBased;
		}
		RuleNode(String type,boolean contextBased){
			this.type = type;
			this.contextBased = contextBased;
		}
		boolean hasChar(char c) {
			return charClass.contains(String.valueOf(c));
		}
		@Override
		public String toString() {
			if(type == null) return "error";
			if(type.equals(CHAR)) return "rule node char class '"+charClass+"' CB:"+contextBased;
			if(type.equals(NEG_CHAR)) return "rule node char class without '"+charClass+"' CB:"+contextBased;
			else return "rule node of type ( "+type+" )";
		}
		
		@Override
		public boolean equals(Object other) {
			if(other instanceof RuleNode) {
				RuleNode otherRuleNode = (RuleNode)other;
				
				return otherRuleNode.type.equals(this.type) && otherRuleNode.charClass.equals(this.charClass) && otherRuleNode.contextBased == this.contextBased;
			}
			return false;
		}
		boolean followsRule(ParseNode parseNode) {//check if a parse node matches according to the rule node
			if(parseNode.type.equals(this.type)) {
				if(this.type.equals(CHAR)) {
					if(this.hasChar(parseNode.leaf_char)) return true;
				}else return true;
			}else if(this.type.equals(ANY_CHAR) && parseNode.type.equals(CHAR)) {
				return true;
			}else if(this.type.equals(NEG_CHAR) && parseNode.type.equals(CHAR)) {
				if(!this.hasChar(parseNode.leaf_char)) return true;
			}else if(this.type.equals(ANY)) {
				return true;
			}
			return false;
		}
	}
	
	private static class ParseRule{
		//flags
		//up parse is bottom up and down parse is top down parsing
		static int NONE = 0b0,MERGE = 0b1,REMOVE_OPERATOR = 0b10000,LOOP = 0b10;//removes the operator as its usually not needed
		//flags END
		
		static boolean DOWN_PARSE = true,UP_PARSE = false;
		
		private boolean mode = UP_PARSE;
		
		private RuleNode[] pattern = null;
		private int flags = NONE;
		private String outType = null;
		
		ParseRule[] childRules = null;
		
		private int downParseType = -1;//not determined yet
		
		private int leftSideContextSize = 0;
		private int rightSideContextSize = 0;
		
		private int operatorLength = 0;
		private int leftEncapLength = 0;
		private int rightEncapLength = 0;
		
		private boolean leftRightEncapUnique = true;//are the left and right encap symbols different
		
		ParseRule(int flags,String outType,RuleNode... pattern){
			this.pattern = pattern;
			this.outType = outType;
			this.flags = flags;
			init();
		}
		ParseRule(String outType,RuleNode... pattern){
			this.pattern = pattern;
			this.outType = outType;
			init();
		}
		
		ParseRule(ParseRule... childRules){
			this.childRules = childRules;
			init();
		}
		
		ParseRule(int flags,ParseRule... childRules){
			this.childRules = childRules;
			this.flags = flags;
			init();
		}
		
		public RuleNode get(int i) {
			return pattern[i];
		}
		public int size() {
			return pattern.length;
		}
		
		boolean merge() {
			return ((flags&MERGE) != 0b0);
		}
		
		boolean upParse() {
			return mode == UP_PARSE;
		}
		boolean downParse() {
			return mode == DOWN_PARSE;
		}
		boolean removeOperator() {
			return ((flags&REMOVE_OPERATOR) != 0b0);
		}
		boolean loop() {
			return ((flags&LOOP) != 0b0);
		}
		
		boolean isRuleTree() {
			return childRules != null;
		}
		
		int countType(String type,int startIndex,int endIndex) {//end index is inclusive
			int count = 0;
			for(int i = startIndex;i <= endIndex;i++) {
				if(get(i).type.equals(type)) count++;
			}
			return count;
		}
		
		boolean matchesRegion(ParseNode parseNode, int parseNodeStart,int patternMinIndex,int patternMaxIndex) {//pattern max index is inclusive
			if(parseNodeStart < 0) return false;//invalid start
			
			int patternWidth = patternMaxIndex-patternMinIndex+1;//get width of pattern
			
			if(parseNodeStart+patternWidth-1>parseNode.endIndex()) return false;//pattern is larger than available space
			
			for(int i = patternMinIndex;i <= patternMaxIndex;i++) {
				int accessIndex = parseNodeStart+i-patternMinIndex;
				if(accessIndex > parseNode.endIndex()) return false;//just in case
				
				if(!(this.get(i).followsRule(parseNode.getNode(accessIndex)))) return false;
			}
			
			return true;
		}
		
		int endIndex() {
			return this.size()-1;
		}
		boolean startsWithType(String type) {
			return get(0).type.equals(type);
		}
		boolean endsWithType(String type) {
			return get(endIndex()).type.equals(type);
		}
		
		void init() {
			if(!isRuleTree()) {
				while(pattern[leftSideContextSize].contextBased) {
					leftSideContextSize++;
				}
				while(pattern[endIndex()-rightSideContextSize].contextBased) {
					rightSideContextSize++;
				}
				
				if(countType(NODE_SEQUENCE,0,endIndex()) > 0) mode = DOWN_PARSE;
				else mode = UP_PARSE;
				
				if(downParse()) initDownParseType();
			}
		}
		
		void initDownParseType() {
			//classify the down parse type
			
			
			//things that encapsulate example parenthesis brackets etc
			//example {something,any,something_else}
			//there can only be one 'any' token in between
			if(size() >= 3 && !startsWithType(NODE_SEQUENCE) && !endsWithType(NODE_SEQUENCE) && countType(NODE_SEQUENCE,1,endIndex()-1) == 1) {
				downParseType = ENCAP;
				while(!pattern[leftEncapLength].type.equals(NODE_SEQUENCE)) {
					leftEncapLength++;
				}
				while(!pattern[endIndex()-rightEncapLength].type.equals(NODE_SEQUENCE)) {
					rightEncapLength++;
				}
				
				if(leftEncapLength == rightEncapLength) {
					boolean same = true;
					
					for(int i = 0;i<leftEncapLength && same;i++) {
						if(!get(i).equals( get( endIndex()-rightEncapLength+1+i ) ) ) same = false;
					}
					
					if(same) leftRightEncapUnique = false;//left and right encap symbols are the same
				}
			}
			
			//first token is any and everything after has a type
			//example {any,something,something_else}
			else if(this.size() >= 2 && this.get(0).type.equals(NODE_SEQUENCE) && countType(NODE_SEQUENCE,1,endIndex()) == 0) {
				downParseType = OPERATOR_RIGHT;
			}
			//last token is any and everything before has a type
			//example {something,something_else,any}
			else if(this.size() >= 2 && endsWithType(NODE_SEQUENCE) && countType(NODE_SEQUENCE,0,endIndex()-1) == 0) {
				downParseType = OPERATOR_LEFT;
			}
			//first and last token are 'any' everything in between is an operator
			else if(this.size()>= 3 && startsWithType(NODE_SEQUENCE) && endsWithType(NODE_SEQUENCE) && countType(NODE_SEQUENCE,1,endIndex()-1) == 0) {
				downParseType = OPERATOR;
				operatorLength = size()-2;
			}
			//could not be classified
			else {
				downParseType = NOT_OPERATOR;
			}
		}
		
		@Override
		public String toString() {
			String out = "";
			if(upParse()) out += "upParseRule(";
			else out += "downParseRule(";
			for(RuleNode rn:pattern) {
				out+=rn;
				out+=",";
			}
			out+=" -> "+outType+")";
			return out;
		}
	}
	
	//apply the parse rule to the tree starting from the root and ending with the leafs
	//the reason to start at the root is because that is how top_down parsing works
	static void applyRuleToTree(ParseNode parseNode,ParseRule parseRule) {
		applyRuleToSingleNode(parseNode,parseRule);
		for(int i = 0;i < parseNode.size();i++) {
			applyRuleToTree(parseNode.getNode(i),parseRule);
		}
	}
	
	static boolean applyRuleToSingleNode(ParseNode parseNode,ParseRule parseRule) {
		if(parseNode.fullyParsed) return false;//node is finished
		
		//decide what kind of rule it is
		if(parseRule.isRuleTree()) {
			boolean made_changes = false;
			
			boolean making_changes;
			do{
				making_changes = false;
				for(int i = 0;i<parseRule.childRules.length;i++) {
					making_changes |= applyRuleToSingleNode(parseNode,parseRule.childRules[i]);
				}
				made_changes |= making_changes;
			}while(making_changes && parseRule.loop());
			return made_changes;
			
		}else if(parseRule.downParse()) {
			return applyDownParse(parseNode,parseRule);
		}else if(parseRule.upParse()) {
			return applyUpParse(parseNode,parseRule);
		}
		return false;
	}
	
	static boolean applyDownParse(ParseNode parseNode,ParseRule parseRule) {
		if(parseRule.downParseType == OPERATOR) {
			return applyOperatorDownParse(parseNode,parseRule);
		}else if(parseRule.downParseType == ENCAP) {
			return applyEncapDownParse(parseNode,parseRule);
		}
		//TODO
		return false;
	}
	
	static boolean applyEncapDownParse(ParseNode parseNode,ParseRule parseRule) {
		boolean made_changes = false;
		
		char EMPTY = 't';
		char OPEN = '[';
		char CLOSE = ']';
		
		char[] basicInterpretation = new char[parseNode.size()];//make a basic interpretation to make things easier
		for(int i = 0;i<basicInterpretation.length;i++) {
			basicInterpretation[i] = EMPTY;
		}
		
		if(parseRule.leftRightEncapUnique) {
			//detect left
			for(int i = 0;i<=parseNode.endIndex()-parseRule.leftEncapLength+1;i++) {
				if(parseRule.matchesRegion(parseNode, i,0 , parseRule.leftEncapLength-1)){//found
					basicInterpretation[i] = OPEN;
					i--;
					i+=parseRule.leftEncapLength;
				}
			}
			
			//detect right
			for(int i = 0;i<=parseNode.endIndex()-parseRule.rightEncapLength+1;i++) {
				if(parseRule.matchesRegion(parseNode, i, parseRule.endIndex()-parseRule.rightEncapLength+1 , parseRule.endIndex())){//found
					basicInterpretation[i] = CLOSE;
					i--;
					i+=parseRule.leftEncapLength;
				}
			}
		}else {
			char current_state = OPEN;
			//detect
			for(int i = 0;i<=parseNode.endIndex()-parseRule.leftEncapLength+1;i++) {
				if(parseRule.matchesRegion(parseNode, i,0 , parseRule.leftEncapLength-1)){//found
					basicInterpretation[i] = current_state;
					current_state = current_state == OPEN ? CLOSE : OPEN;//toggle 
					i--;
					i+=parseRule.leftEncapLength;
				}
			}
		}
		
		int level = 0;
		int lastClose = 0;
		
		boolean fullyParsed = !parseRule.leftRightEncapUnique;//if they are non-unique then the process is non recursive so it is finished
		
		for(int i = parseNode.endIndex();i>=0;i--) {
			if(basicInterpretation[i] == CLOSE) {
				if(level == 0) {
					lastClose = i;
				}
				level++;
			}else if(basicInterpretation[i] == OPEN) {
				if(level == 1) {
					ParseNode outNode = parseNode.abstractRegion(i+parseRule.leftSideContextSize, lastClose+parseRule.rightEncapLength-1-parseRule.rightSideContextSize, parseRule.outType, true);
					made_changes = true;
					
					int actualLeftLength = parseRule.leftEncapLength-parseRule.leftSideContextSize;
					int actualRightLength = parseRule.rightEncapLength-parseRule.rightSideContextSize;
					
					if(parseRule.removeOperator()) {
						
						int OGEnd = outNode.endIndex()-actualRightLength+1;
						
						for(int j = outNode.endIndex();j>=OGEnd;j--) {
							outNode.removeNode(j);
						}
						
						for(int j = 0;j<actualLeftLength;j++) {
							outNode.removeNode(0);
						}
						
						outNode.abstractRegion(0, outNode.endIndex(), PARSE_PARAM, fullyParsed);
					}else {
						outNode.abstractRegion(actualLeftLength, outNode.endIndex()-actualRightLength, PARSE_PARAM, fullyParsed);
					}
					
				}
				level--;
			}
		}
		
		return made_changes;
	}
	
	static boolean applyOperatorDownParse(ParseNode parseNode,ParseRule parseRule) {
		//context nodes should not be used in operator definitions as it does not make sense
		
		ArrayList<Integer> operatorStartIndexes = new ArrayList<Integer>(); 
		
		
		for(int i = 0;i<=parseNode.endIndex()-parseRule.operatorLength+1;i++) {
			if(parseRule.matchesRegion(parseNode, i, 1, parseRule.endIndex()-1)){//found
				operatorStartIndexes.add(i);
				i--;//compensate for automatic increment
				i += parseRule.operatorLength;//skip forward
			}
		}
		
		if(operatorStartIndexes.size()>0) {
			operatorStartIndexes.add(parseNode.endIndex()+1);//imaginary operator at the end
			
			ParseNode out = new ParseNode(parseRule.outType);
			
			int lastIndex = 0;
			
			for(int nextIndex:operatorStartIndexes) {
				ParseNode paramNode = new ParseNode(PARSE_PARAM);
				
				
				for(int i = lastIndex;i<nextIndex;i++) {
					paramNode.addNode(parseNode.getNode(i));
				}
				
				if(paramNode.size()>0) out.addNode(paramNode);
				
				if(!parseRule.removeOperator()) {
					for(int i = nextIndex;i<Math.min(nextIndex+parseRule.operatorLength,parseNode.endIndex());i++) {
						out.addNode(parseNode.getNode(i));
					}
				}
				
				lastIndex = nextIndex+parseRule.operatorLength;
			}
			
			
			parseNode.childNodes.clear();
			out.fullyParsed = true;
			parseNode.addNode(out);
			return true;
		}
		return false;
	}
	
	static boolean applyUpParse(ParseNode parseNode,ParseRule parseRule) {
		
		boolean made_changes = false;
		
		boolean  making_changes = true;//while things are changing search and apply rule
		
		while(making_changes) {
			making_changes = false;
			
			for(int i = parseNode.endIndex();i>=parseRule.endIndex();i--) {
				
				if(parseRule.matchesRegion(parseNode, i-parseRule.size()+1, 0, parseRule.endIndex())){//matches!
					making_changes = true;
					made_changes = true;
					
					if(parseRule.outType.equals(DELETE)) {
						int startPoint = i-parseRule.size()+1+parseRule.leftSideContextSize;
						int endPoint = i-parseRule.rightSideContextSize;
						for(int j = endPoint;j>=startPoint;j--) {
							parseNode.removeNode(j);
						}
					}else {
						ParseNode out = parseNode.abstractRegion(i-parseRule.size()+1+parseRule.leftSideContextSize, i-parseRule.rightSideContextSize, parseRule.outType,true/*up parse is always fully parsed*/);
						if(parseRule.merge()) out.merge();//merge nodes if that flag is set
					}
				}
			}
		}
		
		
		return made_changes;
	}
	
	private static ParseNode breakUp(String toParse) {//breaks up string into individual parse nodes then has a root parse node that holds each of them
		ParseNode out = new ParseNode();
		for(char c:toParse.toCharArray()) {
			out.addNode(new ParseNode(c));
		}
		return out;
	}
}
