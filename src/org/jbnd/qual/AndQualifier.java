package org.jbnd.qual;

import java.util.Iterator;
import java.util.List;

import org.jbnd.DataObject;


/**
 * A specialized <tt>CompundQualifier</tt> that returns <code>true</code> from
 * it's <tt>accept(DataObject)</tt> method if and only if all of the
 * <tt>Qualifier</tt>s it contains return <code>true</code> from theirs (the
 * boolean AND operation on all <tt>Qualifier</tt>s).
 * <p>
 * Note that if it contains no <tt>Qualifier</tt>s, the <tt>AndQualifier</tt>
 * will return <code>true</code> from it's <tt>accept(DataObject)</tt> method.
 * 
 * @version 1.0 Feb 7, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class AndQualifier extends AbstractCompoundQualifier{
	
	public AndQualifier(){}
	
	public AndQualifier(List<? extends Qualifier> qualifiers){
		super(qualifiers);
	}
	
	public AndQualifier(Qualifier... qualifiers){
		super(qualifiers);
	}

	public boolean accept(DataObject object){
		Iterator<Qualifier> it = iterator();
		while(it.hasNext())
			if(!it.next().accept(object))
				return false;
		
		return true;
	}
}