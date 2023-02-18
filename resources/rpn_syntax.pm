/*
	RPN expression building style
*/

[
	token("=>",becomes),
	token("->",becomes),
	
	token("?:",ternary),
	token("[]",sequence),
	token("{}",set),
	
	[//spacer
		[char(" ") -> spacer],
		[type("spacer"),type("spacer") -> spacer:MERGE]
	],
	
	[//numbers
		[char("0123456789") -> num],
		[type("num"),type("num") -> num:MERGE],
	],
	
	[//variables
		[
			char("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ") -> var
		],
		[type("var"),type("var") -> var:MERGE],
		[type("var"),type("num") -> var:MERGE],
		[type("num"),type("var") -> var:MERGE]
	],
	
	//functions is a var with an opening paren , example "sin("
	[type("var"),char("(") -> func],
	
	[//operators
		[char("+-*/") -> op],
		[type("ternary") -> op],
		[type("sequence") -> op],
		[type("becomes") -> op],
		[type("set") -> op],
		[type("func") -> op]
	],
	
	//data
	[
		[type("var") -> data],
		[type("num") -> data],
	],
	
	//remove spaces
	[type("spacer") -> DELETE],
	
	[//operator application
		[type("NODE_SEQUENCE"),type("op") -> application]
	],
	
	[//parameters
		[type("data") -> app_params],
		[
			type("app_params"),type("app_params") -> app_params
			:MERGE
		],
	],
	
]