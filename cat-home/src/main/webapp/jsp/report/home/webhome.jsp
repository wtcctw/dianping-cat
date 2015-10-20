<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.home.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.home.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.home.Model" scope="request"/>
<a:web_body>
	<div class="tab-content">
	<c:choose>
	   	<c:when test="${payload.docName == 'browserMonitor'}">
	   		<%@ include file="userMonitor/web.jsp"%>
	   	</c:when>
	   	<c:when test="${payload.docName == 'webInterface'}">
	   		<%@ include file="interface/webInterfaceMonitor.jsp"%>
	   	</c:when>
	   	<c:when test="${payload.docName == 'webAlert'}">
	   		<%@ include file="alertDocument/frontendException.jsp"%>
	   	</c:when>
	  	<c:otherwise>
	   		<%@ include file="userMonitor/web.jsp"%>
	   	</c:otherwise>
	 </c:choose>
	</div>
<br>
<br>
<script>
	$('#Web_documents').addClass('active open');
	var liElement = $('#${payload.docName}Button');
	if(liElement.size() == 0){
		liElement = $('#indexButton');
	}
	liElement.addClass('active');
</script>
</a:web_body>