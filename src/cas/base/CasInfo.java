package cas.base;

import java.io.Serializable;

import cas.Defs;

public class CasInfo implements Serializable{
	
	private static final long serialVersionUID = -423836233586167621L;
	private boolean allowComplexNumbers = false;//simplify sqrt(-1) -> i
	private boolean allowAbs = true;//simplify abs(x) -> x
	private boolean factorIrrationalRoots = false;
	private boolean singleSolutionMode = false;//solve returns only the base solution
	private boolean relaxedPower = false;//(a^b)^c -> a^(b*c)
	public Defs definitions;
	
	public CasInfo() {
		definitions = new Defs();
	}
	public CasInfo(CasInfo casInfo) {
		allowComplexNumbers = casInfo.allowComplexNumbers;
		allowAbs = casInfo.allowAbs;
		factorIrrationalRoots = casInfo.factorIrrationalRoots;
		singleSolutionMode = casInfo.singleSolutionMode;
		relaxedPower = casInfo.relaxedPower;
		definitions = casInfo.definitions;
	}
	public static CasInfo normal = new CasInfo();
	
	@Override
	public String toString() {
		String out = "";
		out += "allowComplexNumbers: "+allowComplexNumbers;
		out += " allowAbs: "+allowAbs;
		out += " singleSolutionMode: "+singleSolutionMode;
		out += "relaxedPowers: "+relaxedPower;
		return out;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof CasInfo) {
			CasInfo other = (CasInfo)o;
			return allowComplexNumbers == other.allowComplexNumbers && allowAbs == other.allowAbs;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (allowComplexNumbers ? 872634 : 987423)+(allowAbs ? 109263 : 62200872)+definitions.hashCode();
	}
	
	public void setAllowAbs(boolean allowAbs) {
		this.allowAbs = allowAbs;
	}
	public void setAllowComplexNumbers(boolean allowComplexNumbers) {
		this.allowComplexNumbers = allowComplexNumbers;
	}
	public void setFactorIrrationalRoots(boolean factorIrrationalRoots) {
		this.factorIrrationalRoots = factorIrrationalRoots;
	}
	public void setSingleSolutionMode(boolean singleSolutionMode) {
		this.singleSolutionMode = singleSolutionMode;
	}
	public void setRelaxedPower(boolean relaxedPower) {
		this.relaxedPower = relaxedPower;
	}
	
	public boolean allowAbs() {
		return allowAbs;
	}
	public boolean allowComplexNumbers() {
		return allowComplexNumbers;
	}
	
	public boolean factorIrrationalRoots() {
		return factorIrrationalRoots;
	}
	
	public boolean singleSolutionMode() {
		return singleSolutionMode;
	}
	
	public boolean relaxedPower() {
		return relaxedPower;
	}
}
