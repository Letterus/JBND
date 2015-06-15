package org.jbnd.event;

import java.util.EventObject;

import org.jbnd.DataObject;


/**
 * Defines an event fired by an <tt>DataSource</tt> implementations. The event
 * is list based, it provides info when a certain range of data has been
 * changed, added or removed from the <tt>DataSource</tt>.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, Sep 17, 2007
 * @see DataSourceListener
 */
public final class DataSourceEvent extends EventObject{
	
	/**
	 * Defines different types of changes that a <tt>DataSourceEvent</tt>
	 * represents.
	 * 
	 * @version 1.0 Feb 14, 2009
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public static enum Type{
		DATA_CHANGED, DATA_ADDED, DATA_REMOVED;
	}
	
	/** Indicates that data has changed in the <tt>DataSource</tt>. */
	@Deprecated
	public static final int DATA_CHANGED = 0;
	
	/** Indicates that data has been added to the <tt>DataSource</tt>. */
	@Deprecated
	public static final int DATA_ADDED = 1;
	
	/** Indicates that data has been removed from the <tt>DataSource</tt>. */
	@Deprecated
	public static final int DATA_REMOVED = 2;
	
	// the type of the change event
	private final Type type;
	
	// the start and end index
	private final int index0, index1;
	
	//	the data that was added / removed / changed
	private final DataObject[] data;
	
	/**
	 * Depreciated in favor of
	 * {@link #DataSourceEvent(Object, Type, int, int, DataObject[])}.
	 */
	@Deprecated
	public DataSourceEvent(Object source, int type, int index0, int index1,
			DataObject[] data){
		this(source, Type.values()[type], index0, index1, data);
	}

	/**
	 * Creates a new event object.
	 * 
	 * @param source The source of the event (typically the <tt>DataSource</tt>
	 *            ).
	 * @param type The type of the event (
	 *            <tt>DATA_CHANGED, DATA_ADDED, DATA_REMOVED</tt>).
	 * @param index0 The lower index of the range (inclusive).
	 * @param index1 The higher index of the range (inclusive).
	 * @param data The data that has been added / removed / changed, OPTIONAL.
	 */
	public DataSourceEvent(Object source, Type type, int index0, int index1,
			DataObject[] data){
		super(source);
		this.type = type;
		this.index0 = index0;
		this.index1 = index1;
		this.data = data;
	}


	/**
	 * Returns the lower index of the range (inclusive).
	 * 
	 * @return The lower index of the range (inclusive).
	 */
	public int getIndex0(){
		return index0;
	}

	/**
	 * Returns the upper index of the range (inclusive).
	 * 
	 * @return The upper index of the range (inclusive).
	 */
	public int getIndex1(){
		return index1;
	}

	/**
	 * Returns the constant identifying the Type of the event.
	 * 
	 * @return The event type.
	 * @see Type
	 */
	public Type getType(){
		return type;
	}
	
	/**
	 * Returns the data that has been changed / removed / added.
	 * 
	 * @return	See above.
	 */
	public DataObject[] getData(){
		return data;
	}
}
