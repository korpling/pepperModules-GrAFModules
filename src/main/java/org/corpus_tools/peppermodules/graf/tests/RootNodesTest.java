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
import java.util.Collection;

import org.corpus_tools.peppermodules.graf.GrafReader;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.INode;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;

public class RootNodesTest {

	/** test code for Bug #159: NullPointerException in GrafReader.getRootNodeFromNode */
	public static void main(String[] args) throws GrafException, SAXException, IOException {
		String corpusPath = System.getProperty("user.home").toString()+"/corpora/MASC-3.0.0/";
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









