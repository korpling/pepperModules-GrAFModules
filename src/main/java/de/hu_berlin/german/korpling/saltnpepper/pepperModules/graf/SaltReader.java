package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;

import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltProject;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDataSourceSequence;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SNode;

/** shows how to import stuff from a Salt model / project. since our goal is to
 *  EXPORT to Salt, this class is mainly used for testing. */
public class SaltReader {

	/** loads a SaltProject from the given file path. */
	public static SaltProject loadSaltProject(String saltProjectPath) {
		System.out.println("load salt project from path: " + saltProjectPath);
		SaltProject saltProject = SaltFactory.eINSTANCE.createSaltProject();
		saltProject.loadSaltProject(URI.createFileURI(saltProjectPath));
		System.out.println("OK");
		return saltProject;
	}


	
	/** returns the portion of the primary text represented by an SNode 
	 *  (e.g. and SToken, SSpan or SStructure), or null otherwise.
	 *
	 *  TODO: ask Florian to simplify his API
	 *  What do STYPE_NAME and STYPE_NAME.STEXT_OVERLAPPING_RELATION mean? 
	 *  sDocumentGraph.getOverlappedDSSequences(sNode, interestingRelations).get(0), srsly? */
	public static String getPrimaryTextSequence(SNode sNode, SDocumentGraph sDocumentGraph) {
		// Florian: only search for sequences by traversing relations inheriting text
		EList<STYPE_NAME> interestingRelations = new BasicEList<STYPE_NAME>();
		interestingRelations.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
		// we'll use the first (and only) SDataSource 
		EList<SDataSourceSequence> overlappedDSSequences = sDocumentGraph.getOverlappedDSSequences(sNode, interestingRelations);
		if (!overlappedDSSequences.isEmpty()) {
			SDataSourceSequence firstSequence = overlappedDSSequences.get(0);
			String primaryText = firstSequence.getSSequentialDS().getSData().toString();
			return primaryText.substring(firstSequence.getSStart(), firstSequence.getSEnd());			
		}
		else {return null;}
	}


	
	/** returns the portion of the primary text represented by an SToken. */
	public static String getPrimaryTextSequence(SToken sToken) {
		return getPrimaryTextSequence(sToken, sToken.getSDocumentGraph());
	}	
	
	
	/** returns the portion of the primary text represented by an STextualRelation. */
	public static String getPrimaryTextSequence(STextualRelation textRel) {
		String primaryText = textRel.getSTextualDS().getSText();
		return primaryText.substring(textRel.getSStart(), textRel.getSEnd());
	}

	
	/** returns the primary string represented by an SSpan. */
	public static String getPrimaryTextSequence(SSpan span) {
		return getPrimaryTextSequence(span, span.getSDocumentGraph());
	}
	
	
	/** returns the string onset and offset of an SToken */
	public static int[] getSTokenOffsets(SToken token) {
		EList<STYPE_NAME> interestingRelations= new BasicEList<STYPE_NAME>();
		interestingRelations.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
		SDocumentGraph docGraph = token.getSDocumentGraph();
		SDataSourceSequence sequence = docGraph.getOverlappedDSSequences(token, interestingRelations).get(0);
		return new int[] {sequence.getSStart(), sequence.getSEnd()};
	}

	/** returns the string onset and offset of an STextualRelation */
	public static Pair<Integer, Integer> getSTextualRelationOffsets(STextualRelation textRel) {
		 Pair<Integer, Integer> textRelOffsets = Pair.of(textRel.getSStart(), textRel.getSEnd());
		 return textRelOffsets;
	}
		
		
    /** returns the primary text offsets of an SSpan
     *  FIXME: untested */
	public static Pair<Integer, Integer> getSSpanOffset(SSpan span) {
		EList<STYPE_NAME> interestingRelations= new BasicEList<STYPE_NAME>();
		interestingRelations.add(STYPE_NAME.STEXT_OVERLAPPING_RELATION);
		SDocumentGraph docGraph = span.getSDocumentGraph();
		SDataSourceSequence sequence = docGraph.getOverlappedDSSequences(span, interestingRelations).get(0);
		return Pair.of(sequence.getSStart(), sequence.getSEnd());
	}
	
	
    /** returns a list of STokens that are part of an SSpan. */
	public static List<SToken> getSTokensFromSSpan(SSpan span, SDocumentGraph docGraph) {
		EList<SSpanningRelation> spanningRelations = docGraph.getSSpanningRelations();
		
		List<SToken> spanTokens = new ArrayList<SToken>();
		for (SSpanningRelation spanningRel: spanningRelations) {
			if (spanningRel.getSource() == span) {
				SToken token = (SToken) spanningRel.getTarget();
				spanTokens.add(token);
			}
		}
		return spanTokens;
	}
	
}


