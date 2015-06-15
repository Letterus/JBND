package org.jbnd.swing;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import org.jbnd.DataObject;
import org.jbnd.binding.Binding;
import org.jbnd.binding.Bound;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.support.JBNDUtil;


/**
 * A <tt>Bound</tt> implementation that provides <tt>DataObject</tt>s from it's
 * <tt>Binding</tt> as items in a <tt>ComboBoxModel</tt>, to be used by
 * <tt>JComboBox</tt> and <tt>JList</tt> GUI elements. If used with a
 * <tt>JList</tt>, the item selection methods will be useless, as a
 * <tt>JList</tt> manages it's selection differently.
 * <p>
 * <b>IMPORTANT:</b> If the <tt>Binding</tt> backing this <tt>Bound</tt> object
 * is changed, or the object that is the currently selected item is removed from
 * it, the currently selected item will be set to <tt>null</tt>. If this
 * <tt>BoundComboBoxModel</tt> is used in conjunction with a
 * <tt>ComboBoxConnection</tt>, where this model provides a list of potential
 * objects to relate a to-one relationship of another <tt>DataObject</tt> to,
 * this can be a problem. To avoid setting the <tt>null</tt> on a relationship
 * in such scenarios, use the deferred editing capability built into
 * <tt>EditGroup</tt> and <tt>EditBinding</tt>.
 * 
 * @version 1.0 Dec 22, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class BoundComboBoxModel extends AbstractListModel implements
		ComboBoxModel, Bound{
	
	//	the current selection
	private Object selectedObject;
	
	//	the binding of this bound object
	private Binding binding;
	
	/**
	 * Creates a <tt>BoundComboBoxModel</tt> that is connected to the
	 * given <tt>Binding</tt>.
	 * 
	 * @param	binding	The <tt>Binding</tt> this model is backed by.
	 */
	public BoundComboBoxModel(Binding binding){
		setBinding(binding);
	}
	
	/**
	 * Returns the current selection of this <tt>BoundComboBoxModel<tt>,
	 * which is always a <tt>DataObject</tt> from the <tt>Binding</tt>
	 * this model is bound to, or <tt>null</tt>. This method is irrelevant
	 * when using a <tt>BoundComboBoxModel</tt> with a <tt>JList</tt>, as
	 * <tt>JList</tt>s manage their selections differently.
	 * 
	 * @return	See above.
	 */
	public Object getSelectedItem(){
		return selectedObject;
	}

	/**
	 * Sets the currently selected item of the <tt>BoundComboBoxModel</tt>
	 * to the given <tt>object</tt>; this method will have no visible
	 * results if this model is used with a <tt>JList</tt>. If the given
	 * <tt>object</tt> is not found in the <tt>Binding</tt> that is backing
	 * this model, an exception is thrown. Thereby, <tt>BoundComboBoxModel</tt>s
	 * should never be used with editable <tt>JComboBox</tt>es.
	 * 
	 * @param	object	The object that should be set as the current selection
	 * 					of this <tt>BoundComboBoxModel</tt>; must be a
	 * 					<tt>DataObject</tt> that is present in the
	 * 					<tt>Binding</tt> this model is bound to.
	 * @throws	ClassCastException	If the given object is not an instance of
	 * 					<tt>DataObject</tt>.
	 * @throws	IllegalArgumentException	If the given object is not present
	 * 					in the <tt>Binding</tt>.
	 */
	public void setSelectedItem(Object object){
		if(JBNDUtil.equals(object, selectedObject))
			return;
		
		selectedObject = object;
		fireContentsChanged(this, -1, -1);
	}

	/**
	 * <tt>ListModel</tt> method implementation, returns the object
	 * found at the given <tt>index</tt> in this model, which is
	 * the <tt>DataObject</tt> at the same index in the <tt>Binding</tt>
	 * backing this model.
	 * 
	 * @param	index	The index of the sought object.
	 * @return	The object for the given index.
	 */
	public Object getElementAt(int index){
		if ( index >= 0 && index < binding.size() ){
			DataObject o = binding.get(index);
			if(o == null) return o;
			else return o.get(binding.getKeys()[0]);
		}else
            return null;
	}

	/**
	 * <tt>ListModel</tt> method implementation, returns the number of
	 * objects this model provides, which is the same number as the
	 * size of the <tt>Binding</tt> backing this model.
	 * 
	 * @return	The number of objects this model provides.
	 */
	public int getSize(){
		return binding == null ? 0 : binding.size();
	}

	/**
	 * <tt>Bound</tt> implementation, returns the <tt>Binding</tt> that
	 * is backing this model.
	 * 
	 * @return	The <tt>Binding</tt> that is backing this model.
	 */
	public Binding getBinding(){
		return binding;
	}

	/**
	 * Sets the <tt>Binding</tt> this <tt>Bound</tt> object
	 * is bound to, and sets the current selection to <tt>null</tt>.
	 * 
	 * @param	binding	The <tt>Binding</tt> to connect this <tt>BoundComboBoxModel</tt>
	 * 					to, <tt>null</tt> not acceptable.
	 */
	public void setBinding(Binding binding){
		setSelectedItem(null);
		
		if(this.binding == binding) return;
		if(this.binding != null){
			this.binding.removeDataSourceListener(this);
			int end = this.binding.size() - 1;
			this.binding = null;
			fireIntervalRemoved(this, 0, end);
		}
		
		//	perform all the binding related processes
		this.binding = binding;
		binding.addDataSourceListener(this);
		fireIntervalAdded(this, 0, getSize() - 1);
	}

	/**
	 * <tt>DataSourceListener</tt> implementation, called when the
	 * backing <tt>Binding</tt> fires an event. Fires an appropriate
	 * <tt>ListDataEvent</tt> to notify GUI elements of changes.
	 * 
	 * @param	event	The event fired by the <tt>Binding</tt>.
	 */
	public void dataAdded(DataSourceEvent event){
		fireIntervalAdded(this, event.getIndex0(), event.getIndex1());
		
	}

	/**
	 * <tt>DataSourceListener</tt> implementation, called when the
	 * backing <tt>Binding</tt> fires an event. Fires an appropriate
	 * <tt>ListDataEvent</tt> to notify GUI elements of changes.
	 * 
	 * @param	event	The event fired by the <tt>Binding</tt>.
	 */
	public void dataChanged(DataSourceEvent event){
		fireContentsChanged(this, event.getIndex0(), event.getIndex1());
	}

	/**
	 * <tt>DataSourceListener</tt> implementation, called when the
	 * backing <tt>Binding</tt> fires an event. Fires an appropriate
	 * <tt>ListDataEvent</tt> to notify GUI elements of changes. If
	 * the currently selected object is among the removed <tt>DataObject</tt>s,
	 * the current selection is set to <tt>null</tt>.
	 * 
	 * @param	event	The event fired by the <tt>Binding</tt>.
	 */
	public void dataRemoved(DataSourceEvent event){
		fireIntervalRemoved(this, event.getIndex0(), event.getIndex1());
		//	if the current selection is among the removed data,
		//	then set the current selection to null
		if(JBNDUtil.indexOf(binding.array(), selectedObject, false) == -1)
			setSelectedItem(null);
	}
}