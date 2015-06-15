package org.jbnd.swing.conn;

import java.lang.reflect.Method;

import javax.swing.JComponent;

import org.jbnd.DataObject;
import org.jbnd.UnknownKeyException;
import org.jbnd.binding.Binding;
import org.jbnd.binding.Bound;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.event.DataSourceListener;
import org.jbnd.support.JBNDUtil;
import org.jbnd.support.ValueConverter;

/**
 * A <tt>Bound</tt> implementation that connects a single value from a
 * <tt>Binding</tt> (that being the value found in the first
 * <tt>DataObject</tt> for the first key of the <tt>Binding</tt>) with a
 * Swing component that can display and possibly edit it. Meant to be subclassed
 * by component specific connection classes, this abstract class encapsulates
 * all the common functionality.
 * <p>
 * Implementing classes should listen to changes in the components they are
 * specialized for, and when their data state changes should call the
 * <tt>componentValueChanged(Object)</tt> method, passing along the new value.
 * <p>
 * This implementation does automatic control over the enabledness and editability
 * of the connected <tt>Component</tt>. However, this standard behavior might not
 * be the desired one, in case of editability control, in that case override
 * the <tt>syncEditability()</tt> method.
 * 
 * @version 1.0 Dec 21, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public abstract class ComponentConnection implements Bound {
	
	/**
	 * In order to enable a single GUI component to connect to multiple
	 * Bindings, we need to make sure that a change in that GUI component never
	 * triggers an attempt to update that same component (through a different
	 * Binding). If such an attempt is made, many components will thrown an
	 * exception, as an attempt to modify them has resulted from them firing a
	 * modification event. Solution: In a single threaded environment (which
	 * Swing for all intents and purposes is) we can use a single variable that
	 * points to the component that originated the change. So, when a component
	 * changes (which we are notified of in the {@link #componentValueChanged()}
	 * method), point to the component originating the change. Then the Binding
	 * listening methods (the {@link DataSourceListener} methods implemented in
	 * this class) need to check if the component of the connection is the same
	 * one that originated the change, and if it is, ignore the
	 * <tt>DataSourceEvent</tt>.
	 */
	private static JComponent changeOriginatingComponent = null;
	
	//	the binding this component connection is bound to
	protected Binding binding;
	
	//	the component this connection is connecting to the binding
	protected JComponent component;
	
	/*
	 * Controls if or not the "enabled" state of the component is manipulated as
	 * a consequence of a value change in the binding of this connection.
	 */
	private boolean syncEnabledness = true;
	
	/*
	 * Controls if or not the "editable" state of the component is manipulated
	 * as a consequence of a value change in the binding of this connection.
	 */
	private boolean syncEditability = true;
	
	/*
	 * Controls if or not the the connection pushes values found in the binding
	 * into the component.
	 */
	private boolean syncBindingValue = true;
	
	/*
	 * Controls if or not the the connection pushes values found in the component
	 * into the binding.
	 */
	private boolean syncComponentValue = true;
	
	/*
	 * Values for enabled and editable at the moment of connection instantiation.
	 */
	private final boolean originalEnabledness;
	private final Boolean originalEditability;

	
	/**
	 * Creates a new <tt>ComponentConnection</tt> with the given parameters.
	 * 
	 * @param component The <tt>Component</tt> that needs to be connected,
	 *            <tt>null</tt> not acceptable.
	 * @param binding The<tt>Binding</tt> to connect this
	 *            <tt>ComponentConnection</tt> to, <tt>null</tt> not
	 *            acceptable.
	 */
	protected ComponentConnection(JComponent component, Binding binding){
		this.component = component;
		
		// store the original values for editability and enabledness
		originalEnabledness = component.isEnabled();
		Boolean originalEditabilityToSet = null;
		try{
			Method m = component.getClass().getMethod("isEditable", (Class<?>[])null);
			originalEditabilityToSet = (Boolean)m.invoke(component, (Object[])null);
		}catch(Exception ex){
			// the component does not have an "isEditable()" method,
			// leave the origianlEnabledness method at null
		}
		originalEditability = originalEditabilityToSet;
		
		setBinding(binding);
	}

	/**
	 * Returns the <tt>Binding</tt> this <tt>ComponentConnection</tt> is
	 * bound to.
	 * 
	 * @return The <tt>Binding</tt> this <tt>ComponentConnection</tt> is
	 *         bound to.
	 */
	public Binding getBinding(){
		return binding;
	}
	
	/**
	 * Returns the <tt>JComponent</tt> this <tt>ComponentConnection</tt> is
	 * connecting to the <tt>Binding</tt>.
	 * 
	 * @return The <tt>JComponent</tt> this <tt>ComponentConnection</tt> is
	 *         connecting to the <tt>Binding</tt>.
	 */
	public JComponent getComponent(){
		return component;
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
		
		syncAll();
	}

	/**
	 * Implementation of the <tt>DataSourceListener</tt> method, called when
	 * the <tt>Binding</tt> this <tt>ComponentConnection</tt> is bound to
	 * fires an event. Takes care of updating the component.
	 * 
	 * @param event The event fired by the <tt>Binding</tt>.
	 */
	public void dataAdded(DataSourceEvent event){
		if(changeOriginatingComponent != component && event.getIndex0() == 0)
			syncAll();
		
	}

	/**
	 * Implementation of the <tt>DataSourceListener</tt> method, called when
	 * the <tt>Binding</tt> this <tt>ComponentConnection</tt> is bound to
	 * fires an event. Takes care of updating the component.
	 * 
	 * @param event The event fired by the <tt>Binding</tt>.
	 */
	public void dataChanged(DataSourceEvent event){
		if(changeOriginatingComponent != component && event.getIndex0() == 0)
			syncAll();
	}

	/**
	 * Implementation of the <tt>DataSourceListener</tt> method, called when
	 * the <tt>Binding</tt> this <tt>ComponentConnection</tt> is bound to
	 * fires an event. Takes care of updating the component.
	 * 
	 * @param event The event fired by the <tt>Binding</tt>.
	 */
	public void dataRemoved(DataSourceEvent event){
		if(changeOriginatingComponent != component && event.getIndex0() == 0)
			syncAll();
	}
	
	/**
	 * Calls <tt>syncEnabledness()</tt>, <tt>syncEditability()</tt> and
	 * <tt>syncToBinding()</tt>.
	 */
	private void syncAll(){
		if(syncEnabledness) syncEnabledness();
		if(syncEditability) syncEditability();
		if(syncBindingValue) syncToBinding();
	}
	
	/**
	 * Controls if or not the "enabled" state of the component is manipulated as
	 * a consequence of a value change in the binding of this connection (see
	 * the {@link #syncEnabledness()} method).
	 * <p>
	 * When first initialized the connection has this switch set to <tt>true</tt>.
	 * 
	 * @param sync See above.
	 * @return <tt>this</tt> component connection.
	 */
	public ComponentConnection setSyncEnabledness(boolean sync){
		
		if(sync == syncEnabledness) return this;
		this.syncEnabledness = sync;
		
		// if stopping syncing, revert to original state
		if(!sync)
			component.setEnabled(originalEnabledness);
		
		return this;
	}
	
	/**
	 * Controls if or not the "editable" state of the component is manipulated
	 * as a consequence of a value change in the binding of this connection (see
	 * the {@link #syncEnabledness()} method).
	 * <p>
	 * When first initialized the connection has this switch set to <tt>true</tt>.
	 * 
	 * @param sync See above.
	 * @return <tt>this</tt> component connection.
	 */
	public ComponentConnection setSyncEditability(boolean sync){
		
		if(sync == syncEditability) return this;
		this.syncEditability = sync;
		
		// if stopping syncing, revert to original state
		if(!sync && originalEditability != null){
			try{
				Method m = component.getClass().getMethod
					("setEditable", new Class[]{Boolean.TYPE});
				m.invoke(component, new Object[]{new Boolean(originalEditability)});
			}catch(Exception ex){
				// swallow this, if the component does not have a setEditable(Boolean) method
				// that is not our problem, let it be
			}
		}
		
		return this;
	}
	
	/**
	 * Controls if or not the connection pushes the value found in it's
	 * <tt>Binding</tt> onto the component (see the {@link #syncToBinding()}
	 * method.
	 * <p>
	 * When first initialized the connection has this switch set to <tt>true</tt>.
	 * 
	 * @param sync See above.
	 * @return <tt>this</tt> component connection.
	 */
	public ComponentConnection setSyncBindingValue(boolean sync){
		this.syncBindingValue = sync;
		return this;
	}
	
	/**
	 * Controls if or not the connection pushes the value found in it's
	 * component onto the <tt>Binding</tt> (see the
	 * {@link #componentValueChanged()} method.
	 * <p>
	 * When first initialized the connection has this switch set to
	 * <tt>true</tt>.
	 * 
	 * @param sync See above.
	 * @return <tt>this</tt> component connection.
	 */
	public ComponentConnection setSyncComponentValue(boolean sync){
		this.syncComponentValue = sync;
		return this;
	}
	
	/**
	 * Makes sure that the component is enabled or disabled according to
	 * the <tt>Binding</tt>'s state.
	 * <p>
	 * Note that this method may never get called, if or not it is called is
	 * controlled through the {@link #setSyncEditability(boolean)} method.
	 */
	protected void syncEnabledness(){
		component.setEnabled(binding.size() > 0);
	}
	
	/**
	 * If the current value of the component is different then the current value
	 * of the <tt>Binding</tt>, calls <tt>bindingValueChanged(Object)</tt>
	 * passing it the current value of the <tt>Binding</tt>.
	 * <p>
	 * Note that this method may never get called, if or not it is called is
	 * controlled through the {@link #setSyncBindingValue(boolean)} method.
	 */
	protected final void syncToBinding(){
		
		// get the binding value and the attribute class
		Object bindingValue = null;
		Class<?> valueClass = null; 
		
		if(binding != null && binding.size() > 0){
			
			DataObject bindingObject = binding.get(0);
			String key = binding.getKeys()[0];
			bindingValue = bindingObject.get(key);
			
			try{
				valueClass = JBNDUtil.classForProperty(binding.get(0)
						.getDataType(), binding.getKeys()[0]);
			}catch(UnknownKeyException ex){
				if(bindingValue != null)
					valueClass = bindingValue.getClass();
			}
		}
		
		// and the component value
		Object componentValue = getComponentValue();
		
		// try to convert the component value to the class of the binding value
		if(valueClass != null)
			try{
				componentValue = ValueConverter.toClass(componentValue, valueClass);
			}catch(Exception ex){
				// swallow: ignore the inability to convert
			}

		// if the values are different, set the value on the component
		if(!(JBNDUtil.equals(bindingValue, componentValue)))
			bindingValueChanged(bindingValue);
	}
	
	/**
	 * Makes sure that the component's editability is set according to the
	 * <tt>Binding</tt>'s state. This implementation checks if the component has
	 * a method with signature <tt>setEditable(boolean)</tt>, and if so uses
	 * that method. If not, then it changes the component's enabledness with the
	 * editability parameter. If this behavior is not appropriate for the
	 * component of the concrete implementation, it can be overridden.
	 * <p>
	 * Note that this method may never get called, if or not it is called is
	 * controlled through the {@link #setSyncEditability(boolean)} method.
	 */
	protected void syncEditability(){
		
		boolean editable = binding.size() > 0 && binding.isEditable(0, binding.getKeys()[0]);
		
		try{
			Method m = component.getClass().getMethod
				("setEditable", new Class[]{Boolean.TYPE});
			m.invoke(component, new Object[]{new Boolean(editable)});
		}catch(Exception ex){
			component.setEnabled(editable && component.isEnabled());
		}
	}
	
	/**
	 * Implementing classes should call this method when they detect that the
	 * component's value changes; this method attempts an edit of the value in
	 * the <tt>Binding</tt>.
	 * <p>
	 * Note that this method may do nothing, if or not it performs anything is
	 * controlled through the {@link #setSyncComponentValue(boolean)} method.
	 */
	protected void componentValueChanged(){
		
		changeOriginatingComponent = component;
		try{
			processComponentValueChanged();
		}finally{
			changeOriginatingComponent = null;
		}
	}
	
	/**
	 * This method does the actual work of reacting to a change in the tracked
	 * component. Called by the {@link #componentValueChanged()} method, which
	 * takes care of tracking the change originating component. For more info
	 * see {@link #changeOriginatingComponent} documentation.
	 */
	private void processComponentValueChanged(){
		if(!syncComponentValue) return;
		
		if(binding == null || binding.size() == 0) return;
		
		// only change the value of the component if the
		// new value is different then the present one
		Object bindingValue = binding.get(0).get(binding.getKeys()[0]);
		Object componentValue = getComponentValue();
		
		// try to find out the desired class
		Class<?> attributeClass = null;
		try{
			attributeClass = 
				binding.get(0).getDataType().classForAttribute(binding.getKeys()[0]);
		}catch(UnknownKeyException e) {
			// most likely the key is for a derived property,
			// try to extrapolate the class from the value
			if(bindingValue != null) attributeClass = bindingValue.getClass();
		}

		// try to convert the component value to the class of the binding value
		if(attributeClass != null)
			try{
				componentValue = ValueConverter.toClass(componentValue, attributeClass);
			}catch(Exception ex){
				// swallow: ignore the inability to convert
			}

		// if the binding is editable and the binding value is different the component value,
		// set the value
		if(binding.isEditable(0, binding.getKeys()[0])
				&& !(JBNDUtil.equals(bindingValue, componentValue)))
			
			binding.edit(0, binding.getKeys()[0], componentValue);
	}
	
	/**
	 * Implementing class should refresh the component to update the bound value
	 * to the one in the <tt>Binding</tt> because it has possibly changed.
	 * 
	 * @param newValue The current value of the <tt>Binding</tt>, that needs to
	 *            be set on the component.
	 */
	protected abstract void bindingValueChanged(Object newValue);
	
	/**
	 * Implementing classes should return their component's current value
	 * (state).
	 * 
	 * @return Implementing classes should return their component's current
	 *         value (state).
	 */
	protected abstract Object getComponentValue();
}