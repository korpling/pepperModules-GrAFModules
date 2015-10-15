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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.ILink;
import org.xces.graf.api.INode;
import org.xces.graf.api.IRegion;
import org.xces.graf.impl.DefaultImplementation;


/**
 * @author Arne Neumann
 * @version 1.0
 *
 */
@Component(name="GrAFImporterComponent", factory="PepperImporterComponentFactory")
public class GrAFImporter extends PepperImporterImpl implements PepperImporter
{
	public static final String MODULE_NAME="GrAFImporter";
	
	public GrAFImporter()
	{
		super();
		//setting name of module
		setName("GrAFImporter");
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-GrAFModules"));
		setDesc("This importer transforms data in the GrAF format to a Salt model. ");

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
		String corpusPath= this.getCorpusDesc().getCorpusPath().toFileString();
		try
		{
			List<String> docHeaderPaths = recursiveListDir(corpusPath, ((GrAFImporterProperties)this.getProperties()).getHeaderFileEnding());;
			
			List<String> docIds = GrafReader.getDocumentIds(docHeaderPaths);
			docIdDocHeaderMap = createDocIdDocHeaderMap(docHeaderPaths);
			
			// generate a corpus (incl. subcorpora) and add documents to them.
			// right now these documents only contain an Name string
			SCorpus corpus = SaltFactory.createSCorpus();
			corpus.setName("MASC_labels_not_namespaces");
			corpusGraph.addNode(corpus); // add corpus to corpus graph
			
			for (String docId: docIds) {
				SDocument sDoc = SaltFactory.createSDocument();
				sDoc.setName(docId);
				corpusGraph.addDocument(corpus, sDoc);
			}		
			
			rscHeader = new GrafResourceHeader(corpusPath);
		}catch (Exception e)
		{
			throw new PepperModuleException(this, "Cannot import corpus at location '"+corpusPath+"'. ", e);
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
	 * 	@param Identifier - the id value for the current document or corpus to process,
	 *  	e.g. IdentifierImpl@76fe15f1 (namespace: graph, name: id, value: salt:/MASC_labels_not_namespaces/MASC1-00030)
	 */
	@Override
	public void start(Identifier Identifier) throws PepperModuleException 
	{
		if (	(Identifier!= null) &&
				(Identifier.getIdentifiableElement()!= null) &&
				((Identifier.getIdentifiableElement() instanceof SDocument) ||
				((Identifier.getIdentifiableElement() instanceof SCorpus))))
		{//only if given Identifier belongs to an object of type SDocument or SCorpus	
			if (Identifier.getIdentifiableElement() instanceof SDocument)
			{
				
				try {
					SDocument sDocument= (SDocument)Identifier.getIdentifiableElement();
					// add new document graph to SDocument
					sDocument.setDocumentGraph(SaltFactory.createSDocumentGraph());

					String sDocName = sDocument.getName();
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
					SaltWriter.addSyntaxToSDocument(fixedIGraph,
											iNodeIdToSNodeIdsMap, 
											sNodeIdToSNodeMap, 
											sDocument);
				}
				
				
				catch (Exception e) {
					throw new PepperModuleException(this, "Cannot import SDocument '"+Identifier+"' ",e);
				}
			}
		}//only if given Identifier belongs to an object of type SDocument or SCorpus
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
