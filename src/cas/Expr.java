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
import java.util.HashMap;
import java.util.Random;

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

public abstract class Expr extends QuickMath implements Comparable<Expr>, Serializable{
	
	static Random random;
	public boolean commutative = false;
	public boolean simplifyChildren = true;
	public Flags flags = new Flags();
	static boolean USE_CACHE = true;
	static int CACHE_SIZE = 2048;
	private static final long serialVersionUID = -8297916729116741273L;
	private ArrayList<Expr> subExpr = new ArrayList<Expr>();//many expression types have sub expressions like sums
	
	public abstract Sequence getRuleSequence();
	public abstract ComplexFloat convertToFloat(ExprList varDefs);
	public String typeName() {
		String name = getClass().getSimpleName();
		return Character.toLowerCase(name.charAt(0))+name.substring(1);
	}
	
	private static class ExprAndSettings {//used for caching, saves the settings used for the compute
		Expr expr;
		Settings settings;
		int hash = 0;
		
		ExprAndSettings(Expr expr,Settings settings){
			this.expr = expr;
			this.settings = settings;
			hash = expr.hashCode()+87623846*settings.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof ExprAndSettings) {
				ExprAndSettings other = (ExprAndSettings)o;
				return other.settings.equals(settings) && other.expr.equals(expr);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
		
		@Override
		public String toString() {
			return this.getClass().getSimpleName()+" "+expr+" "+settings;
		}
	}
	
	public static HashMap<ExprAndSettings,Expr> cached = new HashMap<ExprAndSettings,Expr>();
	public static ArrayList<ExprAndSettings> cachedKeys = new ArrayList<ExprAndSettings>();
	public static void clearCache() {
		cached.clear();
		cachedKeys.clear();
	}
	
	void print() {
		System.out.print(this);
	}
	void println() {
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
		public boolean simple = false;
		public boolean sorted = false;
		
		public void set(Flags other) {
			simple = other.simple;
			sorted = other.sorted;
		}
		
		public void reset() {
			simple = false;
			sorted = false;
		}
		@Override
		public String toString(){
			return "sorted: "+sorted+", simple: "+simple;
		}
	}
	
	public static long ruleCallCount = 0;
	public static int RECURSION_SAFETY;
	
	public Expr simplify(Settings settings){//all expressions will have a simplify call, this is the most important function 
		if(Thread.currentThread().isInterrupted()) return null;
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		
		if(USE_CACHE && !(this instanceof Func)) {
			ExprAndSettings thisWithSettings = new ExprAndSettings(this,settings);
			Expr cacheOut = cached.get(thisWithSettings);
			if(cacheOut != null){
				return cacheOut.copy();
			}
		}
		RECURSION_SAFETY++;
		if(RECURSION_SAFETY>4096) {
			System.err.println("RECURSION DETECTED");
			return toBeSimplified;
		}
		//System.out.println(toBeSimplified);
		String originalType = toBeSimplified.typeName();
		if(simplifyChildren || get().typeName().equals("result")) toBeSimplified.simplifyChildren(settings);//simplify sub expressions, result function can override this behavior
		
		Sequence ruleSequence = getRuleSequence();
		
		if(ruleSequence != null){
			
			for (int i = 0;i<ruleSequence.size();i++){
				Rule rule = (Rule)ruleSequence.get(i);
				
				toBeSimplified = rule.applyRuleToExpr(toBeSimplified, settings);
				//System.out.println(rule);
				ruleCallCount++;
				if(!toBeSimplified.typeName().equals(originalType)) break;
			}
		}
		
		toBeSimplified.flags.simple = true;//result is simplified and should not be simplified again
		
		if(USE_CACHE){
			if(cached.size()<CACHE_SIZE){
				if(!(this instanceof Func)) {
					Expr original = copy();
					ExprAndSettings cache = new ExprAndSettings( original,settings);
					cached.put(cache, toBeSimplified.copy());
					cachedKeys.add( cache );
				}
			}else{
				//remove a random 1024 items from cache
				//System.out.println("shrinking cache!");
				if(random == null) random = new Random(761234897);
				for(int i = 0;i<CACHE_SIZE/4;i++){
					int randomIndex = random.nextInt(cachedKeys.size());
					ExprAndSettings cache = cachedKeys.get(randomIndex);
					cachedKeys.remove(randomIndex);
					cached.remove(cache);
				}
			}
		}
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
	
	@SuppressWarnings("static-method")
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
			return factor(sub(this,casted)).simplify(Settings.normal).equals(num(0));		
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
				
				if(this.commutative){
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
				for(int i = 0;i<size();i++){//not commutative
					if(!get(i).equals(otherCasted.get(i))) return false;
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int sum = this.getClass().hashCode();
		if(commutative){
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
		}else if(this instanceof Div) {
			Div casted = (Div)this;
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
		}else if(this instanceof Div) {
			Div casted = (Div)this;
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
	
	/*
	 * this behaves like an absolute value but numbers sign is based on a special rule to maintain sign
	 * -2+3*i -> 2-3*i
	 * -i -> i
	 * 2-3*i -> 2-3*i
	 * 
	 * basically if it has a real and imaginary component, the real value takes precedence otherwise use the imaginary
	 * component
	 */
	public Expr strangeAbs(Settings settings) {//Assumes its already simplified
		if(this instanceof Num) return ((Num)this).strangeAbs();
		else if(this instanceof Prod) {
			for(int i = 0;i<size();i++) {
				if(get(i) instanceof Num) {
					if(((Num)get(i)).signum() == -1) {
						Expr copy = copy();
						copy.set(i, ((Num)get(i)).strangeAbs() );
						return copy.simplify(settings);
					}
				}
			}
		}else if(this instanceof Div) {
			Div casted = (Div)this;
			return div(casted.getNumer().strangeAbs(settings), casted.getDenom().strangeAbs(settings)).simplify(settings);
		}
		return copy();
	}
	
	
	public Expr getCoefficient() {//returns numerator coefficient
		if(this instanceof Prod) {
			for(int i = 0;i<size();i++) if(get(i) instanceof Num) return get(i).copy();
		}else if(this instanceof Num) {
			return copy();
		}
		else if(this instanceof Div) {
			Div casted = (Div)this;
			Num numerCoef = (Num)casted.getNumer().getCoefficient();
			Num denomCoef = (Num)casted.getDenom().getCoefficient();
			return Div.unCast(div(numerCoef,denomCoef));
			
		}
		return num(1);
	}
	//returns if the expression is in this, not full proof for products and sums
	public boolean contains(Expr expr) {
		if(this.equals(expr)) return true;
		for(Expr e:subExpr)if(e.contains(expr)) return true;
		return false;
	}
	
	//returns if the expression contains any variables
	public boolean containsVars() {
		if(this instanceof Var && ((Var)this).generic) return true;
		for(Expr e:subExpr) if(e.containsVars()) return true;
		return false;
	}
	
	//counts the variables in an expression into the provided arrayList and sorts them by frequency
	public void countVars(ArrayList<VarCount> varcounts) {
		if(this instanceof Var && ((Var)this).generic) {
			for(int i = 0;i<varcounts.size();i++) {//search
				if(varcounts.get(i).v.equals(this)) {
					varcounts.get(i).count++;
					return;
				}
			}
			varcounts.add(new VarCount((Var)copy(),1));
		}
		else for(Expr e:subExpr) e.countVars(varcounts);
		Collections.sort(varcounts);
	}
	
	/*
	 * a score of how complex the expression is, this counts the number of nodes in the expression
	 * 
	 * however products are special and triple the complexity, this is important for sorting (for complexityForSorting only)
	 * examples
	 * x^3 -> 3
	 * sin(x) -> 2
	 * sqrt(x) -> 5
	 * sin(x+a) -> 4
	 * -cos(b) -> 12
	 * sin(x+a)-cos(b) -> 17 , when this is sorted -cos(b) has a higher complexity than sin(x+a) pushing it to the front even though they have the same number of nodes
	 */
	public long complexityForSorting() {
		int sum = 1;
		for(Expr e:subExpr) {
			sum+= e.complexityForSorting();
		}
		if(this instanceof Prod) sum*=3;
		return sum;
	}
	public long complexity() {
		int sum = 1;
		for(Expr e:subExpr) {
			sum+= e.complexityForSorting();
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
		flags.reset();
		subExpr.add(e);
	}
	public void add(int i,Expr e) {
		flags.reset();
		subExpr.add(i,e);
	}
	public void remove(int index) {
		flags.reset();
		subExpr.remove(index);
	}
	public void set(int index,Expr e) {
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
		flags.reset();
		subExpr.clear();
	}
	/*
	 * sorted expressions are based on complexity and variable frequency
	 * 
	 * more complex parts are put in the front and simpler in the back
	 * 
	 * variables which appear more often in the expression are pushed to the fron
	 * 
	 * example x+sqrt(y+x) -> sqrt(x+y)+x
	 *     since the sqrt(y+x) is more complex than x it is moved to the front
	 *     sqrt(y+x) -> sqrt(x+y) , x is moved to the fron because it appears more often
	 * 
	 * sorting allows for the similarStuct comparison to occur allowing for more freedom in what modifyFromExample can do
	 * 
	 * the sort output is NOT completely guaranteed to output the same order, this is because the complexity of two or more expressions may be the same
	 */
	public void sort(ArrayList<VarCount> varcounts) {
		if(!flags.sorted) {
			if (varcounts == null) {
				varcounts = new ArrayList<VarCount>();
				countVars(varcounts);
			}
			if(this instanceof Sum || this instanceof Prod || this instanceof ExprList) {
				boolean wasSimple = flags.simple;
				
				int moved = 0;
				for(int i = 0;i<varcounts.size();i++) {//sort based on frequency
					VarCount varcount = varcounts.get(i);
					int subMoved = 0;
					for(int j = moved;j<size() && subMoved != varcount.count;j++) {
						if(get(j).contains(varcount.v)) {
							Expr temp = get(moved);
							set(moved,get(j));
							set(j,temp);
							moved++;
							subMoved++;
						}
						
					}	
				}
				
				Collections.sort(subExpr);
				
				flags.simple = wasSimple;
			}
			
			for(int i = 0;i<size();i++) {
				get(i).flags.sorted = false;//we have to reset the sort because parent was not sorted so varcounts may change
				get(i).sort(varcounts);
			}
			
			flags.sorted = true;
		}
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
	
	public void sort() {
		sort(null);
	}
	public void simplifyChildren(Settings settings) {
		for(int i = 0;i<subExpr.size();i++) {
			Expr temp = subExpr.get(i);
			if(!temp.flags.simple) {
				temp = temp.simplify(settings);
				subExpr.set(i, temp);
			}
		}
	}
	
	public static void saveExpr(Expr expr,String fileName)throws IOException{
	    FileOutputStream fos = new FileOutputStream("saves/"+fileName);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(expr);
	    oos.close();
	}

	public static Expr openExpr(String fileName)throws IOException, ClassNotFoundException{
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
			out+=("EXPR-TREE-START-----------: "+this+" flags: "+flags)+"\n";
		}
		String tabStr = "";
		if(tab>0) tabStr+="|";
		for(int i = 0;i<tab-1;i++) tabStr+="   |";
		if(tab>0) tabStr+="--->";
		else tabStr+=">";
		out+=(tabStr+ typeName() +" hash: "+hashCode());
		if(this instanceof Num || this instanceof Var || this instanceof FloatExpr) out+=(" name: "+this);
		out+="\n";
		if(this instanceof Func) {
			out+="rules: "+((Func)this).getRuleSequence();
			out+="\n";
		}
		for(int i = 0;i<size();i++) {
			out+=get(i).toStringTree(tab+1);
		}
		if(tab == 0) out+=("EXPR-TREE-END-------------")+"\n";
		
		return out;
	}
	@Override
	public int compareTo(Expr other) {
		int c = -Long.compare(complexityForSorting(), other.complexityForSorting());
		return c;
	}
	
}
