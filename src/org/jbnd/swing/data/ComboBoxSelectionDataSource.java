package org.jbnd.swing.data;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import org.jbnd.DataObject;
import org.jbnd.data.AbstractListDataSource;
import org.jbnd.support.JBNDUtil;

/**
 * A <tt>DataSource</tt> whose data is the current selection of a <tt>JComboBox</tt>.
 * At a time when the selection in the combo box changes it is imperative that
 * the selected item is a <tt>DataObject</tt>, otherwise a <tt>ClassCastException</tt>
 * will be thrown. In that sense the <tt>ComboBoxSelectionDataSource</tt> works
 * perfectly in combination with <tt>JComboBox</tt>es that are backed by a
 * <tt>BoundComboBoxModel</tt> though this is not required.
 *
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.1, Dec 26, 2007
 *
 */
public class ComboBoxSelectionDataSource extends AbstractListDataSource
		implements ItemListener{
	
	//	the combo box whose selection we are proxying
	private JComboBox comboBox;
	
	/**
	 * Creates a new <tt>ComboBoxSelectionDataSource</tt> where the
	 * data it provides will be the current selection of the given
	 * <tt>JComboBox</tt>.
	 * 
	 * @param comboBox	The <tt>ComboBox</tt> whose selection should
	 * 					be proxied as data; <tt>null</tt> not allowed.
	 */
	public ComboBoxSelectionDataSource(JComboBox comboBox){
		comboBox.addItemListener(this);
		this.comboBox = comboBox;
	}

	/**
	 * <tt>ItemListener</tt> implementation, called when the selection
	 * of the <tt>ComboBox</tt> changes, fires appropriate
	 * <tt>DataSource</tt> events.
	 * 
	 * @param	e	The event fired by the <tt>JComboBox</tt> notifying
	 * 				listeners that selection changed.
	 */
	public void itemStateChanged(ItemEvent e){
		DataObject selection = (DataObject)comboBox.getSelectedItem();
		
		//	check if the selection is the same as before
		if(selection == null && size() == 0) return;
		if(size() != 0 && JBNDUtil.equals(get(0), selection)) return;
		
		//	selection changed, act accordingly
		clear();
		if(selection != null)
			add(selection);
	}
}