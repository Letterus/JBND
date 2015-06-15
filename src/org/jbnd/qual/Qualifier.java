package org.jbnd.qual;

import org.jbnd.DataObject;


/**
 * Defines an object that can qualify <tt>DataObject</tt>s. <tt>Qualifier</tt>s
 * are mutable objects that have no dependencies outside JBND, and are to be
 * used for in-memory filtering of <tt>DataObject</tt>s.
 * 
 * @version 1.0 Feb 6, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public interface Qualifier extends Filter<DataObject>{

}
