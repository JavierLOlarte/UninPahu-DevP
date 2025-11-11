<template>
  <div class="page-minimal-create">
    <div class="card-create">
      <header class="head-create">
        <button class="btn-back" @click="goBack" aria-label="Volver a mis cuentas">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5"/><path d="M12 19l-7-7 7-7"/></svg>
        </button>
        <h2 class="title-create">Crear Nueva Cuenta</h2>
      </header>

      <div class="subtitle-area">
        <p class="muted-create">Define los parámetros para tu nueva cuenta bancaria.</p>
        <p class="note-create">⚠️ **Versión Educativa:** Puedes asignar el ID de cualquier usuario para pruebas de seguridad.</p>
      </div>

      <form @submit.prevent="onCreate" class="form-create">

        <!-- Campo Owner ID (Educativo/Vulnerable) -->
        <div class="field-group">
          <label for="ownerId" class="label-create">ID del Propietario (Owner ID)</label>
          <input id="ownerId" v-model="ownerId" placeholder="Ej. 1 (Usuario en sesión por defecto)" class="input-create" />
          <small class="hint-create">Asignado por defecto al usuario en sesión para simular un proceso real. Cambiarlo permite la prueba de IDOR.</small>
        </div>

        <!-- Número de cuenta -->
        <div class="field-group">
          <label for="numeroCuenta" class="label-create">Número de Cuenta</label>
          <input id="numeroCuenta" v-model="numeroCuenta" placeholder="0001234567" class="input-create" required/>
        </div>

        <!-- Tipo de cuenta -->
        <div class="field-group">
          <label for="tipo" class="label-create">Tipo de Cuenta</label>
          <select id="tipo" v-model="tipo" class="input-create select-create" required>
            <option value="AHORRO">AHORRO</option>
            <option value="CORRIENTE">CORRIENTE</option>
            <option value="E-WALLET">E-WALLET</option>
          </select>
        </div>

        <!-- Saldo inicial -->
        <div class="field-group">
          <label for="monto" class="label-create">Saldo Inicial (COP)</label>
          <input id="monto" v-model.number="monto" type="number" step="0.01" placeholder="Ej: 1000.00" class="input-create" required/>
        </div>

        <div class="actions-create">
          <button class="btn-primary-create" :disabled="loading">
            <span v-if="loading" class="spinner-white-small"></span>
            {{ loading ? 'Creando...' : 'Confirmar Creación' }}
          </button>
          <button type="button" class="btn-secondary-create" @click="goBack">Cancelar</button>
        </div>

        <!-- Mensaje de estado -->
        <div v-if="msg" :class="['message-status', ok ? 'ok-status' : 'fail-status']">
          {{ msg }}
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const numeroCuenta = ref('')
const tipo = ref('AHORRO') // Default seleccionado
const monto = ref(0)
const ownerId = ref('')
const loading = ref(false)
const msg = ref('')
const ok = ref(false)

const loadSessionUser = () => {
  try {
    const raw = localStorage.getItem('vulnapp_user')
    if (raw) {
      const u = JSON.parse(raw)
      // por defecto prefill con el usuario en sesión
      ownerId.value = u?.id ?? ''
    }
  } catch (e) {
    // ignore
  }
}

onMounted(loadSessionUser)

const goBack = () => router.push('/accounts')

const onCreate = async () => {
  msg.value = ''
  ok.value = false
  loading.value = true

  if (!numeroCuenta.value || !tipo.value || monto.value === null || monto.value === undefined) {
      msg.value = 'Por favor, completa todos los campos requeridos.'
      loading.value = false
      return
  }

  try {
    const payload = {
      numeroCuenta: numeroCuenta.value,
      tipo: tipo.value,
      monto: monto.value
    }

    if (ownerId.value) {
      // Convertir a número para que el backend de Spring Boot lo mapee correctamente
      payload.usuario = { id: Number(ownerId.value) }
    }

    const res = await axios.post('http://localhost:8080/api/accounts', payload)

    ok.value = true
    msg.value = `¡Cuenta creada con éxito! ID: ${res.data?.id ?? 'ok'}.`

    // volver al listado después de breve pausa
    setTimeout(() => router.push('/accounts'), 1000)
  } catch (err) {
    console.error(err)
    ok.value = false
    msg.value = err?.response?.data?.message ?? 'Error creando la cuenta. Revisa el servidor y CORS.'
  } finally {
    loading.value = false
  }
}
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

.page-minimal-create {
    background-color: var(--bg-light);
    min-height: 100vh;
    display: flex;
    justify-content: center;
    align-items: flex-start; /* Alineación arriba en desktop, centrado en móvil */
    padding: 40px 16px;
    font-family: 'Inter', system-ui, -apple-system, "Segoe UI", Roboto, Arial, sans-serif;
}

.card-create {
    width: 100%;
    max-width: 550px; /* Ancho cómodo para formularios */
    background: #ffffff;
    padding: 30px;
    border-radius: 18px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
    border: 1px solid var(--border-light);
}

/* --- HEADER Y NAVEGACIÓN --- */
.head-create {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
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
.title-create {
    font-size: 1.8rem;
    font-weight: 700;
    color: var(--text-dark);
    margin: 0;
}

.subtitle-area {
    margin-bottom: 25px;
    padding-bottom: 15px;
    border-bottom: 1px solid var(--border-light);
}
.muted-create {
    color: var(--text-subtle);
    font-size: 0.95rem;
    margin-bottom: 8px;
}
.note-create {
    color: var(--error-text);
    background-color: var(--error-bg);
    padding: 8px;
    border-radius: 8px;
    font-size: 0.85rem;
    border: 1px solid #fecaca;
    font-weight: 500;
}

/* --- FORMULARIO Y CAMPOS --- */
.form-create {
    display: flex;
    flex-direction: column;
    gap: 20px;
}
.field-group {
    display: flex;
    flex-direction: column;
}
.label-create {
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--text-dark);
    margin-bottom: 6px;
}
.input-create {
    padding: 12px 14px;
    border-radius: 10px;
    border: 1px solid var(--border-light);
    font-size: 1rem;
    color: var(--text-dark);
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}
.input-create:focus {
    border-color: var(--primary-blue);
    box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.15);
    outline: none;
}
.select-create {
    /* Estilo para que el select se vea igual */
    appearance: none;
    background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%236b7280' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 12px center;
    padding-right: 30px;
}
.hint-create {
    font-size: 0.8rem;
    color: var(--text-subtle);
    margin-top: 5px;
}

/* --- ACCIONES Y BOTONES --- */
.actions-create {
    display: flex;
    gap: 15px;
    margin-top: 15px;
}
.btn-primary-create {
    flex-grow: 1;
    background-color: var(--primary-blue);
    color: white;
    padding: 12px 20px;
    border-radius: 10px;
    border: none;
    cursor: pointer;
    font-weight: 700;
    font-size: 1.05rem;
    transition: all 0.2s ease;
    box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
}
.btn-primary-create:hover:not(:disabled) {
    background-color: var(--primary-dark);
    box-shadow: 0 6px 15px rgba(0, 122, 255, 0.4);
    transform: translateY(-1px);
}
.btn-primary-create:disabled {
    opacity: 0.7;
    cursor: not-allowed;
    box-shadow: none;
}
.btn-secondary-create {
    background: none;
    border: 1px solid var(--border-light);
    color: var(--text-dark);
    padding: 12px 20px;
    border-radius: 10px;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.2s ease;
}
.btn-secondary-create:hover {
    background-color: #f3f4f6;
    border-color: var(--primary-blue);
    color: var(--primary-blue);
}

/* --- MENSAJES DE ESTADO --- */
.message-status {
    margin-top: 15px;
    padding: 12px 16px;
    border-radius: 10px;
    font-weight: 600;
    font-size: 0.9rem;
    animation: fadeIn 0.5s;
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
.spinner-white-small {
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top: 3px solid #fff;
  border-radius: 50%;
  width: 16px;
  height: 16px;
  animation: spin 1s linear infinite;
  margin-right: 8px;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(-10px); }
    to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 600px) {
    .actions-create {
        flex-direction: column;
    }
    .page-minimal-create {
        align-items: stretch;
        padding: 20px 10px;
    }
    .card-create {
        padding: 20px;
    }
}
</style>
