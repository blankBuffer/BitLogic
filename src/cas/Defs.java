package cas;

import java.util.HashMap;
import javax.swing.DefaultListModel;

public class Defs extends QuickMath{
	public DefaultListModel<Func> functionsArrayList = new DefaultListModel<Func>();//just for printing purposes
	public DefaultListModel<Equ> varsArrayList = new DefaultListModel<Equ>();//just for printing purposes
	
	
	HashMap<String,Func> functions = new HashMap<String,Func>();
	HashMap<String,Equ> varDefs = new HashMap<String,Equ>();
	
	public ExprList getVars() {
		ExprList list = new ExprList();
		for(int i = 0;i<varsArrayList.size();i++) {
			list.add(varsArrayList.get(i));
		}
		return list;
	}
	
	public void addVar(String varName,Expr def) {
		removeVar(varName);
		Equ equ = equ(var(varName),def);
		varDefs.put(varName, equ);
		varsArrayList.addElement(equ);
	}
	
	public void changeVar(String varName,Expr def) {
		Equ varDef = varDefs.get(varName);
		varDef.setRightSide(def);
	}
	
	public void addFunc(String name,Func f) {
		removeFunc(name);
		functions.put(name, f);
		functionsArrayList.addElement(f);
	}
	
	public Func getFunc(String name) {
		Func out = functions.get(name);
		if(out != null) return (Func)out.copy();
		else return null;
	}
	
	public Expr getVar(String s) {
		Equ out = varDefs.get(s);
		if(out != null) return out.getRightSide().copy();
		else return null;
	}
	
	public void removeVar(String name) {
		Equ equ = varDefs.get(name);
		varDefs.remove(name);
		varsArrayList.removeElement(equ);
	}
	public void removeFunc(String name) {
		Func f = functions.get(name);
		functions.remove(name);
		functionsArrayList.removeElement(f);
	}
	public void clear() {
		varDefs.clear();
		functions.clear();
		functionsArrayList.clear();
		varsArrayList.clear();
	}
	
	@Override
	public String toString() {
		String out = "";
		out+="functions[";
		for(int i = 0;i < functionsArrayList.size();i++) {
			out+=functionsArrayList.get(i).toString();
			if(i!= functionsArrayList.size()-1) out+=",";
		}
		out+="] , variables[";
		for(int i = 0;i < varsArrayList.size();i++) {
			out+=varsArrayList.get(i).toString();
			if(i!= varsArrayList.size()-1) out+=",";
		}
		out+="]";
		return out;
	}
	
	static Defs blank = new Defs();
	
}
