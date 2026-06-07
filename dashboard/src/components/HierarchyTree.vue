<template>
  <div class="tree">
    <div class="tree-item all-node" :class="{ active: !selectedNode }" @click="$emit('select', null, null)">
      🏭 전체 설비
    </div>

    <div v-for="site in hierarchy" :key="site.id">
      <!-- SITE -->
      <div class="tree-row">
        <button class="chevron-btn" @click="toggle('site', site.id)">
          {{ isOpen('site', site.id) ? '▾' : '▸' }}
        </button>
        <div class="tree-node site-node"
             :class="{ active: isActive('site', site.id) }"
             @click="$emit('select', 'site', site)">
          🏢 {{ site.name }}
          <span class="loc-tag">{{ site.location }}</span>
        </div>
      </div>

      <template v-if="isOpen('site', site.id)">
        <div v-for="pm in site.processMainList" :key="pm.id" class="indent-1">
          <!-- 대공정 -->
          <div class="tree-row">
            <button class="chevron-btn" @click="toggle('pm', pm.id)">
              {{ isOpen('pm', pm.id) ? '▾' : '▸' }}
            </button>
            <div class="tree-node pm-node"
                 :class="{ active: isActive('processMain', pm.id) }"
                 @click="$emit('select', 'processMain', pm)">
              ⚙ {{ pm.name }}
            </div>
          </div>

          <template v-if="isOpen('pm', pm.id)">
            <div v-for="line in pm.lines" :key="line.id" class="indent-2">
              <!-- 라인 -->
              <div class="tree-row">
                <button class="chevron-btn" @click="toggle('line', line.id)">
                  {{ isOpen('line', line.id) ? '▾' : '▸' }}
                </button>
                <div class="tree-node line-node"
                     :class="{ active: isActive('line', line.id) }"
                     @click="$emit('select', 'line', line)">
                  ▶ {{ line.name }}
                </div>
              </div>

              <template v-if="isOpen('line', line.id)">
                <div v-for="ps in line.processSubs" :key="ps.id" class="indent-3">
                  <!-- 소공정 -->
                  <div class="tree-row">
                    <span class="chevron-placeholder"></span>
                    <div class="tree-node sub-node"
                         :class="{ active: isActive('processSub', ps.id) }"
                         @click="$emit('select', 'processSub', ps)">
                      • {{ ps.name }}
                      <span class="eq-tags">
                        <span v-for="eqId in ps.equipmentIds" :key="eqId" class="eq-chip">{{ eqId }}</span>
                      </span>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </template>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  hierarchy:    Array,
  selectedType: String,
  selectedNode: Object,
})
defineEmits(['select'])

const openMap = ref({ site: new Set(), pm: new Set(), line: new Set() })

const toggle = (level, id) => {
  const s = openMap.value[level]
  s.has(id) ? s.delete(id) : s.add(id)
}
const isOpen   = (level, id) => openMap.value[level]?.has(id)
const isActive = (type, id)  => props.selectedType === type && props.selectedNode?.id === id
</script>

<style scoped>
.tree { font-size: .87rem; }

.all-node {
  padding: 8px 10px; border-radius: 6px; cursor: pointer;
  font-weight: 600; color: #0f172a; margin-bottom: 6px;
}
.all-node:hover  { background: #f1f5f9; }
.all-node.active { background: #e0f2fe; color: #0369a1; }

.tree-row {
  display: flex; align-items: center; gap: 2px;
  margin-bottom: 2px;
}
.chevron-btn {
  width: 20px; height: 20px; flex-shrink: 0;
  background: none; border: none; cursor: pointer;
  color: #94a3b8; font-size: .75rem; padding: 0;
  display: flex; align-items: center; justify-content: center;
  border-radius: 4px;
}
.chevron-btn:hover { background: #f1f5f9; color: #475569; }
.chevron-placeholder { width: 20px; flex-shrink: 0; }

.tree-node {
  flex: 1; padding: 6px 8px; border-radius: 6px; cursor: pointer;
  color: #334155; transition: background .12s;
  display: flex; align-items: center; gap: 6px; flex-wrap: wrap;
}
.tree-node:hover  { background: #f1f5f9; }
.tree-node.active { background: #e0f2fe; color: #0369a1; font-weight: 600; }

.site-node { font-weight: 700; color: #0f172a; }
.pm-node   { }
.line-node { }
.sub-node  { color: #475569; }

.indent-1 { padding-left: 8px; }
.indent-2 { padding-left: 16px; }
.indent-3 { padding-left: 24px; }

.loc-tag  { font-size: .72rem; color: #94a3b8; font-weight: 400; }
.eq-tags  { display: flex; gap: 3px; flex-wrap: wrap; }
.eq-chip  { background: #e0f2fe; color: #0369a1; padding: 1px 5px; border-radius: 8px; font-size: .7rem; font-weight: 600; }
</style>
