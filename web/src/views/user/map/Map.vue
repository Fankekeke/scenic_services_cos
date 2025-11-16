<template>
  <a-row :gutter="8" class="count-info" style="width: 100%;margin-top: 35px">
    <a-col :span="10" class="visit-count-wrapper">
      <a-card style="height: 825px; overflow-y: auto;">
        <div v-if="scenicShow" class="scenicInfo detail-card">
          <img :src="scenicData.webImg" alt="" class="detail-image">
          <a-card :title="scenicData.name" class="content-card">
            <a slot="extra" @click="scenicBack" class="back-link">← 返回</a>
            <a-tabs default-active-key="1" class="detail-tabs">
              <a-tab-pane key="1" tab="基础信息">
                <ul class="info-list">
                  <li><span class="info-label">地址：</span>{{ scenicData.address }}</li>
                  <li><span class="info-label">等级：</span>{{ scenicData.level }}</li>
                  <!--            <li><span class="info-label">开园时间：</span>{{ scenicData.startDate }} ~ {{ scenicData.endDate }}</li>-->
                  <li><span class="info-label">门票：</span><span class="price">{{ scenicData.scenicPrice }} 元</span>
                  </li>
                  <li><span class="info-label">景区介绍：</span>{{ scenicData.history }}</li>
                </ul>
                <div id="areas" style="width: 100%;height: 310px;box-shadow: 0 0 0 10px white;"></div>
              </a-tab-pane>
              <a-tab-pane key="2" tab="路线规划">
                <a-timeline class="route-timeline">
                  <a-timeline-item v-for="(item,index) in roadData" :key="index">
                    <div v-html="item" class="route-step"></div>
                  </a-timeline-item>
                </a-timeline>
              </a-tab-pane>
            </a-tabs>
          </a-card>
        </div>
        <div v-if="hotelShow" class="scenicInfo detail-card" style="height: 825px; overflow-y: auto">
          <img :src="'http://127.0.0.1:9527/imagesWeb/'+hotelData.images.split(',')[0]" alt="" class="detail-image">
          <a-card :title="hotelData.name" class="content-card">
            <a slot="extra" @click="hotelBack" class="back-link">← 返回</a>
            <div>
               <a-button
                type="primary"
                icon="message"
                @click="openCommunication(hotelData)"    style="margin-top: 10px; width: 100%">
                在线沟通
              </a-button>
            </div>
            <a-tabs default-active-key="1" class="detail-tabs">
              <a-tab-pane key="1" tab="基础信息">
                <ul class="info-list">
                  <li><span class="info-label">地址：</span>{{ hotelData.address }}</li>
                  <li><span class="info-label">备注：</span>{{ hotelData.content }}</li>
                  <li><span class="info-label">政策：</span>{{ hotelData.policy }}</li>
                </ul>
                <div id="areas" style="width: 100%;height: 310px;box-shadow: 0 0 0 10px white;"></div>
              </a-tab-pane>
              <a-tab-pane key="2" tab="路线规划">
                <a-timeline class="route-timeline">
                  <a-timeline-item v-for="(item,index) in roadData" :key="index">
                    <div v-html="item" class="route-step"></div>
                  </a-timeline-item>
                </a-timeline>
              </a-tab-pane>
            </a-tabs>
          </a-card>
        </div>
        <div class="search" style="margin-bottom: 16px;" v-if="!scenicShow && !hotelShow">
            <a-input-search
              v-model="searchKeyword"
              placeholder="输入景点或酒店名称搜索" style="width: 100%"
              @search="onSearch"
            />
          </div>
        <a-tabs default-active-key="1" v-if="!scenicShow && !hotelShow">
          <a-tab-pane key="1" tab="景点" class="list-tab">
            <a-card
              @click="scenicDetail(item)"
              hoverable
              class="item-card"
              v-for="(item, index) in filteredScenicList"
              :key="index"
            >
              <div style="display: flex; align-items: flex-start;">
                <a-popover v-if="item.webImg !== null">
                  <template slot="content">
                    <a-avatar shape="square" :size="132" icon="user" :src="item.webImg"/>
                  </template>
                  <a-avatar
                    shape="square"
                    :size="80"
                    icon="user"
                    class="item-avatar"
                    :src="item.webImg"
                  />
                </a-popover>
                <div class="item-content">
                  <a-card-meta
                    :title="item.scenicName"
                    :description="item.history.slice(0, 40)+'...'"
                    class="item-meta"
                  />
                  <div class="item-details">
                    <p class="price-tag">{{ item.scenicPrice }} 元</p>
                    <p class="level-tag">等级：{{ item.level }}</p>
                  </div>
                </div>
              </div>
            </a-card>
          </a-tab-pane>

          <!-- 酒店列表项美化 -->
          <a-tab-pane key="2" tab="酒店" class="list-tab">
            <a-card
              @click="hotelDetail(item)"
              hoverable
              class="item-card"
              v-for="(item, index) in filteredHotelList"
              :key="index"
              v-if="item.images !== null"
            >
              <div style="display: flex; align-items: flex-start;">
                <div style="display: flex;">
                  <a-popover
                    v-for="(item1, index1) in item.images.split(',')"
                    :key="index1"
                    v-if="item1"
                  >
                    <template slot="content">
                      <a-avatar shape="square" :size="132" icon="user" :src="'http://127.0.0.1:9527/imagesWeb/'+item1"/>
                    </template>
                    <a-avatar
                      shape="square"
                      :size="70"
                      icon="user"
                      class="item-avatar"
                      :src="'http://127.0.0.1:9527/imagesWeb/'+item1"
                    />
                  </a-popover>
                </div>
                <div class="item-content">
                  <a-card-meta
                    :title="item.name"
                    :description="item.content.slice(0, 25)+'...'"
                    class="item-meta"
                  />
                </div>
              </div>
            </a-card>
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </a-col>
    <a-col :span="14" class="visit-count-wrapper" style="padding-left: 24px !important;">
      <div class="scenic-evaluate-section" v-if="scenicEvaluations.length > 0">
        <h3>游客评价</h3>
        <div class="evaluate-list">
          <a-card
            v-for="(evaluation, index) in scenicEvaluations"
            :key="index"
            class="evaluate-item"
          >
            <div class="evaluate-header">
              <a-avatar :src="`http://127.0.0.1:9527/imagesWeb/${evaluation.avatar}`"/>
              <div class="evaluate-user-info">
                <div class="evaluate-user-name">{{ evaluation.userName }}</div>
                <a-rate :value="evaluation.score" disabled/>
              </div>
            </div>
            <div class="evaluate-content">
              {{ evaluation.content }}
            </div>
            <div class="evaluate-footer">
              <span class="evaluate-date">{{ evaluation.createDate }}</span>
            </div>
          </a-card>
        </div>
      </div>
      <div class="empty-state" v-else>
        <div class="empty-icon">
          <a-icon type="info-circle" style="font-size: 48px; color: #bfbfbf;"/>
        </div>
        <div class="empty-description">
          <p>暂无评价</p>
        </div>
      </div>
    </a-col>
  </a-row>
</template>

<script>
import {mapState} from 'vuex'
import moment from 'moment'
import baiduMap from '@/utils/map/baiduMap'

export default {
  name: 'Map',
  data () {
    return {
      searchKeyword: '',
      scenicEvaluations: [],
      scenicList: [],
      hotelList: [],
      scenicShow: false,
      scenicData: null,
      hotelShow: false,
      hotelData: null,
      roadData: [],
      nowPoint: null
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    }),
    filteredScenicList () {
      if (!this.searchKeyword) return this.scenicList
      return this.scenicList.filter(item =>
        item.scenicName && item.scenicName.includes(this.searchKeyword)
      )
    },
    filteredHotelList () {
      if (!this.searchKeyword) return this.hotelList
      return this.hotelList.filter(item =>
        item.name && item.name.includes(this.searchKeyword)
      )
    }
  },
  methods: {
    openCommunication (schedule) {
      this.$post('/cos/chat-record/defaultChat', {
        hotelId: schedule.id,
        userId: this.currentUser.userId,
        senderType: 0,
        content: '你好'
      }).then((r) => {
        // 跳转到聊天页面，并传递默认消息
        this.$router.push({
          path: '/user/chat'
        })
      })
    },
    onSearch (value) {
      this.searchKeyword = value
    },
    scenicBack () {
      this.scenicShow = false
      this.scenicEvaluations = []
    },
    scenicDetail (row) {
      this.scenicData = row
      this.scenicShow = true
      this.queryEvaluateByScenicId(row.id)
      setTimeout(() => {
        baiduMap.initMap('areas')
        this.getLocal()
        setTimeout(() => {
          this.local(row)
        }, 300)
      }, 200)
    },
    hotelBack () {
      this.hotelShow = false
      this.scenicEvaluations = []
    },
    hotelDetail (row) {
      this.hotelData = row
      this.hotelShow = true
      this.queryEvaluateByHotelId(row.id)
      setTimeout(() => {
        baiduMap.initMap('areas')
        this.getLocal()
        setTimeout(() => {
          this.local(row)
        }, 300)
      }, 200)
    },
    getLocal () {
      // eslint-disable-next-line no-undef
      let geolocation = new BMap.Geolocation()
      geolocation.getCurrentPosition(r => {
        this.nowPoint = r.point
      }, {enableHighAccuracy: true})
    },
    local (scenic) {
      baiduMap.clearOverlays()
      baiduMap.rMap().enableScrollWheelZoom(true)
      // eslint-disable-next-line no-undef
      let driving = new BMap.DrivingRoute(baiduMap.rMap(), {renderOptions: {map: baiduMap.rMap(), autoViewport: true}})
      // eslint-disable-next-line no-undef
      driving.search(new BMap.Point(this.nowPoint.lng, this.nowPoint.lat), new BMap.Point(scenic.point.split(',')[0], scenic.point.split(',')[1]))
      this.getRoadData()
    },
    queryEvaluateByScenicId (scenicId) {
      this.$get('/cos/evaluation/queryEvaluateByScenicId', {scenicId}).then((r) => {
        this.scenicEvaluations = r.data.data || []
      })
    },
    queryEvaluateByHotelId (hotelId) {
      this.$get('/cos/evaluation/queryEvaluateByHotelId', {hotelId}).then((r) => {
        this.scenicEvaluations = r.data.data || []
      })
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
      if (this.scenicShow) {
        end = new BMap.Point(this.scenicData.point.split(',')[0], this.scenicData.point.split(',')[1])
      } else {
        end = new BMap.Point(this.hotelData.point.split(',')[0], this.hotelData.point.split(',')[1])
      }
      // eslint-disable-next-line no-undef
      driving.search(start, end)
    },
    home () {
      this.loading = true
      this.$get('/cos/order-info/home', {type: 74, userId: 12}).then((r) => {
        this.scenicList = r.data.scenic
        this.hotelList = r.data.hotel
        setTimeout(() => {
          this.loading = false
        }, 800)
      })
    }
  },
  mounted () {
    this.home()
  }
}
</script>

<style scoped lang="less">
.detail-card {
  .detail-image {
    width: 100%;
    height: 180px;
    object-fit: cover;
    border-radius: 8px 8px 0 0;
  }

  .content-card {
    border-radius: 0 0 8px 8px;

    .back-link {
      color: #1890ff;
      font-size: 14px;

      &:hover {
        color: #40a9ff;
      }
    }
  }

  .detail-tabs {
    .ant-tabs-tab {
      font-size: 16px;
      font-weight: 500;
    }
  }

  .info-list {
    list-style: none;
    padding: 0;

    li {
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .info-label {
        font-weight: bold;
        color: #595959;
      }

      .price {
        color: #ff4d4f;
        font-weight: bold;
        font-size: 16px;
      }
    }
  }

  .route-timeline {
    .route-step {
      font-size: 14px;
      line-height: 1.6;
    }
  }
}

.item-card {
  margin-bottom: 15px;
  border-radius: 8px;
  transition: all 0.3s ease;
  border: 1px solid #e8e8e8;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    border-color: #1890ff;
  }

  .item-avatar {
    margin-bottom: 15px;
    margin-right: 15px;
    border: 2px solid #f0f0f0;
    border-radius: 4px;
  }

  .item-content {
    display: flex;
    flex-direction: column;

    .item-meta {
      .ant-card-meta-title {
        font-size: 16px;
        font-weight: 600;
        color: #262626;
      }

      .ant-card-meta-description {
        color: #8c8c8c;
        font-size: 13px;
        line-height: 1.4;
      }
    }

    .item-details {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 10px;

      .price-tag {
        color: #ff4d4f;
        font-weight: bold;
        font-size: 16px;
        background-color: #fff1f0;
        padding: 2px 8px;
        border-radius: 4px;
      }

      .level-tag {
        color: #595959;
        font-size: 12px;
        background-color: #f5f5f5;
        padding: 2px 8px;
        border-radius: 4px;
      }
    }
  }
}

.scenic-evaluate-section {
  margin-top: 20px;

  h3 {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 15px;
    color: #262626;
  }

  .evaluate-list {
    .evaluate-item {
      margin-bottom: 15px;
      border-radius: 8px;
      border: 1px solid #f0f0f0;

      .evaluate-header {
        display: flex;
        align-items: center;
        margin-bottom: 10px;

        .ant-avatar {
          margin-right: 12px;
        }

        .evaluate-user-info {
          .evaluate-user-name {
            font-weight: 500;
            margin-bottom: 4px;
          }

          .ant-rate {
            font-size: 12px;
          }
        }
      }

      .evaluate-content {
        margin-bottom: 10px;
        color: #595959;
        line-height: 1.5;
      }

      .evaluate-footer {
        display: flex;
        justify-content: space-between;
        font-size: 12px;
        color: #8c8c8c;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;

  .empty-icon {
    margin-bottom: 16px;
  }

  .empty-description {
    p {
      color: #8c8c8c;
      font-size: 14px;
      text-align: center;
      margin: 0;
    }
  }
}
</style>
