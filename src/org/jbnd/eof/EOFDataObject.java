package org.jbnd.eof;

import java.util.*;

import org.jbnd.AbstractDataObject;
import org.jbnd.DataObject;
import org.jbnd.DataType;
import org.jbnd.DataType.PropType;
import org.jbnd.event.DataObjectEvent;
import org.jbnd.event.DataObjectListener;
import org.jbnd.event.DataObjectEvent.Type;
import org.jbnd.paths.KeyPathChangeManager;
import org.jbnd.support.JBNDUtil;
import org.jbnd.support.NamingSupport;
import org.jbnd.support.ValueConverter;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSValidation;


/**
 * <tt>DataObject</tt> implementation connecting Enterprise Objects Frameworks
 * (by Apple<sup>TM</sup>) with the JBND library, as a default implementation to
 * be used as a superclass (direct or otherwise) of all client side EO classes
 * in a WOJC application using JBND. This however is not imperative, since JBND
 * is entirely driven by the <tt>DataObject</tt> interface. If you need to use
 * your own custom subclass of <tt>EOGenericRecord</tt>, JBND will work as long
 * as it implements <tt>DataObject</tt>.
 * <p>
 * <b>IMPORTANT:</b> This implementation of <tt>DataObject</tt> provides means
 * to fire <tt>DataObjectEvent</tt>s, which is required for the proper
 * functioning of the JBND library, but does NOT fire them. It is required that
 * subclasses take care of that by calling
 * {@link #fireDataObjectEvent(String, Object, int)} every time after value is
 * set (most likely this would be done in per-key setters in generated EO
 * classes). <tt>DataObject</tt> method implementations also do not fire events
 * by themselves, they call <tt>takeValue...</tt>, as opposed to
 * <tt>takeStoredValue...</tt>, which should result in the calling of
 * specialized setters, which should take care of event firing.
 * <p>
 * This subclass of <tt>EOGenericRecord</tt> implements the JBND specific
 * {@link DataObject#THIS_KEY} to return the EO itself.
 * <p>
 * Derived property monitoring and caching is fully automated by this class. To
 * enable this feature the <tt>EOFDataType</tt> of this <tt>EOFDataObject</tt>
 * needs to be made aware of property derivations, using
 * {@link DataType#addDerivedProp(String, PropType, Class, String...)}. This is
 * best done through the {@link #initDataType(EOFDataType)} method of this class. Note
 * that if you access derived properties using JBND's key-value access
 * mechanism, you *have* to make JBND aware of those derived properties. Even if
 * you do not want to enable value caching for them, which is controlled through
 * the above mentioned <tt>DataType</tt> method.
 * <p>
 * Besides just implementing <tt>DataObject</tt>, this <tt>EOGenericRecord</tt>
 * subclass takes care of some EOF specific problems:
 * <ul>
 * <li>It overrides {@link #validateToDelete()} to address a bug in the client
 * side <tt>EOClassDescription</tt> validation</tt>.</li>
 * <li>It overrides {@link #validateValueForKey(Object, String)} to force the
 * conversion of empty <tt>String</tt>s to <tt>null</tt>.</li>
 * <li>It provides keypath validation, through the
 * {@link #validate(Object, String)} method.</li>
 * </ul>
 * 
 * @version 1.3 Mar 26, 2009
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class EOFDataObject extends EOGenericRecord implements DataObject,
		Comparable<DataObject>, DataObject.Deletable{
	
	/**
	 * Stores all the classes for which the {@link #initDataType(EOFDataType)} method
	 * has been called, to ensure it does not get called twice.
	 */
	private static final Set<Class<?>> initializedDataTypes = new HashSet<Class<?>>();

	/**
	 * Calls the superclass constructor with the given entity name, used because
	 * of a bug in client side EOF libraries that can occur at certain times if
	 * EOs are not constructed this way.
	 * 
	 * @param classDescription The class description of this kind of EO.
	 */
	protected EOFDataObject(EOClassDescription classDescription){
		super(classDescription);
		
		DataType dataType = getDataType();
		
		/*
		 * Initialize derived properties for this class.
		 */
		Class<?> thisClass = getClass();
		if( ! initializedDataTypes.contains(thisClass)){
			initDataType(dataType);
			initializedDataTypes.add(thisClass);
		}
		
		/*
		 * Need to see if there are derived properties that require
		 * KeyPathChangeManagers (they are derived from keypaths), and if so, we
		 * need to make sure that those KPCMs will get initialized once the
		 * derived props start to be asked for.
		 */
		for(String derivedProp : dataType.derivedProperties()){
			
			// prepare the Set of keypaths variable
			Set<String> keypathsToManage = null;
			
			// get the keys from which a prop is derived from
			for(String derivedFrom : dataType.propertiesThatDerive(derivedProp)){				
				
				if(!KeyPathChangeManager.testPath(derivedFrom)) continue;
				
				// we have a keypath to monitor
				if(keypathsToManage == null) keypathsToManage = new HashSet<String>();
				keypathsToManage.add(derivedFrom);
			}
			
			if(keypathsToManage == null) continue;
			
			// if there are keypaths to manage, add it!
			if(toInitKPCMs == null) toInitKPCMs = new HashMap<String, Set<String>>();
			toInitKPCMs.put(derivedProp, keypathsToManage);
		}
	}

	/**
	 * Overrides the client side implementation because
	 * <tt>EODistributedClassDescription.validateObjectForDelete(EOEnterpriseObject)</tt>
	 * does not perform proper checking against deletion rules, and makes sure a
	 * JBND validation exception is thrown, and not NSValidation's.
	 * <p>
	 * This implementation first invokes the superclass' implementation (which
	 * might throw, or it might not), and thereafter checks manually against
	 * relationship deletion rules and this object's relationships, to verify
	 * that the object indeed is valid for deletion.
	 * <p>
	 * Note that this will result in the fetching of all objects that deletion
	 * rules need to check against, if they were not fetched already.
	 * 
	 * @throws NSValidation.ValidationException If the object is in an invalid
	 *             state for deletion, according to the model defined deletion
	 *             rules.
	 */
	public void validateForDelete() throws NSValidation.ValidationException{

		// superclass implementation
		super.validateForDelete();
		
		// get the relationships keys
		NSArray<String> toOneRelationshipKeys = toOneRelationshipKeys(), 
			toManyRelationshipKeys = toManyRelationshipKeys();

		// get the class description, needed for deletion rule checking
		EOClassDescription cd = classDescription();

		// iterate through the to one relationship keys
		for(int i = 0; i < toOneRelationshipKeys.count(); i++){
			String key = toOneRelationshipKeys.objectAtIndex(i);
			int deleteRule = cd.deleteRuleForRelationshipKey(key);

			// if it is a deny delete rule for a relationship where the
			// destination is
			// not null, the object is invalid for deletion
			if(deleteRule == EOClassDescription.DeleteRuleDeny
					&& valueForKey(key) != null){
				throw new NSValidation.ValidationException(
						"", valueForKey(key), key);

			// if it is a cascade delete rule, we need to make sure that if
			// there is a
			// related object, it also is valid for deletion
			}else if(deleteRule == EOClassDescription.DeleteRuleCascade){
				EOFDataObject relatedObject = (EOFDataObject)valueForKey(key);
				if(relatedObject != null){
					try{
						relatedObject.validateToDelete();
					}catch(NSValidation.ValidationException ex){
						String key2 = key + "." + ex.key();
						throw new NSValidation.ValidationException(
								"", get(key2), key2);
					}
				}
			}
		}// toOneRelationships loop

		// iterate through the to many relationship keys
		for(int i = 0; i < toManyRelationshipKeys.count(); i++){
			String key = toManyRelationshipKeys.objectAtIndex(i);
			int deleteRule = cd.deleteRuleForRelationshipKey(key);

			// if it is a deny delete rule for a relationship where the
			// destination is
			// not null, the object is invalid for deletion
			if(deleteRule == EOClassDescription.DeleteRuleDeny){
				Integer count = (Integer)valueForKeyPath(key + ".@count");
				if(count != null && count != 0)
					throw new NSValidation.ValidationException(
							"", get(key), key);

			// if it is a cascade delete rule, we need to make sure that if
			// there is a
			// related object, it also is valid for deletion
			}else if(deleteRule == EOClassDescription.DeleteRuleCascade){
				NSArray<?> relatedObjects = (NSArray<?>)valueForKey(key);
				try{
					for(int j = 0; j < relatedObjects.count(); j++)
						((EOFDataObject)relatedObjects.objectAtIndex(j))
								.validateToDelete();
				}catch(NSValidation.ValidationException ex){
					String key2 = key + "." + ex.key();
					throw new NSValidation.ValidationException(
							"", get(key2), key2);
				}
			}
		}// toManyRelationships loop

		// valid for deletion!
	}
	
	/**
	 * Overrides <tt>EOCustomObject</tt>'s implementation to trim
	 * <tt>String</tt>s using {@link JBNDUtil#checkForEmptyString(Object)}.
	 * 
	 * @param value Same as super implementation.
	 * @param key Same as super implementation.
	 * @return Same as super implementation.
	 * @throws NSValidation.ValidationException Same as super implementation.
	 */
	public Object validateValueForKey(Object value, String key)
			throws NSValidation.ValidationException{
		return super.validateValueForKey(JBNDUtil.checkForEmptyString(value), key);
	}

	/*
	 * DataObject implementation methods
	 */
	

	/**
	 * Implementation of <tt>DataObject.get(String)</tt> method, checks if the
	 * given <tt>key</tt> is a a keypath, if so it calls
	 * <tt>valueForKeyPath(String)</tt>, otherwise it calls
	 * <tt>valueForKey(String)</tt>.
	 * 
	 * @param key The key for which to return the value.
	 * @return The value for the given key.
	 * @throws UnknownKeyException If the given <tt>key</tt> is not used in this
	 *             type of <tt>DataObject</tt>s.
	 * @see DataObject#get(String)
	 */
	public Object get(String key){
		Object rVal = null;

		//	catch NSKVC.UnknownKeyException 
		//	and convert it to a JBND UnknownKeyException
		try{
			//	key path handling
			if(JBNDUtil.isKeyPath(key))
				rVal = valueForKeyPath(key);
			else
				rVal = valueForKey(key);
		
		}catch(NSKeyValueCoding.UnknownKeyException ex){
			throw new org.jbnd.UnknownKeyException(key, this);
		}

		return rVal;
	}

	// lazy init cache
	private DataType dataType = null;
	/**
	 * Returns the <tt>DataType</tt> that <tt>this DataObject</tt> belongs
	 * to. It will be an instance of <tt>EOFDataType</tt>, obtained from
	 * <tt>EOFDataTypeProvider</tt>.
	 * 
	 * @return The <tt>DataType</tt> of <tt>this DataObject</tt>.
	 * @see DataType
	 * @see EOFDataType
	 * @see EOFDataTypeProvider
	 */
	public DataType getDataType(){
		if(dataType == null)
			dataType = EOFDataType.getInstance(classDescription());
		return dataType;
	}

	/**
	 * Simply calls
	 * <tt>addObjectToBothSidesOfRelationshipWithKey(EORelationshipManipulation, String)</tt>.
	 * Casts the given <tt>object</tt> to <tt>EOFDataObject</tt> to be able
	 * to do so, so while the method is declared as accepting a
	 * <tt>DataObject</tt>, in reality it requires it to be an
	 * <tt>EOFDataObject</tt>.
	 * 
	 * @param object The <tt>DataObject</tt> to relate <tt>this</tt> to.
	 * @param key The key for which to relate the given <tt>object</tt>.
	 * @throws UnknownKeyException If the given <tt>key</tt> is not an
	 *             relationship key in this type of <tt>DataObject</tt>s.
	 */
	public void relate(DataObject object, String key){
		try{
			addObjectToBothSidesOfRelationshipWithKey((EOFDataObject)object,
					key);
		}catch(NSKeyValueCoding.UnknownKeyException ex){
			throw new org.jbnd.UnknownKeyException(key, this);
		}
	}

	/**
	 * Implementation of <tt>DataObject.set(Object, String)</tt> method,
	 * checks if the given <tt>key</tt> is a a keypath, if so it calls
	 * <tt>takeValueForKeyPath(String)</tt>, otherwise it calls
	 * <tt>takeValueForKey(String)</tt>. Should not be used with relationship
	 * properties, use <tt>relate(..)</tt> and <tt>unrelate(...)</tt>
	 * instead.
	 * <p>
	 * Normally the <tt>value</tt> should be validated before trying to call
	 * this method, to ensure it is acceptable to <tt>this</tt> object.
	 * Otherwise it is possible that at a later time (when trying to save data
	 * to the persistent store) an exception will be thrown.
	 * 
	 * @param value The value to set for the given key.
	 * @param key The key for which to return the value.
	 * @throws UnknownKeyException If the given <tt>key</tt> is not an
	 *             attribute key in this type of <tt>DataObject</tt>s.
	 * @throws RuntimeException This method throws some kind of a
	 *             <tt>RuntimeException</tt> if the given <tt>value</tt> is
	 *             not an instance of a class expected for the given
	 *             <tt>key</tt>.
	 * @see #relate(DataObject, String)
	 * @see #unrelate(DataObject, String)
	 * @see #validate(Object, String)
	 */
	public void set(Object value, String key){
		try{
			if(JBNDUtil.isKeyPath(key))
				takeValueForKeyPath(value, key);
			else
				takeValueForKey(value, key);
		}catch(NSKeyValueCoding.UnknownKeyException ex){
			throw new org.jbnd.UnknownKeyException(key, this);
		}
	}

	/**
	 * Simply calls
	 * <tt>removeObjectFromBothSidesOfRelationshipWithKey(EORelationshipManipulation, String)</tt>
	 * . Casts the given <tt>object</tt> to <tt>EOFDataObject</tt> to be able to
	 * do so, so while the method is declared as accepting a <tt>DataObject</tt>
	 * , in reality it requires it to be an <tt>EOFDataObject</tt>.
	 * 
	 * @param object The <tt>DataObject</tt> to unrelate <tt>this</tt> from.
	 * @param key The key for which to unrelate the given <tt>object</tt>.
	 * @throws UnknownKeyException If the given <tt>key</tt> is not an
	 *             relationship key in this type of <tt>DataObject</tt>s.
	 */
	public void unrelate(DataObject object, String key){
		try{
			removeObjectFromBothSidesOfRelationshipWithKey(
					(EOFDataObject)object, key);
		}catch(NSKeyValueCoding.UnknownKeyException ex){
			throw new org.jbnd.UnknownKeyException(key, this);
		}
	}

	/**
	 * Simply calls <tt>EOCustomObject</tt>s implementation of this method
	 * and converts the possibly cast <tt>NSValidation.ValidationException</tt>
	 * to an <tt>EOFValidationException</tt>.
	 * 
	 * @param value The value to validate.
	 * @param key The key to validate the value for.
	 * @return The valid value, potentially coerced. See above for more info.
	 * @throws EOFValidationException If the given <tt>value</tt> is not valid
	 *             to be set on <tt>this</tt> object for the given
	 *             <tt>key</tt>.
	 * @see org.jbnd.ValidationException
	 */
	public Object validate(Object value, String key)
			throws org.jbnd.ValidationException{
		if(JBNDUtil.isKeyPath(key))
			return JBNDUtil.validate(this, value, key);

		Object rVal = null;
		
		//	do value conversion of attributes
		try{
			value = ValueConverter.toClass(value, JBNDUtil.classForProperty(getDataType(), key));
		}catch(Exception ex){}	//	ignore it
		
		try{
			rVal = validateValueForKey(value, key);
		}catch(NSValidation.ValidationException ex){
			throw new org.jbnd.ValidationException(value, key, this);
		}

		return rVal;
	}

	/**
	 * Simply calls <tt>validateForSave()</tt> and converts the possibly cast
	 * <tt>NSValidation.ValidationException</tt> to an
	 * <tt>org.jbnd.ValidationException</tt>.
	 * 
	 * @throws org.jbnd.ValidationException If <tt>this</tt> EO is not currently
	 *             in a state at which it can be saved to the database. The
	 *             exception will provide descriptive information.
	 */
	public void validateToSave() throws org.jbnd.ValidationException{
		try{
			validateForSave();
		}catch(NSValidation.ValidationException ex){
			throw new org.jbnd.ValidationException(get(ex.key()), ex.key(),
					this);
		}
	}
	
	/**
	 * Simply calls <tt>validateForDelete()</tt> and converts the possibly cast
	 * <tt>NSValidation.ValidationException</tt> to an
	 * <tt>org.jbnd.ValidationException</tt>.
	 * 
	 * @throws org.jbnd.ValidationException If <tt>this</tt> EO is not currently
	 *             in a state at which it can be deleted from the database. The
	 *             exception will provide descriptive information.
	 */
	public void validateToDelete() throws org.jbnd.ValidationException{
		try{
			validateForDelete();
		}catch(NSValidation.ValidationException ex){
			throw new org.jbnd.ValidationException(get(ex.key()), ex.key(), this);
		}
	}

	/**
	 * Indicates if this <tt>EOFDataObject</tt> is saved to the persistent store
	 * or not. The EO however does not necessarily need to be the same as the
	 * stored one, it is possible it was modified.
	 * 
	 * @return If this <tt>EOFDataObject</tt> is saved in the persistent store
	 *         or not
	 */
	public boolean isPersistent(){
		EOEditingContext ec = editingContext();
		return ec != null && (!(ec.globalIDForObject(this).isTemporary()));
	}

	// a list of listeners interested in the changes happening in this object
	private final List<DataObjectListener> listeners = new LinkedList<DataObjectListener>();

	/**
	 * Adds the given listener to the list of listeners that should be notified
	 * when <tt>this DataObject</tt> changes. A change is considered the
	 * setting of a value for any of it's properties, that is different then the
	 * one present there before. It is possible that an implementing
	 * <tt>DataObject</tt> will have the capability of storing and modifying
	 * something else as data properties, but the <tt>DataObjectListener</tt>
	 * should never be used to fire events notifying of the changes of those.
	 * 
	 * @param listener The listener to add.
	 */
	public void addDataObjectListener(DataObjectListener listener){
		if(listener == null)
			throw new IllegalArgumentException("Can not add a null listener");
		listeners.add(listener);
	}

	/**
	 * Removes the given listener from the list of listeners that should be
	 * notified when <tt>this DataObject</tt> changes.
	 * 
	 * @param listener The listener to remove.
	 * @see #addDataObjectListener(DataObjectListener)
	 */
	public void removeDataObjectListener(DataObjectListener listener){
		listeners.remove(listener);
	}

	/**
	 * Depreciated, use
	 * {@link #fireDataObjectEvent(String, Object, DataObjectEvent.Type)}
	 * instead.
	 */
	@Deprecated
	protected void fireDataObjectEvent(String key, Object relevantValue, int changeType){
		fireDataObjectEvent(key, relevantValue, Type.values()[changeType]);
	}
	
	/**
	 * This method takes care of instantiating and firing a
	 * <tt>DataObjectEvent</tt> on all registered listeners, should be called
	 * every time the data this <tt>EOFDataObject</tt> contains changes. This
	 * method should be called AFTER the value has been set.
	 * 
	 * @param key The key for which the value changed.
	 * @param relevantValue The value relevant to the change, for more info see
	 *            the {@link DataObjectEvent} API documentation.
	 * @param changeType The type of change, one of the changes defined in
	 *            <tt>DataEvent</tt>.
	 */
	protected void fireDataObjectEvent(String key, Object relevantValue, Type changeType){
		
		if(listeners.size() == 0 && DataObjectEvent.POST_NOTES == false) return;

		//	create an event
		DataObjectEvent e = new DataObjectEvent(this, key, changeType, relevantValue);
		
		//	by now a Note has been posted, if that was necessary
		//	so only fire events if there are listeners
		if(listeners.size() == 0) return;
		
		// clear derived property cache if necessary
		if(changeType == DataObjectEvent.Type.DERIVED_PROPERTY_CHANGE
				&& derivedPropCache != null)
			derivedPropCache.remove(key);
		
		DataObjectListener[] listenersArray = listeners
				.toArray(new DataObjectListener[listeners.size()]);
		for(int i =  listenersArray.length - 1 ; i >= 0 ; i--)
			listenersArray[i].objectChanged(e);
		
		//	fire events for dependent properties
		//	if there are any
		Set<String> derivedPropKeys = getDataType().propsDerivedFrom(key);
		for(String derivedPropKey : derivedPropKeys)
			fireDataObjectEventWithoutNote(derivedPropKey, get(derivedPropKey),
					DataObjectEvent.Type.DERIVED_PROPERTY_CHANGE);
	}
	
	/**
	 * Does exactly the same as {@link #fireDataObjectEvent(String, Object)}, but
	 * it ensure that no <tt>Note</tt> is posted as a result of firing the event.
	 * 
	 * @param key The key for which the value changed.
	 * @param relevantValue The value relevant to the change, for more info see
	 *            the {@link DataObjectEvent} API documentation.
	 * @param changeType The type of change, one of the changes defined in
	 *            <tt>DataEvent</tt>.
	 * @see	DataObjectEvent#POST_NOTES
	 */
	private void fireDataObjectEventWithoutNote(String key, 
			Object relevantValue, Type changeType){
		boolean oldNoteValue = DataObjectEvent.POST_NOTES;
		DataObjectEvent.POST_NOTES = false;
		fireDataObjectEvent(key, relevantValue, changeType);
		DataObjectEvent.POST_NOTES = oldNoteValue;
	}

	/**
	 * Overrides the <tt>EOCustomObject</tt> implementation to handle the
	 * special "!this" key (used to obtain the EO itself), for all other keys it
	 * calls the superclass implementation.
	 * 
	 * @param key The unbound key.
	 * @return If the given <tt>key</tt> is equal to <tt>THIS_KEY</tt>
	 * 			constant, <tt>this</tt> EO is returned, otherwise the
	 * 			superclass implementation is called.
	 */
	@Override
	public Object handleQueryWithUnboundKey(String key){
		if(DataObject.THIS_KEY.equals(key)) return this;
		return super.handleQueryWithUnboundKey(key);
	}
	
	/**
	 * Calls <tt>AbstractDataObject.compareTo(this, object)</tt>.
	 * 
	 * @param object	The object to compare to.
	 * @return Standard <tt>Comparable</tt> return value.
	 * @see	AbstractDataObject#compareTo(DataObject, DataObject)
	 */
	public int compareTo(DataObject object){
		return AbstractDataObject.compareTo(this, object);
	}
	
	/*
	 * A cache of values for derived properties, used to prevent having to
	 * re-evaluate a derived prop value every time it is asked for. The cache
	 * for individual properties is flushed whenever a property it is dependent
	 * on changes, and subsequently re-evaluated, thus ensuring that the cache
	 * is always up to date.
	 */
	private Map<String, Object> derivedPropCache;

	/**
	 * This method is called once per <tt>DataType</tt>, during the
	 * initialization of the first <tt>DataObject</tt> of that type, to allow
	 * subclasses of <tt>EOFDataType</tt> to make static-like initializations of
	 * their <tt>DataType</tt>. Fully static initializations of the type do not
	 * work in a WOJC scenario. The <tt>EOFDataObject</tt> implementation does
	 * nothing.
	 * <p>
	 * This method should can be used to, for example:
	 * <ul>
	 * <li>Initialize derived properties in the <tt>DataType</tt>.</li>
	 * <li>Initialize value sets.</li>
	 * </ul>
	 * 
	 * @param dataType The <tt>DataType</tt> of this <tt>EOFDataObject</tt>.
	 */
	protected void initDataType(DataType dataType){};
	
	/*
	 * Stores key pairs that indicate for which properties KeyPathChangeManager
	 * objects should be created. This is a temporary cache that exists between
	 * the call made to addDerivedProperty(...) and valueForKey(...) for a
	 * derived property key that is contained in this map.
	 * 
	 * The structure is:
	 * for each derived property key there is a Set of Strings which defines
	 * the key paths for which KPCMs need to be created, to observe changes that
	 * affect the derived property. The KPCMs will detect these changes and
	 * notify the monitor of the KPCM (this EO), which will in turn nullify the
	 * derived property cache, causing this EO to re-evaluate it, when it is
	 * next time asked for.
	 */
	private Map<String, Set<String>> toInitKPCMs;
	
	/**
	 * A <tt>KeyPathChangeManager</tt> made to listen to changes of keypaths
	 * that define a derived property, and to inform the <tt>DataObject</tt>
	 * that contains the derived prop of a change that affects that prop.
	 * 
	 * @version 1.0 Mar 26, 2009
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	private class DerivedPropKeyPathChangeManager extends KeyPathChangeManager{
		
		private final String derivedPropKey;
		
		private DerivedPropKeyPathChangeManager(String keypath, String derivedPropKey){
			super(keypath, null);
			this.derivedPropKey = derivedPropKey;
			add(EOFDataObject.this);
		}
		
		/**
		 * Overridden to fire an event.
		 */
		public void pathChanged(DataObject object, String path){
			fireDataObjectEventWithoutNote(derivedPropKey, get(derivedPropKey),
					DataObjectEvent.Type.DERIVED_PROPERTY_CHANGE);
		}
	}

	/**
	 * Overridden to provide derived property caching.
	 * 
	 * @param key
	 * @return Same old.
	 */
	@Override
	public Object valueForKey(String key){
		
		DataType dt = getDataType();
		
		//	check if the asked for property is a derived one
		if(dt.propertyType(key).isDerived()){
			
			// if caching should not be performed, return using standard KVC
			if( ! dt.shouldCacheDerivedPropValues(key))
				return super.valueForKey(key);
			
			//	if the value is cached already, return it
			if(derivedPropCache != null && derivedPropCache.containsKey(key))
				return derivedPropCache.get(key);
			
			//	the value is not cached, so cache it
			Object value = super.valueForKey(key);
			
			// lazy init of the derived property cache
			if(derivedPropCache == null) derivedPropCache = new HashMap<String, Object>();
			derivedPropCache.put(key, value);
			
			// late init of the KeyPathChangeManager
			if(toInitKPCMs != null && toInitKPCMs.containsKey(key)){
				
				// get a list of paths the given prop is derived from
				Set<String> derivedFromPaths = toInitKPCMs.get(key);
				
				// for each path in that list create a KPCM
				for(String path : derivedFromPaths)
					new DerivedPropKeyPathChangeManager(path, key);
				
				// make sure the KPCM is only created once per EO
				toInitKPCMs.remove(key);
				if(toInitKPCMs.isEmpty()) toInitKPCMs = null;
			}
			
			return value;
		}
		
		//	the asked for value is not a derived property
		return super.valueForKey(key);
	}
	
	/**
	 * An extension of the <tt>toString()</tt> concept, this method is meant
	 * to be used in places where it is unacceptable to have no textual
	 * description of the <tt>DataObject</tt>. It is quite possible for
	 * example for the <tt>toString()</tt> method to return <tt>null</tt>,
	 * but this method must never do that. It should provide at least some
	 * description (user presentable) of the <tt>DataObject</tt>, even if
	 * that is something as obscure as 'New Person record'.
	 * <p>
	 * This method returns the same value as <tt>toString()</tt>, and
	 * overrides it's functionality only when the <tt>toString()</tt> returned
	 * value is <tt>null</tt> or empty.
	 * 
	 * @return See above.
	 */
	public String toLogString(){
		String ts = toString();
		if(ts == null || ts.length() == 0)
			return NamingSupport.typeName(entityName()) + " data object";
		else
			return ts;
	}
	
	/**
	 * <b>Does nothing</b>, the property derivation system has been improved in
	 * JBND 0.92 and later, use
	 * {@link DataType#addDerivedProp(String, PropType, Class, String...)}
	 * method and it's companions to achieve better derived property management.
	 * 
	 * @param derivedProp Ignored.
	 * @param dependencies Ignored.
	 */
	@Deprecated
	public void addDerivedProperty(String derivedProp, String... dependencies){}
	
	
	
	
	/*
	 * VALUE SET STUFF
	 */
	
	/**
	 * Overridden to automate the adding of set values to the value sets (if
	 * declared for a fetched property) in this EO's <tt>DataType</tt>.
	 * 
	 * @see DataType#valueSetFor(String)
	 * @see DataType#valueSetKeys()
	 */
	@Override
	public void takeStoredValueForKey(Object value, String key){
		
		super.takeStoredValueForKey(value, key);
		
		// add the value to the value if one is tracked for key
		DataType dt = getDataType();
		if(JBNDUtil.indexOf(dt.valueSetKeys(), key, false) != -1)
			dt.addToValueSet(value, key);
	}
}