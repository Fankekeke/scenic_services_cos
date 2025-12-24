
<template>
  <div class="rate-container" style="width: 100%;">
    <div class="chart-area">
      <!-- 左侧图表区域 -->
      <div class="chart-section">
        <!-- 饼图 -->
        <div class="chart-card">
          <h3>景区等级分布</h3>
          <apexchart
            type="pie"
            :options="pieChartOptions"
            :series="pieSeries"
            height="400"
          ></apexchart>
        </div>

        <!-- 柱状图 -->
        <div class="chart-card">
          <h3>景区等级数量统计</h3>
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
        <h3>景区等级详细统计</h3>
        <div class="level-list">
          <div
            v-for="(item, index) in levelData"
            :key="index"
            class="level-item"
          >
            <div class="level-info">
              <span class="level-name">{{ item.level }}</span>
              <div class="level-details">
                <span class="level-count">{{ item.count }} 个</span>
                <span class="level-percentage">{{ (item.percentage * 100).toFixed(2) }}%</span>
              </div>
            </div>
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{ width: `${item.percentage * 100}%` }"
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
  name: 'Rate3',
  data() {
    return {
      levelData: [],
      pieChartOptions: {
        chart: {
          id: 'pie-chart'
        },
        labels: [],
        title: {
          text: '景区等级分布',
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
      pieSeries: [],
      barChartOptions: {
        chart: {
          id: 'bar-chart'
        },
        xaxis: {
          categories: [],
          title: {
            text: '景区等级'
          }
        },
        yaxis: {
          title: {
            text: '数量'
          }
        },
        title: {
          text: '景区等级数量统计',
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
      this.$get(`/cos/scenic-order/queryScenicLevelRate`).then((r) => {
        this.levelData = r.data.data
        this.processData()
      }).catch(error => {
        console.error('请求景区等级数据出错:', error)
      })
    },

    processData() {
      // 提取数据用于图表
      const levels = this.levelData.map(item => item.level)
      const counts = this.levelData.map(item => item.count)

      // 更新饼图数据
      this.pieChartOptions = {
        ...this.pieChartOptions,
        labels: levels
      }
      this.pieSeries = counts

      // 更新柱状图数据
      this.barChartOptions = {
        ...this.barChartOptions,
        xaxis: {
          categories: levels,
          title: {
            text: '景区等级'
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

.level-list {
  max-height: 700px;
  overflow-y: auto;
}

.level-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.level-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.level-name {
  font-weight: bold;
  color: #333;
  font-size: 16px;
}

.level-details {
  display: flex;
  gap: 15px;
}

.level-count {
  color: #409eff;
  font-weight: bold;
}

.level-percentage {
  color: #67c23a;
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
