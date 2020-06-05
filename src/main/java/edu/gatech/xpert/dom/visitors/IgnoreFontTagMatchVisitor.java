package edu.gatech.xpert.dom.visitors;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.gatech.xpert.dom.DomNode;
import edu.gatech.xpert.dom.DomUtils;

public class IgnoreFontTagMatchVisitor extends DomVisitor {
	DomNode ref;
	Map<DomNode, DomNode> matched;
	boolean found;
	int cnt = 0;
	
	public IgnoreFontTagMatchVisitor(DomNode node, Map<DomNode, DomNode> matched) {
		ref = node;
		this.matched = matched;
	}

	@Override
	public void visit(DomNode node) {
		cnt++;
		if(!found && !node.isMatched()) {
			if(StringUtils.equals(ref.getTagName(), node.getTagName())) {
				DomNode ignoredFontTagNode = getRemovedFontTagNode(node);
				DomNode ignoredFontRefNode = getRemovedFontTagNode(ref);
				float matchIndex = DomUtils.calculateMatchIndex(ignoredFontRefNode, ignoredFontTagNode);
				if(matchIndex==1.0) {
					matched.put(ref, node);
					ref.setMatched(true);
					node.setMatched(true);
					found = true;
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return "IFTMV visited #nodes:"+cnt;
	}

	public DomNode getRemovedFontTagNode(DomNode node){
		String updatedXpath = node.getxPath().replaceAll("(?i)/font(\\[\\d\\])?", "");
		
		DomNode updatedNode = new DomNode(node);
		updatedNode.setxPath(updatedXpath);
		return updatedNode;
	}
}
