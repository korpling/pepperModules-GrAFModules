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

import java.util.Comparator;

import de.hu_berlin.german.korpling.saltnpepper.salt.graph.IdentifiableElement;

/**
 * sorts SNode and SEdge instances by their IDs.
 * 
 * FIXME: I'd rather use guava's Ordering.natural instead of writing yet another
 * class, but OSGI chokes on that dependency.
 */
public class SaltElementSortByID implements Comparator<IdentifiableElement> {
	@Override
	public int compare(IdentifiableElement identifiable1, IdentifiableElement identifiable2) {
		return identifiable1.getId().compareTo(identifiable2.getId());
	}
}
