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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.corpus_tools.salt.common.SCorpusGraph;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SStructure;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SFeature;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.graph.Label;
import org.corpus_tools.salt.graph.Layer;
import org.corpus_tools.salt.graph.Node;

/**
 * This class contains some methods for playing around with Salt Graph objects.
 * It should be useful for comparing GraF (cf. GrafGraphInfo.java) and Salt.
 */
public class SaltGraphInfo {

	/** prints the name, qualified name, namespace and value of a Label object. */
	public static void printLabelInfo(Label label) {
		System.out.println("label name: " + label.getName() + " qualified name: " + label.getQName());
		System.out.println("label namespace: " + label.getNamespace());
		System.out.println("label value: " + label.getValue());
	}

	/**
	 * prints the ID of a Layer (!= SLayer) object. prints the number of nodes,
	 * edges and labels it has.
	 * 
	 * FIXME: what's the difference between Layer and SLayer (not conceptually,
	 * but content-wise) ???
	 */
	public static void printLayerInfo(Layer layer) {
		System.out.println("layer id: " + layer.getId());
		System.out.println("\tnodes: " + layer.getNodes().size() + " edges: " + layer.getRelations().size() + " labels: " + layer.getLabels().size());
	}

	/**
	 * prints the ID of a Node (!= SNode) and its number of labels and layers.
	 * calls printLabelInfo for each label and printLayerInfo for each layer.
	 * FIXME: do not confuse Node with SNode! Nodes occur in the SDocumentGraph.
	 */
	public static void printNodeInfo(Node node) {
		System.out.println("node id: " + node.getId());

		Collection<Label> nodeLabels = node.getLabels();
		System.out.println("\tnode has " + nodeLabels.size() + " labels");
		for (Label label : nodeLabels) {
			printLabelInfo(label);
		}

		Set<SLayer> nodeLayers = (Set<SLayer>)node.getLayers();
		System.out.println("\tnode has " + nodeLayers.size() + " layers");
		for (Layer layer : nodeLayers) {
			printLayerInfo(layer);
		}
	}

	/** FIXME: write meaningful docstring */
	public static void printSaltProjectInfo(SaltProject saltProject) {
		// there's only one SCorpusGraph, but we got to retrieve it from a list
		SCorpusGraph sCorpusGraph = saltProject.getCorpusGraphs().get(0);
		// print all corpora.
		System.out.println("all corpora: " + sCorpusGraph.getCorpora());
		// print all documents
		System.out.println("all documents: " + sCorpusGraph.getDocuments());

		// for all documents print some properties
		for (SDocument sDocument : sCorpusGraph.getDocuments()) {
			SDocumentGraph sDocumentGraph = sDocument.getDocumentGraph();
			System.out.println("============ " + sDocument.getName() + " ============");
			System.out.println("root nodes\t: " + sDocumentGraph.getRoots());

			// print some information for tokens
			for (SToken sToken : sDocumentGraph.getTokens()) {
				System.out.println(SaltReader.getPrimaryTextSequence(sToken, sDocumentGraph));
			}

			System.out.println("----------------------------------------");

			// print some information for spans
			for (SSpan sSpan : sDocumentGraph.getSpans()) {
				System.out.println(SaltReader.getPrimaryTextSequence(sSpan, sDocumentGraph));
			}

			System.out.println("----------------------------------------");

			// print some information for structs
			for (SStructure sStruct : sDocumentGraph.getStructures()) {
				System.out.println(SaltReader.getPrimaryTextSequence(sStruct, sDocumentGraph));
			}

			System.out.println("================================================");
		}
	}

	/**
	 * prints the name, qualified name, namespace and value of an SAnnotation.
	 * calls printLabelInfo for further information, if the annotation has
	 * labels.
	 */
	public static void printSAnnotationInfo(SAnnotation sAnnotation) {
		System.out.println("sAnnotation name: " + sAnnotation.getName() + "qualified name: " + sAnnotation.getQName());
		System.out.println("SAnnotation namespace: " + sAnnotation.getNamespace());
		System.out.println("SAnnotation value: " + sAnnotation.getValue_STEXT());
		Collection<Label> sAnnotationLabels = sAnnotation.getLabels();
		for (Label label : sAnnotationLabels) {
			printLabelInfo(label);
		}
	}

	/**
	 * prints the name and ID of an SCorpusGraph. prints the name and ID of
	 * every document in the corpus graph.
	 */
	public static void printSCorpusGraphInfo(SCorpusGraph sCorpusGraph) {
		System.out.println("corpus graph id: " + sCorpusGraph.getId());
		System.out.println("corpus graph name : " + sCorpusGraph.getName());

		List<SDocument> sDocuments = sCorpusGraph.getDocuments();
		System.out.println("corpus contains " + sDocuments.size() + " documents:");
		for (SDocument doc : sDocuments) {
			printSDocumentInfo(doc);
		}
	}

	/** prints the number of layers and tokens of an SDocumentGraph */
	public static void printSDocumentGraphInfo(SDocumentGraph docGraph) {
		System.out.println("\tdoc graph name: " + docGraph.getName() + " id: " + docGraph.getId() + " sid: " + docGraph.getId());
		System.out.println("\t\troots: " + docGraph.getRoots().size() + " sroots: " + docGraph.getRoots().size());
		System.out.println("\t\tedges: " + docGraph.getRelations().size() + " nodes : " + docGraph.getNodes().size() + " snodes: " + docGraph.getNodes().size() + " sfeatures: " + docGraph.getFeatures().size());
		System.out.println("\t\tlayers: " + docGraph.getLayers().size() + " slayers: " + docGraph.getLayers().size() + " sannotations: " + docGraph.getAnnotations().size() + " stokens: " + docGraph.getTokens().size());
		System.out.println("\t\tsrelations: " + docGraph.getRelations().size() + " sdomrels: " + docGraph.getDominanceRelations().size() + " sorderrels: " + docGraph.getOrderRelations().size() + " sspanningrels: " + docGraph.getSpanningRelations().size() + " spointingrels: " + docGraph.getPointingRelations().size() + " stextualres: " + docGraph.getTextualRelations().size());
	}

	/** prints the name and ID of an SDocument. */
	public static void printSDocumentInfo(SDocument doc) {
		System.out.println("\tdocument name: " + doc.getName() + " id: " + doc.getId());
	}

	/**
	 * prints the name, qualified name, namespace and value of an SFeature, and
	 * prints the number of labels it has.
	 */
	public static void printSFeatureInfo(SFeature sFeature) {
		System.out.println("\nsFeature name: " + sFeature.getName() + " qualified name: " + sFeature.getQName());
		System.out.println("sFeature namespace: " + sFeature.getNamespace());
		System.out.println("sFeature value: " + sFeature.getValue_STEXT());

		Collection<Label> labels = sFeature.getLabels();
		System.out.println("sFeature has " + labels.size() + " labels.");
	}

	/**
	 * prints the name and ID of an SLayer and the number of nodes, edges,
	 * features and relations it contains.
	 * 
	 * FIXME: don't mix this up with printLayerInfo, WTF!
	 */
	public static void printSLayerInfo(SLayer sLayer) {
		System.out.println("\t\tname: " + sLayer.getName() + " id: " + sLayer.getId());
		System.out.println("\t\t\tnodes: " + sLayer.getNodes().size() + " edges: " + sLayer.getRelations().size() + " features: " + sLayer.getFeatures().size() + " relations: " + sLayer.getRelations().size());
		// for (SNode sNode : sLayer.getSNodes()) { printSNodeInfo(sNode); }
		// for (SFeature sFeature : sLayer.getSFeatures()) {
		// printSFeatureInfo(sFeature); }
	}

	/**
	 * prints the name and ID of an SNode. prints the number of labels,
	 * annotations and features it has.
	 * 
	 * FIXME: getPrimaryTextSequence doesn't work with all SNodes
	 */
	public static void printSNodeInfo(SNode sNode, SDocumentGraph sDocumentGraph) {
		System.out.println();
		System.out.println("\tnode name: " + sNode.getName() + " node id: " + sNode.getId());
		System.out.println("\t\tlabels: " + sNode.getLabels().size() + " annotations: " + sNode.getAnnotations().size() + " features: " + sNode.getFeatures().size() + " layers: " + sNode.getLayers().size());
		// System.out.println("\t\tprimary text: " +
		// SaltReader.getPrimaryTextSequence(sNode, sDocumentGraph));
	}

	/**
	 * prints the name and ID of an SRelation, as well as the ID of its source
	 * and target node
	 */
	public static void printSRelationInfo(SRelation relation) {
		System.out.println("\trelation sname: " + relation.getName() + " sid: " + relation.getId());
		System.out.println("\t\tssource id: " + relation.getSource().getId() + " starget id: " + relation.getTarget().getId());
	}

	/**
	 * prints the name and ID of an SSpan, as well as the number of its labels,
	 * annotations, features and layers.
	 */
	public static void printSSpanInfo(SSpan span) {
		Collection<SAnnotation> annotations = span.getAnnotations();

		System.out.println("\tspan sname: " + span.getName() + " sid: " + span.getId());
		System.out.println("\t\tlabels: " + span.getLabels().size() + " sannotations: " + annotations.size() + " sfeatures: " + span.getFeatures().size() + " slayers: " + span.getLayers().size());
		if (annotations.size() > 0) {
			for (SAnnotation anno : annotations) {
				System.out.println("\n\n+++ANNOTATION INFO:");
				printSAnnotationInfo(anno);
				System.out.println();
			}
		}
	}

	/**
	 * prints the name, ID, string onset/offset of an STextualRelation, as well
	 * as the name and ID of the SToken it describes.
	 */
	public static void printSTextualRelationInfo(STextualRelation textRel) {
		System.out.println("\ttextual relation name: " + textRel.getName() + " id: " + textRel.getId());
		System.out.println("\t\tstring onset: " + textRel.getStart() + " offset: " + textRel.getEnd());
		System.out.println("\t\tprimary text: " + SaltReader.getPrimaryTextSequence(textRel));
		System.out.println("\t\ttoken name: " + textRel.getSource().getName() + " id: " + textRel.getSource().getId());
	}

	/**
	 * prints the name and ID of an SToken, as well as its number of
	 * annotations, labels, features and associated layers.
	 */
	public static void printSTokenInfo(SToken token) {
		System.out.println("\t\ttoken name: " + token.getName() + "token id: " + token.getId());

		System.out.println("\t\tannotations: " + token.getAnnotations().size() + " labels: " + token.getLabels().size() + " features: " + token.getFeatures().size() + " layers: " + token.getLayers().size());
		System.out.println("\t\tprimary text: " + SaltReader.getPrimaryTextSequence(token, token.getGraph()));
	}
}
