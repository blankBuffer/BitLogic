package cmd;
import java.util.Scanner;

import math.algebra.*;
import math.booleanAlgebra.*;

public class BasicConstructor {
	static Scanner in = null;
	public static BoolContainer makeBoolContainer() {
		System.out.println("0 state,1 var,2 and,3 or,4 not,5 list");
		
		BoolContainer out = null;
		int choice = in.nextInt();
		
		if(choice == 0) {
			System.out.println("type in value 0 false,1 true");
			out = new BoolState(in.nextInt() == 1);
		}else if(choice == 1) {
			System.out.println("type in variable name");
			in.nextLine();
			String name = in.nextLine();
			out = new BoolVar(name);
		}else if(choice == 2) {
			System.out.println("how many elements");
			int n = in.nextInt();
			And and = new And();
			for(int i = 0;i<n;i++) {
				and.add(makeBoolContainer());
			}
			out = and;
		}else if(choice == 3) {
			System.out.println("how many elements");
			int n = in.nextInt();
			Or or = new Or();
			for(int i = 0;i<n;i++) {
				or.add(makeBoolContainer());
			}
			out = or;
		}else if(choice == 4) {
			out = new Not(makeBoolContainer());
		}else if(choice == 5) {
			System.out.println("how many elements");
			int n = in.nextInt();
			BoolList list = new BoolList();
			for(int i = 0;i<n;i++) {
				list.add(makeBoolContainer());
			}
			out = list;
		}
		
		return out;
	}
	
	public static Container makeContainer() {
		
		System.out.println("0 int,1 var,2 product,3 sum,4 power,5 log,6 E,7 list,8 cos,9 sin,10 pi");
		
		Container out = null;
		int choice = in.nextInt();
		
		if(choice == 0) {
			System.out.println("type in value");
			out = new IntC(in.nextInt());
		}else if(choice == 1) {
			System.out.println("type in variable name");
			in.nextLine();
			String name = in.nextLine();
			out = new Var(name);
		}else if(choice == 2) {
			System.out.println("how many elements");
			int n = in.nextInt();
			Product p = new Product();
			for(int i = 0;i<n;i++) {
				p.add(makeContainer());
			}
			out = p;
		}else if(choice == 3) {
			System.out.println("how many elements");
			long n = in.nextLong();
			Sum s = new Sum();
			for(int i = 0;i<n;i++) {
				s.add(makeContainer());
			}
			out = s;
		}else if(choice == 4) {
			System.out.println("describe base");
			Container base = makeContainer();
			System.out.println("describe expo");
			Container expo = makeContainer();
			out = new Power(base,expo);
		}else if(choice == 5) {
			System.out.println("describe expression");
			out = new Log(makeContainer());
		}else if(choice == 6) {
			out = new E();
		}else if(choice == 7) {
			
		}else if(choice == 8) {
			System.out.println("describe expression");
			out = new Cos(makeContainer());
		}else if (choice == 9) {
			System.out.println("describe expression");
			out = new Sin(makeContainer());
		}else if(choice == 10) {
			return new Pi();
		}
		
        return out;
	}
	
	public static void basicProg() {
		Container c = BasicConstructor.makeContainer();
		c.classicPrint();
		System.out.println();
		System.out.println(c.approx());
		Container simp = c.simplify();
		System.out.println();
		simp.classicPrint();
		System.out.println();
		System.out.println(simp.approx());
		
	}
	/*
	public static void boolBasicProg() {
		BoolContainer c = BasicConstructor.makeBoolContainer();
		c.print();
		System.out.println();
		BoolContainer simp = c.simplify();
		System.out.println();
		simp.print();
		System.out.println();
		
	}
	*/
	
	
}
