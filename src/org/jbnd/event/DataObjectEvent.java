package org.jbnd.event;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.jbnd.DataObject;
import org.jbnd.notes.NoteBoard;
import org.jbnd.undo.DataObjectUndoable;


/**
 * An event identifying a change in a <tt>DataObject</tt>, automatically posts a
 * {@link NoteBoard} whenever instantiated, this can be changed by setting the
 * {@link #POST_NOTES} flag to <tt>false</tt>.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, Sep 15, 2007
 * @see DataObject
 * @see DataObjectListener
 * @see NoteBoard
 * @see #POST_NOTES
 */
public final class DataObjectEvent extends EventObject{
	
	/**
	 * Determines if <tt>Note</tt>s should be posted whenever a
	 * <tt>DataObjectEvent</tt> is fired; default value is <tt>true</tt>.
	 */
	public static boolean POST_NOTES = true;
	
	/**
	 * Flag indicating what kind of a change happened in the <tt>DataObject</tt>.
	 */
	@Deprecated
	public static final int
		ATTRIBUTE_CHANGE		= 0,
		TO_ONE_CHANGE			= 1,
		TO_MANY_CHANGE			= 2,
		TO_MANY_ADD				= 3,
		TO_MANY_REMOVE			= 4,
		DERIVED_PROPERTY_CHANGE = 5,
		CACHED_PROPERTY_CHANGE	= 6,
		QUALIFIER_VALUE_CHANGE	= 7;
	
	
	/**
	 * An enumeration of different types of change that happened in the <tt>DataObject</tt>.
	 * 
	 * @version 1.0 Feb 14, 2009
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public enum Type{
		ATTRIBUTE_CHANGE,
		TO_ONE_CHANGE,
		TO_MANY_CHANGE,
		TO_MANY_ADD,
		TO_MANY_REMOVE,
		DERIVED_PROPERTY_CHANGE,
		CACHED_PROPERTY_CHANGE,
		QUALIFIER_VALUE_CHANGE;
	}
	
	private final String key;
	private final Type type;
	private final Object relevantValue;

	/**
	 * @param object The <tt>DataObject</tt> that has changed (required).
	 * @param key The key on which the DO has changed (required).
	 * @param type The type of change. One of the statically defined types in
	 *            <tt>DataObject</tt> (required).
	 * @param relevantValue The value relevant to the change, for more info see
	 *            the {@link #getRelevantValue()} API documentation.
	 */
	@Deprecated
	public DataObjectEvent(DataObject object, String key, int type, Object relevantValue){
		this(object, key, Type.values()[type], relevantValue);
	}
	
	/**
	 * @param object The <tt>DataObject</tt> that has changed (required).
	 * @param key The key on which the DO has changed (required).
	 * @param type The type of change. One of the statically defined types in
	 *            <tt>DataObject</tt> (required).
	 * @param relevantValue The value relevant to the change, for more info see
	 *            the {@link #getRelevantValue()} API documentation.
	 */
	public DataObjectEvent(DataObject object, String key, Type type, Object relevantValue){
		super(object);
		
		if(object == null) throw new IllegalArgumentException(
				"The object parameter is required to create an DataObjectEvent");
		if(key == null) throw new IllegalArgumentException(
				"The key parameter is required to create an DataObjectEvent");
		
		this.key = key;
		this.type = type;
		this.relevantValue = relevantValue;
		
		//	fire note
		if(DataObjectEvent.POST_NOTES){
			Map<String, Object> info = new HashMap<String, Object>();
			info.put(DataObject.NOTE_INFO_KEY, key);
			info.put(DataObject.NOTE_INFO_TYPE, type);
			info.put(DataObject.NOTE_INFO_RELEVANT_VALUE, relevantValue);
			NoteBoard.postNote(DataObject.DATA_OBJECT_EVENT_NOTE, object, info);
		}
		
		DataObjectUndoable.registerUndoable(this);
	}

	/**
	 * The getter for the data key.
	 * 
	 * @return The data key.
	 */
	public String getKey(){
		return key;
	}

	/**
	 * The getter for the object change type. One of the statically defined type
	 * in <tt>DataObject</tt>.
	 * 
	 * @return object change type.
	 */
	public Type getType(){
		return type;
	}

	/**
	 * The value relevant to the change, in different types of change this value
	 * can be different things:
	 * <ul>
	 * <li>In attribute and to-one changes, the relevant value is the old value</li>
	 * <li>In additions and removals from to-many relationships, the relevant
	 * value is the added / removed <tt>DataObject</tt></li>
	 * <li>In the complete replacement of to-many relationships, the relevant
	 * value is the <tt>List</tt> representing the old state of the relationship
	 * </li>
	 * <li>In cached property, derived property and qualifier changes, the
	 * relevant value is the old value</li>
	 * </ul>
	 * 
	 * @return The old value the changed object had for the given <tt>key</tt>.
	 *         Possibly <tt>null</tt>, depending on the type of change.
	 */
	public Object getRelevantValue(){
		return relevantValue;
	}
	
	/**
	 * Returns the new value by calling <tt>getDataObject().get(key)</tt>.
	 */
	public Object getNewValue(){
		Object rVal = getDataObject().get(key);
		return rVal;
	}

	/**
	 * Returns the changed object.
	 * 
	 * @return	The same object as <tt>getSource()</tt>, cast to <tt>DataObject</tt>.
	 */
	public DataObject getDataObject(){
		return (DataObject)getSource();
	}
}