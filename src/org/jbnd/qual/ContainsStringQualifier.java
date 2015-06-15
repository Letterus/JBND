package org.jbnd.qual;

import org.jbnd.support.JBNDUtil;


/**
 * A <tt>StringQualifier</tt> that by default prefixes the value provided to it
 * through the <tt>setValue(...)</tt> method with a
 * <tt>StringQualifier.WILDCARD_MANY</tt> character, which makes it possible to
 * provide for example 'es' to this qualifier, and it finding the 'Cheese' value
 * acceptable.
 * <p>
 * Controls are provided for defining if the wildcards should be inserted in the
 * beginning and end, the initial values make the qualifier insert them to both.
 * <p>
 * In all other respects the <tt>ContainsStringQualifier</tt> behaves like the
 * <tt>StringQualifier</tt>.
 * 
 * @version 1.0 Mar 18, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class ContainsStringQualifier extends StringQualifier{
	
	//	if or not a wildcard should be inserted in the beginning of the
	//	comparison value
	private boolean wildcardStart = true;
	
	//	if or not a wildcard should be inserted in the end of the
	//	comparison value
	private boolean wildcardEnd = true;
	
	/**
	 * Depreciated, use {@link #ContainsStringQualifier(String, KeyValueQualifier.Op)};
	 */
	@Deprecated
	public ContainsStringQualifier(String key, int operator){
		super(key, operator);
	}
	
	/**
	 * Creates a <tt>KeyValueQualifier</tt> with the given parameters.
	 * 
	 * @param key The key this <tt>KeyValueQualifier</tt> should use to retrieve
	 *            the value to qualify on from the <tt>DataObject</tt> being
	 *            qualified, not allowed to be <code>null</code>.
	 * @param operator The operator flag that should define how this
	 *            <tt>StringQualifier</tt> compares values, must be either
	 *           <tt>Op.LIKE</tt> or <tt>Op.INSENSITIVE_LIKE</tt>.
	 */
	public ContainsStringQualifier(String key, Op operator){
		super(key, operator);
	}

	/**
	 * Depreciated, use
	 * {@link #ContainsStringQualifier(String, KeyValueQualifier.Op, String)}.
	 */
	@Deprecated
	public ContainsStringQualifier(String key, int operator, String value){
		super(key, operator, value);
	}
	
	/**
	 * Creates a <tt>KeyValueQualifier</tt> with the given parameters.
	 * 
	 * @param key The key this <tt>KeyValueQualifier</tt> should use to retrieve
	 *            the value to qualify on from the <tt>DataObject</tt> being
	 *            qualified, not allowed to be <code>null</code>.
	 * @param operator The operator flag that should define how this
	 *            <tt>StringQualifier</tt> compares values, must be either
	 *            <tt>Op.LIKE</tt> or <tt>Op.INSENSITIVE_LIKE</tt>.
	 * @param value The value this <tt>KeyValueQualifier</tt> compares the value
	 *            obtained from a <tt>DataObject</tt> against.
	 */
	public ContainsStringQualifier(String key, Op operator, String value){
		super(key, operator, value);
	}

	/**
	 * Depreciated, use
	 * {@link #ContainsStringQualifier(String, KeyValueQualifier.Op, String, KeyValueQualifier.NullMeans)}
	 * .
	 */
	@Deprecated
	public ContainsStringQualifier(String key, int operator, String value,
			int nullBehavior){
		super(key, operator, value, nullBehavior);
	}
	
	/**
	 * Creates a <tt>KeyValueQualifier</tt> with the given parameters.
	 * 
	 * @param key The key this <tt>KeyValueQualifier</tt> should use to retrieve
	 *            the value to qualify on from the <tt>DataObject</tt> being
	 *            qualified, not allowed to be <code>null</code>.
	 * @param operator The operator flag that should define how this
	 *            <tt>StringQualifier</tt> compares values, must be either
	 *            <tt>Op.LIKE</tt> or <tt>Op.INSENSITIVE_LIKE</tt>.
	 * @param value The value this <tt>KeyValueQualifier</tt> compares the value
	 *            obtained from a <tt>DataObject</tt> against.
	 * @param	nullBehavior	The flag that defines how this <tt>KeyValueQualifier</tt>
	 * 				evaluates <tt>DataObject</tt>s when the value of the qualifier itself
	 * 				is <tt>null</tt>.
	 */
	public ContainsStringQualifier(String key, Op operator, String value, NullMeans nullBehavior){
		super(key, operator, value, nullBehavior);
	}
	
	/**
	 * Overridden to optionally add a <tt>StringQualifier.WILDCARD_MANY</tt> at
	 * the beginning and end of the <tt>value</tt>, as per contract of this
	 * class. Once that is done the new value is passed on to the superclass
	 * implementation of this method.
	 * 
	 * @param value See above.
	 */
	@Override
	public void setValue(String value){
		if(value == null){
			super.setValue(null);
			return;
		}
		
		String sv = value.toString();
		if(wildcardStart && !sv.startsWith(new Character(StringQualifier.WILDCARD_MANY).toString())){
			sv = StringQualifier.WILDCARD_MANY + sv;
		}
		if(wildcardEnd && !sv.endsWith(new Character(StringQualifier.WILDCARD_MANY).toString())){
			sv = sv + StringQualifier.WILDCARD_MANY;
		}
		
		super.setValue(sv);
	}

	/**
	 * Gets if or not the <tt>ContainsStringQualifier</tt> inserts a wildcard
	 * in the beginning of the comparison value provided to it through the
	 * <tt>setValue(...)</tt> method.
	 * 
	 * @return wildcardStart See above.
	 */
	public boolean isWildcardStart(){
		return wildcardStart;
	}

	/**
	 * Sets if or not the <tt>ContainsStringQualifier</tt> should insert a
	 * wildcard in the beginning of the comparison value provided to it through
	 * the <tt>setValue(...)</tt> method.
	 * 
	 * @param wildcardStart	See above.
	 */
	public void setWildcardStart(boolean wildcardStart){
		if(this.wildcardStart == wildcardStart) return;
		this.wildcardStart = wildcardStart;
		
		String value = getValue() != null ? getValue().toString() : null;
		if(!wildcardStart && value != null){
			//	there used to be a wildcard, but not anymore
			value = value.substring(1);
		}
		
		setValue(value);
	}

	/**
	 * Gets if or not the <tt>ContainsStringQualifier</tt> inserts a wildcard
	 * in the end of the comparison value provided to it through the
	 * <tt>setValue(...)</tt> method.
	 * 
	 * @return wildcardEnd See above.
	 */
	public boolean isWildcardEnd(){
		return wildcardEnd;
	}

	/**
	 * Sets if or not the <tt>ContainsStringQualifier</tt> should insert a
	 * wildcard in the end of the comparison value provided to it through the
	 * <tt>setValue(...)</tt> method.
	 * 
	 * @param wildcardEnd See above.
	 */
	public void setWildcardEnd(boolean wildcardEnd){
		if(this.wildcardEnd == wildcardEnd) return;
		this.wildcardEnd = wildcardEnd;
		
		String value = getValue() != null ? getValue().toString() : null;
		if(!wildcardEnd && value != null){
			//	there used to be a wildcard, but not anymore
			value = value.substring(0, value.length() - 1);
		}
		
		setValue(value);
	}

	/**
	 * Overridden to deal with automated wildcard processing.
	 * 
	 * @return The value as it was set on this qualifier using
	 *         <tt>setValue(String)</tt>.
	 */
	@Override
	public String getValue(){
		String value = super.getValue() != null ? super.getValue().toString() : null;
		if(wildcardEnd && value != null){
			//	there used to be a wildcard, but not anymore
			value = value.substring(0, value.length() - 1);
		}
		value = JBNDUtil.supertrim(value);
		if(wildcardStart && value != null){
			//	there used to be a wildcard, but not anymore
			value = value.substring(1);
		}
		
		return value;
	}
}