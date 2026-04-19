<template>
  <div class="emotion-chart" v-loading="loading">
    <div ref="chartRef" class="chart"></div>
    <div v-if="!loading && (!data || data.length === 0)" class="empty-chart">
      <el-empty description="暂无数据" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const chartRef = ref(null)
let chartInstance = null

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return

  chartInstance = echarts.init(chartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: 'rgba(124, 156, 181, 0.3)',
      borderWidth: 1,
      textStyle: {
        color: '#5A5A5A'
      },
      formatter: (params) => {
        const data = params[0]
        return `
          <div style="padding: 4px 0;">
            <div style="font-weight: 600; margin-bottom: 4px;">${data.name}</div>
            <div>情绪得分: <span style="color: #7C9CB5; font-weight: 600;">${data.value}</span></div>
          </div>
        `
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: props.data.map(item => item.date),
      axisLine: {
        lineStyle: {
          color: 'rgba(124, 156, 181, 0.3)'
        }
      },
      axisLabel: {
        color: '#909399',
        fontSize: 11
      },
      axisTick: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 1,
      interval: 0.2,
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#909399',
        fontSize: 11,
        formatter: (value) => value.toFixed(1)
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(124, 156, 181, 0.1)',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '情绪得分',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          color: '#7C9CB5',
          width: 3,
          shadowColor: 'rgba(124, 156, 181, 0.3)',
          shadowBlur: 10
        },
        itemStyle: {
          color: '#7C9CB5',
          borderColor: '#fff',
          borderWidth: 2,
          shadowColor: 'rgba(124, 156, 181, 0.3)',
          shadowBlur: 5
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: 'rgba(124, 156, 181, 0.3)'
            },
            {
              offset: 1,
              color: 'rgba(124, 156, 181, 0.02)'
            }
          ])
        },
        data: props.data.map(item => item.score),
        animationDuration: 1500,
        animationEasing: 'cubicOut'
      }
    ]
  }

  chartInstance.setOption(option)
}

// 更新图表
const updateChart = () => {
  if (!chartInstance) {
    nextTick(initChart)
    return
  }

  chartInstance.setOption({
    xAxis: {
      data: props.data.map(item => item.date)
    },
    series: [{
      data: props.data.map(item => item.score)
    }]
  })
}

// 窗口 resize 监听
const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  nextTick(initChart)
  window.addEventListener('resize', handleResize)
})

watch(() => props.data, () => {
  updateChart()
}, { deep: true })

watch(() => props.loading, (loading) => {
  if (!loading) {
    nextTick(updateChart)
  }
})
</script>

<style scoped>
.emotion-chart {
  width: 100%;
  height: 200px;
  position: relative;
}

.chart {
  width: 100%;
  height: 100%;
}

.empty-chart {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
</style>
