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

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperImporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperImporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.exceptions.GrAFImporterException;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

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
	
	/**
	 * The list of all endings to be handled by this Importer.
	 */
	private EList<String> endings= null;
	
	/**
	 * Stores relation between documents and their resource 
	 */
	private Map<SElementId, URI> documentResourceTable= null;
	
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
	public static HashMap<String, Pair<String, String>> addGrafStructureToSDocument(IGraph iGraph, 
																					SDocument sDocument)
																					throws GrafException {
		HashMap<String, String> regionIdsToTokenIdsMap; 
		regionIdsToTokenIdsMap = SaltWriter.addAllIRegionsToSDocument(iGraph, 
													sDocument, 
													"WORD_SEGMENTATION_ONLY");
		return SaltWriter.addSSpansToSDocument(iGraph, sDocument, regionIdsToTokenIdsMap);
	}
	
	/**
	 * This method is called by method start() of superclass PepperImporter, if the method was not overriden
	 * by the current class. If this is not the case, this method will be called for every document which has
	 * to be processed.
	 * @param sElementId the id value for the current document or corpus to process  
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
					String docHeaderPath = docIdDocHeaderMap.get(sDocName);

					IGraph iGraph = GrafReader.getAnnoGraph(rscHeader, docHeaderPath);

					String primaryText = GrafReader.getDocumentText(iGraph);
					SaltWriter.addPrimaryTextToDocument(sDocument, primaryText);
					
					HashMap<String, Pair<String, String>> iNodeIDsToSTokenSSpanIdsMap;
					
					iNodeIDsToSTokenSSpanIdsMap = addGrafStructureToSDocument(iGraph, sDocument);
					
					Pair<HashMap<String, SToken>, HashMap<String, SSpan>> tokenAndSpanMaps; 
					tokenAndSpanMaps = SaltWriter.addAnnotationsToSDocument(iGraph, 
																iNodeIDsToSTokenSSpanIdsMap, 
																sDocument);
					
					
					
					// adds syntax structures from the IGraph's syntax annotation
					// level to SDocument
					IGraph syntaxIGraph = GrafReader.getAnnoGraph(rscHeader, docHeaderPath, ((GrAFImporterProperties)this.getProperties()).getSyntaxLayer());
					GrafGraphInfo.printSyntaxTreeRoots(syntaxIGraph);
					SaltWriter.addSyntaxToSDocument(syntaxIGraph, 
											iNodeIDsToSTokenSSpanIdsMap, 
											tokenAndSpanMaps, 
											sDocument);
					
				}			
				catch (Exception e) {
					new GrAFImporterException("Cannot import SDocument '"+sElementId+"' ",e);
				}
			}
		}//only if given sElementId belongs to an object of type SDocument or SCorpus
	}
}
