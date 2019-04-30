package com.bcs.core.db.persistence;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.util.ClassUtils;

/**
 * http://stackoverflow.com/questions/2101455/hibernate-set-default-query-timeout
 */
public class QueryTimeoutConfiguredDataSource extends DelegatingDataSource {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(QueryTimeoutConfiguredDataSource.class);

	private int queryTimeout;
	
	public QueryTimeoutConfiguredDataSource(DataSource dataSource) {
	    super(dataSource);
	}
	
	// override this method to proxy created connection
	@Override
	public Connection getConnection() throws SQLException {
	    return proxyWithQueryTimeout(super.getConnection());
	}
	
	// override this method to proxy created connection
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
	    return proxyWithQueryTimeout(super.getConnection(username, password));
	}
	
	private Connection proxyWithQueryTimeout(final Connection connection) {
	    return proxy(connection, new InvocationHandler() {
	        //All the Statement instances are created here, we can do something
	        //If the return is instance of Statement object, we set query timeout to it
	        @Override
	        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	            Object object = method.invoke(connection, args);
	            if (object instanceof Statement) {
	                ((Statement) object).setQueryTimeout(queryTimeout);
	                logger.debug("QueryTimeoutConfiguredDataSource:queryTimeout:" + queryTimeout);
	            }
	            return object;
	        }
	    });
	}
	
	private Connection proxy(Connection connection, InvocationHandler invocationHandler) {
	    return (Connection) Proxy.newProxyInstance(
	            connection.getClass().getClassLoader(), 
	            ClassUtils.getAllInterfaces(connection), 
	            invocationHandler);
	}
	
	public void setQueryTimeout(int queryTimeout) {
	    this.queryTimeout = queryTimeout;
	}
}
