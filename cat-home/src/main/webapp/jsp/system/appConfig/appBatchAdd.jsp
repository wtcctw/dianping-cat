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
			var name = $("#commandName").val()
			var title = $("#commandTitle").val();
			var domain = $("#commandDomain").val();
			var id = $("#commandId").val();
			var threshold = $("#threshold").val();
			var namespace = $("#commandNamespace").val();
			
			if(name == undefined || name == ""){
				if($("#errorMessage").length == 0){
					$("#commandName").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
				}
				return;
			}
			if(${payload.id} <= 0) {
				$.ajax({
					async: false,
					type: "get",
					dataType: "json",
					url: "/cat/s/app?op=appNameCheck&name="+name,
					success : function(response, textStatus) {
						if(response['isNameUnique']){
							if(title==undefined){
								title = "";
							}
							if(domain==undefined){
								domain="";
							}
							if(id==undefined){
								id="";
							}
							
							window.location.href = "/cat/s/app?op=appBatchSubmit&name="+name+"&title="+title+"&domain="+domain+"&id=-1"+"&type=${payload.type}&threshold="+threshold+"&namespace="+namespace;
						}else{
							alert("该名称已存在，请修改名称！");
						}
					}
				});
			}else{
				if(title==undefined){
					title = "";
				}
				if(domain==undefined){
					domain="";
				}
				if(id==undefined){
					id="";
				}
				window.location.href = "/cat/s/app?op=appBatchSubmit&name="+name+"&title="+title+"&domain="+domain+"&id="+id+"&type=${payload.type}&threshold="+threshold+"&namespace="+namespace;
			}
		})
	</script>
	
	<table class="table table-striped table-condensed table-bordered table-hover">
		<tr>
			<td>名称</td><td><textarea  id="commandName" class="form-control" id="form-field-8" placeholder="Default Text"></textarea></td>
		</td>
		</tr>
		<tr>
			<td>App</td><td><input name="namespace" value="${model.updateCommand.namespace}" id="commandNamespace" /><span class="text-danger">&nbsp;&nbsp;命令字归属于哪个App</span><br/>
			</td>
		</tr>
		<tr>
			<td>项目名</td><td><input name="domain" value="${model.updateCommand.domain}" id="commandDomain" /><span class="text-danger">&nbsp;&nbsp;后续配置在这个规则的告警，会根据此项目名查找需要发送告警的联系人信息(告警人信息来源CMDB)</span><br/>
			</td>
		</tr>
		<tr><td>默认过滤时间</td><td><input name="threshold" value="${model.updateCommand.threshold}" id="threshold" /><span class="text-danger">（支持数字）</span><br/>
			</td>
		</tr>
		<c:if test="${payload.id gt 0}">
			<input name="id" value="${payload.id}" id="commandId" style="display:none"/>
		</c:if>
		<tr>
			<td colspan="2" style="text-align:center;"><button class="btn btn-primary btn-sm" id="updateSubmit">提交</button></td>
		</tr>
	</table>

</a:mobile>
