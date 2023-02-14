/*
	nonsense syntax showing the syntax structure and features
*/
[//multi section rule
	[//rule
		//single line comment
		type("some_\"type\""),//type rule node
		char("abc"),//character class rule node
		/*
			many-line 
			comment 
		*/
		typeClass(char("x"),type("no"))//some set of rule nodes as a rule node
		->
		randoli//output
		:REMOVE_OPERATOR,NONE//flags
		
	],
	[
		type("testing file"),
		char("test 2"),
		typeClass(char("x"),type("no"))
		->
		rando
		:MERGE,LOOP
	],
	[//no flags example
		type("car")
		->
		vehicle
	],
	token("hey",blob)
	:LOOP//dont need flags for a rule list juse showing example
]