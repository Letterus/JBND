package org.jbnd.swing.conn;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.jbnd.binding.Binding;
import org.jbnd.support.ValueConverter;

/**
 * A concrete subclass of <tt>ComponentConnection</tt> used to connect
 * <tt>JTextComponent</tt>s (such as <tt>JTextField</tt>s,
 * <tt>JEditorPane</tt>s, <tt>JTextArea</tt>s and <tt>JPasswordField</tt>s)
 * to a single (possibly editable) value in a JBND <tt>Binding</tt>.
 * <p>
 * The <tt>TextComponentConnection</tt> can also be used to display and edit
 * non <tt>String</tt> objects, but they must be convertible to and from
 * <tt>String</tt>s, according to the <tt>ValueConverter</tt> class.
 * <p>
 * If you want to use custom formatting for a non <tt>String</tt> object that
 * should be displayed in <tt>String</tt> form, use the
 * <tt>FormattedTextFieldConnection</tt> instead.
 * 
 * @version 1.0 Dec 22, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see ValueConverter
 */
public class TextComponentConnection extends ComponentConnection
	implements DocumentListener{
	
	/**
	 * Creates a <tt>TextComponentConnection</tt> with the given parameters.
	 * 
	 * @param textComponent The <tt>JTextComponent</tt> to connect.
	 * @param binding The <tt>Binding</tt> to connect it to.
	 */
	@Deprecated
	public TextComponentConnection(JTextComponent textComponent, Binding binding){
		super(textComponent, binding);
		textComponent.getDocument().addDocumentListener(this);
	}

	protected void bindingValueChanged(Object newValue){
		String text = newValue == null ? null : ValueConverter.toString(newValue);
		
		//	set the text on the component
		((JTextComponent)component).setText(text);
	}

	public void changedUpdate(DocumentEvent e){
		componentValueChanged();
	}

	public void insertUpdate(DocumentEvent e){
		componentValueChanged();
	}

	public void removeUpdate(DocumentEvent e){
		componentValueChanged();
	}

	protected Object getComponentValue(){
		String text = ((JTextComponent)component).getText();
		return text == null || text.length() == 0 ? null : text;
	}		
}