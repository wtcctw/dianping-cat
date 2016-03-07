<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.app.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.system.page.app.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.system.page.app.Model" scope="request"/>

<a:mobile>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#userMonitor_config').addClass('active open');
			$('#appList').addClass('active');
			
			var state = '${model.opState}';
			if(state=='Success'){
				$('#state').html('操作成功');
			}else{
				$('#state').html('操作失败');
			}
			setTimeout(function(){
				$('#state').html('&nbsp;');
			},3000);
			
			var type = "${payload.type}";
			
			if(typeof type != "undefined" && type.length > 0) {
				$('#tab-'+ type).addClass('active');
				$('#tabContent-'+ type).addClass('active');
			}else {
				$('#tab-api').addClass('active');
				$('#tabContent-api').addClass('active');
			}
			
			$("#tab-api-default").addClass('active');
			$("#tabContent-api-default").addClass('active');
			$("#tab-constant-版本").addClass('active');
			$("#tabContent-constant-版本").addClass('active');
			
			$(document).delegate('#updateSubmit', 'click', function(e){
				var name = $("#commandName").val();
				var title = $("#commandTitle").val();
				var domain = $("#commandDomain").val();
				var id = $("#commandId").val();
				
				if(name == undefined || name == ""){
					if($("#errorMessage").length == 0){
						$("#commandName").after($("<span class=\"text-danger\" id=\"errorMessage\">  该字段不能为空</span>"));
					}
					return;
				}
				if(title==undefined){
					title = "";
				}
				if(domain==undefined){
					domain="";
				}
				if(id==undefined){
					id="";
				}
				
				window.location.href = "/cat/s/app?op=appSubmit&name="+name+"&title="+title+"&domain="+domain+"&id="+id;
			})
 		});
	</script>
			<div class="tabbable" id="content"> <!-- Only required for left/right tabs -->
				<ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height:50px;" id="myTab">
				    <li id="tab-api" class="text-right"><a href="#tabContent-api" data-toggle="tab"> <strong>API命令字</strong></a></li>
				    <li id="tab-code" class="text-right"><a href="#tabContent-code" data-toggle="tab"> <strong>返回码</strong></a></li>
				    <li id="tab-constant" class="text-right"><a href="#tabContent-constant" data-toggle="tab"><strong>常量配置</strong></a></li>
				    <li id="tab-group" class="text-right"><a href="#tabContent-group" data-toggle="tab"><strong>数据归并</strong></a></li>
				</ul>
				<div class="tab-content">
					<div class="tab-pane" id="tabContent-api">
						<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
						
						  <ul class="nav nav-tabs padding-12 ">
						  	<c:forEach var="entry" items="${model.apiCommands}" varStatus="status">
							    <li id="tab-api-${entry.key}" class="text-right"><a href="#tabContent-api-${entry.key}" data-toggle="tab"> ${entry.key}</a></li>
							</c:forEach>
						  </ul>
						  <div class="tab-content">
						  	<c:forEach var="entry" items="${model.apiCommands}" varStatus="status">
							  	<div class="tab-pane" id="tabContent-api-${entry.key}">
								    <table class="table table-striped table-condensed table-bordered table-hover">
									    <thead><tr>
												<th width="30%">名称</th>
												<th width="32%">标题</th>
												<th width="10%">加入全量统计</th>
												<th width="10%">过滤阈值</th>
												<th width="8%">操作 <a href="?op=appUpdate&type=api&id=-1" class="btn btn-primary btn-xs" >
												<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
											</tr>
										</thead>
										
								    	<c:forEach var="command" items="${entry.value}">
									    	<tr><td>${command.name}</td>
											<td>${command.title}</td>
											<td class="center">
												<c:choose>
												<c:when test="${command.all}">
													<button class="btn btn-xs btn-success">
													<i class="ace-icon glyphicon glyphicon-ok bigger-120 btn-success"></i>
													</button>
												</c:when>
												<c:otherwise>
													<i class="ace-icon glyphicon glyphicon-remove bigger-120"></i>
												</c:otherwise>
												</c:choose>
											</td>
											<td>${command.threshold}</td>
											<c:if test="${command.id ne 0 }">
												<td><a href="?op=appUpdate&id=${command.id}&type=api" class="btn btn-primary btn-xs">
													<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
													<a href="?op=appPageDelete&id=${command.id}&type=api" class="btn btn-danger btn-xs delete" >
													<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
												
											</c:if></tr>
								    	</c:forEach>
								    </table>
							    </div>
							</c:forEach>
						  </div>
						</div>
						
					</div>
					<div class="tab-pane" id="tabContent-code">
						<%@include file="code.jsp"%>
					</div>
					<div class="tab-pane" id="tabContent-constant">
						<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
							  <ul class="nav nav-tabs padding-12 ">
							  	<c:forEach var="entry" items="${model.configItems}" varStatus="status">
								    <li id="tab-constant-${entry.key}" class="text-right"><a href="#tabContent-constant-${entry.key}" data-toggle="tab"> ${entry.key}</a></li>
								</c:forEach>
							  </ul>
							  <div class="tab-content">
							  	<c:forEach var="entry" items="${model.configItems}" varStatus="status">
								  	<div class="tab-pane" id="tabContent-constant-${entry.key}">
									    <table class="table table-striped table-condensed table-bordered table-hover">
										    <thead><tr>
													<th>ID</th>
													<th>值</th>
													<c:if test="${entry.key eq '版本'}">
														<th width="5%"><a href="?op=appConstantAdd&type=${entry.key}" class="btn btn-primary btn-xs" >
														<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
													</c:if>
													<c:if test="${entry.key eq 'APP类型'}">
													<th>desc</th>
												</c:if>
												</tr>
											</thead>
											
									    	<c:forEach var="e" items="${entry.value.items}">
										    	<tr><td>${e.value.id}</td>
												<td>${e.value.name}</td>
												<c:if test="${entry.key eq '版本'}">
													<td><a href="?op=appConstantUpdate&id=${e.key}&type=${entry.key}" class="btn btn-primary btn-xs">
														<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
													</td>
												</c:if>
												<c:if test="${entry.key eq 'APP类型'}">
													<td>${e.value.des}</td>
												</c:if>
									    	</c:forEach>
									    </table>
								    </div>
								</c:forEach>
							  </div>
							</div>
					</div>
					<div class="tab-pane" id="tabContent-group">
						<%@include file="appCommandGroup.jsp"%>
					</div>
				</div>
			</div>
</a:mobile>
