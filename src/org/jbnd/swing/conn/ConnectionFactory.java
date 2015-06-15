package org.jbnd.swing.conn;

import java.lang.reflect.Constructor;
import java.text.Format;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.jbnd.DataType;
import org.jbnd.binding.AddEditGroup;
import org.jbnd.binding.Binding;
import org.jbnd.binding.GPBinding;
import org.jbnd.data.DataSource;
import org.jbnd.swing.BoundTreeModel;


/**
 * Produces concrete {@link ComponentConnection} instances that connect a given
 * user interface component (a <tt>JComponent</tt> subclass) to a
 * <tt>Binding</tt>. <tt>ComponentConnection</tt>s serve to connect a single
 * value from a <tt>Binding</tt> (the value found in the first
 * <tt>DataObject</tt> it contains for the <tt>Binding</tt>s first key) to a
 * user interface element. It is important to emphasize that a *single* value is
 * connected. There are JBND classes used to connect UI elements to multiple
 * (possibly all) values of a <tt>Binding</tt>, but connections do not do that.
 * 
 * @version 1.0 Feb 3, 2009
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class ConnectionFactory{

	/*
	 * Disallow instantiation.
	 */
	private ConnectionFactory(){}
	
	/**
	 * Equivalent to calling:
	 * <tt>ConnectionFactory.connect(component, GPBinding.view(ds, keys));</tt>
	 * 
	 * @param component The component to connect.
	 * @param ds See {@link GPBinding#view(DataSource, String...)}
	 * @param keys ...
	 * @return The created <tt>ComponentConnection</tt>.
	 * @see #connect(JComponent, Binding)
	 */
	public static ComponentConnection connectView(JComponent component, DataSource ds,
			String... keys){
		return connect(component, GPBinding.view(ds, keys));
	}
	
	/**
	 * Equivalent to calling:
	 * <tt>ConnectionFactory.connect(component, GPBinding.edit(ds, keys));</tt>
	 * 
	 * @param component The component to connect.
	 * @param ds See {@link GPBinding#edit(DataSource, String...)}
	 * @param keys ...
	 * @return The created <tt>ComponentConnection</tt>.
	 * @see #connect(JComponent, Binding)
	 */
	public static ComponentConnection connectEdit(JComponent component, DataSource ds,
			String... keys){
		return connect(component, GPBinding.edit(ds, keys));
	}
	
	/**
	 * Equivalent to calling:
	 * <tt>ConnectionFactory.connect(component, GPBinding.edit(ds, keys, editable));</tt>
	 * 
	 * @param component The component to connect.
	 * @param ds See {@link GPBinding#edit(DataSource, String[], boolean[])}
	 * @param keys ...
	 * @param editable ...
	 * @return The created <tt>ComponentConnection</tt>.
	 * @see #connect(JComponent, Binding)
	 */
	public static ComponentConnection connect(JComponent component, DataSource ds,
			String[] keys, boolean[] editable){
		return connect(component, GPBinding.edit(ds, keys, editable));
	}
	
	/**
	 * Equivalent to calling:
	 * <tt>ConnectionFactory.connect(component,
	 * GPBinding.edit(ds, keys, editable, addEditable));</tt>
	 * 
	 * @param component The component to connect.
	 * @param ds See {@link GPBinding#edit(AddEditGroup, String[], boolean[], boolean[])}
	 * @param keys ...
	 * @param editable ...
	 * @return The created <tt>ComponentConnection</tt>.
	 * @see #connect(JComponent, Binding)
	 */
	public static ComponentConnection connect(JComponent component, AddEditGroup ds,
			String[] keys, boolean[] editable, boolean[] addEditable){
		return connect(component, GPBinding.edit(ds, keys, editable, addEditable));
	}
	
	/**
	 * Connects the given <tt>JComponent</tt> subclass and the given
	 * <tt>Binding</tt> using a concrete implementation of
	 * <tt>ComponentConnection</tt>.
	 * <p>
	 * Supported UI component classes (<tt>JComponent</tt> subclasses</tt>):
	 * <ul>
	 * <li><tt>JTextComponent</tt> (text field, text area)</li>
	 * <li><tt>JComboBox</tt> - connecting it's selection</li>
	 * <li><tt>JFormattedTextField</tt></li>
	 * <li><tt>JLabel</tt></li>
	 * <li><tt>JSpinner</tt></li>
	 * <li><tt>JToggleButton</tt> - connecting it's <tt>selected</tt> property</li>
	 * <li><tt>JTree</tt> - connecting it's selection. If the tree is using
	 * <tt>SINGLE_SELECTION</tt> then the value extracted from it will be a
	 * single <tt>DataObject</tt>, otherwise it will be a <tt>List</tt> of
	 * <tt>DataObject</tt>s. Expects that the <tt>JTree</tt> is backed by a
	 * {@link BoundTreeModel}. Also see
	 * {@link #connect(JTree, Binding, DataType)}</li>
	 * <li><tt>JList</tt> - connecting it's selection. If the list is using
	 * <tt>SINGLE_SELECTION</tt> then the value extracted from it will be a
	 * single <tt>DataObject</tt>, otherwise it will be a <tt>List</tt> of
	 * <tt>DataObject</tt>s.</li>
	 * <li><tt>JXDatePicker</tt> - part of the SwingX library, connecting
	 * the selected date</li>
	 * </ul>
	 * 
	 * @param component The component whose value should be connected (bound) to
	 *            the given <tt>Binding</tt>, for more info see
	 *            {@link ConnectionFactory}.
	 * @param binding The <tt>Binding</tt> to connect to.
	 * @return The created <tt>ComponentConnection</tt>.
	 */
	@SuppressWarnings("deprecation")
	public static ComponentConnection connect(JComponent component, Binding binding){
	
		// FormattedTextField comes before JTextComponent as it's a subclass of it
		if(component instanceof JFormattedTextField)
			return new FormattedTextFieldConnection((JFormattedTextField)component, binding);
		
		// instantiated an appropriate connection based on component class
		if(component instanceof JTextComponent)
			return new TextComponentConnection((JTextComponent)component, binding);
		
		if(component instanceof JComboBox)
			return new ComboBoxConnection((JComboBox)component, binding);
		
		if(component instanceof JLabel)
			return new LabelConnection((JLabel)component, binding);
		
		if(component instanceof JSpinner)
			return new SpinnerConnection((JSpinner)component, binding);
		
		if(component instanceof JToggleButton)
			return new ToggleButtonConnection((JToggleButton)component, binding);
		
		if(component instanceof JTree)
			return new TreeConnection((JTree)component, binding);
		
		if(component instanceof JList)
			return new ListConnection((JList)component, binding);
		
		// now do some reflection for SwingX stuff, to avoid class dependencies
		if(component.getClass().getName().equals("org.jdesktop.swingx.JXDatePicker")){
			try{
				// get the connection class
				Class<?> pickerConnClass = Class
						.forName("org.jbnd.swing.x.DatePickerConnection");
				// get a constructor from it
				Constructor<?> constructor = pickerConnClass.getConstructor(
						Class.forName("org.jdesktop.swingx.JXDatePicker"),
						Binding.class);
				// instantiate and return
				return (ComponentConnection)constructor.newInstance(component, binding);
			}catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		// don't know how to create a connection
		throw new IllegalArgumentException(
				"Unable to create a connection for component class: "
						+ component.getClass());
	}
	
	/**
	 * Specialized factory method that connects a <tt>JLabel</tt> with a
	 * <tt>Binding</tt>, and makes the created connection use the given
	 * <tt>Format</tt> to format the value from the <tt>Binding</tt> into text,
	 * in which form it is then displayed in the label.
	 * 
	 * @param label The label to connect.
	 * @param binding The <tt>Binding</tt> containing the value to display in
	 *            the label.
	 * @param format The <tt>Format</tt> used to format the value from the
	 *            <tt>Binding</tt> into a <tt>String</tt>, in which form it is
	 *            then displayed in the value.
	 * @return The created <tt>ComponentConnection</tt>.
	 */
	@SuppressWarnings("deprecation")
	public static ComponentConnection connect(JLabel label, Binding binding, Format format){
		return new LabelConnection(label, binding, format);
	}
	
	/**
	 * Specialized factory method that connects a selection of a <tt>JTree</tt>
	 * to a <tt>Binding</tt>, but filters out any tree selection that is not a
	 * <tt>DataObject</tt> of the given <tt>DataType</tt>. If the tree is using
	 * <tt>SINGLE_SELECTION</tt> then the value extracted from it will be a
	 * single <tt>DataObject</tt>, otherwise it will be a <tt>List</tt> of
	 * <tt>DataObject</tt>s. Expects that the <tt>JTree</tt> is backed by a
	 * {@link BoundTreeModel}.
	 * 
	 * @param tree
	 * @param binding
	 * @param type
	 * @return The created <tt>ComponentConnection</tt>.
	 */
	@SuppressWarnings("deprecation")
	public static ComponentConnection connect(JTree tree, Binding binding, DataType type){
		return new TreeConnection(tree, binding, type);
	}
	
	/**
	 * Creates a <tt>ComponentConnection</tt> that binds a single property of
	 * the component to a <tt>Binding</tt>; uses reflection to get and set the
	 * values on the component. For example, if a connection is created with the
	 * property name 'text', and the property type of 'String.class', it will
	 * expect the component it connects to have these two methods: the getter
	 * method and <tt>setText(String)</tt>. Acceptable getters would be
	 * <tt>getText()</tt>, <tt>isText()</tt> and <tt>text()</tt>. If this
	 * condition is not fulfilled, a runtime exception will occur. Also, the
	 * <tt>GenericPropertyConnection</tt> will only be notified of the changes
	 * of the property of the component if the component fires a
	 * <tt>PropertyChangeEvent</tt> for that particular property. If it does
	 * not, the connection will not forward component changes to the
	 * <tt>Binding</tt>.
	 * 
	 * @param component The component to connect.
	 * @param binding The <tt>Binding</tt> to find the value from.
	 * @param propertyName Name of the component property to bind.
	 * @param propertyClass
	 * @return The created <tt>ComponentConnection</tt>.
	 */
	@SuppressWarnings("deprecation")
	public static ComponentConnection connect(JComponent component,
			Binding binding, String propertyName, Class<?> propertyClass){
		return new GenericPropertyConnection(component, binding, propertyName, propertyClass);
	}
}