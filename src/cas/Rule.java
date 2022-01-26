package cas;

public class Rule extends Expr{
	private static final long serialVersionUID = 5928512201641990677L;
	
	static final int VERY_EASY = 0,EASY = 1,UNCOMMON = 2,TRICKY = 3,CHALLENGING = 4,DIFFICULT = 5,VERY_DIFFICULT = 6;//this is opinion based
	static final boolean VERBOSE_DEFAULT = false;
	static final int VERBOSE_DIFFICULTY = VERY_EASY;
			
	public Equ rule = null;
	String name = null;
	public boolean verbose = VERBOSE_DEFAULT;
	int difficulty = 0;
	
	public Rule(String rule,String name,int difficulty){
		this.rule = (Equ) Interpreter.createExpr(rule);
		this.name = name;
		this.difficulty = difficulty;
		init();
	}
	public Rule(String name,int difficulty){
		this.name = name;
		this.difficulty = difficulty;
		init();
	}
	public void init(){
		
	}
	Expr applyRuleToExpr(Expr e,Settings settings){//note this may modify the original expression. The return is there so that if it changes expression type
		Expr.ModifyFromExampleResult result = e.modifyFromExampleSpecific(rule, settings);
		return result.expr;
	}
	@Override
	public String toString(){
		return name;
	}
	
	public static void loadRules(){
		System.out.println("loading CAS rules...");
		Acos.loadRules();
		And.loadRules();
		Approx.loadRules();
		Asin.loadRules();
		Atan.loadRules();
		Cos.loadRules();
		Diff.loadRules();
		Distr.loadRules();
		Div.loadRules();
		ExprList.loadRules();
		
		Factor.loadRules();
		Gamma.loadRules();
		Integrate.loadRules();
		IntegrateOver.loadRules();
		LambertW.loadRules();
		Limit.loadRules();
		
		Ln.loadRules();
		Not.loadRules();
		Or.loadRules();
		Power.loadRules();
		Prod.loadRules();
		
		Sin.loadRules();
		
		SimpleFuncs.loadRules();
		Sum.loadRules();
		Tan.loadRules();
		System.out.println("done loading Rules!");
	}
	
	@Override
	ExprList getRuleSequence() {
		return null;
	}
	@Override
	public ComplexFloat convertToFloat(ExprList varDefs) {
		return null;
	}
}
