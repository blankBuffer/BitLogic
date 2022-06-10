package cas;

import java.io.Serializable;

public class CasInfo extends QuickMath implements Serializable{
	
	private static final long serialVersionUID = -423836233586167621L;
	private boolean allowComplexNumbers = false;
	private boolean allowAbs = true;
	private boolean factorIrrationalRoots = false;
	private boolean singleSolutionMode = false;
	public Defs definitions;
	
	public CasInfo() {
		definitions = new Defs();
	}
	public CasInfo(CasInfo casInfo) {
		allowComplexNumbers = casInfo.allowComplexNumbers;
		allowAbs = casInfo.allowAbs;
		factorIrrationalRoots = casInfo.factorIrrationalRoots;
		singleSolutionMode = casInfo.singleSolutionMode;
		
		definitions = casInfo.definitions;
	}
	public static CasInfo normal = new CasInfo();
	
	@Override
	public String toString() {
		String out = "";
		out += "allowComplexNumbers: "+allowComplexNumbers;
		out += " allowAbs: "+allowAbs;
		out += " singleSolutionMode: "+singleSolutionMode;
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
}
