<template>
<!--  <a-drawer-->
<!--    placement="right"-->
<!--    width="100%"-->
<!--    :closable="false"-->
<!--    :visible="show"-->
<!--    @close="onClose"-->
<!--    wrapClassName="aa"-->
<!--    :getContainer="false"-->
<!--  >-->
  <a-modal :visible="show" width="80%" :footer="null" @cancel="onClose" :bodyStyle="{ padding: 0 }">
    <div style="width: 100%;height: 85vh;overflow: hidden">
      <a-icon type="arrow-left" style="position: absolute;z-index: 999;color: red;font-size: 20px;margin: 15px" @click="onClose"/>
      <a-row style="height:85vh;font-family: SimHei;overflow-y: auto">
        <a-col :span="14" style="height: 100%;">
          <div id="areas" style="width: 100%;height: 100%;background:#ec9e3c;color:#fff"></div>
        </a-col>
        <a-col :span="10" style="height: 85vh;color: black;overflow: auto">
          <div style="font-size: 13px" v-if="scenicData !== null">
            <a-carousel autoplay style="height: 250px;" v-if="scenicData.webImg !== undefined && scenicData.webImg !== ''">
              <div style="width: 100%;height: 250px">
                <img :src="scenicData.webImg" style="width: 100%;height: 100%">
              </div>
            </a-carousel>
            <a-card :title="scenicData.scenicName" :bordered="false">
              <div slot="extra">
                <!-- 导航方式选择 -->
                <a-radio-group
                  v-model="navigationMode"
                  style="margin-right: 10px;"
                  size="small"
                  @change="onNavigationModeChange"
                >
                  <a-radio-button value="driving">驾车</a-radio-button>
                  <a-radio-button value="riding">骑行</a-radio-button>
                  <a-radio-button value="walking">步行</a-radio-button>
                </a-radio-group>

                <a @click="rentNavigation" style="margin-right: 10px">导航</a>
                <a @click="onClose">返回</a>
              </div>
            </a-card>
            <a-row style="padding-left: 24px;padding-right: 24px;margin-top: 15px">
              <a-col style="margin-bottom: 15px"><span style="font-size: 14px;font-weight: 650;color: #000c17">周边设施</span></a-col>
              <a-col :span="24">
                <a-radio-group button-style="solid" style="width: 100%" @change="gisOnChange">
                  <a-radio-button value="1" style="width: 25%;text-align: center">
                    交通
                  </a-radio-button>
                  <a-radio-button value="2" style="width: 25%;text-align: center">
                    餐饮
                  </a-radio-button>
                  <a-radio-button value="3" style="width: 25%;text-align: center">
                    住宿
                  </a-radio-button>
                  <a-radio-button value="4" style="width: 25%;text-align: center">
                    医疗
                  </a-radio-button>
                </a-radio-group>
              </a-col>
            </a-row>
            <a-row style="padding-left: 24px; padding-right: 24px; margin-top: 15px; background: #f8f9fa; border-radius: 8px; padding: 20px;">
              <a-col style="margin-bottom: 15px">
                <span style="font-size: 18px; font-weight: 600; color: #000c17; border-left: 4px solid #1890ff; padding-left: 10px;">基础信息</span>
              </a-col>
              <a-col :span="8">
                <div style="padding: 10px 0;">
                  <div style="font-size: 12px; color: #666; margin-bottom: 3px;">景点名称</div>
                  <div style="font-size: 14px; color: #333;">{{ scenicData.scenicName }}</div>
                </div>
              </a-col>
              <a-col :span="8">
                <div style="padding: 10px 0;">
                  <div style="font-size: 12px; color: #666; margin-bottom: 3px;">门票价格</div>
                  <div style="font-size: 14px; color: #ff4d4f;">￥{{ scenicData.scenicPrice }}</div>
                </div>
              </a-col>
              <a-col :span="8">
                <div style="padding: 10px 0;">
                  <div style="font-size: 12px; color: #666; margin-bottom: 3px;">具体地址</div>
                  <a-tooltip>
                    <template slot="title">
                      {{ scenicData.address }}
                    </template>
                    <div style="font-size: 14px; color: #333;">{{ scenicData.address.slice(0, 8) }} ...</div>
                  </a-tooltip>
                </div>
              </a-col>
            </a-row>
            <br/>
            <a-row style="padding-left: 24px; padding-right: 24px; background: #f8f9fa; border-radius: 8px; padding: 20px;">
              <a-col style="margin-bottom: 15px">
                <span style="font-size: 18px; font-weight: 600; color: #000c17; border-left: 4px solid #1890ff; padding-left: 10px;">数据指标</span>
              </a-col>
              <a-col :span="8">
                <div style="padding: 10px 0;">
                  <div style="font-size: 12px; color: #666; margin-bottom: 3px;">所属地区</div>
                  <div style="font-size: 14px; color: #333;">{{ scenicData.area !== null ? scenicData.area : '- -' }}</div>
                </div>
              </a-col>
              <a-col :span="8">
                <div style="padding: 10px 0;">
                  <div style="font-size: 12px; color: #666; margin-bottom: 3px;">热度</div>
                  <div style="font-size: 14px; color: #333;">{{ scenicData.hot !== null ? scenicData.hot : '- -' }}</div>
                </div>
              </a-col>
              <a-col :span="8">
                <div style="padding: 10px 0;">
                  <div style="font-size: 12px; color: #666; margin-bottom: 3px;">等级</div>
                  <div style="font-size: 14px; color: #333;">{{ scenicData.level !== null ? scenicData.level : '- -' }}</div>
                </div>
              </a-col>
              <a-col :span="8">
                <div style="padding: 10px 0;">
                  <div style="font-size: 12px; color: #666; margin-bottom: 3px;">游览量</div>
                  <div style="font-size: 14px; color: #333;">{{ scenicData.sold !== null ? scenicData.sold : '- -' }}点</div>
                </div>
              </a-col>
            </a-row>
            <br/>
            <a-row style="padding-left: 24px; padding-right: 24px; background: #f8f9fa; border-radius: 8px; padding: 20px;">
              <a-col style="margin-bottom: 15px">
                <span style="font-size: 18px; font-weight: 600; color: #000c17; border-left: 4px solid #1890ff; padding-left: 10px;">文化历史</span>
              </a-col>
              <a-col :span="24">
                <div style="padding: 15px 0; line-height: 1.6; color: #333;">
                  {{ scenicData.history !== null ? scenicData.history : '暂无文化历史信息' }}
                </div>
              </a-col>
            </a-row>
            <a-row style="padding-left: 24px; padding-right: 24px; background: #f8f9fa; padding: 20px;margin-top: 25px">
              <a-col style="margin-bottom: 15px">
                <span style="font-size: 18px; font-weight: 600; color: #000c17; border-left: 4px solid #1890ff; padding-left: 10px;">景点介绍导航</span>
                <a-button type="primary" @click="queryAiContent" :loading="aiLoading" class="ai-generate-btn" style="float: right" size="small">
                  <a-icon type="thunderbolt" /> 生成介绍
                </a-button>
              </a-col>
              <a-col :span="24">
                <a-spin :spinning="aiLoading" tip="AI分析中..." class="ai-spin">
                  <div v-if="aiAnalysisResult" class="ai-content">
                    <a-alert type="info" show-icon class="ai-result-alert">
                      <template slot="message">
                        <div v-html="formatAiResult(aiAnalysisResult)" class="ai-result-content"></div>
                        <div style="text-align: right; margin-top: 10px;">
                          <a-button
                            type="primary"
                            shape="circle"
                            :icon="isPlaying ? 'pause' : 'sound'"
                            @click="toggleSpeech"
                            :loading="speechLoading"
                            size="small"
                          >
                          </a-button>
                        </div>
                      </template>
                    </a-alert>
                  </div>
                  <div v-else class="ai-placeholder">
                    <div class="ai-empty-container">
                      <div class="ai-icon-placeholder">
                        <a-icon type="thunderbolt" class="ai-icon" />
                      </div>
                      <p class="ai-placeholder-text">暂无AI分析结果</p>
                    </div>
                  </div>
                </a-spin>
              </a-col>
            </a-row>
            <a-row style="padding-left: 24px; padding-right: 24px; background: #f8f9fa; padding: 20px;margin-top: 15px">
              <a-col style="margin-bottom: 15px">
                <span style="font-size: 18px; font-weight: 600; color: #000c17; border-left: 4px solid #1890ff; padding-left: 10px;">人流量</span>
              </a-col>
              <a-col :span="24">
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
                <a-alert v-if="scenicSold" :message="scenicSold" banner />
              </a-col>
            </a-row>
            <a-row style="padding-left: 24px; padding-right: 24px; background: #f8f9fa; padding: 20px;">
              <a-col style="margin-bottom: 15px">
                <span style="font-size: 18px; font-weight: 600; color: #000c17; border-left: 4px solid #1890ff; padding-left: 10px;">未来天气预报</span>
              </a-col>
              <a-col :span="24" v-if="weatherData && weatherData.data && weatherData.data.forecast">
                <div style="max-height: 400px; overflow-y: auto;">
                  <div
                    v-for="(item, index) in weatherData.data.forecast"
                    :key="index"      style="display: flex; align-items: center; padding: 12px 10px; border-bottom: 1px solid #eee; background: white; margin-bottom: 8px; border-radius: 6px; box-shadow: 0 1px 3px rgba(0,0,0,0.05);"
                  >
                    <div style="flex: 1; text-align: center;">
                      <div style="font-size: 14px; font-weight: 500;">{{ item.ymd }}</div>
                      <div style="font-size: 12px; color: #666; margin-top: 3px;">{{ item.week }}</div>
                    </div>
                    <div style="flex: 1; text-align: center;">
                      <div style="font-size: 14px;">{{ item.type }}</div>
                      <div style="font-size: 12px; color: #999; margin-top: 3px;">{{ item.notice }}</div>
                    </div>
                    <div style="flex: 1; text-align: center;">
                      <div style="font-size: 14px; color: #1890ff;">
                        {{ item.low.replace('低温 ', '') }}~{{ item.high.replace('高温 ', '') }}°C
                      </div>
                      <div style="font-size: 12px; color: #999; margin-top: 3px;">
                        {{ item.fx }} {{ item.fl }}
                      </div>
                    </div>
                    <div style="flex: 0.8; text-align: center;">
                      <div style="font-size: 14px; font-weight: 500;">
                        {{ item.aqi }}
                      </div>
                      <div style="font-size: 12px; color: #999; margin-top: 3px;">AQI</div>
                    </div>
                  </div>
                </div>
              </a-col>
            </a-row>
          </div>
        </a-col>
      </a-row>
    </div>
  </a-modal>
</template>

<script>
import baiduMap from '@/utils/map/baiduMap'

export default {
  name: 'ScenicView',
  props: {
    scenicShow: {
      type: Boolean,
      default: false
    },
    scenicData: {
      type: Object
    }
  },
  computed: {
    show: {
      get: function () {
        return this.scenicShow
      },
      set: function () {
      }
    }
  },
  data () {
    return {
      scenicSold: '',
      videoWidth: 800,
      videoHeight: 300,
      imgSrc: '',
      localImageSrc: '', // 添加本地图片数据
      thisCancas: null,
      thisContext: null,
      thisVideo: null,
      isPlaying: false,
      speechLoading: false,
      speechSynthesis: null,
      utterance: null,
      aiLoading: false, // 添加 AI 加载状态
      aiAnalysisResult: '', // 添加 AI 分析结果
      nowPoint: null,
      loading: false,
      weatherData: null,
      studentList: [],
      navigationMode: 'driving', // 默认为驾车模式
      currentRoute: null
    }
  },
  watch: {
    scenicShow: function (value) {
      if (value) {
        setTimeout(() => {
          baiduMap.initMap('areas')
          this.getLocal()
          this.queryWeather()
          setTimeout(() => {
            this.local(this.scenicData)
          }, 500)
        }, 200)
      }
    }
  },
  beforeDestroy () {
    this.stopSpeech()
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
    scanQRCode() {
      this.thisContext.drawImage(this.thisVideo, 0, 0, this.videoWidth, this.videoHeight)
      const canvas = document.getElementById('canvasCamera')
      // 将canvas转换为Base64格式
      const base64Image = canvas.toDataURL('image/jpeg', 0.9)
      // 发送到后端
      this.$post('/cos/ai/recognitionImage', {
        avatar: base64Image
      }).then((r) => {
        this.scenicSold = r.data.msg
        console.log('识别结果:', r)
        // 处理识别结果
      }).catch(err => {
        console.error('识别失败:', err)
      })
    },
    stopNavigator () {
      this.thisVideo.srcObject.getTracks()[0].stop()
    },
    // 导航方式变更处理
    onNavigationModeChange () {
      if (this.nowPoint) {
        this.navigation(this.scenicData)
      }
    },
    // 修改导航方法以支持多种模式
    navigation (data) {
      if (!this.nowPoint) {
        this.$message.warning('无法获取当前位置，请稍后再试')
        return
      }
      baiduMap.clearOverlays()
      baiduMap.rMap().enableScrollWheelZoom(true)
      // eslint-disable-next-line no-undef
      let startPoint = new BMap.Point(this.nowPoint.lng, this.nowPoint.lat)
      // eslint-disable-next-line no-undef
      let endPoint = new BMap.Point(data.point.split(',')[0], data.point.split(',')[1])
      // 根据选择的模式创建不同的导航实例
      let navigationInstance
      switch (this.navigationMode) {
        case 'driving': // 驾车
          // eslint-disable-next-line no-undef
          navigationInstance = new BMap.DrivingRoute(baiduMap.rMap(), {
            renderOptions: { map: baiduMap.rMap(), autoViewport: true }
          })
          break
        case 'riding': // 骑行
          // eslint-disable-next-line no-undef
          navigationInstance = new BMap.RidingRoute(baiduMap.rMap(), {
            renderOptions: { map: baiduMap.rMap(), autoViewport: true }
          })
          break
        case 'walking': // 步行
          // eslint-disable-next-line no-undef
          navigationInstance = new BMap.WalkingRoute(baiduMap.rMap(), {
            renderOptions: { map: baiduMap.rMap(), autoViewport: true }
          })
          break
        default:
          // 默认使用驾车模式
          // eslint-disable-next-line no-undef
          navigationInstance = new BMap.DrivingRoute(baiduMap.rMap(), {
            renderOptions: { map: baiduMap.rMap(), autoViewport: true }
          })
      }
      // 执行导航搜索
      navigationInstance.search(startPoint, endPoint)
      // 保存当前导航实例，以便后续使用
      this.currentRoute = navigationInstance
    },
    // 添加文字转语音功能
    toggleSpeech () {
      if (!this.aiAnalysisResult) {
        this.$message.warning('暂无内容可播放')
        return
      }

      if (!this.speechSynthesis) {
        this.speechSynthesis = window.speechSynthesis
      }

      if (this.isPlaying) {
        // 停止播放
        this.stopSpeech()
      } else {
        // 开始播放
        this.playSpeech()
      }
    },

    playSpeech () {
      this.speechLoading = true

      try {
        // 清理HTML标签，只保留纯文本
        const plainText = this.aiAnalysisResult.replace(/<[^>]*>/g, '')

        // 创建语音对象
        this.utterance = new SpeechSynthesisUtterance(plainText)
        this.utterance.lang = 'zh-CN'
        this.utterance.rate = 1
        this.utterance.pitch = 1

        // 设置事件监听
        this.utterance.onstart = () => {
          this.isPlaying = true
          this.speechLoading = false
        }

        this.utterance.onend = () => {
          this.isPlaying = false
        }

        this.utterance.onerror = () => {
          this.isPlaying = false
          this.speechLoading = false
          // this.$message.error('语音播放出错')
        }

        // 开始播放
        this.speechSynthesis.speak(this.utterance)
      } catch (error) {
        this.speechLoading = false
        this.$message.error('浏览器不支持语音播放功能')
        console.error('Speech synthesis error:', error)
      }
    },

    stopSpeech () {
      if (this.speechSynthesis && this.utterance) {
        this.speechSynthesis.cancel()
        this.isPlaying = false
      }
    },
    formatAiResult (result) {
      if (!result) return ''
      // 简单的换行符替换为 HTML 换行
      return result.replace(/\n/g, '<br>')
    },
    queryAiContent () {
      this.aiLoading = true
      let weatherText = '天气预测：\n'
      if (this.weatherData.data.forecast) {
        for (let i = 0; i < 3; i++) {
          weatherText += this.weatherData.data.forecast[i].ymd + '，' + this.weatherData.data.forecast[i].type + '，' + this.weatherData.data.forecast[i].high + '，' + this.weatherData.data.forecast[i].low + '\n'
        }
      }
      let params = '请介绍景点-' + this.scenicData.scenicName + '，并规划' + this.scenicData.address + '附近公交车 地铁站，500字内' + weatherText
      this.$post(`/cos/ai/aliTyqw`, {
        content: params
      }).then((r) => {
        this.aiAnalysisResult = r.data.msg
      }).catch((error) => {
        this.$message.error('AI分析失败，请稍后重试')
        console.error('AI分析错误:', error)
      }).finally(() => {
        this.aiLoading = false
      })
    },
    queryWeather () {
      this.$get('/cos/weather-info/queryWeatherByCity', {
        city: '朝阳'
      }).then(r => {
        this.weatherData = r.data.data
        console.log(this.weatherData.data.forecast)
        // this.weatherData = r.data.data.data.forecast
      })
    },
    getLocal () {
      // eslint-disable-next-line no-undef
      let geolocation = new BMap.Geolocation()
      geolocation.getCurrentPosition(r => {
        this.nowPoint = r.point
      }, {enableHighAccuracy: true})
    },
    gisOnChange (e) {
      let key = ''
      switch (e.target.value) {
        case '1':
          key = '公交站'
          break
        case '2':
          key = '地铁'
          break
        case '3':
          key = '住宿'
          break
        case '4':
          key = '景点'
          break
      }
      console.log(this.scenicData.point.split(',')[0], this.scenicData.point.split(',')[1])
      // baiduMap.searchNear(this.scenicData.point.split(',')[0], this.scenicData.point.split(',')[1], key)
      baiduMap.searchNear(this.scenicData.point.split(',')[0], this.scenicData.point.split(',')[1], key)
    },
    rentNavigation () {
      this.navigation(this.scenicData)
    },
    getRoadData () {
      let options = {
        onSearchComplete: results => {
          // eslint-disable-next-line eqeqeq,no-undef
          if (driving.getStatus() == BMAP_STATUS_SUCCESS) {
            // 获取第一条方案
            let plan = results.getPlan(0)
            // 获取方案的驾车线路
            // eslint-disable-next-line no-unused-vars
            let route = plan.getRoute(0)
            // 获取每个关键步骤,并输出到页面
            let s = []
            for (let j = 0; j < plan.getNumRoutes(); j++) {
              let route = plan.getRoute(j)
              for (let i = 0; i < route.getNumSteps(); i++) {
                let step = route.getStep(i)
                s.push((i + 1) + '. ' + step.getDescription())
              }
            }
            this.roadData = s
          }
        }
      }
      // eslint-disable-next-line no-undef
      let driving = new BMap.DrivingRoute(baiduMap.rMap(), options)
      // eslint-disable-next-line no-undef
      let start = new BMap.Point(this.nowPoint.lng, this.nowPoint.lat)
      let end = null
      if (this.rentShow) {
        end = new BMap.Point(this.rentData.longitude, this.rentData.latitude)
      } else {
        end = new BMap.Point(this.communityData.longitude, this.communityData.latitude)
      }
      // eslint-disable-next-line no-undef
      driving.search(start, end)
    },
    local (scenic) {
      baiduMap.clearOverlays()
      baiduMap.rMap().enableScrollWheelZoom(true)
      // eslint-disable-next-line no-undef
      let point = new BMap.Point(scenic.point.split(',')[0], scenic.point.split(',')[1])
      baiduMap.pointAdd(point)
      baiduMap.findPoint(point, 16)
      // let driving = new BMap.DrivingRoute(baiduMap.rMap(), {renderOptions:{map: baiduMap.rMap(), autoViewport: true}});
      // driving.search(new BMap.Point(this.nowPoint.lng,this.nowPoint.lat), new BMap.Point(scenic.point.split(",")[0],scenic.point.split(",")[1]));
    },
    onClose () {
      this.aiAnalysisResult = ''
      this.scenicSold = ''
      this.$emit('close')
    }
  }
}
</script>

<style scoped>
>>> .ant-drawer-body {
  padding: 0 !important;
}
>>> .ant-card-meta-title {
  font-size: 13px;
  font-family: SimHei;
}
>>> .ant-card-meta-description {
  font-size: 12px;
  font-family: SimHei;
}
>>> .ant-divider-with-text-left {
  margin: 0;
}

>>> .ant-card-head-title {
  font-size: 13px;
  font-family: SimHei;
}
>>> .ant-card-extra {
  font-size: 13px;
  font-family: SimHei;
}
>>> .ant-radio-button-wrapper {
  border-radius: 0;
}

>>> .ai-placeholder {
  padding: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 120px;
}

>>> .ai-empty-container {
  text-align: center;
  padding: 20px;
}

>>> .ai-icon-placeholder {
  margin-bottom: 12px;
}

>>> .ai-icon {
  font-size: 32px;
  color: #1890ff;
  opacity: 0.6;
}

>>> .ai-placeholder-text {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 16px;
  font-weight: normal;
}
</style>
