package org.jbnd.swing;

import javax.swing.table.AbstractTableModel;

import org.jbnd.DataType;
import org.jbnd.binding.Binding;
import org.jbnd.binding.Bound;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.support.JBNDUtil;
import org.jbnd.support.NamingSupport;

/**
 * A <tt>Bound</tt> implementation that provides the data found in the
 * <tt>Binding</tt> in a tabular form, to be used with <tt>JTable</tt>s
 * that need to display and optionally edit <tt>DataObject</tt>s.<p>
 * 
 * This <tt>TableModel</tt> implementation provides all the necessary
 * information (the column and row counts, column names, editability
 * control, data viewing and editing) based on the information found
 * in the <tt>Binding</tt> backing the model.<p>
 * 
	@version 1.0 Dec 27, 2007
	@author Florijan Stamenkovic (flor385@mac.com)
 */
public class BoundTableModel extends AbstractTableModel implements Bound{
	
	//	the binding backing this TableModel
	private Binding	binding;

	/**
	 * Creates a new <tt>BoundTableModel</tt> with the given parameters.
	 * 
	 * @param binding	The <tt>Binding</tt> to back this table model.
	 */
	public BoundTableModel(Binding binding){
		setBinding(binding);
	}
	
	/**
	 * Returns the <tt>Binding</tt> backing this model.
	 * 
	 * @return	The <tt>Binding</tt> backing this model.
	 */
	public Binding getBinding(){
		return binding;
	}

	/**
	 * Sets the <tt>Binding</tt> backing this model.
	 * 
	 * @param	binding	The <tt>Binding</tt> that should
	 * 			back this model, <tt>null</tt> is not acceptable.
	 */
	public void setBinding(Binding binding){
		if(this.binding == binding && binding != null) return;
		if(this.binding != null){
			this.binding.removeDataSourceListener(this);
		}
		
		//	perform all the binding related processes
		this.binding = binding;
		binding.addDataSourceListener(this);
		fireTableDataChanged();
	}

	/**
	 * <tt>DataSourceListener</tt> implementation, called when the
	 * backing <tt>Binding</tt> fires an event. Fires an appropriate
	 * <tt>TableModel</tt> to notify GUI elements of changes.
	 * 
	 * @param	event	The event fired by the <tt>Binding</tt>.
	 */
	public void dataAdded(DataSourceEvent event){
		fireTableRowsInserted(event.getIndex0(), event.getIndex1());

	}

	/**
	 * <tt>DataSourceListener</tt> implementation, called when the
	 * backing <tt>Binding</tt> fires an event. Fires an appropriate
	 * <tt>TableModel</tt> to notify GUI elements of changes.
	 * 
	 * @param	event	The event fired by the <tt>Binding</tt>.
	 */
	public void dataChanged(DataSourceEvent event){
		fireTableRowsUpdated(event.getIndex0(), event.getIndex1());

	}

	/**
	 * <tt>DataSourceListener</tt> implementation, called when the
	 * backing <tt>Binding</tt> fires an event. Fires an appropriate
	 * <tt>TableModel</tt> to notify GUI elements of changes.
	 * 
	 * @param	event	The event fired by the <tt>Binding</tt>.
	 */
	public void dataRemoved(DataSourceEvent event){
		fireTableRowsDeleted(event.getIndex0(), event.getIndex1());

	}
	
	/**
	 * <tt>TableModel</tt> method implementation, returns the number
	 * of columns this model contains. This implementation returns the
	 * number of keys that are contained in the <tt>Binding</tt>
	 * backing this model.
	 * 
	 * @return	The number of columns this <tt>TableModel</tt> contains.
	 */
	public int getColumnCount(){
		return binding.getKeys().length;
	}

	/**
	 * <tt>TableModel</tt> method implementation, returns the number
	 * of rows this model contains. This implementation returns the
	 * number <tt>DataObject</tt>s provided by the <tt>Binding</tt>
	 * backing this model.
	 * 
	 * @return	The number of rows this <tt>TableModel</tt> contains.
	 */
	public int getRowCount(){
		return binding.size();
	}

	/**
	 * <tt>TableModel</tt> method implementation, returns the value
	 * for the given row and column indices. This implementation
	 * returns the value found in the <tt>DataObject</tt> found in
	 * the <tt>Binding</tt> for the given row index, for a key found
	 * in the <tt>Binding</tt> for the given column index.
	 * 
	 * @param	rowIndex	The row index of the sought value.
	 * @param	columnIndex	The column index of the sought value.
	 * @return	See above.
	 */
	public Object getValueAt(int rowIndex, int columnIndex){
		return binding.get(rowIndex).get(binding.getKeys()[columnIndex]);
	}

	/**
	 * <tt>TableModel</tt> method implementation, returns the class
	 * object for the given column index. This method attempts to find
	 * the class of the value that will be returned from the <tt>Binding</tt>
	 * when this model asks for it.
	 * 
	 * @param	columnIndex	The column index for which to find the class.
	 * @return	See above.
	 */
	public Class<?> getColumnClass(int columnIndex){
		//	if there are no objects in the binding, it is impossible
		//	to get the DataType needed to determine the class for column
		if(binding.size() == 0)
			return Object.class;
		
		//	the DataType of the object in the binding
		DataType type = binding.get(0).getDataType();
		
		//	the key for the given column
		String key = binding.getKeys()[columnIndex];
		
		//	handle key paths
		if(JBNDUtil.isKeyPath(JBNDUtil.cleanPath(key))){
			//	clean up the path
			String path = JBNDUtil.cleanPath(key);
			//	set the type and the key to match the relationship retrieved info
			type = JBNDUtil.dataTypeForRelationship(type, JBNDUtil.keyPathWithoutLastKey(path));
			key = JBNDUtil.lastKeyOfKeyPath(path);
		}
		
		// try to determine the property type from the DataType
		return JBNDUtil.classForProperty(type, key);
	}

	/**
	 * <tt>TableModel</tt> method implementation, returns the column name
	 * for the given column index. This method retrieves the key from the
	 * <tt>Binding</tt> for the given column index, and uses the
	 * <tt>NamingSupport</tt> class to translate that key <tt>String</tt>
	 * into a user presentable name.
	 * 
	 * @param	column	The column index for which to get the name.
	 * @return	See above.
	 */
	public String getColumnName(int column){
		return NamingSupport.propertyName(binding.getKeys()[column]);
	}

	/**
	 * <tt>TableModel</tt> method implementation, indicates if the
	 * value at the given row and column indices should be editable.
	 * This implementation checks the <tt>Binding</tt> to see if
	 * the key at the given <tt>columnIndex</tt> should be editable.
	 * 
	 * @param	rowIndex	The row index of the value checked for editability.
	 * @param	columnIndex	The column index of the value checked for editability.
	 * @return	See above.
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex){
		return binding.isEditable(rowIndex, binding.getKeys()[columnIndex]);
	}

	/**
	 * <tt>TableModel</tt> method implementation, attempts to edit the
	 * backing data to store the given <tt>value</tt>. This implementation
	 * attempts simply calls the <tt>edit(...)</tt> method on the
	 * <tt>Binding</tt> backing this model, with the given parameters.
	 * 
	 * @param	aValue		The new value.
	 * @param	rowIndex	The row index of the value being edited.
	 * @param	columnIndex	The column index of the value being edited.
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex){
		binding.edit(rowIndex, binding.getKeys()[columnIndex], aValue);
		fireTableRowsUpdated(rowIndex, rowIndex);
	}
}