package org.jbnd.data;

import java.util.Collection;
import java.util.List;

import org.jbnd.DataObject;
import org.jbnd.event.DataObjectEvent;
import org.jbnd.event.DataObjectListener;


/**
 * A <tt>DataSource</tt> implementation that is <tt>List</tt> based, takes care
 * of all <tt>DataObject</tt> management (referencing, listening for their
 * changes and firing events, etc).
 * <p>
 * The interface of this class is a subset of <tt>List</tt> methods, the ones
 * concerned with adding and removing objects. These methods take care of
 * registering / unregistering the data source as an <tt>DataObjectListener</tt>
 * with the contained <tt>DataObject</tt>s. All methods are protected to enable
 * subclasses to expose a clean interface.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, Sep 19, 2007
 */
public abstract class AbstractListDataSource extends AbstractNotListeningListDataSource
		implements DataObjectListener{

	/**
	 * Creates a new <tt>AbstractListDataSource</tt>.
	 */
	protected AbstractListDataSource(){
		super();
	}
	
	/**
	 * Creates a new <tt>AbstractListDataSource</tt> with the given initial
	 * capacity of the backing <tt>ArrayList</tt>.
	 * 
	 * @param capacity The initial capacity of the backing <tt>ArrayList</tt>.
	 */
	protected AbstractListDataSource(int capacity){
		super(capacity);
	}
	
	/**
	 * Creates a new <tt>AbstractListDataSource</tt> from an already existing
	 * <tt>List</tt> of <tt>DataObject</tt>s. If the given <tt>List</tt> is an
	 * <tt>ArrayList</tt>, then that object is used as a backing container,
	 * otherwise a new <tt>ArrayList</tt> is created from the given data.
	 * 
	 * @param data See above.
	 */
	protected AbstractListDataSource(List<DataObject> data){
		super(data);
	}

	public void objectChanged(DataObjectEvent event){
		// get the index of the changed <tt>DataObject</tt>s
		int index = indexOf(event.getDataObject());
		if(index != -1)
			fireDataChanged(index, index);
	}
	
	private void listenTo(DataObject object){
		object.addDataObjectListener(this);
	}
	
	private void listenTo(Collection<? extends DataObject> objects){
		for(DataObject object : objects)
			listenTo(object);
	}
	
	private void stopListeningTo(DataObject object){
		object.removeDataObjectListener(this);
	}
	
	/**
	 * Inserts the <tt>DataObject</tt> at the given index of this data source.
	 * 
	 * @param index The index at which to insert the <tt>DataObject</tt>, must
	 *            be between 0 (inclusive) and current source size (inclusive).
	 * @param object The <tt>DataObject</tt> to be added.
	 */
	protected void add(int index, DataObject object){
		listenTo(object);
		super.add(index, object);
	}

	/**
	 * Inserts all the <tt>DataObject</tt> from the <tt>Collection</tt> at the
	 * given index of this data source.
	 * 
	 * @param index The index at which to insert the <tt>DataObject</tt>, must
	 *            be between 0 (inclusive) and current source size (inclusive).
	 * @param collection The <tt>Collection</tt> containing the
	 *            <tt>DataObject</tt> to be added.
	 */
	protected void addAll(int index, Collection<? extends DataObject> collection){
		listenTo(collection);
		super.addAll(index, collection);
		
	}
	
	/**
	 * Removes the <tt>DataObject</tt> at the given <tt>index</tt> from this
	 * data source.
	 * 
	 * @param index The index of the <tt>DataObject</tt> to be removed, must be
	 *            between 0 (inclusive) and current source size (exclusive).
	 * @return The removed <tt>DataObject</tt>.
	 */
	protected DataObject remove(int index){
		stopListeningTo(get(index));
		return super.remove(index);
	}
	
	/**
	 * Removes all <tt>DataObject</tt>s found in the specified range from this
	 * <tt>DataSource</tt>.
	 * 
	 * @param index0 The start of the range, inclusive.
	 * @param index1 The end of the range, inclusive.
	 */
	protected void removeRange(int index0, int index1){
		for(int i = index0 ; i < index1 ; i++)
			stopListeningTo(get(i));
		super.removeRange(index0, index1);
	}
}