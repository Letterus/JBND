package org.jbnd.swing.conn;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;

import javax.swing.JComponent;

import org.jbnd.binding.Binding;
import org.jbnd.support.ValueConverter;

/**
 * A <tt>ComponentConnection</tt> that binds a single property of the
 * component, be that data state or otherwise, to a <tt>Binding</tt>; uses
 * reflection to get and set the values on the component. For example, if a
 * <tt>GenericPropertyConnection</tt> is created with the property name
 * 'text', and the property type of 'String.class', it will expect the component
 * it connects to have these two methods: the getter method and
 * <tt>setText(String)</tt>. Acceptable getters would be <tt>getText()</tt>,
 * <tt>isText()</tt> and <tt>text()</tt>. If this condition is not
 * fulfilled, a runtime exception will occur. Also, the
 * <tt>GenericPropertyConnection</tt> will only be notified of the changes of
 * the property of the component if the component fires a
 * <tt>PropertyChangeEvent</tt> for that particular property. If it does not,
 * the connection will not forward component changes to the <tt>Binding</tt>.
 * 
 * @version 1.0 Mar 3, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class GenericPropertyConnection extends ComponentConnection
	implements PropertyChangeListener{
	
	/**
	 * Creates a new <tt>GenericPropertyConnection</tt> with the given attributes.
	 * For the implications of the attributes, see class documentation.
	 * 
	 * @param component	The component to connect to.
	 * @param binding	The <tt>Binding</tt> to connect it to.
	 * @param propertyName	The name of the property being bound.
	 * @param propertyType	The type of the property being bound.
	 */
	@Deprecated
	public GenericPropertyConnection(JComponent component, Binding binding, 
			String propertyName, Class<?> propertyType){
		super(component, binding);
		component.addPropertyChangeListener(propertyName, this);
		this.propertyName = propertyName;
		this.propertyType = propertyType;
		
		syncToBinding();
	}

	private String propertyName;
	
	private Class<?> propertyType;
	
	protected void bindingValueChanged(Object newValue){
		try{
			try{
				newValue = ValueConverter.toClass(newValue, propertyType);
			}catch(Exception ex){}	// safe to swallow conversion exceptions
			getSetter().invoke(component, new Object[]{newValue});
		}catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}

	protected Object getComponentValue(){
		if(propertyName == null) return null;
		
		//	got it!
		try{
			return getGetter().invoke(component, (Object[])null);
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	public void propertyChange(PropertyChangeEvent evt){
		super.componentValueChanged();
	}
	
	/*
	 * The 'setter' method for the bound property.
	 */
	private Method setter = null;
	
	/**
	 * Lazily initializes and returns the <tt>Method</tt> representing
	 * the 'setter' method for the bound property.
	 * 
	 * @return	See above.
	 * @throws RuntimeException If a setter can not be found.
	 */
	private Method getSetter(){

		// lazy init
		if(setter == null){
			try{
				//	get the setter...
				setter = component.getClass().getMethod(
						"set"+Character.toUpperCase(propertyName.charAt(0))+
						propertyName.substring(1), new Class[]{propertyType});
			}catch(Exception ex){
				throw new RuntimeException(ex);
			}
		}
		
		return setter;
	}
	
	/*
	 * The 'getter' method for the bound property.
	 */
	private Method getter = null;
	
	/**
	 * Lazily initializes and returns the <tt>Method</tt> representing
	 * the 'getter' method for the bound property.
	 * 
	 * @return	See above.
	 * @throws RuntimeException If a getter can not be found.
	 */
	private Method getGetter(){

		// lazy init
		if(getter == null){
			// try with "get"
			try{
				//	get the getter... :) get it?
				getter = component.getClass().getMethod(
						"get"+Character.toUpperCase(propertyName.charAt(0))+
						propertyName.substring(1), (Class[])null);
			}catch(NoSuchMethodException ex){}

			// try with "is"
			if(getter == null){
				try{
					getter = component.getClass().getMethod(
							"is"+Character.toUpperCase(propertyName.charAt(0))+
							propertyName.substring(1), (Class[])null);
				}catch(NoSuchMethodException ex){}
			}

			// try pure
			if(getter == null){
				try{
					getter = component.getClass().getMethod(
							propertyName, (Class[])null);
				}catch(NoSuchMethodException ex){
					//	this was the final attempt, rethrow
					throw new RuntimeException(ex);
				}
			}
		}
		
		return getter;
	}
	
	/**
	 * Overridden to do nothing.
	 */
	protected void syncEditability(){}
	
	/**
	 * Overridden to do nothing.
	 */
	protected void syncEnabledness(){}
}