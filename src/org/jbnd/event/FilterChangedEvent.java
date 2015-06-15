package org.jbnd.event;


import java.util.EventObject;

import org.jbnd.qual.Filter;


/**
 * An event type that is fired when a <tt>Filter</tt> changes it's criteria. The
 * source of the event is the <tt>Filter</tt> that changed.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, 10/17/06
 * @see org.jbnd.qual.Filter
 */
public final class FilterChangedEvent extends EventObject{

	public FilterChangedEvent(Filter<?> source){
		super(source);
	}
}