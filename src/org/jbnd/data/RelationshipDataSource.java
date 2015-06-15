package org.jbnd.data;

import java.util.List;

import org.jbnd.DataObject;
import org.jbnd.binding.Binding;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.event.DataSourceListener;


/**
 * A <tt>DataSource</tt> that provides <tt>DataObject</tt>s retrieved from
 * relationships of other <tt>DataObject</tt>s. The <tt>DataObject</tt>s in
 * which the related objects are proxied are found in the <tt>Binding</tt>
 * provided to the <tt>RelationshipDataSource</tt>. This <tt>DataSource</tt>
 * will contain all the <tt>DataObject</tt>s related to the ones in the
 * <tt>Binding</tt> for all the relationship keys that the <tt>Binding</tt>
 * provides.
 * <p>
 * This implementation is simple, and might not be suitable for complicated
 * scenarios. The drawbacks are these: no check is made if a single related
 * <tt>DataObject</tt> is contained more then once in this <tt>DataSource</tt>.
 * If two <tt>DataObject</tt>s in the <tt>Binding</tt> relate to the same
 * <tt>DataObject</tt>, that <tt>DataObject</tt> will be contained twice in this
 * <tt>DataSource</tt>. Next, the reaction of this <tt>DataSource</tt> to any
 * event fired from it's <tt>Binding</tt> is to perform a complete refreshing of
 * the contained <tt>DataObject</tt>s. Meaning, all of them are removed, and
 * then the <tt>Binding</tt> is queried for the current ones, and they are
 * contained. These two issues might make this implementation inadequate for
 * certain scenarios.
 * 
 * @version 1.0 Mar 18, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public final class RelationshipDataSource extends AbstractListDataSource
		implements DataSourceListener{
	
	//	the binding this relationship data source gets objects from
	private final Binding binding;
	
	/**
	 * Creates a <tt>RelationshipDataSource</tt> bound to the given
	 * <tt>Binding</tt>.
	 * 
	 * @param binding The <tt>Binding</tt> to bind to :)
	 */
	public RelationshipDataSource(Binding binding){
		binding.addDataSourceListener(this);
		this.binding = binding;
		refresh();
	}

	public void dataAdded(DataSourceEvent event){
		refresh();
	}

	public void dataChanged(DataSourceEvent event){
		refresh();
	}

	public void dataRemoved(DataSourceEvent event){
		refresh();
	}
	
	private void refresh(){
		super.clear();
		if(binding == null)  return;
		
		String[] keys = binding.getKeys();
		int size = binding.size();
		
		//	iterate through Binding provided DataObject
		for(int i = 0 ; i < size ; i++){
			DataObject object = binding.get(i);
			
			//	iterate through Binding keys
			//	and add all it finds
			for(String key : keys){
				Object value = object.get(key);
				if(value instanceof DataObject)
					super.add((DataObject)value);
				else if(value instanceof List<?>){
					@SuppressWarnings("unchecked")
					List<DataObject> listValue = (List<DataObject>)value;
					super.addAll(listValue);
				}
			}
		}
	}
}