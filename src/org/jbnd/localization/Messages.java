package org.jbnd.localization;

import java.util.ResourceBundle;


public class Messages{

	private static final String BUNDLE_NAME = "org.jbnd.localization.jbnd_messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Messages(){
	}

	public static String getString(String key){
		return RESOURCE_BUNDLE.getString(key);
	}
	
	/**
	 * Replaces placeholders in the given <tt>String</tt> with the
	 * <tt>toString()</tt> values of other arguments. The placeholder indices
	 * start from 1, not 0.
	 * <p>
	 * For example, if the <tt>s</tt> parameter is
	 * "My {1} is huge, my girl loves it!", and another parameter with the value
	 * of "house" is passed, what will be returned is:
	 * "My house is huge, my girl loves it!".
	 * <p>
	 * This method does NOT throw any exceptions if the passed combination of
	 * parameters is in any way not good, it does what it can, and that's it.
	 * So, check your results!
	 * 
	 * @param s See above.
	 * @param args See above.
	 * @return See above.
	 */
	public static String fillUp(String s, Object... args){
		
		for(int i = 0 ; i < args.length ; i++)
			s = s.replaceAll("\\{"+(i + 1)+"\\}", args[i].toString());
		
		return s;
	}
}