package org.jbnd.eof;

import org.jbnd.*;
import org.jbnd.support.JBNDUtil;
import org.jbnd.support.NamingSupport;
import org.jbnd.support.ValueConverter;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSValidation;


/**
 * A <tt>DataType</tt> implementation that is backed by an EOF's
 * <tt>EOClassDescription</tt>, translates <tt>EOClassDescription</tt> methods
 * to JBND compatible ones (declared in the <tt>DataType</tt> interface.
 * <p>
 * Instances can be obtained using factory methods in this class, or the
 * {@link EOFDataTypeProvider} class.
 * 
 * @version 1.0 Dec 20, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class EOFDataType extends AbstractDataType{
	
	/*
	 * 
	 * Factory methods
	 * 
	 */

	/**
	 * Returns the <tt>DataType</tt> object for the given Java
	 * <tt>Class</tt> instance. The object returned is
	 * the one that an instance of the given <tt>Class</tt>
	 * would return from it's <tt>getDataType()</tt> method.
	 * Naturally, the given <tt>Class</tt> has to be an
	 * implementation of <tt>DataObject</tt> interface.
	 * 
	 * @param clazz	See above.
	 * @return	See above.
	 */
	public static DataType getInstance(Class<?> clazz){
		EOClassDescription cd = EOClassDescription.classDescriptionForClass(clazz);
		return getInstance(cd);
	}

	/**
	 * Returns the <tt>DataType</tt> object for the given
	 * <tt>DataType</tt> name. The object returned has to be
	 * the one that will return <tt>String</tt> from
	 * it's <tt>name()</tt> that is equal to the parameter
	 * passed to this method.
	 * 
	 * @param name	See above.
	 * @return	See above.
	 */
	public static DataType getInstance(String name){
		EOClassDescription cd = EOClassDescription.classDescriptionForEntityName(name);
		return getInstance(cd);
	}

	/**
	 * A method not declared in the <tt>DataTypeProvider</tt> interface,
	 * used to provide <tt>DataType</tt>s that are reflecting the
	 * given <tt>EOClassDescription</tt>s. The value returned is
	 * an instance of <tt>EOFDataType</tt>.
	 * 
	 * @param description	See above.
	 * @return	See above.
	 * @see	EOFDataType
	 */
	public static DataType getInstance(EOClassDescription description){
		EOFDataType dt = EOFDataTypeProvider.eofDataTypes.get(description);
		if(dt == null){
			dt = new EOFDataType(description);
			EOFDataTypeProvider.eofDataTypes.put(description, dt);
		}
		
		return dt;
	}
	
	
	/*
	 * 
	 * End of factory methods
	 * 
	 * 
	 */
	
	
	
	
	//	the EOClassDescription backing this DataType
	private EOClassDescription	classDescription;
	
	/**
	 * Returns the <tt>EOClassDescription</tt> this <tt>EOFDataType</tt>
	 * is based on.
	 * 
	 * @return	See above.
	 */
	public EOClassDescription getClassDescription(){
		return classDescription;
	}
	
	/**
	 * Creates a new <tt>EOFDataType</tt> based on the
	 * given <tt>EOClassDescription</tt>.
	 * 
	 * @param description	The <tt>EOClassDescription</tt>
	 * 			backing this <tt>EOFDataType</tt>.
	 */
	EOFDataType(EOClassDescription description){
		this.classDescription = description;
		if(description == null)
			throw new IllegalStateException("No class description provided to EOFDataType");
	}
	
	public String[] _attributes(){
		NSArray<String> cdAttributes = classDescription.attributeKeys();
		String[] rVal = new String[cdAttributes.count()];
		for(int i = 0 ; i < rVal.length; i++)
			rVal[i] = cdAttributes.objectAtIndex(i);
		return rVal;
	}
	
	public String[] _toManyRelationships(){
		NSArray<String> cdToMany = classDescription.toManyRelationshipKeys();
		String[] rVal = new String[cdToMany.count()];
		for(int i = 0 ; i < rVal.length; i++)
			rVal[i] = cdToMany.objectAtIndex(i);
		return rVal;
	}
		
	public String[] _toOneRelationships(){
		NSArray<String> cdToOne = classDescription.toOneRelationshipKeys();
		String[] rVal = new String[cdToOne.count()];
		for(int i = 0 ; i < rVal.length; i++)
			rVal[i] = cdToOne.objectAtIndex(i);
		return rVal;
	}

	public Class<?> _classForAttribute(String key){
		
		switch(propertyType(key)){
			case ATTRIBUTE :
				return classDescription.classForAttributeKey(key);
			case DERIVED_ATTRIBUTE :
				return derivedPropClasses.get(key);
			default :
				throw new UnknownKeyException(key, this);
		}
	}

	public DataType _dataTypeForRelationship(String key){
		
		switch(propertyType(key)){
			
			// for normal to one and to manys, consult the class description
			case TO_MANY : case TO_ONE : {
				try{
					EOClassDescription destinationCD = 
						classDescription.classDescriptionForDestinationKey(key);
					
					if(destinationCD == null) throw new UnknownKeyException(key, this);
					else return getInstance(destinationCD);
				
				}catch(NSKeyValueCoding.UnknownKeyException ex){
					throw new UnknownKeyException(key, this);
				}
			}
			
			// for derived relationships, consult the relationship class
			case DERIVED_TO_MANY : case DERIVED_TO_ONE :
				return getInstance(derivedPropClasses.get(key));
				
			// for non-relationships, throw an exception
			default :
				throw new UnknownKeyException(key, this);
		}
	}

	/**
	 * Returns the name of the <tt>DataType</tt>, the same
	 * <tt>String</tt> returned by
	 * <tt>backingClassDescription.entityName()</tt>.<p>
	 * 
	 * It is not guaranteed that the <tt>String</tt> returned by this
	 * method will be presentable to the end user. To ensure user
	 * presentability, use <tt>NamingSupport</tt> methods.
	 * 
	 * @return	Name of the <tt>DataType</tt>.
	 * @see		NamingSupport#typeName(String)
	 */
	public String name(){
		return classDescription.entityName();
	}

	public PropType _propertyType(String key){
		
		// is it derived?
		PropType derivedPropType = derivedPropTypes.get(key);
		if(derivedPropType != null) return derivedPropType;
		
		//	iterate through the attributes
		//	check if the key is an attribute
		String[] attributes = attributes();
		for(String attribute : attributes)
			if(attribute.equals(key))
				return PropType.ATTRIBUTE;
		
		//	iterate through the relationships
		//	check if the key is an relationship
		String[] toOnes = toOneRelationships();
		for(String toOne : toOnes)
			if(toOne.equals(key))
				return PropType.TO_ONE;
		
		//	iterate through the relationships
		//	check if the key is an relationship
		String[] toManys = toManyRelationships();
		for(String toMany : toManys)
			if(toMany.equals(key))
				return PropType.TO_MANY;
		
		if(DataObject.THIS_KEY.equals(key))
			return PropType.UNDEFINED;
		
		throw new UnknownKeyException(key, this);
	}
	
	

	/**
	 * Validates the given value for the given key, according to the rules of
	 * <tt>this DataType</tt>. However, even if the value is valid according
	 * to this method, it is not guaranteed to be valid on the
	 * <tt>DataObject</tt> it will being set on, even though that is most
	 * likely the case. This method may perform some coercing, as defined by
	 * validation rules, where the coerced value will be returned. If no
	 * coercing occurs, the <tt>value</tt> parameter will be returned.
	 * <p>
	 * This method will try to convert the given <tt>value</tt>, if
	 * necessary, to the appropriate class, using the JBND
	 * <tt>ValueConverter</tt>.
	 * 
	 * @param value The value to be validated.
	 * @param key The key for which it should be validated.
	 * @return The valid, potentially coerced value.
	 * @throws ValidationException If the given value is not valid for the given
	 *             <tt>key</tt>.
	 * @see DataObject#validate(Object, String)
	 * @see ValueConverter
	 */
	public Object validate(Object value, String key) throws ValidationException{
		Object rVal = null;
		
		//	do value conversion
		try{
			value = ValueConverter.toClass(value, JBNDUtil.classForProperty(this, key));
		}catch(Exception ex){}	//	ignore it
		
		try{
			rVal = classDescription.validateValueForKey(value, key);
		}catch(NSValidation.ValidationException ex){
			throw new ValidationException(value, key, this);
		}
		
		return rVal;
	}

	public String inverseRelationship(String relationshipKey){
		return classDescription.inverseForRelationshipKey(relationshipKey);
	}
	
	/**
	 * Returns {@code this == object}, since there can only be one
	 * <tt>DataType</tt> per entity.
	 * 
	 * @param object
	 * @return
	 */
	@Override
	public boolean equals(Object object){
		return object == this;
	}
	
	@Override
	public String toString(){
		return "EOFDataType for: "+name();
	}
}