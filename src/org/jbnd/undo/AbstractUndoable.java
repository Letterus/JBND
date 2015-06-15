package org.jbnd.undo;

import java.util.LinkedList;
import java.util.List;

import org.jbnd.event.UndoEvent;
import org.jbnd.event.UndoListener;
import org.jbnd.event.UndoEvent.Type;

/**
 * A general purpose starting point for implementing <tt>Undoable</tt> items.
 * The idea is that most of the implementation details assume 'standard'
 * behaviors, allowing the concrete subclass to only concern itself with actual
 * undo / redo work.
 * 
 * @version 1.0 Oct 22, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public abstract class AbstractUndoable implements Undoable{

	/**
	 * Indicates if the <tt>Undoable</tt> is in the state in which it can be
	 * undone; returned from the <tt>canUndo()</tt> method.
	 * 
	 * @see #canUndo()
	 * @see #undo()
	 */
	protected boolean canUndo = true;
	
	/**
	 * Indicates if the <tt>Undoable</tt> is in the state in which it can be
	 * undone; returned from the <tt>canUndo()</tt> method.
	 * 
	 * @see #canUndo()
	 * @see #undo()
	 */
	protected boolean canRedo = false;
	
	/**
	 * Returns the {@link #canRedo} variable.
	 * 
	 * @return	See above.
	 */
	public boolean canRedo(){
		return canRedo;
	}

	/**
	 * Returns the {@link #canUndo} variable.
	 * 
	 * @return	See above.
	 */
	public boolean canUndo(){
		return canUndo;
	}

	/**
	 * Performs the following actions, in the given order:
	 * <ul>
	 * <li>Checks if {@link #canRedo} is <tt>true</tt>, if not an exception is
	 * thrown.</li>
	 * <li>Sets {@link #canRedo} to <tt>false</tt>, and {@link #canUndo} to
	 * <tt>true</tt>.</li>
	 * <li>Invokes {@link #_redo()}</li>
	 * <li>Fires and <tt>UndoEvent</tt></li>
	 * </ul>
	 */
	public final void redo(){
		if(!canRedo) 
			throw new IllegalStateException("Can not redo this undoable: "+this);
		
		canRedo = false;
		canUndo = true;
		_redo();
		fireEvent(new UndoEvent(UndoEvent.Type.REDID, this));
	}

	/**
	 * Performs the following actions, in the given order:
	 * <ul>
	 * <li>Checks if {@link #canUndo} is <tt>true</tt>, if not an exception is
	 * thrown.</li>
	 * <li>Sets {@link #canUndo} to <tt>false</tt>, and {@link #canRedo} to
	 * <tt>true</tt>.</li>
	 * <li>Invokes {@link #_undo()}</li>
	 * <li>Fires and <tt>UndoEvent</tt></li>
	 * </ul>
	 */
	public final void undo(){
		if(!canUndo) 
			throw new IllegalStateException("Can not undo this undoable: "+this);
		
		canUndo = false;
		canRedo = true;
		_undo();
		fireEvent(new UndoEvent(UndoEvent.Type.UNDID, this));
	}
	
	/**
	 * Implementation should provide undoing logic in this method.
	 * <tt>AbstractUndoable</tt> assumes that after this method is called
	 * {@link #canRedo} will be <tt>true</tt> and {@link #canUndo}
	 * <tt>false</tt>, but this method manually set those fields to different
	 * values.
	 */
	protected abstract void _undo();

	/**
	 * Implementation should provide redoing logic in this method.
	 * <tt>AbstractUndoable</tt> assumes that after this method is called
	 * {@link #canRedo} will be <tt>false</tt> and {@link #canUndo}
	 * <tt>true</tt>, but this method manually set those fields to different
	 * values.
	 */
	protected abstract void _redo();

	/**
	 * Calls {@link #_dispose()}, sets {@link #canUndo} and {@link #canRedo}
	 * to <tt>false</tt> and fires an event of the <tt>DISPOSED</tt> type.
	 */
	public final void dispose(){
		_dispose();
		canRedo = false;
		canUndo = false;
		fireEvent(new UndoEvent(Type.DISPOSED, this));
	}
	
	/**
	 * Called from {@link #dispose()}, if necessary override this method to
	 * perform custom disposing.
	 */
	protected void _dispose(){}

	/**
	 * This method returns <tt>true</tt>, if the concrete <tt>Undoable</tt>
	 * subclass is not significant, override.
	 * 
	 * @return See above.
	 */
	public boolean isSignificant(){
		return true;
	}
	
	/*
	 * 
	 * EVENT STUFF
	 * 
	 */
	private List<UndoListener> listeners;
	
	/**
	 * Adds a listener that should be notified of changes in this manager.
	 * @param l	See above.
	 */
	public void addUndoListener(UndoListener l){
		if(listeners == null) listeners = new LinkedList<UndoListener>();
		listeners.add(l);
	}
	
	/**
	 * Removes on of the existing listeners.
	 * 
	 * @param l	See above.
	 */
	public void removeUndoListener(UndoListener l){
		if(listeners == null) return;
		listeners.remove(l);
	}
	
	protected void fireEvent(UndoEvent e){
		if(listeners == null) return;
		
		UndoListener[] listenerArray = new UndoListener[listeners.size()];
		listeners.toArray(listenerArray);
		for(int i = listenerArray.length - 1 ; i > -1 ; i--)
			listenerArray[i].eventOccurred(e);
	}
}