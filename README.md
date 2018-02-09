# AndroidNetworkTrafficStatistics  
![](https://img.shields.io/github/release/sikuquanshu123/AndroidNetworkTrafficStatistics.svg)  

## 主要功能  
+ 实现每月剩余流量的计算，并提供校正功能
+ 统计每日使用的流量  
## 实现细节  
每月剩余流量的查询是通过发送10086的查询短信，并利用其回执短信使用正则表达式提取数据。而每日流量的统计则是通过TrafficStats类提供的静态方法读取数据，不过它提供的数据是统计的手机在一次开机中所使用。所以要考虑关机之前的数据保存。  
## Change Log  
### v0.1  
- 新增闲时统计功能  
- 新增通知管理  
### v1.0  
- 新增可视化图表  
- 修复已知问题  
### v2.1.0  
- 删除无用权限  
- 重绘图标  
### v2.1.1  
- 适配Nougat  
### v3.1.3  
- 扩大正则表达式适用范围  
- 调节后台数据刷新频率，显著降低耗电量  
- 新增流量上限警戒通知
