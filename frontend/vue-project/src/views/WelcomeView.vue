<template>
  <div class="welcome-container">
    <h1>🔓 Laboratorio de Vulnerabilidades</h1>
    <p>Explora, aprende y explota fallos de seguridad de forma controlada.</p>
    <div class="button-group">
      <button @click="goRegister">Registrarse</button>
      <button @click="goLogin">Iniciar sesión</button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

const goRegister = () => {
  router.push('/register')
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.welcome-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  text-align: center;
  padding: 20px;
}

h1 {
  font-size: 2rem;
  margin-bottom: 10px;
}

p {
  margin-bottom: 30px;
  font-size: 1.1rem;
}

.button-group {
  display: flex;
  gap: 20px;
}

button {
  padding: 12px 20px;
  cursor: pointer;
  font-size: 1rem;
  border: none;
  background-color: #4f46e5;
  color: white;
  border-radius: 6px;
}

button:hover {
  opacity: 0.9;
}
</style>
