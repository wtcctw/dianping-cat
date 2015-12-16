<%@ page contentType="text/html; charset=utf-8" isELIgnored="false"
	trimDirectiveWhitespaces="true"%>

<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.eslog.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.eslog.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.eslog.Model" scope="request" />

<a:mobile>
	<script type="text/javascript">
		function query() {
			var time = $("#time").val();
			var times = time.split(" ");
			var period = times[0];
			var start = converTimeFormat(times[1]);
			var end = $("#time2").val();
			var dpid = $("#dpid").val();
			var type=$("#logType").val();

			if (dpid == "" || dpid == undefined) {
				alert("请输入dpid");
				return;
			}
			var href = "?day=" + period + "&start=" + start + "&end=" + end
					+ "&dpid=" + dpid+"&type="+type;
			window.location.href = href;
		}

		function converTimeFormat(time) {
			var times = time.split(":");
			var hour = times[0];
			var minute = times[1];

			if (hour.length == 1) {
				hour = "0" + hour;
			}
			if (minute.length == 1) {
				minute = "0" + minute;
			}
			return hour + ":" + minute;
		}

		$(document).ready(function() {
			$('#App_report').addClass('open active');
			$('#eslog').addClass('active');
			$('#time').datetimepicker({
				format : 'Y-m-d H:i',
				step : 30,
				maxDate : 0
			});
			$('#time2').datetimepicker({
				datepicker : false,
				format : 'H:i',
				step : 30,
				maxDate : 0
			});
			$("#logType").val('${payload.type}');
		});
	</script>

	<table>
		<tr>
			<th>
				<div class="input-group" style="float: left; width: 60px"">
					<select class="" size="1" id="logType">
						<c:forEach var="item" items="${model.logTypes}" >
							<option value="${item}">${item}</option>
						</c:forEach>
					</select>
				</div>
				<div class="input-group" style="float: left;">
					<span class="input-group-addon">开始</span> <input type="text"
						id="time" style="width: 130px" value="${payload.start}" />
				</div>
				<div class="input-group" style="float: left; width: 60px">
					<span class="input-group-addon">结束</span> <input type="text"
						id="time2" style="width: 60px;" value="${payload.end}" />
				</div>
				</div>
				<div class="input-group" style="float: left;">
					<span class="input-group-addon">DPID</span> <input type="text"
						id="dpid" style="width: 260px;" value="${payload.dpid}" />
				</div>
				</div> <input class="btn btn-primary btn-sm"
				value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
				type="submit" />
			</th>
		</tr>
	</table>
	
	<br>
	<table>
		<c:forEach var="item" items="${model.logs}" >
			<tr><td>${item}</td></tr>
		</c:forEach>
	</table>
</a:mobile>

<style type="text/css">
.row-fluid .span2 {
	width: 10%;
}

.row-fluid .span10 {
	width: 87%;
}
</style>
