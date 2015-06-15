package org.jbnd.paths;

import java.util.*;

import org.jbnd.DataObject;
import org.jbnd.DataType.PropType;
import org.jbnd.event.DataObjectEvent;
import org.jbnd.event.DataObjectListener;
import org.jbnd.support.JBNDUtil;


/**
 * Provides the means to observe changes in a tree of related
 * <tt>DataObject</tt>s, which is defined by a key path. This enables viewing
 * data found at some <tt>DataObject</tt>s key path, without worrying about
 * undetected changes to the displayed data. For example, if there is a "Parent"
 * object, and somewhere in the application it is desired to display the value
 * found at it's "child.pet.name" key path, there is no way of knowing (without
 * using the <tt>KeyPathChangeManager</tt>) when some object in the path (could
 * be the "Child", or the "Pet" in this example) has changed, since
 * <tt>DataObject</tt>s fire events only when their own properties change, not
 * when the properties of objects they relate to change. So, this class provides
 * means of observing changes down key paths, and notifying interested objects
 * of when such changes happen.
 * <p>
 * To enable an object to observe changes down the key path of some
 * <tt>DataObject</tt>, implement the <tt>PathChangeMonitor</tt> interface.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, Sep 24, 2007
 * @see PathChangeMonitor
 */
public class KeyPathChangeManager implements PathChangeMonitor {
	
	/**
	 * Tests if the given <tt>path</tt> qualifies to be observed by a
	 * <tt>KeyPathChangeManager</tt>. It qualifies if it is not a <tt>null</tt>
	 * that, after being processed by the <tt>JBNDUtil.cleanPath(String)</tt>
	 * method, contains more then one key. If the path qualifies, a
	 * <tt>KeyPathChangeManager</tt> can be created for it. If not, for example
	 * if the path is "children.@count", the <tt>DataObject</tt>s can be
	 * listened to directly, for they will generate events when their "children"
	 * property has changed.
	 * 
	 * @param path The key path to test.
	 * @return <tt>true</tt> if the path can (and should) be observed by a
	 *         <tt>KeyPathChangeManager</tt> for changes.
	 * @see JBNDUtil#cleanPath(String)
	 */
	public static boolean testPath(String path){
		if(path == null || !JBNDUtil.isKeyPath(path)) return false;
		String clearPath = JBNDUtil.cleanPath(path);
		return JBNDUtil.isKeyPath(clearPath);
	}
	
	
	
	
	//	the monitor to be notified of path changes
	private PathChangeMonitor monitor;
	
	//	the key path for which to observe changes in the DataObjects
	private String keyPath;
	
	//	a list of nodes
	protected List<KeyPathListenerNode> nodes = new LinkedList<KeyPathListenerNode>();
	
	/**
	 * Creates a new <tt>KeyPathChangeManager</tt> that will notify the given
	 * <tt>monitor</tt> when <tt>DataObject</tt>s (provided through
	 * <tt>add...</tt> methods) have changed somewhere along the given
	 * <tt>path</tt>. The <tt>path</tt> will be cleared up of all non standard
	 * keys through the <tt>JBNDUtil.cleanPath(String)</tt> method.
	 * <p>
	 * This constructor will throw if the given <tt>path</tt>, after being
	 * cleared up of non standard keys, does not contain more then one key. To
	 * avoid this, use the <tt>testPath(String)</tt> method prior to attempting
	 * to construct a <tt>KeyPathChangeManager</tt>, to see if it is possible.
	 * 
	 * @param path See above.
	 * @param monitor See above.
	 * @see #testPath(String)
	 * @throws IllegalArgumentException If the given <tt>path</tt>, after being
	 *             cleared up of non standard keys, does not contain more then
	 *             one key.
	 */
	public KeyPathChangeManager(String path, PathChangeMonitor monitor){
		if(!testPath(path)) throw new IllegalArgumentException(
			"Invalid path ("+path+"), check docs for requirements.");
		this.keyPath = JBNDUtil.cleanPath(path);
		this.monitor = monitor;
	}
	
	/**
	 * Gets the monitor observing key path changes of <tt>DataObject</tt>s
	 * managed by this <tt>KeyPathChangeManager</tt>.
	 * 
	 * @return The monitor observing key path changes.
	 */
	public PathChangeMonitor getMonitor(){
		return monitor;
	}

	/**
	 * Sets the monitor observing key path changes of <tt>DataObject</tt>s
	 * managed by this <tt>KeyPathChangeManager</tt>.
	 * 
	 * @param monitor The monitor observing key path changes.
	 */
	public void setMonitor(PathChangeMonitor monitor){
		this.monitor = monitor;
	}
	
	/**
	 * <tt>PathChangeMonitor</tt> implementation, monitors changes in the
	 * <tt>DataObject</tt>s managed by <tt>this KeyPathChangeManager</tt>, and
	 * forwards the call to the <tt>pathChanged(...)</tt> method of the
	 * <tt>monitor</tt>.
	 * 
	 * @param object The <tt>DataObject</tt> that has changed, contained in this
	 *            manager.
	 * @param path The path of the change.
	 */
	public void pathChanged(DataObject object, String path){
		if(monitor != null)
			monitor.pathChanged(object, path);
	}

	/**
	 * Adds the given <tt>DataObject</tt> to the list of managed objects, so
	 * that if a change occurs in it at any point of this manager's
	 * <tt>keyPath</tt>, the <tt>monitor</tt> will be notified.
	 * 
	 * @param object The <tt>DataObject</tt> to manage.
	 */
	public void add(DataObject object){
		nodes.add(new KeyPathListenerNode(object, keyPath, this));
	}
	
	/**
	 * Inserts the given <tt>DataObject</tt> to the list of managed
	 * <tt>DataObject</tt>s, at the given <tt>index</tt>, so that if a change
	 * occurs in it at any point of this manager's <tt>keyPath</tt>, the
	 * <tt>monitor</tt> will be notified.
	 * 
	 * @param index The location where to insert the <tt>DataObject</tt>, must
	 *            be between 0 and size (both inclusive).
	 * @param object The <tt>DataObject</tt> to manage.
	 */
	public void add(int index, DataObject object){
		nodes.add(index, new KeyPathListenerNode(object, keyPath, this));
	}
	
	/**
	 * Adds all <tt>DataObject</tt>s found in the given <tt>collection</tt> to
	 * the list of managed objects, so that if a change occurs in them at any
	 * point of this manager's <tt>keyPath</tt>, the <tt>monitor</tt> will be
	 * notified.
	 * 
	 * @param collection A collection of <tt>DataObject</tt>s.
	 */
	public void addAll(Collection<? extends DataObject> collection){
		for(DataObject object : collection)
			add(object);
	}
	
	/**
	 * Inserts all <tt>DataObject</tt>s found in the given <tt>collection</tt>
	 * to the list of managed objects, at the given index, so that if a change
	 * occurs in them at any point of this manager's <tt>keyPath</tt>, the
	 * <tt>monitor</tt> will be notified.
	 * 
	 * @param index The location where to insert the <tt>DataObject</tt>s, must
	 *            be between 0 and size (both inclusive).
	 * @param collection A collection of <tt>DataObject</tt>s.
	 */
	public void addAll(int index, Collection<? extends DataObject> collection){
		List<KeyPathListenerNode> insertNodes = new LinkedList<KeyPathListenerNode>();
		for(DataObject object : collection)
			insertNodes.add(new KeyPathListenerNode(object, keyPath, this));
		
		nodes.addAll(index, insertNodes);
	}
	
	/**
	 * Removes the given <tt>DataObject</tt> from the list of managed objects.
	 * 
	 * @param object The <tt>DataObject</tt> to stop monitoring.
	 * @return <tt>true</tt> If the <tt>DataObject</tt> was found and removed.
	 */
	public boolean remove(DataObject object){
		ListIterator<KeyPathListenerNode> it = nodes.listIterator();
		while(it.hasNext()){
			KeyPathListenerNode node = it.next();
			if(node.getDataObject() == object){
				it.remove();
				node.release();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes the <tt>DataObject</tt> at the given index from the list of
	 * managed objects.
	 * 
	 * @param index The index of the <tt>DataObject</tt> to remove, must be
	 *            between 0 and size (both inclusive).
	 */
	public void remove(int index){
		KeyPathListenerNode node = nodes.remove(index);
		node.release();
	}
	
	/**
	 * Removes all <tt>DataObject</tt>s that fall into the given range from
	 * being managed by this <tt>KeyPathChangeManager</tt>.
	 * 
	 * @param index0 Start of the range, inclusive.
	 * @param index1 End of the range, exclusive.
	 */
	public void remove(int index0, int index1){
		List<KeyPathListenerNode> range = nodes.subList(index0, index1);
		for(KeyPathListenerNode node : range)
			node.release();
		
		range.clear();
	}
	
	/**
	 * Removes all managed <tt>DataObject</tt> from this
	 * <tt>KeyPathChangeManager</tt>.
	 */
	public void clear(){
		for(KeyPathListenerNode node : nodes)
			node.release();
		
		nodes.clear();
	}
	
	/**
	 * Returns the index of the node that monitors the given
	 * <tt>DataObject</tt>, or -1 if not found.
	 * 
	 * @param object The <tt>DataObject</tt> to find the index of the node for.
	 * @return See above.
	 */
	public int indexOf(DataObject object){
		int i = 0;
		for(KeyPathListenerNode node : nodes){
			if(node.getDataObject() == object)
				return i;
			i++;
		}
		return -1;
	}
	
	/**
	 * Returns the number of <tt>DataObject</tt>s being managed by this
	 * <tt>KeyPathChangeManager</tt>.
	 * 
	 * @return The number of <tt>DataObject</tt>s being managed by this
	 *         <tt>KeyPathChangeManager</tt>.
	 */
	public int size(){
		return nodes.size();
	}
	
	/**
	 * Provides the means to observe changes in a tree of related
	 * <tt>DataObject</tt>s, which is defined by a key path. This enables
	 * viewing data found at some <tt>DataObject</tt>s key path, without
	 * worrying about undetected changes to the displayed data. See the
	 * <tt>KeyPathChangeMonitor</tt> class description for more info.
	 * 
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 * @version 1.1, Dec 11, 2007
	 * @see KeyPathChangeManager
	 */
	protected static class KeyPathListenerNode implements DataObjectListener, 
		PathChangeMonitor{
		
		//	the only DataObject this KeyPathListenerNode is listening to
		private DataObject object;
		
		//	the key for which the children nodes are being watched
		private String key;
		
		//	the rest of the path (to be handled by children)
		private String remainingPath;
		
		//	the object monitoring this KeyPathListenerNode
		private PathChangeMonitor monitor;
		
		//	the children nodes
		private List<KeyPathListenerNode> childrenNodes;
		
		/**
		 * Creates a new <tt>KeyPathListenerNode</tt>, and recursively creates
		 * children nodes, according to the the given <tt>path</tt>. This node
		 * will listen to the given <tt>DataObject</tt>, and monitor (by
		 * implementing <tt>PathChangeModitor</tt>) all children nodes.
		 * <p>
		 * The recursion is defined by the <tt>path</tt> parameter. Children
		 * nodes will be created if <tt>DataObject</tt>(s) are found in the
		 * given <tt>DataObject</tt> for the first key entry in the
		 * <tt>path</tt>, and the <tt>path</tt> contains more then one key. For
		 * example, if the path is "children.pet.name" (and the given
		 * <tt>DataObject</tt> is a "Parent"), where the "children" key points
		 * to an array of "Child" <tt>DataObject</tt>s, then this
		 * <tt>KeyPathListenerNode</tt> will listen to the given ("Parent")
		 * <tt>DataObject</tt> changes, and will create children nodes listening
		 * to individual "Child" <tt>DataObject</tt>s. The child nodes will be
		 * created with the remainder of the path ("pet.name"), and will
		 * continue to recursively generate their own children nodes. If the
		 * <tt>path</tt> points (as it often can, in the last recursion) to a
		 * non-relationship property, no children nodes will be created, and no
		 * exception thrown.
		 * <p>
		 * <b>Important:</b> The given <tt>path</tt> should not contain any non
		 * standard key elements (for example EOF's selectors: "@sum",
		 * "@count"...), otherwise the results are undefined. The tree is
		 * self-maintaining, meaning that if changes occur at some place in the
		 * listened <tt>DataObject</tt>s tree, and that change affects the tree
		 * structure, the tree of <tt>KeyPathListenerNode</tt>s will adapt
		 * accordingly.
		 * 
		 * @param object The <tt>DataObject</tt> to listen to, and generate
		 *            children nodes for, if necessary.
		 * @param path The path in accordance with which to recurse down the
		 *            <tt>DataObject</tt> tree. Should be a standard path, for
		 *            example "child.pet.name", without non standard keys (for
		 *            example EOF's "@sum", "@count"...), otherwise the results
		 *            are undefined. See above for a description on how the path
		 *            defines the node construction.
		 * @param monitor The object interested when either this node, or nodes
		 *            further down the path detect a <tt>DataObject</tt> change.
		 *            Typically the parent node of the created node.
		 */
		public KeyPathListenerNode(DataObject object, String path, PathChangeMonitor monitor){
			
			//	listen to the EO
			this.object = object;
			object.addDataObjectListener(this);
			//	attach the monitor
			this.monitor = monitor;
			
			//	take care of the path splitting
			//	if there is a path to start with
			if(JBNDUtil.isKeyPath(path)){
				this.key = JBNDUtil.firstKeyOfKeyPath(path);
				remainingPath = JBNDUtil.keyPathWithoutFirstKey(path);
			}else{
				this.key = path;
			}
			
			//	listen to the children
			attachChildren();
		}
		
		/**
		 * Returns the <tt>DataObject</tt> this node is listening to for
		 * changes.
		 * 
		 * @return The <tt>DataObject</tt> this node is listening to for
		 *         changes.
		 */
		public DataObject getDataObject(){
			return this.object;
		}
		
		/**
		 * Implementation of the <tt>DataObjectListener</tt> interface. If the
		 * <tt>DataObject</tt> this node listens to changed on the key the
		 * children nodes are created for, they will be refreshed. Next to that
		 * it notifies the <tt>monitor</tt> of this node that an
		 * <tt>DataObject</tt> change occured, if necessary. See constructor
		 * documentation for an explanation of the "monitor" concept.
		 * 
		 * @param event The event fired by the <tt>DataObject</tt> of this node.
		 * @see PathChangeMonitor
		 */
		public void objectChanged(DataObjectEvent event){
			
			//	the DataObject listened to has changed...
			//	if it is for the key the children are being watched, refresh children
			if(event.getKey().equals(key)){
				releaseChildren();
				attachChildren();
				//		Notify the PathChangeMonitor monitoring this DataObject
				this.monitor.pathChanged(object, key);
			}
		}

		/**
		 * Invoked by a child node when a change in the <tt>DataObject</tt>
		 * somewhere down the path has occured. For example, if there is a tree
		 * of nodes listening for changes along the "children.pet.name" path,
		 * then this method would be called for a "Child" <tt>DataObject</tt>
		 * node after the "Child"'s "Pet" changed, or this method was called on
		 * the "Pet" node.
		 * <p>
		 * The call is then forwarded up the path, meaning the "Child" node
		 * would call it monitor's (that being the parent node)
		 * <tt>pathChanged(DataObject, String)</tt> method. This goes on and on
		 * until the root of the tree is reached, at which point interested
		 * <tt>PathChangeMonitor</tt> implementations get notified.
		 * <p>
		 * Every recursion up the path attaches it's key to the <tt>key</tt>
		 * parameter of the method, resulting in a key path which provides the
		 * path from the <tt>DataObject</tt> of the root node all the way to the
		 * property that changed in some <tt>DataObject</tt> in the node tree.
		 * 
		 * @param object The <tt>DataObject</tt> down whose path a change has
		 *            occured.
		 * @param path The path of the change.
		 */
		public void pathChanged(DataObject object, String path){
			//	one of the children DataObject being monitored has changed, forward the call
			this.monitor.pathChanged(this.object, this.key + "." + path);
		}
		
		/**
		 * Makes sure that this node, as well as all children and all descendant
		 * nodes, release their <tt>DataObject</tt>s (unregister themselves as
		 * <tt>DataObject</tt> listeners, and set the <tt>DataObject</tt>
		 * reference to <tt>null</tt>).
		 */
		public void release(){
			releaseChildren();
			object.removeDataObjectListener(this);
			object = null;
		}
		
		/**
		 * Calls the <tt>release()</tt> method on all children nodes, and then
		 * stops referencing them.
		 */
		private void releaseChildren(){
			if(childrenNodes == null) return;
			for(KeyPathListenerNode node : childrenNodes)
				node.release();
			
			childrenNodes.clear();
		}
		
		/**
		 * Creates and "attaches" the children of this node as defined in the
		 * constructor. Does that by seeing what can be found in this node's
		 * <tt>DataObject</tt> under this node's <tt>key</tt>. If it is a
		 * <tt>DataObject</tt>, or an array of <tt>DataObject</tt>s, children
		 * nodes are created, <tt>this</tt> node being their
		 * <tt>PathChangeMonitor</tt>. This is done only if this node was
		 * created with a path consisting of more then one key, otherwise there
		 * is no key for the children nodes to monitor.
		 */
		private void attachChildren(){
			
			// if the key this node is listening to is an attribute, there is
			// no possibility for existing children to create nodes for
			PropType propType = object.getDataType().propertyType(key);
			if(remainingPath == null
					||  propType == PropType.ATTRIBUTE
					|| propType == PropType.DERIVED_ATTRIBUTE
					|| propType == PropType.UNDEFINED) return;
			
			//	get value from the object
			Object value = object.get(key);
			if(value == null) return;
			
			// the objects found at the path of this node: this node's children
			DataObject[] dataObjectsAtPath = null;
			
			//	see if it contains a DataObject, or many of them
			if(value instanceof DataObject){
				dataObjectsAtPath = new DataObject[]{(DataObject)value};
			
			}else if(value instanceof List){
				@SuppressWarnings("unchecked")
				List<DataObject> list = (List<DataObject>)value;
				dataObjectsAtPath = new DataObject[list.size()];
				int i = 0;
				for(DataObject o : list)
					dataObjectsAtPath[i++] = o;
			}else
				return;
			
			//	late init of the childrenNodes list
			if(childrenNodes == null)
				childrenNodes = new ArrayList<KeyPathListenerNode>(dataObjectsAtPath.length + 5);
			
			//	fill up the children nodes
			for(DataObject object : dataObjectsAtPath)
				childrenNodes.add(new KeyPathListenerNode(object, remainingPath, this));
		}
	}

}
