
<template>
  <div class="rate-container" style="width: 100%;">
    <div class="content-area">
      <!-- 左侧词云图 -->
      <div class="wordcloud-section">
        <h3>景区评论词云图</h3>
        <div class="wordcloud-container" ref="wordcloudContainer">
          <div
            v-for="(item, index) in wordcloudData"
            :key="index"
            class="word-item"
            :style="getWordStyle(item)"
            @click="selectWord(item)"
          >
            {{ item.word }}
          </div>
        </div>
      </div>

      <!-- 右侧列表区域 -->
      <div class="list-section">
        <h3>关键词详细统计</h3>
        <div class="keyword-list">
          <div
            v-for="(item, index) in wordcloudData"
            :key="index"
            class="keyword-item"
            :class="{ 'selected': selectedWord === item.word }"
            @click="selectWord(item)"
          >
            <div class="keyword-info">
              <span class="keyword-text">{{ item.word }}</span>
              <span class="keyword-count">{{ item.count }}</span>
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

<script>export default {
  name: 'Rate5',
  data() {
    return {
      wordcloudData: [],
      maxCount: 0,
      selectedWord: '',
      wordPositions: []
    }
  },
  mounted() {
    this.selectRate()
  },
  methods: {
    selectRate() {
      this.$get(`/cos/scenic-order/queryOrderWordCloud`).then((r) => {
        this.wordcloudData = r.data.data
        this.processData()
      }).catch(error => {
        console.error('请求词云数据出错:', error)
      })
    },

    processData() {
      // 计算最大数量用于进度条和字体大小
      this.maxCount = Math.max(...this.wordcloudData.map(item => item.count), 1)

      // 按出现次数降序排序
      this.wordcloudData.sort((a, b) => b.count - a.count)
    },

    getWordStyle(item) {
      // 根据出现次数计算字体大小
      const minSize = 12
      const maxSize = 32
      const fontSize = minSize + (item.count / this.maxCount) * (maxSize - minSize)

      // 随机颜色
      const colors = [
        '#409EFF', '#67C23A', '#E6A23C', '#F56C6C',
        '#909399', '#409EFF', '#66B1FF', '#79BBFF'
      ]
      const color = colors[Math.floor(Math.random() * colors.length)]

      return {
        fontSize: `${fontSize}px`,
        color: color,
        opacity: 0.7 + (item.count / this.maxCount) * 0.3,
        fontWeight: 400 + Math.floor((item.count / this.maxCount) * 400)
      }
    },

    selectWord(item) {
      this.selectedWord = item.word
    }
  }
}
</script>

<style scoped>.rate-container {
  padding: 20px;
}

.content-area {
  display: flex;
  gap: 20px;
}

.wordcloud-section {
  flex: 2;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.wordcloud-section h3 {
  margin-top: 0;
}

.wordcloud-container {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  align-content: center;
  padding: 20px;
  min-height: 500px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
}

.word-item {
  margin: 5px 10px;
  padding: 5px 10px;
  transition: all 0.3s ease;
  border-radius: 4px;
  user-select: none;
}

.word-item:hover {
  transform: scale(1.1);
  opacity: 1 !important;
  background: rgba(64, 158, 255, 0.1);
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

.keyword-list {
  max-height: 600px;
  overflow-y: auto;
}

.keyword-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.keyword-item:hover {
  background-color: #f5f7fa;
}

.keyword-item.selected {
  background-color: #ecf5ff;
  border-left: 3px solid #409eff;
}

.keyword-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.keyword-text {
  font-weight: bold;
  color: #333;
}

.keyword-count {
  color: #409eff;
  font-weight: bold;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background-color: #e0e0e0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #66b1ff);
  transition: width 0.3s ease;
}

@media (max-width: 768px) {
  .content-area {
    flex-direction: column;
  }

  .wordcloud-section {
    order: 2;
  }

  .list-section {
    order: 1;
  }
}
</style>
