[App告警 命令字: ${para.commandName} 告警指标: ${para.metric}] <br/>
${content}[时间: ${date}] <br/><br/>

<table rules="all" border="1" style="font-size: medium;">
	<tr style="background-color:#336699; color:#fff;" align="center">
		<td>告警指标</td>
		<td>命令字</td>
		<td>返回码</td>
		<td>网络类型</td>
		<td>版本</td>
		<td>连接类型</td>
		<td>平台</td>
		<td>地区</td>
		<td>运营商</td>
	</tr>
	<tr style="background-color:#fff" align="center" style="font-size: medium;">
		<td>${para.metric}</td>
		<td>${para.commandName}</td>
		<td>${para.code}</td>
		<td>${para.network}</td>
		<td>${para.version}</td>
		<td>${para.connectType}</td>
		<td>${para.platform}</td>
		<td>${para.city}</td>
		<td>${para.operator}</td>
	</tr>
</table>
<br/>
<a href='http://cat.dianpingoa.com/cat/r/app?query1=;${para.command};;;;;;;;;;23:59'>点击此处查看详情</a><br/>