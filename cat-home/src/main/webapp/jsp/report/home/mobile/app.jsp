<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-danger">1、APP端到端使用说明&nbsp;&nbsp;&nbsp;&nbsp; <a href="/cat/r/app?domain=cat&ip=All&reportType=&op=view">访问链接</a></h4>
<p>监控点评APP的接口调用情况，这个是从用户手机APP采集的数据，从用户角度看点评接口的访问速度。</p>
<p>监控的分析的维度有返回码、网络类型、APP版本、平台、地区、运营商等。</p>
</br>

<h5 class="text-danger">Sample1：Api访问趋势示例图如下方所示：</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor01.png"/>
<br/><br/>

<pre>
图中上方筛选区域。具体的筛选选项详解如下：

<span class="text-danger">开始/结束：</span>查询端到端监控的时间纬度。每条数据来自于SDK中上传的时间戳。

<span class="text-danger">命令字：</span> 1) SDK上传具体监控对象，一般是每条url请求的path最后部分，h5加载监控的命令字是完整的url字符串。
	2) 命令字可以配置别名，别名在页面左侧的Config/App监控/API命令字中进行配置。
	3) 为了便于查找，命令字输入框中加入了模糊匹配辅助功能，输入命令字的一部分可以快速提示匹配的命令字搜索列表。
	4) all表示全部的命令字，如果有些命令字不希望加入all统计中，可以在Config/App监控/API命令中进行修改，将是否加入全量统计的选择框选择为否。

<span class="text-danger">返回码：</span> 1) API请求的返回码，数据来自SDK中的上报。
	2) 返回码可以配置别名，别名在页面左侧的Config/App监控/返回码中进行配置。
	3) 需要注意的是，返回码配置中可以配置返回码代表的是成功还是失败，目前除了200表示成功以外，还有450 451等返回码被配置为表示成功。
	
<span class="text-danger">网络类型：</span>API请求时的网络状况，数据来自SDK中的上报。目前网络类型包括：ALL/WIFI/2G/3G/4G/UNKNOWN

<span class="text-danger">版本：</span>APP的版本号，SDK中自动为每条记录增加APP的版本号。

<span class="text-danger">连接类型：</span>网络请求走的连接通道，数据来自SDK中的上报。目前连接类型包括：ALL/短连接/长连接/UDP连接/WNS连接/HTTPS连接

<span class="text-danger">平台：</span>客户端类型。SDK中自动为每条记录增加平台代码。目前平台包括：ALL/android/ios/Unknown

<span class="text-danger">地区：</span>API请求发生时，客户端所在的城市或地区。数据是大众点评内部的服务根据IP反查出来的，反查IP的数据库来自腾讯，准确率99%

<span class="text-danger">运营商：</span> 1) API请求发生时，客户端网络所使用的运营商。与地区类似，运营商数据也是根据IP反查出来的。
	2) 目前运营商包括：ALL/中国移动/中国联通/中国电信/中国铁通/其他/国外其他/教育网
	
当点击<span class="text-danger">查询</span>按钮时，cat网页会根据筛选选项进行数据查询和图表绘制。

<span class="text-danger">选择对比</span>复选框是网络监控的一个高级功能，用于两种查询的对比展示。

图表上方有<span class="text-danger">请求数/成功率/成功延时</span>的三个切换按钮，用于绘制按照三种数据得到的图表。

图标下方是数据汇总，其中<span class="text-danger">成功率、总请求数和成功平均延迟</span>是三项重要指标。
</pre><br/><br/>

<h5 class="text-danger">Sample2：下图显示可以按照不同维度展开的OLAP功能，下图按照运营商维度展开，看不同接口的访问情况。</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor03.png"/>

<br/><br/>

<h4 class="text-danger">2、APP端到端配置&nbsp;&nbsp;&nbsp;&nbsp; <a href="/cat/s/app?op=appList">访问链接</a> </h4>
<p>用户可以在该界面对Command命令字进行修改操作。</p>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor05.png"/>
<br/><br/>
<pre>
1) 单个添加
   <span class="text-danger">名称：</span>命令字（如：shop.bin，api.baymax.adp.meituan.com, http://bddeal.meishi.sankuai.com/m/list等）
   <span class="text-danger">App: </span>命令字属于哪个App
   <span class="text-danger">项目名：</span>对应的服务项目名，会根据此项目名查找需要发送告警的联系人信息(告警人信息来源CMDB)
   <span class="text-danger">标题：</span>方便记住的标题名字
   <span class="text-danger">默认过滤时间：</span>响应时间超过该阈值的访问数据不予统计
2) 批量
   <span class="text-danger">名称：</span>输入格式（命令字名称1|命令字标题1;命令字名称2|命令字标题2;...）
   	不设置标题默认展示命令字名称，所以格式也可为（命令字名称1;命令字名称2;...）
</pre>
<br/><br/>
<h4 class="text-danger">3、APP端到端告警&nbsp;&nbsp;&nbsp;&nbsp;<a href="/cat/s/config?op=appRule">访问链接</a></h4>
<h5 class="text-success">A) 配置一览表</h5>
<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor06.png"/>
<h5 class="text-success">B) 配置告警规则</h5>
<p>（1）告警名自定义，方便区分告警项。可对<span class="text-danger">请求数、访问成功率、响应时间</span>进行监控。</p>
<p>（2）告警维度：可以根据每个维度进行选择，可以选择<span class="text-danger">All（某一维度的所有聚合），*（某一维度任意条件，如地区中*是对所有地区分别进行告警），具体条件（如地区维度中选择上海市）</span></p>
<p>（3）多个监控规则构成了告警的主体，分别对不同时间段进行配置，以方便准确地进行告警。</p>
<p>（4）监控规则诠释着某个时间段内如何进行告警，由任意多个监控条件组成。任何一条监控条件触发都会引起监控规则触发，从而告警。</p>
<p>（5）监控条件诠释着什么条件会触发监控规则，由任意多个监控子条件组成。当所有子条件同时被触发时，才会触发该监控规则。</p>

<img  class="img-polaroid"  width='80%'  src="${model.webapp}/images/userMonitor/userMonitor07.png"/>



