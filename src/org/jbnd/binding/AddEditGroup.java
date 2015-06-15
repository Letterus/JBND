package org.jbnd.binding;

import java.util.*;

import org.jbnd.AbstractDataObject;
import org.jbnd.DataObject;
import org.jbnd.DataType;
import org.jbnd.ValidationException;
import org.jbnd.data.AbstractDataSource;
import org.jbnd.data.DataSource;
import org.jbnd.event.DataObjectEvent;
import org.jbnd.event.DataObjectListener;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.event.DataSourceListener;
import org.jbnd.support.NamingSupport;
import org.jbnd.support.JBNDUtil;
import org.jbnd.undo.DataObjectUndoable;
import org.jbnd.undo.Undoable;
import org.jbnd.undo.AggregatedDataObjectUndoable.EventAggregator;


/**
 * A <tt>DataSource</tt> implementation that provides the capabilities to view
 * data, edit data (directly or in a deferred way), adding new data (in a
 * deferred way), or any combination of the above. In general however it is
 * pointless to use it to simply view data, a standard <tt>DataSource</tt> can
 * provide <tt>DataObject</tt>s for that purpose.
 * <p>
 * <h2>Editing data</h2> The <tt>AddEditGroup</tt> does the work of caching
 * deferred data edits, it does so through the <tt>CachingDataObject</tt>s,
 * which hold the deferred edit values until a time when the
 * <tt>AddEditGroup</tt> is asked to commit those values, or discard them
 * (cancel editing). The <tt>AddEditGroup</tt> also holds a flag to determine if
 * it should perform deferred editing, or direct editing (the
 * <tt>deferredEditing</tt> boolean). Depending on the value of that boolean,
 * the <tt>Binding</tt> implementation that uses this <tt>AddEditGroup</tt>
 * needs to perform editing directly on <tt>DataObject</tt>s involved, or on the
 * caches provided by the <tt>AddEditGroup</tt>. In that sense the
 * <tt>Binding</tt> implementation needs to 'understand' the
 * <tt>AddEditGroup</tt>, to make use of it's capabilities. The
 * {@link GPBinding} class, the <b>G</b>eneral <b>P</b>urpose binding of JBND,
 * is such an implementation, made specifically to work also with the
 * <tt>AddEditGroup</tt>.
 * <p>
 * This view / editing capability of the <tt>AddEditGroup</tt> can be used
 * independently of it's deferred adding capabilities. If that is what you need
 * to use it for, simply do not set a <tt>DataType</tt> on it, and avoid calling
 * adding specific methods.
 * <p>
 * Methods specific to the editing aspect of the <tt>AddEditGroup</tt>:
 * <ul>
 * <li>{@link #getEditedDataObject(int)}</tt>
 * <li>{@link #commitEdit()}</tt>
 * <li>{@link #validateForCommitEdit()}</tt>
 * <li>{@link #cancelEdit()}</tt>
 * <li>{@link #getDataSource()}</tt>
 * <li>{@link #setDataSource(DataSource)}</tt>
 * <li>{@link #getDeferredEditing()}</tt>
 * </ul>
 * <h2>Adding data</h2> This class also provides the possibility to create data
 * caches (<tt>CachingDataObject</tt>s) that represent objects that have not yet
 * been saved to the persistent data store. This makes it possible to edit and
 * store new data, partially validating it in the process, without messing with
 * the persistent store, or it's in-memory representation. The
 * <tt>AddEditGroup</tt> enables you doing this, and will use a given
 * <tt>DataType</tt> to perform validation of the cached data.
 * <p>
 * The deferred adding capability of the <tt>AddEditGroup</tt> can be used
 * independently of it's viewing / editing capabilities. If that is what you
 * need to use it for, simply do not set a <tt>DataSource</tt> on it, and avoid
 * calling editing specific methods.
 * <p>
 * Methods specific to the adding aspect of the <tt>AddEditGroup</tt>:
 * <ul>
 * <li>{@link #addNewDataObject()}</tt>
 * <li>{@link #addNewDataObject(int)}</tt>
 * <li>{@link #removeNewDataObject(int)}</tt>
 * <li>{@link #commitAdd(List)}</tt>
 * <li>{@link #cancelAdd()}</tt>
 * <li>{@link #validateForCommitAdd()}</tt>
 * <li>{@link #setSkipValidationOnProperties(String[])}</tt>
 * <li>{@link #getSkipValidationOnProperties()}</tt>
 * </ul>
 * <h2>Combined viewing / editing and adding</h2> The <tt>AddEditGroup</tt> can
 * perform both of the above explained functionalities simultaneously, thus
 * making it possible to easily provide the end user of the software a single
 * location to do all the CRUD capabilities. To use the <tt>AddEditGroup</tt> in
 * such a way, simply set both the <tt>DataSource</tt> (for providing data to be
 * viewed / edited) and a <tt>DataType</tt> (for validating newly created data)
 * on the group.
 * <p>
 * Methods provided to deal with both aspects of the group simultaneously:
 * <ul>
 * <li>{@link #cancel()}</tt>
 * <li>{@link #validateForCommit()}</tt>
 * <li>{@link #commit(List)}</tt>
 * </ul>
 * 
 * @version 1.0 Jun 16, 2008
 * @since 0.8
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public final class AddEditGroup extends AbstractDataSource implements
		DataObjectListener, DataSourceListener{
	
	//	the data source for viewing / editing
	private DataSource dataSource;	// TODO make field final, once setDataSource(...) is gone
	
	//	the place where deferred edits are contained
	private final Map<Integer, CachingDataObject> deferredEdits = 
		new HashMap<Integer, CachingDataObject>();
	
	//	the place where deferred adds are contained
	private final List<CachingDataObject> deferredAdds = new LinkedList<CachingDataObject>();
	
	//	the deferred editing flag
	private final boolean deferredEditing;
	
	//	the data type, used for validation of new data objects
	private final DataType dataType;

	
	
	
	/*
	 * 
	 * 
	 * 				--> CONSTRUCTORS <--
	 * 
	 * 
	 */
	
	/**
	 * Creates a new <tt>AddEditGroup</tt> that will be used for deferred
	 * adding. If you at a later time choose to use this <tt>AddEditGroup</tt>
	 * for viewing / editing as well, simply set a <tt>DataSource</tt> on it.
	 * 
	 * @param dataType The <tt>DataType</tt> of <tt>DataObject</tt>s that
	 *            will be created through this group. See {@link AddEditGroup}
	 *            for more info on what <tt>DataType</tt>s are used for.
	 */
	public AddEditGroup(DataType dataType){
		if(dataType == null)
			throw new IllegalArgumentException(
				"Can not create an AddEditGroup with a null DataType");
		this.dataType = dataType;
		this.deferredEditing = false;
	}
	
	/**
	 * Creates a new <tt>AddEditGroup</tt> that is backed by the given
	 * <tt>DataSource</tt>, to be used for viewing / editing. If you create
	 * an <tt>AddEditGroup</tt> using this constructor, you will not be able
	 * to use it for deferred adding. If you will need to do this, use some
	 * other constructor.
	 * 
	 * @param dataSource The <tt>DataSource</tt> this <tt>AddEditGroup</tt>
	 *            should proxy the data from.
	 * @param deferredEditing If this <tt>AddEditGroup</tt> should do deferred
	 *            editing or not.
	 */
	public AddEditGroup(DataSource dataSource, boolean deferredEditing){
		dataSource.addDataSourceListener(this);
		this.dataSource = dataSource;
		this.deferredEditing = deferredEditing;
		this.dataType = null;
	}
	
	/**
	 * Creates a new <tt>AddEditGroup</tt> to be used for both viewing /
	 * editing of records, and deferred adding of new ones.
	 * 
	 * @param dataSource The <tt>DataSource</tt> this <tt>AddEditGroup</tt>
	 *            should proxy the data from.
	 * @param deferredEditing If this <tt>AddEditGroup</tt> should do deferred
	 *            editing or not.
	 * @param dataType The <tt>DataType</tt> of <tt>DataObject</tt>s that
	 *            will be created through this group. See {@link AddEditGroup}
	 *            for more info on what <tt>DataType</tt>s are used for.
	 */
	public AddEditGroup(DataSource dataSource, boolean deferredEditing, DataType dataType){
		dataSource.addDataSourceListener(this);
		this.dataSource = dataSource;
		this.deferredEditing = deferredEditing;
		this.dataType = dataType;
	}
	
	
	
	
	
	
	/*
	 * 
	 * 
	 * 				--> DATASOURCE AND OTHER INTERFACE IMPLEMENTATION METHODS <--
	 * 
	 * 
	 */
	
	/**
	 * Returns the <tt>DataObject</tt> for the given index. If the
	 * <tt>DataObject</tt> at the index is one of the objects being viewed /
	 * edited, and there are no deferred edits for it yet, that same object is
	 * returned. If there are deferred edits for it a <tt>CachingDataObject</tt>
	 * encapsulating the original <tt>DataObject</tt> is returned. If the
	 * object at the given index is one used for deferred adding, a
	 * <tt>CachingDataObject</tt> that encapsulates no other
	 * <tt>DataObject</tt> is returned.
	 * 
	 * @param index The index of the sought <tt>DataObject</tt>.
	 * @return See above.
	 */
	public DataObject get(int index){
		int dataSourceSize = dataSource == null ? 0 : dataSource.size();
		
		//	depending on the index return one of the new objects
		//	or one of the possibly edited objects
		if(index >= dataSourceSize)
			return deferredAdds.get(index - dataSourceSize);
		else{
			CachingDataObject editedDataObject = deferredEdits.get(index);
			
			if(editedDataObject == null)
				return dataSource.get(index);
			else
				return editedDataObject;
		}
	}

	/**
	 * The number of <tt>DataObject</tt>s this <tt>DataSource</tt> implementation
	 * provides, that is the sum of objects that are being viewed / edited and those
	 * that are being added. 
	 * 
	 * @return	See above.
	 */
	public int size(){
		return (dataSource == null ? 0 : dataSource.size()) + deferredAdds.size();
	}

	/**
	 * Reacts to changes in deferred editing / adding caches (<tt>CachingDataObject</tt>s),
	 * and fires an appropriate <tt>DataSource</tt> event as a result
	 * 
	 * @param event The event caused by the change in one of the
	 *            <tt>CachingDataObject</tt>s this group manages.
	 */
	public void objectChanged(DataObjectEvent event){
		int index = JBNDUtil.indexOf(event.getDataObject(), this);
		fireDataChanged(index, index);
	}

	/**
	 * Implementation of <tt>DataSourceListener</tt>, called when the backing
	 * <tt>DataSource</tt> fires and event. Fires the same type of
	 * an event, notifying listeners of this <tt>AddEditGroup</tt> that data
	 * has been added.
	 * 
	 * @param event	The event fired by the backing <tt>DataSource</tt>.
	 */
	public void dataAdded(DataSourceEvent event){
		
		/*
		 * This method is called when data is added to the backing
		 * DataSource. It only affects deferred edits, so the following
		 * code only affects those. Deferred adds are not affected in
		 * any way.
		 */
		
		int start = event.getIndex0(), end = event.getIndex1();
		
		//	change the indices of the <tt>DataObject</tt>s coming after the range
		//	but only if the add did not occur at the end
		if(end != dataSource.size() - 1){
			
			Iterator<Integer> it = deferredEdits.keySet().iterator();
			//	temporary storage for edits for which the location index changed
			Map<Integer, CachingDataObject> tempMap = new HashMap<Integer, CachingDataObject>();
			//	the location index difference
			int difference = end - start + 1;
			while(it.hasNext()){
				Integer index = it.next();
				if(index >= start){
					//	if there is an deferred edit for an index that comes
					//	after the start of added data, it needs to have it's index changed
					tempMap.put(index + difference, deferredEdits.get(index));
					it.remove();
				}
			}
			deferredEdits.putAll(tempMap);
			
		}//	end of index change
		
		//	fire event
		fireDataAdded(start, end);

	}

	/**
	 * Implementation of <tt>DataSourceListener</tt>, called when the backing
	 * <tt>DataSource</tt> fires an event. Only fires the same type of
	 * an event, notifying listeners of this <tt>AddEditGroup</tt> that data
	 * has changed.
	 * 
	 * @param event	The event fired by the backing <tt>DataSource</tt>.
	 */
	public void dataChanged(DataSourceEvent event){
		fireDataChanged(event.getIndex0(), event.getIndex1());

	}

	/**
	 * Implementation of <tt>DataSourceListener</tt>, called when the backing
	 * <tt>DataSource</tt> fires and event. Fires the same type of
	 * an event, notifying listeners of this <tt>AddEditGroup</tt> that data
	 * has been removed.
	 * 
	 * @param event	The event fired by the backing <tt>DataSource</tt>.
	 */
	public void dataRemoved(DataSourceEvent event){
		
		/*
		 * This method is called when data is removed from the backing
		 * DataSource. It only affects deferred edits, so the following
		 * code only affects those. Deferred adds are not affected in
		 * any way.
		 */
		
		int start = event.getIndex0(), end = event.getIndex1();
		
		//	remove all the CachingDataObjects that fall into the range
		Iterator<Integer> it = deferredEdits.keySet().iterator();
		while(it.hasNext()){
			Integer index = it.next();
			if(index >= start && index <= end)
				it.remove();
		}
		
		//	change the indices of the <tt>DataObject</tt>s coming after the range
		it = deferredEdits.keySet().iterator();
		//	temporary storage for edits for which the location index changed
		Map<Integer, CachingDataObject> tempMap = new HashMap<Integer, CachingDataObject>();
		//	the location index difference
		int difference = end - start + 1;
		while(it.hasNext()){
			Integer index = it.next();
			if(index >= start){
				//	if there is a deferred edit for an index that comes
				//	after the removed data, it needs to have it's index changed
				tempMap.put(index - difference, deferredEdits.get(index));
				it.remove();
			}
		}
		deferredEdits.putAll(tempMap);
		
		//	fire an event!
		fireDataRemoved(start, end, event.getData());

	}
	
	
	
	
	
	/*
	 * 
	 * 
	 *		 	--> DEFERRED EDITING SPECIFIC METHODS <--
	 * 
	 * 
	 */
	
	
	/**
	 * Ensures there is a <tt>CachingDataObject</tt> (for deferred editing)
	 * created for the given index, and returns it.
	 * 
	 * @param index	The index for which a <tt>CachingDataObject</tt> is sought.
	 * @return	The sought <tt>CachingDataObject</tt>, if one is not created
	 * 			for the given index, it will be created by this method.
	 */
	public CachingDataObject getEditedDataObject(int index){
		
		if(dataSource == null || dataSource.size() <= index)
			throw new IndexOutOfBoundsException();
		
		CachingDataObject editedDataObject = deferredEdits.get(index);
		if(editedDataObject == null){
			editedDataObject = new CachingDataObject(dataSource.get(index));
			deferredEdits.put(index, editedDataObject);
		}
		
		return editedDataObject;
	}
	
	/**
	 * Commits all values of <tt>CachingDataObject</tt>s that are used for
	 * deferred editing to their backing <tt>DataObject</tt>s.
	 * <p>
	 * It is prudent to validate the values before, by calling
	 * <tt>validateForCommitEdits()</tt> on this <tt>AddEditGroup</tt>, and
	 * also after, by calling <tt>validateForSave()</tt> on individual edited
	 * <tt>DataObject</tt>s.
	 * <p>
	 * <b>IMPORTANT:</b> The <tt>AddEditGroup</tt> will clear itself of all
	 * deferred edits data after this method has been called, with the
	 * <tt>cancelEdits()</tt> method.
	 * 
	 * @see #validateForCommitEdit()
	 * @see #cancelEdit()
	 */
	public void commitEdit(){
		
		Set<Integer> indicesOfEdits = deferredEdits.keySet();
		if(indicesOfEdits.isEmpty()) return;
		
		// do we need to make an aggregated commit undoable?
		EventAggregator a = null;
		if(DataObjectUndoable.CREATE_FOR_DEFERRED_EDIT_COMMITS)
			a = new EventAggregator();
		
		// do we do individual commit undoables?
		DataObjectUndoable.CREATE_UNDOABLES = 
			DataObjectUndoable.CREATE_FOR_DEFERRED_PROP_COMMITS;
		
		// iterate through the caches, and commit each one
		for(Integer index : indicesOfEdits){
			CachingDataObject cdo = deferredEdits.get(index);
			
			// create an aggregated undoable if necessary
			if(a != null) cdo.dataObject.addDataObjectListener(a);
			
			// commit values
			cdo.commitEdit();
			
			if(a != null) cdo.dataObject.removeDataObjectListener(a);
		}
		
		// restore the switch
		DataObjectUndoable.CREATE_UNDOABLES = true;
		
		// if necessary create the aggregated commit undoable
		if(a != null){
			Undoable u = a.makeUndoable();
			if(u != null) DataObjectUndoable.MANAGER.add(u);
		}
			
		// clear deferred edits
		cancelEdit();
	}
	
	/**
	 * Validates the deferred edit values of all <tt>CachingDataObject</tt>s
	 * of this <tt>AddEditGroup</tt>. If this method returns <tt>null</tt>,
	 * then the deferred edit values are valid, individually. This does NOT
	 * guarantee that after the values have been set on a backing
	 * <tt>DataObject</tt> it will be valid for saving, even though that is
	 * mostly likely the case.
	 * <p>
	 * If this method does not return <tt>null</tt>, then it returns a
	 * <tt>Map</tt> in which the keys are indices of <tt>CachingDataObject</tt>s
	 * that failed validation, and the <tt>Map</tt> values are arrays of
	 * <tt>ValidationException</tt>s for all the keys on which validation
	 * failed. This combination provides all the information needed to pinpoint
	 * exactly the position of invalid data.
	 * 
	 * @return See above.
	 */
	public Map<Integer, ValidationException[]> validateForCommitEdit(){
		//	the map which stores info on which objects for which keys are invalid
		Map<Integer, ValidationException[]> rVal = null;
		
		Set<Integer> indicesOfEdits = deferredEdits.keySet();
		for(Integer index : indicesOfEdits){
			//	validate object
			ValidationException[] exceptions = deferredEdits.get(index).validateForCommitEdit();
			if(exceptions != null){
				if(rVal == null) rVal = new HashMap<Integer, ValidationException[]>();
				rVal.put(index, exceptions);
			}
		}
		
		return rVal;
	}
	
	/**
	 * Removes all the deferred edits from this <tt>AddEditGroup</tt>s.
	 */
	public void cancelEdit(){
		if(deferredEdits.isEmpty()) return;
		
		//	get the highest and lowest change index
		Set<Integer> indices = deferredEdits.keySet();
		int high = 0, low = Integer.MAX_VALUE;
		for(Integer index : indices){
			if(high < index) high = index;
			if(low > index) low = index;
		}
		
		//	clear the cached edits
		deferredEdits.clear();
			
		//	fire change event
		fireDataChanged(low, high);
	}
	
	/**
	 * Returns the <tt>DataSource</tt> that is backing this
	 * <tt>AddEditGroup</tt>, if there is one. It is possible that this
	 * <tt>AddEditGroup</tt> is only used for deferred adding of new objects,
	 * in that case this method will return <tt>null</tt>.
	 * 
	 * @return See above.
	 */
	public DataSource getDataSource(){
		return dataSource;
	}

	/**
	 * Sets the <tt>DataSource</tt> that is backing this <tt>AddEditGroup</tt>;
	 * this will remove all present deferred edits from the group.
	 * 
	 * @param dataSource The <tt>DataSource</tt> to back <tt>EditGroup</tt>,
	 *            can be <tt>null</tt> if this <tt>AddEditGroup</tt> will
	 *            only be used for deferred adding.
	 */
	@Deprecated
	public void setDataSource(DataSource dataSource){
		if(this.dataSource == dataSource) return;
		
		//	remove the old data source
		if(this.dataSource != null){
			this.dataSource.removeDataSourceListener(this);
			DataObject[] removedData = this.dataSource.array();
			int count = this.dataSource.size();
			this.dataSource = null;
			deferredEdits.clear();
			fireDataRemoved(0, count - 1, removedData);
		}
		
		//	add the new data source
		if(dataSource != null){
			this.dataSource = dataSource;
			dataSource.addDataSourceListener(this);
			fireDataAdded(0, dataSource.size());
		}
	}
	
	/**
	 * Gets the <tt>deferredEditing</tt> flag of this <tt>AddEditGroup</tt>,
	 * which defines if the editing performed by <tt>Bindings</tt> using this
	 * group should be performed immediately, or postponed until
	 * <tt>commit()</tt> or <tt>commitEdit()</tt> is called.
	 * <p>
	 * 
	 * @return The deferred editing flag.
	 * @see #commitEdit()
	 * @see #cancelEdit()
	 */
	public boolean getDeferredEditing(){
		return deferredEditing;
	}
	
	/**
	 * Returns a set of indices that point to <tt>DataObject</tt>s for which
	 * this <tt>AddEditGroup</tt> currently hold deferred edits.
	 * 
	 * @return	See above.
	 */
	public Set<Integer> deferredEditsIndices(){
		return deferredEdits.keySet();
	}
	
	
	
	
	
	
	
	/*
	 * 
	 * 
	 *		 	--> DEFERRED ADDING SPECIFIC METHODS <--
	 * 
	 * 
	 */
	
	/**
	 * Returns the <tt>DataType</tt> of this <tt>AddEditGroup</tt>, which
	 * can be <tt>null</tt> if this binding is not used for deferred adding.
	 * 
	 * @return See above.
	 */
	public DataType getDataType(){
		return dataType;
	}
	
	/**
	 * Returns the number of currently cached deferred adds.
	 * 
	 * @return See above.
	 */
	public int deferredAddsSize(){
		return deferredAdds.size();
	}
	
	/**
	 * Adds another <tt>CachingDataObject</tt> to the end of the collection of
	 * <tt>CachingDataObject</tt>s contained in this <tt>AddEditGroup</tt>,
	 * to be used for deferred adding. To use this functionality of the
	 * <tt>AddEditGroup</tt>, a <tt>DataType</tt> must be provided to it,
	 * otherwise a runtime exception will occur. For more info see
	 * {@link AddEditGroup}.
	 * 
	 * @return	The newly created <tt>CachingDataObject</tt>.
	 */
	public CachingDataObject addNewDataObject(){
		return addNewDataObject(size());
	}
	
	/**
	 * Adds another <tt>CachingDataObject</tt> at the given index of the
	 * collection of <tt>CachingDataObject</tt>s contained in this
	 * <tt>AddEditGroup</tt>, to be used for deferred adding. To use this
	 * functionality of the <tt>AddEditGroup</tt>, a <tt>DataType</tt> must be
	 * provided to it, otherwise a runtime exception will occur. For more info
	 * see {@link AddEditGroup}.
	 * 
	 * @param index The insertion location of the new <tt>CachingDataObject</tt>
	 *            , must position the new object after the <tt>DataObject</tt>s
	 *            being edited (viewed).
	 * @return The newly created <tt>CachingDataObject</tt>.
	 */
	public CachingDataObject addNewDataObject(int index){
		
		if(dataType == null)
			throw new IllegalStateException(
			"To use deferred adding in the AddEditGroup you must provide it with a DataType");
		
		CachingDataObject newDataObject = new CachingDataObject();
		
		int indexInAddCache = index - (dataSource == null ? 0 : dataSource.size());
		deferredAdds.add(indexInAddCache, newDataObject);
		
		fireDataAdded(index, index, new DataObject[]{newDataObject});
		
		return newDataObject;
	}
	
	/**
	 * Removes the <tt>CachingDataObject</tt> used for deferred adding, found
	 * at the given index, from the collection of <tt>CachingDataObject</tt>s
	 * contained in this <tt>AddEditGroup</tt>.
	 * 
	 * @param index The location of the <tt>CachingDataObject</tt> to be
	 *            removed, must indicate the new object and not a deferred edit
	 *            object.
	 */
	public void removeNewDataObject(int index){
		int indexInAddCache = index - (dataSource == null ? 0 : dataSource.size());
		CachingDataObject removedObject = deferredAdds.remove(indexInAddCache);
		fireDataRemoved(index, index, new DataObject[]{removedObject});
	}
	
	/**
	 * Commits all values of <tt>CachingDataObject</tt>s to the given
	 * <tt>DataObject</tt>s, which are just caches, but of the appropriate
	 * persistent class.
	 * <p>
	 * It is prudent to validate the values before, by calling
	 * <tt>validateForCommitAdd()</tt> on this <tt>AddEditGroup</tt>, and
	 * also after, by calling <tt>validateForSave()</tt> on individual newly
	 * created <tt>DataObject</tt>s.
	 * <p>
	 * <b>IMPORTANT:</b> The <tt>AddEditGroup</tt> will not clear itself of
	 * all deferred add caches after this method has been called. This makes it
	 * possible to use the already contained values in case the persistent class
	 * objects fed with the data from this group are not valid. Once it is sure
	 * they are valid, make a call to <tt>cancelAdd()</tt> to discard all the
	 * deferred add caches.
	 * 
	 * @param objects A <tt>List</tt> containing <tt>DataObject</tt>s to
	 *            which the <tt>CachingDataObject</tt>s of this
	 *            <tt>AddGroup</tt> should transfer their values.
	 * @throws IllegalArgumentException If the number of <tt>DataObject</tt>s
	 *             in the given list is not the same as the number of
	 *             deferred edit caches in this <tt>AddEditGroup</tt>.
	 * @see #validateForCommitAdd()
	 * @see #cancelAdd()
	 */
	public void commitAdd(List<? extends DataObject> objects){
		
		if(objects.size() != deferredAdds.size())
			throw new IllegalArgumentException(
			"commit() requires as many provided DataObjects"+
			" as the AddEditGroup has deferred add caches");
		
		// never generate undoables for commits of deferred adds
		DataObjectUndoable.CREATE_UNDOABLES = false;
		
		// commit cached values to the given DataObjects
		Iterator<? extends DataObject> persistentObjectIterator = objects.iterator();
		Iterator<CachingDataObject> newObjectIterator = deferredAdds.iterator();
		while(newObjectIterator.hasNext()){
			CachingDataObject ndo = newObjectIterator.next();
			DataObject pdo = persistentObjectIterator.next();
			ndo.commitAdd(pdo);
		}
		
		// restore the switch
		DataObjectUndoable.CREATE_UNDOABLES = true;
	}
	
	/**
	 * Does the same thing as {@link #commitAdd(List)}, just accepts
	 * the parameter in a different form.
	 * 
	 * @param objects See {@link #commitAdd(List)}
	 */
	public void commitAdd(DataObject... objects){
		commitAdd(Arrays.asList(objects));
	}
	
	/**
	 * Removes all the currently contained deferred add caches from this
	 * <tt>AddEditGroup</tt>.
	 */
	public void cancelAdd(){
		
		if(deferredAdds.size() == 0) return;
		
		DataObject[] removedData = deferredAdds
				.toArray(new DataObject[deferredAdds.size()]);
		
		deferredAdds.clear();
		
		int sizeWithoutAdds = size();
		fireDataRemoved(sizeWithoutAdds, sizeWithoutAdds + removedData.length
				- 1, removedData);
	}
	
	/**
	 * Validates the deferred add caches of this <tt>AddEditGroup</tt> against
	 * all keys found in the <tt>DataType</tt> of future <tt>DataObject</tt>s.
	 * If this method returns <tt>null</tt>, then the
	 * <tt>CachingDataObject</tt>'s values are valid, according to the
	 * <tt>DataType</tt> for all the properties it defines except those
	 * declared to be ignored through the
	 * <tt>setSkipValidationOnProperties(...)</tt> method. This does NOT
	 * guarantee that after the values have been set on a persistent class
	 * <tt>DataObject</tt> it will be valid for saving, even though that is
	 * mostly likely the case.
	 * <p>
	 * If this method does not return <tt>null</tt>, then it returns a
	 * <tt>Map</tt> in which the keys are indices of
	 * <tt>CachingDataObject</tt>s that failed validation, and the
	 * <tt>Map</tt> values are arrays of <tt>ValidationException</tt>s
	 * containing all the keys on which validation failed. This combination
	 * provides all the information needed to pinpoint exactly the position of
	 * invalid data.
	 * 
	 * @return See above.
	 */
	public Map<Integer, ValidationException[]> validateForCommitAdd(){
		//	counter
		int currentObject = 0;
		//	how many objects are there before the deferred adds?
		int editCount = dataSource == null ? 0 : dataSource.size();
		//	the map which stores info on which objects for which keys are invalid
		Map<Integer, ValidationException[]> rVal = null;
		
		//	loops through the new objects
		for(CachingDataObject newDataObject : deferredAdds){
			//	validate object
			ValidationException[] exceptions = newDataObject.validateForCommitAdd();
			if(exceptions != null){
				if(rVal == null) rVal = new HashMap<Integer, ValidationException[]>();
				rVal.put(editCount + currentObject, exceptions);
			}
			
			//	increment counter
			currentObject++;
		}
		
		return rVal;
	}
	
	//	an array of properties that should be skipped when
	//	validating the group
	private String[] skipValidationPorperties;
	
	/**
	 * Sets an array of keys which should be skipped when the
	 * <tt>AddEditGroup</tt> validates deferred adds for committing. Use with
	 * caution! This does not influence the validation of deferred edits.
	 * 
	 * @param keys An array of property keys, a single <tt>null</tt> argument is
	 *            interpreted as an empty array. If more then one argument is
	 *            passed, it is expected that none of the arguments is
	 *            <tt>null</tt>.
	 */
	public void setSkipValidationOnProperties(String... keys){
		if(keys == null || (keys.length == 1 && keys[0] == null))
			this.skipValidationPorperties = null;
		else
			this.skipValidationPorperties = keys;
	}
	
	/**
	 * Gets an array of keys which are be skipped when the
	 * <tt>AddEditGroup</tt> validates deferred adds for committing.
	 * 
	 * @return See above.
	 */
	public String[] getSkipValidationOnProperties(){
		return skipValidationPorperties;
	}
	
	
	
	
	
	
	/*
	 * 
	 * 
	 *		 	--> METHODS THAT DEAL WITH BOTH DEFERRED ADDING AND EDITING <--
	 * 
	 * 
	 */
	
	/**
	 * Removes all deferred adding / editing caches, thus effectively making
	 * this <tt>DataSource</tt> contain the same <tt>DataObject</tt>s as
	 * the backing <tt>DataSource</tt>, or (if there is no backing
	 * <tt>DataSource</tt>) making it empty.
	 * 
	 * @see #cancelAdd()
	 * @see #cancelEdit()
	 */
	public void cancel(){
		cancelEdit();
		cancelAdd();
	}
	
	/**
	 * Validates all the deferred adding / editing caches, and merges the results.
	 * 
	 * @see #validateForCommitAdd()
	 * @see #validateForCommitEdit()
	 * @return See related methods.
	 */
	public Map<Integer, ValidationException[]> validateForCommit(){
		Map<Integer, ValidationException[]> editValidation = validateForCommitEdit(),
			addValidation = validateForCommitAdd();
		
		if(editValidation == null)
			return addValidation;
		else if(addValidation != null){
			editValidation.putAll(addValidation);
			return editValidation;
		}else
			return null;
		
	}
	
	/**
	 * Commits all the deferred adding / editing caches to persistent
	 * <tt>DataObject</tt>s.
	 * 
	 * @see #commitAdd(DataObject[])
	 * @see #commitEdit()
	 * @param objects See the related <tt>commitAdd(DataObject[])</tt> method.
	 */
	public void commit(DataObject... objects){
		commitAdd(objects);
		commitEdit();
	}
	
	/**
	 * Commits all the deferred adding / editing caches to persistent
	 * <tt>DataObject</tt>s.
	 * 
	 * @see #commitAdd(List)
	 * @see #commitEdit()
	 * @param objects See the related <tt>commitAdd(List)</tt> method.
	 */
	public void commit(List<? extends DataObject> objects){
		commitAdd(objects);
		commitEdit();
	}
	
	/**
	 * Tests if the given <tt>CachingDataObject</tt> is contained in this
	 * <tt>AddEditGroup</tt>.
	 * 
	 * @param cdo
	 * @return See above.
	 */
	public boolean contains(CachingDataObject cdo){
		
		if(deferredEdits.containsValue(cdo))
			return true;
		if(deferredAdds.contains(cdo))
			return true;
		return false;
	}
	
	
	
	
	/*
	 * 
	 * 
	 * 	--> A DATA CACHE DATAOBJECT IMPLEMENTATION USED IN DEFERRED ADDING AND EDITING <--
	 * 
	 * 
	 */
	
	/**
	 * A specialized <tt>DataObject</tt> used by the <tt>AddEditGroup</tt> to
	 * cache data that is used in deferred editing and adding. If this object
	 * represents a deferred edit cache or a deferred add cache can be
	 * determined through the {@link #usedForEditing()} method.
	 * <p>
	 * Note that <tt>Bound</tt> implementations should only ever interact with
	 * the <tt>get(String)</tt> and <tt>toString()</tt> methods of
	 * <tt>CachingDataObject</tt>s (and of all other <tt>DataObject</tt>s for
	 * that matter). <tt>Binding</tt> implementations may interact with other
	 * methods, note however that some of the <tt>DataObject</tt> methods will
	 * throw <tt>UnsupportedOperationException</tt>s, and therefore should not
	 * be used.
	 * 
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 * @since 0.8
	 * @version 1.0, June 17 2008.
	 */
	public final class CachingDataObject extends AbstractDataObject implements
			DataObject.Deletable{
		
		//	the DataObject backing this cache, used only in deferred editing,
		//	and not in deferred adding, thereby possibly null at any time
		private final DataObject dataObject;
		
		//	the data cache
		private final Map<String, Object> edits = new HashMap<String, Object>();
		
		/**
		 * Creates a new <tt>CachingDataObject</tt> to be used for deferred
		 * editing, it will cache the changes that apply to the given
		 * <tt>DataObject</tt> until a time when it is asked (through the
		 * {@link #commit()} method) to push those changes into the given
		 * <tt>DataObject</tt>.
		 * 
		 * @param dataObject The object for which the changes are cached by this
		 *            <tt>CachingDataObject</tt>.
		 */
		private CachingDataObject(DataObject dataObject){
			this.dataObject = dataObject;
			if(dataObject == null) throw new IllegalArgumentException();
			
			addDataObjectListener(AddEditGroup.this);
		}

		/**
		 * Creates a new <tt>CachingDataObject</tt> to be used for deferred
		 * adding, it will cache the data until a time when it is asked (through
		 * the {@link #commit(DataObject)} method) to push those changes into
		 * another (presumably persistent) <tt>DataObject</tt>.
		 */
		private CachingDataObject(){
			addDataObjectListener(AddEditGroup.this);
			dataObject = null;
		}

		/**
		 * If this <tt>CachingDataObject</tt> is used or deferred editing then
		 * the <tt>DataObject</tt> for which this object is a cache is
		 * returned, otherwise <tt>null</tt> is returned.
		 * 
		 * @return See above.
		 */
		public DataObject getBackingDataObject(){
			return dataObject;
		}
		
		/**
		 * Indicates if or not this <tt>CachingDataObject</tt> is used for
		 * deferred editing. If it is not, then it is used for deferred adding.
		 * 
		 * @return See above.
		 */
		public boolean usedForEditing(){
			return dataObject != null;
		}

		/**
		 * Gets the value (possibly the deferred cache) for the given key.
		 * 
		 * @param key	The key :)
		 * @return	See above.
		 */
		public Object get(String key){
			
			//	special key handling
			if(DataObject.THIS_KEY.equals(key))
				return this;
			
			// if talking about a keypath, we need special handling
			if(JBNDUtil.isKeyPath(key)){
				
				// if there's an edit for the full path, return it
				if(edits.containsKey(key))
					return edits.get(key);
				
				// there is no edit for the full path, see if there are
				// edits for a subpath leading to it
				String pathStart = JBNDUtil.cleanPath(key);
				while(JBNDUtil.isKeyPath(pathStart)){
					
					pathStart = JBNDUtil.keyPathWithoutLastKey(pathStart);
					String after = key.substring(pathStart.length() + 1);
					
					// if we have an entry for the starting subpath, use it
					if(edits.containsKey(pathStart)){
						
						// we expect a DataObject to be there
						DataObject objectOnPath = (DataObject)edits.get(pathStart);
						if(objectOnPath != null)
							return JBNDUtil.get(objectOnPath, after);
						else
							return null;
					}
				}
				
				// it is a keypath, but we don't have a deferred edit for
				// anything on the path, so return the non-edit
				return usedForEditing() ? dataObject.get(key) : null;
			
			// we don't have a key path
			}else{
				
				if(usedForEditing() && !edits.containsKey(key))
					return dataObject.get(key);
				else
					return edits.get(key);
			}
		}

		/**
		 * Returns the <tt>DataType</tt> of this <tt>CachingDataObject</tt>.
		 * If this object is a cache for deferred editing then the
		 * <tt>DataType</tt> of the object for which it is caching the edits
		 * is returned; if it is a cache for deferred adding, then the
		 * <tt>DataType</tt> given to the <tt>AddEditGroup</tt> this object
		 * belongs to is returned.
		 * 
		 * @return See above.
		 */
		public DataType getDataType(){
			return usedForEditing() ? dataObject.getDataType() : dataType;
		}

		/**
		 * Always returns false.
		 * 
		 * @return	<tt>false</tt>
		 */		
		public boolean isPersistent(){
			return false;
		}

		/**
		 * Simply throws an <tt>UnsupportedOperationException</tt>, use the
		 * {@link #set(Object, String)} instead.
		 * 
		 * @param object	Ignored.
		 * @param key	Ignored.
		 */
		public void relate(DataObject object, String key){
			throw new UnsupportedOperationException(
			"This is a narrowed down DataObject implementation used by AddEditGroup");
		}
		
		/**
		 * Simply throws an <tt>UnsupportedOperationException</tt>, use the
		 * {@link #set(Object, String)} instead, passing it <tt>null</tt>.
		 * 
		 * @param object	Ignored.
		 * @param key	Ignored.
		 */
		public void unrelate(DataObject object, String key){
			throw new UnsupportedOperationException(
			"This is a narrowed down DataObject implementation used by AddEditGroup");
		}

		/**
		 * Caches the given <tt>value</tt> until a time this
		 * <tt>CachingDataObject</tt> will be asked to commit it.
		 * 
		 * @param value The new value for the given key.
		 * @param key The key :)
		 */
		public void set(Object value, String key){
			
			Object oldValue = get(key);
			if(JBNDUtil.equals(value, oldValue)) return;
			
			edits.put(key, value);
			fireDataObjectEvent(key, oldValue, DataObjectEvent.Type.CACHED_PROPERTY_CHANGE);
		}

		public String toString(){
			return usedForEditing() ?
					
					dataObject.toString()
					:
					"- New "+NamingSupport.typeName(dataType.name())+" record -";
		}
		
		/**
		 * Returns <tt>toString()</tt> provided value.
		 * 
		 * @return See above.
		 */
		public String toLogString(){
			return toString();
		}

		/**
		 * Attempts to validate and optionally coerce the given <tt>value</tt>
		 * for the given <tt>key</tt>. If this <tt>CachingDataObject</tt>
		 * is used for deferred editing the value will be validated against the
		 * backing <tt>DataObject</tt>. If this object is used for deferred
		 * adding, the value will be validated against the <tt>DataType</tt>
		 * of the <tt>AddEditGroup</tt> this object belongs to.
		 * 
		 * @param value The value to be validated.
		 * @param key The key for which it should be validated.
		 * @return The coerced <tt>value</tt>, that is quite possibly the
		 *         same object that was passed as <tt>value</tt>.
		 * @throws ValidationException If the <tt>value</tt> is not valid.
		 */
		public Object validate(Object value, String key)
				throws ValidationException{
			
			try{

				if(!usedForEditing()){
					//	deferred editing cache
					
					// some keypath handling
					if(JBNDUtil.isKeyPath(key)){
						DataType dataType = JBNDUtil.dataTypeForRelationship(getDataType(),
								JBNDUtil.keyPathWithoutLastKey(key));
						Object rVal = dataType.validate(value, JBNDUtil
								.lastKeyOfKeyPath(key));
						return rVal;
					}else
						return getDataType().validate(value, key);

				}else
					//	deferred adding cache
					return dataObject.validate(value, key);

			}catch(ValidationException ex){
				throw new ValidationException(value, key, this);
			}
		}

		/**
		 * Simply throws an <tt>UnsupportedOperationException</tt>.
		 * 
		 * @throws ValidationException	See above.
		 */
		public void validateToSave() throws ValidationException{
			throw new UnsupportedOperationException(
				"This is a narrowed down DataObject implementation used by AddEditGroup");
			
		}

		/**
		 * If this <tt>CachingDataObject</tt> is used for deferred editing
		 * then it asks the backing <tt>DataObject</tt> to validate for
		 * delete, otherwise does nothing.
		 * 
		 * @throws ValidationException
		 */
		public void validateToDelete() throws ValidationException{
			if(dataObject instanceof DataObject.Deletable) 
				((Deletable)dataObject).validateToDelete();
		}
		
		/**
		 * Returns the instance of <tt>AddEditGroup</tt> that is nesting this
		 * <tt>CachingDataObject</tt>.
		 * 
		 * @return See above.
		 */
		public AddEditGroup getNestingInstance(){
			return AddEditGroup.this;
		}
		
		/**
		 * Returns a shallow copy of the <tt>Map</tt> of edits of this
		 * <tt>CachingDataObject</tt>: editing the returned <tt>Map</tt> will
		 * not affect this <tt>CachingDataObject</tt>, but editing it's mutable
		 * content's will.
		 * 
		 * @return See above.
		 */
		public Map<String, Object> edits(){
			return new HashMap<String, Object>(edits);
		}
		
		/**
		 * Validates the currently cached values of this <tt>CachingDataObject</tt>
		 * against all keys found in the <tt>DataType</tt> of future
		 * <tt>DataObject</tt>s. If this method returns <tt>null</tt>,
		 * then the <tt>CachingDataObject</tt>'s values are valid, according to
		 * the <tt>DataType</tt>, for all the properties it defines. This
		 * does NOT guarantee that after the values have been set on a
		 * persistent class <tt>DataObject</tt> it will be valid for saving,
		 * even though that is mostly likely the case.
		 * <p>
		 * If this method does not return <tt>null</tt>, then it returns an
		 * array of <tt>ValidationException</tt>s, one for each key on which
		 * validation failed.
		 * <p>
		 * Besides checking for invalid values, this method uses the
		 * <tt>DataType.validate(Object value, String key)</tt> to coerce the
		 * value into it's proper form, if necessary. This is another reason why
		 * it is almost imperative to call this method just before calling
		 * <tt>commit(DataObject)</tt>.
		 * 
		 * @return See above.
		 * @see DataType#validate(Object, String)
		 */
		private ValidationException[] validateForCommitAdd(){
			
			/*
			 * We need a union of keys to validate on. One part of the union are
			 * all the keys of the DataType being added. The other part of the
			 * union all are all the keypaths for all the DataTypes that a
			 * keypath value is provided for. NOTE: this does not provide 100%
			 * validity, but it's the best we can do at this point in code.
			 */
			 
			// prepare the set of all the keys we'll need to validate on
			Set<String> allKeysToValidateOn = new HashSet<String>();
			
			// first add all the keys of the DataType being added
			for(String key : dataType.properties()) allKeysToValidateOn.add(key);
			
			
			
			
			//	when AddGroup is used with key paths, check that
			//	also for the records that will be related
			//	all the necessary values are provided
			Set<String> editsKeySet = edits.keySet();
			for(String key : editsKeySet){
				
				key = JBNDUtil.cleanPath(key);
				if(!JBNDUtil.isKeyPath(key)) continue;
				
				//	we have a key path
				//	get the related data type
				String withoutLastKey = JBNDUtil.keyPathWithoutLastKey(key);
				DataType relatedDataType = 
					JBNDUtil.dataTypeForRelationship(getDataType(), withoutLastKey);
				
				//	add all the keys from the related data type
				for(String relatedKey : relatedDataType.properties())
					allKeysToValidateOn.add(withoutLastKey+"."+relatedKey);
			}
			
			//	validate all the keys
			//	and return all the thrown exceptions
			List<ValidationException> rVal = null;
			for(String key : allKeysToValidateOn){
				
				//	if the add group is setup to skip certain keys
				//	then just continue the loop
				if(skipValidationPorperties != null && 
						JBNDUtil.indexOf(skipValidationPorperties, key, false) != -1)
					continue;
				
				try{
					//	perform validation / coercing
					Object coercedValue = validate(
							JBNDUtil.checkForEmptyString(edits.get(key)), key);
					//	put the coerced value as the edit
					if(editsKeySet.contains(key)) edits.put(key, coercedValue);
					
				}catch(ValidationException ex){
					//	invalid value detected, add it to the list
					if(rVal == null) rVal = new LinkedList<ValidationException>();
					rVal.add(ex);
				}
			}
			
			return rVal == null ? null : rVal.toArray(new ValidationException[rVal.size()]);
		}
		
		/**
		 * Validates the currently cached values of this
		 * <tt>CachingDataObject</tt> against the backing <tt>DataObject</tt>.
		 * If this method returns <tt>null</tt>, then the
		 * <tt>CachingDataObject</tt>'s deferred edits are valid, according to
		 * the backing object. This does NOT guarantee that after the values
		 * have been set on the backing <tt>DataObject</tt> it will be valid
		 * for saving, even though that is mostly likely the case.
		 * <p>
		 * If this method does not return <tt>null</tt>, then it returns an
		 * array of <tt>ValidationException</tt>s for all the keys on which
		 * validation failed.
		 * <p>
		 * Besides checking for invalid values, this method uses the
		 * <tt>backingObject.validate(Object value, String key)</tt> to coerce
		 * the value into it's proper form, if necessary. This is another reason
		 * why it is almost imperative to call this method just before calling
		 * <tt>commit(DataObject)</tt>.
		 * 
		 * @return See above.
		 * @see DataObject#validate(Object, String)
		 */
		private ValidationException[] validateForCommitEdit(){
			//	first validate individual values
			Set<String> allKeys = edits.keySet();
			
			List<ValidationException> rVal = null;
			for(String key : allKeys){
				try{
					//	perform validation / coercing
					Object coercedValue = 
						dataObject.validate(JBNDUtil.checkForEmptyString(edits.get(key)), key);
					//	put the coerced value as the edit
					edits.put(key, coercedValue);
				}catch(ValidationException ex){
					//	invalid value detected, add it to the list
					if(rVal == null) rVal = new LinkedList<ValidationException>();
					rVal.add(ex);
				}
			}
			
			return rVal == null ? null : rVal.toArray(new ValidationException[rVal.size()]);
		}
		
		/**
		 * Pushes all the cached values to the backing <tt>DataObject</tt>,
		 * without attempting to validate them. The setting of values is done
		 * with <tt>JBNDUtil.set(Object, String, DataObject)</tt>.
		 * <p>
		 * It is prudent to call <tt>validateForCommit()</tt> before calling
		 * this method, both to ensure the validity of the values, and to coerce
		 * them as necessary.
		 * 
		 * @see JBNDUtil#set(Object, String, DataObject)
		 * @see #validateForCommitEdit()
		 */
		private void commitEdit(){
			
			Map<String, Object> editsClone = new HashMap<String, Object>(edits);
			
			for(String key : editsClone.keySet())
				JBNDUtil.set(editsClone.get(key), key, dataObject);
		}
		
		/**
		 * Pushes all the cached values to the given <tt>DataObject</tt>,
		 * without attempting to validate them. The setting of values is done with
		 * <tt>JBNDUtil.set(Object, String, DataObject)</tt>.
		 * 
		 * It is prudent to call <tt>validateForCommitAdd()</tt> before calling
		 * this method, both to ensure the validity of the values, and to
		 * coerce them as necessary.
		 *
		 * @see	JBNDUtil#set(Object, String, DataObject)
		 * @see	#validateForCommitAdd()
		 */
		private void commitAdd(DataObject object){
			
			Map<String, Object> editsClone = new HashMap<String, Object>(edits);
			
			for(String key : editsClone.keySet())
				JBNDUtil.set(editsClone.get(key), key, object);
		}
	}
}