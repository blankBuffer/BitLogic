/*
	nonsense syntax showing the syntax structure and features
*/
[//multi section rule
	[//rule
		//single line comment
		type("some_type"),//type rule node
		char("abc"),//character class rule node
		/*
			many-line 
			comment 
		*/
		typeClass(char("x"),type("no"))//some set of rule nodes as a rule node
		->
		randoli//output
		:test,tester//flags
		
	],
	[
		type("testing file"),
		char("test 2"),
		typeClass(char("x"),type("no"))
		->
		rando
		:test,tester
	],
	[//no flags example
		type("car")
		->
		vehicle
	]
	:extra_data//dont need flags for a rule list juse showing example
]