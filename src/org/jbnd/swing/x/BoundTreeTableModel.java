package org.jbnd.swing.x;

import org.jbnd.DataObject;
import org.jbnd.DataType;
import org.jbnd.UnknownKeyException;
import org.jbnd.binding.Binding;
import org.jbnd.support.JBNDUtil;
import org.jbnd.support.NamingSupport;
import org.jbnd.swing.BoundTreeModel;
import org.jdesktop.swingx.treetable.TreeTableModel;


/**
 * A <tt>TreeTableModel</tt> implementation to be used with SwingX's
 * <tt>JXTreeTable</tt> component. Builds up from <tt>BoundTreeModel</tt>, and
 * adds the required functionalities. Note that this implementation does not
 * support editability as it's impossible to integrate it with existing JBND
 * architecture / contract.
 * <p>
 * For usage info, see superclass documentation. This model relies on it to
 * handle all tree-based functionality. Additionally, this class takes an extra
 * <tt>String[]</tt> containing the keys of properties to be displayed in
 * columns other then the first (the first one is reserved for the tree-like
 * handling).
 * <p>
 * Since it is possible that a tree model displays different types of
 * <tt>DataObject</tt>s, it is also possible that only some of those
 * <tt>DataObject</tt>s contain the properties that are to be displayed in other
 * columns as tabular data. For this reason, the <tt>BoundTreeTableModel</tt>
 * ignores the situation in which a <tt>DataObject</tt> does not contain the
 * property displayed in whichever column.
 * 
 * @version 1.0 Dec 8, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class BoundTreeTableModel extends BoundTreeModel implements TreeTableModel {

	// keys for all columns except the first
	private String[] tableColumns;

	/**
	 * Calls the superclass constructor (
	 * {@link BoundTreeModel#BoundTreeModel(Binding, String...)} and uses
	 * <tt>tableColums</tt> to display other properties in tabular form.
	 * 
	 * @param binding See above.
	 * @param tableColumns See above.
	 * @param recursionKeys See above.
	 */
	public BoundTreeTableModel(Binding binding, String[] tableColumns,
			String... recursionKeys){
		
		super(binding, recursionKeys);
		this.tableColumns = tableColumns;
	}
	
	/**
	 * Calls the superclass constructor (
	 * {@link BoundTreeModel#BoundTreeModel(Binding, int, String...)} and uses
	 * <tt>tableColums</tt> to display other properties in tabular form.
	 * 
	 * @param binding See above.
	 * @param tableColumns See above.
	 * @param recursionPlace See above.
	 * @param recursionKeys See above.
	 */
	public BoundTreeTableModel(Binding binding, String[] tableColumns,
			int recursionPlace, String... recursionKeys){
		
		super(binding, recursionPlace, recursionKeys);
		this.tableColumns = tableColumns;
	}
	
	/**
	 * <tt>TreeTableModel</tt> method implementation, returns the class
	 * object for the given column index.
	 * 
	 * @param	columnIndex	The column index for which to find the class.
	 * @return	See above.
	 */
	public Class<?> getColumnClass(int columnIndex){
		
		// the tree is at the zero index
		if(columnIndex == 0)
			return DataObject.class;
		
		// if there are no objects in the binding, it is impossible
		// to get the DataType needed to determine the class for column
		if(binding.size() == 0)
			return Object.class;
		
		//	the DataType of the object in the binding
		DataType type = binding.get(0).getDataType();
		
		//	the key for the given column
		String key = tableColumns[++columnIndex];
		
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
	 * <tt>TreeTableModel</tt> method implementation, returns the number of
	 * columns this model contains. This implementation returns the number of
	 * keys that are passed as <tt>tableColumns</tt> in the constructors, plus
	 * one (for the tree aspect of the tree-table).
	 * 
	 * @return The number of columns this <tt>TableModel</tt> contains.
	 */
	public int getColumnCount(){
		return tableColumns.length + 1;
	}

	/**
	 * <tt>TreeableModel</tt> method implementation, returns the column name for
	 * the given column index. For the first (hierarchical) column <tt>null</tt>
	 * is returned. For others the localized, beautified version (performed
	 * using {@link NamingSupport}) of the property key for that column is
	 * returned.
	 * 
	 * @param columnIndex The column index for which to get the name.
	 * @return See above.
	 */
	public String getColumnName(int columnIndex){
		if(columnIndex == 0) 
			return null;
		else
			return NamingSupport.propertyName(tableColumns[columnIndex + 1]);
	}

	/**
	 * Always returns 0, indicating that the first column (zero index) is always
	 * the hierarchical column.
	 * 
	 * @return See above.
	 */
	public int getHierarchicalColumn(){
		return 0;
	}

	/**
	 * Should be obvious.
	 * 
	 * @param node
	 * @param column
	 * @return See above.
	 */
	public Object getValueAt(Object node, int column){
		
		// for the first column, expect a DataObjectNode,
		// and return the DataObject itself
		if(column == 0){
			if(node instanceof DataObjectNode)
				return ((DataObjectNode)node).getDataObject();
			else
				return node;
			
		// for all other columns, extract the appropriate value
		// from the DataObjectNode's DataObject
		}else if(node instanceof DataObjectNode){
			try{
				return ((DataObjectNode)node).getDataObject().get(
						tableColumns[column + 1]);
			}catch(UnknownKeyException e){
				// swallow this exception,
				// it is likely that some DataObject's in the tree
				// do not have the property sought
				return null;
			}
		}else
			return null;

	}

	/**
	 * Always returns false. This can be overridden in a subclass to provide
	 * editing capability. It is not provided here because the nature of JBND
	 * <tt>Binding</tt>s, and it's editing system, is not quite compatible with
	 * editing arbitrary tree nodes. In short, the <tt>DataObject</tt> it
	 * represents would need to be edited directly. This circumvents certain
	 * JBND procedures, and is potentially (though not guaranteed) dangerous.
	 * 
	 * @param node
	 * @param column
	 * @return	<tt>false</tt>
	 */
	public boolean isCellEditable(Object node, int column){
		return false;
	}

	/**
	 * Does nothing. For an explanation why, see
	 * {@link #isCellEditable(Object, int)}.
	 * 
	 * @param value
	 * @param node
	 * @param column
	 */
	public void setValueAt(Object value, Object node, int column){
		// do nothing
	}
}