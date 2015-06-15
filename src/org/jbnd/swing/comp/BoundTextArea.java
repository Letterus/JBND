package org.jbnd.swing.comp;

import javax.swing.JTextArea;

import org.jbnd.DataObject;
import org.jbnd.binding.Binding;
import org.jbnd.binding.Bound;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.support.NamingSupport;
import org.jbnd.support.ValueConverter;

/**
 * A <tt>TextArea</tt> that displays all of the <tt>DataObject</tt>s a
 * <tt>Binding</tt> provides, one in a row. If you want to display a single
 * property of a single <tt>DataObject</tt> in a text area use the
 * <tt>TextComponentConnection</tt> instead.
 * <p>
 * The way a <tt>DataObject</tt> is displayed in a row, formatted into a
 * <tt>String</tt> is defined by the
 * <tt>formatObjectWithKeys(DataObject, String[]</tt> method, which can be
 * overridden to perform custom formatting.
 * <p>
 * This component can not be used to edit <tt>DataObject</tt>s, because of 
 * that it is set to be non editable by default.
 * 
 * @version 1.0 Mar 12, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see	#formatObjectWithKeys(DataObject, String[])
 */
public class BoundTextArea extends JTextArea implements Bound{
	
	//	the binding this text area is bound to
	private Binding	binding;
	
	//	if or not this text area should display the keys
	//	before the values
	private boolean displayKeys = false;
	
	/**
	 * Creates a new <tt>BoundTextArea</tt> and sets it to be non editable.
	 */
	public BoundTextArea(){
		setEditable(false);
	}
	
	/**
	 * Creates a new <tt>BoundTextArea</tt> and sets it to be non editable.
	 * 
	 * @param binding The <tt>Binding</tt> this <tt>BoundTextArea</tt> is
	 *            connected to.
	 */
	public BoundTextArea(Binding binding){
		this();
		setBinding(binding);
	}
	
	/**
	 * Returns the <tt>Binding</tt> this <tt>BoundTextArea</tt> is
	 * bound to.
	 * 
	 * @return The <tt>Binding</tt> this <tt>BoundTextArea</tt> is
	 *         bound to.
	 */
	public Binding getBinding(){
		return binding;
	}

	/**
	 * Sets the <tt>Binding</tt> this <tt>BoundTextArea</tt> and takes
	 * care of updating the component.
	 * 
	 * @param binding The <tt>Binding</tt> to connect this
	 *            <tt>BoundTextArea</tt> to, <tt>null</tt> not
	 *            acceptable.
	 */
	public void setBinding(Binding binding){
		if(this.binding == binding) return;
		if(this.binding != null){
			this.binding.removeDataSourceListener(this);
		}
		
		//	perform all the binding related processes
		this.binding = binding;
		binding.addDataSourceListener(this);
		
		refresh();
	}

	/**
	 * Implementation of the <tt>DataSourceListener</tt> method, called when
	 * the <tt>Binding</tt> this <tt>BoundTextArea</tt> is bound to fires an
	 * event. Takes care of updating the component.
	 * 
	 * @param event The event fired by the <tt>Binding</tt>.
	 */
	public void dataAdded(DataSourceEvent event){
		refresh();
	}

	/**
	 * Implementation of the <tt>DataSourceListener</tt> method, called when
	 * the <tt>Binding</tt> this <tt>BoundTextArea</tt> is bound to fires an
	 * event. Takes care of updating the component.
	 * 
	 * @param event The event fired by the <tt>Binding</tt>.
	 */
	public void dataChanged(DataSourceEvent event){
		refresh();
	}

	/**
	 * Implementation of the <tt>DataSourceListener</tt> method, called when
	 * the <tt>Binding</tt> this <tt>BoundTextArea</tt> is bound to fires an
	 * event. Takes care of updating the component.
	 * 
	 * @param event The event fired by the <tt>Binding</tt>.
	 */
	public void dataRemoved(DataSourceEvent event){
		refresh();
	}

	/**
	 * The flag indicating if also the property keys should be displayed
	 * along with their properties in the <tt>BoundTextArea</tt>, used
	 * by the <tt>formatObjectWithKeys(DataObject, String[])</tt>.
	 * 
	 * @return See above.
	 */
	public boolean isDisplayKeys(){
		return displayKeys;
	}

	/**
	 * Sets the flag indicating if also the property keys should be displayed
	 * along with their properties in the <tt>BoundTextArea</tt>, used by the
	 * <tt>formatObjectWithKeys(DataObject, String[])</tt>.
	 * 
	 * @param displayKeys See above.
	 */
	public void setDisplayKeys(boolean displayKeys){
		if(displayKeys == this.displayKeys) return;
		this.displayKeys = displayKeys;
		refresh();
	}
	
	//	used by the formatObjectWithKeys(...) method
	private StringBuilder builder = new StringBuilder(512);
	
	/**
	 * This method defines how a <tt>DataObject</tt> will be displayed in a
	 * row, can be overridden to define custom formatting. The default
	 * implementation displays all the properties of the <tt>DataObject</tt>
	 * for the given <tt>keys</tt>, separated by commas. For example, for a
	 * Person type <tt>DataObject</tt> and an array of keys that is {"name",
	 * "age"}, this method will return something like "John Doe, 34". If the
	 * <tt>displayKeys</tt> flag is true the values will be prefixed by their
	 * key. For the given example this method would return "Name: John Doe, Age:
	 * 34".
	 * <p>
	 * Values are translated into <tt>String</tt>s using the JBND
	 * <tt>ValueConverter</tt>, and keys pre-formatted using the
	 * <tt>NamingSupport</tt> class.
	 * 
	 * @param object The <tt>DataObject</tt> to format into a <tt>String</tt>.
	 * @param keys The <tt>DataObject</tt> property keys found in the
	 *            <tt>Binding</tt> this <tt>BoundTextArea</tt> is bound to.
	 * @return See above.
	 */
	protected String formatObjectWithKeys(DataObject object, String[] keys){
		for(int i = 0 ; i < keys.length ; i++){
			//	append a comma between keys
			if(i != 0) builder.append(", ");
			//	append keys if required
			if(displayKeys){
				builder.append(NamingSupport.propertyName(keys[i]));
				builder.append(": ");
			}
			
			//	append the value
			builder.append(ValueConverter.toString(object.get(keys[i])));
		}
		
		String rVal = builder.toString();
		builder.setLength(0);
		return rVal;
	}
	
	/**
	 * Causes an update of the text of the component to reflect the
	 * state of the <tt>Binding</tt> it is bound to.
	 */
	private void refresh(){
		setText("");
		if(binding == null) return;
		
		//	append data objects
		String[] bindingKeys = binding.getKeys();
		int size = binding.size();
		for(int i = 0 ; i < size ; i++){
			if(i != 0) append("\n");
			append(formatObjectWithKeys(binding.get(i), bindingKeys));
		}
	}
}