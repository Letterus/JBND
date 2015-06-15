package org.jbnd;

/**
 * Thrown if a <tt>DataObject</tt> or a <tt>DataType</tt> is required to perform
 * an operation on a key they do not have.
 * 
 * @version 1.0 Dec 17, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public final class UnknownKeyException extends RuntimeException{

	// the key that is unknown
	private final String key;

	// the data type
	private final DataType dataType;

	/**
	 * Constructs an <tt>UnknownKeyException</tt> with the given
	 * parameters.
	 * 
	 * @param key	The unknown key.
	 * @param object	The <tt>DataObject</tt> on which the
	 * 					key was used.
	 */
	public UnknownKeyException(String key, DataObject object){
		this(key, object.getDataType());
	}

	/**
	 * Constructs an <tt>UnknownKeyException</tt> with the given
	 * parameters.
	 * 
	 * @param key	The unknown key.
	 * @param dataType	The <tt>DataType</tt> on which the
	 * 					key was used.
	 */
	public UnknownKeyException(String key, DataType dataType){
		this.key = key;
		this.dataType = dataType;
	}

	/**
	 * Returns the <tt>DataType</tt> for which the key is unknown.
	 * 
	 * @return The <tt>DataType</tt> for which the key is unknown.
	 */
	public DataType getDataType(){
		return dataType;
	}

	/**
	 * The key that was used, but which is not know to the <tt>DataType</tt> or
	 * <tt>DataObject</tt> on which it was used.
	 * 
	 * @return The unknown key.
	 */
	public String getKey(){
		return key;
	}
	
	public String toString(){
		return "The DataType: '"+dataType.name()+"' does not know the key: '"+key+"'";
	}

	public String getMessage(){
		return toString();
	}	
}