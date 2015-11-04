<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>

<a:base>
	<div class="main-container" id="main-container">
		<script type="text/javascript">
			try{ace.settings.check('main-container', 'fixed')}catch(e){}
		</script>
		<div id="sidebar" class="sidebar responsive">
			<script type="text/javascript">
				try{ace.settings.check('sidebar' , 'fixed')}catch(e){}
			</script>
			<ul class="nav nav-list" style="top: 0px;">
				<li id="serverChart" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-desktop"></i> <span class="menu-text">Servers</span>
						<b class="arrow fa fa-angle-down"></b>
				</a> <b class="arrow"></b>
					<ul class="submenu">
						<li id="serverScreen"><a href="/cat/r/server?domain=${model.domain}&op=screen">
							<i class="menu-icon fa fa-caret-right"></i>系统大盘</a>
							<b class="arrow"></b>
						</li>
						<li id="serverGraph"><a href="/cat/r/server?domain=${model.domain}">
							<i class="menu-icon fa fa-caret-right"></i>系统指标</a>
							<b class="arrow"></b>
						</li>
					</ul>
				</li>
				<li id="serverConfig" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-cogs"></i> <span class="menu-text">Config</span>
						<b class="arrow fa fa-angle-down"></b>
				</a> <b class="arrow"></b>
					<ul class="submenu">
						<li id="screen"><a href="/cat/r/server?op=configUpdate">
								<i class="menu-icon fa fa-caret-right"></i>Screen配置</a>
								<b class="arrow"></b>
						</li>
					</ul>
				</li>
				<li id="serverDocuments" class="hsub"><a href="#" class="dropdown-toggle"> <i class="menu-icon fa fa-book"></i> <span class="menu-text">Documents</span>
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
