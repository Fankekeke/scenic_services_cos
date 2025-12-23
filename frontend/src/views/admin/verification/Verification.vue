<template>
  <a-card :bordered="false" class="card-area">
    <div style="height: 350px">
      <a-row :gutter="15">
        <a-col :span="12">
          <div class="camera_outer">
            <video
              id="videoCamera"
              :width="videoWidth"
              :height="videoHeight"
              style="max-width: 100%; height: auto;"
              autoplay
            ></video>
            <canvas
              style="display:none; max-width: 100%; height: auto;"
              id="canvasCamera"
              :width="videoWidth"
              :height="videoHeight"
            ></canvas>
            <div v-if="imgSrc" class="img_bg_camera">
              <img :src="imgSrc" alt="" class="tx_img">
            </div>
            <div style="margin-top: 10px">
              <a-button
                size="small"
                type="primary"
                @click.stop.prevent="getCompetence">打开摄像头
              </a-button>
              <a-button
                size="small"
                type="primary"
                @click.stop.prevent="stopNavigator">关闭摄像头
              </a-button>
              <a-button
                size="small"
                type="primary"
                @click.stop.prevent="scanQRCode">识别
              </a-button>
            </div>
            <!-- 添加本地图片上传区域 -->
            <div style="margin-top: 10px">
              <a-upload
                :before-upload="beforeUpload"
                :show-upload-list="false"
                accept="image/*"
              >
                <a-button size="small" type="primary">
                  选择本地图片
                </a-button>
              </a-upload>
              <a-button
                v-if="localImageSrc"
                size="small"
                type="primary"
                @click.stop.prevent="scanLocalQRCode"                style="margin-left: 10px"
              >
                识别本地图片
              </a-button>
            </div>

            <!-- 显示本地图片预览 -->
            <div v-if="localImageSrc" style="margin-top: 10px">
              <img
                :src="localImageSrc"
                alt="本地图片预览"
                style="max-width: 100%; max-height: 200px"
              />
            </div>
          </div>
        </a-col>
        <a-col :span="12">
          <div v-if="orderDetail" class="order-detail-container">
            <a-card title="订单详情" :bordered="true" class="order-card">
              <div class="order-info-cards">
                <div class="info-item">
                  <span class="label">订单编号：</span>
                  <span class="value order-code">{{ orderDetail.code }}</span>
                </div>
                <div class="info-item">
                  <span class="label">景区名称：</span>
                  <span class="value scenic-name">{{ orderDetail.scenicName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">订单金额：</span>
                  <span class="value price">¥{{ orderDetail.price }}</span>
                </div>
                <div class="info-item">
                  <span class="label">购买数量：</span>
                  <span class="value amount">{{ orderDetail.amount }} 张</span>
                </div>
                <div class="info-item">
                  <span class="label">订单状态：</span>
                  <span class="value">
                    <a-tag v-if="orderDetail.orderStatus === 1" color="green">待使用</a-tag>
                    <a-tag v-else color="red">已完成</a-tag>
                  </span>
                </div>
                <div class="info-item">
                  <span class="label">下单时间：</span>
                  <span class="value create-date">{{ orderDetail.createDate }}</span>
                </div>
                <div class="info-item">
                  <span class="label">用户姓名：</span>
                  <span class="value user-name">{{ orderDetail.userName }}</span>
                </div>
              </div>
              <div class="verification-actions" style="margin-top: 20px; text-align: center;">
                <a-button
                  v-if="orderDetail.orderStatus === 1"
                  type="primary"
                  @click="verifyOrder"
                  :loading="verifyLoading"
                >
                  核销订单
                </a-button>
                <a-alert
                  v-else-if="orderDetail.orderStatus === 2"
                  message="此订单已经使用"
                  type="warning"
                  show-icon
                />
              </div>
            </a-card>
          </div>
          <div v-else class="empty-placeholder">
            <a-icon type="qrcode" style="font-size: 48px; color: #1890ff;" />
            <div style="margin-top: 10px; font-size: 16px; color: #666;">扫描二维码查看订单信息</div>
          </div>
        </a-col>
      </a-row>
    </div>
  </a-card>
</template>

<script>
import RangeDate from '@/components/datetime/RangeDate'
import {mapState} from 'vuex'
import moment from 'moment'
import jsQR from 'jsqr'
moment.locale('zh-cn')

export default {
  name: 'Post',
  components: {RangeDate},
  data () {
    return {
      videoWidth: 470,
      videoHeight: 300,
      imgSrc: '',
      localImageSrc: '', // 添加本地图片数据
      thisCancas: null,
      thisContext: null,
      thisVideo: null,
      orderDetail: null,
      verifyLoading: false
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    })
  },
  mounted () {
  },
  methods: {
    getCompetence () {
      var _this = this
      this.thisCancas = document.getElementById('canvasCamera')
      this.thisContext = this.thisCancas.getContext('2d')
      this.thisVideo = document.getElementById('videoCamera')
      // 旧版本浏览器可能根本不支持mediaDevices，我们首先设置一个空对象
      if (navigator.mediaDevices === undefined) {
        navigator.mediaDevices = {}
      }
      // 一些浏览器实现了部分mediaDevices，我们不能只分配一个对象
      // 使用getUserMedia，因为它会覆盖现有的属性。
      // 这里，如果缺少getUserMedia属性，就添加它。
      if (navigator.mediaDevices.getUserMedia === undefined) {
        navigator.mediaDevices.getUserMedia = function (constraints) {
          // 首先获取现存的getUserMedia(如果存在)
          var getUserMedia = navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.getUserMedia
          // 有些浏览器不支持，会返回错误信息
          // 保持接口一致
          if (!getUserMedia) {
            return Promise.reject(new Error('getUserMedia is not implemented in this browser'))
          }
          // 否则，使用Promise将调用包装到旧的navigator.getUserMedia
          return new Promise(function (resolve, reject) {
            getUserMedia.call(navigator, constraints, resolve, reject)
          })
        }
      }
      var constraints = { audio: false, video: { width: this.videoWidth, height: this.videoHeight, transform: 'scaleX(-1)' } }
      navigator.mediaDevices.getUserMedia(constraints).then(function (stream) {
        // 旧的浏览器可能没有srcObject
        if ('srcObject' in _this.thisVideo) {
          _this.thisVideo.srcObject = stream
        } else {
          // 避免在新的浏览器中使用它，因为它正在被弃用。
          _this.thisVideo.src = window.URL.createObjectURL(stream)
        }
        _this.thisVideo.onloadedmetadata = function (e) {
          _this.thisVideo.play()
        }
      }).catch(err => {
        console.log(err)
      })
    },
    setImage () {
      var _this = this
      // 绘制当前视频帧到 canvas
      _this.thisContext.drawImage(_this.thisVideo, 0, 0, _this.videoWidth, _this.videoHeight)

      // 获取图像数据用于二维码解析
      const imageData = this.thisContext.getImageData(0, 0, _this.videoWidth, _this.videoHeight)

      // 使用 jsQR 解析二维码
      const code = jsQR(imageData.data, imageData.width, imageData.height)

      if (code) {
        // 解析成功，处理二维码内容
        this.$message.success('二维码解析成功: ' + code.data)
        // 可以在这里处理解析到的数据
      } else {
        // 继续保留原有人脸识别逻辑
        var image = this.thisCancas.toDataURL('image/png')
        let data = { file: image.replace(/^data:image\/\w+;base64,/, ''), name: this.name }
        this.$post('/cos/face/verification', data).then((r) => {
          // 原有人脸识别处理逻辑...
        })
      }
    },
    scanQRCode () {
      // 类似于 setImage 的实现，但专注于二维码解析
      this.thisContext.drawImage(this.thisVideo, 0, 0, this.videoWidth, this.videoHeight)
      const imageData = this.thisContext.getImageData(0, 0, this.videoWidth, this.videoHeight)
      console.log(imageData)

      const code = jsQR(imageData.data, imageData.width, imageData.height)
      console.log(code)

      if (code) {
        this.$message.success('二维码内容: ' + code.data)
      } else {
        this.$message.warning('未检测到二维码')
      }
    },
    dataURLtoFile (dataurl, filename) {
      var arr = dataurl.split(',')
      var mime = arr[0].match(/:(.*?);/)[1]
      var bstr = atob(arr[1])
      var n = bstr.length
      var u8arr = new Uint8Array(n)
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n)
      }
      return new File([u8arr], filename, { type: mime })
    },
    stopNavigator () {
      this.thisVideo.srcObject.getTracks()[0].stop()
    },
    beforeUpload (file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        this.localImageSrc = e.target.result
      }
      reader.readAsDataURL(file)
      return false // 阻止自动上传
    },

    // 识别本地图片中的二维码
    scanLocalQRCode () {
      if (!this.localImageSrc) {
        this.$message.warning('请先选择本地图片')
        return
      }

      // 创建临时图片对象
      const img = new Image()
      img.onload = () => {
        // 设置 canvas 大小为图片大小
        this.thisCancas.width = img.width
        this.thisCancas.height = img.height

        // 绘制图片到 canvas
        this.thisContext.drawImage(img, 0, 0)

        // 获取图像数据用于二维码解析
        const imageData = this.thisContext.getImageData(0, 0, img.width, img.height)

        // 使用 jsQR 解析二维码
        const code = jsQR(imageData.data, imageData.width, imageData.height)

        if (code) {
          this.$message.success('二维码内容: ' + code.data)
          this.queryScenicOrderDetail(code.data)
        } else {
          this.$message.warning('未检测到二维码')
        }
      }
      img.src = this.localImageSrc
    },
    queryScenicOrderDetail (orderCode) {
      this.$get('/cos/scenic-order/queryScenicOrderDetail', { orderCode: orderCode }).then((r) => {
        this.orderDetail = r.data.data
        console.log(this.orderDetail)
      })
    },
    verifyOrder () {
      this.verifyLoading = true
      this.$get('/cos/scenic-order/verifyOrder', {
        orderCode: this.orderDetail.code
      }).then((r) => {
        this.verifyLoading = false
        this.$message.success('订单核销成功')
        // 更新订单状态
        this.orderDetail.orderStatus = 2
        // 可以选择重新查询订单详情以确保数据一致性
        this.queryScenicOrderDetail(this.orderDetail.code)
      }).catch((error) => {
        this.verifyLoading = false
        this.$message.error('核销请求失败: ' + error.message)
      })
    }
  },
  watch: {}
}
</script>
<style lang="less" scoped>
@import "../../../../static/less/Common";
.order-detail-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.order-card {
  height: 100%;
  display: flex;
  flex-direction: column;

  .ant-card-body {
    flex: 1;
    overflow-y: auto;
  }
}

.order-code {
  color: #1890ff;
  font-weight: 500;
}

.price {
  color: #ff4d4f;
  font-size: 16px;
  font-weight: 600;
}

.scenic-name, .user-name {
  color: #333;
  font-weight: 500;
}

.scenic-info-section {
  margin-top: 20px;
}

.scenic-card {
  background-color: #f8f9fa;
  border-radius: 8px;

  .ant-card-head {
    min-height: 36px;
    padding: 0 12px;

    .ant-card-head-title {
      padding: 8px 0;
      font-size: 14px;
    }
  }
}

.scenic-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.scenic-image {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  margin-right: 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.scenic-details {
  flex: 1;

  .scenic-title {
    font-weight: 600;
    margin-bottom: 4px;
    color: #333;
  }

  .scenic-level {
    font-size: 12px;
    color: #666;
    margin-bottom: 2px;
  }

  .scenic-sold {
    font-size: 12px;
    color: #999;
  }
}

.empty-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #999;
}

// 表格样式方案
.order-info-table {
  .info-table {
    width: 100%;
    border-collapse: collapse;

    tr {
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }
    }

    td {
      padding: 8px 0;

      &.label {
        width: 80px;
        color: #666;
        font-size: 13px;
      }

      &.value {
        color: #333;
      }
    }
  }
}

// 卡片块样式方案
.order-info-cards {
  .info-item {
    display: flex;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .label {
      width: 80px;
      color: #666;
      font-size: 13px;
    }

    .value {
      flex: 1;
      color: #333;
    }
  }
}

.verification-actions {
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;

  .ant-alert {
    margin-top: 10px;
  }
}
</style>
