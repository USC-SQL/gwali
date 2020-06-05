package edu.usc.layoutgraph.node;

import java.io.Serializable;

import edu.gatech.xpert.dom.DomNode;

/**
 * Layout Graph Node
 * @author Abdulmajeed
 */
public class LayoutNode implements Serializable{

	private static final long serialVersionUID = 1L;
	long area;
	private DomNode domNode;
	
	public LayoutNode(DomNode domNode) {
		this.setDomNode(domNode);
		area = (getX2() - getX1()) * (getY2() - getY1());
	}

	public long getArea() {
		return area;
	}

	public boolean contains(LayoutNode n) {
		if (this.getX1() <= n.getX1() && this.getY1() <= n.getY1()
				&& this.getX2() >= n.getX2() && this.getY2() >= n.getY2()) {
			return true;
		}
		return false;
	}
	
	public int[] getCenter(){
		int center[] = {(getX1() + getX2()) / 2, (getY1() + getY2()) / 2};
		return center;
	}
	

	/**
	 * @return the domNode
	 */
	public DomNode getDomNode() {
		return domNode;
	}

	/**
	 * @param domNode the domNode to set
	 */
	public void setDomNode(DomNode domNode) {
		this.domNode = domNode;
	}


	/**
	 * @return the x1
	 */
	public int getX1() {
		return domNode.getCoords()[0];
	}


	/**
	 * @return the y1
	 */
	public int getY1() {
		return domNode.getCoords()[1];
	}


	/**
	 * @return the x2
	 */
	public int getX2() {
		return domNode.getCoords()[2];
	}



	/**
	 * @return the y2
	 */
	public int getY2() {
		return domNode.getCoords()[3];
	}




	public String toString() {
		return getDomNode().getxPath()+" coords:("+getX1()+","+getY1()+","+getX2()+","+getY2()+")";
	}
}
