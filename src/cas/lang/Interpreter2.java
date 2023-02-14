package cas.lang;

import java.math.BigInteger;

import cas.Cas;
import cas.SimpleFuncs;
import cas.base.Expr;
import cas.lang.ParseMachine.ObjectBuilder;
import cas.lang.ParseMachine.ParseAction;
import cas.lang.ParseMachine.ParseNode;
import cas.lang.ParseMachine.ParseRule;
import cas.primitive.Num;
import cas.primitive.Sum;

public class Interpreter2 extends Cas{
	static ParseRule bitLogicSyntax = null;
	static ObjectBuilder exprBuilder = null;
	
	public static Expr createExpr(String toParse) {
		//System.out.println("building with new expr builder: "+toParse);
		ParseNode pn = ParseMachine.baseParse(toParse, bitLogicSyntax);
		Expr out = (Expr) exprBuilder.build(pn);
		return out;
	}
	
	public static void init() {
		try {
			bitLogicSyntax = MetaLang.loadLanguageFromFile("resources/bitlogic_syntax.pm");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		exprBuilder = new ObjectBuilder();
		
		exprBuilder.addBuildInstruction("num", new ParseAction() {
			@Override
			void doAction(ParseNode parseNode) throws Exception {
				parseNode.generateBigIntToOutput();
				BigInteger value = (BigInteger)parseNode.getOutput();
				parseNode.setOutput(num(value));
			}
		});
		exprBuilder.addBuildInstruction("var", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				parseNode.generateStringToOutput();
				String varName = (String)parseNode.getOutput();
				if(varName.equals("i")) {
					parseNode.setOutput(Num.I);
					return;
				}
				String lc = varName.toLowerCase();
				if(lc.equals("true")) {
					parseNode.setOutput(bool(true));
				}else if(lc.equals("false")) {
					parseNode.setOutput(bool(false));
				}else {
					parseNode.setOutput(var(varName));
				}
			}
			
		});
		exprBuilder.addBuildInstruction("pow", new ParseAction() {
			@Override
			void doAction(ParseNode parseNode) throws Exception {
				Expr base = (Expr)parseNode.getNode(0).getOutput();
				Expr expo = (Expr)parseNode.getNode(2).getNode(0).getOutput();
				
				if(base == null) throw new Exception("got a null base expr while building pow");
				if(expo == null) throw new Exception("got a null expo expr while building pow");
				
				parseNode.setOutput(power(base,expo));
			}
		});
		exprBuilder.addBuildInstruction("sum", new ParseAction() {
			private static int ADD = 0,SUB = 1;
			
			@Override
			void doAction(ParseNode parseNode) throws Exception {
				Expr sum = sum();
				
				int currentMode = ADD;
				
				for(int i = 0;i<parseNode.size();i++) {
					ParseNode current = parseNode.getNode(i);
					if(current.getType().equals(ParseMachine.CHAR)) {
						if(current.getLeafChar() == '-') currentMode = SUB;
						else if(current.getLeafChar() == '+') currentMode = ADD;
					}else {
						ParseNode param = current.getNode(0);
						Expr expr = (Expr)param.getOutput();
						if(expr == null) throw new Exception("got a null expr while building sum");
						if(currentMode == ADD) {
							sum.add(expr);
						}else if(currentMode == SUB){
							if(expr.typeName().equals("num")) sum.add(((Num)expr).negate());
							else if(expr.typeName().equals("prod")) {
								boolean finished = false;
								for(int j = 0;j < expr.size();j++) {
									Expr prodEl = expr.get(j);
									
									if(prodEl.typeName().equals("num")) {
										expr.set(j, ((Num)prodEl).negate() );
										finished = true;
										break;
									}
								}
								if(!finished) {
									expr.add(num(-1));
								}
								sum.add(expr);
							}
							else sum.add(neg(expr));
						}
					}
				}
				parseNode.setOutput(Sum.unCast(sum));
			}
		});
		exprBuilder.addBuildInstruction("prod", new ParseAction() {
			private static int NUMER = 0,DENOM = 1;
			
			@Override
			void doAction(ParseNode parseNode) throws Exception {
				Expr numer = prod();
				Expr denom = prod();
				
				int currentMode = NUMER;
				
				for(int i = 0;i<parseNode.size();i++) {
					ParseNode current = parseNode.getNode(i);
					if(current.getType().equals(ParseMachine.CHAR)) {
						if(current.getLeafChar() == '/') currentMode = DENOM;
						else if(current.getLeafChar() == '*') currentMode = NUMER;
					}else {
						ParseNode param = current.getNode(0);
						Expr expr = (Expr)param.getOutput();
						if(expr == null) throw new Exception("got a null expr while building sum");
						if(currentMode == NUMER) {
							numer.add(expr);
						}else if(currentMode == DENOM){
							denom.add(expr);
						}
					}
				}
				
				if(denom.size() == 0) {
					parseNode.setOutput(numer);
					return;
				}
				
				if(numer.size() == 1) numer = numer.get();
				if(denom.size() == 1) denom = denom.get();
				
				parseNode.setOutput(div(numer,denom));
			}
		});
		exprBuilder.addBuildInstruction("neg", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				Expr innerExpr = (Expr)parseNode.getNode(1).getOutput();
				if(innerExpr == null) throw new Exception("got a null expr when building neg");
				
				Expr negOut = null;
				if(innerExpr.typeName().equals("num")) {
					negOut = ((Num)innerExpr).negate();
				}else {
					negOut = neg(innerExpr);
				}
				
				parseNode.setOutput(negOut);
			}
			
		});
		
		exprBuilder.addBuildInstruction("paren", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				if(parseNode.size() == 0) return;
				
				if(parseNode.getNode(0).getNode(0).getType().equals("info_list")) {
					//ignore
				}else {
					Expr innerExpr = (Expr)parseNode.getNode(0).getNode(0).getOutput();
					if(innerExpr == null) throw new Exception("got a null expr when building paren");
					
					parseNode.setOutput(innerExpr);
				}
			}
		});
		
		exprBuilder.addBuildInstruction("func", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				String funcName = (String)parseNode.getElementByName("var").getOutput().toString();
				
				ParseNode paren = parseNode.getElementByName("paren");
				Expr func = null;
				
				if(paren.size() == 0) {
					func = SimpleFuncs.getFuncByName(funcName);
				}else {
					Expr[] paramsArray = null;
					ParseNode parenContents = paren.getNode(0);
					if(parenContents.hasElementByName("info_list")) {//many elements
						ParseNode infoList = parenContents.getElementByName("info_list");
						int nElements = infoList.size();
						
						paramsArray = new Expr[nElements];
						for(int i = 0;i<nElements;i++) {
							paramsArray[i] = (Expr)infoList.getNode(i).getNode(0).getOutput();
						}
						
					}else {
						int nElements = 1;
						
						paramsArray = new Expr[nElements];
						
						paramsArray[0] = (Expr)parenContents.getNode(0).getOutput();
					}
					func = SimpleFuncs.getFuncByName(funcName,paramsArray);
				}
				parseNode.setOutput(func);
			}
			
		});
		
		exprBuilder.addBuildInstruction("becomes", new ParseAction() {
			@Override
			void doAction(ParseNode parseNode) throws Exception {
				Expr fromExpr = (Expr)parseNode.getNode(0).getNode(0).getOutput();
				Expr toExpr = (Expr)parseNode.getNode(1).getNode(0).getOutput();
				
				if(fromExpr == null) throw new Exception("got a null from expr while building becomes");
				if(toExpr == null) throw new Exception("got a null to expr while building becomes");
				
				parseNode.setOutput(becomes(fromExpr,toExpr));
			}
		});
		
		exprBuilder.addBuildInstruction("less", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				Expr leftSide = (Expr)parseNode.getNode(0).getNode(0).getOutput();
				Expr rightSide = (Expr)parseNode.getNode(1).getNode(0).getOutput();
				
				if(leftSide == null) throw new Exception("got a null left expr while building less");
				if(rightSide == null) throw new Exception("got a null right expr while building less");
				
				parseNode.setOutput(equLess(leftSide,rightSide));
			}
			
		});
		
		exprBuilder.addBuildInstruction("greater", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				Expr leftSide = (Expr)parseNode.getNode(0).getNode(0).getOutput();
				Expr rightSide = (Expr)parseNode.getNode(1).getNode(0).getOutput();
				
				if(leftSide == null) throw new Exception("got a null left expr while building greater");
				if(rightSide == null) throw new Exception("got a null right expr while building greater");
				
				parseNode.setOutput(equGreater(leftSide,rightSide));
			}
			
		});
		
		exprBuilder.addBuildInstruction("equ", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				Expr leftSide = (Expr)parseNode.getNode(0).getNode(0).getOutput();
				Expr rightSide = (Expr)parseNode.getNode(1).getNode(0).getOutput();
				
				if(leftSide == null) throw new Exception("got a null left expr while building equ");
				if(rightSide == null) throw new Exception("got a null right expr while building equ");
				
				parseNode.setOutput(equ(leftSide,rightSide));
			}
			
		});
		
		exprBuilder.addBuildInstruction("and", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				Expr and = and();
				
				for(int i = 0;i<parseNode.size();i++) {
					ParseNode current = parseNode.getNode(i);
					Expr currentExpr = (Expr)current.getNode(0).getOutput();
					if(currentExpr == null) throw new Exception("got a null expr while building and");
					and.add(currentExpr);
				}
				
				parseNode.setOutput(and);
			}
		});
		
		exprBuilder.addBuildInstruction("or", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				Expr or = or();
				
				for(int i = 0;i<parseNode.size();i++) {
					ParseNode current = parseNode.getNode(i);
					Expr currentExpr = (Expr)current.getNode(0).getOutput();
					if(currentExpr == null) throw new Exception("got a null expr while building or");
					or.add(currentExpr);
				}
				
				parseNode.setOutput(or);
			}
		});
		
		exprBuilder.addBuildInstruction("not", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				Expr innerExpr = (Expr)parseNode.getNode(0).getNode(0).getOutput();
				if(innerExpr == null) throw new Exception("got a null expr while building not");
				
				Expr outExpr = not(innerExpr);
				
				parseNode.setOutput(outExpr);
			}
			
		});
		
		exprBuilder.addBuildInstruction("ternary", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				ParseNode header = parseNode.getElementByName("ternary_header");
				
				Expr headerExpr = (Expr)header.getNode(0).getNode(0).getOutput();
				
				ParseNode ternaryResults = parseNode.getNode(1).getNode(0);

				Expr ifTrue = (Expr)ternaryResults.getNode(0).getNode(0).getOutput();
				Expr ifFalse = (Expr)ternaryResults.getNode(1).getNode(0).getOutput();
				
				if(headerExpr == null) throw new Exception("got a null expr in header while building ternary expression");
				if(ifTrue == null) throw new Exception("got a null expr in true output while building ternary expression");
				if(ifFalse == null) throw new Exception("got a null expr in false output while building ternary expression");
				
				Expr ternaryOut = ternary(headerExpr, ifTrue, ifFalse);
				
				parseNode.setOutput(ternaryOut);
			}
			
		});
		
		exprBuilder.addBuildInstruction("set", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				ParseNode infoList = parseNode.getNode(0).getNode(0);
				Expr exprSet = exprSet();
				
				for(int i = 0;i<infoList.size();i++) {
					Expr currentExpr = (Expr)infoList.getNode(i).getNode(0).getOutput();
					if(currentExpr == null) throw new Exception("got a null expr in list of set expr");
					exprSet.add(currentExpr);
				}
				
				parseNode.setOutput(exprSet);
			}
			
		});
		
		exprBuilder.addBuildInstruction("sequence", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				ParseNode infoList = parseNode.getNode(0).getNode(0);
				Expr exprSequence = sequence();
				
				for(int i = 0;i<infoList.size();i++) {
					Expr currentExpr = (Expr)infoList.getNode(i).getNode(0).getOutput();
					if(currentExpr == null) throw new Exception("got a null expr in list of sequence expr");
					exprSequence.add(currentExpr);
				}
				
				parseNode.setOutput(exprSequence);
			}
			
		});
		
		exprBuilder.addBuildInstruction("float", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				parseNode.generateStringToOutput();
				Expr floatExpr = Cas.floatExpr((String)parseNode.getOutput());
				parseNode.setOutput(floatExpr);
			}
			
		});
		
		exprBuilder.addBuildInstruction("dot", new ParseAction() {

			@Override
			void doAction(ParseNode parseNode) throws Exception {
				Expr dotExpr = dot();
				
				for(int i = 0;i<parseNode.size();i++) {
					Expr dotEl = (Expr)parseNode.getNode(i).getNode(0).getOutput();
					if(dotEl == null) throw new Exception("got a null expr while building dot expr");
					dotExpr.add(dotEl);
				}
				
				parseNode.setOutput(dotExpr);
			}
			
		});
		
		exprBuilder.init();
	}
}
