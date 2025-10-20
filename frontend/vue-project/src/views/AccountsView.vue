<template>
  <div class="accounts-page">
    <!-- Header -->
    <header class="header">
      <h1>💼 Mis cuentas</h1>
      <div class="actions">
        <button class="btn-primary" @click="goCreate">+ Crear cuenta</button>
<button class="btn-secondary" @click="logout">
  🔒 Cerrar sesión
</button>
      </div>
    </header>

    <!-- No sesión activa -->
    <section v-if="!user" class="no-session">
      <p>⚠️ No hay sesión activa. Por favor, inicia sesión.</p>
      <button class="btn-primary" @click="goLogin">Ir a login</button>
    </section>

    <!-- Cuentas del usuario -->
    <section v-else>
      <p class="hint">Sesión: <strong>{{ user.email }}</strong> — ID: <strong>{{ user.id }}</strong></p>

      <div class="panel">
        <h3>Listado de cuentas</h3>
        <div v-if="loading" class="loading">Cargando...</div>

        <table v-else class="table">
         <thead>
           <tr>
             <th>Referencia</th>
             <th>Número</th>
             <th>Tipo</th>
             <th>Saldo</th>
             <th>Acción</th>
           </tr>
         </thead>
         <tbody>
           <tr v-for="acct in accounts" :key="acct.id">
             <td>ACC-{{ acct.id.toString().padStart(4,'0') }}</td>
             <td>{{ acct.numeroCuenta }}</td>
             <td>{{ acct.tipo }}</td>
             <td>
               <div style="display:flex; gap:8px; align-items:center;">
                 <span v-if="!acct.editing">{{ acct.monto }}</span>

                 <!-- Inline input para editar balance -->
                 <input v-if="acct.editing"
                        v-model.number="acct.newBalance"
                        type="number"
                        step="0.01"
                        placeholder="Nuevo saldo"
                        style="width:120px; padding:6px; border-radius:6px; border:1px solid #cbd5e1;" />

                 <!-- Small spinner / status -->
                 <span v-if="acct.updating" class="small">Actualizando...</span>
               </div>
             </td>

             <td>
               <div style="display:flex; gap:6px; align-items:center;">
                 <button class="btn-secondary" @click="goDetail(acct.id)">Detalle</button>

                 <!-- Toggle edit -->
                 <button class="btn-secondary" v-if="!acct.editing" @click="startEdit(acct)">Editar saldo</button>

                 <!-- Confirmar/Cancelar -->
                 <button class="btn-primary" v-if="acct.editing" @click="updateBalance(acct)">Guardar</button>
                 <button class="btn-secondary" v-if="acct.editing" @click="cancelEdit(acct)">Cancelar</button>
               </div>
             </td>
           </tr>
         </tbody>
        </table>

        <div v-if="accounts.length === 0 && !loading" class="empty">
          No hay cuentas registradas.
        </div>
      </div>

      <!-- PoC IDOR -->
      <div class="panel idor">
        <h3>🔍 Prueba IDOR (PoC)</h3>
        <p class="small">Introduce cualquier ID de cuenta para ver si puedes acceder a cuentas ajenas (vulnerable intencionalmente).</p>
        <div class="idor-row">
          <input v-model="probeId" placeholder="ej. 2" />
          <button class="btn-primary" @click="probeAccount">Consultar</button>
        </div>

        <div v-if="probeResult" class="probe-result">
          <h4>Resultado ID {{ probeResult.id }}</h4>
          <pre>{{ probeResult }}</pre>
          <button class="btn-secondary" @click="clearProbe">Limpiar</button>
        </div>

        <div v-if="probeError" class="error">{{ probeError }}</div>
      </div>

      <!-- Mensajes globales -->
      <div v-if="globalMessage" :class="['panel', globalMessageType === 'error' ? 'error-panel' : 'success-panel']" style="margin-top:12px;">
        <div v-if="globalMessageType === 'error'" class="error">{{ globalMessage }}</div>
        <div v-else>{{ globalMessage }}</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const user = ref(null)
const accounts = ref([])
const loading = ref(false)
const probeId = ref('')
const probeResult = ref(null)
const probeError = ref('')
const globalMessage = ref('')
const globalMessageType = ref('') // 'error' | 'success'

// Carga sesión desde localStorage (vulnapp_user)
const loadSession = () => {
  const raw = localStorage.getItem('vulnapp_user')
  if (raw) {
    const u = JSON.parse(raw)
    // mapear username → nombre
    if (!u.nombre && u.username) u.nombre = u.username
    user.value = u
  } else {
    user.value = null
  }
}
const logout = () => {
  localStorage.removeItem('vulnapp_token')
  localStorage.removeItem('vulnapp_user')
  delete axios.defaults.headers.common['Authorization']
  router.push('/login')
}

// Obtener cuentas. Si hay user.id, pasar ownerId para listar solo su cuentas (pero backend en fase vulnerable puede ignorar)
const fetchAccounts = async () => {
  loading.value = true
  accounts.value = []
  probeResult.value = null
  probeError.value = ''
  globalMessage.value = ''
  try {
    let res
    if (user.value && user.value.id) {
      res = await axios.get('http://localhost:8080/api/accounts', {
        params: { ownerId: user.value.id }
      })
    } else {
      res = await axios.get('http://localhost:8080/api/accounts')
    }
    // Normalizar accounts: añadir campos temporales para edición
    accounts.value = (res.data || []).map(a => ({
      ...a,
      editing: false,
      updating: false,
      newBalance: null
    }))
  } catch (err) {
    console.error(err)
    probeError.value = 'Error al cargar cuentas. Revisa backend / CORS.'
  } finally {
    loading.value = false
  }
}

const goCreate = () => router.push('/accounts/create')
const goDetail = (id) => router.push(`/accounts/${id}`)
const goLogin = () => router.push('/login')

// PoC IDOR
const probeAccount = async () => {
  probeResult.value = null
  probeError.value = ''
  if (!probeId.value) { probeError.value = 'Ingresa un id de cuenta'; return }
  try {
    const res = await axios.get(`http://localhost:8080/api/accounts/${probeId.value}`)
    probeResult.value = res.data
  } catch (err) {
    console.error(err)
    probeError.value = err?.response?.data?.message ?? 'No se encontró la cuenta.'
  }
}
const clearProbe = () => { probeId.value = ''; probeResult.value = null; probeError.value = '' }

// Edición inline
const startEdit = (acct) => {
  acct.editing = true
  acct.newBalance = acct.monto // precargar valor actual
  globalMessage.value = ''
}

const cancelEdit = (acct) => {
  acct.editing = false
  acct.newBalance = null
  acct.updating = false
  globalMessage.value = ''
}

/**
 * updateBalance: llama PUT /api/accounts/{id}/balance con body { newBalance: ... }
 * Actualiza acct.monto con lo devuelto por el backend (res.data.monto)
 * Mantiene la vulnerabilidad educativa: no se comprueba ownership desde el frontend.
 */
const updateBalance = async (acct) => {
  // validaciones mínimas en frontend (solo UX)
  if (acct.newBalance === null || acct.newBalance === undefined || isNaN(acct.newBalance)) {
    globalMessageType.value = 'error'
    globalMessage.value = 'Ingresa un valor numérico válido para el nuevo saldo.'
    return
  }

  acct.updating = true
  globalMessage.value = ''
  try {
    const res = await axios.put(`http://localhost:8080/api/accounts/${acct.id}/balance`, {
      newBalance: acct.newBalance
    }, {
      headers: { 'Content-Type': 'application/json' }
    })

    // Si el backend devuelve la entidad, actualizamos la UI
    const returned = res.data
    if (returned && returned.monto !== undefined) {
      acct.monto = returned.monto
    } else if (returned && returned.balance !== undefined) {
      // por si tu entidad usa "balance" como nombre de campo
      acct.monto = returned.balance
    } else {
      // fallback: usar valor enviado
      acct.monto = acct.newBalance
    }

    acct.editing = false
    acct.newBalance = null
    globalMessageType.value = 'success'
    globalMessage.value = `Saldo actualizado correctamente (ID ${acct.id}).`
  } catch (err) {
    console.error(err)
    // intentar leer mensaje del GlobalExceptionHandler del backend
    const msg = err?.response?.data?.message ?? err?.message ?? 'Error actualizando saldo'
    globalMessageType.value = 'error'
    globalMessage.value = msg
  } finally {
    acct.updating = false
    // limpiamos mensaje después de unos segundos para no saturar la UI (opcional)
    setTimeout(() => { if (globalMessageType.value === 'success') globalMessage.value = '' }, 4000)
  }
}

onMounted(() => { loadSession(); fetchAccounts() })
</script>

<style scoped>
.accounts-page { max-width: 980px; margin: 28px auto; padding: 18px; font-family: Inter, system-ui, -apple-system, "Segoe UI", Roboto, Arial; color: #0b1220; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; }
.header h1 { font-size:1.8rem; }
.actions { display:flex; gap:12px; }
.btn-primary { background:#2563eb; color:white; padding:8px 16px; border-radius:8px; border:none; cursor:pointer; font-weight:600; }
.btn-secondary { background:transparent; border:1px solid #2563eb; color:#2563eb; padding:6px 12px; border-radius:6px; cursor:pointer; }
.btn-primary:hover { background:#1e40af; }
.btn-secondary:hover { background:#1e40af; color:white; }
.no-session { text-align:center; padding:24px; background:#fef3c7; border-radius:10px; margin-bottom:16px; }
.panel { background:#f8fafc; padding:16px; border-radius:10px; margin-bottom:16px; box-shadow:0 4px 12px rgba(0,0,0,0.05); }
.table { width:100%; border-collapse: collapse; }
.table th, .table td { padding:10px; text-align:left; border-bottom:1px solid #e2e8f0; }
.table tbody tr:hover { background:#e0f2fe; }
.hint { margin-bottom:12px; font-size:14px; color:#475569; }
.idor .idor-row { display:flex; gap:8px; align-items:center; margin-top:8px; }
.idor input { padding:8px; border-radius:6px; border:1px solid #cbd5e1; }
.probe-result pre { background:#0f172a; color:#eaf3ff; padding:8px; border-radius:6px; overflow:auto; }
.error { color:#b91c1c; margin-top:8px; }
.empty { color:#64748b; padding:12px 0; font-style:italic; }
.small { font-size:0.85rem; color:#64748b; }
.success-panel { background:#ecfdf5; border-left:4px solid #10b981; padding:12px; }
.error-panel { background:#fff1f2; border-left:4px solid #ef4444; padding:12px; }
</style>
