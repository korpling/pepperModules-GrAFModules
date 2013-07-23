package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.util.Comparator;

import org.xces.graf.api.IIdentifiable;

import de.hu_berlin.german.korpling.saltnpepper.salt.graph.IdentifiableElement;

/** sorts SNode and SEdge instances by their IDs.
 * 
 *  FIXME: I'd rather use guava's Ordering.natural instead of writing yet another
 *  class, but OSGI chokes on that dependency. */
public class SaltElementSortByID implements Comparator<IdentifiableElement>{
	@Override
	public int compare(IdentifiableElement identifiable1, IdentifiableElement identifiable2) {
		return identifiable1.getId().compareTo(identifiable2.getId());
	}
}
