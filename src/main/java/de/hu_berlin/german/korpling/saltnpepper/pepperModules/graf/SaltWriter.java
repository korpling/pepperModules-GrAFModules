package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import static java.util.Arrays.asList;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IAnchor;
import org.xces.graf.api.IAnnotation;
import org.xces.graf.api.IAnnotationSpace;
import org.xces.graf.api.IFeature;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.ILink;
import org.xces.graf.api.INode;
import org.xces.graf.api.IRegion;

import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
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
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.exceptions.GrAFImporterException;


public class SaltWriter {
	
	/** A GrAF corpus may contain several primary text segmentations. For
	 *  example, MASC 3.0.0 has a segmentation called "f.s", which partions the
	 *  primary text into sentences, but it is not based on the primary text
	 *  segmentations used by "f.seg" (word tokenization). 
	 *  
	 *  ANNIS2 can't handle multiple primary text segmentations correctly, so
	 *  we have to work around this. */
	public enum IRegionHandlingMethod {
		WORD_SEGMENTATION_ONLY, APPROXIMATE_MATCH, ALL_TOKEN_LEVELS
	}
	
	/** A Salt dominance relation */
	public static STYPE_NAME domRel = STYPE_NAME.SDOMINANCE_RELATION;
	
	private static int floatingNodeCount;
	
	private static HashMap<String, SLayer> annoSpaceSLayerMap;
	
	/** low level method to add an annotation to an SNode (or SToken/SSpan).
	 *  @param sAnnotationName - the name that this type of annotation shall 
	 *  	   have in SALT, e.g. 'POS' or 'Lemma'
	 *  @param sAnnotationId - the ID of the annotation node, e.g. 'penn-n23'
	 *  @param sAnnotationValue - the value / content of the annotation, e.g. 'NNP'
	 *  @param sAnnotationNamespace - the namespace of the annotation, e.g. 'xces' or 'fn'
	 *  @param sNode - the element to be annotated, e.g. an SNode 
	 *  	(or SToken/SSpan) instance*/
	public static void addAnnotationToNode(String sAnnotationName,
										   String sAnnotationId,
										   String sAnnotationValue,
										   String sAnnotationNamespace,
										   SNode sNode) {
		HashSet<String> existingAnnotationSNames = new HashSet<String>();	
		for (SAnnotation existingAnno : sNode.getSAnnotations()) {
			existingAnnotationSNames.add(existingAnno.getSName());
		}
		
		// only add annotations that don't exist yet
		if (!existingAnnotationSNames.contains(sAnnotationName)) {
			SAnnotation sAnno = SaltFactory.eINSTANCE.createSAnnotation();
			sAnno.setSName(sAnnotationName);
			sAnno.setSValue(sAnnotationValue);
			sAnno.setSNS(sAnnotationNamespace);
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
			throw new GrAFImporterException("Cannot create example, because the given sDocument is empty.");
		if (sDocument.getSDocumentGraph()== null)
			throw new GrAFImporterException("Cannot create example, because the given sDocument does not contain an SDocumentGraph.");
		STextualDS sTextualDS = null;
		sTextualDS = SaltFactory.eINSTANCE.createSTextualDS();
		sTextualDS.setSText(primaryText);
		//adding the text to the document-graph
		sDocument.getSDocumentGraph().addSNode(sTextualDS);
	}
	
	
	/** adds an IRegion to an SDocument (in the form of one or several STokens). 
	 *  An IRegion represents a segment of the primary text (e.g. a token),
	 *  which can be annotated by zero or more INodes (each of them belonging
	 *  to a different annotation space). For each of the annotation spaces an
	 *  IRegion belongs to, this method will create a new SToken.
	 *  @return the list of IDs of the created STokens*/
	public static List<String> addIRegionToSDocument(IRegion iRegion, 
			 					 			 SDocument sDocument,
			 					 			 HashMap<String, SLayer> annoSpaceSLayerMap) {
		
		String iRegionId = iRegion.getId();
		List<IAnchor> anchors = iRegion.getAnchors();
		int startAnchor = Integer.parseInt( anchors.get(0).writeString() );
		int endAnchor = Integer.parseInt( anchors.get(1).writeString() );

		List<String> sTokenIds = new ArrayList<String>();
		for (INode annoNode : iRegion.getNodes()) {
			String annoSpaceName = annoNode.getAnnotation().getAnnotationSpace().getName();
			SLayer regionLayer = annoSpaceSLayerMap.get(annoSpaceName);
			String sTokenId = addTokenToDocument(startAnchor, endAnchor, sDocument, regionLayer, iRegionId);
			sTokenIds.add(sTokenId);
		}
				
		return sTokenIds; 	
	}


	/** add ALL IRegions to an SDocument and returns a map from IRegion IDs
	 *  (e.g. 'seg-r316') to their corresponding STokenId(s)
	 *  (e.g. 'salt:/MASC/MASC1-00046/MASC1-00046_graph#seg-r316').
	 *  
	 *  @param iDocumentGraph - the IGraph that contains all the IRegions to be added
	 *  @param sDocument - the SDocument that the regions will be added to (as STokens)
	 *  @return a map from IRegion ID to a list of SToken IDs  */
	public static HashMap<String,List<String>> addAllIRegionsToSDocument(IGraph iDocumentGraph, 
			 													   SDocument sDocument) 
			 													   throws GrafException {
		
		// create a new SLayer for each IAnnotationSpace in an IGraph and add
		// it to the SDocument. create a Map (annotation space name --> SLayer).
		annoSpaceSLayerMap = new HashMap<String, SLayer>();
		for (IAnnotationSpace annoSpace : iDocumentGraph.getAnnotationSpaces()) {
			String annoSpaceName = annoSpace.getName();
			SLayer annoLayer = SaltFactory.eINSTANCE.createSLayer();
			annoLayer.setSName(annoSpaceName);
			annoSpaceSLayerMap.put(annoSpaceName, annoLayer);
			sDocument.getSDocumentGraph().addSLayer(annoLayer);
		}
		
		// add all IRegions from an IGraph to an SDocument. create a map 
		// (IRegion ID --> list of SToken IDs)
		HashMap<String, List<String>> regionIdToTokenIdsMap = new HashMap<String, List<String>>();
		for (IRegion iRegion : iDocumentGraph.getRegions()) {
			String regionId = iRegion.getId();
			List<String> tokenIds = addIRegionToSDocument(iRegion, sDocument, annoSpaceSLayerMap);
			regionIdToTokenIdsMap.put(regionId, tokenIds);				
		}		
		return regionIdToTokenIdsMap;
	}


	/** takes a list of IRegions and returns the corresponding STokens*/
	public static List<SToken> mapRegionsToTokens(List<IRegion> regions, 
											   HashMap<String,List<String>> regionIdToTokenIdsMap,
											   SDocumentGraph docGraph) {
		List<SToken> tokenList = new ArrayList<SToken>();
		for (IRegion region : regions) {
			String regionId = region.getId();
			if (regionIdToTokenIdsMap.containsKey(regionId)) {
				List<String> tokenIds = regionIdToTokenIdsMap.get(regionId);
				for (String tokenId : tokenIds) {
					SToken sToken = (SToken) docGraph.getSNode(tokenId);
					tokenList.add(sToken);					
				}
			}
			else {
				throw new NullPointerException("There's no SToken mapped to the IRegion " + regionId);
			}
		}
		return tokenList;		
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

	
	/** Adds all SSpans to an SDocument.
	 *  Returns a map from INode IDs (e.g. 'ptb-n00409') to SNode (SSpan/SToken) IDs,
	 *  e.g. "salt:/MASC/MASC1-00046/MASC1-00046_graph#seg-r361"
	 *  or "salt:/MASC/MASC1-00046/MASC1-00046_graph#sSpan340>".
	 *  
	 *  An SSpan represents a number of consecutive STokens. In GrAF terminology
	 *  an SSpan is equivalent to an INode that links to more than one IRegion 
	 *  or an INode that is connected via one or more outgoing edges to INodes 
	 *  that do so.
	 *  
	 *  @return a map from an INode ID to an SNode ID (or SToken/SSpan ID),
	 *  @throws GrafException 
	 */
	public static HashMap<String, List<String>> addSSpansToSDocument(IGraph iDocumentGraph, 
												 SDocument sDocument,
												 HashMap<String,List<String>> regionIdsToTokenIdsMap) throws GrafException {

		SDocumentGraph sDocumentGraph = sDocument.getSDocumentGraph();
		HashMap<String, List<String>> iNodeIdsToSNodeIdsMap = new HashMap<String, List<String>>();
		
		Collection<INode> iNodes = iDocumentGraph.getNodes();	
		for (INode iNode : iNodes) {
			List<IRegion> iRegionsCoveredByINode = getIRegionsCoveredByINode(iNode, sDocumentGraph);
			if (iRegionsCoveredByINode.size() == 0) {
				// in GrAF, it is allowed to have nodes that have neither
				// outgoing edges nor links to regions of primary text!
				System.out.println("DEBUG addSSpansToSDocument:");
		
				// the INode doesn't cover any primary text, therefore we have
				// to find the floating nodes it covers and add fake tokens to the document
				List<INode> floatingNodes = GrafReader.findOutboundConnectedFloatingNodes(iNode);
				System.out.println("\tit covers these floating nodes:");
				for (INode floatingNode : floatingNodes) {
					int[] offsets = GrafReader.getFloatingNodeOffsets(iDocumentGraph, floatingNode);
					String regionId = "floating-node-"+String.valueOf(floatingNodeCount);
					STextualDS sTextualDS = sDocument.getSDocumentGraph().getSTextualDSs().get(0);
					String annoSpaceName = floatingNode.getAnnotation().getAnnotationSpace().getName();
					SLayer regionLayer = annoSpaceSLayerMap.get(annoSpaceName);
					
					// FIXME: don't try to add the same fake token twice (can happen b/c of iRegionsCoveredByINode.size() == 0)
					addTokenToDocument(offsets[0], offsets[1], sTextualDS, sDocument, regionLayer, regionId);
					floatingNodeCount++;
				}
			}
			else if (iRegionsCoveredByINode.size() == 1) {
				String coveredIRegionId = iRegionsCoveredByINode.get(0).getId();
				if (regionIdsToTokenIdsMap.containsKey(coveredIRegionId)) {
					List<String> coveredSTokenIds = regionIdsToTokenIdsMap.get(coveredIRegionId);
					iNodeIdsToSNodeIdsMap.put(iNode.getId(), coveredSTokenIds);
				}
				else {
					throw new GrAFImporterException("IRegion "+coveredIRegionId+" can't be found in regionIdsToTokenIdsMap.");
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
				iNodeIdsToSNodeIdsMap.put(iNode.getId(), asList(sSpanId)); 
				// using a list here to make the map usable for both SSpanIDs as well as STokenIDs
			}
		}
		return iNodeIdsToSNodeIdsMap;
	}

	/** creates a map from INodes to the IRegions they link to
	 *  @return a map from INode ID to a list of IRegion IDs */
	public static HashMap<String, List<String>> getINodeIdToIRegionIdsMap(IGraph iDocumentGraph,
													SDocumentGraph sDocumentGraph) {
		HashMap<String, List<String>> iNodeIdToIRegionIdsMap = new HashMap<String, List<String>>();
		for (INode iNode : iDocumentGraph.getNodes()) {
			List<IRegion> iRegionsCoveredByINode = getIRegionsCoveredByINode(iNode, sDocumentGraph);
			List<String> iRegionIds = new ArrayList<String>();
			for (IRegion iRegion : iRegionsCoveredByINode) {
				iRegionIds.add(iRegion.getId());
			}
			iNodeIdToIRegionIdsMap.put(iNode.getId(), iRegionIds);
		}
		return iNodeIdToIRegionIdsMap;
	}
	
	/** creates a map from IRegions to the INodes that link to them
	 *  @return a map from IRegion ID to a list of INode IDs*/
	public static HashMap<String, List<String>> getIRegionIdToINodeIdsMap(IGraph iDocumentGraph) {
		HashMap<String, List<String>> iRegionToINodesMap = new HashMap<String, List<String>>();
		Collection<IRegion> regions = iDocumentGraph.getRegions();
		for (IRegion region : regions) {
			List<INode> nodes = region.getNodes();
			List<String> nodeIds = new ArrayList<String>();
			for (INode node : nodes) {
				nodeIds.add(node.getId());
			}
			iRegionToINodesMap.put(region.getId(), nodeIds);
		}
		return iRegionToINodesMap;
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
	
	
	/** creates an SToken and adds it to the SDocumentGraph of an SDocument.
	 *  the parameters needed are usually retrieved from an IRegion.
	 *
	 *  @param onset - token onset in the primary text
	 *  @param offset - token offset
	 *  @param STextualDS - contains the primary text
	 *  @param SLayer - the annotation layer that the token is added to (e.g. "f.seg")
	 *  @param regionId - the IRegion's id (e.g. 'seg-r91'), will be used to name the SToken 
	 *  
	 *  @return the ID of the created token */
	public static String addTokenToDocument(int onset, int offset,
			SDocument sDocument, SLayer layer, String regionId) {
		STextualDS sTextualDS = sDocument.getSDocumentGraph().getSTextualDSs().get(0);
		SToken sToken = SaltFactory.eINSTANCE.createSToken();
		sToken.setSName(regionId);
		sDocument.getSDocumentGraph().addSNode(sToken);
		layer.getSNodes().add(sToken);
		STextualRelation sTextRel = SaltFactory.eINSTANCE.createSTextualRelation();
		sTextRel.setSToken(sToken);
		sTextRel.setSTextualDS(sTextualDS);
		sTextRel.setSStart(onset);
		sTextRel.setSEnd(offset);
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
	 *  @param iNodeIdToSNodeIdMap - a map from an INode ID to the ID 
	 *  	of an SNode (or SToken/SSpan) which it annotates
	 *  @param sDocument - the Salt document to which the annotations shall be added
	 *  @return a map from SNode (or SToken/SSpan) IDs to SNodes (or STokens/SSpans) 
	 * @throws GrafException */
	public static HashMap<String,SNode> addAnnotationsToSDocument(IGraph iGraph, 
							HashMap<String,List<String>> iNodeIdToSNodeIdMap, 
							SDocument sDocument) throws GrafException {
		
		SDocumentGraph docGraph = sDocument.getSDocumentGraph();
		
		HashMap<String, SNode> nodeIdToNodeMap = new HashMap<String, SNode>();
		for (SNode sNode : docGraph.getSTokens()) {
			nodeIdToNodeMap.put(sNode.getId(), sNode);
		}
		for (SNode sNode : docGraph.getSSpans()) {
			nodeIdToNodeMap.put(sNode.getId(), sNode);
		}
		
		for (String iNodeId : iNodeIdToSNodeIdMap.keySet()) {
			INode annotationINode = iGraph.findNode(iNodeId);
			List<String> sNodeIds = iNodeIdToSNodeIdMap.get(iNodeId);
			for (String sNodeId : sNodeIds) {
				SNode sNode = nodeIdToNodeMap.get(sNodeId);
				addAnnotationsToSNode(annotationINode, sNode);				
			}
		}
		return nodeIdToNodeMap;
	}

	
	/** reads all annotations from an INode and adds them to the given 
	 *  SNode (or SToken/SSpan).*/
	public static void addAnnotationsToSNode(INode annotationINode, SNode sNode) {
		String annotationId = annotationINode.getId();
		IAnnotation iAnnotation = annotationINode.getAnnotation(); // returns default annotation
		String annoNamespace = iAnnotation.getAnnotationSpace().getName();
		Iterable<IFeature> annoFeatures = iAnnotation.getFeatures().features();
		for (IFeature feature : annoFeatures) {
				addAnnotationToNode(feature.getName(), 
									annotationId, 
									feature.getStringValue(), 
									annoNamespace, 
									sNode);
		}
	}	


	/** Creates a new SStructure for each INode in an f.ptb IGraph. Each
	 *  SStructure represents a syntax node (S, NP etc.) and is annotated accordingly.
	 *  Returns a map from INode IDs to the corresponding SStructure.
	 *  
	 * 	@param syntaxIGraph - an IGraph that only contains the "f.ptb" 
	 *  	annotation type (or similar) 
	 * 	@return iNodeIdToSStructureMap - a map from INode IDs to the SStructures
	 * 		they represent*/
	public static HashMap<String, SStructure> createSyntaxINodeSStructures(IGraph syntaxIGraph) {
		// map INodes to SSTructures
		HashMap<String, SStructure> iNodeIdToSStructureMap = new HashMap<String, SStructure>();
		Collection<INode> syntaxINodes = syntaxIGraph.getNodes();
		for (INode syntaxINode: syntaxINodes) {
			if (syntaxINode.getOutEdges().size() > 0) {
				// create an SStructure for each syntax node, i.e. nodes that 
				// are labeled with 'S', 'NP' etc. but don't create SStructures
				// for leaf nodes (here: tokens).
				SStructure syntaxSStructure = SaltFactory.eINSTANCE.createSStructure();
				syntaxSStructure.setSName(syntaxINode.getId());
				addAnnotationsToSNode(syntaxINode, syntaxSStructure);
					
//				syntaxSStructure.createSAnnotation(arg0, arg1, arg2)
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
		for (INode treeRootINode : GrafReader.getRootNodes(syntaxIGraph))
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
	 *  IGraph that have outgoing edges) to the nodes they dominate.
	 *  
	 *  NOTE: token ("tok") nodes don't have outgoing edges but links to regions to 
	 *  primary text segments (aka string onsets/offsets) 
	 * @throws GrafException */
	public static void addSyntaxNodeDomRelsToDocGraph(IGraph syntaxIGraph, 
			SDocumentGraph docGraph, 
			HashMap<String,SStructure> iNodeIdToSStructureMap,
			HashMap<String,List<String>> iNodeIDsToSNodeIdsMap,
			HashMap<String,SNode> sNodeIdToSNodeMap) throws GrafException {
				
		for (INode syntaxINode : syntaxIGraph.getNodes()) {
			if (syntaxINode.getOutEdges().size() > 0) {
				SStructure sourceSStructure = iNodeIdToSStructureMap.get(syntaxINode.getId());
				
				if (!docGraph.getNodes().contains(sourceSStructure)) {
					docGraph.addSNode(sourceSStructure);
				}
				
				List<INode> connectedSyntaxINodes = GrafReader.getOutboundConnectedNodes(syntaxINode);
				for (INode connectedSyntaxINode : connectedSyntaxINodes) {
					String dominatedINodeId = connectedSyntaxINode.getId();

					// don't add domrels to "tok" nodes, but to the tokens
					// they represent
					if (connectedSyntaxINode.getOutEdges().size() == 0) {
						List<ILink> links = connectedSyntaxINode.getLinks();
						if (links.size() > 0) {
							for (ILink link : links) {
								Iterable<IRegion> regions = link.regions();
								for (IRegion region : regions) {
									EList<SToken> dominatedSTokens = GrafReader.getSTokensFromIRegions(syntaxIGraph,
																					region, docGraph);
									for (SToken dominatedSToken : dominatedSTokens) {
										docGraph.addSNode(sourceSStructure, dominatedSToken, domRel);
									}
								}
							}							
						}
//						else { // handle syntax nodes that neither have outedges nor links,
//							    // i.e. trace nodes etc.
//								// FIXME: refactor, asap!
//							System.out.println("DEBUG: this node is broken:");
//							GrafGraphInfo.printNodeInfo(connectedSyntaxINode, syntaxIGraph);
//							Collection<String> nextTokenNodeIds = GrafReader.getBranchingAncestorTokenNodeIds(connectedSyntaxINode);
//							String nextTokenNodeId = nextTokenNodeIds.iterator().next();
//							INode nextTokenNode = syntaxIGraph.findNode(nextTokenNodeId);
//							ILink firstLink = nextTokenNode.getLinks().get(0);
//							IRegion firstRegion = firstLink.getRegions().get(0);
//							String regionId = firstRegion.getId();
//							int[] nextTokenNodeOffsets = GrafReader.getNodeOffsets(nextTokenNode);
//							STextualDS sTextualDS = docGraph.getSTextualDSs().get(0);
//							EList<SLayer> sLayers = docGraph.getSDocument().getSLayers();
//							String annoType = GrafReader.convertElementIdToAnnotationType(regionId);
//							SLayer sLayer = docGraph.getSLayer(annoType);
//							
//							String nullTokenRegionId = "null_token"+regionId;
//							String nullTokenId = addTokenToDocument(nextTokenNodeOffsets[0], 
//									nextTokenNodeOffsets[0], sTextualDS, 
//									docGraph.getSDocument(), sLayer, nullTokenRegionId);
//							
//							SToken nullToken = GrafReader.getSTokensFromOffsets(syntaxIGraph, 
//									nextTokenNodeOffsets[0], nextTokenNodeOffsets[0], docGraph).get(0);
//							docGraph.addSNode(sourceSStructure, nullToken, domRel);
//						}
					}
				
					else { // handle dominated nodes with outgoing edges
						if (iNodeIdToSStructureMap.containsKey(dominatedINodeId)) {
							SStructure dominatedSStructure = iNodeIdToSStructureMap.get(dominatedINodeId);
							docGraph.addSNode(sourceSStructure, dominatedSStructure, domRel);
						} 
						else {
							List<String> dominatedSElementIds = iNodeIDsToSNodeIdsMap.get(dominatedINodeId);
							for (String dominatedSElementId : dominatedSElementIds) {
								if (sNodeIdToSNodeMap.containsKey(dominatedSElementId)) {
									SNode dominatedSNode = sNodeIdToSNodeMap.get(dominatedSElementId);
									docGraph.addSNode(sourceSStructure, dominatedSNode, domRel);
								}
								else {
									System.out.println("DEBUG: DAMN, can't find element '"+dominatedSElementId+"' in token map or span map!1!!");
								}
							}
						}						
					}
				}	
			}
		}		
	}
	
	/** reads the syntax trees from an IGraph (which must only contain the 
	 *  syntax annotation type, e.g. "f.ptb") and adds them to the corresponding
	 *  SDocument 
	 * @param syntaxIGraph - an IGraph that only contains the "f.ptb" 
	 *  	annotation type (or similar)
	 *  @param iNodeIdToSNodeIdMap - maps from INode IDs (GrAF) to SNode IDs (Salt)
	 *  @param sNodeIdToSNodeMap - maps from SNode IDs to SNodes
	 * @throws GrafException */
	public static void addSyntaxToSDocument(IGraph syntaxIGraph,
			HashMap<String, List<String>> iNodeIdToSNodeIdMap,
			HashMap<String, SNode> sNodeIdToSNodeMap,
			SDocument sDocument) throws GrafException {
		
		SDocumentGraph docGraph = sDocument.getSDocumentGraph();
		List<SToken> sTokens= Collections.synchronizedList(docGraph.getSTokens());

		HashMap<String, SStructure> iNodeIdToSStructureMap = createSyntaxINodeSStructures(syntaxIGraph); 
		
		// FIXME: IGraph.getRoots() is broken, so we got to create/add our own root for now
		SStructure rootSStructure = SaltFactory.eINSTANCE.createSStructure();
		rootSStructure.setSName("root");
		docGraph.addSNode(rootSStructure);
		
		// we'll create dominance relations from the root of the SDocument to 
		// all the roots of the syntactic trees that it will contain
		addSyntaxTreeRootDomRelsToDocGraph(syntaxIGraph, docGraph, iNodeIdToSStructureMap, rootSStructure);
		addSyntaxNodeDomRelsToDocGraph(syntaxIGraph, docGraph, iNodeIdToSStructureMap, iNodeIdToSNodeIdMap, sNodeIdToSNodeMap);
	}	
}
