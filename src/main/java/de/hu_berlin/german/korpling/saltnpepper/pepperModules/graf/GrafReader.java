package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

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

/** 
 * These annotation types ("f.ids") are used in the MASC 3.0.0 corpus: 
 * "f.logical", "f.s", "f.fn", "f.fntok", "f.nc", "f.penn", "f.ptb", "f.ptbtok",
 * "f.seg", "f.vc",  "f.cb", "f.event", "f.mpqa", "f.ne"
 * @author Arne Neumann */
public class GrafReader {
	
	/** returns an IGraph that includes ALL annotations made to a document. */
	public static IGraph getAnnoGraph(ResourceHeader rscHeader,
									  String docHeaderPath) 
				   throws GrafException, SAXException, IOException {
				
		GrafLoader loader = new GrafLoader(rscHeader);
		return loader.load(new File(docHeaderPath));
	}	
	
	
	/** returns an IGraph that includes the chosen annotations made to a document 
	 */
	public static IGraph getAnnoGraph(ResourceHeader rscHeader,
									  String docHeaderPath,
									  String[] annoTypes) 
				   throws GrafException, SAXException, IOException {

		GrafDocumentHeader mascDocHeader = new GrafDocumentHeader(docHeaderPath);
		List<String> annoTypesList = Arrays.asList(annoTypes); // avoid iterating over String[]
		List<String> existingAnnotations = mascDocHeader.getAnnotationTypes(); 
		
		if (existingAnnotations.containsAll(annoTypesList)) {
			GrafLoader loader = new GrafLoader(rscHeader);
			loader.setTypes(annoTypes);	// loads ALL annotations if not specified!	
			return loader.load(new File(docHeaderPath));								
		}
		else throw new NullPointerException("The document was not annotated with the "
											+ "chosen annotation type(s).");
	}

	/** returns an IGraph that includes only ONE annotation level */ 
	public static IGraph getAnnoGraph(ResourceHeader rscHeader,
									  String docHeaderPath,
									  String annoType) 
				  throws GrafException, SAXException, IOException {

		GrafDocumentHeader mascDocHeader = new GrafDocumentHeader(docHeaderPath);
		List<String> existingAnnotations = mascDocHeader.getAnnotationTypes(); 

		String[] annoTypes = new String[] {annoType};

		if (existingAnnotations.contains(annoTypes[0]))
			return getAnnoGraph(rscHeader, docHeaderPath, annoTypes);
		else throw new NullPointerException("The document was not annotated with the "
											+ "chosen annotation type.");
	}

	
	/** Inefficient method to retrieve a list of all the annotation types used
	 *  in an IGraph.
	 *  Use MascDocumentHeader.getDocumentAnnotationTypes() instead, if you just 
	 *  want to know which annotation types are specified in the document 
	 *  header. Use GrafResourceHeader.getCorpusAnnotationTypes() to retrieve
	 *  all the annotation types used in the corpus. */
	public static HashSet<String> getGraphAnnotationTypes(IGraph iGraph) {
		Collection<IRegion> regions = iGraph.getRegions();
		HashSet<String> annoTypes = new HashSet<String>();
		
		for (IRegion region : regions) {
			String regionId = region.getId();
			String annotationType = GrafReader.convertElementIdToAnnotationType(regionId);
			if (!annoTypes.contains(annotationType)) {
				annoTypes.add(annotationType);
			}
		}
		return annoTypes; 
	}
	
	
//	/** returns a list of absolute paths to all document header files in a
//	 *  corpus, which each contain meta information about a document. */
//	public static List<String> getDocumentHeaderPaths(String corpusPath) {
//		return Utils.recursiveListDir(corpusPath, ".hdr");
//	}
	
//	/** returns a list of absolute paths to all document header files in a
//	 *  corpus, which each contain meta information about a document. 
//	 *  @param corpusPath - absolute path to the corpus directory 
//	 *  @param fileExtension - file extension used by the header files, e.g. ".hdr" or ".anc" */
//	public static List<String> getDocumentHeaderPaths(String corpusPath, String fileExtension) {
//		return Utils.recursiveListDir(corpusPath, fileExtension);
//	}
	

	/** returns the IDs of all the documents in a corpus, e.g. MASC2-0130 */
	public static List<String> getDocumentIds(List<String> docHeaderPaths) 
				  throws FileNotFoundException, XPathExpressionException {
		List<String> docIds = new ArrayList<String>();
		for (String docHeaderPath: docHeaderPaths) {
			GrafDocumentHeader header = new GrafDocumentHeader(docHeaderPath);
			String docId = header.getDocumentId();
			docIds.add(docId);
		}
		return docIds;
	}
		
	
	/* prints the primary text that is described / annotated by an IGraph object*/
	public static String getDocumentText(IGraph graph) {
		return graph.getContent().toString(); 
		//.getContent() doesn't return a string, because the graph could also annotate some
		// other type of media
	}

	
	/** returns the number of documents annotated with a given annotation type, 
	 *  e.g. 'f.ptbtok' or 'f.seg' */
	public static int getDocumentsWithAnnoTypeCount(List<String> docHeaderPaths, 
												     String annoType) 
					   throws FileNotFoundException, GrafException {

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
	

	/** Takes an annotation type string (e.g. 'f.penn' or 'f.seg') and translates it
	 *  into the corresponding element prefix (e.g. 'penn-' and 'seg-'). 
	 *  
	 *  This method is used to filter regions, nodes etc. by annotation type. */
	public static String convertAnnoTypeToElementIdPrefix(String annotationType) {
		String annoTypeSuffix = annotationType.substring(2); // "f.seg" --> "seg"
		return annoTypeSuffix + "-";
	}


	/** Takes an element ID string (e.g. 'seg-r023' or 'ptb-n00136') and returns 
	 *  the corresponding annotation type, e.g. 'f.seg' or 'f.ptb'.
	 *  
	 *   This method is used to find out, which annotation level/type an 
	 *   element (region, node etc.) belongs to. */
	public static String convertElementIdToAnnotationType(String regionId) {
		String[] splitRegionId = regionId.split("-");
		return "f." + splitRegionId[0];
	}			


	/** returns a list of INodes that belong to a certain annotation layer.
	 *  @param annoType - e.g. "f.seg", will return all nodes whose ID begin with "seg-" */
	public static List<INode> getNodesOfAnnoType(IGraph iGraph, String annoType) throws GrafException {
		String nodeIdPrefix = GrafReader.convertAnnoTypeToElementIdPrefix(annoType);
		ArrayList<INode> filteredNodes = new ArrayList<INode>();
		
		for (INode node : iGraph.getNodes()) {
			if (node.getId().startsWith(nodeIdPrefix)) {
				filteredNodes.add(node);
			}
		}
		return filteredNodes;
	}	
	
	/** returns a list of all INodes that the given INode is connected to 
	 *  (via outgoing edges). */
	public static List<INode> getOutboundConnectedNodes(INode node) {
		List<IEdge> outEdges = node.getOutEdges();
		List<INode> connectedNodes = new ArrayList<INode>();
		for (IEdge outEdge : outEdges) {
			connectedNodes.add(outEdge.getTo());
		}
		return connectedNodes;
	}
	
	/** returns a portion of the primary text (limited by a string onset/offset)
	 *  that is linked to an IGraph */
	public static String getPrimaryTextSequence(int start, int end, IGraph iGraph) {
		String primaryText = getDocumentText(iGraph);
		return primaryText.substring(start, end);		
	}


	/** returns the portion of the primary text that is annotated/described by an IRegion */
	public static String getPrimaryTextSequence(IRegion iRegion, IGraph iGraph) throws GrafException {
		String primaryText = getDocumentText(iGraph);
		int[] regionOffsets = GrafReader.getRegionOffsets(iRegion);
		return primaryText.substring(regionOffsets[0], regionOffsets[1]);		
	}


	/** returns the portion of the primary text that is annotated by the 
	 *  IRegion(s) targeted by an ILink. WARNING: A link can target more 
	 *  than one region. */
	public static String getPrimaryTextSequence(ILink iLink, IGraph iGraph) throws GrafException {
		int[] linkOffsets = GrafReader.getLinkOffsets(iLink);
		return getPrimaryTextSequence(linkOffsets[0], linkOffsets[1], iGraph);
	}


	/** returns the portion of the primary text that is annotated/described by an INode */
	public static String getPrimaryTextSequence(INode iNode, IGraph iGraph) throws GrafException {
		int[] nodeOffsets = GrafReader.getNodeOffsets(iNode);
		return getPrimaryTextSequence(nodeOffsets[0], nodeOffsets[1], iGraph);
	}	

	/** returns the IRegions belonging to a given annotation type */
	public static List<IRegion> getRegionsOfAnnoType(IGraph iGraph, String annoType) throws GrafException {
		String[] annoTypes = new String[] {annoType};
		return getRegionsOfAnnoTypes(iGraph, annoTypes);
	}
	
	/** returns the IRegions belonging to one of the given annotation types */
	public static List<IRegion> getRegionsOfAnnoTypes(IGraph iGraph, String[] annoTypes) throws GrafException {

		List<String> regionIdPrefixes = new ArrayList<String>();
		for (String annoType: annoTypes) {
			String regionIdPrefix = GrafReader.convertAnnoTypeToElementIdPrefix(annoType);
			regionIdPrefixes.add(regionIdPrefix);
		}
		
		Collection<IRegion> iGraphRegions = iGraph.getRegions();
		List<IRegion> filteredRegions = new ArrayList<IRegion>();
		for (IRegion region : iGraphRegions) {
			String regionId = region.getId();
			for (String regionIdPrefix : regionIdPrefixes) {
				if (regionId.startsWith(regionIdPrefix)) {
					filteredRegions.add(region);	
				}
			}
		}
		return filteredRegions;
	}		
	
	
	/** returns an int array, which contains the string onset and 
	 *  string offset of an IRegion */
	public static int[] getRegionOffsets(IRegion region) throws GrafException {
		// every region has 2 anchors (char onset and char offset)
		// they are stored as Long inside an Object instead of integers.
		// we need integers for the String.substring() method.
		int regionStart = GraphUtils.getOffset(region.getStart());
		int regionEnd = GraphUtils.getOffset(region.getEnd());
		return new int[] {regionStart, regionEnd};		
	}


	/** returns an int array, which contains the string onset and 
	 *  string offset of an ILink. An ILink usually targets one IRegion 
	 *  (primary text segment), but may target more than one IRegion for 
	 *  tokenization purposes (e.g. "state-of-the-art" is represented as 
	 *  7 consecutive IRegions in GrAF).*/	
	public static int[] getLinkOffsets(ILink link) throws GrafException {
		List<IRegion> iRegions = link.getRegions();
		if (iRegions.size() == 1) {
			IRegion iRegion = iRegions.get(0);
			return getRegionOffsets(iRegion);
		}
		else { // if a link targets more than one region, find out where the 
			   // earliest region (lowest onset in primary text) starts and 
			   // where the latest one (highest offset in primary text) ends.
			int lowestStartOffset = Integer.MAX_VALUE;
			int highestEndOffset = Integer.MIN_VALUE;
	
			List<IRegion> regions = link.getRegions();
			for (IRegion region: regions) {
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
			return new int[] {lowestStartOffset, highestEndOffset};					
		}
	}


	/** returns an int array, which contains the string onset and 
	 *  string offset of an INode. An INode can either have one ILink 
	 *  (targeting one or more IRegions) OR has one or more outgoing IEdges to
	 *  other INodes.  
	 *  */
	public static int[] getNodeOffsets(INode node) throws GrafException {
		List<ILink> nodeLinks = node.getLinks();
		if (nodeLinks.isEmpty() == false) {
			// if a node has a link, it targets IRegions 
			// ("f.seg", i.e. basic tokenization of primary text into Regions)
			ILink iLink = nodeLinks.get(0);
			return getLinkOffsets(iLink);
		}			
		else {
			List<IEdge> outEdges = node.getOutEdges();
			if (outEdges.isEmpty()) {
				return new int[] {0, 0};
				// FIXME: maybe this should return null
				//
				// node is not connected to other nodes. 
				// onset/offset 0 translates into an empty substring when 
				// calling getPrimaryTextSequence(0, 0, graph)
			}
	
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
			return new int[] {lowestStartOffset, highestEndOffset};				
		}
	}	
	
	/** FIXME: IGraph.getRoots() is broken. This is terribly inefficient workaround. */
	public static List<INode> getSyntaxTreeRootsINodes(IGraph syntaxGraph) {
		Collection<INode> syntaxNodes = syntaxGraph.getNodes();
		List<INode> syntaxRootNodes = new ArrayList<INode>();
		for (INode syntaxNode : syntaxNodes) {
			List<IEdge> inEdges = syntaxNode.getInEdges();
			for (IEdge inEdge : inEdges) {
				INode fromNode = inEdge.getFrom();
				if (fromNode.getInEdges().isEmpty()) {
					syntaxRootNodes.add(syntaxNode);
				}
			}
		}		
		return syntaxRootNodes;
	}
	
//	public static void main(String[] args) throws IOException, GrafException, SAXException {
//		String corpusPath = Utils.getVariableFromYamlFile("corpusPath");
////		GrafResourceHeader rscHeader = new GrafResourceHeader(corpusPath);
////		printResourceHeaderInfo(rscHeader);
//		
//		List<String> docHeaderPaths = getDocumentHeaderPaths(corpusPath);
//		
//		List<String> tokenizedPOSTaggedDocHeaders = GrafToSaltConverter.getTokenizedPOSTaggedDocHeaders(docHeaderPaths);
//		System.out.println(tokenizedPOSTaggedDocHeaders.size());
//		for (String docHeader : tokenizedPOSTaggedDocHeaders) {
//			System.out.println(docHeader);
//		}
//	}

}


	

//DotRenderer dot = new DotRenderer(outFilePath);
//dot.render(graph);
//dot.close();

// print XML representation of graph to System.out
//GrafRenderer graf = new GrafRenderer(System.out);
//graf.render(graph);
