<%@ page pageEncoding="ISO-8859-1"%>
<%@page errorPage="/errorpage.jsp" import="java.util.*, se.anatom.ejbca.webdist.webconfiguration.EjbcaWebBean,se.anatom.ejbca.ra.raadmin.GlobalConfiguration, se.anatom.ejbca.SecConst, se.anatom.ejbca.authorization.AuthorizationDeniedException,
               se.anatom.ejbca.authorization.AvailableAccessRules,
               se.anatom.ejbca.webdist.hardtokeninterface.HardTokenInterfaceBean, se.anatom.ejbca.hardtoken.hardtokenprofiles.*, se.anatom.ejbca.webdist.hardtokeninterface.EditHardTokenProfileJSPHelper, 
               se.anatom.ejbca.hardtoken.HardTokenProfileExistsException"%>

<html>
<jsp:useBean id="ejbcawebbean" scope="session" class="se.anatom.ejbca.webdist.webconfiguration.EjbcaWebBean" />
<jsp:useBean id="hardtokenbean" scope="session" class="se.anatom.ejbca.webdist.hardtokeninterface.HardTokenInterfaceBean" />
<jsp:useBean id="helper" scope="session" class="se.anatom.ejbca.webdist.hardtokeninterface.EditHardTokenProfileJSPHelper" />

<% 

  // Initialize environment
  String includefile = "hardtokenprofilespage.jsp"; 


  GlobalConfiguration globalconfiguration = ejbcawebbean.initialize(request, AvailableAccessRules.HARDTOKEN_EDITHARDTOKENPROFILES); 
                                            hardtokenbean.initialize(request, ejbcawebbean); 
                                            helper.initialize(request,ejbcawebbean, hardtokenbean);
  String THIS_FILENAME            =  globalconfiguration.getHardTokenPath()  + "/edithardtokenprofiles/edithardtokenprofiles.jsp";
  

  
 

%>
 
<head>
  <title><%= globalconfiguration .getEjbcaTitle() %></title>
  <base href="<%= ejbcawebbean.getBaseUrl() %>">
  <link rel=STYLESHEET href="<%= ejbcawebbean.getCssFile() %>">
  <script language=javascript src="<%= globalconfiguration .getAdminWebPath() %>ejbcajslib.js"></script>
</head>
<body>

<%  // Determine action 

  includefile = helper.parseRequest(request);

 // Include page
  if( includefile.equals("hardtokenprofilepage.jsp")){ 
%>
   <%@ include file="hardtokenprofilepage.jsp" %>
<%}
  if( includefile.equals("hardtokenprofilespage.jsp")){ %>
   <%@ include file="hardtokenprofilespage.jsp" %> 
<%} 
  if( includefile.equals("uploadtemplate.jsp")){ %>
   <%@ include file="uploadtemplate.jsp" %> 
<%}

   // Include Footer 
   String footurl =   globalconfiguration.getFootBanner(); %>
   
  <jsp:include page="<%= footurl %>" />

</body>
</html>
