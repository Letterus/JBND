package org.jbnd.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;


/**
 * Provides ways of converting values from one class to another.
 * <p>
 * Users should always use static methods of the class, they in turn will call
 * the appropriate methods of the currently set <tt>CONVERTER</tt>. If using
 * software requires JBND to perform conversions in a specific way, another
 * implementation of <tt>ValueConverter.Interface</tt> can be set as the
 * <tt>CONVERTER</tt> field.
 * 
 * @version 1.0 Dec 26, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see #CONVERTER
 * @see Interface
 */
public class ValueConverter{
	
	/**
	 * This field is used by all of <tt>ValueConverter</tt>'s
	 * static methods to perform the actual converting. Isolating
	 * the conversion logic into a field enables users to implement
	 * different conversion methods, and then set the converting
	 * object (one that implements the <tt>ValueConverter.Interface</tt>)
	 * as this field. JBND will then use their custom conversion
	 * methods.
	 */
	public static Interface CONVERTER = new DefaultImplementation();
	
	/**
	 * This method ATTEMPTS to convert the given value to the given class, if
	 * the given class is one of the classes for which specialized methods are
	 * provided; if it is not, the same value is returned. If there is a
	 * specialized conversion method for the given class, it is possible that
	 * this method will throw a <tt>RuntimeException</tt>. If the given value
	 * already is of the given class, it will simply be returned.
	 * 
	 * @param value The same value which should be converted.
	 * @param clazz
	 * @return The given <tt>value</tt> converted to the given <tt>clazz</tt>,
	 *         or the given <tt>value</tt> itself, if no conversion method is
	 *         provided for the given <tt>clazz</tt>.
	 * @throws RuntimeException If <tt>value</tt> can not be converted to the
	 *             given <tt>clazz</tt>.
	 * @throws ClassCastException If <tt>value</tt> can not be converted to the
	 *             given <tt>clazz</tt>.
	 */
	public static <T> T toClass(Object value, Class<T> clazz){
		@SuppressWarnings("unchecked")
		T rVal = (T)CONVERTER.toClass(value, clazz);
		return rVal;
	}
	
	/**
	 * Converts the given <tt>value</tt> into a <tt>Boolean</tt>,
	 * if possible. This method simply calls the <tt>toBoolean(Object)</tt>
	 * method of the current <tt>CONVERTER</tt>, to change the way
	 * this method behaves, use another implementation.
	 * 
	 * @param value	The object to try to convert.
	 * @return		The RuntimeException object, converted into <tt>Boolean</tt>.
	 * @throws	RuntimeException	If the given value can
	 * 					not be converted into a <tt>Boolean</tt>.
	 * @see	#CONVERTER
	 */
	public static Boolean toBoolean(Object value){
		return CONVERTER.toBoolean(value);
	}

	/**
	 * Converts the given <tt>value</tt> into an <tt>Integer</tt>,
	 * if possible. This method simply calls the <tt>toInteger(Object)</tt>
	 * method of the current <tt>CONVERTER</tt>, to change the way
	 * this method behaves, use another implementation.
	 * 
	 * @param value	The object to try to convert.
	 * @return		The given object, converted into <tt>Integer</tt>.
	 * @throws	RuntimeException	If the given value can
	 * 					not be converted into a <tt>Integer</tt>.
	 * @see	#CONVERTER
	 */
	public static Integer toInteger(Object value){
		return CONVERTER.toInteger(value);
	}
	
	/**
	 * Converts the given <tt>value</tt> into a <tt>Long</tt>,
	 * if possible. This method simply calls the <tt>toLong(Object)</tt>
	 * method of the current <tt>CONVERTER</tt>, to change the way
	 * this method behaves, use another implementation.
	 * 
	 * @param value	The object to try to convert.
	 * @return		The given object, converted into <tt>Long</tt>.
	 * @throws	RuntimeException	If the given value can
	 * 					not be converted into a <tt>Long</tt>.
	 * @see	#CONVERTER
	 */
	public static Long toLong(Object value){
		return CONVERTER.toLong(value);
	}
	
	/**
	 * Converts the given <tt>value</tt> into a <tt>Float</tt>,
	 * if possible. This method simply calls the <tt>toFloat(Object)</tt>
	 * method of the current <tt>CONVERTER</tt>, to change the way
	 * this method behaves, use another implementation.
	 * 
	 * @param value	The object to try to convert.
	 * @return		The given object, converted into <tt>Float</tt>.
	 * @throws	RuntimeException	If the given value can
	 * 					not be converted into a <tt>Float</tt>.
	 * @see	#CONVERTER
	 */
	public static Float toFloat(Object value){
		return CONVERTER.toFloat(value);
	}
	
	/**
	 * Converts the given <tt>value</tt> into a <tt>Double</tt>,
	 * if possible. This method simply calls the <tt>toDouble(Object)</tt>
	 * method of the current <tt>CONVERTER</tt>, to change the way
	 * this method behaves, use another implementation.
	 * 
	 * @param value	The object to try to convert.
	 * @return		The given object, converted into <tt>Double</tt>.
	 * @throws	RuntimeException	If the given value can
	 * 					not be converted into a <tt>Double</tt>.
	 * @see	#CONVERTER
	 */
	public static Double toDouble(Object value){
		return CONVERTER.toDouble(value);
	}
	
	/**
	 * Converts the given <tt>value</tt> into a <tt>Date</tt>,
	 * if possible. This method simply calls the <tt>toDate(Object)</tt>
	 * method of the current <tt>CONVERTER</tt>, to change the way
	 * this method behaves, use another implementation.
	 * 
	 * @param value	The object to try to convert.
	 * @return		The given object, converted into <tt>Date</tt>.
	 * @throws	RuntimeException	If the given value can
	 * 					not be converted into a <tt>Date</tt>.
	 * @see	#CONVERTER
	 */
	public static Date toDate(Object value){
		if(value == null) return null;
		return CONVERTER.toDate(value);
	}
	
	/**
	 * Converts the given <tt>value</tt> into a <tt>String</tt>.
	 * This method simply calls the <tt>toString(Object)</tt>
	 * method of the current <tt>CONVERTER</tt>, to change the way
	 * this method behaves, use another implementation.
	 * 
	 * @param value	The object to try to convert.
	 * @return		The given object, converted into <tt>String</tt>.
	 * @see	#CONVERTER
	 */
	public static String toString(Object value){
		if(value == null) return null;
		return CONVERTER.toString(value);
	}
	
	/**
	 * Converts the given <tt>value</tt> into a <tt>BigDecimal</tt>,
	 * if possible. This method simply calls the <tt>toBigDecimal(Object)</tt>
	 * method of the current <tt>CONVERTER</tt>, to change the way
	 * this method behaves, use another implementation.
	 * 
	 * @param value	The object to try to convert.
	 * @return		The given object, converted into <tt>BigDecimal</tt>.
	 * @throws	RuntimeException	If the given value can
	 * 					not be converted into a <tt>BigDecimal</tt>.
	 * @see	#CONVERTER
	 */
	public static BigDecimal toBigDecimal(Object value){
		return CONVERTER.toBigDecimal(value);
	}
	
	/**
	 * Converts the given <tt>value</tt> into a <tt>BigInteger</tt>,
	 * if possible. This method simply calls the <tt>BigInteger(Object)</tt>
	 * method of the current <tt>CONVERTER</tt>, to change the way
	 * this method behaves, use another implementation.
	 * 
	 * @param value	The object to try to convert.
	 * @return		The given object, converted into <tt>BigInteger</tt>.
	 * @throws	RuntimeException	If the given value can
	 * 					not be converted into a <tt>BigInteger</tt>.
	 * @see	#CONVERTER
	 */
	public static BigInteger toBigInteger(Object value){
		return CONVERTER.toBigInteger(value);
	}
	
	/**
	 * An interface for converter objects, implementations can be set
	 * as the <tt>ValueConverter.CONVERTER</tt> field which will then
	 * make the <tt>ValueConverter</tt>'s static method use them. This
	 * will influence the value converting processes in the JBND library.
	 *
		@version 1.0 Dec 26, 2007
		@author Florijan Stamenkovic (flor385@mac.com)
		@see	ValueConverter
		@see	ValueConverter#CONVERTER
	 */
	public interface Interface{
		
		/**
		 * This method ATTEMPTS to convert the given value to the given class.
		 * If there is a specialized conversion method for the given class, it
		 * is possible that this method will throw a <tt>RuntimeException</tt>.
		 * If the given value already is of the given class, it will simply be
		 * returned. If conversion is not possible, the same value will be
		 * returned.
		 * 
		 * @param value The same value which should be converted.
		 * @param clazz The type to which it should be converted.
		 * @return The given <tt>value</tt> converted to the given
		 *         <tt>clazz</tt>, or the given <tt>value</tt> itself, if
		 *         no conversion method is provided for the given <tt>clazz</tt>.
		 * @throws RuntimeException In certain cases some subclass of
		 *             <tt>RuntimeException</tt> can be thrown.
		 */
		public Object toClass(Object value, Class<?> clazz);
		
		/**
		 * Converts the given <tt>value</tt> into a <tt>Boolean</tt>,
		 * if possible.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>Boolean</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>Boolean</tt>.
		 */
		public Boolean toBoolean(Object value);
		
		/**
		 * Converts the given <tt>value</tt> into an <tt>Integer</tt>,
		 * if possible.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>Integer</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>Integer</tt>.
		 */
		public Integer toInteger(Object value);
		
		/**
		 * Converts the given <tt>value</tt> into an <tt>Long</tt>,
		 * if possible.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>Long</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>Long</tt>.
		 */
		public Long toLong(Object value);
		
		/**
		 * Converts the given <tt>value</tt> into a <tt>Float</tt>,
		 * if possible.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>Float</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>Float</tt>.
		 */
		public Float toFloat(Object value);
		
		/**
		 * Converts the given <tt>value</tt> into a <tt>Double</tt>,
		 * if possible.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>Double</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>Double</tt>.
		 */
		public Double toDouble(Object value);
		
		/**
		 * Converts the given <tt>value</tt> into a <tt>BigInteger</tt>,
		 * if possible.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>BigInteger</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>BigInteger</tt>.
		 */
		public BigInteger toBigInteger(Object value);
		
		/**
		 * Converts the given <tt>value</tt> into a <tt>BigDecimal</tt>,
		 * if possible.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>BigDecimal</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>BigDecimal</tt>.
		 */
		public BigDecimal toBigDecimal(Object value);
		
		/**
		 * Converts the given <tt>value</tt> into a <tt>Date</tt>,
		 * if possible.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>Date</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>Date</tt>.
		 */
		public Date toDate(Object value);
		
		/**
		 * Converts the given <tt>value</tt> into a <tt>String</tt>.
		 * This enables implementations to use a different
		 * conversion then the <tt>toString()</tt> method provides.
		 * 
		 * @param value	The object to convert.
		 * @return		The given object, converted into <tt>String</tt>.
		 */
		public String toString(Object value);
	}
	
	/**
	 * The default implementation used by the <tt>ValueConverter</tt>
	 * to convert values.
	 *
		@version 1.0 Dec 26, 2007
		@author Florijan Stamenkovic (flor385@mac.com)
		@see	ValueConverter#CONVERTER
	 */
	public static class DefaultImplementation implements Interface{
		
		public Object toClass(Object value, Class<?> clazz){
			
			if(value instanceof OneValDataObject)
				value = (((OneValDataObject)value).get(OneValDataObject.KEY));
			
			// check for null, in combination with
			// asking for primitive types
			if(clazz.isPrimitive() && value == null){
				if(clazz.equals(Boolean.TYPE))
					return false;
				if(clazz.equals(Integer.TYPE))
					return 0;
				if(clazz.equals(Long.TYPE))
					return 0l;
				if(clazz.equals(Float.TYPE))
					return 0f;
				if(clazz.equals(Double.TYPE))
					return 0d;
				if(clazz.equals(Byte.TYPE))
					return (byte)0;
				if(clazz.equals(Character.TYPE))
					return '\u0000';
			}
			
			//	if it is assignable, no conversion necessary
			if(value != null && clazz.isAssignableFrom(value.getClass()))
				return value;
			
			// classes that can handle null
			if(clazz == Boolean.class || clazz == Boolean.TYPE)
				return toBoolean(value);
			
			// null
			if(value == null) return null;
				
			// classes that can't handle null
			if(clazz == Date.class)
				return toDate(value);
			if(clazz == String.class)
				return toString(value);
			if(clazz == Integer.class || clazz == Integer.TYPE)
				return toInteger(value);
			if(clazz == Long.class || clazz == Long.TYPE)
				return toInteger(value);
			if(clazz == Float.class || clazz == Float.TYPE)
				return toFloat(value);
			if(clazz == Double.class || clazz == Double.TYPE)
				return toDouble(value);
			if(clazz == BigDecimal.class)
				return toBigDecimal(value);
			if(clazz == BigInteger.class)
				return toBigInteger(value);
			
			return value;
		}
		
		/**
		 * Converts the given <tt>value</tt> into a <tt>Boolean</tt>, if
		 * possible. This implementation can convert <tt>String</tt>s,
		 * <tt>Number</tt>s and other <tt>Object</tt>s. <tt>String</tt>s
		 * can be converted if the given value can be parsed by the
		 * <tt>Boolean(String)</tt> constructor; <tt>Number</tt>s convert
		 * to <tt>false</tt> if their <tt>intValue()</tt> == 0, and to
		 * <tt>true</tt> otherwise. Other <tt>Object</tt>s convert to
		 * <tt>true</tt> if they are not <tt>null</tt>, and to
		 * <tt>false</tt> if they are.
		 * 
		 * @param value The object to try to convert.
		 * @return The given object, converted into <tt>Boolean</tt>.
		 */
		public Boolean toBoolean(Object value){
			
			if(value instanceof OneValDataObject)
				value = (((OneValDataObject)value).get(OneValDataObject.KEY));
			
			if(value instanceof String)
				return new Boolean((String)value);
			if(value instanceof Number)
				return ((Number)value).intValue() != 0;
			if(value instanceof Boolean)
				return (Boolean)value;
			
			return (value != null);
		}

		/**
		 * Converts the given <tt>value</tt> into an <tt>Integer</tt>,
		 * if possible. This implementation can convert <tt>String</tt>s
		 * <tt>Number</tt>s and <tt>Boolean</tt>s. <tt>String</tt>s can
		 * be converted if the given value can be parsed by the 
		 * <tt>Integer(String)</tt> constructor; <tt>Number</tt>s convert
		 * easily with their <tt>toInt()</tt> method, and <tt>Boolean</tt>s
		 * convert to 0 if <tt>false</tt>, and to 1 if <tt>true</tt>.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>Integer</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>Integer</tt>.
		 */
		public Integer toInteger(Object value){
			
			if(value instanceof OneValDataObject)
				value = (((OneValDataObject)value).get(OneValDataObject.KEY));
			
			if(value == null)
				return null;
			
			if(value instanceof String)
				return new Integer((String)value);
			if(value instanceof Boolean)
				return ((Boolean)value).booleanValue() ? 1 : 0;
			if(value instanceof Number)
				return ((Number)value).intValue();
			if(value instanceof Enum)
				return ((Enum<?>)value).ordinal();
			
			throw new IllegalArgumentException(
					"Can not convert from "+value.getClass()+" to Integer");
		}
		
		/**
		 * Converts the given <tt>value</tt> into an <tt>Long</tt>,
		 * if possible. This implementation can convert <tt>String</tt>s
		 * <tt>Number</tt>s and <tt>Boolean</tt>s. <tt>String</tt>s can
		 * be converted if the given value can be parsed by the 
		 * <tt>Integer(String)</tt> constructor; <tt>Number</tt>s convert
		 * easily with their <tt>toLong()</tt> method, and <tt>Boolean</tt>s
		 * convert to 0 if <tt>false</tt>, and to 1 if <tt>true</tt>.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>Long</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>Long</tt>.
		 */
		public Long toLong(Object value){
			
			if(value instanceof OneValDataObject)
				value = (((OneValDataObject)value).get(OneValDataObject.KEY));
			
			if(value == null)
				return null;
			
			if(value instanceof String)
				return new Long((String)value);
			if(value instanceof Boolean)
				return ((Boolean)value).booleanValue() ? 1l : 0l;
			if(value instanceof Number)
				return ((Number)value).longValue();
			if(value instanceof Enum)
				return new Long(((Enum<?>)value).ordinal());
			
			throw new IllegalArgumentException(
					"Can not convert from "+value.getClass()+" to Long");
		}
		
		/**
		 * Converts the given <tt>value</tt> into a <tt>Float</tt>,
		 * if possible. This implementation can convert <tt>String</tt>s
		 * <tt>Number</tt>s and <tt>Boolean</tt>s. <tt>String</tt>s can
		 * be converted if the given value can be parsed by the 
		 * <tt>Integer(String)</tt> constructor; <tt>Number</tt>s convert
		 * easily with their <tt>toFloat()</tt> method, and <tt>Boolean</tt>s
		 * convert to 0 if <tt>false</tt>, and to 1 if <tt>true</tt>.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>Float</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>Float</tt>.
		 */
		public Float toFloat(Object value){
			
			if(value instanceof OneValDataObject)
				value = (((OneValDataObject)value).get(OneValDataObject.KEY));
			
			if(value == null)
				return null;
			
			if(value instanceof String)
				return new Float((String)value);
			if(value instanceof Boolean)
				return ((Boolean)value).booleanValue() ? 1.0f : 0.0f;
			if(value instanceof Number)
				return ((Number)value).floatValue();
			if(value instanceof Enum)
				return new Float(((Enum<?>)value).ordinal());
			
			throw new IllegalArgumentException(
					"Can not convert from "+value.getClass()+" to Float");
		}
		
		/**
		 * Converts the given <tt>value</tt> into a <tt>Double</tt>,
		 * if possible. This implementation can convert <tt>String</tt>s
		 * <tt>Number</tt>s and <tt>Boolean</tt>s. <tt>String</tt>s can
		 * be converted if the given value can be parsed by the 
		 * <tt>Integer(String)</tt> constructor; <tt>Number</tt>s convert
		 * easily with their <tt>toDouble()</tt> method, and <tt>Boolean</tt>s
		 * convert to 0 if <tt>false</tt>, and to 1 if <tt>true</tt>.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>Double</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>Double</tt>.
		 */
		public Double toDouble(Object value){
			
			if(value instanceof OneValDataObject)
				value = (((OneValDataObject)value).get(OneValDataObject.KEY));
			
			if(value == null)
				return null;
			
			if(value instanceof String)
				return new Double((String)value);
			if(value instanceof Boolean)
				return ((Boolean)value).booleanValue() ? 1.0d : 0.0d;
			if(value instanceof Number)
				return ((Number)value).doubleValue();
			if(value instanceof Enum)
				return new Double(((Enum<?>)value).ordinal());
			
			throw new IllegalArgumentException(
					"Can not convert from "+value.getClass()+" to Double");
		}
		
		private static final DateFormat[] _DATE_FORMATS = new DateFormat[]{
			DateFormat.getDateInstance(),
			DateFormat.getDateInstance(DateFormat.SHORT),
			DateFormat.getDateInstance(DateFormat.MEDIUM),
			DateFormat.getDateInstance(DateFormat.LONG),
			DateFormat.getDateTimeInstance(),
			DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT),
			DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT),
			DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT),
			new SimpleDateFormat("MM/dd/yy"),
			new SimpleDateFormat("dd, MM, yy"),
			new SimpleDateFormat("dd/MM/yy"),
			new SimpleDateFormat("MM, dd, yy"),
			new SimpleDateFormat("MM dd yy"),
			new SimpleDateFormat("yy, MM, dd"),
			new SimpleDateFormat("yy/MM/dd"),
		};
		
		private static final DecimalFormat[] _DECIMAL_NUMBER_FORMATS = new DecimalFormat[]{
			new DecimalFormat("#,###.#"),
			new DecimalFormat("#,###.#;(#,###.#)"),
			new DecimalFormat("#.#E0"),
			new DecimalFormat("#.#E0;(#.#E0)"),
		};
		
		static{
			for(DecimalFormat f : _DECIMAL_NUMBER_FORMATS)
				f.setParseBigDecimal(true);
		}
		
		/**
		 * Converts the given <tt>value</tt> into a <tt>Date</tt>,
		 * if possible. This implementation can convert <tt>String</tt>
		 * and <tt>Number</tt> objects. <tt>Number</tt>s are converted
		 * by using their <tt>toLong()</tt> value with a <tt>DateConverter</tt>
		 * that accepts it.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>Date</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>Date</tt>.
		 */
		public Date toDate(Object value){
			
			if(value instanceof OneValDataObject)
				value = (((OneValDataObject)value).get(OneValDataObject.KEY));
			
			if(value == null)
				return null;
			
			if(value instanceof Date)
				return (Date)value;
			else if(value instanceof String)
				for(DateFormat format : _DATE_FORMATS){
					try{
						return format.parse((String)value);
					}catch(ParseException e){}
				}
			else if(value instanceof Number)
				return new Date(((Number)value).longValue());

			throw new IllegalArgumentException(
					"Can not convert from "+value.getClass()+" to Date");
		}
		
		/**
		 * Converts the given <tt>value</tt> into a <tt>String</tt>.
		 * This implementation does special conversion for <tt>Collection</tt>s
		 * and <tt>Map</tt>s, for all other classes of objects it simply
		 * calls their <tt>toString</tt> method.
		 * 
		 * @param value	The object to convert.
		 * @return		The given object, converted into <tt>String</tt>.
		 */
		public String toString(Object value){
			
			if(value instanceof OneValDataObject)
				value = (((OneValDataObject)value).get(OneValDataObject.KEY));
			
			if(value == null)
				return null;
			
			if(value instanceof Collection){
				Collection<?> c = (Collection<?>)value;
				StringBuilder builder = new StringBuilder();
				boolean first = true;
				for(Object o : c){
					if(first){
						first = false;
					}else
						builder.append("\n");
					builder.append(o.toString());
				}
				return builder.toString();
				
			}else if(value instanceof Map){
				Map<?,?> m = (Map<?,?>)value;
				Set<?> keys = m.keySet();
				StringBuilder builder = new StringBuilder();
				boolean first = true;
				for(Object key : keys){
					if(first){
						first = false;
					}else
						builder.append("\n");
					builder.append(key.toString());
					builder.append(": ");
					builder.append(m.get(key));
				}
				return builder.toString();
				
			}else
				return value.toString();
		}

		/**
		 * Converts the given <tt>value</tt> into a <tt>BigDecimal</tt>,
		 * if possible.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>BigDecimal</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>BigDecimal</tt>.
		 */
		public BigDecimal toBigDecimal(Object value){
			
			if(value instanceof OneValDataObject)
				value = (((OneValDataObject)value).get(OneValDataObject.KEY));
			
			if(value == null)
				return null;
			
			if(value instanceof char[])
				value = new String((char[])value);
			
			if(value instanceof String){
				
				// we have some parsers for this
				for(DecimalFormat f : _DECIMAL_NUMBER_FORMATS){
					// try to parse, if not successful, that's OK
					try{
						return (BigDecimal)f.parse((String)value);
					}catch (Exception e) {
						// swallow, let other parsers to give it a go
					}
				}
				
			}if(value instanceof BigInteger)
				return new BigDecimal((BigInteger)value);
			
			if(value instanceof Integer || value instanceof Long)
				return new BigDecimal(((Number)value).longValue());
			if(value instanceof Float || value instanceof Double)
				return new BigDecimal(((Number)value).doubleValue());
			
			throw new IllegalArgumentException(
					"Can not convert from "+value.getClass()+" to BigDecimal");
		}
		
		private static final BigDecimal POINT_HALF = new BigDecimal(0.5f);

		/**
		 * Converts the given <tt>value</tt> into a <tt>BigInteger</tt>,
		 * if possible.
		 * 
		 * @param value	The object to try to convert.
		 * @return		The given object, converted into <tt>BigInteger</tt>.
		 * @throws	RuntimeException	If the given value can
		 * 					not be converted into a <tt>BigInteger</tt>.
		 */
		public BigInteger toBigInteger(Object value){
			
			if(value instanceof OneValDataObject)
				value = (((OneValDataObject)value).get(OneValDataObject.KEY));
			
			if(value == null)
				return null;
			
			if(value instanceof String)
				return toBigDecimal(value).add(POINT_HALF).toBigInteger();
			if(value instanceof Number)
				return new BigInteger(value.toString());
			
			throw new IllegalArgumentException(
					"Can not convert from "+value.getClass()+" to BigInteger");
		}
	}
}