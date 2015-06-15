package org.jbnd.qual;

import org.jbnd.DataObject;


/**
 * A specialized <tt>CompundQualifier</tt> that returns the opposite value from
 * the single <tt>Qualifier</tt> it contains (the boolean NOT operation).
 * <p>
 * Note that if it contains no <tt>Qualifier</tt>s, the <tt>NotQualifier</tt>
 * will return <code>true</code> from it's <tt>accept(DataObject)</tt> method.
 * 
 * @version 1.0 Feb 7, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class NotQualifier extends AbstractCompoundQualifier{

	public boolean accept(DataObject object){
		if(size() > 0)
			return !get(0).accept(object);
		else
			return true;
	}

	/**
	 * Overridden to do nothing unless the given <tt>index</tt> is
	 * 0. That ensures that the <tt>NotQualfier</tt> only ever
	 * contains a single <tt>Qualifier</tt>.
	 * 
	 * @param index	The index at which to add the given <tt>Qualifier</tt>.
	 * @param qualifier	To be added.
	 */
	public void add(int index, Qualifier qualifier){
		if(index == 0)
			super.add(index, qualifier);
	}

	/**
	 * Overridden to do nothing unless the given <tt>index</tt> is
	 * 0. That ensures that the <tt>NotQualfier</tt> only ever
	 * contains a single <tt>Qualifier</tt>.
	 * 
	 * @param index	Index of the <tt>Qualifier</tt> to be removed.
	 * @return	The <tt>Qualifier</tt> removed, or <tt>null</tt> if
	 * 			the given index was not 0.
	 * @throws	IndexOutOfBoundsException	If the index is out of
	 * 			bounds :)
	 */
	public Qualifier set(int index, Qualifier qualifier){
		if(index == 0)
			return super.set(index, qualifier);
		else
			return null;
	}
}