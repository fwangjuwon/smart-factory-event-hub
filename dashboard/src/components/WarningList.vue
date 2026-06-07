<template>
  <div class="card-box">
    <h2 class="section-title warning">△ 사전 이상 감지 (WARNING)</h2>
    <div v-if="warnings.length === 0" class="empty">임계값 근접 이벤트 없음</div>
    <div v-else class="table-wrap">
      <table>
        <thead>
          <tr><th>설비</th><th>위치</th><th>이벤트 유형</th><th>측정값</th><th>발생 시각</th></tr>
        </thead>
        <tbody>
          <tr v-for="w in warnings.slice(0, 20)" :key="w.id">
            <td><span class="eq-badge">{{ w.equipmentId }}</span></td>
            <td><span class="location-path">{{ locationOf(w.equipmentId) }}</span></td>
            <td>{{ eventTypeLabel(w.eventType) }}</td>
            <td class="warn-value">{{ formatValue(w) }}</td>
            <td>{{ formatTime(w.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({ warnings: Array, statusMap: Object })

const locationOf = (equipmentId) => {
  const s = props.statusMap?.[equipmentId]
  if (!s?.siteName) return '-'
  return `${s.siteName} › ${s.processMainName} › ${s.lineName}`
}
const eventTypeLabel = (t) => ({ TEMPERATURE: '온도', OPERATION_RATE: '가동률', DEFECT_RATE: '불량률' }[t] ?? t)
const formatValue    = (e) => `${Number(e.value).toFixed(1)}${e.eventType === 'TEMPERATURE' ? '℃' : '%'}`
const formatTime     = (t) => t ? new Date(t).toLocaleString('ko-KR') : '-'
</script>

<style scoped>
.section-title         { font-size: 1rem; font-weight: 700; margin-bottom: 16px; }
.section-title.warning { color: #d97706; }
.empty      { text-align: center; color: #94a3b8; padding: 24px; }
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: .88rem; }
th  { background: #fef9ec; padding: 10px 12px; text-align: left; color: #92400e; font-weight: 600; }
td  { padding: 10px 12px; border-bottom: 1px solid #fef3c7; color: #334155; }
tr:last-child td { border-bottom: none; }
.eq-badge      { background: #fef3c7; color: #b45309; padding: 2px 8px; border-radius: 10px; font-weight: 600; font-size: .8rem; }
.location-path { font-size: .78rem; color: #64748b; }
.warn-value    { color: #d97706; font-weight: 700; }
</style>
