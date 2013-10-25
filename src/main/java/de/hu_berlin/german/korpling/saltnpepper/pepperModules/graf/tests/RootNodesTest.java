/**
 * 
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.tests;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.INode;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.GrafReader;

public class RootNodesTest {

	/** test code for Bug #159: NullPointerException in GrafReader.getRootNodeFromNode */
	public static void main(String[] args) throws GrafException, SAXException, IOException {
		String corpusPath = System.getProperty("user.home").toString()+"/corpora/MASC-3.0.0_20130930/";
		File headerFile = new File(corpusPath, "resource-header.xml");
		ResourceHeader rscHeader = new ResourceHeader(headerFile);

			IGraph graph = GrafReader.getAnnoGraph(rscHeader, corpusPath+"data/spoken/face-to-face/NapierDianne.hdr");
			Collection<INode> nodes = graph.getNodes();
			for (INode node : nodes) {
				if ("ptb-n00140".equals(node.getId())) {
					INode weirdNode = node;
					if (weirdNode.getInEdges().isEmpty()) {
						// excerpt from NapierDianne-ptb.xml:
						// <edge xml:id="ptb-e00143" from="ptb-n00107" to="ptb-n00140"/>
						System.out.println("graf-api claims this node has no ingoing edges!");
					}
					break;
				}
			}
	}
}









