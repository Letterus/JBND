package org.jbnd.swing.x;

import org.jbnd.DataObject;
import org.jbnd.binding.Binding;
import org.jbnd.swing.BoundTableModel;
import org.jdesktop.swingx.JXTable;

/**
 * JBNDUtil specific to the SwingX bindings aspect of JBND.
 * 
 * @version 1.0 Nov 24, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class JBNDSwingXUtil{

	/**
	 * Assumes that the model backing the given <tt>table</tt> is a
	 * <tt>BoundTableModel</tt>, finds the selected rows (<tt>DataObject</tt>s),
	 * extracts and returns them.
	 * 
	 * @param table See above.
	 * @return See above.
	 */
	public static DataObject[] selectedRecords(JXTable table){
		int[] selection = table.getSelectedRows();
		DataObject[] rVal = new DataObject[selection.length];
		
		// accommodate for the possible sorting done by JXTable
		for(int i = 0 ; i < selection.length ; i++)
			selection[i] = table.convertRowIndexToModel(selection[i]);

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
	public static DataObject selectedRecord(JXTable table){
		int selection = table.getSelectedRow();
		if(selection < 0) return null;

		Binding binding = ((BoundTableModel)table.getModel()).getBinding();
		return binding.get(table.convertRowIndexToModel(selection));
	}
}
