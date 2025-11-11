<template>
  <div class="page form-page-animated">

    <div class="card card-animated" @mousemove="handleMouseMove" @mouseleave="resetCardTransform">
      <div class="card-inner">
        <div class="brand">
          <span class="logo-icon">🔒</span>
          <h1>SecureBank Demo</h1>
        </div>

        <h2 class="title title-animated">Iniciar Sesión</h2>
        <p class="subtitle">Ejercicio educativo: **Login Vulnerable** (¡SQLi posible!) - Backend v1.0</p>

        <form @submit.prevent="handleFormSubmit" novalidate class="form">

          <label class="field input-group">
            <span class="label-text input-label-animated">Email de Acceso (Usuario)</span>
            <input
              v-model="email"
              type="text"
              placeholder="tu@ejemplo.com"
              :class="['custom-input-animated', { invalid: emailTouched && !validEmail }]"
              @blur="emailTouched = true"
              @keypress.enter="onSubmit"
              autocomplete="username"
            />
            <small v-if="emailTouched && !validEmail" class="error">Ingresa un email válido.</small>
          </label>

          <label class="field input-group">
            <span class="label-text input-label-animated">Contraseña</span>
            <div class="password-row">
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Mínimo 8 caracteres"
                @blur="passwordTouched = true"
                @keypress.enter="onSubmit"
                :class="['custom-input-animated', { invalid: passwordTouched && !password }]"
                autocomplete="current-password"
              />
              <button type="button" class="peek btn-animated secondary-animated" @click="showPassword = !showPassword" :aria-pressed="showPassword">
                {{ showPassword ? 'Ocultar' : 'Mostrar' }}
              </button>
            </div>
            <small v-if="passwordTouched && !password" class="error">La contraseña es requerida.</small>
          </label>

          <div class="row-animated">
            <button
              type="button"
              class="btn-animated primary-animated"
              :disabled="loading || !formValid"
              @click="onSubmit"
            >
              <span v-if="!loading">Iniciar Sesión</span>
              <span v-else class="spinner" aria-hidden="true"></span>
            </button>
          </div>

          <!-- BOTÓN DE REGISTRO - CORREGIDO AQUÍ, LA CLASE DEL PADRE SE AJUSTA EN CSS -->
          <div class="actions extra">
            <button
              type="button"
              class="btn-animated secondary-animated wide-btn"
              @click="goRegister"
            >
              ¿No tienes cuenta? Regístrate ahora
            </button>
          </div>

          <p v-if="message" :class="['message msg-animated', success ? 'ok' : 'fail']">{{ message }}</p>

          <div v-if="user" class="result">
            <h3>Sesión Activa:</h3>
            <pre>Email: {{ user.email }}</pre>
            <p><strong>Token de Sesión (inseguro):</strong> <code>{{ token }}</code></p>
            <button class="btn-link" @click="clearSession">Cerrar sesión demo</button>
          </div>
        </form>

        <footer class="help">
          <small>Pista: Usa el Asistente de SQLi para bypass o ingresa credenciales válidas.</small>
        </footer>
      </div>
    </div>

    <div class="assistant-container">
      <button class="assistant-button" @click="isAssistantOpen = !isAssistantOpen" :aria-expanded="isAssistantOpen" :title="isAssistantOpen ? 'Cerrar Asistente' : 'Abrir Asistente de SQLi'">
        <svg v-if="isAssistantOpen" class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
        <svg v-else class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2a5 5 0 0 0-5 5c0 1.5 1 4 5 7 4-3 5-5.5 5-7a5 5 0 0 0-5-5z"></path><path d="M12 18c-2 0-3.5-1-4-3h8c-.5 2-2 3-4 3z"></path><line x1="12" y1="2" x2="12" y2="3"></line><line x1="21" y1="7" x2="20" y2="8"></line><line x1="3" y1="7" x2="4" y2="8"></line><line x1="18.5" y1="14.5" x2="19.5" y2="15.5"></line><line x1="4.5" y1="14.5" x2="5.5" y2="15.5"></line></svg>
      </button>

      <Transition name="assistant-fade-slide">
        <div v-if="isAssistantOpen" class="assistant-modal">
          <div class="assistant-header">
            Asistente de Práctica: Bypass de Autenticación SQLi
          </div>
          <div class="assistant-body">
            <p class="assistant-gemini-info">
              <small>
                Misión: Iniciar sesión sin conocer la contraseña, aprovechando la vulnerabilidad del campo Email.
              </small>
            </p>

            <div class="chat-response">
              <div v-html="assistantMessage"></div>

              <div class="hint-control">
                  <button
                      v-if="hintLevel < 4 && !user"
                      @click="advanceHint"
                      class="btn-hint btn-primary"
                  >
                      {{ hintLevel === 0 ? 'Iniciar Guía (Nivel 1)' : `Pista Siguiente (Nivel ${hintLevel + 1})` }}
                  </button>
                  <button
                      v-if="hintLevel > 0 && !user"
                      @click="hintLevel = 0"
                      class="btn-hint btn-secondary"
                  >
                      Reiniciar Pistas
                  </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </div>
    </div>
  </template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

// estado
const email = ref('')
const password = ref('')
// ELIMINAMOS 'remember'
const showPassword = ref(false)
const loading = ref(false)
const message = ref('')
const success = ref(false)
const user = ref(null)
const token = ref(null)
const emailTouched = ref(false)
const passwordTouched = ref(false)

// --- ESTADO DEL ASISTENTE ---
const isAssistantOpen = ref(false);
const hintLevel = ref(0); // 0: Introducción, 1-4: Pistas

const advanceHint = () => {
    if (hintLevel.value < 4) {
        hintLevel.value += 1;
    }
};

// --- MENSAJES DINÁMICOS DEL ASISTENTE (NIVELES DE PISTA) ---
const assistantMessage = computed(() => {
    const SQL_PAYLOAD = "' OR 1=1 -- ";

    if (user.value) {
        const isSqliLogin = email.value.trim().toUpperCase().includes(SQL_PAYLOAD.trim().toUpperCase());

        let successMessage = isSqliLogin ?
                            `<p><strong>¡Misión de SQLi Completada!</strong></p>
                <p>El servidor ejecutó el payload inyectado: <code>${SQL_PAYLOAD}</code>. Esto forzó a la base de datos a devolver el primer usuario, **saltando la verificación de contraseña**.</p>
                <p>¡Vulnerabilidad demostrada! Ahora intenta cerrar sesión y usar credenciales válidas para comprobar el *backend*.</p>`
                        :
                            `<p><strong>¡Inicio de Sesión Exitoso!</strong></p>
                <p>Has usado una cuenta válida. El **backend** está funcionando correctamente. Presiona **Reiniciar Pistas** si quieres intentar el ataque SQLi.</p>`
            ;
        return `<div class="success-message">${successMessage}</div>`;
    }

    switch (hintLevel.value) {
        case 0:
            return `<p><strong>Bienvenido.</strong> Este login está diseñado para fallar si no se toman medidas de seguridad adecuadas en el servidor.</p>
                <p>Tu objetivo es iniciar sesión como el primer usuario de la base de datos (típicamente el administrador) sin saber ninguna contraseña.</p>
                <p>Presiona <strong>"Iniciar Guía"</strong> para la primera pista.</p>`;
        case 1:
            return `<p><strong>Nivel 1: Cerrando la Consulta</strong></p>
                <p>El servidor usa tu Email para construir una sentencia SQL. Para inyectar tu propio código, debes terminar primero la cadena que el servidor espera.</p>
                <p><strong>Pista:</strong> El campo Email probablemente está envuelto en comillas simples (<code>'</code>). Usa la comilla simple para finalizarlo.</p>
                <pre class="payload-example">Email: <strong>'</strong></pre>
                <p>¡Presiona el botón para la siguiente pista!</p>`;
        case 2:
            return `<p><strong>Nivel 2: La Condición Universal</strong></p>
                <p>Ahora que has cerrado la cadena, puedes agregar una condición lógica que siempre sea cierta. Esto le dirá a la base de datos que la autenticación es válida sin verificar la contraseña real.</p>
                <p><strong>Pista:</strong> Agrega <code>OR 1=1</code>. Esto le dice a SQL "O (la condición original de contraseña) **O** (uno es igual a uno)".</p>
                <pre class="payload-example">Email: <strong>' OR 1=1</strong></pre>`;
        case 3:
            return `<p><strong>Nivel 3: El Silencio es Oro</strong></p>
                <p>Aún quedan caracteres en la sentencia SQL original (la parte de la contraseña, etc.). Para que la base de datos los ignore y no arroje un error de sintaxis, usa el comando de comentario SQL.</p>
                <p><strong>Pista:</strong> El comentario de una sola línea es <code>-- </code> (dos guiones y un espacio).</p>
                <pre class="payload-example">Email: <strong>' OR 1=1 -- </strong></pre>`;
        case 4:
            return `<p><strong>Nivel 4: Ejecución Final (¡A Inyectar!)</strong></p>
                <p>El payload completo para el bypass de autenticación (el más clásico) es:</p>
                <pre class="payload-example"><strong>' OR 1=1 -- </strong></pre>
                <p><strong>Instrucciones:</strong></p>
                <ol>
                    <li>Copia el payload de arriba.</li>
                    <li>Pégalo en el campo **Email**.</li>
                    <li>Ingresa **cualquier cosa** en el campo Contraseña (debe tener al menos un carácter).</li>
                    <li>Presiona **Iniciar Sesión**.</li>
                </ol>
                <p>Si la vulnerabilidad existe en el servidor, serás autenticado.</p>`;
        default:
            return `<p>Error. Reinicia el contador de pistas.</p>`;
    }
});


// validaciones
const validEmail = computed(() => {
    const isSqliAttempt = email.value.trim().toUpperCase().includes("' OR 1=1");
    if (isSqliAttempt) return true;
    return /\S+@\S+\.\S+/.test(email.value)
})
const formValid = computed(() => validEmail.value && password.value.length > 0)

// Función para manejar el submit del formulario
const handleFormSubmit = (event) => {
  event.preventDefault()
  event.stopPropagation()
  onSubmit()
}

const onSubmit = async () => {
  emailTouched.value = true
  passwordTouched.value = true
  message.value = ''
  success.value = false
  user.value = null
  token.value = null

  if (!formValid.value) {
    message.value = 'Completa los campos correctamente.'
    return
  }

  loading.value = true
  try {
    const resp = await axios.post('http://localhost:8080/api/auth/login', {
      email: email.value,
      password: password.value
    }, { headers: { 'Content-Type': 'application/json' } })

    const data = resp.data

    user.value = {
      id: data.userId ?? null,
      email: data.email ?? email.value
    }

    token.value = data.token
    if (token.value) {
      axios.defaults.headers.common['Authorization'] = 'Bearer ' + token.value
      localStorage.setItem('vulnapp_token', token.value)
      localStorage.setItem('vulnapp_user', JSON.stringify(user.value))
    }

    const isSqliLogin = email.value.trim().toUpperCase().includes("' OR 1=1 --");

    if (isSqliLogin) {
      message.value = '¡Acceso conseguido por Inyección SQL! Revisa el asistente para más detalles.'
      isAssistantOpen.value = true;
    } else {
      message.value = 'Login exitoso. Bienvenido a tu portal.'
    }

    success.value = true

    setTimeout(() => {
       router.push('/accounts')
    }, 6000)

  } catch (err) {
    success.value = false
    if (err.response?.status === 401) {
      message.value = 'Credenciales inválidas. Revisa el asistente si intentas el bypass SQLi.'
    } else {
      message.value = 'Error de conexión. Asegúrate de que tu backend esté corriendo en http://localhost:8080.'
    }
    console.error(err)
  } finally {
    loading.value = false
  }
}

const clearSession = () => {
  email.value = user.value.email;
  password.value = '';
  user.value = null
  token.value = null
  message.value = ''
  success.value = false
  isAssistantOpen.value = false;
  hintLevel.value = 0;

  localStorage.removeItem('vulnapp_token')
  localStorage.removeItem('vulnapp_user')

  delete axios.defaults.headers.common['Authorization']

  router.push('/login')
}

const goRegister = () => {
  router.push('/register')
}


// ** LÓGICA DE PARALLAX **
const handleMouseMove = (event) => {
  const card = event.currentTarget;
  const rect = card.getBoundingClientRect();
  const centerX = rect.left + rect.width / 2;
  const centerY = rect.top + rect.height / 2;

  const mouseX = event.clientX;
  const mouseY = event.clientY;

  const sensitivity = 0.03;
  const rotateX = (mouseY - centerY) * sensitivity;
  const rotateY = (mouseX - centerX) * -sensitivity;

  card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
}

const resetCardTransform = (event) => {
  event.currentTarget.style.transform = `perspective(1000px) rotateX(0deg) rotateY(0deg)`;
}

onMounted(() => {
  const cardElement = document.querySelector('.card-animated');
  if (cardElement) {
    cardElement.addEventListener('mouseleave', resetCardTransform);
  }
});
</script>


<style scoped>
/* ========================================================== */
/* CONFIGURACION DE COLORES Y VARIABLES */
/* ========================================================== */
:root {
    --bg-dark: #0a1128; /* Fondo principal */
    --card-bg: rgba(10, 20, 40, 0.85); /* Fondo de la tarjeta con opacidad */
    --primary-light: #c9e6ff; /* Blanco/Azul claro para texto */
    --primary-blue-start: #1e90ff; /* Azul vibrante inicio */
    --primary-blue-end: #3c7dff; /* Azul vibrante fin */
    --input-bg: #152538; /* Fondo del input */
    --input-border: #1f3750; /* Borde del input */
    --input-focus: #4c8fff; /* Borde del input en foco */
    --text-subtle: #a0c2e6; /* Texto sutil */
    --focus-glow: rgba(76, 143, 255, 0.2);
    /* Colores del asistente */
    --assistant-primary: #ffc107; /* Amarillo */
    --assistant-bg: #101a35; /* Fondo del modal */
}

/* --- ANIMACIONES GLOBALES DE FONDO --- */
@keyframes gradient-animation {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
@keyframes background-pan {
  from { background-position: 0 0; }
  to { background-position: 100% 100%; }
}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
@keyframes pulse-shadow {
  0% { filter: drop-shadow(0 0 5px rgba(76, 143, 255, 0.5)); }
  100% { filter: drop-shadow(0 0 15px rgba(76, 143, 255, 0.8)); }
}

.page.form-page-animated {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 28px;
  font-family: 'Inter', system-ui, Arial, sans-serif;
  overflow: hidden;

  /* Fondo de gradiente animado (CORREGIDO) */
  background: linear-gradient(135deg, var(--bg-dark), #112a4c, var(--bg-dark));
  background-size: 300% 300%;
  animation: gradient-animation 15s ease infinite;
  position: relative;
  /* La línea background-image: none; fue eliminada de aquí */
}

/* --- Efecto de Fondo "Malla" --- */
.page.form-page-animated::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%233c7dff' fill-opacity='0.08'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zm0-30V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.8;
  pointer-events: none;
  animation: background-pan 60s linear infinite;
}

/* --- TARJETA PRINCIPAL (Parallax + Glassmorphism) --- */
.card-animated {
  width: 100%;
  max-width: 450px;
  padding: 3px;
  border-radius: 25px;
  background: linear-gradient(145deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0.03) 100%);
  position: relative;
  z-index: 10;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  transition: transform 0.5s cubic-bezier(0.25, 0.8, 0.25, 1), box-shadow 0.5s ease;
  will-change: transform;
}
.card-animated:hover {
  box-shadow: 0 30px 90px rgba(0, 0, 0, 0.7);
}

.card-inner {
  padding: 40px;
  border-radius: 22px;
  background: var(--card-bg);
  backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

/* --- TÍTULOS Y SUBTÍTULOS --- */
.title-animated {
    margin-bottom: 5px;
    font-size: 2.2rem;
    font-weight: 900;
    text-align: center;
    letter-spacing: -0.04em;
    background: linear-gradient(90deg, #b2d2ff, #e5f1ff);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}
.title, .title-animated { margin-top: 15px; }
.subtitle { margin: 0 0 30px; color: var(--text-subtle); font-size: 14px; text-align: center; }

/* --- LOGO Y BRAND --- */
.brand { display: flex; justify-content: center; gap: 12px; margin-bottom: 0px; align-items: center;}
.brand h1 { font-size: 1.2rem; color: var(--primary-light); margin: 0; font-weight: 700;}
.logo-icon {
    font-size: 36px;
    filter: drop-shadow(0 0 10px rgba(76, 143, 255, 0.8));
    animation: pulse-shadow 3s infinite alternate ease-in-out;
    color: var(--primary-blue-start);
    line-height: 1;
}
/* El resto de los estilos del logo, input, botones y asistente son correctos y se mantienen. */

/* --- INPUTS --- */
.input-group { margin-bottom: 22px; }
.input-label-animated {
    display: block; margin-bottom: 6px; color: var(--text-subtle);
    font-size: 0.9rem; font-weight: 500;
}
.custom-input-animated {
    width: 100%; padding: 16px 18px; border-radius: 12px;
    border: 1px solid var(--input-border); background: var(--input-bg);
    color: var(--primary-light); font-size: 1rem; transition: all 0.3s ease;
    box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.5);
}
.custom-input-animated::placeholder { color: #5b7393; }
.custom-input-animated:focus {
    border-color: var(--input-focus); outline: none;
    box-shadow: 0 0 0 4px var(--focus-glow), inset 0 1px 3px rgba(0, 0, 0, 0.5);
}

.password-row { display:flex; gap:10px; align-items:stretch; }
.password-row input { flex-grow: 1; padding-right: 18px; }

/* Botón Mostrar/Ocultar */
.peek {
    padding: 0 15px;
    font-size: 0.9rem;
    font-weight: 600;
    background: transparent;
    color: var(--text-subtle);
    border: 1px solid rgba(255, 255, 255, 0.15);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
}
.peek:hover {
    background: rgba(76, 143, 255, 0.1);
    border-color: rgba(76, 143, 255, 0.3);
    color: var(--primary-light);
}

/* --- BOTONES --- */
.row-animated {
    display: flex; justify-content: center; margin-top: 25px; margin-bottom: 0px;
}
.btn-animated {
    padding: 16px 28px; border-radius: 12px; border: none; cursor: pointer;
    font-weight: 700; font-size: 1.1rem; transition: all 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);
    letter-spacing: 0.03em; position: relative; overflow: hidden;
    display: flex; align-items: center; justify-content: center; gap: 10px;
}
.primary-animated {
    background: linear-gradient(90deg, var(--primary-blue-start), var(--primary-blue-end));
    color: #ffffff;
    box-shadow: 0 10px 30px rgba(30, 144, 255, 0.5);
    flex-grow: 1;
    max-width: 100%;
}
.primary-animated:hover:not(:disabled) {
    transform: translateY(-3px);
    box-shadow: 0 15px 35px rgba(30, 144, 255, 0.7);
    background: linear-gradient(90deg, var(--primary-blue-end), var(--primary-blue-start));
}
.primary-animated:disabled {
    opacity: 0.5; cursor: not-allowed; box-shadow: none;
    background: linear-gradient(90deg, #5a6b7e, #4b5a6c);
}
.actions.extra {
    margin-top: 15px;
    margin-bottom: 10px;
    display: flex; /* Añadido para habilitar el centrado */
    justify-content: center; /* Centra el botón horizontalmente */
}
.secondary-animated {
    background: transparent; color: var(--text-subtle);
    border: 1px solid rgba(255, 255, 255, 0.15);
    font-size: 0.9rem; padding: 12px 20px;
    max-width: 100%; border-radius: 12px; display: block;
}
.secondary-animated.wide-btn {
    width: 100%; /* Se eliminó el max-width: 300px para que ocupe todo el ancho disponible */
}
.secondary-animated:hover {
    background: rgba(76, 143, 255, 0.1); border-color: rgba(76, 143, 255, 0.3);
    color: var(--primary-light); transform: translateY(-1px);
}

/* --- MENSAJES & RESULTADOS --- */
.msg-animated {
    margin-top: 25px; padding: 14px; border-radius: 12px; text-align: center;
    color: #e5f1ff; font-size: 0.95rem; box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}
.ok { background: rgba(40,167,69,0.15); border: 1px solid #28a745; }
.fail { background: rgba(220,53,69,0.15); border: 1px solid #dc3545; }
.error { color: #ffb4b4; font-size: 0.8rem; margin-top: 4px; }
.help { margin-top: 25px; text-align: center; color: var(--text-subtle); font-size: 0.8rem; }
.result { margin-top:20px; background: rgba(0, 123, 255, 0.05); padding:15px; border-radius:10px; border:1px solid rgba(0, 123, 255, 0.1); }
.result h3 { margin-top: 0; font-size: 16px; color: #7fbdff; }
.result pre { white-space:pre-wrap; word-break:break-word; color:#dbefff; font-size:14px; margin: 5px 0 10px 0;}
.btn-link {
    background: none; border: none; color: #7fbdff; text-decoration: underline;
    cursor: pointer; font-size: 0.9rem; padding: 0; margin-top: 10px; display: block;
    transition: color 0.2s;
}
.btn-link:hover { color: #fff; }

/* SPINNER */
.spinner {
  border: 4px solid rgba(255, 255, 255, 0.3); border-top: 4px solid #fff;
  width: 18px; height: 18px; border-radius: 50%; animation: spin 1s linear infinite;
}

/* ========================================================== */
/* ESTILOS DEL ASISTENTE VIRTUAL (Mantenidos) */
/* ========================================================== */
.assistant-container {
  position: fixed; bottom: 30px; right: 30px; z-index: 100;
}
.assistant-button {
  width: 55px; height: 55px; border-radius: 50%; border: none;
  /* CAMBIOS PRINCIPALES AQUÍ: */
  background: #4ade80; /* Un verde más brillante */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.6), 0 0 20px 5px rgba(74, 222, 128, 0.8); /* Sombra más fuerte y brillo */
  /* FIN DE CAMBIOS */
  color: #1e284a; display: flex; align-items: center;
  justify-content: center; cursor: pointer;
  /* Mantenemos la transición y el tamaño */
  transition: transform 0.2s, box-shadow 0.2s;
}
 /* Efecto Hover para hacerlo más dinámico */
 .assistant-button:hover {
     transform: scale(1.1); /* Crece ligeramente al pasar el mouse */
     box-shadow: 0 4px 15px rgba(0, 0, 0, 0.8), 0 0 30px 8px rgba(74, 222, 128, 1); /* Sombra un poco más intensa al interactuar */
 }
.assistant-button .icon { stroke: #1e284a; transition: stroke 0.2s; }

.assistant-modal {
    position: absolute; bottom: 70px; right: 0; width: 300px;
    max-height: 450px; overflow-y: auto;
    background: var(--assistant-bg);
    border: 1px solid rgba(76, 143, 255, 0.2);
    border-radius: 12px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.8);
    backdrop-filter: blur(10px);
    z-index: 99;
}

.assistant-header {
    background: var(--primary-blue-end);
    color: #fff; padding: 14px 20px; font-size: 16px; font-weight: 600;
    border-top-left-radius: 12px; border-top-right-radius: 12px;
    position: sticky; top: 0; z-index: 10;
}
.assistant-body {
    padding: 20px; color: var(--text-subtle); font-size: 14px;
}
.assistant-gemini-info small {
    display: block; margin-bottom: 15px; padding-bottom: 15px;
    border-bottom: 1px dashed rgba(76, 143, 255, 0.2);
    color: #94a3b8;
}

.chat-response code, .chat-response pre {
    background: #0d1226;
    color: #6a95ff;
    padding: 3px 6px; border-radius: 6px;
    font-family: monospace; font-size: 0.9em;
}
.chat-response pre.payload-example {
    border: 1px dashed var(--primary-blue-start);
    background: #0d1226;
    display: block;
    padding: 10px;
    margin: 10px 0;
    overflow-x: auto;
    font-size: 1em;
}

.success-message {
    padding: 15px; background: rgba(40, 167, 69, 0.15);
    border: 1px solid #28a745; border-radius: 8px; color: #9be9a8;
}
.hint-control { margin-top: 25px; display: flex; gap: 10px; justify-content: center; }
.btn-hint { font-size: 13px; font-weight: 600; padding: 8px 12px; border: none; cursor: pointer; border-radius: 8px;}

.btn-hint.btn-primary { background: var(--primary-blue-start); color: white; }
.btn-hint.btn-primary:hover { background: var(--primary-blue-end); transform: translateY(-1px); }
.btn-hint.btn-secondary {
    background: #1a2342; border: 1px solid var(--primary-blue-start);
    color: var(--text-subtle);
}
.btn-hint.btn-secondary:hover { background: #232f50; }

.chat-response ol {
    margin: 10px 0 0 20px;
    padding: 0;
    list-style-type: decimal;
    color: #e6eef8;
}
.chat-response ol li {
    margin-bottom: 5px;
}

/* Transición de Vue */
.assistant-fade-slide-enter-active,
.assistant-fade-slide-leave-active { transition: all 0.3s ease-in-out; }
.assistant-fade-slide-enter-from,
.assistant-fade-slide-leave-to { opacity: 0; transform: translateY(10px) scale(0.95); }

/* responsive */
@media (max-width:520px) {
  .card-inner { padding:25px; }
  .title-animated { font-size: 1.8rem; }
  .btn-animated { font-size: 1rem; }
  .btn-animated { font-size: 1rem; }
  .actions.extra { margin-top: 10px; }

  /* responsive assistant */
    .assistant-container { bottom: 15px; right: 15px; }
    .assistant-modal { width: calc(100vw - 30px); bottom: 75px; right: 0; left: 0; margin: 0 auto; }
}
</style>
