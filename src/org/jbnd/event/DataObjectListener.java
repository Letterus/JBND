package org.jbnd.event;

import java.util.EventListener;

import org.jbnd.DataObject;


/**
 * An interface for objects interested in listening for changes in
 * <tt>DataObject</tt>s.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, 10/15/07
 * @see DataObject
 * @see DataObjectEvent
 */
public interface DataObjectListener extends EventListener{
	
	/**
	 * Invoked on the listener when a <tt>DataObject</tt> has changed.
	 * 
	 * @param event	The event fired by the <tt>DataObject</tt>.
	 */
	public void objectChanged(DataObjectEvent event);
}
