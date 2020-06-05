package edu.gatech.xpert.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.gatech.xpert.dom.visitors.ApproxMatchVisitor;
import edu.gatech.xpert.dom.visitors.DomVisitor;
import edu.gatech.xpert.dom.visitors.IgnoreFontTagMatchVisitor;
import edu.gatech.xpert.dom.visitors.LevelAssignVisitor;
import edu.gatech.xpert.dom.visitors.ExactMatchVisitor;
import edu.gatech.xpert.dom.visitors.UnmatchedVisitor;

public class Matcher {
	
	float THRESHOLD_LEVEL = 0.75f;
	
	Map<DomNode, DomNode> matched = new HashMap<DomNode, DomNode>();
	List<DomNode> unmatched1 = new ArrayList<DomNode>(), unmatched2 =  new ArrayList<DomNode>();
	
	public MatchResult doMatch(Map<String, DomNode> doms, String[] urls) {
		
		if(doms.size() < 2) {
			System.err.println("DOM compare error: list size="+doms.size());
			return null;
		}
		
		DomNode root1 = doms.get(urls[0]);
		DomNode root2 = doms.get(urls[1]);
		
		// 1: Perfect Match Visitor
		List<DomNode> worklist = new ArrayList<DomNode>();
		worklist.add(root1);
		while(!worklist.isEmpty()) {
			DomNode node = worklist.remove(0);
			DomVisitor pmv = new ExactMatchVisitor(node, matched);
			root2.accept(pmv, false);
			for(DomNode child: node.getChildren()) {
				worklist.add(child);
			}
		}
		
		UnmatchedVisitor uv = new UnmatchedVisitor();
		root1.accept(uv);
		List<DomNode> unmatchedNodes = uv.getUnmatched();
		
		// 2: ignore font tag matching. 
		// used to handle cases where <font> tag is added to the text
		// for example google translate pads text elements with font tags
		// this matcher ignores these added font tags
		for(DomNode node : unmatchedNodes) {
			DomVisitor iftMatchVisitor = new IgnoreFontTagMatchVisitor(node, matched);
			root2.accept(iftMatchVisitor, false);
		}
		

		
		// Assign levels
		LevelAssignVisitor lvl = new LevelAssignVisitor(); 
		root1.accept(lvl, true);
		lvl.init();
		root2.accept(lvl, true);
		List<List<DomNode>> levels2 = lvl.getLevels();

		uv = new UnmatchedVisitor();
		root1.accept(uv);
		unmatchedNodes = uv.getUnmatched();

		// 3: Level Match Visitor
		for(DomNode node : unmatchedNodes) {
			int level = node.getLevel();
			if(level < levels2.size()) {
				List<DomNode> lNodes = levels2.get(level);
				float bestMatchIndex = 0;
				DomNode bestMatchNode = null;
				for(DomNode ln : lNodes) {
					if(!ln.isMatched() && ln.isTag()) {
						float matchIdx = DomUtils.calculateMatchIndex(node, ln);
						if(matchIdx >= THRESHOLD_LEVEL && matchIdx > bestMatchIndex) {
							bestMatchIndex = matchIdx;
							bestMatchNode = ln;
						}
					}
				}
				if(bestMatchNode != null) {
					node.setMatched(true);
					bestMatchNode.setMatched(true);
					matched.put(node, bestMatchNode);
				} else {
					worklist.add(node);
				}
			}
			else{
				worklist.add(node);
			}
		}
		
		int numberOfLevelMatch = matched.size();

		
		// 4: Approximate global matching
		for(DomNode node : worklist) {
			ApproxMatchVisitor amv = new ApproxMatchVisitor(node, matched);
			root2.accept(amv);
			amv.matchPost();
			if(!node.isMatched()) {
				unmatched1.add(node);
			}
		}
		
		uv.init();
		root2.accept(uv);
		unmatched2 = uv.getUnmatched();
		
		
		return new MatchResult(matched, unmatched1, unmatched2, numberOfLevelMatch);
	}
}
