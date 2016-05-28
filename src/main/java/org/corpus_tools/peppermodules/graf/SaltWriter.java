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
package org.corpus_tools.peppermodules.graf;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IAnchor;
import org.xces.graf.api.IAnnotation;
import org.xces.graf.api.IAnnotationSpace;
import org.xces.graf.api.IFeature;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.ILink;
import org.xces.graf.api.INode;
import org.xces.graf.api.IRegion;

public class SaltWriter {
	protected static final Logger logger= LoggerFactory.getLogger(GrAFImporter.MODULE_NAME);
	/**
	 * A GrAF corpus may contain several primary text segmentations. For
	 * example, MASC 3.0.0 has a segmentation called "f.s", which partions the
	 * primary text into sentences, but it is not based on the primary text
	 * segmentations used by "f.seg" (word tokenization).
	 * 
	 * ANNIS2 can't handle multiple primary text segmentations correctly, so we
	 * have to work around this.
	 */
	public enum IRegionHandlingMethod {
		WORD_SEGMENTATION_ONLY, APPROXIMATE_MATCH, ALL_TOKEN_LEVELS
	}

	/** A Salt dominance relation */
	public static SALT_TYPE domRel = SALT_TYPE.SDOMINANCE_RELATION;

	private static int floatingNodeCount;

	private static HashMap<String, SLayer> annoSpaceSLayerMap;

	/**
	 * low level method to add an annotation to an SNode (or SToken/SSpan).
	 * 
	 * @param sAnnotationName
	 *            - the name that this type of annotation shall have in SALT,
	 *            e.g. 'POS' or 'Lemma'
	 * @param sAnnotationId
	 *            - the ID of the annotation node, e.g. 'penn-n23'
	 * @param sAnnotationValue
	 *            - the value / content of the annotation, e.g. 'NNP'
	 * @param sAnnotationNamespace
	 *            - the namespace of the annotation, e.g. 'xces' or 'fn'
	 * @param sNode
	 *            - the element to be annotated, e.g. an SNode (or SToken/SSpan)
	 *            instance
	 */
	public static void addAnnotationToNode(String sAnnotationName, String sAnnotationId, String sAnnotationValue, String sAnnotationNamespace, SNode sNode) {
		HashSet<String> existingAnnotationSNames = new HashSet<String>();
		for (SAnnotation existingAnno : sNode.getAnnotations()) {
			existingAnnotationSNames.add(existingAnno.getName());
		}

		// only add annotations that don't exist yet
		if (!existingAnnotationSNames.contains(sAnnotationName)) {
			SAnnotation sAnno = SaltFactory.createSAnnotation();
			sAnno.setName(sAnnotationName);
			sAnno.setValue(sAnnotationValue);
			sAnno.setNamespace(sAnnotationNamespace);
			sNode.addAnnotation(sAnno);
		}
	}

	/**
	 * Creates a {@link STextualDS} object containing the primary text
	 * {@link SaltSample#PRIMARY_TEXT} and adds the object to the
	 * {@link SDocumentGraph} being contained by the given {@link SDocument}
	 * object.
	 * 
	 * @param sDocument
	 *            the document, to which the created {@link STextualDS} object
	 *            will be added
	 */
	public static void addPrimaryTextToDocument(SDocument sDocument, String primaryText) {
		if (sDocument == null)
			throw new PepperModuleException("Cannot create example, because the given sDocument is empty.");
		if (sDocument.getDocumentGraph() == null)
			throw new PepperModuleException("Cannot create example, because the given sDocument does not contain an SDocumentGraph.");
		STextualDS sTextualDS = null;
		sTextualDS = SaltFactory.createSTextualDS();
		sTextualDS.setText(primaryText);
		// adding the text to the document-graph
		sDocument.getDocumentGraph().addNode(sTextualDS);
	}

	/**
	 * adds an IRegion to an SDocument (in the form of one or several STokens).
	 * An IRegion represents a segment of the primary text (e.g. a token), which
	 * can be annotated by zero or more INodes (each of them belonging to a
	 * different annotation space). For each of the annotation spaces an IRegion
	 * belongs to, this method will create a new SToken.
	 * 
	 * @return the list of IDs of the created STokens
	 */
	public static List<String> addIRegionToSDocument(IRegion iRegion, SDocument sDocument, HashMap<String, SLayer> annoSpaceSLayerMap) {

		String iRegionId = iRegion.getId();
		List<IAnchor> anchors = iRegion.getAnchors();
		int startAnchor = Integer.parseInt(anchors.get(0).writeString());
		int endAnchor = Integer.parseInt(anchors.get(1).writeString());

		List<String> sTokenIds = new ArrayList<String>();
		List<INode> annoNodes = iRegion.getNodes();
		if (annoNodes.isEmpty()) { // there's a special SLayer for all
									// unannotated regions
			SLayer regionLayer = annoSpaceSLayerMap.get("not-annotated");
			String sTokenId = addTokenToDocument(startAnchor, endAnchor, sDocument, regionLayer, iRegionId);
			sTokenIds.add(sTokenId);
		} else { // if region is annotated by one or more nodes
			for (INode annoNode : annoNodes) {
				String annoSpaceName = annoNode.getAnnotation().getAnnotationSpace().getName();
				SLayer regionLayer = annoSpaceSLayerMap.get(annoSpaceName);
				String sTokenId = addTokenToDocument(startAnchor, endAnchor, sDocument, regionLayer, iRegionId);
				sTokenIds.add(sTokenId);
			}
		}

		return sTokenIds;
	}

	/**
	 * add ALL IRegions to an SDocument and returns a map from IRegion IDs (e.g.
	 * 'seg-r316') to their corresponding STokenId(s) (e.g.
	 * 'salt:/MASC/MASC1-00046/MASC1-00046_graph#seg-r316').
	 * 
	 * @param iDocumentGraph
	 *            - the IGraph that contains all the IRegions to be added
	 * @param sDocument
	 *            - the SDocument that the regions will be added to (as STokens)
	 * @return a map from IRegion ID to a list of SToken IDs
	 */
	public static HashMap<String, List<String>> addAllIRegionsToSDocument(IGraph iDocumentGraph, SDocument sDocument) throws GrafException {

		// create a new SLayer for each IAnnotationSpace in an IGraph and add
		// it to the SDocument. create a Map (annotation space name --> SLayer).
		annoSpaceSLayerMap = new HashMap<String, SLayer>();
		for (IAnnotationSpace annoSpace : iDocumentGraph.getAnnotationSpaces()) {
			String annoSpaceName = annoSpace.getName();
			addSLayerToSDocument(sDocument, annoSpaceName);
		}

		// add an additional SLayer that covers all IRegions that aren't
		// annotated
		String annoSpaceName = "not-annotated";
		addSLayerToSDocument(sDocument, annoSpaceName);

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

	/** adds a new SLayer with the given name to an existing SDocument */
	public static void addSLayerToSDocument(SDocument doc, String layerName) {
		SLayer annoLayer = SaltFactory.createSLayer();
		annoLayer.setName(layerName);
		annoSpaceSLayerMap.put(layerName, annoLayer);
		doc.getDocumentGraph().addLayer(annoLayer);
	}

	/** takes a list of IRegions and returns the corresponding STokens */
	public static List<SToken> mapRegionsToTokens(List<IRegion> regions, HashMap<String, List<String>> regionIdToTokenIdsMap, SDocumentGraph docGraph) {
		List<SToken> tokenList = new ArrayList<SToken>();
		for (IRegion region : regions) {
			String regionId = region.getId();
			if (regionIdToTokenIdsMap.containsKey(regionId)) {
				List<String> tokenIds = regionIdToTokenIdsMap.get(regionId);
				for (String tokenId : tokenIds) {
					SToken sToken = (SToken) docGraph.getNode(tokenId);
					tokenList.add(sToken);
				}
			} else {
				throw new NullPointerException("There's no SToken mapped to the IRegion " + regionId);
			}
		}
		return tokenList;
	}

	/** adds an SSpan to an SDocument and returns the SSpan ID. */
	public static String addSSpanToSDocument(List<SToken> sTokens, SDocument sDocument, Set<SLayer> sLayers) {
		List<SToken> sTokensList = new ArrayList<>(sTokens);
		// createSSpan only accepts Lists, not Lists
		SSpan sSpan = sDocument.getDocumentGraph().createSpan(sTokensList);
		for (SLayer layer : sLayers) {
			layer.addNode(sSpan);
		}
		return sSpan.getId();
	}

	/**
	 * Adds all SSpans to an SDocument. Returns a map from INode IDs (e.g.
	 * 'ptb-n00409') to SNode (SSpan/SToken) IDs, e.g.
	 * "salt:/MASC/MASC1-00046/MASC1-00046_graph#seg-r361" or
	 * "salt:/MASC/MASC1-00046/MASC1-00046_graph#sSpan340>".
	 * 
	 * An SSpan represents a number of consecutive STokens. In GrAF terminology
	 * an SSpan is equivalent to an INode that links to more than one IRegion or
	 * an INode that is connected via one or more outgoing edges to INodes that
	 * do so.
	 * 
	 * @return a map from an INode ID to an SNode ID (or SToken/SSpan ID),
	 * @throws GrafException
	 */
	public static HashMap<String, List<String>> addSSpansToSDocument(IGraph iDocumentGraph, SDocument sDocument, HashMap<String, List<String>> regionIdsToTokenIdsMap) throws GrafException {

		SDocumentGraph sDocumentGraph = sDocument.getDocumentGraph();
		HashMap<String, List<String>> iNodeIdsToSNodeIdsMap = new HashMap<String, List<String>>();

		for (INode iNode : iDocumentGraph.getNodes()) {
			List<IRegion> iRegionsCoveredByINode = getIRegionsCoveredByINode(iNode, sDocumentGraph);
			if (iRegionsCoveredByINode.isEmpty()) {
				if (GrafReader.isFloatingNode(iNode)) {
					addFloatingNodeToSDocument(iDocumentGraph, sDocument, iNode, iNodeIdsToSNodeIdsMap);
				} else { // the mother node of a floating node often doesn't
							// cover any regions
							// throw new UnsupportedOperationException
					logger.debug("INode " + iNode.getId() + " doesn't cover" + " any IRegions but is not a floating node either!" + " Do we need to handle it separately?");
				}
			} else if (iRegionsCoveredByINode.size() == 1) {
				String coveredIRegionId = iRegionsCoveredByINode.get(0).getId();
				addRegionToINodeSNodeMap(coveredIRegionId, iNode, regionIdsToTokenIdsMap, iNodeIdsToSNodeIdsMap);
			} else if (iRegionsCoveredByINode.size() > 1) {
				addRegionsToINodeSNodeMap(iNode, iRegionsCoveredByINode, iNodeIdsToSNodeIdsMap, regionIdsToTokenIdsMap, sDocument);
			}
		}
		return iNodeIdsToSNodeIdsMap;
	}

	/**
	 * takes a floating INode (a GrAF node that has neither outgoing edges nor
	 * links to regions of primary text), creates a fake SToken for it, adds it
	 * to the SDocument and to the map (INode IDs --> SNode IDs).
	 */
	public static void addFloatingNodeToSDocument(IGraph iDocumentGraph, SDocument sDocument, INode floatingINode, HashMap<String, List<String>> iNodeIdsToSNodeIdsMap) throws GrafException {
		// in GrAF, it is allowed to have nodes that have neither
		// outgoing edges nor links to regions of primary text!
		int[] offsets = GrafReader.getFloatingNodeOffsets(iDocumentGraph, floatingINode);
		String annoSpaceName = floatingINode.getAnnotation().getAnnotationSpace().getName();
		SLayer regionLayer = annoSpaceSLayerMap.get(annoSpaceName);
		String regionId = "floating-" + regionLayer.getName() + "-node-" + String.valueOf(floatingNodeCount);

		String fakeTokenId = addTokenToDocument(offsets[0], offsets[1], sDocument, regionLayer, regionId);
		iNodeIdsToSNodeIdsMap.put(floatingINode.getId(), asList(fakeTokenId));
		floatingNodeCount++;
	}

	/**
	 * adds an INode that only covers one IRegion to the INode IDs --> SNode IDs
	 * map.
	 */
	public static void addRegionToINodeSNodeMap(String coveredIRegionId, INode iNode, HashMap<String, List<String>> regionIdsToTokenIdsMap, HashMap<String, List<String>> iNodeIdsToSNodeIdsMap) {
		if (regionIdsToTokenIdsMap.containsKey(coveredIRegionId)) {
			List<String> coveredSTokenIds = regionIdsToTokenIdsMap.get(coveredIRegionId);
			iNodeIdsToSNodeIdsMap.put(iNode.getId(), coveredSTokenIds);
		} else {
			throw new PepperModuleException("IRegion " + coveredIRegionId + " can't be found in regionIdsToTokenIdsMap.");
		}
	}

	/**
	 * adds an INode that covers multiple IRegions to the INode IDs --> SNode
	 * IDs map and also adds SSpans to the SDocument that cover the same primary
	 * text as those IRegions.
	 */
	public static void addRegionsToINodeSNodeMap(INode iNode, List<IRegion> iRegionsCoveredByINode, HashMap<String, List<String>> iNodeIdsToSNodeIdsMap, HashMap<String, List<String>> regionIdsToTokenIdsMap, SDocument sDocument) {
		// IRegions are already added to the document, we just need to add
		// SSpans for INodes that cover more than one IRegion
		List<SToken> tokens = mapRegionsToTokens(iRegionsCoveredByINode, regionIdsToTokenIdsMap, sDocument.getDocumentGraph());
		Set<SLayer> sLayers = SaltWriter.mapTokensToSLayers(tokens);
		String sSpanId = addSSpanToSDocument(tokens, sDocument, sLayers);
		iNodeIdsToSNodeIdsMap.put(iNode.getId(), asList(sSpanId));
		// using a list here to make the map usable for both SSpanIDs as well as
		// STokenIDs
	}

	/**
	 * creates a map from INodes to the IRegions they link to
	 * 
	 * @return a map from INode ID to a list of IRegion IDs
	 */
	public static HashMap<String, List<String>> getINodeIdToIRegionIdsMap(IGraph iDocumentGraph, SDocumentGraph sDocumentGraph) {
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

	/**
	 * creates a map from IRegions to the INodes that link to them
	 * 
	 * @return a map from IRegion ID to a list of INode IDs
	 */
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

	/**
	 * returns a list of IRegions that an INode covers (via links or recursively
	 * via outbound IEdges that connect to other INodes (that link to IRegions).
	 */
	public static List<IRegion> getIRegionsCoveredByINode(INode iNode, SDocumentGraph sDocumentGraph) {

		List<IRegion> iRegionsCoveredByINode = new ArrayList<IRegion>();

		List<INode> connectedINodes = GrafReader.getOutboundConnectedNodes(iNode);
		for (INode connectedINode : connectedINodes) {
			List<IRegion> regionsFromConnectedNode = getIRegionsCoveredByINode(connectedINode, sDocumentGraph);
			iRegionsCoveredByINode.addAll(regionsFromConnectedNode);
		}

		List<ILink> links = iNode.getLinks();
		for (ILink link : links) {
			List<IRegion> regions = link.getRegions();
			iRegionsCoveredByINode.addAll(regions);
		}
		return iRegionsCoveredByINode;
	}

	/**
	 * creates an SToken and adds it to the SDocumentGraph of an SDocument. the
	 * parameters needed are usually retrieved from an IRegion.
	 * 
	 * @param onset
	 *            - token onset in the primary text
	 * @param offset
	 *            - token offset
	 * @param STextualDS
	 *            - contains the primary text
	 * @param SLayer
	 *            - the annotation layer that the token is added to (e.g.
	 *            "f.seg")
	 * @param regionId
	 *            - the IRegion's id (e.g. 'seg-r91'), will be used to name the
	 *            SToken
	 * 
	 * @return the ID of the created token
	 */
	public static String addTokenToDocument(int onset, int offset, SDocument sDocument, SLayer layer, String regionId) {
		STextualDS sTextualDS = sDocument.getDocumentGraph().getTextualDSs().get(0);
		SToken sToken = SaltFactory.createSToken();
		sToken.setName(regionId);
		sDocument.getDocumentGraph().addNode(sToken);
		layer.addNode(sToken);
		STextualRelation sTextRel = SaltFactory.createSTextualRelation();
		sTextRel.setSource(sToken);
		sTextRel.setTarget(sTextualDS);
		sTextRel.setStart(onset);
		sTextRel.setEnd(offset);
		sDocument.getDocumentGraph().addRelation(sTextRel);
		return sToken.getId();
	}

	/**
	 * takes a list of STokens and returns a list of all the SLayers that at
	 * least one of them occurs in.
	 */
	public static Set<SLayer> mapTokensToSLayers(List<SToken> tokens) {
		Set<SLayer> sLayersList = new HashSet<>();
		for (SToken token : tokens) {
			Set<SLayer> tokenLayers = token.getLayers();
			sLayersList.addAll(tokenLayers);
		}
		return sLayersList;
	}

	/**
	 * adds all annotations to an SDocument.
	 * 
	 * @param iGraph
	 *            - the IGraph that contains all the annotations to be added
	 * @param iNodeIdToSNodeIdMap
	 *            - a map from an INode ID to the ID of an SNode (or
	 *            SToken/SSpan) which it annotates
	 * @param sDocument
	 *            - the Salt document to which the annotations shall be added
	 * @return a map from SNode (or SToken/SSpan) IDs to SNodes (or
	 *         STokens/SSpans)
	 * @throws GrafException
	 */
	public static HashMap<String, SNode> addAnnotationsToSDocument(IGraph iGraph, HashMap<String, List<String>> iNodeIdToSNodeIdMap, SDocument sDocument) throws GrafException {

		SDocumentGraph docGraph = sDocument.getDocumentGraph();

		HashMap<String, SNode> nodeIdToNodeMap = new HashMap<String, SNode>();
		for (SNode sNode : docGraph.getTokens()) {
			nodeIdToNodeMap.put(sNode.getId(), sNode);
		}
		for (SNode sNode : docGraph.getSpans()) {
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

	/**
	 * reads all annotations from an INode and adds them to the given SNode (or
	 * SToken/SSpan).
	 */
	public static void addAnnotationsToSNode(INode annotationINode, SNode sNode) {
		String annotationId = annotationINode.getId();
		IAnnotation iAnnotation = annotationINode.getAnnotation(); 
		// returns default annotation
		String annoNamespace = iAnnotation.getAnnotationSpace().getName();
		Iterable<IFeature> annoFeatures = iAnnotation.getFeatures().features();
		for (IFeature feature : annoFeatures) {
			addAnnotationToNode(feature.getName(), annotationId, feature.getStringValue(), annoNamespace, sNode);
		}
	}

	/**
	 * Creates a new SStructure for all INodes in an f.ptb IGraph that have
	 * outgoing edges. Each SStructure represents a syntax node (S, NP etc.) and
	 * is annotated accordingly. Returns a map from INode IDs to the
	 * corresponding SStructure.
	 * 
	 * @param syntaxIGraph
	 *            - an IGraph that only contains the "f.ptb" annotation type (or
	 *            similar)
	 * @return iNodeIdToSStructureMap - a map from INode IDs to the SStructures
	 *         they represent
	 */
	public static HashMap<String, SStructure> createSyntaxINodeSStructures(IGraph syntaxIGraph) {
		// map INodes to SSTructures
		HashMap<String, SStructure> iNodeIdToSStructureMap = new HashMap<String, SStructure>();
		Collection<INode> syntaxINodes = syntaxIGraph.getNodes();
		for (INode syntaxINode : syntaxINodes) {
			if (syntaxINode.getOutEdges().size() > 0) {
				// create an SStructure for each syntax node, i.e. nodes that
				// are labeled with 'S', 'NP' etc. but don't create SStructures
				// for leaf nodes (here: tokens).
				SStructure syntaxSStructure = SaltFactory.createSStructure();
				syntaxSStructure.setName(syntaxINode.getId());
				syntaxSStructure.setId(syntaxINode.getId());
				addAnnotationsToSNode(syntaxINode, syntaxSStructure);

				// syntaxSStructure.createSAnnotation(arg0, arg1, arg2)
				iNodeIdToSStructureMap.put(syntaxINode.getId(), syntaxSStructure);
			}
		}
		return iNodeIdToSStructureMap;
	}

	/**
	 * adds dominance relations to the document graph (from the document graph
	 * root node to each syntax tree root node)
	 * 
	 * @throws GrafException
	 */
	public static void addSyntaxTreeRootDomRelsToDocGraph(IGraph syntaxIGraph, SDocumentGraph docGraph, HashMap<String, SStructure> iNodeIdToSStructureMap, SStructure rootSStructure) throws GrafException {
		for (INode treeRootINode : GrafReader.getRootNodes(syntaxIGraph)) {
			String treeRootNodeId = treeRootINode.getId();
			if (iNodeIdToSStructureMap.containsKey(treeRootNodeId)) {
				SStructure dominatedSStructure = iNodeIdToSStructureMap.get(treeRootNodeId);
				if (!docGraph.getNodes().contains(dominatedSStructure)) {
					docGraph.addNode(dominatedSStructure);
				}

				docGraph.addNode(rootSStructure, dominatedSStructure, domRel);
			}
		}
	}

	/**
	 * add a dominance relation from each syntax node (all nodes in a "f.ptb"
	 * IGraph that have outgoing edges) to the nodes they dominate.
	 * 
	 * NOTE: token ("tok") nodes don't have outgoing edges but links to regions
	 * to primary text segments (aka string onsets/offsets)
	 * 
	 * @throws GrafException
	 */
	public static void addSyntaxNodeDomRelsToDocGraph(IGraph syntaxIGraph, SDocumentGraph docGraph, HashMap<String, SStructure> iNodeIdToSStructureMap, HashMap<String, List<String>> iNodeIDsToSNodeIdsMap, HashMap<String, SNode> sNodeIdToSNodeMap) throws GrafException {
		for (INode syntaxINode : syntaxIGraph.getNodes()) {
			if (syntaxINode.getOutEdges().size() > 0) {
				SStructure sourceSStructure = iNodeIdToSStructureMap.get(syntaxINode.getId());

				// TODO: check why the document graph doesn't contain certain
				// SStructures
				if (!docGraph.getNodes().contains(sourceSStructure)) {
					docGraph.addNode(sourceSStructure);
				}

				for (INode connectedSyntaxINode : GrafReader.getOutboundConnectedNodes(syntaxINode)) {
					// add dominance relation between a syntax node (e.g. one
					// that represents a syntactic categories) and a token node
					if (connectedSyntaxINode.getOutEdges().size() == 0) {
						List<ILink> linksToTokenRegions = connectedSyntaxINode.getLinks();
						if (linksToTokenRegions.size() > 0) {
							addDomRelToNonFloatingSToken(syntaxIGraph, docGraph, sourceSStructure, linksToTokenRegions);
						} else {
							addDomRelToFloatingSToken(syntaxIGraph, docGraph, sourceSStructure, connectedSyntaxINode);
						}
					}

					else { // handle dominated nodes with outgoing edges
						addDomRelBetweenSyntaxNodes(docGraph, sourceSStructure, connectedSyntaxINode, iNodeIdToSStructureMap, iNodeIDsToSNodeIdsMap, sNodeIdToSNodeMap);
					}
				}
			}
		}
	}

	/**
	 * add a dominance relation between two syntax nodes (i.e. not dominating
	 * tokens directly)
	 */
	public static void addDomRelBetweenSyntaxNodes(SDocumentGraph docGraph, SStructure sourceSStructure, INode dominatedINode, HashMap<String, SStructure> iNodeIdToSStructureMap, HashMap<String, List<String>> iNodeIDsToSNodeIdsMap, HashMap<String, SNode> sNodeIdToSNodeMap) {

		String dominatedINodeId = dominatedINode.getId();
		if (iNodeIdToSStructureMap.containsKey(dominatedINodeId)) {
			SStructure dominatedSStructure = iNodeIdToSStructureMap.get(dominatedINodeId);
			docGraph.addNode(sourceSStructure, dominatedSStructure, domRel);
		} else { // TODO: check why iNodeIdToSStructureMap doesn't contain
					// certain dominated INode IDs
			List<String> dominatedSElementIds = iNodeIDsToSNodeIdsMap.get(dominatedINodeId);
			for (String dominatedSElementId : dominatedSElementIds) {
				if (sNodeIdToSNodeMap.containsKey(dominatedSElementId)) {
					SNode dominatedSNode = sNodeIdToSNodeMap.get(dominatedSElementId);
					docGraph.addNode(sourceSStructure, dominatedSNode, domRel);
				} else {
					throw new PepperModuleException(" Can't find element '" + dominatedSElementId + "' in token map or span map!1!!");
				}
			}
		}
	}

	/**
	 * add a dominance relation between a syntax node and a floating SToken
	 * node. TODO: implement method / merge it with addDomRelToNonFloatingSToken
	 */
	public static void addDomRelToFloatingSToken(IGraph syntaxIGraph, SDocumentGraph docGraph, SStructure sourceSStructure, INode connectedSyntaxINode) {
		logger.debug("Connected syntax INode " + connectedSyntaxINode.getId() + " does neither have out edges nor links.");
		throw new UnsupportedOperationException("TODO: METHOD NOT IMPLEMENTED, YET");
		// docGraph.addSNode(sourceSStructure, connectedSyntaxINode, domRel);
	}

	/**
	 * adds a dominance relation from a syntax node to one or more token nodes.
	 * 
	 * @param sourceSStructure
	 *            - the SStructure (SNode) that dominates the token node
	 * @param linksToTokenRegions
	 *            - list of ILinks to IRegions of primary text that are
	 *            equivalent to SToken nodes
	 */
	public static void addDomRelToNonFloatingSToken(IGraph syntaxIGraph, SDocumentGraph docGraph, SStructure sourceSStructure, List<ILink> linksToTokenRegions) throws GrafException {
		for (ILink link : linksToTokenRegions) {
			for (IRegion region : link.regions()) {
				List<SToken> dominatedSTokens = GrafReader.getSTokensFromIRegions(syntaxIGraph, region, docGraph);
				for (SToken dominatedSToken : dominatedSTokens) {
					docGraph.addNode(sourceSStructure, dominatedSToken, domRel);
				}
			}
		}
	}

	/**
	 * reads the syntax trees from an IGraph (which must only contain the syntax
	 * annotation type, e.g. "f.ptb") and adds them to the corresponding
	 * SDocument
	 * 
	 * @param syntaxIGraph
	 *            - an IGraph that only contains the "f.ptb" annotation type (or
	 *            similar)
	 * @param iNodeIdToSNodeIdMap
	 *            - maps from INode IDs (GrAF) to SNode IDs (Salt)
	 * @param sNodeIdToSNodeMap
	 *            - maps from SNode IDs to SNodes
	 * @throws GrafException
	 */
	public static void addSyntaxToSDocument(IGraph syntaxIGraph, HashMap<String, List<String>> iNodeIdToSNodeIdMap, HashMap<String, SNode> sNodeIdToSNodeMap, SDocument sDocument) throws GrafException {

		SDocumentGraph docGraph = sDocument.getDocumentGraph();
		List<SToken> sTokens = Collections.synchronizedList(docGraph.getTokens());

		HashMap<String, SStructure> iNodeIdToSStructureMap = createSyntaxINodeSStructures(syntaxIGraph);
		
		// FIXME: IGraph.getRoots() is broken, so we got to create/add our own
		// root for now
		SStructure rootSStructure = SaltFactory.createSStructure();
		rootSStructure.setName("root");
		docGraph.addNode(rootSStructure);

		// we'll create dominance relations from the root of the SDocument to
		// all the roots of the syntactic trees that it will contain
		addSyntaxTreeRootDomRelsToDocGraph(syntaxIGraph, docGraph, iNodeIdToSStructureMap, rootSStructure);
		addSyntaxNodeDomRelsToDocGraph(syntaxIGraph, docGraph, iNodeIdToSStructureMap, iNodeIdToSNodeIdMap, sNodeIdToSNodeMap);
	}
}