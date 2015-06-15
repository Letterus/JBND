//
//  AbstractFilter.java
//
//  Created by Florijan Stamenkovic on 2006 10 9.
//
package org.jbnd.qual;

import java.util.LinkedList;
import java.util.List;

import org.jbnd.event.FilterChangeListener;
import org.jbnd.event.FilterChangedEvent;


/**
 * The skeleton implementation of <tt>Filter</tt>, to be used in all situations
 * where the <tt>Filter</tt> subclass does not have to inherit another class.
 * <p>
 * <b>IMPORTANT: </b>Filter objects can be implemented as mutable, but should
 * call <tt>fireFilterChanged()</tt> after changing. This class fires events in
 * whichever <tt>Thread</tt> the <tt>fireFilterChanged()</tt> call was made.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, 10/16/06
 */
public abstract class AbstractFilter<T> implements Filter<T>{
	
	//	used as for firing events on listeners
	private final List<FilterChangeListener> filterChangeListeners = 
		new LinkedList<FilterChangeListener>();
	
	/**
	 * Adds the given listener object to the list of listeners.
	 *
	 *	@param	l	The listener to add
	 */
	public final void addFilterChangeListener(FilterChangeListener l){
		if(l == null) throw new NullPointerException("Can not add a null listener");
		filterChangeListeners.add(l);
	}
	
	/**
	 * Removes the given listener object from the list of listeners.
	 *
	 *	@param	l	The listener to register
	 */
	public final void removeFilterChangeListener(FilterChangeListener l){
		filterChangeListeners.remove(l);
	}
	
	/**
	 * Fires a "filterChanged" event on all registered listeners.
	 */
	protected final void fireFilterChanged(){
		
		FilterChangedEvent e = new FilterChangedEvent(this);
		FilterChangeListener[] listeners = filterChangeListeners
				.toArray(new FilterChangeListener[filterChangeListeners.size()]);
		for(int i = listeners.length - 1 ; i > - 1 ; i--)
			listeners[i].filterChanged(e);
	}
}
