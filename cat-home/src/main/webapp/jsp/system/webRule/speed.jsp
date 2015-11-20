<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.web.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.web.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.web.Model" scope="request" />

<a:web_body>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#Web_config').addClass('active open');
			$('#speed').addClass('active');
		});
	</script>
 	<div class="tabbable tabs-left" id="content"><!-- Only required for left/right tabs -->
		<ul class="nav nav-tabs padding-12 ">
			<c:forEach var="entry" items="${model.speeds}"
				varStatus="status">
				<li id="tab-api-${entry.key}" class="text-right"><a
					href="#tabContent-api-${entry.key}" data-toggle="tab">
						${entry.key}</a></li>
			</c:forEach>
		</ul>
		<div class="tab-content">
			<c:forEach var="entry" items="${model.speeds}"
				varStatus="status">
				<div class="tab-pane" id="tabContent-api-${entry.key}">
					<table
						class="table table-striped table-condensed table-bordered table-hover">
						<thead>
							<tr>
							<th colspan='2'>
								<a href="?op=speedUpdate" class="btn btn-primary btn-xs"> <i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a>
								<a href="?op=speedUpdate&page=${entry.key}" class="btn btn-primary btn-xs">
									 <i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a> 
									<a href="?op=speedDelete&page=${entry.key}" class="btn btn-danger btn-xs delete">
									<i class="ace-icon fa fa-trash-o bigger-120"></i></a>
								</th>
							</tr>
							<tr>
								<th width="30%">测速点编号</th>
								<th width="32%">名称</th>
							</tr>
						</thead>

						<c:forEach var="step" items="${entry.value.steps}">
							<tr>
								<td>${step.key}</td>
								<td>${step.value.title}</td>
							</tr>
						</c:forEach>
					</table>
				</div>
			</c:forEach>
		</div>
	</div> 
</a:web_body>