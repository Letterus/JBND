package org.jbnd.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jbnd.DataObject;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.event.DataSourceListener;
import org.jbnd.event.FilterChangeListener;
import org.jbnd.event.FilterChangedEvent;
import org.jbnd.qual.Filter;
import org.jbnd.support.JBNDUtil;

/**
 * A <tt>DataSource</tt> that proxies a filtered down set of <tt>DataObject</tt>
 * s from the given target <tt>DataSource</tt>. Does all the processing when the
 * target <tt>DataSource</tt> (objects are added, removed, or modified): updates
 * itself to add / remove data that was added / removed from the target model,
 * updates itself to include objects that were changed and now are accepted by
 * the <tt>Filter</tt>, and exclude objects when they are not anymore accepted
 * by the <tt>Filter</tt>.
 * <p>
 * It is possible to chain models, filtering them down along the chain, as well
 * as have multiple <tt>FilteringDataSource</tt>s tapping into the same target
 * model:
 * 
 * <pre>
 * 
 * //	target data source, actually containing objects
 * DataSource targetDataSource = new ListDataSource();
 * 
 * //	various filtered down models, only proxies to the target model
 * FilteringDataSource filterDownOnName = new FilteringDataSource(
 * 		targetDataSource, new NameFilter(&quot;John&quot;));
 * FilteringDataSource filterDownOnNumber = new FilteringDataSource(
 * 		targetDataSource, new NumberFilter(10));
 * 
 * //	a proxy to a proxy, will provide data filtered down both on name and number
 * FilteringDataSource filterDownOnBoth = new FilteringDataSource(
 * 		filterDownOnName, filterDownOnNumber.getFilter());
 * </pre>
 * <p>
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 2 Jan 09, 2009
 */
public final class FilteringDataSource extends AbstractNotListeningListDataSource
	implements FilterChangeListener, DataSourceListener, ProxyingDataSource{
	
	//	filter used by this list model
	private Filter<? super DataObject> filter;
	
	//	target data source, where data can be found, and filtered out
	private DataSource targetDataSource;
	
	
	/**
	 * Creates a new <tt>FilteringDataSource</tt> with the given parameters.
	 * 
	 * @param targetDataSource The data source to proxy <tt>DataObject</tt>s
	 *            from.
	 * @param filter The <tt>Filter</tt> used to determine which
	 *            <tt>DataObject</tt>s to proxy of the proxied data.
	 */
	public FilteringDataSource(DataSource targetDataSource, Filter<? super DataObject> filter){
		_setFilter(filter);
		_setTargetDataSource(targetDataSource);
		refilter();
	}
	
	/**
	 * Sets the <tt>Filter</tt> used to narrow down the data stored in the
	 * target model. This method will NOT refilter <tt>this</tt>, it is created
	 * to optimize performance.
	 * 
	 * @param filter The filter used no narrow down data from this model, if
	 *            <tt>null</tt> then <tt>Filter.ACCEPT_ALL</tt> is used.
	 */
	protected void _setFilter(Filter<? super DataObject> filter){
		//	make sure we don't have a null filter
		if(filter == null) filter = new Filter.ACCEPT_ALL<DataObject>();
		//	return if the filter didn't change
		if(this.filter == filter) return;
		
		//	remove this as a listener from previous filter
		if(this.filter != null)
			this.filter.removeFilterChangeListener(this);
		
		//	set value
		this.filter = filter;
		filter.addFilterChangeListener(this);
	}
	
	/**
	 * Sets the <tt>Filter</tt> used to narrow down the data stored in the
	 * target model. This method will refilter <tt>this</tt>, to update the
	 * model according to the new <tt>Filter</tt>.
	 * 
	 * @param filter The filter used no narrow down data from this model, if
	 *            <tt>null</tt> then <tt>Filter.ACCEPT_ALL</tt> is used.
	 * @see #refilter()
	 */
	public void setFilter(Filter<? super DataObject> filter){
		_setFilter(filter);
		refilter();
	}
	
	/**
	 * Gets the <tt>Filter</tt> used to narrow down the data stored in the
	 * target <tt>DataSource</tt>.
	 * 
	 * @return filter The filter used no narrow down data from this model.
	 */
	public Filter<? super DataObject> getFilter(){
		return filter;
	}
	
	/**
	 * <tt>FilterChangeListener</tt> implementation, called when the
	 * <tt>Filter</tt> used by <tt>this</tt> model fires a
	 * <tt>FilterChangedEvent</tt>; refilters the model.
	 * 
	 * @param e The event fired.
	 */
	public void filterChanged(FilterChangedEvent e){
		refilter();
	}
	
	/**
	 * Sets the target <tt>DataSource</tt> (the source data is proxied from),
	 * and refilters <tt>this</tt>.
	 * 
	 * @param targetDataSource The <tt>DataSource</tt> to proxy data from.
	 * @see #refilter()
	 */
	public void setTargetDataSource(DataSource targetDataSource){
		_setTargetDataSource(targetDataSource);
		refilter();
	}
	
	/**
	 * Sets the target <tt>DataSource</tt> (the source data is proxied from),
	 * without immediately refiltering <tt>this</tt>.
	 * 
	 * @param targetDataSource The <tt>DataSource</tt> to proxy data from.
	 */
	protected void _setTargetDataSource(DataSource targetDataSource){
		//	make sure we don't have a null model
		if(targetDataSource == null)
			throw new IllegalArgumentException("Can not use a null DataSource");
		
		//	return if the model didn't change
		if(this.targetDataSource == targetDataSource) return;
		
		//	remove this as a listener from the previous model
		if(this.targetDataSource != null)
			this.targetDataSource.removeDataSourceListener(this);
		
		//	set value
		this.targetDataSource = targetDataSource;
		targetDataSource.addDataSourceListener(this);
	}
	
	/**
	 * Gets the target <tt>DataSource</tt> (the source data is proxied from).
	 * 
	 * @return The <tt>DataSource</tt> the data is proxied from.
	 */
	public DataSource getTargetDataSource(){
		return targetDataSource;
	}
	
	/**
	 * Updates <tt>this DataSource</tt> to properly reflect the data found in
	 * the <tt>targetDataSource</tt>, filtered down according to the
	 * <tt>filter</tt>.
	 * 
	 * @see #setTargetDataSource(DataSource)
	 * @see #setFilter(Filter)
	 */
	protected void refilter(){
		
		clear();
		
		// cache the indexes of acceptable objects
		int targetSourceSize = targetDataSource.size();
		for(int i = 0 ; i < targetSourceSize ; i++){
			DataObject object = targetDataSource.get(i);
			if(filter.accept(object)) add(object);
		}
		
		// fire event
		int newSize = size();
		if(newSize > 0)
			fireDataAdded(0, newSize - 1);
	}
	
	/**
	 * A <tt>DataSourceListener</tt> method implementation, called when the
	 * <tt>targetDataSource</tt> fires a <tt>DataSourceEvent</tt> implying that
	 * it's contents changed; updates <tt>this</tt> to accept / deny objects
	 * that have changed, and fires appropriate events.
	 * 
	 * @param e The event fired by the <tt>targetDataSource</tt>.
	 */
	public void dataChanged(DataSourceEvent e){
		
		// target source info
		int startIndex = e.getIndex0(), endIndex = e.getIndex1();
		
		// iterate through the changed <tt>DataObject</tt>s, see where they fit, and handle
		for(int i = startIndex ; i <= endIndex ; i++){
			//	index of the current <tt>DataObject</tt> in this source
			int translatedIndex = translateToFilteredIndexAbsolute(i);
			if(size() <= translatedIndex
					|| get(translatedIndex) != targetDataSource.get(i))
				translatedIndex = -1;
			
			//	<tt>DataObject</tt> being reevaluated
			DataObject dataObject = targetDataSource.get(i);
			
			// if the item is not present in this data source
			if( translatedIndex == -1){
				// ...and it should be
				if(filter.accept(dataObject))
					// ..then add it!
					add(translateToFilteredIndexAbsolute(i), dataObject);
			
			// the item IS present in this list...
			}else{
				// ... but does not belong here
				if(!filter.accept(dataObject))
					remove(translatedIndex);
				//...the item is here, and does belong here, just fire an event
				else
					fireDataChanged(translatedIndex, translatedIndex,
							new DataObject[]{dataObject});
			}
		}
	}
	
	/**
	 * A <tt>DataSourceListener</tt> method implementation, called when the
	 * <tt>targetDataSource</tt> fires a <tt>DataSourceEvent</tt> implying that
	 * <tt>DataObject</tt>s were added to it, updates <tt>this</tt> to include
	 * <tt>DataObject</tt>s that were added and are accepted by the filter, and
	 * fires appropriate events.
	 * 
	 * @param e The event fired by the <tt>targetDataSource</tt>.
	 */
	public void dataAdded(DataSourceEvent e){
		
		DataObject[] added = e.getData();

		//	temporary storage for items that will be visible in this list
		LinkedList<DataObject> toAdd = new LinkedList<DataObject>();
		
		//	find which of the new items should also be visible in this list
		for(DataObject object : added)
			if(filter.accept(object))
				toAdd.add(object);
				
		//	determine where to add
		int translatedFirstIndex = translateToFilteredIndexAbsolute(e.getIndex0());

		// and add!
		addAll(translatedFirstIndex, toAdd);
	}
	
	/**
	 * A <tt>DataSourceListener</tt> method implementation, called when the
	 * <tt>targetDataSource</tt> fires a <tt>DataSourceEvent</tt> implying that
	 * <tt>DataObject</tt>s were removed from it, updates <tt>this</tt> to
	 * remove <tt>DataObject</tt>s that were proxied but were removed from the
	 * <tt>targetDataSource</tt>, and fires appropriate events.
	 * 
	 * @param e The event fired by the <tt>targetDataSource</tt>
	 */
	public void dataRemoved(DataSourceEvent e){
		
		List<Integer> indicesOfRemovedObjects = new ArrayList<Integer>(e.getData().length);
		
		// translate indices and cache them
		for(DataObject o : e.getData()){
			int indexInThis = JBNDUtil.indexOf(o, this);
			if(indexInThis != -1) indicesOfRemovedObjects.add(indexInThis);
		}
	
		// if none of the removed objects are filtered into this, there's nothing to do
		if(indicesOfRemovedObjects.size() == 0)
			return;
		
		// sort the indices
		Collections.sort(indicesOfRemovedObjects);
		
		removeRange(indicesOfRemovedObjects.get(0), indicesOfRemovedObjects
				.get(indicesOfRemovedObjects.size() - 1));
		
	}
	
	/**
	 * Translates the index of an object in the <tt>targetModel</tt> to the
	 * index of an object in <tt>this</tt>, to accommodate for the filtered out
	 * objects. If the given index points to an object in the
	 * <tt>targetModel</tt> that is NOT proxied in <tt>this</tt> model, then the
	 * position where that index <b>would</b> be, if it were in this model, is
	 * returned. For example, if <tt>this</tt> proxies objects that are in the
	 * <tt>targetModel</tt> found at indexes: 0,1,2,4,7,8 (implying that the
	 * target model contains minimum 9 objects, and <tt>this</tt> model 6
	 * objects), then index of 0 will translate to 0, index of 8 will translate
	 * to 5, and index of 3 will translate to 3, because that is the position it
	 * would have, if it was proxied. Used mostly to determine where objects
	 * that are not yet proxied should be "inserted".
	 * 
	 * @param index The index of an object in the <tt>targetModel</tt>
	 * @return The translated index, where the object needed is or would be
	 *         found in <tt>this</tt> model, guaranteed that it is <tt>>=
	 *         0</tt>.
	 */
	private int translateToFilteredIndexAbsolute(int indexInTarget){
		
		int low = 0;
		int high = size() - 1;
		
		while (low <= high){
			//	get the object in the middle of the list
			int mid = (low + high) >> 1;
			DataObject midVal = get(mid);
			int midValInTarget = JBNDUtil.indexOf(midVal, targetDataSource);
			
			//	see if the given object compares as being
			//	before or after the middle object
			int cmp = midValInTarget - indexInTarget;

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else{
				// found the position of the object in the targetDataSource
				return mid;
			}
		}
		
		return low;
	}
}