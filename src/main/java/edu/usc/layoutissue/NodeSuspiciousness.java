package edu.usc.layoutissue;

import edu.gatech.xpert.dom.DomNode;

public class NodeSuspiciousness implements Comparable<NodeSuspiciousness>{
	private DomNode domNode;
	private int suspiciousness;
	public NodeSuspiciousness(DomNode domNode) {
		this.domNode = domNode;
		suspiciousness = 1;
	}
	public NodeSuspiciousness(DomNode domNode, int initialScore) {
		this.domNode = domNode;
		suspiciousness = initialScore;
	}
	
	public void increaseSuspiciousness(){
		suspiciousness++;
	}
	
	public void increaseSuspiciousness(int amount){
		suspiciousness += amount;
	}
	
	public int getSuspiciousness(){
		return suspiciousness;
	}
	public DomNode getDomNode() {
		return domNode;
	}
	@Override
	public int compareTo(NodeSuspiciousness o) {		
		return o.suspiciousness - this.suspiciousness ;
	}
	
	@Override
	public boolean equals(Object o){
		return this.domNode == ((NodeSuspiciousness)o).domNode;
	}
	
	@Override
	public String toString() {
		return domNode.getxPath() + " : (" + suspiciousness + " suspiciousness)";
	}
}
