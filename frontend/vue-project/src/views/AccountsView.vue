<template>
  <div class="accounts-page-modern">

    <div class="assistant-container">
      <div
        class="assistant-icon"
        @click="toggleAssistantList"
        :class="{ 'active': showVulnerabilityList }">
        <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="animate-float">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5M2 12l10 5 10-5"/>
        </svg>
      </div>

      <div class="assistant-bubble" :class="{ 'expanded': showVulnerabilityList }">
        <div v-if="!showVulnerabilityList" class="initial-message">
            ¡Bienvenido! Esta es la vista más importante. **Haz clic en mí** para ver qué fallos de seguridad críticos puedes encontrar aquí.
        </div>

        <div v-if="showVulnerabilityList" class="vulnerability-list-inner">
            <h3 class="list-title">🎯 Fallos IDOR (Insecure Direct Object Reference)</h3>

            <div class="vulnerability-item" @click.stop="vulnerabilidades[0].isOpen = !vulnerabilidades[0].isOpen">
                <div class="vulnerability-header">
                    <span class="vulnerability-name">1. IDOR: Ver Cuentas de Otros (GET)</span>
                    <span class="vulnerability-icon">{{ vulnerabilidades[0].isOpen ? '▲' : '▼' }}</span>
                </div>
                <div v-if="vulnerabilidades[0].isOpen" class="vulnerability-details">
                    <p class="simple-description">Utiliza el **Panel de Desarrollo (IDOR PoC)** de abajo. Si pones el ID de una cuenta que no es tuya, y el sistema te la muestra, ¡has encontrado el fallo!</p>
                </div>
            </div>

            <div class="vulnerability-item" @click.stop="vulnerabilidades[1].isOpen = !vulnerabilidades[1].isOpen">
                <div class="vulnerability-header">
                    <span class="vulnerability-name">2. IDOR: Modificar Saldo Ajeno (PUT)</span>
                    <span class="vulnerabilidades-icon">{{ vulnerabilidades[1].isOpen ? '▲' : '▼' }}</span>
                </div>
                <div v-if="vulnerabilidades[1].isOpen" class="vulnerability-details">
                    <p class="simple-description">La función 'Editar Saldo' funciona solo en tus cuentas desde el *front-end*. Para explotar el *back-end*, utiliza **Postman** (o DevTools) para interceptar la URL de actualización (`/api/accounts/{id}/balance`), cambia el `{id}` por el de otra cuenta, y verifica si logras modificar el saldo de un tercero.</p>
                </div>
            </div>

            <div class="vulnerability-item" @click.stop="vulnerabilidades[4].isOpen = !vulnerabilidades[4].isOpen">
                <div class="vulnerability-header">
                    <span class="vulnerability-name">3. IDOR: Eliminar Cuenta Ajena (DELETE)</span>
                    <span class="vulnerability-icon">{{ vulnerabilidades[4].isOpen ? '▲' : '▼' }}</span>
                </div>
                <div v-if="vulnerabilidades[4].isOpen" class="vulnerability-details">
                    <p class="simple-description">Desde el *front-end* solo puedes eliminar tus cuentas. El reto es replicar este fallo en el *back-end*: utiliza una herramienta externa (**Postman**) para enviar una petición **DELETE** a la URL de eliminación (`/api/accounts/{id}`), cambiando el `{id}` por el de un usuario distinto. ¡El control de acceso es débil!</p>
                </div>
            </div>

            <h3 class="list-title secondary-title">💡 Fallos de Sesión y Datos</h3>

            <div class="vulnerability-item" @click.stop="vulnerabilidades[2].isOpen = !vulnerabilidades[2].isOpen">
                <div class="vulnerability-header">
                    <span class="vulnerability-name">4. Contraseñas y Tokens sin Cifrar</span>
                    <span class="vulnerability-icon">{{ vulnerabilidades[2].isOpen ? '▲' : '▼' }}</span>
                </div>
                <div v-if="vulnerabilidades[2].isOpen" class="vulnerability-details">
                    <p class="simple-description">Este es un fallo doble:
                        <br/>
                        **Contraseñas:** Confirma en la **base de datos** si las contraseñas están almacenadas como texto simple (plaintext).
                        <br/>
                        **Token:** Revisa el **Token de Sesión** almacenado en el Local Storage. Si su contenido es legible o no está adecuadamente cifrado, es una debilidad grave en el manejo de datos sensibles.
                    </p>
                </div>
            </div>

            <div class="vulnerability-item" @click.stop="vulnerabilidades[3].isOpen = !vulnerabilidades[3].isOpen">
                <div class="vulnerability-header">
                    <span class="vulnerability-name">5. Token Reutilizable (Falta de Invalidación)</span>
                    <span class="vulnerabilidad-icon">{{ vulnerabilidades[3].isOpen ? '▲' : '▼' }}</span>
                </div>
                <div v-if="vulnerabilidades[3].isOpen" class="vulnerability-details">
                    <p class="simple-description">Tu Token de Acceso se guarda en el navegador (**Local Storage**). Cópialo, luego **cierra sesión**. Si utilizas ese mismo token copiado para acceder a un recurso restringido (por ejemplo, con Postman), y el acceso se mantiene, ¡el token no se invalidó al cerrar la sesión!</p>
                </div>
            </div>

        </div>
      </div>
    </div>
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
            <button class="btn-dev-secondary" @click="deleteOwnAccount">Eliminar Mi Cuenta (PoC)</button>
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
// --- LÓGICA DEL ASISTENTE ---
const showVulnerabilityList = ref(false)
const vulnerabilidades = ref([
    // IDOR - Lectura (GET)
    { id: 1, nombre: "IDOR: Lectura", isOpen: false },
    // IDOR - Modificación de Saldo (PUT)
    { id: 2, nombre: "IDOR: Escritura Saldo", isOpen: false },
    // Contraseñas y Token sin cifrar
    { id: 3, nombre: "Cifrado débil", isOpen: false },
    // Robo de Token (Invalidación)
    { id: 4, nombre: "Token Reutilizable", isOpen: false },
    // IDOR - Eliminar Cuenta (DELETE) <--- Nueva
    { id: 5, nombre: "IDOR: Eliminación Cuenta", isOpen: false },
])

const toggleAssistantList = () => {
  showVulnerabilityList.value = !showVulnerabilityList.value
}
// El resto de la lógica del asistente se mantiene igual.
// --- FIN LÓGICA DEL ASISTENTE ---


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
    const num = Number(value);
    if (isNaN(num)) return '$0';

    return num.toLocaleString('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
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

// Obtiene la lista de cuentas
const fetchAccounts = async () => {
  if (!user.value) return;

  loading.value = true
  accounts.value = []
  probeResult.value = null
  probeError.value = ''
  globalMessage.value = ''
  try {
    // Nota: El backend VULNERABLE permite filtrar por ownerId pero no lo fuerza
    const res = await axios.get('http://localhost:8080/api/accounts', {
        params: { ownerId: user.value.id }
    })

    accounts.value = (res.data || []).map(a => ({
      ...a,
      editing: false,
      updating: false,
      newBalance: a.monto || 0,
    }))
  } catch (err) {
    console.error(err)
    globalMessageType.value = 'error'
    globalMessage.value = 'Error al cargar cuentas. Revisa backend / CORS.'
  } finally {
    loading.value = false
  }
}

const goCreate = () => router.push('/accounts/create')
const goDetail = (id) => router.push(`/accounts/${id}`)
const goLogin = () => router.push('/login')

// PoC IDOR: Prueba de Insecure Direct Object Reference (VULNERABLE) - Lectura
const probeAccount = async () => {
  probeResult.value = null
  probeError.value = ''
  if (!probeId.value) { probeError.value = 'Ingresa un id de cuenta'; return }

  try {
    // VULNERABLE: No verifica si la cuenta ID es del usuario logueado
    const res = await axios.get(`http://localhost:8080/api/accounts/${probeId.value}`)
    probeResult.value = res.data
  } catch (err) {
    console.error(err)
    probeError.value = err?.response?.data?.message ?? 'No se encontró la cuenta.'
  }
}
const clearProbe = () => { probeId.value = ''; probeResult.value = null; probeError.value = '' }


// PoC IDOR: Prueba de Insecure Direct Object Reference (VULNERABLE) - Eliminación
const deleteOwnAccount = async () => {
    const defaultAccountId = accounts.value.length > 0 ? accounts.value[0].id : null;
    if (!defaultAccountId) {
        probeError.value = 'No tienes cuentas para eliminar (se usa la primera por defecto)';
        return;
    }

    if (!confirm(`¿Estás seguro de eliminar tu primera cuenta (ID ${defaultAccountId})? Este botón existe para dar la pista de la vulnerabilidad DELETE.`)) {
        return;
    }

    probeResult.value = null
    probeError.value = ''
    try {
        // VULNERABLE: El endpoint permite la eliminación, y si el usuario cambia el ID en Postman, eliminará la cuenta de otro.
        const res = await axios.delete(`http://localhost:8080/api/accounts/${defaultAccountId}`)

        globalMessageType.value = 'success'
        globalMessage.value = `Cuenta ${defaultAccountId} eliminada correctamente. ¡Prueba ahora con Postman y el ID de otra cuenta!`
        fetchAccounts();
    } catch (err) {
        console.error(err)
        globalMessageType.value = 'error'
        globalMessage.value = err?.response?.data?.message ?? 'Error eliminando cuenta (¿IDOR exitoso en otra cuenta?)'
    }
}


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

// Actualiza el saldo (VULNERABLE)
const updateBalance = async (acct) => {
  if (acct.newBalance === null || acct.newBalance === undefined || isNaN(acct.newBalance)) {
    globalMessageType.value = 'error'
    globalMessage.value = 'Ingresa un valor numérico válido para el nuevo saldo.'
    return
  }

  if (acct.newBalance < 0) {
    globalMessageType.value = 'error'
    globalMessage.value = 'El saldo no puede ser negativo.'
    return
  }

  acct.updating = true
  globalMessage.value = ''
  try {
    // VULNERABLE: No verifica la propiedad de la cuenta ID
    const res = await axios.put(`http://localhost:8080/api/accounts/${acct.id}/balance`, {
      newBalance: acct.newBalance
    }, {
      headers: { 'Content-Type': 'application/json' }
    })

    const returned = res.data
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

    /* PALETA ASISTENTE (Dark Mode Sobrio) */
    --color-primary: #3b82f6;
    --color-primary-dark: #2563eb;
    --color-accent: #fcd34d;
    --color-background-dark: #0f172a;
    --color-card-bg-dark: #1e293b;
    --color-text-light-dark: #f1f5f9;
    --color-text-subtle-dark: #94a3b8;
    --color-border-dark: #334155;
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
    background-color: var(--primary-blue) !important;
    color: white !important;
    padding: 8px 16px;
    border-radius: 10px;
    border: none;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.2s ease;
    box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3);
}

.btn-icon svg {
    stroke: white !important;
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

/* --- INFORMACIÓN DEL USUARIO --- */
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
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
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
    background-color: #d1fae5;
    color: #059669;
}
.type-checking {
    background-color: #bfdbfe;
    color: #1d4ed8;
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
    flex-wrap: wrap;
}

.edit-input {
    flex-grow: 1;
    padding: 10px;
    border-radius: 8px;
    border: 1px solid var(--border-light);
    font-size: 1rem;
    transition: border-color 0.2s ease;
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
    background-color: #1f2937;
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
    background-color: #059669;
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

/* --- ESTILOS DEL ASISTENTE --- */

@keyframes float {
    0% { transform: translateY(0px); }
    50% { transform: translateY(-10px); }
    100% { transform: translateY(0px); }
}

.assistant-container {
    position: fixed;
    top: 50px;
    right: 50px;
    display: flex;
    flex-direction: column-reverse;
    align-items: flex-end;
    max-width: 450px;
    z-index: 1000;
}

.assistant-bubble {
    background-color: var(--color-card-bg-dark);
    border: 1px solid var(--color-primary);
    color: var(--color-text-light-dark);
    padding: 15px;
    border-radius: 12px 0 12px 12px;
    margin-bottom: 5px;
    position: relative;
    font-size: 0.9rem;
    text-align: left;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.4);
    min-width: 280px;
    transition: all 0.3s ease;
    order: 1;
}

.assistant-bubble.expanded {
    min-width: 350px;
    padding: 20px;
    border-color: var(--color-accent);
}

/* Flecha del Asistente apuntando al icono */
.assistant-bubble::after {
    content: '';
    position: absolute;
    bottom: 100%;
    top: auto;
    right: 10px;
    width: 0;
    height: 0;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
    border-top: none;
    border-bottom: 10px solid var(--color-primary);
    transition: all 0.3s ease;
}
.assistant-bubble.expanded::after {
    border-bottom: 10px solid var(--color-accent);
}


.assistant-icon {
    order: 2;
    color: var(--color-primary);
    cursor: pointer;
    animation: float 3s ease-in-out infinite;
    transition: all 0.3s ease;
}

.assistant-icon:hover {
    color: var(--color-accent);
}

.assistant-icon.active {
    animation: none;
    color: var(--color-accent);
    transform: scale(1.1);
}

/* --- LISTA DENTRO DEL GLOBO --- */
.vulnerability-list-inner {
    max-height: 400px;
    overflow-y: auto;
    padding-right: 5px;
}

.list-title {
    font-size: 1rem;
    color: var(--color-accent);
    margin-bottom: 10px;
    padding-bottom: 5px;
    border-bottom: 1px dashed var(--color-border-dark);
}
.secondary-title {
    margin-top: 15px;
    color: var(--color-primary);
}

.vulnerability-item {
    cursor: pointer;
    padding: 8px 0;
    border-bottom: 1px dotted var(--color-border-dark);
    transition: background-color 0.2s;
}

.vulnerability-item:last-child {
    border-bottom: none;
}
.vulnerability-item:hover {
    background-color: rgba(59, 130, 246, 0.1);
    border-radius: 4px;
    padding: 8px;
    margin: 0 -8px;
}

.vulnerability-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    color: var(--color-text-light-dark);
}

.vulnerability-name {
    flex-grow: 1;
    color: var(--color-primary);
}

.vulnerability-icon {
    color: var(--color-accent);
    font-size: 0.7rem;
    margin-left: 10px;
}

.vulnerability-details {
    padding: 5px 0 0 10px;
    font-size: 0.8rem;
    color: var(--color-text-subtle-dark);
}

.simple-description {
    margin-top: 5px;
    color: var(--color-text-light-dark);
    font-weight: 400;
    line-height: 1.4;
}

/* Responsive para Móviles */
@media (max-width: 768px) {
    .assistant-container {
        position: static;
        align-items: flex-start;
        margin: 20px 0 0 0;
        right: auto;
        left: 16px;
        max-width: 90%;
        display: flex;
        flex-direction: column;
    }
    .assistant-bubble {
        min-width: 100%;
        margin-bottom: 10px;
        order: 2;
        border-radius: 12px;
    }
    .assistant-bubble.expanded {
        min-width: 100%;
    }
    .assistant-bubble::after {
        display: none;
    }
    .assistant-icon {
        order: 1;
        align-self: flex-start;
        margin-bottom: 5px;
    }
}
</style>
