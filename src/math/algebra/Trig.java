package math.algebra;

public abstract class Trig extends Container{
	
	Container container;
	
	static boolean init = false;

	static Product piOver2;
	static Product piOver4;
	static Product piOver3;
	static Product piOver6;
	static Product fivePiOver6;
	static Product threePiOver4;
	static Product sevenPiOver6;
	static Product fivePiOver4;
	static Product elevenPiOver6;
	static Product fivePiOver3;
	static Product threePiOver2;
	static Product sevenPiOver4;
	static Product twoPiOver3;
	static Product fourPiOver3;
	static Product twoPi;
	static IntC zero;
	
	static void init() {
		if (init)
			return;
		
		
		piOver2 = new Product();
		piOver4 = new Product();
		piOver3 = new Product();
		piOver6 = new Product();
		fivePiOver6 = new Product();
		threePiOver4 = new Product();
		sevenPiOver6 = new Product();
		fivePiOver4 = new Product();
		elevenPiOver6 = new Product();
		fivePiOver3 = new Product();
		threePiOver2 = new Product();
		sevenPiOver4 = new Product();
		twoPiOver3 = new Product();
		fourPiOver3 = new Product();
		twoPi = new Product();
		zero = new IntC(0);

		piOver2.add(new Pi());
		piOver2.add(new Power(new IntC(2), new IntC(-1)));

		piOver4.add(new Pi());
		piOver4.add(new Power(new IntC(4), new IntC(-1)));

		piOver3.add(new Pi());
		piOver3.add(new Power(new IntC(3), new IntC(-1)));

		piOver6.add(new Pi());
		piOver6.add(new Power(new IntC(6), new IntC(-1)));

		fivePiOver6.add(new IntC(5));
		fivePiOver6.add(new Pi());
		fivePiOver6.add(new Power(new IntC(6), new IntC(-1)));

		threePiOver4.add(new IntC(3));
		threePiOver4.add(new Pi());
		threePiOver4.add(new Power(new IntC(4), new IntC(-1)));

		sevenPiOver6.add(new IntC(7));
		sevenPiOver6.add(new Pi());
		sevenPiOver6.add(new Power(new IntC(6), new IntC(-1)));

		fivePiOver4.add(new IntC(5));
		fivePiOver4.add(new Pi());
		fivePiOver4.add(new Power(new IntC(4), new IntC(-1)));

		elevenPiOver6.add(new IntC(11));
		elevenPiOver6.add(new Pi());
		elevenPiOver6.add(new Power(new IntC(6), new IntC(-1)));

		fivePiOver3.add(new IntC(5));
		fivePiOver3.add(new Pi());
		fivePiOver3.add(new Power(new IntC(3), new IntC(-1)));

		threePiOver2.add(new IntC(3));
		threePiOver2.add(new Pi());
		threePiOver2.add(new Power(new IntC(2), new IntC(-1)));

		sevenPiOver4.add(new IntC(7));
		sevenPiOver4.add(new Pi());
		sevenPiOver4.add(new Power(new IntC(4), new IntC(-1)));

		twoPi.add(new Pi());
		twoPi.add(new IntC(2));

		twoPiOver3.add(new IntC(2));
		twoPiOver3.add(new Pi());
		twoPiOver3.add(new Power(new IntC(3), new IntC(-1)));

		fourPiOver3.add(new IntC(4));
		fourPiOver3.add(new Pi());
		fourPiOver3.add(new Power(new IntC(3), new IntC(-1)));

		init = true;
	}

	@Override
	public boolean constant() {
		return container.constant();
	}

	@Override
	public boolean containsVars() {
		return container.containsVars();
	}

	@Override
	public boolean containsVar(String name) {
		return container.containsVar(name);
	}
	
}
