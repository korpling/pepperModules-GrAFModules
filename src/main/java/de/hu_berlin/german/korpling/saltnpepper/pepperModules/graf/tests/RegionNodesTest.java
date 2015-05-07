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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.tests;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.IRegion;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.GrAFImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.GrafReader;

/**
 * @author arne
 *
 */
public class RegionNodesTest {

	/** find all IRegions that are not annotated by any nodes
	 * @param args
	 * @throws GrafException 
	 * @throws XPathExpressionException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws XPathExpressionException, GrafException, SAXException, IOException {
//		String corpusPath = System.getProperty("user.home").toString()+"/corpora/MASC-3.0.0_sfb632_sync/";
		String corpusPath = System.getProperty("user.home").toString()+"/corpora/masc_nyt/";
		File headerFile = new File(corpusPath, "resource-header.xml");
		ResourceHeader rscHeader = new ResourceHeader(headerFile);

		List<String> docHeaderPaths = GrAFImporter.recursiveListDir(corpusPath, "hdr");
		for (String docHeaderPath : docHeaderPaths) {
			System.out.println("DEBUG document: "+docHeaderPath);
			IGraph graph = GrafReader.getAnnoGraph(rscHeader, docHeaderPath);
			IGraph fixedIGraph = GrAFImporter.repairFloatingNodes(graph);
			printRegionNodeStatistics(fixedIGraph);
		}
	}

	
	public static void printRegionNodeStatistics(IGraph graph) {
		int nonAnnotatedRegions = 0;
		int annotatedRegions = 0;
		for (IRegion region : graph.getRegions()) {
			if (region.getNodes().isEmpty()) {
				nonAnnotatedRegions += 1;
			}
			else {
				annotatedRegions += 1;
			}
		}
		System.out.println("IGraph has "+annotatedRegions+" annotated regions"
				+" and "+nonAnnotatedRegions+" non-annotated regions");
	}
	
}









