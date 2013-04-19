/** This module shall convert the MASC corpus into SaltXML for further processing
 *  and import into ANNIS2.
 * 
 *  The MASC 3.0.0 corpus offers 14 different types of annotation.
 * 
 * 	6 types of annotation work directly with the primary text. They partition 
 *  the primary text into regions (string onsets and string offsets). 
 *  
 *  f.cb: 		committed belief
 *  f.event: 	events (e.g. humanitarian crisis, war ending)
 *  f.logical:	logical markup of the primary text (e.g. body, text, p) 
 *  f.mpqa: 	opinion (e.g. direct-subjective, attitude)
 *  f.s:		sentence boundaries
 *  f.seg: 		segmentation into tokens
 *  
 *  NOTE: "f.seg" is the only annotation type that doesn't use nodes, 
 *  but regions only.
 *  
 *  8 types of annotion work with these regions, i.e. they annotate segments 
 *  of the primary text and not the primary text itself.
 *  
 *  f.fn:		depends on: f.fntok		framenet frame elements
 *  f.fntok:	depends on: f.seg		framenet tokens with POS tags
 *  f.nc:		depends on: f.penn		noun chunks
 *  f.ne:		depends on: f.penn		named entities (e.g. person, location, org)
 *  f.penn:		depends on: f.seg		tokens with Penn POS tags
 *  f.ptb:		depends on: f.ptbtok	Penn Treebank syntax
 *  f.ptbtok:	depends on: f.seg		Penn Treebank tokens with POS tags
 *  f.vc:		depends on: f.penn		verb chunks
 *  
 *  */

package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

//import org.apache.commons.lang3.tuple.Pair;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.INode;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;

public class GrafToSaltConverter {
	
//	/** maps from an INode ID (e.g. 'ptb-n00108') to an SToken or SSpan,
//	 *  represented by its type and ID (e.g. <SToken,
//	 *  salt:/MASC/MASC1-00044/MASC1-00044_graph#seg-r101>) */
//	public static HashMap<String, Pair<String, String>> iNodeIDsToSTokenSSpanIdsMap;

	/** Describes the set of documents from the corpus to be processed.
	 *  'COMPLETE' refers to all the documents in the corpus,
	 *  'POS_TAGGED' refers to all documents in the corpus, that are at least
	 *  tokenized and POS tagged and
	 *  'TESTSET' refers to a small hand picked set of documents
	 *  
	 *  TODO: remove 'TESTSET' before release */
	public enum ProcessedDocumentSet {
		COMPLETE, POS_TAGGED, TESTSET
	}
	
//	/** returns a list of document header paths.
//	 *  @param documentSet a set of documents, of which the header paths
//	 *  	should be returned (COMPLETE, POS_TAGGED or TESTSET),
//	 *  	c.f. {@link ProcessedDocumentSet} */
//	public static List<String> createInputDocumentList(String documentSet) 
//				  throws FileNotFoundException, XPathExpressionException, GrafException {
//
//		String corpusPath = Utils.getVariableFromYamlFile("corpusPath");
//		ProcessedDocumentSet docSet = ProcessedDocumentSet.valueOf(documentSet);
//		switch(docSet) {
//			case COMPLETE: 
//				return GrafReader.getDocumentHeaderPaths(corpusPath);
//			case POS_TAGGED:
//				List<String> allDocHeaderPaths = GrafReader.getDocumentHeaderPaths(corpusPath);	
//				return getTokenizedPOSTaggedDocHeaders(allDocHeaderPaths);
//			case TESTSET:
//				return genDocHeaderPathsTestset(corpusPath);
//			default:
//				return GrafReader.getDocumentHeaderPaths(corpusPath);
//		}
//	}
		
		
	/** returns a tiny list of document header paths, which point to documents 
	 *  that are annotated with ALL 14 types of annotation present in MASC 3.0.0. */
	public static List<String> genDocHeaderPathsTestset(String corpusPath) 
				  throws FileNotFoundException {
		File corpusDirectory = new File(corpusPath);
		String[] relativeTestDocHeaderPaths = {
				"data/written/newspaper/nyt/20000424_nyt-NEW.hdr",
				"data/written/newspaper/nyt/20000410_nyt-NEW.hdr",
				"data/written/newspaper/nyt/20000415_apw_eng-NEW.hdr",
				"data/written/newspaper/nyt/20020731-nyt.hdr",
				"data/written/newspaper/wsj/wsj_0027.hdr"};

		List<String> desiredDocHeaderPaths = new ArrayList<String>();
		for (String filePath : relativeTestDocHeaderPaths) {
			File docHeaderFile = new File(corpusDirectory, filePath);
			desiredDocHeaderPaths.add(docHeaderFile.getAbsolutePath());
		}
		return desiredDocHeaderPaths;
	}

	/** returns a list of document header file paths, which belong to documents
	 *  that are segmented and POS tagged. */
	public static List<String> getTokenizedPOSTaggedDocHeaders(List<String> documentHeaderPaths) 
				  throws FileNotFoundException, GrafException {
		List<String> desiredAnnotations = Arrays.asList(new String[] {"f.seg", "f.penn"});
		
		List<String> desiredDocHeadersPaths = new ArrayList<String>();
		for (String docHeaderPath : documentHeaderPaths) {
			GrafDocumentHeader docHeader = new GrafDocumentHeader(docHeaderPath);
			List<String> annoTypes = docHeader.getAnnotationTypes();
			if (annoTypes.containsAll(desiredAnnotations)) {
				desiredDocHeadersPaths.add(docHeaderPath);
			}
		}
		return desiredDocHeadersPaths;
	}	
	
	/** maps document IDs to MascDocumentHeader objects*/
	public static HashMap<String, String> createDocIdDocHeaderMap(List<String> documentHeaderPaths) 
				  throws FileNotFoundException, XPathExpressionException {
		HashMap<String, String> docIdDocHeader = new HashMap<String, String>();
		for (String headerPath : documentHeaderPaths) {
			GrafDocumentHeader mascDocHeader = new GrafDocumentHeader(headerPath);
			String docId = mascDocHeader.getDocumentId();
			docIdDocHeader.put(docId, headerPath);
		}
		return docIdDocHeader;
	}
	
//	/** create a map with string onset/offset as keys and the corresponding INodes as values. */
//	public static HashMap<Pair<Integer, Integer>, INode> createINodeOffsetsMap(Collection<INode> iNodes) throws GrafException {
//		HashMap<Pair<Integer, Integer>, INode> iNodeOffsetsMap = new HashMap<Pair<Integer, Integer>, INode>();
//		
//		for (INode node : iNodes) {
//			int[] nodeOffsetsArray = GrafReader.getNodeOffsets(node);
//			Pair<Integer, Integer> nodeOffsetsPair = Pair.of(nodeOffsetsArray[0], nodeOffsetsArray[1]);
//			iNodeOffsetsMap.put(nodeOffsetsPair, node);
//		}
//		return iNodeOffsetsMap;
//	}
	
	/** reads IRegions from an IGraph, maps them to STokens and saves them in
	 *  an SDocument. The (IRegion -> SToken) mapping is used to generate a
	 *  mapping from INodes to STokens (if the INode only covers one IRegion)
	 *  or a mapping from INodes to SSpans (if the INode covers more than one
	 *  IRegion).
	 *  
	 *  Returns a map from INode IDs (e.g. 'ptb-n00409') to SSpans and STokens,
	 *  encoded as Pair<String "SToken", String STokenID>
	 *  (e.g. <"SToken", "salt:/MASC/MASC1-00046/MASC1-00046_graph#seg-r361">)
	 *  or Pair<"SSpan", SSpanID>
	 *  (e.g. <"SSpan", "salt:/MASC/MASC1-00046/MASC1-00046_graph#sSpan340>").
	 *  
	 *  An SSpan represents a number of consecutive STokens. In GrAF terminology
	 *  an SSpan is equivalent to an INode that links to more than one IRegion 
	 *  or an INode that is connected via one or more outgoing edges to INodes 
	 *  that do so.
	 *  
	 *  @return a map from INode IDs to a Pair<String, String>, where the left 
	 *  	string is the class name ("SToken" or "SSpan") and the right string
	 *  	is the SToken/SSpan ID. */
//	public static HashMap<String, Pair<String, String>> addGrafStructureToSDocument(IGraph iGraph, 
//																					SDocument sDocument)
//																					throws GrafException {
//		HashMap<String, String> regionIdsToTokenIdsMap; 
//		regionIdsToTokenIdsMap = SaltWriter.addAllIRegionsToSDocument(iGraph, 
//													sDocument, 
//													"WORD_SEGMENTATION_ONLY");
//		return SaltWriter.addSSpansToSDocument(iGraph, sDocument, regionIdsToTokenIdsMap);
//	}
	
	/** tries to convert document annotation IGraph objects (MASC/GrAF) into 
	 *  equivalent SDocument/SDocumentGraph objects (Annis/SALT)
	 *  
	 *  steps:
	 *  #1 takes a few MASC documents, creates corresponding annotation IGraph
	 *  #2 creates a SaltProject, SCorpusGraph 
	 *  #3 add an SDocument/SDocumentGraph for each of the MASC documents 
	 *  #4 add the primary text from the MASC documents to the corresponding SDocuments 
	 *  #5 retrieve the annotation types from the MASC document header 
	 *  #6 try to add all IRegions of a MASC document to an SDocument 
	 *  
	 *  TODO: IGraph just contains one Collection<IRegion>, not separated 
	 *  by annotation types. check, if it would make sense to generate separate
	 *  IGraphs for each annotation type. */
//	public static void main(String[] args) 
//					   throws XPathExpressionException, GrafException, 
//					   	      SAXException, IOException {
//
//		List<String> docHeaderPaths = createInputDocumentList("TESTSET"); // processes just 4 documents
////		List<String> docHeaderPaths = createInputDocumentList("COMPLETE");
//		List<String> docIds = GrafReader.getDocumentIds(docHeaderPaths);
//		HashMap<String, String> docIdDocHeaderMap = createDocIdDocHeaderMap(docHeaderPaths);
//		
//		SaltProject saltProject = SaltFactory.eINSTANCE.createSaltProject(); // new Salt project
//		// generate a corpus (incl. subcorpora) and add documents to them.
//		// right now these documents only contain an SName string
//		SCorpusGraph sCorpusGraph = SaltWriter.createCorpusStructure(saltProject, docIds, "MASC_labels_not_namespaces");	
//		
////		String corpusPath = Utils.getVariableFromYamlFile("corpusPath");
////		GrafResourceHeader rscHeader = new GrafResourceHeader(corpusPath);
//		
//		for (SDocument sDocument : sCorpusGraph.getSDocuments()) {
//			try {
//				// add new document graph to SDocument
//				sDocument.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
//
//				String sDocName = sDocument.getSName();
//				String docHeaderPath = docIdDocHeaderMap.get(sDocName);
////				System.out.println("Building document " + sDocName 
////						+ " graph from file " + docHeaderPath);
//
//				IGraph iGraph = GrafReader.getAnnoGraph(rscHeader, docHeaderPath);
////				GrafGraphInfo.printNodesStatistics(iGraph);
////				GrafGraphInfo.printRegionsStatistics(iGraph);
////				Collection<INode> nodes = iGraph.getNodes();
////				System.out.println("doc has " + nodes.size() + " nodes");
//
//				String primaryText = GrafReader.getDocumentText(iGraph);
//				SaltWriter.addPrimaryTextToDocument(sDocument, primaryText);
//
//				iNodeIDsToSTokenSSpanIdsMap = addGrafStructureToSDocument(iGraph, sDocument);
//				
//				// add all annotations from the IGraph as feature value pairs to SDocument
//				Pair<HashMap<String, SToken>, HashMap<String, SSpan>> tokenAndSpanMaps; 
//				tokenAndSpanMaps = SaltWriter.addAnnotationsToSDocument(iGraph, 
//															iNodeIDsToSTokenSSpanIdsMap, 
//															sDocument);
//				
//				// adds syntax structures from the IGraph's syntax annotation
//				// level to SDocument
//				IGraph syntaxIGraph = GrafReader.getAnnoGraph(rscHeader, docHeaderPath, "f.ptb");
//				GrafGraphInfo.printSyntaxTreeRoots(syntaxIGraph);
////				HashMap<String, SToken> tokenIDToSTokenMap = tokenAndSpanMaps.getKey();
//				
////				SaltWriter.addSyntaxToSDocument(syntaxIGraph, 
////										iNodeIDsToSTokenSSpanIdsMap, 
////										tokenAndSpanMaps, 
////										sDocument);
//				SaltWriter.addSyntaxToSDocument(syntaxIGraph, 
//										iNodeIDsToSTokenSSpanIdsMap, 
//										tokenAndSpanMaps, 
//										sDocument);
//				
//			}			
//			catch (Exception e) {
//				Utils.writeStackTraceToFile(e, "exceptions.log");
//				e.printStackTrace();
//			}
//		}
//		
//		SaltWriter.saveSaltProject(saltProject);
//		
//	}

}
