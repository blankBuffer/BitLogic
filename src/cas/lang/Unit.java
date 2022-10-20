package cas.lang;

import java.util.ArrayList;
import java.util.HashMap;

import cas.Expr;
import cas.Cas;
import cas.primitive.FloatExpr;

public class Unit extends Cas{
	
	public static ArrayList<String> unitNames = new ArrayList<String>();
	public static final double EARTH_GRAVITY = 9.80665;
	
	static{
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
		unitNames.add("N");
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
	
	public enum Type{
		m,ft,cm,mm,km,in,yd,mi,smoot,//meters,feet,centimeter,millimeter,kilometer,inches,yards,miles,smoots
		mps,kmps,kmphr,ftps,miphr,//meters per second,kilometers per second,kilometers per hour,feet per second,miles per hour
		fh,ce,//fahrenheit , celsius
		kg,lb,N,gs,g,mg,t//kilograms, pounds, newtons, newtons/(earth gravity),grams,milligrams,tons
	}
	public static HashMap<Type,Double> unitTable = new HashMap<Type,Double>();
	static{
		//distance
		unitTable.put(Type.m, 1.0);
		unitTable.put(Type.cm, 100.0);
		unitTable.put(Type.mm, 1000.0);
		unitTable.put(Type.mm, 1000.0);
		unitTable.put(Type.km, .001);
		unitTable.put(Type.ft, 3.280839895);
		unitTable.put(Type.in, 39.37007874);
		unitTable.put(Type.yd, 1.0936132983);
		unitTable.put(Type.mi,0.00062137119224);
		unitTable.put(Type.smoot, 0.587613);
		//speed
		unitTable.put(Type.mps, 1.0);
		unitTable.put(Type.kmps, .001);
		unitTable.put(Type.kmphr, 3.6);
		unitTable.put(Type.ftps, 3.280839895);
		unitTable.put(Type.miphr, 2.236936292);
		//weight/force
		unitTable.put(Type.kg, 1.0);
		unitTable.put(Type.lb, 2.2046226218);
		unitTable.put(Type.N, EARTH_GRAVITY);
		unitTable.put(Type.gs, 1.0);
		unitTable.put(Type.g, 1000.0);
		unitTable.put(Type.mg, 1000000.0);
		unitTable.put(Type.t, .001);
		
	}
	private static final double PREC = 10000000000.0;
	public static Expr conv(Expr from,Type fromUnit, Type toUnit){
		if(fromUnit == Type.ce){
			return celsiusToFahrenheit(from);
		}else if(fromUnit == Type.fh){
			return fahrenheitToCelsius(from);
		}else{
			double multiplier = unitTable.get(toUnit)/unitTable.get(fromUnit);
			double dig = Math.pow(10,Math.floor(Math.log10(multiplier)));
			multiplier = Math.round(multiplier/dig*PREC)/PREC*dig;//round to the number of sigFigs of PREC
			FloatExpr multiplierExpr = floatExpr(multiplier);
			return prod(multiplierExpr,from);
		}
	}
	public static Type getUnit(String s) throws Exception{
		s = s.toLowerCase();
		if(!s.contains("/") && s.charAt(s.length()-1) == 's'){//strip unneeded s
			s = s.substring(0,s.length()-1);
		}
		//distance
		if(s.equals("m") || s.equals("meter")){
			return Type.m;
		}else if(s.equals("ft") || s.equals("feet") || s.equals("foot")){
			return Type.ft;
		}else if(s.equals("centimeter") || s.equals("cm")){
			return Type.cm;
		}else if(s.equals("millimeter") || s.equals("mm")){
			return Type.mm;
		}else if(s.equals("kilometer") || s.equals("km")){
			return Type.km;
		}else if(s.equals("in") || s.equals("inche") || s.equals("inch")){//not spelling err btw
			return Type.in;
		}else if(s.equals("yard") || s.equals("yd")){
			return Type.yd;
		}else if(s.equals("mile") || s.equals("mi")){
			return Type.mi;
		}else if(s.equals("smoot")){
			return Type.smoot;
		}
		//speed
		else if(s.equals("m/s") || s.equals("meters/second") || s.equals("meter/second")){
			return Type.mps;
		}else if(s.equals("km/s") || s.equals("kilometers/second") || s.equals("kilometer/second")){
			return Type.kmps;
		}else if(s.equals("km/hr") || s.equals("kilometers/hour") || s.equals("kilometer/hour")){
			return Type.kmphr;
		}else if(s.equals("ft/s") || s.equals("feet/second") || s.equals("foot/second")){
			return Type.ftps;
		}else if(s.equals("mi/hr") || s.equals("miles/hour") || s.equals("mile/hour")){
			return Type.miphr;
		}else if(s.equals("f") || s.equals("fahrenheit")){
			return Type.fh;
		}else if(s.equals("c") || s.equals("celsiu") || s.equals("ce")){
			return Type.ce;
		}
		//weight/force
		else if(s.equals("kg") || s.equals("kilogram") || s.equals("kilo")){
			return Type.kg;
		}else if(s.equals("lb") || s.equals("pound")){
			return Type.lb;
		}else if(s.equals("newton") || s.equals("N")){
			return Type.N;
		}else if(s.equals("g'")){
			return Type.gs;
		}else if(s.equals("gram") || s.equals("g")){
			return Type.g;
		}else if(s.equals("milligram") || s.equals("mg")){
			return Type.mg;
		}else if(s.equals("ton") || s.equals("t")){
			return Type.t;
		}
		throw new Exception("no known unit of distance named: "+s);
	}
	
}
