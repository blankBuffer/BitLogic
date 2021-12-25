package cas;

public class Rule {
	static final int VERY_EASY = 0,EASY = 1,UNCOMMON = 2,TRICKY = 3,CHALLENGING = 4,DIFFICULT = 5,VERY_DIFFICULT = 6;
	static final boolean VERBOSE_DEFAULT = true;
	static final int VERBOSE_DIFFICULTY = VERY_EASY;
			
	public Equ rule = null;
	String name = null;
	public boolean verbose = VERBOSE_DEFAULT;
	String example = null;
	int difficulty = 0;
	
	public Rule(String rule,String name,int difficulty){
		this.rule = (Equ) Interpreter.createExpr(rule);
		this.name = name;
		this.difficulty = difficulty;
		init();
	}
	public Rule(Equ rule,String name,int difficulty){
		this.rule = rule;
		this.name = name;
		this.difficulty = difficulty;
		init();
	}
	public Rule(String name,int difficulty){
		this.name = name;
		this.difficulty = difficulty;
		init();
	}
	public void verboseMessage(Expr original,Expr result){
		if(verbose && difficulty>=VERBOSE_DIFFICULTY){
			if(rule != null){
				System.out.println("applied : "+name+" : "+rule+" : "+original+" -> "+result);
			}else{
				System.out.println("applied : "+name+" : example : "+example+" : "+original+" -> "+result);
			}
		}
	}
	public void init(){
		
	}
	Expr applyRuleToExpression(Expr e,Settings settings){//note this may modify the original expression. The return is there so that if it changes expression type
		Expr.ModifyFromExampleResult result = e.modifyFromExampleSpecific(rule, settings);
		if(result.success){
			verboseMessage(e,result.expr);
		}
		return result.expr;
	}
}
