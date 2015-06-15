package org.jbnd.swing;

import java.awt.Component;
import java.lang.reflect.Method;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.jbnd.DataObject;
import org.jbnd.binding.Binding;
import org.jbnd.binding.Bound;
import org.jbnd.swing.BoundTreeModel.DataObjectNode;

/**
 * JBNDUtil specific to the Swing bindings aspect of JBND.
 * 
 * @version 1.0 Nov 24, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class JBNDSwingUtil{
	
	/*
	 * Disallow instantiation and subclassing.
	 */
	private JBNDSwingUtil(){}
	
	/**
	 * Attempts to find a <tt>DataObject</tt> that is bound to the passed
	 * <tt>Component</tt>. Works for combo boxes, lists, tables and trees. For
	 * all of these components the currently selected record (assuming the
	 * component contains <tt>DataObject</tt>s) is returned.
	 * <p>
	 * If the method is unable to find a record connected to the component,
	 * <tt>null</tt> is returned.
	 * 
	 * @param c See above.
	 * @return See above.
	 */
	public static DataObject recordBoundToComponent(Component c){
		
		// combo box
		if(c instanceof JComboBox){
			Object selection = ((JComboBox)c).getSelectedItem();
			if(selection instanceof DataObject)
				return (DataObject)selection;
			return null;
		
		// list
		}else if(c instanceof JList){
			Object selection = ((JList)c).getSelectedValue();
			if(selection instanceof DataObject)
				return (DataObject)selection;
			return null;
		
		}else if("org.jdesktop.swingx.JXTable".equals(c.getClass().getName())){
			
			// get the table and the selected row
			JTable table = (JTable)c;
			int selectedRow = table.getSelectedRow();
			if(selectedRow < 0) return null;
			
			// we have a SwingX table that we need to deal with dynamically
			try{
				Method m = c.getClass().getDeclaredMethod("convertRowIndexToModel", Integer.TYPE);
				selectedRow = (Integer)m.invoke(c, selectedRow);
			}catch(Exception e){
				throw new RuntimeException(e);
			}
			
			// we have the selected row, sorting handled
			Binding binding = ((BoundTableModel)((JTable)c).getModel()).getBinding();
			return binding.get(selectedRow);
		
		// check for the model of a table
		}else if(c instanceof JTable){
			return selectedRecord((JTable)c);
		
		// check for the model of a tree
		}else if(c instanceof JTree){
			Object node = ((JTree)c).getSelectionPath().getLastPathComponent();
			if(node instanceof BoundTreeModel.DataObjectNode)
				return ((BoundTreeModel.DataObjectNode)node).getDataObject();
			return null;
		
			// check for anything Bound
		}else if(c instanceof Bound){
			Binding b = ((Bound)c).getBinding();
			if(b != null && b.size() > 0)
				return b.get(0);
			return null;
		}
		
		
		return null;
	}
	
	/**
	 * Assumes that the model backing the given <tt>table</tt> is a
	 * <tt>BoundTableModel</tt>, finds the selected rows (<tt>DataObject</tt>s),
	 * extracts and returns them.
	 * 
	 * @param table See above.
	 * @return See above.
	 */
	public static DataObject[] selectedRecords(JTable table){
		int[] selection = table.getSelectedRows();
		DataObject[] rVal = new DataObject[selection.length];

		Binding binding = ((BoundTableModel)table.getModel()).getBinding();
		for(int i = 0 ; i < selection.length ; i++)
			rVal[i] = binding.get(selection[i]);

		return rVal;
	}
	
	/**
	 * Assumes that the model backing the given <tt>table</tt> is a
	 * <tt>BoundTableModel</tt>, finds the selected row (<tt>DataObject</tt>),
	 * extracts and returns it. If no row is selected, <tt>null</tt> is
	 * returned.
	 * 
	 * @param table See above.
	 * @return See above.
	 */
	public static DataObject selectedRecord(JTable table){
		int selection = table.getSelectedRow();
		if(selection < 0) return null;

		Binding binding = ((BoundTableModel)table.getModel()).getBinding();
		return binding.get(selection);
	}
	
	/**
	 * Assumes that the model backing the given <tt>tree</tt> is a
	 * <tt>BoundTreeModel</tt>, finds the selected nodes (<tt>DataObject</tt>s),
	 * extracts and return them.
	 * 
	 * @param tree See above.
	 * @return See above.
	 */
	public static DataObject[] selectedRecords(JTree tree){
		TreePath[] selection = tree.getSelectionPaths();
		DataObject[] rVal = new DataObject[selection == null ? 0 : selection.length];

		for(int i = 0 ; i < rVal.length ; i++)
			rVal[i] = ((DataObjectNode)selection[i].getLastPathComponent())
					.getDataObject();

		return rVal;
	}

}