package cas.lang;

import cas.lang.ParseMachine.ParseNode;
import cas.lang.ParseMachine.ParseRule;
import cas.lang.ParseMachine.RuleNode;

import java.util.ArrayList;
import java.util.HashMap;

import cas.lang.ParseMachine.ObjectBuilder;
import cas.lang.ParseMachine.ParseAction;

public class MetaLang {
	
	private static boolean loaded = false;

	static ParseRule metaLangDef = null;
	static ObjectBuilder parseRuleBuilder = null;
	
	public static ParseRule loadLanguageFromFile(String fileName) throws Exception {
		String fileText = ParseMachine.getFileAsString(fileName);
		ParseNode parseTree = ParseMachine.baseParse(fileText,metaLangDef);
		ParseRule parseRule = (ParseRule)parseRuleBuilder.build(parseTree);
		
		if(parseRule != null) {
			System.out.println("Done loading language from file: "+fileName);
		}else {
			throw new Exception("Language parse error");
		}
		return parseRule;
	}
	
	public static void init() {
		if(loaded) return;
		
		//hard-coded definition of the meta language
		/*
		 * This syntax structure might be changed slightly in the future.
		 * I'm probably only going to add features not remove any
		 */
		
		metaLangDef = new ParseRule(
				//define comment as a // or /**/
				new ParseRule(
					new ParseRule("comment",//comment starts with double slash
						ParseMachine.ruleNodeChar("/"),
						ParseMachine.ruleNodeChar("/")
					),
					new ParseRule(ParseRule.MERGE,"comment",//comment merges with any character other than newline into its own node
						ParseMachine.ruleNode("comment"),
						ParseMachine.ruleNodeNegChar("\n")
					),
					new ParseRule("comment",
						ParseMachine.ruleNodeChar("/"),
						ParseMachine.ruleNodeChar("*"),
						ParseMachine.ruleNode(ParseMachine.NODE_SEQUENCE),
						ParseMachine.ruleNodeChar("*"),
						ParseMachine.ruleNodeChar("/")
					)
				),
				
				//remove comments, spaces, newlines and tabs
				new ParseRule(ParseMachine.DELETE,
					ParseMachine.ruleNodeTypeClass(ParseMachine.ruleNodeChar(" \n\t"),ParseMachine.ruleNode("comment"))
				),
				
				//escape sequences
				new ParseRule(
						//quote character escape sequence for strings
						ParseMachine.tokenRule("quote_char", "\\\""),
						ParseMachine.tokenRule("newline_char", "\\\n"),
						ParseMachine.tokenRule("tab_char", "\\\t"),
						ParseMachine.tokenRule("backslash_char", "\\\\")
				),
			   
				//define a string as anything in quotes
				new ParseRule(ParseRule.REMOVE_OPERATOR,"string",
					ParseMachine.ruleNodeChar("\""),
					ParseMachine.ruleNode(ParseMachine.NODE_SEQUENCE),
					ParseMachine.ruleNodeChar("\"")
				),
				
				//define becomes token as ->
				ParseMachine.tokenRule("becomes", "->"),
				
				//define parenthesis as anything in round brackets ( )
				new ParseRule(ParseRule.REMOVE_OPERATOR,"paren",//define parenthesis
					ParseMachine.ruleNodeChar("("),
					ParseMachine.ruleNode(ParseMachine.NODE_SEQUENCE),
					ParseMachine.ruleNodeChar(")")
				),
				
				//token token :)
				ParseMachine.tokenRule("token", "token"),
				
				//special token parse rule
				new ParseRule("parseRule",
						ParseMachine.ruleNode("token"),
						ParseMachine.ruleNode("paren")
				),
				
				//name , used for node class names and constants
				new ParseRule(
					new ParseRule("name",
						ParseMachine.ruleNodeChar("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_")),
					new ParseRule(ParseRule.MERGE,"name",
						ParseMachine.ruleNode("name"),
						ParseMachine.ruleNode("name")
					)
				),
				
				//define a rule node as a name and parenthesis next to it. Classic function syntax f(x,y,z)
				new ParseRule("ruleNode",
						ParseMachine.ruleNode("name"),
						ParseMachine.ruleNode("paren")
				),
				
				//rule node list is any length of rule nodes next to each other possibly with commas between
				new ParseRule(
					new ParseRule("ruleNodeList",
						ParseMachine.ruleNode("ruleNode")
					),
					new ParseRule(ParseRule.MERGE,"ruleNodeList",
						ParseMachine.ruleNode("ruleNodeList"),
						ParseMachine.ruleNodeChar(","),
						ParseMachine.ruleNode("ruleNodeList")
					)
				),
				
				//define rule as anything in brackets
				new ParseRule(ParseRule.REMOVE_OPERATOR,"parseRule",
					ParseMachine.ruleNodeChar("["),
					ParseMachine.ruleNode(ParseMachine.NODE_SEQUENCE),
					ParseMachine.ruleNodeChar("]")
				),
				
				//parse rule list is any length of parse rule nodes next to each other, must be greater than one element
				new ParseRule(
					new ParseRule("parseRuleList",
						ParseMachine.ruleNode("parseRule"),
						ParseMachine.ruleNodeChar(","),
						ParseMachine.ruleNode("parseRule")
					),
					new ParseRule(ParseRule.MERGE,"parseRuleList",
						ParseMachine.ruleNode("parseRule"),
						ParseMachine.ruleNodeChar(","),
						ParseMachine.ruleNode("parseRuleList")
					),
					new ParseRule(ParseRule.MERGE,"parseRuleList",
						ParseMachine.ruleNode("parseRuleList"),
						ParseMachine.ruleNodeChar(","),
						ParseMachine.ruleNode("parseRule")
					),
					new ParseRule(ParseRule.MERGE,"parseRuleList",
						ParseMachine.ruleNode("parseRuleList"),
						ParseMachine.ruleNodeChar(","),
						ParseMachine.ruleNode("parseRuleList")
					)
				),
				
				//flags may be present and are additional instructions to the parser
				new ParseRule(ParseRule.REMOVE_OPERATOR,"flags",
					ParseMachine.ruleNodeChar(":"),
					ParseMachine.ruleNode(ParseMachine.NODE_SEQUENCE)
				),
				
				//the transformation of nodes into another node
				new ParseRule("transform",
					ParseMachine.ruleNode("ruleNodeList"),
					ParseMachine.ruleNode("becomes"),
					ParseMachine.ruleNode("name")
				)
		);
		
		parseRuleBuilder = new ObjectBuilder();
		
		parseRuleBuilder.addBuildInstruction("string", new ParseAction() {
			@Override
			void doAction(ParseNode parseNode) {
				String out = "";
				ParseNode param = parseNode.getNode(0);
				
				for(int i = 0;i<param.size();i++) {
					ParseNode curNode = param.getNode(i);
					if(curNode.getType().equals(ParseMachine.CHAR)) {
						out+=curNode.getLeafChar();
					}else if(curNode.getType().equals("quote_char")) {
						out+="\"";
					}else if(curNode.getType().equals("newline_char")) {
						out+="\n";
					}else if(curNode.getType().equals("tab_char")) {
						out+="\t";
					}else if(curNode.getType().equals("backslash_char")) {
						out+="\\";
					}
				}
				
				parseNode.setOutput(out);
			}
		});
		
		parseRuleBuilder.addBuildInstruction("name", new ParseAction() {
			@Override
			void doAction(ParseNode parseNode) {
				parseNode.generateStringToOutput();
			}
		});
		
		parseRuleBuilder.addBuildInstruction("ruleNodeList", new ParseAction() {
			@Override
			void doAction(ParseNode parseNode) throws Exception {
				ArrayList<RuleNode> ruleNodes = new ArrayList<RuleNode>();
				
				for(int i = 0;i<parseNode.size();i++) {
					ParseNode current = parseNode.getNode(i);
					
					if(current.getType().equals("ruleNode")) {
						if(current.getOutput() != null) {
							ruleNodes.add((RuleNode)current.getOutput());
						}else {
							throw new Exception("found ruleNode with null output!");
						}
					}
					
				}
				
				parseNode.setOutput(ruleNodes);
			}
		});
		
		parseRuleBuilder.addBuildInstruction("ruleNode", new ParseAction() {
			@SuppressWarnings("unchecked")
			@Override
			void doAction(ParseNode parseNode) throws Exception {
				String type = (String)parseNode.getElementByName("name").getOutput();
				
				ParseNode contents = parseNode.getElementByName("paren").getNode(0).getNode(0);
				
				String str = "";
				ArrayList<RuleNode> ruleNodes = null;
				
				if(contents.getType().equals("string")) {
					str = (String)contents.getOutput();
				}else if(contents.getType().equals("ruleNodeList")) {
					ruleNodes = (ArrayList<RuleNode>)contents.getOutput();
				}
				
				//char stuff
				if(type.equals("char")) {
					parseNode.setOutput(ParseMachine.ruleNodeChar(str));
				}else if(type.equals("negChar")) {
					parseNode.setOutput(ParseMachine.ruleNodeNegChar(str));
				}else if(type.equals("contextChar")) {
					parseNode.setOutput(ParseMachine.contextRuleNodeChar(str));
				}else if(type.equals("contextNegChar")) {
					parseNode.setOutput(ParseMachine.contextRuleNodeNegChar(str));
				}
				//types
				else if(type.equals("type")) {
					parseNode.setOutput(ParseMachine.ruleNode(str));
				}else if(type.equals("contextType")) {
					parseNode.setOutput(ParseMachine.contextRuleNode(str));
				}else if(type.equals("typeClass")) {
					parseNode.setOutput(ParseMachine.ruleNodeTypeClass( ruleNodes.toArray( new RuleNode[ruleNodes.size()] ) ));
				}else if(type.equals("contextTypeClass")) {
					parseNode.setOutput(ParseMachine.contextRuleNodeTypeClass( ruleNodes.toArray( new RuleNode[ruleNodes.size()] ) ));
				}else if(type.equals("negTypeClass")) {
					parseNode.setOutput(ParseMachine.ruleNodeNegTypeClass( ruleNodes.toArray( new RuleNode[ruleNodes.size()] ) ));
				}else {
					throw new Exception("unkown parseNode type: "+type);
				}
				
			}
		});
		
		parseRuleBuilder.addBuildInstruction("flags", new ParseAction() {
			
			HashMap<String,Integer> flagStringToInt = new HashMap<String,Integer>();
			
			@Override
			void init() {
				flagStringToInt.put("NONE", ParseRule.NONE);
				flagStringToInt.put("MERGE", ParseRule.MERGE);
				flagStringToInt.put("REMOVE_OPERATOR", ParseRule.REMOVE_OPERATOR);
				flagStringToInt.put("LOOP", ParseRule.LOOP);
			}
			
			@Override
			void doAction(ParseNode parseNode) throws Exception {
				ParseNode param = parseNode.getNode(0);
				Integer flags = ParseRule.NONE;
				
				for(int i = 0;i<param.size();i++) {
					ParseNode current = param.getNode(i);
					if(current.getType().equals("name")) {
						String flagStr = ((String)current.getOutput()).toUpperCase();
						Integer flag = flagStringToInt.get(flagStr);
						if(flag != null) flags |= flag;
						else throw new Exception("flag not recognized: "+flagStr);
					}
				}
				
				parseNode.setOutput(flags);
			}
		});
		
		parseRuleBuilder.addBuildInstruction("parseRuleList", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) {
				ArrayList<ParseRule> parseRules = new ArrayList<ParseRule>();
				
				for(int i = 0;i<parseNode.size();i++) {
					ParseNode current = parseNode.getNode(i);
					
					if(current.getType().equals("parseRule")) {
						parseRules.add((ParseRule)current.getOutput());
					}
					
				}
				
				
				parseNode.setOutput(parseRules);
			}
			
		});
		
		parseRuleBuilder.addBuildInstruction("parseRule", new ParseAction() {
			@Override
			void doAction(ParseNode parseNode) {
				if(parseNode.hasElementByName("token")) {
					String outType;
					String charSeq;
					
					ParseNode parenParam = parseNode.getElementByName("paren").getNode(0);
					
					outType = (String)parenParam.getNode(0).getOutput();
					charSeq = (String)parenParam.getNode(2).getOutput();
					
					ParseRule tokenRule = ParseMachine.tokenRule(outType, charSeq);
					
					parseNode.setOutput(tokenRule);
				}else {
					ParseNode parseRuleParam = parseNode.getNode(0);
					
					int flags = ParseRule.NONE;
					if(parseRuleParam.hasElementByName("flags")) {
						flags |= (Integer)(parseRuleParam.getElementByName("flags").getOutput());
					}
					
					if(parseRuleParam.hasElementByName("transform")) {
						ParseNode transform = parseRuleParam.getElementByName("transform");
						
						String outType = (String)(transform.getElementByName("name").getOutput());
						
						@SuppressWarnings("unchecked")
						ArrayList<RuleNode> ruleNodes = (ArrayList<RuleNode>)(transform.getElementByName("ruleNodeList").getOutput());
						
						ParseRule parseRule = new ParseRule(flags,outType,ruleNodes.toArray(new RuleNode[ruleNodes.size()]));
						
						parseNode.setOutput(parseRule);
						
					}else if(parseRuleParam.hasElementByName("parseRuleList")) {
						ArrayList<ParseRule> parseRules = new ArrayList<ParseRule>();
						
						ParseRule compositeRule = new ParseRule(flags,parseRules.toArray(new ParseRule[parseRules.size()]));
						parseNode.setOutput(compositeRule);
					}else if(parseRuleParam.hasElementByName("parseRule")){
						ParseRule pr = (ParseRule)(parseRuleParam.getElementByName("parseRule").getOutput());
						
						ParseRule outRule = new ParseRule(flags,pr);
						
						parseNode.setOutput(outRule);
					}
					
				}
			}
		});
		
		
		parseRuleBuilder.init();
		loaded = true;
	}
	
}
