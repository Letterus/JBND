package org.jbnd.swing;

import java.util.*;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jbnd.DataObject;
import org.jbnd.binding.Binding;
import org.jbnd.binding.Bound;
import org.jbnd.event.DataObjectEvent;
import org.jbnd.event.DataObjectListener;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.support.JBNDUtil;

/**
 * A <tt>Bound</tt> implementation that provides the data found in the
 * <tt>Binding</tt> in a <tt>TreeModel</tt>, to be used with <tt>JTree</tt>s
 * for displaying tree structures of <tt>DataObject</tt>s. This model can
 * only be used to display <tt>DataObject</tt> structures, but not their other
 * properties (data such as text, numbers, etc). It does NOT support editing
 * (attempts to do so are simply ignored), and thereby should be used with
 * <tt>JTree</tt>s that are set not to be editable.
 * <p>
 * As said this model gets it's data from the given <tt>Binding</tt>. Since
 * tree structures represent data in a specific way, it is important to
 * understand how the tree model uses the <tt>DataObject</tt>s and keys found
 * in the <tt>Binding</tt>. The root node of the tree is always an instance
 * of <tt>RootNode</tt> class (inner class of this model), and is not backed
 * by data from the <tt>Binding</tt>. The <tt>DataObject</tt>s found in
 * the <tt>Binding</tt> are used to construct children nodes of the root node,
 * and are instances of the <tt>DataObjectNode</tt> class. After that the
 * structure of the tree model depends on keys found in the <tt>Binding</tt>.
 * Root node's children (<tt>DataObject</tt>s provided by the
 * <tt>Binding</tt>) will have as their children the <tt>DataObject</tt>s
 * found in them for the first key of the <tt>Binding</tt>. In turn those
 * nodes will have as their children the <tt>DataObject</tt>s found in them
 * for the second key of the binding. For example, if one wishes to create a
 * tree that displays at it's first level Parent objects, at it's second
 * Children objects, and at it's third Pet objects, then the
 * <tt>BoundTreeModel</tt> should be provided with a <tt>Binding</tt> that
 * contains an arbitrary number of Parent <tt>DataObject</tt>s, and has two
 * keys (obtainable through the <tt>Binding</tt>'s <tt>getKeys()</tt>
 * method), those being <tt>{"children", "pets"}</tt>.
 * <p>
 * Note that the <tt>BoundTreeModel</tt> supports key paths as well, but they
 * will NOT define more then one generation of the tree hierarchy. For example,
 * the <tt>Binding</tt> defined as the one above, but having different keys,
 * those being <tt>{"children", "bestFriend.pets"}</tt> will have the same
 * number of generations. The only difference being that the last generation
 * objects will not be a list of Children's pets, but a list of their best
 * friend's pets. The tree will NOT display the BestFriend <tt>DataObject</tt>s
 * in between of the Children and the Pets. To accomplish that, the binding has
 * to provide keys as follows: <tt>{"children", "bestFriend", "pets"}</tt>.
 * <p>
 * <b>Recursions:</b> The <tt>BoundTreeModel</tt> supports recursive
 * relationship display till infinite depth (theoretically). Meaning, if it is
 * desired to represent a geneological tree structure of Person
 * <tt>DataObject</tt>s, where each person has a to-many relationship to
 * other Person <tt>DataObject</tt>s that is called 'children', the resulting
 * tree of undefined depth can be represented with this model. The
 * <tt>Binding</tt> of the model needs to provide person objects, and the
 * model itself needs to be instantiated with a constructor that can accept an
 * array of <tt>String</tt>s, called <tt>recursionKeys</tt>. This array of
 * <tt>String</tt>s will be used on every generation of <tt>DataObject</tt>s
 * in the tree to look for relationships. If one is found, the
 * <tt>DataObject</tt>s it provides will added to the tree. In the above
 * example of genealogy, one should simply instantiate a <tt>BoundTreeModel</tt>
 * with the recursion keys parameter being <tt>{"children"}</tt>. Recursion
 * keys and normal binding keys can be combined in any desired way, there is no
 * limitation on the number of either. If a <tt>DataObject</tt> can provide
 * relationships for more then one recursion key, the order in which the
 * children of that object will appear in the tree is defined by the order of
 * recursion keys. To determine what goes first between objects found for
 * standard binding keys and recursion keys two flags have been provided (<tt>RECURSIONS_FIRST</tt>
 * and <tt>RECURSIONS_LAST</tt>), as well as a constructor that can accept
 * one of those two indication flags.
 * <p>
 * <b>Tree structure changes:</b> The client programmer has no access to the
 * structure changing methods of the <tt>BoundTreeModel</tt> nor the need to
 * interact with it on that level. The model keeps track of changes happening to
 * <tt>DataObject</tt>s it contains, and if such changes influence the
 * structure of the tree, appropriate measures are taken. The model of course
 * also responds to changes in the <tt>Binding</tt>, so if the
 * <tt>DataObject</tt>s the <tt>Binding</tt> provides get partly removed,
 * or more are added, the model will react accordingly.
 * <p>
 * The nodes of the tree (which is useful to know when monitoring a tree's
 * selection) are instances either of <tt>BoundTreeModel.RootNode</tt> (the
 * root node only) or <tt>BoundTreeModel.DataObjectNode</tt> class (all other
 * nodes). This is guaranteed, so it is safe to cast them, in order to use their
 * specific functionalities.
 * 
 * @version 1.0 Dec 30, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see DataObjectNode
 * @see RootNode
 */
public class BoundTreeModel implements Bound, TreeModel{
	
	/**
	 * An enumeration of <tt>DataObjectNode</tt>s that contains no elements.
	 * Used in the DataObjectNode#children() method.
	 */
	private static final Enumeration<DataObjectNode> _EMPTY_CHILDREN_ENUM =
		new Enumeration<DataObjectNode>(){

			public boolean hasMoreElements(){
				return false;
			}

			public DataObjectNode nextElement(){
				return null;
			}};
	
	/**
	 * A flag indicating that this <tt>TreeModel</tt> should put the
	 * <tt>DataObject</tt>s found in recursion relationships BEFORE
	 * those found for relationships defined by the keys that
	 * the <tt>Biding</tt> provides.
	 */
	public static final int RECURSIONS_FIRST = 0;
	
	/**
	 * A flag indicating that this <tt>TreeModel</tt> should put the
	 * <tt>DataObject</tt>s found in recursion relationships AFTER
	 * those found for relationships defined by the keys that
	 * the <tt>Biding</tt> provides.
	 */
	public static final int RECURSIONS_LAST = 1;
	
	/**
	 * Returns an array of <tt>TreeNode</tt>s that define the full path
	 * from the root node of the model to the given <tt>lastNode</tt>.
	 * The returned array is ordered so that the root node is the first item
	 * in it, and the given <tt>lastNode</tt> is the last.
	 * 
	 * @param lastNode	The last node in the path.
	 * @return			See above.
	 */
	public static TreeNode[] getPath(TreeNode lastNode){
		//	the list containing the whole path
		//	in reverse order
		LinkedList<TreeNode> pathList = new LinkedList<TreeNode>();
		
		//	add nodes to the path list, starting from the
		//	given last node, working towards the root
		//	loop will stop when the root is reached,
		//	because it's parent node is null
		while(lastNode != null){
			pathList.add(lastNode);
			lastNode = lastNode.getParent();
		}
		
		//	translate the reverse order list into an array
		TreeNode[] rVal = new TreeNode[pathList.size()];
		int index = rVal.length - 1;
		for(TreeNode node : pathList)
			rVal[index--] = node;
		
		return rVal;
	}
	
	
	
	/*
	 * 
	 * END OF STATIC
	 * 
	 */
	
	
	//	a set of keys on which recursion should be performed
	private String[] recursionKeys = new String[0];
	
	//	how this model orders it's recursions
	private int recursionPlace = RECURSIONS_FIRST;
	
	//	the root node of the tree model
	private RootNode rootNode = new RootNode();
	
	
	/**
	 * Creates a <tt>BoundTreeModel</tt> with the given parameters.
	 * 
	 * @param binding	The <tt>Binding</tt> to be used with with the model.
	 */
	public BoundTreeModel(Binding binding){
		this(binding, RECURSIONS_FIRST);
	}
	
	/**
	 * Creates a <tt>BoundTreeModel</tt> with the given parameters.
	 * 
	 * @param binding	The <tt>Binding</tt> to be used with with the model.
	 * @param recursionKeys	An array of <tt>String</tt>s that is not allowed
	 * 					to be <tt>null</tt>, nor is any of the objects contained
	 * 					allowed to be <code>null</code>; usage explained in class
	 * 					documentation (see {@link BoundTreeModel}.
	 */
	public BoundTreeModel(Binding binding, String... recursionKeys){
		this(binding, RECURSIONS_FIRST, recursionKeys);
	}
	
	/**
	 * Creates a <tt>BoundTreeModel</tt> with the given parameters.
	 * 
	 * @param binding	The <tt>Binding</tt> to be used with with the model.
	 * @param recursionKeys	An array of <tt>String</tt>s that is not allowed
	 * 					to be <tt>null</tt>, nor is any of the objects contained
	 * 					allowed to be <code>null</code>; usage explained in class
	 * 					documentation (see {@link BoundTreeModel}.
	 * @param recursionPlace A flag indicating if the recursively found relationships
	 * 					should go before or after the binding defined relationships,
	 * 					usage explained in class documentation (see {@link BoundTreeModel}.
	 * 					The given value must be either <tt>RECURSIONS_FIRST</tt> or
	 * 					<tt>RECURSIONS_LAST</tt>.
	 */
	public BoundTreeModel(Binding binding, int recursionPlace, String... recursionKeys){
		setBinding(binding);
		this.recursionKeys = recursionKeys;
		this.recursionPlace = recursionPlace;
	}
	
	/**
	 * Sets the <tt>String</tt> value of the root node of the tree,
	 * which is never a <tt>DataObject</tt>, but a simple placeholder.
	 * 
	 * @param root	The <tt>String</tt> the root node should represent
	 * 				itself with, <tt>null</tt> not allowed.
	 */
	public void setRootString(String root){
		if(root == null)
			throw new IllegalArgumentException("Can not set a null tree root");
		rootNode.toStringValue = root;
	}
	
	//	the binding backing this model
	protected Binding binding;
	
	/**
	 * Returns the <tt>Binding</tt> backing this model.
	 * 
	 * @return	The <tt>Binding</tt> backing this model.
	 */
	public Binding getBinding(){
		return binding;
	}

	/**
	 * Sets the <tt>Binding</tt> backing this model.
	 * 
	 * @param	binding	The <tt>Binding</tt> that should
	 * 			back this model, <tt>null</tt> is not acceptable.
	 */
	public void setBinding(Binding binding){
		if(this.binding == binding && binding != null) return;
		if(this.binding != null){
			this.binding.removeDataSourceListener(this);
			rootNode.removeCaches();
		}
		
		//	perform all the binding related processes
		this.binding = binding;
		binding.addDataSourceListener(this);
		rootNode.initChildren();
		fireTreeStructureChanged(new TreeModelEvent(
			this,
			new Object[]{rootNode}
		));
	}

	/**
	 * <tt>DataSourceListener</tt> implementation, called when the
	 * backing <tt>Binding</tt> fires an event. Fires an appropriate
	 * <tt>TreeModelEvent</tt> to notify GUI elements of changes.
	 * 
	 * @param	event	The event fired by the <tt>Binding</tt>.
	 */
	public void dataAdded(DataSourceEvent event){
		rootNode.dataObjectsAddedToBinding(event.getIndex0(), event.getIndex1());

	}

	/**
	 * <tt>DataSourceListener</tt> implementation, called when the
	 * backing <tt>Binding</tt> fires an event. Fires an appropriate
	 * <tt>TreeModelEvent</tt> to notify GUI elements of changes.
	 * 
	 * @param	event	The event fired by the <tt>Binding</tt>.
	 */
	public void dataChanged(DataSourceEvent event){
		//	ignore this event
		//	because the DataObjectNodes utilized in
		//	constructing the BoundTreeModel take care
		//	of listening for changes within the tree

	}

	/**
	 * <tt>DataSourceListener</tt> implementation, called when the
	 * backing <tt>Binding</tt> fires an event. Fires an appropriate
	 * <tt>TreeModelEvent</tt> to notify GUI elements of changes.
	 * 
	 * @param	event	The event fired by the <tt>Binding</tt>.
	 */
	public void dataRemoved(DataSourceEvent event){
		rootNode.dataObjectsRemovedFromBinding(event.getIndex0(), event.getIndex1());

	}
	
	//	the listeners registered with this model
	private List<TreeModelListener> treeModelListeners = 
		new LinkedList<TreeModelListener>();
	
	/**
	 * Adds the given <tt>listener</tt> to the list of listeners
	 * that get notified when changes occur in the <tt>BoundTreeModel</tt>.
	 * 
	 * @param	listener	The listener to add.
	 */
	public void addTreeModelListener(TreeModelListener listener){
		treeModelListeners.add(listener);
	}
	
	/**
	 * Removes the given <tt>listener</tt> from the list of listeners
	 * that get notified when changes occur in the <tt>BoundTreeModel</tt>.
	 * 
	 * @param	listener	The listener to remove.
	 */
	public void removeTreeModelListener(TreeModelListener listener){
		treeModelListeners.remove(listener);
	}

	/**
	 * <tt>TreeModel</tt> implementation, returns the child node
	 * found in the given <tt>parent</tt> object for the given
	 * <tt>index</tt>.
	 * 
	 * @param	parent	The parent for which the child is sought.
	 * @param	index	The index of the sought child.
	 * @return	The child object found in the parent for the given index.
	 */
	public Object getChild(Object parent, int index){
		return ((TreeNode)parent).getChildAt(index);
	}

	/**
	 * Returns the number of children the given <tt>parent</tt>
	 * has.
	 * 
	 * @param	parent	The object in the tree for which the child count is sought.
	 * @return	The number of children the given parent has.
	 */
	public int getChildCount(Object parent){
		int rVal = ((TreeNode)parent).getChildCount();
		return rVal;
	}

	/**
	 * Returns the index of the given <tt>child</tt> object in
	 * the given <tt>parent</tt>.
	 * 
	 * @param	parent	The parent in which to look for the child.
	 * @param	child	The child for which the index is sought.
	 * @return	The index of the given child in the given parent.
	 */
	public int getIndexOfChild(Object parent, Object child){
		return ((TreeNode)parent).getIndex((TreeNode)child);
	}

	/**
	 * Returns the root node of the tree model.
	 * 
	 * @return	The root node of the tree model.
	 */
	public Object getRoot(){
		return rootNode;
	}

	/**
	 * Tells if the given node object is a leaf or not.
	 * 
	 * @param	node	The node in question.
	 * @return	If the given node object is a leaf or not.
	 */
	public boolean isLeaf(Object node){
		return ((TreeNode)node).isLeaf();
	}
	
	/**
	 * Finds the path for the given <tt>DataObject</tt> in this tree, or
	 * returns <tt>null</tt> if nothing is found.
	 * 
	 * @param object The sought <tt>DataObject</tt>.
	 * @return The path for the given <tt>DataObject</tt> in this tree, or
	 *         returns <tt>null</tt> if nothing is found.
	 */
	public TreePath pathForObject(DataObject object){
		DataObjectNode node = _searchForObject(object, rootNode.children());
		if(node == null) return null;
		else return new TreePath(getPath(node));
	}
	
	private DataObjectNode _searchForObject(DataObject object, Enumeration<DataObjectNode> nodes){
		while(nodes.hasMoreElements()){
			DataObjectNode node = nodes.nextElement();
			if(node.getDataObject().equals(object)) return node;
			DataObjectNode rVal = _searchForObject(object, node.children());
			if(rVal != null) return rVal;
		}
		
		return null;
	}

	/**
	 * This method does nothing, because <tt>BoundTreeModel</tt> does not
	 * support editing.
	 * 
	 * @param path Ignored.
	 * @param newValue Ignored.
	 */
	public void valueForPathChanged(TreePath path, Object newValue){
		//	do nothing because BoundTreeModel backed are not editable
	}
	
	/**
	 * Fires the given event on all registered <tt>TreeModelListener</tt>s.
	 * 
	 * @param event	The event object.
	 */
	private void fireTreeNodesChanged(TreeModelEvent event){
		TreeModelListener[] listeners = treeModelListeners
				.toArray(new TreeModelListener[treeModelListeners.size()]);
		for(int i = listeners.length - 1 ; i >= 0 ; i--)
			listeners[i].treeNodesChanged(event);
	}
	
	/**
	 * Fires the given event on all registered <tt>TreeModelListener</tt>s.
	 * 
	 * @param event	The event object.
	 */
	private void fireTreeNodesInserted(TreeModelEvent event){
		TreeModelListener[] listeners = treeModelListeners
			.toArray(new TreeModelListener[treeModelListeners.size()]);
		for(int i = listeners.length - 1 ; i >= 0 ; i--)
			listeners[i].treeNodesInserted(event);
	}
	
	/**
	 * Fires the given event on all registered <tt>TreeModelListener</tt>s.
	 * 
	 * @param event	The event object.
	 */
	private void fireTreeNodesRemoved(TreeModelEvent event){
		TreeModelListener[] listeners = treeModelListeners
			.toArray(new TreeModelListener[treeModelListeners.size()]);
		for(int i = listeners.length - 1 ; i >= 0 ; i--)
			listeners[i].treeNodesRemoved(event);
	}
	
	/**
	 * Fires the given event on all registered <tt>TreeModelListener</tt>s.
	 * 
	 * @param event	The event object.
	 */
	private void fireTreeStructureChanged(TreeModelEvent event){
		TreeModelListener[] listeners = treeModelListeners
			.toArray(new TreeModelListener[treeModelListeners.size()]);
		for(int i = listeners.length - 1 ; i >= 0 ; i--)
			listeners[i].treeStructureChanged(event);
	}
	
	/**
	 * A <tt>TreeNode</tt> used by the <tt>BoundTreeModel</tt> to
	 * represent a single <tt>DataObject</tt>, and to provide
	 * functionalities that translate <tt>DataObject</tt> relationships
	 * into tree node structures. You should probably interact with
	 * instances of this class to retrieve the <tt>DataObject</tt>
	 * it represents.
	 *
		@version 1.0 Dec 29, 2007
		@author Florijan Stamenkovic (flor385@mac.com)
		@see	#getDataObject()
	 */
	public class DataObjectNode implements TreeNode, DataObjectListener{
		
		//	parent node
		private TreeNode parent;
		
		//	the index of the key within the binding of the tree model
		//	this node belongs to...
		//	the key found in the binding for this index is the key
		//	the CHILDREN nodes of this node are generated
		private int	bindingKeyIndex;
		
		//	the data object of this node
		private DataObject dataObject;

		//	a list of children nodes, all containing data objects
		//	lazily initialized in the _initChildren() method
		private List<DataObjectNode> children;
		
		/**
		 * Creates a new <tt>DataObjectNode</tt> with the given parameters.
		 * 
		 * @param parent	The parent <tt>Node</tt> of this node.
		 * @param object	The <tt>DataObject</tt> this node represents.
		 * @param bindingKeyIndex	The index of the key in the <tt>Binding</tt>
		 * 					if the model for which the children of this node
		 * 					can be found.
		 */
		private DataObjectNode(TreeNode parent, DataObject object, int bindingKeyIndex){
			this.parent = parent;
			this.bindingKeyIndex = bindingKeyIndex;
			this.dataObject = object;
			dataObject.addDataObjectListener(this);
		}

		
		
		
		/**
		 * Returns the children <tt>DataObjectNode</tt>s of this node.
		 * 
		 * @return	The children <tt>DataObjectNode</tt>s of this node.
		 */
		@SuppressWarnings("unchecked")
		public Enumeration<DataObjectNode> children(){
			if(children == null) return _EMPTY_CHILDREN_ENUM;
			return (Enumeration<DataObjectNode>)JBNDUtil.enumFromIt(children.iterator());
		}

		/**
		 * Returns the <tt>DataObject</tt> this node represents in the
		 * tree.
		 * 
		 * @return	The <tt>DataObject</tt> this node represents in the
		 * 			tree.
		 */
		public DataObject getDataObject(){
			return dataObject;
		}
		
		/**
		 * Returns the key(path) for which this node will attempt to create
		 * it's children, or <code>null</code> if this node has no descendants
		 * according to the <tt>Binding</tt>. Note that even if this method
		 * returns <code>null</code> this node can still end up having
		 * children, if the <tt>BoundTreeModel</tt> using it has been setup
		 * to use recursion on some of it's keys.
		 * 
		 * @return	See above.
		 */
		public String getChildrenKey(){
			return binding.getKeys().length > bindingKeyIndex ?
					binding.getKeys()[bindingKeyIndex] : null;
		}

		/**
		 * Returns if this node allows children, whatever that may imply,
		 * the <tt>TreeNode</tt> API is not very clear on that. This 
		 * method always returns <code>true</code>.
		 */
		public boolean getAllowsChildren(){
			return true;
		}

		/**
		 * Returns the child node (an instanceof <tt>DataObjectNode</tt>)
		 * for the given index.
		 * 
		 * @param	index	The index of the sought child.
		 * @return	The child node for the given index.
		 */
		public TreeNode getChildAt(int index){
			_initChildren();
			return children.get(index);
		}

		/**
		 * Returns the children node count for this node.
		 * 
		 * @return	The children node count for this node.
		 */
		public int getChildCount(){
			_initChildren();
			return children.size();
		}

		/**
		 * Returns the index of the given node among this node's
		 * children, or -1 if not found.
		 * 
		 * @param	node	The node for which the index is sought.
		 * @return	The index of the given node among this node's children.
		 */
		public int getIndex(TreeNode node){
			return children.indexOf(node);
		}

		/**
		 * Returns the parent node of this node.
		 * 
		 * @return	The parent node of this node.
		 */
		public TreeNode getParent(){
			return parent;
		}

		/**
		 * Returns <tt>true</tt> if the count of children is 0.
		 * 
		 * @return	<tt>true</tt> if the count of children is 0.
		 */
		public boolean isLeaf(){
			return getChildCount() == 0;
		}
		
		/**
		 * Lazily initializes the children nodes of this node.
		 */
		private void _initChildren(){
			
			if(children != null) return;
			
			children = new ArrayList<DataObjectNode>(10);
			
			//	if recursions go first, do them
			if(recursionPlace == RECURSIONS_FIRST)
				_initRecursions();
			
			//	initialize children from the standard key
			_initNonRecursions();
			
			//	if recursions go last, do them now
			if(recursionPlace == RECURSIONS_LAST)
				_initRecursions();
		}

		
		/**
		 * Fills up the cache of children nodes with nodes that
		 * are created from the key found in the <tt>Binding</tt> for
		 * the key this node finds it children for (obtainable with
		 * the <tt>getChildrenKey()</tt> method). 
		 */
		private void _initNonRecursions(){
			
			//	get the key from the binding
			String key = getChildrenKey();
			
			//	if there is no key, it means that this node is created for
			//	the last one in the binding, and it's the end of the road
			//	if there is a key, add whatever can be found for it
			if(key != null) _addObjectsFoundForKey(key);
		
		}

		/**
		 * Fills up the cache of children nodes with nodes that
		 * are created from all the recursion keys that the
		 * <tt>DataObject</tt> of this node has.
		 */
		private void _initRecursions(){
			
			//	the binding key of this node, should be skipped if
			//	among the recursions
			String thisNodeBindingKey = getChildrenKey();
			
			//	iterate through the recursion keys
			for(String recursionKey : recursionKeys){
				
				if(recursionKey.equals(thisNodeBindingKey)) continue;
				try{
					_addObjectsFoundForKey(recursionKey);
				}catch(org.jbnd.UnknownKeyException ex){
					//	swallow this exception, one never knows
					//	what kind of recursive keys have been used,
					//	they might be valid for some nodes, but not
					//	for others
				}
			}
		}
		
		/**
		 * Adds all the <tt>DataObject</tt>s (or just one, in case of
		 * to-one relationships) found in the <tt>DataObject</tt> this
		 * node represents for the given <tt>key</tt>, to the cache of
		 * children nodes.
		 * 
		 * @param key	The key for which to look for relationships in
		 * 				the <tt>DataObject</tt> this node represents. 
		 */
		@SuppressWarnings("unchecked")
		private void _addObjectsFoundForKey(String key){
			//	get the value in the DataObject for the key
			
			Object valueForKey = dataObject.get(key);
			if(valueForKey == null) return;
			
			//	the binding key index that the children nodes will have
			//	ca not simply increment the one of this node by one
			//	because of possible recursions...
			//	see the _indexForChild(String) method
			int bindingKeyIndexOfChildren = _indexForChild(key);
			
			if(valueForKey instanceof DataObject)
				//	we have a to one relationship
				//	simply add it to the tree
				children.add(new DataObjectNode(
					this, (DataObject)valueForKey, bindingKeyIndexOfChildren));
			else if(valueForKey instanceof List){
				//	we have a to many relationships
				//	iterate through the individual objects
				//	and add them as children
				List<DataObject> list = (List<DataObject>)valueForKey;
				for(DataObject object : list){
					children.add(new DataObjectNode(
						this, object, bindingKeyIndexOfChildren));
				}
			}
		}
		
		/**
		 * Returns what should be the index of the key (among those
		 * found in the <tt>Binding</tt> for the children nodes of this
		 * node. The index is specific to the relationship they were
		 * retrieved for.
		 * 
		 * @param key	The key (in the <tt>DataObject</tt> of this node,
		 * 				for which children <tt>DataObject</tt>s were found,
		 * 				for which nodes are being created.
		 * @return	See above.
		 */
		private int _indexForChild(String key){
			//	iterate through the recursion keys
			//	looking for a match
			for(String recursionKey : recursionKeys){
				if(recursionKey.equals(key)){
					//	found a matching recursion key
					//	get it's index from the binding keys
					String[] bindingKeys = binding.getKeys();
					for(int i = 0 ; i < bindingKeys.length ; i++)
						if(bindingKeys[i].equals(key)) return i + 1;
				}
			}
			
			//	the given key was not found among the recursion keys
			//	so just increment
			return bindingKeyIndex + 1;
		}
		
		/**
		 * Makes sure that this node, as well as all children and all descendant nodes,
		 * release their <tt>DataObject</tt>s (unregister themselves as <tt>DataObject</tt>
		 * listeners, and set the <tt>DataObject</tt> reference to <tt>null</tt>).
		 *
		 */
		private void release(){
			releaseChildren();
			dataObject.removeDataObjectListener(this);
			dataObject = null;
			parent = null;
		}
		
		/**
		 * Calls the <tt>release()</tt> method on all children nodes, and then stops
		 * referencing them.
		 *
		 */
		private void releaseChildren(){
			
			if(children == null) return;
			for(DataObjectNode node : children)
				node.release();
			
			children = null;
		}

		/**
		 * <tt>DataObjectListner</tt> implementation, called when the
		 * <tt>DataObject</tt> this node represents in the tree changes.
		 * 
		 * @param	event	The change event.
		 */
		public void objectChanged(DataObjectEvent event){
			
			//	see if the key for which the object can be
			//	the key of any children
			String eventKey = event.getKey();
			boolean isChildrenKey = eventKey.equals(getChildrenKey());
			for(String recursionKey : recursionKeys)
				if(eventKey.equals(recursionKey)){
					isChildrenKey = true;
					break;
				}
			
			
			if(!isChildrenKey)
				//	the DataObject change did not affect the tree structure
				//	so just fire an event saying that this node changed
				//	maybe it should get repainted
				fireTreeNodesChanged(new TreeModelEvent(
						BoundTreeModel.this, 	//	source of change
						getPath(this), 			//	path to this node
						null, 					//	null, as only this node changed
						null));					//	null, as only this node changed
			else{
				//	the tree structure did change, one way or another
				//	though this DataObject change event most likely does NOT
				//	signify an enormous structural change, it is fairly difficult
				//	to construct a detailed TreeModelEvent, so use the simple version
				releaseChildren();
				fireTreeStructureChanged(new TreeModelEvent(BoundTreeModel.this, getPath(this)));
			}
		}
		
		/**
		 * Returns the <tt>toString()</tt> value of the <tt>DataObject</tt>
		 * this node encapsulates.
		 * 
		 * @return	The <tt>toString()</tt> value of the <tt>DataObject</tt>
		 * 			this node encapsulates.
		 */
		public String toString(){
			return dataObject.toString();
		}
	}
	
	/**
	 * A <tt>TreeNode</tt> used in <tt>BoundTreeModel</tt> as the root node.
	 * This node taps directly into the <tt>Binding</tt> that backs the model,
	 * and uses the <tt>DataObject</tt>s found there as it's children. It
	 * creates <tt>DataObjectNode</tt>s from them. It also provides facilities
	 * for the <tt>BoundTreeModel</tt> to keep in synch with the <tt>Binding</tt>;
	 * it does so by defining methods for adding and removing individual
	 * children nodes, which the model object can use.
	 *
		@version 1.0 Dec 29, 2007
		@author Florijan Stamenkovic (flor385@mac.com)
	 */
	public class RootNode implements TreeNode{
		
		//	the to string value, it's only purpose is to be returned
		//	from the toString() method of this class
		//	can be changed with the setRootString() method of the model
		private String toStringValue = "Data";
		
		/**
		 * Returns whichever value has been provided for the
		 * purpose through the {@link BoundTreeModel#setRootString(String)}
		 * method; the default one is "Data".
		 * 
		 * @return	See above.
		 */
		public String toString(){
			return toStringValue;
		}

		//	children nodes cache
		//	initiated with the _initChildrenMethod
		//	potentially mutated
		//	contains nodes that encapsulate the DataObjects
		//	the Binding of the model provides
		private List<DataObjectNode> children;
		
		/**
		 * Returns the children <tt>DataObjectNode</tt>s of this root node.
		 * 
		 * @return	The children <tt>DataObjectNode</tt>s of this root node.
		 */
		@SuppressWarnings("unchecked")
		public Enumeration<DataObjectNode> children(){
			return (Enumeration<DataObjectNode>)JBNDUtil.enumFromIt(children.iterator());
		}

		/**
		 * Returns if this node allows children, whatever that may imply,
		 * the <tt>TreeNode</tt> API is not very clear on that. This 
		 * method always returns <code>true</code>.
		 */
		public boolean getAllowsChildren(){
			return true;
		}

		/**
		 * Returns the child node (an instanceof <tt>DataObjectNode</tt>)
		 * for the given index.
		 * 
		 * @param	index	The index of the sought child.
		 * @return	The child node for the given index.
		 */
		public TreeNode getChildAt(int index){
			return children.get(index);
		}

		/**
		 * Returns the children node count for this node.
		 * 
		 * @return	The children node count for this node.
		 */
		public int getChildCount(){
			return binding.size();
		}

		/**
		 * Returns the index of the given node among this node's
		 * children, or -1 if not found.
		 * 
		 * @param	node	The node for which the index is sought.
		 * @return	The index of the given node among this node's children.
		 */
		public int getIndex(TreeNode node){
			return children.indexOf(node);
		}

		/**
		 * Returns the parent node of this node, which is always <code>null</code>.
		 * 
		 * @return	The parent node of this node,  which is always <code>null</code>.
		 */
		public TreeNode getParent(){
			return null;
		}

		/**
		 * Always returns false.
		 * 
		 * @return	<tt>false</tt>.
		 */
		public boolean isLeaf(){
			return false;
		}
		
		/**
		 * Performs the initialization of children nodes.
		 */
		private void initChildren(){
			DataObject[] bindingData = binding.array();
			children = new LinkedList<DataObjectNode>();
			for(DataObject object : bindingData)
				children.add(new DataObjectNode(this, object, 0));
		}
		
		/**
		 * Removes the children cache.
		 */
		private void removeCaches(){
			if(children == null) return;
			dataObjectsRemovedFromBinding(0, children.size());
			
			children = null;
		}
		
		/**
		 * Needs to be called when new objects have been added to the
		 * <tt>Binding</tt>, this method will ensure that the added
		 * objects are also present in the <tt>BoundTreeModel</tt>, and
		 * fire appropriate tree events to call for a GUI update. The
		 * indices are both inclusive, matching the <tt>DataSourceEvent</tt>
		 * that triggered the calling of this method.
		 * 
		 * @param startIndex	The index of the first object added.
		 * @param endIndex		The index of the last object added.
		 */
		private void dataObjectsAddedToBinding(int startIndex, int endIndex){
			if(children == null) children = new LinkedList<DataObjectNode>();
			
			int count = endIndex - startIndex + 1;
			//	create nodes for inserted objects
			List<DataObjectNode> newNodes = new ArrayList<DataObjectNode>(count);
			for(int i = 0 ; i < count ; i++)
				newNodes.add(new DataObjectNode(this, binding.get(i + startIndex), 0));
				
			//	insert them into the children cache
			children.addAll(startIndex, newNodes);
			
			//	fire event
			//	first create an array of indices of the added children
			int[] indices = new int[count];
			for(int i = 0 ; i < count ; i++)
				indices[i] = startIndex + i;
			//	then fire away!
			fireTreeNodesInserted(new TreeModelEvent(
					BoundTreeModel.this,				//	source of change
					new Object[]{this},	//	path to the parent of inserted objects
					indices,			//	indices of inserted objects
					newNodes.toArray()	//	inserted objects
				));
			
		}
		
		/**
		 * Needs to be called when objects have been removed from the
		 * <tt>Binding</tt>, this method will ensure that the removed
		 * objects are also removed from the <tt>BoundTreeModel</tt>, and
		 * fire appropriate tree events to call for a GUI update. The
		 * indices are both inclusive, matching the <tt>DataSourceEvent</tt>
		 * that triggered the calling of this method.
		 * 
		 * @param startIndex	The index of the first object removed.
		 * @param endIndex		The index of the last object removed.
		 */
		private void dataObjectsRemovedFromBinding(int startIndex, int endIndex){
			//	if the lazy initialization of the children did not occur
			//	yet, it means that there is no TreeModel consumers currently
			//	using it... thereby, this event can be safely ignored, for
			//	the children (excluding those just removed) will be initialized
			//	when somebody actually asks for it
			if(children == null) return;
			
			int count = endIndex - startIndex + 1;
			
			//	get an sublist of the range removed
			List<DataObjectNode> removedRangeList = 
				children.subList(startIndex, endIndex + 1);
			
			//	cache the objects to be removed, for the event firing
			DataObjectNode[] removedNodes = removedRangeList.toArray(
					new DataObjectNode[count]);
			
			//	remove the nodes!
			removedRangeList.clear();
			
			//	fire event
			//	first create an array of indices of the added children
			int[] indices = new int[count];
			for(int i = 0 ; i < count ; i++)
				indices[i] = startIndex + i;
			//	then fire away!
			fireTreeNodesRemoved(new TreeModelEvent(
					BoundTreeModel.this,	//	source of change
					new Object[]{this},		//	path to the parent of removed objects
					indices,				//	indices of removed objects
					removedNodes			//	removed objects
				));
			
			//	now that the event has been fired and all, release the removed nodes
			for(DataObjectNode removedNode : removedNodes)
				removedNode.release();
		}
	}
}