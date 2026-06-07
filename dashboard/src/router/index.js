import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import SpecManagementView from '../views/SpecManagementView.vue'
import HierarchyView from '../views/HierarchyView.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/',         component: DashboardView,      name: 'dashboard' },
    { path: '/specs',    component: SpecManagementView, name: 'specs' },
    { path: '/hierarchy',component: HierarchyView,      name: 'hierarchy' },
  ]
})
