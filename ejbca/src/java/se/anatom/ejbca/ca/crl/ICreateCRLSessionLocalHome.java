/*
 * Generated by XDoclet - Do not edit!
 */
package se.anatom.ejbca.ca.crl;

/**
 * Local home interface for CreateCRLSession.
 */
public interface ICreateCRLSessionLocalHome
   extends javax.ejb.EJBLocalHome
{
   public static final String COMP_NAME="java:comp/env/ejb/CreateCRLSessionLocal";
   public static final String JNDI_NAME="CreateCRLSessionLocal";

   public se.anatom.ejbca.ca.crl.ICreateCRLSessionLocal create()
      throws javax.ejb.CreateException;

}
