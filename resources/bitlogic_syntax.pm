/*

This defines the structure of the parsing for bit logic
Do not modify this file unless you know what you are doing

*/
[
	//becomes token either one works
	token("->",becomes_char),
	token("=>",becomes_char),
	
	[//numbers
		[char("0123456789") -> num],
		[type("num"),type("num") -> num:MERGE],
	],
	[//float
		[type("num"),char("."),type("num") -> frac],
		[type("num"),char(".") -> frac],
		[type("frac"),char("eE"),type("num") -> float],
		[type("frac"),char("eE"),char("-"),type("num") -> float],
		[type("frac") -> float]
	],
	[//variables
		[
			char("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ") -> var
		],
		[type("var"),type("var") -> var:MERGE],
		[type("var"),type("num") -> var:MERGE],
		[type("num"),type("var") -> var:MERGE]
	],
	[//parenthesis
		char("("),type("NODE_SEQUENCE"),char(")") -> paren
		:REMOVE_OPERATOR
	],
	[//set
		char("{"),type("NODE_SEQUENCE"),char("}") -> set
		:REMOVE_OPERATOR
	],
	[//sequence
		char("["),type("NODE_SEQUENCE"),char("]") -> sequence
		:REMOVE_OPERATOR
	],
	//information list
	[
		type("NODE_SEQUENCE"),char(","),type("NODE_SEQUENCE") -> info_list
		:REMOVE_OPERATOR
	],
	//becomes
	[
		type("NODE_SEQUENCE"),type("becomes_char"),type("NODE_SEQUENCE") -> becomes
		:REMOVE_OPERATOR
	],
	[//ternary stuff
		//ternary header
		[
			type("NODE_SEQUENCE"),char("?") -> ternary_header
			:REMOVE_OPERATOR
		],
		
		//ternary
		[
			type("ternary_header"),type("NODE_SEQUENCE") -> ternary
		],
		//ternary_results
		[
			type("NODE_SEQUENCE"),char(":"),type("NODE_SEQUENCE") -> ternary_results
			:REMOVE_OPERATOR
		],
	],
	//equ
	[
		type("NODE_SEQUENCE"),char("="),type("NODE_SEQUENCE") -> equ
		:REMOVE_OPERATOR
	],
	//greater
	[
		type("NODE_SEQUENCE"),char(">"),type("NODE_SEQUENCE") -> greater
		:REMOVE_OPERATOR
	],
	//less
	[
		type("NODE_SEQUENCE"),char("<"),type("NODE_SEQUENCE") -> less
		:REMOVE_OPERATOR
	],
	
	//function
	[type("var"),type("paren")->func],
	
	[//boolean algebra
		//or
		[
			type("NODE_SEQUENCE"),char("|"),type("NODE_SEQUENCE") -> or
			:REMOVE_OPERATOR
		],
		
		//and
		[
			type("NODE_SEQUENCE"),char("&"),type("NODE_SEQUENCE") -> and
			:REMOVE_OPERATOR
		],
		//not
		[
			char("~"),type("NODE_SEQUENCE") -> not
			:REMOVE_OPERATOR
		],
	],
	[//classic math operators
		//negative is based on context
		[
			[contextChar("*"),char("-") -> neg_minus],
			[contextChar("^"),char("-") -> neg_minus],
			[contextChar("+"),char("-") -> neg_minus],
			[contextChar("/"),char("-") -> neg_minus]
		],
		//sum
		[
			type("NODE_SEQUENCE"),char("+-"),type("NODE_SEQUENCE") -> sum
		],
		//product and div operators
		[
			type("NODE_SEQUENCE"),char("*/"),type("NODE_SEQUENCE") -> prod
		],
		//dot operator
		[
			type("NODE_SEQUENCE"),char("."),type("NODE_SEQUENCE") -> dot
			:REMOVE_OPERATOR
		],
		//negative
		[
			type("neg_minus"),type("ANY") -> neg
		],
		//pow
		[
			type("ANY"),char("^"),type("NODE_SEQUENCE") -> pow
		],
		//factorial
		[
			type("NODE_SEQUENCE"),char("!") -> factorial
			:REMOVE_OPERATOR
		]
	]
	
]