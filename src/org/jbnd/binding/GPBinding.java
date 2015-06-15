package org.jbnd.binding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbnd.DataObject;
import org.jbnd.ValidationException;
import org.jbnd.binding.AddEditGroup.CachingDataObject;
import org.jbnd.data.DataSource;
import org.jbnd.data.ProxyingDataSource;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.support.JBNDUtil;
import org.jbnd.support.ValueConverter;


/**
 * The <b>G</b>eneral <b>P</b>urpose binding is one that can be used to view,
 * edit (deferred or direct) and add (deferred) <tt>DataObject</tt>s. Edit
 * deferring is not controlled by the <tt>GPBinding</tt>, but by an
 * <tt>AddEditGroup</tt>. To use it, create a deferring <tt>AddEditGroup</tt>
 * and create a <tt>GPBinding</tt> with it as the binding's <tt>DataSource</tt>.
 * The <tt>GPBinding</tt> will automatically detect deferring
 * <tt>AddEditGroup</tt>s and do the right thing.
 * <p>
 * To create <tt>GPBinding</tt> instances for any specific purpose, use the
 * factory methods: {@link #view(DataSource, String...)}, and various overloaded
 * versions of {@link #edit(int, String, Object)}.
 * 
 * @version 1.0 Jun 19, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see AddEditGroup
 */
public final class GPBinding extends AbstractBinding{
	
	private static final ValidationHandler	_DEFAULT_VH	= new DefaultValidationHandler();
	
	/*
	 * 
	 * Factory methods.
	 * 
	 */
	
	/**
	 * Instantiates and returns a <tt>GPBinding</tt> with the given
	 * <tt>DataSource</tt> and binding <tt>key</tt>s, that is not at all
	 * editable, to be used for data viewing purposes only.
	 * 
	 * @param ds The <tt>DataSource</tt> that provides <tt>DataObject</tt>s to
	 *            this binding.
	 * @param keys The keys of properties (in the <tt>DataObject</tt>s of the
	 *            <tt>DataSource</tt> that this binding should provide / edit.
	 * @return See above.
	 */
	public static GPBinding view(DataSource ds, String... keys){
		if(keys == null || keys.length == 0)
			throw new IllegalArgumentException("At least one key is required");
		
		boolean[] editable = new boolean[keys.length];
		
		return new GPBinding(ds, keys, editable, editable);
	}
	
	/**
	 * Instantiates and returns a <tt>GPBinding</tt> with the given
	 * <tt>DataSource</tt> and binding <tt>key</tt>s, that is editable for all
	 * the given <tt>key</tt>s, for both direct or deferred editing and adding.
	 * 
	 * @param ds The <tt>DataSource</tt> that provides <tt>DataObject</tt>s to
	 *            this binding.
	 * @param keys The keys of properties (in the <tt>DataObject</tt>s of the
	 *            <tt>DataSource</tt> that this binding should provide / edit.
	 * @return See above.
	 */
	public static GPBinding edit(DataSource ds, String... keys){
		if(keys == null || keys.length == 0)
			throw new IllegalArgumentException("At least one key is required");
		
		boolean[] editable = new boolean[keys.length];
		for(int i = 0, c = editable.length ; i < c ; i++)
			editable[i] = true;
		
		return new GPBinding(ds, keys, editable, editable);
	}
	
	/**
	 * Instantiates and returns a <tt>GPBinding</tt> with the given
	 * <tt>DataSource</tt> and binding <tt>key</tt>s. Per key editability is
	 * controlled with the <tt>editable</tt> parameter, for both direct or
	 * deferred editing and adding.
	 * 
	 * @param ds The <tt>DataSource</tt> that provides <tt>DataObject</tt>s to
	 *            this binding.
	 * @param keys The keys of properties (in the <tt>DataObject</tt>s of the
	 *            <tt>DataSource</tt> that this binding should provide / edit.
	 * @param editable Per key editability control.
	 * @return See above.
	 */
	public static GPBinding edit(DataSource ds, String[] keys, boolean[] editable){
		if(keys == null || keys.length == 0)
			throw new IllegalArgumentException("At least one key is required");
		
		return new GPBinding(ds, keys, editable, editable);
	}
	
	/**
	 * Instantiates and returns a <tt>GPBinding</tt> with the given
	 * <tt>DataSource</tt> and binding <tt>key</tt>s. Per key editability for
	 * direct or deferred editing is controlled with the <tt>editable</tt>
	 * parameter; per key editability for deferred adding is controlled with the
	 * <tt>addEditable</tt> parameter.
	 * 
	 * @param ds The <tt>DataSource</tt> that provides <tt>DataObject</tt>s to
	 *            this binding.
	 * @param keys The keys of properties (in the <tt>DataObject</tt>s of the
	 *            <tt>DataSource</tt> that this binding should provide / edit.
	 * @param editable Per key editability control for direct or deferred
	 *            editing.
	 * @param addEditable Per key editability control for deferred adding.
	 * @return See above.
	 */
	public static GPBinding edit(AddEditGroup ds, String[] keys,
			boolean[] editable, boolean[] addEditable){
		if(keys == null || keys.length == 0)
			throw new IllegalArgumentException("At least one key is required");

		return new GPBinding(ds, keys, editable, addEditable);
	}
	
	
	/*
	 * Maps containing editability keys for editing and adding, will be queried
	 * for editability if the GPBinding is being backed by an AddEditGroup.
	 */
	private final Map<String, Boolean>			
			editEditability	= new HashMap<String, Boolean>(),
			addEditability;
	
	/**
	 * Creates a <tt>GPBinding</tt> that will autodetect the type of the given
	 * <tt>DataSource</tt>, and will be provide editability if it is an
	 * instance of <tt>AddEditBinding</tt>. To use the <tt>GPBinding</tt>
	 * with an <tt>AddEditGroup</tt> have it not provide editability, use a
	 * constructor that accepts <tt>boolean</tt> editability flag(s).
	 * 
	 * @param ds The <tt>DataSource</tt> backing this binding.
	 * @param keys The key(s) of this binding.
	 */
	@Deprecated
	public GPBinding(DataSource ds, String... keys){
		super(ds, keys, _DEFAULT_VH);
		
		if(ds instanceof AddEditGroup)
			for(String key : keys) editEditability.put(key, true);
		addEditability = null;
	}
	
	/**
	 * Creates a <tt>GPBinding</tt> for a single key whose editability is
	 * controlled with the <tt>editable</tt> parameter..
	 * 
	 * @param ds The <tt>DataSource</tt> backing this binding.
	 * @param key The key of this binding.
	 * @param editable If the binding should be editable for the given
	 *            <tt>key</tt>.
	 */
	@Deprecated
	public GPBinding(DataSource ds, String key, boolean editable){
		super(ds, new String[]{key}, _DEFAULT_VH);
		editEditability.put(key, editable);
		addEditability = null;
	}
	
	/**
	 * Creates a <tt>GPBinding</tt> that will be usable viewing/editing and
	 * adding of data.
	 * 
	 * @param ds The <tt>DataSource</tt> backing this binding.
	 * @param keys The keys of this binding.
	 * @param editable An indicator of editability for both editing and adding
	 *            of new records; if <tt>null</tt> all the keys will be presumed
	 *            editable; if insufficient length the keys for which info is
	 *            lacking will be presumed editable.
	 * @param addEditable An indicator of editability for the adding of new
	 *            records; if <tt>null</tt> all the keys will be presumed
	 *            editable; if insufficient length the keys for which info is
	 *            lacking will be presumed editable.
	 */
	@Deprecated
	public GPBinding(DataSource ds, String[] keys, boolean[] editable, boolean[] addEditable){
		super(ds, keys, _DEFAULT_VH);
		
		// set editability
		for(int i = 0 ; i < keys.length ; i++)
			editEditability.put(keys[i], editable[i]);
		
		// set add editability
		addEditability = new HashMap<String, Boolean>();
		for(int i = 0 ; i < keys.length ; i++)
			addEditability.put(keys[i], addEditable[i]);
	}
	
	/**
	 * Creates a <tt>GPBinding</tt> that will be usable viewing/editing and
	 * adding of data.
	 * 
	 * @param ds The <tt>DataSource</tt> backing this binding.
	 * @param keys The keys of this binding.
	 * @param editable An indicator of editability for both editing and adding
	 *            of new records; if <tt>null</tt> all the keys will be
	 *            presumed editable; if insufficient length the keys for
	 *            which info is lacking will be presumed editable.
	 */
	@Deprecated
	public GPBinding(AddEditGroup ds, String[] keys, boolean[] editable){
		this(ds, keys, editable, editable);
	}
	
	/**
	 * Creates a <tt>GPBinding</tt> that will be usable viewing/editing and
	 * adding of data.
	 * 
	 * @param ds The <tt>DataSource</tt> backing this binding.
	 * @param key The key of this binding.
	 * @param editable An indicator of editability for both editing and adding
	 *            of new records for the given key.
	 */
	@Deprecated
	public GPBinding(AddEditGroup ds, String key, boolean editable){
		this(ds, new String[]{key}, new boolean[]{editable});
	}

	
	/**
	 * Creates a <tt>GPBinding</tt> that will be usable viewing/editing and
	 * adding of data, and that allows you to define separate editability rules
	 * for editing and adding..
	 * 
	 * @param ds The <tt>DataSource</tt> backing this binding.
	 * @param keys The keys of this binding.
	 * @param editable An indicator of editability for editing of existing
	 *            records; if <tt>null</tt> all the keys will be presumed
	 *            editable; if insufficient length the keys for which info is
	 *            lacking will be presumed editable.
	 * @param addEditable An indicator of editability for the adding of new
	 *            records; if <tt>null</tt> all the keys will be presumed
	 *            editable; if insufficient length the keys for which info is
	 *            lacking will be presumed editable.
	 */
	@Deprecated
	public GPBinding(AddEditGroup ds, String[] keys, boolean[] editable, boolean[] addEditable){
		super(ds, keys, _DEFAULT_VH);
		
		//	set editability
		for(int i = 0 ; i < keys.length ; i++){
			boolean edit = editable == null || (editable.length <= i) || editable[i];
			editEditability.put(keys[i], edit);
		}
		
		//	set add editability
		addEditability = new HashMap<String, Boolean>();
		for(int i = 0 ; i < keys.length ; i++){
			boolean edit = addEditable == null || (addEditable.length <= i) || addEditable[i];
			addEditability.put(keys[i], edit);
		}
	}
	
	/**
	 * Creates a <tt>GPBinding</tt> that will be usable viewing/editing and
	 * adding of data, and that allows you to define separate editability rules
	 * for editing and adding..
	 * 
	 * @param ds The <tt>DataSource</tt> backing this binding.
	 * @param key The keys of this binding.
	 * @param editable An indicator of editability for editing of records for
	 *            the given key.
	 * @param addEditable An indicator of editability for the adding of new
	 *            records for the given key.
	 */
	@Deprecated
	public GPBinding(AddEditGroup ds, String key, boolean editable, boolean addEditable){
		this(ds, new String[]{key}, new boolean[]{editable}, new boolean[]{addEditable});
	}
	
	
	/**
	 * Implementation of <tt>DataSourceListener</tt>, just re-fires the same type
	 * of event, with the same indices and data.
	 * 
	 * @param event	The event fired by the <tt>DataSource</tt> backing this <tt>Binding</tt>.
	 */
	public void dataAdded(DataSourceEvent event){
		fireDataAdded(event.getIndex0(), event.getIndex1(), event.getData());
	}

	/**
	 * Implementation of <tt>DataSourceListener</tt>, just re-fires the same type
	 * of event, with the same indices and data.
	 * 
	 * @param event	The event fired by the <tt>DataSource</tt> backing this <tt>Binding</tt>.
	 */
	public void dataChanged(DataSourceEvent event){
		fireDataChanged(event.getIndex0(), event.getIndex1(), event.getData());
	}

	/**
	 * Implementation of <tt>DataSourceListener</tt>, just re-fires the same type
	 * of event, with the same indices and data.
	 * 
	 * @param event	The event fired by the <tt>DataSource</tt> backing this <tt>Binding</tt>.
	 */
	public void dataRemoved(DataSourceEvent event){
		fireDataRemoved(event.getIndex0(), event.getIndex1(), event.getData());
	}

	/**
	 * Attempts to edit the <tt>DataObject</tt> at the given index. If the
	 * <tt>DataSource</tt> of this <tt>GPBinding</tt> is an instance of
	 * <tt>AddEditGroup</tt>, and that groups is set to perform deferred
	 * editing, this method will 'do the right thing' and perform a deferred
	 * edit.
	 * <p>
	 * The current <tt>ValidationHandler</tt> is used to handle the validation
	 * exception, if thrown, both if the editing is deferred and if it is not.
	 * The default validation handler will swallow the exception if it was
	 * thrown by a <tt>CachingDataObject</tt>, and the method will return
	 * <tt>true</tt>, based on the assumption that validation on the deferred
	 * caches will be performed manually later on. If the exception was not
	 * thrown by a <tt>CachingDataObject</tt>, it will be simply re-thrown.
	 * <p>
	 * The further validate and commit deferred caches, if this binding is used
	 * for deferred adding / editing, use methods in the <tt>AddEditGroup</tt>
	 * it belongs to.
	 * 
	 * @param index The index of the <tt>DataObject</tt> to be edited.
	 * @param key The key(path) for which the <tt>DataObject</tt> should be
	 *            edited.
	 * @param value The new value to be set.
	 * @throws ValidationException See above.
	 * @see AddEditGroup#validateForCommit()
	 * @see AddEditGroup#commit(List)
	 * @see AddEditGroup.CachingDataObject
	 */
	public void edit(int index, String key, Object value){
		
		//	automatic value conversion
		value = ValueConverter.toClass(
				value, JBNDUtil.classForProperty(get(index).getDataType(), key));
		
		//	it could be that the AddEditGroup is not the immediate DataSource
		//	of this Binding, search for it
		AddEditGroup addEditGroup = null;
		int indexInAddEditGroup = -1;
		DataSource ds = dataSource;
		do{
			if(ds instanceof AddEditGroup){
				addEditGroup = (AddEditGroup)ds;
				indexInAddEditGroup = JBNDUtil.indexOf(get(index), addEditGroup);
			}
		}while(ds instanceof ProxyingDataSource && addEditGroup == null);
		
		//	if or not the DataObject to be edited should be obtained
		//	by insisting on a deferred edit cache
		boolean deferredEditing = 
			addEditGroup != null && 
			addEditGroup.getDataSource() != null && 
			addEditGroup.getDeferredEditing() && 
			indexInAddEditGroup < addEditGroup.getDataSource().size();
		
		//	the DataObject that will be edited, could be persistent DataObject,
		//	a deferred edit cache, or a deferred add cache
		DataObject toEdit = deferredEditing ? addEditGroup.getEditedDataObject(indexInAddEditGroup)
				: get(index);

		// validate the value
		boolean setValue = true;
		try{
			value = toEdit.validate(value, key);
		}catch(ValidationException ex){
			setValue = getValidationHandler().handleException(ex);
		}

		// set the value
		if(setValue) toEdit.set(value, key);
	}

	public DataObject get(int index){
		return dataSource.get(index);
	}

	/**
	 * Determines editability based on the rules defined through the
	 * constructors.
	 * 
	 * @param index The index of the <tt>DataObject</tt> of this binding that
	 *            is to be edited.
	 * @param key The key for which it is to be edited.
	 * @return If or not the <tt>DataObject</tt> at the given index is
	 *         editable for the given key through this binding.
	 */
	public boolean isEditable(int index, String key){
		
		boolean adding = (get(index) instanceof CachingDataObject) &&
			!((CachingDataObject)get(index)).usedForEditing();
		
		//	adding
		if(adding && addEditability != null) return addEditability.get(key);
		
		//	editing, or adding without custom adding editability rules set
		return editEditability.containsKey(key) && editEditability.get(key);
		
	}
	
	/**
	 * The default validation handler for a <tt>GPBinding</tt>.
	 * 
	 * @version 1.0 Jun 17, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	private static class DefaultValidationHandler implements ValidationHandler{

		/**
		 * This method will behave differently depending on the source of the
		 * exception. If the exception was thrown by a
		 * <tt>CachingDataObject</tt>, it will be swallowed, and the method
		 * will return <tt>true</tt>, based on the assumption that validation
		 * on the deferred caches will be performed manually later on. If the
		 * exception was not thrown by a <tt>CachingDataObject</tt>, it will
		 * be simply re-thrown.
		 * 
		 * @param exception The original validation exception.
		 * @return See above.
		 * @throws ValidationException See above.
		 */
		public boolean handleException(ValidationException exception)
				throws ValidationException{
			
			if(exception.getSource() instanceof CachingDataObject)
				return true;
			else throw exception;
			
		}
		
	}
}