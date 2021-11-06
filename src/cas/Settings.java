package cas;

import java.io.Serializable;

public class Settings extends QuickMath implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -423836233586167621L;
	public boolean allowComplexNumbers = false;
	
	public Settings() {
	}
	public static Settings normal = new Settings();
	
	@Override
	public String toString() {
		return "allowComplexNumbers:"+allowComplexNumbers+"\n";
		
	}
	
	public boolean isSame(Settings other) {
		return allowComplexNumbers == other.allowComplexNumbers;
	}
}
