package org.jbnd.data;

import org.jbnd.event.DataSourceListener;

/**
 * An tagging interface defining a <tt>DataSource</tt> that proxies
 * <tt>DataObject</tt>s from another <tt>DataSource</tt>. The
 * proxied data set can be narrowed down (filtered), presented
 * in a different order, augmented, etc. So, comparing data
 * between a proxying data source and it's target may not
 * yield any sensible results. This interface is provided to
 * enable interested code to reach the underlying <tt>DataSource</tt>
 * of the different proxying data sources that JBND provides.
 * For example:
 * 
 * <pre>//	we have a data source
//	that is most likely a proxy to
//	another data source
DataSource ds;

//	we need the underlying ListDataSource
while(ds instanceof ProxyingDataSource)
	ds = ((ProxyingDataSource)ds).getTargetDataSource();

//	now the ds is most likely an instance of
//	ListDataSource, but still check
if(ds instanceof ListDataSource()){
	//	do whatever needs to be done
}</pre>
 *
	@version 1.0 Feb 17, 2008
	@author Florijan Stamenkovic (flor385@mac.com)
 */
public interface ProxyingDataSource extends DataSource, DataSourceListener{
	
	/**
	 * Returns the <tt>DataSource</tt> that is backing
	 * this <tt>ProxyingDataSource</tt>.
	 * 
	 * @return	The <tt>DataSource</tt> that is backing
	 * 			this <tt>ProxyingDataSource</tt>.
	 */
	public DataSource getTargetDataSource();
}