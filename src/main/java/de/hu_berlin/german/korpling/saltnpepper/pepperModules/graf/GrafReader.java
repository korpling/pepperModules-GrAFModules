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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.emf.common.util.EList;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IEdge;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.ILink;
import org.xces.graf.api.INode;
import org.xces.graf.api.IRegion;
import org.xces.graf.io.GrafLoader;
import org.xces.graf.io.dom.DocumentHeader;
import org.xces.graf.io.dom.ResourceHeader;
import org.xces.graf.util.GraphUtils;
import org.xml.sax.SAXException;

import de.hu_berlin.german.korpling.saltnpepper.pepper.modules.exceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualDS;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;

/**
 * These annotation types ("f.ids") are used in the MASC 3.0.0 corpus:
 * "f.logical", "f.s", "f.fn", "f.fntok", "f.nc", "f.penn", "f.ptb", "f.ptbtok",
 * "f.seg", "f.vc", "f.cb", "f.event", "f.mpqa", "f.ne"
 * 
 * @author Arne Neumann
 */
public class GrafReader {

	/** returns an IGraph that includes ALL annotations made to a document. */
	public static IGraph getAnnoGraph(ResourceHeader rscHeader, String docHeaderPath) throws GrafException, SAXException, IOException {

		GrafLoader loader = new GrafLoader(rscHeader);
		return loader.load(new File(docHeaderPath));
	}

	/**
	 * returns an IGraph that includes the chosen annotations made to a document
	 */
	public static IGraph getAnnoGraph(ResourceHeader rscHeader, String docHeaderPath, List<String> annoTypes) throws GrafException, SAXException, IOException {

		GrafDocumentHeader mascDocHeader = new GrafDocumentHeader(docHeaderPath);
		List<String> existingAnnotations = mascDocHeader.getAnnotationTypes();

		if (existingAnnotations.containsAll(annoTypes)) {
			GrafLoader loader = new GrafLoader(rscHeader);
			loader.setTypes(annoTypes); // loads ALL annotations if not
										// specified!
			return loader.load(new File(docHeaderPath));
		} else
			throw new NullPointerException("The document was not annotated with the " + "chosen annotation type(s).");
	}

	/** returns an IGraph that includes only ONE annotation level */
	public static IGraph getAnnoGraph(ResourceHeader rscHeader, String docHeaderPath, String annoType) throws GrafException, SAXException, IOException {

		GrafDocumentHeader mascDocHeader = new GrafDocumentHeader(docHeaderPath);
		List<String> existingAnnotations = mascDocHeader.getAnnotationTypes();

		if (existingAnnotations.contains(annoType))
			return getAnnoGraph(rscHeader, docHeaderPath, Arrays.asList(annoType));
		else
			throw new NullPointerException("The document was not annotated with the " + "chosen annotation type.");
	}

	/**
	 * returns a list of names of annotation spaces that the given IRegion is
	 * annotated in.
	 */
	public static List<String> getAnnoSpaceNamesFromRegion(IRegion region) {
		ArrayList<String> annoSpaceNames = new ArrayList<String>();
		for (INode annoNodes : region.getNodes()) {
			String annoSpaceName = annoNodes.getAnnotation().getAnnotationSpace().getName();
			annoSpaceNames.add(annoSpaceName);
		}
		return annoSpaceNames;
	}

	/** returns the IDs of all the documents in a corpus, e.g. MASC2-0130 */
	public static List<String> getDocumentIds(List<String> docHeaderPaths) throws FileNotFoundException, XPathExpressionException {
		List<String> docIds = new ArrayList<String>();
		for (String docHeaderPath : docHeaderPaths) {
			GrafDocumentHeader header = new GrafDocumentHeader(docHeaderPath);
			String docId = header.getDocumentId();
			docIds.add(docId);
		}
		return docIds;
	}

	/* prints the primary text that is described / annotated by an IGraph object */
	public static String getDocumentText(IGraph graph) {
		return graph.getContent().toString();
		// .getContent() doesn't return a string, because the graph could also
		// annotate some
		// other type of media
	}

	/**
	 * returns the number of documents annotated with a given annotation type,
	 * e.g. 'f.ptbtok' or 'f.seg'
	 */
	public static int getDocumentsWithAnnoTypeCount(List<String> docHeaderPaths, String annoType) throws FileNotFoundException, GrafException {

		int fileCount = 0;
		for (String docHeaderPath : docHeaderPaths) {
			File docHeaderFile = new File(docHeaderPath);
			DocumentHeader header = new DocumentHeader(docHeaderFile);
			List<String> annoTypes = header.getAnnotationTypes();
			if (annoTypes.contains(annoType) == true) {
				fileCount += 1;
			}
		}
		return fileCount;
	}

	/**
	 * returns a list of INodes that belong to a certain annotation layer.
	 * 
	 * @param annoSpaceName
	 *            - e.g. "ptb", will return all nodes which contain annotations
	 *            in the annotation space "ptb"
	 */
	public static List<INode> getNodesOfAnnoSpace(IGraph iGraph, String annoSpaceName) throws GrafException {
		ArrayList<INode> filteredNodes = new ArrayList<INode>();

		for (INode node : iGraph.getNodes()) {
			if (annoSpaceName.equals(node.getAnnotation().getAnnotationSpace().getName())) {
				filteredNodes.add(node);
			}
		}
		return filteredNodes;
	}

	/**
	 * returns a list of all INodes that are connected to the give INode (via
	 * ingoing edges).
	 */
	private static List<INode> getInboundConnectedNodes(INode node) {
		List<IEdge> inEdges = node.getInEdges();
		List<INode> connectedNodes = new ArrayList<INode>();
		for (IEdge inEdge : inEdges) {
			connectedNodes.add(inEdge.getFrom());
		}
		return connectedNodes;
	}

	/**
	 * returns a list of all INodes that the given INode is connected to (via
	 * outgoing edges) in ascending order.
	 * 
	 * FIXME: INode.getOutEdges() returns edges in no guaranteed order, so we
	 * had to resort to sorting them by their ID. This works fine with the MASC
	 * corpus, but is not part of the GrAF ISO standard!
	 */
	public static List<INode> getOutboundConnectedNodes(INode node) {
		List<IEdge> outEdges = node.getOutEdges();
		Collections.sort(outEdges, new GrafElementSortByID());
		List<INode> connectedNodes = new ArrayList<INode>();
		for (IEdge outEdge : outEdges) {
			connectedNodes.add(outEdge.getTo());
		}
		return connectedNodes;
	}

	/**
	 * returns a portion of the primary text (limited by a string onset/offset)
	 * that is linked to an IGraph
	 */
	public static String getPrimaryTextSequence(int start, int end, IGraph iGraph) {
		String primaryText = getDocumentText(iGraph);
		return primaryText.substring(start, end);
	}

	/**
	 * returns the portion of the primary text that is annotated/described by an
	 * IRegion
	 */
	public static String getPrimaryTextSequence(IRegion iRegion, IGraph iGraph) throws GrafException {
		String primaryText = getDocumentText(iGraph);
		int[] regionOffsets = GrafReader.getRegionOffsets(iRegion);
		return primaryText.substring(regionOffsets[0], regionOffsets[1]);
	}

	/**
	 * returns the portion of the primary text that is annotated by the
	 * IRegion(s) targeted by an ILink. WARNING: A link can target more than one
	 * region.
	 */
	public static String getPrimaryTextSequence(ILink iLink, IGraph iGraph) throws GrafException {
		int[] linkOffsets = GrafReader.getLinkOffsets(iLink);
		return getPrimaryTextSequence(linkOffsets[0], linkOffsets[1], iGraph);
	}

	/**
	 * returns the portion of the primary text that is annotated/described by an
	 * INode
	 */
	public static String getPrimaryTextSequence(INode iNode, IGraph iGraph) throws GrafException {
		int[] nodeOffsets = GrafReader.getNodeOffsets(iNode);
		return getPrimaryTextSequence(nodeOffsets[0], nodeOffsets[1], iGraph);
	}

	/** returns the IRegions belonging to a given annotation type */
	public static List<IRegion> getRegionsOfAnnoSpace(IGraph iGraph, String annoSpace) throws GrafException {
		return getRegionsOfAnnoSpaces(iGraph, asList(annoSpace));
	}

	/** returns the IRegions belonging to one of the given annotation types */
	public static List<IRegion> getRegionsOfAnnoSpaces(IGraph iGraph, List<String> annoSpaces) throws GrafException {
		List<IRegion> filteredRegions = new ArrayList<IRegion>();
		for (IRegion region : iGraph.getRegions()) {
			List<INode> nodes = region.getNodes();
			for (INode node : nodes) {
				String nodeAnnoSpace = node.getAnnotation().getAnnotationSpace().getName();
				if (annoSpaces.contains(nodeAnnoSpace)) {
					filteredRegions.add(region);
				}
			}
		}
		return filteredRegions;
	}

	/**
	 * returns an int array, which contains the string onset and string offset
	 * of an IRegion
	 */
	public static int[] getRegionOffsets(IRegion region) throws GrafException {
		// every region has 2 anchors (char onset and char offset)
		// they are stored as Long inside an Object instead of integers.
		// we need integers for the String.substring() method.
		int regionStart = GraphUtils.getOffset(region.getStart());
		int regionEnd = GraphUtils.getOffset(region.getEnd());
		return new int[] { regionStart, regionEnd };
	}

	/**
	 * returns an int array, which contains the string onset and string offset
	 * of an ILink. An ILink usually targets one IRegion (primary text segment),
	 * but may target more than one IRegion for tokenization purposes (e.g.
	 * "state-of-the-art" is represented as 7 consecutive IRegions in GrAF).
	 */
	public static int[] getLinkOffsets(ILink link) throws GrafException {
		List<IRegion> iRegions = link.getRegions();
		if (iRegions.size() == 1) {
			IRegion iRegion = iRegions.get(0);
			return getRegionOffsets(iRegion);
		} else { // if a link targets more than one region, find out where the
					// earliest region (lowest onset in primary text) starts and
					// where the latest one (highest offset in primary text)
					// ends.
			int lowestStartOffset = Integer.MAX_VALUE;
			int highestEndOffset = Integer.MIN_VALUE;

			List<IRegion> regions = link.getRegions();
			for (IRegion region : regions) {
				int[] regionOffsets = getRegionOffsets(region);
				int startOffset = regionOffsets[0];
				int endOffset = regionOffsets[1];

				if (startOffset < lowestStartOffset) {
					lowestStartOffset = startOffset;
				}

				if (endOffset > highestEndOffset) {
					highestEndOffset = endOffset;
				}
			}
			return new int[] { lowestStartOffset, highestEndOffset };
		}
	}

	/**
	 * returns an int array, which contains the string onset and string offset
	 * of an INode. An INode can either have one ILink (targeting one or more
	 * IRegions) OR has one or more outgoing IEdges to other INodes.
	 * */
	public static int[] getNodeOffsets(INode node) throws GrafException {
		List<ILink> nodeLinks = node.getLinks();
		if (nodeLinks.isEmpty() == false) {
			// if a node has a link, it targets IRegions
			// ("f.seg", i.e. basic tokenization of primary text into Regions)
			ILink iLink = nodeLinks.get(0);
			return getLinkOffsets(iLink);
		} else { // if the node has no links to regions of the primary text
			List<IEdge> outEdges = node.getOutEdges();
			if (outEdges.isEmpty()) {
				// this node is floating, i.e. it has neither links to regions
				// nor outgoing edges to other nodes and therefore doesn't cover
				// any primary text
				throw new PepperModuleException("INode " + node.getId() + " is floating. It doesn't cover any primary text.");
			} else { // if the node has outgoing edges, get offsets recursively
				int lowestStartOffset = Integer.MAX_VALUE;
				int highestEndOffset = Integer.MIN_VALUE;

				for (IEdge outEdge : outEdges) {
					INode targetNode = outEdge.getTo();
					int[] targetNodeOffsets = getNodeOffsets(targetNode);

					int startOffset = targetNodeOffsets[0];
					int endOffset = targetNodeOffsets[1];

					if (startOffset < lowestStartOffset) {
						lowestStartOffset = startOffset;
					}
					if (endOffset > highestEndOffset) {
						highestEndOffset = endOffset;
					}
				}
				return new int[] { lowestStartOffset, highestEndOffset };
			}
		}
	}

	/**
	 * returns the string onset and offset of a floating node. since a floating
	 * node doesn't cover any primary text, fake offsets will be generated. It
	 * will cover zero characters, located at the beginning of the primary text
	 * covered by the next leaf node. If there's no succeeding leaf node which
	 * covers primary text, it will be located at the end of the primary text
	 * covered by the preceding leaf node.
	 * 
	 * @throws GrafException
	 */
	public static int[] getFloatingNodeOffsets(IGraph graph, INode floatingNode) throws GrafException {
		DepthFirstSearch floatSearch = new DepthFirstSearch(graph, floatingNode);
		INode succeedingLeafNode = floatSearch.getSucceedingLeafNode(graph, floatingNode);
		INode precedingLeafNode = floatSearch.getPrecedingLeafNode(graph, floatingNode);
		if (succeedingLeafNode != null) {
			int[] successorOffsets = getNodeOffsets(succeedingLeafNode);
			return new int[] { successorOffsets[0], successorOffsets[0] };
		} else if (precedingLeafNode != null) {
			int[] predecessorOffsets = getNodeOffsets(precedingLeafNode);
			return new int[] { predecessorOffsets[1], predecessorOffsets[1] };
		} else {
			throw new PepperModuleException("Can't produce fake offsets for floating node " + floatingNode.getId());
		}
	}

	/**
	 * FIXME: IGraph.getRoots() is broken. This is a terribly inefficient
	 * workaround.
	 */
	public static List<INode> getRootNodes(IGraph iGraph) {
		Collection<INode> iNodes = iGraph.getNodes();
		List<INode> rootNodes = new ArrayList<INode>();
		for (INode iNode : iNodes) {
			for (IEdge inEdge : iNode.getInEdges()) {
				INode motherNode = inEdge.getFrom();
				if (motherNode.getInEdges().isEmpty() && !iNode.getOutEdges().isEmpty()) {
					rootNodes.add(iNode);
				}
			}
		}
		return rootNodes;
	}

	/**
	 * returns the STokens that represent the primary text segments that an
	 * IRegion links to.
	 */
	public static EList<SToken> getSTokensFromIRegions(IGraph iGraph, IRegion region, SDocumentGraph docGraph) throws GrafException {
		STextualDS sTextualDS = docGraph.getSTextualDSs().get(0);
		SDataSourceSequence sDataSourceSequence = SaltFactory.eINSTANCE.createSDataSourceSequence();
		int[] regionOffsets = getRegionOffsets(region);
		sDataSourceSequence.setSStart(regionOffsets[0]);
		sDataSourceSequence.setSEnd(regionOffsets[1]);
		sDataSourceSequence.setSSequentialDS(sTextualDS);
		return docGraph.getSTokensBySequence(sDataSourceSequence);
	}

	/**
	 * returns the STokens that represent the primary text within the given
	 * offsets.
	 */
	public static EList<SToken> getSTokensFromOffsets(IGraph iGraph, int onset, int offset, SDocumentGraph docGraph) throws GrafException {
		STextualDS sTextualDS = docGraph.getSTextualDSs().get(0);
		SDataSourceSequence sDataSourceSequence = SaltFactory.eINSTANCE.createSDataSourceSequence();
		sDataSourceSequence.setSStart(onset);
		sDataSourceSequence.setSEnd(offset);
		sDataSourceSequence.setSSequentialDS(sTextualDS);
		return docGraph.getSTokensBySequence(sDataSourceSequence);
	}

	/**
	 * returns true iff an INode has no outgoing edges but has at least one link
	 * (to a region).
	 */
	public static boolean isLeafNode(INode iNode) {
		if (iNode.getOutEdges().isEmpty() && !iNode.getLinks().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * returns true iff an INode has no outgoing edges and does not link to any
	 * IRegions.
	 */
	public static boolean isFloatingNode(INode iNode) {
		if (iNode.getOutEdges().isEmpty() && iNode.getLinks().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * returns the root INode of the syntax tree (or any other structure) to
	 * which the given (leaf) node belongs.
	 */
	public static INode getRootNodeFromNode(INode leafNode, IGraph iGraph) {
		List<INode> rootNodes = getRootNodes(iGraph);
		List<String> rootNodeCandidateIds = new ArrayList<String>();
		for (INode rootNodeCandidate : rootNodes) {
			String candidateId = rootNodeCandidate.getId();
			rootNodeCandidateIds.add(candidateId);
		}

		if (leafNode.getInEdges().isEmpty()) {
			System.out.println("DEBUG getRootNodeFromNode: IGraph has a weird floating root node: " + leafNode.getId());
			return leafNode; // a node without ingoing edges is a root node
		} else {
			INode firstParentNode = leafNode.getInEdge(0).getFrom();
			if (rootNodeCandidateIds.contains(firstParentNode.getId())) {
				return firstParentNode;
			} else {
				return getRootNodeFromNode(firstParentNode, iGraph);
			}
		}
	}

	/**
	 * returns a list of token node ID (with ILinks to IRegion to segments of
	 * the primary text) that elements of the syntax tree represented by the
	 * given root node.
	 */
	public static List<String> getTokenNodesCoveredByRootNode(INode rootNode) {
		List<INode> connectedNodes = getOutboundConnectedNodes(rootNode);
		List<String> tokenNodeIds = new ArrayList<String>();
		for (INode connectedNode : connectedNodes) {
			// a token node is an INode that has at least 1 ILink (to an
			// IRegion)
			if (connectedNode.getLinks().size() > 0) {
				tokenNodeIds.add(connectedNode.getId());
			} else {
				List<String> recursivelyConnectedTokenNodes = getTokenNodesCoveredByRootNode(connectedNode);
				tokenNodeIds.addAll(recursivelyConnectedTokenNodes);
			}
		}
		return tokenNodeIds;
	}

	/** returns a list of floating nodes that the given node dominates */
	public static List<INode> findOutboundConnectedFloatingNodes(INode dominatingNode) {
		List<INode> outboundConnectedNodes = getOutboundConnectedNodes(dominatingNode);
		List<INode> floatingNodes = new ArrayList<INode>();
		for (INode connectedNode : outboundConnectedNodes) {
			if (isFloatingNode(connectedNode)) {
				floatingNodes.add(connectedNode);
			}
		}
		return floatingNodes;

	}

}
