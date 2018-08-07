/**
 * 小程序配置文件
 */

var host = 'http://127.0.0.1:8090'; // 开发环境
var tunnel = 'ws://127.0.0.1:9688';

var appId = 'appid';

var secret = 'secret';

var config = {
  service: {
    appId,

    secret: `${secret}`,

    // 登录地址，用于建立会话
    loginUrl: `${host}/wechat/user/login`,

    // rooturl
    rootUrl: `${host}/`,

    // 基础url
    baseUrl: `${host}/wechat/`,

    // 测试的请求地址，用于测试会话
    requestUrl: `${host}/wechat/user`,

    // 测试的信道服务地址
    tunnelUrl: `${tunnel}/websocket`,

    // 上传图片接口
    uploadUrl: `${host}/wechat/upload`
  }
};

module.exports = config;
