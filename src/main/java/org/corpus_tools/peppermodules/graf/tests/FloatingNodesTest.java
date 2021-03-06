/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
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
/**
 * 
 */
package org.corpus_tools.peppermodules.graf.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.corpus_tools.peppermodules.graf.DepthFirstSearch;
import org.corpus_tools.peppermodules.graf.GrAFImporter;
import org.corpus_tools.peppermodules.graf.GrafDocumentHeader;
import org.corpus_tools.peppermodules.graf.GrafGraphInfo;
import org.corpus_tools.peppermodules.graf.GrafReader;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.INode;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;

/**
 * @author arne
 *
 */
public class FloatingNodesTest {

	/** find all "f.ptb" annotated documents that don't have Trace nodes and other weird stuff
	 * @param args
	 * @throws GrafException 
	 * @throws XPathExpressionException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws XPathExpressionException, GrafException, SAXException, IOException {
		String corpusPath = System.getProperty("user.home").toString()+"/corpora/masc_one/";
		File headerFile = new File(corpusPath, "resource-header.xml");
		ResourceHeader rscHeader = new ResourceHeader(headerFile);

		List<String> docHeaderPaths = GrAFImporter.recursiveListDir(corpusPath, "hdr");
		List<String> desiredDocHeaderPaths = new ArrayList<String>();
		for (String docHeaderPath : docHeaderPaths) {
			GrafDocumentHeader docHeader = new GrafDocumentHeader(docHeaderPath);
			List<String> annoTypes = docHeader.getAnnotationTypes();
			if (annoTypes.contains("f.ptb")) {
//				System.out.println("doc with ptb: "+docHeaderPath);
				desiredDocHeaderPaths.add(docHeaderPath);
			}
		}

		for (String docHeaderPath : desiredDocHeaderPaths) {
			IGraph graph = GrafReader.getAnnoGraph(rscHeader, docHeaderPath);
			GrafGraphInfo.printAnnotationSpacesInfo(graph);
			
//			INode floatingNode = graph.findNode("fn-n16");
//			DepthFirstSearch floatSearch = new DepthFirstSearch(graph, floatingNode);
//			INode precedingLeafNode = floatSearch.getPrecedingLeafNode(graph, floatingNode);
//			String precedingLeafNodeId = precedingLeafNode.getId();
//			INode succeedingLeafNode = floatSearch.getSucceedingLeafNode(graph, floatingNode);
//			if (succeedingLeafNode != null) {
//				System.out.println("\t\tsucceeding leaf node: "+succeedingLeafNode.getId());
//			}
//			
//			
//			for (int nodeNumber : floatSearch.OrderedNodeNumberToNodeIdMap.keySet()) {
//				System.out.println("node number: "+nodeNumber+" --> nodeId: "
//								   + floatSearch.OrderedNodeNumberToNodeIdMap.get(nodeNumber));
//			}

			for (INode node : graph.getNodes()) {
				if (GrafReader.isFloatingNode(node)) {
					System.out.println("\n\nfloating node: "+node.getId());
					INode rootNode = GrafReader.getRootNodeFromNode(node, graph);
//					System.out.println("\troot node: "+rootNode.getId());
					DepthFirstSearch floatSearch = new DepthFirstSearch(graph, node);
					INode precedingLeafNode = floatSearch.getPrecedingLeafNode(graph, node);
					if (precedingLeafNode != null) {
						System.out.println("\t\tpreceding leaf node: "+floatSearch.getPrecedingLeafNode(graph, node).getId());						
					}
					else { System.out.println("\t\t there's no preceding leaf node!"); }
					INode succeedingLeafNode = floatSearch.getSucceedingLeafNode(graph, node);
					if (succeedingLeafNode != null) {
						System.out.println("\t\tsucceeding leaf node: "+floatSearch.getSucceedingLeafNode(graph, node).getId());
					}
					else {System.out.println("\t\t there's no succeeding leaf node!");}
				}
			}
		}

	}
	
//	/** maps a leaf INode to its SNode counterpart and links the SNode to a null token.*/
//	public static void addNullTokenToLeafNode(INode leafNode, IGraph iGraph, 
//			HashMap<String,String> iNodeIdToSNodeIdMap) throws GrafException {
//		Collection<String> branchingAncestorTokenNodeIds = GrafReader.getBranchingAncestorTokenNodeIds(leafNode);
//		String nextTokenNodeId = branchingAncestorTokenNodeIds.iterator().next();
//		INode nextTokenNode = iGraph.findNode(nextTokenNodeId);
//		int[] nextTokenNodeOffsets = GrafReader.getNodeOffsets(nextTokenNode);
//	}

}









