<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request"/>
<a:mobile>
<table class="table table-striped table-condensed table-bordered table-hover">

	<tr>
		<td width="15%">Error Time </td>
		<td width="85%">${model.crashLogDetailInfo.crashTime}</td>
	</tr>
	<tr>
		<td>APP</td>
		<td>${model.crashLogDetailInfo.appName} - ${model.crashLogDetailInfo.appVersion}</td>
	</tr>
	<tr>
		<td>Platform</td>
		<td>${model.crashLogDetailInfo.platform} - ${model.crashLogDetailInfo.platformVersion}</td>
	</tr>
	<tr>
		<td>Level </td>
		<td>${model.crashLogDetailInfo.level}</td>
	</tr>
	<tr>
		<td>Module </td>
		<td>${model.crashLogDetailInfo.module}</td>
	</tr>
	<tr>
		<td>Device </td>
		<td>${model.crashLogDetailInfo.deviceBrand} - ${model.crashLogDetailInfo.deviceModel}</td>
	</tr>
	<tr>
		<td>Detail </td>
		<td>${model.crashLogDetailInfo.detail}</td>
	</tr>
</table>
<script type="text/javascript">
$(document).ready(
	function() {
		$('#Web_report').addClass('active open');
		$('#web_problem').addClass('active');
});

</script>
</a:mobile>