package org.jbnd.qual;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jbnd.DataObject;
import org.jbnd.event.FilterChangedEvent;


/**
 * Provides a basic implementation for a <tt>CompoundQualifier</tt>.
 * 
 * @version 1.0 Feb 7, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public abstract class AbstractCompoundQualifier
		extends AbstractFilter<DataObject>
		implements CompoundQualifier{
	
	/**
	 * A <tt>List</tt> of <tt>Qualifier</tt>s that this
	 * <tt>CompoundQualifier</tt> uses to qualify objects on, the exact way how
	 * this happens is determined by the concrete <tt>CompoundQualifier</tt>
	 * implementation.
	 */
	private final List<Qualifier> qualifiers = new CopyOnWriteArrayList<Qualifier>();
	
	/**
	 * Creates an empty <tt>AbstractCompoundQualifier</tt>.
	 */
	public AbstractCompoundQualifier(){}
	
	/**
	 * Creates an <tt>AbstractCompoundQualifier</tt> containing all the
	 * <tt>Qualifier</tt>s from the given list.
	 * 
	 * @param qualifiers The <tt>Qualifier</tt>s that should be added.
	 */
	public AbstractCompoundQualifier(List<? extends Qualifier> qualifiers){
		for(Qualifier qualifier : qualifiers)
			add(qualifier);
	}
	
	/**
	 * Creates an <tt>AbstractCompoundQualifier</tt> containing all the
	 * <tt>Qualifier</tt>s from the given array.
	 * 
	 * @param qualifiers The <tt>Qualifier</tt>s that should be added.
	 */
	public AbstractCompoundQualifier(Qualifier... qualifiers){
		for(Qualifier qualifier : qualifiers)
			add(qualifier);
	}

	public void add(int index, Qualifier qualifier){
		qualifier.addFilterChangeListener(this);
		qualifiers.add(index, qualifier);
		fireFilterChanged();
	}

	public void add(Qualifier... qualifiers){
		for(Qualifier q : qualifiers)
			q.addFilterChangeListener(this);
		this.qualifiers.addAll(Arrays.asList(qualifiers));
	}

	public Qualifier get(int index){
		return qualifiers.get(index);
	}

	public Qualifier remove(int index){
		Qualifier removedQual = qualifiers.remove(index);
		removedQual.removeFilterChangeListener(this);
		fireFilterChanged();
		return removedQual;
	}

	public boolean remove(Qualifier qualifier){
		boolean removed = qualifiers.remove(qualifier);
		if(removed){
			qualifier.removeFilterChangeListener(this);
			return true;
		}
		
		return false;
	}

	public Qualifier set(int index, Qualifier qualifier){
		Qualifier replacedQual = qualifiers.set(index, qualifier);
		replacedQual.removeFilterChangeListener(this);
		qualifier.addFilterChangeListener(this);
		fireFilterChanged();
		return replacedQual;
	}

	public int size(){
		return qualifiers.size();
	}
	
	public Iterator<Qualifier> iterator(){
		return qualifiers.iterator();
	}

	public void filterChanged(FilterChangedEvent e){
		fireFilterChanged();
	}
	
	public String toString(){
		String rVal = getClass().getName() + "[";
		Iterator<Qualifier> it = iterator();
		int i = 0;
		while(it.hasNext()){
			if(i++ != 0) rVal = rVal + ", ";
			rVal = rVal + it.next();
		}
		return rVal + "]";
	}
}