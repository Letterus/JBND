package org.jbnd.swing.conn;

import java.text.Format;

import javax.swing.JLabel;

import org.jbnd.binding.Binding;
import org.jbnd.support.ValueConverter;

/**
 * A concrete subclass of <tt>ComponentConnection</tt> used to connect
 * <tt>JLabel</tt>s to a single value in a JBND <tt>Binding</tt>.
 * <p>
 * The <tt>LabelConnection</tt> can also be used to display non
 * <tt>String</tt> objects, in which case the objects will be converted using
 * the JBND <tt>ValueConverter</tt>. A custom <tt>Format</tt> can be provided
 * to perform a different kind of formatting.
 * 
 * @version 1.0 Dec 22, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see ValueConverter
 */
public class LabelConnection extends ComponentConnection{

	private Format format;
	private Object value;
	
	/**
	 * Creates a <tt>LabelConnection</tt> with the given parameters.
	 * 
	 * @param label The <tt>JLabel</tt> to connect.
	 * @param binding The <tt>Binding</tt> to connect it to.
	 */
	@Deprecated
	public LabelConnection(JLabel label, Binding binding){
		super(label, binding);
	}
	
	/**
	 * Creates a <tt>LabelConnection</tt> with the given parameters.
	 * 
	 * @param label The <tt>JLabel</tt> to connect.
	 * @param binding The <tt>Binding</tt> to connect it to.
	 * @param format The <tt>Format</tt> to use to format binding provided
	 *            values into text.
	 */
	@Deprecated
	public LabelConnection(JLabel label, Binding binding, Format format){
		super(label, binding);
		this.format = format;
	}

	protected void bindingValueChanged(Object value){
		
		this.value = value;
		String text = value == null ? null : format == null ? ValueConverter
				.toString(value) : format.format(value);
		
		//	set the text on the component
		((JLabel)component).setText(text);
	}

	protected Object getComponentValue(){
		return value;
	}
	
	public Format getFormat(){
		return format;
	}

	
	public void setFormat(Format format){
		this.format = format;
		String text = value == null ? null : format == null ? ValueConverter
				.toString(value) : format.format(value);

		((JLabel)component).setText(text);
	}
	
	protected void syncEditability(){}
}