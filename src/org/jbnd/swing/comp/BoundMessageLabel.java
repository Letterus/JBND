package org.jbnd.swing.comp;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

import org.jbnd.binding.Binding;
import org.jbnd.binding.Bound;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.support.ValueConverter;


/**
 * A <tt>Bound</tt> label that allows the parsing of multiple values from a
 * single <tt>DataObject</tt> (the first in the <tt>Binding</tt>) into a single
 * message. The message is created using <tt>java.text.MessageFormat</tt>, the
 * pattern for which is provided to the label through the
 * <tt>setText(String)</tt> method, which is overridden specifically for that
 * purpose.
 * <p>
 * The pattern to be provided should not use formatting elements, since all
 * values are translated to <tt>String</tt>s using JBND's
 * <tt>ValueConverter</tt> before they are fed to the <tt>MessageFormat</tt>
 * defined by the pattern.
 * <p>
 * If no <tt>Binding</tt> is set on the <tt>BoundMessageLabel</tt>, it will
 * display it's message pattern. If a <tt>Binding</tt> is set, but contains no
 * <tt>DataObject</tt>s, then the text that can be set using
 * {@link #setEmptyBindingDisplay(String)} is displayed.
 * 
 * @version 1.0 Nov 12, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class BoundMessageLabel extends JLabel implements Bound{
	
	// storage for message formats
	private static final Map<String, MessageFormat> formats =
		new HashMap<String, MessageFormat>();
	
	// used in constructing the end result label text
	private static final StringBuffer buff = new StringBuffer();
	
	//	the binding this component connection is bound to
	protected Binding binding;
	
	// the currently used messageFormat
	private String messageFormat;
	
	// the string displayed when the binding contains no data
	private String emptyBindingDisplay = " ";

	/**
	 * Returns the <tt>String</tt> that is displayed when there is a
	 * <tt>Binding</tt> set on this <tt>BoundMessageLabel</tt>, but it contains
	 * no data.
	 * 
	 * @return See above.
	 */
	public String getEmptyBindingDisplay(){
		return emptyBindingDisplay;
	}

	/**
	 * Sets the <tt>String</tt> that is displayed when there is a
	 * <tt>Binding</tt> set on this <tt>BoundMessageLabel</tt>, but it contains
	 * no data.
	 * 
	 * @param emptyBindingDisplay See above.
	 */
	public void setEmptyBindingDisplay(String emptyBindingDisplay){
		this.emptyBindingDisplay = emptyBindingDisplay;
	}

	public Binding getBinding(){
		return binding;
	}

	/**
	 * Sets the <tt>Binding</tt> this <tt>ComponentConnection</tt> and takes
	 * care of updating the component.
	 * 
	 * @param binding The <tt>Binding</tt> to connect this
	 *            <tt>ComponentConnection</tt> to, <tt>null</tt> not
	 *            acceptable.
	 */
	public void setBinding(Binding binding){
		if(this.binding == binding) return;
		if(this.binding != null){
			this.binding.removeDataSourceListener(this);
		}
		
		//	perform all the binding related processes
		this.binding = binding;
		binding.addDataSourceListener(this);
		
		syncText();
	}

	/**
	 * Implementation of the <tt>DataSourceListener</tt> method, called when
	 * the <tt>Binding</tt> this <tt>BoundMessageLabel</tt> is bound to
	 * fires an event. Takes care of updating the label.
	 * 
	 * @param event The event fired by the <tt>Binding</tt>.
	 */
	public void dataAdded(DataSourceEvent event){
		if(event.getIndex0() == 0)
			syncText();
		
	}

	/**
	 * Implementation of the <tt>DataSourceListener</tt> method, called when
	 * the <tt>Binding</tt> this <tt>BoundMessageLabel</tt> is bound to
	 * fires an event. Takes care of updating the label.
	 * 
	 * @param event The event fired by the <tt>Binding</tt>.
	 */
	public void dataChanged(DataSourceEvent event){
		if(event.getIndex0() == 0)
			syncText();
		
	}

	/**
	 * Implementation of the <tt>DataSourceListener</tt> method, called when
	 * the <tt>Binding</tt> this <tt>BoundMessageLabel</tt> is bound to
	 * fires an event. Takes care of updating the label.
	 * 
	 * @param event The event fired by the <tt>Binding</tt>.
	 */
	public void dataRemoved(DataSourceEvent event){
		if(event.getIndex0() == 0)
			syncText();
	}	
	
	/*
	 * Updates the text displayed by the label to reflect the current message
	 * format and data state (Binding).
	 */
	private void syncText(){
		
		// is there a Binding?
		if(binding == null){
			super.setText(messageFormat);
			return;
		}else if(binding.size() == 0){
			// is it just an empty binding
			super.setText(emptyBindingDisplay);
			return;
		}
		
		// obtain the message format
		MessageFormat f = formats.get(messageFormat);
		if(f == null){
			f = new MessageFormat(messageFormat);
			formats.put(messageFormat, f);
		}
		
		// obtain values
		String[] keys = binding.getKeys();
		Object values[] = new Object[keys.length];
		for(int i = 0 ; i < keys.length ; i++)
			values[i] = ValueConverter.toString(binding.get(0).get(keys[i]));
		
		// format them into the string
		f.format(values, buff, null);
		
		super.setText(buff.toString());
		buff.delete(0, buff.length());
	}
	
	/**
	 * Overridden to use the given <tt>text</tt> as the message format.
	 * 
	 * @param text See above.
	 */
	public void setText(String text){
		if(text != null){
			this.messageFormat = text;
			syncText();
		}
	}
}