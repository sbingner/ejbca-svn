package se.anatom.ejbca.ra.raadmin;

import RegularExpression.RE;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import se.anatom.ejbca.ra.raadmin.DNFieldExtractor;
import se.anatom.ejbca.SecConst;
import se.anatom.ejbca.util.UpgradeableDataHashMap;

/**
 * The model representation of an end entity profile, used in in the ra module of ejbca web interface.
 *
 * @author  Philip Vendil
 * @version $Id: EndEntityProfile.java,v 1.2 2003-01-12 17:16:33 anatom Exp $
 */
public class EndEntityProfile extends UpgradeableDataHashMap implements java.io.Serializable, Cloneable {

    public static final float LATEST_VERSION = 0;      
    
    // Public constants
    // Type of data constants.
    public static final int VALUE      = 0;
    public static final int USE        = 1;
    public static final int ISREQUIRED = 2;
    public static final int MODIFYABLE = 3;

    // Field constants. 
    public static final int USERNAME           = 0;
    public static final int PASSWORD           = 1;
    public static final int CLEARTEXTPASSWORD  = 2;
    public static final int OLDEMAILDN         = 3;    
    public static final int COMMONNAME         = 4;
    public static final int SERIALNUMBER       = 5;  
    public static final int TITLE              = 6;    
    public static final int ORGANIZATIONUNIT   = 7;
    public static final int ORGANIZATION       = 8;
    public static final int LOCALE             = 9;
    public static final int STATE              = 10;
    public static final int DOMAINCOMPONENT    = 11;    
    public static final int COUNTRY            = 12;
    public static final int RFC822NAME         = 13;
    public static final int DNSNAME            = 14;
    public static final int IPADDRESS          = 15;
    public static final int OTHERNAME          = 16;
    public static final int UNIFORMRESOURCEID  = 17;
    public static final int X400ADDRESS        = 18;
    public static final int DIRECTORYNAME      = 19;
    public static final int EDIPARTNAME        = 20;
    public static final int REGISTEREDID       = 21;       
    public static final int EMAIL              = 22;
    public static final int ADMINISTRATOR      = 23;
    public static final int KEYRECOVERABLE     = 24;    
    public static final int DEFAULTCERTPROFILE = 25;
    public static final int AVAILCERTPROFILES  = 26;    
    public static final int DEFKEYSTORE        = 27;
    public static final int AVAILKEYSTORE      = 28;
    public static final int DEFAULTTOKENISSUER = 29;    
    public static final int AVAILTOKENISSUER   = 30;
    
     
    public static final int NUMBEROFPARAMETERS = 31;
    
    public static final String SPLITCHAR          = ";";

    public static final String TRUE  = "true";
    public static final String FALSE = "false";
        
    // Constants used with field ordering
    public static final int FIELDTYPE = 0;
    public static final int NUMBER    = 1;
    
    // Public methods.
    /** Creates a new instance of EndEntity Profile */
    public EndEntityProfile() {
      super();  

      // Set default required fields.
      init(false);     
    }
    
    /** Creates a default empty end entity profile with all standard fields added to it. */
    public  EndEntityProfile(boolean allfields){
      super();
      
      init(allfields);        
    }
    
    private void init(boolean allfields){
      if(allfields){
        // initialize profile data     
        ArrayList numberoffields = new ArrayList(NUMBEROFPARAMETERS);
        for(int i =0; i < NUMBEROFPARAMETERS; i++){
          numberoffields.add(new Integer(0));  
        }
        data.put(NUMBERARRAY,numberoffields);   
        data.put(SUBJECTDNFIELDORDER,new ArrayList());      
        data.put(SUBJECTALTNAMEFIELDORDER,new ArrayList()); 
      
        for(int i=0; i < NUMBEROFPARAMETERS; i++){
          addField(i);  
          setValue(i,0,"");
          setRequired(i,0,false);
          setUse(i,0,true);
          setModifyable(i,0,true);  
        }
      
        setRequired(USERNAME,0,true);
        setRequired(PASSWORD,0,true);
        setRequired(COMMONNAME,0,true);
        setRequired(DEFAULTCERTPROFILE,0,true);
        setRequired(AVAILCERTPROFILES,0,true);
        setRequired(DEFKEYSTORE,0,true);   
        setRequired(AVAILKEYSTORE,0,true); 
        setValue(DEFAULTCERTPROFILE,0,"1");
        setValue(AVAILCERTPROFILES,0,"1;2;3");        
        setValue(DEFKEYSTORE,0, "" + SecConst.TOKEN_SOFT_BROWSERGEN);
        setValue(AVAILKEYSTORE,0, SecConst.TOKEN_SOFT_BROWSERGEN + ";" + SecConst.TOKEN_SOFT_P12 +  ";" + SecConst.TOKEN_SOFT_JKS + ";" + SecConst.TOKEN_SOFT_PEM);        
          
      }else{
         // initialize profile data      
         ArrayList numberoffields = new ArrayList(NUMBEROFPARAMETERS);
         for(int i =0; i < NUMBEROFPARAMETERS; i++){
           numberoffields.add(new Integer(0));  
         }
      
         data.put(NUMBERARRAY,numberoffields);         
         data.put(SUBJECTDNFIELDORDER,new ArrayList());
         data.put(SUBJECTALTNAMEFIELDORDER,new ArrayList());   
      
         addField(USERNAME);
         addField(PASSWORD);
         addField(COMMONNAME);
         addField(EMAIL);         
         addField(DEFAULTCERTPROFILE);
         addField(AVAILCERTPROFILES);
         addField(DEFKEYSTORE);      
         addField(AVAILKEYSTORE);      
         addField(DEFAULTTOKENISSUER);
         addField(AVAILTOKENISSUER);
      
         setRequired(USERNAME,0,true);
         setRequired(PASSWORD,0,true);
         setRequired(COMMONNAME,0,true);
         setRequired(DEFAULTCERTPROFILE,0,true);
         setRequired(AVAILCERTPROFILES,0,true);
         setRequired(DEFKEYSTORE,0,true);   
         setRequired(AVAILKEYSTORE,0,true);  
         setValue(DEFAULTCERTPROFILE,0,"1");
         setValue(AVAILCERTPROFILES,0,"1;2;3");
         setValue(DEFKEYSTORE,0, "" + SecConst.TOKEN_SOFT_BROWSERGEN);
         setValue(AVAILKEYSTORE,0, SecConst.TOKEN_SOFT_BROWSERGEN + ";" + SecConst.TOKEN_SOFT_P12 +  ";" + SecConst.TOKEN_SOFT_JKS + ";" + SecConst.TOKEN_SOFT_PEM); 
      }                
    }
    
    /**
     * Function that adds a field to the profile.
     *
     * @param paramter is the field and one of the field constants.
     */
    public void addField(int parameter){
      int size =  getNumberOfField(parameter);
      setValue(parameter,size,"");
      setRequired(parameter,size,false);
      setUse(parameter,size,true);
      setModifyable(parameter,size,true);   
      if(parameter >= OLDEMAILDN && parameter <= COUNTRY){
        ArrayList fieldorder = (ArrayList) data.get(SUBJECTDNFIELDORDER);
        fieldorder.add(new Integer((NUMBERBOUNDRARY*parameter) + size));  
        Collections.sort(fieldorder);
      }  
      if(parameter >= RFC822NAME && parameter <= REGISTEREDID){
        ArrayList fieldorder = (ArrayList) data.get(SUBJECTALTNAMEFIELDORDER);
        fieldorder.add(new Integer((NUMBERBOUNDRARY*parameter) + size));  
      }       
      incrementFieldnumber(parameter);          
    }
    
    /** 
     * Function that removes a field from the end entity profile.
     *
     * @param parameter is the field to remove.
     * @param number is the number of field.
     */
    public void removeField(int parameter, int number){
      // Remove field and move all fileds above.
      int size =  getNumberOfField(parameter);
      
      if(size>0){
        for(int n = number; n < size-1; n++){
          setValue(parameter,n,getValue(parameter,n+1));
          setRequired(parameter,n,isRequired(parameter,n+1));
          setUse(parameter,n,getUse(parameter,n+1));
          setModifyable(parameter,n,isModifyable(parameter,n+1));                    
        }
        
        // Remove from order list.
        if(parameter >= OLDEMAILDN && parameter <= COUNTRY){
          ArrayList fieldorder = (ArrayList) data.get(SUBJECTDNFIELDORDER);          
          int value = (NUMBERBOUNDRARY*parameter) + number;
          for(int i=0; i < fieldorder.size(); i++){
             if( value ==  ((Integer) fieldorder.get(i)).intValue()){
                fieldorder.remove(i);
                break;
             }
          }
        }   
        
        if(parameter >= RFC822NAME && parameter <= REGISTEREDID){
          ArrayList fieldorder = (ArrayList) data.get(SUBJECTALTNAMEFIELDORDER);        
          int value = (NUMBERBOUNDRARY*parameter) + number;
          for(int i=0; i < fieldorder.size(); i++){
             if( value ==  ((Integer) fieldorder.get(i)).intValue()){
                fieldorder.remove(i);
                break;
             }
          }
        }         
 
        data.remove(new Integer((VALUE*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter)); 
        data.remove(new Integer((USE*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter));
        data.remove(new Integer((ISREQUIRED*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter));
        data.remove(new Integer((MODIFYABLE*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter));
      
        decrementFieldnumber(parameter);
      }   
    }

    /**
     * Function that returns the number of one kind of field.
     *
     */
    public int getNumberOfField(int parameter){
      return ((Integer) ((ArrayList) data.get(NUMBERARRAY)).get(parameter)).intValue();   
    }
    
    public void setValue(int parameter, int number, String value) {
       if(value !=null){
          value=value.trim();
          data.put(new Integer((VALUE*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter), value);
       }else{
          data.put(new Integer((VALUE*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter), "");
       }
    }
    
    public void setUse(int parameter, int number, boolean use){
          data.put(new Integer((USE*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter), new Boolean(use));
    }

    public void setRequired(int parameter, int number,  boolean isrequired) {
      data.put(new Integer((ISREQUIRED*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter), new Boolean(isrequired));
    }

    public void setModifyable(int parameter, int number, boolean changeable) {
       data.put(new Integer((MODIFYABLE*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter), new Boolean(changeable));
    }

    public String getValue(int parameter, int number) {
      String returnval = (String) data.get(new Integer((VALUE*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter)); 
      if(returnval != null)
        return returnval;
      else
        return "";  
    }

    public boolean getUse(int parameter, int number){
      Boolean returnval = (Boolean) data.get(new Integer((USE*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter)); 
      if(returnval != null)
        return returnval.booleanValue();
      else
        return false;              
    }
    
    public boolean isRequired(int parameter, int number) {
      Boolean returnval = (Boolean) data.get(new Integer((ISREQUIRED*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter));   
      if(returnval != null)
        return returnval.booleanValue();
      else
        return false;          
    }

    public boolean isModifyable(int parameter, int number){
      Boolean returnval = (Boolean) data.get(new Integer((MODIFYABLE*FIELDBOUNDRARY) + (NUMBERBOUNDRARY*number) + parameter));   
      if(returnval != null)
        return returnval.booleanValue();
      else
        return false;            
    }
    
    public int getSubjectDNFieldOrderLength(){
      return ((ArrayList) data.get(SUBJECTDNFIELDORDER)).size();   
    }
    public int getSubjectAltNameFieldOrderLength(){
      return ((ArrayList) data.get(SUBJECTALTNAMEFIELDORDER)).size();   
    }   
    
    public int[] getSubjectDNFieldsInOrder(int index){
      int[] returnval = new int[2];
      ArrayList fieldorder = (ArrayList) data.get(SUBJECTDNFIELDORDER);
      returnval[NUMBER] = ((Integer) fieldorder.get(index)).intValue() % NUMBERBOUNDRARY;
      returnval[FIELDTYPE] = ((Integer) fieldorder.get(index)).intValue() / NUMBERBOUNDRARY;
      
      return returnval;        
    }
    
    public int[] getSubjectAltNameFieldsInOrder(int index){
      int[] returnval = new int[2];
      ArrayList fieldorder = (ArrayList) data.get(SUBJECTALTNAMEFIELDORDER);      
      returnval[NUMBER] = ((Integer) fieldorder.get(index)).intValue() % NUMBERBOUNDRARY;
      returnval[FIELDTYPE] = ((Integer) fieldorder.get(index)).intValue() / NUMBERBOUNDRARY;
      
      return returnval;        
    }    
    
    /** A function that takes an fieldid pointing to a coresponding id in UserView and DnFieldExctractor.
     *  For example : profileFieldIdToUserFieldIdMapper(EndEntityProfile.COMMONNAME) returns DnFieldExctractor.COMMONNAME.
     *
     *  Should only be used with subjectDN and Subject Alternative Names fields.
     */
    public static int profileFieldIdToUserFieldIdMapper(int parameter){
      return  PROFILEIDTOUSERIDMAPPER[parameter]; 
    }
    
    
    public void doesUserFullfillEndEntityProfile(String username, String password, String dn, String subjectaltname, String email,  int certificateprofileid, boolean clearpwd, boolean administrator, boolean keyrecoverable, int tokentype, int hardwaretokenissuerid)
       throws UserDoesntFullfillEndEntityProfile{
     
       
      if(!isModifyable(PASSWORD,0)){
        if(!password.equals(getValue(PASSWORD,0))) 
          throw new UserDoesntFullfillEndEntityProfile("Password didn't match requirement of it's profile.");        
      }
      else
        if(isRequired(PASSWORD,0)){
          if(password == null || password.trim().equals(""))
            throw new UserDoesntFullfillEndEntityProfile("Password cannot be empty or null.");           
        }
   
      if(!getUse(CLEARTEXTPASSWORD,0) && clearpwd)
          throw new UserDoesntFullfillEndEntityProfile("Clearpassword (used in batch proccessing) cannot be used.");
      
      if(isRequired(CLEARTEXTPASSWORD,0)){
        if(getValue(CLEARTEXTPASSWORD,0).equals(TRUE) && !clearpwd)
           throw new UserDoesntFullfillEndEntityProfile("Clearpassword (used in batch proccessing) cannot be false.");          
        if(getValue(CLEARTEXTPASSWORD,0).equals(FALSE) && clearpwd)      
           throw new UserDoesntFullfillEndEntityProfile("Clearpassword (used in batch proccessing) cannot be true.");               
      }             
       
      doesUserFullfillEndEntityProfileWithoutPassword(username, dn, subjectaltname, email,  certificateprofileid, administrator, keyrecoverable, tokentype, hardwaretokenissuerid);
       
    }    
    
    public void doesUserFullfillEndEntityProfileWithoutPassword(String username,  String dn, String subjectaltname, String email,  int certificateprofileid, boolean administrator, boolean keyrecoverable, int tokentype, int hardwaretokenissuerid) throws UserDoesntFullfillEndEntityProfile{ 
      DNFieldExtractor subjectdnfields = new DNFieldExtractor(dn, DNFieldExtractor.TYPE_SUBJECTDN);
      DNFieldExtractor subjectaltnames   = new DNFieldExtractor(subjectaltname, DNFieldExtractor.TYPE_SUBJECTALTNAME);      
      String dnfield;
      String[] values;  
      
      // Check that no other than supported dn fields exists in the subject dn.
      if(subjectdnfields.existsOther())
        throw new UserDoesntFullfillEndEntityProfile("Unsupported Subject DN Field found.");   
  
      if(subjectaltnames.existsOther())
        throw new UserDoesntFullfillEndEntityProfile("Unsupported Subject Alternate Name Field found.");       
      
      checkIfAllRequiredFieldsExists(subjectdnfields, subjectaltnames,  username, email);      
      
      // Check contents of username. 
      checkIfDataFullfillProfile(USERNAME,0,username, "Username");
      
      // Check contents of Subject DN fields.
      int[] subjectdnfieldnumbers = subjectdnfields.getNumberOfFields();
      for(int i = 0; i < DNFieldExtractor.SUBJECTALTERNATIVENAMEBOUNDRARY; i++){
        for(int j=0; j < subjectdnfieldnumbers[i]; j++){
          checkIfDataFullfillProfile(DNEXTRATORTOPROFILEMAPPER[i],j,subjectdnfields.getField(i,j), DNEXTRATORTOPROFILEMAPPERTEXTS[i]);            
        } 
      }
       // Check contents of Subject Alternative Name fields.     
      int[] subjectaltnamesnumbers = subjectaltnames.getNumberOfFields();
      for(int i = DNFieldExtractor.SUBJECTALTERNATIVENAMEBOUNDRARY; i < subjectdnfieldnumbers.length; i++){
        for(int j=0; j < subjectaltnamesnumbers[i]; j++){
          checkIfDataFullfillProfile(DNEXTRATORTOPROFILEMAPPER[i],j,subjectaltnames.getField(i,j), DNEXTRATORTOPROFILEMAPPERTEXTS[i]);            
        }                  
      }    
           
    //  Check Email address.
     checkIfDataFullfillProfile(EMAIL,0,email,"Email");

      
      
   // Check for administrator flag.
      if(!getUse(ADMINISTRATOR,0) &&  administrator)
          throw new UserDoesntFullfillEndEntityProfile("Administrator cannot be set.");          
  
      if(isRequired(ADMINISTRATOR,0)){
        if(getValue(ADMINISTRATOR,0).equals(TRUE) && !administrator)
           throw new UserDoesntFullfillEndEntityProfile("Administrator flag is required.");         
        if(getValue(ADMINISTRATOR,0).equals(FALSE) && administrator)        
           throw new UserDoesntFullfillEndEntityProfile("Administrator flag cannot be set in end entity profile.");         
      }   
   // Check for administrator flag.
      if(!getUse(KEYRECOVERABLE,0) &&  administrator)
          throw new UserDoesntFullfillEndEntityProfile("Key Recoverable cannot be used.");  
  
      if(isRequired(KEYRECOVERABLE,0)){
        if(getValue(KEYRECOVERABLE,0).equals(TRUE) && !administrator)
           throw new UserDoesntFullfillEndEntityProfile("Key Recoverable is required.");  
        if(getValue(KEYRECOVERABLE,0).equals(FALSE) && administrator)       
           throw new UserDoesntFullfillEndEntityProfile("Key Recoverable cannot be set in end entity profile.");            
      }  
       
      // Check if certificate profile is among available certificate profiles.       
      String[] availablecertprofiles; 
      try{
        availablecertprofiles = new RE(SPLITCHAR, false).split(getValue(AVAILCERTPROFILES,0));  
      }catch(Exception e){
          throw new UserDoesntFullfillEndEntityProfile("Error parsing end entity profile.");  
      }
      if(availablecertprofiles == null)
           throw new UserDoesntFullfillEndEntityProfile("Error Available certificate profiles is null.");   
      else{
        boolean found=false;  
        for(int i=0; i < availablecertprofiles.length;i++){
          if( Integer.parseInt(availablecertprofiles[i]) == certificateprofileid)
            found=true;
        }
        
        if(!found)
           throw new UserDoesntFullfillEndEntityProfile("Couldn't find certificate profile among available certificate profiles."); 
      }
      

 
      // Check if tokentype is among available  token types.    
      String[] availablesofttokentypes; 
      try{
        availablesofttokentypes = new RE(SPLITCHAR, false).split(getValue(AVAILKEYSTORE,0));  
      }catch(Exception e){
        throw new UserDoesntFullfillEndEntityProfile("Error parsing end entity profile.");  
      }
      if(availablesofttokentypes == null)
         throw new UserDoesntFullfillEndEntityProfile("Error available  token types is null.");   
      else{
        boolean found=false;  
        for(int i=0; i < availablesofttokentypes.length;i++){
          if( Integer.parseInt(availablesofttokentypes[i]) == tokentype)
            found=true;
        }
       
        if(!found)
           throw new UserDoesntFullfillEndEntityProfile("Couldn't find  token type among available token types."); 
      }
        
      
      // If soft token check for hardwaretoken issuer id = 0.
      if(tokentype <= SecConst.TOKEN_SOFT){       
        if(hardwaretokenissuerid != 0)
           throw new UserDoesntFullfillEndEntityProfile("Soft tokens cannot have a hardware token issuer.");           
      }    
      // If Hard token type check if hardware token issuer is among available hardware token issuers.
      if(tokentype > SecConst.TOKEN_SOFT){ // Hardware token.
        String[] availablehardtokenissuers; 
        try{
          availablehardtokenissuers = new RE(SPLITCHAR, false).split(getValue(AVAILTOKENISSUER,0));  
        }catch(Exception e){
          throw new UserDoesntFullfillEndEntityProfile("Error parsing end entity profile.");  
        }
        if(availablehardtokenissuers == null)
           throw new UserDoesntFullfillEndEntityProfile("Error available hard token issuers is null.");   
        else{
          boolean found=false;  
          for(int i=0; i < availablehardtokenissuers.length;i++){
            if( Integer.parseInt(availablehardtokenissuers[i]) == hardwaretokenissuerid)
              found=true;
          }
        
          if(!found)
            throw new UserDoesntFullfillEndEntityProfile("Couldn't find hard token issuers among available hard token issuers."); 
        }
      } 
    }
    
    public Object clone() throws CloneNotSupportedException {
      EndEntityProfile clone = new EndEntityProfile();
      HashMap clonedata = (HashMap) clone.saveData();
      
      Iterator i = (data.keySet()).iterator();
      while(i.hasNext()){
        Object key = i.next();  
        clonedata.put(key,data.get(key)); 
      }
      
      clone.loadData(clonedata);
      return clone;
    }
    
    /** Implemtation of UpgradableDataHashMap function getLatestVersion */
    public float getLatestVersion(){
       return LATEST_VERSION;  
    }
    
    /** Implemtation of UpgradableDataHashMap function upgrade. */    
    
    public void upgrade(){
      if(LATEST_VERSION != getVersion()){
        // New version of the class, upgrade  
        
        data.put(VERSION, new Float(LATEST_VERSION));  
      }  
    } 
    
    // Private Methods
    
    private void checkIfDataFullfillProfile(int field, int number, String data, String text) throws UserDoesntFullfillEndEntityProfile {
        
      if(data == null && field != EMAIL)
          throw new UserDoesntFullfillEndEntityProfile("Field " +  text + " cannot be null."); 
        
      if(data !=null)        
        if(!getUse(field,number) && !data.trim().equals(""))
          throw new UserDoesntFullfillEndEntityProfile(text + " cannot be used in end entity profile."); 

      if(field == OLDEMAILDN || field == RFC822NAME){
        if(!isModifyable(EMAIL,0)){
          String[] values;  
          try{  
            values = new RE(SPLITCHAR, false).split(getValue(EMAIL,number)); 
          }catch(Exception e){          
             throw new UserDoesntFullfillEndEntityProfile("Error parsing end entity profile."); 
          }   
          boolean exists = false;
          for(int i = 0; i < values.length ; i++){
            if(data.equals(values[i].trim())) 
              exists = true;
          }   
          if(!exists)
            throw new UserDoesntFullfillEndEntityProfile("Field " + text + " data didn't match requirement of end entity profile.");           
        }      
      }
      else{
        if(!isModifyable(field,number)){
          String[] values;     
          try{  
            values = new RE(SPLITCHAR, false).split(getValue(field,number)); 
          }catch(Exception e){          
            throw new UserDoesntFullfillEndEntityProfile("Error parsing end entity profile."); 
          }   
          boolean exists = false;
          for(int i = 0; i < values.length ; i++){
            if(data.equals(values[i].trim())) 
              exists = true;
          }   
          if(!exists)
            throw new UserDoesntFullfillEndEntityProfile("Field " + text + " data didn't match requirement of end entity profile.");           
        }
      }  
    }
    
    private void checkIfAllRequiredFieldsExists(DNFieldExtractor subjectdnfields, DNFieldExtractor subjectaltnames, String username, String email)  throws UserDoesntFullfillEndEntityProfile{
        int size;
        
        // Check if Username exists.
        if(isRequired(USERNAME,0)){
          if(username == null || username.trim().equals(""))
            throw new UserDoesntFullfillEndEntityProfile("Username cannot be empty or null.");           
        }     
        
        // Check if required Email fields exists.
        if(isRequired(EMAIL,0)){
          if(email == null || email.trim().equals(""))
            throw new UserDoesntFullfillEndEntityProfile("Email address cannot be empty or null.");           
        }  
        
        
        // Check if all required subjectdn fields exists.
        for(int i = 0; i < SUBJECTDNFIELDS.length; i++){
          size = getNumberOfField(SUBJECTDNFIELDS[i]);
          for(int j = 0; j < size; j++){        
            if(isRequired(SUBJECTDNFIELDS[i],j))
              if(subjectdnfields.getField(SUBJECTDNFIELDEXTRACTORNAMES[i],j).trim().equals(""))
                throw new UserDoesntFullfillEndEntityProfile("Subject DN field '" + SUBJECTDNFIELDNAMES[i] + "' must exist.");                      
          }
        } 
        
         // Check if all required subject alternate name fields exists.
        for(int i = 0; i < SUBJECTALTNAMEFIELDS.length; i++){
          size = getNumberOfField(SUBJECTALTNAMEFIELDS[i]);
          for(int j = 0; j < size; j++){        
            if(isRequired(SUBJECTALTNAMEFIELDS[i],j))
              if(subjectaltnames.getField(SUBJECTALTNAMEFIELDEXTRACTORNAMES[i],j).trim().equals(""))
                throw new UserDoesntFullfillEndEntityProfile("Subject DN field '" + SUBJECTALTNAMEFIELDNAMES[i] + "' must exist.");                      
          }
        }           
    }
    
    
    private void  incrementFieldnumber(int parameter){
      ArrayList numberarray = (ArrayList) data.get(NUMBERARRAY);  
      numberarray.set(parameter, new Integer(((Integer) numberarray.get(parameter)).intValue() + 1));  
    }
    
    private void  decrementFieldnumber(int parameter){
      ArrayList numberarray = (ArrayList) data.get(NUMBERARRAY);  
      numberarray.set(parameter, new Integer(((Integer) numberarray.get(parameter)).intValue() - 1));         
    }    
    
    // Private Constants.
    private static final int FIELDBOUNDRARY  = 10000;
    private static final int NUMBERBOUNDRARY = 100;
    
    private static final int[] SUBJECTDNFIELDS              = {EMAIL, COMMONNAME, SERIALNUMBER, TITLE, ORGANIZATIONUNIT, ORGANIZATION, LOCALE, STATE, DOMAINCOMPONENT, COUNTRY}; 
    private static final int[] SUBJECTDNFIELDEXTRACTORNAMES = {DNFieldExtractor.EMAILADDRESS, DNFieldExtractor.COMMONNAME, DNFieldExtractor.SERIALNUMBER, DNFieldExtractor.TITLE,
                                                               DNFieldExtractor.ORGANIZATIONUNIT, DNFieldExtractor.ORGANIZATION, DNFieldExtractor.LOCALE, 
                                                               DNFieldExtractor.STATE, DNFieldExtractor.DOMAINCOMPONENT, DNFieldExtractor.COUNTRY}; 
    private static final String[] SUBJECTDNFIELDNAMES       = {"Email Address (EmailAddress)", "CommonName (CN)", "SerialNumber (SN)", "Title (T)", "OrganizationUnit (OU)", "Organization (O)",
                                                               "Location (L)", "State (ST)", "DomainComponent (DC)", "Country (C)"};  
    
     
    private static final int[] SUBJECTALTNAMEFIELDS              = {DNSNAME,IPADDRESS, OTHERNAME, UNIFORMRESOURCEID, X400ADDRESS, DIRECTORYNAME, EDIPARTNAME, REGISTEREDID, EMAIL};
    private static final int[] SUBJECTALTNAMEFIELDEXTRACTORNAMES = {DNFieldExtractor.DNSNAME,DNFieldExtractor.IPADDRESS, DNFieldExtractor.OTHERNAME,
                                                                    DNFieldExtractor.UNIFORMRESOURCEIDENTIFIER, DNFieldExtractor.X400ADDRESS, DNFieldExtractor.DIRECTORYNAME, 
                                                                    DNFieldExtractor.EDIPARTNAME, DNFieldExtractor.REGISTEREDID, DNFieldExtractor.RFC822NAME};
    private static final String[] SUBJECTALTNAMEFIELDNAMES       = {"DNSName", "IPAddress", "OtherName", "UniformResourceId", "X400Address", "DirectoryName",
                                                                    "EDIPartName","RegisteredId","RFC822Name"};   
    
    // Used to map constants of DNFieldExtractor to end entity profile constants.
    private static final int[] DNEXTRATORTOPROFILEMAPPER      = {OLDEMAILDN, COMMONNAME, SERIALNUMBER, TITLE, ORGANIZATIONUNIT, ORGANIZATION, LOCALE, 
                                                                 STATE, DOMAINCOMPONENT, COUNTRY, OTHERNAME, RFC822NAME, DNSNAME,
                                                                 IPADDRESS, X400ADDRESS, DIRECTORYNAME, EDIPARTNAME, UNIFORMRESOURCEID, REGISTEREDID};
    private static final String[] DNEXTRATORTOPROFILEMAPPERTEXTS = {"Email Address (EmailAddress)", "CommonName (CN)", "SerialNumber (SN)", "Title (T)", "OrganizationUnit (OU)", "Organization (O)", "Location (L)", 
                                                                 "State (ST)", "DomainComponent (DC)", "Country (C)", "OtherName", "RFC822Name", "DNSName",
                                                                 "IPAddress", "X400Address", "DirectoryName", "EDIPartName", "UniformResourceId", "RegisteredId"};                                                                 
      
    private static final int[] PROFILEIDTOUSERIDMAPPER        = {0,0,0,DNFieldExtractor.EMAILADDRESS, DNFieldExtractor.COMMONNAME ,DNFieldExtractor.SERIALNUMBER ,DNFieldExtractor.TITLE 
                                                                 ,DNFieldExtractor.ORGANIZATIONUNIT, DNFieldExtractor.ORGANIZATION ,DNFieldExtractor.LOCALE ,DNFieldExtractor.STATE
                                                                 ,DNFieldExtractor.DOMAINCOMPONENT ,DNFieldExtractor.COUNTRY ,DNFieldExtractor.RFC822NAME 
                                                                 ,DNFieldExtractor.DNSNAME ,DNFieldExtractor.IPADDRESS ,DNFieldExtractor.OTHERNAME ,DNFieldExtractor.UNIFORMRESOURCEIDENTIFIER 
                                                                 ,DNFieldExtractor.X400ADDRESS ,DNFieldExtractor.DIRECTORYNAME ,DNFieldExtractor.EDIPARTNAME ,DNFieldExtractor.REGISTEREDID};
                                                                
                                                                 
    private static final String NUMBERARRAY               = "NUMBERARRAY";
    private static final String SUBJECTDNFIELDORDER       = "SUBJECTDNFIELDORDER";
    private static final String SUBJECTALTNAMEFIELDORDER  = "SUBJECTALTNAMEFIELDORDER";    
    // Private fields.
    

}
