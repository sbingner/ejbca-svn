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
 
package se.anatom.ejbca.util.query;

/**
 * An exception thrown if Query strucure is illegal.
 *
 * @author Philip Vendil
 * @version $Id: IllegalQueryException.java,v 1.3 2004-04-16 07:39:01 anatom Exp $
 */
public class IllegalQueryException extends java.lang.Exception {
    /**
     * Creates a new instance of <code>IllegalQueryException</code> without detail message.
     */
    public IllegalQueryException() {
        super();
    }

    /**
     * Constructs an instance of <code>IllegalQueryException</code> with the specified detail
     * message.
     *
     * @param msg the detail message.
     */
    public IllegalQueryException(String msg) {
        super(msg);
    }
}
