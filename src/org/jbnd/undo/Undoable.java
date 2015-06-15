package org.jbnd.undo;

import org.jbnd.event.UndoListener;


/**
 * Represents an action that can be undone / redone. To be used in combination
 * with the <tt>UndoManager</tt>.
 * 
 * @version 1.0 Oct 22, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see UndoManager
 */
public interface Undoable{

	/**
	 * Indicates if this <tt>Undoable</tt> is in the state in which it can be
	 * undone.
	 * 
	 * @return See above.
	 * @see #undo()
	 */
	public boolean canUndo();
	
	/**
	 * Indicates if this <tt>Undoable</tt> is in the state in which it can be
	 * redone.
	 * 
	 * @return See above.
	 * @see #redo()
	 */
	public boolean canRedo();
	
	/**
	 * Makes this <tt>Undoable</tt> perform the undo. After a call to this
	 * method the <tt>Undoable</tt> will most likely return <tt>false</tt> from
	 * it's <tt>canUndo()</tt> method, and <tt>true</tt> from it's
	 * <tt>canRedo()</tt> method. This however is not insisted upon.
	 */
	public void undo();
	
	/**
	 * Makes this <tt>Undoable</tt> perform the redo, after it has performed an
	 * undo. After a call to this method the <tt>Undoable</tt> will most likely
	 * return <tt>true</tt> from it's <tt>canUndo()</tt> method, and
	 * <tt>false</tt> from it's <tt>canRedo()</tt> method. This however is not
	 * insisted upon.
	 */
	public void redo();
	
	/**
	 * Tells the <tt>Undoable</tt> that it is no longer applicable, and that it
	 * will not be asked to undo nor redo again. Can be used to release
	 * whichever resources the <tt>Undoable</tt> might be holding.
	 * <p>
	 * When an <tt>Undoable</tt> is disposed it must fire an <tt>UndoEvent</tt>
	 * of the <tt>DISPOSED</tt> type, which will cause it to be removed from the
	 * queue of it's <tt>UndoManager</tt>. Also, <tt>dispose()</tt> will be
	 * called on the <tt>Undoable</tt> if it is removed from the manager's queue
	 * for whichever other reason.
	 * <p>
	 * After a call to <tt>dispose()</tt> the <tt>Undoable</tt> should return
	 * <tt>false</tt> from it's <tt>canUndo()</tt> and <tt>canRedo()</tt>
	 * methods, and trying to undo or redo should result in an exception.
	 */
	public void dispose();
	
	/**
	 * Called on the last <tt>Undoable</tt> that is contained in the
	 * <tt>UndoManager</tt>'s queue when another <tt>Undoable</tt> is being
	 * added to it. Allows this <tt>Undoable</tt> to combine the newly added one
	 * into a single <tt>Undoable</tt>.
	 * <p>
	 * The returned <tt>Undoable</tt> should be the combination of the two, or
	 * <tt>null</tt> if it is not possible to combine them. It is allowed that
	 * the returned reference is <tt>this</tt>, the passed <tt>u</tt>, or a
	 * newly created <tt>Undoable</tt>.
	 * 
	 * @param u See above.
	 * @return See above.
	 */
	public Undoable combine(Undoable u);
	
	/**
	 * Indicates if this <tt>Undoable</tt> represents a significant action that
	 * can be undone. This affects how the <tt>Undoable</tt> is treated by the
	 * <tt>UndoManager</tt>. For example, when <tt>undo()</tt> is called on the
	 * manager, it will undo all <tt>Undoable</tt>s until it reaches and undoes
	 * a significant one. For more information see relevant <tt>UndoManager</tt>
	 * methods.
	 * <p>
	 * Note that an <tt>Undoable</tt> should not change it's significance status
	 * after it has been constructed. Doing that can result in unpredictable
	 * behavior.
	 * 
	 * @return See above.
	 * @see UndoManager#nextUndo()
	 * @see UndoManager#nextRedo()
	 * @see UndoManager#undo()
	 * @see UndoManager#redo()
	 * @see UndoManager#setLimit(int)
	 */
	public boolean isSignificant();
	
	/**
	 * Provides a short, concise name of this <tt>Undoable</tt> intended for
	 * user feedback in places in which a short identifier is required. It is
	 * not required to exactly describe the functionality of the
	 * <tt>Undoable</tt>, nor to uniquely represent it. Only to give a general
	 * idea about what it does.
	 * <p>
	 * It is recommended that the name is a maximum of 4 words long, the first
	 * word being capitalized, and that no punctuation is contained in it.
	 * 
	 * @return See above.
	 */
	public String name();
	
	/**
	 * Adds a listener that will be notified when this <tt>Undoable</tt>
	 * performs undo / redo operations.
	 * 
	 * @param listener See above.
	 */
	public void addUndoListener(UndoListener listener);
	
	/**
	 * Removes the given <tt>listener</tt>.
	 * 
	 * @param listener	To be removed.
	 */
	public void removeUndoListener(UndoListener listener);
}