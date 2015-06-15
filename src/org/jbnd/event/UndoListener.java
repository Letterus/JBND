package org.jbnd.event;

import java.util.EventListener;

import org.jbnd.undo.UndoManager;


/**
 * A interface that objects interested in observing changes in an
 * <tt>Undoable</tt> or in an <tt>UndoManager</tt> should implement.
 * 
 * @version 1.0 Oct 23, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see UndoManager
 */
public interface UndoListener extends EventListener{

	/**
	 * Called when the <tt>Undoable</tt> performs and undo / redo, or when an
	 * <tt>UndoManager</tt> changes in whichever way.
	 * 
	 * @param event The change event.
	 */
	public void eventOccurred(UndoEvent event);
}