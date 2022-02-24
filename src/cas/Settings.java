package cas;

import java.io.Serializable;

public class Settings extends QuickMath implements Serializable{
	
	private static final long serialVersionUID = -423836233586167621L;
	public boolean allowComplexNumbers = false;
	public boolean allowAbs = true;
	
	public Settings() {
	}
	public static Settings normal = new Settings();
	
	@Override
	public String toString() {
		String out = "";
		out += "allowComplexNumbers:"+allowComplexNumbers;
		out += "allowAbs:"+allowAbs;
		return out;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Settings) {
			Settings other = (Settings)o;
			return allowComplexNumbers == other.allowComplexNumbers && allowAbs == other.allowAbs;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (allowComplexNumbers ? 872634 : 987423)+(allowAbs ? 109263 : 62200872);
	}
}
