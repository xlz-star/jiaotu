-- 数据库表结构
CREATE TABLE
IF
	NOT EXISTS public.user (
		uid VARCHAR PRIMARY KEY,
		uname VARCHAR NOT NULL,
		passwd VARCHAR NOT NULL,
		identify int2 NOT NULL 
	);
CREATE TABLE
IF
	NOT EXISTS public.file (
		fid VARCHAR PRIMARY KEY,
		fname VARCHAR NOT NULL,
		view_path VARCHAR NOT NULL,
		real_path VARCHAR NOT NULL,
		parent_path VARCHAR NOT NULL,
		update_time DATE NOT NULL,
		dir bool NOT NULL,
		ftype VARCHAR NOT NULL,
		size int8 NOT NULL,
		uid VARCHAR NOT NULL REFERENCES public.user ( uid ) 
	);
-- 数据库属性设置
COMMENT ON COLUMN public.user.uid is  '用户id';
COMMENT ON COLUMN public.user.uname is  '用户名';
COMMENT ON COLUMN public.user.passwd is  '用户密码';
COMMENT ON COLUMN public.user.identify is  '用户身份（0：管理员，1：普通用户）';
CREATE INDEX user_name ON public.user ( uname );
COMMENT ON COLUMN public.file.fid is  '文件id';
COMMENT ON COLUMN public.file.fname is  '文件名';
COMMENT ON COLUMN public.file.view_path is  '文件显示路径';
COMMENT ON COLUMN public.file.real_path is  '文件实际路径';
COMMENT ON COLUMN public.file.uid is  '文件所有者id';
COMMENT ON COLUMN public.file.update_time is  '文件更新时间';
COMMENT ON COLUMN public.file.dir is  '是否是文件夹';
COMMENT ON COLUMN public.file.ftype is  '文件类型';
COMMENT ON COLUMN public.file.size is  '文件大小';
CREATE INDEX file_name ON public.file ( fname );
