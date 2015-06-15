package org.jbnd.eof;

import org.jbnd.DataObject;
import org.jbnd.data.ListDataSource;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;


/**
 * JBND utility methods that are EOF specific.
 * 
 * @version 1.0 Jan 15, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class EOFUtil{
	
	/**
	 * Disallow instantiation.
	 */
	private EOFUtil(){};
	
	/**
	 * Fills up a <tt>ListDataSource</tt> with EOs from the given
	 * <tt>NSArray</tt>. If the given <tt>dataSource</tt> is <code>null</code>,
	 * then a new one is instantiated. The filled up <tt>ListDataSource</tt> is
	 * returned; if the given <tt>dataSource</tt> was not <code>null</code>, it
	 * is returned.
	 * <p>
	 * <b>IMPORTANT</b>: This is a method to support the unreliable
	 * implementation of the <tt>List</tt> interface on part of the
	 * <tt>NSArray</tt> class in versions of WO 5.3 and lower. If you are using
	 * a version of WO in which <tt>NSArray</tt> implements <tt>List</tt>
	 * properly, you can simply use the <tt>List</tt> accepting constructor of
	 * <tt>ListDataSource</tt>, and do not have to use this method.
	 * 
	 * @param dataSource See above.
	 * @param eos See above.
	 * @param clearFirst See above.
	 * @return See above.
	 */
	public static ListDataSource fill(
			ListDataSource dataSource, 
			NSArray<? extends EOEnterpriseObject> eos, 
			boolean clearFirst){
		
		//	prepare the data source
		if(dataSource == null) dataSource = new ListDataSource(eos.count());
		if(clearFirst) dataSource.clear();
		
		DataObject[] dataObjects = new DataObject[eos.count()];
		for(int i = 0 ; i < dataObjects.length ; i++)
			dataObjects[i] = (DataObject)eos.objectAtIndex(i);
		
		dataSource.addAll(dataObjects);
		
		return dataSource;
	}

	/**
	 * Unrelates the given <tt>eo</tt> from all of it's relationships that are
	 * set to <tt>nullify</tt> (in their deletion rules). This needs to be done
	 * in JBND based WOJC apps sometimes to ensure that the EO is removed from
	 * the GUI, because just deleting an <tt>eo</tt> in an
	 * <tt>EOEditingContext</tt> does not automatically unrelate it.
	 * 
	 * @param eo See above.
	 */
	public static void willDeleteEO(EOEnterpriseObject eo){
		
		EOClassDescription cd = eo.classDescription();
		
		// get all relationship keys
		NSArray<String>
			toManyRelationships = eo.toManyRelationshipKeys(),
			toOneRelationships = eo.toOneRelationshipKeys();
		
		// remove the object from all  to one relationships
		for(int i = 0, c = toOneRelationships.count() ; i < c ; i++){
			
			String key = (String)toOneRelationships.objectAtIndex(i);
			
			// only perform if the delete rule is set to "nullify"
			if(cd.deleteRuleForRelationshipKey(key) != EOClassDescription.DeleteRuleNullify)
				continue;
			
			EOEnterpriseObject relatedEO = (EOEnterpriseObject)eo.valueForKey(key);
			if(relatedEO != null){
				eo.removeObjectFromBothSidesOfRelationshipWithKey(relatedEO, key);
			}
		}
		
		//	remove the objects from all to many relationships
		for(int i = 0, c = toManyRelationships.count() ; i < c ; i++){
			
			String key = (String)toManyRelationships.objectAtIndex(i);
			
			// only perform if the delete rule is set to "nullify"
			if(cd.deleteRuleForRelationshipKey(key) != EOClassDescription.DeleteRuleNullify)
				continue;
			
			@SuppressWarnings("unchecked")
			NSArray<EOEnterpriseObject> relatedEOs = 
				(NSArray<EOEnterpriseObject>)eo.valueForKey(key);
			for(int j = 0 ; j < relatedEOs.count() ; j++)
				eo.removeObjectFromBothSidesOfRelationshipWithKey(
					(EOEnterpriseObject)relatedEOs.objectAtIndex(j), key);
		}
	}
}