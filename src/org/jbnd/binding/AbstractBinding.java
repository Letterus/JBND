package org.jbnd.binding;

import java.util.LinkedList;
import java.util.List;

import org.jbnd.DataObject;
import org.jbnd.data.AbstractDataSource;
import org.jbnd.data.DataSource;
import org.jbnd.paths.DataSourceKeyPathChangeManager;
import org.jbnd.paths.KeyPathChangeManager;
import org.jbnd.paths.PathChangeMonitor;
import org.jbnd.support.JBNDUtil;


/**
 * Provides a base to implement concrete binding classes upon. Takes care of the
 * following functionalities:
 * <p>
 * The adding / removing of <tt>DataSourceListener</tt>s, and
 * <tt>DataSourceEvent</tt> firing (from extending <tt>AbstractDataSource</tt>).
 * <p>
 * Key and key path handling. Even though a binding does not listen for
 * <tt>DataObjectEvent</tt>s, it does utilize
 * {@link DataSourceKeyPathChangeManager} objects to listen for changes of
 * <tt>DataObject</tt>s at all key paths present in the binding. This, in
 * combination with the <tt>DataSource</tt> guarantee of notifying listeners of
 * changes in <tt>DataObject</tt>s it contains, ensures that relevant changes in
 * the object graph will always be detected and appropriate events fired by the
 * binding.
 * <p>
 * <tt>ValidationHandler</tt> setting and getting.
 * <p>
 * Other functionalities of an <tt>Binding</tt> (getting the data, editing,
 * responding to events coming from the backing data source) are left for the
 * concrete subclass to implement, as they may vary substantially depending on
 * the type of binding. For exact descriptions check subclass documentation.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, Sep 25, 2007
 */
public abstract class AbstractBinding extends AbstractDataSource implements
		Binding, PathChangeMonitor{

	//	the key path managers monitoring changes on key paths, if there are any
	private List<DataSourceKeyPathChangeManager> keyPathManagers;
	
	/** The <tt>DataSource</tt> backing this binding */
	protected final DataSource dataSource;

	/** The keys of this binding */
	private final String[] keys;

	/** The validation handler used by this binding */
	private ValidationHandler validationHandler;
	
	/**
	 * Creates a new <tt>AbstractBinding</tt> that utilizes the given data
	 * source, keys and validation handler.
	 * 
	 * @param dataSource The <tt>DataSource</tt> to proxy <tt>DataObject</tt>s
	 *            from.
	 * @param keys The keys this <tt>Binding</tt> uses to retrieve data from
	 *            <tt>DataObject</tt>s.
	 * @param validationHandler The validation handler to be used by this
	 *            binding.
	 */
	protected AbstractBinding(DataSource dataSource, String[] keys, 
			ValidationHandler validationHandler){
		
		setValidationHandler(validationHandler);
		
		//	set the data source
		dataSource.addDataSourceListener(this);
		this.dataSource = dataSource;
		
		//	set the keys
		if(keys == null || keys.length == 0)
			throw new IllegalArgumentException("Bindings must have at least one key");
		this.keys = keys;
		
		
		
		/*
		 * 
		 *  KeyPathChangeManager (KPCM) initialization
		 *  
		 */
		
		// iterate through the keys, see if any need key path management
		for(String key : keys){
			if(KeyPathChangeManager.testPath(key)){

				// there is a key path that needs listening to, create a KPCM for it
				if(keyPathManagers == null)
					keyPathManagers = new LinkedList<DataSourceKeyPathChangeManager>();
				keyPathManagers.add(new DataSourceKeyPathChangeManager(key, this));
			}
		}

		// if there are KPCMs, then initialize them
		if(keyPathManagers != null && keyPathManagers.size() > 0)
			// set the key path change managers
			for(DataSourceKeyPathChangeManager manager : keyPathManagers)
				manager.setDataSource(this);
	}

	/**
	 * Returns the keys (possibly key paths) used in this binding.
	 * 
	 * @return	Keys (possibly key paths) used in this binding.
	 */
	public String[] getKeys(){
		return keys;
	}
	
	/**
	 * Gets the <tt>ValidationHandler</tt> used by the binding to handle
	 * exceptions caused by attempted setting of invalid values.
	 * 
	 * @return The validation handler used by this binding.
	 */
	public final ValidationHandler getValidationHandler(){
		return this.validationHandler;
	}
	
	/**
	 * Sets the <tt>ValidationHandler</tt> to be used by the binding to handle
	 * exceptions caused by attempted setting of invalid values.
	 * 
	 * @param handler The validation handler to be used by this binding.
	 * @throws NullPointerException If the given <tt>handler</tt> is
	 *             <tt>null</tt>.
	 */
	public final void setValidationHandler(ValidationHandler handler){
		if(handler == null)
			throw new NullPointerException("A null ValidationHandler is not allowed");
		this.validationHandler = handler;
	}

	/**
	 * Returns the same size as the backing <tt>DataSource</tt>.
	 * 
	 * @return See above.
	 */
	public final int size(){
		return dataSource.size();
	}
	
	/**
	 * Implementation of <tt>PathChangeMonitor</tt>, called when a change on the
	 * given path has been detected in a <tt>DataObject</tt> contained in this
	 * binding. Fires a <tt>DataSourceEvent</tt> of the
	 * <tt>DataSourceEvent.DATA_CHANGED</tt> type with the range covering the
	 * <tt>DataObject</tt> that changed.
	 * 
	 * @param object The <tt>DataObject</tt> down whose path a change has
	 *            occured.
	 * @param path The path of the change.
	 */
	public final void pathChanged(DataObject object, String path){
		//	get the index of the changed DataObject
		int index = JBNDUtil.indexOf(object, dataSource);
		fireDataChanged(index, index);
	}
}