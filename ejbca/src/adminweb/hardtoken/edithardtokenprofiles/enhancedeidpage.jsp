<%               
  int[] signpintypes = { HardTokenProfile.PINTYPE_4DIGITS,HardTokenProfile.PINTYPE_6TO8DIGITS,
  		         HardTokenProfile.PINTYPE_6TO8DIGITSLETTERS, HardTokenProfile.PINTYPE_6TO8ALLPRINTABLE}; 
		
  int[] authpintypes = { HardTokenProfile.PINTYPE_4DIGITS,HardTokenProfile.PINTYPE_6TO8DIGITS,
                            HardTokenProfile.PINTYPE_6TO8DIGITSLETTERS, HardTokenProfile.PINTYPE_6TO8ALLPRINTABLE,
			    EnhancedEIDProfile.PINTYPE_AUTH_SAME_AS_SIGN};

  int[] encpintypes = { HardTokenProfile.PINTYPE_4DIGITS,HardTokenProfile.PINTYPE_6TO8DIGITS,
                            HardTokenProfile.PINTYPE_6TO8DIGITSLETTERS, HardTokenProfile.PINTYPE_6TO8ALLPRINTABLE,
			    EnhancedEIDProfile.PINTYPE_ENC_SAME_AS_AUTH};

  String[] signpintexts    = {"4DIGITS","6TO8DIGITS","6TO8DIGITSLETTERS","6TO8ALLPRINTABLE"};
  String[] authpintexts    = {"4DIGITS","6TO8DIGITS","6TO8DIGITSLETTERS","6TO8ALLPRINTABLE", "SAMEASSIGNCERT"};
  String[] encpintexts     = {"4DIGITS","6TO8DIGITS","6TO8DIGITSLETTERS","6TO8ALLPRINTABLE", "SAMEASAUTHCERT"};

  String[] keytexts = {"RSA1024BIT", "RSA2048BIT"};

  EnhancedEIDProfile curprofile = (EnhancedEIDProfile) helper.profiledata;

%>

   <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
         &nbsp;
        </div>
      </td>
      <td width="50%" valign="top"> 
         &nbsp;
      </td>
   </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("MINKEYLENGTH") %>
        </div>
      </td>
      <td width="50%" valign="top">   
        <select name="<%=EditHardTokenProfileJSPHelper.SELECT_MINKEYLENGTH%>" size="1"  >       
            <% int currentkeylength = curprofile.getMinimumKeyLength(EnhancedEIDProfile.CERTUSAGE_SIGN);      
               for(int i=0;i < EnhancedEIDProfile.AVAILABLEMINIMUMKEYLENGTHS.length;i++){ %>
              <option value="<%=EnhancedEIDProfile.AVAILABLEMINIMUMKEYLENGTHS[i]%>" <% if(EnhancedEIDProfile.AVAILABLEMINIMUMKEYLENGTHS[i] == currentkeylength) out.write(" selected "); %>> 
                  <%= ejbcawebbean.getText(keytexts[i]) %>
               </option>
            <%}%>
          </select>         
      </td>
    </tr>
   <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
         &nbsp;
        </div>
      </td>
      <td width="50%" valign="top"> 
         &nbsp;
      </td>
    </tr>    
   <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("CERTIFICATESETTINGS") %>:
        </div>
      </td>
      <td width="50%" valign="top"> 
         &nbsp;
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("SIGNINGCERTIFICATE") %>
        </div>
      </td>
      <td width="50%" valign="top"> 
         &nbsp;
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("CERTIFICATEPROFILE") %>
        </div>
      </td>
      <td width="50%" valign="top">   
        <select name="<%=EditHardTokenProfileJSPHelper.SELECT_CERTIFICATEPROFILE + "0"%>" size="1"  >       
            <% int currentcert = curprofile.getCertificateProfileId(EnhancedEIDProfile.CERTUSAGE_SIGN);
               Iterator iter = authorizedcertprofiles.keySet().iterator();
               while(iter.hasNext()){
                 String certprof = (String) iter.next();
                 Integer certprofid = (Integer) authorizedcertprofiles.get(certprof);%>
              <option value="<%=certprofid.intValue()%>" <% if(certprofid.intValue() == currentcert) out.write(" selected "); %>> 
                  <%= certprof %>
               </option>
            <%}%>
          </select>         
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("PINTYPE") %>
        </div>
      </td>
      <td width="50%" valign="top">          
         <select name="<%=EditHardTokenProfileJSPHelper.SELECT_PINTYPE + "0"%>" size="1"  >
            <% int currentpintype = curprofile.getPINType(EnhancedEIDProfile.CERTUSAGE_SIGN);
               
               for(int i=0;i < signpintypes.length;i++){%>
              <option value="<%=signpintypes[i]%>" <% if(signpintypes[i] == currentpintype) out.write(" selected "); %>> 
                  <%= ejbcawebbean.getText(signpintexts[i]) %>
               </option>
            <%}%>
          </select>         
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        &nbsp;
      </td>
      <td width="50%" valign="top"> 
         &nbsp;
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("AUTHCERTIFICATE") %>
        </div>
      </td>
      <td width="50%" valign="top"> 
         &nbsp;
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("CERTIFICATEPROFILE") %>
        </div>
      </td>
      <td width="50%" valign="top">   
        <select name="<%=EditHardTokenProfileJSPHelper.SELECT_CERTIFICATEPROFILE + "1"%>" size="1"  >       
            <% currentcert = curprofile.getCertificateProfileId(EnhancedEIDProfile.CERTUSAGE_AUTH);
               iter = authorizedcertprofiles.keySet().iterator();
               while(iter.hasNext()){
                 String certprof = (String) iter.next();
                 Integer certprofid = (Integer) authorizedcertprofiles.get(certprof);%>
              <option value="<%=certprofid.intValue()%>" <% if(certprofid.intValue() == currentcert) out.write(" selected "); %>> 
                  <%= certprof %>
               </option>
            <%}%>
          </select>         
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("PINTYPE") %>
        </div>
      </td>
      <td width="50%" valign="top">          
         <select name="<%=EditHardTokenProfileJSPHelper.SELECT_PINTYPE + "1"%>" size="1"  >
            <% currentpintype = curprofile.getPINType(EnhancedEIDProfile.CERTUSAGE_AUTH);
               
               for(int i=0;i < authpintypes.length;i++){%>
              <option value="<%=authpintypes[i]%>" <% if(authpintypes[i] == currentpintype) out.write(" selected "); %>> 
                  <%= ejbcawebbean.getText(authpintexts[i]) %>
               </option>
            <%}%>
          </select>         
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        &nbsp;
      </td>
      <td width="50%" valign="top"> 
         &nbsp;
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("ENCCERTIFICATE") %>
        </div>
      </td>
      <td width="50%" valign="top"> 
         &nbsp;
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("CERTIFICATEPROFILE") %>
        </div>
      </td>
      <td width="50%" valign="top">   
        <select name="<%=EditHardTokenProfileJSPHelper.SELECT_CERTIFICATEPROFILE + "2"%>" size="1"  >       
            <% currentcert = curprofile.getCertificateProfileId(EnhancedEIDProfile.CERTUSAGE_ENC);
               iter = authorizedcertprofiles.keySet().iterator();
               while(iter.hasNext()){
                 String certprof = (String) iter.next();
                 Integer certprofid = (Integer) authorizedcertprofiles.get(certprof);%>
              <option value="<%=certprofid.intValue()%>" <% if(certprofid.intValue() == currentcert) out.write(" selected "); %>> 
                  <%= certprof %>
               </option>
            <%}%>
          </select>         
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("PINTYPE") %>
        </div>
      </td>
      <td width="50%" valign="top">          
         <select name="<%=EditHardTokenProfileJSPHelper.SELECT_PINTYPE + "2"%>" size="1"  >
            <% currentpintype = curprofile.getPINType(EnhancedEIDProfile.CERTUSAGE_ENC);
               
               for(int i=0;i < encpintypes.length;i++){%>
              <option value="<%=encpintypes[i]%>" <% if(encpintypes[i] == currentpintype) out.write(" selected "); %>> 
                  <%= ejbcawebbean.getText(encpintexts[i]) %>
               </option>
            <%}%>
          </select>         
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("KEYRECOVERABLE") %>
        </div>
      </td>
      <td width="50%" valign="top">                   
          <input type="checkbox" name="<%= EditHardTokenProfileJSPHelper.CHECKBOX_KEYRECOVERABLE + "2" %>" value="<%=EditHardTokenProfileJSPHelper.CHECKBOX_VALUE %>" 
           <%  if(curprofile.getIsKeyRecoverable(EnhancedEIDProfile.CERTUSAGE_ENC))
                 out.write("CHECKED");
           %>>
      </td>
    </tr>

