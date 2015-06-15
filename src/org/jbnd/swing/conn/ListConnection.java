package org.jbnd.swing.conn;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jbnd.DataObject;
import org.jbnd.binding.Binding;
import org.jbnd.support.JBNDUtil;
import org.jbnd.swing.BoundComboBoxModel;

/**
 * A concrete subclass of <tt>ComponentConnection</tt> used to connect
 * <tt>JList</tt>s to a single value in a JBND <tt>Binding</tt>.
 * <p>
 * This connection expects the list's selection to only ever be
 * <tt>DataObject</tt>s.
 * <p>
 * The value this component will extract from the list depends on the list's
 * selection mode. If it is <tt>ListSelectionModel.SINGLE_SELECTION</tt>,
 * then only the first selected <tt>DataObject</tt> will be returned,
 * otherwise a <tt>List</tt> of all selected <tt>DataObject</tt>s will be
 * returned.
 * 
 * @version 1.0 Apr 17, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see BoundComboBoxModel
 */
public class ListConnection extends ComponentConnection implements ListSelectionListener{

	/**
	 * Creates a <tt>ListConnection</tt> with the given parameters.
	 * 
	 * @param list The <tt>JList</tt> to connect to a binding.
	 * @param binding The <tt>Binding</tt> to connect it to.
	 */
	@Deprecated
	public ListConnection(JList list, Binding binding){
		super(list, binding);
		list.addListSelectionListener(this);
	}
	
	protected void bindingValueChanged(Object newValue){
		//	if the new value is a single data object
		//	make sure it is selected
		if(newValue instanceof DataObject)
			((JList)component).setSelectedValue(newValue, true);
		
		
		//	if the new value is a list of objects
		//	make sure that is selected
		else if(newValue instanceof List){
			
			int[] selectedIndices = new int[((List<?>)newValue).size()];
			Iterator<?> dataObjects = ((List<?>)newValue).iterator();
			
			for(int i = 0; dataObjects.hasNext(); i++)
				selectedIndices[i] = JBNDUtil.indexOf((DataObject)dataObjects
						.next(), binding);
			
			((JList)component).setSelectedIndices(selectedIndices);
		}
	}

	protected Object getComponentValue(){
		//	if the tree is setup as single selection
		//	return the DataObject at the selected node
		if(((JList)component).getSelectionModel().getSelectionMode() == 
			ListSelectionModel.SINGLE_SELECTION)
			
			return ((JList)component).getSelectedValue();
			
		//	otherwise return DataObjects for all selected nodes
		else{
			
			List<DataObject> rVal = new LinkedList<DataObject>();
			Object[] selectedValues = ((JList)component).getSelectedValues();
			for(Object selectedValue : selectedValues)
				rVal.add((DataObject)selectedValue);
			
			return rVal;
		}
	}

	public void valueChanged(ListSelectionEvent e){
		componentValueChanged();
	}
}