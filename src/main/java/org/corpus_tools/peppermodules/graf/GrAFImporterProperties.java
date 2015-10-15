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

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;


/**
 * Defines the properties to be used for the {@link GrAFImporter}.
 * @author florian
 * @author Arne Neumann
 * 
 */
public class GrAFImporterProperties extends PepperModuleProperties {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5025815039524710477L;
	public static final String PREFIX = "graf.importer.";
	public static final String PROP_SYNTAX_LAYER = PREFIX + "syntaxLayer";
	public static final String PROP_TOKENIZATION_LAYER = PREFIX + "tokenizationLayer";
	public static final String PROP_POS_LAYER = PREFIX + "posLayer";
	public static final String PROP_HEADER_FILE_ENDING = PREFIX + "headerEnding";

	public GrAFImporterProperties() {
		this.addProperty(new PepperModuleProperty<String>(PROP_SYNTAX_LAYER, String.class, "This property determines the name for the syntax layer in the GrAF encoded corpus.", "f.ptb", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_TOKENIZATION_LAYER, String.class, "This property determines the name for the tokenization layer in the GrAF encoded corpus.", "f.seg", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_POS_LAYER, String.class, "This property determines the name for the pos annotations in the GrAF encoded corpus.", "f.penn", false));
		this.addProperty(new PepperModuleProperty<String>(PROP_HEADER_FILE_ENDING, String.class, "This property determines ending of the header files.", ".hdr", false));
	}

	/**
	 * Returns the name of the syntactic layer in the GrAF corpus.
	 */
	@SuppressWarnings("unchecked")
	public String getSyntaxLayer() {
		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getProperty(PROP_SYNTAX_LAYER);
		if (prop.getValue() == null)
			return (null);
		else
			return prop.getValue().toString();
	}

	/**
	 * Returns the name of the tokenization layer in the GrAF corpus.
	 */
	@SuppressWarnings("unchecked")
	public String getTokenizationLayer() {
		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getProperty(PROP_TOKENIZATION_LAYER);
		if (prop.getValue() == null)
			return (null);
		else
			return prop.getValue().toString();
	}

	/**
	 * Returns the name of the part-of-speech layer in the GrAF corpus.
	 */
	@SuppressWarnings("unchecked")
	public String getPOSLayer() {
		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getProperty(PROP_POS_LAYER);
		if (prop.getValue() == null)
			return (null);
		else
			return prop.getValue().toString();
	}

	/**
	 * Returns the ending of header files.
	 */
	@SuppressWarnings("unchecked")
	public String getHeaderFileEnding() {

		PepperModuleProperty<String> prop = (PepperModuleProperty<String>) this.getProperty(PROP_HEADER_FILE_ENDING);
		return prop.getValue().toString();
	}
}
