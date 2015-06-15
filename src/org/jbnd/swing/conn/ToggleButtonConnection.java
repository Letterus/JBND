package org.jbnd.swing.conn;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JToggleButton;

import org.jbnd.binding.Binding;
import org.jbnd.support.ValueConverter;

/**
 * A concrete subclass of <tt>ComponentConnection</tt> used to connect
 * <tt>JToggleButton</tt>s (such as <tt>JCheckBox</tt> and
 * <tt>JRadioBox</tt>) to a single (possibly editable) value in a JBND
 * <tt>Binding</tt>.
 * <p>
 * The <tt>ToggleButtonConnection</tt> can also be used to display and edit
 * non <tt>boolean</tt> objects, but they must be convertible to and from
 * boolean values, according to the <tt>ValueConverter</tt> class.
 * 
 * @version 1.0 Dec 26, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see	ValueConverter
 */
public class ToggleButtonConnection extends ComponentConnection implements ItemListener{
	
	/**
	 * Creates a <tt>ToggleButtonConnection</tt> with the given parameters.
	 * 
	 * @param button The button to connect.
	 * @param binding The binding to connect it to.
	 */
	@Deprecated
	public ToggleButtonConnection(JToggleButton button, Binding binding){
		super(button, binding);
		button.addItemListener(this);
	}

	protected void bindingValueChanged(Object value){
		Boolean booleanValue = ValueConverter.toBoolean(value);
		
		//	set the value on the component
		((JToggleButton)component).setSelected(booleanValue);
	}

	protected Object getComponentValue(){
		return ((JToggleButton)component).isSelected();
	}

	public void itemStateChanged(ItemEvent e){
		componentValueChanged();
	}
}