package org.jbnd.qual;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.jbnd.DataObject;
import org.jbnd.support.JBNDUtil;


/**
 * A <tt>KeyValueQualifier</tt> specialized in evaluating <tt>Date</tt>s. A
 * <tt>Date</tt> set as this qualifier's value is normalized using
 * {@link JBNDUtil#normalize(Date, TimeZone)}. Then two values are extrapolated
 * from it, the beginning of the day that the normalized <tt>Date</tt>
 * represents, and the end of it, both in GMT. Those values are then used to
 * evaluate <tt>Date</tt>s found in <tt>DataObject</tt>s this qualifier is
 * evaluating. If the <tt>Date</tt> from the <tt>DataObject</tt>, when
 * normalized using the mentioned method, falls between the range mentioned, it
 * is considered equal. If it falls before the range, it is considered lesser,
 * if it falls after the range it is considered greater.
 * 
 * @version 1.0 Dec 30, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public final class NormalizedDateQualifier extends KeyValueQualifier<Date>{

	// the calendar used to evaluate whether a given normalized date
	// fits within the current Date value of this qualifier
	private static final Calendar gmtCalendar = 
		Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	
	// the timezone used to normalized the given Date value
	private static final TimeZone tz = TimeZone.getDefault();
	
	// the upper limit (inclusive) of the Date value of this qualifier
	private Date fromBoundary;
	
	// the lower limit (inclusive) of the Date value of this qualifier
	private Date toBoundary;
	
	/**
	 * Depreciated, use
	 * {@link #NormalizedDateQualifier(String, KeyValueQualifier.Op, Date, KeyValueQualifier.NullMeans)}
	 * .
	 */
	@Deprecated
	public NormalizedDateQualifier(String key, int operator,
			Date value, int nullBehavior){
		super(key, operator, value, nullBehavior);
	}
	
	/**
	 * Creates a <tt>NormalizedDateQualifier</tt>, the parameters are simply
	 * passed along to the superclass constructor.
	 * 
	 * @param key See superclass.
	 * @param operator See superclass.
	 * @param value See superclass.
	 * @param nullBehavior See superclass.
	 * @see KeyValueQualifier#KeyValueQualifier(String, Op, Object, NullMeans)
	 */
	public NormalizedDateQualifier(String key, Op operator, Date value,
			NullMeans nullBehavior){
		super(key, operator, value, nullBehavior);
	}

	/**
	 * Depreciated, use
	 * {@link #NormalizedDateQualifier(String, KeyValueQualifier.Op, Date)}.
	 */
	@Deprecated
	public NormalizedDateQualifier(String key, int operator, Date value){
		super(key, operator, value);
	}
	
	/**
	 * Creates a <tt>NormalizedDateQualifier</tt>, the parameters are simply
	 * passed along to the superclass constructor.
	 * 
	 * @param key See superclass.
	 * @param operator See superclass.
	 * @param value See superclass.
	 * @see KeyValueQualifier#KeyValueQualifier(String, Op, Object, NullMeans)
	 */
	public NormalizedDateQualifier(String key, Op operator, Date value){
		super(key, operator, value);
	}

	/**
	 * Depreciated, use
	 * {@link #NormalizedDateQualifier(String, KeyValueQualifier.Op)}.
	 */
	@Deprecated
	public NormalizedDateQualifier(String key, int operator){
		super(key, operator);
	}
	
	/**
	 * Creates a <tt>NormalizedDateQualifier</tt>, the parameters are simply
	 * passed along to the superclass constructor.
	 * 
	 * @param key See superclass.
	 * @param operator See superclass.
	 * @see KeyValueQualifier#KeyValueQualifier(String, Op, Object, NullMeans)
	 */
	public NormalizedDateQualifier(String key, Op operator){
		super(key, operator);
	}
	
	/**
	 * Overridden to disable support for <tt>Op.LIKE</tt> and
	 * <tt>Op.INSENSITIVE_LIKE</tt> as the <tt>NormalizedDateQualifier</tt> does
	 * not support those operators.
	 * 
	 * @param operator The <tt>Op</tt> to set on this qualifier.
	 */
	@Override
	public void setOperator(Op operator){
		switch(operator){
			case LIKE :
			case INSENSITIVE_LIKE :
				throw new IllegalArgumentException(
						"NormalizedDateQualifier does not support operator: "
								+ operator);
			default :
				super.setOperator(operator);
		}
	}

	/**
	 * Overridden to determine the upper and lower boundaries (relative to
	 * the GMT timezone) of the given <tt>Date</tt>, which will then be
	 * used to evaluate date values found in <tt>DataObject</tt>s.
	 * 
	 * @param value	The <tt>Date</tt> which defines which <tt>Date<tt> values
	 * are acceptable or not.
	 */
	@Override
	public void setValue(Date value){
		value = value == null ? null : JBNDUtil.normalize(value, tz);
		
		if(value != null){
			
			// need to extract the GMT start and end for the existing date
			gmtCalendar.setTime(value);
			int year = gmtCalendar.get(Calendar.YEAR);
			int month = gmtCalendar.get(Calendar.MONTH);
			int day = gmtCalendar.get(Calendar.DATE);
			
			// and set the from and to boundaries
			
			// to boundary
			gmtCalendar.set(year, month, day, 23, 59, 59);
			gmtCalendar.roll(Calendar.MILLISECOND, 999);
			toBoundary = gmtCalendar.getTime();
			
			// from boundary
			gmtCalendar.set(year, month, day, 0, 0, 0);
			gmtCalendar.roll(Calendar.MILLISECOND, 001);
			fromBoundary = gmtCalendar.getTime();
			
		}else{
			fromBoundary = null;
			toBoundary = null;
		}
		
		super.setValue(value);
	}

	/**
	 * Overridden to perform custom qualification based on a day wide range of
	 * <tt>Date</tt> values.
	 * 
	 * @param object The <tt>DataObject</tt> being checked if it qualifies.
	 * @return Si o no.
	 */
	@Override
	public boolean accept(DataObject object){
		
		if(object == null) return false;
		Date objectValue = (Date)object.get(getKey());
		Op operator = getOperator();
		
		if(getValue() == null){
			//	a value is null situation
			//	return based on the nullBehavior flag
			if(getNullBehavior() == NullMeans.ANY) return true;
			else return objectValue == null;
		}
		
		// the value is not null, do standard comparison
		
		// deal with the possibility of the DataObject value being null
		if(objectValue == null){
			return false;
		}
		
		//	need to actually compare the values
		int compareValue = 0;
		long date = objectValue.getTime();
		if(date >= fromBoundary.getTime()){
			compareValue = date <= toBoundary.getTime() ? 0 : -1;
		}else
			compareValue = 1;
		
		switch(getOperator()){
			case EQUAL :
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
						"Operator not supported by NormalizedDateQualifier: "+operator);
		}
	}	
}