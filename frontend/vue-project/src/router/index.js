// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import WelcomeView from '../views/WelcomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterForm from '../views/RegisterForm.vue'
import axios from 'axios'

// rutas principales
const routes = [
  { path: '/', name: 'welcome', component: WelcomeView },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/register', name: 'register', component: RegisterForm },

  // rutas privadas
  { path: '/accounts', name: 'Accounts', component: () => import('@/views/AccountsView.vue'), meta: { requiresAuth: true } },
  { path: '/accounts/create', name: 'CreateAccount', component: () => import('@/views/CreateAccountView.vue'), meta: { requiresAuth: true } },
  { path: '/accounts/:id', name: 'AccountDetail', component: () => import('@/views/AccountDetailView.vue'), meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// util - token válido (mínimo)
function hasValidTokenInStorage() {
  const t = localStorage.getItem('vulnapp_token')
  return t && t !== 'null' && t !== 'undefined' && t.trim() !== ''
}

// Guard global: solo permite acceso si hay token *y* header de axios configurado
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('vulnapp_token')
  const hasAuthHeader = !!axios.defaults.headers?.common?.Authorization

  const publicPages = ['login', 'register', 'welcome']

  // Si intenta entrar a login y sí hay un token con header -> mandar a accounts
  if (publicPages.includes(to.name)) {
    if (token && hasAuthHeader) {
      return next({ path: '/accounts' })
    }
    return next()
  }

  // Protección de rutas privadas
  if (to.meta?.requiresAuth) {
    // Si no hay token o no hay header -> limpiar y mandar al login
    if (!token || !hasAuthHeader) {
      localStorage.removeItem('vulnapp_token')
      localStorage.removeItem('vulnapp_user')
      delete axios.defaults.headers.common['Authorization']
      return next({ name: 'login' })
    }
  }

  next()
})


export default router
