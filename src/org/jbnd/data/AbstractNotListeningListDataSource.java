package org.jbnd.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jbnd.DataObject;


/**
 * A <tt>DataSource</tt> implementation that it does NOT listen to changes in
 * <tt>DataObject</tt>s, and consequently does not fire events when they change.
 * The purpose of this class it to be a slightly more lightweight starting point
 * for concrete subclasses that handle event firing differently.
 * 
 * @version 1.0 Jan 9, 2009
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public abstract class AbstractNotListeningListDataSource extends AbstractDataSource{

	/*
	 * The <tt>List</tt> backing this data source. For performance reasons this
	 * variable should always be an ArrayList, and is therefore declared as
	 * such, not as a List.
	 */
	protected final ArrayList<DataObject> data;
	
	/**
	 * Creates a new <tt>AbstractListDataSource</tt>.
	 */
	protected AbstractNotListeningListDataSource(){
		data = new ArrayList<DataObject>();
	}
	
	/**
	 * Creates a new <tt>AbstractListDataSource</tt> with the given initial
	 * capacity of the backing <tt>ArrayList</tt>.
	 * 
	 * @param capacity The initial capacity of the backing <tt>ArrayList</tt>.
	 */
	protected AbstractNotListeningListDataSource(int capacity){
		this.data = new ArrayList<DataObject>(capacity);
	}
	
	/**
	 * Creates a new <tt>AbstractListDataSource</tt> from an already existing
	 * <tt>List</tt> of <tt>DataObject</tt>s. If the given <tt>List</tt> is an
	 * <tt>ArrayList</tt>, then that object is used as a backing container,
	 * otherwise a new <tt>ArrayList</tt> is created from the given data.
	 * 
	 * @param data See above.
	 */
	protected AbstractNotListeningListDataSource(List<DataObject> data){
		if(data instanceof ArrayList)
			this.data = (ArrayList<DataObject>)data;
		else
			this.data = new ArrayList<DataObject>(data);
	}
	
	public final DataObject get(int index){
		return data.get(index);
	}

	public final int size(){
		return data.size();
	}
	
	/**
	 * Forwards the call to the method of the same name in the
	 * <tt>java.util.List</tt> that is backing this <tt>DataSource</tt>.
	 * 
	 * @param object The <tt>DataObject</tt> for which the index is sought.
	 * @return Index of <tt>object</tt> in this <tt>DataSource</tt>, or -1 if
	 *         not found.
	 */
	protected int indexOf(DataObject object){
		return data.indexOf(object);
	}
	
	/**
	 * Adds the <tt>DataObject</tt> to the end of this data source.
	 * 
	 * @param object The <tt>DataObject</tt> to be added.
	 */
	protected void add(DataObject object){
		add(data.size(), object);
	}
	
	/**
	 * Inserts the <tt>DataObject</tt> at the given index of this data source.
	 * 
	 * @param index The index at which to insert the <tt>DataObject</tt>, must
	 *            be between 0 (inclusive) and current source size (inclusive).
	 * @param object The <tt>DataObject</tt> to be added.
	 */
	protected void add(int index, DataObject object){
		data.add(index, object);
		fireDataAdded(index, index);
	}

	/**
	 * Adds the <tt>DataObject</tt> to the end of this data source.
	 * 
	 * @param objects	The <tt>DataObject</tt>s to be added.
	 */
	protected void addAll(DataObject[] objects){
		addAll(data.size(), objects);
	}
	
	/**
	 * Inserts the <tt>DataObject</tt> at the given index of this data source.
	 * 
	 * @param index The index at which to insert the <tt>DataObject</tt>, must
	 *            be between 0 (inclusive) and current source size (inclusive).
	 * @param objects The <tt>DataObject</tt>s to be added,
	 */
	protected void addAll(int index, DataObject[] objects){
		if(objects.length == 0) return;
		
		List<DataObject> addList = new ArrayList<DataObject>(objects.length);
		for(DataObject object : objects)
			addList.add(object);
		
		addAll(index, addList);
	}
	
	/**
	 * Adds all the <tt>DataObject</tt> from the <tt>Collection</tt> to the end
	 * of this data source.
	 * 
	 * @param collection The <tt>Collection</tt> containing the
	 *            <tt>DataObject</tt> to be added.
	 */
	protected void addAll(Collection<? extends DataObject> collection){
		addAll(data.size(), collection);
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
		if(collection.size() == 0) return;
		data.addAll(index, collection);
		fireDataAdded(index, index + collection.size() - 1);
		
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
		DataObject object = get(index);
		data.remove(index);
		fireDataRemoved(index, index, new DataObject[]{object});
		return object;
	}
	
	/**
	 * Removes the given <tt>DataObject</tt> from this data source.
	 * 
	 * @param object The <tt>DataObject</tt> to be removed.
	 * @return <tt>true</tt> If the given <tt>DataObject</tt> was contained in
	 *         this data source and has been removed.
	 */
	protected boolean remove(DataObject object){
		int index = data.indexOf(object);
		if(index == -1) return false;
		remove(index);
		return true;
	}
	
	/**
	 * Removes all the <tt>DataObject</tt>s found in the given
	 * <tt>Collection</tt> from this data source.
	 * 
	 * @param collection The <tt>Collection</tt> containing the
	 *            <tt>DataObject</tt>s to be removed.
	 * @return <tt>true</tt> if this data source was changed as a result of this
	 *         call.
	 */
	protected boolean removeAll(Collection<? extends DataObject> collection){
		//	remove DataObjects one by one because we don't know how ordered they are in the
		//	data List, which makes it impossible to fire an event with appropriate indices
		boolean rVal = false;
		for(DataObject object : collection)
			rVal = remove(object);
		
		return rVal;
	}
	
	/**
	 * Removes all <tt>DataObject</tt>s found in the specified range from this
	 * <tt>DataSource</tt>.
	 * 
	 * @param index0 The start of the range, inclusive.
	 * @param index1 The end of the range, inclusive.
	 */
	protected void removeRange(int index0, int index1){
		List<DataObject> subList = data.subList(index0, index1 + 1);
		DataObject[] removedData = subList.toArray(new DataObject[subList.size()]);
		//	remove listeners
		subList.clear();
		fireDataRemoved(index0, index1, removedData);
	}
	
	/**
	 * Removes all the <tt>DataObject</tt>s from this data source.
	 */
	protected void clear(){
		int size = size();
		if(size > 0)
			removeRange(0, size - 1);
	}

}
