package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IAnnotation;
import org.xces.graf.api.IAnnotationSpace;
import org.xces.graf.api.IEdge;
import org.xces.graf.api.IFeature;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.ILink;
import org.xces.graf.api.INode;
import org.xces.graf.api.IRegion;
import org.xces.graf.api.IStandoffHeader;

/** This class contains some methods for playing around with GrAF's IGraph objects.
 *  The MASC corpus does not use many of GrAF's features, therefore they are largely
 *  unused / unmaintained. */
public class GrafGraphInfo {


	/** prints the ID and label of an IAnnotation, as well the name and type 
	 *  of the corresponding IAnnotationSpace. prints the name and value of 
	 *  features belonging to the annotation (via calling printFeatureInfo). */
	public static void printAnnotationInfo(IAnnotation anno) {
		IAnnotationSpace annoSpace = anno.getAnnotationSpace();
		System.out.println("    anno space name: " + annoSpace.getName());
		System.out.println("    anno space type: " + annoSpace.getType()); 
		// shows the URI, where the annotation space is defined/explained
//		String graphElementId = anno.getElement().getId(); // get the ID of the node/edge associated with this annotation
		String annoId = anno.getId();
		String annoLabel = anno.getLabel();
		System.out.println("\tannotation ID: " + annoId);
		System.out.println("\tannotation label: " + annoLabel);
		Iterable<IFeature> features = anno.features();
		for (IFeature feature : features) {
			System.out.println();
			printFeatureInfo(feature);
		}
	}
	
	
	/**	prints all annotation spaces used in an IGraph */
	public static void printAnnotationSpacesInfo(IGraph graph) {
		Collection<IAnnotationSpace> annoSpaces = graph.getAnnotationSpaces();
		int annoSpacesSize = annoSpaces.size();
		System.out.println("graph contains " + annoSpacesSize + " annotation spaces");
		for (IAnnotationSpace annoSpace : annoSpaces) {
			String annoSetName = annoSpace.getName();
			String annoSetType = annoSpace.getType();
			System.out.println("annotation set: " + annoSetName + " (name) " 
							   + annoSetType + " (type)");
		}
	}	
	
	/** prints the ID, source node ID and destination node ID of an IEdge.
	 *  print annotations (via calling printAnnotationInfo), if present. 
	 *  (edges don't seem to have annotations in MASC 3.0.0) */
	public static void printEdgeInfo(IEdge edge) {		
		System.out.println("\t" + "edge ID: " + edge.getId());
		System.out.println("\t\t" + "source node ID: " + edge.getFrom().getId());
		System.out.println("\t\t" + "destination node ID: " + edge.getTo().getId());

		if (edge.annotated() == true) {
			for (IAnnotation anno : edge.annotations()) {
				printAnnotationInfo(anno);
			}
		} else {
			System.out.println("    edge is not annotated.");
		}
	}


	/** Prints the name and value of a feature.
	 *  In MASC 3.0.0, features are attaced to the 
	 *  annotations of nodes. */
	public static void printFeatureInfo(IFeature feature) {
		System.out.println("\t    feature name: " + feature.getName());
		System.out.println("\t    feature value: " + feature.getStringValue());
		}		
	
	
	/** prints various information about an IGraph object (GrAF/MASC), 
	 *  i.e. printEdgeInfo, printNodeInfo, printRegionInfo, printRootInfo*/
	public static void printGraphInfo(IGraph graph) throws GrafException {
		System.out.println("edge size: " + graph.getEdgeSetSize());
//		for (IEdge edge : graph.getEdges()) {
//			printEdgeInfo(edge);
//		}

		System.out.println("node size: " + graph.getNodeSetSize());
		for (INode node: graph.getNodes()) {
			printNodeInfo(node, graph);
		}
		
		Collection<IRegion> regions = graph.getRegions();
		System.out.println("regions: " + regions.size());
//		for (IRegion region : regions) {
//			printRegionInfo(region, graph);
//			System.out.println();
//		}
		
		printRootInfo(graph);		
		System.out.println("\n\n");
	}

	/** prints the IDs of IRegions targeted by an ILink and the portion of the 
	 * primary text covered by them. */
	public static void printLinkInfo(ILink iLink, IGraph iGraph) throws GrafException {
		List<IRegion> regions = iLink.getRegions();
		int regionsCount = iLink.getRegions().size();
		System.out.print("\tlink targets " + regionsCount + " region(s):");
		for (IRegion region: regions) {
			System.out.print(" " + region.getId());
		}
		System.out.println();
		System.out.println("\t\tprimary text: " + GrafReader.getPrimaryTextSequence(iLink, iGraph));
	}
	
	
	/** prints information about an INode: node ID, node degree, IDs of 
	 *  in edges and out edges. prints node annotations via calling 
	 *  printAnnotationInfo 
	 * @throws GrafException */
	public static void printNodeInfo(INode iNode, IGraph iGraph) throws GrafException {
		System.out.println("node ID: " + iNode.getId() 
						   + " has degree: " + iNode.degree());
		
		System.out.println();
		List<IEdge> inEdges = iNode.getInEdges();
		System.out.print("    ingoing edges: ");
		for (IEdge edge : inEdges) {
			System.out.print(edge.getId() + " ");
		}
		System.out.println();
		
		List<IEdge> outEdges = iNode.getOutEdges();
		System.out.print("    outgoing edges: ");
		for (IEdge edge : outEdges) {
			System.out.print(edge.getId() + " ");
		}
		System.out.println();
		
		IAnnotationSpace defaultAnnotation = iNode.getAnnotation().getAnnotationSpace();
		System.out.println("\tannotation space name: " + defaultAnnotation.getName());
		System.out.println("\tannotation space type: " + defaultAnnotation.getType());
		
		for (IAnnotation anno : iNode.annotations()) {
			printAnnotationInfo(anno);
		}
		System.out.println();
		
		List<ILink> links = iNode.getLinks();
		for (ILink link : links) {
			printLinkInfo(link, iGraph);
		}
	}	

	/** prints the annotation types present in an IGraph and counts the number 
	 *  of INodes using that annotation type */
	public static void printNodesStatistics(IGraph iGraph) {
		HashMap<String,Integer> annoTypeNodeCountMap = getNodeCountPerAnnoType(iGraph);
		
		System.out.println("node annotation types:");
		for (String annoType : annoTypeNodeCountMap.keySet()) {
			System.out.println("\t" + annoType + " : " + annoTypeNodeCountMap.get(annoType) + " nodes");
		}
	}

	/** returns a <String, int> map that counts the nodes for each 
	 *  annotation type */
	public static HashMap<String, Integer> getNodeCountPerAnnoType(IGraph iGraph) {
		Collection<INode> nodes = iGraph.getNodes();
		HashMap<String, Integer> annoTypeNodeCountMap = new HashMap<String, Integer>();
		
		for (INode node : nodes) {
			String nodeId = node.getId();
			String annotationType = GrafReader.convertElementIdToAnnotationType(nodeId);
			if (annoTypeNodeCountMap.containsKey(annotationType)) {
				Integer nodeCount = annoTypeNodeCountMap.get(annotationType);
				annoTypeNodeCountMap.put(annotationType, nodeCount+1);
			} else {
				annoTypeNodeCountMap.put(annotationType, 1);
			}
		}
		return annoTypeNodeCountMap;
	}
		

	/** prints the ID and primary text of all INodes present in a 
	 *  given annotation type/level. */
	public static void printNodesOfAnnoType(IGraph iGraph, String annoType) throws GrafException {
		String nodeIdPrefix = GrafReader.convertAnnoTypeToElementIdPrefix(annoType);
		for (INode node : iGraph.getNodes()) {
			if (node.getId().startsWith(nodeIdPrefix)) {
				String primaryTextSequence = GrafReader.getPrimaryTextSequence(node, iGraph);
				System.out.println("node " + node.getId() + ": " + primaryTextSequence);
			}
		}
	}		
	
	
	/** prints the anchors (string onset and string offset) of an IRegion */
	public static void printRegionInfo(IRegion region, IGraph iGraph) throws GrafException {
		int[] regionOffsets = GrafReader.getRegionOffsets(region);
		System.out.println("\tregion id: " + region.getId() + " start: " 
				+ regionOffsets[0] + " end: " + regionOffsets[1]);
		System.out.println("    primary text sequence: " + GrafReader.getPrimaryTextSequence(regionOffsets[0], regionOffsets[1], iGraph));
	}		
	
	
	/** prints all annotation types that work directly on the primary text 
	 *  (e.g. f.seg, f.logical) and list how many IRegions they have. */
	public static void printRegionsStatistics(IGraph iGraph) {
		HashMap<String,Integer> annoTypeRegionCountMap = getRegionCountPerAnnoType(iGraph);
		
		System.out.println("region annotation types (working directly on the primary text):");
		for (String annoType : annoTypeRegionCountMap.keySet()) {
			System.out.println("\t" + annoType + " : " + annoTypeRegionCountMap.get(annoType) + " regions");
		}
	}
	
	/** returns a <String, int> map that counts the regions for each 
	 *  annotation type that work directly on the primary text 
	 *  (e.g. f.seg, f.logical) */
	public static HashMap<String, Integer> getRegionCountPerAnnoType(IGraph iGraph) {
		Collection<IRegion> regions = iGraph.getRegions();
		HashMap<String, Integer> annoTypeRegionCountMap = new HashMap<String, Integer>();
		
		for (IRegion region : regions) {
			String regionId = region.getId();
			String annotationType = GrafReader.convertElementIdToAnnotationType(regionId);
			if (annoTypeRegionCountMap.containsKey(annotationType)) {
				Integer regionCount = annoTypeRegionCountMap.get(annotationType);
				annoTypeRegionCountMap.put(annotationType, regionCount+1);
			} else {
				annoTypeRegionCountMap.put(annotationType, 1);
			}
		}
		return annoTypeRegionCountMap;
	}
	
	/** prints the ID and primary text of all IRegions present in a 
	 *  given annotation type/level. */
	public static void printRegionsOfAnnoType(IGraph iGraph, String annoType) throws GrafException {
		String regionIdPrefix = GrafReader.convertAnnoTypeToElementIdPrefix(annoType);
		Collection<IRegion> regions = iGraph.getRegions();
		for (IRegion region : regions) {
			if (region.getId().startsWith(regionIdPrefix)) {
				String primaryTextSequence = GrafReader.getPrimaryTextSequence(region, iGraph);
				System.out.println("region " + region.getId() + ": " + primaryTextSequence);
			}
		}
	}
	
	
	/** prints the root id of a graph, if there is one. 
	 *  MASC 3.0.0 documents don't even seem to have a root! */
	public static void printRootInfo(IGraph graph) {
		INode root = graph.getRoot();
		if (root == null) {
			System.out.println("This graph has no root.");
		}
		else System.out.println("root id: " + root.getId());		
	}
	
	/** Lists the dependencies, annotation spaces and roots that an IGraph has.
	 *  Results will only be meaningful if the IGraph contains only one or few 
	 *  annotation types. */
	public static void printStandoffHeaderInfo(IGraph graph) {
		IStandoffHeader header = graph.getHeader();

		List<String> dependencies = header.getDependsOn();
		if (dependencies.isEmpty() == true) {System.out.println("0 dependencies.");}
		else {
			System.out.print(dependencies.size() + " dependencies: ");
			for (String dependency : dependencies) {
				System.out.print(dependency);
			}
			System.out.println();
		}
		
		List<IAnnotationSpace> annoSpaces = header.getAnnotationSpaces();
		if (annoSpaces.isEmpty() == true) {
			System.out.println("0 annotation spaces.");
		}
		else printAnnotationSpacesInfo(graph);		
		
		List<String> headerRoots = header.getRoots();
		if (headerRoots.isEmpty() == true) {
			System.out.println("This graph is not a forest.");
		}
		else {
			System.out.println("This graph has " + headerRoots.size() + " roots: ");
			for (String root : headerRoots) {
				System.out.print(root + " ");
			}			
		}
		
		System.out.println("The IGraph uses the following labels:");
		for (String label : header.getLabels()) {
			System.out.println("\t" + label);
		}
	}


	/** Prints all the syntax tree roots present in an IGraph. The method
	 *  should be used on an IGraph that only contains a syntax annotation
	 *  level (e.g. "f.ptb").
	 *  
	 *  Example output:
	 *  f.ptb has these sentence roots: 
	 *	    ptb-n00313 S
	 *		ptb-n00030 S
	 *		ptb-n00018 FRAG
	 *		ptb-n00371 S
	 *		ptb-n00001 NP
	 *  
	 *  FIXME: write bug report about IGraph.getRoots() */
	public static void printSyntaxTreeRoots(IGraph syntaxIGraph) {
		List<INode> syntacticTreeRoots = GrafReader.getSyntaxTreeRootsINodes(syntaxIGraph);
		System.out.println("The IGraph has these syntactic sentence roots: ");
		for (INode syntaxTreeRoot : syntacticTreeRoots) {
			System.out.println("\t" + syntaxTreeRoot.getId() + " " 
					+ syntaxTreeRoot.getAnnotation().getLabel());
		}
	}


}
