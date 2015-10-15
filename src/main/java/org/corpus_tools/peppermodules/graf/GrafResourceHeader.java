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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xces.graf.api.GrafException;
import org.xces.graf.io.dom.FileType;
import org.xces.graf.io.dom.ResourceHeader;

/**
 * a resource header is an xml file describing which primary texts a corpus
 * contains and which types of annotations are used. we need to create a
 * resource header before we are able to create annotation graphs.
 */
public class GrafResourceHeader extends ResourceHeader {

	public GrafResourceHeader(File file) throws FileNotFoundException {
		super(file); // Auto-generated constructor stub
	}

	public GrafResourceHeader(InputStream input) {
		super(input); // Auto-generated constructor stub
	}

	/**
	 * constructs a resource header object given the corpus directory path. will
	 * only work, if the resource header is named 'resource-header.xml'.
	 */
	public GrafResourceHeader(String corpusPath) throws FileNotFoundException {
		super(loadHeaderFile(corpusPath));
	}

	/**
	 * constructs a resource header object given the corpus directory path and
	 * the resource header file name
	 */
	public GrafResourceHeader(String corpusPath, String headerFileName) throws FileNotFoundException {
		super(loadHeaderFile(corpusPath, headerFileName));
	}

	/**
	 * WTF: this method is needed because Java doesn't allow any statements
	 * prior to super() when constructing a derived class
	 */
	private static File loadHeaderFile(String corpusPath, String headerFileName) {
		return new File(corpusPath, headerFileName);
	}

	/**
	 * WTF: this method is needed because Java doesn't allow any statements
	 * prior to super() when constructing a derived class
	 */
	private static File loadHeaderFile(String corpusPath) {
		return new File(corpusPath, "resource-header.xml");
	}

	/** prints the types of annotations that are used in a corpus. */
	public void printResourceHeaderInfo() throws GrafException {
		System.out.println("The corpus / resource header uses these types of annotations: ");
		List<FileType> fileTypes = this.getFileTypes();
		for (FileType fileType : fileTypes) {
			System.out.println("AnnotationId: " + fileType.getAnnotationId()); // a.nc,
																				// a.seg
			System.out.println("FileId: " + fileType.getFileId()); // e.g.
																	// f.seg,
																	// f.penn
			System.out.println("Medium: " + fileType.getMedium()); // e.g. xml,
																	// txt
			System.out.println("Suffix: " + fileType.getSuffix()); // e.g. seg,
																	// penn
			System.out.println();
		}
	}

	/**
	 * Returns a list of strings representing the types of annotations that are
	 * used in a corpus. Note that not all of the documents in a corpus are
	 * annotated with them.
	 * 
	 * NOTE: annotation types (aka file IDs have slightly different names in the
	 * resource header (compared to individual document headers). This is a
	 * confirmed bug in MASC 3.0.0. Use
	 * MascDocumentHeader.getDocumentAnnotationTypes() instead to retrieve the
	 * annotation types used in a document.
	 */
	public List<String> getCorpusAnnotationTypes() throws GrafException {
		List<FileType> fileTypes = this.getFileTypes();
		List<String> annoTypes = new ArrayList<String>();
		for (FileType fileType : fileTypes) {
			annoTypes.add(fileType.getFileId()); // e.g. f.seg, f.penn
		}
		return annoTypes;
	}

}
