package org.jbnd.support;

import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Provides a way to translate data type and property names into user
 * presentable (understandable) names. Does so in most cases by simply parsing
 * the key and type names into something more readable. However, it is possible
 * to provide <tt>ResourceBundle</tt>s that contain internationalized or simply
 * custom beautified <tt>DataType</tt> and key names.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0, 12/08/06
 */
public final class NamingSupport {
	
	//	used for String construction
	private static final StringBuffer buff = new StringBuffer(1024);
	
	//	storage for loaded type and property name files
	private static final List<ResourceBundle>
		typeNames = new LinkedList<ResourceBundle>(),
		propertyNames = new LinkedList<ResourceBundle>();
	
	/**
	 * Adds a <tt>ResourceBundle</tt> to be queried for <tt>DataType</tt>
	 * names, for more info see the {@link #typeName(String)} method.
	 * 
	 * @param rb See above.
	 */
	public static void addTypeNameBundle(ResourceBundle rb){
		typeNames.add(rb);
	}
	
	/**
	 * Adds s <tt>ResourceBundle</tt> to be queried for property names, for
	 * more info see the {@link #propertyName(String)} method.
	 * 
	 * @param rb See above.
	 */
	public static void addPopertyNameBundle(ResourceBundle rb){
		propertyNames.add(rb);
	}
	
	
	/**
	 * Disallow instantiation, as the class is intended for static calls only.
	 */
	private NamingSupport(){}

	/**
	 * Returns a more readable <tt>DataType</tt> name. If there are
	 * <tt>ResourceBundle</tt>s added for type names (through the
	 * {@link #addTypeNameBundle(ResourceBundle)} method), then they are one by
	 * one queried for a value corresponding to the given <tt>typeName</tt>
	 * parameter. The first found match is returned. This facilitates
	 * internationalization of type names. If no appropriate value is found in
	 * all the type resource bundles provided, the <tt>typeName</tt> is
	 * beautified programatically.
	 * 
	 * @param typeName Name of the type as defined by <tt>DataType.name()</tt>.
	 * @return The visually more appealing type name, for GUI display.
	 */
	public static String typeName(String typeName){
		
		for(ResourceBundle bundle : typeNames){
			//	try different ResourceBundles
			try{
				return bundle.getString(typeName);
			}catch (MissingResourceException e) {
				// swallow, and proceed with other kind of
				// beautification
			}
		}
		
		return processKey(typeName);
	}
	
	/**
	 * Returns a more readable property name. If there are
	 * <tt>ResourceBundle</tt>s added for property names (through the
	 * {@link #addPopertyNameBundle(ResourceBundle)} method), then they are one
	 * by one queried for a value corresponding to the given <tt>propName</tt>
	 * parameter. The first found match is returned. This facilitates
	 * internationalization of property names. If no appropriate value is found
	 * in all the type resource bundles provided, the <tt>propName</tt> is
	 * beautified programatically.
	 * 
	 * @param propName Name of the property.
	 * @return The visually more appealing property name, for GUI display.
	 */
	public static String propertyName(String propName){
	
		for(ResourceBundle bundle : propertyNames){
			//	try different ResourceBundles
			try{
				return bundle.getString(propName);
			}catch (MissingResourceException e) {
				// swallow, and proceed with other kind of
				// beautification
			}
		}
		
		return processKey(propName);
	}
	
	/**
	 * Returns the same string with the first letter of the first word switched
	 * to uppercase or lowercase, depending on the boolean parameter.
	 * 
	 * @param name The name to modify
	 * @param uppercase Indicates if the first word should start with an
	 *            uppercase letter, or a lowercase letter
	 */
	private static String firstCharCaseChange(String name, boolean uppercase){
		if(name == null || name.length() == 0) return null;
		
		if(uppercase && Character.isLowerCase(name.charAt(0))){
			buff.append(Character.toUpperCase(name.charAt(0)));
			buff.append(name.substring(1));
			String rVal = buff.toString();
			buff.delete(0, buff.length());

			return rVal;
		}
		
		if(!uppercase && Character.isUpperCase(name.charAt(0))){
			buff.append(Character.toLowerCase(name.charAt(0)));
			buff.append(name.substring(1));
			String rVal = buff.toString();
			buff.delete(0, buff.length());

			return rVal;
		}

		return name;
	}
	
	
	/**
	 * Translates variable names into a more readable form, by breaking them
	 * down into individual words. For example "firstName" is translated to
	 * "first name", "free4All" is translated to "free 4 all". Also eliminates
	 * leading non-letter characters, so for example "@count" is translated to
	 * "count".
	 * 
	 * @param name The name to convert to split into words
	 * @return The parameter, broken down into words separated with a single
	 *         space character.
	 */
	private static String nameWordSplit(String name){
		if(name == null || name.length() == 0) return null;
		
		//	break down the string on upper case letters and numbers
		for(int i = 0, s = name.length() ; i < s ; i++){
			char c = name.charAt(i);
			if(Character.isUpperCase(c) && i > 0){
				buff.append(' ');
				buff.append(Character.toLowerCase(c));
			}else if(Character.isDigit(c)){
				buff.append(' ');
				buff.append(c);
				if(i < s - 1) buff.append(' ');
			}else
				buff.append(c);
		}
		
		String rVal = buff.toString();
		buff.delete(0, buff.length());

		return rVal;
	}
	
	/**
	 * Processes a key(path) into a user presentable string of text. Path
	 * separators ('.') are replaced with ' - ', and every path component is
	 * split over words and numbers, and capitalized. Example:
	 * "one.two.free4all" is translated into "One - Two - Free 4 all"
	 * 
	 * @param key The key(path) to translate.
	 * @return User presentable version of the key(path) parameter.
	 */
	private static String processKey(String key){
		if(key == null || key.length() == 0) return null;
		
		//	can't use the buff on this one, coz it is used by other methods in the meanwhile
		//	use raw String concatenation
		String rVal = "";
		
		//	split the keypath on key separator
		String[] keys = key.split("\\.");

		//	generate user presentable key path
		for(int i = 0 ; i < keys.length ; i++){
			if(i > 0)
				rVal += " - ";
			
			rVal += keys.length > 1 ? propertyName(keys[i]) : 
				firstCharCaseChange(nameWordSplit(keys[i]), true);
		}
		
		return rVal;
	}
}
