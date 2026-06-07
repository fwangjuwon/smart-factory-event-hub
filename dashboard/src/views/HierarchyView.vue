<template>
  <div class="page">
    <div class="hierarchy-layout">

      <!-- 좌측: 아코디언 트리 -->
      <aside class="tree-panel card-box">
        <h2 class="panel-title">생산 라인</h2>
        <div v-if="!hierarchy.length" class="empty">불러오는 중...</div>
        <HierarchyTree
          v-else
          :hierarchy="hierarchy"
          :selectedType="selectedType"
          :selectedNode="selectedNode"
          @select="onSelect"
        />
      </aside>

      <!-- 우측 -->
      <main class="status-panel">
        <div class="card-box">
          <div class="status-header">
            <h2 class="panel-title">{{ selectedLabel }} — 설비 현황</h2>
            <span class="refresh-hint">3초 자동 갱신</span>
          </div>
          <div v-if="!selectedType" class="empty">좌측에서 항목을 선택하세요.</div>
          <div v-else-if="statusList.length === 0" class="empty">해당 범위에 설비가 없습니다.</div>
          <div v-else class="status-grid">
            <EquipmentCard v-for="eq in statusList" :key="eq.equipmentId" :equipment="eq" />
          </div>
        </div>

        <template v-if="selectedType">
          <WarningList :warnings="filteredWarnings" :statusMap="statusMap" />
          <AlertList   :alerts="filteredAlerts"     :statusMap="statusMap" />
        </template>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import HierarchyTree from '../components/HierarchyTree.vue'
import EquipmentCard from '../components/EquipmentCard.vue'
import WarningList   from '../components/WarningList.vue'
import AlertList     from '../components/AlertList.vue'
import {
  getHierarchy,
  getStatusBySite, getStatusByProcessMain, getStatusByLine, getStatusByProcessSub,
  getAlerts, getWarnings,
} from '../api/factoryApi.js'

const hierarchy    = ref([])
const statusList   = ref([])
const allAlerts    = ref([])
const allWarnings  = ref([])
const selectedType = ref(null)
const selectedNode = ref(null)

const selectedLabel = computed(() => selectedNode.value?.name ?? '전체')

const selectedEquipmentIds = computed(() => statusList.value.map(e => e.equipmentId))
const filteredAlerts   = computed(() => allAlerts.value.filter(a => selectedEquipmentIds.value.includes(a.equipmentId)))
const filteredWarnings = computed(() => allWarnings.value.filter(w => selectedEquipmentIds.value.includes(w.equipmentId)))
const statusMap = computed(() => Object.fromEntries(statusList.value.map(e => [e.equipmentId, e])))

const onSelect = async (type, node) => {
  selectedType.value = type
  selectedNode.value = node
  await fetchStatus()
}

const fetchStatus = async () => {
  if (!selectedType.value || !selectedNode.value) return
  const id = selectedNode.value.id
  const fetchers = {
    site:        () => getStatusBySite(id),
    processMain: () => getStatusByProcessMain(id),
    line:        () => getStatusByLine(id),
    processSub:  () => getStatusByProcessSub(id),
  }
  statusList.value = await fetchers[selectedType.value]()
}

const fetchAll = async () => {
  const [hier, alerts, warnings] = await Promise.all([getHierarchy(), getAlerts(), getWarnings()])
  hierarchy.value   = hier
  allAlerts.value   = alerts
  allWarnings.value = warnings
  await fetchStatus()
}

let timer
onMounted(() => { fetchAll(); timer = setInterval(fetchAll, 3000) })
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.hierarchy-layout { display: grid; grid-template-columns: 280px 1fr; gap: 16px; align-items: start; }
@media (max-width: 900px) { .hierarchy-layout { grid-template-columns: 1fr; } }

.tree-panel   { height: calc(100vh - 100px); overflow-y: auto; position: sticky; top: 72px; }
.status-panel { display: flex; flex-direction: column; gap: 16px; }

.panel-title  { font-size: 1rem; font-weight: 700; color: #1e293b; margin-bottom: 16px; }
.status-header{ display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.refresh-hint { font-size: .75rem; color: #94a3b8; }
.status-grid  { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 12px; }
.empty        { text-align: center; color: #94a3b8; padding: 40px; }
</style>
