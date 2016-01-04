<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<h4>HTTP API调用方式</h4>
<br>
<h5 class="text-success"><strong>1. CAT接口调用请求说明</strong></h5>
<pre>
	http请求方式: GET（请使用http协议）
	http://cat.dianpingoa.com/cat/r/monitor?
</pre>
<p>参数说明</p>
<table style="width:90%" class="table table-bordered table-striped table-condensed">
	<tr><th width="30%">参数</th><th>说明</th></tr>
	<tr><td>group</td><td>监控组唯一ID名称，<span class="text-danger">必需，仅仅包括字母、数字，不能包含特殊字符，比如引号，冒号等。建议TuanGou这类命名方式</span></td></tr>
	<tr><td>domain</td><td>应用唯一ID名称，<span class="text-danger">必需，仅仅包括字母、数字，不能包含特殊字符，比如引号，冒号等。建议用TuanGouWeb这类命名方式</span></td></tr>
	<tr><td>key</td><td>监控业务唯一ID名称，<span class="text-danger">必需，仅仅包括字母、数字，不能包含特殊字符，比如引号，冒号等。建议用PayCount这类命名方式</span></td></tr>
	<tr><td>timestamp</td><td>时间戳,<span class="text-danger">必需，仅仅为数字。如果缺失，选取服务器当前时间</span></td></tr>
	<tr><td>op</td><td>sum，avg，count[<span class="text-danger">默认</span>]</td></tr>
	<tr><td>count</td><td>op=count时所需，<span class="text-danger">默认为1</span></td></tr>
	<tr><td>sum</td><td>op=sum时所需，<span class="text-danger">默认为0</span></td></tr>
	<tr><td>avg</td><td>op=avg时所需，<span class="text-danger">默认为0</span></td></tr>
</table>

<p> 1).op = count时，用于记录一个指标值出现的次数</p>
<pre>
	http://cat.dianpingoa.com/cat/r/monitor?timestamp=1404815988&group=myGroup&domain=myApp&key=myKey&op=count
</pre>
<p> 2).op = avg时，用于记录一个指标出现的平均值</p>
<pre>
	http://cat.dianpingoa.com/cat/r/monitor?timestamp=1404815988&group=myGroup&domain=myApp&key=myKey&op=avg&avg=500
</pre>
<p> 3).op = sum时，用于记录一个指标出现的总和</p>
<pre>
	http://cat.dianpingoa.com/cat/r/monitor?timestamp=1404815988&group=myGroup&domain=myApp&key=myKey&op=sum&sum=500
</pre>
<p> 4).op = batch时，用于批量提交指标。（TAB、ENTER分别是制表符和换行符）</p>
<pre>
	http://cat.dianpingoa.com/cat/r/monitor?op=batch&batch=group<span class="text-danger">TAB</span>domain<span class="text-danger">TAB</span>key<span class="text-danger">TAB</span>type<span class="text-danger">TAB</span>time<span class="text-danger">TAB</span>value<span class="text-danger">ENTER</span>
	group<span class="text-danger">TAB</span>domain<span class="text-danger">TAB</span>key<span class="text-danger">TAB</span>type<span class="text-danger">TAB</span>time<span class="text-danger">TAB</span>value<span class="text-danger">ENTER</span>
</pre>
<p>返回说明</p>
<pre>
	<span class="text-danger">{"statusCode":"-1","errorMsg":"Unknown [ domain,group,key ] name!"} ——> 失败 [必需参数缺失]</span>
	<span class="text-success">{"statusCode":"0"} ——> 成功</span>
</pre>
<br>
<h5 class="text-success">2. Metric-broker-service接口调用请求说明</h5>
<pre>
	http请求方式: POST（请使用http协议）
	http://metric-broker.dp/metric-broker-service/api/metric
</pre>
<p>参数说明</p>
<table style="width:90%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="30%">参数</th><th>说明</th></tr>
	<tr><td>data</td><td>要发送的Json数据，<span class="text-danger">必需，需要Url Encode</span></td></tr>
</table>
<p>Json数据</p>
<pre><code>{
    "category":"network",
    "entities":[
        {
            "measure":"network.flow-in",
            "timestamp":1451268386119,
            "tags":{
                "port":"1",
                "endPoint":"switch-01"
            },
            "fields":{
                "value":0.7220096548596434
            }
        },
        {
            "measure":"network.flow-in",
            "timestamp":1451268386119,
            "tags":{
                "port":"2",
                "endPoint":"switch-01"
            },
                "fields":{
            	"value":0.19497605734770518
            }
        }
    ]
}
</code></pre>
<table style="width:90%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="30%">属性</th><th>说明</th></tr>
	<tr><td>category</td><td>监控分类，包括system, network, database；<span class="text-danger">必需，不能包含特殊字符</span></td></tr>
	<tr><td>measure</td><td>监控指标名称，<span class="text-danger">必需，以“category.”开头。例如，网络设备进口流量：“network.flow-in”</span></td></tr>
	<tr><td>timestamp</td><td>监控数据产生的时间戳，<span class="text-danger">必需，毫秒为单位的时间，注意值不要加引号</span></td></tr>
	<tr><td>tags</td><td>标签,<span class="text-danger">必需，且必须有endPoint这个key，endPoint值为当前监控对象的唯一ID；</span>如有其它标签，可以添加，一般适用于网络设备端口等。</td></tr>
	<tr><td>fields</td><td>采集的监控数据<span class="text-danger">必须，一般只需一个key：{"value":data}，注意value值不要加引号</td></tr>
</table>

<p> 示例
<pre>
	http://metric-broker.dp/metric-broker-service/api/metric?data={encodedData}
</pre>
<p>返回说明</p>
<pre>
	<span class="text-danger">{"status":500, "info":"failed"}  ——> 失败</span>
	<span class="text-success">{"status":200, "info":"success"} ——> 成功</span>
</pre>