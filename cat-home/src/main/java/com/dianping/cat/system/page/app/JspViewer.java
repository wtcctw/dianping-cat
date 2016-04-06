package com.dianping.cat.system.page.app;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.system.SystemPage;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case APP_NAME_CHECK:
			return JspFile.APP_NAME_CHECK.getPath();
		case APP_LIST:
		case APP_COMMAND_SUBMIT:
		case APP_COMMAND_DELETE:
		case APP_CODE_SUBMIT:
		case APP_CODE_DELETE:
		case APP_CONSTATN_DELETE:
		case APP_CONSTATN_SUBMIT:
		case APP_COMMAND_GROUP_DELETE:
		case APP_COMMAND_GROUP_SUBMIT:
			return JspFile.APP_LIST.getPath();
		case APP_CODE_ADD:
		case APP_CODE_UPDATE:
			return JspFile.APP_CODE_UPDATE.getPath();
		case APP_SPEED_LIST:
		case APP_SPEED_DELETE:
		case APP_SPEED_SUBMIT:
			return JspFile.APP_SPEED_LIST.getPath();
		case APP_SPEED_ADD:
		case APP_SPEED_UPDATE:
			return JspFile.APP_SPEED_UPDATE.getPath();
		case APP_COMMMAND_UPDATE:
			return JspFile.APP_UPDATE.getPath();
		case APP_RULE:
		case APP_RULE_ADD_OR_UPDATE_SUBMIT:
		case APP_RULE_DELETE:
			return JspFile.APP_RULE.getPath();
		case APP_RULE_ADD_OR_UPDATE:
			return JspFile.APP_RULE_UPDATE.getPath();
		case APP_CONFIG_UPDATE:
			return JspFile.APP_CONFIG_UPDATE.getPath();
		case BROKER_CONFIG_UPDATE:
			return JspFile.BROKER_CONFIG_UPDATE.getPath();
		case APP_COMMAND_BATCH:
		case APP_COMMAND_BATCH_UPDATE:
			return JspFile.APP_COMMAND_BATCH.getPath();
		case APP_CONSTANT_ADD:
		case APP_CONSTANT_UPDATE:
			return JspFile.APP_CONSTANT_UPDATE.getPath();
		case APP_COMMAND_FORMAT_CONFIG:
			return JspFile.APP_COMMAND_FORMAT_CONFIG.getPath();
		case APP_COMMAND_GROUP_ADD:
			return JspFile.APP_COMMAND_GROUP_ADD.getPath();
		case APP_COMMAND_GROUP_UPDATE:
			return JspFile.APP_COMMAND_GROUP_UPDATE.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
