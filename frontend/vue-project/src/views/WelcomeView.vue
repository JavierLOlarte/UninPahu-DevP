<template>
  <div class="welcome-cyber">
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
            ¡Hola! Soy tu asistente de seguridad. Aquí te mostraremos cómo proteger tus aplicaciones de ataques como IDOR y Mass Assignment. ¡Haz clic en mí para ver el índice de fallos!
        </div>

        <div v-if="showVulnerabilityList" class="vulnerability-list-inner">
            <h3 class="list-title">🛡️ Índice de Fallos de Seguridad</h3>
            <div
              v-for="v in vulnerabilidades"
              :key="v.id"
              class="vulnerability-item"
              @click.stop="toggleVulnerabilityDetails(v.id)">
                <div class="vulnerability-header">
                    <span class="vulnerability-name">{{ v.id }}. {{ v.nombre }}</span>
                    <span class="vulnerability-icon">{{ v.isOpen ? '▲' : '▼' }}</span>
                </div>
                <div v-if="v.isOpen" class="vulnerability-details">
                    <p class="simple-description">{{ v.descripcionSimple }}</p>
                </div>
            </div>
        </div>
      </div>
    </div>

    <div class="card-glass-dark">
      <header class="card-header">
        <h1 class="card-title">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lock-icon"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
            Laboratorio de Vulnerabilidades
        </h1>
        <p class="card-subtitle">
          Portal de Acceso Simulado | Versión Educativa
        </p>
      </header>

      <div class="card-body">
        <p class="card-description">
          ¡Bienvenido! Este entorno controlado te permite explorar fallos de seguridad críticos dentro de un simulador bancario.
        </p>
      </div>

      <p class="cta-message">
          Utiliza tus credenciales de prueba para **iniciar sesión** y comenzar a auditar el sistema.
      </p>

      <div class="button-container">
        <button class="btn-primary" @click="goLogin">
          Iniciar Sesión
        </button>
        <button class="btn-secondary" @click="goRegister">
          Registrarse
        </button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref } from 'vue'

const router = useRouter()
const showVulnerabilityList = ref(false)

const goRegister = () => {
  router.push('/register')
}

const goLogin = () => {
  router.push('/login')
}

// Array de vulnerabilidades con descripción simple para público no técnico
const vulnerabilidades = ref([
  {
    id: 1,
    nombre: "Inyección SQL",
    descripcionSimple: "Un atacante puede **manipular lo que el sistema pide a la base de datos**, logrando ver o cambiar información que no le pertenece (como datos de otros usuarios o cuentas bancarias)."
    , isOpen: false
  },
  {
    id: 2,
    nombre: "Token Inseguro",
    descripcionSimple: "Los **identificadores de tu sesión** (tokens) son muy fáciles de adivinar o copiar, permitiendo que un tercero se haga pasar por ti sin conocer tu contraseña."
    , isOpen: false
  },
  {
    id: 3,
    nombre: "Contraseñas en Texto Plano",
    descripcionSimple: "El sistema **guarda tu contraseña sin protegerla** (sin cifrado), lo que significa que cualquiera que acceda a la base de datos (incluso un empleado) puede leer tu clave."
    , isOpen: false
  },
  {
    id: 4,
    nombre: "Robo y Reutilización de Token",
    descripcionSimple: "Aunque cierres tu sesión de forma normal, el **código de acceso (token) sigue siendo válido**. Si un atacante lo robó antes, puede seguir entrando a tu cuenta cuando quiera."
    , isOpen: false
  },
  {
    id: 5,
    nombre: "IDOR (Referencia Insegura a Objeto Directo)",
    descripcionSimple: "El sistema **no comprueba si eres el dueño de la información** que solicitas. Si cambias el número de cuenta en la dirección web, puedes ver los datos de otra persona."
    , isOpen: false
  },
  {
    id: 6,
    nombre: "Vista no Protegida por Login",
    descripcionSimple: "Existe una página (o vista) importante que **se puede ver sin haber iniciado sesión**. Un atacante puede acceder a funciones o información confidencial sin usar usuario ni contraseña."
    , isOpen: false
  },
])

const toggleAssistantList = () => {
  showVulnerabilityList.value = !showVulnerabilityList.value
}

const toggleVulnerabilityDetails = (id) => {
  const v = vulnerabilidades.value.find(item => item.id === id)
  if (v) {
    v.isOpen = !v.isOpen
  }
}
</script>

<style scoped>
/* --- NUEVAS VARIABLES (Paleta Oscura/Sobria) --- */
:root {
    --color-primary: #3b82f6; /* Azul más corporativo */
    --color-primary-dark: #2563eb;
    --color-accent: #fcd34d; /* Amarillo para el acento/alerta */
    --color-background: #0f172a; /* Fondo muy oscuro (slate/navy) */
    --color-card-bg: #1e293b; /* Fondo de tarjeta un poco más claro */
    --color-text-light: #f1f5f9;
    --color-text-subtle: #94a3b8;
    --color-border: #334155;
    --color-error: #ef4444;
}

/* Animación del Asistente */
@keyframes float {
    0% { transform: translateY(0px); }
    50% { transform: translateY(-10px); }
    100% { transform: translateY(0px); }
}

.welcome-cyber {
  background-color: var(--color-background);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  font-family: 'Inter', system-ui, -apple-system, "Segoe UI", Roboto, Arial, sans-serif;
  color: var(--color-text-light);
  box-sizing: border-box;
}

/* --- ASISTENTE VISUAL --- */
.assistant-container {
    position: absolute;
    top: 50px;
    right: 50px;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    max-width: 450px;
    z-index: 10;
}

.assistant-bubble {
    background-color: var(--color-card-bg);
    border: 1px solid var(--color-primary);
    color: var(--color-text-light);
    padding: 15px;
    border-radius: 12px 12px 0 12px;
    margin-bottom: 10px;
    position: relative;
    font-size: 0.9rem;
    text-align: left;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.4);
    min-width: 280px;
    transition: all 0.3s ease;
}

.assistant-bubble.expanded {
    min-width: 350px;
    padding: 20px;
    border-color: var(--color-accent);
}

.assistant-bubble::after {
    content: '';
    position: absolute;
    bottom: -10px;
    right: 15px;
    width: 0;
    height: 0;
    border-left: 10px solid transparent;
    border-right: 10px solid transparent;
    border-top: 10px solid var(--color-primary);
    transition: all 0.3s ease;
}

.assistant-bubble.expanded::after {
    border-top: 10px solid var(--color-accent);
}


.assistant-icon {
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

/* --- LISTA DENTRO DEL GLOBO (Estilo Terminal) --- */
.vulnerability-list-inner {
    max-height: 400px;
    overflow-y: auto;
    padding-right: 5px;
}

.list-title {
    font-size: 1.1rem;
    color: var(--color-accent);
    margin-bottom: 10px;
    padding-bottom: 5px;
    border-bottom: 1px dashed var(--color-border);
}

.vulnerability-item {
    padding: 8px 0;
    border-bottom: 1px dotted var(--color-border);
    transition: background-color 0.2s;
}

.vulnerability-item:last-child {
    border-bottom: none;
}

.vulnerability-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    color: var(--color-text-light);
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
    color: var(--color-text-subtle);
}

.simple-description {
    margin-top: 5px;
    color: var(--color-text-light);
    font-weight: 400;
    line-height: 1.4;
}

/* --- TARJETA PRINCIPAL (Dark Card) --- */
.card-glass-dark {
  width: 100%;
  max-width: 500px;
  background: var(--color-card-bg);
  padding: 40px;
  border-radius: 18px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
  border: 1px solid var(--color-border);
  text-align: center;
}

/* --- HEADER Y TÍTULOS (Sin Cambios) --- */
.card-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border);
}

.card-title {
  font-size: 2.2rem;
  font-weight: 800;
  color: var(--color-primary);
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.lock-icon {
    color: var(--color-accent);
}

.card-subtitle {
  font-size: 0.9rem;
  color: var(--color-text-subtle);
  margin-top: 5px;
}

/* --- CUERPO (Sin Cambios) --- */
.card-description {
    font-size: 1rem;
    color: var(--color-text-light);
    line-height: 1.6;
    margin-bottom: 25px;
}

.cta-message {
    font-size: 1rem;
    font-weight: 600;
    color: var(--color-accent);
    margin-bottom: 30px;
    padding: 10px;
    border: 1px dashed var(--color-border);
    border-radius: 8px;
}

/* --- BOTONES (Sin Cambios) --- */
.button-container {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.btn-primary, .btn-secondary {
    padding: 14px 24px;
    border-radius: 8px;
    border: none;
    cursor: pointer;
    font-weight: 700;
    font-size: 1.05rem;
    transition: all 0.2s ease;
    min-width: 150px;
}

/* 1. Botón Principal (Iniciar Sesión): Azul */
.btn-primary {
    background-color: var(--color-primary);
    color: var(--color-text-light);
    box-shadow: 0 4px 10px rgba(59, 130, 246, 0.3);
}

.btn-primary:hover {
    background-color: var(--color-primary-dark);
    box-shadow: 0 6px 15px rgba(59, 130, 246, 0.5);
    transform: translateY(-2px);
}

/* 2. Botón Secundario (Registrarse): Contorno */
.btn-secondary {
    background: none;
    border: 2px solid var(--color-border);
    color: var(--color-text-light);
}

.btn-secondary:hover {
    background-color: var(--color-border);
    border-color: var(--color-accent);
    color: var(--color-accent);
}

/* Responsive para Móviles */
@media (max-width: 768px) {
    .assistant-container {
        position: static;
        align-items: flex-start;
        margin-bottom: 20px;
        max-width: 100%;
        text-align: left;
        right: auto;
    }
    .assistant-bubble {
        min-width: 100%;
    }
    .assistant-bubble.expanded {
        min-width: 100%;
    }
    .assistant-bubble::after {
        right: auto;
        left: 15px;
        border-top: 10px solid var(--color-card-bg);
    }
    .card-glass-dark {
        padding: 30px 20px;
    }
    .button-container {
        flex-direction: column;
        gap: 10px;
    }
    .btn-primary, .btn-secondary {
        min-width: 100%;
    }
}
</style>
