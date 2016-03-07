<%@ page contentType="text/html; charset=utf-8" %>

<script type="text/javascript">
	$(document).ready(function(){
		var domain = '';
		var id = '';
		if('${payload.type}' == 'code'){
			domain = '${payload.domain}';
			id = '${payload.id}';
		}
	})
</script>
		<div class="tabbable tabs-left" id="content"> <!-- Only required for left/right tabs -->
		
		  <ul class="nav nav-tabs padding-12 ">
		  	<c:forEach var="entry" items="${model.commandGroupConfig.commands}" varStatus="status">
			    <li id="tab-group-${entry.key}" class="text-right"><a href="#tabContent-group-${status.index}" data-toggle="tab"> ${entry.key}</a></li>
			</c:forEach>
		  </ul>
		  <div class="tab-content">
		  	<c:forEach var="entry" items="${model.commandGroupConfig.commands}" varStatus="status">
			  	<div class="tab-pane" id="tabContent-group-${status.index}">
				    <table class="table table-striped table-condensed table-bordered table-hover">
					    <thead>
					    <tr>
							<th width="90%">命令字</th>
							<th width="10%" class="center"><a href="?op=appCommandGroupAdd&type=group" class="btn btn-primary btn-xs" >
							<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
						</tr>
						</thead>
				    	<c:forEach var="command" items="${entry.value.subCommands}">
					    	<tr><td>${command.key}</td>
								<td  class="center">
									<a href="?op=appCommandGroupDelete&parent=${entry.key}&name=${command.key}&type=group" class="btn btn-danger btn-xs delete" >
									<i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
							</tr>
				    	</c:forEach>
				    </table>
			    </div>
			</c:forEach>
		  </div>
		</div>