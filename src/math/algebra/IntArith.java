package math.algebra;

import java.math.BigInteger;
import java.util.ArrayList;

public class IntArith {
	
	public static Power toPower(BigInteger value) {
		
		if(value.equals(BigInteger.ZERO)) return new Power(new IntC(0),new IntC(1));
		
		Product pr =  Prime.primeFactor(value);
		
		//System.out.println(value);
		
		BigInteger lowestExpo = BigInteger.valueOf(Long.MAX_VALUE);
		boolean found = false;
		
		for(Container c:pr.containers) {
			Power cP = (Power)c;
			IntC expo = (IntC)cP.expo;
			
			if(expo.value.compareTo(lowestExpo) == -1) {
				lowestExpo = expo.value;
				found = true;
			}
		}
		
		if(!found) lowestExpo = BigInteger.ONE;
		
		for(Container c:pr.containers) {
			Power cP = (Power)c;
			IntC expo = (IntC)cP.expo;
			
			if(expo.value.mod(lowestExpo).equals(BigInteger.ZERO)) {
				BigInteger newBaseVal = power(((IntC)cP.base).value,expo.value.divide(lowestExpo));
				cP.base = new IntC(newBaseVal);
				cP.expo = new IntC(lowestExpo);
			}else {
				return null;
			}
			
		}
		
		
		BigInteger prod = BigInteger.ONE;
		
		for(Container c:pr.containers) {
			Power cP = (Power)c;
			IntC base = (IntC)cP.base;
			prod = prod.multiply(base.value);
		}
		
		Power pwr = new Power(new IntC(prod),new IntC(lowestExpo));
		
		return pwr;
	}
	
	public static class Prime{
		
		public static void init() {
			if (init)
				return;
			
			System.out.println("initializing first 100 primes...");
			for (int i = 0; i < 100; i++) {
				largest = largest.nextProbablePrime();
				firstFewPrimes.add(largest);
			}
			
			init = true;
		}
		
		public static ArrayList<BigInteger> firstFewPrimes = new ArrayList<BigInteger>();
		public static BigInteger largest = BigInteger.ZERO;
		public static boolean init = false;
		public static ArrayList<BigInteger> largeCachedPrimes = new ArrayList<BigInteger>();
		public static ArrayList<BigInteger> fails = new ArrayList<BigInteger>();
		
		public static boolean isPrime(BigInteger num) {
			
			if(num.signum() == -1) return false;
			return num.isProbablePrime(100);
		}
		
		public static Product primeFactor(BigInteger num) {
			init();
			
			BigInteger original = num;
			
			Product factors = new Product();
			
			if(num.equals(BigInteger.ZERO)) return factors;
			
			if(num.isProbablePrime(100)) {
				factors.add(new Power(new IntC(num),new IntC(1)));
				return factors;
			}
			for(BigInteger c:fails) {
				if(num.equals(c)) {
					return factors;
				}
			}
			//
			if(num.signum() == -1) {
				factors.add(new Power(new IntC(BigInteger.valueOf(-1)),new IntC(1)));
				num = num.abs();
			}
			
			Power pwr = null;
			BigInteger lastBase = null;
			
			outer: while (true) {
				
				for (BigInteger c : largeCachedPrimes) {
					if (num.mod(c).equals(BigInteger.ZERO)) {
						if(c.equals(lastBase)) {
							pwr.expo = new IntC( ((IntC)pwr.expo).value.add(BigInteger.ONE));
						}else {
							pwr = new Power(new IntC(c),new IntC(1));
							factors.add(pwr);
						}
						num = num.divide(c);
						lastBase = c;
						continue outer;
					}
				}
				
				for (BigInteger c : firstFewPrimes) {
					if (num.mod(c).equals(BigInteger.ZERO)) {
						if(c.equals(lastBase)) {
							pwr.expo = new IntC( ((IntC)pwr.expo).value.add(BigInteger.ONE));
						}else {
							pwr = new Power(new IntC(c),new IntC(1));
							factors.add(pwr);
						}
						num = num.divide(c);
						lastBase = c;
						continue outer;
					}
				}
				break outer;
			}
			
			
			
			if (num.isProbablePrime(100)) {
				factors.add(new Power(new IntC(num),new IntC(1)));
			}else {
				
				outer: while (true) {
					for(BigInteger i:largeCachedPrimes) {
						if (num.mod(i).equals(BigInteger.ZERO)) {
							num = num.divide(i);
							if(i.equals(lastBase)) {
								pwr.expo = new IntC( ((IntC)pwr.expo).value.add(BigInteger.ONE));
							}else {
								pwr = new Power(new IntC(i),new IntC(1));
								factors.add(pwr);
							}
						}
					}
					BigInteger sqrt = num.sqrt().add(BigInteger.ONE);
					if(sqrt.compareTo(BigInteger.TWO.pow(20)) == 1)
						sqrt = BigInteger.TWO.pow(20);
					
					boolean found = false;
					inner:for (BigInteger iB = largest; iB.compareTo(sqrt)==-1; iB = iB.add(BigInteger.TWO)) {
						if (num.mod(iB).equals(BigInteger.ZERO)) {
							num = num.divide(iB);
							found = true;
							
							if(iB.equals(lastBase)) {
								pwr.expo = new IntC( ((IntC)pwr.expo).value.add(BigInteger.ONE));
							}else {
								pwr = new Power(new IntC(iB),new IntC(1));
								factors.add(pwr);
							}
							
							if(largeCachedPrimes.size()>128)
								largeCachedPrimes.remove(0);
							largeCachedPrimes.add(iB);
							
							lastBase = iB;
							
							break inner;
							
						}
					}
					
					if(!found) break outer;
					
					if (num.isProbablePrime(100)) {
						
						if(num.equals(lastBase)) {
							pwr.expo = new IntC( ((IntC)pwr.expo).value.add(BigInteger.ONE));
						}else {
							pwr = new Power(new IntC(num),new IntC(1));
							factors.add(pwr);
						}
						
						lastBase = num;
						
						break outer;
					} else {
						continue outer;
					}
					
				}
				
			}
			if(!num.isProbablePrime(100) && !num.equals(BigInteger.ONE)) {
				fails.add(num);
				fails.add(original);
				factors.containers.clear();
				return factors;
			}
			return factors;
		}
		
	}


	public static BigInteger power(BigInteger base, BigInteger expo) {
		BigInteger res = BigInteger.valueOf(1);
		for (BigInteger i = BigInteger.valueOf(0); i.compareTo(expo) == -1; i = i.add(BigInteger.valueOf(1))) {
			res = res.multiply(base);
		}
		return res;
	}

	public static Container simpleFrac(BigInteger num, BigInteger den) {
		boolean neg = (num.signum() == -1) != (den.signum() == -1);
		num = num.abs();
		den = den.abs();

		BigInteger gcd = num.gcd(den);
		num = num.divide(gcd);
		den = den.divide(gcd);

		if (neg)
			num = num.multiply(BigInteger.valueOf(-1));

		if (!den.equals(BigInteger.ONE) && !num.equals(BigInteger.ONE)) {
			Product out = new Product();
			out.add(new IntC(num));
			out.add(new Power(new IntC(den), new IntC(-1)));
			return out;
		}
		if (!den.equals(BigInteger.ONE)) {
			return new Power(new IntC(den), new IntC(-1));
		}

		return new IntC(num);

	}
}
