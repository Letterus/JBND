package org.jbnd.data;

import java.util.ArrayList;
import java.util.List;

import org.jbnd.DataObject;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.event.DataSourceListener;


/**
 * A base class for developing <tt>DataSource</tt> implementations. Takes care
 * of providing an event firing mechanism demanded by the interface.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.1, Dec 11, 2007
 */
public abstract class AbstractDataSource implements DataSource{

	//	event listeners
	private List<DataSourceListener> eventListeners;
	
	public void addDataSourceListener(DataSourceListener listener){
		if(eventListeners == null) eventListeners = new ArrayList<DataSourceListener>(5);
		eventListeners.add(listener);
	}

	/**
	 * Implemented to get the <tt>size()</tt> of the data source, and then
	 * add objects into an array of identical size, one by one. Subclasses
	 * may want to override this in favour of a more optimized implementation.
	 * 
	 * @return	All the <tt>DataObject</tt>s managed by <tt>this DataSource</tt>.
	 */
	public DataObject[] array(){
		int size = size();
		return size == 0 ? new DataObject[0] : range(0, size() - 1);
	}

	public abstract DataObject get(int index);

	public void removeDataSourceListener(DataSourceListener listener){
		if(eventListeners == null) return;
		eventListeners.remove(listener);

	}

	public abstract int size();
	
	/**
	 * Fires a <tt>DataSourceEvent</tt> of the <tt>DataSourceEvent.DATA_ADDED</tt> type
	 * on all registered listeners. The <tt>data</tt> property of the fired event
	 * will be generated from the given indices. Indices do not have to be provided
	 * lower first. Both indices are inclusive (the object found under the given index
	 * is considered a part of the event).
	 * 
	 * @param index0	The first index (not necessarily the lower number).
	 * @param index1	The second index (not necessarily the higher number).
	 */
	protected final void fireDataAdded(int index0, int index1){
		
		if(eventListeners == null || eventListeners.size() == 0) return;
		
		int start = Math.min(index0, index1);
		int end = Math.max(index0, index1);
		fireEvent(new DataSourceEvent(this, DataSourceEvent.Type.DATA_ADDED,
				start, end, range(start, end)));
	}
	
	/**
	 * Fires a <tt>DataSourceEvent</tt> of the <tt>DataSourceEvent.DATA_ADDED</tt> type
	 * on all registered listeners. Indices do not have to be provided
	 * lower first. Both indices are inclusive (the object found under the given index
	 * is considered a part of the event).
	 * 
	 * @param index0	The first index (not necessarily the lower number).
	 * @param index1	The second index (not necessarily the higher number).
	 * @param data		The data (an array of <tt>DataObject</tt>s) that was added.
	 */
	protected final void fireDataAdded(int index0, int index1, DataObject[] data){
		
		if(eventListeners == null || eventListeners.size() == 0) return;
		
		int start = Math.min(index0, index1);
		int end = Math.max(index0, index1);
		fireEvent(new DataSourceEvent(this, DataSourceEvent.Type.DATA_ADDED,
				start, end, data));
	}
	
	/**
	 * Fires a <tt>DataSourceEvent</tt> of the <tt>DataSourceEvent.DATA_REMOVED</tt> type
	 * on all registered listeners. Indices do not have to be provided
	 * lower first. Both indices are inclusive (the object found under the given index
	 * is considered a part of the event).
	 * 
	 * @param index0	The first index (not necessarily the lower number).
	 * @param index1	The second index (not necessarily the higher number).
	 * @param data		<tt>DataObject</tt>s that were removed, optional.
	 */
	protected final void fireDataRemoved(int index0, int index1, DataObject[] data){
		
		if(eventListeners == null || eventListeners.size() == 0) return;
		
		int start = Math.min(index0, index1);
		int end = Math.max(index0, index1);
		
		fireEvent(new DataSourceEvent(this, DataSourceEvent.Type.DATA_REMOVED,
				start, end, data));
	}
	
	/**
	 * Fires a <tt>DataSourceEvent</tt> of the <tt>DataSourceEvent.DATA_CHANGED</tt> type
	 * on all registered listeners. The <tt>data</tt> property of the fired event
	 * will be generated from the given indices. Indices do not have to be provided
	 * lower first. Both indices are inclusive (the object found under the given index
	 * is considered a part of the event).
	 * 
	 * @param index0	The first index (not necessarily the lower number).
	 * @param index1	The second index (not necessarily the higher number).
	 */
	protected final void fireDataChanged(int index0, int index1){
		
		if(eventListeners == null || eventListeners.size() == 0) return;
		
		int start = Math.min(index0, index1);
		int end = Math.max(index0, index1);
		fireEvent(new DataSourceEvent(this, DataSourceEvent.Type.DATA_CHANGED,
				start, end, range(start, end)));
	}
	
	/**
	 * Fires an <tt>DataSourceEvent</tt> of the <tt>DataSourceEvent.DATA_CHANGED</tt> type
	 * on all registered listeners. Indices do not have to be provided
	 * lower first. Both indices are inclusive (the object found under the given index
	 * is considered a part of the event).
	 * 
	 * @param index0	The first index (not necessarily the lower number).
	 * @param index1	The second index (not necessarily the higher number).
	 * @param data		The data (an array of <tt>DataObject</tt>s) that was changed.
	 */
	protected final void fireDataChanged(int index0, int index1, DataObject[] data){
		
		if(eventListeners == null || eventListeners.size() == 0) return;
		
		int start = Math.min(index0, index1);
		int end = Math.max(index0, index1);
		fireEvent(new DataSourceEvent(this, DataSourceEvent.Type.DATA_CHANGED,
				start, end, data));
	}

	/**
	 * Retrieves the defined range of <tt>DataObject</tt>s.
	 * 
	 * @param index0	The start index, inclusive.
	 * @param index1	The end index, inclusive.
	 * @return	<tt>DataObject</tt>s found in the stated range.
	 */
	protected final DataObject[] range(int index0, int index1){
		DataObject[] rVal = new DataObject[index1 - index0 + 1];
		for(int i = 0 ; i < rVal.length ; i++)
			rVal[i] = get(i + index0);
		
		return rVal;
	}

	/**
	 * Take care of invoking the appropriate listener method on all
	 * registered listeners. The appropriate method is determined by
	 * the <tt>type</tt> of the event.
	 * 
	 * @param event	The event object.
	 */
	private void fireEvent(DataSourceEvent event){
		//	iterate through the listeners 
		switch(event.getType()){
			case DATA_ADDED :{
				for(int i = eventListeners.size() - 1 ; i >= 0 ; i--)
					eventListeners.get(i).dataAdded(event);
				break;
			}
			case DATA_REMOVED :{
				for(int i = eventListeners.size() - 1 ; i >= 0 ; i--)
					eventListeners.get(i).dataRemoved(event);
				break;
			}
			case DATA_CHANGED :{
				for(int i = eventListeners.size() - 1 ; i >= 0 ; i--)
					eventListeners.get(i).dataChanged(event);
				break;
			}
			default:
				throw new IllegalStateException(
						"Unknown DataSourceListener event type: " + event);
		}//	 end of switch statement
	}
}
