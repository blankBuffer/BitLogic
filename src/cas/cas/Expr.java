package cas;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public abstract class Expr extends QuickMath implements Comparable<Expr>, Serializable{
	
	static class Flags implements Serializable{
		private static final long serialVersionUID = -2823404902493132716L;
		boolean simple = false;
		boolean sorted = false;
		void set(Flags other) {
			simple = other.simple;
			sorted = other.sorted;
		}
		
		void reset() {
			simple = false;
			sorted = false;
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
	
	public abstract Expr replace(ArrayList<Equ> equs);
	
	public abstract double convertToFloat(ExprList varDefs);
	
	public boolean negative() {
		if(this instanceof Num) return ((Num)this).value.signum() == -1;
		else if(this instanceof Prod) {
			for(int i = 0;i<size();i++) {
				if(get(i) instanceof Num) if(((Num)get(i)).value.signum() == -1) return true;
				else if(invObj.fastSimilarStruct(get(i))) {
					Power castedPower = (Power)get(i);
					if(castedPower.getBase() instanceof Num) {
						if(((Num)castedPower.getBase()).value.signum() == -1) return true;
					}
				}
			}
		}
		return false;
	}
	
	public Expr abs(Settings settings) {
		if(this instanceof Num) return num(((Num)this).value.abs());
		else if(this instanceof Prod) {
			for(int i = 0;i<size();i++) {
				if(get(i) instanceof Num) {
					if(((Num)get(i)).value.signum() == -1) {
						Expr copy = (Prod)copy();
						copy.set(i, num(((Num)get(i)).value.abs()) );
						return copy.simplify(settings);
					}
				}else if(invObj.fastSimilarStruct(get(i))) {
					Power castedPower = (Power)get(i);
					if(castedPower.getBase() instanceof Num) {
						if(((Num)castedPower.getBase()).value.signum() == -1) {
							Expr copy = (Prod)copy();
							copy.set(i, inv(num(((Num)castedPower.getBase()).value.abs())));
							return copy.simplify(settings);
						}
					}
				}
			}
		}
		return copy();
	}
	
	Num getCoefficient() {
		if(this instanceof Prod) {
			for(int i = 0;i<size();i++) if(get(i) instanceof Num) return (Num)get(i).copy();
		}else if(this instanceof Num) return (Num)copy();
		return num(1);
	}
	
	public boolean contains(Expr expr) {
		if(this.equalStruct(expr)) return true;
		else for(Expr e:subExpr)if(e.contains(expr)) return true;
		return false;
	}
	
	public boolean containsVars() {
		if(this instanceof Var) return true;
		else for(Expr e:subExpr) if(e.containsVars()) return true;
		return false;
	}
	
	//the checked variable simply keeps track weather chechForMatches has been checked
	abstract boolean similarStruct(Expr other,boolean checked);//returns if another expression is similar to this but any comparison with variables is returned true
	
	
	public boolean fastSimilarStruct(Expr other) {
		return similarStruct(other,true);
	}
	
	public boolean strictSimilarStruct(Expr other) {
		return similarStruct(other,false);
	}
	
	void getParts(ArrayList<Var> vars,ArrayList<Expr> exprs,Expr other) {//other is input,vars and exprs are outputs
			
		if(this instanceof Var) {
			if(vars != null) vars.add((Var)copy());
			if(other != null) exprs.add(other.copy());
		}else if(other != null) {
			if(size()==other.size()) {
				for(int i = 0;i<size();i++) get(i).getParts(vars,exprs,other.get(i));
			}
		}
	}
	
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
	
	
	
	boolean checkForMatches(ArrayList<Var> vars,ArrayList<Expr> exprs,Expr other) {//other is input,vars and exprs are outputs
		
		//part one, create index pairs
		
		ArrayList<Var> usedVars = new ArrayList<Var>();
		ArrayList<IndexSet> indexSets = new ArrayList<IndexSet>();
		
		sort();
		other.sort();
		
		getParts(vars,exprs,other);
		
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
	
	int nestDepth() {
		int max = 0;
		for(Expr e:subExpr) {
			int current = e.nestDepth();
			if(current>max) max = current;
		}
		return max+1;
	}
	
	boolean checkForMatches(Expr other) {//shortcut
		ArrayList<Var> vars = new ArrayList<Var>();
		ArrayList<Expr> exprs = new ArrayList<Expr>();
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
		if(example.getLeftSide().fastSimilarStruct(this)) {//we use fast similar struct here because we don't want to call the getParts function twice and its faster
			
			ArrayList<Var> exampleParts = new ArrayList<Var>();
			ArrayList<Expr> parts = new ArrayList<Expr>();
			boolean match = example.getLeftSide().checkForMatches(exampleParts,parts,this);
			if(!match) {
				return new ModifyFromExampleResult(this,false);
			}
			ArrayList<Equ> equs = new ArrayList<Equ>();
			for(int i = 0;i<parts.size();i++) equs.add(new Equ(exampleParts.get(i),parts.get(i)));
			
			Expr out = example.getRightSide().replace(equs);
			
			if(example.getLeftSide().getClass() == example.getRightSide().getClass()) {
				out.simplifyChildren(settings);
			}else {
				out = out.simplify(settings);//no recursion since different class type
			}
			return new ModifyFromExampleResult(out,true);
			
		}else {
			return new ModifyFromExampleResult(this,false);
		}
	}
	
	public boolean containsIntegrals() {
		if(this instanceof Integrate) {
			return true;
		}else {
			for(int i = 0; i< size();i++) {
				if(get(i).containsIntegrals()) return true;
			}
		}
		return false;
	}
	
	public Expr replace(Equ equ) {
		ArrayList<Equ> l = new ArrayList<Equ>();
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
					for(int j = moved;j<size();j++) {
						if(get(j).contains(varcount.v)) {
							
							Expr temp = get(moved);
							set(moved,get(j));
							set(j,temp);
							moved++;
							break;
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
	
	Expr getNextInnerFunction(Var v) {
		if(nestDepth() <= 2) return copy();
		else if(size()>0 && contains(v)){
			Expr highest = null;
			int highestNestDepth = 0;
			for(int i = 0;i<size();i++) {
				int current = get(i).nestDepth();
				if(current>highestNestDepth && get(i).contains(v)) {
					highestNestDepth = current;
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
	   return expr;
	}
	
	@Override
	public int compareTo(Expr other) {
		int c = -Integer.compare(nestDepth(), other.nestDepth());
		if(c == 0) return Long.compare(generateHash(), other.generateHash());
		return c;
	}
	
}
