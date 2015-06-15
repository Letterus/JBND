package org.jbnd.binding;

import org.jbnd.DataObject;
import org.jbnd.ValidationException;
import org.jbnd.data.AbstractDataSource;
import org.jbnd.event.DataSourceEvent;
import org.jbnd.event.FilterChangeListener;
import org.jbnd.event.FilterChangedEvent;
import org.jbnd.qual.Qualifier;


/**
 * A very specific <tt>Binding</tt> implementation, used to bind user interface
 * elements with the values of a {@link Qualifier}. This class provides that
 * possibility in conjunction with the <tt>QualifierGroup</tt> class (which
 * serves as a <tt>DataSource</tt> for the binding).
 * <p>
 * <tt>QualifierBinding</tt>s are obtained from <tt>QualifierGroup</tt>s, and can
 * not be instantiated directly. See the
 * {@link QualifierGroup#getBinding(org.jbnd.qual.KeyValueQualifier, Class, boolean)}
 * and
 * {@link QualifierGroup#getBinding(java.util.List, java.util.List, boolean[])}
 * methods.
 * <p>
 * Example usage of qualifier bindings:
 * 
 * <pre>{@code
 * //	define a qualifier or three
 * KeyValueQualifier<Date> dateFromQual = new KeyValueQualifier<Date>("date",
 * 		KeyValueQualifier.GREATER_THAN), dateToQual = new KeyValueQualifier<Date>(
 * 		"date", KeyValueQualifier.LESS_THAN);
 * KeyValueQualifier<Integer> sizeQual = new KeyValueQualifier<Integer>("size",
 * 		KeyValueQualifier.EQUAL);
 * 
 * //	first define the qualifier group
 * //	and obtain QualifierBindings from it
 * QualifierGroup qualifierGroup = new QualifierGroup();
 * QualifierBinding dateFromBinding = qualifierGroup.getBinding(dateFromQual,
 * 		Date.class, false), dateToBinding = qualifierGroup.getBinding(
 * 		dateToQual, Date.class, false), sizeBinding = qualifierGroup
 * 		.getBinding(sizeQual, Integer.class, false);
 * 
 * //	create a compound qualifier from the defined qualifiers
 * List<KeyValueQualifier<?>> qualifiers = new LinkedList<KeyValueQualifier<?>>();
 * Qualifier compoundQualifier = new AndQualifier(qualifiers);
 * 
 * //	the dynamic compound qualifier is now ready
 * //	to be used....
 * compoundQualifier.filteredArray(anArrayOfDataObjects);
 * FilteringDataSource dataSource = new FilteringDataSource(aDataSource,
 * 		compoundQualifier);
 * }</pre>
 * 
 * @version 1.0 Feb 8, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 * @see QualifierGroup
 */
public final class QualifierBinding extends AbstractDataSource implements Binding, 
	FilterChangeListener{
	
	/** The <tt>DataSource</tt> backing this binding */
	private final QualifierGroup qualifierGroup;
	
	/** The keys of this binding */
	private final String[] keys;

	/** The validation handler used by this binding */
	private ValidationHandler validationHandler = new ValidationHandler(){

		public boolean handleException(ValidationException exception)
				throws ValidationException{
			throw exception;
		}};
	
	/**
	 * Creates a <tt>QualifierBinding</tt> with the given parameters.
	 * 
	 * @param qualifierGroup The <tt>QualifierGroup</tt> of the binding.
	 * @param key The key this <tt>QualifierBinding</tt> uses.
	 */
	QualifierBinding(QualifierGroup qualifierGroup, String key){
		this(qualifierGroup, new String[]{key});
	}
	
	/**
	 * Creates a <tt>QualifierBinding</tt> with the given parameters.
	 * 
	 * @param qualifierGroup	The <tt>QualifierGroup</tt> of the binding.
	 * @param keys	The keys this <tt>QualifierBinding</tt> uses.
	 */
	QualifierBinding(QualifierGroup qualifierGroup, String[] keys){
		
		//	set the data source
		this.qualifierGroup = qualifierGroup;
		qualifierGroup.addDataSourceListener(this);
		
		//	set the keys
		if(keys == null || keys.length == 0)
			throw new IllegalArgumentException("Bindings must have at least one key");
		this.keys = keys;
	}

	public String[] getKeys(){
		return keys;
	}
	
	/**
	 * Gets the <tt>ValidationHandler</tt> used by the binding to handle
	 * exceptions caused by attempted setting of invalid values.
	 * 
	 * @return The validation handler used by this binding.
	 */
	public ValidationHandler getValidationHandler(){
		return this.validationHandler;
	}
	
	/**
	 * Sets the <tt>ValidationHandler</tt> to be used by the binding to handle
	 * exceptions caused by attempted setting of invalid values.
	 * 
	 * @param handler The validation handler to be used by this binding.
	 * @throws NullPointerException If the given <tt>handler</tt> is
	 *             <tt>null</tt>.
	 */
	public void setValidationHandler(ValidationHandler handler){
		if(handler == null)
			throw new NullPointerException("A null ValidationHandler is not allowed");
		this.validationHandler = handler;
	}

	public int size(){
		return qualifierGroup.size();
	}
	
	/**
	 * Attempts to set the <tt>value</tt> on the <tt>DataObject</tt>
	 * at the <tt>index</tt> for the <tt>key</tt>. Validation is
	 * performed for the <tt>value</tt> in this method, and if it
	 * fails (the value is not valid), the behavior of the method is
	 * determined by the current <tt>ValidationHandler</tt> of the
	 * binding. The default <tt>ValidationHandler</tt> swallows the
	 * validation exception and returns <tt>false</tt>.
	 * 
	 * @param	index	The index of the <tt>DataObject</tt> to edit.
	 * @param	key		The key for which to edit it.
	 * @param	value	The value to attempt to set.
	 * @throws	ValidationException	If the given <tt>value</tt> fails
	 * 				on validation, and the current <tt>ValidationHandler</tt>
	 * 				of the binding chooses to rethrow the exception.
	 */
	public void edit(int index, String key, Object value){
		
		boolean setValue = true;
		DataObject object = get(index);
		
		//	validate the value
		try{
			value = object.validate(value, key);
		}catch(ValidationException ex){
			setValue = validationHandler.handleException(ex);
		}
		
		//	set the value
		if(setValue) object.set(value, key);
	}

	public DataObject get(int index){
		return qualifierGroup.get(index);
	}
	
	/**
	 * Always returns <tt>true</tt>.
	 * 
	 * @param index Ignored.
	 * @param key Ignored.
	 * @return	<code>true</code>
	 */
	public boolean isEditable(int index, String key){
		return true;
	}

	
	/**
	 * Does nothing, because it is never called, because the
	 * <tt>QualifierGroup</tt> never has data added to it.
	 * 
	 * @param	event	Ignored.
	 */
	public void dataAdded(DataSourceEvent event){}
	
	/**
	 * Called when the data changes in the backing
	 * <tt>QualifierGroup</tt>.
	 * 
	 * @param	event	The event fired by the <tt>QualifierGroup</tt>.
	 */
	public void dataChanged(DataSourceEvent event){
		fireDataChanged(event.getIndex0(), event.getIndex1(), event.getData());
	}

	/**
	 * Does nothing, because it is never called, because the
	 * <tt>QualifierGroup</tt> never has data removed from it.
	 * 
	 * @param	event	Ignored.
	 */
	public void dataRemoved(DataSourceEvent event){}

	/**
	 * <tt>QualifierBinding</tt>s listen to changes in their <tt>Qualifier</tt>
	 * s. This is because <tt>QualiferGroup</tt> does not fire
	 * <tt>DataObjectEvent</tt>s. This is a hack to enable multiple
	 * <tt>QualifierBinding</tt>s to connect to a single GUI element. If
	 * <tt>DataObjectEvent</tt>s were fired, it would result in an exception. So
	 * that system is circumvented, and <tt>Qualifier</tt> changes are observed
	 * instead.
	 * 
	 * @param e
	 */
	public void filterChanged(FilterChangedEvent e){
		fireDataChanged(0, 0);		
	}
}