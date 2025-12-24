
<template>
  <div class="rate-container" style="width: 100%;">
    <div class="chart-area">
      <!-- 左侧图表区域 -->
      <div class="chart-section">
        <!-- 折线图 -->
        <div class="chart-card">
          <h3>各省份景区数量统计</h3>
          <apexchart
            type="line"
            :options="lineChartOptions"
            :series="lineSeries"
            height="400"
          ></apexchart>
        </div>

        <!-- 柱状图 -->
        <div class="chart-card">
          <h3>各省份景区数量统计</h3>
          <apexchart
            type="bar"
            :options="barChartOptions"
            :series="barSeries"
            height="400"
          ></apexchart>
        </div>
      </div>

      <!-- 右侧列表区域 -->
      <div class="list-section">
        <h3>省份景区数量列表</h3>
        <div class="province-list">
          <div
            v-for="(item, index) in provinceData"
            :key="index"
            class="province-item"
          >
            <div class="province-info">
              <span class="province-name">{{ item.province }}</span>
              <span class="province-count">{{ item.count }}</span>
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
  name: 'Rate2',
  data() {
    return {
      provinceData: [],
      maxCount: 0,
      lineChartOptions: {
        chart: {
          id: 'line-chart'
        },
        xaxis: {
          categories: [],
          title: {
            text: '省份'
          }
        },
        yaxis: {
          title: {
            text: '景区数量'
          }
        },
        title: {
          text: '省份景区数量折线图',
          align: 'left'
        },
        stroke: {
          curve: 'smooth'
        },
        markers: {
          size: 4
        }
      },
      lineSeries: [{
        name: '景区数量',
        data: []
      }],
      barChartOptions: {
        chart: {
          id: 'bar-chart'
        },
        xaxis: {
          categories: [],
          title: {
            text: '省份'
          }
        },
        yaxis: {
          title: {
            text: '景区数量'
          }
        },
        title: {
          text: '省份景区数量柱状图',
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
      }]
    }
  },
  mounted() {
    this.selectRate()
  },
  methods: {
    selectRate() {
      this.$get(`/cos/scenic-order/queryAreaScenicNumRate`).then((r) => {
        this.provinceData = r.data.data
        this.processData()
      }).catch(error => {
        console.error('请求省份景区数量数据出错:', error)
      })
    },

    processData() {
      // 按景区数量降序排序
      this.provinceData.sort((a, b) => b.count - a.count)

      // 计算最大数量用于进度条
      this.maxCount = Math.max(...this.provinceData.map(item => item.count), 1)

      // 提取省份名称和数量用于图表
      const provinces = this.provinceData.map(item => item.province)
      const counts = this.provinceData.map(item => item.count)

      // 更新折线图数据
      this.lineChartOptions = {
        ...this.lineChartOptions,
        xaxis: {
          categories: provinces,
          title: {
            text: '省份'
          }
        }
      }
      this.lineSeries = [{
        name: '景区数量',
        data: counts
      }]

      // 更新柱状图数据
      this.barChartOptions = {
        ...this.barChartOptions,
        xaxis: {
          categories: provinces,
          title: {
            text: '省份'
          }
        }
      }
      this.barSeries = [{
        name: '景区数量',
        data: counts
      }]
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

.province-list {
  max-height: 700px;
  overflow-y: auto;
}

.province-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.province-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.province-name {
  font-weight: bold;
  color: #333;
}

.province-count {
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
  background: linear-gradient(90deg, #409eff, #66b1ff);
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
