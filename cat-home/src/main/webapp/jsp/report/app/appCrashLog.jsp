<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.app.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.app.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.app.Model" scope="request" />
<a:mobile>
	<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
	<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>

	<div class="report">
		<c:set var="navUrlPrefix" value="op=${payload.action.name}&query1=${payload.query1}"/> 
		<table class="table ">
		<tr>
		<td colspan="2">
				<div class="input-group" style="float:left;">
	              <span class="input-group-addon">开始</span>
	              <input type="text" id="time" style="width:130px"/>
	            </div>
				<div class="input-group" style="float:left;width:60px">
	              <span class="input-group-addon">结束</span>
        	      <input type="text" id="time2" style="width:60px;"/></div>
				 <div class="input-group" style="float:left;width:60px">
	              <span class="input-group-addon">平台</span>  
				<select id="platform" style="width: 80px;height:33px">
					<option value='-1'>ALL</option>
					<option value='1'>Android</option>
					<option value='2'>IOS</option>
				</select></div>
				  <div class="input-group" style="float:left;width:60px">
	              <span class="input-group-addon">APP Name</span>  
				<select id="appName" style="width: 200px; height:33px">
						<c:forEach var="appName" items="${model.crashLogDisplayInfo.appNames}">
							<option value="${appName.title}">${appName.title}</option>
						</c:forEach>
				</select></div>
					&nbsp;&nbsp;&nbsp;<input class="btn btn-primary btn-sm "
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" /></td></tr>
					<tr><td width="100px;">APP版本</td><td>
						<div>
						<label class="btn btn-info btn-sm">
		    				<input type="checkbox" id="appVersionAll" onclick="clickAll('${model.crashLogDisplayInfo.fieldsInfo.appVersions}', 'appVersion')" unchecked>All
		  				</label><c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.appVersions}" varStatus="status"><label class="btn btn-info btn-sm"><input type="checkbox" id="appVersion_${item}" value="${item}" onclick="clickMe('${model.crashLogDisplayInfo.fieldsInfo.appVersions}', 'appVersion')" unchecked>${item}</label></c:forEach>
						</div>
						</td></tr>
					<tr><td width="60px;">平台版本</td><td><div><label class="btn btn-info btn-sm">
		    				<input type="checkbox" id="platformVersionAll" onclick="clickAll('${model.crashLogDisplayInfo.fieldsInfo.platVersions}', 'platformVersion')" unchecked>All
		  				</label><c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.platVersions}" varStatus="status"><label class="btn btn-info btn-sm"><input type="checkbox" id="platformVersion_${item}" value="${item}" onclick="clickMe('${model.crashLogDisplayInfo.fieldsInfo.platVersions}', 'platformVersion')" unchecked>${item}</label></c:forEach>
						</div>
						</td></tr>
					<tr><td width="60px;"> 模块</td><td><div>
						<label class="btn btn-info btn-sm">
		    				<input type="checkbox" id="moduleAll" onclick="clickAll('${model.crashLogDisplayInfo.fieldsInfo.modules}', 'module')" unchecked>All
		  				</label><c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.modules}" varStatus="status"><label class="btn btn-info btn-sm"><input type="checkbox" id="module_${item}" value="${item}" onclick="clickMe('${model.crashLogDisplayInfo.fieldsInfo.modules}', 'module')" unchecked>${item}</label></c:forEach>
						</div>
						</td></tr>
					<tr><td width="60px;"> 级别</td><td><div>
						<label class="btn btn-info btn-sm">
		    				<input type="checkbox" id="levelAll" onclick="clickAll('${model.crashLogDisplayInfo.fieldsInfo.levels}', 'level')"  unchecked>All
		  				</label><c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.levels}" varStatus="status"><label class="btn btn-info btn-sm"><input type="checkbox" id="level_${item}" value="${item}" onclick="clickMe('${model.crashLogDisplayInfo.fieldsInfo.levels}', 'level')" unchecked>${item}</label></c:forEach>
						</div>
						</td></tr>
					<tr><td width="60px;">设备</td><td><div>
						<label class="btn btn-info btn-sm">
		    				<input type="checkbox" id="deviceAll" onclick="clickAll('${model.crashLogDisplayInfo.fieldsInfo.devices}', 'level')"  unchecked>All
		  				</label><c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.devices}" varStatus="status"><label class="btn btn-info btn-sm"><input type="checkbox" id="level_${item}" value="${item}" onclick="clickMe('${model.crashLogDisplayInfo.fieldsInfo.devices}', 'device')" unchecked>${item}</label></c:forEach>
						</div>
						</td></tr>
	</table>
	</div>
	<br>
	<table class="table table-hover table-striped table-condensed"  style="width:100%">
	<tr>
		<th width="30%">Msg</th>
		<th width="5%">Count</th>
		<th width="55%">SampleLinks</th>
	</tr>
	<tr>
		<td><strong>Total</strong></td>
		<td class="right">${w:format(model.crashLogDisplayInfo.totalCount,'#,###,###,###,##0')}&nbsp;</td>
		<td></td>
	</tr>
	<c:forEach var="error" items="${model.crashLogDisplayInfo.errors}" varStatus="index">
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

</a:mobile>

<script type="text/javascript">
	function query(){
		var appName = $("#appName").val();
		var platform = $("#platform").val();
		var appVersion = queryField('${model.crashLogDisplayInfo.fieldsInfo.appVersions}','appVersion');
		var platVersion = queryField('${model.crashLogDisplayInfo.fieldsInfo.platVersions}','platformVersion');
		var module = queryField('${model.crashLogDisplayInfo.fieldsInfo.modules}','module');
		var level = queryField('${model.crashLogDisplayInfo.fieldsInfo.levels}','level');
		var device = queryField('${model.crashLogDisplayInfo.fieldsInfo.devices}','device');
		var split = ";";
		var query = plat + split + appVersion + split + platVersion + split + module + split + level;
		window.location.href = "?op=${payload.action.name}&query1=" + query + "&step=${payload.step}";
	}
	
	function clickMe(fields, prefix) {
		var fs = [];
		if(fields != "[]") {
			fs = fields.replace(/[\[\]]/g,'').split(', ');
		}
		
		var num = 0;
		for( var i=0; i<fs.length; i++){
		 	var f = prefix + "_" + fs[i];
			if(document.getElementById(f).checked){
				num ++;
			}else{
				document.getElementById(prefix + "All").checked = false;
				break;
			} 
		}
		if(num > 0 && num == fs.length) {
			document.getElementById(prefix + "All").checked = true;
		}
	}
	
	function clickAll(fields, prefix) {
		var fs = [];
		if(fields.length > 0){
			fs = fields.replace(/[\[\]]/g,'').split(', ');
			for( var i=0; i<fs.length; i++){
			 	var f = prefix + "_" + fs[i];
			 	if(document.getElementById(f) != undefined) {
					document.getElementById(f).checked = document.getElementById(prefix + "All").checked;
			 	}
			}
		}
	}
	
	function queryField(fields, prefix){
		var fs = [];
		if(fields.length > 0) {
			fs = fields.replace(/[\[\]]/g,'').split(', ');
		}
		
		var url = '';
		var num = 0;
		if(document.getElementById(prefix + "All").checked == false && fs.length > 0) {
			for( var i=0; i<fs.length; i++){
			 	var f = prefix + "_" + fs[i];
				if(document.getElementById(f) != undefined 
						&& document.getElementById(f).checked){
					url += fs[i] + ":";
				} 
			}
			url = url.substring(0, url.length-1);
		}else{
			url = "";
		}
		return url;
	}
	
	function docReady(field, fields, prefix){
		var urls = [];
		
		if(typeof field == "undefined" || field.length == 0){
			document.getElementById(prefix + "All").checked = true;
			clickAll(fields, prefix);
		}else{
			urls = field.split(":");
			for(var i=0; i<urls.length; i++) {
				if(document.getElementById(prefix + "_" + urls[i]) != null) {
					document.getElementById(prefix + "_" + urls[i]).checked = true;
				}
			}
		}
	}
	
	$("#platformType")
	  .change(function () {
		  window.location.href = "?op=${payload.action.name}&query1=" + this.value + ";;;;&date=${model.date}&reportType=${payload.reportType}";
	  })
	  
	$(document).ready(
		function() {
			$('#crashLog').addClass('active');
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
			
			var startTime = '${payload.crashLogQuery.startTime}';
			if (startTime == null || startTime.length == 0) {
				$("#time").val(getDate());
			} else {
				$("#time").val('${payload.crashLogQuery.day} ' + startTime);
			}
			
			var endTime = '${payload.crashLogQuery.endTime}';
			if (endTime == null || endTime.length == 0){
				$("#time2").val(getTime());
			}else{
				$("#time2").val(endTime);
			}
			
			var appName = '${payload.crashLogQuery.appName}';
			if (appName != null && appName.length != 0) {
				$("#appName").val(appName);
			}
			
			var platform = '${payload.crashLogQuery.platform}';
			if (platform != null && platform.length != 0) {
				$("#platform").val(platform);
			}
			
			var fields = "${payload.query1}".split(";");
			if("${payload.query1}".length > 0) {
				$("#platformType").val(fields[0]);
			}
			docReady(fields[1], '${model.crashLogDisplayInfo.fieldsInfo.appVersions}','appVersion');
			docReady(fields[2], '${model.crashLogDisplayInfo.fieldsInfo.platVersions}','platformVersion');
			docReady(fields[3], '${model.crashLogDisplayInfo.fieldsInfo.modules}','module');
			docReady(fields[4], '${model.crashLogDisplayInfo.fieldsInfo.levels}','level');
			docReady(fields[4], '${model.crashLogDisplayInfo.fieldsInfo.devices}','device');

			
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

<style type="text/css">
	.row-fluid .span2 {
		width:10%;
	}
	.row-fluid .span10 {
		width:87%;
	}
	.report .btn-group {
		position: relative;
		display: inline-block;
		font-size: 0;
		white-space: normal;
		vertical-align: middle;
	}
</style>