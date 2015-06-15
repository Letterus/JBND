package org.jbnd.binding;

import org.jbnd.DataObject;
import org.jbnd.ValidationException;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.undo.DataObjectUndoable;


/**
 * A <tt>Binding</tt> implementation that sets the provided values on all the
 * <tt>DataObject</tt>s it comes in contact with, for the given set of keys.
 * Used for example in situations when always the same value should be set on
 * <tt>DataObject</tt>s for a certain key, without the need to ask the user to
 * provide them; this <tt>Binding</tt> will take care that happens behind the
 * scenes.
 * <p>
 * Example: In a certain part of the user interface new 'person' type records
 * are added, that are always of the 'administrator' flavor. An instance of the
 * <tt>ConstantValueBinding</tt> could be used with the 'personFlavor' key, and
 * the 'admin' value. That way the right value will be set on the new
 * <tt>DataObject</tt>s, without the need for the user to provide it.
 * <p>
 * The <tt>ConstantValueBinding</tt> does not perform the editing of values
 * itself, it needs to connect to another <tt>Binding</tt> implementation that
 * takes care of that, the <tt>ConstantValueBinding</tt> will use it to attempt
 * to edit the <tt>DataObject</tt>s with it's value.
 * 
 * @version 1.0 Feb 29, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public final class ConstantValueBinding extends AbstractBinding{
	
	/**
	 * The default validation handler used by this implementation of
	 * <tt>Binding</tt>, it is never used.
	 */
	private static final ValidationHandler _DEFAULT_VALIDATION_HANDLER = 
		new DefaultValidationHandler();

	// the binding used by this ConstantValueBinding to
	// set the values
	private final Binding binding;

	// the values that will be set on the DataObjects for the bindings
	// keys
	private final Object[] values;
	
	/**
	 * Creates a <tt>ConstantValueBinding</tt> that uses the given
	 * <tt>Binding</tt> to set the given <tt>value</tt> for the given
	 * <tt>key</tt> on all the <tt>DataObject</tt>s that pass through it.
	 * 
	 * @param binding The <tt>Binding</tt> to use to attempt to edit the
	 *            <tt>DataObject</tt>s.
	 * @param value The value to set on all the <tt>DataObject</tt>s passing
	 *            through this <tt>Binding</tt> for the key in the
	 *            <tt>Binding</tt>.
	 */
	public ConstantValueBinding(Binding binding, Object value){
		this(binding, new Object[]{value});
	}
	
	/**
	 * Creates a <tt>ConstantValueBinding</tt> that uses the given
	 * <tt>Binding</tt> to set the given <tt>values</tt> for the given
	 * <tt>keys</tt> on all the <tt>DataObject</tt>s that pass through it.
	 * A set of <tt>DataObject</tt> keys to be used with the
	 * <tt>ConstantValueBinding</tt>, all the <tt>DataObject</tt>s passing
	 * through this <tt>Binding</tt> will have the given <tt>values</tt> set
	 * on on them for the given keys. Thereby, the keys and values arrays need
	 * to contain the same number of objects.
	 * 
	 * @param binding The <tt>Binding</tt> to use to attempt to edit the
	 *            <tt>DataObject</tt>s.
	 * @param values The values to set on all the <tt>DataObject</tt>s
	 *            passing through this <tt>Binding</tt> for the keys in the
	 *            <tt>Binding</tt>.
	 */
	public ConstantValueBinding(Binding binding, Object[] values){
		super(binding, binding.getKeys(), _DEFAULT_VALIDATION_HANDLER);
		
		//	check there are as many values as there are keys
		if(binding.getKeys().length != values.length)
			throw new IllegalArgumentException(
			"The number of provided values needs to be equal to the number of keys in the Binding");
		
		binding.addDataSourceListener(this);
		this.binding = binding;
		this.values = values;
		
		setValues(0, binding.size() - 1);
	}

	/**
	 * Forwards the call to the <tt>Binding</tt> used by this
	 * <tt>ConstantValueBinding</tt>.
	 * 
	 * @param index The index of the sought <tt>DataObject</tt>.
	 * @return The <tt>DataObject</tt> for the given index.
	 */
	public DataObject get(int index){
		return binding.get(index);
	}
	
	/**
	 * Does nothing, as the <tt>ConstantValueBinding</tt> does not provide
	 * means of editing itself. 
	 * 
	 * @param index	Ignored.
	 * @param key	Ignored.
	 * @param value	Ignored.
	 */
	public void edit(int index, String key, Object value){}
	
	/**
	 * Returns <code>false</code> always, as the <tt>ConstantValueBinding</tt>
	 * does not provide means of editing itself.
	 * 
	 * @param index Ignored.
	 * @param key Ignored.
	 * @return <code>false</code>
	 */
	public boolean isEditable(int index, String key){
		return false;
	}

	/**
	 * Reacts to the event fired by the <tt>Binding</tt> used by this
	 * <tt>ConstantValueBinding</tt>, attempts to set the values for keys
	 * that this <tt>ConstantValueBinding</tt> manages on all the
	 * <tt>DataObject</tt>s covered by the event.
	 * 
	 * @param event The event fired by the <tt>Binding</tt> used by this
	 *            <tt>ConstantValueBinding</tt>.
	 */
	public void dataAdded(DataSourceEvent event){
		setValues(event.getIndex0(), event.getIndex1());
		fireDataAdded(event.getIndex0(), event.getIndex1(), event.getData());
	}
	
	/**
	 * Reacts to the event fired by the <tt>Binding</tt> used by this
	 * <tt>ConstantValueBinding</tt>, attempts to set the values for keys
	 * that this <tt>ConstantValueBinding</tt> manages on all the
	 * <tt>DataObject</tt>s covered by the event.
	 * 
	 * @param event The event fired by the <tt>Binding</tt> used by this
	 *            <tt>ConstantValueBinding</tt>.
	 */
	public void dataChanged(DataSourceEvent event){
		setValues(event.getIndex0(), event.getIndex1());
		fireDataChanged(event.getIndex0(), event.getIndex1(), event.getData());
	}
	
	/**
	 * Fires an identical event.
	 * 
	 * @param event The event fired by the <tt>Binding</tt> used by this
	 *            <tt>ConstantValueBinding</tt>.
	 */
	public void dataRemoved(DataSourceEvent event){
		fireDataRemoved(event.getIndex0(), event.getIndex1(), event.getData());
	}
	
	/**
	 * Attempts to edit the <tt>DataObject</tt>s found in the
	 * <tt>Binding</tt> used by this <tt>ConstantValueBinding</tt> between
	 * the given indices with the keys and values it uses.
	 * 
	 * @param startIndex	The start index, inclusive.
	 * @param endIndex	The end index, inclusive.
	 */
	private void setValues(int startIndex, int endIndex){
		
		// do not create undoables for this, as it's not a user action
		DataObjectUndoable.CREATE_UNDOABLES = false;
		
		for(int i = startIndex ; i <= endIndex ; i++){
			String[] keys = getKeys();
			for(int j = 0 ; j < keys.length ; j++){
				if(binding.isEditable(i, keys[j]))
					binding.edit(i, keys[j], values[j]);
			}
		}
		
		// reset the switch
		DataObjectUndoable.CREATE_UNDOABLES = true;
	}
	
	/**
	 * Validation handler used by this <tt>Binding</tt> implementation, does
	 * nothing, as it is never really used.
	 * 
	 * @version 1.0 Feb 29, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	private static class DefaultValidationHandler implements ValidationHandler{

		public boolean handleException(ValidationException exception)
				throws ValidationException{
			throw exception;
		}
	}
}