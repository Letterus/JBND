package org.jbnd.data;

import java.util.*;

import org.jbnd.DataObject;


/**
 * A <tt>DataSource</tt> implementation that is defined more or less as a
 * <tt>java.util.List</tt>; the basic <tt>DataObject</tt> container. The
 * interface of this class is made to reflect the one of <tt>List</tt>, and all
 * methods common to both classes behave identically. Internally backed by a
 * <tt>List</tt>, this data source take care of listening for changes of
 * contained <tt>DataObject</tt>s, and forwarding their events as
 * <tt>DataSourceEvent</tt>s.
 * <p>
 * <b>Iterator:</b> This class provides <tt>Iterator</tt>s and
 * <tt>ListIterator</tt>s that support all the methods defined in those
 * interfaces. The <tt>Iterator</tt>s are implemented to fail-fast on all of
 * their methods if a disproportion between the current size of the
 * <tt>ListDataSource</tt> and the size the <tt>Iterator</tt> expects is
 * detected.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.1, Dec 11, 2007
 */
public class ListDataSource extends AbstractListDataSource{

	/**
	 * Creates a new <tt>AbstractListDataSource</tt>.
	 */
	public ListDataSource(){};
	
	/**
	 * Creates a new <tt>AbstractListDataSource</tt> with the given initial capacity of
	 * the backing <tt>List</tt>.
	 * 
	 * @param capacity	The initial capacity of the backing <tt>List</tt>.
	 */
	public ListDataSource(int capacity){
		super(capacity);
	}

	/**
	 * Creates a new <tt>AbstractListDataSource</tt> from an already existing <tt>List</tt>
	 * of <tt>DataObject</tt>s. If the given <tt>List</tt> is an <tt>ArrayList</tt>,
	 * then that object is used as a backing container, otherwise a new <tt>ArrayList</tt>
	 * is created from the given data.
	 * 
	 * @param data	See above.
	 */
	
	public ListDataSource(List<DataObject> data){
		super(data);
	}


	public void add(DataObject object){
		super.add(object);
	}

	public void add(int index, DataObject object){
		super.add(index, object);
	}

	public void addAll(DataObject[] objects){
		super.addAll(objects);
	}

	public void addAll(Collection<? extends DataObject> collection){
		super.addAll(collection);
	}

	public void addAll(int index, DataObject[] objects){
		super.addAll(index, objects);
	}

	public void addAll(int index, Collection<? extends DataObject> collection){
		super.addAll(index, collection);
	}

	public void clear(){
		super.clear();
	}

	public boolean remove(DataObject object){
		return super.remove(object);
	}

	public DataObject remove(int index){
		return super.remove(index);
	}

	public boolean removeAll(Collection<? extends DataObject> collection){
		return super.removeAll(collection);
	}

	public void removeRange(int index0, int index1){
		super.removeRange(index0, index1);
	}
	
	public void set(int index, DataObject object){
		remove(index);
		add(index, object);
	}
	
	/**
	 * Returns <tt>true</tt> if this data source contains the given <tt>DataObject</tt>.
	 * 
	 * @param object	The <tt>DataObject</tt> in question.
	 * @return		True of the <tt>DataObject</tt> in question is contained in this data source.
	 */
	public boolean contains(DataObject object){
		return indexOf(object) != -1;
	}
	
	/**
	 * Returns <tt>true</tt> if this data source contains all the <tt>DataObject</tt>s in the
	 * given <tt>Collection</tt>.
	 * 
	 * @param collection	A <tt>Collection</tt> of <tt>DataObject</tt>s.
	 * @return	<tt>true</tt> if all the <tt>DataObject</tt>s from the container
	 * 			are contained in this data source.
	 */
	public boolean containsAll(Collection<? extends DataObject> collection){
		for(DataObject o : collection)
			if(indexOf(o) == -1) return false;
		
		return true;
	}
	
	/**
	 * Returns an <tt>Iterator</tt> for this data source.
	 * 
	 * @return	An <tt>Iterator</tt>.
	 */
	public Iterator<DataObject> iterator(){
		return listIterator(0);
	}	
	
	/**
	 * Returns a <tt>ListIterator</tt> for this data source.
	 * 
	 * @return	A <tt>ListIterator</tt>.
	 */
	public ListIterator<DataObject> listIterator(){
		return listIterator(0);
	}
	
	/**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list.  The
     * specified index indicates the first element that would be returned by
     * an initial call to the <tt>next</tt> method.  An initial call to
     * the <tt>previous</tt> method would return the element with the
     * specified index minus one.
     *
     * @param index Index of first element to be returned from the
     *		    list iterator (by a call to the <tt>next()</tt> method).
     * @return A list iterator of the elements in this list (in proper
     * 	       sequence), starting at the specified position in this list.
     * @throws IndexOutOfBoundsException If the index is out of range (index
     *         &lt; 0 || index &gt; size()).
     */
	public ListIterator<DataObject> listIterator(int index){
		if (index<0 || index>size())
			throw new IndexOutOfBoundsException("Index: "+index);

			return new DSIterator(index);
	}
	
	private class DSIterator implements ListIterator<DataObject>{
		
		//	track the size of the ListDataSource to be able to
		//	detect concurrent modifications
		private int size = 0;
		
		//	the position of iteration, the cursor falls in between of
		//	the object at the index that corresponds to the cursor value
		//	and the one before it
		private int cursor = 0;
		
		//	the index of the element that was last returned by next() or previous()
		//	used by data modifying methods
		private int lastReturned = -1;
		
		public DSIterator(int index){
			
			size = size();
			cursor = index;
			if(index > size || index < 0) throw new IndexOutOfBoundsException("Index: "+index);
		}

		public void add(DataObject object){
			checkForConcurrentMod();
			
			ListDataSource.this.add(cursor, object);
			
			cursor++;
			size++;
			lastReturned = -1;
		}

		public boolean hasNext(){
			checkForConcurrentMod();
			
			return cursor < size;
		}

		public boolean hasPrevious(){
			checkForConcurrentMod();
			
			return cursor > 0;
		}

		public DataObject next(){
			checkForConcurrentMod();
			
			if(cursor >= size)
				throw new NoSuchElementException();
			lastReturned = cursor;
			return get(cursor++);
		}

		public int nextIndex(){
			checkForConcurrentMod();
			
			return cursor >= size ? size : cursor;
		}

		public DataObject previous(){
			checkForConcurrentMod();
			
			if(cursor < 1)
				throw new NoSuchElementException();
			lastReturned = --cursor;
			return get(cursor);
		}

		public int previousIndex(){
			checkForConcurrentMod();
			
			return cursor - 1;
		}

		public void remove(){
			checkForConcurrentMod();
			
			if(lastReturned == -1)
				throw new IllegalStateException(
				"Can call remove() only directly after a call to next() or previos()");
			
			//	remove from the DataSource
			ListDataSource.this.remove(lastReturned);
			lastReturned = -1;
			size--;
		}

		public void set(DataObject object){
			checkForConcurrentMod();
			
			if(lastReturned == -1)
				throw new IllegalStateException(
				"Can call set() only directly after a call to next() or previos()");
			ListDataSource.this.set(lastReturned, object);
		}
		
		private void checkForConcurrentMod(){
			if(size != size())
				throw new ConcurrentModificationException();
		}
	}
}