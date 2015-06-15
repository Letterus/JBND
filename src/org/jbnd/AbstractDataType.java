package org.jbnd;

import java.util.*;

import org.jbnd.data.DataSource;
import org.jbnd.data.ListDataSource;
import org.jbnd.support.JBNDUtil;
import org.jbnd.support.OneValDataObject;


/**
 * Performance optimized skeleton implementation, also implements property
 * derivation methods, the best starting point for implementing a concrete
 * <tt>DataType</tt>.
 * 
 * @version 1.0 Mar 24, 2009
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public abstract class AbstractDataType implements DataType{
	
	
	// cache for keys designating non-derived attributes
	private String[] attributes;
	
	/**
	 * Performance optimization: this implementation retrieves the attributes of
	 * this <tt>DataType</tt> through the {@link #_attributes()} method, and
	 * caches that value to be returned on any subsequent call.
	 * 
	 * @return All the attribute keys that <tt>DataObject</tt>s of this type
	 *         have.
	 */
	public String[] attributes(){
		//	lazy initialization
		if(attributes == null){
			attributes = _attributes();
		}
		
		return attributes;
	}
	
	/**
	 * Should return value as defined in {@link DataType#attributes()}, called
	 * by the {@link #attributes()} method as a performance optimization.
	 * 
	 * @return See above.
	 */
	protected abstract String[] _attributes();
	
	// cache for keys designating non-derived to-one relationships
	private String[] toOneRelationships;
	
	/**
	 * Performance optimization: this implementation retrieves the to-one
	 * relationships of this <tt>DataType</tt> through the
	 * {@link #_toOneRelationships()} method, and caches that value to be
	 * returned on any subsequent call.
	 * 
	 * @return All the to-one relationship keys that <tt>DataObject</tt>s of
	 *         this type have.
	 */
	public String[] toOneRelationships(){
		//	lazy initialization
		if(toOneRelationships == null){
			toOneRelationships = _toOneRelationships();
		}
		
		return toOneRelationships;
	}
	
	/**
	 * Should return value as defined in {@link DataType#toOneRelationships()}, called
	 * by the {@link #toOneRelationships()} method as a performance optimization.
	 * 
	 * @return See above.
	 */
	protected abstract String[] _toOneRelationships();
	
	// cache for keys designating non-derived to-many relationships
	private String[] toManyRelationships;
	
	/**
	 * Performance optimization: this implementation retrieves the to-many
	 * relationships of this <tt>DataType</tt> through the
	 * {@link #_toManyRelationships()} method, and caches that value to be
	 * returned on any subsequent call.
	 * 
	 * @return All  the to-many relationship keys that <tt>DataObject</tt>s of
	 *         this type have.
	 */
	public String[] toManyRelationships(){
		//	lazy initialization
		if(toManyRelationships == null){
			toManyRelationships = _toManyRelationships();
		}
		
		return toManyRelationships;
	}
	
	/**
	 * Should return value as defined in {@link DataType#toManyRelationships()}, called
	 * by the {@link #toManyRelationships()} method as a performance optimization.
	 * 
	 * @return See above.
	 */
	protected abstract String[] _toManyRelationships();
	
	// cache for the keys designating the union of all properties (derived included)
	// of this data type
	private String[] _properties;
	
	/**
	 * Returns all the property keys that <tt>DataObject</tt>s of this type
	 * have. This is the sum of all the attribute, to-one relationship, to-many
	 * relationships and derived property keys. The sorting of returned
	 * <tt>String</tt>s is not defined.
	 * 
	 * @return All all the property keys that <tt>DataObject</tt>s of this type
	 *         have.
	 */
	public String[] properties(){
			
		if(_properties == null){	
			
			// merge them all!
			String[]
			       attributes = attributes(),
			       toOnes = toOneRelationships(),
			       toManys = toManyRelationships(),
			       derived = derivedProperties();
			
			List<String> allProps = new ArrayList<String>(
					attributes.length + toOnes.length + toManys.length + derived.length);
			
			for(String attribute : attributes)
				allProps.add(attribute);
			for(String toOne : toOnes)
				allProps.add(toOne);
			for(String toMany : toManys)
				allProps.add(toMany);
			for(String derivedProp : derived)
				allProps.add(derivedProp);
			
			_properties = allProps.toArray(new String[allProps.size()]);
		}
		
		return _properties;
	}
	
	// map containing class for attribute values, performance optimization
	private final Map<String, Class<?>> classForAttribute = new HashMap<String, Class<?>>();
	
	/**
	 * Performance optimization: this implementation retrieves the class for attribute
	 * (derived attributes too) of this <tt>DataType</tt> through the
	 * {@link #_classForAttribute(String)} method, and caches that value to be
	 * returned on any subsequent call.
	 * 
	 * @param key The key designating the attribute for which the class is sought.
	 * @return The <tt>Class</tt> object of which the given attribute's values
	 *         are instances.
	 * @throws UnknownKeyException If this <tt>DataType</tt> has no attribute
	 *             for the given key.
	 */
	public Class<?> classForAttribute(String key){
		
		Class<?> rVal = classForAttribute.get(key);
		if(rVal == null){
			rVal = _classForAttribute(key);
			classForAttribute.put(key, rVal);
		}
		
		return rVal;
	}
	
	/**
	 * Should return value as defined in
	 * {@link DataType#classForAttribute(String)}, called by the
	 * {@link #classForAttribute(String)} method as a performance optimization.
	 * 
	 * @param key See {@link #classForAttribute(String)}.
	 * @return See {@link #classForAttribute(String)}.
	 * @throws UnknownKeyException See {@link #classForAttribute(String)}.
	 */
	protected abstract Class<?> _classForAttribute(String key);
	
	// map containing data type for relationship values, performance optimization
	private final Map<String, DataType> dataTypeForRelationship = 
		new HashMap<String, DataType>();
	
	/**
	 * Performance optimization: this implementation retrieves the
	 * <tt>DataType</tt> for relationship (derived relationships too) of this
	 * <tt>DataType</tt> through the {@link #_dataTypeForRelationship(String)}
	 * method, and caches that value to be returned on any subsequent call.
	 * 
	 * @param key The key designating the relationship for which the
	 *            <tt>DataType</tt> is sought.
	 * @return The <tt>DataType</tt> of which the given relationship's values
	 *         are instances.
	 * @throws UnknownKeyException If this <tt>DataType</tt> has no relationship
	 *             for the given key.
	 */
	public DataType dataTypeForRelationship(String key){
		
		DataType rVal = dataTypeForRelationship.get(key);
		if(rVal == null){
			rVal = _dataTypeForRelationship(key);
			dataTypeForRelationship.put(key, rVal);
		}
		
		return rVal;
	}
	
	/**
	 * Should return value as defined in
	 * {@link DataType#dataTypeForRelationship(String)}, called by the
	 * {@link #dataTypeForRelationship(String)} method as a performance
	 * optimization.
	 * 
	 * @param key See {@link #dataTypeForRelationship(String)}.
	 * @return See {@link #dataTypeForRelationship(String)}.
	 * @throws UnknownKeyException See {@link #dataTypeForRelationship(String)}.
	 */
	protected abstract DataType _dataTypeForRelationship(String key);
	
	
	// map containing prop types for property keys, performance optimization
	private final Map<String, PropType> propTypes = 
		new HashMap<String, PropType>();
	
	/**
	 * Performance optimization: this implementation retrieves the
	 * <tt>PropType</tt> for a property of this <tt>DataType</tt> that is
	 * designated by <tt>key</tt>, through the {@link #_propertyType(String)}
	 * method, and caches that value to be returned on any subsequent call.
	 * 
	 * @param key The key designating the property for which the
	 *            <tt>PropType</tt> is sought.
	 * @return The <tt>PropType</tt> of the property designated by <tt>key</tt>.
	 * @throws UnknownKeyException If this <tt>DataType</tt> has no property for
	 *             the given key.
	 */
	public PropType propertyType(String key){
		
		PropType rVal = propTypes.get(key);
		if(rVal == null){
			rVal = _propertyType(key);
			propTypes.put(key, rVal);
		}
		
		return rVal;
	}
	
	/**
	 * Should return value as defined in
	 * {@link DataType#propertyType(String)}, called by the
	 * {@link #propertyType(String)} method as a performance
	 * optimization.
	 * 
	 * @param key See {@link #propertyType(String)}.
	 * @return See {@link #propertyType(String)}.
	 * @throws UnknownKeyException See {@link #propertyType(String)}.
	 */
	protected abstract PropType _propertyType(String key);
	
	
	
	/*
	 * 
	 * 
	 * DERIVED PROPERTY STUFF
	 * 
	 * 
	 */
	
	
	/**
	 * Contains a <tt>Boolean</tt> entry for every derived property added to
	 * this <tt>DataType</tt> using the
	 * {@link #addDerivedProp(String, org.jbnd.DataType.PropType, Class, String...)}
	 * method, that indicates if or not value caching should be performed for
	 * the derived property designated by the key.
	 * 
	 * @see #shouldCacheDerivedPropValues(String)
	 */
	private final Map<String, Boolean> derivedPropCachingSwitches =
		new HashMap<String, Boolean>();
	
	/**
	 * Contains a <tt>PropType</tt> entry for every derived property added to
	 * this <tt>DataType</tt> using the
	 * {@link #addDerivedProp(String, org.jbnd.DataType.PropType, Class, String...)}
	 * method, that indicates the derived property's type.
	 */
	protected final Map<String, PropType> derivedPropTypes = 
		new HashMap<String, PropType>();
	
	/**
	 * Contains a <tt>Class</tt> entry for every derived property added to this
	 * <tt>DataType</tt> using the
	 * {@link #addDerivedProp(String, org.jbnd.DataType.PropType, Class, String...)}
	 * method, that indicates the derived property's class.
	 */
	protected final Map<String, Class<?>> derivedPropClasses = 
		new HashMap<String, Class<?>>();
	
	/**
	 * Map keys are properties that one or more derived properties are derived
	 * from, map entries are <tt>Set</tt>s of derived properties. For example,
	 * if a derived property 'fullName' is derived from 'firstName' and
	 * 'lastName', then this map will contain two entries: one for the
	 * 'firstName' key, containing the 'fullName', and the other for the
	 * 'lastName' key, also containing the 'fullName' key.
	 */
	protected final Map<String, Set<String>> derivedPropsForDerivedFrom = 
		new HashMap<String, Set<String>>();
	
	/**
	 * Map keys are derived properties, map values are the properties they are
	 * derived from.
	 */
	protected final Map<String, Set<String>> derivedFromForDerivedProp = 
		new HashMap<String, Set<String>>();
	
	// lazily initialized cache of derived prop keys
	private String[] derivedProperties;
	
	/**
	 * Returns all the derived property keys (derived attributes and derived
	 * relationships) that <tt>DataObject</tt>s of this type have. The sorting
	 * of returned <tt>String</tt>s is not defined.
	 * 
	 * @return All all the derived property keys that <tt>DataObject</tt>s of
	 *         this type have.
	 */
	public String[] derivedProperties(){
		
		if(derivedProperties == null){
			derivedProperties = derivedPropTypes.keySet().toArray(
					new String[derivedPropTypes.size()]);
		}
		
		return derivedProperties;
	}

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
			String... derivedFromProps)
	{
		// first check if the prop type is acceptable
		switch(type){
			case DERIVED_ATTRIBUTE : case DERIVED_TO_MANY : case DERIVED_TO_ONE :
				break;
			default :
				throw new IllegalArgumentException("Can't set a derived prop of type: "+type);
		}
		
		if(key == null) throw new NullPointerException("Can't use a null key");
		if(propClass == null) throw new NullPointerException("Can't use a null prop class");
		
		// nullify cache, to make it re-initialize
		derivedProperties = null;
		
		// set derivation config
		derivedPropClasses.put(key, propClass);
		derivedPropTypes.put(key, type);
		derivedPropCachingSwitches.put(key, derivedFromProps.length > 0);
		
		// store the info about derived prop -> deriving props
		Set<String> derivedFromPropsSet = derivedPropsForDerivedFrom.get(key);
		if(derivedFromPropsSet == null){
			
			// if this set does not exist, create it
			derivedFromPropsSet = new HashSet<String>();
			derivedFromForDerivedProp.put(key, derivedFromPropsSet);
		}
		for(String derivedFrom : derivedFromProps)
			derivedFromPropsSet.add(derivedFrom);
		
		// store the info about deriving properties -> derived props
		for(String derivedFrom : derivedFromProps){
			
			// clean it up
			derivedFrom = JBNDUtil.cleanPath(derivedFrom);
			
			// init a set of keys that are derived from the key we have
			Set<String> derivedProps = derivedPropsForDerivedFrom.get(derivedFrom);
			if(derivedProps == null){
				
				// if this set does not exist, create it
				derivedProps = new HashSet<String>();
				derivedPropsForDerivedFrom.put(derivedFrom, derivedProps);
			}
			
			derivedProps.add(key);
		}
	}
	
	/**
	 * Returns a <tt>Set</tt> of keys designating properties that are derived
	 * from the given <tt>key</tt>, or <tt>Collections.EMPTY_SET</tt> if there
	 * aren't any.
	 * 
	 * @param key The key that might be used in the derivation of any number of
	 *            derived properties.
	 * @return See above.
	 */
	@SuppressWarnings("unchecked")
	public Set<String> propsDerivedFrom(String key){
		Set<String> rVal = derivedPropsForDerivedFrom.get(key);
		if(rVal == null) return Collections.EMPTY_SET;
		return rVal;
	}
	
	/**
	 * Returns a <tt>Set</tt> of keys designating properties that are used to
	 * derive a property designated by <tt>key</tt>, or
	 * <tt>Collections.EMPTY_SET</tt> if there aren't any.
	 * 
	 * @param key The key designating a derived property.
	 * @return See above.
	 */
	@SuppressWarnings("unchecked")
	public Set<String> propertiesThatDerive(String key){
		Set<String> rVal = derivedFromForDerivedProp.get(key);
		if(rVal == null) return Collections.EMPTY_SET;
		return rVal;
	}
	
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
	public boolean shouldCacheDerivedPropValues(String key){
		Boolean rVal = derivedPropCachingSwitches.get(key);
		if(rVal == null) throw new UnknownKeyException(key, this);
		return rVal;
	}

	/*
	 * VALUE SET STUFF
	 */
	private final Map<String, Set<Object>> valueSetSets = new HashMap<String, Set<Object>>();
	private final Map<String, ListDataSource> valueSetSources = new HashMap<String, ListDataSource>();
	private String[] valueSetKeys;

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
	public boolean addToValueSet(Object value, String key){
		
		// if the set adds it, add to the datasource
		if(valueSetSets.get(key).add(value)){
			ListDataSource dataSource = valueSetSources.get(key);
			dataSource.add(new OneValDataObject(value));
			return true;
		}
		
		return false;
	}
	
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
	public DataSource valueSetFor(String key){
		
		ListDataSource rVal = valueSetSources.get(key);
		
		// if not yet initialized, do that now
		if(rVal == null){
			
			valueSetSets.put(key, new HashSet<Object>());
			rVal = new ListDataSource();
			valueSetSources.put(key, rVal);
			
			// flush the keys cache
			valueSetKeys = null;
		}
		
		return rVal;
	}
	
	/**
	 * Returns the keys of all value sets tracked by this <tt>DataType</tt>,
	 * that have been initialized by the {@link #valueSetFor(String)} method.
	 * 
	 * @return See above.
	 */
	public String[] valueSetKeys(){
		if(valueSetKeys == null){
			valueSetKeys = valueSetSets.keySet().toArray(new String[]{});
		}
		
		return valueSetKeys;
	}
}