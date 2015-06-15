package org.jbnd.qual;

import java.util.Iterator;
import java.util.List;

import org.jbnd.DataObject;


/**
 * A specialized <tt>CompundQualifier</tt> that returns <code>true</code> from
 * it's <tt>accept(DataObject)</tt> method if any of the <tt>Qualifier</tt>s it
 * contains return <code>true</code> from theirs, and <code>false</code>
 * otherwise (the boolean OR operation on all <tt>Qualifier</tt>s).
 * <p>
 * Note that if it contains no <tt>Qualifier</tt>s, the <tt>OrQualifier</tt>
 * will return <code>false</code> from it's <tt>accept(DataObject)</tt> method.
 * 
 * @version 1.0 Feb 7, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class OrQualifier extends AbstractCompoundQualifier{

	public OrQualifier(){}
	
	public OrQualifier(List<Qualifier> qualifiers){
		super(qualifiers);
	}

	public boolean accept(DataObject object){
		Iterator<Qualifier> it = iterator();
		while(it.hasNext())
			if(it.next().accept(object))
				return true;
		
		return false;
	}
}