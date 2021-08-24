package cas;

public class Settings extends QuickMath{
	public boolean factor = true;
	public boolean distr = true;
	public boolean allowComplexNumbers = false;
	public boolean powExpandMode = false;
	
	public Settings() {
	}
	public Settings(Settings other) {
		factor = other.factor;
		distr = other.distr;
		allowComplexNumbers = other.allowComplexNumbers;
		powExpandMode = other.powExpandMode;
	}
	public static Settings normal = new Settings();
}
