# SecuritySensorApplication-

#### 实验目的
•	获取红外人体感应数据
•	设置报警器的安防状态
•	获取烟雾报警数据
#### 实验过程
1.	启动应用，走近人体红外传感器，查看手机接收的数据
2.	设置报警器的布放和撤防（报警器只有布放状态下才会启动监测）
3.	模拟烟雾场景，触发烟雾报警，查看手机接收的数据


#### 核心类说明
| 类路径                                        | 描述                              |
| ------------------------------------------ | ------------------------------- |
| `owon/sdk/util/DeviceMessagesManager.java` | 设备操作类，封装灯光、电机、传感器等设备的控制与查询方法    |
| `owon/sdk/util/SocketMessageListener.java` | 数据回调类，包含登录结果、设备操作结果、设备状态获取等回调方法 |

###### 设备数据回调方法
类名： SocketMessageListener.java
方法： getMessage
| 字段名         | 类型    | 描述                 |
| ----------- | ----- | ------------------ |
| `commandID` | int   | 数据回调码（对应回调类型常量）    |
| `bean`      | class | 数据返回基类，需强转为对应设备的子类 |


#### 功能接口

###### 安全部防
说明：烟雾传感器只有在布防状态下才会触发报警。
方法名：SecurityDeployment
| 参数名    | 类型  | 描述     |
| ------ | --- | ------ |
| zoneID | int | 默认 255 |

###### 安全撤防
说明：撤出布防状态，不会触发报警。
方法名：SecurityDisarming

| 参数名    | 类型  | 描述     |
| ------ | --- | ------ |
| zoneID | int | 默认 255 |


#### 数据回调码
回调方法： getMessage
| 属性                                    | 描述           |
| ------------------------------------- | ------------ |
| `Constants.UpdateEPList`              | 获取网关列表       |
| `Constants.ZigBeeGetEPList`           | 获取设备列表       |
| `Constants.SmartLightSetupSwitchgear` | 设置灯光结果       |
| `Constants.UpdateSwitchgear`          | 查询或控制灯后的状态回调 |
| `Constants.UpdateLight`               | 物理控制灯后的状态回调  |
| `Constants.THI_UPDATE`                | 温湿度传感器数据回调   |
| `Constants.ILLUM_UPDATE`              | 光照传感器数据回调    |
| `Constants.MotionSensorUpdate`        | 人体传感器查询状态回调  |
| `Constants.MotionSensor`              | 人体传感触发数据回调   |
| `Constants.WarningSensor`             | 烟雾监测数据回调     |

#### 设备类型
| 属性                                            | 描述        |
| --------------------------------------------- | --------- |
| `Constants.LIGHT_601`                         | 灯，只有开关    |
| `Constants.LIGHT_EXTEND_LO_COLOR_TEMP_GOODVB` | 可调节亮度和色温灯 |
| `DeviceTypeCode.TH_SENSOR`                    | 温湿度传感器    |
| `DeviceTypeCode.LX_SENSOR`                    | 光照传感器     |
| `DeviceTypeCode.SMOKE_SENSOR_ZONE`            | 烟雾报警器     |
| `DeviceTypeCode.MOTION_SENSOR_ZONE`           | 人体传感器     |
| `DeviceTypeCode.AC_SENSOR`                    | 红外转发器     |
| `DeviceTypeCode.WARN_SENSOR`                  | 声光报警器     |
| `DeviceTypeCode.WARN_MOTOR`                   | 窗帘电机      |
| `DeviceTypeCode.DOOR_SENSOR`                  | 门磁感应器     |


