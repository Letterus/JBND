package org.jbnd.support;

import java.util.Arrays;
import java.util.Map;

import org.jbnd.DataObject;
import org.jbnd.DataType;
import org.jbnd.ValidationException;
import org.jbnd.localization.Messages;


/**
 * Provides methods to process <tt>ValidationException</tt>s into user
 * presentable descriptions of what went wrong and where. Typically used by
 * other classes that control presenting the validation error messages to the
 * user. The main purpose is isolating the logic of processing an exception
 * object to only one place, for possible future localization purposes.
 * <p>
 * <tt>NamingSupport</tt> class is used for translating <tt>key</tt> and
 * <tt>DataType</tt> names into user presentable text.
 * 
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @version 1.0
 * @see NamingSupport
 */
public final class ValidationExceptionSupport {

	/**
	 * Disallow instantiation, as the class is intended for static calls only.
	 */
	private ValidationExceptionSupport(){}
	
	/**
	 * Flag that indicates WHEN validation threw an exception; this flag
	 * indicates it happened at record creation time.
	 */
	public static final int CREATION_TIME = 0;
	
	/**
	 * Flag that indicates WHEN validation threw an exception; this flag
	 * indicates it happened at record edit time.
	 */
	public static final int EDIT_TIME = 1;
	
	/**
	 * Flag that indicates WHEN validation threw an exception; this flag
	 * indicates it happened at record deletion time. (record was not valid
	 * for deletion).
	 */
	public static final int DELETION_TIME = 2;
	
	/**
	 * Flag that indicates WHEN validation threw an exception; this flag
	 * indicates undefined time.
	 */
	public static final int UNDEFINED_TIME = 3;
	
	/**
	 * Used for all internal <tt>String</tt> construction, to avoid constant
	 * instantiation.
	 */
	private static final StringBuffer buff = new StringBuffer(1024);
	
	/**
	 * Constructs a user presentable title indicating which and what kind of
	 * object failed, on what field, and at what time. The title may be too long
	 * to be displayed as a window title, it is meant to be displayed as a 1 to
	 * 3 row title.
	 * 
	 * @param ex The thrown exception.
	 * @param time A flag indicating WHEN the validation exception was thrown,
	 *            one of the flags defined in this class (<tt>CREATION_TIME, EDIT_TIME</tt>,
	 *            etc...).
	 * @return A <tt>String</tt> that can be used as a title of user feedback
	 *         on validation failure.
	 */
	public static String getTitle(ValidationException ex, int time){
		
		DataObject object = ex.getSource();
		DataType type = ex.getSourceType();
		String key = ex.getKey() == null ? "<" //$NON-NLS-1$
				+ Messages.getString("ValidationExceptionSupport.0") + ">" : ex.getKey(); //$NON-NLS-1$ //$NON-NLS-2$
		
		//	append the validation failure time
		switch(time){
			
			//	construction in case of a creation time validation exception
			case CREATION_TIME : {
				
				buff.append(Messages.fillUp(
					Messages.getString("ValidationExceptionSupport.1"), //$NON-NLS-1$
					JBNDUtil.quote(NamingSupport.typeName(type.name())),
					JBNDUtil.quote(NamingSupport.propertyName(key))));
				
				break;
			}
			
			//	construction in case of an edit time validation exception
			case EDIT_TIME : {
				
				buff.append(Messages.fillUp(
						Messages.getString("ValidationExceptionSupport.2"), //$NON-NLS-1$
						JBNDUtil.quote(object.toLogString()),
						NamingSupport.typeName(type.name()),
						JBNDUtil.quote(NamingSupport.propertyName(key))));
					
				break;
			}
			
			case DELETION_TIME : {
				
				buff.append(Messages.fillUp(
						Messages.getString("ValidationExceptionSupport.3"), //$NON-NLS-1$
						JBNDUtil.quote(object.toLogString()),
						NamingSupport.typeName(type.name()),
						JBNDUtil.quote(NamingSupport.propertyName(key))));
					
				break;
			}
			
			//	construction in case of an UNDEFINED time validation exception
			case UNDEFINED_TIME : {
				
				if(object != null)
					buff.append(Messages.fillUp(
							Messages.getString("ValidationExceptionSupport.4"), //$NON-NLS-1$
							JBNDUtil.quote(object.toLogString()),
							NamingSupport.typeName(type.name()),
						JBNDUtil.quote(NamingSupport.propertyName(key))));
				else
					buff.append(Messages.fillUp(
							Messages.getString("ValidationExceptionSupport.5"), //$NON-NLS-1$
							JBNDUtil.quote(NamingSupport.typeName(type.name())),
							JBNDUtil.quote(NamingSupport.propertyName(key))));
				
				break;
			}
			
			default : {
				throw new IllegalStateException("Time argument not a standard flag"); //$NON-NLS-1$
			}
		}

		String rVal = buff.toString();
		buff.delete(0, buff.length());
		return rVal;
	}
	
	/**
	 * Constructs a user presentable title for a situation where multiple
	 * objects have possibly failed validation on multiple keys. The title may
	 * be too long to be displayed as a window title, it is meant to be
	 * displayed as a 1 to 3 row title.
	 * 
	 * @param validationResult A <tt>Map</tt> containing as keys the indices
	 *            of <tt>DataObject</tt>s that failed validation, and as
	 *            values an array of keys for which the validation failed for
	 *            that particular object.
	 * @param time A flag indicating WHEN the validation exception was thrown,
	 *            one of the flags defined in this class (<tt>CREATION_TIME, EDIT_TIME</tt>,
	 *            etc...).
	 * @return A <tt>String</tt> that can be used as a title of user feedback
	 *         on validation failure.
	 */
	public static String getTitle(
			Map<Integer, ValidationException[]> validationResult, int time){
		
		//	if this will be a short message, the title should be short
		String formattedResult = _format(validationResult);
		if(formattedResult.length() < 40)
			return Messages.getString("ValidationExceptionSupport.7"); //$NON-NLS-1$
		
		return introText(validationResult, time);
	}
	
	private static String introText(
			Map<Integer, ValidationException[]> validationResult, int time){
		// translate info into a pretext
		
		return time == ValidationExceptionSupport.CREATION_TIME ? 
				Messages.getString("ValidationExceptionSupport.8") //$NON-NLS-1$
				: Messages.getString("ValidationExceptionSupport.9"); //$NON-NLS-1$
	}
	
	/**
	 * Constructs a user presentable message indicating why validation failed.
	 * 
	 * @param ex The thrown exception.
	 * @param time A flag indicating WHEN the validation exception was thrown,
	 *            one of the flags defined in this class (<tt>CREATION_TIME, EDIT_TIME</tt>,
	 *            etc...).
	 * @return A <tt>String</tt> that contains the message to be used as user
	 *         feedback on validation failure.
	 */
	public static String getMessage(ValidationException ex, int time){
		
		//	if there is a customized message provided in the validation
		//	exception, use that
		if(ex.getMessage() != null) return ex.getMessage();
		
		//	otherwise construct the message based on the info in the exception
		
		//	at deletion time do some fancy exception formatting
		if(time == DELETION_TIME){
			
			DataType dt = ex.getSource().getDataType();
			return Messages.fillUp(
					Messages.getString("ValidationExceptionSupport.10"), //$NON-NLS-1$
					JBNDUtil.quote(NamingSupport.typeName(dt.name())),
					JBNDUtil.quote(NamingSupport.typeName(
							dt.dataTypeForRelationship(ex.getKey()).name())));
			
			
		}
		
		//	extract the string representation of the value
		//	that failed validation
		String value = ex.getValue() == null ? null : ex.getValue().toString();
		if(value != null && value.length() > 50)
			value = value.substring(0, 50) + "...";  //$NON-NLS-1$
		
		//	if we have a null value that failed validation
		if(value == null)
			return Messages.getString("ValidationExceptionSupport.11"); //$NON-NLS-1$
		
		return Messages.fillUp(Messages.getString("ValidationExceptionSupport.12"), value); //$NON-NLS-1$
		
		//	TODO Depending on exception provided data, other descriptions of
		//	"causes" of why the exception was thrown could be defined
	}
	
	/**
	 * Constructs a user presentable message for a situation where multiple
	 * objects have possibly failed validation on multiple keys.
	 * 
	 * @param validationResult A <tt>Map</tt> containing as keys the indices
	 *            of <tt>DataObject</tt>s that failed validation, and as
	 *            values an array of keys for which the validation failed for
	 *            that particular object.
	 * @param time A flag indicating WHEN the validation exception was thrown,
	 *            one of the flags defined in this class (<tt>CREATION_TIME, EDIT_TIME</tt>,
	 *            etc...).
	 * @return A <tt>String</tt> that contains the message to be used as user
	 *         feedback on validation failure.
	 */
	public static String getMessage(
			Map<Integer, ValidationException[]> validationResult, int time){
		
		String formattedResult = _format(validationResult);
		if(formattedResult.length() >= 40)
			return formattedResult;
		
		return introText(validationResult, time)+"\n\n"+formattedResult;  //$NON-NLS-1$
	}
	
	private static String _format(Map<Integer, ValidationException[]> validationResult){
		Integer[] indices = validationResult.keySet().toArray(new Integer[0]);
		Arrays.sort(indices);
		
		//	translate info into a message
		StringBuilder messageBuilder = new StringBuilder();
		for(int i = 0 ; i < indices.length ; i++){
			Integer index = indices[i];
			
			if(i != 0) messageBuilder.append("\n"); //$NON-NLS-1$
			
			if(indices.length != 1){
				messageBuilder.append(Messages.getString("ValidationExceptionSupport.14") + ": " + (index + 1)); //$NON-NLS-1$ //$NON-NLS-2$
				messageBuilder.append("\n\t"); //$NON-NLS-1$
			}
			
			//	add all the keys of this index
			ValidationException[] exceptions = validationResult.get(index);
			for(int j = 0 ; j < exceptions.length ; j++){
				messageBuilder.append(NamingSupport
						.propertyName(exceptions[j].getKey()));
				if(j != exceptions.length - 1)
					messageBuilder.append(", "); //$NON-NLS-1$
			}
		}
		
		return messageBuilder.toString();
	}
}
