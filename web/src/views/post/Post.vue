<template>
  <div :class="[multipage === true ? 'multi-page':'single-page', 'not-menu-page', 'home-page']" style="width: 68%;margin: 0 auto">
    <a-card :bordered="false" style="margin-left: -14px;margin-right: -14px;padding: 12px;">
      <post/>
    </a-card>
  </div>
</template>
<script>
import HeadInfo from '@/views/common/HeadInfo'
import Scenic from '@/views/home/scenic/Scenic'
import Post from '@/views/home/post/Post'
import User from '@/views/user/user/User'
import { getUrlKey } from '@/utils/urlKey'
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
      avatar: '',
      userInfo: null,
      welcomeMessage: '',
      userEdit: {
        visiable: false
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
        if (this.newsList.length !== 0) {
          this.newsContent = `《${this.newsList[0].title}》 ${this.newsList[0].content}`
        }
      })
    },
    newsNext () {
      if (this.newsPage + 1 === this.newsList.length) {
        this.newsPage = 0
      } else {
        this.newsPage += 1
      }
      this.newsContent = `《${this.newsList[this.newsPage].title}》 ${this.newsList[this.newsPage].content}`
    },
    edit () {
      this.$refs.userEdit.setFormValues(this.userInfo)
      this.userEdit.visiable = true
    },
    handleUserEditClose () {
      this.userEdit.visiable = false
      this.getUserInfo()
    },
    handleUserEditSuccess () {
      this.userEdit.visiable = false
      this.$message.success('修改个人信息成功')
    },
    getUserInfo () {
      this.$get(`/cos/user-info/${this.user.userId}`).then((r) => {
        this.userInfo = r.data.data
        this.avatar = r.data.data.avatar
      })
    },
    welcome () {
      const date = new Date()
      const hour = date.getHours()
      let time = hour < 6 ? '早上好' : (hour <= 11 ? '上午好' : (hour <= 13 ? '中午好' : (hour <= 18 ? '下午好' : '晚上好')))
      return `${time}`
    }
  },
  mounted () {
    this.getUserInfo()
    this.getNewList()
    this.welcomeMessage = this.welcome()
    if (getUrlKey('out_trade_no') !== null) {
      this.$get('/cos/pay/orderAudit', { orderCode: getUrlKey('out_trade_no') }).then((r) => {
        this.$message.success('支付成功！')
      })
    }
  }
}
</script>
<style lang="less">
.home-page {
  .head-info {
    margin-bottom: .5rem;

    .head-info-card {
      padding: .5rem;
      border-color: #f1f1f1;

      .head-info-avatar {
        display: inline-block;
        float: left;
        margin-right: 1rem;

        img {
          width: 5rem;
          border-radius: 2px;
        }
      }

      .head-info-count {
        display: inline-block;
        float: left;

        .head-info-welcome {
          font-size: 1.05rem;
          margin-bottom: .1rem;
        }

        .head-info-desc {
          color: rgba(0, 0, 0, 0.45);
          font-size: .8rem;
          padding: .2rem 0;

          p {
            margin-bottom: 0;
          }
        }

        .head-info-time {
          color: rgba(0, 0, 0, 0.45);
          font-size: .8rem;
          padding: .2rem 0;
        }
      }
    }
  }

  .count-info {
    .visit-count-wrapper {
      padding-left: 0 !important;

      .visit-count {
        padding: .5rem;
        border-color: #f1f1f1;

        .ant-card-body {
          padding: .5rem 1rem !important;
        }
      }
    }

    .project-wrapper {
      padding-right: 0 !important;

      .project-card {
        border: none !important;

        .ant-card-head {
          border-left: 1px solid #f1f1f1 !important;
          border-top: 1px solid #f1f1f1 !important;
          border-right: 1px solid #f1f1f1 !important;
        }

        .ant-card-body {
          padding: 0 !important;

          table {
            width: 100%;

            td {
              width: 50%;
              border: 1px solid #f1f1f1;
              padding: .6rem;

              .project-avatar-wrapper {
                display: inline-block;
                float: left;
                margin-right: .7rem;

                .project-avatar {
                  color: #42b983;
                  background-color: #d6f8b8;
                }
              }
            }
          }
        }

        .project-detail {
          display: inline-block;
          float: left;
          text-align: left;
          width: 78%;

          .project-name {
            font-size: .9rem;
            margin-top: -2px;
            font-weight: 600;
          }

          .project-desc {
            color: rgba(0, 0, 0, 0.45);

            p {
              margin-bottom: 0;
              font-size: .6rem;
              white-space: normal;
            }
          }
        }
      }
    }
  }
}
</style>
