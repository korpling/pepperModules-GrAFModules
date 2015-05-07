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
import java.io.InputStream;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.xces.graf.api.GrafException;
import org.xces.graf.io.dom.DocumentHeader;

public class GrafDocumentHeader extends DocumentHeader {

	public GrafDocumentHeader(File headerFile) throws FileNotFoundException {
		super(headerFile);
		// Auto-generated constructor stub
	}

	public GrafDocumentHeader(InputStream input) {
		super(input);
		// Auto-generated constructor stub
	}

	/** constructs a document header object given the header's file path */
	public GrafDocumentHeader(String pathToHeaderFile) throws FileNotFoundException {
		super(loadHeaderFile(pathToHeaderFile));
	}

	/**
	 * this method is needed because Java doesn't allow any statements prior to
	 * super() when constructing a derived class
	 */
	private static File loadHeaderFile(String pathToHeaderFile) {
		return new File(pathToHeaderFile);
	}

	public void printDocumentHeaderInfo() throws GrafException, XPathExpressionException {
		System.out.println("document id: " + this.getDocumentId());
		System.out.println("document title: " + this.getDocumentTitle());
		System.out.println("The document has " + this.getDocumentAnnotationTypeCount() + " types of annotations:");
		for (String annoType : this.getAnnotationTypes()) {
			System.out.println("\t" + annoType);
		}
		System.out.println();
	}

	/**
	 * Returns a list of all the annotation types used in a document (as
	 * specified in the document header). This wrapper method around
	 * DocumentHeader.getAnnotationTypes() purely exists to distinguish it from
	 * related methods, i.e. GrafResourceHeader.getCorpusAnnotationTypes() and
	 * GrafReader.getGraphAnnotationTypes().
	 */
	public List<String> getDocumentAnnotationTypes() throws GrafException {
		return this.getAnnotationTypes();
	}

	/**
	 * returns the number of different types of annotations (e.g. POS, noun
	 * chunks, sentence boundaries etc.) made to the document.
	 */
	public int getDocumentAnnotationTypeCount() throws GrafException {
		List<String> annoTypes = getAnnotationTypes();
		return annoTypes.size();
	}

	/** returns the title of the document */
	public String getDocumentTitle() throws XPathExpressionException {
		Node srcDescNode = this.getNode("/g:documentHeader/g:fileDesc/g:sourceDesc/g:title");
		return srcDescNode.getTextContent();
	}

	/**
	 * returns the ID of a document (unique within a corpus)
	 * 
	 * @throws XPathExpressionException
	 */
	public String getDocumentId() throws XPathExpressionException {
		Node docHeaderNode = this.getNode("/g:documentHeader");
		return this.getAttributeValue(docHeaderNode, "docId");
	}
}
