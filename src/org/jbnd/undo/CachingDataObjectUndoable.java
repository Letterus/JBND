package org.jbnd.undo;

import org.jbnd.binding.AddEditGroup;
import org.jbnd.binding.AddEditGroup.CachingDataObject;
import org.jbnd.event.DataObjectEvent;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.event.DataSourceListener;


/**
 * A specialized <tt>DataObjectUndoable</tt> that deals with
 * {@link CachingDataObject} instances, because they have a limited life span.
 * JBND will automatically create these undoables for changes in
 * <tt>CachingDataObject</tt>s. They behave virtually the same as standard
 * <tt>DataObjectUndoable</tt>s, but they listen to the nesting
 * <tt>AddEditGroup</tt> instance's events, to detect when the cache is either
 * released or committed to real <tt>DataObject</tt>. When that happens, these
 * undoables will automatically dispose, since the <tt>CachingDataObject</tt>s
 * they represent for all intents and purposes don't exist anymore.
 * 
 * @version 1.0 Oct 28, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class CachingDataObjectUndoable extends DataObjectUndoable implements
		DataSourceListener{

	CachingDataObjectUndoable(DataObjectEvent event){
		super(event);
		((CachingDataObject)dataObject).getNestingInstance()
			.addDataSourceListener(this);
	}

	public void dataAdded(DataSourceEvent event){}

	public void dataChanged(DataSourceEvent event){
		_checkCachingDO();
	}

	public void dataRemoved(DataSourceEvent event){
		_checkCachingDO();
	}

	private void _checkCachingDO(){
		
		CachingDataObject cdo = (CachingDataObject)dataObject;
		AddEditGroup aeg = cdo.getNestingInstance();
		
		if(!aeg.contains(cdo)){
			dispose();
			aeg.removeDataSourceListener(this);
		}
	}
}