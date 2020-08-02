package math.algebra;

public abstract class Trig extends Container{
	
	Container container;

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
