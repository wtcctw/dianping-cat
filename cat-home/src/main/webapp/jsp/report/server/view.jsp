<%@ page contentType="text/html; charset=utf-8" isELIgnored="false"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.server.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.server.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.server.Model"
	scope="request" />
<a:serverBody>
	<link rel="stylesheet" type="text/css"
		href="${model.webapp}/js/jquery.datetimepicker.css" />
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js" />
	<div class="breadcrumbs" id="breadcrumbs">
		<script type="text/javascript">
			try{ace.settings.check('breadcrumbs' , 'fixed')}catch(e){}
		</script>

		<ul class="breadcrumb">
		<table>
			<tr>
				<th>分类
				<select id="category" style="width: 150px">
					<c:forEach var="item" items="${model.serverMetricConfig.groups}" varStatus="status">
					  <option value="${item.key}">${item.key}</option>
					</c:forEach>
				</select>
				</th>
				<th>分组
					<select id="group" style="width: 100px">
					</select>
				</th>
				<th>EndPoint
					<input type="text" placeholder="input endPoint for search" value="${payload.endPoint}" id="endPoint" style="width: 200px">
				</th>
				<th class="left">
					<div style="float: left;">
						&nbsp;开始 <input type="text" id="startTime" style="width: 150px;" />
						结束 <input type="text" id="endTime" style="width: 150px;" />
					</div>
				</th>
				<th>&nbsp;<input class="btn btn-primary btn-sm "
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" /></th>
			</tr>
		</table>

		</ul><!-- /.breadcrumb -->
	</div>
	
	<div class="page-content">
	<div class="page-content-area">
	<div class="row">
	<div class="col-xs-12">
	<div class="tabbable">
	<br>
	<div>
		<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
			<div style="float: left;">
				<div id="${item.id}" class="metricGraph" style="width:450px;height:350px;"></div>
			</div>
		</c:forEach>
	</div></div></div></div></div></div>


	<script type="text/javascript">
		function query() {
			var start = $("#startTime").val();
			var end = $("#endTime").val();
			var category = $("#category").val();
			var group = $("#group").val();
			var endPoint = $("#endPoint").val();

			window.location.href = "?op=view&category=" + category +"&group="+ group +"&startDate=" 
					+ start + "&endDate=" + end +"&endPoint=" + endPoint;
		}
		
		function groupChange() {
			var category = $("#category").val();
			var group = ${model.serverMetricConfigJson}[category];
			
			$("#group").empty();
			
			var opt = $('<option />');
			opt.html("All");
			opt.val("");
			opt.appendTo($("#group"));
			
			for ( var prop in group.items) {
				var opt = $('<option />');
				opt.html(group.items[prop].id);
				opt.val(group.items[prop].id);
				opt.appendTo($("#group"));
			}
		}

		$(document).ready(function() {
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
			
			var category = '${payload.category}';
			
			if(category!=''){
				$("#category").val(category);
			}
			groupChange();
			$("#group").val("${payload.group}");
			$("#category").on('change',groupChange);
			
			$('#serverChart').addClass('active open');
			$('#view').addClass('active');

			<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
				var data = ${item.jsonString};
				graphMetricChart(document.getElementById('${item.id}'), data);
			</c:forEach>
		});
	</script>

</a:serverBody>