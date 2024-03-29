/*
 * Benjamin R Currie, BitLogic Program
 * some code/equations is copied from online but I try to remember to site the source
 */


package cas.base;

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

import cas.Algorithms;
import cas.bool.BoolState;
import cas.primitive.*;

import static cas.Cas.*;

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

public abstract class Expr{
	
	public static Random random;
	public Flags flags = new Flags();
	private ArrayList<Expr> subExprs = new ArrayList<Expr>();//many expression types have sub expressions like sums
	
	public abstract Rule getRule();
	public abstract String help();
	
	public Rule getDoneRule() {//post processing rule
		return null;
	}
	
	public abstract ComplexFloat convertToFloat(Func varDefsSet);
	public abstract String typeName();
	
	public boolean isType(String name) {
		return typeName().equals(name);
	}
	
	public void print() {
		System.out.print(this);
	}
	public void println() {
		System.out.println(this);
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
		
		private boolean simple = false;
		private boolean sorted = false;
		private boolean mutable = true;
		
		public void set(Flags other) {
			simple = other.simple;
			sorted = other.sorted;
			//we ignore the mutable flag as this function is used for copying
		}
		
		public void reset() {
			simple = false;
			sorted = false;
			mutable = true;
		}
		
		@Override
		public String toString(){
			return "sorted: "+sorted+", simple: "+simple+", mutable: "+mutable;
		}
	}
	
	public boolean isMutable(){
		return flags.mutable;
	}
	
	public void setMutableSingleNode(boolean choice){
		flags.mutable = choice;
	}
	
	public void setMutableFullTree(boolean choice) {
		setMutableSingleNode(choice);
		for(int i = 0;i < subExprs.size();i++){
			subExprs.get(i).setMutableFullTree(choice);
		}
	}
	
	public boolean isSorted() {
		return flags.sorted;
	}
	
	public void setSortedSingleNode(boolean choice){
		flags.sorted = choice;
	}
	
	public void setSortedFullTree(boolean choice) {
		setSortedSingleNode(choice);
		for(int i = 0;i < subExprs.size();i++){
			subExprs.get(i).setSortedFullTree(choice);
		}
	}
	
	public boolean isSimple() {
		return flags.simple;
	}
	
	public void setSimpleSingleNode(boolean choice){
		flags.simple = choice;
	}
	
	public void setSimpleFullTree(boolean choice) {
		setSimpleSingleNode(choice);
		for(int i = 0;i < subExprs.size();i++){
			subExprs.get(i).setSimpleFullTree(choice);
		}
	}
	
	public void fullFlagReset() {
		this.flags.reset();
		for(int i = 0;i < subExprs.size();i++){
			Expr current = subExprs.get(i);
			current.fullFlagReset();
		}
	}
	
	public static boolean USE_RECUSION_SAFTEY = false;
	
	public static int RECURSION_SAFETY;
	
	public Expr simplify(CasInfo casInfo){//all expressions will have a simplify call, this is the most important function 
		
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
		if(this instanceof Func && (((Func)this).behavior.simplifyChildren || get().isType("result"))) toBeSimplified.simplifyChildren(casInfo);//simplify sub expressions, result function can override this behavior
		
		
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
		if(other instanceof Expr && ((Expr)other).isType(typeName())) {//make sure same type
			
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
		else if(this.isType("prod")) {
			Func prodCopy = (Func)copy();
			for(int i = 0;i<prodCopy.size();i++) {
				if(prodCopy.get(i) instanceof Num) {
					prodCopy.remove(i);
					i--;
				}
			}
			return Prod.unCast(prodCopy);
		}else if(this.isType("div")) {
			Func casted = (Func)this;
			return Div.unCast( div(casted.getNumer().removeCoefficients(),casted.getDenom().removeCoefficients()));
		}
		return copy();
	}
	
	public boolean negative() {//Assumes its already simplified
		if(this instanceof Num) return ((Num)this).signum() == -1;
		else if(this.isType("prod")) {
			for(int i = 0;i<size();i++) {
				if(get(i) instanceof Num) {
					if(((Num)get(i)).signum() == -1) return true;
				}
			}
		}else if(this.isType("div")) {
			Func casted = (Func)this;
			return casted.getNumer().negative() || casted.getDenom().negative();
		}else if(this.isType("sum")) {
			Func castedSum = (Func)this;
			
			int highest  = Integer.MIN_VALUE;
			int index = 0;
			for(int i = 0;i<castedSum.size();i++) {
				Expr current = castedSum.get(i);
				int currentHash = current.removeCoefficients().hashCode();//its very important to use the absolute value
				if(currentHash>highest) {
					highest = currentHash;
					index = i;
				}
			}
			
			Expr lowestHashExpr = castedSum.get(index);
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
		else if(this.isType("prod")) {
			for(int i = 0;i<size();i++) {
				if(get(i) instanceof Num) {
					if(((Num)get(i)).signum() == -1) {
						Expr copy = copy();
						copy.set(i, ((Num)get(i)).strangeAbs() );
						return copy.simplify(casInfo);
					}
				}
			}
		}else if(this.isType("div")) {
			Func casted = (Func)this;
			return div(casted.getNumer().strangeAbs(casInfo), casted.getDenom().strangeAbs(casInfo)).simplify(casInfo);
		}
		return copy();
	}
	
	
	public Expr getCoefficient() {
		if(this.isType("prod")) {
			for(int i = 0;i<size();i++) if(get(i) instanceof Num) return get(i).copy();
		}else if(this instanceof Num) {
			return copy();
		}
		else if(this.isType("div")) {
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
		for(int i = 0;i<subExprs.size();i++){
			Expr e = subExprs.get(i);
			if(e.contains(expr)) return true;
		}
		return false;
	}
	
	//returns if the expression contains any variables
	public boolean containsVars() {
		if(this instanceof Var && ((Var)this).isGeneric()) return true;
		for(int i = 0;i<subExprs.size();i++){
			Expr e = subExprs.get(i);
			if(e.containsVars()) return true;
		}
		return false;
	}
	
	//counts the variables in an expression into the provided arrayList and sorts them by frequency
	public void countVars(ArrayList<Algorithms.VarCount> varcounts) {
		if(this instanceof Var && ((Var)this).isGeneric()) {
			for(int i = 0;i<varcounts.size();i++) {//search
				if(varcounts.get(i).v.equals(this)) {
					varcounts.get(i).count++;
					return;
				}
			}
			varcounts.add(new Algorithms.VarCount((Var)copy(),1));
		}
		else{
			for(int i = 0;i<subExprs.size();i++){
				Expr e = subExprs.get(i);
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
		for(int i = 0;i<subExprs.size();i++) {
			Expr e = subExprs.get(i);
			sum+= e.complexity();
		}
		return sum;
	}
	
	public boolean containsType(String typeName) {//used for faster processing
		if(isType(typeName)) {
			return true;
		}
		for(int i = 0; i< size();i++) {
			if(get(i).containsType(typeName)) return true;
		}
		return false;
	}
	
	/* DEBUG code to prevent tree loops
	private boolean containsExactObject(Expr e) {//for debug
		if(this == e) return true;
		for(int i = 0; i< size();i++) {
			if(get(i).containsExactObject(e)) return true;
		}
		return false;
	}
	
	private void verifyIfSafe(Expr e) {
		if((e instanceof Func) && this.containsExactObject(e)) {
			throw new RuntimeException("double object detected! this: "+this+" obj: "+e);
		}
	}
	*/
	
	public void add(Expr e) {
		if(!isMutable()) throw new RuntimeException("expression is not mutable!");
		
		flags.reset();
		subExprs.add(e);
	}
	public void add(int i,Expr e) {
		if(!isMutable()) throw new RuntimeException("expression is not mutable!");
		
		flags.reset();
		subExprs.add(i,e);
	}
	public void remove(int index) {
		if(!isMutable()) throw new RuntimeException("expression is not mutable!");
		flags.reset();
		subExprs.remove(index);
	}
	public void set(int index,Expr e) {
		if(!isMutable()) throw new RuntimeException("expression is not mutable!");
		
		flags.reset();
		subExprs.set(index, e);
	}
	public Expr get(int index) {
		Expr subExpr = subExprs.get(index);
		return subExpr;
	}
	public Expr get() {
		return subExprs.get(0);
	}
	public int size() {
		return subExprs.size();
	}
	public void clear() {
		if(!isMutable()) throw new RuntimeException("expression is not mutable!");
		flags.reset();
		subExprs.clear();
	}
	
	private static int priorityNum(Expr e) {
		int priority = 0;
		
		if(e.isType("var")) priority = 2;
		else if(e.isType("num")) priority = 1;
		else priority = e.typeName().hashCode();
		
		return Math.abs(priority);
	}
	
	public void sort(ArrayList<Algorithms.VarCount> varcounts) {
		
		if(!isMutable()) throw new RuntimeException("expression is not mutable!");
		
		if(!flags.sorted) {
			if (varcounts == null) {
				varcounts = new ArrayList<Algorithms.VarCount>();
				countVars(varcounts);
			}
			if(this.isType("sum") || this.isType("prod") || this.isType("set")) {
				boolean wasSimple = flags.simple;
				
				final ArrayList<Algorithms.VarCount> varcountsConst = varcounts;
				subExprs.sort(new Comparator<Expr>() {//sort based on variable frequency then type priority then by complexity then by priority of child comparison
					@Override
					public int compare(Expr first, Expr second) {
						
						if(first instanceof Num && second instanceof Num) return ((Num)first).getRealValue().compareTo(((Num)second).getRealValue());
						
						if(first instanceof Var && second instanceof Var) {//sort for frequency
							for(int i = 0;i<varcountsConst.size();i++) {
								Algorithms.VarCount vc = varcountsConst.get(i);
								
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
	
	public Expr replace(Func in) {
		if(!isMutable()) throw new RuntimeException("expression is not mutable!");
		
		if(in.isType("set")) {
			Func equsSet = in;
			for(int i = 0;i<equsSet.size();i++) {
				Func equ = (Func)equsSet.get(i);
				if(equals(Equ.getLeftSide(equ))) return Equ.getRightSide(equ).copy();
			}
			
			Expr replacedChildren = copy();
			
			for(int i = 0;i<replacedChildren.size();i++) {
				replacedChildren.set(i, get(i).replace(equsSet));
			}
			return replacedChildren;
		}else if(in.isType("equ")){
			Func equ = in;
			Func l = exprSet();
			l.add(equ);
			return replace(l);
		}else {
			throw new RuntimeException("invalid parameter for replace");
		}
	}
	
	public void simplifyChildren(CasInfo casInfo) {
		if(!isMutable()) throw new RuntimeException("expression is not mutable!");
		
		for(int i = 0;i<subExprs.size();i++) {
			Expr temp = subExprs.get(i);
			if(!temp.flags.simple) {
				temp = temp.simplify(casInfo);
				subExprs.set(i, temp);
			}
		}
	}
	
	public void simplifyChildren(CasInfo casInfo,String ignoredType) {
		if(!isMutable()) throw new RuntimeException("expression is not mutable!");
		
		for(int i = 0;i<subExprs.size();i++) {
			Expr temp = subExprs.get(i);
			if(!temp.flags.simple) {
				if(temp.containsType(ignoredType)) {
					temp.simplifyChildren(casInfo, ignoredType);
				}else {
					temp = temp.simplify(casInfo);
					subExprs.set(i, temp);
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
		for(int i = 0;i<size();i++) {
			out+=get(i).toStringTree(tab+1);
		}
		if(tab == 0) out+=("EXPR-TREE-END-------------")+"\n";
		
		return out;
	}
	
}
