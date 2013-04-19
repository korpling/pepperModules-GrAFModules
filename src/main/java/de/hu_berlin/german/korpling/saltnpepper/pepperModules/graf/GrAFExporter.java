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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperFWException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperExporter;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperExporterImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf.exceptions.GrAFExporterException;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.exceptions.GraphException;
//import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.resources.graf.v01.GrAFResourceFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpus;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;

/**
 * This is a sample PepperExporter, which can be used for creating individual Exporters for the 
 * Pepper Framework. Therefore you have to take a look to todo's and adapt the code.
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
@Component(name="GrAFExporterComponent", factory="PepperExporterComponentFactory")
public class GrAFExporter extends PepperExporterImpl implements PepperExporter
{
	public GrAFExporter()
	{
		super();
		//setting name of module
		this.name= "GrAFExporter";
		//set list of formats supported by this module
		this.addSupportedFormat("GrAF", "1.0", null);
	}
	
	//===================================== start: thread number
	/**
	 * Defines the number of processes which can maximal work in parallel for importing documents.
	 * Means the number of parallel imported documents. Default value is 5.
	 */
	private Integer numOfParallelDocuments= 5;
	/**
	 * Sets the number of processes which can maximal work in parallel for importing documents.
	 * Means the number of parallel imported documents.
	 * @param numOfParallelDocuments the numOfParallelDocuments to set
	 */
	public void setNumOfParallelDocuments(Integer numOfParallelDocuments) {
		this.numOfParallelDocuments = numOfParallelDocuments;
	}

	/**
	 * Returns the number of processes which can maximal work in parallel for importing documents.
	 * Means the number of parallel imported documents.
	 * @return the numOfParallelDocuments
	 */
	public Integer getNumOfParallelDocuments() {
		return numOfParallelDocuments;
	}	
	
	public static final String PROP_NUM_OF_PARALLEL_DOCUMENTS="paulaImporter.numOfParallelDocuments";
//===================================== start: thread number
	/**
	 * a property representation of a property file
	 */
	protected Properties props= null;
	
	/**
	 * Extracts properties out of given special parameters.
	 */
	private void exctractProperties()
	{
		if (this.getSpecialParams()!= null)
		{//check if flag for running in parallel is set
			File propFile= new File(this.getSpecialParams().toFileString());
			this.props= new Properties();
			try{
				this.props.load(new FileInputStream(propFile));
			}catch (Exception e)
			{throw new GrAFExporterException("Cannot find input file for properties: "+propFile+"\n nested exception: "+ e.getMessage());}
			if (this.props.containsKey(PROP_NUM_OF_PARALLEL_DOCUMENTS))
			{
				try {
					Integer val= new Integer(this.props.getProperty(PROP_NUM_OF_PARALLEL_DOCUMENTS));
					if (val > 0)
						this.setNumOfParallelDocuments(val);
				} catch (Exception e) 
				{
					if (this.getLogService()!= null)
						this.getLogService().log(LogService.LOG_WARNING, "Cannot set correct property value of property "+PROP_NUM_OF_PARALLEL_DOCUMENTS+" to "+this.getName()+", because of the value is not castable to Integer. A correct value must be a positiv, whole number (>0).");
				}
			}
		}//check if flag for running in parallel is set
	}
	
	/**
	 * The name of the file ending for the format GrAF.
	 */
	private final static String GRAF_FILE_ENDING= "graf";
	
	/**
	 * Returns the ending of the format saltXML.
	 * @return
	 */
	public static String getGrAFFileEnding()
	{
		return(GRAF_FILE_ENDING);
	}
	
	/**
	 * Stores the resource for salt project
	 */
	private XMLResource saltProjectResource= null;
	
	/**
	 * The resource set for all resources.
	 */
	private ResourceSet resourceSet= null;
	
	/**
	 * This method creates rersources for the saltProject and every document and will attach 
	 * them to the resource.
	 */
	private void createGrAFProjectResource()
	{
		if (this.getSaltProject()== null)
			throw new PepperModuleException("Cannot export the SaltProject, because the saltProject is null.");
		if (this.getCorpusDefinition()== null)
			throw new PepperModuleException("Cannot export the SaltProject, because no corpus definition is given for export.");
		if (this.getCorpusDefinition().getCorpusPath()== null)
			throw new PepperModuleException("Cannot export the SaltProject, because no corpus path is given for export.");
		
		//create export URI
		URI saltProjectURI= URI.createFileURI(this.getCorpusDefinition().getCorpusPath().toFileString() +"/"+ "saltProject"+"."+ getGrAFFileEnding());
		
		this.saltProjectResource= (XMLResource)resourceSet.createResource(saltProjectURI);
		this.saltProjectResource.getContents().add(this.getSaltProject());
		this.saltProjectResource.setEncoding("UTF-8");	
	}
	
	/**
	 * ThreadPool
	 */
	private ExecutorService executorService= null;
		
	/**
	 * Stores the corresponation between an SElementId belonging to an SDocument and
	 * a file-resource where it shall be written to.
	 */
	private Hashtable<SElementId, File> sDocumentId2ResourceTable= null;
	
	/**
	 * List of all used writers.
	 */
	private EList<SDocumentWriter> sDocumentWriters= null;
	
	/**
	 * If this method is not really implemented, it will call the Method start(sElementId) for every document 
	 * and corpus, which shall be processed. If it is not really implemented, the method-call will be serial and
	 * and not parallel. To implement a parallelization override this method and take care, that your code is
	 * thread-safe. 
	 * For getting an impression how to implement this method, here is a snipplet of super class 
	 * PepperExporter of this method:
	 * After all documents were processed this method of super class will call the method end().
	 */
	@Override
	public void start() throws PepperModuleException
	{
		{//extract property files
			this.exctractProperties();
		}//extract property files
		{//initialize ThreadPool
			this.executorService= Executors.newFixedThreadPool(this.getNumOfParallelDocuments());
		}//initialize ThreadPool
		
		File exportFile= null;
		{//check export URI if it is a folder
			exportFile= new File(this.getCorpusDefinition().getCorpusPath().toFileString());
			
			if (exportFile.exists())
				exportFile.mkdirs();
			if (!exportFile.isDirectory())
				throw new GraphException("Cannot export SaltProject, because the given uri does not belong to folder.");
		}//check export URI if it is a folder	
		{//create a resource for every SDocument
			this.sDocumentId2ResourceTable= new Hashtable<SElementId, File>();
			for (SCorpusGraph sCorpusGraph: this.getSaltProject().getSCorpusGraphs())
			{	
				for (SDocument sDocument: sCorpusGraph.getSDocuments())
				{
					StringBuffer resourceName= new StringBuffer();
					resourceName.append(exportFile.getAbsolutePath());
					resourceName.append("/");
					resourceName.append(sDocument.getSElementPath().devicePath());
					resourceName.append(".");
					resourceName.append(getGrAFFileEnding());
					File resourceFile= new File(resourceName.toString());
					try {
						resourceFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					resourceFile.getParentFile().mkdirs();
					this.sDocumentId2ResourceTable.put(sDocument.getSElementId(), resourceFile);
				}
			}
		}//create a resource for every SDocument
		
		//creating resources for saltProject
		this.createGrAFProjectResource();
		
		boolean isStart= true;
		SElementId sElementId= null;
		// stores number of active threads to export
		while ((isStart) || (sElementId!= null))
		{	
			isStart= false;
			sElementId= this.getPepperModuleController().get();
			if (sElementId== null)
				break;
			this.start(sElementId);	
		}
		{//exporting corpus structure
//			try {
//				saltProjectResource.save(null);
//			} catch (IOException e) {
//				throw new PepperModuleException("Cannot export saltProject, nested exception is: ", e);
//			}
		}//exporting corpus structure
		
		for (SDocumentWriter sDocWriter: this.sDocumentWriters)
		{//wait until all SDocumentWriters have been finished
			sDocWriter.waitUntilFinish();
		}//wait until all SDocumentWriters have been finished
		this.end();
	}
	
	/**
	 * This method is called by method start() of superclass PepperExporter, if the method was not overriden
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
			{//selementId belongs to SDocument	
				if (this.getNumOfParallelDocuments()> 1)
				{//run import in parallel	
					SDocumentWriter sDocumentWriter= new SDocumentWriter();
					sDocumentWriter.currSElementId= sElementId;
					sDocumentWriter.resource= this.sDocumentId2ResourceTable.get(sElementId);
					if (this.sDocumentWriters== null)
						this.sDocumentWriters= new BasicEList<SDocumentWriter>();
					this.sDocumentWriters.add(sDocumentWriter);
					//start export
					this.executorService.execute(sDocumentWriter);
				}//run import in parallel
			}//selementId belongs to SDocument
		}//only if given sElementId belongs to an object of type SDocument or SCorpus
	}
	
	/**
	 * This method is called by method start() of super class PepperModule. If you do not implement
	 * this method, it will call start(sElementId), for all super corpora in current SaltProject. The
	 * sElementId refers to one of the super corpora. 
	 */
	@Override
	public void end() throws PepperModuleException
	{
		super.end();
	}
	
	/**
	 * This class is only for threading.
	 * @author Florian Zipser
	 *
	 */
	private class SDocumentWriter implements Runnable  
	{
		private SElementId currSElementId= null;
		
		private File resource= null;

		/**
		 * Lock to lock await and signal methods.
		 */
		protected Lock lock= new ReentrantLock();
		
		/**
		 * Flag wich says, if mapperRunner has started and finished
		 */
		private Boolean isFinished= false;
		
		/**
		 * If condition is achieved a new SDocument can be created.
		 */
		private Condition finishCondition=lock.newCondition();
		
		public void waitUntilFinish()
		{
			lock.lock();
			try {
				if (!isFinished)
					finishCondition.await();
			} catch (InterruptedException e) {
				throw new PepperFWException(e.getMessage());
			}
			lock.unlock();
		}
		
		public void run() {
			this.start(currSElementId);
			
		}
		
		public void start(SElementId sElementId) throws PepperModuleException 
		{
//			if (	(sElementId!= null) &&
//					(sElementId.getSIdentifiableElement()!= null) &&
//					((sElementId.getSIdentifiableElement() instanceof SDocument) ||
//					((sElementId.getSIdentifiableElement() instanceof SCorpus))))
//			{//only if given sElementId belongs to an object of type SDocument or SCorpus	
//				if (sElementId.getSIdentifiableElement() instanceof SDocument)
//				{//export SDocument structure
//					SDocument sDocument= (SDocument) sElementId.getSIdentifiableElement();
//					if (sDocument.getSDocumentGraph()!= null)
//					{//only export, if a structure exists					
//						//creating uri for exporting document
//						URI sDocumentURI= URI.createFileURI(resource.getAbsolutePath());
//						
//						// Register XML resource factory
//						resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("graf", new GrAFResourceFactory());
//						Resource sDocumentResource = resourceSet.createResource(sDocumentURI);
//						sDocumentResource.getContents().add(sDocument.getSDocumentGraph());
//						
//						try 
//						{
//							sDocumentResource.save(null);
//							getPepperModuleController().put(currSElementId);
//						}catch (Exception e)
//						{
//							if (getLogService()!= null)
//							{
//								throw new PepperModuleException("Cannot export document '"+sElementId.getSElementPath()+"', nested exception is: ", e);
//							}
//							
////							//TODO delete this in delivery state
//							e.printStackTrace();
//							getPepperModuleController().finish(currSElementId);
//						}
//					}//only export, if a structure exists
//				}//export SDocument structure
//			}//only if given sElementId belongs to an object of type SDocument or SCorpus
//			this.lock.lock();
//			this.isFinished= true;
//			this.finishCondition.signal();
//			this.lock.unlock();
		}
	}


}
