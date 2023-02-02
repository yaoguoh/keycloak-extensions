# keycloak-login-spi

## 功能描述

> 拓展`keycloak`登录`手机验证码登录`、`微信登录`、`邮箱登录`等登录功能

## 设计思路

> 通过对 oauth2 中 grant_type: password 的登陆模式进行拓展
>
> 提供新的登录入参并重写登录实现逻辑
>
> 选择 Custom Login Authenticator 作为 Authentication Flow 时
>
> 将会串行执行其中包含的多个 execution (Login Authenticator Implement)
>
> 通过判断新增的参数 login_type , 仅有一个 execution 会作为实际的鉴权过程

## 使用流程

### 一、`Docker`运行`keycloak server`

```text
keycloak.version : 20.0.3

docker run -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=password quay.io/keycloak/keycloak:20.0.3

docker run --name keycloak -d --link mysql:mysql -p 8080:8080 \
        -e KEYCLOAK_ADMIN=admin \
        -e KEYCLOAK_ADMIN_PASSWORD=password \
        -e KC_DB=mysql \
        -e KC_DB_URL=jdbc:mysql://mysql:3306/keycloak \
        -e KC_DB_USERNAME=root \
        -e KC_DB_PASSWORD=root \
        -e KC_HTTP_RELATIVE_PATH=auth \
        -e KC_LOG_LEVEL=debug \
        quay.io/keycloak/keycloak:20.0.3 start-dev
```

### 二、使用自定义插件

> 使用shadow打完整包"keycloak-login-spi-20.0.3-all.jar"

```shell
# keycloak-login-spi-20.0.3-all.jar 传到 `/opt/keycloak/providers` 目录 -> 然后重启pod
curl -s -k -L --output keycloak-login-spi-20.0.3.jar https://github.com/yaoguoh/keycloak-extensions/releases/download/20.0.3/keycloak-login-spi-20.0.3-all.jar
```

### 三、创建 Authentication Flow

1. ***点击新增按钮 :***
   ![add_authentication_1](images/add_authentication_1.png)
2. ***填写 alias 点击保存 :***
   ![add_authentication_2](images/add_authentication_2.png)

### 四、创建 client 并指定接口登录校验方式

1. ***点击创建 client :***
   ![add_client_1](images/add_client_1.png)
2. ***填写 client Id , 选择 openid-connect :***
   ![add_client_2](images/add_client_2.png)
3. ***完成创建后进入编辑页，settings最下方 , 选择上一步创建的 authentication flow 并保存  :***
   ![edit_client_1](images/edit_client_1.png)

### 五、用户属性修改

1. ***点击修改按钮 :***
   ![edit_user_1](images/edit_user_1.png)
2. ***编辑用户属性 :***
   ![edit_user_2](images/edit_user_2.png)

### 六、配置自定义登录流程

1. ***编辑`Authentication`并添加`Execution`:***
   ![edit_authentication_1](images/edit_authentication_1.png)
   ![add_execution_1](images/add_execution_1.png)
   ![add_execution_2](images/add_execution_2.png)
2. ***设置`Execution`可用:*** 并添加配置
   ![edit_execution_1](images/edit_execution_1.png)
3. ***添加配置:***
   ![edit_execution_2](images/edit_execution_2.png)
4. ***推荐完整配置:***
   ![suggest_execution_flow](images/suggest_execution_flow.png)

---

##### 1、密码登陆模式

---

***config :***

```txt
# 登录接口用户名字段名
# eg: username
login form key - username

# 登录接口密码字段名
# eg: password
login form key - password

```

***request :***

```txt
POST http://localhost:8080/auth/realms/master/protocol/openid-connect/token

Content-Type:application/x-www-form-urlencoded

grant_type:password
client_id:custom-login-client
login_type:password
username:admin
password:123456
```

---

##### 2、手机验证码模式

---

***config :***

```txt
# 使用手机号检索用户时，所用 keycloak user attribute 的字段名
# eg: phone
user attribute key - phone

# 登录接口手机号字段名
# eg: phone
login form key - phone 

# 登录接口手机验证码字段名
# eg: code
login form key - code

# 短信校验请求地址
# eg: https://localhost:8080/v1/verifycode/checkcode
sms request url

# 短信校验请求方式 ( get / post.form / post.json )
# eg: post.json
sms request method

# 请求短信服务时默认请求参数，json格式，用于配置常量参数，例如模板类型
# eg: {"clientId": "AAABBB"}
sms request param - default

# 请求短信服务时传入手机号的字段名称
# eg: phone
sms request param - phone

# 请求短信服务时传入验证码的字段名称
# eg: code
sms request param - code

# 短信服务返回值用于确认是否成功的字段名(多个json层级使用.取下级键)
# eg: data.successFlag
sms response check - key

# 短信服务返回值用于确认是否成功的字段值，会根据实际返回值类型转换后校验
# eg: true
sms response check - value
```

***request :***

```txt
POST http://localhost:8080/auth/realms/master/protocol/openid-connect/token

Content-Type:application/x-www-form-urlencoded

grant_type:password
client_id:custom-login-client
login_type:phone
phone:10086
code:1234
```

##### 3、用户名登录

---

***config :***

```txt
# 登录接口用户名字段名
# eg: username
login form key - username

```

***request :***

```txt
POST http://localhost:8080/auth/realms/master/protocol/openid-connect/token

Content-Type:application/x-www-form-urlencoded

grant_type:password
client_id:custom-login-client
client_secret:custom-login-client-secret
login_type:username
username:admin
```

---

##### 4、微信登录模式

---

***config :***

```
# 使用微信号检索用户时，所用 keycloak user attribute 的字段名
# eg: wechat_unionid
user attribute key - wechat unionid

# 登录接口微信号字段名
# eg: wechat_unionid
login form key - wechat unionid
```

***request :***

```txt
POST http://localhost:8080/auth/realms/master/protocol/openid-connect/token

Content-Type:application/x-www-form-urlencoded

grant_type:password
client_id:custom-login-client
client_secret:custom-login-client-secret
login_type:wechat
wechat_unionid:unionid_admin
```

### 七、Keycloak部分接口说明

***官方接口文档地址 :***

> https://www.keycloak.org/docs/latest/server_development/index.html
>
> 调用admin相关接口需要传入授权令牌
>
> 可以使用root用户先以接口方式获取授权令牌，再用来调用以下接口
>
> root授权令牌默认超时时间为1分钟

##### 1、新增用户

```http request
POST http://localhost:8080/auth/admin/realms/master/users

Content-Type:application/json

{
	"username": "admin",
	"credentials": [{
		"type": "password",
		"value": "admin",
		"temporary": false
	}],
	"enabled": true
}
```

##### 2、使用用户名查询用户

> 可通过此接口获取用户id
>
>该查询为模糊查询，查询结果需要二次校验用户名是否匹配

```http request
GET http://localhost:8080/auth/admin/realms/master/users?username=admin
```

##### 3、重置密码

```http request
PUT http://localhost:8080/auth/admin/realms/master/users/{id}/reset-password

Content-Type:application/json

{
	"type": "password",
    "value": "123456",
    "temporary": false
}
```

##### 3、为用户添加属性

> 更新用户属性仅有全量更新的方式可用，所以需要先查询出用户已有属性，在新增属性后，将全量属性更新到用户信息中。
>
>此处应该将查询到更新完成应保证事务的原子性，可使用分布式锁实现

```http request
GET http://localhost:8080/auth/admin/realms/master/users/{id}
```

```http request
PUT http://localhost:8080/auth/admin/realms/master/users/{id}

Content-Type:application/json

{
	"attributes": {
		"phone": "10086"
	}
}
```

### 八、自定义登录设计思路

> 由于 keycloak 未提供使用用户自定义属性检索用户的 rest 接口，所以也无法对自定义属性进行重复校验
>
> 推荐对接 keycloak 对外提供服务的中间件应当持久化用户与第三方登录所需唯一键的映射关系
>
> 用来完成用户是否已存在，以及第三方唯一键是否重复等相关校验
>
> 但需要保证所有映射关系的添加都经过中间件，而不是直接在 keycloak 管理页面完成

