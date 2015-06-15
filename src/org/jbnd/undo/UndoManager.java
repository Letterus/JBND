package org.jbnd.undo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jbnd.event.UndoEvent;
import org.jbnd.event.UndoListener;
import org.jbnd.event.UndoEvent.Type;


/**
 * Collects and manages <tt>Undoable</tt>s, and performs undo / redo operations
 * on that collection.
 * <p>
 * In all normal situations the single instance of this class should be used, to
 * be obtained using the {@link #get()} method.
 * <p>
 * At this moment undo operations of any kind are not thread-safe.
 * 
 * @version 1.0 Oct 22, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public final class UndoManager implements UndoListener{
	
	private static final UndoManager _INSTANCE = new UndoManager();
	
	/**
	 * Returns the 'default' instance of this class, which should be used in all
	 * normal situations. However, this is not enforced, if necessary you can
	 * instantiate multiple <tt>UndoManager</tt>s, they will be the same as the
	 * instance returned from this method.
	 * 
	 * @return See above.
	 */
	public static UndoManager get(){
		return _INSTANCE;
	}

	/*
	 * Contains all the <tt>Undoable</tt>s this manager is tracking, in the same
	 * order in which they were added.
	 */
	private final List<Undoable> queue = new ArrayList<Undoable>();
	
	/*
	 * Points to the next element of the queue that is undoable. Can never be
	 * greater then the queue size - 1, but can reach -1, at times when a undo
	 * can not be performed on this manager.
	 */
	private int current = -1;
	
	/*
	 * The maximum number of significant Undoable elements that this manager is
	 * allowed to track before starting to discard the oldest ones.
	 */
	private int limit = 30;
	
	/**
	 * Returns the next significant <tt>Undoable</tt> of this manager that would
	 * be performed (along with all the insignificant ones leading to it) if
	 * <tt>undo()</tt> was called. If there is no such <tt>Undoable</tt> in the
	 * queue, <tt>null</tt> is returned.
	 * <p>
	 * This method is just for checking, it does not affect the queue.
	 * 
	 * @return See above.
	 */
	public Undoable nextUndo(){
		
		if(current < 0) return null;
		
		int index = current;
		Undoable rVal = queue.get(index);
		
		// search for a significant undoable, while there are more elements
		while(!rVal.isSignificant() && --index > -1)
			rVal = queue.get(index);
		
		return rVal.isSignificant() ? rVal : null;
	}
	
	/**
	 * Returns the next significant <tt>Undoable</tt> of this manager that would
	 * be performed (along with all the insignificant ones leading to it) if
	 * <tt>redo()</tt> was called. If there is no such <tt>Undoable</tt> in the
	 * queue, <tt>null</tt> is returned.
	 * <p>
	 * This method is just for checking, it does not affect the queue.
	 * 
	 * @return See above.
	 */
	public Undoable nextRedo(){
		
		int index = current + 1;
		int queueSize = queue.size();
		
		if(index >= queueSize) return null;
		
		Undoable rVal = queue.get(index);
		
		// search for a significant undoable, while there are more elements
		while(!rVal.isSignificant() && ++index < queueSize)
			rVal = queue.get(index);
		
		return rVal.isSignificant() ? rVal : null;
	}
	
	/**
	 * Undoes <tt>Undoable</tt>s in the queue until it reaches and undoes one
	 * that is significant. If after that only insignificant <tt>Undoable</tt>s
	 * are left in the queue, they are undone as well.
	 * <p>
	 * If there are no significant <tt>Undoable</tt>s in the queue to be undone,
	 * an exception will be thrown. This can be checked using the
	 * {@link #nextUndo()} method.
	 */
	public void undo(){
		
		// are there undoables in the stack?
		if(nextUndo() == null)
			throw new IllegalStateException(
					"There is no significant undoable to undo in the queue");
		
		// get the last undoable and check if it can be undone
		Undoable u = queue.get(current);
		
		// perform it
		u.undo();
		
		// if the undoable can not be redone, remove it from the stack, along with
		// all the items that come after it
		if(!u.canRedo()){
			List<Undoable> subList = queue.subList(current, queue.size());
			for(Undoable toDispose : subList){
				toDispose.removeUndoListener(this);
				toDispose.dispose();
			}
			subList.clear();
		}
		
		// decrement the current item index
		current--;
		
		// we are done only if the undoable just undone is significant
		if(u.isSignificant()){
			
			// if this was the last significant undo in the queue,
			// then go all the way
			if(nextUndo() == null)
				while(current > -1)
					undo();
			
			fireEvent(new UndoEvent(UndoEvent.Type.UNDID, this, u));
			
		}else
			undo();
	}
	
	/**
	 * Undoes <tt>Undoable</tt>s in the queue until it reaches and undoes one
	 * that is significant. If after that only insignificant <tt>Undoable</tt>s
	 * are left in the queue, they are undone as well. All the <tt>Undoable</tt>
	 * s undone will be disposed of, and removed from the stack.
	 * <p>
	 * If there are no significant <tt>Undoable</tt>s in the queue to be undone,
	 * an exception will be thrown. This can be checked using the
	 * {@link #nextUndo()} method.
	 */
	public void undoAndDispose(){
		
		// are there undoables in the stack?
		if(nextUndo() == null)
			throw new IllegalStateException(
					"There is no significant undoable to undo in the queue");
		
		// get the last undoable and check if it can be undone
		Undoable u = queue.get(current);
		
		// perform it
		u.undo();
		
		// remove it from the stack, along with
		// all the items that come after it
		List<Undoable> subList = queue.subList(current, queue.size());
		for(Undoable toDispose : subList){
			toDispose.removeUndoListener(this);
			toDispose.dispose();
		}
		subList.clear();
		
		// decrement the current item index
		current--;
		
		// we are done only if the undoable just undone is significant
		if(u.isSignificant()){
			
			// if this was the last significant undo in the queue,
			// then go all the way
			if(nextUndo() == null)
				while(current > -1)
					undo();
			
			fireEvent(new UndoEvent(UndoEvent.Type.UNDID, this, u));
			
		}else
			undo();
	}
	
	/**
	 * Redoes <tt>Undoable</tt>s in the queue until it reaches and redoes one
	 * that is significant. If after that only insignificant <tt>Undoable</tt>s
	 * are left in the queue, they are undone as well.
	 * <p>
	 * If there are no significant <tt>Undoable</tt>s in the queue to be redone,
	 * an exception will be thrown. This can be checked using the
	 * {@link #nextRedo()} method.
	 */
	public void redo(){
		
		// are there undoables in the stack?
		if(nextRedo() == null)
			throw new IllegalStateException("There is nothing to redo in the stack");
		
		int index = current + 1;
		
		// get the last undoable and check if it can be undone
		Undoable u = queue.get(index);
		
		// perform it
		u.redo();
		
		// if the undoable can not be undone, remove it from the stack, along with
		// all the items that come before it
		if(!u.canUndo()){
			List<Undoable> subList = queue.subList(0, index + 1);
			for(Undoable toDispose : subList){
				toDispose.removeUndoListener(this);
				toDispose.dispose();
			}
			subList.clear();
			// the current item index will thereby need to be reset
			current = -1;
		}
		else
			current++;
		
		// we are done only if the undoable just undone is significant,
		// or we've run out of things to redo
		if(u.isSignificant())
			fireEvent(new UndoEvent(UndoEvent.Type.REDID, this, u));
		else 
			redo();
	}
	
	/**
	 * Adds an <tt>Undoable</tt> to the queue of this <tt>UndoManager</tt>. This
	 * can have various consequences:
	 * <p>
	 * If there are items to be redone in this manager, they will be removed
	 * from the queue.
	 * <p>
	 * If there already are items in the queue, an attempt will be made to
	 * combine the last of them with <tt>u</tt>, as defined by the
	 * {@link Undoable#combine(Undoable)} method.
	 * <p>
	 * If <tt>u</tt> is significant, and it tops the limit of significant
	 * <tt>Undoable</tt>s this manager is allowed to manage, the oldest
	 * significant <tt>Undoable</tt> will be removed from the queue, along with
	 * all the insignificant ones before it.
	 * 
	 * @param u The <tt>Undoable</tt> to add to the queue of this manager.
	 */
	public void add(Undoable u){
		
		// need to remove every undoable that is at a higher index then
		// current... those are the items waiting to be redone possibly,
		// but that will not be possible after another item has been
		// added
		if(current + 1 < queue.size()){
			// remove items
			List<Undoable> subList = queue.subList(current + 1, queue.size());
			for(Undoable toDispose : subList){
				toDispose.removeUndoListener(this);
				toDispose.dispose();
			}
			subList.clear();
		}
		
		// get the last undoable on the stack and try to combine with u
		if(queue.size() > 0){
			
			// try to combine
			Undoable last = queue.get(queue.size() - 1);
			Undoable combined = last.combine(u);
			
			if(combined != null){
				// successfully combined
				
				// change queue
				queue.remove(queue.size() - 1);
				queue.add(combined);
				// remove this from the list of event listeners
				// because it will be added again at the end of 
				// this method
				combined.removeUndoListener(this);
				
				// dispose those that are unnecessary
				if(combined != u){
					u.removeUndoListener(this);
					u.dispose();
				}
				if(combined != last){
					last.removeUndoListener(this);
					last.dispose();
				}
				
				// set the u reference to combined, for event firing
				u = combined;
				
			}else{
				// could not combine
				queue.add(u);
			}
			
		}else{
			// there are no items in the queue, so can't combine
			queue.add(u);
		}
		
		// check if this item is going over the limit... if so, remove
		// all undoables up until the first significant one
		if(u.isSignificant() && getSignificantSize() > limit){
			
			// find the first significant undoable
			int firstSignificantIndex = -1;
			for(int i = 0, c = queue.size() ; i < c ; i++)
				if(queue.get(i).isSignificant()){
					firstSignificantIndex = i;
					break;
				}
			
			// remove it, and all insignificant ones before it
			List<Undoable> subList = queue.subList(0, firstSignificantIndex + 1);
			for(Undoable u2 : subList){
				u2.removeUndoListener(this);
				u2.dispose();
			}
			
			subList.clear();
		}
		
		// when adding a new undoable, the currentIndex gets set to it
		current = queue.size() - 1;
		
		// listen for disposing of the added undoable
		u.addUndoListener(this);
		
		// adding has been performed, fire event
		fireEvent(new UndoEvent(UndoEvent.Type.ADDED, this, u));
	}
	
	/*
	 * Returns the number of significant undoables in the queue of the manager.
	 */
	protected int getSignificantSize(){
		int count = 0;
		for(Undoable u : queue)
			if(u.isSignificant()) count++;
		
		return count;
	}
	
	/**
	 * Removes all the <tt>Undoable</tt>s from the queue of this manager.
	 */
	public void clear(){
		for(Undoable u : queue){
			u.removeUndoListener(this);
			u.dispose();
		}
		
		current = -1;
		queue.clear();
		fireEvent(new UndoEvent(Type.CLEARED, this, null));
	}
	
	/**
	 * Returns the number indicating the maximum allowed number of significant
	 * <tt>Undoable</tt>s for this manager.
	 * 
	 * @return	See above.
	 */
	public int getLimit(){
		return limit;
	}
	
	/**
	 * Sets the new limit indicating the maximum allowed number of significant
	 * <tt>Undoable</tt>s for this manager. If it is lower then the number of
	 * currently held significant <tt>Undoable</tt>s, then the queue is trimmed.
	 * 
	 * @param newLimit The new limit.
	 */
	public void setLimit(int newLimit){
		
		int oldLimit = limit;
		limit = newLimit;
		
		// what's the difference between the old and new limits
		if(oldLimit <= newLimit) return;
		
		// the new limit is lesser then the old one
		// how many items do we need to remove?
		int sig = getSignificantSize();
		if(newLimit >= sig) return;
		
		int toRemove = sig - newLimit;
		
		// find the index of the last significant undoable to remove
		int significantIndex = -1;
		for(int i = 0, c = queue.size() ; i < c ; i++)
			if(queue.get(i).isSignificant())
				if(--toRemove == 0){
					significantIndex = i;
					break;
				}
		
		// remove them
		List<Undoable> subList = queue.subList(0, significantIndex + 1);
		for(Undoable u2 : subList){
			u2.removeUndoListener(this);
			u2.dispose();
		}
		
		// need to set the currentIndex to accommodate for having 
		// reduced the size of the queue
		current -= subList.size();
		current = Math.max(-1, current);
		
		subList.clear();
		
		// adding has been performed, fire event
		fireEvent(new UndoEvent(UndoEvent.Type.LIMIT_REDUCED, this, null));
	}
	
	
	/*
	 * 
	 * EVENT STUFF
	 * 
	 */
	private final List<UndoListener> listeners = new LinkedList<UndoListener>();
	
	/**
	 * Adds a listener that should be notified of changes in this manager.
	 * @param l	See above.
	 */
	public void addUndoListener(UndoListener l){
		listeners.add(l);
	}
	
	/**
	 * Removes on of the existing listeners.
	 * 
	 * @param l	See above.
	 */
	public void removeUndoListener(UndoListener l){
		listeners.remove(l);
	}
	
	protected void fireEvent(UndoEvent e){
		UndoListener[] listenerArray = new UndoListener[listeners.size()];
		listeners.toArray(listenerArray);
		for(int i = listenerArray.length - 1 ; i > -1 ; i--)
			listenerArray[i].eventOccurred(e);
	}

	/**
	 * <tt>UndoListener</tt> implementation, listens to events from the
	 * <tt>Undoable</tt>s contained in this manager.
	 * 
	 * @param event
	 */
	public void eventOccurred(UndoEvent event){
		
		if(event.getType() == Type.DISPOSED){

			int index = queue.indexOf(event.getUndoable());
			if(index == -1)
				throw new IllegalStateException("Should not happen");

			if(index <= current) current--;
			queue.remove(index);

			fireEvent(new UndoEvent(Type.REMOVED, this, event.getUndoable()));
		}
		
		else if(event.getType() == Type.CHANGED)
			fireEvent(new UndoEvent(Type.CHANGED, this, event.getUndoable()));
		
	}
}