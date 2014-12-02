/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.util.HashMap;

import org.xces.graf.api.IGraph;
import org.xces.graf.api.INode;

/**
 * This class is meant as a workaround for floating leaf nodes in MASC/GrAF. The
 * depth first search creates an ordering of the graph that is used to find the
 * preceding/succeeding leaf node of a floating node.
 */
public class DepthFirstSearch {

	private boolean[] visited;
	private int nodeIndex;
	// TODO: replace two HashMaps with one BiMap from Guava!
	public HashMap<String, Integer> nodeIdToOrderedNodeNumberMap;
	public HashMap<Integer, String> OrderedNodeNumberToNodeIdMap;

	/**
	 * creates a depth-first search based ordering of nodes in a (part of a)
	 * graph.
	 */
	public DepthFirstSearch(IGraph graph, INode floatingNode) {
		visited = new boolean[graph.getNodeSetSize()];
		nodeIdToOrderedNodeNumberMap = new HashMap<String, Integer>();
		OrderedNodeNumberToNodeIdMap = new HashMap<Integer, String>();
		INode rootNode = GrafReader.getRootNodeFromNode(floatingNode, graph);
		dfs(graph, rootNode);
	}

	private void dfs(IGraph graph, INode sourceNode) {
		// System.out.println("\tstart dfs with sourceNode "+sourceNode.getId());
		String nodeId = sourceNode.getId();
		if (nodeIdToOrderedNodeNumberMap.containsKey(nodeId)) {
			int orderedNodeNumber = nodeIdToOrderedNodeNumberMap.get(nodeId);
			visited[orderedNodeNumber] = true;
		} else {
			nodeIdToOrderedNodeNumberMap.put(nodeId, nodeIndex);
			OrderedNodeNumberToNodeIdMap.put(nodeIndex, nodeId);
			visited[nodeIndex] = true;
			nodeIndex++;
		}

		// System.out.println("\toutbound connected nodes of source node: ");
		for (INode outboundConnectedNode : GrafReader.getOutboundConnectedNodes(sourceNode)) {
			// System.out.println("\t\t outboundConnectedNode "+outboundConnectedNode.getId());
			if (!isVisited(outboundConnectedNode)) {
				dfs(graph, outboundConnectedNode);
			}
		}

	}

	public boolean isVisited(INode node) {
		String nodeId = node.getId();
		if (nodeIdToOrderedNodeNumberMap.containsKey(nodeId)) {
			return visited[nodeIdToOrderedNodeNumberMap.get(nodeId)];
		} else
			return false;
	}

	/**
	 * returns the leaf node that precedes a floating node (or null if there is
	 * none)
	 */
	public INode getPrecedingLeafNode(IGraph graph, INode node) {
		String nodeId = node.getId();
		if (nodeIdToOrderedNodeNumberMap.containsKey(nodeId)) {
			int nodeNumber = nodeIdToOrderedNodeNumberMap.get(nodeId);

			for (int i = nodeNumber; i > 0; i--) {
				String precedingNodeId = OrderedNodeNumberToNodeIdMap.get(i - 1);
				INode precedingNode = graph.findNode(precedingNodeId);
				if (GrafReader.isLeafNode(precedingNode) && !GrafReader.isFloatingNode(precedingNode)) {
					return precedingNode;
				}
			}
			return null; // there's no preceding node that is a non-floating
							// leaf node
		} else
			return null; // node has no ordered number
	}

	/**
	 * returns the leaf node that succeedes a floating node (or null if there is
	 * none)
	 */
	public INode getSucceedingLeafNode(IGraph graph, INode node) {
		String nodeId = node.getId();
		if (nodeIdToOrderedNodeNumberMap.containsKey(nodeId)) {
			int nodeNumber = nodeIdToOrderedNodeNumberMap.get(nodeId);

			for (int i = nodeNumber; i < nodeIdToOrderedNodeNumberMap.keySet().size(); i++) {
				String succeedingNodeId = OrderedNodeNumberToNodeIdMap.get(i + 1);
				INode succceedingNode = graph.findNode(succeedingNodeId);
				if (succceedingNode != null && GrafReader.isLeafNode(succceedingNode) && !GrafReader.isFloatingNode(succceedingNode)) {
					return succceedingNode;
				}
			}
			return null; // there's no succeeding node that is a non-floating
							// leaf node
		} else
			return null; // node has no ordered number
	}

}
