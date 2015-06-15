package org.jbnd.swing.x;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jbnd.data.AbstractListDataSource;
import org.jdesktop.swingx.JXTable;


/**
 * A <tt>DataSource</tt> whose data is the current selection of a
 * <tt>JXTable</tt>. The selection is extracted from the <tt>JXTable</tt>
 * using {@link JBNDSwingXUtil#selectedRecords(JXTable)}, so the <tt>JXTable</tt>
 * used with this class must conform to the requirements of that method.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, Mar 20, 2009
 */
public class JXTableSelectionDataSource extends AbstractListDataSource
	implements ListSelectionListener{

	//	the table to proxy the selection of as data
	private JXTable table;
	
	/**
	 * Creates a new <tt>DataSource</tt> that will proxy the selection of the given
	 * <tt>JTable</tt> as <tt>DataObject</tt>s.
	 * 
	 * @param table	The list to proxy the selection of as <tt>DataObject</tt>s.
	 */
	public JXTableSelectionDataSource(JXTable table){
		table.getSelectionModel().addListSelectionListener(this);
		this.table = table;
	}
	
	/**
	 * Implementation of the <tt>ListSelectionListener</tt> interface.
	 * 
	 * @param e	The selection change event.
	 */
	public void valueChanged(ListSelectionEvent e){
		if(e.getValueIsAdjusting()) return;
		
		clear();
		addAll(JBNDSwingXUtil.selectedRecords(table));
	}
}