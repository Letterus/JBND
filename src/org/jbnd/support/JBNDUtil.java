package org.jbnd.support;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;

import org.jbnd.DataObject;
import org.jbnd.DataType;
import org.jbnd.UnknownKeyException;
import org.jbnd.ValidationException;
import org.jbnd.DataType.PropType;
import org.jbnd.binding.AddEditGroup.CachingDataObject;
import org.jbnd.data.DataSource;
import org.jbnd.qual.Filter;
import org.jbnd.qual.Qualifier;


/**
 * Defines static methods used at various places.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, Sep 28, 2007
 *
 */
public final class JBNDUtil{
	
	/*
	 * Disallow instantiation and subclassing.
	 */
	private JBNDUtil(){
	}
	
	/**
	 * Returns <tt>true</tt> if the given <tt>String</tt> contains more then one key.
	 *
	 * 
	 * @param key	The <tt>String</tt> to test.
	 * @return	<tt>true</tt> if the given <tt>String</tt> contains more then one key,
	 * 				meaning, if there is a '.' character present in it.
	 */
	public static boolean isKeyPath(String key){
		return key.indexOf('.') > 0;
	}

	/**
	 * Returns a keypath that contains all the keys of the <tt>keypath</tt> parameter
	 * except for the last one. For example, for 'child.pet.name' as a parameter,
	 * 'child.pet' is returned.
	 * If given anything else then a standard key(path) the results are undefined.
	 * 
	 * @param keypath	The original keypath.
	 * @return	The original keypath, without the last key.
	 * @throws	IllegalArgumentException	If the <tt>keypath</tt> parameter is not
	 * 				a keypath (does not contain the '.' separator).
	 */
	public static String keyPathWithoutLastKey(String keypath){
		int x = keypath.lastIndexOf('.');
		if(x < 0) throw new IllegalArgumentException("'"+keypath+"' is not a key path");
		return keypath.substring(0, x);
	}
	
	/**
	 * Returns a keypath that contains all the keys of the <tt>keypath</tt> parameter
	 * except for the first one. For example, for 'child.pet.name' as a parameter,
	 * 'pet.name' is returned.
	 * If given anything else then a standard key(path) the results are undefined.
	 * 
	 * @param keypath	The original keypath.
	 * @return	The original keypath, without the first key.
	 * @throws	IllegalArgumentException	If the <tt>keypath</tt> parameter is not
	 * 				a keypath (does not contain the '.' separator).
	 */
	public static String keyPathWithoutFirstKey(String keypath){
		int x = keypath.indexOf('.');
		if(x < 0) throw new IllegalArgumentException("'"+keypath+"' is not a key path");
		return keypath.substring(x + 1, keypath.length());
	}

	/**
	 * Returns the first key of the <tt>keypath</tt> parameter. For example, for
	 * 'child.pet.name' as a parameter, 'child' is returned. For 'child' as a
	 * parameter, the same <tt>String</tt> is returned.
	 * 
	 * @param keypath The original keypath.
	 * @return The first key of the original keypath.
	 */
	public static String firstKeyOfKeyPath(String keypath){
		int x = keypath.indexOf('.');
		if(x < 0) return keypath;
		return keypath.substring(0, x);
	}
	
	/**
	 * Returns the last key of the <tt>keypath</tt> parameter. For example, for
	 * 'child.pet.name' as a parameter, 'name' is returned. For 'child' as a
	 * parameter, the same <tt>String</tt> is returned.
	 * 
	 * @param keypath The original keypath.
	 * @return The last key of the original keypath.
	 */
	public static String lastKeyOfKeyPath(String keypath){
		int x = keypath.lastIndexOf('.');
		if(x < 0) return keypath;
		return keypath.substring(x + 1);
	}
	
	/**
	 * Breaks down a keypath into an array of the keys it contains. If given
	 * anything else then a standard keypath the results are undefined.
	 * 
	 * @param keypath The original keypath.
	 * @return An array of <tt>String</tt>s, the keys of the original keypath.
	 */
	public static String[] keys(String keypath){
		return keypath.split("\\.");
	}
	
	/**
	 * Generates a clean path from any standard path, eliminating all the data
	 * storage implementation specific path elements (like for example EOF
	 * selectors). For example if given the "children.pets.#NON_KEY_ELEMENT#"
	 * path, it will return "children.pets". The elimination process is done by
	 * comparing every path element with an array of regular expressions (
	 * <tt>NON_KEY_ELEMENTS</tt>). If a path element matches any of the regular
	 * expressions, it is removed from the path.
	 * <p>
	 * To add regular expressions of data storage implementation specific path
	 * elements that are not yet present, modify the source code of this file,
	 * specifically the <tt>NON_KEY_ELEMENTS</tt> constant.
	 * 
	 * @param path A path potentially containing non standard keys.
	 * @return The same path, without non standard keys; see above.
	 */
	public static String cleanPath(String path){
		
		//	split the given path to keys (split on the '.' character)
		String[] keys = JBNDUtil.keys(path);
		
		//	the buffer to construct the results
		StringBuffer buff = new StringBuffer();
		outerLoop:
		for(String key : keys){
			
			//	loop through regular expressions, look for matches
			for(String regex : NON_KEY_ELEMENTS){
				if(Pattern.matches(regex, key)) continue outerLoop;
			}
			
			if(buff.length() != 0) buff.append(".");
			buff.append(key);
		}
		
		//	return the results
		return buff.toString();
	}
	
	/**
	 * An array of regular expressions used to eliminate non  standard key elements
	 * (such as data storage specific keys) from a key path, by the
	 * <tt>cleanPath(String)</tt> method.<p>
	 * 
	 * Currently present regular expressions:
	 * <ul>
	 * <li>EOF selector expression (regex: '@.*')</li>
	 * <li>JBND's convention for extension (non property) keys (regex: '!.*')</li>
	 * </ul>
	 * 
	 * @see	#cleanPath(String)
	 */
	public static final String[] NON_KEY_ELEMENTS = new String[]{
		"\\@.*",
		"\\!.*"
	};
	
	/**
	 * Sets the given value on the given <tt>DataObject</tt> for the given
	 * key(path), using appropriate methods (property type determined through
	 * the <tt>key</tt>). The <tt>set()</tt> method is used in case of object
	 * attributes being set, and when the <tt>DataObject</tt> is a
	 * <tt>CachingDataObject</tt>. <tt>relate()</tt> is used in case of to-one
	 * relationships, and in that case the <tt>value</tt> is assumed to be
	 * another <tt>DataObject</tt>. If setting a to-many relationship the
	 * <tt>value</tt> is assumed to be either a <tt>List</tt> of
	 * <tt>DataObject</tt>s or a single <tt>DataObject</tt>, and
	 * <tt>relate()</tt> is used with each object in it, individually.
	 * <p>
	 * This method support keypath editing, but only for keypaths that consist
	 * of a chain of to-one relationships. The last key in the path can be a
	 * property of any kind (attribute, to-one, to-many).
	 * <p>
	 * <b>IMPORTANT</b>: No validation is performed, make sure you supply this
	 * method with valid arguments otherwise arbitrary exceptions may be thrown.
	 * 
	 * @param value The new value.
	 * @param key The key(path) for which the new value is to be set.
	 * @param object The <tt>DataObject</tt> on which to set the new value.
	 */
	public static void set(Object value, String key, DataObject object){
		
		// caching data object?
		if(object instanceof CachingDataObject){
			object.set(value, key);
			return;
		}
		
		//	if we are talking about key path editing,
		//	then take care of it here
		if(JBNDUtil.isKeyPath(key)){
			//	key path separation
			String pathWithoutLastKey = keyPathWithoutLastKey(key),
				lastKey = lastKeyOfKeyPath(key);
			
			//	get the data object that should be edited
			DataObject objectAtPath = (DataObject)get(object, pathWithoutLastKey);
			if(objectAtPath == null)
				throw new RuntimeException("No object at path: "+pathWithoutLastKey);
			
			//	and then edit it!
			set(value, lastKey, objectAtPath);
			return;
		}
		
		//	perform the right kind of value setting
		//	depending on the type of the property being set
		PropType propertyType = object.getDataType().propertyType(key);
		
		// derived properties should be skipped
		if(propertyType.isDerived()) return;
		
		if(propertyType == PropType.ATTRIBUTE){
			//	it is an attribute we are talking about
			//	try to convert the value, if necessary
			Object convertedValue = ValueConverter.toClass(
					value, object.getDataType().classForAttribute(key));
			//	set the value
			object.set(convertedValue, key);
			
		
		}else if(propertyType == PropType.TO_ONE){
			//	it is a to one relationship
			if(value != null)
				object.relate((DataObject)value, key);
			else{
				DataObject currentRel = (DataObject)object.get(key);
				if(currentRel != null) object.unrelate(currentRel, key);
			}
		
		}else if(propertyType == PropType.TO_MANY){
			// it is a to many relationship
			//	it could be a single object, in which case include it
			//	or it could be a list of objects
			if(value instanceof List){
				@SuppressWarnings("unchecked")
				List<DataObject> toManyObjects = (List<DataObject>)value;
				for(DataObject oneOfMany : toManyObjects)
					object.relate(oneOfMany, key);
			}else if(value instanceof DataObject)
				object.relate((DataObject)value, key);
			else
				throw new IllegalArgumentException(
					"Can not relate object of class '"+
					object.getClass().getName()+"' to "+
					object.toLogString()+ " for key '"+key+"'");
				
		}else
			throw new UnknownKeyException(key, object);
	}
	
	/**
	 * Retrieves the value found at the given keypath in the given object, even
	 * if the implementing persistence system does not support key-paths. If a
	 * relationship can not be resolved somewhere during the path (for example a
	 * Pet object is sought in the Person object, but the Person has no Pets)
	 * this method will return <tt>null</tt> and no exception will be thrown.
	 * 
	 * @param object The <tt>DataObject</tt> from which to get the value.
	 * @param keypath The path at which to find the value.
	 * @return The value found in the <tt>object</tt> at the </tt>keypath</tt>,
	 *         or <tt>null</tt>, see method description for more details.
	 */
	public static Object get(DataObject object, String keypath){
		
		if(isKeyPath(keypath)){
			
			String firstKey = firstKeyOfKeyPath(keypath),
				theRest = keyPathWithoutFirstKey(keypath);
			
			DataObject objectAtFirstKey = (DataObject)object.get(firstKey);
			if(objectAtFirstKey == null) return null;
			
			return get(objectAtFirstKey, theRest);
		}else
			return object.get(keypath);
	}
	
	/**
	 * Retrieves the final <tt>DataType</tt> of the relationship defined by
	 * <tt>keypath</tt> in <tt>dataType</tt>. For example, providing this method
	 * with a 'Person' <tt>DataType</tt>, and the 'children.pet' keypath causes
	 * it to return a 'Pet' <tt>DataType</tt>. Providing it with the
	 * 'children.pet.name' keypath will cause it to return the same: the 'Pet'
	 * <tt>DataType</tt>.
	 * 
	 * @param dataType The <tt>DataType</tt> from which to start.
	 * @param keypath The path defining the relationship for which to find the
	 *            <tt>DataType</tt>.
	 * @return See above.
	 */
	public static DataType dataTypeForRelationship(DataType dataType, String keypath){
		
		// first clean the path
		keypath = cleanPath(keypath);
		
		// if it is a keypath, resolve the first key, and then recurse
		if(isKeyPath(keypath)){
			String firstKey = firstKeyOfKeyPath(keypath),
				theRest = keyPathWithoutFirstKey(keypath);
			
			DataType typeAtFirstKey = dataType.dataTypeForRelationship(firstKey);
			
			return dataTypeForRelationship(typeAtFirstKey, theRest);
		
		// if it's not a keypath, resolve the first key and return the result
		}else{
			switch(dataType.propertyType(keypath)){
				case TO_ONE : case TO_MANY : case DERIVED_TO_ONE : case DERIVED_TO_MANY :
					return dataType.dataTypeForRelationship(keypath);
				default :
					return dataType;
			}
		}
	}
	
	/**
	 * Retrieves the <tt>Class</tt> of the attribute that is defined by
	 * <tt>keypath</tt> in the <tt>dataType</tt>. For example, providing this
	 * method with a 'Person' <tt>DataType</tt>, and the 'children.pet.name'
	 * keypath causes it to return a <tt>String.class</tt> instance (if the
	 * 'Pet' type's name is a <tt>String</tt> attribute). However, this method
	 * is not tolerant of in-exact keypaths, so if a keypath points to other
	 * then an attribute, this method will throw an <tt>UnknownKeyException</tt>
	 * .
	 * 
	 * @param dataType The <tt>DataType</tt> from which to start.
	 * @param keypath The path defining the relationship for which to find the
	 *            attribute class.
	 * @return See above.
	 * @throws UnknownKeyException If the <tt>keypath</tt> does not point to an
	 *             attribute when resolved in the <tt>dataType</tt>.
	 */
	public static Class<?> classForAttribute(DataType dataType, String keypath){
		
		// first clean the path
		keypath = cleanPath(keypath);
		
		// if it is a keypath, resolve the first key, and then recurse
		if(isKeyPath(keypath)){
			String firstKey = firstKeyOfKeyPath(keypath),
				theRest = keyPathWithoutFirstKey(keypath);
			
			DataType typeAtFirstKey = dataType.dataTypeForRelationship(firstKey);
			
			return classForAttribute(typeAtFirstKey, theRest);
		
		// if it's not a keypath, resolve the first key and return the result
		}else
			return dataType.classForAttribute(keypath);
	}
	
	/**
	 * Retrieves the <tt>Class</tt> of the property that is defined by
	 * <tt>keypath</tt> in the <tt>dataType</tt>. For example, providing this
	 * method with a 'Person' <tt>DataType</tt>, and the 'children.pet.name'
	 * keypath causes it to return a <tt>String.class</tt>. Providing it with
	 * the same <tt>DataType</tt> and the 'children.pet' causes it to return
	 * <tt>DataObject</tt>, as it points to a to-one relationship.
	 * 
	 * @param dataType The <tt>DataType</tt> from which to start.
	 * @param keypath The path defining the relationship for which to find the
	 *            attribute class.
	 * @return See above.
	 * @throws UnknownKeyException If it can't be determined with certainty what
	 *             the <tt>Class</tt> of the property at the end of the keypath
	 *             is.
	 */
	public static Class<?> classForProperty(DataType dataType, String keypath){
		
		// first clean the path
		keypath = cleanPath(keypath);
		
		// if it is a keypath, resolve the first key, and then recurse
		if(isKeyPath(keypath)){
			String firstKey = firstKeyOfKeyPath(keypath),
				theRest = keyPathWithoutFirstKey(keypath);
			
			DataType typeAtFirstKey = dataType.dataTypeForRelationship(firstKey);
			
			return classForProperty(typeAtFirstKey, theRest);
		
		// if it's not a keypath, resolve the first key and return the result
		}else{
			switch(dataType.propertyType(keypath)){
				case ATTRIBUTE : case DERIVED_ATTRIBUTE : 
					return dataType.classForAttribute(keypath);
				case TO_ONE : case DERIVED_TO_ONE :
					return DataObject.class;
				case TO_MANY : case DERIVED_TO_MANY :
					return List.class;
				default : throw new UnknownKeyException(keypath, dataType);
			}
		}
	}
	
	/**
	 * Returns the <tt>PropType</tt> of the property found at the
	 * <tt>keypath</tt> of the given <tt>dataType</tt>, different from
	 * <tt>DataType.propertyType(String)</tt> only in that it can handle
	 * keypaths.
	 * 
	 * @param dataType See above.
	 * @param keypath See above.
	 * @return See above.
	 */
	public static PropType propertyType(DataType dataType, String keypath){
		
		// first clean the path
		keypath = cleanPath(keypath);
		
		// if it is a keypath, resolve the first key, and then recurse
		if(isKeyPath(keypath)){
			String firstKey = firstKeyOfKeyPath(keypath),
				theRest = keyPathWithoutFirstKey(keypath);
			
			DataType typeAtFirstKey = dataType.dataTypeForRelationship(firstKey);
			
			return propertyType(typeAtFirstKey, theRest);
		
		// if it's not a keypath, use the key
		}else{
			return dataType.propertyType(keypath);
		}
	}
	
	/**
	 * Validates a <tt>value</tt> on an <tt>object</tt> across a
	 * <tt>keypath</tt>. This method will throw a <tt>ValidationException</tt>
	 * if: there is no <tt>DataObject</tt> found at (<tt>keypath</tt> without
	 * the last key element), or if there is a single <tt>DataObject</tt> found,
	 * but the given <tt>value</tt> is not valid when tested against it.
	 * 
	 * @param object The <tt>DataObject</tt> on which the validate the value.
	 * @param value The value to validate.
	 * @param keypath The path on which to validate it.
	 * @return The possibly coerced value, according to the contract of
	 *         <tt>org.jbnd.DataObject#validate(Object, String)</tt>.
	 * @throws ValidationException See above.
	 */
	public static Object validate(DataObject object, Object value, String keypath)
		throws ValidationException
	{
		keypath = cleanPath(keypath);
		
		if(!isKeyPath(keypath))
			return object.validate(value, keypath);
		
		//	get the object on which the validation
		//	of the value should actually occur
		Object o = get(object, keyPathWithoutLastKey(keypath));
		if(o == null || !(o instanceof DataObject))
			throw new ValidationException(value, keypath, object);
		
		return ((DataObject)o).validate(value, lastKeyOfKeyPath(keypath));
	}
	
	/**
	 * Checks if the given object is a <tt>String</tt>, if so, and if it is
	 * <tt>null</tt> OR if it contains no characters, <tt>null</tt> is returned.
	 * Otherwise the same <tt>Object</tt> that was passed to the method is
	 * returned.
	 * 
	 * @param s The object to check.
	 * @return See above.
	 */
	public static <T> T checkForEmptyString(T s){
		if(s instanceof String){
			String string = (String)s;
			if(string == null || string.length() == 0)
				s = null;
		}
		
		return s;
	}
	
	/**
	 * Trims down <tt>String</tt>s, and if that results in an
	 * empty <tt>String</tt>, returns <tt>null</tt>.
	 * 
	 * @param s The object to check.
	 * @return	See above.
	 */
	public static String supertrim(String s){
		s = s == null ? "" : s.trim();
		return s.length() == 0 ? null : s;
	}
	
	/**
	 * Returns the index of an object in the given <tt>array</tt> that is equal
	 * (according to the {@link #equals(Object, Object)} method) to the given
	 * <tt>object</tt> parameter. If the given array is sorted (using a natural
	 * sort ordering, as described in <tt>java.util.Arrays.sort(Object[])</tt>,
	 * that can be indicated with the <tt>sorted</tt> parameter, which will
	 * improve the performance of this method by using a binary search. If there
	 * is no object in the array that is equal to the <tt>object</tt> parameter,
	 * -1 is returned.
	 * 
	 * @param array The array to search in for an object equal to the
	 *            <tt>object</tt> param.
	 * @param object The object to search an object equal to.
	 * @param sorted If the given <tt>array</tt> is sorted in the natural order
	 *            of it's objects.
	 * @return The index of an object in the given <tt>array</tt> that is equal
	 *         to the <tt>object</tt> parameter, or -1 if not found.
	 * @see #equals(Object, Object)
	 */
	public static int indexOf(Object[] array, Object object, boolean sorted){
		
		//	a simple search algorithm for unsorted arrays
		if(!sorted){
			int index = 0;
			for(Object element : array){
				if(equals(element, object))
					return index;
				index++;
			}
			
			return -1;
		}
		
		//	an algorithm for sorted arrays
		int index = Arrays.binarySearch(array, object);
		//	if the found index is out of bounds, return -1
		if(index < 0 || index >= array.length) return -1;
		//	if the index is within bounds, check for object equality
		return equals(array[index], object) ? index : -1;
	}
	
	/**
	 * Returns the lowest index of a <tt>DataObject</tt> that is equal to the
	 * given <tt>object</tt> according to the {@link #equals(Object, Object)}
	 * method, of -1 if such an object is not found.
	 * 
	 * @param object The object to look for, <tt>null</tt> is acceptable.
	 * @param dataSource The <tt>DataSource</tt> to look in.
	 * @return The lowest index of a <tt>DataObject</tt> that is equal to the
	 *         given <tt>Object</tt>, or -1 if not found.
	 */
	public static int indexOf(DataObject object, DataSource dataSource){
		
		// don't deal with CachingDataObject
		while(object instanceof CachingDataObject)
			object = ((CachingDataObject)object).getBackingDataObject();
		
		int bindingSize = dataSource.size();
		for(int i = 0 ; i < bindingSize ; i++){
			DataObject dsObject = dataSource.get(i);
			
			// don't deal with CachingDataObject
			while(dsObject instanceof CachingDataObject)
				dsObject = ((CachingDataObject)dsObject).getBackingDataObject();
			
			if(JBNDUtil.equals(dsObject, object))
				return i;
		
		}
		
		return -1;
	}
	
	/**
	 * Compares two objects in a <tt>null</tt> friendly way: if both parameters
	 * are <tt>null</tt>, then <tt>true</tt> is returned; if only one of them is
	 * <tt>null</tt>, then <tt>false</tt> is returned, otherwise
	 * <tt>a.equals(b)</tt> is returned.
	 * 
	 * @param a The first object.
	 * @param b The second object.
	 * @return See above.
	 */
	public static boolean equals(Object a, Object b){
		return a == null ? b == null : a.equals(b);
	}

	/**
	 * Returns an <tt>Enumeration</tt> that is backed by the given
	 * <tt>Iterator</tt>, allowing legacy code that still uses
	 * <tt>Enumeration</tt>s to go over objects provided through
	 * <tt>Iterator</tt>s.
	 * 
	 * @param iterator	The iterator that provides the data
	 * 					to the returned <tt>Enumeration</tt>.
	 * @return			An <tt>Enumeration</tt> containing all
	 * 					the <tt>Object</tt>s of the given
	 * 					<tt>Iterator</tt>.
	 */
	public static Enumeration<?> enumFromIt(final Iterator<?> iterator){
		return new Enumeration<Object>(){
			public boolean hasMoreElements(){ return iterator.hasNext();}
			public Object nextElement(){return iterator.next();}
		};
	}
	
	/**
	 * Encapsulates an arbitrary number of {@code Comparator<? super T>} objects
	 * from the given <tt>List</tt> into a single {@code Comparator<T>} that
	 * will iterate though the <tt>List</tt>, and return the first (!=0) value
	 * that a contained <tt>Comparator</tt> returns from it's
	 * <tt>compare(...)</tt> method. If all of the given <tt>comparators</tt>
	 * return 0, then this method returns 0.
	 * 
	 * @param comparators	See above.
	 * @return	See above.
	 */
	public static <T> Comparator<T> encapsulatedComparators(
			final List<Comparator<T>> comparators){
		
		return new Comparator<T>(){

			public int compare(T o1, T o2){
				
				for(Comparator<? super T> c : comparators){
					int compareVal = c.compare(o1, o2);
					if(compareVal != 0) return compareVal;
				}
				
				return 0;
			}};
	}
	
	/**
	 * Returns a <tt>List</tt> containing all objects from the
	 * <tt>biggerList</tt>, reduced for all the objects from the
	 * <tt>smallerList</tt>. The <tt>bigger</tt> list does not
	 * necessarily need to be bigger. Both lists do not need to
	 * be sorted, the sorting will have no influence on the result.
	 * The returned value will be only contain objects from the
	 * <tt>biggerList</tt>, ordered in the same way, with possible
	 * gaps.
	 * 
	 * @param biggerList	See above.
	 * @param smallerList	See above.
	 * @return	See above.
	 */
	public static <T> List<T> reduce(List<T> biggerList, List<T> smallerList){
		List<T> rVal = new LinkedList<T>();
		for(T object : biggerList){
			if(smallerList.contains(object)) continue;
			rVal.add(object);
		}
		
		return rVal;
	}
	
	/**
	 * Returns a <tt>List</tt> containing all the <tt>DataObject</tt>s from the
	 * given <tt>DataSource</tt> that are accepted by the given
	 * <tt>Qualifier</tt>. The returned object is an <tt>ArrayList</tt> that has
	 * a capacity that is equal to the size of the given <tt>DataSource</tt>.
	 * 
	 * @param ds The <tt>DataSource</tt> to narrow down.
	 * @param qual The <tt>Qualifier</tt> that determines which objects are
	 *            acceptable.
	 * @return See above.
	 */
	public static List<DataObject> filter(DataSource ds, Qualifier qual){
		List<DataObject> rVal = new ArrayList<DataObject>(ds.size());
		for(int i = 0, c = ds.size() ; i < c ; i++){
			DataObject o = ds.get(i);
			if(qual.accept(o))
				 rVal.add(o);
		}
		
		return rVal;
	}

	/**
	 * Returns an array that consist of the elements of the passed
	 * <tt>array</tt> that are accepted by the <tt>filter</tt>.
	 * 
	 * @param <T>
	 * @param array The array to filter down.
	 * @param filter The filter to use.
	 * @return See above.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] filteredArray(T[] array, Filter<T> filter){
		
		// filter into a list
		List<T> filteredList = new LinkedList<T>();
		for(T t : array)
			if(filter.accept(t)) filteredList.add(t);
		
		return filteredList.toArray((T[])Array.newInstance(array.getClass()
				.getComponentType(), filteredList.size()));
	}
	
	/*
	 * 
	 * Some STRING stuff
	 * 
	 */
	
	/**
	 * For a not <tt>null</tt> object this method returns a <tt>String</tt>
	 * composed of it's <tt>toString()</tt> value, and a single white-space
	 * placed both in front and after it.
	 * 
	 * @param s	See above.
	 * @return	See above.
	 */
	public static String pad(Object s){
		return padRight(padLeft(s));
	}
	
	/**
	 * For a not <tt>null</tt> object this method returns a <tt>String</tt>
	 * composed of it's <tt>toString()</tt> value, and a single white-space
	 * placed in front of it.
	 * 
	 * @param s	See above.
	 * @return	See above.
	 */
	public static String padLeft(Object s){
		return s == null ? null : " "+s.toString();
	}
	
	/**
	 * For a not <tt>null</tt> object this method returns a <tt>String</tt>
	 * composed of it's <tt>toString()</tt> value, and a single white-space
	 * placed in after it.
	 * 
	 * @param s	See above.
	 * @return	See above.
	 */
	public static String padRight(Object s){
		return s == null ? null : s.toString()+" ";
	}
	
	/**
	 * For a not <tt>null</tt> object this method returns a <tt>String</tt>
	 * composed of it's <tt>toString()</tt> value, with quotation characters
	 * placed both in front and after it.
	 * 
	 * @param s	See above.
	 * @return	See above.
	 */
	public static String quote(Object s){
		return s == null ? null : "\""+s.toString()+"\"";
	}
	
	/**
	 * Casts an entire array of <tt>Object</tt>s into an array of another type.
	 * 
	 * @param <T> The type to which the given <tt>Object[]</tt> should be cast.
	 * @param array The array to cast. All of the <tt>Object</tt>s contained
	 *            should be instances of <tt>T</tt>.
	 * @param clazz The type to which the given <tt>Object[]</tt> should be
	 *            cast.
	 * @return The same array passed as a parameter, cast into <tt>T[]</tt>.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] castArray(Object[] array, Class<T> clazz){
		T[] rVal = (T[])Array.newInstance(clazz, array.length);
		
		for(int i = 0 ; i < array.length ; i++)
			rVal[i] = (T)array[i];
		
		return rVal;
	}
	
	/**
	 * Returns an array that consists of all the objects in
	 * <tt>array</tt> as well as <tt>objects</tt>.
	 * 
	 * @param <T>
	 * @param array
	 * @param objects
	 * @return	See above.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] augmentArray(T[] array, T... objects){
		if(objects == null || objects.length == 0)
			return array;
		
		T[] rVal = (T[])Array.newInstance(
				objects.getClass().getComponentType(), array.length
						+ objects.length);
		
		for(int i = 0 ;  i < array.length ; i++)
			rVal[i] = array[i];
		for(int i = 0 ; i < objects.length; i++)
			rVal[i + array.length] = objects[i];
		
		return rVal;
	}
	
	// used in the normalize(...) method
	private static final Calendar GMTCalendar = 
		Calendar.getInstance(TimeZone.getTimeZone("GMT"));

	/**
	 * Takes a <tt>Date</tt> and a <tt>TimeZone</tt>, and returns a normalized
	 * version of it. The returned <tt>Date</tt> that has the time of 11:30:00
	 * GMT. It's year, month and day are same as the given <tt>original</tt> has
	 * in the given <tt>timezone</tt>. If <tt>timezone</tt> is <tt>null</tt>,
	 * the local timezone is assumed to apply.
	 * <p>
	 * The returned <tt>Date</tt> will, when formatted, have the same year,
	 * month, and day as the original date has in it's timezone, regardless of
	 * the timezone it is represented in.
	 * 
	 * @param original The date to normalize.
	 * @param timezone The timezone in which the <tt>original</tt> is in, if
	 *            <tt>null</tt>, the local timezone is assumed to apply.
	 * @return See above.
	 */
	public static Date normalize(Date original, TimeZone timezone){

		// first, get a calendar for the original timezone
		Calendar c = Calendar.getInstance(
				timezone == null ? TimeZone.getDefault() : timezone);
		c.setTime(original);
		
		// make a date using the GMT calendar
		GMTCalendar.set(
				c.get(Calendar.YEAR),
				c.get(Calendar.MONTH),
				c.get(Calendar.DATE),
				11, 30, 0);
		GMTCalendar.roll(0, Calendar.MILLISECOND);
		
		return GMTCalendar.getTime();
	}
	
	/**
	 * Searches among <tt>classes</tt> for the closest superclass,
	 * super-interface or implemented interface of <tt>sought</tt>. The
	 * <tt>allowInterfaces</tt> parameter controls whether interfaces contained
	 * in <tt>classes</tt> should be considered as valid matches. If
	 * <tt>sought</tt> itself is an interface, the <tt>allowInterfaces</tt>
	 * param is ignored.
	 * <p>
	 * If <tt>sought</tt> is an implementation (abstract or concrete), and
	 * <tt>allowInterfaces</tt> is <tt>true</tt>, the rules for determining the
	 * best match are as follows:
	 * <ol>
	 * <li>If there is an exact match in <tt>classes</tt>, return it.</li>
	 * <li>If there is a superclass of <tt>sought</tt>, return the one
	 * hierarchically closest.</li>
	 * <li>If there are no superclasses, and there are interfaces that
	 * <tt>sought</tt> implements, return the one that has fewest
	 * super-interfaces.</li>
	 * <li>If there are interfaces with the same number of super-interfaces
	 * competing, return the one with the lowest index in <tt>classes</tt>.</li>
	 * <li>If no match is found, return <tt>null</tt>.
	 * </ol>
	 * 
	 * @param <T> See method signature.
	 * @param classes A number of <tt>Class</tt> instances to search for a match
	 *            among.
	 * @param sought The <tt>Class</tt> whose superclass, super-interface or
	 *            implemented interface is sought.
	 * @param allowInterfaces See above.
	 * @return See above.
	 */
	public static <T> Class<? super T> findClosestToAssignTo(
			Iterable<Class<?>> classes, Class<T> sought, boolean allowInterfaces)
		{
		
		// prepare a list for all the found classes and interfaces that sought
		// can be assigned to
		List<Class<? super T>> assigneableToList = new LinkedList<Class<? super T>>();
		
		boolean sougthIsImplementation = !sought.isInterface();
		
		// go through all the given classes, look for both exact matches and superclasses
		for(Class<?> c : classes){
			
			// interface check
			if(!allowInterfaces && sougthIsImplementation && c.isInterface()) continue;
			
			// if the class is equal, there's nothing more to think of
			if(c.equals(sought)){
				@SuppressWarnings("unchecked")	// it's guaranteed that c is a superclass of sought
				Class<? super T> rVal = (Class<? super T>)c;
				return rVal;
			}
			
			// if the class is not equal, it might be assignable
			if(c.isAssignableFrom(sought)){
				@SuppressWarnings("unchecked")	// it's guaranteed that c is a superclass of sought
				Class<? super T> toAdd = (Class<? super T>)c;
				assigneableToList.add(toAdd);
			}
		}
		
		// check if we found anything, if not, return null
		if(assigneableToList.size() == 0) return null;
		
		// we found one more more superclasses of toFind
		// go through them, find out which one is the closest, and that's our match
		Class<? super T> closest = null;
		int closestDistance = Integer.MAX_VALUE;
		
		// measure the hierarchy distance of the superclass from the sought class
		for(Class<? super T> assigneableTo : assigneableToList){
			
			// we discriminate against interfaces in a way that ensures that if there
			// is an implementation superclass, it is the preferred result
			int distance = assigneableTo.isInterface() ? 5000 : 0;
			
			for(	Class<?> toFindSC = sought.getSuperclass() ; 			// init
					toFindSC != null && !assigneableTo.equals(toFindSC) ; 	// condition
					toFindSC = toFindSC.getSuperclass()						// increment
				)
				distance++;
			
			// see if the distance is the lowest found yet
			if(distance < closestDistance){
				closestDistance = distance;
				closest = assigneableTo;
			}
		}
		
		return closest;
	}
}