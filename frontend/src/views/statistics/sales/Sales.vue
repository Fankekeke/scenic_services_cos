<template>
  <div class="sales-prediction-container">
    <a-select
      v-model="selectedDrugId" style="width: 300px"
      placeholder="请选择景点"
      show-search
      :filter-option="false"
      :loading="searchLoading"
      :options="filteredDrugOptions.map(item => ({ label: item.scenicName, value: item.id }))"
      @search="handleSearch"
      @change="onDrugChange"
    />

    <!-- 预测图表区域 -->
    <div class="chart-container">
      <apexchart
        v-if="chartOptions && series"
        type="line"
        height="450"
        :options="chartOptions"
        :series="series"
      ></apexchart>

      <!-- 预测详情卡片 -->
      <a-card
        v-if="predictionData"
        class="prediction-details-card"
      >
        <template #title>
          <a-icon type="pie-chart"/>
          <strong style="margin-left: 8px;font-size: 18px">销售预测详情</strong>
        </template>
        <a-row :gutter="16">
          <a-col :span="24">
            <div class="detail-item">
              <!--              <span class="detail-label">模型置信度</span>-->
<!--              <span-->
<!--                :class="[-->
<!--            'detail-value',-->
<!--            {-->
<!--              'confidence-high': predictionData.confidence > 0.8,-->
<!--              'confidence-medium': predictionData.confidence > 0.6 && predictionData.confidence <= 0.8,-->
<!--              'confidence-low': predictionData.confidence <= 0.6-->
<!--            }-->
<!--          ]"-->
<!--              >-->
<!--          {{ (predictionData.confidence * 100).toFixed(2) }}%-->
<!--        </span>-->
            </div>
          </a-col>
          <a-col :span="24">
            <div class="detail-item">
              <span class="detail-label">预测时间范围</span>
              <span class="detail-value">
          {{ predictionData.dates[0] }} 至 {{ predictionData.dates[predictionData.dates.length - 1] }}
        </span>
            </div>
          </a-col>
          <a-col :span="24">
            <div class="detail-item">
              <span class="detail-label">预测天数</span>
              <span class="detail-value">
          {{ predictionData.dates.length }} 天
        </span>
            </div>
          </a-col>
        </a-row>
      </a-card>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'Sales',
  data () {
    return {
      selectedDrugId: null,
      drugList: [],
      dataList: [],
      predictionData: null,
      chartOptions: null,
      series: [],
      searchLoading: false,
      filteredDrugOptions: [],
      searchTimeout: null
    }
  },
  mounted () {
    this.initChart() // 先初始化图表
  },
  methods: {
    handleSearch (value) {
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout)
      }

      this.searchTimeout = setTimeout(() => {
        this.fetchDrugsByKeyword(value)
      }, 300)
    },

    fetchDrugsByKeyword (keyword) {
      if (!keyword) {
        // If no keyword, show all drugs
        this.filteredDrugOptions = [...this.drugList]
        return
      }
      this.searchLoading = true
      this.$get('/cos/scenic-info/queryScenicInfoByKey', {key: keyword})
        .then((r) => {
          this.filteredDrugOptions = r.data.data || []
        })
        .catch(() => {
          this.filteredDrugOptions = []
        })
        .finally(() => {
          this.searchLoading = false
        })
    },
    getScenicList () {
      this.$get('/cos/scenic-info/queryScenicInfoByKey'
      ).then((r) => {
        this.drugList = r.data.data
      })
    },
    getDrugSales (drugId = 1) {
      this.$get('/cos/prediction/sales', {scenicId: drugId}).then((r) => {
        this.predictionData = r.data.data
        this.dataList = r.data.data
        this.updateChart()
      })
    },
    onDrugChange (value) {
      this.$get('/cos/prediction/sales', {scenicId: value}).then((r) => {
        this.predictionData = r.data.data
        this.updateChart()
      })
    },
    initChart () {
      this.chartOptions = {
        chart: {
          type: 'line',
          height: 450,
          toolbar: {
            show: true
          }
        },
        title: {
          text: '景点销售预测',
          align: 'left'
        },
        xaxis: {
          categories: [] // 初始为空
        },
        yaxis: {
          title: {
            text: '销售数量'
          }
        },
        tooltip: {
          enabled: true
        },
        markers: {
          size: 4
        }
      }

      this.series = [{
        name: '预测销量',
        data: []
      }]
    },
    updateChart () {
      if (!this.predictionData) return

      // 更新 x 轴数据
      this.chartOptions = {
        ...this.chartOptions,
        xaxis: {
          categories: this.predictionData.dates
        }
      }

      // 更新系列数据
      this.series = [{
        name: '预测销量',
        data: this.predictionData.predictedValues
      }]
    }
  }
}
</script>

<style scoped>.sales-prediction-container {
  width: 100%;
  padding: 20px;
}

.chart-container {
  width: 100%;
  margin-top: 20px;
}

.sales-chart {
  width: 100%;
  height: 450px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.prediction-details-card {
  width: 35%;
  margin-top: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 25px;
}

.prediction-details-card :deep(.ant-card-head) {
  background-color: #f7f9fc;
  border-bottom: 1px solid #e8e8e8;
}

.prediction-details-card :deep(.ant-descriptions-row > th) {
  font-weight: 500;
  color: #595959;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 14px;
  color: #8c8c8c;
}

.detail-value {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
}

.confidence-high {
  color: #52c41a;
}

.confidence-medium {
  color: #faad14;
}

.confidence-low {
  color: #ff4d4f;
}
</style>
