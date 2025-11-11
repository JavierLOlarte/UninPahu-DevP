<template>
  <div class="page-minimal-detail">
    <div class="card-detail">
      <header class="head-detail">
        <button class="btn-back" @click="goBack" aria-label="Volver a mis cuentas">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><path d="M12 19l-7-7 7-7"/></svg>
        </button>
        <h2 class="title-detail">Detalle de Cuenta</h2>
      </header>

      <div v-if="loading" class="loading-state">
        <div class="spinner-blue-detail"></div>
        <p>Cargando información de la cuenta ID: **{{ id }}**</p>
      </div>

      <div v-else-if="account">

        <!-- Tarjeta de Saldo Principal -->
        <div class="balance-card">
          <p class="balance-label">Saldo Disponible</p>
          <h3 class="balance-amount">{{ formatCurrency(account.monto) }}</h3>

          <div class="account-meta">
            <p>
              **No. Cuenta:** {{ account.numeroCuenta }}
              <span class="separator">|</span>
              **Tipo:** <span :class="['type-badge', account.tipo === 'AHORRO' ? 'type-saving' : 'type-checking']">
                {{ account.tipo }}
              </span>
            </p>
            <p class="owner-id">
              Owner ID (Usuario en sesión o manipulado): **{{ account.usuario?.id ?? '—' }}**
            </p>
          </div>
        </div>


        <!-- Panel de Administración: Actualizar Saldo -->
        <section class="admin-panel">
          <h3 class="panel-title">1. Actualizar Saldo (Vulnerable)</h3>
          <p class="panel-hint">Este campo simula una petición de PUT/balance. No comprueba la propiedad del ID. Útil para pruebas IDOR.</p>

          <div class="row-action">
            <input v-model.number="newMonto" type="number" step="0.01" placeholder="Nuevo saldo (ej. 500000.00)" class="input-action" />
            <button class="btn-primary-action" @click="updateBalance" :disabled="updating">
              <span v-if="updating" class="spinner-white-small-detail"></span>
              {{ updating ? 'Actualizando...' : 'Actualizar' }}
            </button>
          </div>

          <div v-if="updateMsg" :class="['message-status', updateOk ? 'ok-status' : 'fail-status']">{{ updateMsg }}</div>
        </section>

        <!-- Panel de Administración: Eliminar Cuenta -->
        <section class="admin-panel">
          <h3 class="panel-title">2. Eliminar Cuenta</h3>
          <p class="panel-hint">La eliminación de la cuenta tampoco realiza verificación de propiedad en la versión vulnerable.</p>

          <div class="row-action delete-row">
            <button class="btn-danger-action" @click="deleteAccount" :disabled="deleting">
              <span v-if="deleting" class="spinner-white-small-detail"></span>
              {{ deleting ? 'Eliminando...' : 'Eliminar Cuenta Permanentemente' }}
            </button>
            <button class="btn-secondary-action" @click="goBack">Volver al Listado</button>
          </div>

          <div v-if="deleteMsg" :class="['message-status', deleteOk ? 'ok-status' : 'fail-status']">{{ deleteMsg }}</div>
        </section>

      </div>

      <div v-else class="empty-state">Cuenta ID **{{ id }}** no encontrada.</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const account = ref(null)
const loading = ref(false)

const newMonto = ref(0)
const updating = ref(false)
const updateMsg = ref('')
const updateOk = ref(false)

const deleting = ref(false)
const deleteMsg = ref('')
const deleteOk = ref(false)

// Función de formato de moneda (esencial)
const formatCurrency = (value) => {
    return Number(value).toLocaleString('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0, // Usar 0 para números redondos
      maximumFractionDigits: 2, // Pero permitir hasta 2 decimales si existen
    });
};

const fetchAccount = async () => {
  loading.value = true
  account.value = null
  updateMsg.value = ''
  deleteMsg.value = ''
  try {
    const res = await axios.get(`http://localhost:8080/api/accounts/${id}`)
    account.value = res.data
    newMonto.value = account.value?.monto ?? 0
  } catch (err) {
    const status = err?.response?.status
    if (status === 404) {
      updateMsg.value = 'Cuenta no encontrada (404). Redirigiendo al listado...'
      setTimeout(() => router.push('/accounts'), 1500)
    } else {
      updateMsg.value = 'Error al cargar la cuenta. Revisa backend / CORS.'
    }
  } finally {
    loading.value = false
  }
}

const updateBalance = async () => {
  updateMsg.value = ''
  updateOk.value = false
  updating.value = true
  if (isNaN(Number(newMonto.value))) {
     updateMsg.value = 'El nuevo saldo debe ser un número válido.'
     updateOk.value = false
     updating.value = false
     return
  }

  try {
    const res = await axios.put(`http://localhost:8080/api/accounts/${id}/balance`, { newBalance: Number(newMonto.value) })
    updateMsg.value = 'Saldo actualizado correctamente.'
    updateOk.value = true
    account.value = res.data
  } catch (err) {
    console.error('updateBalance error', err)
    const status = err?.response?.status
    if (status === 404) updateMsg.value = 'Cuenta no encontrada al actualizar.'
    else if (status === 400) updateMsg.value = err?.response?.data?.message ?? 'Petición inválida.'
    else updateMsg.value = 'Error al actualizar saldo. Revisa backend.'
    updateOk.value = false
  } finally {
    updating.value = false
  }
}

const deleteAccount = async () => {
  deleteMsg.value = ''
  deleteOk.value = false
  deleting.value = true
  try {
    await axios.delete(`http://localhost:8080/api/accounts/${id}`)
    deleteMsg.value = '¡Éxito! Cuenta eliminada y redirigiendo al listado.'
    deleteOk.value = true
    setTimeout(() => router.push('/accounts'), 1500)
  } catch (err) {
    console.error('deleteAccount error', err)
    const status = err?.response?.status
    if (status === 404) deleteMsg.value = 'Cuenta no encontrada al intentar eliminar.'
    else deleteMsg.value = 'Error al eliminar la cuenta. Revisa backend.'
    deleteOk.value = false
  } finally {
    deleting.value = false
  }
}


const goBack = () => router.push('/accounts')

onMounted(fetchAccount)
</script>

<style scoped>
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

.page-minimal-detail {
    background-color: var(--bg-light);
    min-height: 100vh;
    display: flex;
    justify-content: center;
    padding: 40px 16px;
    font-family: 'Inter', system-ui, -apple-system, "Segoe UI", Roboto, Arial, sans-serif;
    color: var(--text-dark);
}

.card-detail {
    width: 100%;
    max-width: 650px;
    background: #ffffff;
    padding: 30px;
    border-radius: 18px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
    border: 1px solid var(--border-light);
}

/* --- HEADER Y BOTÓN DE REGRESO --- */
.head-detail {
    display: flex;
    align-items: center;
    margin-bottom: 25px;
}
.btn-back {
    background: none;
    border: none;
    color: var(--text-subtle);
    padding: 8px;
    cursor: pointer;
    transition: color 0.2s ease;
    margin-right: 15px;
}
.btn-back:hover {
    color: var(--primary-blue);
}
.title-detail {
    font-size: 1.8rem;
    font-weight: 700;
    color: var(--text-dark);
    margin: 0;
}
.loading-state, .empty-state {
    text-align: center;
    padding: 30px;
    color: var(--text-subtle);
    font-style: italic;
    background-color: #f3f4f6;
    border-radius: 12px;
    border: 1px solid var(--border-light);
}
.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
}

/* --- TARJETA DE SALDO PRINCIPAL --- */
.balance-card {
    background: linear-gradient(45deg, #e0f2fe 0%, #ffffff 100%);
    padding: 25px;
    border-radius: 18px;
    border: 1px solid #bfdbfe;
    box-shadow: 0 5px 15px rgba(0, 122, 255, 0.1);
    margin-bottom: 30px;
}
.balance-label {
    font-size: 0.95rem;
    color: var(--text-subtle);
    font-weight: 500;
    margin-bottom: 5px;
}
.balance-amount {
    font-size: 3rem;
    font-weight: 900;
    color: var(--primary-dark);
    letter-spacing: -0.05em;
    margin-top: 0;
    margin-bottom: 15px;
}
.account-meta {
    font-size: 0.9rem;
    color: var(--text-dark);
    line-height: 1.6;
    border-top: 1px dashed #bfdbfe;
    padding-top: 10px;
}
.account-meta p { margin: 0; }
.separator { color: #bfdbfe; margin: 0 8px; }
.type-badge {
    font-size: 0.8rem;
    font-weight: 700;
    padding: 3px 8px;
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

/* --- PANELES DE ADMINISTRACIÓN --- */
.admin-panel {
    margin-top: 25px;
    padding: 20px;
    border-radius: 12px;
    background-color: #f8f8f8; /* Fondo más neutro para secciones de administración */
    border: 1px solid var(--border-light);
}
.panel-title {
    font-size: 1.3rem;
    font-weight: 600;
    margin-top: 0;
    color: var(--text-dark);
}
.panel-hint {
    font-size: 0.85rem;
    color: #9ca3af;
    margin-bottom: 15px;
}

.row-action {
    display: flex;
    gap: 10px;
    align-items: center;
    margin-top: 10px;
}
.input-action {
    flex-grow: 1;
    padding: 12px 14px;
    border-radius: 10px;
    border: 1px solid var(--border-light);
    font-size: 1rem;
    color: var(--text-dark);
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.input-action:focus {
    border-color: var(--primary-blue);
    box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.15);
    outline: none;
}
.btn-primary-action, .btn-secondary-action, .btn-danger-action {
    padding: 12px 18px;
    border-radius: 10px;
    border: none;
    cursor: pointer;
    font-weight: 700;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    justify-content: center;
}
.btn-primary-action {
    background-color: var(--primary-blue);
    color: white;
}
.btn-primary-action:hover {
    background-color: var(--primary-dark);
}
.btn-danger-action {
    background-color: #ef4444; /* Rojo para eliminar */
    color: white;
    flex-grow: 1;
}
.btn-danger-action:hover {
    background-color: #b91c1c;
}
.btn-secondary-action {
    background: none;
    border: 1px solid var(--border-light);
    color: var(--text-dark);
    font-weight: 600;
}
.btn-secondary-action:hover {
    background-color: #e5e7eb;
}
.delete-row { margin-top: 15px; }

/* --- MENSAJES DE ESTADO --- */
.message-status {
    margin-top: 15px;
    padding: 12px 16px;
    border-radius: 10px;
    font-weight: 600;
    font-size: 0.9rem;
}
.ok-status {
    background: var(--success-bg);
    color: var(--success-text);
    border: 1px solid #a7f3d0;
}
.fail-status {
    background: var(--error-bg);
    color: var(--error-text);
    border: 1px solid #fca5a5;
}

/* --- SPINNER --- */
.spinner-blue-detail, .spinner-white-small-detail {
  border: 4px solid rgba(0, 122, 255, 0.3);
  border-top: 4px solid var(--primary-blue);
  border-radius: 50%;
  width: 25px;
  height: 25px;
  animation: spin 1s linear infinite;
}
.spinner-white-small-detail {
    border: 3px solid rgba(255, 255, 255, 0.3);
    border-top: 3px solid #fff;
    width: 16px;
    height: 16px;
    margin-right: 8px;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 600px) {
    .row-action {
        flex-direction: column;
        align-items: stretch;
    }
    .delete-row {
        flex-direction: column-reverse;
    }
    .btn-secondary-action { order: 1; }
    .btn-danger-action { order: 2; }
    .page-minimal-detail { padding: 20px 10px; }
}
</style>
