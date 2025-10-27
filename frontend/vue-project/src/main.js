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

axios.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      // Solo limpiar el localStorage, NO recargar la página
      localStorage.removeItem('vulnapp_token')
      localStorage.removeItem('vulnapp_user')
      delete axios.defaults.headers.common['Authorization']

      // En lugar de recargar, puedes redirigir usando el router si es necesario
      // pero para el login, dejamos que el componente maneje el error
      console.log('🔐 Sesión expirada o credenciales inválidas')
    }
    return Promise.reject(err)
  }
)

createApp(App).use(router).mount('#app')
