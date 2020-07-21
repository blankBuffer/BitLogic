package math.booleanAlgebra;

import java.util.ArrayList;

public abstract class BoolContainer {
	public abstract void print();

	public abstract boolean equalStruct(BoolContainer other);

	public abstract BoolContainer copy();

	public abstract BoolContainer toSOP();

	public abstract void getVars(ArrayList<BoolVar> varList);

	public BoolContainer simplify() {

		BoolContainer sop = this.toSOP();

		// !x | x & y
		// x | !x & y
		if (!(sop instanceof Or))
			return sop;

		{
			boolean modded = false;

			Or or = (Or) sop.copy();

			for (int i = 0; i < or.containers.size(); i++) {
				BoolContainer temp = or.containers.get(i);
				boolean v = false;
				BoolContainer compare = null;
				if (temp instanceof BoolVar || temp instanceof Not) {
					v = true;
					compare = temp;
				}
				if (v) {
					compare = new Not(compare).toSOP();
					for (int j = 0; j < or.containers.size(); j++) {
						BoolContainer temp2 = or.containers.get(j);
						if (temp2 instanceof And) {
							And temp2And = (And) temp2;
							for (int k = 0; k < temp2And.containers.size(); k++) {
								BoolContainer temp2Andtemp = temp2And.containers.get(k);
								if (temp2Andtemp.equalStruct(compare)) {
									temp2And.containers.remove(k);
									modded = true;
									break;
								}
							}
						}
					}
				}
			}
			if (modded)
				sop = or.toSOP();
		}
		
		if (!(sop instanceof Or))
			return sop;

		// a & b | !a & c | b & c
		
		{

			Or or = (Or) sop;
			boolean[] indexOfR = new boolean[or.containers.size()];
			boolean modded = false;
			
			for(int i = 0;i < or.containers.size();i ++) {
				BoolContainer temp = or.containers.get(i);
				
				if(temp instanceof And) {
					And tempAnd = (And)(temp.copy());
					if(tempAnd.containers.size() == 2) {
						
						BoolContainer varSearch1 = new Not(tempAnd.containers.get(0)).toSOP();
						BoolContainer varSearch2 = new Not(tempAnd.containers.get(1)).toSOP();
						And compare = null;
						
						for(int k = i+1; k < or.containers.size();k++) {
							
							BoolContainer temp2 = or.containers.get(k).copy();
							
							if(temp2 instanceof And) {
								And temp2And = (And)temp2;
								if(temp2And.containers.size() == 2) {
									//look for each variable search
									
									if(temp2And.containers.get(0).equalStruct(varSearch1)) {
										compare = new And();
										compare.add(tempAnd.containers.get(1));
										compare.add(temp2And.containers.get(1));
										break;
									}
									if(temp2And.containers.get(1).equalStruct(varSearch1)) {
										compare = new And();
										compare.add(tempAnd.containers.get(1));
										compare.add(temp2And.containers.get(0));
										break;
									}
									if(temp2And.containers.get(0).equalStruct(varSearch2)) {
										compare = new And();
										compare.add(tempAnd.containers.get(0));
										compare.add(temp2And.containers.get(1));
										break;
									}
									if(temp2And.containers.get(1).equalStruct(varSearch2)) {
										compare = new And();
										compare.add(tempAnd.containers.get(0));
										compare.add(temp2And.containers.get(0));
										break;
									}
									
									
								}
							}
							
							
						}//
						
						if(compare != null) {
							//search and mark
							
							for(int k = 0;k < or.containers.size();k++) {
								if(or.containers.get(k).equalStruct(compare)) {
									modded = true;
									indexOfR[k] = true;
									break;
								}
							}
							
						}
						
					}
					
				}
				
			}
			
			if (modded) {
				for(int k = indexOfR.length-1;k > -1;k--) {
					if(indexOfR[k]) or.containers.remove(k);
				}
			}
			
		}

		ArrayList<BoolVar> vars = new ArrayList<BoolVar>();
		sop.getVars(vars);

		// x | (x & y)->x
		for (BoolContainer v : vars) {
			for (int n = 0; n < 2; n++) {
				if (n == 1)
					v = new Not(v);
				if (!(sop instanceof Or))
					return sop;
				Or or = (Or) sop.copy();

				And factorProd = new And();
				factorProd.add(v.copy());
				Or internalSum = new Or();

				for (int i = 0; i < or.containers.size(); i++) {
					BoolContainer temp = or.containers.get(i);
					if (temp.equalStruct(v)) {

						internalSum.add(new BoolState(true));
						or.containers.remove(i);
						i--;
					} else if (temp instanceof And) {

						And tempAnd = (And) temp;
						for (int j = 0; j < tempAnd.containers.size(); j++) {
							BoolContainer temp2 = tempAnd.containers.get(j);
							if (temp2.equalStruct(v)) {

								tempAnd.containers.remove(j);
								internalSum.add(tempAnd);
								or.containers.remove(i);
								i--;
								break;
							}

						}

					}

				}

				BoolContainer internalSumSimp = internalSum.simplify();
				
				
				if (internalSumSimp instanceof Or) {

					Or internalSumSimpOr = (Or) internalSumSimp;

					boolean justVars = true;
					for (BoolContainer c : internalSumSimpOr.containers) {
						if (c instanceof And) {
							justVars = false;
							break;
						}
					}

					if (justVars) {
						BoolContainer compare = (new Not(internalSumSimp)).toSOP();

						for (BoolContainer c : or.containers) {

							if (c.equalStruct(compare)) {
								internalSumSimp = new BoolState(true);
								break;
							}

						}

					}

				}
				

				boolean better = !(internalSumSimp.equalStruct(internalSum));
				if (better) {
					factorProd.add(internalSumSimp);
					or.add(factorProd.toSOP());
					sop = or.toSOP();
				}
			}
		}

		return sop;

	}

}
