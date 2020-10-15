## 系统介绍
该系统为免签支付系统，支持微信、支付宝、qq钱包、paypal等多种支付方式

## 环境说明
#### jdk 1.8
#### mysql 5.7.8+ 
#### redis 3.2+
#### node 10.0+ (LTS版本)
#### npm 6.0+
#### maven 3.5 +
#### IDEA/Eclipse

## 数据库说明
#### db文件夹下是数据库脚本，根据文件顺序配置数据库
#### 1scheme.sql    建库语句
#### 2pigxx.sql     核心数据库
#### 3pigxx_config.sql  配置中心数据库
#### 4pigxx_pay.sql   支付模块数据库

### 数据源修改
#### pigx/pigx-register/src/main/resources/application.yml
### 数据源相关配置
#### db:
####   num: 1
####   user: root      #只需要修改此处用户名密码
####   password: root  #只需要修改此处用户名密码

## 本地hosts配置
#### 127.0.0.1 pigx-register
#### 127.0.0.1 pigx-gateway
#### 127.0.0.1 pigx-redis
#### 127.0.0.1 pigx-mysql


## 启动顺序
#### 1. PigxNacosApplication   
#### 2. PigxAdminApplication  
#### 3. PigxGatewayApplication 
#### 4. PigxPayPlatformApplication 
