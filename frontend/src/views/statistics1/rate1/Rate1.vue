
<template>
  <div class="rate-container" style="width: 100%;">
    <div class="chart-section">
      <!-- 评分分布饼图 -->
      <div class="chart-card">
        <h3>评分分布统计</h3>
        <apexchart
          type="pie"
          :options="pieChartOptions"
          :series="pieSeries"
          height="350"
        ></apexchart>
      </div>

      <!-- 评分趋势柱状图 -->
      <div class="chart-card">
        <h3>各评分区间统计</h3>
        <apexchart
          type="bar"
          :options="barChartOptions"
          :series="barSeries"
          height="350"
        ></apexchart>
      </div>
    </div>

    <!-- 右侧评分列表 -->
    <div class="list-section">
      <h3>详细评分统计</h3>
      <div class="score-list">
        <div
          v-for="item in rateData"
          :key="item.score"
          class="score-item"
          :class="{ 'highlight': item.num > 0 }"
        >
          <span class="score-label">评分 {{ item.score }}</span>
          <span class="score-count">{{ item.num }} 次</span>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: `${(item.num / maxCount) * 100}%` }"
            ></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  name: 'Rate1',
  data () {
    return {
      rateData: [],
      chartLoading: false,
      pieChartOptions: {
        chart: {
          id: 'pie-chart'
        },
        labels: ['1-2星', '2-3星', '3-4星', '4-5星'],
        title: {
          text: '评分区间分布'
        },
        responsive: [{
          breakpoint: 480,
          options: {
            chart: {
              width: 200
            },
            legend: {
              position: 'bottom'
            }
          }
        }]
      },
      pieSeries: [],
      barChartOptions: {
        chart: {
          id: 'bar-chart'
        },
        xaxis: {
          categories: [],
          title: {
            text: '评分'
          }
        },
        yaxis: {
          title: {
            text: '数量'
          }
        },
        title: {
          text: '各评分统计'
        }
      },
      barSeries: [{
        name: '评价数量',
        data: []
      }],
      maxCount: 0
    }
  },
  mounted () {
    this.selectRate()
  },
  methods: {
    selectRate () {
      this.chartLoading = true
      this.$get(`/cos/scenic-order/queryEvaluateRate`).then((r) => {
        this.rateData = r.data.data
        this.processData()
        this.chartLoading = false
      }).catch(error => {
        console.error('请求评分数据出错:', error)
        this.chartLoading = false
      })
    },

    processData () {
      // 计算评分区间分布
      const ranges = {
        '1-2星': 0,
        '2-3星': 0,
        '3-4星': 0,
        '4-5星': 0
      }

      // 提取柱状图的x轴和y轴数据
      const categories = []
      const seriesData = []

      // 找到最大数量用于进度条显示
      this.maxCount = Math.max(...this.rateData.map(item => item.num), 1)

      this.rateData.forEach(item => {
        const score = item.score
        if (score >= 1 && score <= 2) {
          ranges['1-2星'] += item.num
        } else if (score > 2 && score <= 3) {
          ranges['2-3星'] += item.num
        } else if (score > 3 && score <= 4) {
          ranges['3-4星'] += item.num
        } else if (score > 4 && score <= 5) {
          ranges['4-5星'] += item.num
        }

        // 添加到柱状图数据
        categories.push(`${score}分`)
        seriesData.push(item.num)
      })

      // 设置饼图数据
      this.pieSeries = [
        ranges['1-2星'],
        ranges['2-3星'],
        ranges['3-4星'],
        ranges['4-5星']
      ]

      // 设置柱状图数据
      this.barChartOptions = {
        ...this.barChartOptions,
        xaxis: {
          categories: categories,
          title: {
            text: '评分'
          }
        }
      }

      this.barSeries = [{
        name: '评价数量',
        data: seriesData
      }]
    }
  }
}
</script>

<style scoped>.rate-container {
  display: flex;
  padding: 20px;
  gap: 20px;
}

.chart-section {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.list-section {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: fit-content;
}

.list-section h3 {
  margin-top: 0;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.score-list {
  max-height: 500px;
  overflow-y: auto;
}

.score-item {
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.score-item.highlight {
  background-color: #f0f9ff;
  border-left: 3px solid #409eff;
}

.score-label {
  font-weight: bold;
  color: #333;
}

.score-count {
  float: right;
  color: #666;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background-color: #e0e0e0;
  border-radius: 3px;
  margin-top: 5px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #409eff;
  transition: width 0.3s ease;
}

@media (max-width: 768px) {
  .rate-container {
    flex-direction: column;
  }
}
</style>
