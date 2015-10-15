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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SaltProject;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.eclipse.emf.common.util.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * shows how to import stuff from a Salt model / project. since our goal is to
 * EXPORT to Salt, this class is mainly used for testing.
 */
public class SaltReader {
	protected static final Logger logger= LoggerFactory.getLogger(GrAFImporter.MODULE_NAME);
	/** loads a SaltProject from the given file path. */
	public static SaltProject loadSaltProject(String saltProjectPath) {
		logger.trace("load salt project from path: " + saltProjectPath);
		SaltProject saltProject = SaltFactory.createSaltProject();
		saltProject.loadSaltProject(URI.createFileURI(saltProjectPath));
		logger.trace("OK");
		return saltProject;
	}

	/**
	 * returns the portion of the primary text represented by an SNode (e.g. and
	 * SToken, SSpan or SStructure), or null otherwise.
	 * 
	 */
	public static String getPrimaryTextSequence(SNode sNode, SDocumentGraph sDocumentGraph) {
		List<SALT_TYPE> interestingRelations = new ArrayList<SALT_TYPE>();
		interestingRelations.add(SALT_TYPE.STEXT_OVERLAPPING_RELATION);
		// we'll use the first (and only) SDataSource
		List<DataSourceSequence> overlappedDSSequences = sDocumentGraph.getOverlappedDataSourceSequence(sNode, interestingRelations);
		if (!overlappedDSSequences.isEmpty()) {
			DataSourceSequence<Integer> firstSequence = overlappedDSSequences.get(0);
			String primaryText = firstSequence.getDataSource().getData().toString();
			return primaryText.substring(firstSequence.getStart(), firstSequence.getEnd());
		} else {
			return null;
		}
	}

	/** returns the portion of the primary text represented by an SToken. */
	public static String getPrimaryTextSequence(SToken sToken) {
		return getPrimaryTextSequence(sToken, sToken.getGraph());
	}

	/**
	 * returns the portion of the primary text represented by an
	 * STextualRelation.
	 */
	public static String getPrimaryTextSequence(STextualRelation textRel) {
		String primaryText = textRel.getTarget().getText();
		return primaryText.substring(textRel.getStart(), textRel.getEnd());
	}

	/** returns the primary string represented by an SSpan. */
	public static String getPrimaryTextSequence(SSpan span) {
		return getPrimaryTextSequence(span, span.getGraph());
	}

	/** returns the string onset and offset of an SToken */
	public static int[] getSTokenOffsets(SToken token) {
		List<SALT_TYPE> interestingRelations = new ArrayList<SALT_TYPE>();
		interestingRelations.add(SALT_TYPE.STEXT_OVERLAPPING_RELATION);
		SDocumentGraph docGraph = token.getGraph();
		DataSourceSequence<Integer> sequence = docGraph.getOverlappedDataSourceSequence(token, interestingRelations).get(0);
		return new int[] { sequence.getStart(), sequence.getEnd() };
	}

	/** returns the string onset and offset of an STextualRelation */
	public static Pair<Integer, Integer> getSTextualRelationOffsets(STextualRelation textRel) {
		Pair<Integer, Integer> textRelOffsets = Pair.of(textRel.getStart(), textRel.getEnd());
		return textRelOffsets;
	}

	/**
	 * returns the primary text offsets of an SSpan FIXME: untested
	 */
	public static Pair<Integer, Integer> getSSpanOffset(SSpan span) {
		List<SALT_TYPE> interestingRelations = new ArrayList<>();
		interestingRelations.add(SALT_TYPE.STEXT_OVERLAPPING_RELATION);
		SDocumentGraph docGraph = span.getGraph();
		DataSourceSequence<Integer> sequence = docGraph.getOverlappedDataSourceSequence(span, interestingRelations).get(0);
		return Pair.of(sequence.getStart(), sequence.getEnd());
	}

	/** returns a list of STokens that are part of an SSpan. */
	public static List<SToken> getSTokensFromSSpan(SSpan span, SDocumentGraph docGraph) {
		List<SSpanningRelation> spanningRelations = docGraph.getSpanningRelations();

		List<SToken> spanTokens = new ArrayList<SToken>();
		for (SSpanningRelation spanningRel : spanningRelations) {
			if (spanningRel.getSource() == span) {
				SToken token = (SToken) spanningRel.getTarget();
				spanTokens.add(token);
			}
		}
		return spanTokens;
	}

}
