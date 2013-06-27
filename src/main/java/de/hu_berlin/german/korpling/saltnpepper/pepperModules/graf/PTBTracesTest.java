/**
 * 
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.xces.graf.api.GrafException;
import org.xces.graf.api.IAnnotation;
import org.xces.graf.api.IEdge;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.ILink;
import org.xces.graf.api.INode;
import org.xces.graf.api.IRegion;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.GrafDocumentHeader;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.GrafGraphInfo;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.GrafReader;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.SaltWriter;

/**
 * @author arne
 *
 */
public class PTBTracesTest {

	/** find all "f.ptb" annotated documents that don't have Trace nodes and other weird stuff
	 * @param args
	 * @throws GrafException 
	 * @throws XPathExpressionException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws XPathExpressionException, GrafException, SAXException, IOException {
		// TODO Auto-generated method stub
		String corpusPath = "/home/arne/corpora/masc_one/";
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









