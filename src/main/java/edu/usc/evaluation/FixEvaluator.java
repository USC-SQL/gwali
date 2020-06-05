package edu.usc.evaluation;

import edu.usc.layoutgraph.LayoutGraphBuilder;
import edu.usc.util.Utils;


public class FixEvaluator {

	public static void main(String[] args){

		String s = Utils.getPkgFileContents(LayoutGraphBuilder.class,"/getMBRs.js");
		System.out.println(s);
	}

}
