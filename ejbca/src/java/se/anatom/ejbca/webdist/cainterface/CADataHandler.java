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
 
package se.anatom.ejbca.webdist.cainterface;

import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import se.anatom.ejbca.authorization.AuthorizationDeniedException;
import se.anatom.ejbca.authorization.IAuthorizationSessionLocal;
import se.anatom.ejbca.ca.caadmin.CAInfo;
import se.anatom.ejbca.ca.caadmin.ICAAdminSessionLocal;
import se.anatom.ejbca.ca.caadmin.extendedcaservices.ExtendedCAServiceInfo;
import se.anatom.ejbca.ca.caadmin.extendedcaservices.OCSPCAServiceInfo;
import se.anatom.ejbca.ca.crl.RevokedCertInfo;
import se.anatom.ejbca.ca.exception.CADoesntExistsException;
import se.anatom.ejbca.ca.exception.CAExistsException;
import se.anatom.ejbca.ca.exception.CATokenAuthenticationFailedException;
import se.anatom.ejbca.ca.exception.CATokenOfflineException;
import se.anatom.ejbca.ca.sign.ISignSessionLocal;
import se.anatom.ejbca.ca.store.CertificateDataBean;
import se.anatom.ejbca.ca.store.ICertificateStoreSessionLocal;
import se.anatom.ejbca.ca.store.certificateprofiles.CertificateProfile;
import se.anatom.ejbca.common.ExtendedPKCS10CertificationRequest;
import se.anatom.ejbca.exception.EjbcaException;
import se.anatom.ejbca.log.Admin;
import se.anatom.ejbca.protocol.IRequestMessage;
import se.anatom.ejbca.protocol.IResponseMessage;
import se.anatom.ejbca.protocol.PKCS10RequestMessage;
import se.anatom.ejbca.protocol.X509ResponseMessage;
import se.anatom.ejbca.ra.IUserAdminSessionLocal;
import se.anatom.ejbca.ra.raadmin.IRaAdminSessionLocal;
import se.anatom.ejbca.util.CertTools;
import se.anatom.ejbca.webdist.webconfiguration.EjbcaWebBean;
import se.anatom.ejbca.webdist.webconfiguration.InformationMemory;

/**
 * A class help administrating CAs. 
 *
 * @author  TomSelleck
 * @version $Id: CADataHandler.java,v 1.14 2005-11-24 21:21:25 herrvendil Exp $
 */
public class CADataHandler implements Serializable {

    
    /** Creates a new instance of CertificateProfileDataHandler */
    public CADataHandler(Admin administrator, 
                         ICAAdminSessionLocal caadminsession, 
                         IUserAdminSessionLocal adminsession, 
                         IRaAdminSessionLocal raadminsession, 
                         ICertificateStoreSessionLocal certificatesession,
                         IAuthorizationSessionLocal authorizationsession,
                         ISignSessionLocal signsession,
                         EjbcaWebBean ejbcawebbean) {
                            
       this.caadminsession = caadminsession;           
       this.authorizationsession = authorizationsession;
       this.adminsession = adminsession;
       this.certificatesession = certificatesession;
       this.raadminsession = raadminsession;
       this.administrator = administrator;          
       this.signsession = signsession;
       this.info = ejbcawebbean.getInformationMemory();       
       this.ejbcawebbean = ejbcawebbean;
    }
    
  /**
   *  @see se.anatom.ejbca.ca.caadmin.CAAdminSessionBean
   */    
  public void createCA(CAInfo cainfo) throws CAExistsException, CATokenOfflineException, CATokenAuthenticationFailedException, AuthorizationDeniedException{
    caadminsession.createCA(administrator, cainfo);
    info.cAsEdited();
  }
  
  /**
   *  @see se.anatom.ejbca.ca.caadmin.CAAdminSessionBean
   */
  public void editCA(CAInfo cainfo) throws AuthorizationDeniedException{
    caadminsession.editCA(administrator, cainfo);  
    info.cAsEdited();
  }

  /**
   *  @see se.anatom.ejbca.ca.caadmin.CAAdminSessionBean
   */  
  public boolean removeCA(int caid) throws AuthorizationDeniedException{
      
    boolean caidexits = this.adminsession.checkForCAId(administrator, caid) ||
                        this.certificatesession.existsCAInCertificateProfiles(administrator, caid) ||
                        this.raadminsession.existsCAInEndEntityProfiles(administrator, caid) ||
                        this.authorizationsession.existsCAInRules(administrator, caid);
     
    if(!caidexits){
      caadminsession.removeCA(administrator, caid);
      info.cAsEdited();
    }
    
    return !caidexits;
  }

  /**
   *  @see se.anatom.ejbca.ca.caadmin.CAAdminSessionBean
   */  
  public void renameCA(String oldname, String newname) throws CAExistsException, AuthorizationDeniedException{
    caadminsession.renameCA(administrator, oldname, newname);  
    info.cAsEdited();
  }

  /**
   *  @see se.anatom.ejbca.ca.caadmin.ICAAdminSessionLocal
   */  
  public CAInfoView getCAInfo(String name) throws Exception{
    CAInfoView cainfoview = null; 
    CAInfo cainfo = caadminsession.getCAInfo(administrator, name);
    if(cainfo != null)
      cainfoview = new CAInfoView(cainfo, ejbcawebbean, info.getPublisherIdToNameMap());
    
    return cainfoview;
  }
  
  /**
   *  @see se.anatom.ejbca.ca.caadmin.CAAdminSessionBean
   */  
  public CAInfoView getCAInfo(int caid) throws Exception{
    // temporate        
    CAInfoView cainfoview = null; 
    CAInfo cainfo = caadminsession.getCAInfo(administrator, caid);
    if(cainfo != null)
      cainfoview = new CAInfoView(cainfo, ejbcawebbean, info.getPublisherIdToNameMap());
    
    return cainfoview;  
  }

  /**
   *  @see se.anatom.ejbca.ca.caadmin.CAAdminSessionBean
   */  
  public HashMap getCAIdToNameMap(){
    return info.getCAIdToNameMap();
  }
  
  /**
   *  @see se.anatom.ejbca.ca.caadmin.CAAdminSessionBean
   */  
  public ExtendedPKCS10CertificationRequest  makeRequest(int caid, Collection cachain, boolean setstatustowaiting) throws CADoesntExistsException, AuthorizationDeniedException, CertPathValidatorException, CATokenOfflineException{
  	
	  PKCS10RequestMessage result = (PKCS10RequestMessage) caadminsession.makeRequest(administrator, caid,cachain,setstatustowaiting);
	  return result.getCertificationRequest();    
  }	    

  /**
   *  @see se.anatom.ejbca.ca.caadmin.CAAdminSessionBean
   */  
  public void receiveResponse(int caid, InputStream is) throws Exception{
  	 Collection certs = CertTools.getCertsFromPEM(is);
  	 Iterator iter = certs.iterator();
  	 Certificate cert = (Certificate) iter.next();
  	 X509ResponseMessage resmes = new X509ResponseMessage();
  	 resmes.setCertificate(cert);
  
     caadminsession.receiveResponse(administrator, caid, resmes);
     info.cAsEdited(); 
  }

  /**
   *  @see se.anatom.ejbca.ca.caadmin.CAAdminSessionBean
   */  
  public Certificate processRequest(CAInfo cainfo, IRequestMessage requestmessage) throws Exception {      
      Certificate returnval = null;
      IResponseMessage result = caadminsession.processRequest(administrator, cainfo, requestmessage);
      if(result instanceof X509ResponseMessage){
         returnval = ((X509ResponseMessage) result).getCertificate();      
      }            
      info.cAsEdited();
      
      return returnval;      
  }

  /**
   *  @see se.anatom.ejbca.ca.caadmin.CAAdminSessionBean
   */  
  public void renewCA(int caid, IResponseMessage responsemessage) throws CADoesntExistsException, AuthorizationDeniedException, CertPathValidatorException, CATokenOfflineException{
      caadminsession.renewCA(administrator, caid, responsemessage);
      info.cAsEdited();
  }

  /**
   *  @see se.anatom.ejbca.ca.caadmin.CAAdminSessionBean
   */  
  public void revokeCA(int caid, int reason) throws CADoesntExistsException, AuthorizationDeniedException {
      caadminsession.revokeCA(administrator, caid, reason);
      info.cAsEdited();
  }
      
  /**
   *  @see se.anatom.ejbca.ca.caadmin.CAAdminSessionBean
   */  
 public void publishCA(int caid){
 	CAInfo cainfo = caadminsession.getCAInfo(administrator, caid);
 	CertificateProfile certprofile = certificatesession.getCertificateProfile(administrator, cainfo.getCertificateProfileId());
 	int certtype = CertificateDataBean.CERTTYPE_SUBCA;
 	if(cainfo.getSignedBy() == CAInfo.SELFSIGNED)
 	  certtype = CertificateDataBean.CERTTYPE_ROOTCA;
 	// A CA certificate is published where the CRL is published and if there is a publisher noted in the certificate profile 
 	// (which there is probably not) 
 	Collection publishers = cainfo.getCRLPublishers();
 	publishers.addAll(certprofile.getPublisherList());
 	signsession.publishCACertificate(administrator, cainfo.getCertificateChain(), publishers, certtype);
 }
 
 public void revokeOCSPCertificate(int caid){
 	CAInfo cainfo = caadminsession.getCAInfo(administrator, caid);
	Iterator iter = cainfo.getExtendedCAServiceInfos().iterator();
	while(iter.hasNext()){
	  ExtendedCAServiceInfo next = (ExtendedCAServiceInfo) iter.next();	
	  if(next instanceof OCSPCAServiceInfo){
	  	X509Certificate ocspcert = (X509Certificate)((OCSPCAServiceInfo) next).getOCSPSignerCertificatePath().get(0);
		certificatesession.revokeCertificate(administrator,ocspcert, cainfo.getCRLPublishers(), RevokedCertInfo.REVOKATION_REASON_UNSPECIFIED);	  	 
	  }
	}  
 }
 
 public void activateCAToken(int caid, String authorizationcode) throws AuthorizationDeniedException, CATokenAuthenticationFailedException, CATokenOfflineException {
   caadminsession.activateCAToken(administrator,caid,authorizationcode);	
 }
 
 public void deactivateCAToken(int caid) throws AuthorizationDeniedException, EjbcaException{
    caadminsession.deactivateCAToken(administrator, caid);	
 }
   
  private ICAAdminSessionLocal           caadminsession; 
  private Admin                          administrator;
  private IAuthorizationSessionLocal     authorizationsession;
  private InformationMemory              info;
  private IUserAdminSessionLocal         adminsession;
  private IRaAdminSessionLocal           raadminsession; 
  private ICertificateStoreSessionLocal  certificatesession;                          
  private EjbcaWebBean                   ejbcawebbean;
  private ISignSessionLocal               signsession;
}
