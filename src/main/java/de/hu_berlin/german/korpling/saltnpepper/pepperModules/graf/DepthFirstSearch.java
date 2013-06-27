package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.util.HashMap;
import java.util.List;

import org.xces.graf.api.IEdge;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.INode;

/** This class is meant as a workaround for floating leaf nodes in MASC/GrAF.*/
public class DepthFirstSearch {

	private boolean[] visited;
	private int nodeIndex;
	//TODO: replace two HashMaps with one BiMap from Guava!
	public HashMap<String, Integer> nodeIdToOrderedNodeNumberMap;
	public HashMap<Integer, String> OrderedNodeNumberToNodeIdMap;
	
	/** creates a depth-first search based ordering of nodes in a (part of a)
	 *  graph. */
	public DepthFirstSearch(IGraph graph, INode floatingNode) {
		visited = new boolean[graph.getNodeSetSize()];
		nodeIdToOrderedNodeNumberMap = new HashMap<String, Integer>();
		OrderedNodeNumberToNodeIdMap = new HashMap<Integer, String>();
//		System.out.println("\n\ninitialize DepthFirstSearch with sourceNode "+floatingNode.getId());
		INode treeRootNode = GrafReader.getTreeRootNodeFromLeafNode(floatingNode, graph);
		System.out.println("\troot node: "+treeRootNode.getId());
		dfs(graph, treeRootNode);
	}
	
	private void dfs(IGraph graph, INode sourceNode) {
//		System.out.println("\tstart dfs with sourceNode "+sourceNode.getId());
		String nodeId = sourceNode.getId();
		if (nodeIdToOrderedNodeNumberMap.containsKey(nodeId)) {
			int orderedNodeNumber = nodeIdToOrderedNodeNumberMap.get(nodeId);
			visited[orderedNodeNumber] = true;
		}
		else {
			nodeIdToOrderedNodeNumberMap.put(nodeId, nodeIndex);
			OrderedNodeNumberToNodeIdMap.put(nodeIndex, nodeId);
			visited[nodeIndex] = true;
			nodeIndex++;
		}
		
//		System.out.println("\toutbound connected nodes of source node: ");
		for (INode outboundConnectedNode : GrafReader.getOutboundConnectedNodes(sourceNode)) {
//			System.out.println("\t\t outboundConnectedNode "+outboundConnectedNode.getId());
			if (!isVisited(outboundConnectedNode)) {
				dfs(graph, outboundConnectedNode);
			}
		}
		
	}

	public boolean isVisited(INode node) {
		String nodeId = node.getId();
		if (nodeIdToOrderedNodeNumberMap.containsKey(nodeId)) {
			return visited[nodeIdToOrderedNodeNumberMap.get(nodeId)];
		}
		else return false;
	}
	
	/** returns the leaf node that precedes a floating node (or null if there 
	 *  is none) */
	public INode getPrecedingLeafNode(IGraph graph, INode node) {
		String nodeId = node.getId();
		if (nodeIdToOrderedNodeNumberMap.containsKey(nodeId)) {
			int nodeNumber = nodeIdToOrderedNodeNumberMap.get(nodeId);

			for (int i = nodeNumber; i>0; i--) {
				String precedingNodeId = OrderedNodeNumberToNodeIdMap.get(i-1);
				INode precedingNode = graph.findNode(precedingNodeId);
				if (GrafReader.isLeafNode(precedingNode) && !GrafReader.isFloatingNode(precedingNode)) {
					return precedingNode;
				}
			}
			return null; // there's no preceding node that is a non-floating leaf node
		}
		else return null; // node has no ordered number
	}

	/** returns the leaf node that succeedes a floating node (or null if there 
	 *  is none) */
	public INode getSucceedingLeafNode(IGraph graph, INode node) {
		String nodeId = node.getId();
		if (nodeIdToOrderedNodeNumberMap.containsKey(nodeId)) {
			int nodeNumber = nodeIdToOrderedNodeNumberMap.get(nodeId);

			for (int i = nodeNumber; i<nodeIdToOrderedNodeNumberMap.keySet().size(); i++) {
				String succeedingNodeId = OrderedNodeNumberToNodeIdMap.get(i+1);
				INode succceedingNode = graph.findNode(succeedingNodeId);
				if (succceedingNode != null && GrafReader.isLeafNode(succceedingNode) && !GrafReader.isFloatingNode(succceedingNode)) {
					return succceedingNode;
				}
			}
			return null; // there's no succeeding node that is a non-floating leaf node
		}
		else return null; // node has no ordered number
	}

	
}
