//
//  Filter.java
//  Dvis
//
//  Created by Florijan Stamenkovic on 2006 10 16.
//  Copyright 2006 CNG Havaso Ltd. All rights reserved.
//
package org.jbnd.qual;

import org.jbnd.event.FilterChangeListener;


/**
 * Defines a simple filtering interface, can accept or reject an object, the
 * criteria for which must be defined in the implementing class. Normally you
 * should simply use <tt>AbstractFilter</tt> as a base of your filter.
 * <p>
 * <b>IMPORTANT:</b> Filter objects can be implemented as mutable, and should
 * after changing fire a <tt>FilterChangedEvent</tt> on all registered
 * <tt>FilterChangeListener</tt>s.
 * <p>
 * Use <tt>ACCEPT_ALL</tt> implementation whenever no actual filtering is
 * required. Use <tt>ACCEPT_NONE</tt> when all objects are to be filtered out.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.1, 12/07/06
 * @see AbstractFilter
 * @see ACCEPT_ALL
 */
public interface Filter<E> {
	
	/**
	 * A <tt>Filter</tt> in which the <tt>accept(Object)</tt> method always
	 * returns <tt>true</tt>.
	 */
	public static class ACCEPT_ALL<T> extends AbstractFilter<T>{
		public boolean accept(T o){
			return true;
		}

		public T[] filteredArray(T[] objects){
			return objects;
		}

		public T filteredObjectAt(T[] objects, int index){
			if(objects.length <= index)
				throw new ArrayIndexOutOfBoundsException(index);
			return objects[index];
		}
	};
		
	/**
	 * A <tt>Filter</tt> in which the <tt>accept(Object)</tt> method always
	 * returns <tt>false</tt>.
	 */
	public static class ACCEPT_NONE<T> extends AbstractFilter<T>{
		public boolean accept(T o){
			return false;
		}
		
		@SuppressWarnings("unchecked")
		public T[] filteredArray(T[] objects){
			return (T[])new Object[0];
		}

		public T filteredObjectAt(T[] objects, int index){
			throw new ArrayIndexOutOfBoundsException(index);
		}
	};
	
	/**
	 * Override this method with filtering logic.
	 * 
	 * @param object The object for which the <tt>Filter</tt> should determine
	 *            if it is acceptable or not.
	 * @return true or false
	 */
	public boolean accept(E object);
	
	/**
	 * Adds the given listener object to the list of listeners.
	 * 
	 * @param l The listener to add
	 */
	public void addFilterChangeListener(FilterChangeListener l);
	
	/**
	 * Removes the given listener object from the list of listeners.
	 * 
	 * @param l The listener to register
	 */
	public void removeFilterChangeListener(FilterChangeListener l);

}
