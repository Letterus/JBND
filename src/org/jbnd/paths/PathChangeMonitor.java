package org.jbnd.paths;

import org.jbnd.DataObject;


/**
 * An interface for objects interested in monitoring the changes occurring at an
 * arbitrary key path of a <tt>DataObject</tt>. Used in combination with a
 * <tt>KeyPathChangeManager</tt>.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.1, Sep 21, 2007
 * @see KeyPathChangeManager
 */
public interface PathChangeMonitor{

	/**
	 * Called when a change has been detected in a <tt>DataObject</tt> some
	 * point down it's path. The change will always have occured at the
	 * <tt>DataObject</tt>s path.
	 * 
	 * @param object The <tt>DataObject</tt> down whose path a change has
	 *            occured.
	 * @param path The path of the change.
	 */
	public void pathChanged(DataObject object, String path);
}