import wx from 'weixin-js-sdk'

// 微信SDK配置
export const initWechatSDK = (config: any) => {
  return new Promise((resolve, reject) => {
    wx.config({
      debug: false,
      appId: config.appId,
      timestamp: config.timestamp,
      nonceStr: config.nonceStr,
      signature: config.signature,
      jsApiList: config.jsApiList || [],
    })
    
    wx.ready(() => {
      resolve(true)
    })
    
    wx.error((res) => {
      reject(res)
    })
  })
}

// 分享配置
export const shareToWechat = (shareData: any) => {
  wx.updateAppMessageShareData({
    title: shareData.title,
    desc: shareData.desc,
    link: shareData.link,
    imgUrl: shareData.imgUrl,
  })
  
  wx.updateTimelineShareData({
    title: shareData.title,
    link: shareData.link,
    imgUrl: shareData.imgUrl,
  })
}
