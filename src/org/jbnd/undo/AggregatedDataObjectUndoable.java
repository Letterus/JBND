package org.jbnd.undo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jbnd.DataObject;
import org.jbnd.event.DataObjectEvent;
import org.jbnd.event.DataObjectListener;
import org.jbnd.event.UndoEvent;
import org.jbnd.event.UndoListener;
import org.jbnd.event.UndoEvent.Type;
import org.jbnd.localization.Messages;


/**
 * Aggregates multiple changes in a <tt>DataObject</tt> (multiple
 * <tt>DataObjectEvent</tt>s) into a single <tt>Undoable<tt>.
 * 
 * @version 1.0 Oct 27, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class AggregatedDataObjectUndoable extends AbstractUndoable implements
		UndoListener{
	
	// the undoables this object aggregates
	private final List<Undoable> undoables;
	
	/**
	 * Creates an <tt>AggregatedDataObjectUndoable</tt> from an arbitrary number
	 * of <tt>DataObjectEvent</tt>s. At least one is required. This constructor
	 * assumes that events are passed to it in the order in which they occurred.
	 * In other words, undo operations will be applied in the reverse order on
	 * the passed arguments (the last argument, or the last argument in the
	 * array will be undone first). Redo operations will be applied in the order
	 * the arguments were passed (the last argument is executed first). To
	 * achieve the opposite ordering use the {@link #inverse()} method.
	 * 
	 * @param events See above.
	 */
	public AggregatedDataObjectUndoable(DataObjectEvent... events){
		
		if(events.length < 1)
			throw new IllegalArgumentException("Need at least one event");
		
		undoables = new ArrayList<Undoable>(events.length);
		for(int i = 0 ; i < events.length ; i++){
			
			// create the undoable from an event
			Undoable u = new DataObjectUndoable(events[i]);
			
			// try to combine the old and the new
			if(i > 0){
				Undoable last = undoables.get(undoables.size() - 1);
				Undoable combined = last.combine(u);
				
				if(combined != null){
					
					// remove last from the list
					last.removeUndoListener(this);
					undoables.remove(undoables.size() - 1);
					
					// dispose those that are unnecessary
					if(combined != u)
						u.dispose();
					
					if(combined != last)
						last.dispose();
					
					// set combined as u, to set a listener
					u = combined;
				}
			}
			
			undoables.add(u);
			
			// need to listen for undoables being disposed
			u.addUndoListener(this);
		}
	}
	
	/**
	 * Same as {@link #AggregatedDataObjectUndoable(DataObjectEvent...)}, but
	 * accepts a <tt>List</tt> of events.
	 * 
	 * @param events See above.
	 */
	public AggregatedDataObjectUndoable(List<DataObjectEvent> events){
		this(events.toArray(new DataObjectEvent[events.size()]));
	}
	
	/**
	 * Inverses the order in which individual operations of this
	 * <tt>AggregatedDataObjectUndoable</tt> are performed, and returns itself.
	 * 
	 * @return See above.
	 * @see #AggregatedDataObjectUndoable(DataObjectEvent...)
	 */
	public AggregatedDataObjectUndoable inverse(){
		
		List<Undoable> newList = 
			new ArrayList<Undoable>(undoables.size());
		
		for(int i = undoables.size() - 1 ; i > -1 ; i--)
			newList.add(undoables.get(i));
		
		undoables.clear();
		undoables.addAll(newList);
		
		return this;
	}

	protected void _redo(){
		for(int i = 0, c = undoables.size() ; i < c ; i++)
		
			undoables.get(i).redo();
		
		_refreshCanUndoRedo();
	}

	protected void _undo(){
		for(int i = undoables.size() - 1 ; i > -1 ; i--)
			undoables.get(i).undo();
		
		_refreshCanUndoRedo();
	}
	
	private void _refreshCanUndoRedo(){
		canUndo = true;
		canRedo = true;
		for(Undoable u : undoables){
			if(!u.canRedo()) canRedo = false;
			if(!u.canUndo()) canUndo = false;
			
			if(!(canUndo || canRedo)) return;
		}
	}

	/**
	 * Always returns <tt>null</tt>.
	 * 
	 * @param u Ignored.
	 * @return <tt>null</tt>.
	 */
	public Undoable combine(Undoable u){
		return null;
	}

	public String name(){
		return undoables.size() > 1 ? 
				Messages.fillUp(Messages.getString(
						"AggregatedDataObjectUndoable.0"), undoables.size()) //$NON-NLS-1$
				: undoables.get(0).name();
	}
	
	public void eventOccurred(UndoEvent event){
		
		// only react to undoables being disposed
		if(event.getType() == Type.DISPOSED){
			
			// remove the disposed event from the list
			event.getUndoable().removeUndoListener(this);
			undoables.remove(event.getUndoable());
			
			// will there be at least one event left
			if(undoables.size() == 0){
				dispose();
			}else
				fireEvent(new UndoEvent(Type.CHANGED, this));
		}
		
		else if(event.getType() == Type.CHANGED)
			fireEvent(new UndoEvent(Type.CHANGED, this));
	}
	
	protected void _dispose(){
		for(Undoable u : undoables){
			u.removeUndoListener(this);
			u.dispose();
		}
	}

	/**
	 * Convenience class for accumulating <tt>DataObjectEvent</tt>s and making
	 * an <tt>AggregatedDataObjectUndoable</tt> of them. To accumulate events
	 * simply instantiate an <tt>EventAggregator</tt> and add it as a listener
	 * to <tt>DataObject</tt>s whose change events should be accumulated.
	 * <p>
	 * Note that this object will not automatically un-register itself from the
	 * <tt>DataObject</tt>s it is listening to, you need to do that.
	 * 
	 * @version 1.0 Oct 27, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public static class EventAggregator implements DataObjectListener{
		
		public EventAggregator(){}
		
		/**
		 * Creates an <tt>EventAggregator</tt> and adds it as a
		 * <tt>DataObjectListener</tt> to the passed <tt>object</tt>.
		 * 
		 * @param object See above.
		 */
		public EventAggregator(DataObject object){
			object.addDataObjectListener(this);
		}

		// the list of accumulated events
		private final List<DataObjectEvent> events = new LinkedList<DataObjectEvent>();
		
		public void objectChanged(DataObjectEvent event){
			if(event.getType() != DataObjectEvent.Type.DERIVED_PROPERTY_CHANGE)
				events.add(event);
		}

		/**
		 * Creates an <tt>AggregatedDataObjectUndoable</tt> and returns it. Does
		 * NOT un-register itself as a listener of any <tt>DataObject</tt>s
		 * whose changes it is tracking, you need to do that manually. It
		 * however does forget any events that occurred till now, so it is in
		 * principle reusable.
		 * <p>
		 * If there are no events aggregated yet, no undoable is created, and
		 * <tt>null</tt> is returned.
		 * 
		 * @return See above.
		 */
		public AggregatedDataObjectUndoable makeUndoable(){
			
			int eventsSize = events.size();
			if(eventsSize == 0) return null;
			
			AggregatedDataObjectUndoable rVal = new AggregatedDataObjectUndoable(
					events.toArray(new DataObjectEvent[events.size()]));
			
			events.clear();
			
			return rVal;
		}
	}
}