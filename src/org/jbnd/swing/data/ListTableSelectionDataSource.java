package org.jbnd.swing.data;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jbnd.DataObject;
import org.jbnd.data.AbstractListDataSource;
import org.jbnd.support.JBNDUtil;
import org.jbnd.swing.JBNDSwingUtil;


/**
 * A <tt>DataSource</tt> whose data is the current selection of a <tt>JTable</tt>
 * or a <tt>JList</tt>. The table whose selection should be
 * provided as data *must* be backed by a <tt>Bound</tt> data model,
 * otherwise a runtime exception will be thrown. If used with a <tt>JList</tt>,
 * it's model does not necessarily have to be <tt>Bound</tt>, but selected
 * objects will be assumed to be <tt>DataObject</tt>s.<p>
 *
 * This class works on the following principle: the selection indices are obtained
 * from the list/table. <tt>DataObject</tt>s at the same indices found in the
 * data source of the <tt>Bound</tt> list/table model constitute this
 * <tt>DataSource</tt>'s data.
 *
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.2, Mar 20, 2009
 *
 */
public class ListTableSelectionDataSource extends AbstractListDataSource
	implements ListSelectionListener{

	//	the list to proxy the selection of as data
	private JList list;
	//	the table to proxy the selection of as data
	private JTable table;
	
	/**
	 * Creates a new <tt>DataSource</tt> that will proxy the selection of the given
	 * <tt>JList</tt> as <tt>DataObject</tt>s.
	 * 
	 * @param list	The list to proxy the selection of as <tt>DataObject</tt>s.
	 */
	public ListTableSelectionDataSource(JList list){
		list.addListSelectionListener(this);
		this.list = list;
	}
	
	/**
	 * Creates a new <tt>DataSource</tt> that will proxy the selection of the given
	 * <tt>JTable</tt> as <tt>DataObject</tt>s.
	 * 
	 * @param table	The list to proxy the selection of as <tt>DataObject</tt>s.
	 */
	public ListTableSelectionDataSource(JTable table){
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
		
		//	get selected DataObjects
		clear();
		addAll(list != null ? JBNDUtil.castArray(list.getSelectedValues(), DataObject.class) : 
			JBNDSwingUtil.selectedRecords(table));
	}
}