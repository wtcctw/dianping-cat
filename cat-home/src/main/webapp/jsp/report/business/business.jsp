<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.business.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.business.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.business.Model" scope="request" />

<a:application>
	<script type="text/javascript">
		function query() {
			var name = $("#search").val();
			var type = $("#type").val();
			var start = $("#startTime").val();
			var end = $("#endTime").val();
			
			window.location.href = "?name=" + name + "&type=" + type 
					+ "&startDate=" + start + "&endDate="
					+ end; 
		}
		
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
					$('#type').val('${payload.type}');
					$('#domain').val('${payload.name}');
					
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
					<c:forEach var="item" items="${model.domains}">
						var item = {};
						item['label'] = '${item}';
						item['category'] = '产品线';
						data.push(item);
					</c:forEach>
					
					<c:forEach var="item" items="${model.tags}">
						var item = {};
						item['label'] = '${item}';
						item['category'] = '标签';
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
					
					
					var domain = '${payload.name}';
					if(domain != ''){
						$('#search').val(domain);
					}
					
					var type = '${payload.type}';
					if(type != ''){
						$('#type').val(type);
					}
					
					<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
						var data = ${item.jsonString};
						graphMetricChart(document.getElementById('${item.id}'), data);
					</c:forEach>
				
			});
	</script>
		<table>
			<tr>
				<th class="left">
				<div style="float:left;">
						&nbsp;开始
					<input type="text" id="startTime" style="width:150px;"/>
						结束
						<input type="text" id="endTime" style="width:150px;"/></div>
		        &nbsp;查询类型<select style="width: 100px;" name="type" id="type" >
							<option value="domain">Domain</option>
							<option value="tag">Tag</option>
					
					</select>
					</th>
				<th>&nbsp;&nbsp;</th>
		        <th>
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
					</th>
			</tr>
		</table>
	 	<div>
        	<c:forEach var="item" items="${model.lineCharts}" varStatus="status">
       			<div style="float:left;">
       				<div id="${item.id}" class="metricGraph"></div>
       			</div>
			</c:forEach>
		</div>
</a:application>
