<template>
  <div class="page">
    <div class="card">
      <header class="head">
        <h2>Detalle de cuenta</h2>
        <p class="muted">ID: <strong>{{ id }}</strong></p>
      </header>

      <div v-if="loading" class="loading">Cargando...</div>

      <div v-else-if="account">
        <div class="info">
          <p><strong>Número:</strong> {{ account.numeroCuenta }}</p>
          <p><strong>Tipo:</strong> {{ account.tipo }}</p>
          <p><strong>Saldo actual:</strong> {{ account.monto }}</p>
          <p><strong>Owner (usuario.id):</strong> {{ account.usuario?.id ?? '—' }}</p>
        </div>

        <section class="panel">
          <h3>Actualizar saldo (vulnerable)</h3>
          <p class="small">Este endpoint permite actualizar monto sin autorización en la versión vulnerable.</p>

          <div class="row">
            <input v-model.number="newMonto" type="number" step="0.01" placeholder="nuevo saldo" />
            <button class="btn" @click="updateBalance" :disabled="updating">Actualizar</button>
          </div>

          <div v-if="updateMsg" :class="['message', updateOk ? 'ok' : 'fail']">{{ updateMsg }}</div>
        </section>

        <section class="panel">
          <h3>Eliminar cuenta</h3>
          <p class="small">Botón de eliminación (sin checks en versión vulnerable).</p>
          <div class="row">
            <button class="btn-danger" @click="deleteAccount" :disabled="deleting">Eliminar cuenta</button>
            <button class="btn-sec" @click="goBack">Volver</button>
          </div>

          <div v-if="deleteMsg" :class="['message', deleteOk ? 'ok' : 'fail']">{{ deleteMsg }}</div>
        </section>
      </div>

      <div v-else class="empty">Cuenta no encontrada.</div>
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

// dentro de <script setup> — sustituir implementaciones previas

const fetchAccount = async () => {
  loading.value = true
  account.value = null
  try {
    console.log('fetching account id=', id)
    const res = await axios.get(`http://localhost:8080/api/accounts/${id}`)
    account.value = res.data
    newMonto.value = account.value?.monto ?? 0
  } catch (err) {
    console.error('fetchAccount error', err)
    // Manejo claro según status
    const status = err?.response?.status
    if (status === 404) {
      updateMsg.value = 'Cuenta no encontrada (404). Redirigiendo al listado...'
      setTimeout(() => router.push('/accounts'), 900)
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
  try {
    // enviar payload en objeto para evitar inconsistencias
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
    deleteMsg.value = 'Cuenta eliminada.'
    deleteOk.value = true
    setTimeout(() => router.push('/accounts'), 700)
  } catch (err) {
    console.error('deleteAccount error', err)
    const status = err?.response?.status
    if (status === 404) deleteMsg.value = 'Cuenta no encontrada al eliminar.'
    else deleteMsg.value = 'Error al eliminar la cuenta.'
    deleteOk.value = false
  } finally {
    deleting.value = false
  }
}


const goBack = () => router.push('/accounts')

onMounted(fetchAccount)
</script>

<style scoped>
.page { max-width:720px; margin:28px auto; padding:12px; font-family:Inter,system-ui; }
.card { padding:18px; border-radius:12px; background:#fff; box-shadow:0 8px 24px rgba(2,6,23,0.06); }
.head h2 { margin:0; }
.muted { color:#6b7280; font-size:13px; margin-bottom:12px; }
.info p { margin:6px 0; }
.panel { margin-top:12px; padding:12px; border-radius:8px; background:#f8fafc; }
.row { display:flex; gap:8px; align-items:center; margin-top:8px; }
.row input { padding:8px; border-radius:8px; border:1px solid #e6eef8; }
.btn { background:#2563eb; color:#fff; padding:8px 12px; border-radius:8px; border:none; cursor:pointer; }
.btn-danger { background:#ef4444; color:#fff; padding:8px 12px; border-radius:8px; border:none; cursor:pointer; }
.btn-sec { background:transparent; border:1px solid #cbd5e1; padding:8px 12px; border-radius:8px; }
.message { margin-top:10px; padding:10px; border-radius:8px; }
.ok { background:#ecfdf5; color:#065f46; border:1px solid #bbf7d0; }
.fail { background:#fff1f2; color:#991b1b; border:1px solid #fecaca; }
.empty { padding:20px; color:#6b7280; }
</style>
