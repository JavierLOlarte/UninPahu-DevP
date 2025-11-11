<template>
  <div class="accounts-page-modern">
    <header class="header-modern">
      <h1 class="page-title">💼 Mis Cuentas</h1>
      <div class="actions-group">
        <button class="btn-icon" @click="goCreate" aria-label="Crear nueva cuenta">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14M5 12h14"/></svg>
          <span>Crear</span>
        </button>
        <button class="btn-logout" @click="logout" aria-label="Cerrar sesión">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
        </button>
      </div>
    </header>

    <section v-if="!user" class="no-session-card">
      <p>⚠️ No hay sesión activa. Por favor, inicia sesión.</p>
      <button class="btn-primary-card" @click="goLogin">Ir a login</button>
    </section>

    <section v-else>
      <div class="user-info-card">
          <div class="user-detail">
              <span class="detail-icon">👤</span>
              <span class="detail-label">Usuario Conectado</span>
              <strong class="detail-value">{{ user.email }}</strong>
          </div>
          <div class="user-detail">
              <span class="detail-icon">#️⃣</span>
              <span class="detail-label">ID de Sesión</span>
              <strong class="detail-value">{{ user.id }}</strong>
          </div>
      </div>
      <div v-if="globalMessage" :class="['global-message', globalMessageType === 'error' ? 'error-msg' : 'success-msg']">
        {{ globalMessage }}
      </div>

      <h2 class="section-title">Cuentas Registradas</h2>

      <div v-if="loading" class="loading-state">
        <div class="spinner-blue"></div>
        <p>Cargando cuentas...</p>
      </div>

      <div v-if="!loading" class="accounts-grid">
        <div v-for="acct in accounts" :key="acct.id" class="account-card">
          <div class="card-header">
            <span :class="['account-type', acct.tipo === 'AHORRO' ? 'type-saving' : 'type-checking']">
              {{ acct.tipo }}
            </span>
            <span class="account-ref">ACC-{{ acct.id.toString().padStart(4,'0') }}</span>
          </div>

          <div class="card-balance-section">
            <p class="balance-label">Saldo Disponible</p>

            <div v-if="acct.editing" class="edit-balance-group">
                <input v-model.number="acct.newBalance"
                        type="number"
                        step="0.01"
                        placeholder="Nuevo saldo"
                        class="edit-input" />
                <button class="btn-save" @click="updateBalance(acct)" :disabled="acct.updating">
                  <span v-if="acct.updating" class="spinner-white small-spinner"></span>
                  <span v-else>Guardar</span>
                </button>
                <button class="btn-cancel" @click="cancelEdit(acct)">Cancelar</button>
            </div>

            <p v-else class="balance-amount">{{ formatCurrency(acct.monto) }}</p>
          </div>

          <div class="card-actions">
            <button class="action-btn" @click="goDetail(acct.id)">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
              Detalle
            </button>

            <button class="action-btn" v-if="!acct.editing" @click="startEdit(acct)">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5l4 4L7.5 19.5l-4 4L12 12l4.5-4.5z"/></svg>
              Editar Saldo
            </button>
          </div>
        </div>

        <div v-if="accounts.length === 0 && !loading" class="empty-state">
          No hay cuentas registradas. ¡Crea una!
        </div>
      </div>


      <details class="dev-panel">
        <summary class="dev-summary">
          🔍 Panel de Pruebas de Desarrollo (IDOR PoC)
        </summary>
        <div class="dev-content">
          <p class="small">Introduce cualquier ID de cuenta para consultar su información directamente (Vulnerabilidad intencional).</p>
          <div class="idor-row">
            <input v-model="probeId" placeholder="ej. 2" class="idor-input" />
            <button class="btn-dev-action" @click="probeAccount">Consultar</button>
          </div>

          <div v-if="probeResult" class="probe-result">
            <h4>Resultado ID {{ probeResult.id }}</h4>
            <pre>{{ probeResult }}</pre>
            <button class="btn-dev-secondary" @click="clearProbe">Limpiar</button>
          </div>

          <div v-if="probeError" class="error-msg dev-error">{{ probeError }}</div>
        </div>
      </details>

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

// Función de formato de moneda (esencial para una app bancaria)
const formatCurrency = (value) => {
    // Usamos 'es-CO' para pesos colombianos (COP), pero con formato simple para mantenerlo limpio
    // Convertimos a string para asegurar que la función toLocaleString esté disponible
    const num = Number(value);
    if (isNaN(num)) return '$0';

    return num.toLocaleString('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0, // Quitamos decimales para simplificar
      maximumFractionDigits: 0,
    });
};

// Carga la información del usuario desde localStorage y configura el token en Axios
const loadSession = () => {
  const token = localStorage.getItem('vulnapp_token')
  const rawUser = localStorage.getItem('vulnapp_user')

  if (token && rawUser) {
    try {
        const u = JSON.parse(rawUser)
        user.value = u

        // Configurar el token para todas las peticiones
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
    } catch (e) {
        // Fallback si el JSON está corrupto
        console.error("Error parsing user data:", e)
        user.value = null
        localStorage.removeItem('vulnapp_token')
        localStorage.removeItem('vulnapp_user')
    }
  } else {
    user.value = null
    delete axios.defaults.headers.common['Authorization']
  }
}
const logout = () => {
  localStorage.removeItem('vulnapp_token')
  localStorage.removeItem('vulnapp_user')
  delete axios.defaults.headers.common['Authorization']
  router.push('/login')
}

// Obtiene la lista de cuentas (solo las del usuario logueado en un entorno real,
// pero aquí cargará las cuentas generales por cómo configuraremos el backend vulnerable)
const fetchAccounts = async () => {
  if (!user.value) return;

  loading.value = true
  accounts.value = []
  probeResult.value = null
  probeError.value = ''
  globalMessage.value = ''
  try {

    // NOTA: Esta API endpoint es VULNERABLE.
    // En un entorno seguro, esta ruta debería obtener las cuentas asociadas al 'user.id'
    // implícitamente a través del token de autenticación. Aquí, el backend simplemente
    // lista todas las cuentas o usa el ownerId para filtrar si existe.
    const res = await axios.get('http://localhost:8080/api/accounts', {
        // Pasamos el ownerId como parámetro. Si el backend es débil,
        // podría ignorar la verificación de autenticación.
        params: { ownerId: user.value.id }
    })

    accounts.value = (res.data || []).map(a => ({
      ...a,
      editing: false,
      updating: false,
      newBalance: a.monto || 0, // Inicializar newBalance con el monto actual
    }))
  } catch (err) {
    console.error(err)
    globalMessageType.value = 'error'
    globalMessage.value = 'Error al cargar cuentas. Revisa backend / CORS.'
  } finally {
    loading.value = false
  }
}

const goCreate = () => router.push('/accounts/create') // Pendiente de crear esta ruta
const goDetail = (id) => router.push(`/accounts/${id}`) // Pendiente de crear esta ruta
const goLogin = () => router.push('/login')

// PoC IDOR: Prueba de Insecure Direct Object Reference (VULNERABLE)
const probeAccount = async () => {
  probeResult.value = null
  probeError.value = ''
  if (!probeId.value) { probeError.value = 'Ingresa un id de cuenta'; return }

  // RUTA CLAVE VULNERABLE: Intenta obtener la cuenta por ID sin verificar
  // si la cuenta pertenece al usuario logueado.
  try {
    const res = await axios.get(`http://localhost:8080/api/accounts/${probeId.value}`)
    probeResult.value = res.data // Muestra la data de CUALQUIER cuenta.
  } catch (err) {
    console.error(err)
    probeError.value = err?.response?.data?.message ?? 'No se encontró la cuenta.'
  }
}
const clearProbe = () => { probeId.value = ''; probeResult.value = null; probeError.value = '' }

// Edición inline
const startEdit = (acct) => {
  acct.editing = true
  acct.newBalance = acct.monto
  globalMessage.value = ''
}

const cancelEdit = (acct) => {
  acct.editing = false
  acct.newBalance = null
  acct.updating = false
  globalMessage.value = ''
}

// Actualiza el saldo (VULNERABLE si el backend no verifica propiedad)
const updateBalance = async (acct) => {
  if (acct.newBalance === null || acct.newBalance === undefined || isNaN(acct.newBalance)) {
    globalMessageType.value = 'error'
    globalMessage.value = 'Ingresa un valor numérico válido para el nuevo saldo.'
    return
  }

  // El nuevo saldo no debe ser negativo
  if (acct.newBalance < 0) {
    globalMessageType.value = 'error'
    globalMessage.value = 'El saldo no puede ser negativo.'
    return
  }

  acct.updating = true
  globalMessage.value = ''
  try {
    // RUTA CLAVE VULNERABLE: PUT a un ID de cuenta. Si el backend no verifica
    // que acct.id pertenece a user.id, es una vulnerabilidad IDOR.
    const res = await axios.put(`http://localhost:8080/api/accounts/${acct.id}/balance`, {
      newBalance: acct.newBalance
    }, {
      headers: { 'Content-Type': 'application/json' }
    })

    const returned = res.data
    // Intenta actualizar el monto con el valor devuelto por el backend
    const updatedMonto = returned?.monto ?? returned?.balance ?? acct.newBalance;
    acct.monto = updatedMonto;

    acct.editing = false
    acct.newBalance = null
    globalMessageType.value = 'success'
    globalMessage.value = `Saldo actualizado correctamente (ID ${acct.id}). Saldo nuevo: ${formatCurrency(acct.monto)}`
  } catch (err) {
    console.error(err)
    const msg = err?.response?.data?.message ?? err?.message ?? 'Error actualizando saldo (¿IDOR exitoso en otra cuenta?)'
    globalMessageType.value = 'error'
    globalMessage.value = msg
  } finally {
    acct.updating = false
    // Limpiar mensaje de éxito después de 4 segundos
    setTimeout(() => {
        if (globalMessageType.value === 'success' && globalMessage.value) {
            globalMessage.value = '';
            globalMessageType.value = '';
        }
    }, 4000)
  }
}

onMounted(() => {
    loadSession();
    // Solo intentar cargar cuentas si hay un usuario. Si no, se muestra el botón de login.
    if (user.value) {
        fetchAccounts()
    }
})
</script>

<style>
/* --- PALETA Y BASE (Light Mode Profesional) --- */
:root {
    --primary-blue: #007AFF;
    --primary-dark: #004d99;
    --text-dark: #1f2937;
    --text-subtle: #6b7280;
    --bg-light: #f9fafb;
    --border-light: #e5e7eb;
    --success-bg: #ecfdf5;
    --success-text: #059669;
    --error-bg: #fef2f2;
    --error-text: #ef4444;
}

.accounts-page-modern {
    background-color: var(--bg-light);
    min-height: 100vh;
    padding: 20px 0;
    font-family: 'Inter', system-ui, -apple-system, "Segoe UI", Roboto, Arial, sans-serif;
    color: var(--text-dark);
}

.accounts-page-modern > section {
    max-width: 800px;
    margin: 0 auto;
    padding: 0 16px;
}

/* --- NO SESSION CARD --- */
.no-session-card {
    padding: 30px;
    text-align: center;
    background-color: #ffffff;
    border-radius: 15px;
    box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
    margin: 50px auto;
    max-width: 400px;
}
.no-session-card p {
    font-size: 1.1rem;
    color: var(--text-dark);
    margin-bottom: 20px;
}
.btn-primary-card {
    background-color: var(--primary-blue);
    color: white;
    padding: 10px 20px;
    border-radius: 10px;
    border: none;
    cursor: pointer;
    font-weight: 600;
    transition: background-color 0.2s;
}
.btn-primary-card:hover {
    background-color: var(--primary-dark);
}

/* --- HEADER Y NAVEGACIÓN --- */
.header-modern {
    display: flex;
    justify-content: space-between;
    align-items: center;
    max-width: 800px;
    margin: 0 auto 30px auto;
    padding: 0 16px;
}
.page-title {
    font-size: 2rem;
    font-weight: 700;
    color: var(--text-dark);
}
.actions-group {
    display: flex;
    gap: 12px;
}
.btn-icon {
    display: flex;
    align-items: center;
    gap: 6px;

    /* 🔴 CORRECCIÓN CLAVE: Asegura el fondo azul para el botón */
    background-color: var(--primary-blue) !important;

    color: white !important; /* Mantenemos este para asegurar el texto */
    padding: 8px 16px;
    border-radius: 10px;
    border: none;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.2s ease;
    box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3);
}

.btn-icon svg {
    stroke: white !important; /* Mantenemos este para asegurar el ícono */
}

.btn-icon:hover {
    background-color: var(--primary-dark);
    box-shadow: 0 6px 15px rgba(0, 122, 255, 0.4);
    transform: translateY(-1px);
}
.btn-logout {
    background: none;
    border: 1px solid var(--border-light);
    color: var(--text-subtle);
    padding: 8px;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s ease;
}
.btn-logout:hover {
    color: var(--primary-blue);
    border-color: var(--primary-blue);
}

/* --- INFORMACIÓN DEL USUARIO (NUEVOS ESTILOS) --- */
.user-info-card {
    background-color: #ffffff;
    border: 1px solid var(--border-light);
    border-radius: 12px;
    padding: 15px 20px;
    margin-bottom: 25px;
    display: flex;
    gap: 25px;
    align-items: center;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.user-detail {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
}

.detail-icon {
    font-size: 1.5rem;
    color: var(--primary-blue);
    margin-bottom: 4px;
}

.detail-label {
    font-size: 12px;
    color: var(--text-subtle);
    text-transform: uppercase;
    font-weight: 500;
}

.detail-value {
    font-size: 16px;
    color: var(--text-dark);
    font-weight: 700;
    word-break: break-all;
}

@media (max-width: 520px) {
    .user-info-card {
        flex-direction: column;
        align-items: stretch;
        gap: 15px;
    }
    .user-detail {
        flex-direction: row;
        align-items: center;
        gap: 10px;
    }
    .detail-label {
        flex-grow: 1;
        text-align: left;
    }
}
/* FIN: Nuevos Estilos de Información de Usuario */


/* --- SECCIÓN DE CUENTAS (LISTADO DE TARJETAS) --- */
.section-title {
    font-size: 1.5rem;
    font-weight: 600;
    margin-bottom: 20px;
}
.accounts-grid {
    display: grid;
    gap: 20px;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); /* Responsivo */
    margin-bottom: 30px;
}
.account-card {
    background: linear-gradient(135deg, #ffffff 0%, #fefefe 100%);
    padding: 25px;
    border-radius: 18px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
    border: 1px solid var(--border-light);
    transition: transform 0.3s ease;
}
.account-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 12px 25px rgba(0, 0, 0, 0.1);
}
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
}
.account-type {
    font-size: 0.8rem;
    font-weight: 700;
    padding: 4px 10px;
    border-radius: 9999px;
    text-transform: uppercase;
}
.type-saving {
    background-color: #d1fae5; /* Green light */
    color: #059669; /* Green dark */
}
.type-checking {
    background-color: #bfdbfe; /* Blue light */
    color: #1d4ed8; /* Blue dark */
}
.account-ref {
    font-size: 0.85rem;
    color: var(--text-subtle);
}

/* --- SALDO (EL FOCO PRINCIPAL) --- */
.card-balance-section {
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px dashed var(--border-light);
}
.balance-label {
    font-size: 0.9rem;
    color: var(--text-subtle);
    margin-bottom: 5px;
}
.balance-amount {
    font-size: 2.2rem;
    font-weight: 800;
    color: var(--text-dark);
    letter-spacing: -0.04em;
}

/* --- EDICIÓN INLINE --- */
.edit-balance-group {
    display: flex;
    gap: 8px;
    align-items: center;
    /* Aseguramos que los elementos no se salgan del contenedor */
    flex-wrap: wrap;
}

.edit-input {
    /* Permitimos que el input crezca más para ocupar espacio */
    flex-grow: 1;
    padding: 10px;
    border-radius: 8px;
    border: 1px solid var(--border-light);
    font-size: 1rem;
    transition: border-color 0.2s ease;
    /* Forzamos un ancho mínimo para que ocupe toda la fila si no hay espacio */
    min-width: 120px;
}
.edit-input:focus {
    border-color: var(--primary-blue);
    outline: none;
}
.btn-save, .btn-cancel {
    padding: 10px 14px;
    border-radius: 8px;
    font-weight: 600;
    border: none;
    cursor: pointer;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
}
.btn-save {
    background-color: var(--primary-blue);
    color: white;
}
.btn-save:hover {
    background-color: var(--primary-dark);
}
.btn-cancel {
    background-color: #f3f4f6;
    color: var(--text-dark);
}
.btn-cancel:hover {
    background-color: #e5e7eb;
}

/* --- ACCIONES DE LA TARJETA --- */
.card-actions {
    display: flex;
    gap: 10px;
    justify-content: space-between;
}
.action-btn {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 8px 12px;
    border-radius: 8px;
    background: none;
    border: 1px solid var(--border-light);
    color: var(--text-dark);
    font-size: 0.9rem;
    cursor: pointer;
    transition: all 0.2s ease;
    flex-grow: 1;
    justify-content: center;
}
.action-btn:hover {
    background-color: #f3f4f6;
    border-color: var(--primary-blue);
    color: var(--primary-blue);
}

/* --- MENSAJES Y ESTADOS --- */
.global-message {
    padding: 14px 20px;
    border-radius: 12px;
    margin-bottom: 25px;
    font-weight: 600;
    font-size: 0.95rem;
    animation: fadeIn 0.5s;
}
.success-msg {
    background-color: var(--success-bg);
    color: var(--success-text);
    border: 1px solid #a7f3d0;
}
.error-msg {
    background-color: var(--error-bg);
    color: var(--error-text);
    border: 1px solid #fca5a5;
}
.loading-state, .empty-state {
    text-align: center;
    padding: 40px;
    color: var(--text-subtle);
    font-style: italic;
    background-color: #ffffff;
    border-radius: 12px;
    border: 1px solid var(--border-light);
    margin-bottom: 30px;
}
.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
}

/* --- SPINNER --- */
.spinner-blue, .spinner-white {
  border: 4px solid rgba(0, 122, 255, 0.3);
  border-top: 4px solid var(--primary-blue);
  border-radius: 50%;
  width: 25px;
  height: 25px;
  animation: spin 1s linear infinite;
}
.spinner-white {
    border: 3px solid rgba(255, 255, 255, 0.3);
    border-top: 3px solid #fff;
    width: 18px;
    height: 18px;
}
.small-spinner { margin-right: 8px; }
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(-10px); }
    to { opacity: 1; transform: translateY(0); }
}

/* --- PANEL DE DESARROLLO (IDOR PoC) --- */
.dev-panel {
    margin-top: 40px;
    background-color: #1f2937; /* Dark background para diferenciar */
    border-radius: 12px;
    border: 1px solid #374151;
    color: #d1d5db;
}
.dev-summary {
    cursor: pointer;
    padding: 15px 20px;
    font-weight: 600;
    font-size: 1rem;
    border-bottom: 1px solid transparent;
    display: block;
}
.dev-panel[open] .dev-summary {
    border-bottom: 1px solid #374151;
}
.dev-content {
    padding: 20px;
}
.dev-content .small {
    font-size: 0.9rem;
    color: #9ca3af;
    margin-bottom: 10px;
}
.idor-row {
    display: flex;
    gap: 10px;
    margin-top: 10px;
}
.idor-input {
    flex-grow: 1;
    padding: 10px;
    border-radius: 8px;
    border: 1px solid #4b5563;
    background-color: #374151;
    color: #e5e7eb;
}
.btn-dev-action {
    background-color: #059669; /* Verde para acción de prueba */
    color: white;
    padding: 10px 16px;
    border-radius: 8px;
    border: none;
    cursor: pointer;
    font-weight: 600;
}
.btn-dev-secondary {
    background-color: #4b5563;
    color: white;
    padding: 8px 12px;
    border-radius: 8px;
    border: none;
    cursor: pointer;
    font-size: 0.9rem;
    margin-top: 10px;
}
.probe-result {
    margin-top: 15px;
    background-color: #111827;
    padding: 15px;
    border-radius: 8px;
    border: 1px solid #4b5563;
}
.probe-result h4 {
    color: #6ee7b7;
    margin-top: 0;
}
.probe-result pre {
    white-space: pre-wrap;
    word-break: break-all;
    font-size: 0.85rem;
    color: #e5e7eb;
    background: none;
    padding: 0;
}
.dev-error {
    background: #450a0a;
    color: #fecaca;
    border-left: 4px solid #ef4444;
    padding: 10px;
    border-radius: 8px;
    margin-top: 10px;
}
</style>
