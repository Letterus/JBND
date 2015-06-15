package org.jbnd.support;

import org.jbnd.*;
import org.jbnd.event.DataObjectEvent.Type;


/**
 * A <tt>DataObject</tt> implementation that wraps around a single attribute
 * value for the <tt>SINGLE_VALUE_KEY</tt> key. No other properties and property
 * keys are valid to be used with this kind of <tt>DataObject</tt>. Any value of
 * any type is valid as the attribute value.
 * 
 * @version 1.0 May 5, 2009
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class OneValDataObject extends AbstractDataObject{
	
	/**
	 * The key for the single possible attribute of a
	 * <tt>OneValDataObject</tt>.
	 */
	public static final String KEY = "__SOVDAKey";
	
	/**
	 * The <tt>DataType</tt> of all <tt>OneValDataObject</tt> instances.
	 */
	private static final SingleAttributeDataType _DATA_TYPE = new SingleAttributeDataType();
	
	/**
	 * The value for the attribute identified by <tt>KEY</tt>.
	 */
	private Object value;

	/**
	 * Creates a <tt>SingleAttributeDataSource</tt> with <tt>null</tt> as the
	 * single attribute value it contains.
	 */
	public OneValDataObject(){}
	
	/**
	 * Creates a <tt>SingleAttributeDataSource</tt> with <tt>value</tt> as the
	 * single attribute value it contains.
	 * 
	 * @param value The initial value of this <tt>SingleAttributeDataSource</tt>.
	 */
	public OneValDataObject(Object value){
		this.value = value;
	}

	public Object get(String key){
		if(DataObject.THIS_KEY.equals(key)) return this;
		if(KEY.equals(key)) return value;
		throw new UnknownKeyException(key, this);
	}

	/**
	 * @return A <tt>DataType</tt> implementation specific to
	 *         <tt>OneValDataObject</tt>.
	 */
	public DataType getDataType(){
		return _DATA_TYPE;
	}

	/**
	 * 
	 * @return <tt>false</tt>
	 */
	public boolean isPersistent(){
		return false;
	}

	/**
	 * Always throws <tt>UnknownKeyException</tt> as it is guaranteed that a
	 * <tt>OneValDataObject</tt> does not relate to anything.
	 * 
	 * @param object Ignored.
	 * @param key Ignored.
	 * @throws UnknownKeyException Always.
	 */
	public void relate(DataObject object, String key){
		throw new UnknownKeyException(key, this);
	}

	/**
	 * If <tt>KEY.equals(key)</tt> evaluates to <tt>false</tt>, an
	 * <tt>UnknownKeyException</tt> is thrown. Otherwise if the <tt>value</tt>
	 * is different then the current value for <tt>KEY</tt>, it is set and an
	 * event is fired.
	 * 
	 * @param value The new value for <tt>key</tt>.
	 * @param key Key, should be <tt>KEY</tt>.
	 * @throws UnknownKeyException If <tt>key</tt> is not equal to {@link #KEY}.
	 */
	public void set(Object value, String key){
		if(!KEY.equals(key)) 
			throw new UnknownKeyException(key, this);
		
		if(JBNDUtil.equals(this.value, value)) return;
		
		Object oldValue = this.value;
		this.value = value;
		fireDataObjectEvent(key, oldValue, Type.ATTRIBUTE_CHANGE);
	}

	/**
	 * If the value for <tt>KEY</tt> attribute is <tt>null</tt>, then
	 * <tt>null</tt> is returned, otherwise it's <tt>toString()</tt> value is
	 * returned.
	 * 
	 * @return See above.
	 */
	public String toString(){
		return value == null ? null : value.toString();
	}
	
	/**
	 * A string to log.
	 * 
	 * @return	See above.
	 */
	public String toLogString(){
		return "OneValDataObject with value: "+value;
	}

	/**
	 * Always throws <tt>UnknownKeyException</tt> as it is guaranteed that a
	 * <tt>OneValDataObject</tt> does not relate to anything.
	 * 
	 * @param object Ignored.
	 * @param key Ignored.
	 * @throws UnknownKeyException Always.
	 */
	public void unrelate(DataObject object, String key){
		throw new UnknownKeyException(key, this);
	}

	/**
	 * Accepts any value for <tt>KEY</tt> as valid, if <tt>key</tt> does not
	 * equal <tt>KEY</tt>, an <tt>UnknownKeyException</tt> is thrown.
	 * 
	 * @param object Ignored.
	 * @param key The key for which to evaluate the value.
	 * @throws UnknownKeyException If <tt>key</tt> does not equal <tt>KEY</tt>.
	 */
	public Object validate(Object value, String key) throws ValidationException{
		if(KEY.equals(key)) return value;
		throw new UnknownKeyException(key, this);
	}

	/**
	 * Never throws.
	 * 
	 * @throws ValidationException Never.
	 */
	public void validateToSave() throws ValidationException{}
	
	/**
	 * Returns <tt>true</tt> if <tt>object</tt> is a
	 * <tt>OneValDataObject</tt> instance, and it's value is equal (
	 * <tt>null</tt>-friendly) to this object's value.
	 * 
	 * @param object To compare to.
	 * @return See above.
	 */
	@Override
	public boolean equals(Object object){
		if(!(object instanceof OneValDataObject)) return false;
		return JBNDUtil.equals(value, ((OneValDataObject)object).value);
	}
	
	@Override
	public int hashCode(){
		return value == null ? -1 : value.hashCode();
	}
	
	/**
	 * A <tt>DataType</tt> for <tt>OneValDataObject</tt>s.
	 * 
	 * @version 1.0 May 5, 2009
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	private static class SingleAttributeDataType extends AbstractDataType{
		
		private static final String[] _EMPTY_STRING_ARRAY = new String[]{};
		private static final String[] _SINGLE_VALUE_KEY_ARRAY = new String[]{KEY};
		
		protected String[] _attributes(){
			return _SINGLE_VALUE_KEY_ARRAY;
		}
		
		protected Class<?> _classForAttribute(String key){
			if(KEY.equals(key)) return Object.class;
			throw new UnknownKeyException(key, this);
		}
		
		protected DataType _dataTypeForRelationship(String key){
			throw new UnknownKeyException(key, this);
		}
		
		protected PropType _propertyType(String key){
			if(KEY.equals(key)) return PropType.ATTRIBUTE;
			throw new UnknownKeyException(key, this);
		}
		
		protected String[] _toManyRelationships(){
			return _EMPTY_STRING_ARRAY;
		}
		
		protected String[] _toOneRelationships(){
			return _EMPTY_STRING_ARRAY;
		}
		
		public String inverseRelationship(String relationshipKey){
			throw new UnknownKeyException(relationshipKey, this);
		}
		
		public String name(){
			return "SingleValueDataObjectType";
		}
		
		public Object validate(Object value, String key)
				throws ValidationException{
			if(KEY.equals(key)) return value;
			throw new UnknownKeyException(key, this);
		}
	}
}