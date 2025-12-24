
<template>
  <div class="rate-container" style="width: 100%;">
    <div class="chart-area">
      <!-- 左侧图表区域 -->
      <div class="chart-section">
        <!-- 柱状图 -->
        <div class="chart-card">
          <h3>价格区间景区数量统计</h3>
          <apexchart
            type="bar"
            :options="barChartOptions"
            :series="barSeries"
            height="400"
          ></apexchart>
        </div>

        <!-- 饼图 -->
        <div class="chart-card">
          <h3>价格区间分布</h3>
          <apexchart
            type="pie"
            :options="pieChartOptions"
            :series="pieSeries"
            height="400"
          ></apexchart>
        </div>
      </div>

      <!-- 右侧列表区域 -->
      <div class="list-section">
        <h3>价格区间详细统计</h3>
        <div class="price-list">
          <div
            v-for="(item, index) in priceData"
            :key="index"
            class="price-item"
          >
            <div class="price-info">
              <span class="price-range">价格区间: {{ item.range }}元</span>
              <span class="price-count">{{ item.count }} 个</span>
            </div>
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{ width: `${(item.count / maxCount) * 100}%` }"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  name: 'Rate4',
  data() {
    return {
      priceData: [],
      maxCount: 0,
      barChartOptions: {
        chart: {
          id: 'bar-chart'
        },
        xaxis: {
          categories: [],
          title: {
            text: '价格区间'
          }
        },
        yaxis: {
          title: {
            text: '景区数量'
          }
        },
        title: {
          text: '景区价格区间统计',
          align: 'left'
        },
        plotOptions: {
          bar: {
            horizontal: false,
            columnWidth: '85%',
            endingShape: 'rounded'
          }
        }
      },
      barSeries: [{
        name: '景区数量',
        data: []
      }],
      pieChartOptions: {
        chart: {
          id: 'pie-chart'
        },
        labels: [],
        title: {
          text: '价格区间分布',
          align: 'center'
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
      pieSeries: []
    }
  },
  mounted() {
    this.selectRate()
  },
  methods: {
    selectRate() {
      this.$get(`/cos/scenic-order/queryPriceStepRate`).then((r) => {
        this.priceData = r.data.data
        this.processData()
      }).catch(error => {
        console.error('请求价格区间数据出错:', error)
      })
    },

    processData() {
      // 按价格区间排序
      this.priceData.sort((a, b) => {
        const [minA] = a.range.split('-').map(Number)
        const [minB] = b.range.split('-').map(Number)
        return minA - minB
      })

      // 计算最大数量用于进度条
      this.maxCount = Math.max(...this.priceData.map(item => item.count), 1)

      // 提取数据用于图表
      const ranges = this.priceData.map(item => item.range)
      const counts = this.priceData.map(item => item.count)

      // 更新柱状图数据
      this.barChartOptions = {
        ...this.barChartOptions,
        xaxis: {
          categories: ranges,
          title: {
            text: '价格区间(元)'
          }
        }
      }
      this.barSeries = [{
        name: '景区数量',
        data: counts
      }]

      // 更新饼图数据
      this.pieChartOptions = {
        ...this.pieChartOptions,
        labels: ranges
      }
      this.pieSeries = counts
    }
  }
}
</script>

<style scoped>.rate-container {
  padding: 20px;
}

.chart-area {
  display: flex;
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

.price-list {
  max-height: 700px;
  overflow-y: auto;
}

.price-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.price-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.price-range {
  font-weight: bold;
  color: #333;
  font-size: 15px;
}

.price-count {
  color: #409eff;
  font-weight: bold;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background-color: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #f56c6c, #f78989);
  transition: width 0.3s ease;
}

@media (max-width: 768px) {
  .chart-area {
    flex-direction: column;
  }

  .chart-section {
    order: 2;
  }

  .list-section {
    order: 1;
  }
}
</style>
