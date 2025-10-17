<template>
  <div class="page">
    <div class="card">
      <header class="head">
        <h2>Crear nueva cuenta</h2>
        <p class="muted">Versión educativa — puedes fijar el ownerId para pruebas (IDOR)</p>
      </header>

      <form @submit.prevent="onCreate" class="form">
        <label class="field">
          <span>Owner ID (usuario)</span>
          <input v-model="ownerId" placeholder="ej. 1 (puedes tomarlo de localStorage)" />
          <small class="hint">Si dejas vacío, se establecerá manualmente (vulnerable si cliente lo fija).</small>
        </label>

        <label class="field">
          <span>Número de cuenta</span>
          <input v-model="numeroCuenta" placeholder="0001234567" />
        </label>

        <label class="field">
          <span>Tipo</span>
          <input v-model="tipo" placeholder="AHORROS / CORRIENTE" />
        </label>

        <label class="field">
          <span>Saldo inicial</span>
          <input v-model.number="monto" type="number" step="0.01" placeholder="1000.00" />
        </label>

        <div class="actions">
          <button class="btn" :disabled="loading">Crear cuenta</button>
          <button type="button" class="btn-sec" @click="goBack">Cancelar</button>
        </div>

        <div v-if="msg" :class="['message', ok ? 'ok' : 'fail']">{{ msg }}</div>
      </form>

      <footer class="help">
        <small>Tip: para pruebas de IDOR, crea una cuenta con ownerId = 2 y luego intenta verla desde otro usuario.</small>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const numeroCuenta = ref('')
const tipo = ref('')
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

  try {
    // Construimos el payload; NOTA: en la versión vulnerable permitimos enviar usuario completo o ownerId.
    const payload = {
      numeroCuenta: numeroCuenta.value || `ACC-${Date.now()}`,
      tipo: tipo.value || 'AHORROS',
      monto: monto.value ?? 0
    }

    // if ownerId provided, attach minimal usuario object to match AccountEntity.usuario relation
    if (ownerId.value) {
      payload.usuario = { id: Number(ownerId.value) }
    }

    const res = await axios.post('http://localhost:8080/api/accounts', payload)
    msg.value = 'Cuenta creada con id ' + (res.data?.id ?? 'ok')
    ok.value = true

    // volver al listado después de breve pausa
    setTimeout(() => router.push('/accounts'), 700)
  } catch (err) {
    console.error(err)
    ok.value = false
    msg.value = err?.response?.data?.message ?? 'Error creando la cuenta. Revisa backend.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { max-width:720px; margin:28px auto; padding:12px; font-family:Inter,system-ui; }
.card { padding:18px; border-radius:12px; background:#fff; box-shadow:0 8px 24px rgba(2,6,23,0.06); }
.head h2 { margin:0; }
.muted { color:#6b7280; font-size:13px; margin-bottom:12px; }
.form { display:flex; flex-direction:column; gap:10px; }
.field input { padding:8px 10px; border-radius:8px; border:1px solid #e6eef8; }
.actions { display:flex; gap:8px; margin-top:8px; }
.btn { background:#2563eb; color:#fff; padding:8px 12px; border-radius:8px; border:none; }
.btn-sec { background:transparent; border:1px solid #cbd5e1; padding:8px 12px; border-radius:8px; }
.message { margin-top:10px; padding:10px; border-radius:8px; }
.ok { background:#ecfdf5; color:#065f46; border:1px solid #bbf7d0; }
.fail { background:#fff1f2; color:#991b1b; border:1px solid #fecaca; }
.help { margin-top:12px; color:#6b7280; font-size:13px; }
</style>
