package org.jbnd.binding;

import org.jbnd.DataObject;
import org.jbnd.event.DataSourceListener;

/**
 * An interface for classes connecting to <tt>Binding</tt>s. Those can be
 * user interface classes, user interface model classes, or any functionalities
 * that need to interact with data on it's highest level (viewing and
 * manipulating values found in individual <tt>DataObject</tt>s).<p>
 * 
 * A <tt>Bound</tt> object must listent to <tt>DataSourceEvent</tt>s coming
 * from it's <tt>Binding</tt>, and behave accordingly.<p>
 * 
 * <b>Important:</b> <tt>Bound</tt> implementations should never edit the 
 * <tt>DataObject</tt>s directly, but do so through the binding object.<p>
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, Sep 20, 2007
 * @see Binding
 * @see DataObject
 *
 */
public interface Bound extends DataSourceListener{
	
	/**
	 * Sets the <ttt>Binding</tt> for this bound element.
	 * 
	 * @param binding	The binding.
	 */
	public void setBinding(Binding binding);
	
	/**
	 * Gets the <tt>Binding</tt> this element is bound to.
	 * 
	 * @return	The <tt>Binding</tt> this element is bound to.
	 */
	public Binding getBinding();
}
