# keycloak-social-wechat-enterprise

## 企业微信

### 企业微信的UA

> 以企业微信的UA既包含微信的`user agent`，也包含企业微信的`user agent`。这样做的原因，是为了兼容已有的系统。

| 系统 | 示例 |
| -----  | ----- |
|iPhone  | Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_2 like Mac OS X) AppleWebKit/603.2.4 (KHTML, like Gecko) Mobile/14F89 wxwork/2.2.0 MicroMessenger/6.3.2 |
|Android | Mozilla/5.0 (Linux; Android 7.1.2; g3ds Build/NJH47F; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043508 Safari/537.36 wxwork/2.2.0 MicroMessenger/6.3.22 NetType/WIFI Language/zh |
|Windows | Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.110 Safari/537.36 wxwork/2.1.3 (MicroMessenger/6.2) WindowsWechat QBCore/3.43.644.400 QQBrowser/9.0.2524.400 |
|Mac     | Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/603.3.8 (KHTML, like Gecko) wxwork/2.2.0 (MicroMessenger/6.2) WeChat/2.0.4 |

> 其中`wxwork`是企业微信关键字，2.1.0为用户安装的微信版本号。`MicroMessenger`是微信的关键字。
