package org.jbnd.qual;

import java.util.regex.Pattern;

import org.jbnd.DataObject;
import org.jbnd.support.JBNDUtil;


/**
 * A <tt>Qualifier</tt> specialized in handling <tt>String</tt>s, provides means
 * to qualify on partial <tt>String</tt>s (using wildcards).
 * <p>
 * The two wildcard characters (<tt>WILDCARD_ONE</tt> and <tt>WILDCARD_MANY</tt>
 * ) which respectively stand for any single character, and any number of
 * whichever characters can be used within the given value.
 * <p>
 * The only operators valid in this <tt>KeyValueQualifier</tt> subclass are the
 * <tt>Op.LIKE</tt> and <tt>Op.INSENSITIVE_LIKE</tt> operators. They are
 * used to determine of the <tt>StringQualifier</tt> should qualify
 * <tt>String</tt>s in a case sensitive, or a case insensitive way.
 * <p>
 * In other respects the <tt>StringQualifier</tt> behaves like it's superclass,
 * the <tt>KeyValueQualifier</tt>.
 * 
 * @version 1.0 Feb 6, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class StringQualifier extends KeyValueQualifier<String>{
	
	/**
	 * Qualification operator flag, defines how the <tt>Qualifier</tt>
	 * compares the given <tt>value</tt> and the value found in the
	 * <tt>DataObject</tt> that is being checked.
	 */
	@Deprecated
	public static final int SENSITIVE_LIKE = 100;
	
	/**
	 * Qualification operator flag, defines how the <tt>Qualifier</tt>
	 * compares the given <tt>value</tt> and the value found in the
	 * <tt>DataObject</tt> that is being checked.
	 */
	@Deprecated
	public static final int INSENSITIVE_LIKE = 101;
	
	/**
	 * The character used as a wildcard in the values given to
	 * a <tt>StringQualifier</tt>, during evaluation matches any
	 * single character.
	 */
	public static final char WILDCARD_ONE = '?';
	
	/**
	 * The character used as a wildcard in the values given to
	 * a <tt>StringQualifier</tt>, during evaluation matches any
	 * number of whichever characters.
	 */
	public static final char WILDCARD_MANY = '*';
	
	/**
	 * Depreciated, use {@link #StringQualifier(String, KeyValueQualifier.Op)};
	 */
	@Deprecated
	public StringQualifier(String key, int operator){
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
	public StringQualifier(String key, Op operator){
		super(key, operator);
	}

	/**
	 * Depreciated, use
	 * {@link #StringQualifier(String, KeyValueQualifier.Op, String)}.
	 */
	@Deprecated
	public StringQualifier(String key, int operator, String value){
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
	public StringQualifier(String key, Op operator, String value){
		super(key, operator, value);
	}

	/**
	 * Depreciated, use
	 * {@link #StringQualifier(String, KeyValueQualifier.Op, String, KeyValueQualifier.NullMeans)}
	 * .
	 */
	@Deprecated
	public StringQualifier(String key, int operator, String value,
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
	public StringQualifier(String key, Op operator, String value, NullMeans nullBehavior){
		super(key, operator, value, nullBehavior);
	}
	
	/**
	 * Overridden to check that the given <tt>operator</tt> is either
	 * <tt>Op.LIKE</tt> or<tt>Op.INSENSITIVE_LIKE</tt>); if it is not, an
	 * <tt>IllegalArgumentException</tt> is thrown.
	 * 
	 * @param operator See above.
	 */
	@Override
	public void setOperator(Op operator){
		if(operator != Op.LIKE && operator != Op.INSENSITIVE_LIKE)
			throw new IllegalArgumentException(
					"StringQualifier can not be used with operator: "
							+ operator);

		super.setOperator(operator);
	}
	
	/**
	 * Overridden to manage internal caches, effect of calling this method is
	 * the same as for calling the superclass implementation.
	 * 
	 * @param value The value this <tt>KeyValueQualifier</tt> compares the value
	 *            obtained from a <tt>DataObject</tt> against.
	 */
	@Override
	public void setValue(String value){
		if(JBNDUtil.equals(getValue(), value)) return;
		
		// reset the regex cache
		_cachedRegex = null;
		super.setValue(value);
	}

	/**
	 * <tt>Qualifier</tt> implementation, does the actual evaluation
	 * of a <tt>DataObject</tt> by comparing (using the operator of
	 * the qualifier) the value found for the key of the qualifier
	 * and the value of the qualifier.
	 * 
	 * @param	object	The object to qualify.
	 * @return	Can't imagine what ;)
	 */
	public boolean accept(DataObject object){
		
		// get references to objects used
		if(object == null) return false;
		Object objectValue = object.get(getKey());
		Op operator = getOperator();
		
		if(value == null){
			//	a value is null situation
			//	return based on the nullBehavior flag
			if(getNullBehavior() == NullMeans.ANY) return true;
			else return objectValue == null;
		}
		
		// now deal with the possibility of the DataObject value being null
		if(objectValue == null){
			return false;
		}
		
		String stringValue = objectValue.toString();
		if(operator == Op.INSENSITIVE_LIKE) stringValue = stringValue.toLowerCase();
		
		//	get a regular expression from the value string
		if(_cachedRegex == null){
			String s = operator == Op.INSENSITIVE_LIKE ? value.toString()
					.toLowerCase() : value.toString();
			_cachedRegex = regexFromValue(s);
		}
		
		//	do the actual matching process
		return _cachedRegex.matcher(stringValue).matches();
	}
	
	/*
	 * The regular expression version of the value String (if value is a
	 * String), translates the String that potentially contains wildcards
	 */
	private Pattern _cachedRegex;
	
	/**
	 * Translates any <tt>String</tt> that contains inside it the wildcard
	 * characters defined in the <tt>StringQualifier</tt> into a regular
	 * expression <tt>String</tt> that will not contain those wildcards, but
	 * will be a pattern that can be used to match any other <tt>String</tt> as
	 * defined by the given <tt>String</tt> with wildcards.
	 * 
	 * @param value The <tt>String</tt> possibly containing wildcards.
	 * @return A regular expression <tt>String</tt> that can be used to match
	 *         any other <tt>String</tt> as defined by the given <tt>String</tt>
	 *         with wildcards.
	 */
	public static Pattern regexFromValue(String value){
		//	the two string builders used in the process
		StringBuilder builder = new StringBuilder();
		StringBuilder tempBuilder = new StringBuilder();
		
		for(char c : value.toCharArray()){
			//	if it is a regular character
			//	append it to the temp builder
			if(c != WILDCARD_MANY && c != WILDCARD_ONE){
				tempBuilder.append(c);
				continue;
			}
			
			//	the character is a wildcard!
			//	get the value from the temp string builder
			String tempBuilderString = tempBuilder.toString();
			tempBuilder.delete(0, tempBuilder.length());
			//	and append it to the big builder
			if(tempBuilderString != null && tempBuilderString.length() > 0)
				builder.append(Pattern.quote(tempBuilderString));
			
			//	and then translate the wildcard into a regular expression
			if(c == WILDCARD_MANY) builder.append(".*");
			if(c == WILDCARD_ONE) builder.append(".");
		}
		
		//	there could be stuff left in the tempBuilder
		//	take care of it
		String tempBuilderString = tempBuilder.toString();
		tempBuilder.delete(0, tempBuilder.length());
		if(tempBuilderString != null && tempBuilderString.length() > 0)
			builder.append(Pattern.quote(tempBuilderString));
		
		//	the end!
		String rVal = builder.toString();
		builder.delete(0, builder.length());
		return Pattern.compile(rVal);
	}
}