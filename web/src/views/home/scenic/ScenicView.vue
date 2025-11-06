<template>
  <a-drawer
    placement="right"
    width="100%"
    :closable="false"
    :visible="show"
    @close="onClose"
    wrapClassName="aa"
    :getContainer="false"
  >
    <div style="width: 100%;height: 100vh;overflow: hidden">
      <a-icon type="arrow-left" style="position: absolute;z-index: 999;color: red;font-size: 20px;margin: 15px" @click="onClose"/>
      <a-row style="height:100vh;font-family: SimHei;overflow-y: auto">
        <a-col :span="8" style="height: 100%;">
          <div id="areas" style="width: 100%;height: 100%;background:#ec9e3c;color:#fff"></div>
        </a-col>
        <a-col :span="8" style="height: 100vh;color: black;overflow: auto">
          <div style="font-size: 13px" v-if="scenicData !== null">
            <a-carousel autoplay style="height: 250px;" v-if="scenicData.webImg !== undefined && scenicData.webImg !== ''">
              <div style="width: 100%;height: 250px">
                <img :src="scenicData.webImg" style="width: 100%;height: 100%">
              </div>
            </a-carousel>
            <a-card :title="scenicData.scenicName" :bordered="false">
              <a slot="extra" @click="rentNavigation" style="margin-right: 10px">导航</a>
              <a slot="extra" @click="onClose">返回</a>
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
            <a-row style="padding-left: 24px; padding-right: 24px; background: #f8f9fa; border-radius: 8px; padding: 20px; margin-top: 15px;">
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
            </a-row>
            <a-row style="padding-left: 24px; padding-right: 24px;">
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
          </div>
        </a-col>
        <a-col :span="8">
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
        </a-col>
      </a-row>
    </div>
  </a-drawer>
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
      nowPoint: null,
      loading: false,
      weatherData: null,
      studentList: []
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
  methods: {
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
          key = '餐饮'
          break
        case '3':
          key = '住宿'
          break
        case '4':
          key = '医疗'
          break
      }
      console.log(this.scenicData.point.split(',')[0], this.scenicData.point.split(',')[1])
      // baiduMap.searchNear(this.scenicData.point.split(',')[0], this.scenicData.point.split(',')[1], key)
      baiduMap.searchNear(121.488206, 31.239463, key)
    },
    rentNavigation () {
      this.navigation(this.scenicData)
    },
    navigation (data) {
      baiduMap.clearOverlays()
      baiduMap.rMap().enableScrollWheelZoom(true)
      // eslint-disable-next-line no-undef
      let driving = new BMap.DrivingRoute(baiduMap.rMap(), {renderOptions: {map: baiduMap.rMap(), autoViewport: true}})
      // eslint-disable-next-line no-undef
      let point = new BMap.Point(data.point.split(',')[0], data.point.split(',')[1])
      driving.search(new BMap.Point(this.nowPoint.lng, this.nowPoint.lat), point)
      this.getRoadData()
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
</style>
