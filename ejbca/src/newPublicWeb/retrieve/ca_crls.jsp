<%@ page pageEncoding="ISO-8859-1"%>
<%@ page contentType="text/html; charset=@page.encoding@" %>
<%@ page language="Java" import="javax.naming.*,javax.rmi.*,java.util.*,java.net.*,org.ejbca.core.ejb.ca.sign.*,org.ejbca.core.ejb.ca.caadmin.*,org.ejbca.core.model.ca.caadmin.*,org.ejbca.core.model.log.Admin"%>
<%@ include file="header.jsp" %>
  <h1>@EJBCA@ Fetch CA CRL</h1>
  <%
  try  {
      Admin admin = new Admin(Admin.TYPE_PUBLIC_WEB_USER, request.getRemoteAddr());
      InitialContext ctx = new InitialContext();
      ISignSessionHome home = home = (ISignSessionHome) PortableRemoteObject.narrow(ctx.lookup("RSASignSession"), ISignSessionHome.class );
      ISignSessionRemote ss = home.create();
      ICAAdminSessionHome cahome = (ICAAdminSessionHome) javax.rmi.PortableRemoteObject.narrow(ctx.lookup("CAAdminSession"), ICAAdminSessionHome.class );            
      ICAAdminSessionRemote caadminsession = cahome.create();          
      Collection caids = caadminsession.getAvailableCAs(admin);
      Iterator iter = caids.iterator();
      while (iter.hasNext()) {
          int caid = ((Integer)iter.next()).intValue();
          CAInfo ca = caadminsession.getCAInfo(admin, caid);
          String urlsubjectdn = URLEncoder.encode(ca.getSubjectDN(), "UTF-8"); 
  %>
  <hr>
  <div align="center">CA: <%= ca.getName() %></div>
  </p>
  <div align="center">
  	<a href="../certdist?cmd=crl&issuer=<%= urlsubjectdn %>">DER format</a> | 
  	<a href="../certdist?cmd=crl&format=PEM&issuer=<%= urlsubjectdn %>">PEM format</a> | 
  	<a href="../certdist?cmd=crl&issuer=<%= urlsubjectdn %>&moz=y">Import </a>in Mozilla/Netscape
  </div>
  <div align="center">
  <%
      }
  } catch(Exception ex) {
      ex.printStackTrace();
  }                                             
  %>
  </div>
<%@ include file="footer.inc" %>
