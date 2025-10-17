// src/main.js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'

function hasValidToken(t) {
  return t && t !== 'null' && t !== 'undefined' && t.trim() !== ''
}

const savedToken = localStorage.getItem('vulnapp_token')
if (hasValidToken(savedToken)) {
  axios.defaults.headers.common['Authorization'] = 'Bearer ' + savedToken
  console.log('✅ Token restaurado automáticamente:', savedToken)
} else {
  localStorage.removeItem('vulnapp_token')
  localStorage.removeItem('vulnapp_user')
  delete axios.defaults.headers.common['Authorization']
}

// ✅ Interceptor
axios.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('vulnapp_token')
      localStorage.removeItem('vulnapp_user')
      delete axios.defaults.headers.common['Authorization']
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// Opcional: validar token con backend al inicio (async)
// Si lo activas, haz que la app espere a la respuesta antes de montar.
// Ejemplo rápido (no obligatorio):
/*
if (hasValidToken(savedToken)) {
  axios.get('http://localhost:8080/api/auth/validate')
    .then(() => {
      createApp(App).use(router).mount('#app')
    })
    .catch(() => {
      // token inválido: limpiar y montar app (redirección al login por guard)
      localStorage.removeItem('vulnapp_token')
      localStorage.removeItem('vulnapp_user')
      delete axios.defaults.headers.common['Authorization']
      createApp(App).use(router).mount('#app')
    })
} else {
  createApp(App).use(router).mount('#app')
}
*/

// montaje normal (si no usas la validación opcional)
createApp(App).use(router).mount('#app')
