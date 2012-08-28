package it.unibz.krdb.obda.protege4.gui.component;

import it.unibz.krdb.obda.model.Predicate;

public class MapItem {

	private PredicateItem predicateItem;
	private String targetMapping = "";

	/**
	 * Constructs a map item without specifying the predicate type. By default,
	 * this constructor assumes the predicate type is a class predicate.
	 */
	public MapItem() {
		this(null);
	}
	
	public MapItem(PredicateItem predicate) {
		this.predicateItem = predicate;
	}

	public String getName() {
		if (predicateItem == null) {
			return "";
		} else {
			return predicateItem.getFullName();
		}
	}
	
	public Predicate getSourcePredicate() {
		return predicateItem.getSource();
	}

	public void setTargetMapping(String columnOrUriTemplate) {
		targetMapping = columnOrUriTemplate;
	}

	public String getTargetMapping() {
		return targetMapping;
	}

	public boolean isSubjectMap() {
		// A null predicate is assumed to be a class predicate
		return (predicateItem == null || predicateItem.isClassPredicate());
	}

	public boolean isObjectMap() {
		return predicateItem.isDataPropertyPredicate();
	}

	public boolean isRefObjectMap() {
		return predicateItem.isObjectPropertyPredicate();
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		MapItem other = (MapItem) obj;
		return this.getName() == other.getName();
	}

	@Override
	public String toString() {
		if (predicateItem == null) {
			return "";
		} else {
			return predicateItem.getQualifiedName();
		}
	}
}
