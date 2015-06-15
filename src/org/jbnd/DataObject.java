package org.jbnd;

import org.jbnd.event.DataObjectEvent;
import org.jbnd.event.DataObjectListener;
import org.jbnd.notes.NoteBoard;


/**
 * Defines the interface for a data carrying object in the JBND library. A
 * <tt>DataObject</tt> provides values through key-value pairs (similar to
 * <tt>java.util.Map</tt>), and defines standard operations on them. They can
 * store data attributes (text, numbers, dates...) as well as relate to each
 * other (to-one and to-many relationships). In that sense they can be used to
 * represent relational database rows, any kind of tabular data, XML defined
 * objects etc... JBND uses the convention of calling raw data (text, numbers,
 * dates...) 'attributes', and related <tt>DataObject</tt>s 'relationships'. All
 * together the attributes and relationships are referred to as 'properties' of
 * a <tt>DataObject</tt>.
 * <p>
 * <b>Keypath value access:</b> Some data persistence systems provide keypath
 * access to values. For example, next to being able to retrieve the name of a
 * dog by calling <tt>aDog.get("name");</tt>, the programmer is also able to
 * retrieve the name of some person's dog by calling
 * <tt>aPerson.get("dog.name")</tt>. JBND internally fully supports keypath data
 * handling (and is in many ways specifically tailored to use keypaths). It
 * however does not differentiate keys from keypaths on the level of this
 * interface: use the <tt>set(Object, String)</tt> both with keys and keypaths.
 * JBND will in it's internal processing automatically detect when keypaths are
 * being used, and act accordingly. Naturally, there is no requirement by JBND
 * for a persistence system to support keypaths, but it does provide help for a
 * persistence system to implement it. See the following methods in the
 * {@link org.jbnd.support.JBNDUtil} class:
 * <ul>
 * <li>get(DataObject, String)</li>
 * <li>set(Object, String, DataObject)</li>
 * <li>validate(DataObject, Object, String)</li>
 * </ul>
 * <p>
 * <b>Extended keys:</b> It might be desired to use keys which are not
 * <tt>DataObject</tt> properties in the persistent store. One such example
 * might be a special key to return the <tt>DataObject</tt> itself (the
 * <tt>this</tt> object). This needs to be handled in the <tt>DataObject</tt>
 * implementing class itself, however, JBND suggests that as a convention you
 * use the "!key" standard for such keys. For example, "!this" could be used as
 * a key to return the <tt>DataObject</tt> itself. To see why this convention is
 * recommended, see <tt>org.jbnd.support.JBNDUtil#cleanPath(String)</tt>. This
 * convention is meant to work next to whichever non-property keys the
 * persistence system you are using already supports, and not instead of it.
 * <p>
 * <b>Data types:</b> Normally there will be many different <tt>DataObject</tt>s
 * within any system that have the same set of properties, and are of the same
 * Java class. To provide means of describing those objects on a structural
 * level, the <tt>DataType</tt> interface is defined. The <tt>DataType</tt> an
 * object is an instance of can be obtained through the <tt>getDataType()</tt>
 * method, when access to the structural information it provides is necessary.
 * <p>
 * <b>Data manipulation:</b> Attributes are retrieved and stored for different
 * keys through <tt>get(String)</tt> and <tt>set(Object, String)</tt>.
 * Relationships are retrieved normally with the <tt>get(String)</tt> method,
 * and manipulated with <tt>relate(DataObject, String)</tt> and
 * <tt>unrelate(DataObject, String)</tt> methods.
 * <p>
 * <b>Data change notification:</b> <tt>DataObject</tt>s are required to fire
 * events (<tt>DataObjectEvent</tt> instances) when any of their properties
 * changes. The JBND library is depending on appropriate event firing to
 * function correctly. The <tt>DataObject</tt> event class will also by default
 * post <tt>Note</tt>s (see {@link NoteBoard} and related classes) when
 * instantiated, this can be disabled through the
 * {@link DataObjectEvent#POST_NOTES} flag.
 * <p>
 * <b>More on notes:</b> The <tt>Note</tt>s posted by this class are of the
 * <tt>DATA_OBJECT_EVENT_NOTE</tt> type, and contain additional information it
 * their <tt>info Map</tt>:
 * <ul>
 * <li>{@link #NOTE_INFO_KEY}</li>
 * <li>{@value #NOTE_INFO_TYPE}</li>
 * <li>{@value #NOTE_INFO_RELEVANT_VALUE}</li>
 * </ul>
 * <p>
 * <b>Validation:</b> A simple validation interface is a part of
 * <tt>DataObject</tt>, and used throughout the library.
 * <p>
 * <b>Interface concept:</b> This interface is not meant to be a complete
 * template for functional implementation, but a subset of methods that are
 * required by the JBND library's internal functioning. Most likely an
 * implementation class will contain many more functionalities.
 * 
 * @version 1.0 Dec 10, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see DataType
 * @see DataObjectEvent
 */
public interface DataObject extends Comparable<DataObject>{
	
	/**
	 * The extension of standard EOF key-value coding, this constant is the name
	 * of the key used to obtain <tt>this</tt> EO from key-value access
	 * methods.
	 */
	public static final String	THIS_KEY	= "!this";

	/**
	 * The note type of <tt>Note</tt>s that are automatically
	 * fired when instantiating <tt>DataObjectEvent</tt>s.
	 */
	public static final String DATA_OBJECT_EVENT_NOTE = 
		"DataObjectEventNoteType";

	/**
	 * The key used in the additional info of the <tt>Note</tt> posted
	 * about <tt>DataObject</tt> changes, the value in the additional
	 * info map for this key is the <tt>DataObject</tt> key for which
	 * the change occured.
	 */
	public static final String NOTE_INFO_KEY = "key";

	/**
	 * The key used in the additional info of the <tt>Note</tt> posted
	 * about <tt>DataObject</tt> changes, the value in the additional
	 * info map for this key is the type of change that occured in the
	 * <tt>DataObject</tt>.
	 */
	public static final String NOTE_INFO_TYPE = "type";

	/**
	 * The key used in the additional info of the <tt>Note</tt> posted about
	 * <tt>DataObject</tt> changes, the relevant value for this change. Same
	 * like the relevant value in events that are fired when <tt>DataObject</tt>
	 * s change.
	 * 
	 * @see DataObjectEvent#getRelevantValue()
	 */
	public static final String NOTE_INFO_RELEVANT_VALUE = "relevantValue";
	
	/**
	 * Returns the value found for the given <tt>key</tt>. That can be any
	 * property (an attribute, a to-one or a to-many relationship).
	 * <p>
	 * If it is an attribute, then the returned value is an instance of a class
	 * that can be obtained by <tt>getDataType().classForAttribute(key)</tt>.
	 * If it is a to-one relationship, then it is an instance of a class
	 * implementing <tt>DataObject</tt>. If it is a to-many relationship,
	 * then it is an instance of <tt>List</tt>, containing
	 * <tt>DataObject</tt>s.
	 * <p>
	 * If needed, the kind of the property can be obtained for a key beforehand,
	 * by using <tt>getDataType().propertyType(key)</tt>.
	 * 
	 * @param key The key for which to return the value.
	 * @return The value for the given key.
	 * @throws UnknownKeyException If the given <tt>key</tt> is not used in
	 *             this type of <tt>DataObject</tt>s.
	 */
	public Object get(String key);
	
	/**
	 * Sets the value for the given attribute <tt>key</tt>. This method should
	 * not be used with relationships, <tt>relate(...)</tt> and
	 * <tt>unrelate(...)</tt> should be used instead.
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
	public void set(Object value, String key);
	
	/**
	 * Relates the given <tt>DataObject</tt> to the relationship for the given
	 * <tt>key</tt>; used for both to-one and to-many relationships. This
	 * method should automatically detect if the method for the given
	 * <tt>key</tt> is a to-one or to-many (which can be done with
	 * <tt>getDataType().propertyType(key)</tt>), and relate the given object
	 * accordingly. It is also expected that a single call to this method will
	 * update the reverse relationship, if present.
	 * 
	 * @param object The <tt>DataObject</tt> to relate <tt>this</tt> to.
	 * @param key The key for which to relate the given <tt>object</tt>.
	 * @throws UnknownKeyException If the given <tt>key</tt> is not an
	 *             relationship key in this type of <tt>DataObject</tt>s.
	 */
	public void relate(DataObject object, String key);
	
	/**
	 * Breaks the relationship with the given <tt>DataObject</tt> and
	 * <tt>this</tt> for the given <tt>key</tt>; used for both to-one and
	 * to-many relationships. This method should automatically detect if the
	 * method for the given <tt>key</tt> is a to-one or to-many (which can be
	 * done with <tt>getDataType().propertyType(key)</tt>), and unrelate the
	 * given object accordingly. It is also expected that a single call to this
	 * method will update the reverse relationship, if present.
	 * 
	 * @param object The <tt>DataObject</tt> to unrelate <tt>this</tt> from.
	 * @param key The key for which to unrelate the given <tt>object</tt>.
	 * @throws UnknownKeyException If the given <tt>key</tt> is not an
	 *             relationship key in this type of <tt>DataObject</tt>s.
	 */
	public void unrelate(DataObject object, String key);
	
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
	public void addDataObjectListener(DataObjectListener listener);
	
	/**
	 * Removes the given listener from the list of listeners that should be
	 * notified when <tt>this DataObject</tt> changes.
	 * 
	 * @param listener The listener to remove.
	 * @see #addDataObjectListener(DataObjectListener)
	 */
	public void removeDataObjectListener(DataObjectListener listener);
	
	/**
	 * Validates <tt>this</tt> object for being in a state in which it can be
	 * saved to the persistent data store. It is possible that
	 * <tt>DataObject</tt> implementations are not capable of determining this
	 * (for example in the case of temporary data storage <tt>DataObject</tt>
	 * implementations), in such a case this method should thrown an
	 * <tt>UnsupportedOperationExeption</tt> to indicate inappropriate usage.
	 * 
	 * @throws ValidationException If <tt>this</tt> object is not currently in
	 *             a state at which it can be saved to the persistent data
	 *             store. The exception will provide descriptive information.
	 * @throws UnsupportedOperationException See above.
	 * @see ValidationException
	 */
	public void validateToSave() throws ValidationException;
	
	/**
	 * Validates the given <tt>value</tt> to be acceptable to <tt>this</tt>
	 * object for the given <tt>key</tt>. It is possible that the validation
	 * process will perform some coercing on the <tt>value</tt> (for example
	 * trimming half-empty <tt>String</tt>s, or converting the value to
	 * another class), so it is the returned value that should be set on
	 * <tt>this</tt>, always. If no coercing is necessary, the returned value
	 * will be the same object passed as the <tt>value</tt> parameter.
	 * <p>
	 * If the value is invalid, for whichever reason, a
	 * <tt>ValidationException</tt> is thrown.
	 * 
	 * @param value The value to validate.
	 * @param key The key to validate the value for.
	 * @return The valid value, potentially coerced. See above for more info.
	 * @throws ValidationException If the given <tt>value</tt> is not valid to
	 *             be set on <tt>this</tt> object for the given <tt>key</tt>.
	 * @see ValidationException
	 */
	public Object validate(Object value, String key) throws ValidationException;
	
	/**
	 * An extension of the <tt>toString()</tt> concept, this method is meant
	 * to be used in places where it is unacceptable to have no textual
	 * description of the <tt>DataObject</tt>. It is quite possible for
	 * example for the <tt>toString()</tt> method to return <tt>null</tt>,
	 * but this method must never do that. It should provide at least some
	 * description (user presentable) of the <tt>DataObject</tt>, even if
	 * that is something as obscure as 'New Person record'.
	 * <p>
	 * It is possible (and recommended) that this method should return the same
	 * value as <tt>toString()</tt>, and override it's functionality only
	 * when the <tt>toString()</tt> returned value is <tt>null</tt> or
	 * empty.
	 * 
	 * @return See above.
	 */
	public String toLogString();
	
	/**
	 * Returns the <tt>DataType</tt> that <tt>this DataObject</tt> belongs
	 * to.
	 * 
	 * @return The <tt>DataType</tt> of <tt>this DataObject</tt>.
	 * @see DataType
	 */
	public DataType getDataType();
	
	/**
	 * Indicates if this <tt>DataObject</tt> is saved in the persistent store
	 * or not. The object however does not necessarily need to be the same as
	 * the stored one, it is possible it was modified.
	 * 
	 * @return If this <tt>DataObject</tt> is saved in the persistent store or
	 *         not.
	 */
	public boolean isPersistent();
	
	/**
	 * Defines an object (presumably a <tt>DataObject</tt>) that can be
	 * deleted, and it's functionalities.
	 * 
	 * @version 1.0 Apr 15, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public static interface Deletable extends DataObject{
		
		/**
		 * Checks if the implementing object (presumably a <tt>DataObject</tt>)
		 * is in a valid state for deletion. An object can be in an invalid
		 * state if it is for example has a relationship that denies the
		 * possibility to delete it.
		 * 
		 * @throws ValidationException If the object is not in a state valid for
		 *             deletion.
		 */
		public void validateToDelete() throws ValidationException;
	}
}