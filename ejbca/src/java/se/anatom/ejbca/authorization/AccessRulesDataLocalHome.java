/*
 * Generated by XDoclet - Do not edit!
 */
package se.anatom.ejbca.authorization;

/**
 * Local home interface for AccessRulesData.
 */
public interface AccessRulesDataLocalHome
   extends javax.ejb.EJBLocalHome
{
   public static final String COMP_NAME="java:comp/env/ejb/AccessRulesDataLocal";
   public static final String JNDI_NAME="AccessRulesDataLocal";

   public se.anatom.ejbca.authorization.AccessRulesDataLocal create(java.lang.String admingroupname , int caid , se.anatom.ejbca.authorization.AccessRule accessrule)
      throws javax.ejb.CreateException;

   public se.anatom.ejbca.authorization.AccessRulesDataLocal findByPrimaryKey(se.anatom.ejbca.authorization.AccessRulesPK pk)
      throws javax.ejb.FinderException;

}
