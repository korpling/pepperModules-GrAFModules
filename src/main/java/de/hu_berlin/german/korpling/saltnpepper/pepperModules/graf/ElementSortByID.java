package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.util.Comparator;

import org.xces.graf.api.IIdentifiable;

/** sorts IEdge and INode instances by their IDs.
 * 
 *  FIXME: I'd rather use guava's Ordering.natural instead of writing yet another
 *  class, but OSGI chokes on that dependency. */
public class ElementSortByID implements Comparator<IIdentifiable>{
	@Override
	public int compare(IIdentifiable identifiable1, IIdentifiable identifiable2) {
		return identifiable1.getId().compareTo(identifiable2.getId());
	}
}
