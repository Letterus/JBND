package org.jbnd.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jbnd.DataObject;


/**
 * A <tt>DataSource</tt> implementation that can only ever contain one of the
 * two equal <tt>DataObject</tt>s, in which sense it behaves like a <tt>Set</tt>
 * . The exposed API however is list based, as <tt>DataSource</tt>s are by their
 * contract more list-like (indexed collections).
 * 
 * @version 1.0 May 2, 2009
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class SetDataSource extends ListDataSource{
	
	private final Set<DataObject> checkSet = new HashSet<DataObject>();

	@Override
	public void add(int index, DataObject object){
		if(checkSet.add(object));
			super.add(index, object);
	}

	@Override
	public void addAll(int index, Collection<? extends DataObject> collection){
		// we need to create a set consisting only of the intersection of the given
		// collection and the existing set
		Set<DataObject> collectionSet = new HashSet<DataObject>(collection);
		collectionSet.removeAll(checkSet);
		if(!collectionSet.isEmpty())
			super.addAll(index, collectionSet);
	}
	
	@Override
	public DataObject remove(int index){
		DataObject removed = super.remove(index);
		checkSet.remove(removed);
		return removed;
	}
	
	@Override
	public void removeRange(int index0, int index1){
		checkSet.removeAll(Arrays.asList(range(index0, index1)));
		super.removeRange(index0, index1);
	}
}