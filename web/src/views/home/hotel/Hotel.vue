<template>
  <div>
<!--    <a-form layout="horizontal">-->
<!--      <a-row :gutter="15" style="margin: 0">-->
<!--        <div>-->
<!--          <a-col :md="6" :sm="24">-->
<!--            <a-form-item-->
<!--              label="房间名称"-->
<!--              :labelCol="{span: 5}"-->
<!--              :wrapperCol="{span: 17, offset: 2}">-->
<!--              <a-input v-model="queryParams.name"/>-->
<!--            </a-form-item>-->
<!--          </a-col>-->
<!--          <a-col :md="6" :sm="24">-->
<!--            <a-form-item-->
<!--              label="地 区"-->
<!--              :labelCol="{span: 5}"-->
<!--              :wrapperCol="{span: 17, offset: 2}">-->
<!--              <a-input v-model="queryParams.address"/>-->
<!--            </a-form-item>-->
<!--          </a-col>-->
<!--        </div>-->
<!--        <span style="float: right; margin-top: 3px;">-->
<!--            <a-button type="primary" @click="search">查询</a-button>-->
<!--          </span>-->
<!--      </a-row>-->
<!--    </a-form>-->
    <a-alert type="info" message="此景点20公里内酒店" show-icon></a-alert>
    <a-row :gutter="15">
      <a-skeleton active v-if="loading" />
      <a-col :span="6" v-for="(item, index) in roomData" :key="index" style="margin-top: 30px" v-if="!loading">
        <a-card hoverable bordered @click="orderAdd(item)">
          <img
            slot="cover"
            alt="example"
            height="160"
            :src="'http://127.0.0.1:9527/imagesWeb/' + item.images.split(',')[0]"
          />
          <a-card-meta :title="item.name">
            <template slot="description">
              <p>￥ <span style="font-size: 16px;color: red">{{ item.price.toFixed(2) }}</span> 元</p>
              <p v-if="item.hotelName" class="hotel-name">酒店：{{ item.hotelName }}</p>
              <p v-if="item.distance" class="distance">距离景点：{{ item.distance.toFixed(2) }} 公里</p>
              {{ item.content.slice(0, 18) }}...
            </template>
          </a-card-meta>
        </a-card>
      </a-col>
      <a-col :span="24" v-if="roomData.length === 0 && !loading" class="no-data-container">
        <div class="no-data-content">
          <div class="no-data-icon">🏨</div>
          <h3 class="no-data-title">附近没有酒店😭</h3>
          <p class="no-data-desc">当前景点20公里范围内暂无酒店信息</p>
        </div>
      </a-col>
    </a-row>
    <br/>
    <br/>
    <a-pagination :default-current="pagination.defaultCurrent" :defaultPageSize="pagination.defaultPageSize" :total="pagination.total" @change="pageChange" style="float: right"/>
    <order-add
      :roomData="orderView.data"
      :roomShow="orderView.visiable"
      @close="handleOrderViewClose">
    </order-add>
  </div>
</template>

<script>
import OrderAdd from './OrderAdd'
export default {
  name: 'Hotel',
  props: {
    scenicId: {
      type: Number
    }
  },
  components: {OrderAdd},
  data () {
    return {
      queryParams: {},
      loading: false,
      roomData: [],
      orderView: {
        visiable: false,
        data: null
      },
      pagination: {
        defaultCurrent: 1,
        defaultPageSize: 12,
        total: 0
      }
    }
  },
  watch: {
    scenicId () {
      console.log(this.scenicId)
      this.queryRoomTypeByScenic()
    }
  },
  mounted () {
    // this.queryRoomTypeByScenic()
    // this.fetch()
  },
  methods: {
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
    queryRoomTypeByScenic () {
      this.$get('/cos/room-type/queryRoomTypeByScenic', {
        scenicId: this.scenicId
      }).then((r) => {
        this.roomData = r.data.data
      })
    },
    fetch (params = {}) {
      // 显示loading
      this.loading = true
      params.size = this.pagination.defaultPageSize
      params.current = this.pagination.defaultCurrent
      this.$get('/cos/room-type/page', {
        ...params
      }).then((r) => {
        let data = r.data.data
        this.pagination.total = data.total
        this.roomData = data.records
        // 数据加载完毕，关闭loading
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>/deep/ .ant-card-meta-title {
  font-size: 14px;
  margin-top: 10px;
}
/deep/ .ant-card-meta-description {
  font-size: 13px;
  margin-bottom: 8px;
}
/deep/ .hotel-name {
  color: #666;
  margin: 5px 0;
  font-size: 12px;
}
/deep/ .distance {
  color: #999;
  margin: 5px 0;
  font-size: 12px;
}

.no-data-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

.no-data-content {
  text-align: center;
  padding: 40px 20px;
  background: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.no-data-icon {
  font-size: 60px;
  margin-bottom: 16px;
}

.no-data-title {
  font-size: 18px;
  color: #333;
  margin: 0 0 8px;
}

.no-data-desc {
  font-size: 14px;
  color: #999;
  margin: 0;
}
</style>
