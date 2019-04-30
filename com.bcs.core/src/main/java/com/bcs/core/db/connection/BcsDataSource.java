package com.bcs.core.db.connection;

import net.sourceforge.jtds.jdbcx.JtdsDataSource;

import org.apache.log4j.Logger;

public class BcsDataSource extends JtdsDataSource {

	/** Logger */
	private static Logger logger = Logger.getLogger(BcsDataSource.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BcsDataSource() {
		super();
		logger.info("BcsDataSource public");
	}
}
