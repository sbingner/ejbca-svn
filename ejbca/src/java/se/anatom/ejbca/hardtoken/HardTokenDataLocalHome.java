/*
 * Generated by XDoclet - Do not edit!
 */
package se.anatom.ejbca.hardtoken;

/**
 * Local home interface for HardTokenData.
 */
public interface HardTokenDataLocalHome
   extends javax.ejb.EJBLocalHome
{
   public static final String COMP_NAME="java:comp/env/ejb/HardTokenDataLocal";
   public static final String JNDI_NAME="HardTokenDataLocal";

   public se.anatom.ejbca.hardtoken.HardTokenDataLocal create(java.lang.String tokensn , java.lang.String username , java.util.Date createtime , java.util.Date modifytime , int tokentype , java.lang.String significantissuerdn , se.anatom.ejbca.hardtoken.hardtokentypes.HardToken tokendata)
      throws javax.ejb.CreateException;

   public java.util.Collection findByUsername(java.lang.String username)
      throws javax.ejb.FinderException;

   public se.anatom.ejbca.hardtoken.HardTokenDataLocal findByPrimaryKey(java.lang.String pk)
      throws javax.ejb.FinderException;

}
