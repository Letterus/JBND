package org.jbnd.swing.x;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jbnd.binding.Binding;
import org.jbnd.support.ValueConverter;
import org.jbnd.swing.conn.ComponentConnection;
import org.jdesktop.swingx.JXDatePicker;


/**
 * A JBND <tt>ComponentConnection</tt> that binds SwingX <tt>JXDatePicker</tt>s.
 * 
 * @version 1.0 Nov 24, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class DatePickerConnection extends ComponentConnection implements
		PropertyChangeListener{

	@Deprecated
	public DatePickerConnection(JXDatePicker picker, Binding binding){
		super(picker, binding);
		picker.addPropertyChangeListener("date", this);
	}

	protected void bindingValueChanged(Object newValue){
		((JXDatePicker)component).setDate(ValueConverter.toDate(newValue));
	}

	protected Object getComponentValue(){
		return ((JXDatePicker)component).getDate();
	}

	public void propertyChange(PropertyChangeEvent evt){
		componentValueChanged();
	}
}