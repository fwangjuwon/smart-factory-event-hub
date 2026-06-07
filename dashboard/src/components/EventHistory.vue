<template>
  <div class="event-history">
    <div class="history-header">
      <h2 class="section-title">이벤트 이력</h2>
      <div class="filter">
        <label>설비 선택</label>
        <select v-model="selectedId" @change="$emit('select', selectedId)">
          <option v-for="id in equipmentIds" :key="id" :value="id">{{ id }}</option>
        </select>
      </div>
    </div>
    <div v-if="events.length === 0" class="empty">데이터 없음</div>
    <div v-else class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>설비</th>
            <th>이벤트 유형</th>
            <th>측정값</th>
            <th>상태</th>
            <th>발생 시각</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="e in events.slice(0, 50)" :key="e.id">
            <td><span class="eq-badge">{{ e.equipmentId }}</span></td>
            <td>{{ eventTypeLabel(e.eventType) }}</td>
            <td>{{ formatValue(e) }}</td>
            <td>
              <span class="status-badge" :class="e.status === 'ALERT' ? 'status-badge--alert' : 'status-badge--normal'">
                {{ e.status === 'ALERT' ? '이상' : '정상' }}
              </span>
            </td>
            <td>{{ formatTime(e.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  events: Array,
  equipmentIds: Array,
})
defineEmits(['select'])

const selectedId = ref(props.equipmentIds?.[0] ?? 'EQ-A')

watch(() => props.equipmentIds, (ids) => {
  if (ids?.length && !ids.includes(selectedId.value)) selectedId.value = ids[0]
})

const eventTypeLabel = (t) => ({ TEMPERATURE: '온도', OPERATION_RATE: '가동률', DEFECT_RATE: '불량률' }[t] ?? t)
const formatValue = (e) => {
  const v = Number(e.value).toFixed(1)
  return e.eventType === 'TEMPERATURE' ? `${v}℃` : `${v}%`
}
const formatTime = (t) => t ? new Date(t).toLocaleString('ko-KR') : '-'
</script>

<style scoped>
.event-history { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.history-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-title { font-size: 1rem; font-weight: 700; color: #1e293b; margin: 0; }
.filter { display: flex; align-items: center; gap: 8px; font-size: .9rem; color: #475569; }
.filter select { padding: 6px 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: .9rem; }
.empty { text-align: center; color: #94a3b8; padding: 24px; }
.table-wrap { overflow-x: auto; }

table { width: 100%; border-collapse: collapse; font-size: .9rem; }
th { background: #f1f5f9; padding: 10px 12px; text-align: left; color: #475569; font-weight: 600; }
td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; color: #334155; }
tr:last-child td { border-bottom: none; }

.eq-badge { background: #e0f2fe; color: #0369a1; padding: 2px 8px; border-radius: 10px; font-weight: 600; font-size: .8rem; }
.status-badge { padding: 2px 8px; border-radius: 10px; font-size: .8rem; font-weight: 600; }
.status-badge--normal { background: #dcfce7; color: #16a34a; }
.status-badge--alert  { background: #fee2e2; color: #dc2626; }
</style>
