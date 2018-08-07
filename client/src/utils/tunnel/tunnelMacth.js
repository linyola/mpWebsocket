const util = require('./util.js');

function match(page, app, opt) {
  const that = page;
  if (app.data.tunnelStatus !== 'close') {
    app.data.tunnel.close();
  }

  app.tunnelCreate();// 新建信道，并监听相关变化
  const tunnel = app.data.tunnel;
  that.data.tunnel = app.data.tunnel;
  // function getCurrentTunnelId() {
  //   console.log('tunnelId:', app.tunnel);
  //   return app.data.tunnel.socketUrl.slice(app.data.tunnel.socketUrl.indexOf('tunnelId=') + 9, app.data.tunnel.socketUrl.indexOf('&'));
  // }
  app.tunnelConnectCallback = () => {
    let userInfo = that.data.userInfo;
    // userInfo.tunnelId = getCurrentTunnelId()
    that.setData({
      status: '已连接，对手匹配中...',
      userInfo// 用户信息存储当前的信道ID
    });
    if (page.tunnelConnectCallback()) {
      page.tunnelConnectCallback();
    }
  };

  app.tunnelCloseCallback = () => {
    that.setData({ status: '连接已关闭' });
    // util.showSuccess('连接已断开')
  };

  app.tunnelReconnectCallback = () => {
    util.showSuccess('已重新连接');
    let userInfo = that.data.userInfo;
    // userInfo.tunnelId = getCurrentTunnelId()
    that.setData({
      status: '网络已重连，匹配中...',
      userInfo
    });
    tunnel.emit('updateMatchInfo', {// 发起匹配
      openId: that.data.openId,
      sortId: opt.sortId,
      friendsFightingRoom: opt.friendsFightingRoom// 匹配者含friendsFightingRoom则说明是好友之间的匹配
    });
  };

  app.tunnelReconnectCallback = () => {
    util.showSuccess('已重新连接');
    let userInfo = that.data.userInfo;
    // userInfo.tunnelId =  getCurrentTunnelId()
    that.setData({
      status: '网络重连成功，对手匹配中...',
      userInfo
    });
  };

  app.tunnelErrorCallback = (error) => {
    that.setData({ status: '信道发生错误：' + error });
    util.showSuccess('连接错误');
  };

  tunnel.on('GuestEnterRoom', (res) => { // 监听队友进入对战房间
    console.log('GuestEnterRoom res', res);
    page.data.showStartBattleFlat = true;
    var userInfo = wx.getStorageSync('userInfo');
    var host = {};
    host.userId = wx.getStorageSync('uid');
    host.nickName = userInfo.nickName;
    host.avatarUrl = userInfo.avatarUrl;
    host.webScoketServer = res.webScoketServer;
    var guest = {};
    guest.userId = res.guestUserId;
    guest.nickName = res.guestNickName;
    guest.avatarUrl = res.guestAvatarUrl;
    guest.webScoketServer = res.webScoketServer;
    wx.setStorageSync('userMe', host);
    wx.setStorageSync('userOthers', guest);
    that.data.status = host.nickName + ' VS ' + guest.nickName;
  });
}
module.exports = { match };
