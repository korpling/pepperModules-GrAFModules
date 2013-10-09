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

public class RootTest {

	/** test if IGraph.getRoot() anywhere returns results other than null */
	public static void main(String[] args) throws GrafException, SAXException, IOException {
		String corpusPath = System.getProperty("user.home").toString()+"/corpora/MASC-3.0.0_20130930/";
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









