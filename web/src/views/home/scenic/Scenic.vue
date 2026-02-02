<template>
  <div>
    <a-form layout="horizontal">
      <a-row :gutter="15" style="margin: 0">
        <div>
          <a-col :md="6" :sm="24">
            <a-form-item
              label="景点名称"
              :labelCol="{span: 5}"
              :wrapperCol="{span: 17, offset: 2}">
              <a-input v-model="queryParams.scenicName"/>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="24">
            <a-form-item
              label="景区地址"
              :labelCol="{span: 5}"
              :wrapperCol="{span: 17, offset: 2}">
              <a-input v-model="queryParams.address"/>
            </a-form-item>
          </a-col>
        </div>
        <span style="float: right; margin-top: 3px;">
            <a-button type="primary" @click="search">查询</a-button>
          </span>
      </a-row>
    </a-form>
    <a-row :gutter="15">
      <a-skeleton active v-if="loading" />
      <a-col :span="6" v-for="(item, index) in scenicData" :key="index" style="margin-top: 30px" v-if="!loading">
        <a-card hoverable bordered class="scenic-card">
          <div class="price-tag">¥{{ item.scenicPrice }}</div>
          <div class="hot-tag">{{ item.hot }}</div>
          <img
            slot="cover"
            alt="example"
            height="160"
            :src="item.webImg"
          />
          <template slot="actions" class="ant-card-actions">
            <a-icon key="shopping-cart" type="shopping-cart" @click="orderAdd(item)"/>
            <a-icon key="ellipsis" type="ellipsis" @click="view(item)"/>
          </template>
          <a-card-meta :title="item.scenicName">
            <template slot="description">
              <div>{{ item.address.slice(0, 18) }}...</div>
              <div style="margin-top: 5px; font-size: 12px; color: #999;">已售: {{ item.sold }}</div>
            </template>
          </a-card-meta>
        </a-card>
      </a-col>
    </a-row>
    <br/>
    <br/>
    <a-pagination :default-current="pagination.defaultCurrent" :defaultPageSize="pagination.defaultPageSize" :total="pagination.total" @change="pageChange" style="float: right"/>
    <scenic-info
      :scenicData="scenicView.data"
      :scenicShow="scenicView.visiable"
      @close="handlescenicViewClose">
    </scenic-info>
    <order-add
      :scenicData="orderView.data"
      :scenicShow="orderView.visiable"
      @close="handleOrderViewClose">
    </order-add>
  </div>
</template>

<script>
import ScenicInfo from './ScenicView'
import OrderAdd from './OrderAdd'
import {mapState} from 'vuex'
export default {
  name: 'Scenic',
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    })
  },
  components: {ScenicInfo, OrderAdd},
  data () {
    return {
      queryParams: {},
      loading: false,
      scenicData: [],
      scenicView: {
        visiable: false,
        data: null
      },
      orderView: {
        visiable: false,
        data: null
      },
      pagination: {
        defaultCurrent: 1,
        defaultPageSize: 20,
        total: 0
      }
    }
  },
  mounted () {
    // this.fetch()
    this.getLocal()
  },
  methods: {
    getLocal () {
      let geolocation = new BMap.Geolocation()
      geolocation.getCurrentPosition(r => {
        console.log(r)
        console.log(r.point)
        if (r.point) {
          this.queryScenicRecommend(r.point.lat, r.point.lng)
        }
      }, {enableHighAccuracy: true})
    },
    queryScenicRecommend (lat, lng) {
      this.$get('/cos/scenic-info/queryScenicRecommend', {
        lat,
        lng,
        userId: this.currentUser.userId
      }).then((r) => {
        this.pagination.total = 1
        this.scenicData = r.data.data
      })
    },
    view (record) {
      this.scenicView.data = record
      this.scenicView.visiable = true
    },
    handlescenicViewClose () {
      this.scenicView.visiable = false
    },
    orderAdd (record) {
      this.orderView.data = record
      this.orderView.visiable = true
    },
    handleOrderViewClose () {
      this.orderView.visiable = false
    },
    pageChange (page, pageSize) {
      this.pagination.defaultCurrent = page
      this.fetch({
        ...this.queryParams
      })
    },
    search () {
      this.fetch({
        ...this.queryParams
      })
    },
    fetch (params = {}) {
      // 显示loading
      this.loading = true
      params.size = this.pagination.defaultPageSize
      params.current = this.pagination.defaultCurrent
      this.$get('/cos/scenic-info/page', {
        ...params
      }).then((r) => {
        let data = r.data.data
        this.pagination.total = data.total
        this.scenicData = data.records
        // 数据加载完毕，关闭loading
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>
  /deep/ .ant-card-meta-title {
    font-size: 14px;
    margin-top: 10px;
  }
  /deep/ .ant-card-meta-description {
    font-size: 13px;
    margin-bottom: 8px;
  }
</style>

<style scoped>  /deep/ .ant-card-meta-title {
  font-size: 16px;
  font-weight: 600;
  margin-top: 12px;
  color: #333;
}

/deep/ .ant-card-meta-description {
  font-size: 13px;
  margin-bottom: 8px;
  color: #666;
  height: 40px;
  overflow: hidden;
}

/* 新增卡片美化样式 */
/deep/ .ant-card {
  border-radius: 3px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
}

/deep/ .ant-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/deep/ .ant-card-cover {
  height: 160px;
  overflow: hidden;
}

/deep/ .ant-card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

/deep/ .ant-card:hover .ant-card-cover img {
  transform: scale(1.05);
}

/deep/ .ant-card-actions {
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

/deep/ .ant-card-actions > li {
  margin: 0;
}

/deep/ .ant-card-actions > li:first-child {
  border-right: 1px solid #f0f0f0;
}

/deep/ .ant-card-actions > li > span {
  color: #4A90E2;
  font-size: 16px;
}

/deep/ .ant-card-actions > li > span:hover {
  color: #3A7BC8;
}

/* 价格标签样式 */
.price-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background: linear-gradient(45deg, #ff6b6b, #ff8e53);
  color: white;
  padding: 4px 10px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
  z-index: 2;
}

/* 热度标签样式 */
.hot-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  z-index: 2;
}
</style>
