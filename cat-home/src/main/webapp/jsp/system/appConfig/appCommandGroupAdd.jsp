<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.app.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.app.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.app.Model" scope="request"/>

<a:mobile>
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#userMonitor_config').addClass('active open');
			$('#appList').addClass('active');
		});
		
		$(document).delegate('#updateSubmit', 'click', function(e){
			var parent = $("#parent").val();
			var subCommand = $("#subCommand").val();
			
			window.location.href = "/cat/s/app?op=appCommandGroupSubmit&type=group&parent="+parent+"&name="+subCommand;
		}) 
	</script>
	
	<table class="table table-striped table-condensed table-bordered ">
		<tr>
			<td>父命令字</td><td><input name="parent" id="parent" /><br/>
		</td>
		<tr>
			<td>子命令字</td><td><input name="subCommand" id="subCommand" /><br/>
		</td>
		<tr>
		<tr>
			<td colspan="2" style="text-align:center;"><button class="btn btn-primary" id="updateSubmit">提交</button></td>
		</tr>
	</table>

</a:mobile>
