<%               
  HardCATokenInfo hardcatokeninfo = (HardCATokenInfo) catokeninfo; 
  
%>
    <tr  id="Row<%=row++%2%>"> 
      <input type="hidden" name='<%= HIDDEN_CATOKENPATH %>' value='<%=catokenpath %>'>
      <td width="50%"  align="right"> 
        <%= ejbcawebbean.getText("HARDCATOKENPROPERTIES") %>
      </td>
      <td width="50%"> 
        <textarea name="<%=TEXTFIELD_HARDCATOKENPROPERTIES%>" cols=40 rows=6><% if(hardcatokeninfo != null && hardcatokeninfo.getProperties() != null) out.write(hardcatokeninfo.getProperties());%></textarea>
      </td>
    </tr>
    <% if(!editca){ %>
    <tr  id="Row<%=row++%2%>">       
      <td width="50%"  align="right"> 
        <%= ejbcawebbean.getText("AUTHENTICATIONCODE") %>
      </td>
      <td width="50%"> 
        <input type="text" name="<%=TEXTFIELD_AUTHENTICATIONCODE%>" size="40" maxlength="255">
      </td>
    </tr>
    <% }%>
    <tr  id="Row<%=row++%2%>"> 
      <td width="50%"  align="right"> 
        <%= ejbcawebbean.getText("SIGNALGORITHM") %>
      </td>
      <td width="50%"> 
           <% if(editca){
                  out.write(hardcatokeninfo.getSignatureAlgorithm());
              }else{%>
        <select name="<%=SELECT_SIGNATUREALGORITHM %>" size="1">
                
                <% for(int i=0; i < SoftCATokenInfo.AVAILABLE_SIGALGS.length; i++){ %>
                     <option value="<%= SoftCATokenInfo.AVAILABLE_SIGALGS[i]%>"><%= SoftCATokenInfo.AVAILABLE_SIGALGS[i] %></option>  
                <% } %> 
	 </select>
           <% } %> 
      </td>
    </tr>