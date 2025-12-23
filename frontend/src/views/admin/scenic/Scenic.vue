<template>
  <a-card :bordered="false" class="card-area">
    <div :class="advanced ? 'search' : null">
      <!-- 搜索区域 -->
      <a-form layout="horizontal">
        <a-row :gutter="15">
          <div :class="advanced ? null: 'fold'">
            <a-col :md="6" :sm="24">
              <a-form-item
                label="景点名称"
                :labelCol="{span: 4}"
                :wrapperCol="{span: 18, offset: 2}">
                <a-input v-model="queryParams.scenicName"/>
              </a-form-item>
            </a-col>
            <a-col :md="6" :sm="24">
              <a-form-item
                label="景区地址"
                :labelCol="{span: 4}"
                :wrapperCol="{span: 18, offset: 2}">
                <a-input v-model="queryParams.address"/>
              </a-form-item>
            </a-col>
          </div>
          <span style="float: right; margin-top: 3px;">
            <a-button type="primary" @click="search">查询</a-button>
            <a-button style="margin-left: 8px" @click="reset">重置</a-button>
          </span>
        </a-row>
      </a-form>
    </div>
    <div>
      <div class="operator">
        <a-button type="primary" ghost @click="add">新增</a-button>
        <a-button @click="batchDelete">删除</a-button>
      </div>
      <!-- 表格区域 -->
      <a-table ref="TableInfo"
               :columns="columns"
               :dataSource="dataSource"
               :pagination="pagination"
               :loading="loading"
               :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
               :scroll="{ x: 900 }"
               @change="handleTableChange">
        <template slot="scenicNameShow" slot-scope="text, record">
          <a-tooltip>
            <template slot="title">
              {{ record.scenicName }}
            </template>
            {{ record.scenicName.slice(0, 10) }} ...
          </a-tooltip>
        </template>
        <template slot="addressShow" slot-scope="text, record">
          <a-tooltip>
            <template slot="title">
              {{ record.address }}
            </template>
            {{ record.address.slice(0, 10) }} ...
          </a-tooltip>
        </template>
        <template slot="historyShow" slot-scope="text, record">
          <a-tooltip>
            <template slot="title">
              {{ record.history }}
            </template>
            {{ record.history.slice(0, 10) }} ...
          </a-tooltip>
        </template>
        <template slot="operation" slot-scope="text, record">
          <a-icon type="cloud" @click="view(record)" title="查 看" style="margin-right: 15px"></a-icon>
          <a-icon type="setting" theme="twoTone" twoToneColor="#4a9ff5" @click="edit(record)" title="修改景点" style="margin-right: 15px"></a-icon>
          <a-icon type="play-square" @click="browseView.visiable = true" title="人浏览检测"></a-icon>
        </template>
      </a-table>
    </div>
    <scenic-info
      :scenicData="scenicView.data"
      :scenicShow="scenicView.visiable"
      @close="handlescenicViewClose">
    </scenic-info>
    <user-add
      @close="handleUserAddClose"
      @success="handleUserAddSuccess"
      :userAddVisiable="userAdd.visiable">
    </user-add>
    <user-edit
      ref="userEdit"
      @close="handleUserEditClose"
      @success="handleUserEditSuccess"
      :userEditVisiable="userEdit.visiable">
    </user-edit>
    <a-modal v-model="browseView.visiable" :footer="null">
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
      </div>
    </a-modal>
  </a-card>
</template>

<script>
import DeptInputTree from '../../system/dept//DeptInputTree'
import RangeDate from '@/components/datetime/RangeDate'
import ScenicInfo from './ScenicView'
import UserAdd from './ScenicAdd'
import UserEdit from './ScenicEdit'
import jsQR from "jsqr";

export default {
  name: 'User',
  components: {UserAdd, UserEdit, ScenicInfo, DeptInputTree, RangeDate},
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
      verifyLoading: false,
      browseView: {
        visiable: false,
        data: null
      },
      advanced: false,
      scenicView: {
        visiable: false,
        data: null
      },
      userAdd: {
        visiable: false
      },
      userEdit: {
        visiable: false
      },
      queryParams: {},
      filteredInfo: null,
      sortedInfo: null,
      paginationInfo: null,
      dataSource: [],
      selectedRowKeys: [],
      loading: false,
      pagination: {
        pageSizeOptions: ['10', '20', '30', '40', '100'],
        defaultCurrent: 1,
        defaultPageSize: 10,
        showQuickJumper: true,
        showSizeChanger: true,
        showTotal: (total, range) => `显示 ${range[0]} ~ ${range[1]} 条记录，共 ${total} 条记录`
      }
    }
  },
  computed: {
    columns () {
      return [{
        title: '景点名称',
        dataIndex: 'scenicName',
        scopedSlots: {customRender: 'scenicNameShow'}
      }, {
        title: '票价',
        dataIndex: 'scenicPrice',
        customRender: (text, row, index) => {
          return '￥' + text
        },
      }, {
        title: '所在地',
        dataIndex: 'address',
        scopedSlots: {customRender: 'addressShow'}
      }, {
        title: '地区',
        dataIndex: 'area',
      }, {
        title: '热度',
        dataIndex: 'hot',
        customRender: (text, row, index) => {
          return text.substr(0, 7)
        },
      }, {
        title: '等级',
        dataIndex: 'level',
      }, {
        title: '游量',
        dataIndex: 'sold',
      }, {
        title: '图片',
        dataIndex: 'webImg',
        scopedSlots: {customRender: 'webImg'},
        customRender: (text, record, index) => {
          if (!record.webImg) return <a-avatar shape="square" icon="user"/>
          return <a-popover>
            <template slot="content">
              <a-avatar shape="square" size={132} icon="user" src={record.webImg}/>
            </template>
            <a-avatar shape="square" icon="user" src={record.webImg}/>
          </a-popover>
        }
      }, {
        title: '文化历史',
        dataIndex: 'history',
        scopedSlots: {customRender: 'historyShow'}
      }, {
        title: '操作',
        dataIndex: 'operation',
        scopedSlots: {customRender: 'operation'}
      }]
    }
  },
  mounted () {
    this.fetch()
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
        this.$message.success(r.data.msg)
        console.log('识别结果:', r)
        // 处理识别结果
      }).catch(err => {
        console.error('识别失败:', err)
      })
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
    onSelectChange (selectedRowKeys) {
      this.selectedRowKeys = selectedRowKeys
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
      if (!this.advanced) {
        this.queryParams.createTimeFrom = ''
        this.queryParams.createTimeTo = ''
      }
    },
    view (record) {
      this.scenicView.data = record
      this.scenicView.visiable = true
    },
    add () {
      this.userAdd.visiable = true
    },
    handleUserAddClose () {
      this.userAdd.visiable = false
    },
    handleUserAddSuccess () {
      this.userAdd.visiable = false
      this.$message.success('新增景点成功')
      this.search()
    },
    edit (record) {
      this.$refs.userEdit.setFormValues(record)
      this.userEdit.visiable = true
    },
    handleUserEditClose () {
      this.userEdit.visiable = false
    },
    handleUserEditSuccess () {
      this.userEdit.visiable = false
      this.$message.success('修改景点成功')
      this.search()
    },
    handlescenicViewClose () {
      this.scenicView.visiable = false
    },
    handleTypeChange (value) {
      this.queryParams.accountType = value || ''
    },
    handleDateChange (value) {
      if (value) {
        this.queryParams.createTimeFrom = value[0]
        this.queryParams.createTimeTo = value[1]
      }
    },
    batchDelete () {
      if (!this.selectedRowKeys.length) {
        this.$message.warning('请选择需要删除的记录')
        return
      }
      let that = this
      this.$confirm({
        title: '确定删除所选中的记录?',
        content: '当您点击确定按钮后，这些记录将会被彻底删除',
        centered: true,
        onOk () {
          let ids = []
          for (let key of that.selectedRowKeys) {
            ids.push(that.dataSource[key].id)
          }
          that.$delete('/cos/scenic-info/' + ids).then(() => {
            that.$message.success('删除成功')
            that.selectedRowKeys = []
            that.search()
          })
        },
        onCancel () {
          that.selectedRowKeys = []
        }
      })
    },
    exportExcel () {
      let {sortedInfo, filteredInfo} = this
      let sortField, sortOrder
      // 获取当前列的排序和列的过滤规则
      if (sortedInfo) {
        sortField = sortedInfo.field
        sortOrder = sortedInfo.order
      }
      this.$export('user/excel', {
        sortField: sortField,
        sortOrder: sortOrder,
        ...this.queryParams,
        ...filteredInfo
      })
    },
    search () {
      let {sortedInfo, filteredInfo} = this
      let sortField, sortOrder
      // 获取当前列的排序和列的过滤规则
      if (sortedInfo) {
        sortField = sortedInfo.field
        sortOrder = sortedInfo.order
      }
      this.fetch({
        sortField: sortField,
        sortOrder: sortOrder,
        ...this.queryParams,
        ...filteredInfo
      })
    },
    reset () {
      // 取消选中
      this.selectedRowKeys = []
      // 重置分页
      this.$refs.TableInfo.pagination.current = this.pagination.defaultCurrent
      if (this.paginationInfo) {
        this.paginationInfo.current = this.pagination.defaultCurrent
        this.paginationInfo.pageSize = this.pagination.defaultPageSize
      }
      // 重置列过滤器规则
      this.filteredInfo = null
      // 重置列排序规则
      this.sortedInfo = null
      // 重置查询参数
      this.queryParams = {}
      // 清空部门树选择
      this.$refs.deptTree.reset()
      // 清空时间选择
      if (this.advanced) {
        this.$refs.createTime.reset()
      }
      this.fetch()
    },
    handleTableChange (pagination, filters, sorter) {
      // 将这三个参数赋值给Vue data，用于后续使用
      this.paginationInfo = pagination
      this.filteredInfo = filters
      this.sortedInfo = sorter
      this.scenicView.visiable = false
      this.fetch({
        sortField: sorter.field,
        sortOrder: sorter.order,
        ...this.queryParams,
        ...filters
      })
    },
    fetch (params = {}) {
      // 显示loading
      this.loading = true
      if (this.paginationInfo) {
        // 如果分页信息不为空，则设置表格当前第几页，每页条数，并设置查询分页参数
        this.$refs.TableInfo.pagination.current = this.paginationInfo.current
        this.$refs.TableInfo.pagination.pageSize = this.paginationInfo.pageSize
        params.size = this.paginationInfo.pageSize
        params.current = this.paginationInfo.current
      } else {
        // 如果分页信息为空，则设置为默认值
        params.size = this.pagination.defaultPageSize
        params.current = this.pagination.defaultCurrent
      }
      this.$get('/cos/scenic-info/page', {
        ...params
      }).then((r) => {
        let data = r.data.data
        const pagination = {...this.pagination}
        pagination.total = data.total
        this.dataSource = data.records
        this.pagination = pagination
        // 数据加载完毕，关闭loading
        this.loading = false
      })
    }
  }
}
</script>
<style lang="less" scoped>
@import "../../../../static/less/Common";
</style>
