package org.jbnd.event;

import java.util.EventListener;


/**
 * Implementing object can listen for <tt>FilterChangedEvent</tt>s, and process
 * them as needed.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, 10/07/06
 * @see org.jbnd.qual.Filter
 */
public interface FilterChangeListener extends EventListener{
	
	/**
	 * An event reporting that the <tt>Filter</tt> changed.
	 * 
	 * @param e The event
	 */
	public void filterChanged(FilterChangedEvent e);
}