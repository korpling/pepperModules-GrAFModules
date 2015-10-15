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
import java.util.List;

import org.corpus_tools.peppermodules.graf.GrAFImporter;
import org.corpus_tools.peppermodules.graf.GrafReader;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;

public class RootTest {

	/** test if IGraph.getRoot() anywhere returns results other than null */
	public static void main(String[] args) throws GrafException, SAXException, IOException {
		String corpusPath = System.getProperty("user.home").toString()+"/corpora/MASC-3.0.0/";
		File headerFile = new File(corpusPath, "resource-header.xml");
		ResourceHeader rscHeader = new ResourceHeader(headerFile);
		
		List<String> docHeaderPaths = GrAFImporter.recursiveListDir(corpusPath, "hdr");
		for (String docHeaderPath : docHeaderPaths) {
			IGraph graph = GrafReader.getAnnoGraph(rscHeader, docHeaderPath);
			if (graph.getRoot() != null) {
				System.out.println("document "+docHeaderPath+" has this root: "+graph.getRoot());
			}
			else {System.out.print("no root. ");}
		}
	}
}









