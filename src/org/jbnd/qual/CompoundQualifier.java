package org.jbnd.qual;

import java.util.Iterator;

import org.jbnd.event.FilterChangeListener;


/**
 * A <tt>Qualifier</tt> that consists of one or more other <tt>Qualifier</tt>s,
 * and defines it's qualification process through them. All the methods in this
 * class are defined like the methods in the <tt>java.util.List</tt> class, and
 * behave exactly like them.
 * 
 * @version 1.0 Feb 7, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public interface CompoundQualifier extends Qualifier, FilterChangeListener{
	
	/**
	 * Returns the number of <tt>Qualifier</tt>s this <tt>CompoundQualifier</tt>
	 * contains.
	 * 
	 * @return The number of <tt>Qualifier</tt>s this <tt>CompoundQualifier</tt>
	 *         contains.
	 */
	public int size();
	
	/**
	 * Returns the <tt>Qualifier</tt> at the given <tt>index</tt>.
	 * 
	 * @return The <tt>Qualifier</tt> at the given <tt>index</tt>.
	 * @throws IndexOutOfBoundsException If the index is out of bounds :)
	 */
	public Qualifier get(int index);
	
	/**
	 * Adds the given <tt>Qualifier</tt>s at the end of the list.
	 * 
	 * @param qualifiers To be added.
	 */
	public void add(Qualifier... qualifiers);
	
	/**
	 * Inserts the given <tt>Qualifier</tt> to the given position
	 * in the list.
	 * 
	 * @param qualifier	To be added.
	 * @throws	IndexOutOfBoundsException	If the index is out of
	 * 			bounds :)
	 */
	public void add(int index, Qualifier qualifier);
	
	/**
	 * Removes the given <tt>Qualifier</tt> from the list.
	 * 
	 * @param qualifier	To be removed.
	 * @return	If the given <tt>Qualifier</tt> was found and removed.
	 */
	public boolean remove(Qualifier qualifier);
	
	/**
	 * Removes the <tt>Qualifier</tt> at the given index from the list.
	 * 
	 * @param index Index of the <tt>Qualifier</tt> to be removed.
	 * @return The <tt>Qualifier</tt> removed.
	 * @throws IndexOutOfBoundsException If the index is out of bounds :)
	 */
	public Qualifier remove(int index);
	
	/**
	 * Replaces the <tt>Qualifier</tt> at the given position in this list with
	 * the given <tt>Qualifier</tt>.
	 * 
	 * @param index Index of the <tt>Qualifier</tt> to replace.
	 * @param qualifier The <tt>Qualifier</tt> to replace it with.
	 * @return The <tt>Qualifier</tt> previously at the given index.
	 * @throws IndexOutOfBoundsException If the index is out of bounds :)
	 */
	public Qualifier set(int index, Qualifier qualifier);
	
	/**
	 * Returns an <tt>Iterator</tt> for convenience.
	 * 
	 * @return An <tt>Iterator</tt> for convenience.
	 */
	public Iterator<Qualifier> iterator();
}
