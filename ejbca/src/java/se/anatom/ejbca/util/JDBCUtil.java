/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package se.anatom.ejbca.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;


/**
 * Utility methods for jdbc calls.
 * <p/>
 * close() methods do not close quietly but log errors (exceptions) as warning. Closing
 * quietly could mask potential errors that could have no impact depending on GC
 * stress conditions, or simply exhaust database resources, so it is better to have some
 * warning than nothing at all.
 *
 * @version $Id: JDBCUtil.java,v 1.6 2005-09-19 13:09:47 anatom Exp $
 */
public class JDBCUtil {

    private static final Logger LOG = Logger.getLogger(JDBCUtil.class);

    /**
     * Return the requested datasource name. It assumes that the dsName returns a reference (java.lang.String)
     * to the real datasource instance, otherwise you're likely to get a ClassCastException.
     *
     * @param dsName the name of the requested datasource
     * @return the requested datasource
     * @throws ServiceLocatorException if there is an error locating the datasource
     */
    public static DataSource getDataSource(String dsName)
            throws ServiceLocatorException {
        String dataSourceName = ServiceLocator.getInstance().getString(dsName);
        return ServiceLocator.getInstance().getDataSource(dataSourceName);
    }

    /**
     * return a requested database connection
     *
     * @param dsName the name of the datasource
     * @return a database connection
     * @throws ServiceLocatorException if it cannot get the datasource connection
     * @see #getDataSource(java.lang.String)
     */
    public static Connection getDBConnection(String dsName) throws ServiceLocatorException {
        try {
            return getDataSource(dsName).getConnection();
        } catch (SQLException e) {
            throw new ServiceLocatorException("Error while getting db connection", e);
        }
    }

    /**
     * Close a db connection logging closing errors as warning.
     *
     * @param con the connection to close (can be null)
     */
    public static void close(Connection con) {
        if (con != null)
            try {
                con.close();
            } catch (SQLException e) {
                LOG.warn("Could not close connection", e);
            }
    }

    /**
     * Close a resultset logging closing errors as warning.
     *
     * @param rs the resultset to close (can be null)
     */
    public static void close(ResultSet rs) {
        if (rs != null)
            try {
                rs.close();
            } catch (SQLException e) {
                LOG.warn("Could not close ResultSet", e);
            }
    }

    /**
     * Close a prepared statement logging closing errors as warning.
     *
     * @param ps the prepared statement to close (can be null)
     */
    public static void close(PreparedStatement ps) {
        if (ps != null)
            try {
                ps.close();
            } catch (SQLException e) {
                LOG.warn("Could not close PreparedStatement", e);
            }
    }

    /**
     * Close a connection, prepared statement and result set, logging potential
     * closing errors as warning.
     *
     * @param con the connection to close (can be null)
     * @param ps  the prepared statement to close (can be null)
     * @param rs  the result set to close (can be null)
     */
    public static void close(Connection con, PreparedStatement ps, ResultSet rs) {
        close(rs);
        close(ps);
        close(con);
    }
    /**
     * Check if a certain column exists in the database.
     * @param con the connection to use, not closed by this method
     * @param table the table where the column perhaps exists
     * @param col the name of the column to check for
     * @return true if the column exists, falst if not (the database throws an SQLException)
     */
    public static boolean columnExists(Connection con, String table, String col) {
        PreparedStatement st = null;
        try {
            st = con.prepareStatement("select "+col+" from "+table+" where 1=0");
            st.executeQuery();
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            JDBCUtil.close(st);
        }
    }
}
