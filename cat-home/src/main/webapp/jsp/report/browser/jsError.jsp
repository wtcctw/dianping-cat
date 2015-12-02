<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>

<jsp:useBean id="ctx" type="com.dianping.cat.report.page.browser.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.browser.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.browser.Model" scope="request"/>
<a:web_body>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
	<script type="text/javascript" src="/cat/js/baseGraph.js"></script>

<style type="text/css">
.graph {
	width: 500px;
	height: 300px;
	margin: 4px auto;
}
</style> 
<table>
	<tr>
			<th>
				<div class="input-group" style="float:left;">
	              <span class="input-group-addon">开始</span>
	              <input type="text" id="time" style="width:130px"/>
	            </div>
				<div class="input-group" style="float:left;width:60px">
	              <span class="input-group-addon">结束</span>
        	      <input type="text" id="time2" style="width:60px;"/></div>
	             <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">level</span>
					<select id="level" style="width:100px">
						<option value=''>ALL</option>
						<c:forEach var="level" items="${model.jsErrorDisplayInfo.levels}">
							<option value="${level}">${level}</option>
						</c:forEach>
					</select>
	            </div>
	         <!--    <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">browser</span>
					<select id="browser" style="width:100px">
						<option value=''>All</option>
					</select>
	            </div> -->
	            <div class="input-group" style="float:left;">
					<span class="input-group-addon">模块</span>
						<span class="input-icon" style="width:250px;">
							<input type="text" placeholder="" class="search-input search-input form-control ui-autocomplete-input" id="module" autocomplete="on" data=""/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
	            </div>
	            <div class="input-group" style="float:left;">
					<span class="input-group-addon">Dpid</span>
					<input type="text"  id="dpid"/>
	            </div>
	              <input class="btn btn-primary btn-sm"
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" />
			</th>
			</tr>
	</table>
	<table class="table table-hover table-striped table-condensed"  style="width:100%">
	<tr>
		<th width="30%">Msg</th>
		<th width="5%">Count</th>
		<th width="55%">SampleLinks</th>
	</tr>
	<tr>
		<td><strong>Total</strong></td>
		<td class="right">${w:format(model.jsErrorDisplayInfo.totalCount,'#,###,###,###,##0')}&nbsp;</td>
		<td></td>
	</tr>
	<c:forEach var="error" items="${model.jsErrorDisplayInfo.errors}" varStatus="index">
	<tr>
		<td>${error.msg}</td>
		<td  class="right">${w:format(error.count,'#,###,###,###,##0')}&nbsp;</td>
		<td >
			<c:forEach var="id" items="${error.ids}" varStatus="linkIndex">
				<a href="/cat/r/browser?op=jsErrorDetail&id=${id}">${linkIndex.first?'L':(linkIndex.last?'g':'o')}</a>
			</c:forEach>
		</td>
	</tr>
	</c:forEach>
</table>
 <table align="center">
	<tr>
		<td><h5 style="text-align:center"  class='text-center text-info'>错误分布</h5>
		<div id="distributionChart" class="graph"></div></td>
	</tr>
	<tr><td  style="display:none">
		<div id ="distributionChartMeta">${model.jsErrorDisplayInfo.distributionChart}</div>
		</td>
	</tr>
</table> 
<script type="text/javascript">
$(document).ready(
	function() {
		$('#Web_report').addClass('active open');
		$('#web_problem').addClass('active');
		$('#time').datetimepicker({
			format:'Y-m-d H:i',
			step:30,
			maxDate:0
		});
		$('#time2').datetimepicker({
			datepicker:false,
			format:'H:i',
			step:30,
			maxDate:0
		});
		
		var startTime = '${payload.jsErrorQuery.startTime}';
		if (startTime == null || startTime.length == 0) {
			$("#time").val(getDate());
		} else {
			$("#time").val('${payload.jsErrorQuery.day} ' + startTime);
		}
		
		var endTime = '${payload.jsErrorQuery.endTime}';
		if(endTime == null || endTime.length == 0){
			$("#time2").val(getTime());
		}else{
			$("#time2").val(endTime);
		}
		
		var level = '${payload.jsErrorQuery.level}';
		if(level != null && level.length != 0) {
			$("#level").val(level);
		}
		
		var module = '${payload.jsErrorQuery.module}';
		if(module != null && module.length != 0) {
			$("#module").val(module);
		}
		
		var dpid = '${payload.jsErrorQuery.dpid}';
		if(dpid != null && dpid.length != 0) {
			$("#dpid").val(dpid);
		}
		 
		//custom autocomplete (category selection)
		$.widget( "custom.catcomplete", $.ui.autocomplete, {
			_renderMenu: function( ul, items ) {
				var that = this,
				currentCategory = "";
				$.each( items, function( index, item ) {
					that._renderItemData( ul, item );
				});
			}
		});
		
		var data = [];
		<c:forEach var="module" items="${model.jsErrorDisplayInfo.modules}">
			var item = {};
			item['label'] = '${module}';
			data.push(item);
		</c:forEach>
		
		$("#module").catcomplete({
			delay: 0,
			source: data
		});
		
		var distributionChart = ${model.jsErrorDisplayInfo.distributionChart};

		if(distributionChart!=null){
			graphPieChart(document.getElementById('distributionChart'), distributionChart);
		}	 
});

function query() {
	var time = $("#time").val();
	var times = time.split(" ");
	var period = times[0];
	var start = converTimeFormat(times[1]);
	var end = converTimeFormat($("#time2").val());
	var level = $("#level").val();
	var module = $("#module").val();
	var dpid = $("#dpid").val();
	
	var href = "?op=jsError&jsErrorQuery.day=" + period + "&jsErrorQuery.startTime=" +start + "&jsErrorQuery.endTime=" + end +
			"&jsErrorQuery.level=" + level + "&jsErrorQuery.module=" + module + "&jsErrorQuery.dpid=" + dpid;
	window.location.href = href;
}

function getDate() {
	var myDate = new Date();
	var myMonth = new Number(myDate.getMonth());
	var month = myMonth + 1;
	var day = myDate.getDate();
	
	if(month<10){
		month = '0' + month;
	}
	if(day<10){
		day = '0' + day;
	}
	
	var myHour = new Number(myDate.getHours());
	
	if(myHour < 10){
		myHour = '0' + myHour;
	}
	
	return myDate.getFullYear() + "-" + month + "-" + day + " " + myHour + ":00";
}

function getTime(){
	var myDate = new Date();
	var myHour = new Number(myDate.getHours());
	var myMinute = new Number(myDate.getMinutes());
	
	if(myHour < 10){
		myHour = '0' + myHour;
	}
	if(myMinute < 10){
		myMinute = '0' + myMinute;
	}
	return myHour + ":" + myMinute;
}

function converTimeFormat(time){
	var times = time.split(":");
	var hour = times[0];
	var minute = times[1];
	
	if(hour.length == 1){
		hour = "0" + hour;
	}
	if(minute.length == 1) {
		minute = "0" + minute;
	}
	return hour + ":" + minute;
}
</script>
</a:web_body>