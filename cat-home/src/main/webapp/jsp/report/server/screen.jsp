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
	
	<div class="report">
			<div class="breadcrumbs" id="breadcrumbs">
			<span class="text-danger title">【报表时间】</span><span class="text-success">&nbsp;&nbsp;${w:format(model.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.endTime,'yyyy-MM-dd HH:mm:ss')}</span>
			<div class="nav-search nav" id="nav-search">
				<c:forEach var="nav" items="${model.navs}">
							&nbsp;[ <a href="${model.baseUri}?op=metric&date=${model.date}&domain=${model.domain}&step=${nav.hours}&timeRange=${payload.timeRange}&${navUrlPrefix}">${nav.title}</a> ]&nbsp;
						</c:forEach>
						&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}&op=metric&timeRange=${payload.timeRange}">now</a> ]&nbsp;
			</div></div>
			<div class="col-xs-12">
			<table width="100%">
				<tr>
					<th>
						<select id="productId" onchange="screenChange()">
							<c:forEach var="item" items="${model.screens}" varStatus="status">
								<option value="${item.id}">${item.title}</option>
							</c:forEach>
						</select>
					</th>
					<th>时间段 
						<c:forEach var="range" items="${model.allRange}">
							<c:choose>
								<c:when test="${payload.timeRange eq range.duration}">
									&nbsp;&nbsp;&nbsp;[ <a href="?op=metric&date=${model.date}&domain=${model.domain}&timeRange=${range.duration}" class="current">${range.title}</a> ]
								</c:when>
								<c:otherwise>
									&nbsp;&nbsp;&nbsp;[ <a href="?op=metric&date=${model.date}&domain=${model.domain}&timeRange=${range.duration}">${range.title}</a> ]
								</c:otherwise>
								</c:choose>
						</c:forEach>
					</th>
				</tr>
			</table>
			</div>
	      	<div class="col-xs-12">
		        	<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
		       			<div style="float:left;">
		       				<div id="${item.id}" class="metricGraph"></div>
		       			</div>
					</c:forEach>
			</div>
		</div>
	
	
	
 	<div>
       	<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
   			<div style="float:left;">
   				<div id="${item.id}" class="metricGraph"></div>
   			</div>
		</c:forEach>
	</div>
	
	
	<script type="text/javascript">
		function screenChange(){
			var date='${model.date}';
			var screen='${payload.screen}';
			var network=$("#search").val();
			var timeRange=${payload.timeRange};
			var href = "?op=screen&date="+date+"&domain=${payload.domain}&screen="+screen+"&timeRange="+timeRange;
			window.location.href=href;
		}

		$(document).ready(
			function() {
				$('#serverChart').addClass('active open');
				$('#serverScreen').addClass('active');
				
				<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
					var data = ${item.jsonString};
					graphMetricChart(document.getElementById('${item.id}'), data);
				</c:forEach>
			});		
	</script>
	
</a:serverBody>