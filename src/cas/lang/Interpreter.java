package cas.lang;

import java.math.BigInteger;

import cas.Cas;
import cas.base.Expr;
import cas.base.FunctionsLoader;
import cas.lang.ParseMachine.ObjectBuilder;
import cas.lang.ParseMachine.ParseAction;
import cas.lang.ParseMachine.ParseNode;
import cas.primitive.Num;
import cas.primitive.Sum;

public class Interpreter extends Cas{
	private static boolean loaded = false;
	
	static ObjectBuilder exprBuilder = null;
	
	public static Expr createExpr(String toParse) {
		//System.out.println("building with new expr builder: "+toParse);
		Expr out = (Expr) exprBuilder.build(toParse);
		return out;
	}
	
	public static void init() {
		if(loaded) return;
		
		System.out.println("- Loading BitLogic expression builder...");
		
		exprBuilder = new ObjectBuilder();
		try {
			exprBuilder.setLang("resources/bitlogic_syntax.pm");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		exprBuilder.addBuildInstruction("num", new ParseAction() {
			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				parseNode.generateBigIntToOutput();
				BigInteger value = (BigInteger)parseNode.getOutput();
				return num(value);
			}
		});
		exprBuilder.addBuildInstruction("var", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				parseNode.generateStringToOutput();
				String varName = (String)parseNode.getOutput();
				if(varName.equals("i")) {
					return Num.I;
				}
				String lc = varName.toLowerCase();
				if(lc.equals("true")) {
					return bool(true);
				}else if(lc.equals("false")) {
					return bool(false);
				}else {
					return var(varName);
				}
			}
			
		});
		exprBuilder.addBuildInstruction("pow", new ParseAction() {
			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				Expr base = (Expr)parseNode.getNode(0).getOutput();
				Expr expo = (Expr)parseNode.getNode(2).getNode(0).getOutput();
				
				if(base == null) throw new Exception("got a null base expr while building pow");
				if(expo == null) throw new Exception("got a null expo expr while building pow");
				
				return power(base,expo);
			}
		});
		exprBuilder.addBuildInstruction("sum", new ParseAction() {
			private static int ADD = 0,SUB = 1;
			
			@Override
			Object doAction(ParseNode parseNode) throws Exception {
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
							if(expr.isType("num")) sum.add(((Num)expr).negate());
							else if(expr.isType("prod")) {
								boolean finished = false;
								for(int j = 0;j < expr.size();j++) {
									Expr prodEl = expr.get(j);
									
									if(prodEl.isType("num")) {
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
				return Sum.unCast(sum);
			}
		});
		exprBuilder.addBuildInstruction("prod", new ParseAction() {
			private static int NUMER = 0,DENOM = 1;
			
			@Override
			Object doAction(ParseNode parseNode) throws Exception {
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
					return numer;
				}
				
				if(numer.size() == 1) numer = numer.get();
				if(denom.size() == 1) denom = denom.get();
				
				return div(numer,denom);
			}
		});
		exprBuilder.addBuildInstruction("neg", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				Expr innerExpr = (Expr)parseNode.getNode(1).getOutput();
				if(innerExpr == null) throw new Exception("got a null expr when building neg");
				
				Expr negOut = null;
				if(innerExpr.isType("num")) {
					negOut = ((Num)innerExpr).negate();
				}else {
					negOut = neg(innerExpr);
				}
				
				return negOut;
			}
			
		});
		
		exprBuilder.addBuildInstruction("paren", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				if(parseNode.size() == 0) return null;
				
				if(parseNode.getNode(0).getNode(0).getType().equals("info_list")) {
					//ignore
					return null;
				}else {
					Expr innerExpr = (Expr)parseNode.getNode(0).getNode(0).getOutput();
					if(innerExpr == null) throw new Exception("got a null expr when building paren");
					
					return innerExpr;
				}
			}
		});
		
		exprBuilder.addBuildInstruction("func", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				String funcName = (String)parseNode.getElementByName("var").getOutput().toString();
				
				ParseNode paren = parseNode.getElementByName("paren");
				Expr func = null;
				
				if(paren.size() == 0) {
					func = FunctionsLoader.getFuncByName(funcName);
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
					func = FunctionsLoader.getFuncByName(funcName,paramsArray);
				}
				return func;
			}
			
		});
		
		exprBuilder.addBuildInstruction("define", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				Expr leftSide = (Expr)parseNode.getNode(0).getNode(0).getOutput();
				Expr rightSide = (Expr)parseNode.getNode(1).getNode(0).getOutput();
				
				if(leftSide == null) throw new Exception("got a null leftSide expr while building define");
				if(rightSide == null) throw new Exception("got a null rightSide expr while building define");
				
				return define(leftSide,rightSide);
			}
			
		});
		
		exprBuilder.addBuildInstruction("becomes", new ParseAction() {
			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				Expr fromExpr = (Expr)parseNode.getNode(0).getNode(0).getOutput();
				Expr toExpr = (Expr)parseNode.getNode(1).getNode(0).getOutput();
				
				if(fromExpr == null) throw new Exception("got a null from expr while building becomes");
				if(toExpr == null) throw new Exception("got a null to expr while building becomes");
				
				return becomes(fromExpr,toExpr);
			}
		});
		
		exprBuilder.addBuildInstruction("less", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				Expr leftSide = (Expr)parseNode.getNode(0).getNode(0).getOutput();
				Expr rightSide = (Expr)parseNode.getNode(1).getNode(0).getOutput();
				
				if(leftSide == null) throw new Exception("got a null left expr while building less");
				if(rightSide == null) throw new Exception("got a null right expr while building less");
				
				return equLess(leftSide,rightSide);
			}
			
		});
		
		exprBuilder.addBuildInstruction("greater", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				Expr leftSide = (Expr)parseNode.getNode(0).getNode(0).getOutput();
				Expr rightSide = (Expr)parseNode.getNode(1).getNode(0).getOutput();
				
				if(leftSide == null) throw new Exception("got a null left expr while building greater");
				if(rightSide == null) throw new Exception("got a null right expr while building greater");
				
				return equGreater(leftSide,rightSide);
			}
			
		});
		
		exprBuilder.addBuildInstruction("equ", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				Expr leftSide = (Expr)parseNode.getNode(0).getNode(0).getOutput();
				Expr rightSide = (Expr)parseNode.getNode(1).getNode(0).getOutput();
				
				if(leftSide == null) throw new Exception("got a null left expr while building equ");
				if(rightSide == null) throw new Exception("got a null right expr while building equ");
				
				return equ(leftSide,rightSide);
			}
			
		});
		
		exprBuilder.addBuildInstruction("and", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				Expr and = and();
				
				for(int i = 0;i<parseNode.size();i++) {
					ParseNode current = parseNode.getNode(i);
					Expr currentExpr = (Expr)current.getNode(0).getOutput();
					if(currentExpr == null) throw new Exception("got a null expr while building and");
					and.add(currentExpr);
				}
				
				return and;
			}
		});
		
		exprBuilder.addBuildInstruction("or", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				Expr or = or();
				
				for(int i = 0;i<parseNode.size();i++) {
					ParseNode current = parseNode.getNode(i);
					Expr currentExpr = (Expr)current.getNode(0).getOutput();
					if(currentExpr == null) throw new Exception("got a null expr while building or");
					or.add(currentExpr);
				}
				
				return or;
			}
		});
		
		exprBuilder.addBuildInstruction("not", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				Expr innerExpr = (Expr)parseNode.getNode(0).getNode(0).getOutput();
				if(innerExpr == null) throw new Exception("got a null expr while building not");
				
				return not(innerExpr);
			}
			
		});
		
		exprBuilder.addBuildInstruction("ternary", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				ParseNode header = parseNode.getElementByName("ternary_header");
				
				Expr headerExpr = (Expr)header.getNode(0).getNode(0).getOutput();
				
				ParseNode ternaryResults = parseNode.getNode(1).getNode(0);

				Expr ifTrue = (Expr)ternaryResults.getNode(0).getNode(0).getOutput();
				Expr ifFalse = (Expr)ternaryResults.getNode(1).getNode(0).getOutput();
				
				if(headerExpr == null) throw new Exception("got a null expr in header while building ternary expression");
				if(ifTrue == null) throw new Exception("got a null expr in true output while building ternary expression");
				if(ifFalse == null) throw new Exception("got a null expr in false output while building ternary expression");
				
				Expr ternaryOut = ternary(headerExpr, ifTrue, ifFalse);
				
				return ternaryOut;
			}
			
		});
		
		exprBuilder.addBuildInstruction("set", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				if(parseNode.size() == 0 ) {
					return exprSet();
				}else if(parseNode.getNode(0).getNode(0).getType().equals("info_list")) {
					ParseNode infoList = parseNode.getNode(0).getNode(0);
					Expr exprSet = exprSet();
					
					for(int i = 0;i<infoList.size();i++) {
						Expr currentExpr = (Expr)infoList.getNode(i).getNode(0).getOutput();
						if(currentExpr == null) throw new Exception("got a null expr in list of set expr");
						exprSet.add(currentExpr);
					}
					
					return exprSet;
				}else {
					return exprSet((Expr)parseNode.getNode(0).getNode(0).getOutput());
				}
			}
			
		});
		
		exprBuilder.addBuildInstruction("sequence", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				if(parseNode.size() == 0 ) {
					return sequence();
				}else if(parseNode.getNode(0).getNode(0).getType().equals("info_list")) {
					ParseNode infoList = parseNode.getNode(0).getNode(0);
					Expr exprSequence = sequence();
					
					for(int i = 0;i<infoList.size();i++) {
						Expr currentExpr = (Expr)infoList.getNode(i).getNode(0).getOutput();
						if(currentExpr == null) throw new Exception("got a null expr in list of sequence expr");
						exprSequence.add(currentExpr);
					}
					
					return exprSequence;
				}else {
					return sequence((Expr)parseNode.getNode(0).getNode(0).getOutput());
				}
			}
			
		});
		
		exprBuilder.addBuildInstruction("float", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				parseNode.generateFloat64ToOutput();
				Expr floatExpr = Cas.floatExpr((Double)parseNode.getOutput());
				return floatExpr;
			}
			
		});
		
		exprBuilder.addBuildInstruction("dot", new ParseAction() {

			@Override
			Object doAction(ParseNode parseNode) throws Exception {
				Expr dotExpr = dot();
				
				for(int i = 0;i<parseNode.size();i++) {
					Expr dotEl = (Expr)parseNode.getNode(i).getNode(0).getOutput();
					if(dotEl == null) throw new Exception("got a null expr while building dot expr");
					dotExpr.add(dotEl);
				}
				
				return dotExpr;
			}
			
		});
		
		exprBuilder.init();
		loaded = true;
		System.out.println("- Done loading BitLogic expression builder!");
	}
}
