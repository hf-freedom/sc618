import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/create',
    name: 'CreateReimbursement',
    component: () => import('@/views/CreateReimbursement.vue')
  },
  {
    path: '/my',
    name: 'MyReimbursements',
    component: () => import('@/views/MyReimbursements.vue')
  },
  {
    path: '/approval',
    name: 'Approval',
    component: () => import('@/views/Approval.vue')
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('@/views/Payment.vue')
  },
  {
    path: '/ledger',
    name: 'Ledger',
    component: () => import('@/views/Ledger.vue')
  },
  {
    path: '/risk',
    name: 'Risk',
    component: () => import('@/views/Risk.vue')
  },
  {
    path: '/stats',
    name: 'Stats',
    component: () => import('@/views/Stats.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
