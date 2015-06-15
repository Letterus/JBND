package org.jbnd.swing.conn;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jbnd.DataObject;
import org.jbnd.DataType;
import org.jbnd.binding.Binding;
import org.jbnd.swing.BoundTreeModel;

/**
 * A concrete subclass of <tt>ComponentConnection</tt> used to connect
 * <tt>JTree</tt>s to a single value in a JBND <tt>Binding</tt>.
 * <p>
 * This connection expects the <tt>JTree</tt> it connects to be backed by a
 * <tt>BoundTreeModel</tt>, if it is not, a runtime exception will occur.
 * <p>
 * The value this component will extract from the tree depends on the tree's
 * selection mode. If it is <tt>TreeSelectionModel.SINGLE_TREE_SELECTION</tt>,
 * then only the first selected path's <tt>DataObject</tt> will be returned,
 * otherwise a <tt>List</tt> of <tt>DataObject</tt>s belong to all of the
 * selected paths will be returned.
 * <p>
 * Since at any given time a tree can contain <tt>DataObject</tt>s that are
 * of different <tt>DataType</tt>s, this <tt>TreeConnection</tt> can be
 * provided with a <tt>DataType</tt> object against which all the selected
 * <tt>DataObject</tt>s will be compared. If they are of a different type,
 * they will not be included in the value returned by
 * <tt>getComponentValue()</tt>. If no filtering <tt>DataType</tt> is
 * provided, the connection will be indiscriminate of different types of
 * <tt>DataObject</tt>s.
 * 
 * @version 1.0 Apr 17, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see BoundTreeModel
 */
public class TreeConnection extends ComponentConnection implements TreeSelectionListener{
	
	//	the filtering DataType
	private DataType dataType;
	
	/**
	 * Creates a <tt>TreeConnection</tt> with the given parameters.
	 * 
	 * @param tree The <tt>JTree</tt> to connect to a binding.
	 * @param binding The <tt>Binding</tt> to connect it to.
	 */
	@Deprecated
	public TreeConnection(JTree tree, Binding binding){
		super(tree, binding);
		tree.addTreeSelectionListener(this);
	}
	
	/**
	 * Creates a <tt>TreeConnection</tt> with the given parameters.
	 * 
	 * @param tree The <tt>JTree</tt> to connect to a binding.
	 * @param binding The <tt>Binding</tt> to connect it to.
	 * @param dataType The filtering <tt>DataType</tt> (tree selections that
	 *            are <tt>DataObject</tt>s of a different data type then this
	 *            one will be ignored.
	 */
	@Deprecated
	public TreeConnection(JTree tree, Binding binding, DataType dataType){
		super(tree, binding);
		tree.addTreeSelectionListener(this);
		this.dataType = dataType;
	}

	protected void bindingValueChanged(Object newValue){
		//	if the new value is a single data object
		//	make sure it is selected
		if(newValue instanceof DataObject){
			TreePath pathToNode = ((BoundTreeModel)((JTree)component).getModel())
				.pathForObject((DataObject)newValue);
			((JTree)component).setSelectionPath(pathToNode);
		
		}
		//	if the new value is a list of objects
		//	make sure that is selected
		else if(newValue instanceof List){
			Iterator<?> dataObjects = ((List<?>)newValue).iterator();
			TreePath[] pathsToNodes = new TreePath[((List<?>)newValue).size()];
			
			for(int i = 0 ; dataObjects.hasNext() ; i++)
				pathsToNodes[i] = ((BoundTreeModel)((JTree)component).getModel())
				.pathForObject((DataObject)dataObjects.next());
			
			((JTree)component).setSelectionPaths(pathsToNodes);
		}
	}

	protected Object getComponentValue(){
		//	if the tree is setup as single selection
		//	return the DataObject at the selected node
		if(((JTree)component).getSelectionModel().getSelectionMode() == 
			TreeSelectionModel.SINGLE_TREE_SELECTION){
			
			TreePath selectionPath = ((JTree)component).getSelectionPath();
			DataObject rVal = selectionPath == null ? null : 
				((BoundTreeModel.DataObjectNode)selectionPath.getLastPathComponent()).getDataObject();
			
			//	if there is a filtering data type present
			//	check if the return value is of that type
			if(dataType != null && rVal != null && rVal.getDataType() != dataType)
				return null;
			else
				return rVal;
		//	otherwise return DataObjects for all selected nodes
		}else{
			
			List<DataObject> rVal = new LinkedList<DataObject>();
			TreePath[] selectedPaths = ((JTree)component).getSelectionPaths();
			if(selectedPaths == null) return rVal;
			for(TreePath path : selectedPaths)
				rVal.add(((BoundTreeModel.DataObjectNode)path.getLastPathComponent()).getDataObject());
			
			//	if there is a filtering data type present
			//	remove objects of a different type
			if(dataType != null){
				ListIterator<DataObject> it = rVal.listIterator();
				while(it.hasNext())
					if(it.next().getDataType() != dataType) it.remove();
			}
			
			return rVal;
		}
	}

	public void valueChanged(TreeSelectionEvent e){
		componentValueChanged();
	}
}