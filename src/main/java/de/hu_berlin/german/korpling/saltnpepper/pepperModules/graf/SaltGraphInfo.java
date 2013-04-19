package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import org.eclipse.emf.common.util.EList;

import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Label;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Layer;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Node;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SCorpusGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SStructure;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SFeature;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;

/** This class contains some methods for playing around with Salt Graph 
 *  objects. It should be useful for comparing GraF (cf. GrafGraphInfo.java) 
 *  and Salt. */
public class SaltGraphInfo {

	/** prints the name, qualified name, namespace and value of a Label object.*/
	public static void printLabelInfo(Label label) {
		System.out.println("label name: " + label.getName() + " qualified name: " + label.getQName()); 
		System.out.println("label namespace: " + label.getNamespace());
		System.out.println("label value: " + label.getValueString());
	}
	
	/** prints the ID of a Layer (!= SLayer) object.
	 *  prints the number of nodes, edges and labels it has. 
	 * 
	 * FIXME: what's the difference between Layer and SLayer 
	 *  (not conceptually, but content-wise) ??? */
	public static void printLayerInfo(Layer layer) {
		System.out.println("layer id: " + layer.getId());
		System.out.println("\tnodes: " + layer.getNodes().size()
						 + " edges: " + layer.getEdges().size()
						 + " labels: " + layer.getLabels().size());
	}	

	/** prints the ID of a Node (!= SNode) and its number of labels and layers.
	 *  calls printLabelInfo for each label and printLayerInfo for each layer.
	 * FIXME: do not confuse Node with SNode! Nodes occur in the SDocumentGraph.*/
	public static void printNodeInfo(Node node) {
		System.out.println("node id: " + node.getId());
		
		EList<Label> nodeLabels = node.getLabels();
		System.out.println("\tnode has " + nodeLabels.size() + " labels");
		for (Label label : nodeLabels) {
			printLabelInfo(label);
		}
		
		EList<Layer> nodeLayers = node.getLayers();
		System.out.println("\tnode has " + nodeLayers.size() + " layers");
		for (Layer layer : nodeLayers) {
			printLayerInfo(layer);
		}
	}
	


	/** FIXME: write meaningful docstring */
	public static void printSaltProjectInfo(SaltProject saltProject) {
		// there's only one SCorpusGraph, but we got to retrieve it from a list
		SCorpusGraph sCorpusGraph = saltProject.getSCorpusGraphs().get(0);
		//print all corpora.
		System.out.println("all corpora: "+ sCorpusGraph.getSCorpora());
		//print all documents
		System.out.println("all documents: "+ sCorpusGraph.getSDocuments());

		//for all documents print some properties
		for (SDocument sDocument: sCorpusGraph.getSDocuments()) {
			SDocumentGraph sDocumentGraph = sDocument.getSDocumentGraph();
			System.out.println("============ "+ sDocument.getSName()+ " ============");
			System.out.println("root nodes\t: "+ sDocumentGraph.getSRoots());
			
			//print some information for tokens
			for (SToken sToken: sDocumentGraph.getSTokens()) {
				System.out.println(SaltReader.getPrimaryTextSequence(sToken, sDocumentGraph));
			}
		
			System.out.println("----------------------------------------");
			
			//print some information for spans
			for (SSpan sSpan: sDocumentGraph.getSSpans()) {
				System.out.println(SaltReader.getPrimaryTextSequence(sSpan, sDocumentGraph));
			}
			
			System.out.println("----------------------------------------");
			
			//print some information for structs
			for (SStructure sStruct: sDocumentGraph.getSStructures()) {
				System.out.println(SaltReader.getPrimaryTextSequence(sStruct, sDocumentGraph));
			}

			System.out.println("================================================");
		}
	}	
	
	
	/** prints the name, qualified name, namespace and value of an SAnnotation.
	 *  calls printLabelInfo for further information, if the annotation has labels. */
	public static void printSAnnotationInfo(SAnnotation sAnnotation) {
		System.out.println("sAnnotation name: " + sAnnotation.getName() 
						 + "qualified name: " + sAnnotation.getQName());
		System.out.println("SAnnotation namespace: " + sAnnotation.getNamespace());
		System.out.println("SAnnotation value: " + sAnnotation.getSValueSTEXT());
		EList<Label> sAnnotationLabels = sAnnotation.getLabels();
		for (Label label : sAnnotationLabels) {
			printLabelInfo(label);
		}
	}
	
	
	/** prints the name and ID of an SCorpusGraph.
	 *  prints the name and ID of every document in the corpus graph. */
	public static void printSCorpusGraphInfo(SCorpusGraph sCorpusGraph) {
		System.out.println("corpus graph id: " + sCorpusGraph.getId());
		System.out.println("corpus graph name : " + sCorpusGraph.getSName());
		
		EList<SDocument> sDocuments = sCorpusGraph.getSDocuments();
		System.out.println("corpus contains " + sDocuments.size() + " documents:");
		for (SDocument doc : sDocuments) {
			printSDocumentInfo(doc);
		}
	}


	/** prints the number of layers and tokens of an SDocumentGraph */
	public static void printSDocumentGraphInfo(SDocumentGraph docGraph) {
		System.out.println("\tdoc graph name: " + docGraph.getSName() 
				+ " id: " + docGraph.getId() + " sid: "+ docGraph.getSId());
		System.out.println("\t\troots: " + docGraph.getRoots().size() + " sroots: " + docGraph.getSRoots().size());
		System.out.println("\t\tedges: " + docGraph.getEdges().size() + " nodes : " + docGraph.getNodes().size() 
				+ " snodes: " + docGraph.getSNodes().size() + " sfeatures: " + docGraph.getSFeatures().size());
		System.out.println("\t\tlayers: " + docGraph.getLayers().size() + " slayers: " + docGraph.getSLayers().size() 
				+ " sannotations: " + docGraph.getSAnnotations().size() + " stokens: " + docGraph.getSTokens().size());
		System.out.println("\t\tsrelations: " + docGraph.getSRelations().size() + " sdomrels: " 
				+ docGraph.getSDominanceRelations().size() + " sorderrels: " + docGraph.getSOrderRelations().size() 
				+ " sspanningrels: " + docGraph.getSSpanningRelations().size() 
				+ " spointingrels: " + docGraph.getSPointingRelations().size() 
				+ " stextualres: " + docGraph.getSTextualRelations().size());

//		docGraph.getLabels().size()
//				docGraph.getLeafs().size()
//		docGraph.getSLeafs().size()
//		docGraph.getOverlappedDSSequences(sNode, sRelationTypes)
//		docGraph.getSSpans().size()
//		docGraph.getSStructures().size()
//		docGraph.getSTextualDSs().size()
//
//		docGraph.getSortedSTokenByText().size()
	}
	
	/** prints the name and ID of an SDocument. */
	public static void printSDocumentInfo(SDocument doc) {
		System.out.println("\tdocument name: " + doc.getSName() 
				         + " id: " + doc.getId());
	}


	


	/** prints the name, qualified name, namespace and value of an SFeature, and
	 *  prints the number of labels it has. */
	public static void printSFeatureInfo(SFeature sFeature) {
		System.out.println("\nsFeature name: " + sFeature.getName() 
						   + " qualified name: " + sFeature.getQName());
		System.out.println("sFeature namespace: " + sFeature.getNamespace());
		System.out.println("sFeature value: " + sFeature.getSValueSTEXT());
		
		EList<Label> labels = sFeature.getLabels();
		System.out.println("sFeature has " + labels.size() + " labels.");
//		for (Label label : labels) {
//			printLabelInfo(label);
//		}
	}

	
	/** prints the name and ID of an SLayer and the number of nodes, edges,
	 *  features and relations it contains. 
	 *  
	 *  FIXME: don't mix this up with printLayerInfo, WTF! */
	public static void printSLayerInfo(SLayer sLayer) {
		System.out.println("\t\tname: " + sLayer.getSName() 
				         + " id: " + sLayer.getId());
		System.out.println("\t\t\tnodes: " + sLayer.getSNodes().size()
						 + " edges: " + sLayer.getEdges().size()
						 + " features: " + sLayer.getSFeatures().size()
						 + " relations: " + sLayer.getSRelations().size());
//		for (SNode sNode : sLayer.getSNodes()) { printSNodeInfo(sNode); }
//		for (SFeature sFeature : sLayer.getSFeatures()) { printSFeatureInfo(sFeature); }
	}	
	

	/** prints the name and ID of an SNode. 
	 *  prints the number of labels, annotations and features it has. 
	 *  
	 *  FIXME: getPrimaryTextSequence doesn't work with all SNodes */
	public static void printSNodeInfo(SNode sNode, SDocumentGraph sDocumentGraph) {
		System.out.println();
		System.out.println("\tnode name: " + sNode.getSName()
						 + " node id: " + sNode.getId());
		System.out.println("\t\tlabels: " + sNode.getLabels().size() 
				         + " annotations: " + sNode.getSAnnotations().size()
				         + " features: " + sNode.getSFeatures().size());
		System.out.println("\t\tprimary text: " + SaltReader.getPrimaryTextSequence(sNode, sDocumentGraph));
	}

	
	/** prints the name and ID of an SRelation, as well as the ID of its source
	 *  and target node */
	public static void printSRelationInfo(SRelation relation) {
		System.out.println("\trelation sname: " + relation.getSName() + " sid: " + relation.getSId());
		System.out.println("\t\tssource id: " + relation.getSSource().getId() 
				         + " starget id: " + relation.getSTarget().getId());
//		relation.getLabels()
//		relation.getLayers()
//		relation.getSAnnotations()
//		relation.getSFeatures()
//		relation.getSLayers()
//		relation.getSTypes()
	}
	
	/** prints the name and ID of an SSpan, as well as the number of its
	 *  labels, annotations, features and layers. */
	public static void printSSpanInfo(SSpan span) {
		EList<SAnnotation> annotations = span.getSAnnotations();
		
		System.out.println("\tspan sname: " + span.getSName() + " sid: " + span.getSId());
		System.out.println("\t\tlabels: " + span.getLabels().size()  
				+ " sannotations: " + annotations.size() 
				+ " sfeatures: " + span.getSFeatures().size() 
				+ " slayers: " + span.getSLayers().size());
		if (annotations.size() > 0) {
			for (SAnnotation anno : annotations) {
				System.out.println("\n\n+++ANNOTATION INFO:");
				printSAnnotationInfo(anno);
				System.out.println();
			}
		}
	}
	
	/** prints the name, ID, string onset/offset of an STextualRelation,
	 *  as well as the name and ID of the SToken it describes. */
	public static void printSTextualRelationInfo(STextualRelation textRel) {
		System.out.println("\ttextual relation name: " + textRel.getSName() + " id: " + textRel.getId());
		System.out.println("\t\tstring onset: " + textRel.getSStart() + " offset: " + textRel.getSEnd());
		System.out.println("\t\tprimary text: " + SaltReader.getPrimaryTextSequence(textRel));
		System.out.println("\t\ttoken name: " + textRel.getSToken().getSName() + " id: " + textRel.getSToken().getSId());
//		textRel.getLabels()
//		textRel.getLayers()
//		textRel.getSAnnotations()
//		textRel.getSFeatures()
//		textRel.getSLayers()
//		textRel.getSource()
//		textRel.getSTarget()
//		textRel.getSTypes()
	}
	
	
	/** prints the name and ID of an SToken, as well as its number of 
	 *  annotations, labels, features and associated layers. */
	public static void printSTokenInfo(SToken token) {
		System.out.println("\t\ttoken name: " + token.getSName()
				         + "token id: " + token.getId());
		
		System.out.println("\t\tannotations: " + token.getSAnnotations().size()
				         + " labels: " + token.getLabels().size()
				         + " features: " + token.getSFeatures().size()
				         + " layers: " + token.getLayers().size());	
		System.out.println("\t\tprimary text: " + SaltReader.getPrimaryTextSequence(token, token.getSDocumentGraph()));
	}
	
	
//	public static void main(String[] args) throws FileNotFoundException {
//		String saltProjectPath = Utils.getVariableFromYamlFile("saltProjectPath");
//		SaltProject saltProject = SaltReader.loadSaltProject(saltProjectPath);
////		printSaltProjectInfo(saltProject);
//		System.out.println("salt project name: " + saltProject.getSName());
//		
//		SCorpusGraph corpusGraph = saltProject.getSCorpusGraphs().get(0);
//		printSCorpusGraphInfo(corpusGraph);
//		System.out.println();
//		
//		for (SDocument doc : corpusGraph.getSDocuments()) {
//			SDocumentGraph docGraph = doc.getSDocumentGraph();
//			printSDocumentGraphInfo(docGraph);
//			System.out.println();
//			
//			for (STextualRelation textRel : docGraph.getSTextualRelations()) {
//				printSTextualRelationInfo(textRel);
//			}
//		}
//			
//	}

}
