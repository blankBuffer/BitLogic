package ui;
import java.util.ArrayList;

import javax.swing.*;

class Help extends JFrame{

	private static final long serialVersionUID = -3833826144414049766L;

	static class Doc{
		String doc = "";
		void addHeader(String text,int level) {
			doc+="<h"+level+">"+text+"</h"+level+">";
		}
		void addParagraph(String text) {
			doc+= "<p>"+text+"</p>";
		}
		void addList(ArrayList<String> listItems) {
			doc+="<ul>";
			for(String text:listItems) {
				doc+="<li>"+text+"</li>";
			}
			doc+="</ul>";
		}
	}
	
	void initMainDoc(Doc mainDoc) {
		mainDoc.addHeader("Getting Started", 1);
		mainDoc.addParagraph("Welcome to my Tool Box. The blue Area is the stack you can add, modify and remove expressions. The text field below the stack is where you type expressios. Hit the enter key to execute whetever is in the enrty area.");
		mainDoc.addHeader("Algebraic Input", 2);
		mainDoc.addParagraph("If for example you want to add x^2 into the stack. In the entry area type \"x^2\" then hit enter. If you want to see the equations in the stack visually, hit the \"show graph\" button");
		mainDoc.addHeader("RPN Input", 2);
		mainDoc.addParagraph("");
		mainDoc.addHeader("List Of Commands", 2);
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("sqrt - square root");
		commands.add("inv - inverse which is the same as 1/[]");
		commands.add("approx - convert some expression into a floating point. Has two parameters. First parameter is the expression to convert and seconds paramter is definitions of variables");
		
		mainDoc.addList(commands);
		
		
	}
	
	public Help() {
		super("help");
		setSize(400,400);
		JTextPane textArea = new JTextPane();
		textArea.setContentType("text/html");
		textArea.setEditable(false);
		Doc mainDoc = new Doc();
		initMainDoc(mainDoc);
		textArea.setText(mainDoc.doc);
		textArea.setCaretPosition(0);
		add(textArea);
		JScrollPane scrollBar = new JScrollPane(textArea);
		add(scrollBar);
		setVisible(true);
	}
}
