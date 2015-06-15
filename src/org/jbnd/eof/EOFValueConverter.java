package org.jbnd.eof;

import java.util.Date;

import org.jbnd.support.ValueConverter;
import org.jbnd.support.ValueConverter.DefaultImplementation;

import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSTimestamp;

/**
 * Adds some EOF specific conversions to the standard <tt>ValueConverter</tt>,
 * note that an instance of this class has to be set as the default converter
 * (in the <tt>ValueConverter</tt> class) before JBND can use the additional
 * conversion it provides.
 * 
 * @version 1.0 Apr 8, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see	ValueConverter
 */
public class EOFValueConverter extends DefaultImplementation{

	/**
	 * Extends the <tt>DefaultImplementation</tt> functionality to encompass
	 * the conversion between <tt>byte[]</tt> and <tt>NSData</tt>,
	 * <tt>java.util.Date</tt>s and <tt>NSTimestamp</tt>s.
	 * 
	 * @param value The value to try to convert.
	 * @param clazz The type to try to convert it into.
	 * @return See superclass documentation.
	 */
	public Object toClass(Object value, Class<?> clazz){
		
		//	byte[] to NSData and vice versa
		if(clazz.equals(new byte[0].getClass()) && value instanceof NSData)
			return ((NSData)value).bytes();
		if(clazz.equals(NSData.class) && new byte[0].getClass().isInstance(value))
			return new NSData((byte[])value);
		
		//	java.util.Date to NSDate and vice versa
		if(clazz.equals(NSTimestamp.class) && value instanceof Date)
			return new NSTimestamp((Date)value);
		if(clazz.equals(Date.class) && value instanceof NSTimestamp)
			return new Date(((NSTimestamp)value).getTime());
		
		return super.toClass(value, clazz);
	}
}