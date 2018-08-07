# mpWebsocket
微信小程序，websocket长连接服务器客户端的解决方案
-----------------------------------------------------
世界杯期间做了一个足球答题类的小程序，对微信小程序有了一个比较深刻的认知。
关于长连接，微信提供了原生的websocket接口。但是这个接口离一个完善的可商用的标准，相差甚远。
这里提供一个经过线上测试的、稳定并且具有断线重连的解决方案。
整个方案，包括客户端和服务器部分。
客户端是基于WePY框架（ https://tencent.github.io/wepy/ ），服务器是基于netty实现。
------------------------------------------------------
客户端：
	1.需要替换config.js中的appid和secret
	2.
------------------------------------------------------
服务器：
	客户端发送的协议需要对应
