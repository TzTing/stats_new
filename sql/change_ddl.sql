/*

 Source Server         : 192.168.0.101 DM
 Source Server Type    : DM
 Source Host           : 192.168.0.101:5236
 Source Schema         : ANALYSIS_GZ_NEW

 Target Server Type    : DM

*/

--2023/01/13
alter table tableType add curNewYear int
--所属地区号
alter table excelTemplates add belongDistNo varchar(64)
--下拉选项格式
alter table fileItem add formatterSelect varchar(128)

CREATE TABLE "ANALYSIS_GZ_NEW"."mq_message"
(
"id" INTEGER IDENTITY(1, 1) NOT NULL,
"keyword" VARCHAR2(255),
"username" VARCHAR2(255),
"topic_type" VARCHAR2(255),
"topic_name" VARCHAR2(255),
"years" INTEGER,
"months" INTEGER,
"dist_name" VARCHAR2(255),
"dist_no" VARCHAR2(255),
"type_code" VARCHAR2(255),
"content_type" INTEGER,
"content" TEXT,
"consumer_flag" INTEGER,
"run_flag" BIT DEFAULT 1 NOT NULL,
"ok_flag" BIT DEFAULT 0 NOT NULL,
"read_flag" BIT DEFAULT 0 NOT NULL,
"created_by" VARCHAR2(255),
"created_time" DATETIME(6),
"updated_by" VARCHAR2(255),
"updated_time" DATETIME(6),
"exec_time" INT,
NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "MAIN", CLUSTERBTR) ;

COMMENT ON TABLE "ANALYSIS_GZ_NEW"."mq_message" IS 'mq消息记录表';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."consumer_flag" IS '消费状态';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."content" IS '内容';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."content_type" IS '内容类型';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."created_by" IS '创建人';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."created_time" IS '创建时间';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."dist_name" IS '地区名称';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."dist_no" IS '地区编号';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."id" IS 'ID';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."keyword" IS '关键词';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."months" IS '月份';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."ok_flag" IS '是否消费';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."read_flag" IS '是否已读消息';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."run_flag" IS '消息是否可用';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."topic_name" IS '主题名称';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."topic_type" IS '主题类型';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."type_code" IS '模式类型';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."updated_by" IS '更新人';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."updated_time" IS '更新时间';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."username" IS '用户名';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."mq_message"."years" IS '年份';

CREATE TABLE "ANALYSIS_GZ_NEW"."operation_log"
(
"id" INT IDENTITY(1, 1) NOT NULL,
"ope_user" VARCHAR2(255),
"ope_ip" VARCHAR2(255),
"req_uri" VARCHAR2(255),
"ope_class" VARCHAR2(255),
"req_type" VARCHAR2(255),
"ope_description" VARCHAR(255),
"ope_param" TEXT,
"ope_stats" BIT,
"ope_result" TEXT,
"ope_error_message" TEXT,
"ope_mode" VARCHAR2(255),
"ope_sql" TEXT,
"ope_type" VARCHAR2(255),
"ope_begin_date" DATETIME(6),
"ope_end_date" DATETIME(6),
"ope_databases" VARCHAR2(255),
"ope_exec_time" INTEGER,
NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "MAIN", CLUSTERBTR) ;

COMMENT ON TABLE "ANALYSIS_GZ_NEW"."operation_log" IS '操作日志';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."id" IS 'id';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_begin_date" IS '操作开始时间';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_class" IS '操作的类';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_databases" IS '操作的数据库信息';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_description" IS '操作说明';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_end_date" IS '操作结束时间';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_error_message" IS '操作错误的日志';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_exec_time" IS '操作执行时间（毫秒）';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_ip" IS 'ip地址';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_mode" IS '操作的模块';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_param" IS '传入的参数';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_result" IS '操作结果';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_sql" IS '执行的语句';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_stats" IS '操作状态';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_type" IS '操作类型';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."ope_user" IS '操作人';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."req_type" IS '请求方式';
COMMENT ON COLUMN "ANALYSIS_GZ_NEW"."operation_log"."req_uri" IS '请求地址';




--2023/01/13
CREATE TABLE "ANALYSIS_GZ_NEW"."notes"
(
"id" INT IDENTITY(15, 1) NOT NULL,
"description" VARCHAR(50),
"sqlstr" VARCHAR(4000),
"visible" BIT,
"disId" INT,
"typecode" VARCHAR(50),
"keyword" VARCHAR(100),
"alterType" INTEGER,
"alterUrl" VARCHAR(100),
"alterParam" VARCHAR(255),
"alterSql" VARCHAR(4000),
NOT CLUSTER PRIMARY KEY("id")) STORAGE(ON "MAIN", CLUSTERBTR) ;

CREATE UNIQUE  INDEX "INDEX1203488332593173" ON "ANALYSIS_GZ_NEW"."notes"("id" ASC) STORAGE(ON "MAIN", CLUSTERBTR) ;




--2023/11/28
--兼容人大金仓添加的字段
alter table fun_contrast add kingbase_sqlstr varchar(8000)




--2023/12/14
--模式显示标题
alter table tableType add showName varchar(500)
--模式跳转的旧显目链接
alter table tableType add oldProjectUrl varchar(255)
--首页模式的排序
alter table tableType add sortNum int