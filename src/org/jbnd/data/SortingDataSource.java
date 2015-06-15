package org.jbnd.data;

import java.util.*;

import org.jbnd.DataObject;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.event.DataSourceListener;


/**
 * A <tt>DataSource</tt> that proxies all the <tt>DataObject</tt>s from another
 * <tt>DataSource</tt>, but in a sorted way. <tt>Comparator</tt>s are used to
 * define the sort ordering of <tt>DataObject</tt>s. An unlimited number of
 * <tt>Comparator</tt>s can be used for sorting.
 * <p>
 * The <tt>DataObjectComparator</tt> class is provided for basic value-based
 * comparison, along with several specialized <tt>Comparators</tt> as well.
 * 
 * @version 2 Jan 09, 2009
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see DataObjectComparator
 * @see INVERSE
 * @see STRING_DESC
 * @see STRING_CASE_INS
 * @see STRING_CASE_INS_DESC
 */
public final class SortingDataSource extends AbstractNotListeningListDataSource
	implements DataSourceListener, ProxyingDataSource{
	
	/*
	 * 
	 * Some statically defined standard issue comparators.
	 * 
	 */
	
	/**
	 * A <tt>Comparator</tt> that can be used to sort <tt>DataObject</tt>s in
	 * exactly the opposite order then the <tt>DataObjectComparator</tt> class.
	 * Should only be used to sort <tt>DataObject</tt>s on keys that have a
	 * natural sort ordering (they implement <tt>Comparable</tt>), such as
	 * numbers, dates and strings. Note that due to some specifics in
	 * <tt>String</tt> comparison, a specialized comparator is normally
	 * preferable.
	 */
	public static class INVERSE extends DataObjectComparator{

		/**
		 * Creates a <tt>Comparator</tt> that will compare <tt>DataObject</tt>s
		 * based on the opposite of the natural ordering of values found in the
		 * <tt>DataObject</tt>s for the given <tt>key</tt>.
		 * 
		 * @param key The key used to retrieve values from the
		 *            <tt>DataObject</tt>s, on which they are then compared.
		 */
		public INVERSE(String key){
			super(key);
		}
		
		public int compare(DataObject object1, DataObject object2){
			
			//	do some null checking and act accordingly
			Object o1 = object1.get(key), o2 = object2.get(key);
			if(o1 == null || o2 == null)
				return o1 == null && o2 == null ? 0 : o1 == null ? 1 : -1;
			
			return -super.compare(object1, object2);
		}
	}
	
	/**
	 * A <tt>Comparator</tt> that compares <tt>DataObject</tt> properties based
	 * on their <tt>toString()</tt> values, but in a descending way.
	 * 
	 * @version 1.0 Feb 5, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public static class STRING_DESC extends DataObjectComparator{
		
		/**
		 * Creates a <tt>Comparator</tt> that compares <tt>DataObject</tt>
		 * properties based on their <tt>toString()</tt> values, but in a
		 * descending way.
		 * 
		 * @param key The key used to retrieve values from the
		 *            <tt>DataObject</tt>s, on which they are then compared.
		 */
		public STRING_DESC(String key){
			super(key);
		}
		
		public int compare(DataObject object1, DataObject object2){
			
			//	extract the values from the data objects
			Object val1 = object1.get(key), val2 = object2.get(key);
			String o1 = val1 == null ? null : val1.toString(),
				o2 = val2 == null ? null : val2.toString();
			
			//	do some null checking and act accordingly
			if(o1 == null) o1 = "";
			if(o2 == null) o2 = "";
			
			//	if they are of different lengths
			//	try comparing them first on the
			//	size of the smaller
			int o1l = o1.length(), o2l = o2.length();
			if(o1l != o2l){
				//	extract strings of matching lengths
				//	and compare that
				int shorter = Math.min(o1l, o2l);
				
				//	if the shorter string is zero length,
				//	return the length difference
				if(shorter == 0) return o1l - o2l;
				
				String o12 = o1.substring(0, shorter - 1);
				String o22 = o2.substring(0, shorter - 1);
				//	compare the shortened strings
				//	and if they are not the same, use that value
				int comparedValue = o12.compareTo(o22);
				if(comparedValue != 0) return -comparedValue;
					
				//	strings are of different lengths
				//	but where comparable they are the same, so:
				return o1l - o2l;
			}else{
				//	the strings are of equal lengths
				return -o1.compareTo(o2);
			}
		}
	}
	
	/**
	 * A <tt>Comparator</tt> that compares <tt>DataObject</tt> properties based
	 * on their <tt>toString()</tt> values, but in a ascending case insensitive way.
	 * 
	 * @version 1.0 Feb 5, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public static class STRING_CASE_INS extends DataObjectComparator{
		
		/**
		 * Creates a <tt>Comparator</tt> that compares <tt>DataObject</tt>
		 * properties based on their <tt>toString()</tt> values, but in a
		 * ascending, case insensitive way.
		 * 
		 * @param key The key used to retrieve values from the
		 *            <tt>DataObject</tt>s, on which they are then compared.
		 */
		public STRING_CASE_INS(String key){
			super(key);
		}
		
		public int compare(DataObject object1, DataObject object2){
			
			// extract the values from the data objects
			Object val1 = object1.get(key), val2 = object2.get(key);
			String o1 = val1 == null ? null : val1.toString(),
				o2 = val2 == null ? null : val2.toString();
			
			//	do some null checking and act accordingly
			if(o1 == null) o1 = "";
			if(o2 == null) o2 = "";
			
			return o1.compareToIgnoreCase(o2);
		}
	}
	
	/**
	 * A <tt>Comparator</tt> that compares anything based on it's
	 * <tt>toString()</tt> value.
	 * 
	 * @version 1.0 Feb 5, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public static class TO_STRING extends DataObjectComparator{
		
		/**
		 * Creates a <tt>Comparator</tt> specialized in comparing the
		 * <tt>String</tt> found for the given <tt>key</tt> of
		 * <tt>DataObject</tt>s, but in a ascending, case insensitive way.
		 * 
		 * @param key The key used to retrieve values from the
		 *            <tt>DataObject</tt>s, on which they are then compared.
		 */
		public TO_STRING(String key){
			super(key);
		}
		
		public int compare(DataObject object1, DataObject object2){
			
			// extract the values from the data objects
			Object val1 = object1.get(key), val2 = object2.get(key);
			String o1 = val1 == null ? null : val1.toString(),
				o2 = val2 == null ? null : val2.toString();
			
			//	do some null checking and act accordingly
			if(o1 == null) o1 = "";
			if(o2 == null) o2 = "";
			
			return o1.compareTo(o2);
		}
	}
	
	/**
	 * A <tt>Comparator</tt> specialized in comparing <tt>String</tt> attributes
	 * of <tt>DataObject</tt>s, but in a descending case insensitive way.
	 * 
	 * @version 1.0 Feb 5, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public static class STRING_CASE_INS_DESC extends DataObjectComparator{
		
		/**
		 * Creates a <tt>Comparator</tt> specialized in comparing the
		 * <tt>String</tt> found for the given <tt>key</tt> of
		 * <tt>DataObject</tt>s, but in a descending, case insensitive way.
		 * 
		 * @param key The key used to retrieve values from the
		 *            <tt>DataObject</tt>s, on which they are then compared.
		 */
		public STRING_CASE_INS_DESC(String key){
			super(key);
		}
		
		public int compare(DataObject object1, DataObject object2){
			
			// extract the values from the data objects
			Object val1 = object1.get(key), val2 = object2.get(key);
			String o1 = val1 == null ? null : val1.toString(),
				o2 = val2 == null ? null : val2.toString();
			
			//	do some null checking and act accordingly
			if(o1 == null) o1 = "";
			if(o2 == null) o2 = "";
			
			//	if they are of different lengths
			//	try comparing them first on the
			//	size of the smaller
			int o1l = o1.length(), o2l = o2.length();
			if(o1l != o2l){
				//	extract strings of matching lengths
				//	and compare that
				int shorter = Math.min(o1l, o2l);
				
				//	if the shorter string is zero length,
				//	return the length difference
				if(shorter == 0) return o1l - o2l;
				
				String o12 = o1.substring(0, shorter - 1);
				String o22 = o2.substring(0, shorter - 1);
				//	compare the shortened strings
				//	and if they are not the same, use that value
				int comparedValue = o12.compareToIgnoreCase(o22);
				if(comparedValue != 0) return -comparedValue;
					
				//	strings are of different lengths
				//	but where comparable they are the same, so:
				return o1l - o2l;
			}else{
				//	the strings are of equal lengths
				return -o1.compareToIgnoreCase(o2);
			}
		}
	}
	
	
	
	/*
	 * 
	 * End of provided comparators, start of the SortingDataSource implementation.
	 * 
	 */
	
	
	
	//	the source of DataObjects that this
	//	data source is sorting
	private DataSource targetDataSource;
	
	//	a list of comparators used to sort the data objects
	private List<Comparator<DataObject>> comparators;
	
	// a comparator that relies on the comparators to do the comparison
	// but exposes that functionality in the standard Comparator manner
	private Comparator<DataObject> comparator = new Comparator<DataObject>(){

		public int compare(DataObject o1, DataObject o2){
			if(comparators == null) throw new IllegalStateException();
			for(Comparator<DataObject> c : comparators){
				int val = c.compare(o1, o2);
				if(val != 0) return val;
			}
			
			return 0;
				
		}};
	
	/**
	 * Creates a new <tt>SoritingDataSource</tt> that will provide all the
	 * <tt>DataObject</tt>s that the given <tt>targetDataSource</tt> provides,
	 * but ordered as defined by the given <tt>comparator</tt>.
	 * 
	 * @param targetDataSource The data source from which the data is proxied.
	 * @param comparators The comparator(s) used to sort the data, <tt>null</tt>
	 *            is allowed.
	 */
	public SortingDataSource(DataSource targetDataSource,
			Comparator<DataObject>... comparators)
	{
		//	hook up the data source
		targetDataSource.addDataSourceListener(this);
		this.targetDataSource = targetDataSource;

		//	set the comparators
		setComparators(comparators);
	}

	/**
	 * Creates a new <tt>SoritingDataSource</tt> that will provide all the
	 * <tt>DataObject</tt>s that the given <tt>targetDataSource</tt> provides,
	 * but ordered as defined by the given <tt>comparators</tt>.
	 * 
	 * @param targetDataSource The data source from which the data is proxied.
	 * @param comparators The comparators used to sort the data.
	 */
	public SortingDataSource(DataSource targetDataSource,
			List<Comparator<DataObject>> comparators)
	{
		//	hook up the data source
		targetDataSource.addDataSourceListener(this);
		this.targetDataSource = targetDataSource;
		
		//	set the comparators
		setComparators(comparators);
	}

	/**
	 * Sets the <tt>Comparator</tt>s used by the <tt>SortingDataSource</tt> to
	 * determine the order of objects it provides. This will trigger a complete
	 * resorting of the data source, and appropriate events will be fired.
	 * 
	 * @param comparators The <tt>Comparator</tt>s to use for ordering the
	 *            <tt>DataObject</tt>s of this <tt>DataSource</tt>.
	 */
	public void setComparators(List<Comparator<DataObject>> comparators){
		this.comparators = comparators == null ? 
				new LinkedList<Comparator<DataObject>>() : comparators;
		_reSort();
	}
	
	/**
	 * Sets the <tt>Comparator</tt>s used by the <tt>SortingDataSource</tt> to
	 * determine the order of objects it provides. This will trigger a complete
	 * resorting of the data source, and appropriate events will be fired.
	 * 
	 * @param comparators The <tt>Comparator</tt>s to use for ordering the
	 *            <tt>DataObject</tt>s of this <tt>DataSource</tt>.
	 */
	public void setComparators(Comparator<DataObject>... comparators){
		
		if(comparators == null){
			setComparators((List<Comparator<DataObject>>)null);
			return;
		}
		
		LinkedList<Comparator<DataObject>> list = new LinkedList<Comparator<DataObject>>();
		for(Comparator<DataObject> c : comparators)
			list.add(c);
		setComparators(list);
	}

	/**
	 * Completely remakes the index translation list, and fires appropriate
	 * events. Provided as support for actions such as setting a different set
	 * of comparators on the <tt>SortingDataSource</tt>.
	 */
	private void _reSort(){
		//	first "remove" all the objects
		clear();
		
		if(comparators.size() == 0){
			// there are no comparators!
			ArrayList<DataObject> toAdd = new ArrayList<DataObject>();
			for(int i = 0, c = targetDataSource.size() ; i < c ; i++)
				toAdd.add(targetDataSource.get(i));
			
			addAll(0, toAdd);
		}else{
			//	there are comparators, we need to sort
			for(int i = 0, c = targetDataSource.size() ; i < c ; i++){
				DataObject inTarget = targetDataSource.get(i);
				add(binarySearch(inTarget), inTarget);
			}
		}
		
		fireDataAdded(0, size() - 1);
	}
	
	/**
	 * Gets the target <tt>DataSource</tt> (the source data is proxied from).
	 * 
	 * @return The <tt>DataSource</tt> the data is proxied from.
	 */
	public DataSource getTargetDataSource(){
		return targetDataSource;
	}

	/**
	 * A <tt>DataSourceListener</tt> method implementation, called when the
	 * <tt>targetDataSource</tt> fires a <tt>DataSourceEvent</tt> implying that
	 * <tt>DataObject</tt>s were added to it, updates <tt>this</tt> to include
	 * <tt>DataObject</tt>s that were added and fires appropriate events.
	 * 
	 * @param event The event fired by the <tt>targetDataSource</tt>.
	 */
	public void dataAdded(DataSourceEvent event){
		
		// if there are no comparators, no index translation is needed
		if(comparators.size() == 0){
			addAll(event.getIndex0(), event.getData());
		}else{
			// there are comparators, we need to see where each added object
			// fits, in the sorted DataSource
			DataObject[] addedObjects = event.getData();

			//	add the objects, one by one
			for(DataObject addedObject : addedObjects){
				add(binarySearch(addedObject), addedObject);
			}
		}
	}

	/**
	 * A <tt>DataSourceListener</tt> method implementation, called when the
	 * <tt>targetDataSource</tt> fires a <tt>DataSourceEvent</tt> implying that
	 * it's contents changed; updates <tt>this</tt> and fires appropriate
	 * events.
	 * 
	 * @param event The event fired by the <tt>targetDataSource</tt>.
	 */
	public void dataChanged(DataSourceEvent event){
		
		if(comparators.size() == 0){
			fireDataChanged(event.getIndex0(), event.getIndex1(), event.getData());
			return;
		}
			
		DataObject[] changedObjects = event.getData();
		
		// remove changed data
		for(DataObject changedObject : changedObjects)
			remove(changedObject);
		
		//	now add changed data in it's new place
		for(DataObject changedObject : changedObjects)
			add(binarySearch(changedObject), changedObject);
		
	}
	
	/**
	 * A <tt>DataSourceListener</tt> method implementation, called when the
	 * <tt>targetDataSource</tt> fires a <tt>DataSourceEvent</tt> implying that
	 * <tt>DataObject</tt>s were removed from it, updates <tt>this</tt> to
	 * remove <tt>DataObject</tt>s that were and fires appropriate events.
	 * 
	 * @param event The event fired by the <tt>targetDataSource</tt>
	 */
	public void dataRemoved(DataSourceEvent event){
		
		if(comparators.size() == 0){
			removeRange(event.getIndex0(), event.getIndex1());
			return;
		}
		
		for(DataObject removedObject : event.getData())
			remove(binarySearch(removedObject));
	}
	
	/**
	 * Searches for the given <tt>DataObject</tt> in this
	 * <tt>SortingDataSource</tt>, and returns it's index; if not present then
	 * the index where it would be is returned.
	 * 
	 * @param object The <tt>DataObject</tt> to look for.
	 * @return Index of the <tt>object</tt>, or the index where it would be (if
	 *         it is not present).
	 */
	private int binarySearch(DataObject object){
		
		if(comparators.size() == 0)
			throw new IllegalStateException(
					"binarySearch(...) called but there are no Compararors");
		
		int binSearchResult = Collections.binarySearch(data, object, comparator);
		if(binSearchResult < 0)
			// not found, translate
			return -(binSearchResult + 1);
		else
			return binSearchResult;
		
		
		// the old implementation, delete if the above proves reliable
		
//		if(comparators.size() == 0)
//			throw new IllegalStateException(
//					"binarySearch(...) called but there are no Compararors");
//		
//		int low = 0;
//		int high = size() - 1;
//		
//		while (low <= high){
//			//	get the object in the middle of the list
//			int mid = (low + high) >> 1;
//			DataObject midVal = get(mid);
//			
//			//	see if the given object compares as being
//			//	before or after the middle object
//			int cmp = 0;
//			Iterator<Comparator<DataObject>> it = comparators.iterator();
//			while(it.hasNext() && cmp == 0)
//				cmp = it.next().compare(midVal, object);
//
//			if (cmp < 0)
//				low = mid + 1;
//			else if (cmp > 0)
//				high = mid - 1;
//			else{
//				for(int i = low ; i <= high ; i++)
//					if(get(i).equals(object)) return i;
//				
//				//	the object is not contained in this DataSource,
//				//	so put at a *most* likely place it should be
//				return mid;
//			}
//		}
//		
//		return low;
	}
	
	/**
	 * A <tt>Comparator</tt> that can be used to compare <tt>DataObject</tt>s
	 * based on the values found in them for a given <tt>key</tt>. If used
	 * without an additional <tt>Comparator</tt> (to compare the values found
	 * for the key), this class will use the natural ordering of found values,
	 * by assuming them to be <tt>Comparable</tt>. It is therefore possible that
	 * a runtime exception will occur if the found values do not implement the
	 * <tt>Comparable</tt> interface. This does not occur if an additional
	 * <tt>Comparator</tt> is provided, for comparing the values.
	 * 
	 * @version 1.0 Feb 5, 2008
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public static class DataObjectComparator implements Comparator<DataObject>{

		//	the key on which to compare DataObjects
		protected String key; 
		
		//	the comparator, if it is null
		//	the objects that need to be compared will be
		//	cast to Comparable
		protected Comparator<Object> comparator;
		
		/**
		 * Creates a <tt>Comparator</tt> that will compare <tt>DataObject</tt>s
		 * based on the natural ordering of values found in the <tt>DataObject</tt>s
		 * for the given <tt>key</tt>. See class documentation for info about the
		 * possible pitfalls of this.
		 * 
		 * @param key	The key used to retrieve values from the <tt>DataObject</tt>s,
		 * 				on which they are then compared.
		 */
		public DataObjectComparator(String key){
			if(key == null)
				throw new IllegalArgumentException(
				"Can not create a DataObjectComparator with a null key");
			
			this.key = key;
		}
		
		/**
		 * Creates a <tt>Comparator</tt> that will compare <tt>DataObject</tt>s
		 * based on the values found in them for the given <tt>key</tt>, and the
		 * given <tt>comparator</tt>.
		 * 
		 * @param key	The key used to retrieve values from the <tt>DataObject</tt>s,
		 * 				on which they are then compared.
		 * @param comparator	The <tt>Comparator</tt> to use for determining
		 * 				how the <tt>DataObject</tt> retrieved values compare to each other.
		 */
		public DataObjectComparator(String key, Comparator<Object> comparator){
			this(key);
			this.comparator = comparator;
		}
		
		public int compare(DataObject object1, DataObject object2){
			//	extract the values from the data objects
			Object o1 = object1.get(key),
				o2 = object2.get(key);
			
			//	if there is a comparator present, use it,
			//	otherwise cast to comparable
			if(comparator != null)
				return comparator.compare(o1, o2);
			
			//	do some null checking and act accordingly
			if(o1 == null){
				return o2 == null ? 0 : 1;
			}else if(o2 == null)
				return -1;
			
			// do some casting, and if that results in an exception,
			// return 0
			try{
				@SuppressWarnings("unchecked")
				Comparable<Object> o1c = (Comparable<Object>)o1;
				return o1c.compareTo(o2);
			}catch(ClassCastException ex){
				return 0;
			}
		}
	}
}