package org.jbnd.eof;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jbnd.qual.*;
import org.jbnd.qual.KeyValueQualifier.Op;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSSelector;

/**
 * Translates JBND's <tt>org.jbnd.qual.Qualifier</tt>s into
 * <tt>com.webobjects.eocontrol.EOQualifier</tt>s. Note that this translator
 * does not detect if the keys used in the JBND qualifiers are present in the
 * entities they will be used on, so it is possible that it will produce
 * EOQualifiers that do not work.
 * 
 * @version 1.0 Feb 11, 2008
 * @author Florijan Stamenkovic (flor385@mac.com)
 */
public class EOFQualifierTranslator{

	/**
	 * Tests if the given JBND <tt>Qualifier</tt> can be translated
	 * into an <tt>EOQualifier</tt>.
	 * 
	 * @param qualifier	The JBND qualifier to translate.
	 * @return	If the given JBND <tt>Qualifier</tt> can be translated
	 * 			into an <tt>EOQualifier</tt>.
	 */
	public static boolean canTranslate(Qualifier qualifier){
		//	key value qualifiers are translatable
		if(qualifier instanceof KeyValueQualifier<?>) return true;
		
		//	compound qualifiers that are translatable
		if(qualifier instanceof NotQualifier ||
				qualifier instanceof AndQualifier ||
				qualifier instanceof OrQualifier){
			
			//	go through all the qualifiers of the
			//	compound qualifier and see if they can
			//	be translated
			Iterator<Qualifier> it = ((CompoundQualifier)qualifier).iterator();
			while(it.hasNext())
				//	if bumping into a qualifier that can't translate
				//	return false
				if(! canTranslate(it.next()))
					return false;
			
			//	all of the qualifiers of the given
			//	compound qualifier are translatable
			return true;
		}
		
		//	an unknown kind of qualifier...
		return false;
	}
	
	/**
	 * Translates the given JBND <tt>Qualifier</tt> into an
	 * <tt>EOQualifier</tt>. This method takes care of producing
	 * the most simple EOQualifier possible, for example if there
	 * is a JBND <tt>KeyValueQualifier</tt> that contains no value,
	 * and is configured so that a null in it implies that every
	 * object is valid, then that qualifier is eliminated from
	 * the end result. It is therefore possible for this method
	 * to return <tt>null</tt>, this will be the result when the
	 * given <tt>qualifier</tt> is one that does not disqualify
	 * any objects.
	 * 
	 * @param qualifier	The JBND qualifier to translate.
	 * @return	An <tt>EOQualifier</tt> that is equivalent to
	 * 			the given <tt>qualifier</tt> or <tt>null</tt> if
	 * 			the given <tt>qualifier</tt> can not disqualify
	 * 			any objects.
	 */
	public static EOQualifier translate(Qualifier qualifier){
		
		// ContainsString qualifier
		if(qualifier instanceof ContainsStringQualifier){
			ContainsStringQualifier qual = (ContainsStringQualifier)qualifier;
			Object value = qual.getValue();
			if(value == null) return translateQualifierWithNullValue(qual);
			
			if(qual.isWildcardStart()) value = "*"+value;
			if(qual.isWildcardEnd()) value = value+"*";
			
			return new EOKeyValueQualifier(
					qual.getKey(), selectorForFlag(qual.getOperator()), value);
		}
		
		//	KeyValueQualifier translation
		if(qualifier instanceof KeyValueQualifier){
			KeyValueQualifier<?> qual = (KeyValueQualifier<?>)qualifier;
			
			//	check if the value is null, and if so, take care
			Object value = qual.getValue();
			if(value == null)
				return translateQualifierWithNullValue(qual);
			
			//	value is not null
			//	so do the standard thing
			return new EOKeyValueQualifier(
				qual.getKey(), selectorForFlag(qual.getOperator()), value);
		}
		
		//	Compound qualifiers
		if(qualifier instanceof CompoundQualifier){
			//	translate individual qualifiers
			List<EOQualifier> qualifiers = breakDown((CompoundQualifier)qualifier);
			//	if there are no qualifiers in the translated list
			//	return null
			if(qualifiers.size() == 0) return null;
			
			//	AndQualifier
			if(qualifier instanceof AndQualifier){
				//	if there's only one, return that one
				if(qualifiers.size() == 1) return qualifiers.get(0);
				
				//	and if there are more then one, well, return
				// an EOAndQualifier\
				return new EOAndQualifier(new NSArray<EOQualifier>(qualifiers
						.toArray(new EOQualifier[qualifiers.size()])));
			}
			
			//	NotQualifier
			if(qualifier instanceof NotQualifier){
				return new EONotQualifier(qualifiers.get(0));
			}
			
			//	OrQualifier
			if(qualifier instanceof OrQualifier){
				//	if there's only one, return that one
				if(qualifiers.size() == 1) return qualifiers.get(0);
				
				//	and if there are more then one, well, return
				//	an EOAndQualifier\
				return new EOOrQualifier(new NSArray<EOQualifier>(qualifiers
						.toArray(new EOQualifier[qualifiers.size()])));
			}
			
			throw new IllegalStateException("Unknown CompoundQualifier implementation");
		}
		
		throw new IllegalStateException("Untranslatable qualifier being translated: "
			+qualifier);
	}
	
	/**
	 * Breaks down a JBND <tt>CompoundQualifier</tt> into a <tt>List</tt>
	 * of <tt>EOQualifier</tt>s it is contained of. The resulting
	 * <tt>List</tt> will not contain qualifiers that can not disqualify
	 * any objects, so it is possible that it's size will be 0.
	 * 
	 * @param qualifier	The <tt>CompoundQualifier</tt>.
	 * @return	See above.
	 */
	private static List<EOQualifier> breakDown(CompoundQualifier qualifier){
		
		// all the JBND qualifiers...
		Iterator<Qualifier> it = qualifier.iterator();
		
		List<EOQualifier> rVal = new LinkedList<EOQualifier>();
		
		//	translate qualifiers one by one
		while(it.hasNext()){
			EOQualifier translatedElement = translate(it.next());
			//	only add a qualifier if it does not translate to null
			if(translatedElement != null) rVal.add(translatedElement);
		}
		
		return rVal;
	}
		
	/**
	 * If a JBND <tt>KeyValueQualifier</tt> has <tt>null</tt> as
	 * it's qualification value, it can be configured to behave in
	 * different ways (assume that a null value implies that all
	 * objects qualify, or that only objects that have null for
	 * the qualification key qualify). This method takes this into
	 * consideration when translating a <tt>KeyValueQualifier</tt>
	 * into an <tt>EOQualifier</tt>, and returns the right thing.
	 * If the given <tt>qualifier</tt> is one that can not
	 * disqualify any object, then <tt>null</tt> is returned.
	 * 
	 * @param qualifier	The JBND qualifier to translate.
	 * @return	See above.
	 */
	private static EOQualifier translateQualifierWithNullValue(
			KeyValueQualifier<?> qualifier)
	{
		//	depending on the null behavior of the
		//	given qualifier, return null or generate
		//	an EOQualifier
		switch(qualifier.getNullBehavior()){

			case ANY : return null;
			case NULL :
				//	when NULL_MEANS_NULL
				//	we need to make an EOQualifier that compares
				//	on null
				return EOQualifier.qualifierWithQualifierFormat(
						//	generate the qualifier string
						qualifier.getKey()+" "+
						EOQualifier.stringForOperatorSelector(
								selectorForFlag(qualifier.getOperator()))+" nil",
								null);
			default:
				throw new IllegalStateException(
						"Unknown KeyValueQualifier null behavior flag");
		}
	}

	/**
	 * Returns one of the <tt>NSSelector</tt>s defined in the
	 * <tt>EOQualifier</tt> class that is the equivalent of a
	 * JBND operator flag.
	 * 
	 * @param flag	The JBND operator flag to translate.
	 * @return	One of the <tt>NSSelector</tt>s defined in the
	 * 			<tt>EOQualifier</tt> class.
	 */
	public static NSSelector<?> selectorForFlag(Op flag){
		switch(flag){
			case EQUAL : 
				return EOQualifier.QualifierOperatorEqual;
			case GREATER_THAN : 
				return EOQualifier.QualifierOperatorGreaterThan;
			case GREATER_THAN_OR_EQUAL : 
				return EOQualifier.QualifierOperatorGreaterThanOrEqualTo;
			case LESS_THAN : 
				return EOQualifier.QualifierOperatorLessThan;
			case LESS_THAN_OR_EQUAL : 
				return EOQualifier.QualifierOperatorGreaterThanOrEqualTo;
			case NOT_EQUAL : 
				return EOQualifier.QualifierOperatorNotEqual;
			case INSENSITIVE_LIKE :
				return EOQualifier.QualifierOperatorCaseInsensitiveLike;
			case LIKE :
				return EOQualifier.QualifierOperatorLike;
			default :
				throw new IllegalStateException(
						"Unknown KeyValueQualifier operator");
		}
	}
}