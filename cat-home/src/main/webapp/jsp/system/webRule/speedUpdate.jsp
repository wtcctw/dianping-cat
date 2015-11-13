<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.web.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.web.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.web.Model" scope="request" />

<a:web_body>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#Web_config').addClass('active open');
			$('#speed').addClass('active');
		});
	</script>
		<form name="speedUpdate" id="form" method="post" action="${model.pageUri}?op=speedSubmit">
			<table style='width:100%' class='table table-striped table-condensed '>
				<input type="hidden" class="input-xlarge"  name="step.pageid" value="${model.step.pageid}" />
				 <tr>
					<td>测速页面</td>
					<td>
					<c:choose>
					<c:when test="${model.step.pageid eq 0}">
						<input type="text" class="input-xlarge" name="step.page" required/>
					</c:when>
					<c:otherwise>
			  			<input type="text" class="input-xlarge" name="step.page" value="${model.step.page}" readonly/>
					</c:otherwise>
					</c:choose>
					</td>
				</tr>
				<c:if test="${model.step.pageid ne 0}">
				<tr>
					<td>测速点编号</td>
					<td>
		            	<input type="text" class="input-xlarge"  name="step.stepid" value="${model.step.stepid}" readonly/>
					</td>
				</tr>
				</c:if>
				<tr>
					<td>测速点名称</td>
					<td><input type="text" class="input-xlarge" name="step.step" required value="${model.step.step}"/></td>
				</tr>
				<tr>
					<td style='text-align:center' colspan='2'><input class='btn btn-primary btn-sm' type="submit" name="submit" value="提交" /></td>
				</tr> 
			</table>
		</form>
</a:web_body>