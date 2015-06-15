package org.jbnd.swing.conn;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jbnd.binding.Binding;
import org.jbnd.undo.DataObjectUndoable;

/**
 * A concrete subclass of <tt>ComponentConnection</tt> used to connect a
 * <tt>JSpinner</tt>'s current value to a single (possibly editable) value in
 * a JBND <tt>Binding</tt>.
 * <p>
 * <tt>JSpinner</tt>s support viewing / selecting many different classes
 * (numbers, text, random objects...). This connection is also not value class
 * specific, it can be used to view / select (and thereby edit) any class of
 * value.
 * 
 * @version 1.0 Dec 22, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class SpinnerConnection extends ComponentConnection implements ChangeListener{

	/**
	 * Creates a <tt>FormattedTextFieldConnection</tt> with the given parameters.
	 * 
	 * @param spinner	The <tt>JSpinner</tt> to connect.
	 * @param binding	The <tt>Binding</tt> to connect it to.
	 */
	@Deprecated
	public SpinnerConnection(JSpinner spinner, Binding binding){
		super(spinner, binding);
		spinner.addChangeListener(this);
	}

	protected void bindingValueChanged(Object value){
		//	set the value on the component
		if(value != null){
			((JSpinner)component).setValue(value);
			return;
		}
		
		//	for standard Swing SpinnerModels, find the lower bound value
		//	and set that as the current value,
		//	for anything else, use the current value
		SpinnerModel sm = ((JSpinner)component).getModel();
		Object lowestValue = sm.getValue();
		
		//	number model
		if(sm instanceof SpinnerNumberModel){ 
			Object lowestNumber = ((SpinnerNumberModel)sm).getMinimum();
			if(lowestNumber != null) lowestValue = lowestNumber;
		}
		
		//	list model
		if(sm instanceof SpinnerListModel){ 
			try{
				Object lowestItem = ((SpinnerListModel)sm).getList().get(0);
				if(lowestItem != null) lowestValue = lowestItem;
			}catch(Exception ex){}	//	model backed by an empty list, ignore it
			
		}
		
		//	date model
		if(sm instanceof SpinnerDateModel){ 
			Object lowestDate = ((SpinnerDateModel)sm).getStart();
			if(lowestDate != null) lowestValue = lowestDate;
		}
		
		//	set the lowest value on the spinner, do not create undoables
		DataObjectUndoable.CREATE_UNDOABLES = false;
		((JSpinner)component).setValue(lowestValue);
		componentValueChanged();
		DataObjectUndoable.CREATE_UNDOABLES = true;
	}

	public void stateChanged(ChangeEvent e){
		componentValueChanged();
	}

	protected Object getComponentValue(){
		return ((JSpinner)component).getValue();
	}
}