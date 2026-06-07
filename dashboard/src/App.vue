<template>
  <div class="app">
    <header class="header">
      <div class="header-left">
        <span class="logo">🏭 Smart Factory</span>
        <nav class="nav">
          <RouterLink to="/"          class="nav-link" active-class="nav-link--active">대시보드</RouterLink>
          <RouterLink to="/hierarchy" class="nav-link" active-class="nav-link--active">공정 모니터링</RouterLink>
          <RouterLink to="/specs"     class="nav-link" active-class="nav-link--active">스펙 관리</RouterLink>
        </nav>
      </div>
      <div class="header-right">
        <span class="refresh-dot" :class="{ active: refreshing }"></span>
        <span>3초 자동 갱신</span>
      </div>
    </header>
    <RouterView :refreshing="refreshing" @refreshing="v => refreshing = v" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
const refreshing = ref(false)
</script>

<style>
*, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Segoe UI', sans-serif; background: #f1f5f9; color: #1e293b; }

.app { min-height: 100vh; }

.header {
  background: #0f172a;
  color: #fff;
  padding: 0 32px;
  height: 56px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-left  { display: flex; align-items: center; gap: 32px; }
.header-right { display: flex; align-items: center; gap: 8px; font-size: .85rem; color: #94a3b8; }

.logo { font-size: 1.1rem; font-weight: 700; }

.nav { display: flex; gap: 4px; }
.nav-link {
  color: #94a3b8;
  text-decoration: none;
  padding: 6px 14px;
  border-radius: 6px;
  font-size: .9rem;
  transition: background .15s, color .15s;
}
.nav-link:hover        { color: #fff; background: rgba(255,255,255,.08); }
.nav-link--active      { color: #fff; background: rgba(255,255,255,.15); font-weight: 600; }

.refresh-dot { width: 8px; height: 8px; border-radius: 50%; background: #475569; transition: background .3s; }
.refresh-dot.active { background: #22c55e; }

/* 공통 레이아웃 */
.page { max-width: 1280px; margin: 0 auto; padding: 24px 16px; display: flex; flex-direction: column; gap: 24px; }
.section-label { font-size: .8rem; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: .05em; margin-bottom: 12px; }
.card-box { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
</style>
