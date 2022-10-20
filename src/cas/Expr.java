/*
 * Benjamin R Currie, BitLogic Program
 * some code/equations is copied from online but I try to remember to site the source
 */


package cas;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import cas.bool.BoolState;
import cas.primitive.*;

/*
 * The Expr class is a description of what an expression is
 * 
 * most expression of child node expressions, only certain types of expressions are the leafs
 * most of java class files are inheriting the Expr class
 * Too see the entire tree the print Tree function can be used
 * 
 * the simplify call is when the expression is processed to a more useful form, not just algebra rewrites but also operator expansion like the derivative and integrals
 * the simplify call is not guaranteed to output a less complex equation, just a decision of what makes sense based on my heuristic
 * The modifyFromExample function can transform many steps into one step by mapping one expression to a result expression with the same meaning
 * 
 */

public abstract class Expr extends Cas{
	
	static Random random;
	public boolean simplifyChildren = true;
	public Flags flags = new Flags();
	private ArrayList<Expr> subExpr = new ArrayList<Expr>();//many expression types have sub expressions like sums
	
	public abstract Rule getRule();
	public abstract String help();
	
	public Rule getDoneRule() {//post processing rule
		return null;
	}
	
	public abstract ComplexFloat convertToFloat(ExprList varDefs);
	public abstract String typeName();
	
	public void print() {
		System.out.print(this);
	}
	public void println() {
		System.out.println(this);
	}
	
	public boolean isMutable(){
		return flags.mutable;
	}
	
	public void setMutable(boolean choice){
		flags.mutable = choice;
	}
	
	/*
	 * flags attempt to reduce the amount of work needed during simplification
	 * if an expression finishes a simplify call it is made simple such that it will not simplify again unless
	 * it is modified
	 * 
	 * sorted is weather or not a sum,product or list is sorted or not into a determinate order
	 * 
	 * 
	 * 
	 * copying an expression keeps the original flags
	 */
	public static class Flags implements Serializable{
		private static final long serialVersionUID = -2823404902493132716L;
		
		public boolean simple = false;
		public boolean sorted = false;
		public boolean mutable = true;
		
		public void set(Flags other) {
			simple = other.simple;
			sorted = other.sorted;
		}
		
		public void reset() {
			simple = false;
			sorted = false;
			mutable = true;
		}
		@Override
		public String toString(){
			return "sorted: "+sorted+", simple: "+simple;
		}
	}
	
	public void fullFlagReset() {
		this.flags.reset();
		for(int i = 0;i < subExpr.size();i++){
			Expr current = subExpr.get(i);
			current.fullFlagReset();
		}
	}
	
	public static boolean USE_RECUSION_SAFTEY = false;
	
	public static int RECURSION_SAFETY;
	
	public Expr simplify(CasInfo casInfo){//all expressions will have a simplify call, this is the most important function 
		
		if(!Rule.rulesLoaded()) throw new RuntimeException("you are simplifying before all rules are loaded");
		
		if(Thread.currentThread().isInterrupted()) return null;
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		if(this instanceof Var) return casInfo.definitions.getVar(toString());//find variable in definitions
		
		Rule rule = getRule();
		
		
		RECURSION_SAFETY++;
		if(USE_RECUSION_SAFTEY && RECURSION_SAFETY>256) {
			System.err.println("RECURSION DETECTED");
			return toBeSimplified;
		}
		//System.out.println(toBeSimplified);
		if(simplifyChildren || get().typeName().equals("result")) toBeSimplified.simplifyChildren(casInfo);//simplify sub expressions, result function can override this behavior
		
		
		if(rule != null) toBeSimplified = rule.applyRuleToExpr(toBeSimplified, casInfo);
		
		
		Rule doneRule = getDoneRule();
		if(doneRule != null) toBeSimplified = doneRule.applyRuleToExpr(toBeSimplified, casInfo);//give it the original problem to have access to variables
		
		toBeSimplified.flags.simple = true;//result is simplified and should not be simplified again
		
		RECURSION_SAFETY--;
		return toBeSimplified;
	}
	
	public Expr copy(){//copying this expression object
		Expr out = null;
		try {
			out = this.getClass().getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(int i = 0;i<size();i++) {
			out.add(get(i).copy());
		}
		out.flags.set(flags);
		return out;
		
	}
	
	public Var getVar(){
		return null;
	}
	
	@Override
	public String toString(){
		String out = typeName()+"(";
		for(int i = 0;i<size();i++){
			out+=get(i);
			if(i!=size()-1) out+=",";
		}
		out+=")";
		return out;
	}
	
	public boolean exactlyEquals(Object other){
		if(other instanceof Expr){
			Expr casted = (Expr)other;
			if(casted.equals(this)) return true;
			return factor(sub(this,casted)).simplify(CasInfo.normal).equals(num(0));		
		}
		return false;
	}
	
	@Override
	public boolean equals(Object other){//compares if two expression have the same tree
		if(other == null || !(other instanceof Expr)) return false;
		if(other == this) return true;
		Expr otherCasted = (Expr)other;
		if(other instanceof Expr && ((Expr)other).typeName().equals(typeName())) {//make sure same type
			
			if(otherCasted.size() == size()) {//make sure they are the same size
				
				if(this.isCommutative()){
					boolean usedIndex[] = new boolean[size()];//keep track of what indices have been used
					int length = otherCasted.size();//length of the lists
					
					outer:for(int i = 0;i < length;i++) {
						for(int j = 0;j < length;j++) {
							if(usedIndex[j]) continue;
							if(get(i).equals(otherCasted.get(j))) {
								usedIndex[j] = true;
								continue outer;
							}
						}
						return false;//the subExpr was never found 
					}
					
					return true;//they are the same as everything was found
				}
				for(int i = 0;i<size();i++){//not commutative or sorted
					if(!get(i).equals(otherCasted.get(i))) return false;
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int sum = this.typeName().hashCode()+1928372;
		if(this.isCommutative()){
			for(int i = 0;i<size();i++){
				sum+=get(i).hashCode()*1092571862;
			}
		}else{
			int randomNum = -623630115;
			for(int i = 0;i<size();i++){
				sum+=get(i).hashCode()*randomNum;
				randomNum = randomNum*randomNum+939083489;
			}
		}
		return sum;
	}
	
	@Override
	public Object clone(){//deep copy of expression
		return copy();
	}
	
	public Expr removeCoefficients() {
		if(this instanceof Num) return num(1);
		else if(this instanceof Prod) {
			Prod prodCopy = (Prod)copy();
			for(int i = 0;i<prodCopy.size();i++) {
				if(prodCopy.get(i) instanceof Num) {
					prodCopy.remove(i);
					i--;
				}
			}
			return Prod.unCast(prodCopy);
		}else if(this.typeName().equals("div")) {
			Func casted = (Func)this;
			return Div.unCast( div(casted.getNumer().removeCoefficients(),casted.getDenom().removeCoefficients()));
		}
		return copy();
	}
	
	public boolean negative() {//Assumes its already simplified
		if(this instanceof Num) return ((Num)this).signum() == -1;
		else if(this instanceof Prod) {
			for(int i = 0;i<size();i++) {
				if(get(i) instanceof Num) {
					if(((Num)get(i)).signum() == -1) return true;
				}
			}
		}else if(this.typeName().equals("div")) {
			Func casted = (Func)this;
			return casted.getNumer().negative() || casted.getDenom().negative();
		}else if(this instanceof Sum) {
			Sum casted = (Sum)this;
			
			int highest  = Integer.MIN_VALUE;
			int index = 0;
			for(int i = 0;i<casted.size();i++) {
				int current = casted.get(i).removeCoefficients().hashCode();//its very important to use the absolute value
				if(current>highest) {
					highest = current;
					index = i;
				}
			}
			
			Expr lowestHashExpr = casted.get(index);
			return lowestHashExpr.negative();
			
		}
		return false;
	}
	
	public boolean isCommutative(){
		return false;
	}
	
	/*
	 * this behaves like an absolute value but numbers sign is based on a special rule to maintain sign
	 * -2+3*i -> 2-3*i
	 * -i -> i
	 * 2-3*i -> 2-3*i
	 * 
	 * basically if it has a real and imaginary component, the real value takes precedence otherwise use the imaginary
	 * component
	 */
	public Expr strangeAbs(CasInfo casInfo) {//Assumes its already simplified
		if(this instanceof Num) return ((Num)this).strangeAbs();
		else if(this instanceof Prod) {
			for(int i = 0;i<size();i++) {
				if(get(i) instanceof Num) {
					if(((Num)get(i)).signum() == -1) {
						Expr copy = copy();
						copy.set(i, ((Num)get(i)).strangeAbs() );
						return copy.simplify(casInfo);
					}
				}
			}
		}else if(this.typeName().equals("div")) {
			Func casted = (Func)this;
			return div(casted.getNumer().strangeAbs(casInfo), casted.getDenom().strangeAbs(casInfo)).simplify(casInfo);
		}
		return copy();
	}
	
	
	public Expr getCoefficient() {
		if(this instanceof Prod) {
			for(int i = 0;i<size();i++) if(get(i) instanceof Num) return get(i).copy();
		}else if(this instanceof Num) {
			return copy();
		}
		else if(this.typeName().equals("div")) {
			Func casted = (Func)this;
			Num numerCoef = (Num)casted.getNumer().getCoefficient();
			Num denomCoef = (Num)casted.getDenom().getCoefficient();
			return Div.unCast(div(numerCoef,denomCoef));
			
		}
		return num(1);
	}
	//returns if the expression is in this, not full proof for products and sums
	public boolean contains(Expr expr) {
		if(this.equals(expr)) return true;
		for(int i = 0;i<subExpr.size();i++){
			Expr e = subExpr.get(i);
			if(e.contains(expr)) return true;
		}
		return false;
	}
	
	//returns if the expression contains any variables
	public boolean containsVars() {
		if(this instanceof Var && ((Var)this).isGeneric()) return true;
		for(int i = 0;i<subExpr.size();i++){
			Expr e = subExpr.get(i);
			if(e.containsVars()) return true;
		}
		return false;
	}
	
	//counts the variables in an expression into the provided arrayList and sorts them by frequency
	public void countVars(ArrayList<VarCount> varcounts) {
		if(this instanceof Var && ((Var)this).isGeneric()) {
			for(int i = 0;i<varcounts.size();i++) {//search
				if(varcounts.get(i).v.equals(this)) {
					varcounts.get(i).count++;
					return;
				}
			}
			varcounts.add(new VarCount((Var)copy(),1));
		}
		else{
			for(int i = 0;i<subExpr.size();i++){
				Expr e = subExpr.get(i);
				e.countVars(varcounts);
			}
		}
		Collections.sort(varcounts);
	}
	
	/*
	 * a score of how complex the expression is, this counts the number of nodes in the expression
	 */
	public int complexity() {
		int sum = 1;
		for(int i = 0;i<subExpr.size();i++) {
			Expr e = subExpr.get(i);
			sum+= e.complexity();
		}
		return sum;
	}
	
	public boolean containsType(String typeName) {//used for faster processing
		if(typeName().equals(typeName)) {
			return true;
		}
		for(int i = 0; i< size();i++) {
			if(get(i).containsType(typeName)) return true;
		}
		return false;
	}
	
	public Expr replace(Equ equ) {
		ExprList l = new ExprList();
		l.add(equ);
		return replace(l);
	}
	
	
	
	public void add(Expr e) {
		if(flags.mutable == false) throw new RuntimeException("expression is not mutable!");
		flags.reset();
		subExpr.add(e);
	}
	public void add(int i,Expr e) {
		if(flags.mutable == false) throw new RuntimeException("expression is not mutable!");
		flags.reset();
		subExpr.add(i,e);
	}
	public void remove(int index) {
		if(flags.mutable == false) throw new RuntimeException("expression is not mutable!");
		flags.reset();
		subExpr.remove(index);
	}
	public void set(int index,Expr e) {
		if(flags.mutable == false) throw new RuntimeException("expression is not mutable!");
		flags.reset();
		subExpr.set(index, e);
	}
	public Expr get(int index) {
		return subExpr.get(index);
	}
	public Expr get() {
		return subExpr.get(0);
	}
	public int size() {
		return subExpr.size();
	}
	public void clear() {
		if(flags.mutable == false) throw new RuntimeException("expression is not mutable!");
		flags.reset();
		subExpr.clear();
	}
	
	private static int priorityNum(Expr e) {
		int priority = 0;
		
		if(e.typeName().equals("var")) priority = 2;
		else if(e.typeName().equals("num")) priority = 1;
		else priority = e.typeName().hashCode();
		
		return Math.abs(priority);
	}
	
	public void sort(ArrayList<VarCount> varcounts) {
		
		if(!flags.sorted) {
			if (varcounts == null) {
				varcounts = new ArrayList<VarCount>();
				countVars(varcounts);
			}
			if(this instanceof Sum || this instanceof Prod || this instanceof ExprList) {
				boolean wasSimple = flags.simple;
				
				final ArrayList<VarCount> varcountsConst = varcounts;
				subExpr.sort(new Comparator<Expr>() {//sort based on variable frequency then type priority then by complexity then by priority of child comparison
					@Override
					public int compare(Expr first, Expr second) {
						
						if(first instanceof Num && second instanceof Num) return ((Num)first).getRealValue().compareTo(((Num)second).getRealValue());
						
						if(first instanceof Var && second instanceof Var) {//sort for frequency
							for(int i = 0;i<varcountsConst.size();i++) {
								VarCount vc = varcountsConst.get(i);
								
								boolean firstSame = first.equals(vc.v);
								boolean secondSame = second.equals(vc.v);
								
								if(firstSame && !secondSame) {
									return -1;
								}else if(!firstSame && secondSame) {
									return 1;
								}
								
							}
						}
						
						int fPriority = priorityNum(first);
						int sPriority = priorityNum(second);
						
						if(fPriority != sPriority) {
							return -Integer.compare(fPriority, sPriority);
						}
						int fComplexity = first.complexity();
						int sComplexity = second.complexity();
						
						if(fComplexity != sComplexity) {
							return -Integer.compare(fComplexity, sComplexity);
						}
						
						if(fComplexity>1 && sComplexity>1) {
							int minSize = Math.min(first.size(),second.size());
							for(int i = 0;i<minSize;i++) {//search for a difference in type
								int comparison = compare(first.get(i), second.get(i));
								if(comparison != 0) {
									return comparison;
								}
							}
						}
						
						return 0;
					}
				});
				
				flags.simple = wasSimple;
			}
			
			for(int i = 0;i<size();i++) {
				get(i).flags.sorted = false;//we have to reset the sort because parent was not sorted so varcounts may change
				get(i).sort(varcounts);
			}
			
			flags.sorted = true;
		}
		
	}
	
	public void sort() {
		sort(null);
	}
	
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equals(e.getLeftSide())) return e.getRightSide().copy();
		}
		
		Expr replacedChildren = copy();
		
		for(int i = 0;i<replacedChildren.size();i++) {
			replacedChildren.set(i, get(i).replace(equs));
		}
		return replacedChildren;
	}
	
	public void simplifyChildren(CasInfo casInfo) {
		for(int i = 0;i<subExpr.size();i++) {
			Expr temp = subExpr.get(i);
			if(!temp.flags.simple) {
				temp = temp.simplify(casInfo);
				subExpr.set(i, temp);
			}
		}
	}
	
	public void simplifyChildren(CasInfo casInfo,String ignoredType) {
		for(int i = 0;i<subExpr.size();i++) {
			Expr temp = subExpr.get(i);
			if(!temp.flags.simple) {
				if(temp.containsType(ignoredType)) {
					temp.simplifyChildren(casInfo, ignoredType);
				}else {
					temp = temp.simplify(casInfo);
					subExpr.set(i, temp);
				}
			}
		}
	}
	
	public static void serializedSaveExpr(Expr expr,String fileName)throws IOException{
	    FileOutputStream fos = new FileOutputStream("saves/"+fileName);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(expr);
	    oos.close();
	}

	public static Expr serializedOpenExpr(String fileName)throws IOException, ClassNotFoundException{
	   FileInputStream fin = new FileInputStream("saves/"+fileName);
	   ObjectInputStream ois = new ObjectInputStream(fin);
	   Expr expr = (Expr) ois.readObject();
	   ois.close();
	   expr.flags.reset();//this is important when retrying something in a new version
	   return expr;
	}
	/*
	 * shows the entire expression structure without resorting or fancy printing
	 * used for debugging
	 */
	public String toStringTree(int tab) {
		String out = "";
		
		if(tab == 0){
			out+=("EXPR-TREE-START-----------: flags: "+flags)+"\n";
		}
		String tabStr = "";
		if(tab>0) tabStr+="|";
		for(int i = 0;i<tab-1;i++) tabStr+="   |";
		if(tab>0) tabStr+="--->";
		else tabStr+=">";
		out+=(tabStr+ typeName() +" hash: "+hashCode());
		if(this instanceof Num || this instanceof Var || this instanceof FloatExpr || this instanceof BoolState) out+=(" name: "+this);
		out+="\n";
		if(this instanceof Func) {
			out+="rules: "+((Func)this).getRule();
			out+="\n";
		}
		for(int i = 0;i<size();i++) {
			out+=get(i).toStringTree(tab+1);
		}
		if(tab == 0) out+=("EXPR-TREE-END-------------")+"\n";
		
		return out;
	}
	
}
