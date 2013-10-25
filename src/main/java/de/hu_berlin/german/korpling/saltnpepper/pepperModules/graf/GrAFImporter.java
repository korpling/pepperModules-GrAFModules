/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.osgi.service.component.annotations.Component;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.ILink;
import org.xces.graf.api.INode;
import org.xces.graf.api.IRegion;
import org.xces.graf.impl.DefaultImplementation;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.exceptions.GrAFImporterException;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;

/**
 * This is a sample PepperImporter, which can be used for creating individual Importers for the 
 * Pepper Framework. Therefor you have to take a look to todo's and adapt the code.
 * 
 * <ul>
 *  <li>the salt model to fill, manipulate or export can be accessed via SaltProject::this.getSaltProject()</li>
 * 	<li>special parameters given by Pepper workflow can be accessed via URI::this.getSpecialParams()</li>
 *  <li>a place to store temprorary datas for processing can be accessed via URI::this.getTemproraries()</li>
 *  <li>a place where resources of this bundle are, can be accessed via URL::this.getResources()</li>
 *  <li>a logService can be accessed via LogService::this.getLogService()</li>
 * </ul>
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name="GrAFImporterComponent", factory="PepperImporterComponentFactory")
public class GrAFImporter extends PepperImporterImpl implements PepperImporter
{
	public GrAFImporter()
	{
		super();
		//setting name of module
		this.name= "GrAFImporter";
		//set list of formats supported by this module
		this.addSupportedFormat("GrAF", "1.0", null);
		this.setProperties(new GrAFImporterProperties());
	}
		
	/** returns a list of document header file paths, which belong to documents
	 *  that are segmented and POS tagged. */
	public List<String> getTokenizedPOSTaggedDocHeaders(List<String> documentHeaderPaths) 
				  throws FileNotFoundException, GrafException {
		List<String> desiredDocHeadersPaths = new ArrayList<String>();
		for (String docHeaderPath : documentHeaderPaths) {
			GrafDocumentHeader docHeader = new GrafDocumentHeader(docHeaderPath);
			List<String> annoTypes = docHeader.getAnnotationTypes();
			if (	(annoTypes.contains(((GrAFImporterProperties) this.getProperties()).getTokenizationLayer()))||
					(annoTypes.contains(((GrAFImporterProperties) this.getProperties()).getPOSLayer()))) 
			{
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
	
	Map<String, String> docIdDocHeaderMap= null;
	private GrafResourceHeader rscHeader= null;
	
	/** returns a list of paths to all files with the given extension that
	 *  exist in a directory (incl. subdirectories) */
	public static List<String> recursiveListDir(String path, String fileNameEndsWith){
		List<String> fnamesList = recursiveListDir(path);
		List<String> filteredFnamesList = new ArrayList<String>();
		for (String fname : fnamesList) {
			if (fname.endsWith(fileNameEndsWith)) {
				filteredFnamesList.add(fname);
			}
		}
		return filteredFnamesList;
	}	
	
	/** returns a list of paths to all files that
	 *  exist in a directory (incl. subdirectories) */
	public static List<String> recursiveListDir(String path){
		  List<String> fnamesList = new ArrayList<String>();	   
		  File[] faFiles = new File(path).listFiles();
		  
		  for(File file: faFiles){
		    if(file.isDirectory()){
		    	List<String> tmpFnames = recursiveListDir(file.getAbsolutePath());
		    	for (String fname: tmpFnames) {
		    		fnamesList.add(fname);
		    	}
		    }		
		    else {
		      String absPath = file.getAbsolutePath();
		      String correctedPath = absPath.replace("\\", "/"); // Really necessary?
		      fnamesList.add(correctedPath);
		    }
		  }
		  return fnamesList;
	}
		
	/**
	 * This method is called by Pepper at the start of conversion process. 
	 * It shall create the structure the corpus to import. That means creating all necessary SCorpus, 
	 * SDocument and all Relation-objects between them. The path tp the corpus to import is given by
	 * this.getCorpusDefinition().getCorpusPath().
	 * @param an empty graph given by Pepper, which shall contains the corpus structure
	 */
	@Override
	public void importCorpusStructure(SCorpusGraph corpusGraph)
			throws PepperModuleException 
	{
		String corpusPath= this.getCorpusDefinition().getCorpusPath().toFileString();
		try
		{
			List<String> docHeaderPaths = recursiveListDir(corpusPath, ((GrAFImporterProperties)this.getProperties()).getHeaderFileEnding());;
			
			List<String> docIds = GrafReader.getDocumentIds(docHeaderPaths);
			docIdDocHeaderMap = createDocIdDocHeaderMap(docHeaderPaths);
			
			// generate a corpus (incl. subcorpora) and add documents to them.
			// right now these documents only contain an SName string
			SCorpus corpus = SaltFactory.eINSTANCE.createSCorpus();
			corpus.setSName("MASC_labels_not_namespaces");
			corpusGraph.addSNode(corpus); // add corpus to corpus graph
			
			for (String docId: docIds) {
				SDocument sDoc = SaltFactory.eINSTANCE.createSDocument();
				sDoc.setSName(docId);
				corpusGraph.addSDocument(corpus, sDoc);
			}		
			
			rscHeader = new GrafResourceHeader(corpusPath);
		}catch (Exception e)
		{
			throw new GrAFImporterException("Cannot import corpus at location '"+corpusPath+"'. ", e);
		}
		
	}
	
	
	/** Adds all IRegions from an IGraph to an SDocument and returns a map
	 *  from INode IDs to SNode (or: SToken/SSpan IDs).
	 *  
	 *  First, the method generates a mapping from IRegions to STokens and 
	 *  uses it to build a map from INode IDs to SToken IDs 
	 *  (if the INode only covers one IRegion) OR from INode IDs to SSpan IDs 
	 *  (if the INode covers more than one IRegion).
	 *  
	 *  An SSpan represents a number of consecutive STokens. In GrAF terminology
	 *  an SSpan is equivalent to an INode that links to more than one IRegion 
	 *  or an INode that is connected via one or more outgoing edges to INodes 
	 *  that do so.
	 *  
	 *  @return a map from INode IDs to a SNode (here: SToken/SSpan) IDs */
	public static HashMap<String, List<String>> addGrafStructureToSDocument(IGraph iGraph, 
																					SDocument sDocument)
																					throws GrafException {
		HashMap<String, List<String>> regionIdToTokenIdsMap; 
		regionIdToTokenIdsMap = SaltWriter.addAllIRegionsToSDocument(iGraph, 
													sDocument);
		return SaltWriter.addSSpansToSDocument(iGraph, sDocument, regionIdToTokenIdsMap);
	}
	
	/**
	 * 	This is the "main" method of the GrafModules. It converts one IGraph to
	 *  an SDocument(Graph).
	 * 
	 * 	This method is called by method start() of superclass PepperImporter, if the method was not overriden
	 * 	by the current class. If this is not the case, this method will be called for every document which has
	 * 	to be processed.
	 * 	@param sElementId - the id value for the current document or corpus to process,
	 *  	e.g. SElementIdImpl@76fe15f1 (namespace: graph, name: id, value: salt:/MASC_labels_not_namespaces/MASC1-00030)
	 */
	@Override
	public void start(SElementId sElementId) throws PepperModuleException 
	{
		if (	(sElementId!= null) &&
				(sElementId.getSIdentifiableElement()!= null) &&
				((sElementId.getSIdentifiableElement() instanceof SDocument) ||
				((sElementId.getSIdentifiableElement() instanceof SCorpus))))
		{//only if given sElementId belongs to an object of type SDocument or SCorpus	
			if (sElementId.getSIdentifiableElement() instanceof SDocument)
			{
				
				try {
					SDocument sDocument= (SDocument)sElementId.getSIdentifiableElement();
					// add new document graph to SDocument
					sDocument.setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());

					String sDocName = sDocument.getSName();
					System.out.println("filling SDocument "+sDocName+" ...");
					String docHeaderPath = docIdDocHeaderMap.get(sDocName);

					IGraph iGraph = GrafReader.getAnnoGraph(rscHeader, docHeaderPath);
					IGraph fixedIGraph = repairFloatingNodes(iGraph);

					String primaryText = GrafReader.getDocumentText(iGraph);
					SaltWriter.addPrimaryTextToDocument(sDocument, primaryText);
					
					HashMap<String, List<String>> iNodeIdToSNodeIdsMap = addGrafStructureToSDocument(fixedIGraph, sDocument);
					HashMap<String, SNode> sNodeIdToSNodeMap = SaltWriter.addAnnotationsToSDocument(fixedIGraph, 
																	iNodeIdToSNodeIdsMap, 
																	sDocument);
					
//					GrafGraphInfo.printSyntaxTreeRoots(fixedIGraph);
					SaltWriter.addSyntaxToSDocument(fixedIGraph,
											iNodeIdToSNodeIdsMap, 
											sNodeIdToSNodeMap, 
											sDocument);
										

				
					// TODO: test if all SNodes cover some primary text
					// cannot map SStructuredNode object 'salt:/MASC_labels_
					// not_namespaces/MASC1-00030/MASC1-00030_graph#ptb-n00229' to ra-node, 
					// because it does not overlap a text.
//					SDocumentGraph sDocumentGraph = sDocument.getSDocumentGraph();
//					// convert EList to List and sort it
//					List<SNode> sNodes = new ArrayList<SNode>();
//					for (SNode node : sDocumentGraph.getSNodes()) { sNodes.add(node); }
//					Collections.sort(sNodes, new SaltElementSortByID());
//									
//					for (SNode sNode : sNodes) {
//						String nodePrimaryText = SaltReader.getPrimaryTextSequence(sNode, sDocumentGraph);
//						System.out.println("SNode ID: "+sNode.getId()+" covers this primary text sequence:");
//						System.out.println("\t"+nodePrimaryText);
//					}
				}
				
				
				catch (Exception e) {
					throw new GrAFImporterException("Cannot import SDocument '"+sElementId+"' ",e);
				}
			}
		}//only if given sElementId belongs to an object of type SDocument or SCorpus
	}

	/** find floating nodes in an IGraph and link them to a fake region
	 *  that covers an empty segment of primary text located between
	 *  the preceding and succeeding segments of primary text.
	 * @throws GrafException */
	public static IGraph repairFloatingNodes(IGraph iGraph) throws GrafException {
		DefaultImplementation grafFactory = new DefaultImplementation();

		int floatingNodeCount = 0;
		for (INode iNode : iGraph.getNodes()) {
			if (GrafReader.isFloatingNode(iNode)) {
				int[] floatingNodeOffsets = GrafReader.getFloatingNodeOffsets(iGraph, iNode);
				IRegion emptyRegion = grafFactory.newRegion("seg-fake"+floatingNodeCount,
													Long.valueOf(floatingNodeOffsets[0]),
													Long.valueOf(floatingNodeOffsets[1]));
				ILink linkToEmptyRegion = grafFactory.newLink();
				linkToEmptyRegion.addTarget(emptyRegion);
				iNode.addLink(linkToEmptyRegion);
				iGraph.addRegion(emptyRegion);
				floatingNodeCount++;
			}
		}
		return iGraph;
	}
}
