/*
 * Benjamin R Currie, BitLogic Program
 * do not claim this to be your work!
 * this is free to use and copy without modification, if you modify it it must be for personal use
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
	static class Flags implements Serializable{
		private static final long serialVersionUID = -2823404902493132716L;
		boolean simple = false;
		boolean sorted = false;
		Settings settingsUsedForSimplify = Settings.normal;
		
		void set(Flags other) {
			simple = other.simple;
			sorted = other.sorted;
			settingsUsedForSimplify = other.settingsUsedForSimplify;
		}
		
		void reset() {
			simple = false;
			sorted = false;
			settingsUsedForSimplify = Settings.normal;
		}
		@Override
		public String toString(){
			return "sorted: "+sorted+", simple: "+simple;
		}
	}
	
	public Flags flags = new Flags();
	
	private static final long serialVersionUID = -8297916729116741273L;

	private ArrayList<Expr> subExpr = new ArrayList<Expr>();//many expression types have sub expressions like sums
	
	public abstract Expr simplify(Settings settings);//all expressions will have a simplify call, this is the most important function 
	
	public abstract Expr copy();//copying this expression object
	
	@Override
	public abstract String toString();//print the expression into console
	
	public abstract boolean equalStruct(Expr other);//compares if two expression have the same tree
	
	public abstract long generateHash();
	
	public abstract ComplexFloat convertToFloat(ExprList varDefs);
	
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
			
			long highest  = Long.MIN_VALUE;
			int index = 0;
			for(int i = 0;i<casted.size();i++) {
				long current = casted.get(i).abs(Settings.normal).generateHash();//its very important to use the absolute value
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
	
	public Expr abs(Settings settings) {//Assumes its already simplified
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
			return div(casted.getNumer().abs(settings), casted.getDenom().abs(settings)).simplify(settings);
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
		if(this.equalStruct(expr)) return true;
		for(Expr e:subExpr)if(e.contains(expr)) return true;
		return false;
	}
	
	//returns if the expression contains any variables
	public boolean containsVars() {
		if(this instanceof Var) return true;
		for(Expr e:subExpr) if(e.containsVars()) return true;
		return false;
	}
	
	//the checked variable simply keeps track weather chechForMatches has been checked
	abstract boolean similarStruct(Expr other,boolean checked);//returns if another expression is similar to this but any comparison with variables is returned true
	
	/*
	 * letting the template expression=this we check if the other expression is similar to the template
	 * we DONT check for matches which is why its called fast
	 * 
	 * similar is defined as having a same tree but the variables might be different or other has expressions where the varibales would be
	 * 
	 * this is best understood by example
	 * 
	 * this=a^b other=2^3 , fastSimilar = true , strictSimilar = false
	 * this=a^a other=2^3 , fastSimilar = true , strictSimilar = fasle (because 2 not equal to 3)
	 * 
	 * this=sin(x)^b other = a^b , not similar at all, why? because the template was expecting the base to be the sine function
	 * 
	 * note that with sums and products the length of sub expressions must be equal. It does its best to sort the expressions in the sum/product in a similar kind of way, but its not perfect
	 * 
	 */
	public boolean fastSimilarStruct(Expr other) {
		return similarStruct(other,true);
	}
	/*
	 * a full similar comparisons we DO check for matches making it a guarantee
	 * 
	 * note that with sums and products the length must be equal
	 * 
	 */
	public boolean strictSimilarStruct(Expr other) {
		return similarStruct(other,false);
	}
	
	/*
	 * extracts the parts and variables from the template=this, and expr is the parts to be extracted 
	 * example, let this=diff(x^a,x) and expr=diff(m^(k*l),m)
	 * these expression break down into lists
	 * this -> [x,a,x]
	 * expr -> [m,k*l,m]
	 * 
	 * if this lists for some reason are not the same length it returns false saying that they can't be compared
	 * returning true is saying no problems were encountered
	 * 
	 * this is useful because we can later compare the list for matching sets,
	 * for example in this example the first and last element must be the same [ ->x ,a, ->x ],
	 * this is what the check for matches function does
	 * 
	 */
	boolean getParts(ExprList vars,ExprList exprs,Expr expr) {//template and this is input,vars and exprs are outputs
		if(this instanceof Var) {//template encountered a variable, time for extraction into the expr list
			vars.add(copy());
			exprs.add(expr.copy());
		}else{
			if(size()==expr.size()) {
				for(int i = 0;i<size();i++) {
					if(!get(i).getParts(vars,exprs,expr.get(i))) return false;
				}
			}else {
				return false;//different size so variables will obviously not match
			}
		}
		return true;
	}
	//counts the variables in an expression into the provided arrayList and sorts them by frequency
	void countVars(ArrayList<VarCount> varcounts) {
		if(this instanceof Var) {
			for(int i = 0;i<varcounts.size();i++) {//search
				if(varcounts.get(i).v.equalStruct(this)) {
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
	 * looks at the template=this and checks if the parts list from this have the same pairs as the other expression
	 */
	
	boolean checkForMatches(ExprList vars,ExprList exprs,Expr other) {//other is input,vars and exprs are outputs
		
		//part one, create index pairs
		
		ExprList usedVars = new ExprList();
		ArrayList<IndexSet> indexSets = new ArrayList<IndexSet>();
		
		sort();
		other.sort();
		
		if(!getParts(vars,exprs,other)) return false;//its possible for expressions to be un-strictly similar
		
		if(vars.size()!=exprs.size()) return false;
		
		
		for(int i = 0;i<vars.size();i++) {
			
			if(usedVars.contains(vars.get(i))) continue;
			
			IndexSet set = new IndexSet();
			set.ints.add(i);
			
			for(int j = i+1;j<vars.size();j++) {
				if(vars.get(i).equalStruct(vars.get(j))) {
					set.ints.add(j);
					
				}
			}
			
			if(set.ints.size() != 1) indexSets.add(set);
			
			usedVars.add(vars.get(i));
		}
		
		
		///parts two, check other has matches
		
		for(IndexSet set:indexSets) {
			Expr e = exprs.get(set.ints.get(0));
			for(int i = 1;i<set.ints.size();i++) {
				if(set.ints.get(i) > exprs.size()) return false;
				
				Expr e2 =  exprs.get(set.ints.get(i));
				
				if(!e2.equalStruct(e)) return false;
			}
			
		}
		
		
		return true;
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
	
	boolean checkForMatches(Expr other) {//shortcut
		ExprList vars = new ExprList();
		ExprList exprs = new ExprList();
		return checkForMatches(vars,exprs,other);
	}
	
	static class ModifyFromExampleResult{
		Expr expr;
		boolean success;
		ModifyFromExampleResult(Expr expr,boolean success ){
			this.expr = expr;
			this.success = success;
		}
	}
	
	public Expr modifyFromExample(Equ example,Settings settings) {
		return modifyFromExampleSpecific(example,settings).expr;
	}
	
	public ModifyFromExampleResult modifyFromExampleSpecific(Equ example,Settings settings) {//returns object describing weather or not it was successful
		//System.out.println(example.getLeftSide());
		if(example.getLeftSide().fastSimilarStruct(this)) {//we use fast similar struct here because we don't want to call the getParts function twice and its faster
			
			//System.out.println(this+" "+example);
			
			ExprList exampleParts = new ExprList();
			ExprList parts = new ExprList();
			boolean match = example.getLeftSide().checkForMatches(exampleParts,parts,this);
			if(!match) {
				return new ModifyFromExampleResult(this,false);
			}
			ExprList equs = new ExprList();
			for(int i = 0;i<parts.size();i++) equs.add(new Equ(exampleParts.get(i),parts.get(i)));
			
			Expr out = example.getRightSide().replace(equs);
			
			if(example.getLeftSide().getClass() == example.getRightSide().getClass()) {
				out.simplifyChildren(settings);
			}else {
				out = out.simplify(settings);//no recursion since different class type
			}
			return new ModifyFromExampleResult(out,true);
			
		}
		return new ModifyFromExampleResult(this,false);
	}
	
	public boolean containsType(@SuppressWarnings("rawtypes") Class c) {//used for faster processing
		if(this.getClass().equals(c)) {
			return true;
		}
		for(int i = 0; i< size();i++) {
			if(get(i).containsType(c)) return true;
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
			if(this instanceof Sum || this instanceof Prod || this instanceof ExprList) {
				if (varcounts == null) {
					varcounts = new ArrayList<VarCount>();
					countVars(varcounts);
				}
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
				get(i).sort(varcounts);
			}
			
			flags.sorted = true;
		}
	}
	
	public Expr replace(ExprList equs) {
		for(int i = 0;i<equs.size();i++) {
			Equ e = (Equ)equs.get(i);
			if(equalStruct(e.getLeftSide())) return e.getRightSide().copy();
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
	void simplifyChildren(Settings settings) {
		for(int i = 0;i<subExpr.size();i++) {
			Expr temp = subExpr.get(i);
			if(!temp.flags.simple) {
				temp = temp.simplify(settings);
				subExpr.set(i, temp);
			}
		}
	}
	
	public Expr getNextInnerFunction(Var v) {
		if(size()>0 && contains(v)){
			Expr highest = null;
			long highestComplexity = 0;
			for(int i = 0;i<size();i++) {
				long current = get(i).complexity();
				if(current>highestComplexity && get(i).contains(v)) {
					highestComplexity = current;
					highest = get(i);
				}
			}
			return highest;
		}
		return copy();
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
	public void printTree(int tab) {
		
		if(tab == 0){
			System.out.println("EXPR-TREE-START-----------: "+this+" flags: "+flags);
		}
		String tabStr = "";
		if(tab>0) tabStr+="|";
		for(int i = 0;i<tab-1;i++) tabStr+="   |";
		if(tab>0) tabStr+="--->";
		else tabStr+=">";
		System.out.print(tabStr+this.getClass().getSimpleName() );
		if(this instanceof Num || this instanceof Var) System.out.print(" : "+this);
		System.out.println();
		for(int i = 0;i<size();i++) {
			get(i).printTree(tab+1);
		}
		if(tab == 0) System.out.println("EXPR-TREE-END-------------");
	}
	@Override
	public int compareTo(Expr other) {
		int c = -Long.compare(complexityForSorting(), other.complexityForSorting());
		return c;
	}
	
}
