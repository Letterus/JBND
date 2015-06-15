package org.jbnd;

import java.util.Set;

import org.jbnd.data.DataSource;
import org.jbnd.support.NamingSupport;


/**
 * Defines a common type to which <tt>DataObject</tt>s belong. The
 * <tt>DataType</tt> interface does not define all the methods that might be
 * required by an implementation class to actually provide functionalities it
 * needs, but only a subset which is used in the data binding process.
 * <p>
 * Persistence systems can take different approaches to providing
 * <tt>DataType</tt> objects: they can be instantiated in a parameterized way,
 * pre-defined programatically, or obtained from an outside source. For the
 * third mentioned scenario the <tt>DataTypeProvider</tt> interface is provided.
 * <p>
 * Though this can not be enforced by the interface, it is recommended that
 * implementing classes implement the <tt>equals(Object)</tt> method in a fast
 * and absolute way.
 * 
 * @version 1.0 Dec 16, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see DataTypeProvider
 */
public interface DataType{

	/**
	 * Defines different property types (attributes, relationships etc).
	 * 
	 * @version 1.0 Mar 18, 2009
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public static enum PropType{
		ATTRIBUTE(false),
		TO_ONE(false),
		TO_MANY(false),
		DERIVED_ATTRIBUTE(true),
		DERIVED_TO_ONE(true),
		DERIVED_TO_MANY(true),
		UNDEFINED(false);
		
		private final boolean isDerived;
		
		private PropType(boolean isDerived){
			this.isDerived = isDerived;
		}
		
		/**
		 * Indicates if the property of this <tt>PropType</tt> is a derived
		 * property (a property that is evaluated at runtime based on some other
		 * property / properties).
		 * 
		 * @return See above.
		 */
		public boolean isDerived(){
			return isDerived;
		}
	}

	/**
	 * Returns the name of the <tt>DataType</tt>, for example if the data type
	 * is describing employee data, the name returned by this method will most
	 * likely be 'Employee'.
	 * <p>
	 * It is not guaranteed that the <tt>String</tt> returned by this method
	 * will be presentable to the end user. To ensure user presentability, use
	 * <tt>NamingSupport</tt> methods.
	 * 
	 * @return Name of the <tt>DataType</tt>.
	 * @see NamingSupport#typeName(String)
	 */
	public String name();

	/**
	 * Returns all the property keys that <tt>DataObject</tt>s of this type
	 * have. This is the sum of all the attribute, to-one relationship, to-many
	 * relationships and derived property keys. The sorting of returned
	 * <tt>String</tt>s is not defined.
	 * 
	 * @return All all the property keys that <tt>DataObject</tt>s of this type
	 *         have.
	 */
	public String[] properties();

	/**
	 * Returns the property type for the given <tt>key</tt>.
	 * 
	 * @param key The key designating the property for which the
	 *            <tt>PropType</tt> is sought.
	 * @return The <tt>PropType</tt> of the property designated by <tt>key</tt>.
	 * @throws UnknownKeyException If this <tt>DataType</tt> has no property for
	 *             the given key.
	 */
	public PropType propertyType(String key);

	/**
	 * Returns all the attribute (non relationship property) keys that
	 * <tt>DataObject</tt>s of this type have. The sorting of returned
	 * <tt>String</tt>s is not defined.
	 * 
	 * @return All all the attribute keys that <tt>DataObject</tt>s of this type
	 *         have.
	 */
	public String[] attributes();

	/**
	 * Returns all the to-one relationship keys that <tt>DataObject</tt>s of
	 * this type have. The sorting of returned <tt>String</tt>s is not defined.
	 * 
	 * @return All all the to-one relationship keys that <tt>DataObject</tt>s of
	 *         this type have.
	 */
	public String[] toOneRelationships();

	/**
	 * Returns all the to-many relationship keys that <tt>DataObject</tt>s of
	 * this type have. The sorting of returned <tt>String</tt>s is not defined.
	 * 
	 * @return All all the to-many relationship keys that <tt>DataObject</tt>s
	 *         of this type have.
	 */
	public String[] toManyRelationships();

	/**
	 * Returns the key for the relationship pointing back to this
	 * <tt>DataType</tt> from that named by <tt>relationshipKey</tt>, or
	 * <tt>null</tt> if there isn't one.
	 * 
	 * @param relationshipKey A relationship in this <tt>DataType</tt>.
	 * @return See above.
	 */
	public String inverseRelationship(String relationshipKey);

	/**
	 * Returns the <tt>Class</tt> object of which the given attribute's (derived
	 * attributes included) values are instances.
	 * 
	 * @param key The key designating the attribute for which the class is sought.
	 * @return The <tt>Class</tt> object of which the given attribute's values
	 *         are instances.
	 * @throws UnknownKeyException If this <tt>DataType</tt> has no attribute
	 *             for the given key.
	 */
	public Class<?> classForAttribute(String key);

	/**
	 * Returns the <tt>DataType</tt> object of which the given relationship's
	 * values are instances (derived relationships included).
	 * 
	 * @param key The key designating the relationship for which the
	 *            <tt>DataType</tt> is sought.
	 * @return The <tt>DataType</tt> of which the given relationship's values
	 *         are instances.
	 * @throws UnknownKeyException If this <tt>DataType</tt> has no relationship
	 *             for the given key.
	 */
	public DataType dataTypeForRelationship(String key);

	/**
	 * Validates the given value for the given key, according to the rules of
	 * <tt>this DataType</tt>. However, even if the value is valid according to
	 * this method, it is not guaranteed to be valid on the <tt>DataObject</tt>
	 * it will being set on, even though that is most likely the case. This
	 * method may perform some coercing, as defined by validation rules, where
	 * the coerced value will be returned. If no coercing occurs, the
	 * <tt>value</tt> parameter will be returned.
	 * 
	 * @param value The value to be validated.
	 * @param key The key for which it should be validated.
	 * @return The valid, potentially coerced value.
	 * @throws ValidationException If the given value is not valid for the given
	 *             <tt>key</tt>.
	 * @see DataObject#validate(Object, String)
	 */
	public Object validate(Object value, String key) throws ValidationException;

	
	
	
	
	
	
	
	/*
	 * DERIVED PROPERTY STUFF
	 */

	/**
	 * Returns all the derived property keys (derived attributes and derived
	 * relationships) that <tt>DataObject</tt>s of this type have. The sorting
	 * of returned <tt>String</tt>s is not defined.
	 * 
	 * @return All all the derived property keys that <tt>DataObject</tt>s of
	 *         this type have.
	 */
	public String[] derivedProperties();

	/**
	 * Adds meta-information about a derived property (does <b>not</b> accept a
	 * property definition). This meta-info could (and should) be used for
	 * derived property value caching, to reduce the overhead involved in
	 * calculating a derived property every time it is asked for. Also the info
	 * (specifically the derived property type and class) is used throughout
	 * JBND where such info is required.
	 * <p>
	 * If no prop keys are passed as the <tt>derivedFromProps</tt> argument,
	 * then JBND should not perform value caching for the derived property
	 * designated by <tt>key</tt>. This is controlled through the
	 * {@link #shouldCacheDerivedPropValues(String)} method. In that sense the
	 * <tt>derivedFromProps</tt> argument has a dual meaning: it indicates
	 * whether value caching should be performed, and if so, indicates which
	 * properties the derived prop is derived from, which makes it possible to
	 * release cached values, when it is necessary.
	 * 
	 * @param key The key of the derived property.
	 * @param type The property type, must be one of the <tt>PropertyType</tt>s
	 *            that start with <tt>DERIVED_</tt>.
	 * @param propClass The class of the property, for attributes it should be
	 *            the attribute value class, for relationship it should be the
	 *            class of related <tt>DataObject</tt>s.
	 * @param derivedFromProps An arbitrary number (zero or more) of properties
	 *            that the property accessed through <tt>key</tt> is derived
	 *            from.
	 */
	public void addDerivedProp(String key, PropType type, Class<?> propClass,
			String... derivedFromProps);

	/**
	 * Returns a <tt>Set</tt> of keys designating properties that are derived
	 * from the given <tt>key</tt>, or <tt>Collections.EMPTY_SET</tt> if there
	 * aren't any.
	 * 
	 * @param key The key that might be used in the derivation of any number of
	 *            derived properties.
	 * @return See above.
	 */
	public Set<String> propsDerivedFrom(String key);
	
	/**
	 * Returns a <tt>Set</tt> of keys designating properties that are used to
	 * derive a property designated by <tt>key</tt>, or
	 * <tt>Collections.EMPTY_SET</tt> if there aren't any.
	 * 
	 * @param key The key designating a derived property.
	 * @return See above.
	 */
	public Set<String> propertiesThatDerive(String key);
	
	/**
	 * Indicates whether or not JBND should perform value caching for the
	 * derived property designated by <tt>key</tt>. This method should return
	 * <tt>false</tt> for derived properties which were added (using the
	 * {@link #addDerivedProp(String, PropType, Class, String...)} method)
	 * without an indication from which properties they are derived, and
	 * <tt>true</tt> for all other derived properties.
	 * 
	 * @param key The key designating the derived property in question.
	 * @return See above.
	 * @throws UnknownKeyException If <tt>key</tt> does not designate a derived
	 *             property.
	 */
	public boolean shouldCacheDerivedPropValues(String key);
	
	
	
	
	
	/*
	 * VALUE SET STUFF
	 */

	/**
	 * Attempts to add the given <tt>value</tt> to the value set maintained by
	 * this <tt>DataType</tt> for <tt>propertyKey</tt>. If an object equal to
	 * <tt>value</tt> is not yet contained in the <tt>DataSource</tt> that
	 * represents the value set, it will be added, resulting in that
	 * <tt>DataSource</tt> firing an event.
	 * <p>
	 * A value set for a key can be obtained using {@link #valueSetFor(String)},
	 * which is also used to initialize a value set. A value set must be
	 * initialized before attempting to add values to it using this method.
	 * 
	 * @param value The value to attempt to add.
	 * @param key The key of the value set to add to, typically a key
	 *            designating an attribute in this <tt>DataType</tt>.
	 * @return If or not the value was added.
	 * @throws NullPointerException If this method is called before a value set
	 *             for <tt>key</tt> has been initialized.
	 */
	public boolean addToValueSet(Object value, String key);
	
	/**
	 * Initializes and returns a value set for the given <tt>key</tt>; if a
	 * value set for <tt>key</tt> has been initialized already, it is simply
	 * returned.
	 * <p>
	 * The value set is represented by a <tt>DataSource</tt> that contains
	 * <tt>OneValDataObject</tt>s. Generally you should not try to edit the
	 * contents of the returned <tt>DataSource</tt> directly but use the
	 * {@link #addToValueSet(Object, String)} method to add values to it.
	 * 
	 * @param key The key (most often designating an attribute in this
	 *            <tt>DataType</tt>, but not necessarily) for which a value set
	 *            is sought.
	 * @return See above.
	 */
	public DataSource valueSetFor(String key);
	
	/**
	 * Returns the keys of all value sets tracked by this <tt>DataType</tt>,
	 * that have been initialized by the {@link #valueSetFor(String)} method.
	 * 
	 * @return See above.
	 */
	public String[] valueSetKeys();
}