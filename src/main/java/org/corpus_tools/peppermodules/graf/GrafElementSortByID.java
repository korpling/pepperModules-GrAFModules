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

import java.util.Comparator;

import org.xces.graf.api.IIdentifiable;

/**
 * sorts IEdge and INode instances by their IDs.
 * 
 * FIXME: I'd rather use guava's Ordering.natural instead of writing yet another
 * class, but OSGI chokes on that dependency.
 */
public class GrafElementSortByID implements Comparator<IIdentifiable> {
	@Override
	public int compare(IIdentifiable identifiable1, IIdentifiable identifiable2) {
		return identifiable1.getId().compareTo(identifiable2.getId());
	}
}
