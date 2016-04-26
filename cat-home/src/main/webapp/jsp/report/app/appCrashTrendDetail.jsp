<%@ page contentType="text/html; charset=utf-8"%>
	<table>
		<tr>
			<th align=left>
				<div class="input-group" style="float:left;">
	              <span class="input-group-addon">开始</span>
	              <input type="text" id="startTime" style="width:130px"/>
	            </div>
				<div class="input-group" style="float:left;width:80px">
	              <span class="input-group-addon">结束</span>
        	      <input type="text" id="endTime" style="width:80px;"/></div>
				<div class="input-group" style="float:left;">
					<span class="input-group-addon">App Name</span>
					<select id="appName" style="width: 240px;">
						<c:forEach var="appName" items="${model.crashLogDisplayInfo.appNames}">
							<option value="${appName.value}">${appName.des}</option>
						</c:forEach>
					</select></div>
				<div class="input-group" style="float:left;">
					<span class="input-group-addon">App 版本</span>
					 <select id="appVersion" style="width: 240px;">
					 	<option value="">All</option>
					 	<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.appVersions}" varStatus="status">
					 		<option value="${item}">${item}</option>
					 	</c:forEach>
					 </select> 
				</div>
			</th>
		</tr>
		<tr>
			<th align=left>
			 <div class="input-group" style="float:left;">
			 	<span class="input-group-addon">平台版本</span>
					 <select id="platVersion" style="width: 100px;">
						<option value="">All</option>
						<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.platVersions}" varStatus="status">
					 		<option value="${item}">${item}</option>
					 	</c:forEach>
					</select>
				<span class="input-group-addon">模块</span>
				<select id="modules" style="width: 150px;">
					<option value="">All</option>
					<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.modules}" varStatus="status">
						<option value="${item}">${item}</option>
					</c:forEach>
				</select> 
				<span class="input-group-addon">级别</span>
				<select id="level" style="width: 100px;">
					<option value="">All</option>
					<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.levels}" varStatus="status">
						<option value="${item}">${item}</option>
					</c:forEach>
				</select>  
			<%-- 	<span class="input-group-addon">设备</span>
				<select multiple="false"	class="chosen-select tag-input-style" id="device" name="devices"  data-placeholder="Choose devices...">
					<option id="All" value="All">ALL</option>
					<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.devices}">
						<option id="${item}" value="${item}">${item}</option>
					</c:forEach>
				</select> --%>
				<input class="btn btn-primary btn-sm"
				value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
				type="submit" /> <input class="btn btn-primary" id="checkbox"
				onclick="check()" type="checkbox" /> <label for="checkbox"
				style="display: -webkit-inline-box">选择对比</label></div>
			</th>
		</tr>
	</table>
	<table id="history" style="display: none">
			<tr>
			<th align=left>
				<div class="input-group" style="float:left;">
	              <span class="input-group-addon">开始</span>
	              <input type="text" id="startTime2" style="width:130px"/>
	            </div>
				<div class="input-group" style="float:left;width:80px">
	              <span class="input-group-addon">结束</span>
        	      <input type="text" id="endTime2" style="width:80px;"/></div>
				<div class="input-group" style="float:left;">
					<span class="input-group-addon">App Name</span>
					<select id="appName2" style="width: 240px;">
						<c:forEach var="appName" items="${model.crashLogDisplayInfo.appNames}">
							<option value="${appName.value}">${appName.des}</option>
						</c:forEach>
					</select></div>
				<div class="input-group" style="float:left;">
					<span class="input-group-addon">App 版本</span>
					 <select id="appVersion2" style="width: 240px;">
					 	<option value="">All</option>
					 	<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.appVersions}" varStatus="status">
					 		<option value="${item}">${item}</option>
					 	</c:forEach>
					 </select> 
				</div>
			</th>
		</tr>
		<tr>
			<th align=left>
			 <div class="input-group" style="float:left;">
			 <span class="input-group-addon">平台版本</span>
				<select id="platVersion2" style="width: 100px;">
						<option value="">All</option>
						<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.platVersions}" varStatus="status">
					 		<option value="${item}">${item}</option>
					 	</c:forEach>
					</select>
				<span class="input-group-addon">模块</span>
				<select id="modules2" style="width: 150px;">
					<option value="">All</option>
					<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.modules}" varStatus="status">
						<option value="${item}">${item}</option>
					</c:forEach>
				</select> 
				<span class="input-group-addon">级别</span>
				<select id="level2" style="width: 100px;">
					<option value="">All</option>
					<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.levels}" varStatus="status">
						<option value="${item}">${item}</option>
					</c:forEach>
				</select>  
				<%-- <span class="input-group-addon">设备</span>
				<select multiple="false"	class="chosen-select tag-input-style" id="device2" name="devices"  data-placeholder="Choose devices...">
					<option id="All" value="All">ALL</option>
					<c:forEach var="item" items="${model.crashLogDisplayInfo.fieldsInfo.devices}">
						<option id="${item}" value="${item}">${item}</option>
					</c:forEach>
				</select> --%>
				</div>
			</th>
		</tr>
	</table>

	<div style="float: left; width: 100%;">
		<div id="${model.crashLogDisplayInfo.lineChart.id}"></div>
	</div>
