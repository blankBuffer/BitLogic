package cas;

import java.io.Serializable;

public class CasInfo extends QuickMath implements Serializable{
	
	private static final long serialVersionUID = -423836233586167621L;
	public boolean allowComplexNumbers = false;
	public boolean allowAbs = true;
	public Defs definitions;
	
	public CasInfo() {
		definitions = new Defs();
	}
	public static CasInfo normal = new CasInfo();
	
	@Override
	public String toString() {
		String out = "";
		out += "allowComplexNumbers:"+allowComplexNumbers;
		out += "allowAbs:"+allowAbs;
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
}
