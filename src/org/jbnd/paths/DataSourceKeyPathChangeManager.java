package org.jbnd.paths;

import java.util.Collection;

import org.jbnd.DataObject;
import org.jbnd.data.DataSource;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.event.DataSourceListener;


/**
 * A <tt>KeyPathChangeManager</tt> specialized in managing <tt>DataObject</tt>s
 * found in the given <tt>DataSource</tt>; keeps in synch with the data source.
 * After setting the <tt>DataSource</tt> there is nothing else to do but monitor
 * the key path changes.
 * <p>
 * All the methods for adding / removing <tt>DataObject</tt>s directly have been
 * overridden in this class to do nothing, to ensure there is no way to get this
 * manager out of sync with it's data source.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.1, Dec 12, 2007
 */
public class DataSourceKeyPathChangeManager extends KeyPathChangeManager 
		implements DataSourceListener{

	//	the data source whose <tt>DataObject</tt>s are managed
	private DataSource dataSource;
	
	/**
	 * Creates a new <tt>DataSourceKeyPathChangeManager</tt> that will notify
	 * the given <tt>monitor</tt> when <tt>DataObject</tt>s (provided through
	 * the <tt>DataSource</tt>) have changed somewhere along the given <tt>path</tt>.
	 * The <tt>path</tt> will be cleared up of all non standard keys through the
	 * <tt>JBNDUtil.cleanPath(String)</tt> method.<p>
	 * 
	 * This constructor will throw if the given <tt>path</tt>, after being cleared
	 * up of non standard keys, does not contain more then one key. To avoid this,
	 * use the <tt>testPath(String)</tt> method prior to attempting to construct a
	 * <tt>DataSourceKeyPathChangeManager</tt>, to see if it is possible.
	 * 
	 * @param path		See above.
	 * @param monitor	See above.
	 * @see	KeyPathChangeManager#testPath(String)
	 * @throws	IllegalArgumentException If the given <tt>path</tt>, after being
	 * 			cleared up of non standard keys, does not contain more then one key.
	 */
	public DataSourceKeyPathChangeManager(String path, PathChangeMonitor monitor){
		super(path, monitor);
	}

	/**
	 * Gets the data source this whose <tt>DataObject</tt>s this
	 * <tt>DataSourceKeyPathChangeManager</tt> is managing.
	 * 
	 * @return	The data source this whose <tt>DataObject</tt>s this
	 * 			<tt>DataSourceKeyPathChangeManager</tt> is managing.
	 */
	public DataSource getDataSource(){
		return dataSource;
	}

	/**
	 * Sets the data source this whose <tt>DataObject</tt>s this
	 * <tt>DataSourceKeyPathChangeManager</tt> is managing.
	 * 
	 * param dataSource	The data source this whose <tt>DataObject</tt>s
	 * 					this <tt>DataSourceKeyPathChangeManager</tt> is managing.
	 */
	public void setDataSource(DataSource dataSource){
		if(this.dataSource == dataSource)
			return;
		
		//	stop listening to the old data source
		if(this.dataSource != null)
			this.dataSource.removeDataSourceListener(this);
		
		//	set the data source
		this.dataSource = dataSource;
		dataSource.addDataSourceListener(this);
		refresh();
	}
	
	/**
	 * Refreshes the list of managed <tt>DataObject</tt>s, removes
	 * all currently managed <tt>DataObject</tt>s and thereafter adds
	 * all the <tt>DataObject</tt>s found in the managed <tt>dataSource</tt>.
	 */
	private void refresh(){
		//	remove all currently managed DataObjects
		super.clear();
		
		if(dataSource == null)
			return;
		
		int size = dataSource.size();
		for(int i = 0 ; i < size ; i++)
			super.add(dataSource.get(i));
	}

	/**
	 * <tt>DataSourceListener</tt> implementation, takes care of keeping this
	 * manager in sync with the <tt>DataSource</tt> (makes sure it manages
	 * all <tt>DataObject</tt>s currently in the data source, and nothing else).
	 * 
	 * @param	event	The event fired by the data source of this manager.
	 */
	public void dataAdded(DataSourceEvent event){
		//	add the <tt>DataObject</tt>s added to the data source to be managed by this
		int end = event.getIndex1();
		for(int i = event.getIndex0() ; i <= end ; i++)
			super.add(i, dataSource.get(i));
		
	}

	/**
	 * <tt>DataSourceListener</tt> implementation, takes care of keeping this
	 * manager in sync with the <tt>DataSource</tt> (makes sure it manages
	 * all <tt>DataObject</tt>s currently in the data source, and nothing else).
	 * 
	 * @param	event	The event fired by the data source of this manager.
	 */
	public void dataChanged(DataSourceEvent event){
		//	can safely ignore this
		
	}

	/**
	 * <tt>DataSourceListener</tt> implementation, takes care of keeping this
	 * manager in sync with the <tt>DataSource</tt> (makes sure it manages
	 * all <tt>DataObject</tt>s currently in the data source, and nothing else).
	 * 
	 * @param	event	The event fired by the data source of this manager.
	 */
	public void dataRemoved(DataSourceEvent event){
		//	remove the range of <tt>DataObject</tt>s that were removed from the data source
		super.remove(event.getIndex0(), event.getIndex1() + 1);
		
	}

	/**
	 * Overridden to do nothing.
	 * 
	 * @param object	The <tt>DataObject</tt> to manage.
	 */
	public void add(DataObject object){}
	
	/**
	 * Overridden to do nothing.
	 * 
	 * @param index	The location where to insert the <tt>DataObject</tt>, must be between 0 and size (both inclusive).
	 * @param object	The <tt>DataObject</tt> to manage.
	 */
	public void add(int index, DataObject object){}
	
	/**
	 * Overridden to do nothing.
	 * 
	 * @param collection	A collection of <tt>DataObject</tt>s.
	 */
	public void addAll(Collection<? extends DataObject> collection){}
	
	/**
	 * Overriden to do nothing.
	 * 
	 * @param index	The location where to insert the <tt>DataObject</tt>s, must be between 0 and size (both inclusive).
	 * @param collection	A collection of <tt>DataObject</tt>s.
	 */
	public void addAll(int index, Collection<? extends DataObject> collection){}
	
	/**
	 * Overriden to do nothing.
	 * 
	 * @param object	The <tt>DataObject</tt> to stop monitoring.
	 * @return	<tt>false</tt>
	 */
	public boolean remove(DataObject object){ return false;}
	
	/**
	 * Overriden to do nothing.
	 * 
	 * @param index	The index of the <tt>DataObject</tt> to remove, must be between 0 and size (both inclusive).
	 */
	public void remove(int index){}
	
	/**
	 * Overriden to do nothing.
	 * 
	 * @param index0	Start of the range, inclusive.
	 * @param index1	End of the range, exclusive.
	 */
	public void remove(int index0, int index1){}
	
	/**
	 * Overriden to do nothing.
	 *
	 */
	public void clear(){}
}
