<template>
  <div class="page">

    <!-- 날짜 선택 -->
    <div class="date-bar card-box">
      <label class="date-label">조회 기준일</label>
      <input type="date" v-model="selectedDate" class="date-input" @change="fetchStats" />
      <span class="date-hint">일별 공정 현황을 표시합니다</span>
    </div>

    <!-- SITE 카드 목록 -->
    <div class="site-grid">
      <div v-for="site in stats" :key="site.siteId" class="site-card card-box">

        <!-- 사이트 헤더 -->
        <div class="site-header" @click="toggleSite(site.siteId)">
          <div class="site-info">
            <span class="site-name">🏢 {{ site.siteName }}</span>
            <span class="site-loc">{{ site.siteLocation }}</span>
          </div>
          <div class="site-kpi">
            <div class="kpi-item">
              <span class="kpi-label">평균 가동률</span>
              <span class="kpi-value" :class="oprClass(site.avgOperationRate)">
                {{ fmt(site.avgOperationRate) }}%
              </span>
            </div>
            <div class="kpi-item">
              <span class="kpi-label">수율</span>
              <span class="kpi-value" :class="yieldClass(site.yieldRate)">
                {{ fmt(site.yieldRate) }}%
              </span>
            </div>
            <div class="kpi-item">
              <span class="kpi-label">ALERT</span>
              <span class="kpi-value" :class="site.alertCount > 0 ? 'text-alert' : 'text-ok'">
                {{ site.alertCount }}건
              </span>
            </div>
            <div class="kpi-item">
              <span class="kpi-label">WARNING</span>
              <span class="kpi-value" :class="site.warningCount > 0 ? 'text-warn' : 'text-ok'">
                {{ site.warningCount }}건
              </span>
            </div>
          </div>
          <span class="chevron">{{ expandedSites.has(site.siteId) ? '▲' : '▼' }}</span>
        </div>

        <!-- 대공정 상세 (접힘/펼침) -->
        <div v-show="expandedSites.has(site.siteId)" class="pm-table-wrap">
          <table class="pm-table">
            <thead>
              <tr>
                <th>대공정</th>
                <th>평균 가동률</th>
                <th>수율</th>
                <th>ALERT</th>
                <th>WARNING</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="pm in site.processMainList" :key="pm.processMainId">
                <td class="pm-name">{{ pm.processMainName }}</td>
                <td>
                  <div class="bar-wrap">
                    <div class="bar" :class="oprClass(pm.avgOperationRate)"
                         :style="{ width: pm.avgOperationRate + '%' }"></div>
                  </div>
                  <span :class="oprClass(pm.avgOperationRate)">{{ fmt(pm.avgOperationRate) }}%</span>
                </td>
                <td>
                  <div class="bar-wrap">
                    <div class="bar yield-bar" :class="yieldClass(pm.yieldRate)"
                         :style="{ width: pm.yieldRate + '%' }"></div>
                  </div>
                  <span :class="yieldClass(pm.yieldRate)">{{ fmt(pm.yieldRate) }}%</span>
                </td>
                <td>
                  <span v-if="pm.alertCount > 0" class="count-badge alert-badge">{{ pm.alertCount }}건</span>
                  <span v-else class="count-ok">-</span>
                </td>
                <td>
                  <span v-if="pm.warningCount > 0" class="count-badge warn-badge">{{ pm.warningCount }}건</span>
                  <span v-else class="count-ok">-</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="stats.length === 0" class="card-box empty">데이터를 불러오는 중...</div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDailyStats } from '../api/factoryApi.js'

const stats        = ref([])
const selectedDate = ref(new Date().toISOString().slice(0, 10))
const expandedSites = ref(new Set())

const fetchStats = async () => {
  stats.value = await getDailyStats(selectedDate.value)
  // 기본으로 전체 펼침
  stats.value.forEach(s => expandedSites.value.add(s.siteId))
}

const toggleSite = (id) => {
  if (expandedSites.value.has(id)) expandedSites.value.delete(id)
  else expandedSites.value.add(id)
}

const fmt       = (v) => v != null ? Number(v).toFixed(1) : '-'
const oprClass  = (v) => v < 85 ? 'text-alert' : v < 90 ? 'text-warn' : 'text-ok'
const yieldClass = (v) => v < 99 ? 'text-alert' : v < 99.5 ? 'text-warn' : 'text-ok'

onMounted(fetchStats)
</script>

<style scoped>
.date-bar   { display: flex; align-items: center; gap: 12px; }
.date-label { font-size: .88rem; font-weight: 600; color: #475569; }
.date-input { padding: 6px 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: .9rem; }
.date-hint  { font-size: .8rem; color: #94a3b8; }

.site-grid { display: flex; flex-direction: column; gap: 16px; }

/* 사이트 카드 */
.site-card   { overflow: hidden; }
.site-header {
  display: flex; align-items: center; gap: 16px;
  cursor: pointer; user-select: none;
  padding: 4px 0;
}
.site-header:hover { opacity: .85; }

.site-info   { min-width: 140px; }
.site-name   { font-size: 1rem; font-weight: 700; color: #0f172a; display: block; }
.site-loc    { font-size: .78rem; color: #94a3b8; }

.site-kpi    { display: flex; gap: 24px; flex: 1; }
.kpi-item    { text-align: center; }
.kpi-label   { display: block; font-size: .72rem; color: #64748b; margin-bottom: 2px; }
.kpi-value   { font-size: 1.1rem; font-weight: 700; }

.chevron { font-size: .85rem; color: #94a3b8; margin-left: auto; }

/* 색상 */
.text-ok    { color: #16a34a; }
.text-warn  { color: #d97706; }
.text-alert { color: #dc2626; }

/* 대공정 테이블 */
.pm-table-wrap { margin-top: 16px; border-top: 1px solid #f1f5f9; padding-top: 16px; overflow-x: auto; }
.pm-table { width: 100%; border-collapse: collapse; font-size: .88rem; }
.pm-table th { background: #f8fafc; padding: 8px 12px; text-align: left; color: #475569; font-weight: 600; }
.pm-table td { padding: 10px 12px; border-bottom: 1px solid #f8fafc; vertical-align: middle; }
.pm-table tr:last-child td { border-bottom: none; }
.pm-name { font-weight: 600; color: #1e293b; min-width: 140px; }

/* 바 차트 */
.bar-wrap { background: #f1f5f9; border-radius: 4px; height: 6px; width: 100px; margin-bottom: 4px; overflow: hidden; }
.bar      { height: 100%; border-radius: 4px; background: #22c55e; transition: width .4s; }
.bar.text-warn  { background: #f59e0b; }
.bar.text-alert { background: #ef4444; }
.yield-bar { background: #3b82f6; }
.yield-bar.text-warn  { background: #f59e0b; }
.yield-bar.text-alert { background: #ef4444; }

.count-badge { padding: 2px 8px; border-radius: 10px; font-size: .8rem; font-weight: 600; }
.alert-badge { background: #fee2e2; color: #dc2626; }
.warn-badge  { background: #fef3c7; color: #d97706; }
.count-ok    { color: #94a3b8; font-size: .85rem; }

.empty { text-align: center; color: #94a3b8; padding: 60px; }
</style>
