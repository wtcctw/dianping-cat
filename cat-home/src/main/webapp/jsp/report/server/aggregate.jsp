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
	
	<div class="navbar-header pull-left position" style="width:350px;">
		<form id="wrap_search" style="margin-bottom:0px;">
		<div class="input-group">
		<input id="search" type="text" class="search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off"/>
		<span class="input-group-btn">
			<button class="btn btn-sm btn-primary" type="button" id="search_go">
				Go
			</button> 
		</span>
		</div>
		</form>
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
				<c:forEach var="item" items="${model.productLines}">
							var item = {};
							item['label'] = '${item}';
							item['category'] = '产品线';
							data.push(item);
				</c:forEach>
						
				$( "#search" ).catcomplete({
					delay: 0,
					source: data
				});
				
				$("#search_go").bind("click",function(e){
					query();
				});
				$('#wrap_search').submit(
					function(){
						query();
						return false;
					}		
				);
				
				
			});		
	</script>
	
</a:serverBody>