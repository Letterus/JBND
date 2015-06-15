package org.jbnd.event;

import java.util.EventObject;

import org.jbnd.undo.UndoManager;
import org.jbnd.undo.Undoable;


/**
 * Fired by an <tt>Undoable</tt> when it performs an undo / redo, or by an
 * <tt>UndoManager</tt> when it changes in any way.
 * 
 * @version 1.0 Oct 22, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see UndoManager
 */
public final class UndoEvent extends EventObject{

	/**
	 * Indicates if an <tt>Undoable</tt> performed an undo or a redo, or the
	 * type of change that occurred in the <tt>UndoManager</tt>.
	 * 
	 * @version 1.0 Oct 22, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public enum Type{
		ADDED,
		REMOVED,
		UNDID,
		REDID,
		DISPOSED,
		CHANGED,
		CLEARED,
		LIMIT_REDUCED;
	}

	private final Type type;
	private final Undoable undoable;
	
	/**
	 * A constructor to be used by <tt>Undoable</tt>s to create an event. The
	 * resulting event will have the <tt>Undoable</tt> as it's source and as the
	 * object returned from the <tt>getUndoable</tt> method.
	 * 
	 * @param type The type of the event, should only ever be <tt>UNDID</tt>,
	 *            <tt>REDID</tt>, <tt>DISPOSED</tt> or <tt>CHANGED</tt>.
	 * @param source The <tt>Undoable</tt> that has performed an undo / redo.
	 */
	public UndoEvent(Type type, Undoable source){
		super(source);
		this.type=type;
		this.undoable=source;
	}

	/**
	 * A constructor that is used by <tt>UndoManager</tt>s to create an event.
	 * The resulting event will have the manager as it's source, and will return
	 * the (optional) <tt>undoable</tt> from it's <tt>getUndoable</tt> method.
	 * 
	 * @param type The type of the event, can be whichever type.
	 * @param source The manager that fired the event.
	 * @param undoable Optional parameter used with some types of an undo event.
	 */
	public UndoEvent(Type type, UndoManager source, Undoable undoable){
		super(source);
		this.type = type;
		this.undoable = undoable;
	}
	
	/**
	 * Returns the <tt>Type</tt> of this <tt>UndoEvent</tt>.
	 * 
	 * @return See above.
	 */
	public Type getType(){ return type; }
	
	/**
	 * Returns the <tt>Undoable</tt> that performed an undo / redo, or the
	 * <tt>Undoable</tt> caused the change in the <tt>UndoManager</tt>. When
	 * this event was fired by a manager, the value returned to the type of
	 * change:
	 * <p>
	 * If type is <tt>ADDED</tt>, returns the item that was added.
	 * <p>
	 * If type is <tt>REMOVED</tt>, returns the item that was removed (typically
	 * as a consequence of the <tt>Undoable</tt> being disposed by something
	 * else then the manager itself).
	 * <p>
	 * If type is <tt>CHANGED</tt>, returns the <tt>Undoable</tt> that originated
	 * the change (fired a <tt>CHANGED</tt> type event itself).
	 * <p>
	 * If type is <tt>UNDID</tt>, returns the significant <tt>Undoable</tt> that
	 * was undone.
	 * <p>
	 * If type is <tt>REDID</tt>, returns the significant <tt>Undoable</tt> that
	 * was redone.
	 * <p>
	 * If type is <tt>CLEARED</tt>, returns <tt>null</tt>.
	 * <p>
	 * If type is <tt>LIMIT_REDUCED</tt>, returns <tt>null</tt>.
	 * 
	 * @return See above.
	 */
	public Undoable getUndoable(){ return undoable; }
}