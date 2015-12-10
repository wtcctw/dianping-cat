<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<a:base>
	<script  type="text/javascript">
		$(document).ready(function() {
			$("#nav_mobile").addClass("disabled");
		});
	</script>
	<div class="main-container" id="main-container">
			<script type="text/javascript">
				try{ace.settings.check('main-container' , 'fixed')}catch(e){}
			</script>
			<!-- #section:basics/sidebar -->
			<div id="sidebar" class="sidebar   responsive">
				<script type="text/javascript">
					try{ace.settings.check('sidebar' , 'fixed')}catch(e){}
				</script>
				<ul class="nav nav-list" style="top: 0px;">
					<li id="App_report" class="hsub open"><a href="#" class="dropdown-toggle"> <i class="menu-icon glyphicon glyphicon-phone"></i> <span class="menu-text">App</span>
							<b class="arrow fa fa-angle-down"></b>
					</a> <b class="arrow"></b>
						<ul class="submenu">
							<li id="trend"><a href="/cat/r/app?op=view&showActivity=false&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>API访问趋势</a>
								<b class="arrow"></b></li>
							<li id="accessPiechart"><a href="/cat/r/app?op=piechart&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>访问量分布</a>
								<b class="arrow"></b></li>
							<li id="speed"><a href="/cat/r/app?op=speed&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>APP页面测速</a>
								<b class="arrow"></b></li>
							<li id="statistics"><a href="/cat/r/app?op=statistics&domain=${model.domain}&type=all">
								<i class="menu-icon fa fa-caret-right"></i>每天报表统计</a>
								<b class="arrow"></b></li>
							<li id="connTrend"><a href="/cat/r/app?op=connLinechart&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>长连访问趋势</a>
								<b class="arrow"></b></li>
							<li id="connPiechart"><a href="/cat/r/app?op=connPiechart&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>长连访问分布</a>
								<b class="arrow"></b></li>
							<li id="crashLog"><a href="/cat/r/app?op=crashLog&domain=${model.domain}">
								<i class="menu-icon fa fa-caret-right"></i>Crash日志</a>
								<b class="arrow"></b></li>
							<li id="crashLog"><a href="/cat/r/app?op=appCrashLog">
								<i class="menu-icon fa fa-caret-right"></i>Crash日志(新)</a>
								<b class="arrow"></b></li>
							<li id="traceLog"><a href="http://mobile-tracer-web01.nh/" target="_blank">
								<i class="menu-icon fa fa-caret-right"></i>dpid跟踪日志</a>
								<b class="arrow"></b></li>
							<li id="eslog"><a href="/cat/r/eslog" target="_blank">
								<i class="menu-icon fa fa-caret-right"></i>dpid实时日志</a>
								<b class="arrow"></b></li>
							<li id="esConfig" style="display:none"><a href="/cat/r/eslog?op=config" target="_blank">
								<i class="menu-icon fa fa-caret-right"></i>es日志配置</a>
								<b class="arrow"></b></li>
						</ul>
					</li>
				</ul>
				<!-- /section:basics/sidebar.layout.minimize -->
				<script type="text/javascript">
					try{ace.settings.check('sidebar' , 'collapsed')}catch(e){}
				</script>
			</div>
				
			<!-- /section:basics/sidebar -->
			<div class="main-content">
 				<div id="dialog-message" class="hide">
					<p>
						你确定要删除吗？(不可恢复)
					</p>
				</div>
 				<div style="padding-top:2px;padding-right:8px;">
 				<jsp:doBody/>
 				</div>
			</div>
		</div></a:base>
		<script  type="text/javascript">
	$(document).ready(function() {
		$("#tab_realtime").click(function(){
			window.location.href = "/cat/r/t?";
		});
		$("#tab_offtime").click(function(){
			window.location.href = "/cat/r/statistics?op=service";
		});
		$("#tab_document").click(function(){
			window.location.href = "/cat/r/home?";
		});
		$("#tab_config").click(function(){
			window.location.href = "/cat/s/config?op=projects";
		});});
		$("#tab_config").addClass("disabled");
</script>
