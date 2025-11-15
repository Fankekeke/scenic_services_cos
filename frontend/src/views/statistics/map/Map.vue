<template>
  <div id="areas" style="width: 100%;height: 810px;box-shadow: 0 0 0 10px white;"></div>
</template>

<script>
import baiduMap from '@/utils/map/baiduMap'
export default {
  name: 'Map',
  data () {
    return {
      map: null
    }
  },
  mounted () {
    baiduMap.initMap('areas')
    this.queryScenicListRate()
  },
  methods: {
    queryScenicListRate () {
      this.$get('/cos/scenic-order/queryScenicListRate').then((r) => {
        const data = r.data.data
        this.generateHeatMap(data)
      })
    },
    generateHeatMap (scenicData) {
    // Get the Baidu map instance
      const map = baiduMap.rMap()

      // Process data to create heatmap points
      const heatmapData = scenicData.map(item => {
        const [lng, lat] = item.point.split(',').map(Number)
        return {
          lng: lng,
          lat: lat,
          count: Math.round(item.hot * 100) // Scale hot value for better visualization
        }
      })
      // Configure heatmap options
      const heatmapOptions = {
        radius: 15, // Heatmap point radius
        visible: true, // Show heatmap
        gradient: { // Color gradient
          0.1: 'rgb(0, 0, 255)', // Blue
          0.3: 'rgb(0, 255, 255)', // Cyan
          0.5: 'rgb(0, 255, 0)', // Green
          0.7: 'rgb(255, 255, 0)', // Yellow
          0.9: 'rgb(255, 0, 0)' // Red
        }
      }

      // Create heatmap overlay
      // eslint-disable-next-line no-undef
      const heatmapOverlay = new BMapLib.HeatmapOverlay(heatmapOptions)
      // Add overlay to map
      map.addOverlay(heatmapOverlay)
      // Set heatmap data
      heatmapOverlay.setDataSet({data: heatmapData})
    }
  }
}
</script>

<style scoped>

</style>
