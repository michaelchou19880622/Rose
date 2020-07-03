package com.bcs.core.db.connection;

import java.sql.Types;

import org.hibernate.dialect.SQLServer2008Dialect;
import org.hibernate.type.StandardBasicTypes;

public class SQlServer2008DialectExtended extends SQLServer2008Dialect {
	public SQlServer2008DialectExtended() {
        super();
        registerHibernateType(Types.NCHAR, StandardBasicTypes.CHARACTER.getName()); 
        registerHibernateType(Types.NCHAR, 1, StandardBasicTypes.CHARACTER.getName());
        registerHibernateType(Types.NCHAR, 255, StandardBasicTypes.STRING.getName());
        registerHibernateType(Types.NVARCHAR, StandardBasicTypes.STRING.getName());
        registerHibernateType(Types.LONGNVARCHAR, StandardBasicTypes.TEXT.getName());
        registerHibernateType(Types.NCLOB, StandardBasicTypes.CLOB.getName());
    }
}
