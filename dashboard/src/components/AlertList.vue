<template>
  <div class="card-box">
    <h2 class="section-title alert">⚠ 이상 이벤트 (ALERT)</h2>
    <div v-if="alerts.length === 0" class="empty">이상 이벤트 없음</div>
    <div v-else class="table-wrap">
      <table>
        <thead>
          <tr><th>설비</th><th>위치</th><th>이벤트 유형</th><th>측정값</th><th>발생 시각</th></tr>
        </thead>
        <tbody>
          <tr v-for="a in alerts.slice(0, 20)" :key="a.id">
            <td><span class="eq-badge">{{ a.equipmentId }}</span></td>
            <td><span class="location-path">{{ locationOf(a.equipmentId) }}</span></td>
            <td>{{ eventTypeLabel(a.eventType) }}</td>
            <td class="alert-value">{{ formatValue(a) }}</td>
            <td>{{ formatTime(a.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({ alerts: Array, statusMap: Object })

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
.section-title       { font-size: 1rem; font-weight: 700; margin-bottom: 16px; }
.section-title.alert { color: #dc2626; }
.empty      { text-align: center; color: #94a3b8; padding: 24px; }
.table-wrap { overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: .88rem; }
th  { background: #fff1f2; padding: 10px 12px; text-align: left; color: #9f1239; font-weight: 600; }
td  { padding: 10px 12px; border-bottom: 1px solid #fee2e2; color: #334155; }
tr:last-child td { border-bottom: none; }
.eq-badge    { background: #fee2e2; color: #dc2626; padding: 2px 8px; border-radius: 10px; font-weight: 600; font-size: .8rem; }
.location-path { font-size: .78rem; color: #64748b; }
.alert-value { color: #ef4444; font-weight: 700; }
</style>
