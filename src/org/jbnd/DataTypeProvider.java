package org.jbnd;

/**
 * An interface for objects that retrieve <tt>DataType</tt> implementations from
 * external sources (meaning: <tt>DataType</tt>s can not be simply
 * instantiated). Not really required by the JBND library, an never used
 * internally, but meant more as a convention.
 * 
 * @version 1.0 Dec 17, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see DataType
 */
public interface DataTypeProvider{
	
	/**
	 * Returns the <tt>DataType</tt> object for the given Java <tt>Class</tt>
	 * instance. The object returned has to be the one that an instance of the
	 * given <tt>Class</tt> would return from it's <tt>getDataType()</tt>
	 * method. Naturally, the given <tt>Class</tt> has to be an implementation
	 * of <tt>DataObject</tt> interface.
	 * 
	 * @param clazz See above.
	 * @return See above.
	 */
	public DataType getDataType(Class<?> clazz);
	
	/**
	 * Returns the <tt>DataType</tt> object for the given <tt>DataType</tt>
	 * name. The object returned has to be the one that will return
	 * <tt>String</tt> from it's <tt>name()</tt> that is equal to the parameter
	 * passed to this method.
	 * 
	 * @param name See above.
	 * @return See above.
	 */
	public DataType getDataType(String name);
}
