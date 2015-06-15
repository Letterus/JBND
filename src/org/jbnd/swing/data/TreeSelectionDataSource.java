package org.jbnd.swing.data;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jbnd.DataObject;
import org.jbnd.DataType;
import org.jbnd.data.AbstractListDataSource;
import org.jbnd.swing.BoundTreeModel;
import org.jbnd.swing.BoundTreeModel.DataObjectNode;


/**
 * A <tt>DataSource</tt> whose data is the current selection of a
 * <tt>TreeSelectionModel</tt>. At a time when the selection in the changes it
 * is recommended that the model backing the tree is an instance of
 * <tt>BoundTreeModel</tt> (a model specialized in mapping <tt>DataObject</tt>s
 * into tree structures). The model will work regardless, but remember that only
 * <tt>DataObject</tt>s and <tt>BoundTreeModel.DataObjectNode</tt>s are valid
 * selections in the eyes of this tree selection listener.
 * <p>
 * Since at any given time a tree can contain <tt>DataObject</tt>s that are of
 * different <tt>DataType</tt>s, this <tt>DataSource</tt> can be provided with a
 * <tt>DataType</tt> object against which all the selected <tt>DataObject</tt>s
 * will be compared. If they are of a different type, they will not be included
 * in the selection. Another measure one might choose to take when using this
 * class is to set the <tt>JTree</tt> whose selection is provided to use a
 * single selection.
 * 
 * @version 1.0 Dec 30, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see BoundTreeModel
 * @see JTree#getSelectionModel()
 * @see TreeSelectionModel#setSelectionMode(int)
 */
public class TreeSelectionDataSource extends AbstractListDataSource implements
		TreeSelectionListener{
	
	//	the selection model whose selection is proxied as data
	private TreeSelectionModel selectionModel;
	
	//	the filtering DataType
	private DataType dataType;
	
	/**
	 * Creates a <tt>TreeSelectionDataSource</tt> that provides the current
	 * selection of the given <tt>tree</tt> as data (implements the
	 * <tt>DataSource</tt> interface).
	 * 
	 * @param tree The selection tree whose selection should be
	 *            provided as data.
	 */
	public TreeSelectionDataSource(JTree tree){
		this(tree.getSelectionModel());
	}
	
	/**
	 * Creates a <tt>TreeSelectionDataSource</tt> that provides the current
	 * selection of the given <tt>selectionModel</tt> as data (implements the
	 * <tt>DataSource</tt> interface).
	 * 
	 * @param selectionModel The selection model whose selection should be
	 *            provided as data.
	 */
	public TreeSelectionDataSource(TreeSelectionModel selectionModel){
		this.selectionModel = selectionModel;
		selectionModel.addTreeSelectionListener(this);
	}
	
	/**
	 * Creates a <tt>TreeSelectionDataSource</tt> that provides the
	 * current selection of the given <tt>selectionModel</tt> as data (implements
	 * the <tt>DataSource</tt> interface). This constructor ensures
	 * that only those <tt>DataObject</tt>s whose <tt>DataType</tt> is
	 * equal to the given <tt>dataType</tt> parameter will be provided
	 * as data.
	 * 
	 * @param selectionModel The selection model whose selection should be
	 *            provided as data.
	 * @param dataType	See above.
	 */
	public TreeSelectionDataSource(TreeSelectionModel selectionModel, DataType dataType){
		this(selectionModel);
		this.dataType = dataType;
	}
	
	/**
	 * Implementation of <tt>TreeSelectionListener</tt>, called when the
	 * selection in the tree changes.
	 * 
	 * @param e The event fired by the tree selection model.
	 */
	public void valueChanged(TreeSelectionEvent e){
		//	update to the latest selection
		_cacheCurrentSelection();
		
		//	clear the data this data source provides
		clear();
		
		//	add the new data
		addAll(selectionCache);
		
		//	make sure the selection cache is empty
		selectionCache.clear();
	}
	
	//	a cache of the current selection,
	//	filled up on request in the _dataObjectForSelectionNode(...) method
	//	used and cleared in the valueChanged(...) method
	private final List<DataObject> selectionCache = new LinkedList<DataObject>();
	
	/**
	 * Ensures that the <tt>selectionCache</tt> is synchronized with the
	 * current selection, that it contains all the selected <tt>DataObject</tt>s
	 * that have the same <tt>DataType</tt> as the one given to this
	 * <tt>TreeSelectionDataSource</tt>. If none was given,
	 * <tt>DataObject</tt>s of all types are acceptable.
	 */
	private void _cacheCurrentSelection(){
		TreePath[] selectedPaths = selectionModel.getSelectionPaths();
		if(selectedPaths == null) return;
		for(TreePath path : selectedPaths){
			DataObject object = _dataObjectForSelectedNode(path.getLastPathComponent());
			if(object != null) selectionCache.add(object);
		}
	}
	
	/**
	 * Returns the <tt>DataObject</tt> for the given tree node. If
	 * the given node object is neither a <tt>DataObject</tt> nor
	 * a <tt>BoundTreeModel.DataObjectNode</tt> instance, <tt>null</tt>
	 * is returned. If a <tt>DataObject</tt> is found for the node,
	 * but has a different <tt>DataType</tt> then the one given to
	 * this <tt>TreeSelectionDataSource</tt> (if one WAS given),
	 * <tt>null</tt> is returned. 
	 * 
	 * @param selectedNode	See above.
	 * @return				See above.
	 */
	private DataObject _dataObjectForSelectedNode(Object selectedNode){
		//	get the data object from the selected node
		DataObject object = null;
		if(selectedNode instanceof DataObject)
			object = (DataObject)selectedNode;
		else if(selectedNode instanceof BoundTreeModel.DataObjectNode)
			object = ((DataObjectNode)selectedNode).getDataObject();
		
		//	now, if DataType filtering is on, check if the selected DataObject
		//	is of the appropriate DataType
		if(object != null && dataType != null)
			return object.getDataType().equals(dataType) ? object : null;
		else
			return object;
	}
}