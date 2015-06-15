package org.jbnd.binding;

import org.jbnd.data.DataSource;
import org.jbnd.event.DataSourceListener;


/**
 * The binding object made to connect <tt>DataSource</tt>s and end consumers of
 * the data (<tt>Bound</tt> implementations).
 * <p>
 * Bindings do not provide <tt>DataObject</tt>s only, but also a set of (one or
 * more) keys to define which values in the <tt>DataObject</tt>s the consumers
 * should use.
 * <p>
 * <b>Data:</b> The <tt>Binding</tt> interface is defined as at the same time
 * being a data consumer and a data provider. What it does proxy all the data (
 * <tt>DataObject</tt>s) from it's <tt>DataSource</tt> to the <tt>Bound</tt>
 * object that is using it. In that sense it is similar to a typical
 * <tt>DataSource</tt>.
 * <p>
 * Even though editing methods are provided, it is not guaranteed that every
 * type of binding will enable editing capabilities. For exact functionalities
 * check the implementing class documentation. For a description of the editing
 * process, see {@link #edit(int, String, Object)}.
 * <p>
 * <b>Important:</b> <tt>Bound</tt> implementations should never edit the
 * <tt>DataObject</tt>s directly, but do so through the binding object.
 * <p>
 * <b>Validation:</b> When the <tt>edit(int, String, Object)</tt> method is
 * provided by invalid values it is possible that a validation exception will be
 * thrown. The <tt>edit(...)</tt> method however should not simply rethrow that
 * exception, but let the current <tt>ValidationHandler</tt> of the binding
 * handle it. It is possible that that will result in the rethrowing of the
 * exception, if that is how the <tt>ValidationHandler</tt> is implemented.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.1, Dec 12, 2007
 */
public interface Binding extends DataSourceListener, DataSource{
	
	/**
	 * Returns the keys (possibly key paths) used in this binding.
	 * 
	 * @return	Keys (possibly key paths) used in this binding.
	 */
	public String[] getKeys();
	
	/**
	 * Gets the <tt>ValidationHandler</tt> that is used by the binding to handle
	 * exceptions caused by attempted setting of invalid values.
	 * 
	 * @return The validation handler used by this binding.
	 */
	public ValidationHandler getValidationHandler();
	
	/**
	 * Sets the <tt>ValidationHandler</tt> to be used by the binding to handle
	 * exceptions caused by attempted setting of invalid values.
	 * 
	 * @param handler The validation handler to be used by this binding.
	 * @throws NullPointerException If the given <tt>handler</tt> is
	 *             <tt>null</tt>.
	 */
	public void setValidationHandler(ValidationHandler handler);
	
	
	/**
	 * Indicates if the <tt>DataObject</tt> at the given <tt>index</tt>
	 * binding provides is editable for the given key.
	 * 
	 * @param index The index of the <tt>DataObject</tt> for which editability
	 *            should be determined.
	 * @param key The key for which editability should be determined.
	 * @return See above.
	 */
	public boolean isEditable(int index, String key);
	
	/**
	 * Defines a standard editing method for binding classes, even though different
	 * implementations may perform editing quite differently, or not at all. See
	 * implementations for specifics.
	 * 
	 * @param index	The index of the <tt>DataObject</tt> of this binding that is to be edited.
	 * @param key	The key for which it is to be edited.
	 * @param value	The new value.
	 */
	public void edit(int index, String key, Object value);
}
