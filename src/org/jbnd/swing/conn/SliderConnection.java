package org.jbnd.swing.conn;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jbnd.binding.Binding;
import org.jbnd.support.ValueConverter;

/**
 * A concrete subclass of <tt>ComponentConnection</tt> used to connect
 * <tt>JSlider</tt>s to a single (possibly editable) value in a JBND
 * <tt>Binding</tt>.
 * <p>
 * The <tt>SliderConnection</tt> can also be used to display and edit non
 * <tt>Integer</tt> objects, but they must be convertible to and from <tt>Integer</tt>
 * values, according to the <tt>ValueConverter</tt> class.
 * 
 * @version 1.0 Dec 26, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see	ValueConverter
 */
public class SliderConnection extends ComponentConnection implements ChangeListener{
	
	/**
	 * Creates a <tt>SliderConnection</tt> with the given parameters.
	 * 
	 * @param slider The slider to connect.
	 * @param binding The binding to connect it to.
	 */
	@Deprecated
	public SliderConnection(JSlider slider, Binding binding){
		super(slider, binding);
		slider.addChangeListener(this);
	}

	protected void bindingValueChanged(Object value){
		Integer integerValue = ValueConverter.toInteger(value);
		
		//	set the value on the component
		((JSlider)component).setValue(integerValue);
	}

	public void stateChanged(ChangeEvent e){
		if(((JSlider)component).getValueIsAdjusting()) return;
		componentValueChanged();
	}

	protected Object getComponentValue(){
		return ((JSlider)component).getValue();
	}
}