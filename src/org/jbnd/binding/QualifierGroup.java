package org.jbnd.binding;

import java.util.*;

import org.jbnd.*;
import org.jbnd.data.AbstractDataSource;
import org.jbnd.event.DataObjectEvent;
import org.jbnd.event.DataObjectListener;
import org.jbnd.qual.KeyValueQualifier;
import org.jbnd.support.JBNDUtil;
import org.jbnd.support.ValueConverter;


/**
 * A <tt>DataSource</tt> specialized in providing binding capabilities for
 * values in JBND's qualifiers (specifically in <tt>KeyValueQualifier</tt>s),
 * used in combination with <tt>QualifierBinding</tt>s. Note that this class
 * might NOT safe to use with <tt>Binding</tt>s other then
 * <tt>QualifierBinding</tt>s, though it is technically possible.
 * <p>
 * <tt>QualifierBinding</tt>s are not instantiated directly, but obtained
 * through the <tt>QualifierGroup</tt>'s <tt>getBinding(...)</tt> methods. Those
 * methods take care of connecting the group and the bindings it produces, as
 * well as generating binding keys. For an explanation why <tt>DataObject</tt>
 * property keys are not used as binding keys for <tt>QualifierBinding</tt>s,
 * check the <tt>QualifierBinding</tt> class documentation.
 * 
 * @version 1.0 Feb 11, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see QualifierBinding
 * @see #getBinding(KeyValueQualifier, Class, boolean)
 * @see #getBinding(List, List, boolean[])
 */
public final class QualifierGroup extends AbstractDataSource implements DataObjectListener{

	//	a single DataObject is ever provided by the QualifierGroup
	//	since it is only a front for values that are actually stored
	//	in KeyValueQualifiers
	private final QualifierDataObject dataObject = new QualifierDataObject();
	
	//	the DataType of the single DataObject that the QualifierGroup
	//	provides
	private final QualifierDataType dataType = new QualifierDataType();
	
	//	a Map containing String keys and QConnections
	//	the keys are the same keys that are used by the QualifierBindings
	//	belonging to this QualifierGroup
	//	the QConnection objects contain metadata for each of the keys
	private final Map<String, QConnection<?>> connections = new HashMap<String, QConnection<?>>();
	
	public DataObject get(int index){
		if(index < 0 || index > 1)
			throw new IndexOutOfBoundsException();
		return dataObject;
	}

	/**
	 * Always returns 1.
	 * 
	 * @return	1.
	 */
	public int size(){
		return 1;
	}
	
	/**
	 * Creates and returns a new <tt>QualifierBinding</tt> for the given
	 * parameters. The key of the <tt>Binding</tt> will be automatically
	 * generated from the <tt>key</tt> and <tt>operator</tt> of the given
	 * <tt>Qualifier</tt>. Automatically connects the returned
	 * <tt>QualifierBinding</tt> and this <tt>QualifierGroup</tt>.
	 * 
	 * @param qualifier The JBND <tt>KeyValueQualifier</tt> to whose value the
	 *            returned <tt>Binding</tt> will be bound to.
	 * @param c The <tt>Class</tt> of the value required by the
	 *            <tt>qualifier</tt>, used for validation and conversion.
	 * @param allowNull If or not <tt>null</tt> is a valid value for the
	 *            <tt>Qualifier</tt>, used for validation.
	 * @return A <tt>QualifierBinding</tt> bound to the value of the passed
	 *         <tt>KeyValueQualifier</tt>.
	 */
	public <T> QualifierBinding getBinding(
			KeyValueQualifier<T> qualifier, Class<T> c, boolean allowNull)
	{
		//	automatic key generation
		String key = generateKey(qualifier);
		
		//	create a new connection, and add it to the group
		QConnection<T> newQc = new QConnection<T>(qualifier, c, allowNull);
		connections.put(key, newQc);
		
		//	return the binding object
		QualifierBinding rVal = new QualifierBinding(this, key);
		
		return rVal;
	}
	
	/**
	 * Creates and returns a new <tt>QualifierBinding</tt> for the given
	 * parameters. The keys of the <tt>Binding</tt> will be automatically
	 * generated from the <tt>key</tt>s and <tt>operator</tt>s of the given
	 * <tt>Qualifier</tt>s. Automatically connects the returned
	 * <tt>QualifierBinding</tt> and this <tt>QualifierGroup</tt>. Note that all
	 * the parameters must contain the same number of elements, otherwise an
	 * exception is thrown.
	 * 
	 * @param qualifiers The JBND <tt>KeyValueQualifier</tt>s to whose values
	 *            the returned <tt>Binding</tt> will be bound to.
	 * @param classes The <tt>Class</tt>es of the values required by the
	 *            <tt>qualifier</tt>, used for validation and conversion.
	 * @param allowNull If or not <tt>null</tt>s are valid values for the
	 *            <tt>Qualifier</tt>s, used for validation.
	 * @return A <tt>QualifierBinding</tt> bound to the values of the passed
	 *         <tt>KeyValueQualifier</tt>s.
	 */
	public QualifierBinding getBinding(
			List<KeyValueQualifier<?>> qualifiers, List<Class<?>> classes, boolean[] allowNull)
	{
		//	prepare the String[] that will be used to created
		//	the Binding
		String[] keys = new String[qualifiers.size()];
		
		//	iterate through the given KeyValueQualifiers, Classes and booleans
		Iterator<KeyValueQualifier<?>> qualIt = qualifiers.iterator();
		Iterator<Class<?>> classIt = classes.iterator();
		for(int i = 0 ; i < keys.length ; i++){
			KeyValueQualifier<?> qualifier = qualIt.next();
			
			//	generate a key for the given qualifier
			keys[i] = generateKey(qualifier);
			//	create a QConnection, and add it to this group
			@SuppressWarnings("unchecked")
			QConnection<?> qc = new QConnection(qualifier, classIt.next(), allowNull[i]);
			connections.put(keys[i], qc);
		}
		
		//	return a multi-key binding
		QualifierBinding rVal = new QualifierBinding(this, keys);
		
		return rVal;
	} 
	
	/**
	 * Generates a <tt>String</tt> key unique to this
	 * <tt>QualifierGroup</tt> based on the <tt>key</tt> and <tt>operator</tt>
	 * of the given <tt>KeyValueQualifier</tt>.
	 * 
	 * @param qualifier	The qualifier for which to generate the key.
	 * @return	See above.
	 */
	private String generateKey(KeyValueQualifier<?> qualifier){
		//	the key and operator Strings
		String key = qualifier.getKey();
		String operator = KeyValueQualifier.readableStringForOperator(qualifier.getOperator());
		
		//	connect them
		key = operator + "_" + key;
		
		//	this is good enough, if the generated key is not
		//	already existing in this group
		if(!connections.containsKey(key)) return key;
		
		//	generate the key with a numerical appendix
		for(int i = 0 ; i < Integer.MAX_VALUE ; i++){
			String possibleKey = key + "_" + i;
			if(!connections.containsKey(possibleKey)) return possibleKey;
		}
		
		throw new IllegalStateException("This should NOT happen");
	}
	
	/**
	 * Validates all the values of this <tt>QualifierGroup</tt>'s
	 * bindings, according to the rules provided when creating the
	 * <tt>Binding</tt>s (the <tt>getBinding(...)</tt>) methods).
	 * 
	 * @return	A list of <tt>ValidationException</tt>s that represent
	 * 			all the invalid values of all the <tt>Binding</tt>s
	 * 			belonging to this <tt>QualifierGroup</tt>.
	 */
	public List<ValidationException> validate(){
		List<ValidationException> rVal = new LinkedList<ValidationException>();
		
		//	get all the keys
		Set<String> keys = connections.keySet();
		for(String key : keys){
			//	validate the data object on the keys
			//	of all the qualifiers that are bound
			try{
				dataObject.validate(dataObject.get(key), key);
			}catch(ValidationException ex){
				rVal.add(ex);
			}
		}
		
		return rVal;
	}
	
	/**
	 * Clears all the qualification values from this group (sets the values of
	 * all referenced <tt>KeyValueQualifier</tt>s to <tt>null</tt>).
	 */
	public void clear(){
		for(QConnection<?> connection : connections.values())
			connection.qualifier.setValue(null);
		
		QualifierGroup.this.fireDataChanged(0, 0);
	}
	
	/**
	 * <tt>DataObjectListener</tt> implementation, listens for changes in
	 * <tt>DataObject</tt>s this group tracks and fires a change event
	 * as a reaction when they happen.
	 * 
	 * @param event	The event!
	 */
	public void objectChanged(DataObjectEvent event){
		int index = JBNDUtil.indexOf(event.getDataObject(), this);
		fireDataChanged(index, index);
	}
	
	/**
	 * A <tt>DataObject</tt> implementation that serves as an in-between for
	 * <tt>Binding</tt>s and <tt>Qualifier</tt>s. Does no caching, but retrieves
	 * and sets values directly on the <tt>KeyValueQualifier</tt>s of the
	 * <tt>QualifierGroup</tt>. A single <tt>QualifierDataObject</tt> is used
	 * per <tt>QualifierGroup</tt>.
	 * 
	 * @version 1.0 Feb 11, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	private final class QualifierDataObject extends AbstractDataObject{
		
		{
			addDataObjectListener(QualifierGroup.this);
		}
		
		public Object get(String key){
			QConnection<?> qc = connections.get(key);
			if(qc == null)
				throw new UnknownKeyException(key, this);
			
			return qc.qualifier.getValue();
		}

		public DataType getDataType(){
			return dataType;
		}

		public void relate(DataObject object, String key){
			throw new UnsupportedOperationException(
			"This is a narrowed down DataObject implementation used by QualifierGroup");
		}

		@SuppressWarnings("unchecked")
		public void set(Object value, String key){
			QConnection qc = connections.get(key);
			if(qc == null)
				throw new UnknownKeyException(key, this);
			
			if(JBNDUtil.equals(value, qc.qualifier.getValue()))
				return;
			
			Object oldValue = qc.qualifier.getValue();
			qc.qualifier.setValue(value);
			fireDataObjectEvent(key, oldValue, DataObjectEvent.Type.QUALIFIER_VALUE_CHANGE);
		}

		public String toLogString(){
			return "Qualifier data object";
		}

		public void unrelate(DataObject object, String key){
			throw new UnsupportedOperationException(
			"This is a narrowed down DataObject implementation used by QualifierGroup");
		}

		public Object validate(Object value, String key)
				throws ValidationException{
			return dataType.validate(value, key);
		}

		public void validateToSave() throws ValidationException{
			throw new UnsupportedOperationException(
			"This is a narrowed down DataObject implementation used by QualifierGroup");
		}

		public boolean isPersistent(){
			return false;
		}
	}
	
	
	/**
	 * A <tt>DataType</tt> implementation, an instance of which is
	 * returned from the <tt>QualifierDataObject</tt>'s
	 * <tt>getDataType()</tt> method. Ensures some validation
	 * for different keys the <tt>QualifierGroup</tt> uses, and
	 * basic provides basic metadata.
	 *
		@version 1.0 Feb 11, 2008
		@author Florijan Stamenkovic (flor385@mac.com)
	 */
	private final class QualifierDataType extends AbstractDataType{

		public String[] _attributes(){
			return connections.keySet().toArray(new String[0]);
		}

		public Class<?> _classForAttribute(String key){
			QConnection<?> bqc = connections.get(key);
			if(bqc == null)
				throw new UnknownKeyException(key, this);
			else
				return bqc.c;
		}

		public DataType _dataTypeForRelationship(String key){
			throw new UnknownKeyException(key, dataType);
		}

		public String name(){
			return "Qualifier";
		}

		public PropType _propertyType(String key){
			if(connections.containsKey(key))
				return PropType.ATTRIBUTE;
			else
				throw new UnknownKeyException(key, this);
		}

		public String[] _toManyRelationships(){
			return new String[0];
		}

		public String[] _toOneRelationships(){
			return new String[0];
		}

		public Object validate(Object value, String key)
				throws ValidationException{
			
			//	get the qualifier connection
			QConnection<?> qc = connections.get(key);
			if(qc == null)
				throw new UnknownKeyException(key, dataType);
			
			//	check on null validity, if the value is null
			if(value == null){
				if(!qc.allowNull)
					throw new ValidationException(value, key, this);
				else
					// null is allowed, so just return it
					return value;
			}
			
			//	now check if the classes are compatible
			if(value.getClass().isAssignableFrom(qc.c))
				return value;
			else{
				//	classes are NOT compatible
				//	try to convert value to appropriate class
				try{
					return ValueConverter.toClass(value, qc.c);
				}catch(Exception ex){}
				
				//	value is of a different class
				//	and not convertible
				//	so throw!
				throw new ValidationException(value, key, this);
			}
		}

		public String inverseRelationship(String relationshipKey){
			return null;
		}
	}
	
	/**
	 * Defines a trio containing a <tt>KeyValueQualifier</tt>, a <tt>Class</tt>
	 * which is the type of the value expected by the <tt>KeyValueQualifier</tt>
	 * , and a <tt>boolean</tt> indicating if <tt>null</tt> is a valid value for
	 * the <tt>KeyValueQualifier</tt>. A <tt>QConnection</tt> is then used by
	 * the <tt>QualifierGroup</tt>, paired with a single <tt>String</tt>, which
	 * is the key that relates a <tt>QualifierBinding</tt> and the value of a
	 * single <tt>KeyValueQualifier</tt> (that being the qualifier encapsulated
	 * in an instance of this class).
	 * 
	 * @version 1.0 Feb 11, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	private final static class QConnection<T>{
		
		private final KeyValueQualifier<T> qualifier;
		
		private final Class<T> c;
		
		private final boolean allowNull;
		
		/**
		 * Creates a new <tt>QConnection</tt> with the given parameters.
		 * 
		 * @param qualifier	The qualifier being bound to a <tt>QualifierBinding</tt>.
		 * @param c		The type of value that is expected by the <tt>qualifier</tt>.
		 * @param allowNull	If or not <tt>null</tt> is a valid value for the
		 * 				<tt>qualifier</tt>.
		 */
		public QConnection(KeyValueQualifier<T> qualifier, Class<T> c, boolean allowNull){
			
			//	check that there are no null values
			if(qualifier == null)
				throw new NullPointerException("Qualifier can not be null");
			if(c == null)
				throw new NullPointerException("Class can not be null");
			
			//	assign variables
			this.qualifier = qualifier;
			this.c = c;
			this.allowNull = allowNull;
		}
	}
}