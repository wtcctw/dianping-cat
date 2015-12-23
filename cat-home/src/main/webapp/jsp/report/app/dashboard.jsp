<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request" />
<a:mobile>
	<script src="${model.webapp}/js/echarts-all.js"></script>
	<script src="${model.webapp}/js/baseGraph.js"></script>
	
	<table align="center">
	<tr> <th>
				<div class="input-group" style="float:left;">
	              <span class="input-group-addon">开始</span>
	              <input type="text" id="time" style="width:130px"/>
	            </div>
				<div class="input-group" style="float:left;width:60px">
	              <span class="input-group-addon">结束</span>
        	      <input type="text" id="time2" style="width:60px;"/></div>
				<div class="input-group" style="float:left;">
					<span class="input-group-addon">命令字</span>
		            <form id="wrap_search" style="margin-bottom:0px;">
						<span class="input-icon" style="width:350px;">
							<input type="text" placeholder="" class="search-input search-input form-control ui-autocomplete-input" id="command" autocomplete="on" data=""/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</form>
	            </div>   
	            <input class="btn btn-primary btn-sm"
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" />   </th> </tr> </table>
	<div style="height:10px"></div>
	<div style="height: 400px;width: 600px;float:left;margin-left:30px;"><div id="delay" style="height: 400px;width: 600px; "></div></div>
	<div style="height: 400px;width: 400px;margin-left:50px;float:left"><div id="operator"></div></div>
</a:mobile>
<script type="text/javascript">
function query() {
	var time = $("#time").val();
	var times = time.split(" ");
	var period = times[0];
	var start = converTimeFormat(times[1]);
	var end = converTimeFormat($("#time2").val());
	var command = $("#command").val().split('|')[0];
	var commandId = ${model.command2IdJson}[command].id;
	var split = ";";
	var query1 = period + split + commandId + split + split + split  + split + split  + split  + split  + split + start + split + end;
	
	var field = $("#piechartSelect").val();
	var href = "?op=dashboard&query1=" + query1 + "&commandId="+$("#command").val() ;
		window.location.href = href;
}

$(document).ready(
	function() {
		$('#Dashboard').addClass('active');
		$('#App_report').removeClass('active open');
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
		var query1 = '${payload.query1}';
		var words = query1.split(";");
		if (words[0] == null || words.length == 1) {
			$("#time").val(getDate());
		} else {
			$("#time").val(words[0] + " " + words[9]);
		}
		
		if(words[10] == null || words.length == 1){
			$("#time2").val(getTime());
		}else{
			$("#time2").val(words[10]);
		}
		
		if(typeof(words[1]) != 'undefined' && words[1].length > 0){
			$("#command").val('${payload.commandId}');
		}else{
			$("#command").val('all|all');
		}
		 $.widget( "custom.catcomplete", $.ui.autocomplete, {
				_renderMenu: function( ul, items ) {
					var that = this,
					currentCategory = "";
					$.each( items, function( index, item ) {
						if ( item.category != currentCategory ) {
							ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
							currentCategory = item.category;
						}
						that._renderItemData( ul, item );
					});
				}
			});

		
		var data = [];
		<c:forEach var="command" items="${model.commands}">
					var item = {};
					item['label'] = '${command.name}|${command.title}';
					if('${command.domain}'.length >0 ){
						item['category'] ='${command.domain}';
					}else{
						item['category'] ='未知项目';
					}
					data.push(item);
		</c:forEach>
				
		$( "#command" ).catcomplete({
			delay: 0,
			source: data
		});
		graphMapChart('delay', '${model.dashBoardInfo.mapChart.title}', '${model.dashBoardInfo.mapChart.subTitle}', 'delay', ${model.dashBoardInfo.mapChart.min}, ${model.dashBoardInfo.mapChart.max},  ${model.dashBoardInfo.mapChart.data});
		graphBarChart('#operator', '${model.dashBoardInfo.operatorChart.title}', '',
				${model.dashBoardInfo.operatorChart.xAxisJson}, '${model.dashBoardInfo.operatorChart.yAxis}',
				${model.dashBoardInfo.operatorChart.valuesJson}, '${model.dashBoardInfo.operatorChart.serieName}');

	});
	
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
