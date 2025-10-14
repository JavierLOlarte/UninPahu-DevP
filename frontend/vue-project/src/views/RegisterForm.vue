<template>
  <div class="form-page">
    <div class="card">
      <h2>Crear cuenta</h2>

      <form @submit.prevent="doRegister">

        <label>Nombre completo
          <input v-model="nombre" type="text" required />
        </label>

        <label>Email
          <input v-model="email" type="email" required />
        </label>

        <label>Rol
          <select v-model="rol" required>
            <option value="USER">USER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </label>

        <label>Contraseña
          <input v-model="password" type="password" required />
        </label>

        <label>Repetir contraseña
          <input v-model="password2" type="password" required />
        </label>

        <div class="row">
          <button class="btn primary" :disabled="loading">Registrar</button>
          <router-link to="/" class="btn secondary">Volver</router-link>
        </div>

        <p v-if="message" class="msg">{{ message }}</p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const nombre = ref('')
const email = ref('')
const rol = ref('USER')
const password = ref('')
const password2 = ref('')
const loading = ref(false)
const message = ref('')

async function doRegister() {
  message.value = ''

  if (password.value !== password2.value) {
    message.value = 'Las contraseñas no coinciden'
    return
  }

  loading.value = true
  try {
    await axios.post('http://localhost:8080/api/users', {
      nombre: nombre.value,
      email: email.value,
      rol: rol.value,
      password: password.value
    })
    message.value = 'Usuario creado exitosamente. Redirigiendo...'
    setTimeout(() => { location.href = '/login' }, 1000)
  } catch (err) {
    message.value = err.response?.data || 'Error al crear usuario'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.form-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(180deg,#07102a,#061627);
  padding: 28px;
  color: #eaf3ff;
  font-family: Inter, system-ui, Arial, sans-serif;
}
.card {
  width: 100%;
  max-width: 420px;
  padding: 22px;
  border-radius: 10px;
  background: rgba(255,255,255,0.02);
  border: 1px solid rgba(255,255,255,0.04);
}
h2 {
  margin-bottom: 18px;
  color: #bcd9f6;
  font-size: 1.3rem;
  text-align: center;
}
label {
  display: block;
  margin: 10px 0;
  color: #cfe6ff;
  font-size: 0.9rem;
}
input, select {
  width: 100%;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid rgba(255,255,255,0.06);
  background: transparent;
  color: #eaf3ff;
  margin-top: 6px;
}
.row {
  display: flex;
  gap: 10px;
  justify-content: space-between;
  margin-top: 14px;
}
.btn {
  padding: 10px 14px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  text-align: center;
}
.primary {
  background: linear-gradient(90deg,#1e90ff,#3b82f6);
  color: #ffffff;
}
.secondary {
  background: transparent;
  color: #cfe6ff;
  border: 1px solid rgba(255,255,255,0.04);
  padding: 8px 12px;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
}
.msg {
  margin-top: 12px;
  color: #cfe6ff;
}
</style>
