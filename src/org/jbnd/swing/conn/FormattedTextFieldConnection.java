package org.jbnd.swing.conn;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFormattedTextField;

import org.jbnd.binding.Binding;


/**
 * A concrete subclass of <tt>ComponentConnection</tt> used to connect a
 * <tt>JFormattedTextField</tt> to a single (possibly editable) value in a
 * JBND <tt>Binding</tt>.
 * 
 * @version 1.0 Dec 22, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class FormattedTextFieldConnection extends ComponentConnection implements PropertyChangeListener{

	/**
	 * Creates a <tt>FormattedTextFieldConnection</tt> with the given
	 * parameters.
	 * 
	 * @param formattedField The <tt>JFormattedTextField</tt> to connect.
	 * @param binding The <tt>Binding</tt> to connect it to.
	 */
	@Deprecated
	public FormattedTextFieldConnection(JFormattedTextField formattedField, Binding binding){
		super(formattedField, binding);
		formattedField.addPropertyChangeListener("value", this);
	}

	protected void bindingValueChanged(Object value){
		//	set the value on the component
		((JFormattedTextField)component).setValue(value);
	}

	public void propertyChange(PropertyChangeEvent evt){
		componentValueChanged();
	}

	protected Object getComponentValue(){
		return ((JFormattedTextField)component).getValue();
	}
}