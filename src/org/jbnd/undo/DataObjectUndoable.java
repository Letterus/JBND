package org.jbnd.undo;

import java.util.List;

import org.jbnd.DataObject;
import org.jbnd.binding.AddEditGroup.CachingDataObject;
import org.jbnd.event.DataObjectEvent;
import org.jbnd.event.DataObjectEvent.Type;
import org.jbnd.localization.Messages;
import org.jbnd.support.NamingSupport;


/**
 * An automatically created <tt>Undoable</tt> that represents a change that
 * happened in a <tt>DataObject</tt>. Automated creation happens as a
 * consequence of <tt>DataObjectEvent</tt> firing. Undoing / redoing is
 * performed by applying the inverse action. If this is not adequate or
 * sufficient for a <tt>DataObject</tt>, it can implement {@link Override},
 * which will make <tt>DataObjectUndoable</tt> us that functionality instead of
 * the default one.
 * <p>
 * Switches are provided for determining which <tt>DataObjectEvent</tt>s should
 * result in the creation of a <tt>DataObjectUndoable</tt>. Note that most of
 * those switches function in conjunction with appropriate classes in the
 * <tt>org.jbnd.binding</tt> package, and may not function if you use custom
 * made extensions to JBND.
 * 
 * @version 1.0 Oct 23, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class DataObjectUndoable extends AbstractUndoable{
	
	/**
	 * A boolean indicating whether <tt>DataObjectUndoable</tt>s should be
	 * generated when <tt>DataObject</tt>s change. In most cases you should not
	 * change this value manually, it is used internally by JBND to handle
	 * undoable creation. To disable the automated creation of undoables use one
	 * of the <tt>CREATE_FOR...</tt> flags in this class. Still, in some cases
	 * it might be necessary to temporarily set this switch to <tt>false</tt>,
	 * perform some work, and switch it back to <tt>true</tt>.
	 */
	public static boolean CREATE_UNDOABLES = true;
	
	/**
	 * Controls whether <tt>DataObjectUndoable</tt>s should be created when
	 * non-deferred (direct, always per property since there is no edit
	 * aggregation process) edits are performed; defaults to <tt>true</tt>.
	 */
	public static boolean CREATE_FOR_DIRECT_PROP_EDITS = true;
	
	/**
	 * Controls whether <tt>DataObjectUndoable</tt>s should be created when
	 * individual (per property) deferred adds and edits are performed (using
	 * <tt>AddEditGroup</tt>s and <tt>AddEditBinding</tt>s); defaults to
	 * <tt>true</tt>.
	 */
	public static boolean CREATE_FOR_DEFERRED_PROP_ADDS_EDITS = true;

	/**
	 * Controls whether <tt>DataObjectUndoable</tt>s should be created when
	 * individual (per property) deferred edits are committed (using
	 * <tt>AddEditGroup</tt>s and <tt>AddEditBinding</tt>s); defaults to
	 * <tt>false</tt>.
	 * <p>
	 * <b>Adds:</b> Deferred adds never generate undoables in the same
	 * situation, because an undo performed after a deferred add should not
	 * clear the newly set values from the record, but remove the whole record.
	 */
	public static boolean CREATE_FOR_DEFERRED_PROP_COMMITS = false;
	
	/**
	 * Controls whether <tt>AggregatedDataObjectUndoable</tt>s should be created
	 * when deferred edits are committed (as a single per-DataObject undoable
	 * operation, using <tt>AddEditGroup</tt>s and <tt>AddEditBinding</tt>s);
	 * defaults to <tt>true</tt>.
	 * 
	 * @see AggregatedDataObjectUndoable
	 */
	public static boolean CREATE_FOR_DEFERRED_EDIT_COMMITS = true;
	
	/**
	 * Controls whether <tt>DataObjectUndoable</tt>s should be created when
	 * <tt>DataObject</tt>s holding qualifier values (using
	 * <tt>QualifierGroup</tt>s and <tt>QualifierBinding</tt>s) are changed;
	 * defaults to <tt>true</tt>.
	 */
	public static boolean CREATE_FOR_QUALIFIERS = true;
	
	/**
	 * The manager into which the <tt>DataObjectUndoable</tt>s will be
	 * automatically inserted. Defaults to {@link UndoManager#get()}, but any
	 * not <tt>null</tt> <tt>UndoManager</tt> will do. Note that this manager
	 * will be also used by JBND as the default one for
	 * {@link AggregatedDataObjectUndoable} and
	 * {@link CachingDataObjectUndoable} objects, since they are concerned with
	 * changes in <tt>DataObject</tt>s.
	 */
	public static UndoManager MANAGER = UndoManager.get();
	
	/**
	 * The central method for creating and registering <tt>Undoable</tt>s that
	 * represent undoable changes in a <tt>DataObject</tt>. This method may or
	 * may not create and register an <tt>Undoable</tt>. The behavior depends on
	 * the type of change that happened (as can be determined from the
	 * <tt>event</tt>, and the context (the state of switched, as explained in
	 * {@link DataObjectUndoable}). If it is determined that an
	 * <tt>Undoable</tt> should be created and registered, it will be registered
	 * with {@link #MANAGER}.
	 * 
	 * @param event The <tt>DataObjectEvent</tt> encapsulating information about
	 *            a change in a <tt>DataObject</tt>.
	 * @return If or not an <tt>Undoable</tt> was created and registered.
	 */
	public static boolean registerUndoable(DataObjectEvent event){
		
		// if the main switch is false, don't do it
		if(!DataObjectUndoable.CREATE_UNDOABLES) return false;
		
		// depending on the change type Undoables should or shouldn't be generated
		switch(event.getType()){
			case DERIVED_PROPERTY_CHANGE : return false;
			case QUALIFIER_VALUE_CHANGE :
				if(!CREATE_FOR_QUALIFIERS) return false;
				break;
			case CACHED_PROPERTY_CHANGE :
				if(!CREATE_FOR_DEFERRED_PROP_ADDS_EDITS) return false;
				break;
		}
		
		if(event.getDataObject() instanceof CachingDataObject)
			DataObjectUndoable.MANAGER.add(new CachingDataObjectUndoable(event));
		else
			DataObjectUndoable.MANAGER.add(new DataObjectUndoable(event));
		
		return true;
	}
	
	
	
	
	
	
	
	
	// data required by this undoable
	protected final DataObject dataObject;
	private final String key;
	private final Type changeType;
	private final Object relevantValue;
	// new value is not final as it is potentially changed in the combine(Undoable) method
	private Object newValue;
	
	/**
	 * Creates a <tt>DataObjectUndoable</tt> based on a <tt>DataObjectEvent</tt>
	 * that is a consequence of a change in any <tt>DataObject</tt>. This
	 * process is automated, you do not have to instantiate
	 * <tt>DataObjectUndoable</tt>s manually.
	 * 
	 * @param event See above.
	 */
	protected DataObjectUndoable(DataObjectEvent event){
		
		this.dataObject = (DataObject)event.getSource();
		
		this.key = event.getKey();
		this.changeType = event.getType();
		this.relevantValue = event.getRelevantValue();
		this.newValue = event.getNewValue();
	}

	/**
	 * Does the actual work of redoing an undone <tt>DataObject</tt> change.
	 * This implementation will simply get the <tt>DataObject</tt> into the
	 * state in which it was before the change was undone. If different /
	 * augmented behavior is desired, use an {@link Override}.
	 */
	protected void _redo(){
		try{
			// turn this switch off, temporarily, because when applying
			// an inverse action on a DataObject an event will be fired,
			// which will normally result in the creation of another
			// Undoable, but when performing this action we don't want that
			CREATE_UNDOABLES = false;
			
			if(dataObject instanceof Override){
				if( ! ((Override)dataObject).redo(this) )
					return;
			}
			
			// simply inverse the process
			switch(changeType){
				
				case ATTRIBUTE_CHANGE : case CACHED_PROPERTY_CHANGE : case QUALIFIER_VALUE_CHANGE :
					dataObject.set(newValue, key);
					break;
				
				case TO_ONE_CHANGE :
					if(newValue == null)
						dataObject.unrelate((DataObject)relevantValue, key);
					else
						dataObject.relate((DataObject)newValue, key);
					break;
				
				case TO_MANY_ADD :
					dataObject.relate((DataObject)relevantValue, key);
					break;
				
				case TO_MANY_REMOVE :
					dataObject.unrelate((DataObject)relevantValue, key);
					break;
				
				case TO_MANY_CHANGE :
					
					@SuppressWarnings("unchecked")
					List<DataObject> old = (List<DataObject>)relevantValue;
					for(DataObject o : old)
						dataObject.unrelate(o, key);
					
					@SuppressWarnings("unchecked")
					List<DataObject> newRelation = (List<DataObject>)newValue;
					for(DataObject o : newRelation)
						dataObject.relate(o, key);
					break;
					
				default :
					throw new IllegalStateException("Should never happen"); //$NON-NLS-1$
			}
		
		}finally{
			// reset the switch
			CREATE_UNDOABLES = true;
		}
	}
	
	/**
	 * Does the actual work of undoing a <tt>DataObject</tt> change. This
	 * implementation will simply get the <tt>DataObject</tt> into the state in
	 * which it was before the change was done. If different / augmented
	 * behavior is desired, use an {@link Override}.
	 */
	protected void _undo(){
		try{
			// turn this switch off, temporarily, because when applying
			// an inverse action on a DataObject an event will be fired,
			// which will normally result in the creation of another
			// Undoable, but when performing this action we don't want that
			CREATE_UNDOABLES = false;
			
			if(dataObject instanceof Override){
				if( ! ((Override)dataObject).undo(this) )
					return;
			}
			
			// simply inverse the process
			switch(changeType){
				
				case ATTRIBUTE_CHANGE : case CACHED_PROPERTY_CHANGE : case QUALIFIER_VALUE_CHANGE :
					dataObject.set(relevantValue, key);
					break;
				
				case TO_ONE_CHANGE :
					if(relevantValue == null)
						dataObject.unrelate((DataObject)newValue, key);
					else
						dataObject.relate((DataObject)relevantValue, key);
					break;
				
				case TO_MANY_ADD :
					dataObject.unrelate((DataObject)relevantValue, key);
					break;
				
				case TO_MANY_REMOVE :
					dataObject.relate((DataObject)relevantValue, key);
					break;
				
				case TO_MANY_CHANGE :
					
					@SuppressWarnings("unchecked")
					List<DataObject> current = (List<DataObject>)newValue;
					for(DataObject o : current)
						dataObject.unrelate(o, key);
					
					@SuppressWarnings("unchecked")
					List<DataObject> old = (List<DataObject>)relevantValue;
					for(DataObject o : old)
						dataObject.relate(o, key);
					break;
					
				default :
					throw new IllegalStateException(
							"Should never happen, unknown change type: "+changeType); //$NON-NLS-1$
			}
		
		}finally{
			// reset the switch
			CREATE_UNDOABLES = true;
		}
	}

	/**
	 * Combines the given <tt>Undoable</tt> if:
	 * <ul>
	 * <li>It is a <tt>DataObjectUndoable</tt></li> 
	 * <li>Both it and this represent an attribute change</li>
	 * <li>The change is for the same <tt>DataObject</tt></li>
	 * <li>And for the same key</li>
	 * </ul>
	 * or if the two undoables represent changes to two sides of the same
	 * relationship.
	 * 
	 * @param u See above.
	 * @return Hm, hm, hm, so hard to say.
	 */
	public Undoable combine(Undoable u){
		
		if(!(u instanceof DataObjectUndoable))
			return null;
		
		DataObjectUndoable dou = (DataObjectUndoable)u;

		// non-relationships of the same change type can
		// maybe be combined
		if(this.changeType == dou.changeType && isDirectlySettable()){

			// if the keys are not the same, can't combine
			if(!(dou.key.equals(key))) return null;

			// if the data objects are not the same, can't combine
			if(!(dou.dataObject.equals(dataObject))) return null;

			/*
			 * Since this is the older undoable, it's old value should be kept, and
			 * the new value of the newer undoable should be used.
			 */
			this.newValue = dou.newValue;

			return this;
		}
		
		// relationships and inverse relationships can maybe be combined
		if(isRelationship() && dou.isRelationship()){
			
			// check that relationships keys match
			String inverse = dataObject.getDataType().inverseRelationship(key);
			if(inverse == null || !inverse.equals(dou.key))
				return null;
			
			// check that the data types match
			if(!dataObject.getDataType().dataTypeForRelationship(key).
					equals(dou.dataObject.getDataType()))
				return null;
			
			// we are talking about two sides of the same
			// relationship. return the toOne
			if(dou.changeType == DataObjectEvent.Type.TO_ONE_CHANGE)
				return dou;
			else
				return this;
		}
		
		return null;
	}

	public String name(){
		
		if(changeType == DataObjectEvent.Type.QUALIFIER_VALUE_CHANGE)
			return Messages.getString("DataObjectUndoable.2");
		
		return Messages.fillUp(
				Messages.getString("DataObjectUndoable.0"), //$NON-NLS-1$
				NamingSupport.propertyName(key));
	}
	
	private boolean isDirectlySettable(){
		switch(changeType){
			case ATTRIBUTE_CHANGE :
				return true;
			case CACHED_PROPERTY_CHANGE :
				return true;
			case QUALIFIER_VALUE_CHANGE :
				return true;
			default :
				return false;
		}
	}
	
	private boolean isRelationship(){
		switch(changeType){
			case TO_MANY_ADD :
				return true;
			case TO_MANY_CHANGE :
				return true;
			case TO_MANY_REMOVE :
				return true;
			case TO_ONE_CHANGE :
				return true;
			default :
				return false;
		}
	}
	
	/**
	 * Same as <tt>DataObjectEvent.getDataObject()</tt>.
	 * 
	 * @return	See above.
	 * @see DataObjectEvent#getDataObject()
	 */
	public DataObject getDataObject(){
		return dataObject;
	}

	/**
	 * Same as <tt>DataObjectEvent.getKey()</tt>.
	 * 
	 * @return	See above.
	 * @see DataObjectEvent#getKey()
	 */
	public String getKey(){
		return key;
	}

	/**
	 * Same as <tt>DataObjectEvent.getType()</tt>.
	 * 
	 * @return See above.
	 * @see DataObjectEvent#getType()
	 */
	public Type getChangeType(){
		return changeType;
	}

	/**
	 * Same as <tt>DataObjectEvent.getRelevantValue()</tt>.
	 * 
	 * @return	See above.
	 * @see DataObjectEvent#getRelevantValue()
	 */
	public Object getRelevantValue(){
		return relevantValue;
	}

	/**
	 * Same as <tt>DataObjectEvent.getNewValue()</tt>.
	 * 
	 * @return	See above.
	 * @see DataObjectEvent#getNewValue()
	 */
	public Object getNewValue(){
		return newValue;
	}

	/**
	 * An interface that allows <tt>DataObject</tt>s to customize their undo /
	 * redo process... Normally undoing and redoing a change in the
	 * <tt>DataObject</tt> is done by simply reversing the original action.
	 * However, that might not be appropriate or sufficient in some cases. That
	 * is why this interface is provided. If a <tt>DataObject</tt> implements
	 * this interface, then the undo / redo methods of
	 * <tt>DataObjectUndoable</tt> will use it's methods.
	 * 
	 * @version 1.0 Oct 23, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public static interface Override extends DataObject{
		
		/**
		 * Called on a <tt>DataObject</tt> asking it to undo the action that is
		 * encapsulated in the passed <tt>DataObjectUndoable</tt>.
		 * <p>
		 * Should return <tt>true</tt> if the undo action is performed entirely
		 * by this method, and <tt>false</tt> if after this method is invoked
		 * the standard undo procedure should still be performed.
		 * 
		 * @param dou See above.
		 * @return See above.
		 */
		public boolean undo(DataObjectUndoable dou);
		
		/**
		 * Called on a <tt>DataObject</tt> asking it to redo the action that is
		 * encapsulated in the passed <tt>DataObjectUndoable</tt>.
		 * <p>
		 * Should return <tt>true</tt> if the redo action is performed entirely
		 * by this method, and <tt>false</tt> if after this method is invoked
		 * the standard redo procedure should still be performed.
		 * 
		 * @param dou See above.
		 * @return See above.
		 */
		public boolean redo(DataObjectUndoable dou);
	}
}