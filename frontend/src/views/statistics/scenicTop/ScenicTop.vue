<template>
  <div class="scenic-top-container">
    <a-card title="景区销量排行榜" :bordered="false" class="ranking-card">
      <div class="ranking-list">
        <div
          v-for="(item, index) in scenicRanking"
          :key="index"
          class="ranking-item"
          :class="{ 'top-three': index < 3 }"
        >
          <div class="rank-number">
            <span v-if="index === 0" class="first-place">🥇</span>
            <span v-else-if="index === 1" class="second-place">🥈</span>
            <span v-else-if="index === 2" class="third-place">🥉</span>
            <span v-else>{{ index + 1 }}</span>
          </div>
          <div class="scenic-image">
            <img :src="item.scenicImages" :alt="item.scenicName"/>
          </div>
          <div class="scenic-info">
            <h3 class="scenic-name">{{ item.scenicName }}</h3>
            <div class="sales-info">
              <span class="amount">销量: {{ item.amount }}</span>
              <span class="price">¥{{ item.price }}</span>
            </div>
          </div>
        </div>
      </div>
    </a-card>
  </div>
</template>

<script>
export default {
  name: 'scenicTop',
  data () {
    return {
      scenicRanking: []
    }
  },
  mounted () {
    this.queryScenicTop()
  },
  methods: {
    queryScenicTop () {
      this.$get('/cos/scenic-order/queryScenicTop', {
        date: '2025-11'
      }).then((r) => {
        this.scenicRanking = r.data.data
      })
    }
  }
}
</script>

<style scoped>.scenic-top-container {
  padding: 20px;
  background-color: #f5f5f5;
  width: 100%;
}

.ranking-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.ranking-card ::v-deep .ant-card-head {
  background-color: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.ranking-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.ranking-item.top-three {
  background: linear-gradient(90deg, #f0f7ff, #ffffff);
}

.rank-number {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  margin-right: 16px;
}

.rank-number span:not(:first-child) {
  color: #1890ff;
}

.first-place, .second-place, .third-place {
  font-size: 24px;
}

.scenic-image {
  width: 80px;
  height: 60px;
  margin-right: 16px;
  border-radius: 4px;
  overflow: hidden;
}

.scenic-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scenic-info {
  flex: 1;
}

.scenic-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 500;
  color: #262626;
}

.sales-info {
  display: flex;
  justify-content: space-between;
}

.amount {
  color: #595959;
  font-size: 14px;
}

.price {
  color: #ff4d4f;
  font-size: 16px;
  font-weight: 500;
}
</style>
