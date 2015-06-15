package org.jbnd.data;

import java.util.LinkedList;
import java.util.List;

import org.jbnd.DataObject;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.event.DataSourceListener;


/**
 * A <tt>DataSource</tt> that contains all the <tt>DataObject</tt>s from
 * multiple other <tt>DataSource</tt>s, use with caution as certain parts of
 * JBND expect all the <tt>DataObject</tt>s in a single <tt>DataSource</tt> to
 * be of the same type. The objects are ordered in the way the
 * <tt>DataSource</tt>s are passed to the constructor: first all the objects
 * from the first <tt>DataSource</tt>, then all the ones from the second one
 * etc...
 * 
 * @version 1.0 Sep 1, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public final class MergingDataSource extends AbstractDataSource implements
		DataSourceListener{
	
	private final List<DataSource> sources = new LinkedList<DataSource>();
	
	/**
	 * Creates a <tt>MergingDataSource</tt> that will 'contain' all the
	 * <tt>DataObject</tt>s from all the given <tt>dataSources</tt>. See class
	 * doc for more info.
	 * 
	 * @param dataSources See above.
	 */
	public MergingDataSource(DataSource... dataSources){
		for(DataSource ds : dataSources){
			ds.addDataSourceListener(this);
			sources.add(ds);
		}
	}

	public DataObject get(int index){
		int done = 0;
		for(DataSource ds : sources){
			int low = done;
			done = low + ds.size();
			if(index < done) return ds.get(index - low);
		}
		
		throw new IndexOutOfBoundsException("Index out of bounds: " + index);
	}

	public int size(){
		
		int rVal = 0;
		for(DataSource ds : sources)
			rVal += ds.size();
		
		return rVal;
	}

	public void dataAdded(DataSourceEvent event){

		int start = fromContainedToThis(event.getIndex0(), (DataSource)event.getSource());
		int end = start + (event.getIndex1() - event.getIndex0());
		fireDataChanged(start, end, event.getData());
		
	}

	public void dataChanged(DataSourceEvent event){
		int start = fromContainedToThis(event.getIndex0(), (DataSource)event.getSource());
		int end = start + (event.getIndex1() - event.getIndex0());
		fireDataChanged(start, end, event.getData());
		
	}

	public void dataRemoved(DataSourceEvent event){
		
		int start = fromContainedToThis(event.getIndex0(), (DataSource)event.getSource());
		int end = start + (event.getIndex1() - event.getIndex0());
		fireDataChanged(start, end, event.getData());
	}
	
	private int fromContainedToThis(int index, DataSource ds){
		int done = 0;
		for(DataSource ds2 : sources){
			if(ds == ds2) return done + index;
			else done += ds2.size();
		}
		
		throw new IllegalStateException();
	}
}