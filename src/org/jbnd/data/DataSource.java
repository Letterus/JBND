package org.jbnd.data;

import org.jbnd.DataObject;
import org.jbnd.event.DataSourceListener;


/**
 * An interface for an object that is able to provide <tt>DataObject</tt>s. The
 * interface is defined in the form of an ordered list that provides a change
 * notification mechanism. It is implemented throughout JBND to imply that a
 * class can provide <tt>DataObject</tt>s in a standardized form.
 * <tt>DataSource</tt> instances are used by <tt>Binding</tt>s, to which they
 * provide data.
 * <p>
 * Note that the idea is that a <tt>DataSource</tt> will contain only one type
 * of <tt>DataObject</tt>s (<tt>DataObject</tt>s of the same <tt>DataType</tt>)
 * during the whole of it's lifetime. While this is most likely not enforced by
 * implementing classes, it may be assumed by classes that use them.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, Sep 17, 2007
 * @see DataSourceListener
 * @see org.jbnd.binding.Binding
 */
public interface DataSource{
	
	/**
	 * Provides the number of <tt>DataObject</tt>s <tt>this DataSource</tt> provides.
	 * 
	 * @return	The number of <tt>DataObject</tt>s <tt>this DataSource</tt> provides. 
	 */
	public int size();
	
	/**
	 * Returns the <tt>DataObject</tt> at the given index.
	 * 
	 * @param index
	 * @return The <tt>DataObject</tt> at the given index.
	 */
	public DataObject get(int index);
	
	/**
	 * Returns all the <tt>DataObject</tt>s <tt>this DataSource</tt> provides.
	 * 
	 * @return All the <tt>DataObject</tt>s <tt>this DataSource</tt> provides.
	 */
	public DataObject[] array();
	
	/**
	 * Registers the given <tt>listener</tt> for events fired by
	 * <tt>this DataSource</tt>.
	 * 
	 * @param listener The listener to be added.
	 */
	public void addDataSourceListener(DataSourceListener listener);
	
	/**
	 * Un-registers the given <tt>listener</tt> for events fired by
	 * <tt>this DataSource</tt>.
	 * 
	 * @param listener The listener to be removed.
	 */
	public void removeDataSourceListener(DataSourceListener listener);

}
