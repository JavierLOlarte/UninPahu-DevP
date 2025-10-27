-- Crear esquema uninpahu si no existe
CREATE SCHEMA IF NOT EXISTS uninpahu AUTHORIZATION vulnuser;

-- Establecer esquema por defecto
ALTER DATABASE vulnappdb SET search_path TO uninpahu, public;
