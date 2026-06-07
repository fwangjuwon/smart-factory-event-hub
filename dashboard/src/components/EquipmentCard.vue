<template>
  <div class="card" :class="cardClass">
    <div class="card__header">
      <div>
        <span class="card__id">{{ equipment.equipmentId }}</span>
        <span class="card__name">{{ equipment.equipmentName }}</span>
      </div>
      <span class="badge" :class="badgeClass">{{ statusLabel }}</span>
    </div>
    <div class="card__path" v-if="equipment.siteName">
      <span class="path-chip">{{ equipment.siteName }}</span>
      <span class="path-sep">›</span>
      <span class="path-chip">{{ equipment.processMainName }}</span>
      <span class="path-sep">›</span>
      <span class="path-chip">{{ equipment.lineName }}</span>
      <span class="path-sep">›</span>
      <span class="path-chip">{{ equipment.processSubName }}</span>
    </div>
    <div class="card__metrics">
      <div class="metric" v-for="m in metrics" :key="m.label">
        <span class="metric__label">{{ m.label }}</span>
        <span class="metric__value" :class="m.alertClass">{{ m.display }}</span>
      </div>
    </div>
    <div class="card__footer">마지막 업데이트: {{ formatTime(equipment.lastUpdated) }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ equipment: Object })

const status = computed(() => props.equipment.overallStatus)
const cardClass  = computed(() => ({
  'card--alert':   status.value === 'ALERT',
  'card--warning': status.value === 'WARNING',
  'card--normal':  status.value === 'NORMAL',
}))
const badgeClass = computed(() => ({
  'badge--alert':   status.value === 'ALERT',
  'badge--warning': status.value === 'WARNING',
  'badge--normal':  status.value === 'NORMAL',
}))
const statusLabel = computed(() =>
  ({ ALERT: '⚠ 이상', WARNING: '△ 주의', NORMAL: '✓ 정상' }[status.value] ?? status.value))

const metrics = computed(() => {
  const e = props.equipment
  return [
    {
      label: '온도',
      display: e.latestTemperature != null ? `${Number(e.latestTemperature).toFixed(1)}℃` : '-',
      alertClass: e.latestTemperature > 80 ? 'alert' : e.latestTemperature > 76 ? 'warn' : '',
    },
    {
      label: '가동률',
      display: e.latestOperationRate != null ? `${Number(e.latestOperationRate).toFixed(1)}%` : '-',
      alertClass: e.latestOperationRate < 70 ? 'alert' : e.latestOperationRate < 73.5 ? 'warn' : '',
    },
    {
      label: '불량률',
      display: e.latestDefectRate != null ? `${Number(e.latestDefectRate).toFixed(1)}%` : '-',
      alertClass: e.latestDefectRate >= 3 ? 'alert' : e.latestDefectRate >= 2.85 ? 'warn' : '',
    },
  ]
})

const formatTime = (t) => t ? new Date(t).toLocaleTimeString('ko-KR') : '-'
</script>

<style scoped>
.card { border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); transition: transform .2s; }
.card:hover { transform: translateY(-2px); }
.card--normal  { background: #fff;    border-left: 4px solid #22c55e; }
.card--warning { background: #fffbeb; border-left: 4px solid #f59e0b; }
.card--alert   { background: #fff7f7; border-left: 4px solid #ef4444; }

.card__header  { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px; }
.card__id      { font-size: 1.1rem; font-weight: 700; display: block; }
.card__name    { font-size: .78rem; color: #64748b; }

.card__path    { display: flex; align-items: center; flex-wrap: wrap; gap: 2px; margin-bottom: 12px; }
.path-chip     { font-size: .72rem; color: #64748b; background: #f1f5f9; padding: 1px 6px; border-radius: 4px; }
.path-sep      { font-size: .7rem; color: #cbd5e1; }

.badge { padding: 4px 10px; border-radius: 20px; font-size: .8rem; font-weight: 600; }
.badge--normal  { background: #dcfce7; color: #16a34a; }
.badge--warning { background: #fef3c7; color: #d97706; }
.badge--alert   { background: #fee2e2; color: #dc2626; }

.card__metrics { display: flex; gap: 12px; }
.metric        { flex: 1; text-align: center; background: #f8fafc; border-radius: 8px; padding: 12px 8px; }
.metric__label { display: block; font-size: .75rem; color: #64748b; margin-bottom: 4px; }
.metric__value { font-size: 1.1rem; font-weight: 700; color: #1e293b; }
.metric__value.warn  { color: #d97706; }
.metric__value.alert { color: #ef4444; }

.card__footer { margin-top: 12px; font-size: .75rem; color: #94a3b8; text-align: right; }
</style>
