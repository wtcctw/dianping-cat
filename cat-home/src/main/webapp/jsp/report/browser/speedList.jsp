<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.report.page.browser.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.browser.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.browser.Model" scope="request" />

<a:web_body>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#Web_report').addClass('active open');
			$('#web_speed').addClass('active');
			
		//custom autocomplete (category selection)
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
		<c:forEach var="speed" items="${model.speeds}">
			var item = {};
			item['label'] = '${speed.value.id}:${speed.key}';
			item['category'] ="pages";
			data.push(item);
		</c:forEach>
		
		$("#speeds").catcomplete({
			delay: 0,
			source: data
		}); 
		
	});	
		
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
	
 	function query() {
		var speeds = $("#speeds").val();
		var pageId = speeds.split(":")[0];
		var query1 = getDate() + ";" + pageId + ";1;;;;;"  ;
		
		var href = "?op=speed&query1=" + query1;
		window.location.href = href;
	} 
	</script>
	<table align="center">
	<tr><th>
	<div class="input-group" style="float:left;">
	<span class="input-group-addon">页面名称</span>
	<span class="input-icon" style="width:250px;">
	<input type="text" placeholder="" class="search-input search-input form-control ui-autocomplete-input" id="speeds" autocomplete="on" data=""/>
	<i class="ace-icon fa fa-search nav-search-icon"></i>
	</span>
	</div>
	 <input class="btn btn-primary btn-sm"
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" />		
	</th>
			</tr>
	</table>
</a:web_body>