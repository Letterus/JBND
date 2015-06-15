package org.jbnd.data;

import org.jbnd.DataObject;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.event.DataSourceListener;


/**
 * A <tt>DataSource</tt> that proxies data from another <tt>DataSource</tt>, but
 * never allows it's size to exceed a given limit.
 * <p>
 * For a target <tt>DataSource</tt> that exceeds it's limit, the
 * <tt>LimitingDataSource</tt> can proxy either the first n or the last n number
 * of records (where n is the limit). This is controlled through a
 * <tt>boolean</tt> accepting constructor. The default behavior is to provide
 * the first n records.
 * 
 * @version 1.0 Dec 4, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public final class LimitingDataSource extends AbstractDataSource implements
		ProxyingDataSource, DataSourceListener{
	
	// the maximum number of DataObjects this DataSource can provide
	private final int limit;
	
	// the DataSource this limiting data source gets it's data from
	private final DataSource targetDS;
	
	// if proxying the first n record, or the last n records (n == limit)
	private final boolean fromBeginning;
	
	/**
	 * Creates a <tt>LimitingDataSource</tt> with the given parameters.
	 * 
	 * @param target The <tt>DataSource</tt> this <tt>LimitingDataSource</tt>
	 *            gets it's data from.
	 * @param limit The maximum number of <tt>DataObject</tt>s this
	 *            <tt>DataSource</tt> can provide.
	 * @param fromBeginning If this <tt>LimitingDataSource</tt> should provide
	 *            the first n records (as opposed to the last n records), where
	 *            n == limit.
	 */
	public LimitingDataSource(DataSource target, int limit, boolean fromBeginning){
		if(limit <= 0)
			throw new IllegalArgumentException("Limit must be greater then 0");
		this.limit = limit;
		
		target.addDataSourceListener(this);
		targetDS = target;
		
		this.fromBeginning = fromBeginning;
	}
	
	/**
	 * Creates a <tt>LimitingDataSource</tt> with the given parameters.
	 * 
	 * @param target The <tt>DataSource</tt> this <tt>LimitingDataSource</tt>
	 *            gets it's data from.
	 * @param limit The maximum number of <tt>DataObject</tt>s this
	 *            <tt>DataSource</tt> can provide.
	 */
	public LimitingDataSource(DataSource target, int limit){
		this(target, limit, true);
	}

	public DataObject get(int index){
		// start?
		if(fromBeginning)
			return targetDS.get(index);
		
		// if the targetDS's size does not exceed  the limit
		// then the objects are in the same position
		int targetSize = targetDS.size();
		if(targetSize <= limit) return targetDS.get(index);
		
		// anchor is END, and target size exceeds limit
		return targetDS.get(index + targetSize - limit);
	}

	public int size(){
		int targetSize = targetDS.size();
		return Math.min(targetSize, limit);
	}

	public DataSource getTargetDataSource(){
		return targetDS;
	}

	public void dataAdded(DataSourceEvent event){
		
		int start = event.getIndex0(), end = event.getIndex1();
		
		// check if the target size exceeds the limit
		int targetSize = targetDS.size();
		int oldSize = targetSize - event.getData().length;
		if(targetSize <= limit){
			fireDataChanged(start, end, event.getData());
			return;
		}
		
		// the target size exceeds the limit
		
		if(fromBeginning){
			if(start < limit)
				fireDataAdded(start, Math.min(end, limit - 1));
		
		}else{
			int size = size();
			int sizeChange = Math.max(oldSize - limit, 0);
			if(sizeChange > 0)
				fireDataAdded(size - (sizeChange), size - 1);
			
			fireDataChanged(0, size - sizeChange - 1);
		}
	}

	public void dataChanged(DataSourceEvent event){
		
		int start = event.getIndex0(), end = event.getIndex1();
		
		// check if the target size exceeds the limit
		int targetSize = targetDS.size();
		if(targetSize <= limit){
			fireDataChanged(start, end, event.getData());
			return;
		}
		
		// the target size exceeds the limit
		
		if(fromBeginning){
			if(start < limit)
				fireDataChanged(start, Math.min(end, limit - 1));
		
		}else{
			int diff = targetSize - limit;
			if(end >= diff)
				fireDataChanged(Math.max(diff, start), end);
		}
	}

	public void dataRemoved(DataSourceEvent event){
		
		int start = event.getIndex0(), end = event.getIndex1();
		
		// check if the target size exceeds the limit
		int oldSize = targetDS.size() + event.getData().length;
		if(oldSize <= limit){
			fireDataChanged(start, end, event.getData());
			return;
		}
		
		// old target size exceeds the limit
		
		if(fromBeginning){
			if(start < limit){
				
				int index0 = start, index1 = Math.min(end, limit - 1);
				
				// translate the event data to data removed from this DataSource
				DataObject[] removedData = new DataObject[index1 - index0 + 1];
				DataObject[] eventData = event.getData();
				for(int i = 0 ; i < removedData.length ; i++)
					removedData[i] = eventData[i];
				
				fireDataRemoved(index0, index1, removedData);
			}
		
		}else{
			int diff = oldSize - limit;
			if(end >= diff){
				
				int index0 = Math.max(diff, start), index1 = end;
				
				// translate the event data to data removed from this DataSource
				DataObject[] removedData = new DataObject[index1 - index0 + 1];
				DataObject[] eventData = event.getData();
				int offset = eventData.length - removedData.length;
				for(int i = 0 ; i < removedData.length ; i++)
					removedData[i] = eventData[i + offset];
				
				fireDataChanged(index0, index1, event.getData());
			}
		}
		
	}
}