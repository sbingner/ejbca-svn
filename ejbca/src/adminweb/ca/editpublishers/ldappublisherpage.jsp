<%               

   LdapPublisher ldappublisher = (LdapPublisher) publisherhelper.publisherdata;

   int[] usefieldsindn =  {DNFieldExtractor.CN, DNFieldExtractor.UID, DNFieldExtractor.SN, 
                           DNFieldExtractor.GIVENNAME, DNFieldExtractor.SURNAME, DNFieldExtractor.T, 
                           DNFieldExtractor.L, DNFieldExtractor.INITIALS, DNFieldExtractor.E };

   String[] usefieldsindntexts = {"MATCHCOMMONNAME","MATCHUID","MATCHDNSERIALNUMBER",
                                "MATCHGIVENNAME", "MATCHSURNAME","MATCHTITLE",
                                "MATCHLOCALE","MATCHINITIALS","OLDEMAILDN1"}; 

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
          <%= ejbcawebbean.getText("LDAPSETTINGS") %>:
        </div>
      </td>
      <td width="50%" valign="top"> 
         &nbsp;
      </td>
    </tr>  
   <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("HOSTNAME") %>:
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="text" name="<%=EditPublisherJSPHelper.TEXTFIELD_LDAPHOSTNAME%>" size="30" maxlength="255" 
               value='<%= ldappublisher.getHostname ()%>'>
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("PORT") %>
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="text" name="<%=EditPublisherJSPHelper.TEXTFIELD_LDAPPORT%>" size="30" maxlength="255" 
               value='<%= ldappublisher.getPort ()%>'> <%= ejbcawebbean.getText("USESSL") %>
          <input type="checkbox" name="<%= EditPublisherJSPHelper.CHECKBOX_LDAPUSESSL %>" onClick='setUseSSLPort()' value="<%=EditPublisherJSPHelper.CHECKBOX_VALUE %>" 
           <%  if(ldappublisher. getUseSSL())
                 out.write(" CHECKED ");
           %>>
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("BASEDN") %>
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="text" name="<%=EditPublisherJSPHelper.TEXTFIELD_LDAPBASEDN%>" size="30" maxlength="255" 
               value='<%= ldappublisher.getBaseDN()%>'> 
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("LOGINDN") %>
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="text" name="<%=EditPublisherJSPHelper.TEXTFIELD_LDAPLOGINDN%>" size="30" maxlength="255" 
               value='<%= ldappublisher.getLoginDN()%>'> 
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("LOGINPWD") %>
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="password" name="<%=EditPublisherJSPHelper.PASSWORD_LDAPLOGINPASSWORD%>" size="30" maxlength="255" 
               value='<%= ldappublisher.getLoginPassword()%>'> 
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("CONFIRMPASSWORD") %>
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="password" name="<%=EditPublisherJSPHelper.PASSWORD_LDAPCONFIRMLOGINPWD%>" size="30" maxlength="255" 
               value='<%= ldappublisher.getLoginPassword()%>'> 
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
          <%= ejbcawebbean.getText("CREATENONEXISTINGUSERS") %>
        </div>
      </td>
      <td width="50%" valign="top"> 
          <input type="checkbox" name="<%= EditPublisherJSPHelper.CHECKBOX_LDAPCREATENONEXISTING %>" value="<%=EditPublisherJSPHelper.CHECKBOX_VALUE %>" 
           <%  if(ldappublisher.getCreateNonExisingUsers())
                 out.write(" CHECKED ");
           %>>
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("MODIFYEXISTINGUSERS") %>
        </div>
      </td>
      <td width="50%" valign="top"> 
          <input type="checkbox" name="<%= EditPublisherJSPHelper.CHECKBOX_LDAPMODIFYEXISTING %>" value="<%=EditPublisherJSPHelper.CHECKBOX_VALUE %>" 
           <%  if(ldappublisher.getModifyExistingUsers())
                 out.write(" CHECKED ");
           %>>
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
          <%= ejbcawebbean.getText("USEROBJECTCLASS") %>:
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="text" name="<%=EditPublisherJSPHelper.TEXTFIELD_LDAPUSEROBJECTCLASS%>" size="30" maxlength="255" 
               value='<%= ldappublisher.getUserObjectClass()%>'>
      </td>
    </tr>
   <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("CAOBJECTCLASS") %>:
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="text" name="<%=EditPublisherJSPHelper.TEXTFIELD_LDAPCAOBJECTCLASS%>" size="30" maxlength="255" 
               value='<%= ldappublisher.getCAObjectClass()%>'>
      </td>
    </tr>
   <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("USERCERTIFICATEATTR") %>:
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="text" name="<%=EditPublisherJSPHelper.TEXTFIELD_LDAPUSERCERTATTRIBUTE%>" size="30" maxlength="255" 
               value='<%= ldappublisher.getUserCertAttribute()%>'>
      </td>
    </tr>
   <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("CACERTIFICATEATTR") %>:
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="text" name="<%=EditPublisherJSPHelper.TEXTFIELD_LDAPCACERTATTRIBUTE%>" size="30" maxlength="255" 
               value='<%= ldappublisher.getCACertAttribute()%>'>
      </td>
    </tr>
   <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("CRLATTRIBUTE") %>:
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="text" name="<%=EditPublisherJSPHelper.TEXTFIELD_LDAPCRLATTRIBUTE%>" size="30" maxlength="255" 
               value='<%= ldappublisher.getCRLAttribute()%>'>
      </td>
    </tr>
   <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
      <%= ejbcawebbean.getText("ARLATTRIBUTE") %>:
        </div>
      </td>
      <td width="50%" valign="top"> 
         <input type="text" name="<%=EditPublisherJSPHelper.TEXTFIELD_LDAPARLATTRIBUTE%>" size="30" maxlength="255" 
               value='<%= ldappublisher.getARLAttribute()%>'>
      </td>
    </tr>
    <tr id="Row<%=row++%2%>"> 
      <td width="50%" valign="top"> 
        <div align="right"> 
          <%= ejbcawebbean.getText("USEFIELDSINDN") %><br>
          <%= ejbcawebbean.getText("DCOCSHOULDBEDEFINED") %>
        </div>
      </td>
      <td width="50%" valign="top">   
        <select name="<%=EditPublisherJSPHelper.SELECT_LDAPUSEFIELDINLDAPDN%>" size="9" multiple >       
            <% HashSet currentfields = new HashSet(ldappublisher.getUseFieldInLdapDN());
               for(int i=0;i < usefieldsindn.length; i++){ %>                                  
              <option value="<%=usefieldsindn[i]%>" <% if(currentfields.contains(new Integer(usefieldsindn[i]))) out.write(" selected "); %>> 
                  <%= ejbcawebbean.getText(usefieldsindntexts[i]) %>
               </option>
            <%}%>
          </select>         
      </td>
    </tr>
