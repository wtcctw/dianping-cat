<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-danger">用户端监控文档</h4>
<h5 class="text-info"> a).从用户端角度来看点评的业务接口状态，这是一个端到端的监控，能最早的发现用户端出现问题，比如根本访问不到点评，某城市延迟很大等。</h5>
<h5 class="text-info"> b).用户端的监控目前能监控Ajax接口，页面Page不能监控到。</h5>
<h5 class="text-info"> c).一般一个应用会监控1-2个重要接口，后端实时分析会按照城市、运营商维度做一些聚合分析。</h5>

<br/>
<h4>外部监控API文档</h4> 
<p>用途：提供外部监控的Http接口，用于监控用户端的错误信息。</p>
<p>1、为了保留以后的扩展性，移动端和Web端的暂定用不同的API接口。</p>
<p class="text-danger">2、公网IP，221.181.67.144 文档后面{ip}使用这个。</p>

<br/>
<h4 class="text-danger">Web单次接口</h4>
	<pre>	http://{ip}/web-broker-service/api/ajax</pre>
	
	<p>参数说明</p>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>query名</th><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>version</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>ts</td><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>tu</td><td>targetUrl</td><td>调用的URL或API</td><td>String</td></tr>
		<tr><td>d</td><td>duration</td><td>访问耗时</td><td>long 毫秒</td></tr>
		<tr><td>c</td><td>code</td><td>返回结果码</td><td>整型</td></tr>
		<tr><td>s</td><td>requestByte</td><td>发送字节数</td><td>整型，以byte为单位</td></tr>
		<tr><td>r</td><td>responseByte</td><td>返回字节数</td><td>整型，以byte为单位</td></tr>
		<tr><td>n</td><td>network</td><td>网络类型</td><td>整型, 2G,3G,4G,WIFI(iOS只有3G和WIFI)，1=wifi, 2=2G, 3=3G, 4=4G, 0=Unknown</td></tr>
	</table>

 code表
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>code名称</th><th>code含义</th></tr>	
		<tr><td>-100</td><td>如果当前没有连接，不能连接到网络</td></tr>
		<tr><td>-107</td><td>回传的数据格式出错</td></tr>
		<tr><td>-901</td><td>当数据发送之后，500ms 之内没有收到 header</td></tr>
		<tr><td>-902</td><td>当收到header 和 httpstatus 之后，500ms 之内没有开始下载</td></tr>
		<tr><td>-903</td><td>当开始 loading 之后，500ms 之后仍然没有传送完毕</td></tr>
		<tr><td>-904</td><td>实际上不会出现这个 code，因为 readyState 置 4 之后就成功了</td></tr>
		<tr><td>-905</td><td>响应体的类型不符，比如 JSON.parse 失败</td></tr>
		<tr><td>-910</td><td>业务超时，当业务代码中设置了 timeout 以后，触发了超时</td></tr>
		<tr><td>-911</td><td>当业务代码中触发了 cancel 方法后，触发的 ajax 取消，有可能由业务逻辑所致，而不是错误。</td></tr>
		<tr><td>>0</td><td>业务 code</td></tr>
	</table>
<br/>

<br/>
<h4 class="text-danger">JS 错误接口</h4>
	<pre>	http://{ip}/web-broker-service/api/js</pre>
	
	<p>参数说明</p>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>query名</th><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>version</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>t</td><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>msg</td><td>message</td><td>错误的类型,简要信息</td><td>String</td></tr>
		<tr><td>n</td><td>appName</td><td>错误的发生的应用模块</td><td>String</td></tr>
		<tr><td>l</td><td>level</td><td>错误等级</td><td>String,包括ERROR,WARN,INFO,DEV</td></tr>
		<tr><td>a</td><td>agent</td><td>浏览器信息</td><td>String</td></tr>
		<tr><td>id</td><td>dpid</td><td>用户ID，用于搜索错误日志</td><td>String</td></tr>
		<tr><td>data</td><td>data</td><td>详细出错信息</td><td>String，如果没有的话，传空串</td></tr>
	</table>
	<br/>

<h4 class="text-danger">web访问批量接口</h4>
	<pre>	http://{ip}/web-broker-service/api/log</pre>
	
	批量接口POST内容，前面加上v=1&c=，不同请求之间用回车<span class="text-danger">ENTER</span>分隔，字段之间用<span class="text-danger">TAB</span>分隔。

	<pre>
	timstamp<span class="text-danger">TAB</span>level<span class="text-danger">TAB</span>requestId<span class="text-danger">TAB</span>appName<span class="text-danger">TAB</span>url<span class="text-danger">TAB</span>message<span class="text-danger">ENTER</span>
	
	sample如下:
	
	v=1&c=
	1400037748182<span class="text-danger">TAB</span>ERROR<span class="text-danger">TAB</span>11233333<span class="text-danger">TAB</span>shopInfo<span class="text-danger">TAB</span>url1<span class="text-danger">TAB</span>test1<span class="text-danger">ENTER</span>
	1400037748182<span class="text-danger">TAB</span>INFO<span class="text-danger">TAB</span>22339283<span class="text-danger">TAB</span>shopInfo<span class="text-danger">TAB</span>url2<span class="text-danger">TAB</span>test2<span class="text-danger">ENTER</span>
	1400037748182<span class="text-danger">TAB</span>WARN<span class="text-danger">TAB</span>13456664<span class="text-danger">TAB</span>shopInfo<span class="text-danger">TAB</span>url3<span class="text-danger">TAB</span>test3<span class="text-danger">ENTER</span>
	</pre>
	<p class="text-danger">v=1&c=不需要做urlencode,后面的批量的content部分需要urlencode。</p>
	<p>参数说明</p>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>c</td><td>具体内容</td><td>content内容</td></tr>
	</table>
	<p>content内容说明</p>
	<pre>
	timestamp<span class="text-danger">TAB</span>level<span class="text-danger">TAB</span>requestId<span class="text-danger">TAB</span>appName<span class="text-danger">TAB</span>Url<span class="text-danger">TAB</span>message<span class="text-danger">ENTER</span>
	</pre>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>level</td><td>log等级</td><td>DEV,INFO,WARN,ERROR</td></tr>
		<tr><td>requestId</td><td>用户id</td><td>String</td></tr>
		<tr><td>appName</td><td>应用名</td><td>String</td></tr>
		<tr><td>Url</td><td>请求访问的URL</td><td>String</td></tr>
		<tr><td>message</td><td>日志信息</td><td>String</td></tr>
	</table>
<br/>
	
<h4 class="text-danger">Web测速上报接口</h4>
<pre>	http://{ip}/web-broker-service/api/speed</pre>
	
<p>参数说明</p>
<table style="width:90%" class="table table-bordered table-striped table-condensed  ">
		<tr><th style="width:10%">query名</th><th style="width:15%">实际名称</th><th style="width:15%">描述</th><th style="width:60%">类型</th></tr>	
		<tr><td>v</td><td>version</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>t</td><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>n</td><td>network</td><td>网络类型</td><td>整型, 2G,3G,4G,WIFI(iOS只有3G和WIFI)，1=wifi, 2=2G, 3=3G, 4=4G, 0=Unknown</td></tr>
		<tr><td>p</td><td>platform</td><td>平台类型</td><td>整型，</td></tr>
		<tr><td>w</td><td>page</td><td>web页面</td><td>String</td></tr>
		<tr><td>s</td><td>测速点</td><td>详细测速信息</td><td>以测速点编号-时间为一个单元，每个测速点之间以\t分隔， step1-responseTime1<span class="text-danger">TAB</span>step2-responseTime2...,例如1-1\t2-10\t3-100表明编号为1的测速点加载时间1毫秒，编号为2的测速点，加载时间10毫秒...</td></tr>
</table>
<br/>
	
<h4 class="text-danger">CDN监控接口</h4>
	<pre>	http://{ip}/broker-service/api/cdn</pre>
	
	批量接口POST内容，前面加上v=1&c=，不同请求之间用回车<span class="text-danger">ENTER</span>分隔，字段之间用<span class="text-danger">TAB</span>分隔。

	<pre>
	timstamp<span class="text-danger">TAB</span>targetUrl<span class="text-danger">TAB</span>dnslookup<span class="text-danger">TAB</span>tcpconnect<span class="text-danger">TAB</span>request<span class="text-danger">TAB</span>response<span class="text-danger">ENTER</span>
	
	sample如下:
	
	v=1&c=
	1400037748182<span class="text-danger">TAB</span>cdn-resource1<span class="text-danger">TAB</span>300<span class="text-danger">TAB</span>200<span class="text-danger">TAB</span>300<span class="text-danger">TAB</span>300<span class="text-danger">ENTER</span>
	1400037748182<span class="text-danger">TAB</span>cdn-resource2<span class="text-danger">TAB</span>300<span class="text-danger">TAB</span>200<span class="text-danger">TAB</span>300<span class="text-danger">TAB</span>300<span class="text-danger">ENTER</span>
	1400037748182<span class="text-danger">TAB</span>cdn-resource3<span class="text-danger">TAB</span>300<span class="text-danger">TAB</span>200<span class="text-danger">TAB</span>300<span class="text-danger">TAB</span>300<span class="text-danger">ENTER</span>
	</pre>
	
	<p>参数说明</p>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>v</td><td>API版本号</td><td>暂定为1</td></tr>
		<tr><td>c</td><td>具体内容</td><td>content内容</td></tr>
	</table>
	<p>content内容说明</p>
	<pre>
	timstamp<span class="text-danger">TAB</span>targetUrl<span class="text-danger">TAB</span>dnslookup<span class="text-danger">TAB</span>tcpconnect<span class="text-danger">TAB</span>request<span class="text-danger">TAB</span>response<span class="text-danger">ENTER</span>
	</pre>
	<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
		<tr><th>实际名称</th><th>描述</th><th>类型</th></tr>	
		<tr><td>timestamp</td><td>发生时间</td><td>long型，1970到现在的毫秒数</td></tr>
		<tr><td>targetUrl</td><td>具体的cdn资源</td><td>cdn资源的一个定义</td></tr>
		<tr><td>dnslookup</td><td>dns寻址时间</td><td>int</td></tr>
		<tr><td>tcpConnect</td><td>tcp连接建立</td><td>int</td></tr>
		<tr><td>request</td><td>请求时间</td><td>int</td></tr>
		<tr><td>response</td><td>接受时间</td><td>int</td></tr>
	</table>
<br/>
<!-- <h4 class="text-success">URL规则配置&nbsp;  <a target="_blank" href="/cat/s/config?op=urlPatternUpdate">链接</a></h4>

<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
	<tr><th>ID</th><th>描述</th></tr>
	<tr><td>唯一ID</td><td>不能有特殊字符，仅限于英文字母和-</td></tr>	
	<tr><td>所属组</td><td>分析时不起作用，仅仅用作url的分组，用于展示目的</td></tr>	
	<tr><td>Pattern名</td><td>支持完全匹配方式，比如http://m.api.dianping.com/searchshop.api， 
部分匹配，比如 http://www.dianping.com/{City}/food，{City}可以匹配任何字符串</td></tr>	
</table>
<br/>

<h4 class="text-danger">WEB监控报表获取&nbsp;&nbsp;&nbsp;&nbsp; </h4>
<p>Cat支持其它系统通过调用HTTP API来获取WEB监控报表数据（JSON格式）</p>
<pre>
	http请求方式: GET或者POST
	http://主机域名:端口/cat/r/web?
</pre>
<p>参数说明</p>
<table style="width:70%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="30%">参数</th><th width="70%">说明</th></tr>	
	<tr><td>op</td><td>json<span class="text-danger">  必需</span></td></tr>
	<tr><td>其他参数</td><td>参考端到端WEB监控文档，除了op参数不同，其他均相同，可直接复用<span class="text-danger">  必需</span></td></tr>
</table>
<p> url示例<span class="text-danger">（红色部分为不同参数，没有op则需要添加，其他参数相同）</span></p>
<pre>
	http://cat.dianpingoa.com/cat/r/web?<span class="text-danger">op=view&</span>url=s1-small-dnsLookup&group=cdn-s1&city=上海市-&type=info&channel=&startDate=2014-10-28%2016:00&endDate=2014-10-28%2019:00 为APP监控查看的URL链接
	则获取报表的URL为：
	http://cat.dianpingoa.com/cat/r/web?<span class="text-danger">op=json&</span>url=s1-small-dnsLookup&group=cdn-s1&city=上海市-&type=info&channel=&startDate=2014-10-28%2016:00&endDate=2014-10-28%2019:00
</pre>
 -->


	
	
	