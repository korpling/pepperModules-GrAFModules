/**
 * 
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		String corpusPath = "/home/arne/korpora/MASC-3.0.0/";
		File headerFile = new File(corpusPath, "resource-header.xml");
		ResourceHeader rscHeader = new ResourceHeader(headerFile);

		List<String> docHeaderPaths = GrAFImporter.recursiveListDir(corpusPath, "hdr");
		List<String> desiredDocHeaderPaths = new ArrayList<String>();
		for (String docHeaderPath : docHeaderPaths) {
			GrafDocumentHeader docHeader = new GrafDocumentHeader(docHeaderPath);
			List<String> annoTypes = docHeader.getAnnotationTypes();
			if (annoTypes.contains("f.ptb")) {
				System.out.println("doc with ptb: "+docHeaderPath);
				desiredDocHeaderPaths.add(docHeaderPath);
			}
		}

		for (String docHeaderPath : desiredDocHeaderPaths) {
			IGraph graph = GrafReader.getAnnoGraph(rscHeader, docHeaderPath);
			List<String> docsWithoutLeaves = new ArrayList<String>();
			boolean graphHasLeaves = false;
			for (INode node : graph.getNodes()) {
				if (GrafReader.isLeafNode(node) == true) {
					String leafNodeId = node.getId();
					System.out.println("This leaf node: ");
					GrafGraphInfo.printNodeInfo(node, graph);
					System.out.println("is surrounded by these nodes:");
					List<IEdge> inEdges = node.getInEdges();
					for (IEdge inEdge : inEdges) {
						INode fromNode = inEdge.getFrom();
						List<IEdge> outEdges = fromNode.getOutEdges();
						for (IEdge outEdge : outEdges) {
							INode toNode = outEdge.getTo();
							if (!toNode.getId().equals(leafNodeId)) {
								GrafGraphInfo.printNodeInfo(toNode, graph);
							}
						}
					}
					System.out.println("======================================");
					graphHasLeaves = true;
					break;
				}
			}
			if (graphHasLeaves == false) {
				System.out.println("doc without leaves: "+docHeaderPath);
				docsWithoutLeaves.add(docHeaderPath);
			}
			else {
				System.err.println("doc with leaves: "+docHeaderPath);
			}
		}

//		docHeader.printDocumentHeaderInfo();
//		List<INode> roots = graph.getRoots();
//		System.out.println("IGraph has these roots:");
//		for (INode root : roots) {
//			System.out.println("\t"+root.getId());
//		}
//		System.out.println("IGraph annotation types: \n"+GrafReader.getGraphAnnotationTypes(graph));
//		IGraph ptbGraph = GrafReader.getAnnoGraph(rscHeader, docHeaderPath, "f.ptb");
//
//		for (INode node : graph.getNodes()) {
//			if (node.getLinks().size() > 0) {
//				GrafGraphInfo.printNodeInfo(node, graph);
//				List<ILink> links = node.getLinks();
//				for (ILink link : links) {
//					Iterable<IRegion> regions = link.regions();
//					for (IRegion region : regions) {
//						region.getStart();
//						region.getEnd();
//					}
//				}
//			}
//			if (GrafReader.isLeafNode(node)) {
//				GrafGraphInfo.printNodeInfo(node, graph);
//			}
		}
	}









