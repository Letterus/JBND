package org.jbnd;

import org.jbnd.support.ValidationExceptionSupport;

/**
 * An exception indicating that validation of the <tt>value</tt> on the
 * <tt>DataObject</tt> for the <tt>key</tt> has failed. It is possible that
 * the <tt>source DataObject</tt> of the thrown exception is unknown, in that
 * case the <tt>DataType</tt> of the object throwing the exception has to be
 * known.
 * <p>
 * The functionality of translating <tt>ValidationException</tt>s into user
 * feedback is provided by the <tt>ValidationExceptionSupport</tt> class.
 * <p>
 * It is possible to provide a <tt>String</tt> to the
 * <tt>ValidationException</tt> which should contained more detailed
 * information on why the <tt>value</tt> is not valid. If it is provided, that
 * <tt>String</tt> should be used in user feedback.
 * 
 * @version 1.0 Dec 17, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see ValidationExceptionSupport
 */
public final class ValidationException extends RuntimeException{

	// the information String that will override the automatic message creation
	private final String message;

	// the invalid object
	private final Object value;

	// the key for which there is invalidity
	private final String key;

	// the object for which the key and value were being validates
	private final DataObject source;

	// the data source, in case the source is not known
	private final DataType sourceType;

	/**
	 * Creates a new <tt>ValidationException</tt> with the given parameters.
	 * 
	 * @param value	The invalid value.
	 * @param key	The key for which the validation failed.
	 * @param source	The <tt>DataObject</tt> on which validation failed.
	 */
	public ValidationException(Object value, String key, DataObject source){
		this.value = value;
		this.key = key;
		this.source = source;
		this.sourceType = source.getDataType();
		this.message = null;
	}
	
	/**
	 * Creates a new <tt>ValidationException</tt> with the given parameters.
	 * 
	 * @param value The invalid value.
	 * @param key The key for which the validation failed.
	 * @param source The <tt>DataObject</tt> on which validation failed.
	 * @param message An optional parameter, if provided it should be used in
	 *            user feedback, otherwise the feedback message will be
	 *            generated from the other properties.
	 */
	public ValidationException(Object value, String key, DataObject source, String message){
		this.value = value;
		this.key = key;
		this.source = source;
		this.sourceType = source.getDataType();
		this.message = message;
	}
	
	/**
	 * Creates a new <tt>ValidationException</tt> with the given parameters;
	 * this constructor should be used when it is not possible to use
	 * the one that takes a <tt>DataObject</tt> as a parameter.
	 * 
	 * @param value	The invalid value.
	 * @param key	The key for which the validation failed.
	 * @param sourceType	The <tt>DataType</tt> for which validation failed.
	 */
	public ValidationException(Object value, String key, DataType sourceType){
		this.value = value;
		this.key = key;
		this.sourceType = sourceType;
		this.source = null;
		this.message = null;
	}
	
	/**
	 * Creates a new <tt>ValidationException</tt> with the given parameters;
	 * this constructor should be used when it is not possible to use
	 * the one that takes a <tt>DataObject</tt> as a parameter.
	 * 
	 * @param value	The invalid value.
	 * @param key	The key for which the validation failed.
	 * @param sourceType	The <tt>DataType</tt> for which validation failed.
	 * @param message An optional parameter, if provided it should be used in
	 *            user feedback, otherwise the feedback message will be
	 *            generated from the other properties.
	 */
	public ValidationException(Object value, String key, DataType sourceType, String message){
		this.value = value;
		this.key = key;
		this.source = null;
		this.sourceType = sourceType;
		this.message = message;
	}

	/**
	 * Returns the key for which the validation failed.
	 * 
	 * @return	The key for which the validation failed.
	 */
	public String getKey(){
		return key;
	}
	
	/**
	 * Returns the invalid value.
	 * 
	 * @return	The invalid value.
	 */
	public Object getValue(){
		return value;
	}
	
	/**
	 * Returns the <tt>DataObject</tt> on which validation failed, if the
	 * <tt>ValidationException</tt> was created and thrown by a
	 * <tt>DataType</tt>, as opposed to a <tt>DataObject</tt>, then this method
	 * returns <tt>null</tt>.
	 * 
	 * @return The <tt>DataObject</tt> on which validation failed, or
	 *         <tt>null</tt>, when validation did not fail for a specific
	 *         <tt>DataObject</tt>.
	 */
	public DataObject getSource(){
		return source;
	}
	
	/**
	 * Returns the <tt>DataType</tt> of the object on which validation failed;
	 * if the <tt>source</tt> of this exception is not <tt>null</tt>, then the
	 * object this method returns will be it's <tt>DataType</tt>.
	 * 
	 * @return See above.
	 */
	public DataType getSourceType(){
		return sourceType;
	}

	/**
	 * @return The message that was (optionally) provided to this
	 *         <tt>ValidationException</tt>, that provides more appropriate user
	 *         feedback, or <tt>null</tt> if no message was provided. If
	 *         <tt>null</tt> is returned, a message can be generated through the
	 *         {@link ValidationExceptionSupport} class.
	 */
	public String getMessage(){
		return message;
	}
	
	public String toString(){
		if(message != null) return message;
		
		String valueString = value == null ? "" : value.toString(); //$NON-NLS-1$
		if(valueString != null && valueString.length() > 50) 
			valueString = valueString.substring(0, 50)+"..."; //$NON-NLS-1$
		
		String sourceString = source == null ? ("("+sourceType.name()+")") :  //$NON-NLS-1$ //$NON-NLS-2$
			(source.toString()+"("+source.getDataType().name()+")"); //$NON-NLS-1$ //$NON-NLS-2$
		if(sourceString!=null && sourceString.length() > 50) 
			sourceString = sourceString.substring(0, 50)+"..."; //$NON-NLS-1$
		
		return
			"ValidationException for "+ (source == null ? " data type: '" : "data object: '") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sourceString + "' for key: '"+key+"' and value: '"+valueString+"'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}