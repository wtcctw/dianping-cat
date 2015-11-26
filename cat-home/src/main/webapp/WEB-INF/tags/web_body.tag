<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<a:base>
	<script  type="text/javascript">
		$(document).ready(function() {
			$("#nav_browser").addClass("disabled");
		});
	</script>
	<div class="main-container" id="main-container">
		<script type="text/javascript">
			try{ace.settings.check('main-container' , 'fixed')}catch(e){}
		</script>
		<div id="sidebar" class="sidebar   responsive">
			<script type="text/javascript">
				try{ace.settings.check('sidebar' , 'fixed')}catch(e){}
			</script>
			<ul class="nav nav-list" style="top: 0px;">
				<li id="Web_report" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-globe"></i> <span class="menu-text">Web</span>
						<b class="arrow fa fa-angle-down"></b>
				</a> <b class="arrow"></b>
					<ul class="submenu">
						<li id="web_trend"><a href="/cat/r/browser">
							<i class="menu-icon fa fa-caret-right"></i>URL访问趋势</a>
							<b class="arrow"></b></li>
						<li id="web_piechart"><a href="/cat/r/browser?op=piechart">
							<i class="menu-icon fa fa-caret-right"></i>URL访问分布</a>
							<b class="arrow"></b></li>
						<li id="web_speed"><a href="/cat/r/browser?op=speed">
							<i class="menu-icon fa fa-caret-right"></i>Web页面测速</a>
							<b class="arrow"></b></li>
						<li id="web_speedGraph"><a href="/cat/r/browser?op=speedGraph">
							<i class="menu-icon fa fa-caret-right"></i>Web测速分布</a>
							<b class="arrow"></b></li>
						<li id="web_problem"><a href="/cat/r/browser?op=jsError">
							<i class="menu-icon fa fa-caret-right"></i>JS错误日志</a>
							<b class="arrow"></b></li>
						<li id="hive_track"><a href="http://mobile-tracer-web01.nh/" target="_blank">
							<i class="menu-icon fa fa-caret-right"></i>跟踪日志</a>
							<b class="arrow"></b></li>
					</ul>
				</li>
				<li id="Web_config" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-cogs"></i> <span class="menu-text">Config</span>
						<b class="arrow fa fa-angle-down"></b>
				</a> <b class="arrow"></b>
					<ul class="submenu">
						<li id="urlPatternConfigUpdate"><a href="/cat/s/web?op=urlPatternConfigUpdate" style="display:none">
								<i class="menu-icon fa fa-caret-right"></i>Web全局配置</a>
								<b class="arrow"></b></li>
						<li id="code"><a href="/cat/s/web?op=codeList">
								<i class="menu-icon fa fa-caret-right"></i>返回码配置</a>
								<b class="arrow"></b></li>
						<li id="urlPatterns"><a href="/cat/s/web?op=urlPatterns">
								<i class="menu-icon fa fa-caret-right"></i>URL配置</a>
								<b class="arrow"></b></li>
						<li id="webRule"><a href="/cat/s/web?op=webRule">
								<i class="menu-icon fa fa-caret-right"></i>Web告警</a>
								<b class="arrow"></b></li>
						<li id="jsRule"><a href="/cat/s/web?op=jsRuleList">
								<i class="menu-icon fa fa-caret-right"></i>JS告警</a>
								<b class="arrow"></b></li>
						<li id="speed"><a href="/cat/s/web?op=speed">
								<i class="menu-icon fa fa-caret-right"></i>测速配置</a>
								<b class="arrow"></b></li>
						<li id="webConstants"><a href="/cat/s/web?op=webConstants">
								<i class="menu-icon fa fa-caret-right"></i>常量配置</a>
								<b class="arrow"></b></li>
					</ul>
				</li>
				<li id="Web_documents" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-book"></i> <span class="menu-text">Documents</span>
						<b class="arrow fa fa-angle-down"></b>
				</a> <b class="arrow"></b>
					<ul class="submenu">
						<li id="browserMonitorButton"><a href="/cat/r/home?op=webView&docName=browserMonitor">
								<i class="menu-icon fa fa-caret-right"></i>用户侧监控</a>
								<b class="arrow"></b></li>
						<li id="webInterfaceButton"><a href="/cat/r/home?op=webView&docName=webInterface">
								<i class="menu-icon fa fa-caret-right"></i>接口文档</a>
								<b class="arrow"></b></li>
						<li id="webAlertButton"><a href="/cat/r/home?op=webView&docName=webAlert">
								<i class="menu-icon fa fa-caret-right"></i>告警文档</a>
								<b class="arrow"></b></li>
					</ul>
				</li>
			</ul>
			<!-- #section:basics/sidebar.layout.minimize -->
			<div class="sidebar-toggle sidebar-collapse" id="sidebar-collapse">
				<i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left" data-icon2="ace-icon fa fa-angle-double-right"></i>
			</div>

			<!-- /section:basics/sidebar.layout.minimize -->
			<script type="text/javascript">
				try{ace.settings.check('sidebar' , 'collapsed')}catch(e){}
			</script>
		</div>
		<div class="main-content">
				<div id="dialog-message" class="hide">
				<p>
					你确定要删除吗？(不可恢复)
				</p>
			</div>
				<div style="padding-top:2px;padding-left:2px;padding-right:8px;">
				<jsp:doBody/>
				</div>
		</div>
	</div>
</a:base>
