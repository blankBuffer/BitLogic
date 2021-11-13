package cas;

public class ExprList extends Expr{

	private static final long serialVersionUID = 4631446864313039932L;

	@Override
	public Expr simplify(Settings settings) {
		Expr toBeSimplified = copy();
		if(flags.simple) return toBeSimplified;
		toBeSimplified.simplifyChildren(settings);//simplify sub expressions
		
		removeRepeats((ExprList)toBeSimplified);
		toBeSimplified = alone((ExprList)toBeSimplified);
		
		toBeSimplified.flags.simple = true;
		return toBeSimplified;
	}
	
	void removeRepeats(ExprList list) {
		for(int i = 0;i<list.size();i++) {
			for(int j = i+1;j<list.size();j++) {
				if(list.get(i).equalStruct(list.get(j))) {
					list.remove(j);
					j--;
				}
			}
		}
	}
	
	Expr alone(ExprList list) {
		if(list.size() == 1) {//if its only one element 
			return list.get(0);
		}else if(list.size() == 0) {//if its empty return 0
			return num(0);
		}
		return list;
	}

	@Override
	public Expr copy() {
		Expr listCopy = new ExprList();
		for(int i = 0;i<size();i++) {
			listCopy.add(get(i).copy());
		}
		listCopy.flags.set(flags);
		return listCopy;
	}

	@Override
	public String toString() {
		String out = "";
		out+='[';
		for(int i = 0;i<size();i++) {
			out+=get(i).toString();
			if(i != size()-1) out+=',';
		}
		out+=']';
		return out;
	}

	@Override
	public boolean equalStruct(Expr other) {
		if(other instanceof ExprList) {//make sure same type
			
			if(other.size() == size()) {//make sure they are the same size
				
				boolean usedIndex[] = new boolean[size()];//keep track of what indices have been used
				int length = other.size();//length of the lists
				
				outer:for(int i = 0;i < length;i++) {
					for(int j = 0;j < length;j++) {
						if(usedIndex[j]) continue;
						if(get(i).equalStruct(other.get(j))) {
							usedIndex[j] = true;
							continue outer;
						}
					}
					return false;//the subExpr was never found 
				}
				
				return true;//they are the same as everything was found
				 
			}
		}
		return false;
	}

	@Override
	public long generateHash() {
		long sum = 1;
		for(int i = 0;i<size();i++) sum+=get(i).generateHash();//add all sub expressions hashes
		
		return sum-1023954621083462324L;//again arbitrary digits
	}

	@Override
	boolean similarStruct(Expr other, boolean checked) {
		if(other instanceof ExprList) {
			
			sort();
			other.sort();
			
			if(!checked) if(checkForMatches(other) == false) return false;
			if(size() != other.size()) return false;
			
			boolean[] usedIndicies = new boolean[other.size()];
			for(int i = 0;i<size();i++) {
				if(get(i) instanceof Var) continue;//skip because they return true on anything
				boolean found = false;
				for(int j = 0;j<other.size();j++) {
					if(usedIndicies[j]) continue;
					else if(get(i).fastSimilarStruct(other.get(j))) {
						found = true;
						usedIndicies[j] = true;
						break;
					}
				}
				if(!found) return false;
			}
			return true;
		}
		return false;
	}
	
	public static ExprList cast(Expr e) {
		if(e instanceof ExprList) return (ExprList)e;
		ExprList out = new ExprList();
		out.add(e);
		return out;
	}

	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return new ComplexFloat(0,0);
	}

}
