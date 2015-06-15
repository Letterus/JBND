// All custom logic should be stored in this class
// so it is not affected by changes in the model,
// Which will change the abstract superclass instead

#if ($entity.packageName)
package $entity.packageName;

import com.webobjects.eocontrol.EOClassDescription;
#end

public class ${entity.classNameWithoutPackage} extends ${entity.prefixClassNameWithOptionalPackage} {

	/**
	 * Default constructor.
	 */
	public ${entity.prefixClassNameWithoutPackage}(){}
	
	/**
	 * Constructor used in inheritance situations, when this entity is the parent.
	 * 
	 * @param cd The class description of the sub-entity.
	 */
	protected ${entity.prefixClassNameWithoutPackage}(EOClassDescription cd) {
		super(cd);
	}
}