package cas.lang;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class ParseMachine {
	
	/*
	 * Give it a parseNode and some build instructions and it build any object
	 */
	
	public static String getFileAsString(String fileName) {
		String out = "";
		Scanner scanner;
		try {
			scanner = new Scanner(new File(fileName));
			while(scanner.hasNextLine()) {
				out+=scanner.nextLine();
				out+="\n";
			}
		} catch (FileNotFoundException e) {
			System.err.println("file not found! : \""+fileName+"\"");
		}
		
		return out;
	}
	
	public static abstract class ParseAction{
		abstract Object doAction(ParseNode parseNode) throws Exception;
		void init() throws Exception{
			
		}
	}
	
	public static final ParseAction generateStringAction = new ParseAction() {

		@Override
		Object doAction(ParseNode parseNode) throws Exception {
			parseNode.generateStringToOutput();
			return parseNode.getOutput();
		}
		
	};
	
	public static final ParseAction generateIntegerAction = new ParseAction() {

		@Override
		Object doAction(ParseNode parseNode) throws Exception {
			parseNode.generateIntegerToOutput();
			return parseNode.getOutput();
		}
		
	};
	
	public static final ParseAction generateBigIntAction = new ParseAction() {

		@Override
		Object doAction(ParseNode parseNode) throws Exception {
			parseNode.generateBigIntToOutput();
			return parseNode.getOutput();
		}
		
	};
	
	public static final ParseAction generateFloat64Action = new ParseAction() {

		@Override
		Object doAction(ParseNode parseNode) throws Exception {
			parseNode.generateFloat64ToOutput();
			return parseNode.getOutput();
		}
		
	};
	
	public static class ObjectBuilder{
		private HashMap<String,ParseAction> parseNodeTypeToInstruction = new HashMap<String,ParseAction>();
		ParseRule langDescription = null;
		
		public void setLang(ParseRule langDescription) {
			this.langDescription = langDescription;
		}
		public void setLang(String metaLangFileName) throws Exception {
			MetaLang.loadLanguageFromFile(metaLangFileName);
		}
		
		public void addBuildInstruction(String type,ParseAction parseAction) {
			parseNodeTypeToInstruction.put(type, parseAction);
		}
		public void init() {
			try {
				for(ParseAction a:parseNodeTypeToInstruction.values()) {
					a.init();
				}
			}catch(Exception e) {
				System.err.println("Error encoutered in object builder initialization!");
				e.printStackTrace();
			}
		}
		public Object build(String toParse) {
			ParseNode parseNode = baseParse(toParse, langDescription);
			return build(parseNode);
		}
		public Object build(ParseNode node) {
			try {
				generateObjectsInNodes(node);
			}catch(Exception e) {
				System.err.println("Error encountered while building object");
				e.printStackTrace();
			}
			if(node.getOutput() == null) {
				System.err.println("Failed to build object, null object at root");
				System.err.println("dump of parse tree");
				System.out.println(node);
			}
			return node.getOutput();
		}
		
		private void generateObjectsInNodes(ParseNode parseNode) throws Exception {//this is for the meta lang
			for(int i = 0;i<parseNode.size();i++) {
				generateObjectsInNodes(parseNode.getNode(i));
			}
			
			ParseAction whatToDo = parseNodeTypeToInstruction.get(parseNode.type);
			if(whatToDo != null) {
				parseNode.setOutput(whatToDo.doAction(parseNode));
			}
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
	static final String EMPTY_NODE = "EMPTY_NODE";
	static final String NODE_SEQUENCE = "NODE_SEQUENCE";//special
	static final String TYPE_CLASS = "TYPE_CLASS";
	static final String ANY = "ANY";
	static final String UNKNOWN_TYPE = "UNKNOWN";
	static final String CHAR = "CHAR";
	static final String NEG_CHAR = "NEG_CHAR";
	static final String NEG_TYPE = "NEG_TYPE";
	static final String ANY_CHAR = "ANY_CHAR";
	static final String PARSE_PARAM = "PARSE_PARAM";//parameter for a down parse
	static final String DELETE = "DELETE";//TODO ad
	
	static char EMPTY_CHAR = 'ε';//classic epsilon notation
	
	//easy construct
	static RuleNode ruleNode(String type) {
		return new RuleNode(type,false);
	}
	static RuleNode ruleNodeChar(String charClass) {
		return new RuleNode(CHAR,charClass,false);
	}
	static RuleNode ruleNodeNegChar(String charClass) {
		return new RuleNode(NEG_CHAR,charClass,false);
	}
	static RuleNode ruleNodeNegTypeClass(RuleNode... types) {
		return new RuleNode(NEG_TYPE,types,false);
	}
	static RuleNode ruleNodeTypeClass(RuleNode... types) {
		return new RuleNode(TYPE_CLASS,types,false);
	}
	static RuleNode contextRuleNode(String type) {
		return new RuleNode(type,true);
	}
	static RuleNode contextRuleNodeChar(String charClass) {
		return new RuleNode(CHAR,charClass,true);
	}
	static RuleNode contextRuleNodeNegChar(String charClass) {
		return new RuleNode(NEG_CHAR,charClass,true);
	}
	static RuleNode contextRuleNodeTypeClass(RuleNode... types) {
		return new RuleNode(TYPE_CLASS,types,true);
	}
	static ParseRule tokenRule(String outName,String token) {//matches token set
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
		
		//public methods
		
		public String getType() {
			return type;
		}
		
		public char getLeafChar() {
			return leaf_char;
		}
		
		public ParseNode getNode(int i) {
			return childNodes.get(i);
		}
		public Object getOutput() {
			return object;
		}
		public void setOutput(Object object) {
			this.object = object;
		}
		public int size() {
			if(childNodes != null) return childNodes.size();
			else return 0;
		}
		public int endIndex() {
			return this.size()-1;
		}
		public boolean hasElementByName(String name) {
			for(ParseNode n:childNodes) {
				if(n.type.equals(name)) return true;
			}
			return false;
		}
		
		public ParseNode getElementByName(String name) {
			for(ParseNode n:childNodes) {
				if(n.type.equals(name)) return n;
			}
			return null;
		}
		
		public void generateStringToOutput() {//converts leaf char sequence into string and stores it in output object
			String str = "";
			if(this.type.equals(CHAR)) {
				str += this.leaf_char;
			}else {
				for(ParseNode n:childNodes) {
					n.generateStringToOutput();
					str+=((String)n.getOutput());
				}
			}
			this.setOutput(str);
		}
		
		public void generateIntegerToOutput() {
			generateStringToOutput();
			this.setOutput(Integer.parseInt((String)this.getOutput()));
		}
		
		public void generateBigIntToOutput() {
			generateStringToOutput();
			this.setOutput(new BigInteger((String)this.getOutput()));
		}
		
		public void generateFloat64ToOutput() {
			generateStringToOutput();
			this.setOutput(Double.parseDouble((String)this.getOutput()));
		}
		
		@Override
		public String toString() {
			return toString(0);
		}
		
		//private methods
		
		private void setNode(int i,ParseNode node) {
			childNodes.set(i,node);
		}
		private void addNode(ParseNode node) {
			if(childNodes == null) childNodes = new ArrayList<ParseNode>();
			childNodes.add(node);
		}
		private void removeNode(int i) {
			childNodes.remove(i);
		}
		
		private ParseNode abstractRegion(int startIndex,int endIndex,String outType,boolean fullyParsed) {//end index is inclusive

			if(endIndex<startIndex) return new ParseNode(EMPTY_NODE);//can't abstract

			ParseNode paramNode = new ParseNode(outType);
			paramNode.fullyParsed = fullyParsed;
			for(int i = startIndex;i<=endIndex;i++) {
				paramNode.addNode(getNode(i));
			}
			for(int i = endIndex;i>startIndex;i--) {
				removeNode(i);
			}
			setNode(startIndex,paramNode);
			return paramNode;//return the node if needed by other code
		}
		private void merge() {
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
				out+="CHAR: "+ (leaf_char == '\n'? "NEWLINE":leaf_char) +" , fp: "+fullyParsed+" , object: {"+object+"} "+(object == null? "no class" : object.getClass().getCanonicalName())+"\n";
			}else {
				out+="type: "+type+" , fp: "+fullyParsed+" , object: {"+object+"} "+(object == null? "no class" : object.getClass().getCanonicalName())+"\n";
			}
			if(childNodes != null) {
				for(ParseNode child:childNodes) out+=child.toString(tab+1);
			}
			return out;
		}
		
	}
	
	static class RuleNode{
		private String charClass = null;
		private RuleNode[] typeClass = null;
		private String type = NO_TYPE;
		//context nodes are nodes on the left most or right most that are not converted to tree but are required to build tree
		private boolean contextBased = false;
		
		RuleNode(String type,String charClass,boolean contextBased){
			this.charClass = charClass;
			this.type = type;
			this.contextBased = contextBased;
		}
		RuleNode(String type,RuleNode[] typeClass,boolean contextBased){
			this.typeClass = typeClass;
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
				}
				else return true;
			}else if(this.type.equals(ANY_CHAR) && parseNode.type.equals(CHAR)) {
				return true;
			}else if(this.type.equals(NEG_CHAR) && parseNode.type.equals(CHAR)) {
				if(!this.hasChar(parseNode.leaf_char)) return true;
			}else if(this.type.equals(ANY)) {
				return true;
			}else if(this.type.equals(TYPE_CLASS)) {
				for(RuleNode rn:typeClass) {
					if(rn.followsRule(parseNode)) return true;
				}
				return false;
			}else if(this.type.equals(NEG_TYPE)) {
				boolean found = false;
				for(RuleNode rn:typeClass) {
					if(rn.followsRule(parseNode)) found = true;
				}
				return !found;
			}
			return false;
		}
	}
	
	static class ParseRule{
		//flags
		//up parse is bottom up and down parse is top down parsing
		static int NONE = 0b0,MERGE = 0b1,REMOVE_OPERATOR = 0b10,LOOP = 0b100;//removes the operator as its usually not needed
		//flags END
		
		//operator types for down parse
		static final int OPERATOR = 0b0;
		static final int ENCAP = 0b1;//things like parenthesis
		//the operator is on the left and everything else is on the right
		static final int OPERATOR_LEFT = 0b10;
		static final int OPERATOR_RIGHT = 0b100;//opposite of operator left
		static final int NOT_OPERATOR = 0b1000;//not parse-able
		
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
						if(!get(i).equals( get( endIndex()-rightEncapLength+1+i ) ) ) {
							same = false;
							break;
						}
					}
					
					if(same) leftRightEncapUnique = false;//left and right encap symbols are the same
				}
			}
			
			//first token is any and everything after has a type
			//example {any,something,something_else}
			else if(this.size() >= 2 && this.get(0).type.equals(NODE_SEQUENCE) && countType(NODE_SEQUENCE,1,endIndex()) == 0) {
				downParseType = OPERATOR_RIGHT;
				operatorLength = size()-1;
			}
			//last token is any and everything before has a type
			//example {something,something_else,any}
			else if(this.size() >= 2 && endsWithType(NODE_SEQUENCE) && countType(NODE_SEQUENCE,0,endIndex()-1) == 0) {
				downParseType = OPERATOR_LEFT;
				operatorLength = size()-1;
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
			if(childRules != null) {
				out+="[";
				for(int i = 0;i<childRules.length;i++) {
					out+=childRules[i];
					
					if(i != childRules.length-1) {
						out+=",";
					}
				}
				out+="]";
			}else {
				if(upParse()) out += "upParseRule(";
				else out += "downParseRule(";
				for(RuleNode rn:pattern) {
					out+=rn;
					out+=",";
				}
				out+=" -> "+outType+")";
			}
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
		if(parseRule.downParseType == ParseRule.OPERATOR) {
			return applyOperatorDownParse(parseNode,parseRule);
		}else if(parseRule.downParseType == ParseRule.ENCAP) {
			return applyEncapDownParse(parseNode,parseRule);
		}else if(parseRule.downParseType == ParseRule.OPERATOR_LEFT) {
			return applyOperatorLeftDownParse(parseNode,parseRule);
		}else if(parseRule.downParseType == ParseRule.OPERATOR_RIGHT) {
			return applyOperatorRightDownParse(parseNode,parseRule);
		}
		//TODO
		return false;
	}
	
	static boolean applyOperatorRightDownParse(ParseNode parseNode,ParseRule parseRule) {
		boolean made_changes = false;
		
		int startIndex = -1;
		
		for(int i = parseNode.endIndex()-parseRule.operatorLength+1;i >= 0 ;i-- ) {//right to left search
			if(parseRule.matchesRegion(parseNode, i, 1, parseRule.endIndex() )) {
				startIndex = i;
				break;
			}
		}
		
		if(startIndex != -1) {
			ParseNode out = parseNode.abstractRegion(0, startIndex-1+parseRule.operatorLength-parseRule.rightSideContextSize, parseRule.outType, true);
			int actualOperatorLength = parseRule.operatorLength-parseRule.rightSideContextSize;
			
			out.abstractRegion(0, out.endIndex()-actualOperatorLength, PARSE_PARAM, false);
			
			
			if(parseRule.removeOperator()) {//keep popping end index
				for(int i = 0;i<actualOperatorLength;i++) {
					out.removeNode(out.endIndex());
				}
			}
			
			
			made_changes = true;
		}
		
		
		
		return made_changes;
	}
	
	
	static boolean applyOperatorLeftDownParse(ParseNode parseNode,ParseRule parseRule) {
		boolean made_changes = false;
		int startIndex = -1;
		
		for(int i = 0;i<=parseNode.endIndex()-parseRule.operatorLength+1;i++){//move from left to right
			if(parseRule.matchesRegion(parseNode, i, 0, parseRule.operatorLength-1 )) {
				startIndex = i;
				break;
			}
		}
		
		if(startIndex != -1) {
			ParseNode out = parseNode.abstractRegion(startIndex+parseRule.leftSideContextSize, parseNode.endIndex(), parseRule.outType, true);
			
			int actualOperatorLength = parseRule.operatorLength-parseRule.leftSideContextSize;
			
			out.abstractRegion(actualOperatorLength, out.endIndex(), PARSE_PARAM, false);
			
			if(parseRule.removeOperator()) {//keep popping front index
				for(int i = 0;i<actualOperatorLength;i++) {
					out.removeNode(0);
				}
			}
			
			made_changes = true;
		}
		
		return made_changes;
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
