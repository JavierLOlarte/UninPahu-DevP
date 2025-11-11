<template>
  <div class="form-page-animated">
    <div class="card-animated" @mousemove="handleMouseMove">
      <div class="card-inner">

        <!-- Título -->
        <h2 class="title-animated">👋 Crea tu Cuenta</h2>

        <form @submit.prevent="doRegister" class="form-stack">

          <!-- Nombre -->
          <div class="input-group">
            <label class="input-label-animated">Nombre completo</label>
            <input
              v-model="nombre"
              type="text"
              required
              class="custom-input-animated"
              placeholder="Ej: Sofía Rojas"
            />
          </div>

          <!-- Rol (select wrap para control del icono sin romper padding) -->
          <div class="input-group">
            <label class="input-label-animated">Rol (solo prueba)</label>
            <div class="select-wrap">
              <select v-model="rol" required class="custom-input-animated custom-select">
                <option value="USER">Usuario</option>
                <option value="ADMIN">Administrador</option>
              </select>
            </div>
          </div>

          <!-- Email -->
          <div class="input-group">
            <label class="input-label-animated">Email</label>
            <input
              v-model="email"
              type="email"
              required
              class="custom-input-animated"
              placeholder="ejemplo@correo.com"
            />
          </div>

          <!-- Contraseña -->
          <div class="input-group">
            <label class="input-label-animated">Contraseña</label>
            <input
              v-model="password"
              type="password"
              required
              class="custom-input-animated"
              placeholder="Mínimo 8 caracteres"
            />
          </div>

          <!-- Repetir contraseña -->
          <div class="input-group">
            <label class="input-label-animated">Repetir contraseña</label>
            <input
              v-model="password2"
              type="password"
              required
              class="custom-input-animated"
              placeholder="Confirma tu contraseña"
            />
          </div>

          <!-- Botones -->
          <div class="form-buttons">
            <button class="btn-animated primary-animated" :disabled="loading" type="submit">
              <span v-if="loading" class="spinner"></span>
              {{ loading ? "Registrando..." : "Crear Cuenta" }}
            </button>

            <button
              type="button"
              class="btn-animated secondary-animated"
              @click="goLogin"
            >
              ¿Ya tienes cuenta? Inicia Sesión
            </button>
          </div>

          <!-- Mensaje -->
          <p v-if="message" class="msg-animated">{{ message }}</p>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";

const router = useRouter();

// Form state
const nombre = ref("");
const email = ref("");
const rol = ref("USER");
const password = ref("");
const password2 = ref("");
const loading = ref(false);
const message = ref("");

// Register
async function doRegister() {
  message.value = "";
  if (password.value !== password2.value) {
    message.value = "Las contraseñas no coinciden";
    return;
  }

  loading.value = true;

  try {
    await axios.post("http://localhost:8080/api/users", {
      nombre: nombre.value,
      email: email.value,
      rol: rol.value,
      password: password.value,
    });

    message.value =
      "¡Cuenta creada con éxito! Serás redirigido para iniciar sesión.";

    setTimeout(() => {
      router.push("/login");
    }, 1600);
  } catch (err) {
    message.value =
      err.response?.data?.message ||
      "Error al crear usuario. Intenta de nuevo.";
  } finally {
    loading.value = false;
  }
}

// Navigation
function goLogin() {
  router.push("/login");
}

// Mouse Tilt (parallax)
function handleMouseMove(event) {
  const card = event.currentTarget;
  const rect = card.getBoundingClientRect();
  const centerX = rect.left + rect.width / 2;
  const centerY = rect.top + rect.height / 2;

  const rotateX = (event.clientY - centerY) * 0.05;
  const rotateY = (event.clientX - centerX) * -0.05;

  card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
}

function resetCardTransform(event) {
  event.currentTarget.style.transform =
    "perspective(1000px) rotateX(0deg) rotateY(0deg)";
}

onMounted(() => {
  const el = document.querySelector(".card-animated");
  el && el.addEventListener("mouseleave", resetCardTransform);
});
</script>

<style>
/* --- PALETA DE COLORES --- */
:root {
  --bg-dark: #0a1128;
  --card-bg: rgba(10, 20, 40, 0.85);
  --primary-light: #c9e6ff;
  --primary-blue-start: #1e90ff;
  --primary-blue-end: #3c7dff;
  --input-bg: #152538;
  --input-border: #1f3750;
  --input-focus: #4c8fff;
  --text-subtle: #a0c2e6;
}

/* Fondo animado */
@keyframes gradient-animation {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.form-page-animated {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 28px;
  font-family: "Inter", system-ui, Arial, sans-serif;
  overflow: hidden;
  background: linear-gradient(135deg, var(--bg-dark), #112a4c, var(--bg-dark));
  background-size: 300% 300%;
  animation: gradient-animation 15s ease infinite;
  position: relative;
}

/* malla background (sutil) */
.form-page-animated::before {
  content: "";
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%233c7dff' fill-opacity='0.08'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zm0-30V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.8;
  pointer-events: none;
  animation: background-pan 60s linear infinite;
}
@keyframes background-pan {
  from { background-position: 0 0; }
  to { background-position: 100% 100%; }
}

/* --- TARJETA PRINCIPAL (borde externo) --- */
.card-animated {
  width: 100%;
  max-width: 480px;
  padding: 3px;
  border-radius: 26px;
  background: linear-gradient(145deg, rgba(255,255,255,0.06) 0%, rgba(255,255,255,0.02) 100%);
  position: relative;
  z-index: 10;
  box-shadow: 0 26px 70px rgba(0, 0, 0, 0.6);
  transition: transform 0.45s cubic-bezier(0.22,0.9,0.3,1), box-shadow 0.35s ease;
}

/* tarjeta interna (padding simétrico) */
.card-inner {
  padding: 40px 36px; /* <- simetría: top/bot 40, left/right 36 */
  border-radius: 22px;
  background: var(--card-bg);
  backdrop-filter: blur(18px) saturate(160%);
  border: 1px solid rgba(255,255,255,0.06);
}

/* título */
.title-animated {
  margin-bottom: 26px;
  font-size: 2.05rem;
  font-weight: 900;
  text-align: center;
  letter-spacing: -0.02em;
  background: linear-gradient(90deg, #b2d2ff, #e5f1ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* FORM STACK: ritmo vertical constante */
.form-stack {
  display: flex;
  flex-direction: column;
  gap: 20px; /* espacio uniforme entre bloques */
}

/* cada grupo de input */
.input-group {
  margin: 0;
}

/* label */
.input-label-animated {
  display: block;
  margin-bottom: 8px;
  color: var(--text-subtle);
  font-size: 0.9rem;
  font-weight: 500;
}

/* --- Inputs y selects: caja uniforme para simetría perfecta --- */
/* Important: usar box-sizing para que padding no rompa el ancho */
.custom-input-animated {
  width: 100%;
  box-sizing: border-box;
  padding: 14px 22px;    /* <-- izquierda y derecha iguales */
  border-radius: 12px;
  border: 1px solid var(--input-border);
  background: var(--input-bg);
  color: var(--primary-light);
  font-size: 1rem;
  line-height: 1;        /* evita saltos verticales */
  height: auto;
  min-height: 48px;      /* altura visual consistente */
  transition: all 0.25s ease;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.45);
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

/* placeholder más leve */
.custom-input-animated::placeholder {
  color: #5b7393;
  opacity: 1;
}

/* foco */
.custom-input-animated:focus {
  border-color: var(--input-focus);
  outline: none;
  box-shadow: 0 0 0 4px rgba(76, 143, 255, 0.13), inset 0 1px 3px rgba(0, 0, 0, 0.5);
}

/* --- SELECT WRAP (para el icono) --- */
.select-wrap {
  position: relative;
}

/* eliminamos fondo nativo del select y usamos la misma caja */
.custom-select {
  padding-right: 50px; /* espacio para el icono pseudo */
}

/* pseudo-icono (flecha) dentro del wrapper */
.select-wrap::after {
  content: "";
  position: absolute;
  right: 18px; /* mantiene distancia desde borde (simétrica con input) */
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  pointer-events: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'%3E%3Cpath d='M6 9l6 6 6-6' fill='none' stroke='%23a0c2e6' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: center;
  opacity: 0.95;
}

/* Aseguramos que los option tengan fondo acorde */
.custom-select option {
  background: var(--input-bg);
  color: var(--primary-light);
}

/* --- Botones --- */
.form-buttons {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 6px;
}

/* boton base */
.btn-animated {
  padding: 14px 22px;
  border-radius: 12px;
  border: none;
  cursor: pointer;
  font-weight: 700;
  font-size: 1.05rem;
  transition: all 0.28s cubic-bezier(0.2,0.9,0.3,1);
  letter-spacing: 0.02em;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  box-sizing: border-box;
}

/* primario */
.primary-animated {
  background: linear-gradient(90deg, var(--primary-blue-start), var(--primary-blue-end));
  color: #fff;
  box-shadow: 0 12px 34px rgba(30,144,255,0.35);
}
.primary-animated:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 18px 48px rgba(30,144,255,0.45);
}
.primary-animated:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
}

/* secundario */
.secondary-animated {
  background: transparent;
  color: var(--text-subtle);
  border: 1px solid rgba(255,255,255,0.08);
  font-size: 0.95rem;
  padding: 12px 20px;
}
.secondary-animated:hover {
  background: rgba(76, 143, 255, 0.06);
  color: var(--primary-light);
  transform: translateY(-1px);
}

/* mensaje */
.msg-animated {
  margin-top: 14px;
  padding: 12px;
  border-radius: 10px;
  text-align: center;
  background: rgba(76, 143, 255, 0.14);
  color: #e6f3ff;
  font-size: 0.95rem;
  border: 1px solid rgba(76, 143, 255, 0.24);
}

/* spinner */
.spinner {
  border: 3px solid rgba(255,255,255,0.25);
  border-top: 3px solid #fff;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
  display: inline-block;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* small screens */
@media (max-width: 420px) {
  .card-inner {
    padding: 28px 20px;
  }
  .title-animated { font-size: 1.65rem; }
  .custom-input-animated { padding: 12px 16px; min-height: 44px; }
  .select-wrap::after { right: 14px; width: 16px; height: 16px; }
}
</style>
