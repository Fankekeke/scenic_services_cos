<template>
  <div :class="[multipage === true ? 'multi-page':'single-page', 'not-menu-page', 'home-page']" style="width: 68%;margin: 0 auto">
    <a-card :bordered="false" style="margin-left: -14px;margin-right: -14px;padding: 12px;">
      <!-- 公告筛选按钮 -->
      <div style="margin-bottom: 20px;">
        <a-button-group>
          <a-button @click="filterNews('')" :type="!filterType ? 'primary' : 'default'">全部</a-button>
          <a-button @click="filterNews('1')" :type="filterType === '1' ? 'primary' : 'default'">景区公告</a-button>
          <a-button @click="filterNews('2')" :type="filterType === '2' ? 'primary' : 'default'">活动通知</a-button>
          <a-button @click="filterNews('3')" :type="filterType === '3' ? 'primary' : 'default'">紧急消息</a-button>
        </a-button-group>
      </div>

      <!-- 公告列表 -->
      <a-list
        :dataSource="filteredNewsList"
        itemLayout="vertical"
        size="large"
        :pagination="pagination"
      >
        <a-list-item slot="renderItem" slot-scope="item, index">
          <a-card :bordered="false" style="margin-bottom: 16px;">
            <a-card-meta
              :title="item.title"
              :description="`发布者: ${item.publisher} | 发布时间: ${item.date}`"
            />
            <div class="news-content" style="margin-top: 16px;">
              {{ item.content }}
            </div>
            <div class="news-type" style="margin-top: 16px;">
              <a-tag :color="getNewsTypeColor(item.type)">
                {{ getNewsTypeName(item.type) }}
              </a-tag>
            </div>
          </a-card>
        </a-list-item>
      </a-list>
    </a-card>
  </div>
</template>
<script>
import HeadInfo from '@/views/common/HeadInfo'
import Scenic from '@/views/home/scenic/Scenic'
import Post from '@/views/home/post/Post'
import User from '@/views/user/user/User'
import {mapState} from 'vuex'
import moment from 'moment'
moment.locale('zh-cn')

export default {
  name: 'HomePage',
  components: {HeadInfo, Scenic, User, Post},
  data () {
    return {
      newsPage: 0,
      newsContent: '',
      newsList: [],
      filteredNewsList: [],
      filterType: '',
      avatar: '',
      userInfo: null,
      welcomeMessage: '',
      userEdit: {
        visiable: false
      },
      pagination: {
        onChange: page => {
          console.log(page)
        },
        pageSize: 5
      }
    }
  },
  computed: {
    ...mapState({
      multipage: state => state.setting.multipage,
      user: state => state.account.user
    })
  },
  methods: {
    getNewList () {
      this.$get(`/cos/bulletin-info/list`).then((r) => {
        this.newsList = r.data.data
        this.filteredNewsList = this.newsList
      })
    },
    filterNews (type) {
      this.filterType = type
      if (!type) {
        this.filteredNewsList = this.newsList
      } else {
        this.filteredNewsList = this.newsList.filter(item => item.type === type)
      }
    },
    getNewsTypeName (type) {
      const typeMap = {
        '1': '景区公告',
        '2': '活动通知',
        '3': '紧急消息'
      }
      return typeMap[type] || '未知类型'
    },
    getNewsTypeColor (type) {
      const colorMap = {
        '1': 'blue',
        '2': 'green',
        '3': 'red'
      }
      return colorMap[type] || 'gray'
    }
  },
  mounted () {
    this.getNewList()
  }
}
</script>
<style lang="less" scoped>
.news-content {
  max-height: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}
</style>
