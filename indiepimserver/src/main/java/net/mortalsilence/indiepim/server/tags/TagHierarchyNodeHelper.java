package net.mortalsilence.indiepim.server.tags;

import net.mortalsilence.indiepim.server.SharedConstants;
import net.mortalsilence.indiepim.server.ds.Node;
import net.mortalsilence.indiepim.server.ds.Tree;
import org.springframework.stereotype.Service;

@Service
public class TagHierarchyNodeHelper {

	public void addPath(TagHierarchyNode node, String path, Long leafId) {
			
		int index = path.indexOf(SharedConstants.TAG_LINEAGE_SEPARATOR);
		
		TagHierarchyNode child;
		if(index == -1) {
			// Leaf
			child = getOrCreateChild(node, path);
			child.data.id = leafId;
		} else {
			child = getOrCreateChild(node, path.substring(0, index));
			addPath(child, path.substring(index + 1), leafId);
		}
		
	}
	
	public TagHierarchyNode getOrCreateChild(TagHierarchyNode node, String tag) {
		for (Node<TagHierarchyNodeData> child : node.getChildren()) {
			if(tag.equals(child.data.tag))
				return (TagHierarchyNode)child;
		}
		TagHierarchyNodeData nodeData = new TagHierarchyNodeData();
		nodeData.tag = tag;
		TagHierarchyNode newNode = new TagHierarchyNode(nodeData);
		node.addChild(newNode);
		return newNode;
	}
}
