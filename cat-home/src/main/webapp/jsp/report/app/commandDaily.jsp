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
<table width="100%">
			<tr>
				<th>
				<div class="input-group" style="float:left;width:120px">
	              <span class="input-group-addon">开始日期</span>
	              <input type="text" id="time" style="width:100px"/>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              <span class="input-group-addon">结束</span>
	              <input type="text" id="time2" style="width:100px"/>
	            </div>
				<div class="input-group" style="float:left;width:350px">
					<span class="input-group-addon">命令字</span>
		            <form id="wrap_search" style="margin-bottom:0px;">
						<span class="input-icon" style="width:350px;">
							<input type="text" placeholder="" class="search-input search-input form-control ui-autocomplete-input" id="command" autocomplete="on" data=""/>
							<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
					</form>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">返回码</span>
					<select id="code" style="width:120px"><option value=''>All</option></select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">网络类型</span>
					<select id="network">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.networks}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
				</select>
	            </div>
				</th>
				</tr>
			<tr>
				<th align=left>
				<div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">版本</span>
					<select id="version" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.versions}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">连接类型</span>
					<select id="connectionType" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.connectionTypes}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">平台</span>
					<select id="platform" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.platforms}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">地区</span>
					<select id="city" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.cities}" varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <div class="input-group" style="float:left;width:120px">
	              	<span class="input-group-addon">运营商</span>
					<select id="operator" style="width: 100px;">
						<option value=''>All</option>
						<c:forEach var="item" items="${model.operators}"
							varStatus="status">
							<option value='${item.value.id}'>${item.value.value}</option>
						</c:forEach>
					</select>
	            </div>
	            <input class="btn btn-primary btn-sm"
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()" type="submit" /> 
				</th>
			</tr>
		</table>
	<div style="margin-left: 30px;margin-top: 10px">
			<label class="btn btn-info btn-sm"><input type="radio"
				name="typeCheckbox" value="request">请求数
			</label><label class="btn btn-info btn-sm"> <input type="radio"
				name="typeCheckbox" value="success">成功率
			</label><label class="btn btn-info btn-sm">  <input type="radio"
				name="typeCheckbox" value="delay">成功延时
			</label>
		</div>
	<div style="float: left; width: 100%;margin-top: 40px">
		<div id="lineChart"></div>
	</div>
<script>
var commandsMap = ${model.commandsJson};
var commandInfo = ${model.command2CodesJson};
var globalInfo = ${model.globalCodesJson};

var queryCodeByCommand = function queryCode(commandId){
	var value = commandInfo[commandId];
	var command = commandsMap[commandId];
	var globalcodes = globalInfo[command.namespace].codes;
	var result = {};
	
	for(var tmp in globalcodes){
		result[globalcodes[tmp].id] =globalcodes[tmp].name;
	}
	
	for (var prop in value) {
		result[value[prop].id] =value[prop].value;
	}
	
	return result;
}

var commandChange = function commandChange(commandDom, codeDom) {
		var command = $("#"+commandDom).val().split('|')[0];
		var cmd = ${model.command2IdJson}[command];
		
		if(typeof(cmd)!="undefined"){
		var commandId = cmd.id;
		var value = queryCodeByCommand(commandId);
		
		$("#"+codeDom).empty();
		
		var opt = $('<option />');
		opt.html("All");
		opt.val("");
		opt.appendTo($("#"+codeDom));
		
		for ( var prop in value) {
			var opt = $('<option />');
			opt.html(value[prop]);
			opt.val(prop);
			opt.appendTo($("#"+codeDom));
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

function getDateBefore(n) {
	var now = new Date();
	var myDate = new Date(now.getTime() - n * 24 * 60 * 60 * 1000);
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
	var time2 = $("#time2").val();
	var command = $("#command").val().split('|')[0];
	var code = $("#code").val();
	var network = "";
	var version = "";
	var connectionType = "";
	var platform = "";
	var city = "";
	var operator = "";
	if(typeof(networkCode) == "undefined"){
		network = $("#network").val();
	}else{
		network = networkCode;
	}
	if(typeof(appVersionCode) == "undefined"){
		version = $("#version").val();
	}else{
		version = appVersionCode;
	}
	if(typeof(channelCode) == "undefined"){
		connectionType = $("#connectionType").val();
	}else{
		connectionType = channelCode;
	}
	if(typeof(platformCode) == "undefined"){
		platform = $("#platform").val();
	}else{
		platform = platformCode;
	}
	if(typeof(cityCode) == "undefined"){
		city = $("#city").val();
	}else{
		city = cityCode;
	}
	if(typeof(operatorCode) == "undefined"){
		operator = $("#operator").val();
	}else{
		operator = operatorCode;
	}
	var split = ";";
	var checkboxs = document.getElementsByName("typeCheckbox");
	var type = "";

	for (var i = 0; i < checkboxs.length; i++) {
		if (checkboxs[i].checked) {
			type = checkboxs[i].value;
			break;
		}
	}
	var commandId = ${model.command2IdJson}[command].id;
	var query1 = time + split + time2 + split + commandId + split + code + split
			+ network + split + version + split + connectionType
			+ split + platform + split + city + split + operator;
	var command = $('#command').val();
	window.location.href = "?op=commandDaily&query1=" + query1 + "&commandId=" + command + "&type=" + type ;
}

$(document).ready(
		function() {
			$('#apiDaily').addClass('active');
			$('#time').datetimepicker({
				timepicker:false,
				format:'Y-m-d',
				maxDate:0
			});
			$('#time2').datetimepicker({
				timepicker:false,
				format:'Y-m-d',
				maxDate:0
			});

			var query1 = '${payload.query1}';
			var command1 = $('#command');
			var words = query1.split(";");
			
			if(words[0] == null || words.length == 1){
				$("#time").val(getDateBefore(7));
			} else {
				$("#time").val(words[0]);
			}
			
			if(words[1] == null || words.length == 1){
				$("#time2").val(getDate());
			} else {
				$("#time2").val(words[1]);
			}
			
			command1.on('change', commandChange("command","code"));

			if(typeof(words[2]) != 'undefined' && words[2].length > 0){
				$("#command").val('${payload.commandId}');
			}else{
				$("#command").val('${model.defaultCommand}');
			}
			
			commandChange("command","code");
			
			$("#code").val(words[3]);
			$("#network").val(words[4]);
			$("#version").val(words[5]);
			$("#connectionType").val(words[6]);
			$("#platform").val(words[7]);
			$("#city").val(words[8]);
			$("#operator").val(words[9]);

			var checkboxs = document.getElementsByName("typeCheckbox");

			for (var i = 0; i < checkboxs.length; i++) {
				if (checkboxs[i].value == "${payload.type}") {
					checkboxs[i].checked = true;
					break;
				}
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
							item['label'] = '${command.value.name}|${command.value.title}';
							if('${command.value.domain}'.length >0 ){
								item['category'] ='${command.value.domain}';
							}else{
								item['category'] ='未知项目';
							}
							
							data.push(item);
				</c:forEach>
						
				$( "#command" ).catcomplete({
					delay: 0,
					source: data
				});
				$('#command').blur(function(){
					commandChange("command","code");
				})
				$('#wrap_search').submit(
					function(){
						commandChange("command","code");
						return false;
					}		
				);		
		 	var data = ${model.lineChart.jsonString};
			graphLineChart(document.getElementById('lineChart'), data);  
		});

</script>
</a:mobile>