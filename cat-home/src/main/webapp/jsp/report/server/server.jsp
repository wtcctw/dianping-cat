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
	
	<table>
			<tr>
				<th class="left">
				<div style="float:left;">
					&nbsp;开始
					<input type="text" id="startTime" style="width:150px;"/>
						结束
					<input type="text" id="endTime" style="width:150px;"/></div>
				</th>
			</tr>
		</table>
	
	<script type="text/javascript">
			
	$(document).ready(
		function() {
			$('#startTime').datetimepicker({
				format:'Y-m-d H:i',
				step:30,
				maxDate:0
			});
			$('#endTime').datetimepicker({
				format:'Y-m-d H:i',
				step:30,
				maxDate:0
			});
			
			$('#startTime').val("${w:format(model.startTime,'yyyy-MM-dd HH:mm')}");
			$('#endTime').val("${w:format(model.endTime,'yyyy-MM-dd HH:mm')}");
			$('#domain').val('${payload.domain}');
			
			$('#serverChart').addClass('active open');
			$('#serverGraph').addClass('active');
			
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
		});		
	</script>
	
</a:serverBody>