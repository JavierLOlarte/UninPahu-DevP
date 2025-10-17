<template>
  <div class="page">
    <div class="card">
      <div class="brand">
        <img src="/favicon.ico" alt="logo" class="logo" />
        <h1>VulnApp</h1>
      </div>

      <h2 class="title">Inicia sesión</h2>
      <p class="subtitle">Ejercicio educativo — login vulnerable (solo en entorno local)</p>

      <form @submit.prevent="onSubmit" novalidate class="form">
        <label class="field">
          <span class="label-text">Email</span>
          <input
            v-model="email"
            type="text"
            placeholder="tu@ejemplo.com"
            :class="{ invalid: emailTouched && !validEmail }"
            @blur="emailTouched = true"
            autocomplete="username"
          />
          <small v-if="emailTouched && !validEmail" class="error">Ingresa un email válido.</small>
        </label>

        <label class="field">
          <span class="label-text">Contraseña</span>
          <div class="password-row">
            <input
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="••••••••"
              @blur="passwordTouched = true"
              :class="{ invalid: passwordTouched && !password }"
              autocomplete="current-password"
            />
            <button type="button" class="peek" @click="showPassword = !showPassword" :aria-pressed="showPassword">
              {{ showPassword ? 'Ocultar' : 'Mostrar' }}
            </button>
          </div>
          <small v-if="passwordTouched && !password" class="error">La contraseña es requerida.</small>
        </label>

        <div class="actions">
          <label class="remember">
            <input type="checkbox" v-model="remember" />
            <span>Recordarme</span>
          </label>

          <button class="btn" :disabled="loading || !formValid">
            <span v-if="!loading">Ingresar</span>
            <span v-else class="spinner" aria-hidden="true"></span>
          </button>
        </div>


        <!-- Botón para ir al registro -->
        <div class="actions extra">
          <button
            type="button"
            class="btn btn-secondary"
            @click="goRegister"
          >
            ¿No tienes cuenta? Regístrate
          </button>
        </div>


        <p v-if="message" :class="['message', success ? 'ok' : 'fail']">{{ message }}</p>

        <div v-if="user" class="result">
          <h3>Usuario:</h3>
          <pre>{{ user }}</pre>
          <p><strong>Token (inseguro):</strong> <code>{{ token }}</code></p>
          <button class="btn-link" @click="clearSession">Cerrar sesión demo</button>
        </div>
      </form>

      <footer class="help">
        <small>Prueba: <strong>alice / password123</strong> — o usa un payload SQLi para demostrar la vulnerabilidad.</small>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

// estado
const email = ref('')
const password = ref('')
const remember = ref(false)
const showPassword = ref(false)
const loading = ref(false)
const message = ref('')
const success = ref(false)
const user = ref(null)
const token = ref(null)
const emailTouched = ref(false)
const passwordTouched = ref(false)

const router = useRouter()

// validaciones simples
const validEmail = computed(() => {
  return /\S+@\S+\.\S+/.test(email.value)
})
const formValid = computed(() => validEmail.value && password.value.length > 0)

const onSubmit = async () => {
  emailTouched.value = true
  passwordTouched.value = true
  message.value = ''
  success.value = false
  user.value = null
  token.value = null

  if (!formValid.value) {
    message.value = 'Completa los campos correctamente.'
    return
  }

  loading.value = true
  try {
    // => URL correcto del backend que ya tienes
    const resp = await axios.post('http://localhost:8080/api/auth/login', {
      email: email.value,
      password: password.value
    }, { headers: { 'Content-Type': 'application/json' } })

    // Esperamos { token: "...", userId: 1, email: "..." } como devolvías en el backend
    const data = resp.data

    // Construimos un objeto user mínimo para mostrar en UI
    user.value = {
      id: data.userId ?? null,
      email: data.email ?? email.value
    }

    // Guardamos el token real devuelto por el backend
    token.value = data.token
    if (token.value) {
      // opción: configurar axios por defecto para futuras peticiones
      axios.defaults.headers.common['Authorization'] = 'Bearer ' + token.value

      // guardado inseguro intencional para el laboratorio
      localStorage.setItem('vulnapp_token', token.value)
      localStorage.setItem('vulnapp_user', JSON.stringify(user.value))

      // Si el checkbox 'remember' está activo, lo dejamos persistente en storage (ya lo hacemos)
      // Si no, se queda igual (puedes decidir borrar al cerrar la pestaña si quieres)
      if (!remember.value) {
        // opcional: no hacer nada — ya guardamos token, esto es intencionalmente inseguro
      }
    }

    message.value = 'Login exitoso.'
    success.value = true

    // pequeña pausa para UX, luego redirigir a /accounts
// pequeña pausa para UX, luego redirigir
setTimeout(() => {
  // si hay query.redirect (intentaste entrar a ruta protegida), ir ahí; si no, a /accounts
  const redirectPath = router.currentRoute.value.query.redirect || '/accounts'
  router.push(redirectPath)
}, 400)


  } catch (err) {
    success.value = false
    if (err.response?.status === 401) message.value = 'Credenciales inválidas.'
    else message.value = 'Error en el servidor o CORS. Revisa consola.'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const clearSession = () => {
  user.value = null
  token.value = null
  message.value = ''

  // Eliminar token y usuario guardados
  localStorage.removeItem('vulnapp_token')
  localStorage.removeItem('vulnapp_user')

  // Limpiar cabecera
  delete axios.defaults.headers.common['Authorization']

  // Redirigir a /login
  router.push('/login')
}


const goRegister = () => {
  router.push('/register')
}
</script>


<style scoped>
/* Layout */
.page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(180deg,#0f172a 0%,#0b1220 100%);
  padding: 28px;
  color: #e6eef8;
  font-family: Inter, system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial;
}

.card {
  width: 100%;
  max-width: 460px;
  background: linear-gradient(180deg, rgba(255,255,255,0.04), rgba(255,255,255,0.02));
  border-radius: 12px;
  padding: 26px;
  box-shadow: 0 8px 30px rgba(2,6,23,0.7);
  border: 1px solid rgba(255,255,255,0.04);
}

/* Brand */
.brand {
  display:flex;
  align-items:center;
  gap:12px;
}
.logo { width:36px; height:36px; opacity:0.95; border-radius:6px; }
.brand h1 { margin:0; font-size:18px; letter-spacing:0.6px; font-weight:700; color:#fff; }

/* Titles */
.title { margin: 14px 0 4px; font-size:20px; color:#eaf3ff; }
.subtitle { margin: 0 0 18px; color:#a8bed6; font-size:13px; }

/* Form */
.form { display:flex; flex-direction:column; gap:12px; }
.field { display:flex; flex-direction:column; gap:6px; }
.label-text { font-size:13px; color:#cfe6ff; }
input[type="text"], input[type="password"] {
  padding:10px 12px;
  border-radius:8px;
  border: 1px solid rgba(255,255,255,0.06);
  background: rgba(255,255,255,0.02);
  color: #eaf3ff;
  outline: none;
  font-size:14px;
  transition: box-shadow .12s, border-color .12s;
}
input:focus { box-shadow: 0 4px 18px rgba(2,6,23,0.6); border-color: rgba(255,255,255,0.12); }
.invalid { border-color: #ff7b7b !important; }

/* password row and peek button */
.password-row { display:flex; gap:8px; align-items:center; }
.peek {
  border: none;
  background: transparent;
  color:#9fc9ff;
  font-size:13px;
  cursor:pointer;
  padding:6px;
  border-radius:8px;
}
.peek:focus { outline: 2px solid rgba(159,201,255,0.12); }

/* actions */
.actions { display:flex; align-items:center; justify-content:space-between; gap:12px; margin-top:8px; }
.remember { display:flex; align-items:center; gap:8px; color:#9fbfe0; font-size:13px; }
.btn {
  background: linear-gradient(90deg,#1e90ff,#3b82f6);
  color: white;
  padding:10px 16px;
  border-radius:10px;
  border: none;
  cursor:pointer;
  font-weight:600;
  box-shadow: 0 6px 18px rgba(59,130,246,0.18);
}
.btn:disabled { opacity:0.6; cursor:not-allowed; filter:grayscale(0.06); }

/* spinner */
.spinner {
  display:inline-block;
  width:18px; height:18px;
  border-radius:50%;
  border: 2px solid rgba(255,255,255,0.16);
  border-top-color: white;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* feedback */
.message { margin-top:12px; padding:10px; border-radius:8px; font-size:14px; }
.ok { background: rgba(34,197,94,0.12); color: #b8f5c9; border: 1px solid rgba(34,197,94,0.12); }
.fail { background: rgba(239,68,68,0.08); color:#ffc9c9; border: 1px solid rgba(239,68,68,0.06); }
.error { color:#ffb4b4; font-size:12px; }

/* result box */
.result { margin-top:12px; background: rgba(255,255,255,0.02); padding:12px; border-radius:8px; border:1px solid rgba(255,255,255,0.02); }
.result pre { white-space:pre-wrap; word-break:break-word; color:#dbefff; font-size:13px; }

/* footer help */
.help { margin-top:14px; text-align:center; color:#9fbfe0; font-size:12px; }

/* responsive */
@media (max-width:520px) {
  .card { padding:18px; border-radius:10px; }
  .title { font-size:18px; }
}
.btn-secondary {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.3);
  color: #e6eef8;
  padding: 10px 16px;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  width: 100%;
  text-align: center;
  margin-top: 8px;
}

.btn-secondary:hover {
  background: rgba(255,255,255,0.06);
}

</style>
