<template>
  <div class="page">
    <div class="spec-layout">

      <!-- 좌측: 아코디언 트리 -->
      <aside class="tree-panel card-box">
        <h2 class="panel-title">공정 선택</h2>
        <HierarchyTree
          v-if="hierarchy.length"
          :hierarchy="hierarchy"
          :selectedType="selectedType"
          :selectedNode="selectedNode"
          @select="onSelect"
        />
        <div v-else class="empty">불러오는 중...</div>
      </aside>

      <!-- 우측: 스펙 테이블 -->
      <main class="card-box">
        <div class="spec-header">
          <h2 class="panel-title">{{ selectedLabel }} — 스펙 관리</h2>
          <p class="spec-desc">
            경계값과 오차율을 설정합니다.
            오차 범위 내 진입 시 <span class="warn-text">WARNING</span>,
            경계값 초과 시 <span class="alert-text">ALERT</span>가 발생합니다.
          </p>
        </div>

        <div class="threshold-guide">
          <span class="guide-dot normal"></span> NORMAL
          <span class="arrow">←── tolerance% ──→</span>
          <span class="guide-dot warning"></span> WARNING
          <span class="arrow">── 경계값 ──</span>
          <span class="guide-dot alert"></span> ALERT
        </div>

        <div v-if="loading" class="empty">불러오는 중...</div>
        <div v-else-if="editableSpecs.length === 0" class="empty">해당 공정에 설비가 없습니다.</div>
        <table v-else>
          <thead>
            <tr>
              <th>설비 ID</th>
              <th>이벤트 유형</th>
              <th>경계값</th>
              <th>오차율 (%)</th>
              <th>WARNING 발동 구간</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <template v-for="(group, eqId) in groupedSpecs" :key="eqId">
              <tr v-for="(spec, idx) in group" :key="spec.id">
                <td v-if="idx === 0" :rowspan="group.length" class="eq-cell">
                  <span class="eq-badge">{{ eqId }}</span>
                </td>
                <td>{{ eventTypeLabel(spec.eventType) }}</td>
                <td>
                  <input v-model.number="spec.threshold" type="number" class="input" step="0.1" />
                  <span class="unit">{{ unit(spec.eventType) }}</span>
                </td>
                <td>
                  <input v-model.number="spec.tolerancePct" type="number" class="input" step="0.5" min="0" max="50" />
                  <span class="unit">%</span>
                </td>
                <td class="range-cell">{{ warningRange(spec) }}</td>
                <td>
                  <button class="btn-save" @click="save(spec)" :disabled="spec.saving">
                    {{ spec.saving ? '저장 중...' : '저장' }}
                  </button>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
        <div v-if="savedMsg" class="saved-msg">✓ 저장되었습니다.</div>

        <!-- 스펙 추가 폼 -->
        <div class="add-section">
          <button class="btn-add-toggle" @click="showAddForm = !showAddForm">
            {{ showAddForm ? '✕ 닫기' : '+ 스펙 추가' }}
          </button>
          <div v-if="showAddForm" class="add-form">
            <h3 class="add-title">새 스펙 등록</h3>
            <div class="form-grid">
              <div class="form-field">
                <label>소공정 <span class="required">*</span></label>
                <select v-model.number="newSpec.processSubId" class="input input-wide">
                  <option :value="null">선택</option>
                  <option v-for="ps in processSubOptions" :key="ps.id" :value="ps.id">{{ ps.label }}</option>
                </select>
              </div>
              <div class="form-field">
                <label>설비 ID <span class="required">*</span></label>
                <input v-model="newSpec.equipmentId" placeholder="예) EQ-H" class="input" />
              </div>
              <div class="form-field">
                <label>설비명</label>
                <input v-model="newSpec.equipmentName" placeholder="예) 음극 믹서" class="input" />
              </div>
              <div class="form-field">
                <label>이벤트 유형 <span class="required">*</span></label>
                <select v-model="newSpec.eventType" class="input">
                  <option value="">선택</option>
                  <option value="TEMPERATURE">온도</option>
                  <option value="OPERATION_RATE">가동률</option>
                  <option value="DEFECT_RATE">불량률</option>
                </select>
              </div>
              <div class="form-field">
                <label>경계값</label>
                <div class="input-inline">
                  <input v-model.number="newSpec.threshold" type="number" step="0.1" class="input" />
                  <span class="unit">{{ unit(newSpec.eventType) }}</span>
                </div>
              </div>
              <div class="form-field">
                <label>오차율</label>
                <div class="input-inline">
                  <input v-model.number="newSpec.tolerancePct" type="number" step="0.5" min="0" max="50" class="input" />
                  <span class="unit">%</span>
                </div>
              </div>
            </div>
            <div class="form-preview" v-if="newSpec.eventType && newSpec.threshold">
              WARNING 발동 구간: <strong>{{ warningRange(newSpec) }}</strong>
            </div>
            <div class="form-actions">
              <button class="btn-save" @click="addSpec" :disabled="!canAdd || adding">
                {{ adding ? '등록 중...' : '등록' }}
              </button>
              <span v-if="addError" class="error-msg">{{ addError }}</span>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import HierarchyTree from '../components/HierarchyTree.vue'
import { getHierarchy, getSpecs, saveSpec,
         getStatusBySite, getStatusByProcessMain,
         getStatusByLine, getStatusByProcessSub } from '../api/factoryApi.js'

const hierarchy    = ref([])
const allSpecs     = ref([])
const loading      = ref(true)
const savedMsg     = ref(false)
const selectedType = ref(null)
const selectedNode = ref(null)
const filteredIds  = ref(null)
const showAddForm  = ref(false)
const adding       = ref(false)
const addError     = ref('')
const newSpec      = ref({ equipmentId: '', equipmentName: '', eventType: '', threshold: null, tolerancePct: 5, processSubId: null })
const canAdd       = computed(() =>
  newSpec.value.equipmentId && newSpec.value.eventType && newSpec.value.threshold != null && newSpec.value.processSubId)

// 선택된 노드 하위 소공정 목록 추출 (드롭다운용)
const processSubOptions = computed(() => {
  const t = selectedType.value, n = selectedNode.value
  if (!t || !n) {
    return hierarchy.value.flatMap(s => s.processMainList.flatMap(pm => pm.lines.flatMap(l => l.processSubs.map(ps => ({ ...ps, label: `${s.name} › ${pm.name} › ${l.name} › ${ps.name}` })))))
  }
  if (t === 'processSub') return [{ ...n, label: n.name }]
  if (t === 'line')        return (n.processSubs || []).map(ps => ({ ...ps, label: ps.name }))
  if (t === 'processMain') return (n.lines || []).flatMap(l => (l.processSubs || []).map(ps => ({ ...ps, label: `${l.name} › ${ps.name}` })))
  if (t === 'site')        return (n.processMainList || []).flatMap(pm => (pm.lines || []).flatMap(l => (l.processSubs || []).map(ps => ({ ...ps, label: `${pm.name} › ${l.name} › ${ps.name}` }))))
  return []
})

const selectedLabel = computed(() => selectedNode.value?.name ?? '전체')

const editableSpecs = computed(() =>
  filteredIds.value
    ? allSpecs.value.filter(s => filteredIds.value.includes(s.equipmentId))
    : allSpecs.value
)
const groupedSpecs = computed(() =>
  editableSpecs.value.reduce((acc, s) => {
    if (!acc[s.equipmentId]) acc[s.equipmentId] = []
    acc[s.equipmentId].push(s)
    return acc
  }, {})
)

const onSelect = async (type, node) => {
  selectedType.value = type
  selectedNode.value = node
  if (!type || !node) { filteredIds.value = null; return }
  const fetchers = {
    site:        () => getStatusBySite(node.id),
    processMain: () => getStatusByProcessMain(node.id),
    line:        () => getStatusByLine(node.id),
    processSub:  () => getStatusByProcessSub(node.id),
  }
  const statusList = await fetchers[type]()
  filteredIds.value = statusList.map(s => s.equipmentId)
}

const save = async (spec) => {
  spec.saving = true
  try {
    await saveSpec({ id: spec.id, equipmentId: spec.equipmentId,
                     eventType: spec.eventType, threshold: spec.threshold,
                     tolerancePct: spec.tolerancePct })
    savedMsg.value = true
    setTimeout(() => savedMsg.value = false, 2000)
  } finally { spec.saving = false }
}

const eventTypeLabel = (t) => ({ TEMPERATURE: '온도', OPERATION_RATE: '가동률', DEFECT_RATE: '불량률' }[t] ?? t)
const unit = (t) => t === 'TEMPERATURE' ? '℃' : '%'
const warningRange = (spec) => {
  const t = Number(spec.threshold), m = t * Number(spec.tolerancePct) / 100
  if (spec.eventType === 'TEMPERATURE')    return `${(t-m).toFixed(1)}℃ ~ ${t}℃`
  if (spec.eventType === 'OPERATION_RATE') return `${t}% ~ ${(t+m).toFixed(1)}%`
  if (spec.eventType === 'DEFECT_RATE')    return `${(t-m).toFixed(2)}% ~ ${t}%`
  return '-'
}

const addSpec = async () => {
  addError.value = ''
  adding.value   = true
  try {
    const saved = await saveSpec({
      equipmentId:  newSpec.value.equipmentId.trim().toUpperCase(),
      equipmentName: newSpec.value.equipmentName.trim() || newSpec.value.equipmentId.trim().toUpperCase(),
      eventType:    newSpec.value.eventType,
      threshold:    newSpec.value.threshold,
      tolerancePct: newSpec.value.tolerancePct,
      processSubId: newSpec.value.processSubId,
    })
    allSpecs.value.push({ ...saved, saving: false })
    newSpec.value  = { equipmentId: '', equipmentName: '', eventType: '', threshold: null, tolerancePct: 5, processSubId: null }
    showAddForm.value  = false
    // 새로 추가된 항목이 보이도록 필터 초기화
    filteredIds.value  = null
    selectedType.value = null
    selectedNode.value = null
    savedMsg.value = true
    setTimeout(() => savedMsg.value = false, 2000)
  } catch (e) {
    addError.value = '등록 실패: 이미 존재하거나 잘못된 값입니다.'
  } finally {
    adding.value = false
  }
}

onMounted(async () => {
  const [hier, specs] = await Promise.all([getHierarchy(), getSpecs()])
  hierarchy.value = hier
  allSpecs.value  = specs.map(s => ({ ...s, saving: false }))
  loading.value   = false
})
</script>

<style scoped>
.spec-layout { display: grid; grid-template-columns: 280px 1fr; gap: 16px; align-items: start; }
@media (max-width: 900px) { .spec-layout { grid-template-columns: 1fr; } }

.tree-panel  { height: calc(100vh - 100px); overflow-y: auto; position: sticky; top: 72px; }
.panel-title { font-size: 1rem; font-weight: 700; color: #1e293b; margin-bottom: 4px; }
.spec-desc   { font-size: .82rem; color: #64748b; margin-bottom: 16px; }
.warn-text   { color: #d97706; font-weight: 600; }
.alert-text  { color: #dc2626; font-weight: 600; }

.threshold-guide {
  display: flex; align-items: center; gap: 8px; flex-wrap: wrap;
  background: #f8fafc; border-radius: 8px; padding: 10px 14px;
  margin-bottom: 20px; font-size: .82rem; color: #475569;
}
.guide-dot { width: 9px; height: 9px; border-radius: 50%; display: inline-block; }
.guide-dot.normal  { background: #22c55e; }
.guide-dot.warning { background: #f59e0b; }
.guide-dot.alert   { background: #ef4444; }
.arrow { color: #94a3b8; font-size: .78rem; }

table { width: 100%; border-collapse: collapse; font-size: .88rem; }
th { background: #f1f5f9; padding: 10px 12px; text-align: left; color: #475569; font-weight: 600; }
td { padding: 9px 12px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
tr:last-child td { border-bottom: none; }

.eq-cell  { vertical-align: middle; }
.eq-badge { background: #e0f2fe; color: #0369a1; padding: 3px 10px; border-radius: 10px; font-weight: 600; font-size: .8rem; }
.input    { width: 80px; padding: 5px 8px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: .88rem; }
.unit     { margin-left: 4px; color: #64748b; font-size: .82rem; }
.range-cell { font-size: .82rem; color: #d97706; font-weight: 600; }
.btn-save { padding: 5px 12px; background: #0f172a; color: #fff; border: none; border-radius: 6px; cursor: pointer; font-size: .83rem; }
.btn-save:hover:not(:disabled) { background: #1e293b; }
.btn-save:disabled { background: #94a3b8; cursor: not-allowed; }
.saved-msg { margin-top: 12px; color: #16a34a; font-size: .9rem; font-weight: 600; }
.empty { text-align: center; color: #94a3b8; padding: 40px; }

/* 추가 폼 */
.add-section   { margin-top: 24px; border-top: 1px solid #f1f5f9; padding-top: 16px; }
.btn-add-toggle {
  padding: 7px 16px; border: 1.5px dashed #cbd5e1; background: transparent;
  border-radius: 8px; cursor: pointer; font-size: .88rem; color: #475569;
  transition: all .15s;
}
.btn-add-toggle:hover { border-color: #0f172a; color: #0f172a; background: #f8fafc; }

.add-form    { margin-top: 16px; background: #f8fafc; border-radius: 10px; padding: 20px; }
.add-title   { font-size: .95rem; font-weight: 700; color: #1e293b; margin-bottom: 16px; }
.form-grid   { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 12px 16px; margin-bottom: 12px; }
.form-field  { display: flex; flex-direction: column; gap: 4px; }
.form-field label { font-size: .78rem; font-weight: 600; color: #64748b; }
.input-inline { display: flex; align-items: center; gap: 6px; }

.form-preview {
  font-size: .82rem; color: #475569; background: #fff;
  border: 1px solid #e2e8f0; border-radius: 6px; padding: 8px 12px; margin-bottom: 12px;
}
.form-preview strong { color: #d97706; }

.form-actions { display: flex; align-items: center; gap: 12px; }
.error-msg    { font-size: .82rem; color: #dc2626; }
.input-wide   { width: 100%; max-width: 280px; }
.required     { color: #ef4444; font-size: .75rem; }
</style>
