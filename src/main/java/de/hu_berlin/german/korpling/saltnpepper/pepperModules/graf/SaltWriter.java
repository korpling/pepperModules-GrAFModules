package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IAnchor;
import org.xces.graf.api.IAnnotation;
import org.xces.graf.api.IFeature;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.ILink;
import org.xces.graf.api.INode;
import org.xces.graf.api.IRegion;

import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSample.SaltSample;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltSample.SaltSampleException;

public class SaltWriter {
	
	/** A GrAF corpus may contain several primary text segmentations. For
	 *  example, MASC 3.0.0 has a segmentation called "f.s", which partions the
	 *  primary text into sentences, but it is not based on the primary text
	 *  segmentations used by "f.seg" (word tokenization). 
	 *  
	 *  ANNIS2 can't handle multiple primary text segmentations correctly, so
	 *  we have to work around this. */
	public enum IRegionHandlingMethod {
		WORD_SEGMENTATION_ONLY, APPROXIMATE_MATCH, VIRTUAL_TOKEN_LEVEL
	}
	
	/** A Salt dominance relation */
	public static STYPE_NAME domRel = STYPE_NAME.SDOMINANCE_RELATION;

	
//	/** creates a corpus graph and adds an {@link SDocument} for each document ID. 
//	 *  SDocuments will later be filled with the documents' primary text and its annotations.
//	 */
//	public static SCorpusGraph createCorpusStructure(SaltProject saltProject, 
//													 List<String> docIds,
//													 String corpusName) 
//	{
//		SCorpusGraph sCorpGraph = SaltFactory.eINSTANCE.createSCorpusGraph();
//		saltProject.getSCorpusGraphs().add(sCorpGraph);
//		
//		SCorpus corpus = SaltFactory.eINSTANCE.createSCorpus();
//		corpus.setSName(corpusName);
//		sCorpGraph.addSNode(corpus); // add corpus to corpus graph
//		
//		for (String docId: docIds) {
//			SDocument sDoc = SaltFactory.eINSTANCE.createSDocument();
//			sDoc.setSName(docId);
//			sCorpGraph.addSDocument(corpus, sDoc);
//		}		
//		return sCorpGraph;
//	}
	
	/** adds an annotation to an SNode or its subclasses, e.g. SToken or SSpan.
	 *  @param sAnnotationName - the name that this type of annotation shall 
	 *  	   have in SALT, e.g. 'POS' or 'Lemma'
	 *  @param sAnnotationId - the ID of the annotation node, e.g. 'penn-n23'
	 *  @param sAnnotationValue - the value / content of the annotation, e.g. 'NNP'
	 *  @param sAnnotationNamespace - the namespace of the annotation, e.g. 'xces' or 'fn'
	 *  @param sNode - the element to be annotated, e.g. SToken or SSpan */
	public static void addAnnotationToNode(String sAnnotationName,
										   String sAnnotationId,
										   String sAnnotationValue,
										   String sAnnotationNamespace,
										   SNode sNode) {
		SAnnotation sAnno = SaltFactory.eINSTANCE.createSAnnotation();
		sAnno.setSName(sAnnotationName);
		sAnno.setSValue(sAnnotationValue);
		sAnno.setSNS(sAnnotationNamespace);
		
		HashSet<String> existingAnnotationSNames = new HashSet<String>();	
		for (SAnnotation existingAnno : sNode.getSAnnotations()) {
			existingAnnotationSNames.add(existingAnno.getSName());
		}
		
		// don't add annotations that already exist
		if (!existingAnnotationSNames.contains(sAnnotationName)) {
			sNode.addSAnnotation(sAnno); 
		}
	}
	
	
	/**
	 * Creates a {@link STextualDS} object containing the primary text 
	 * {@link SaltSample#PRIMARY_TEXT} and adds the object to the {@link SDocumentGraph} 
	 * being contained by the given {@link SDocument} object.
	 * 
	 * @param sDocument the document, to which the created {@link STextualDS} 
	 * object will be added
	 */
	public static void addPrimaryTextToDocument(SDocument sDocument, String primaryText){
		if (sDocument== null)
			throw new SaltSampleException("Cannot create example, because the given sDocument is empty.");
		if (sDocument.getSDocumentGraph()== null)
			throw new SaltSampleException("Cannot create example, because the given sDocument does not contain an SDocumentGraph.");
		STextualDS sTextualDS = null;
		sTextualDS = SaltFactory.eINSTANCE.createSTextualDS();
		sTextualDS.setSText(primaryText);
		//adding the text to the document-graph
		sDocument.getSDocumentGraph().addSNode(sTextualDS);
	}
	
	
	/** adds an IRegion to an SDocument (in the form of an SToken). 
	 *  An IRegion is an annotation that works directly on the primary text, 
	 *  e.g. word segmentation or sentence boundaries. 
	 *  @return the ID of the created token*/
	public static String addIRegionToSDocument(IRegion iRegion, 
			 					 			 SDocument sDocument,
			 					 			 STextualDS sTextualDS,
			 					 			 HashMap<String, SLayer> annoTypeNameMap) {
		
		String iRegionId = iRegion.getId();
		List<IAnchor> anchors = iRegion.getAnchors();
		int startAnchor = Integer.parseInt( anchors.get(0).writeString() );
		int endAnchor = Integer.parseInt( anchors.get(1).writeString() );

		String annoType = GrafReader.convertElementIdToAnnotationType(iRegionId);
		SLayer regionLayer = annoTypeNameMap.get(annoType);
		
		return addTokenToDocument(startAnchor, endAnchor, sTextualDS, sDocument, regionLayer, iRegionId);	
	}


	/** add regions to a document. regions are annotations that work directly 
	 *  on the primary text, e.g. word segmentation and sentence boundaries. 
	 *	
	 *  FIXME: add method variant that adds ALL IRegions regardless of annotation type 
	 *  
	 *  @param annoTypes - only IRegions belonging to these annotation types will be added
	 *  @return a map with IRegion IDs as keys and SToken IDs as values*/
	public static HashMap<String,String> addIRegionsToSDocument(IGraph iDocumentGraph, 
											 SDocument sDocument, 
											 String[] annoTypes) throws GrafException {

		STextualDS sTextualDS = sDocument.getSDocumentGraph().getSTextualDSs().get(0);
		HashMap<String, SLayer> annoTypeSlayerMap = new HashMap<String, SLayer>();
		
		for (String annoType : annoTypes) {
			SLayer annoLayer = SaltFactory.eINSTANCE.createSLayer();
			annoLayer.setSName(annoType);
			annoTypeSlayerMap.put(annoType, annoLayer);
			sDocument.getSDocumentGraph().addSLayer(annoLayer);
		}
		
		HashMap<String, String> regionIdToTokenIdMap = new HashMap<String, String>();
		for (IRegion iRegion : GrafReader.getRegionsOfAnnoTypes(iDocumentGraph, annoTypes)) {
			String regionId = iRegion.getId();
			String tokenId = addIRegionToSDocument(iRegion, sDocument, sTextualDS, annoTypeSlayerMap);
			regionIdToTokenIdMap.put(regionId, tokenId);
		}
		return regionIdToTokenIdMap;
	}

	/** add ALL IRegions to an SDocument and returns a map from IRegion IDs
	 *  (e.g. 'seg-r316') to their corresponding STokenId
	 *  (e.g. 'salt:/MASC/MASC1-00046/MASC1-00046_graph#seg-r316').
	 *  
	 *  FIXME: implement handling of multiple tokenizations of the same primary
	 *  text when virtual token levels become available in ANNIS (cf. ANNIS3).
	 *  
	 *  @param iDocumentGraph - the IGraph that contains all the IRegions to be added
	 *  @param sDocument - the SDocument that the regions will be added to (as STokens)
	 *  @return a map from IRegion ID to SToken ID  */
	public static HashMap<String,String> addAllIRegionsToSDocument(IGraph iDocumentGraph, 
			 													   SDocument sDocument,
			 													   String regionHandlingMethod) 
			 													   throws GrafException {

		switch (IRegionHandlingMethod.valueOf(regionHandlingMethod)) 
		{
			case WORD_SEGMENTATION_ONLY:
				// FIXME: we only want to get the IRegions from "f.seg", but
				// due to a confirmed bug in graf1.2.0-snapshot, we'll at least
				// have to import one other annotation level. I chose "f.penn"
				// because it doesn't contain any IRegions, so we should be save.
				return addIRegionsToSDocument(iDocumentGraph, sDocument, new String[] {"f.seg", "f.penn"});
				
			case APPROXIMATE_MATCH:
//				STextualDS sTextualDS = sDocument.getSDocumentGraph().getSTextualDSs().get(0);
//				HashMap<String, SLayer> annoTypeSlayerMap = new HashMap<String, SLayer>();
//
//				HashSet<String> annotationTypes = GrafReader.getGraphAnnotationTypes(iDocumentGraph);
//				for (String annoType : annotationTypes) {
//					SLayer annoLayer = SaltFactory.eINSTANCE.createSLayer();
//					annoLayer.setSName(annoType);
//					annoTypeSlayerMap.put(annoType, annoLayer);
//					sDocument.getSDocumentGraph().addSLayer(annoLayer);
//				}
//
//				HashMap<String, String> regionIdToTokenIdMap = new HashMap<String, String>();
//				for (IRegion iRegion : iDocumentGraph.getRegions()) {
//					String regionId = iRegion.getId();
//					String tokenId = addIRegionToSDocument(iRegion, sDocument, sTextualDS, annoTypeSlayerMap);
//					regionIdToTokenIdMap.put(regionId, tokenId);
//				}
//				return regionIdToTokenIdMap;
				throw new UnsupportedOperationException("Not implemented yet.");
				
			case VIRTUAL_TOKEN_LEVEL: 
				throw new UnsupportedOperationException("Not implemented yet." 
						+ " Functionality should be supported in ANNIS3.");
		}
		return null;
	}
	
	

	/** takes a list of IRegions and returns the corresponding STokens*/
	public static List<SToken> mapRegionsToTokens(List<IRegion> regions, 
											   HashMap<String,String> regionToTokenMap,
											   SDocumentGraph docGraph) {
		List<SToken> tokenList = new ArrayList<SToken>();
		for (IRegion region : regions) {
			String regionId = region.getId();
			if (regionToTokenMap.containsKey(regionId)) {
				String tokenId = regionToTokenMap.get(regionId);
				SToken sToken = (SToken) docGraph.getSNode(tokenId);
				tokenList.add(sToken);
			}
			else {
				throw new NullPointerException("There's no SToken mapped to the IRegion " + regionId);
			}
		}
		return tokenList;		
	}
	
	

	
	/** Adds all SSpans to an SDocument.
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
	 *  	is the SToken/SSpan ID.
	 *  	
	 *  example from 20000424_nyt-NEW.*:
	 *  ne-2 node (sex: male) --> OutEdge to two INodes from "f.penn": penn-n6, penn-n7
	 *  		penn-n6 node (tony, NNP) --> link to IRegion seg-r11
	 *  		penn-n7 node (hall, NNP) --> link to IRegion seg-r13
	 */
	public static HashMap<String, Pair<String, String>> addSSpansToSDocument(IGraph iDocumentGraph, 
												 SDocument sDocument,
												 HashMap<String,String> regionIdsToTokenIdsMap) {

		SDocumentGraph sDocumentGraph = sDocument.getSDocumentGraph();
		HashMap<String, Pair<String, String>> iNodeIdsToSTokenSSpanIdsMap = new HashMap<String, Pair<String, String>>();
		
		Collection<INode> iNodes = iDocumentGraph.getNodes();	
		for (INode iNode : iNodes) {
			List<IRegion> iRegionsCoveredByINode = getIRegionsCoveredByINode(iNode, sDocumentGraph);
			if (iRegionsCoveredByINode.size() == 1) {
				String coveredIRegionId = iRegionsCoveredByINode.get(0).getId();
				if (regionIdsToTokenIdsMap.containsKey(coveredIRegionId)) {
					String coveredSTokenId = regionIdsToTokenIdsMap.get(coveredIRegionId);
					iNodeIdsToSTokenSSpanIdsMap.put(iNode.getId(), Pair.of("SToken", coveredSTokenId));
				}
			}
			else if (iRegionsCoveredByINode.size() > 1) {
			// IRegions are already added to the document, we just need to add
		    // SSpans for INodes that cover more than one IRegion
				List<SToken> tokens = SaltWriter.mapRegionsToTokens(iRegionsCoveredByINode,
														regionIdsToTokenIdsMap, 
														sDocumentGraph);
				List<SLayer> sLayers = SaltWriter.mapTokensToSLayers(tokens);
				String sSpanId = addSSpanToSDocument(tokens, sDocument, sLayers);
				iNodeIdsToSTokenSSpanIdsMap.put(iNode.getId(), Pair.of("SSpan", sSpanId));
			}
		}
		return iNodeIdsToSTokenSSpanIdsMap;		
	}

	/** returns a list of IRegions that an INode covers (via links or 
	 *  recursively via outbound IEdges that connect to other INodes (that link
	 *  to IRegions). */
	public static List<IRegion> getIRegionsCoveredByINode(INode iNode,
										   SDocumentGraph sDocumentGraph) {
		
		List<IRegion> iRegionsCoveredByINode = new ArrayList<IRegion>();
		
		List<INode> connectedINodes = GrafReader.getOutboundConnectedNodes(iNode);
		for (INode connectedINode : connectedINodes) {
			List<IRegion> regionsFromConnectedNode = getIRegionsCoveredByINode(connectedINode,
					sDocumentGraph);
			iRegionsCoveredByINode.addAll(regionsFromConnectedNode);
		}			

		List<ILink> links = iNode.getLinks();
		for (ILink link : links) {
			List<IRegion> regions = link.getRegions();
			iRegionsCoveredByINode.addAll(regions);
		}
		return iRegionsCoveredByINode;
	}
	


	/** adds an SSpan to an SDocument and returns the SSpan ID. */
	public static String addSSpanToSDocument(List<SToken> sTokens, 
											SDocument sDocument, 
											List<SLayer> sLayers) {
		EList<SToken> sTokensEList = new BasicEList<SToken>(sTokens);
		// createSSpan only accepts ELists, not Lists 
		SSpan sSpan = sDocument.getSDocumentGraph().createSSpan(sTokensEList);
		for (SLayer layer : sLayers) {
			layer.getSNodes().add(sSpan);
		}
		return sSpan.getId();
	}

	
	/** takes an IRegion and adds it as an SToken/SNode to the SDocumentGraph of an SDocument. 
	 *  @param start - token onset in the primary text
	 *  @param end - token offset 
	 *  @param STextualDS - contains the primary text
	 *  @param SLayer - the annotation layer that the token is added to (e.g. "f.seg")
	 *  @param regionId - the IRegion's id (e.g. 'seg-r91'), will be used to name the SToken 
	 *  
	 *  @return the ID of the created token */
	public static String addTokenToDocument(int start, int end, STextualDS sTextualDS, 
										  SDocument sDocument, SLayer layer, String regionId){
		SToken sToken = SaltFactory.eINSTANCE.createSToken();
		sToken.setSName(regionId);
		sDocument.getSDocumentGraph().addSNode(sToken);
		layer.getSNodes().add(sToken);
		STextualRelation sTextRel = SaltFactory.eINSTANCE.createSTextualRelation();
		sTextRel.setSToken(sToken);
		sTextRel.setSTextualDS(sTextualDS);
		sTextRel.setSStart(start);
		sTextRel.setSEnd(end);
		sDocument.getSDocumentGraph().addSRelation(sTextRel);
		return sToken.getSId();
	}		


	/** takes a list of STokens and returns a list of all the SLayers
	 *  that at least one of them occurs in. */
	public static List<SLayer> mapTokensToSLayers(List<SToken> tokens) {
		List<SLayer> sLayersList = new ArrayList<SLayer>();
		for (SToken token : tokens) {
			EList<SLayer> tokenLayers = token.getSLayers();
			sLayersList.addAll(tokenLayers);
		}
		return sLayersList;
	}

	
	/** adds all annotations to an SDocument. 
	 *  @param iGraph - the IGraph that contains all the annotations to be added
	 *  @param iNodeIDsToSTokenSSpanIdsMap - a map from an INode ID to the ID 
	 *  	of an SToken or SSpan which it annotates
	 *  @param sDocument - the Salt document to which the annotations shall be added
	 *  @return a Pair, where the key contains a token ID to SToken map and the
	 *  	value contains a span ID to SSpan map 
	 * @throws GrafException */
	public static Pair<HashMap<String,SToken>,HashMap<String,SSpan>> addAnnotationsToSDocument(IGraph iGraph, 
							HashMap<String,Pair<String,String>> iNodeIDsToSTokenSSpanIdsMap, 
							SDocument sDocument) throws GrafException {
		
		SDocumentGraph docGraph = sDocument.getSDocumentGraph();
		
		HashMap<String, SToken> tokenIdToTokenMap = new HashMap<String, SToken>();
		for (SToken token : docGraph.getSTokens()) {
			tokenIdToTokenMap.put(token.getId(), token);
		}
		
		HashMap<String, SSpan> spanIdToSpanMap = new HashMap<String, SSpan>();
		for (SSpan span : docGraph.getSSpans()) {
			spanIdToSpanMap.put(span.getId(), span);
		}
		
		for (String iNodeId : iNodeIDsToSTokenSSpanIdsMap.keySet()) {
			INode annotationINode = iGraph.findNode(iNodeId);
			Pair<String, String> mappedElementPair = iNodeIDsToSTokenSSpanIdsMap.get(iNodeId);
			
			if (mappedElementPair.getKey().equals("SToken")) {
				String sTokenId = mappedElementPair.getValue();
				SToken sToken = tokenIdToTokenMap.get(sTokenId);
//				System.out.println("DEBUG: add annotationINode '"+iNodeId+"' to token '"+sTokenId+"'.");
//				GrafGraphInfo.printNodeInfo(annotationINode, iGraph);
				addAnnotationsToSToken(annotationINode, sToken);
			}
			else if (mappedElementPair.getKey().equals("SSpan")) {
				String sSpanId = mappedElementPair.getValue();
				SSpan sSpan = spanIdToSpanMap.get(sSpanId);
//				System.out.println("DEBUG: add annotationINode '"+iNodeId+"' to span '"+sSpanId+"'.");
//				GrafGraphInfo.printNodeInfo(annotationINode, iGraph);
				addAnnotationsToSSpan(annotationINode, sSpan);
			}
			else {
				throw new NullPointerException("I can't map from an INode to an "
						+ mappedElementPair.getKey());
			}
		}
		return Pair.of(tokenIdToTokenMap, spanIdToSpanMap) ;
	}


	/** adds an annotation (e.g. POS tag or named entity type) to an SSpan
	 *  (which represents several STokens). */
	public static void addAnnotationsToSSpan(INode annotationINode, SSpan sSpan) {
		String annotationId = annotationINode.getId();
		IAnnotation iAnnotation = annotationINode.getAnnotation(); // returns default annotation
//		String annoNamespace = iAnnotation.getAnnotationSpace().getName();
		String annoLabel = iAnnotation.getLabel();
		Iterable<IFeature> annoFeatures = iAnnotation.getFeatures().features();

		for (IFeature feature : annoFeatures) {
				addAnnotationToNode(feature.getName(), 
									annotationId, 
									feature.getStringValue(), 
									annoLabel, 
									sSpan);
		}
	}


	/** adds an annotation (e.g. POS tag, named entity type) to an 
	 *  SToken */
	public static void addAnnotationsToSToken(INode annotationINode,
											  SToken sToken) {
		
		String annotationId = annotationINode.getId();
		IAnnotation iAnnotation = annotationINode.getAnnotation(); // returns default annotation
//		String annoNamespace = iAnnotation.getAnnotationSpace().getName();
		String annoLabel = iAnnotation.getLabel();
		Iterable<IFeature> annoFeatures = iAnnotation.getFeatures().features();

		for (IFeature feature : annoFeatures) {
				addAnnotationToNode(feature.getName(), 
									annotationId, 
									feature.getStringValue(), 
									annoLabel, 
									sToken);
		}
	}

//	public static void createSyntaxStructure(SDocument sDocument){

//		// there are two methods named "addSNode" for a SDocumentGraph
//		// the first one simply adds the single parameter of the type SNode to the SDocumentGraph
//		// the second one requires three arguments: two SNodes and a STYPE_NAME
//		// this method will create a SRelation between the two SNodes. The type of the SRelation is determined by
//		// the STYPE_NAME, but only four STYPE_NAMEs are allowed: SDOMINANCE_RELATION, SPOINTING_RELATION,
//		// SSPANNING_RELATION and STEXTUAL_RELATION. For the hierarchical structure that is intended to build, SDOMINANCE_RELATION is used.
//		// the first SNode (the source of the relation) is required to be contained in the SDocumentGraph already, so when building such
//		// a tree, the root node has to be added to the SDocumentGraph before establishing the relations between the other nodes.
//			
//		// creating a variable for the type of relation between the constituents (dominance relation)
//		// (this is just for convenience)
//		STYPE_NAME domRel = STYPE_NAME.SDOMINANCE_RELATION;
//
		// FIXME: ask Florian: what does the root node do in the doc graph??? does it only dominate the 1st SToken? 
//		// adding the root SNode to the SDocumentGraph
//		docGraph.addSNode(sq);				
//		
//		// adding the target nodes to the SDocumentGraph and creating SDominanceRelations between the respective nodes
//		// (addSNode returns the created SDominanceRelation, but it it not used here)
//		docGraph.addSNode(sq,    sTokens.get(0),  domRel); // "Is"
//		docGraph.addSNode(sq,    np1,             domRel);
//		docGraph.addSNode(np1,   sTokens.get(1),  domRel); // "this"
//		docGraph.addSNode(np1,   sTokens.get(2),  domRel); // "example"
//		docGraph.addSNode(sq,    adjp1,           domRel);
//		docGraph.addSNode(adjp1, adjp2,           domRel);
//		docGraph.addSNode(adjp2, sTokens.get(3),  domRel); // "more"
//		docGraph.addSNode(adjp2, sTokens.get(4),  domRel); // "complicated"				
//		docGraph.addSNode(adjp1, sbar,            domRel);
//		docGraph.addSNode(sbar,  sTokens.get(5),  domRel); // "than"
//		docGraph.addSNode(sbar,  s1,              domRel);				
//		docGraph.addSNode(s1,    np2,             domRel);				
//		docGraph.addSNode(np2,   sTokens.get(6),  domRel); // "it"
//		docGraph.addSNode(s1,    vp1,             domRel);
//		docGraph.addSNode(vp1,   sTokens.get(7),  domRel); // "appears"				
//		docGraph.addSNode(vp1,   s2,              domRel);				
//		docGraph.addSNode(s2,    vp2,             domRel);
//		docGraph.addSNode(vp2,   sTokens.get(8),  domRel); // "to"				
//		docGraph.addSNode(vp2,   vp3,             domRel);
//		docGraph.addSNode(vp3,   sTokens.get(9),  domRel); // "be"
//			
//		// creating a layer named "syntax" for the constituents of the tree
//		SLayer syntaxLayer = SaltFactory.eINSTANCE.createSLayer();
//		syntaxLayer.setSName("syntax");
//		docGraph.addSLayer(syntaxLayer);
//
//		// adding the constituents to the syntax layer
//		syntaxLayer.getSNodes().add(sq);
//		syntaxLayer.getSNodes().add(np1);
//		syntaxLayer.getSNodes().add(adjp1);				
//		syntaxLayer.getSNodes().add(adjp2);
//		syntaxLayer.getSNodes().add(sbar);				
//		syntaxLayer.getSNodes().add(s1);
//		syntaxLayer.getSNodes().add(np2);				
//		syntaxLayer.getSNodes().add(vp1);
//		syntaxLayer.getSNodes().add(s2);				
//		syntaxLayer.getSNodes().add(vp2);
//		syntaxLayer.getSNodes().add(vp3);				
//	}

	/** creates a new SStructure for each INode in an f.ptb IGraph, each of 
	 *  which represent a syntax node (S, NP etc.) 
	 *  
	 * @param syntaxIGraph - an IGraph that only contains the "f.ptb" 
	 *  	annotation type (or similar) */
	public static HashMap<String, SStructure> mapINodesToSStructures(IGraph syntaxIGraph) {
		// map INodes to SSTructures
		HashMap<String, SStructure> iNodeIdToSStructureMap = new HashMap<String, SStructure>();
		Collection<INode> syntaxINodes = syntaxIGraph.getNodes();
		for (INode syntaxINode: syntaxINodes) {
			if (!syntaxINode.getAnnotation().getLabel().equals("tok")) {
				// create an SStructure for each syntax node, i.e. nodes that 
				// are labeled with 'S', 'NP' etc. but don't create SStructures
				// for tokens.
				SStructure syntaxSStructure = SaltFactory.eINSTANCE.createSStructure();
				syntaxSStructure.setSName(syntaxINode.getId());
				iNodeIdToSStructureMap.put(syntaxINode.getId(), syntaxSStructure);
				
			}
		}
		return iNodeIdToSStructureMap;
	}

	/** adds dominance relations to the document graph (from the document graph
	 *  root node to each syntax tree root node) */
	public static void addSyntaxTreeRootDomRelsToDocGraph(IGraph syntaxIGraph, 
			SDocumentGraph docGraph, 
			HashMap<String, SStructure> iNodeIdToSStructureMap,
			SStructure rootSStructure) {
		for (INode treeRootINode : GrafReader.getSyntaxTreeRootsINodes(syntaxIGraph))
		{
			String treeRootNodeId = treeRootINode.getId();
			if (iNodeIdToSStructureMap.containsKey(treeRootNodeId)) {
				SStructure dominatedSStructure = iNodeIdToSStructureMap.get(treeRootNodeId);
				if (!docGraph.getNodes().contains(dominatedSStructure)) {
					docGraph.addSNode(dominatedSStructure);
				}
				
				docGraph.addSNode(rootSStructure, dominatedSStructure, domRel);
				System.out.println("added domrel from root to " + dominatedSStructure.getId());
			} 
			else {
				throw new NullPointerException("Can't add dominated SStructure"
					+"to SDocumentGraph. It's not in iNodeIdToSStructureMap.");
			}
		}		
	}
	
	/** add a dominance relation from each syntax node (all nodes in a "f.ptb"
	 *  IGraph that don't have the label "tok") to the nodes they dominate */
	public static void addSyntaxNodeDomRelsToDocGraph(IGraph syntaxIGraph, 
			SDocumentGraph docGraph, 
			HashMap<String, SStructure> iNodeIdToSStructureMap,
			HashMap<String, Pair<String, String>> iNodeIDsToSTokenSSpanIdsMap,
			Pair<HashMap<String, SToken>, HashMap<String, SSpan>> tokenAndSpanMaps) {
		
		HashMap<String, SToken> tokenIDToSTokenMap = tokenAndSpanMaps.getKey();
		HashMap<String, SSpan> spanIDToSSpanMap = tokenAndSpanMaps.getValue();
		
		for (INode syntaxINode : syntaxIGraph.getNodes()) {
			if (!"tok".equals(syntaxINode.getAnnotation().getLabel())) {
				SStructure sourceSStructure = iNodeIdToSStructureMap.get(syntaxINode.getId());
				
				if (!docGraph.getNodes().contains(sourceSStructure)) {
//					System.out.println("\tDEBUGGING: sourceSStructure ("+ sourceSStructure + ") with ID '" + sourceSStructure.getId() + "' to be added");
					docGraph.addSNode(sourceSStructure);
				}
				
				List<INode> connectedSyntaxINodes = GrafReader.getOutboundConnectedNodes(syntaxINode);
				for (INode connectedSyntaxINode : connectedSyntaxINodes) {
					String dominatedINodeId = connectedSyntaxINode.getId();
					if (iNodeIdToSStructureMap.containsKey(dominatedINodeId)) {
						SStructure dominatedSStructure = iNodeIdToSStructureMap.get(dominatedINodeId);
						docGraph.addSNode(sourceSStructure, dominatedSStructure, domRel);
					} 
					else {
						Pair<String, String> dominatedSElement = iNodeIDsToSTokenSSpanIdsMap.get(dominatedINodeId);
						String sElementId = dominatedSElement.getValue();
						if (tokenIDToSTokenMap.containsKey(sElementId)) {
							SToken dominatedSToken = tokenIDToSTokenMap.get(sElementId);							
							docGraph.addSNode(sourceSStructure, dominatedSToken, domRel);
						}
						else if (spanIDToSSpanMap.containsKey(sElementId)) {
							SSpan dominatedSSpan = spanIDToSSpanMap.get(sElementId);
							docGraph.addSNode(sourceSStructure, dominatedSSpan, domRel);
						}
						else {
							System.out.println("DEBUG: DAMN, can't find element '"+sElementId+"' in token map or span map!1!!");
						}
					}
				}	
			}
		}		
	}
	
	/** @param syntaxIGraph - an IGraph that only contains the "f.ptb" 
	 *  	annotation type (or similar) */
	public static void addSyntaxToSDocument(IGraph syntaxIGraph,
			HashMap<String, Pair<String, String>> iNodeIDsToSTokenSSpanIdsMap,
			Pair<HashMap<String, SToken>, HashMap<String, SSpan>> tokenAndSpanMaps,
			SDocument sDocument) {
		
		SDocumentGraph docGraph = sDocument.getSDocumentGraph();
		List<SToken> sTokens= Collections.synchronizedList(docGraph.getSTokens());

		HashMap<String, SStructure> iNodeIdToSStructureMap = mapINodesToSStructures(syntaxIGraph); 
		
		// FIXME: IGraph.getRoots() is broken, so we got to create/add our own root for now
		SStructure rootSStructure = SaltFactory.eINSTANCE.createSStructure();
		rootSStructure.setSName("root");
		docGraph.addSNode(rootSStructure);
		
		addSyntaxTreeRootDomRelsToDocGraph(syntaxIGraph, docGraph, iNodeIdToSStructureMap, rootSStructure);
		addSyntaxNodeDomRelsToDocGraph(syntaxIGraph, docGraph, iNodeIdToSStructureMap, iNodeIDsToSTokenSSpanIdsMap, tokenAndSpanMaps);
	}
	

//	/** saves a Salt project and reloads it for testing purposes 
//	 *  FIXME: add parameter for storage directory */
//	public static void saveSaltProject(SaltProject saltProject) 
//						 throws FileNotFoundException {
//		String pathString = Utils.getVariableFromYamlFile("saltXMLDir");
//		File path = new File(pathString);
//		if (!path.exists()) {
//			path.mkdirs();
//		}
//		System.out.print("store salt project in path: '"+path.getAbsolutePath()+"' ... ");
//		saltProject.saveSaltProject(URI.createFileURI(path.getAbsolutePath()));
//		System.out.println("OK");	
//	}
	
}
