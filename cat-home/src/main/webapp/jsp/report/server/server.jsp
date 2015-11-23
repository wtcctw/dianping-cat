<%@ page contentType="text/html; charset=utf-8" isELIgnored="false"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.server.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.server.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.server.Model" scope="request"/>
<a:serverBody>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	
	<div class="breadcrumbs" id="breadcrumbs">
		<script type="text/javascript">
			try{ace.settings.check('breadcrumbs' , 'fixed')}catch(e){}
		</script>

		<ul class="breadcrumb">
			<table>
			<tr><th>
					<span>开始</span>
					<input type="text" id="startTime" style="width:150px;"/>
				</th>
				<th>
					<span>&nbsp;结束</span>
					<input type="text" id="endTime" style="width:150px;"/>
				</th>
				<th>
					&nbsp;<div class="btn-group">
					<button data-toggle="dropdown" class="btn btn-info btn-sm dropdown-toggle" data-rel="popover" data-trigger="hover" data-placement="top"
				    data-content="<div class='row text-danger center'>测试失败</div>" title="${device}" data-original-title="">看图
						<span class="ace-icon fa fa-caret-down icon-only"></span>
				    </button>
					<ul class="dropdown-menu dropdown-info dropdown-menu-left">
						<li><a href="javascript:query('endPoint')">EndPoint视角</a></li>
						<li><a href="javascript:query('measurement')">Measure视角</a></li>
						<li><a href="javascript:query('')">组合视角</a></li>
					</ul></div>
				</th></tr>
			</table>
		</ul><!-- /.breadcrumb -->
	</div>
	
	<div class="page-content">
	<div class="page-content-area">
	<div class="row">
	<div class="col-xs-12">
	<div class="tabbable">
	<br>
	<div class="col-xs-12">
		<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
   			<div style="float:left;">
   				<div id="${item.id}" class="metricGraph" style="width:450px;height:350px;"></div>
   			</div>
		</c:forEach>
	</div>
	</div></div></div></div></div>
	
	<script type="text/javascript">
	function query(view) {
		var start = $("#startTime").val();
		var end = $("#endTime").val();
		
		window.location.href = "?op=graph&graphId=${payload.graphId}&view="+view+"&startDate=" + start + "&endDate=" + end; 
	}
	$(document).ready(
		function() {
			$('#startTime').datetimepicker({
				format:'Y-m-d H:i',
				step:30,
				maxDate:0
			});
			$('#endTime').datetimepicker({
				format:'Y-m-d H:i',
				step:30,
				maxDate:0
			});
			
			$('#startTime').val("${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm')}");
			$('#endTime').val("${w:format(payload.historyEndDate,'yyyy-MM-dd HH:mm')}");
			
			$('#serverChart').addClass('active open');
			$('#serverGraph').addClass('active');

			<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
				var data = ${item.jsonString};
				graphMetricChart(document.getElementById('${item.id}'), data);
			</c:forEach>
		});	
	</script>
	
</a:serverBody>