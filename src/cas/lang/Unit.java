package cas.lang;

import java.util.ArrayList;
import java.util.HashMap;

import cas.base.Expr;
import cas.primitive.FloatExpr;

import static cas.Cas.*;

public class Unit{
	
	public static void init() {
		initializeIdentifierSets();
		initUnitNames();
		initUnitTable();
	}
	
	public static ArrayList<String> unitNames = new ArrayList<String>();
	
	public static final double EARTH_GRAVITY = 9.80665;
	
	
	private static void initUnitNames(){
		//distance
		unitNames.add("m");
		unitNames.add("meter");
		unitNames.add("meters");
		unitNames.add("ft");
		unitNames.add("feet");
		unitNames.add("foot");
		unitNames.add("centimeter");
		unitNames.add("centimeters");
		unitNames.add("cm");
		unitNames.add("millimeter");
		unitNames.add("millimeters");
		unitNames.add("mm");
		unitNames.add("kilometer");
		unitNames.add("kilometers");
		unitNames.add("km");
		unitNames.add("inch");
		unitNames.add("inches");
		unitNames.add("inche");
		unitNames.add("in");
		unitNames.add("yard");
		unitNames.add("yards");
		unitNames.add("yd");
		unitNames.add("mile");
		unitNames.add("miles");
		unitNames.add("mi");
		unitNames.add("smoot");
		unitNames.add("smoots");
		//speed
		unitNames.add("m/s");
		unitNames.add("meter/second");
		unitNames.add("meters/second");
		unitNames.add("km/s");
		unitNames.add("kilometer/second");
		unitNames.add("kilometers/second");
		unitNames.add("km/hr");
		unitNames.add("kilometer/hour");
		unitNames.add("kilometers/hour");
		unitNames.add("ft/s");
		unitNames.add("foot/second");
		unitNames.add("feet/second");
		unitNames.add("mi/hr");
		unitNames.add("mile/hour");
		unitNames.add("miles/hour");
		//temp
		unitNames.add("f");
		unitNames.add("fahrenheit");
		unitNames.add("c");
		unitNames.add("ce");
		unitNames.add("celsius");
		unitNames.add("celsiu");
		unitNames.add("kelvin");
		unitNames.add("k");
		//weight/force
		unitNames.add("kilogram");
		unitNames.add("kilograms");
		unitNames.add("kilo");
		unitNames.add("kilos");
		unitNames.add("kg");
		unitNames.add("pound");
		unitNames.add("pounds");
		unitNames.add("lb");
		unitNames.add("lbs");
		unitNames.add("newton");
		unitNames.add("newtons");
		unitNames.add("n");
		unitNames.add("g's");
		unitNames.add("g'");
		unitNames.add("grams");
		unitNames.add("gram");
		unitNames.add("g");
		unitNames.add("milligrams");
		unitNames.add("milligram");
		unitNames.add("mg");
		unitNames.add("ton");
		unitNames.add("tons");
		unitNames.add("t");
	}
	
	
	public static Expr celsiusToFahrenheit(Expr c){
		return sum(div(prod(c,floatExpr(9.0)),floatExpr(5.0)),floatExpr(32.0));
	}
	public static Expr fahrenheitToCelsius(Expr f){
		return div(prod(sub(f,floatExpr(32.0)),floatExpr(5.0)),floatExpr(9.0));
	}
	
	public enum UnitType{
		m,ft,cm,mm,km,in,yd,mi,smoot,//meters,feet,centimeter,millimeter,kilometer,inches,yards,miles,smoots
		mps,kmps,kmphr,ftps,miphr,//meters per second,kilometers per second,kilometers per hour,feet per second,miles per hour
		fh,ce,ke,//fahrenheit , celsius
		kg,lb,N,gs,g,mg,t//kilograms, pounds, newtons, newtons/(earth gravity),grams,milligrams,tons
	}
	private static HashMap<UnitType,Double> unitTable = new HashMap<UnitType,Double>();
	private static void initUnitTable(){
		//distance
		unitTable.put(UnitType.m, 1.0);
		unitTable.put(UnitType.cm, 100.0);
		unitTable.put(UnitType.mm, 1000.0);
		unitTable.put(UnitType.mm, 1000.0);
		unitTable.put(UnitType.km, .001);
		unitTable.put(UnitType.ft, 3.280839895);
		unitTable.put(UnitType.in, 39.37007874);
		unitTable.put(UnitType.yd, 1.0936132983);
		unitTable.put(UnitType.mi,0.00062137119224);
		unitTable.put(UnitType.smoot, 0.587613);
		//speed
		unitTable.put(UnitType.mps, 1.0);
		unitTable.put(UnitType.kmps, .001);
		unitTable.put(UnitType.kmphr, 3.6);
		unitTable.put(UnitType.ftps, 3.280839895);
		unitTable.put(UnitType.miphr, 2.236936292);
		//weight/force
		unitTable.put(UnitType.kg, 1.0);
		unitTable.put(UnitType.lb, 2.2046226218);
		unitTable.put(UnitType.N, EARTH_GRAVITY);
		unitTable.put(UnitType.gs, 1.0);
		unitTable.put(UnitType.g, 1000.0);
		unitTable.put(UnitType.mg, 1000000.0);
		unitTable.put(UnitType.t, .001);
		
	}
	
	private static final double PREC = 10000000000.0;
	
	private static final double KELVIN_SHIFT = 273.15;
	
	public static Expr conv(Expr from,UnitType fromUnit, UnitType toUnit){
		if(fromUnit == toUnit) {
			return from;
		}else if(toUnit == UnitType.ke) {
			return sum(conv(from,fromUnit,UnitType.ce),floatExpr(KELVIN_SHIFT));
		}else if(fromUnit == UnitType.ce && toUnit == UnitType.fh){
			return celsiusToFahrenheit(from);
		}else if(fromUnit == UnitType.fh && toUnit == UnitType.ce){
			return fahrenheitToCelsius(from);
		}else if(fromUnit == UnitType.ke) {
			return sum(conv(from,UnitType.ce,toUnit),floatExpr(-KELVIN_SHIFT));
		}else{
			double multiplier = unitTable.get(toUnit)/unitTable.get(fromUnit);
			double dig = Math.pow(10,Math.floor(Math.log10(multiplier)));
			multiplier = Math.round(multiplier/dig*PREC)/PREC*dig;//round to the number of sigFigs of PREC
			FloatExpr multiplierExpr = floatExpr(multiplier);
			return prod(multiplierExpr,from);
		}
	}
	
	//keeps a list of possible names that refers to a particular unit
	static class UnitIdentifierSet{
		private String[] possibleNames = null;
		private UnitType outType = null;
		
		public UnitIdentifierSet(UnitType outType,String ...names) {
			possibleNames = names;
			this.outType = outType;
		}
		public boolean hasName(String name) {
			for(String n:possibleNames) if(name.equals(n)) return true;
			return false;
		}
		public UnitType getUnitType() {
			return outType;
		}
	}
	
	//keeps track of all the names associated with different units
	static class UnitNamesCollection{
		private ArrayList<UnitIdentifierSet> allUnitIdentifiers = null;
		
		public UnitNamesCollection() {
			allUnitIdentifiers = new ArrayList<UnitIdentifierSet>();
		}
		
		private String prepareString(String name) {
			name = name.toLowerCase();
			
			if(!name.contains("/") && name.charAt(name.length()-1) == 's'){//strip unneeded s
				name = name.substring(0,name.length()-1);
			}
			
			return name;
		}
		
		//get the unit associated with a name if it exists
		public UnitType getUnit(String name) throws Exception{
			name = prepareString(name);
			
			for(UnitIdentifierSet ui:allUnitIdentifiers) {
				if(ui.hasName(name)) return ui.getUnitType();
			}
			throw new Exception("no known unit of distance named: "+name);
		}
		
		public void addUnitIdentifierSet(UnitType outType,String ...names) {
			allUnitIdentifiers.add(new UnitIdentifierSet(outType,names));
		}
	}
	
	private static UnitNamesCollection unitNamesCollection;
	
	private static void initializeIdentifierSets() {
		unitNamesCollection = new UnitNamesCollection();
		
		UnitNamesCollection unc = unitNamesCollection;//shortcut name
		
		{//distances
			unc.addUnitIdentifierSet(UnitType.m, "m","meter");
			unc.addUnitIdentifierSet(UnitType.ft,"ft","feet","foot");
			unc.addUnitIdentifierSet(UnitType.cm,"centimeter","cm");
			unc.addUnitIdentifierSet(UnitType.mm,"millimeter","mm");
			unc.addUnitIdentifierSet(UnitType.km,"kilometer","km");
			unc.addUnitIdentifierSet(UnitType.in,"in","inch","inche");//inche is not a spelling mistake
			unc.addUnitIdentifierSet(UnitType.yd,"yd","yard");
			unc.addUnitIdentifierSet(UnitType.mi,"mi","mile");
			unc.addUnitIdentifierSet(UnitType.smoot,"smoot");
		}
		
		{//speed
			unc.addUnitIdentifierSet(UnitType.mps, "m/s","meters/second","meter/second");
			unc.addUnitIdentifierSet(UnitType.kmps, "km/s","kilometers/second","kilometer/second");
			unc.addUnitIdentifierSet(UnitType.kmphr, "km/hr","kilometers/hour","kilometer/hour");
			unc.addUnitIdentifierSet(UnitType.ftps, "ft/s","feet/second","foot/second");
			unc.addUnitIdentifierSet(UnitType.miphr, "mi/hr","miles/hour","mile/hour");
		}
		
		{//temperature
			unc.addUnitIdentifierSet(UnitType.fh,"f", "fahrenheit");
			unc.addUnitIdentifierSet(UnitType.ce,"c", "ce","celsiu");//again not a misspelled word
			unc.addUnitIdentifierSet(UnitType.ke, "k","kelvin");
		}
		
		{//weight or force
			unc.addUnitIdentifierSet(UnitType.kg, "kg","kilogram","kilo");
			unc.addUnitIdentifierSet(UnitType.lb, "lb","pound");
			unc.addUnitIdentifierSet(UnitType.N, "n","newton");
			unc.addUnitIdentifierSet(UnitType.gs, "g'");
			unc.addUnitIdentifierSet(UnitType.g, "gram","g");
			unc.addUnitIdentifierSet(UnitType.mg, "milligram","mg");
			unc.addUnitIdentifierSet(UnitType.t, "t","ton");
		}
	}
	
	public static UnitType getUnit(String s) throws Exception{//shortcut
		return unitNamesCollection.getUnit(s);
	}
	
}
