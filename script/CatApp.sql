CREATE TABLE `app_command_data_1` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
  `period` date NOT NULL COMMENT '时间',
  `minute_order` smallint NOT NULL COMMENT '分钟',
  `city` smallint NOT NULL COMMENT '城市',
  `operator` tinyint NOT NULL COMMENT '运营商',
  `network` tinyint NOT NULL COMMENT '网络类型',
  `app_version` int NOT NULL COMMENT '版本',
  `connect_type` tinyint NOT NULL COMMENT '访问类型，是否长连接',
  `code` smallint NOT NULL COMMENT '返回码',
  `platform` tinyint NOT NULL COMMENT '平台',
  `access_number` bigint NOT NULL COMMENT '访问量',
  `response_sum_time` bigint NOT NULL COMMENT '响应时间大小',
  `request_package` bigint NOT NULL COMMENT '请求包大小',
  `response_package` bigint NOT NULL COMMENT '响应包大小',
  `status` smallint NOT NULL COMMENT '数据状态',
  `creation_date` datetime NOT NULL COMMENT '数据插入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `IX_condition` (`period`,`minute_order`,`city`,`operator`,`network`,`app_version`,`connect_type`,`code`,`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='app基本数据';

CREATE TABLE `app_speed_data_1` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
  `period` date NOT NULL COMMENT '时间',
  `minute_order` smallint NOT NULL COMMENT '分钟',
  `city` smallint NOT NULL COMMENT '城市',
  `operator` tinyint NOT NULL COMMENT '运营商',
  `network` tinyint NOT NULL COMMENT '网络类型',
  `app_version` int NOT NULL COMMENT '版本',
  `platform` tinyint NOT NULL COMMENT '平台',
  `access_number` bigint NOT NULL COMMENT '访问量',
  `slow_access_number` bigint NOT NULL COMMENT '慢用户访问量',
  `response_sum_time` bigint NOT NULL COMMENT '响应时间大小',
  `slow_response_sum_time` bigint NOT NULL COMMENT '慢用户响应时间大小',
  `status` smallint NOT NULL COMMENT '数据状态',
  `creation_date` datetime NOT NULL COMMENT '数据插入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `IX_condition` (period,minute_order,city,operator,network,app_version,platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='app测速数据';


CREATE TABLE `crash_log` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `app_name` varchar(100) NOT NULL DEFAULT '' COMMENT 'app名称',
  `platform` tinyint(4) NOT NULL COMMENT '平台类型，1 for android, 2 for ios',
  `app_version` varchar(50) NOT NULL DEFAULT '' COMMENT 'app版本',
  `platform_version` varchar(50) NOT NULL DEFAULT '' COMMENT '平台版本',
  `module` varchar(50) NOT NULL DEFAULT '' COMMENT 'crash模块',
  `level` tinyint(4) NOT NULL COMMENT '错误级别',
  `msg` varchar(500) DEFAULT NULL,
  `device_brand` varchar(20) DEFAULT NULL COMMENT '手机品牌',
  `device_model` varchar(50) DEFAULT NULL,
  `updatetime` datetime NOT NULL COMMENT '数据更新时间',
  `crash_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT 'crash时间',
  `dpid` varchar(200) DEFAULT NULL COMMENT 'dpid',
  `map_id` varchar(200) DEFAULT NULL COMMENT '混淆mapid',
  `tag` tinyint(4) DEFAULT NULL COMMENT 'tag',
  PRIMARY KEY (`id`),
  KEY `IX_CONDITION` (`crash_time`,`app_name`),
  KEY `updatetime` (`updatetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `app_command_data_daily_1` (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
  `period` date NOT NULL COMMENT '时间',
  `city` smallint(6) NOT NULL COMMENT '城市',
  `operator` tinyint(4) NOT NULL COMMENT '运营商',
  `network` tinyint(4) NOT NULL COMMENT '网络类型',
  `app_version` int(11) NOT NULL COMMENT '版本',
  `connect_type` tinyint(4) NOT NULL COMMENT '访问类型，是否长连接',
  `code` smallint(6) NOT NULL COMMENT '返回码',
  `platform` tinyint(4) NOT NULL COMMENT '平台',
  `access_number` bigint(20) NOT NULL COMMENT '访问量',
  `response_sum_time` bigint(20) NOT NULL COMMENT '响应时间大小',
  `request_package` bigint(20) NOT NULL COMMENT '请求包大小',
  `response_package` bigint(20) NOT NULL COMMENT '响应包大小',
  `status` smallint(6) NOT NULL COMMENT '数据状态',
  `updatetime` datetime NOT NULL COMMENT '数据更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `IX_condition` (`period`,`city`,`operator`,`network`,`app_version`,`connect_type`,`code`,`platform`),
  KEY `updatetime` (`updatetime`)
) ENGINE=InnoDB AUTO_INCREMENT=213370443 DEFAULT CHARSET=utf8 COMMENT='app基本数据';

CREATE TABLE `crash_log_content` (
  `id` int(11) unsigned NOT NULL,
  `content` longblob COMMENT 'crash详细log',
  `updatetime` datetime NOT NULL COMMENT '数据更新时间',
  PRIMARY KEY (`id`),
  KEY `updatetime` (`updatetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `map_file` (
  `map_id` varchar(100) NOT NULL,
  `content` longblob COMMENT 'mapping文件内容',
  `updatetime` datetime NOT NULL COMMENT '数据更新时间',
  PRIMARY KEY (`map_id`),
  KEY `updatetime` (`updatetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;