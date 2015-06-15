package org.jbnd.eof;

import java.util.HashMap;
import java.util.Map;

import org.jbnd.DataType;
import org.jbnd.DataTypeProvider;

import com.webobjects.eocontrol.EOClassDescription;


/**
 * A <tt>DataTypeProvider</tt> that uses EOF <tt>EOClassDescription</tt> objects
 * to construct <tt>EOFDataType</tt> objects and provide them.
 * <p>
 * Use the <tt>INSTANCE</tt> constant to access <tt>EOFDataProvider</tt>
 * functionality.
 * 
 * @version 1.0 Dec 20, 2007
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see EOFDataType
 * @see #INSTANCE
 */
public class EOFDataTypeProvider implements DataTypeProvider{

	/**
	 * The only existing instance of this class, should be used for all required
	 * operations.
	 */
	public static final EOFDataTypeProvider INSTANCE = new EOFDataTypeProvider();
	
	public static DataType get(Class<?> clazz){
		return INSTANCE.getDataType(clazz);
	}
	
	public static DataType get(String entityName){
		return INSTANCE.getDataType(entityName);
	}
	
	public static DataType get(EOClassDescription cd){
		return INSTANCE.getDataType(cd);
	}
	
	/**
	 * Mapping <tt>EOClassDescription</tt>s to <tt>EOFDataType</tt>s.
	 */
	static final Map<EOClassDescription, EOFDataType> eofDataTypes
		= new HashMap<EOClassDescription, EOFDataType>();
	
	//	disallow instantiation
	private EOFDataTypeProvider(){}

	
	/**
	 * Returns the <tt>DataType</tt> object for the given Java
	 * <tt>Class</tt> instance. The object returned is
	 * the one that an instance of the given <tt>Class</tt>
	 * would return from it's <tt>getDataType()</tt> method.
	 * Naturally, the given <tt>Class</tt> has to be an
	 * implementation of <tt>DataObject</tt> interface.
	 * 
	 * @param clazz	See above.
	 * @return	See above.
	 */
	public DataType getDataType(Class<?> clazz){
		EOClassDescription cd = EOClassDescription.classDescriptionForClass(clazz);
		return getDataType(cd);
	}

	/**
	 * Returns the <tt>DataType</tt> object for the given
	 * <tt>DataType</tt> name. The object returned has to be
	 * the one that will return <tt>String</tt> from
	 * it's <tt>name()</tt> that is equal to the parameter
	 * passed to this method.
	 * 
	 * @param name	See above.
	 * @return	See above.
	 */
	public DataType getDataType(String name){
		EOClassDescription cd = EOClassDescription.classDescriptionForEntityName(name);
		return getDataType(cd);
	}

	/**
	 * A method not declared in the <tt>DataTypeProvider</tt> interface,
	 * used to provide <tt>DataType</tt>s that are reflecting the
	 * given <tt>EOClassDescription</tt>s. The value returned is
	 * an instance of <tt>EOFDataType</tt>.
	 * 
	 * @param description	See above.
	 * @return	See above.
	 * @see	EOFDataType
	 */
	public DataType getDataType(EOClassDescription description){
		EOFDataType dt = eofDataTypes.get(description);
		if(dt == null){
			dt = new EOFDataType(description);
			eofDataTypes.put(description, dt);
		}
		
		return dt;
	}
}