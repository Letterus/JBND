package org.jbnd.undo;

import org.jbnd.DataObject;
import org.jbnd.DataType;
import org.jbnd.DataType.PropType;
import org.jbnd.support.JBNDUtil;
import org.jbnd.undo.AggregatedDataObjectUndoable.EventAggregator;

/**
 * Provides JBND undo / redo specific functionalities.
 * 
 * @version 1.0 Dec 3, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class UndoUtil{

	/*
	 * Disallow instantiation and subclassing.
	 */
	private UndoUtil(){}
	
	/**
	 * Relates all the given <tt>objects</tt> to <tt>relateTo</tt>, for the
	 * given relationship name (<tt>relKey</tt>). The relationship name is
	 * sought from the perspective of <tt>objects</tt>.
	 * 
	 * @param objects See above.
	 * @param relKey See above.
	 * @param relateTo See above.
	 * @return An <tt>AggregatedDataObjectUndoable</tt> that makes it possible
	 *         to undo / redo this action with a single command. The undoable is
	 *         not automatically inserted into whichever manager. If no changes
	 *         occurred in involved <tt>DataObject</tt>s as a result of a call
	 *         to this method, <tt>null</tt> is returned.
	 */
	public static AggregatedDataObjectUndoable batchRelate(DataObject[] objects,
			String relKey, DataObject relateTo){
		
		// turn off undoable generation
		DataObjectUndoable.CREATE_UNDOABLES = false;
		
		// aggregate events
		EventAggregator a = new EventAggregator();
		
		// perform relations
		for(DataObject object : objects){
			if(object.get(relKey) == relateTo) continue;
			object.addDataObjectListener(a);
			object.relate(relateTo, relKey);
			object.removeDataObjectListener(a);
		}
		
		// restore the switch
		DataObjectUndoable.CREATE_UNDOABLES = true;
		
		return a.makeUndoable();
	}

	/**
	 * Copies all the values from one <tt>DataObject</tt> (the <tt>from</tt>
	 * param) to another one (the <tt>to</tt> param). The properties that are
	 * copied are those whose keys are returned from the <tt>from</tt> object's
	 * <tt>DataType</tt>'s <tt>properties()</tt> method.
	 * <p>
	 * Undoable generation. This method can copy values without generating
	 * <tt>DataObjectUndoable</tt>s for each property copied. This behavior is
	 * controlled with the <tt>createUndoables</tt> parameter. Also, an
	 * <tt>AggregatedDataObjectUndoable</tt> that encapsulates all of the
	 * property changes is created and returned, but not automatically added to
	 * a manager.
	 * 
	 * @param from See above.
	 * @param to See above.
	 * @param createUndables See above.
	 * @return See above.
	 */
	public static AggregatedDataObjectUndoable copyValues(DataObject from,
			DataObject to, boolean createUndables){
		
		// aggregate property copying into a single undoable
		EventAggregator a = new EventAggregator();
		to.addDataObjectListener(a);
		
		// should individual undoables be created?
		DataObjectUndoable.CREATE_UNDOABLES = createUndables;
		
		// copy values
		DataType dt = from.getDataType();
		for(String key : dt.properties()){
			
			// skip derived and undefined properties
			PropType type = dt.propertyType(key);
			if(type.isDerived() || type == PropType.UNDEFINED)
				continue;
			
			JBNDUtil.set(from.get(key), key, to);
		}
			
		// reset the switch
		DataObjectUndoable.CREATE_UNDOABLES = true;
	
		return a.makeUndoable();
	}
}