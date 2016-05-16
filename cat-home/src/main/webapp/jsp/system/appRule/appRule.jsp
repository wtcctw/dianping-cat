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
 	var commandChange = function commandChange() {
		var key = $("#command").val();
		var value = commandInfo[key];
		var code = document.getElementById("code");
		for ( var prop in value) {
			var opt = $('<option />');

			opt.html(value[prop].name);
			opt.val(value[prop].id);
			opt.appendTo(code);
		}
	}
		$(document).ready(function() {
			$('#userMonitor_config').addClass('active open');
			$('#appRule').addClass('active');
 		});
	</script>
			<table class="table table-striped table-condensed table-bordered  table-hover" id="contents" width="100%">
			<thead>
				<tr >
					<th width="15%">告警名</th>
					<th width="15%">命令字</th>
					<th width="10%">返回码</th>
					<th width="6%">网络类型</th>
					<th width="6%">版本</th>
					<th width="6%">连接类型</th>
					<th width="6%">平台</th>
					<th width="6%">地区</th>
					<th width="6%">运营商</th>
					<th width="6%">告警指标</th>
					<th width="8%">操作 <a href="?op=appRuleUpdate" class="btn btn-primary btn-xs" >
						<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
				</tr></thead><tbody>

				<c:forEach var="entry" items="${model.ruleInfos}" varStatus="status">
				 	<c:forEach var="item" items="${entry.value}">
					<c:set var="command" value="${item.rule.dynamicAttributes['command']}" />
					<c:set var="code" value="${item.rule.dynamicAttributes['code']}" />
					<c:set var="network" value="${item.rule.dynamicAttributes['网络类型']}" />
					<c:set var="version" value="${item.rule.dynamicAttributes['版本']}" />
					<c:set var="connectType" value="${item.rule.dynamicAttributes['连接类型']}" />
					<c:set var="platform" value="${item.rule.dynamicAttributes['平台']}" />
					<c:set var="city" value="${item.rule.dynamicAttributes['城市']}" />
					<c:set var="operator" value="${item.rule.dynamicAttributes['运营商']}" />
					<c:set var="type" value="${item.rule.dynamicAttributes['metric']}" />
					<c:set var="name" value="${item.rule.id}" />
					<tr class="">
						<td>${name}</td>
						<c:choose>
							<c:when test="${command ne -1}">
							<td>
							<c:forEach var="i" items="${model.commands}">
							<c:if test="${i.value.id eq command}">${i.value.name}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${code ne -1}">
							<td>
							<c:forEach var="i" items="${model.command}">
								<c:if test="${i.key eq command}">
								<c:forEach var="i2" items="${i.value}">
									<c:if test="${i2.id eq code}">${i2.name}</c:if>
								</c:forEach>
								</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${network ne -1}">
							<td>
							<c:forEach var="i" items="${model.networks}">
							<c:if test="${i.value.id eq network}">${i.value.value}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${version ne -1}">
							<td>
							<c:forEach var="i" items="${model.versions}">
							<c:if test="${i.value.id eq version}">${i.value.value}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${connectType ne -1}">
							<td>
							<c:forEach var="i" items="${model.connectionTypes}">
							<c:if test="${i.value.id eq connectType}">${i.value.value}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${platform ne -1}">
							<td>
							<c:forEach var="i" items="${model.platforms}">
							<c:if test="${i.value.id eq platform}">${i.value.value}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${city ne -1}">
							<td>
							<c:forEach var="i" items="${model.cities}">
							<c:if test="${i.value.id eq city}">${i.value.value}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${operator ne -1}">
							<td>
							<c:forEach var="i" items="${model.operators}">
							<c:if test="${i.value.id eq operator}">${i.value.value}</c:if>  
							</c:forEach>
							</td>
							</c:when>
						<c:otherwise>
							<td>All</td>
						</c:otherwise>
						</c:choose>
						<td>
							<c:if test="${type eq 'request'}">请求数</c:if> 
							<c:if test="${type eq 'success'}">成功率</c:if>  
							<c:if test="${type eq 'delay'}">响应时间</c:if>
						</td>
						<td><a href="?op=appRuleUpdate&id=${item.entity.id}" class="btn btn-primary btn-xs">
						<i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
						<a href="?op=appRuleDelete&id=${item.entity.id}" class="btn btn-danger btn-xs delete" >
						<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
					</tr>
					</c:forEach>
				</c:forEach>
				</tbody>
			</table>
</a:mobile>
