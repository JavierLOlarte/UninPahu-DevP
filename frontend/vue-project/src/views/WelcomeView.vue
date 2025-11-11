<template>
  <div class="welcome-cyber">
    <div class="assistant-container">
      <div class="assistant-bubble">
        ¡Hola! Soy tu asistente de seguridad. Aquí te mostraremos cómo proteger tus aplicaciones de ataques como IDOR y Mass Assignment. ¡Asegúrate de iniciar sesión!
      </div>
      <!-- Personaje Asistente (estilo robot/navegante) -->
      <div class="assistant-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="animate-float">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5M2 12l10 5 10-5"/>
        </svg>
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
          ¡Bienvenido! Este entorno controlado te permite explorar fallos de seguridad críticos como **IDOR**, **Broken Access Control** o **Mass Assignment** dentro de un simulador bancario.
        </p>
      </div>

      <p class="cta-message">
          Utiliza tus credenciales de prueba para **iniciar sesión** y comenzar a auditar el sistema.
      </p>

      <div class="button-container">
        <!-- Botón Primario: Iniciar Sesión (Azul brillante con texto blanco) -->
        <button class="btn-primary" @click="goLogin">
          Iniciar Sesión
        </button>
        <!-- Botón Secundario: Registrarse (Contorno oscuro con texto claro) -->
        <button class="btn-secondary" @click="goRegister">
          Registrarse
        </button>
      </div>

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
/* --- VARIABLES Y FUENTES (Tema Ciberseguridad Oscuro) --- */
:root {
    --color-primary: #00FFC2; /* Verde/Cian Neón para el acento */
    --color-primary-dark: #00cc99;
    --color-background: #111827; /* Fondo muy oscuro */
    --color-card-bg: #1f2937; /* Fondo de tarjeta un poco más claro */
    --color-text-light: #f9fafb;
    --color-text-subtle: #9ca3af;
    --color-border: #374151;
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
    max-width: 300px;
    z-index: 10;
}

.assistant-bubble {
    background-color: var(--color-card-bg);
    border: 1px solid var(--color-primary);
    color: var(--color-text-light);
    padding: 15px;
    border-radius: 12px;
    margin-bottom: 10px;
    position: relative;
    font-size: 0.9rem;
    text-align: left;
    box-shadow: 0 4px 10px rgba(0, 255, 194, 0.15);
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
}

.assistant-icon {
    color: var(--color-primary);
    animation: float 3s ease-in-out infinite;
}


/* --- TARJETA PRINCIPAL (Dark Card) --- */
.card-glass-dark {
  width: 100%;
  max-width: 500px;
  background: var(--color-card-bg);
  padding: 40px;
  border-radius: 18px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
  border: 1px solid var(--color-border);
  text-align: center;
}

/* --- HEADER Y TÍTULOS --- */
.card-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--color-border);
}

.card-title {
  font-size: 2.2rem;
  font-weight: 800;
  color: var(--color-primary); /* Título en color de acento */
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.lock-icon {
    color: var(--color-text-subtle);
}

.card-subtitle {
  font-size: 0.9rem;
  color: var(--color-text-subtle);
  margin-top: 5px;
}

/* --- CUERPO --- */
.card-description {
    font-size: 1rem;
    color: var(--color-text-light);
    line-height: 1.6;
    margin-bottom: 25px;
}

.cta-message {
    font-size: 1rem;
    font-weight: 600;
    color: var(--color-primary);
    margin-bottom: 30px;
    padding: 10px;
    border: 1px dashed var(--color-border);
    border-radius: 8px;
}

/* --- BOTONES --- */
.button-container {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.btn-primary, .btn-secondary {
    padding: 14px 24px;
    border-radius: 12px;
    border: none;
    cursor: pointer;
    font-weight: 700;
    font-size: 1.05rem;
    transition: all 0.2s ease;
    min-width: 150px;
}

/* 1. Botón Principal (Iniciar Sesión): Cian/Neón */
.btn-primary {
    background-color: var(--color-primary);
    color: var(--color-background); /* Texto oscuro sobre neón */
    box-shadow: 0 6px 15px rgba(0, 255, 194, 0.3);
}

.btn-primary:hover {
    background-color: var(--color-primary-dark);
    box-shadow: 0 8px 20px rgba(0, 255, 194, 0.5);
    transform: translateY(-2px);
}

/* 2. Botón Secundario (Registrarse): Transparente/Contorno */
.btn-secondary {
    background: none;
    border: 2px solid var(--color-border);
    color: var(--color-text-light); /* Texto claro */
}

.btn-secondary:hover {
    background-color: var(--color-border);
    border-color: var(--color-primary);
    color: var(--color-primary);
}

/* Responsive para Móviles */
@media (max-width: 768px) {
    .assistant-container {
        position: static;
        align-items: flex-start;
        margin-bottom: 20px;
        max-width: 100%;
        text-align: left;
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


Con este rediseño:
1.  **Estilo:** Es un tema oscuro (`#111827`) que se ve profesional y asociado a la ciberseguridad.
2.  **Botones:** El botón **Iniciar Sesión** es cian/verde neón brillante con texto oscuro (máximo contraste) y se destaca.
3.  **Asistente:** Se añadió un personaje flotante (`.assistant-icon` con la clase `animate-float`) con un globo de texto (`.assistant-bubble`) para cumplir con tu solicitud de un "muñequito que vuele y explique".

Espero que este diseño sea el que buscabas. Ahora, podemos avanzar a la página de Transferencias.
