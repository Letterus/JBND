package org.jbnd.data;

import org.jbnd.DataObject;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.event.DataSourceListener;


/**
 * A <tt>DataSource</tt> that proxies data from another <tt>DataSource</tt>, and
 * optionally inserts a <tt>null</tt> object in front of the proxied data.
 * <b>Note:</b> This class fires <tt>DataSourceEvent</tt>s that return valid
 * data form the <tt>getData()</tt> method as long as the target data source
 * does. However, on occasion this can be an array of a single <tt>null</tt>
 * object. Such an event is fired when <tt>this</tt> data source is asked to
 * stop inserting a <tt>null</tt> in front of it's data. In that case the
 * <tt>data</tt> of the fired event is always the {@link #NULL_ARRAY} object.
 * <p>
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, Sep 18, 2007
 */
public final class NullAddingDataSource extends AbstractDataSource 
	implements DataSourceListener, ProxyingDataSource{
	
	/**
	 * The array that is used as the <tt>data</tt> attribute of an event
	 * that was fired as a result of changing the <tt>insertNull</tt> setting.
	 */
	public static final DataObject[] NULL_ARRAY	= new DataObject[]{null};

	//	indicates if a null value should be inserted at the beginning of the data
	//	regardless of that null being there in the target model or not
	private boolean insertNull = true;
	
	//	the data source that data is proxied from
	private final DataSource targetDataSource;
	
	/**
	 * Creates a new <tt>NullAddingDataSource</tt> that by default inserts a
	 * <tt>null</tt> in front of the proxied data.
	 * 
	 * @param targetDataSource	The data source to proxy data from.
	 */
	public NullAddingDataSource(DataSource targetDataSource){
		targetDataSource.addDataSourceListener(this);
		this.targetDataSource = targetDataSource;
	}
	
	/**
	*	Sets the flag determining if or not a <tt>null</tt> value should be inserted
	*	at the beginning of this <tt>ListModel</tt>, in front of the filtered down objects.
	*	The model will be updated to the new setting after this method is called.
	*
		@param	insertNull	If or not a <tt>null</tt> should be inserted at the beginning of the model.
	*/
	public void setInsertNull(boolean insertNull){
		if(this.insertNull == insertNull) return;
		this.insertNull = insertNull;
		
		if(insertNull)
			fireDataAdded(0, 0, NULL_ARRAY);
		else
			fireDataRemoved(0, 0, NULL_ARRAY);
	}
	
	/**
	*	Gets the property determining if or not a <tt>null</tt> value should be inserted
	*	at the beginning of this <tt>DataSource</tt>, before the filtered down objects.
	*
		@return	If or not a <tt>null</tt> should be inserted at the beginning of the model
	*/
	public boolean getInsertNull(){
		return insertNull;
	}
	
	public DataObject get(int index){
		if(insertNull)
			return index == 0 ? null : targetDataSource.get(index - 1);
		else
			return targetDataSource.get(index);
	}

	public int size(){
		if(insertNull)
			return targetDataSource.size() + 1;
		else
			return targetDataSource.size();
	}

	public void dataAdded(DataSourceEvent event){
		if(insertNull)
			fireDataAdded(event.getIndex0() + 1, event.getIndex1() + 1, event.getData());
		else
			fireDataAdded(event.getIndex0(), event.getIndex1(), event.getData());
	}

	public void dataChanged(DataSourceEvent event){
		if(insertNull)
			fireDataChanged(event.getIndex0() + 1, event.getIndex1() + 1, event.getData());
		else
			fireDataChanged(event.getIndex0(), event.getIndex1(), event.getData());
	}

	public void dataRemoved(DataSourceEvent event){
		if(insertNull)
			fireDataRemoved(event.getIndex0() + 1, event.getIndex1() + 1, event.getData());
		else
			fireDataRemoved(event.getIndex0(), event.getIndex1(), event.getData());

	}

	public DataSource getTargetDataSource(){
		return targetDataSource;
	}
}