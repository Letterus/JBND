package org.jbnd.swing.conn;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import org.jbnd.binding.Binding;

/**
 * A concrete subclass of <tt>ComponentConnection</tt> used to connect a
 * <tt>JComboBox</tt>'s current selection to a single (possibly editable)
 * value in a JBND <tt>Binding</tt>.
 * <p>
 * <tt>JComboBox</tt>s support viewing / selecting many different classes
 * (numbers, text, random objects...). This connection is also not value class
 * specific, it can be used to view / select (and thereby edit) any class of
 * value. It is however necessary to make sure that the data type being viewed /
 * selected, is the same as the class of <tt>Object</tt>s provided through
 * the <tt>ComboBoxModel</tt>.
 * <p>
 * The <tt>ComboBoxConnection</tt> can be used to edit to-one relationships in
 * <tt>DataObject</tt>s, in that case a <tt>BoundComboBoxModel</tt> is
 * probably the most appropriate model for the connected <tt>ComboBox</tt>.
 * 
 * @version 1.0 Dec 22, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class ComboBoxConnection extends ComponentConnection implements ItemListener{

	/**
	 * Creates a <tt>FormattedTextFieldConnection</tt> with the given
	 * parameters.
	 * 
	 * @param comboBox The <tt>JComboBox</tt> to connect.
	 * @param binding The <tt>Binding</tt> to connect it to.
	 */
	@Deprecated
	public ComboBoxConnection(JComboBox comboBox, Binding binding){
		super(comboBox, binding);
		comboBox.addItemListener(this);
	}

	protected void bindingValueChanged(Object value){
		//	set the value on the component
		((JComboBox)component).setSelectedItem(value);
	}
	
	/**
	 * Overridden because the 'editable' property means something different in a
	 * <tt>JComboBox</tt> then in most other components.
	 */
	protected void syncEditability(){
		boolean editable = binding.size() > 0 && binding.isEditable(0, binding.getKeys()[0]);
		component.setEnabled(component.isEnabled() && editable);
	}

	public void itemStateChanged(ItemEvent e){
		
		/*
		 * XXX a workaround for the bug in SwingX's autocompletion where it is
		 * possible for an empty String to be set as the selected value of the
		 * combo box, even if such a value is illegal (i.e. not present in the
		 * model of a non-editable JComboBox).
		 */
		if("".equals(((JComboBox)component).getSelectedItem()))
				((JComboBox)component).setSelectedItem(null);
		else
			componentValueChanged();
	}

	protected Object getComponentValue(){
		return ((JComboBox)component).getSelectedItem();
	}
}