package org.jbnd;

import java.util.LinkedList;
import java.util.List;

import org.jbnd.event.DataObjectEvent;
import org.jbnd.event.DataObjectListener;
import org.jbnd.event.DataObjectEvent.Type;
import org.jbnd.support.NamingSupport;


/**
 * Provides a skeleton implementation of the <tt>DataObject</tt> interface: the
 * event firing and listener management that is identical for any type of
 * <tt>DataObject</tt>. Can be used as a superclass for a <tt>DataObject</tt>
 * implementation, in cases when it does not need to inherit some other class.
 * 
 * @version 1.0 Feb 28, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public abstract class AbstractDataObject implements DataObject{
	
	//	storage for event listeners
	private List<DataObjectListener> listeners;
	
	/**
	 * Adds the given listener to the list of listeners that should be notified
	 * when <tt>this DataObject</tt> changes. A change is considered the
	 * setting of a value for any of it's properties, that is different then the
	 * one present there before. It is possible that an implementing
	 * <tt>DataObject</tt> will have the capability of storing and modifying
	 * something else as data properties, but the <tt>DataObjectListener</tt>
	 * should never be used to fire events notifying of the changes of those.
	 * 
	 * @param listener The listener to add.
	 */
	public void addDataObjectListener(DataObjectListener listener){
		if(listeners == null) listeners = new LinkedList<DataObjectListener>(); 
		listeners.add(listener);
	}

	/**
	 * Removes the given listener from the list of listeners that should be
	 * notified when <tt>this DataObject</tt> changes.
	 * 
	 * @param listener The listener to remove.
	 * @see #addDataObjectListener(DataObjectListener)
	 */
	public void removeDataObjectListener(DataObjectListener listener){
		if(listeners == null) return;
		listeners.remove(listener);
	}

	/**
	 * This method takes care of instantiating and firing a
	 * <tt>DataObjectEvent</tt> on all registered listeners, should be called
	 * every time the data this <tt>DataObject</tt> contains changes. This
	 * method should be called AFTER the value has been set.
	 * 
	 * @param key The key for which the value changed.
	 * @param relevantValue The value relevant to the change, for more info see
	 *            the {@link DataObjectEvent} API documentation.
	 * @param changeType The type of change, one of the changes defined in
	 *            <tt>DataEvent</tt>.
	 */
	protected void fireDataObjectEvent(String key, Object relevantValue, Type changeType){
		if((listeners == null || listeners.size() == 0)
				&& DataObjectEvent.POST_NOTES == false) return;
		
		//	create an event
		DataObjectEvent e = new DataObjectEvent(this, key, changeType, relevantValue);
		
		//	by now a Note has been posted, if that was necessary
		//	so only fire events if there are listeners
		if(listeners == null) return;
		
		DataObjectListener[] listenersArray = listeners
				.toArray(new DataObjectListener[listeners.size()]);
		for(int i =  listenersArray.length - 1 ; i >= 0 ; i--)
			listenersArray[i].objectChanged(e);
	}
	
	/**
	 * Calls <tt>AbstractDataObject.compareTo(this, object)</tt>.
	 * 
	 * @param object	The object to compare to.
	 * @return	Standard <tt>Comparable</tt> return value.
	 * @see	#compareTo(DataObject, DataObject)
	 */
	public int compareTo(DataObject object){
		return compareTo(this, object);
	}
	
	/**
	 * A method provided for convenient general purpose implementation of
	 * <tt>Comparable</tt> by <tt>DataObject</tt> implementing classes. The
	 * process of comparing is as follows:
	 * <ul>
	 * <li>If objects are equal, return 0.</li>
	 * <li>If their <tt>DataType</tt>s are different, return the compareTo()
	 * result of their names.</li>
	 * <li>If objects are of a different class, return the compareTo() result
	 * of their class names.</li>
	 * <li>If one is persistent and the other is not, return a negative value
	 * if this object is persistent, negative otherwise.</li>
	 * <li>If possible compare the <tt>toString()</tt> values of this and the
	 * given object, if result is not 0 return it.</li>
	 * <li>Compare all the attributes of both <tt>DataObject</tt>s that are
	 * not <tt>null</tt>, if the result is not 0 return it.</li>
	 * <li>Compare the hash code of the two objects, if different, return the
	 * difference.</li>
	 * <li>Compare the System.identityHashCode(...) code of the two objects, if
	 * different, return the difference.</li>
	 * <li>Admit defeat and return 0, thus breaking the consistency with equals</tt>.
	 * 
	 * @param o1 The first comparison <tt>DataObject</tt>.
	 * @param o2 The second comparison <tt>DataObject</tt>.
	 * @return See above.
	 */
	public static int compareTo(DataObject o1, DataObject o2){
		if(o2.equals(o1)) return 0;
		
		//	DataType check
		int rVal = NamingSupport.typeName(o1.getDataType().name()).compareTo(
				NamingSupport.typeName(o2.getDataType().name()));
		if(rVal != 0) return rVal;
		
		//	DataObject implementation check
		if(!o1.getClass().equals(o2.getClass())){
			rVal = o1.getClass().getName().compareTo(o2.getClass().getName());
			if(rVal != 0) return rVal;
		}
		
		//	see if one is saved and the other one is not
		if(o1.isPersistent() && !o2.isPersistent())
			return -100;
		if(!o1.isPersistent() && o2.isPersistent())
			return 100;
		
		//	do a toString comparison
		String thisS = o1.toString();
		String thatS = o2.toString();
		rVal = thisS != null && thatS != null ? thisS.compareTo(thatS) : 0;
		if(rVal != 0) return rVal;

		//	do attribute per attribute comparison
		String[] attributes = o1.getDataType().attributes();
		for(String attribute : attributes){
			Object a1 = o1.get(attribute), a2 = o2.get(attribute);
			if(a1 != null && a2 != null && a1 instanceof Comparable){
				try{
					@SuppressWarnings("unchecked")
					Comparable<Object> a1c = (Comparable<Object>)a1;
					rVal = a1c.compareTo(a2);
					if(rVal != 0) return rVal;
				}catch(ClassCastException e){
					// we made a cast to Comparable, and the value is not comparable
					// ignoring this exception is acceptable
				}
			}
		}

		//	this is just bad, comparing attributes still yields nothing
		rVal = o1.hashCode() - o2.hashCode();
		if(rVal != 0) return rVal;
		
		rVal = System.identityHashCode(o1) - System.identityHashCode(o2);
		if(rVal != 0) return rVal;
		
		//	there's virtually nothing left to compare on!
		return 0;
	}
}