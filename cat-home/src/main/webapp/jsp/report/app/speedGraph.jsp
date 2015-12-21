<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix='fmt' uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request" />

<a:mobile>
		<table>
		<tr>
			<th align=left>
					<div class="input-group" style="float:left;">
						<span class="input-group-addon">日期</span>
					<input type="text" id="time" style="width:110px;"/>
					</div>
					<div class="input-group" style="float:left;">
					<span class="input-group-addon">页面</span>
					<select id="page" style="width: 240px;">
					<c:forEach var="item" items="${model.appSpeedDisplayInfo.pages}" varStatus="status">
							<option value='${item}'>${item}</option>
					</c:forEach>
					</select></div>
					<div class="input-group" style="float:left;">
					<span class="input-group-addon">阶段</span>
					 <select id="step" style="width: 240px;">
					</select> <span class="input-group-addon">网络类型</span>
					 <select id="network" style="width: 80px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.networks}" varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select></div>
			</th>
		</tr>
		<tr>
			<th align=left>
			 <div class="input-group" style="float:left;">
					<span class="input-group-addon">版本</span><select id="version" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.versions}" varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select>  <span class="input-group-addon">平台</span><select id="platform" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.platforms}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select>  <span class="input-group-addon">地区</span><select id="city" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.cities}" varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select>  <span class="input-group-addon">运营商</span><select id="operator" style="width: 100px;">
					<option value=''>All</option>
					<c:forEach var="item" items="${model.operators}"
						varStatus="status">
						<option value='${item.value.id}'>${item.value.name}</option>
					</c:forEach>
			</select> <input class="btn btn-primary btn-sm"
				value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
				type="submit" /> </div>
			</th>
		</tr>
	</table>
	<table width="100%">
	<tr><td colspan='3'><div id="cityChart" style="height: 400px"></div></td></tr>
	<tr>
		<td><div id="operatorChart" style="width:40%; height: 400px"></div></td>
		<td><div id="versionChart" style="width:40%; height: 400px"></div></td>
	</tr>
	<tr>
		<td><div id="platformChart" style="height: 400px"></div></td>
		<td><div id="networkChart" style="height: 400px"></div></td>
	</tr>
</table>
<c:forEach var="entry" items="${model.appSpeedDisplayInfo.appSpeedBarDetails}" >
<table class="table table-striped table-condensed table-bordered table-hover"> 
	<tr>
		<th class="text-success" width="40%">${entry.key}</th>
		<th class="text-success" width="30%">访问量</th>
		<th class="text-success" width="30%">平均响应时间</th>
	</tr>
	 <c:forEach var="item" items="${entry.value}">
		<tr><td>${item.itemName}</td><td>${item.accessNumberSum}</td><td>${item.responseTimeAvg}</td></tr>
	</c:forEach> 
</table>
</c:forEach>	
	<script type="text/javascript">
		var page2Steps = ${model.appSpeedDisplayInfo.page2StepsJson};
		
		function changeStepByPage(){
			var page = "";
			var stepSelect;
			
			if($(this).attr("id")=="page") {
				page = $("#page").val();
				stepSelect = $("#step");
			}else {
				page = $("#page2").val();
				stepSelect = $("#step2");
			}
			var steps = page2Steps[page];
			stepSelect.empty();
			
			for(var s in steps){
				var step = steps[s];
				if(step['title'] != undefined && step['title'].length > 0){
					stepSelect.append($("<option value='"+step['id']+"'>"+step['title']+"</option>"));
				}else{
					stepSelect.append($("<option value='"+step['id']+"'>"+step['step']+"</option>"));
				}
			}
		}
		
		function getDate() {
			var myDate = new Date();
			var myMonth = new Number(myDate.getMonth());
			var month = myMonth + 1;
			var day = myDate.getDate();

			if (month < 10) {
				month = '0' + month;
			}
			if (day < 10) {
				day = '0' + day;
			}

			return myDate.getFullYear() + "-" + month + "-" + day;
		}

		function query() {
			var time = $("#time").val();
			var page = $("#page").val();
			var step = $("#step").val();
			var network = $("#network").val();
			var version = $("#version").val();
			var platform = $("#platform").val();
			var city = $("#city").val();
			var operator = $("#operator").val();
			var split = ";";
			var query1 = time + split + page + split + step + split + network
					+ split + version + split + platform + split + city + split
					+ operator;

			window.location.href = "?op=speedGraph&query1=" + query1;
		}

		$(document).ready(
			function() {
				$('#speedGraph').addClass('active');
				$('#time').datetimepicker({
					format:'Y-m-d',
					timepicker:false,
					maxDate:0
				});
				$('#time2').datetimepicker({
					format:'Y-m-d',
					timepicker:false,
					maxDate:0
				});

				var query1 = '${payload.query1}';
				var words = query1.split(";");

				$("#page").on('change', changeStepByPage);

				if (typeof (words[0]) != "undefined"
						&& words[0].length == 0) {
					$("#time").val(getDate());
				} else {
					$("#time").val(words[0]);
				}
				if(typeof words[1] != "undefined"  && words[1].length > 0) {
					$("#page").val(words[1]);
				}
				$("#page").change();
				if(typeof words[2] != "undefined"  && words[2].length > 0) {
					$("#step").val(words[2]);
				}
				$("#network").val(words[3]);
				$("#version").val(words[4]);
				$("#platform").val(words[5]);
				$("#city").val(words[6]);
				$("#operator").val(words[7]);
				
				graphBarChart('#cityChart', '${model.appSpeedDisplayInfo.cityChart.title}', '',
						${model.appSpeedDisplayInfo.cityChart.xAxisJson}, '${model.appSpeedDisplayInfo.cityChart.yAxis}',
						${model.appSpeedDisplayInfo.cityChart.valuesJson}, '${model.appSpeedDisplayInfo.cityChart.serieName}');
				
				graphBarChart('#operatorChart', '${model.appSpeedDisplayInfo.operatorChart.title}', '',
						${model.appSpeedDisplayInfo.operatorChart.xAxisJson}, '${model.appSpeedDisplayInfo.operatorChart.yAxis}',
						${model.appSpeedDisplayInfo.operatorChart.valuesJson}, '${model.appSpeedDisplayInfo.operatorChart.serieName}');

				graphBarChart('#versionChart', '${model.appSpeedDisplayInfo.versionChart.title}', '',
						${model.appSpeedDisplayInfo.versionChart.xAxisJson}, '${model.appSpeedDisplayInfo.versionChart.yAxis}',
						${model.appSpeedDisplayInfo.versionChart.valuesJson}, '${model.appSpeedDisplayInfo.versionChart.serieName}');

				graphBarChart('#platformChart', '${model.appSpeedDisplayInfo.platformChart.title}', '',
						${model.appSpeedDisplayInfo.platformChart.xAxisJson}, '${model.appSpeedDisplayInfo.platformChart.yAxis}',
						${model.appSpeedDisplayInfo.platformChart.valuesJson}, '${model.appSpeedDisplayInfo.platformChart.serieName}');

				graphBarChart('#networkChart', '${model.appSpeedDisplayInfo.networkChart.title}', '',
						${model.appSpeedDisplayInfo.networkChart.xAxisJson}, '${model.appSpeedDisplayInfo.networkChart.yAxis}',
						${model.appSpeedDisplayInfo.networkChart.valuesJson}, '${model.appSpeedDisplayInfo.networkChart.serieName}');

			});
	</script>

</a:mobile>

<style type="text/css">
.row-fluid .span2 {
	width: 10%;
}

.row-fluid .span10 {
	width: 87%;
}
</style>