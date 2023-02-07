package cas.lang;

import cas.lang.ParseMachine.ParseNode;
import cas.lang.ParseMachine.ParseRule;
import cas.lang.ParseMachine.ObjectBuilder;

public class MetaLang {

	static ParseRule metaLangDef = null;
	static ObjectBuilder parseRuleBuilder = null;
	
	public static void test() {
		MetaLang.init();
		String test = ParseMachine.getFileAsString("resources/example_syntax.pm");
		System.out.println(test);
		ParseNode out = ParseMachine.baseParse(test, metaLangDef);
		System.out.println(out);
	}
	
	public static void init() {
		
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
				
				//quote character escape sequence for strings
				ParseMachine.tokenRule("quote char", "\\\""),
			   
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
		
	}
	
}
