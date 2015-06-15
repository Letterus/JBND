package org.jbnd.event;

import java.util.EventListener;


/**
 * A listener interface for objects interested in events fired by
 * <tt>DataSource</tt>s.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, Sep 17, 2007
 * @see DataSourceEvent
 */
public interface DataSourceListener extends EventListener{
	
	/**
	 * Implies that data managed by the <tt>DataSource</tt> has changed. 
	 * 
	 * @param event	The event object, contains info on the range of changed data.
	 */
	public void dataChanged(DataSourceEvent event);
	
	
	/**
	 * Implies that new data objects have been added to the <tt>DataSource</tt>.
	 * 
	 * @param event The event object, contains info on the range of added data.
	 */
	public void dataAdded(DataSourceEvent event);
	
	/**
	 * Implies that new data objects have been removed from the <tt>DataSource</tt>.
	 * 
	 * @param event The event object, contains info on the range of removed data.
	 */
	public void dataRemoved(DataSourceEvent event);

}
