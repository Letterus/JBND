package org.jbnd.qual;

import java.util.Comparator;

import org.jbnd.DataObject;
import org.jbnd.support.JBNDUtil;
import org.jbnd.support.ValueConverter;


/**
 * A <tt>Qualifier</tt> that compares a single value with a value found in a
 * <tt>DataObject</tt> for a single key. The <tt>Qualifier</tt> compares values
 * based on one of the operator flags (the {@link Op} enumeration).
 * <p>
 * If there is no value provided to the qualifier, or a <tt>null</tt> is
 * provided, then the qualifier will behave as defined by one of the two null
 * handling flags (the {@link NullMeans} enumeration).
 * <p>
 * A <tt>KeyValueQualifier</tt> has to be parameterized to the same type that
 * the comparison value is comparable to. Meaning, if using it to qualify on an
 * <tt>Integer</tt> attribute of a <tt>DataObject</tt>, then only values that
 * are comparable to an <tt>Integer</tt> (they implement
 * <tt>Comparable&lt;Integer&gt;</tt>) are valid comparison values, and the
 * <tt>KeyValueQualifier</tt> needs to be parameterized with <tt>Integer</tt>.
 * Note that if then the value found in the <tt>DataObject</tt> for the key of
 * the qualifier is not an instance of the class <tt>KeyValueQualifier</tt> is
 * parameterized on, a class cast exception will be thrown.
 * <p>
 * Example:
 * 
 * <pre>
 * 
 * 
 * 
 * //	create a qualifier that only accepts DataObject that
 * //	have their 'size' attribute bigger then 5
 * Qualifier kvq = new KeyValueQualifier&lt;Integer&gt;(&quot;size&quot;,
 * 		KeyValueQualifier.Op.GREATER_THAN, new Integer(5));
 * //	if during runtime this qualifier find something else as
 * //	an Integer or a null in a DataObject for the &quot;size&quot; key
 * //	an exception will be thrown
 * </pre>
 * 
 * This class can be subclassed to define custom key-value based qualifiers. For
 * additional information on overriding individual methods, see their
 * documentation.
 * 
 * @version 1.0 Feb 6, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class KeyValueQualifier<T> extends AbstractFilter<DataObject> implements Qualifier{
	

	/**
	 * Qualification operator flag, defines how the <tt>Qualifier</tt>
	 * compares the given <tt>value</tt> and the value found in the
	 * <tt>DataObject</tt> that is being checked.
	 */
	@Deprecated
	public static final int
		EQUAL = 0,
		GREATER_THAN = 1,
		GREATER_THAN_OR_EQUAL = 3,
		LESS_THAN = 4,
		LESS_THAN_OR_EQUAL = 5,
		NOT_EQUAL = 6;
	
	/**
	 * Qualification operators, define how the <tt>Qualifier</tt> compares the
	 * given <tt>value</tt> and the value found in the <tt>DataObject</tt> that
	 * is being checked.
	 * 
	 * @version 1.0 Feb 14, 2009
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public static enum Op{
		EQUAL,
		GREATER_THAN,
		GREATER_THAN_OR_EQUAL,
		LESS_THAN,
		LESS_THAN_OR_EQUAL,
		NOT_EQUAL,
		LIKE,
		INSENSITIVE_LIKE;
	}
	
	/**
	 * Depreciated, use {@link #readableStringForOperator(Op)}.
	 */
	@Deprecated
	public static String readableStringForOperator(int operator){
		return readableStringForOperator(Op.values()[operator]);
	}
	
	/**
	 * Returns a descriptive, readable <tt>String</tt> that in
	 * textual form describes an operator. For example, for the
	 * <tt>EQUAL</tt> operator 'equal' is returned.
	 * 
	 * @param operator	The operator.
	 * @return	A textual description of the given flag.
	 */
	public static String readableStringForOperator(Op operator){
		switch(operator){
			case EQUAL	: return "equal";
			case GREATER_THAN	: return "greaterThan";
			case GREATER_THAN_OR_EQUAL	: return "greaterThanOrEqual";
			case LESS_THAN	: return "lessThan";
			case LESS_THAN_OR_EQUAL	: return "lessThanOrEqual";
			case NOT_EQUAL	: return "notEqual";
			case INSENSITIVE_LIKE : return "likeCaseInsensitive";
			case LIKE : return "like";
			default : throw new IllegalArgumentException("Invalid flag");
		}
	}
	
	/**
	 *  A flag defining how the <tt>Qualifier</tt> behaves in cases then
	 *  the <tt>value</tt> of the <tt>Qualifier</tt> is set to <tt>null</tt>;
	 *  <tt>NULL_MEANS_ANY</tt> implies that any <tt>DataObject</tt> qualifies,
	 *  the <tt>accept</tt> method will return <tt>true</tt>.
	 */
	public static final int NULL_MEANS_ANY = 0;
	
	/**
	 *  A flag defining how the <tt>Qualifier</tt> behaves in cases then
	 *  the <tt>value</tt> of the <tt>Qualifier</tt> is set to <tt>null</tt>;
	 *  <tt>NULL_MEANS_NULL</tt> implies that a <tt>DataObject</tt> only
	 *  qualifies if the value it has for the qualifier's <tt>key</tt> is
	 *  <tt>null</tt>.
	 */
	public static final int NULL_MEANS_NULL = 1;
	
	/**
	 * Flags defining how the <tt>Qualifier</tt> behaves in cases when the
	 * <tt>value</tt> of the <tt>Qualifier</tt> is set to <tt>null</tt>;
	 * <tt>ANY</tt> implies that any value found in the <tt>DataObject</tt>
	 * value qualifies, <tt>NULL</tt> implies that only <tt>null</tt> values
	 * found in it qualify.
	 * 
	 * @version 1.0 Feb 14, 2009
	 * @author Florijan Stamenkovic (flor385@mac.com)
	 */
	public static enum NullMeans{
		ANY,
		NULL;
	}
	
	
	// the key on which this qualifier is comparing DataObjects
	private String key;

	// the value on which this qualifier is comparing DataObjects
	protected T value;

	// the operator (equals, lessThen, moreThen...)
	private Op operator;

	// the comparator, if present it is to be used for evaluation,
	// if not, then try to compare values themselves
	private Comparator<? super T> comparator;

	// a flag indicating how this qualifier treats
	// a situation when value is null
	private NullMeans nullBehavior = NullMeans.ANY;
	
	/**
	 * Depreciated, use {@link #KeyValueQualifier(String, Op)}.
	 */
	@Deprecated
	public KeyValueQualifier(String key, int operator){
		this(key, Op.values()[operator]);
	}
	
	/**
	 * Creates a <tt>KeyValueQualifier</tt> with the given parameters.
	 * 
	 * @param	key	The key this <tt>KeyValueQualifier</tt> should use to retrieve
	 * 			the value to qualify on from the <tt>DataObject</tt> being
	 * 			qualified, not allowed to be <code>null</code>.
	 * @param	operator	The operator flag that should define how this
	 *				<tt>KeyValueQualifier</tt> compares values.
	 */
	public KeyValueQualifier(String key, Op operator){
		setKey(key);
		setOperator(operator);
	}
	
	/**
	 * Depreciated, use {@link #KeyValueQualifier(String, Op, Object)}.
	 */
	@Deprecated
	public KeyValueQualifier(String key, int operator, T value){
		this(key, Op.values()[operator], value);
	}
	
	/**
	 * Creates a <tt>KeyValueQualifier</tt> with the given parameters.
	 * 
	 * @param	key	The key this <tt>KeyValueQualifier</tt> should use to retrieve
	 * 			the value to qualify on from the <tt>DataObject</tt> being
	 * 			qualified, not allowed to be <code>null</code>.
	 * @param	operator	The operator flag that should define how this
	 *				<tt>KeyValueQualifier</tt> compares values.
	 * @param	value	The value this <tt>KeyValueQualifier</tt> compares the
	 * 			value obtained from a <tt>DataObject</tt> against.
	 */
	public KeyValueQualifier(String key, Op operator, T value){
		setKey(key);
		setOperator(operator);
		setValue(value);
	}
	
	/**
	 * Depreciated, use {@link #KeyValueQualifier(String, Op, Object, NullMeans)}.
	 */
	@Deprecated
	public KeyValueQualifier(String key, int operator, T value,
			int nullBehavior){
		
	}
	
	/**
	 * Creates a <tt>KeyValueQualifier</tt> with the given parameters.
	 * 
	 * @param	key	The key this <tt>KeyValueQualifier</tt> should use to retrieve
	 * 				the value to qualify on from the <tt>DataObject</tt> being
	 * 				qualified, not allowed to be <code>null</code>.
	 * @param	operator	The operator flag that should define how this
	 *				<tt>KeyValueQualifier</tt> compares values.
	 * @param	value	The value this <tt>KeyValueQualifier</tt> compares the
	 * 				value obtained from a <tt>DataObject</tt> against.
	 * @param	nullBehavior	The flag that defines how this <tt>KeyValueQualifier</tt>
	 * 				evaluates <tt>DataObject</tt>s when the value of the qualifier itself
	 * 				is <tt>null</tt>.
	 */
	public KeyValueQualifier(String key, Op operator, T value,
			NullMeans nullBehavior)
	{
		setKey(key);
		setOperator(operator);
		setValue(value);
		setNullBehavior(nullBehavior);
	}
	
	/**
	 * Returns the key this <tt>KeyValueQualifier</tt> uses to retrieve
	 * the value to qualify on from the <tt>DataObject</tt> being
	 * qualified, not allowed to be <code>null</code>.
	 * 
	 * @return	The key this <tt>KeyValueQualifier</tt> uses to retrieve
	 * 			the value to qualify on from the <tt>DataObject</tt> being
	 * 			qualified.
	 */
	public String getKey(){
		return key;
	}

	/**
	 * Sets the key this <tt>KeyValueQualifier</tt> uses to retrieve the value
	 * to qualify on from the <tt>DataObject</tt> being qualified.
	 * <p>
	 * <b>This implementation:</b> the key is not allowed to be
	 * <code>null</code>. If the key is equal to the current key, nothing is
	 * done. Otherwise the new key is set as the current key, and a
	 * <tt>FilterChangeEvent</tt> is fired on all listeners.
	 * 
	 * @param key The key this <tt>KeyValueQualifier</tt> should use to retrieve
	 *            the value to qualify on from the <tt>DataObject</tt> being
	 *            qualified.
	 */
	public void setKey(String key){
		//	check if the given key is valid
		key = (String)JBNDUtil.supertrim(key);
		if(key == null) throw new IllegalArgumentException();
		//	and if it is different then the current key
		if(key.equals(this.key)) return;
		
		//	set the key
		this.key = key;
		fireFilterChanged();
	}

	/**
	 * Returns the value this <tt>KeyValueQualifier</tt> compares the
	 * value obtained from a <tt>DataObject</tt> against.
	 * 
	 * @return	The value this <tt>KeyValueQualifier</tt> compares the
	 * 			value obtained from a <tt>DataObject</tt> against.
	 */
	public T getValue(){
		return value;
	}

	/**
	 * Sets the value this <tt>KeyValueQualifier</tt> compares the value
	 * obtained from a <tt>DataObject</tt> against.
	 * <p>
	 * <b>This implementation: </b> Sets the value of this qualifier if it is
	 * different then the current value, according to
	 * <tt>JBNDUtil.equals(...)</tt>, and fires a <tt>FilterChangedEvent</tt>.
	 * If the value is the same as the existing one, it does nothing.
	 * 
	 * @param value The value this <tt>KeyValueQualifier</tt> compares the value
	 *            obtained from a <tt>DataObject</tt> against.
	 */
	public void setValue(T value){
		if(JBNDUtil.equals(this.value, value)) return;
		
		//	set the value
		this.value = value;
		fireFilterChanged();
	}

	/**
	 * Returns the operator flag that defines how this
	 * <tt>KeyValueQualifier</tt> compares values.
	 * 
	 * @return The operator flag that defines how this
	 *         <tt>KeyValueQualifier</tt> compares values
	 */
	public Op getOperator(){
		return operator;
	}

	/**
	 * Depreciated, use {@link #setOperator(Op)}.
	 */
	@Deprecated
	public void setOperator(int operator){
		switch(operator){
			case EQUAL :
				setOperator(Op.EQUAL);
				return;
			case GREATER_THAN :
				setOperator(Op.GREATER_THAN);
				return;
			case GREATER_THAN_OR_EQUAL :
				setOperator(Op.GREATER_THAN_OR_EQUAL);
				return;
			case LESS_THAN :
				setOperator(Op.LESS_THAN);
				return;
			case LESS_THAN_OR_EQUAL :
				setOperator(Op.LESS_THAN_OR_EQUAL);
				return;
			case NOT_EQUAL :
				setOperator(Op.NOT_EQUAL);
				return;
			case StringQualifier.SENSITIVE_LIKE :
				setOperator(Op.LIKE);
				return;
			case StringQualifier.INSENSITIVE_LIKE :
				setOperator(Op.INSENSITIVE_LIKE);
				return;
			default:
				throw new IllegalArgumentException("Unknown operator flag: "+operator);
		}
	}
	
	/**
	 * Sets the operator flag that defines how this <tt>KeyValueQualifier</tt>
	 * compares values.
	 * <p>
	 * <b>This implementation: </b> sets the new operator if it is different
	 * then the current one, and fires a <tt>FilterChangedEvent</tt>. If the
	 * given <tt>operator</tt> is the same as the current one, it does nothing.
	 * <tt>null</tt> is not accepted.
	 * 
	 * @param operator The operator flag that should define how this
	 *            <tt>KeyValueQualifier</tt> compares values.
	 */
	public void setOperator(Op operator){
		if(operator == null) throw new NullPointerException("Can't have a null operator...");
		if(this.operator == operator) return;
		
		this.operator = operator;
		fireFilterChanged();
	}
	
	/**
	 * Gets the comparator that's used to compare the value set on this
	 * <tt>KeyValueQualifier</tt> and the value found in a <tt>DataObject</tt>
	 * being checked. <tt>null</tt> is allowed, it implies that the natural
	 * comparison method of the two values is used. Default value is
	 * <tt>null</tt>.
	 * 
	 * @return See above.
	 */
	public Comparator<? super T> getComparator(){
		return comparator;
	}
	
	/**
	 * Sets the comparator to be used to compare the value set on this
	 * <tt>KeyValueQualifier</tt> and the value found in a <tt>DataObject</tt>
	 * being checked. <tt>null</tt> is allowed, it will imply that the natural
	 * comparison method of the two values will be used. Default value is
	 * <tt>null</tt>.
	 * <p>
	 * <b>This implementation: </b> sets the new <tt>comparator</tt> if it is
	 * not equal to the current one according to
	 * <tt>JBNDUtil.equals(...), and fires
	 * a <tt>FilterChangedEvent</tt>.
	 * 
	 * @param comparator See above.
	 */
	public void setComparator(Comparator<? super T> comparator){
		if(this.comparator == comparator) return;
		
		this.comparator = comparator;
		fireFilterChanged();
	}

	/**
	 * Returns the flag that defines how this <tt>KeyValueQualifier</tt>
	 * evaluates <tt>DataObject</tt>s when the value of the qualifier itself is
	 * <tt>null</tt>.
	 * 
	 * @return See above.
	 */
	public NullMeans getNullBehavior(){
		return nullBehavior;
	}

	/**
	 * Depreciated, use {@link #setNullBehavior(NullMeans)}.
	 */
	@Deprecated
	public void setNullBehavior(int nullBehavior){
		setNullBehavior(NullMeans.values()[nullBehavior]);
	}
	
	/**
	 * Sets the flag that defines how this <tt>KeyValueQualifier</tt> evaluates
	 * <tt>DataObject</tt>s when the value of the qualifier itself is
	 * <tt>null</tt>.
	 * <p>
	 * <b>This implementation: </b> sets the new behavior if it is different
	 * then the current one, and fires a <tt>FilterChangedEvent</tt>. If the
	 * given <tt>nullBehavior</tt> is the same as the current one, it does
	 * nothing. <tt>null</tt> is not accepted.
	 * 
	 * @param nullBehavior See above.
	 */
	public void setNullBehavior(NullMeans nullBehavior){
		if(nullBehavior == null) throw new NullPointerException("Can't accept a null argument...");
		if(this.nullBehavior == nullBehavior) return;
		
		this.nullBehavior = nullBehavior;
		fireFilterChanged();
	}

	/**
	 * <tt>Qualifier</tt> implementation, does the actual evaluation of a
	 * <tt>DataObject</tt> by comparing (using the operator of the qualifier)
	 * the value found for the key of the qualifier and the value of the
	 * qualifier.
	 * <p>
	 * <b>This implementation: </b> does the standard thing. It supports all
	 * possible <tt>Op</tt>s, and <tt>NullMeans</tt> values. If the value found
	 * in the <tt>DataObject</tt> for the key of this qualifier is <tt>null</tt>
	 * , this method returns <tt>false</tt>. Depending on the current setup of
	 * the qualifier this method might attempt to cast the value found in the
	 * <tt>DataObject</tt> to <tt>T</tt> (when there is a <tt>Comparator</tt>
	 * set on the qualifier), or to <tt>Comparable<T></tt> (when there is no
	 * <tt>Comparator</tt> set).
	 * 
	 * @param object The object to qualify.
	 * @return Can't imagine what ;)
	 */
	public boolean accept(DataObject object){
		
		if(object == null) return false;
		Object objectValue = object.get(key);
		
		if(value == null){
			//	a value is null situation
			//	return based on the nullBehavior flag
			if(nullBehavior == NullMeans.ANY) return true;
			else return objectValue == null;
		}
		
		// the value is not null, do standard comparison
		
		// first do value conversion
		try{
			objectValue = ValueConverter.toClass(objectValue, value.getClass());
		}catch(Exception ex){
			/*
			 * We have to ignore this exception as conversion can fail for any
			 * number of reasons. And even if an exception is raised, the system
			 * should be able to continue with normal comparison, the only
			 * potential error being that a value that should qualify (if it was
			 * converted properly) does not qualify.
			 */
		}
		
		// equality can be checked without actually comparing
		// the two values, when the comparator is null
		if(comparator == null){
			if(operator == Op.EQUAL) return value.equals(objectValue);
			if(operator == Op.NOT_EQUAL) return !value.equals(objectValue);
			if((operator == Op.GREATER_THAN_OR_EQUAL || operator == Op.LESS_THAN_OR_EQUAL)
					&& value.equals(objectValue)) return true;
		}
		
		// now deal with the possibility of the DataObject value being null
		if(objectValue == null){
			return false;
		}
		
		// now deal with string-specific Ops
		if(operator == Op.INSENSITIVE_LIKE){
			return value.toString().compareToIgnoreCase(objectValue.toString()) == 0;
		}
		
		// need to actually compare the values
		int compareValue = 0;
		if(comparator == null){
			@SuppressWarnings("unchecked")
			Comparable<T> comparableObjectValue = (Comparable<T>)objectValue;
			compareValue = -(comparableObjectValue.compareTo(value));
		}else{
			@SuppressWarnings("unchecked")
			T castObjectValue = (T)objectValue;
			compareValue = comparator.compare(value, castObjectValue);
		}
		
		switch(operator){
			case EQUAL : case LIKE :
				return compareValue == 0;
			case NOT_EQUAL :
				return compareValue != 0;
			case GREATER_THAN :
				return compareValue < 0;
			case GREATER_THAN_OR_EQUAL :
				return compareValue <= 0;
			case LESS_THAN :
				return compareValue > 0;
			case LESS_THAN_OR_EQUAL :
				return compareValue >= 0;
			default :
				// if it did happen, then an operator was set
				// on the qualifier, but one that's not handled in this method
				throw new IllegalStateException(
						"This exception should never happen...");
		}
	}
	
	public String toString(){
		String valueString = value == null ? "[null]" :
			value+" ("+value.getClass().getName()+")";
		return "KeyValueQualifier, key: "+key+
			", value: "+valueString+
			", operator:"+readableStringForOperator(operator);
	}
}