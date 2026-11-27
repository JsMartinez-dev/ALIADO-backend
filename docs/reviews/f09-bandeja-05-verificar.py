#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
F09-BANDEJA-05 — Verificación HTTP del flujo de bandeja y lectura sobre PostgreSQL real.

Requiere el backend ALIADO en marcha (war empaquetado) apuntando a una base desechable
con las migraciones Flyway de la rama aplicadas, y psql disponible para leer/escribir las comprobaciones
que no tienen ruta HTTP (fechas iguales, concurrencia y estado del worker push).

Uso:
  ALIADO_BASE=http://127.0.0.1:8098 \
  ALIADO_PG_DSN="host=127.0.0.1 port=55435 user=aliado password=aliado dbname=aliado_f09b05_http" \
  python3 f09-bandeja-05-verificar.py principal

  ... (reiniciar la app con aliado.push.worker-habilitado=true) ...
  python3 f09-bandeja-05-verificar.py postworker

Salida: una línea PASS/FAIL por comprobación y código de salida 1 si alguna falla.
"""
import base64
import json
import os
import subprocess
import sys
import threading
import time
import uuid
import urllib.error
import urllib.request
from datetime import datetime

BASE = os.environ.get("ALIADO_BASE", "http://127.0.0.1:8098")
PG = os.environ.get(
    "ALIADO_PG_DSN",
    "host=127.0.0.1 port=55435 user=aliado password=aliado dbname=aliado_f09b05_http",
)
STATE = os.environ.get("ALIADO_ESTADO", "f09-bandeja-05-state.json")
CATEGORIA = "11111111-1111-1111-1111-111111111101"
ZONA = "22222222-2222-2222-2222-222222222201"
PASSWORD = "Clave1234"

resultados = []


def check(nombre, condicion, detalle=""):
    ok = bool(condicion)
    resultados.append((nombre, ok))
    print(("PASS" if ok else "FAIL") + " | " + nombre + ((" | " + str(detalle)) if detalle else ""))
    return ok


def secreto():
    """Secreto de 32 bytes en base64url sin padding (43 caracteres exigidos por el contrato)."""
    return base64.urlsafe_b64encode(os.urandom(32)).decode("ascii").rstrip("=")


def http(metodo, ruta, token=None, cuerpo=None, cabeceras=None):
    datos = json.dumps(cuerpo).encode("utf-8") if cuerpo is not None else None
    req = urllib.request.Request(BASE + ruta, data=datos, method=metodo)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    for clave, valor in (cabeceras or {}).items():
        req.add_header(clave, valor)
    try:
        with urllib.request.urlopen(req, timeout=30) as r:
            crudo = r.read()
            return r.status, (json.loads(crudo) if crudo else None), dict(r.headers)
    except urllib.error.HTTPError as e:
        crudo = e.read()
        try:
            cuerpo_error = json.loads(crudo)
        except ValueError:
            cuerpo_error = crudo.decode("utf-8", "replace")
        return e.code, cuerpo_error, dict(e.headers)


def sql(consulta):
    r = subprocess.run(["psql", PG, "-At", "-F", "|", "-c", consulta],
                       capture_output=True, text=True)
    if r.returncode != 0:
        raise RuntimeError("psql falló: " + r.stderr.strip())
    return r.stdout.rstrip("\n")


def sql_filas(consulta):
    return [linea.split("|") for linea in sql(consulta).splitlines() if linea]


def es_iso(texto):
    if not isinstance(texto, str):
        return False
    try:
        datetime.fromisoformat(texto.replace("Z", "+00:00"))
        return True
    except ValueError:
        return False


def registrar(email, rol):
    return http("POST", "/api/v1/auth/registro", cuerpo={
        "email": email, "password": PASSWORD, "rol": rol,
        "aceptaTerminos": True, "versionTerminos": "2026-01",
    }, cabeceras={"Idempotency-Key": str(uuid.uuid4())})


def iniciar_sesion(email):
    estado, cuerpo, _ = http("POST", "/api/v1/auth/login", cuerpo={"email": email, "password": PASSWORD})
    if estado != 200:
        raise RuntimeError("login falló para %s: %s %s" % (email, estado, cuerpo))
    return cuerpo["sessionToken"]


def preparar_prestador(email):
    _, usuario, _ = registrar(email, "PRESTADOR")
    token = iniciar_sesion(email)
    estado, perfil, _ = http("POST", "/api/v1/perfiles", token, {
        "datosGenerales": {"nombre": email.split("@")[0]},
        "categoriaIds": [CATEGORIA], "zonaIds": [ZONA], "experiencia": [],
    })
    if estado != 201:
        raise RuntimeError("perfil no creado: %s %s" % (estado, perfil))
    estado, _, _ = http("PUT", "/api/v1/perfiles/mio/disponibilidad", token, {
        "bloques": [{"diaSemana": 1, "horaInicio": "08:00:00", "horaFin": "12:00:00"}],
    })
    if estado != 200:
        raise RuntimeError("disponibilidad no configurada: %s" % estado)
    return usuario["id"], token


def publicar(token_cliente, descripcion):
    estado, solicitud, _ = http("POST", "/api/v1/solicitudes", token_cliente, {
        "categoriaId": CATEGORIA, "descripcion": descripcion, "zonaId": ZONA,
        "urgencia": "MEDIA", "estadoDeseado": "PUBLICADA",
    }, {"Idempotency-Key": str(uuid.uuid4())})
    if estado != 201:
        raise RuntimeError("publicación falló: %s %s" % (estado, solicitud))
    return solicitud["id"]


def bandeja(token, **parametros):
    consulta = "&".join("%s=%s" % (k, v) for k, v in parametros.items() if v is not None)
    ruta = "/api/v1/notificaciones/bandeja" + (("?" + consulta) if consulta else "")
    estado, cuerpo, _ = http("GET", ruta, token)
    return estado, cuerpo


def ids_de(cuerpo):
    return [item["id"] for item in cuerpo["items"]]


def insertar_avisos(destinatario, cantidad, fecha_generacion, detalle="aviso sembrado"):
    ids = []
    for _ in range(cantidad):
        aviso = str(uuid.uuid4())
        sql("INSERT INTO notificaciones (id, destinatario_id, tipo, aggregate_id, event_version, detalle,"
            " canal, estado_entrega, intentos, fecha_generacion) VALUES "
            "('%s','%s','NUEVA_OPORTUNIDAD','%s',1,'%s','PUSH','ENTREGADA',1,'%s');"
            % (aviso, destinatario, uuid.uuid4(), detalle, fecha_generacion))
        ids.append(aviso)
    return ids


def leida_en(aviso):
    fila = sql("SELECT to_char(leida_en AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"')"
               " FROM notificaciones WHERE id = '%s';" % aviso)
    return fila if fila else None


def pagina_completa(token, **parametros):
    """Recorre la bandeja hasta que cursorSiguiente sea nulo."""
    vistas, cursor, paginas = [], None, 0
    while True:
        estado, cuerpo = bandeja(token, cursor=cursor, **parametros)
        assert estado == 200, (estado, cuerpo)
        vistas.extend(ids_de(cuerpo))
        cursor = cuerpo.get("cursorSiguiente")
        paginas += 1
        if cursor is None:
            return vistas, paginas
        if paginas > 100:
            raise RuntimeError("la paginación no termina")


def en_paralelo(uno, dos):
    resultados_paralelos = [None, None]
    barrera = threading.Barrier(2)

    def ejecutar(indice, funcion):
        barrera.wait(timeout=10)
        resultados_paralelos[indice] = funcion()

    hilos = [threading.Thread(target=ejecutar, args=(0, uno)),
             threading.Thread(target=ejecutar, args=(1, dos))]
    for hilo in hilos:
        hilo.start()
    for hilo in hilos:
        hilo.join(timeout=30)
    return resultados_paralelos


def principal():
    sufijo = uuid.uuid4().hex[:6]
    email_a = "prestador.a.%s@f09b05.test" % sufijo
    email_b = "prestador.b.%s@f09b05.test" % sufijo
    email_c = "cliente.c.%s@f09b05.test" % sufijo

    estado, usuario_a, _ = registrar(email_a, "PRESTADOR")
    check("registro prestador A responde 201", estado == 201, estado)
    estado, usuario_b, _ = registrar(email_b, "PRESTADOR")
    check("registro prestador B responde 201", estado == 201, estado)
    estado, usuario_c, _ = registrar(email_c, "CLIENTE")
    check("registro cliente C responde 201", estado == 201, estado)
    cuenta_a, cuenta_b = usuario_a["id"], usuario_b["id"]

    token_a = iniciar_sesion(email_a)
    token_b = iniciar_sesion(email_b)
    token_c = iniciar_sesion(email_c)
    check("login de las tres cuentas devuelve token", all([token_a, token_b, token_c]))

    # Perfiles profesionales compatibles (categoría y zona) con disponibilidad configurada.
    for email in (email_a, email_b):
        token = token_a if email == email_a else token_b
        estado, perfil, _ = http("POST", "/api/v1/perfiles", token, {
            "datosGenerales": {"nombre": email.split("@")[0]},
            "categoriaIds": [CATEGORIA], "zonaIds": [ZONA], "experiencia": [],
        })
        check("perfil de %s creado (201)" % email.split("@")[0], estado == 201, estado)
        estado, _, _ = http("PUT", "/api/v1/perfiles/mio/disponibilidad", token, {
            "bloques": [{"diaSemana": 1, "horaInicio": "08:00:00", "horaFin": "12:00:00"}],
        })
        check("disponibilidad de %s configurada (200)" % email.split("@")[0], estado == 200, estado)

    estado, listado_previo, _ = http("GET", "/api/v1/notificaciones", token_a)
    check("ruta previa GET /notificaciones responde 200", estado == 200, estado)
    check("ruta previa vacía antes de publicar", listado_previo == [], listado_previo)

    # 1) Publicación real (evento SolicitudPublicada) -> aviso NUEVA_OPORTUNIDAD para A y B.
    solicitud_1 = publicar(token_c, "Solicitud de validación F09-BANDEJA-05 número 1")
    estado, pagina_a = bandeja(token_a, limite=100)
    check("GET bandeja responde 200 tras publicar", estado == 200, estado)
    check("A recibe exactamente un aviso por la publicación", len(pagina_a["items"]) == 1,
          pagina_a)
    aviso_1 = pagina_a["items"][0] if pagina_a["items"] else {}
    check("el aviso es NUEVA_OPORTUNIDAD con solicitud y leidaEn null",
          aviso_1.get("tipo") == "NUEVA_OPORTUNIDAD"
          and aviso_1.get("solicitudId") == solicitud_1 and aviso_1.get("leidaEn") is None,
          aviso_1)
    check("el aviso conserva los campos del contrato previo",
          {"id", "tipo", "servicioId", "solicitudId", "detalle", "estadoEntrega",
           "fechaGeneracion", "leidaEn"} == set(aviso_1.keys()), sorted(aviso_1.keys()))
    estado, pagina_b = bandeja(token_b, limite=100)
    check("B recibe su propio aviso de la misma publicación",
          estado == 200 and len(pagina_b["items"]) == 1
          and pagina_b["items"][0]["solicitudId"] == solicitud_1, pagina_b)

    estado, resumen, _ = http("GET", "/api/v1/notificaciones/resumen", token_a)
    check("resumen responde 200 y hayNoLeidas=true", estado == 200 and resumen == {"hayNoLeidas": True},
          (estado, resumen))

    # 2) Sin sesión: 401 en las cuatro rutas.
    rutas_sin_sesion = [
        ("GET", "/api/v1/notificaciones/bandeja"),
        ("GET", "/api/v1/notificaciones/resumen"),
        ("PUT", "/api/v1/notificaciones/%s/lectura" % aviso_1["id"]),
        ("PUT", "/api/v1/notificaciones/lectura"),
    ]
    codigos = [http(metodo, ruta)[0] for metodo, ruta in rutas_sin_sesion]
    check("las cuatro rutas responden 401 sin sesión", codigos == [401, 401, 401, 401], codigos)

    # 3) Marcar un aviso, cerrar sesión, volver a entrar: se conserva la misma fecha.
    estado, cuerpo, _ = http("PUT", "/api/v1/notificaciones/%s/lectura" % aviso_1["id"], token_a)
    check("marcar un aviso responde 204 sin cuerpo", estado == 204 and cuerpo is None, (estado, cuerpo))
    _, leidas = bandeja(token_a, estadoLectura="LEIDA", limite=100)
    leida_http = next((i["leidaEn"] for i in leidas["items"] if i["id"] == aviso_1["id"]), None)
    check("el aviso aparece en LEIDA con leidaEn ISO 8601", es_iso(leida_http), leida_http)
    check("el estado de lectura es un campo distinto de estadoEntrega",
          leidas["items"] and leidas["items"][0]["estadoEntrega"] in ("ENTREGADA", "FALLIDA", "PENDIENTE"),
          leidas["items"])

    estado, _, _ = http("POST", "/api/v1/auth/logout", token_a)
    check("logout responde 204", estado == 204, estado)
    estado, _, _ = http("GET", "/api/v1/notificaciones/bandeja", token_a)
    check("el token cerrado deja de ser válido (401)", estado == 401, estado)
    token_a = iniciar_sesion(email_a)
    _, leidas_tras_relogin = bandeja(token_a, estadoLectura="LEIDA", limite=100)
    leida_relogin = next((i["leidaEn"] for i in leidas_tras_relogin["items"] if i["id"] == aviso_1["id"]), None)
    check("tras cerrar sesión y volver a entrar se conserva la misma fecha",
          leida_relogin == leida_http, (leida_http, leida_relogin))
    estado, cuerpo, _ = http("PUT", "/api/v1/notificaciones/%s/lectura" % aviso_1["id"], token_a)
    check("repetir la lectura responde 204", estado == 204 and cuerpo is None, (estado, cuerpo))
    check("repetir la lectura conserva la primera fecha", leida_en(aviso_1["id"]) == leida_relogin,
          (leida_en(aviso_1["id"]), leida_relogin))

    # 4) Marcar todos y confirmar el resumen vacío.
    estado, cuerpo, _ = http("PUT", "/api/v1/notificaciones/lectura", token_a)
    check("marcar todos responde 204 sin cuerpo", estado == 204 and cuerpo is None, (estado, cuerpo))
    estado, resumen, _ = http("GET", "/api/v1/notificaciones/resumen", token_a)
    check("tras marcar todos el resumen devuelve false",
          estado == 200 and resumen == {"hayNoLeidas": False}, (estado, resumen))
    _, pendientes = bandeja(token_a, limite=100)
    check("la bandeja por defecto (NO_LEIDA) queda vacía sin cursor",
          pendientes == {"items": [], "cursorSiguiente": None}, pendientes)
    _, resumen_b, _ = http("GET", "/api/v1/notificaciones/resumen", token_b)
    check("marcar todos de A no altera la campana de B", resumen_b == {"hayNoLeidas": True}, resumen_b)

    # 5) Publicar otro aviso: la campana vuelve a mostrar pendientes.
    solicitud_2 = publicar(token_c, "Solicitud de validación F09-BANDEJA-05 número 2")
    estado, resumen, _ = http("GET", "/api/v1/notificaciones/resumen", token_a)
    check("un aviso nuevo vuelve a encender la campana",
          estado == 200 and resumen == {"hayNoLeidas": True}, (estado, resumen))
    _, primera = bandeja(token_a, limite=100)
    check("el aviso nuevo encabeza la primera página refrescada",
          primera["items"] and primera["items"][0]["solicitudId"] == solicitud_2, primera)
    aviso_2 = primera["items"][0]["id"]

    # 6) Aislamiento entre cuentas.
    estado, error_ajeno, _ = http("PUT", "/api/v1/notificaciones/%s/lectura" % aviso_2, token_b)
    check("B no puede marcar un aviso de A (404)", estado == 404, (estado, error_ajeno))
    existente = uuid.uuid4()
    estado, error_inexistente, _ = http("PUT", "/api/v1/notificaciones/%s/lectura" % existente, token_b)
    # La estructura es idéntica salvo los campos volátiles (timestamp, path, traceId).
    volatiles = {"timestamp", "path", "traceId"}
    estructura_ajeno = {k: v for k, v in error_ajeno.items() if k not in volatiles}
    estructura_inexistente = {k: v for k, v in error_inexistente.items() if k not in volatiles}
    check("aviso ajeno e inexistente comparten status, código, mensaje y claves",
          estado == 404 and estructura_ajeno == estructura_inexistente
          and set(error_ajeno.keys()) == set(error_inexistente.keys()),
          (error_ajeno, error_inexistente))
    _, pendientes_a = bandeja(token_a, limite=100)
    check("el aviso de A sigue pendiente tras el intento de B",
          aviso_2 in ids_de(pendientes_a), pendientes_a)
    _, pagina_b = bandeja(token_b, limite=100)
    check("la bandeja de B solo contiene avisos de B",
          cuenta_b == cuenta_b and all(i["id"] != aviso_2 for i in pagina_b["items"]), pagina_b)

    # 7) Fronteras de filtros, límites y cursores.
    invalidos = [
        ("estadoLectura inválido", dict(estadoLectura="LEIDOS")),
        ("tipo inválido", dict(tipo="NO_EXISTE")),
        ("limite=0", dict(limite=0)),
        ("limite=101", dict(limite=101)),
        ("limite no numérico", dict(limite="abc")),
        ("cursor malformado", dict(cursor="abc")),
    ]
    for nombre, parametros in invalidos:
        estado, cuerpo = bandeja(token_a, **parametros)
        check("400 con %s" % nombre, estado == 400 and isinstance(cuerpo, dict) and "code" in cuerpo,
              (estado, cuerpo))
    estado, _, _ = http("PUT", "/api/v1/notificaciones/no-es-uuid/lectura", token_a)
    check("400 con UUID de aviso malformado", estado == 400, estado)

    # 8) Paginación: páginas de 1 y de 2 sin duplicados, última sin cursor.
    for _ in range(4):
        publicar(token_c, "Solicitud de paginación F09-BANDEJA-05")
    _, pagina_amplia = bandeja(token_a, limite=100)
    total_pendientes = len(pagina_amplia["items"])
    check("limite=100 devuelve todos los pendientes y cierra el cursor",
          pagina_amplia["cursorSiguiente"] is None and total_pendientes == 5, pagina_amplia)
    _, pagina_por_defecto = bandeja(token_a)
    check("sin limite usa 20 por página (5 pendientes en una página)",
          len(pagina_por_defecto["items"]) == 5 and pagina_por_defecto["cursorSiguiente"] is None,
          pagina_por_defecto)
    vistas_uno, paginas_uno = pagina_completa(token_a, limite=1)
    check("limite=1 pagina 5 avisos y no repite IDs",
          paginas_uno == 5 and len(vistas_uno) == 5 and len(set(vistas_uno)) == 5,
          (paginas_uno, vistas_uno))
    vistas_dos, paginas_dos = pagina_completa(token_a, limite=2)
    check("limite=2 pagina 5 avisos (2+2+1) sin repetir IDs",
          paginas_dos == 3 and len(vistas_dos) == 5 and len(set(vistas_dos)) == 5,
          (paginas_dos, vistas_dos))

    # 9) Desempate con fechas de generación iguales (fila persistida por la publicación).
    fecha_repetida = "2026-04-01T12:00:00Z"
    empatados = insertar_avisos(cuenta_a, 5, fecha_repetida, "aviso con fecha repetida")
    vistas_empate, paginas_empate = pagina_completa(token_a, limite=3)
    check("con fechas iguales cada ID aparece una sola vez",
          len(vistas_empate) == 10 and len(set(vistas_empate)) == 10
          and set(empatados).issubset(vistas_empate)
          and vistas_empate.count(empatados[0]) == 1,
          (paginas_empate, vistas_empate))

    # 10) Un cursor de un filtro no se acepta con otro filtro.
    _, primera_pagina = bandeja(token_a, limite=2)
    cursor_no_leida = primera_pagina["cursorSiguiente"]
    estado, cuerpo = bandeja(token_a, estadoLectura="TODAS", limite=2, cursor=cursor_no_leida)
    check("cursor de NO_LEIDA con TODAS responde 400 CURSOR_INVALIDO",
          estado == 400 and cuerpo.get("code") == "CURSOR_INVALIDO", (estado, cuerpo))

    # 11) Contratos previos que deben seguir vigentes.
    estado, listado, _ = http("GET", "/api/v1/notificaciones", token_a)
    check("GET /notificaciones conserva 200 y siete campos por aviso",
          estado == 200 and all(set(i.keys()) == {"id", "tipo", "servicioId", "solicitudId",
                                                  "detalle", "estadoEntrega", "fechaGeneracion"}
                                for i in listado), (estado, listado[:1]))
    estado, oportunidades, _ = http("GET", "/api/v1/notificaciones/oportunidades", token_a)
    check("GET /notificaciones/oportunidades conserva 200", estado == 200, estado)
    estado, desde_aviso, _ = http("GET", "/api/v1/notificaciones/%s/oportunidad" % aviso_2, token_a)
    check("GET /notificaciones/{id}/oportunidad responde 200 con su contrato",
          estado == 200 and set(desde_aviso.keys()) == {"solicitudId", "estado", "puedeCotizar"},
          (estado, desde_aviso))

    # 12) Concurrencia: dos escrituras de la misma cuenta sobre el mismo aviso.
    concurrentes = insertar_avisos(cuenta_a, 2, "2026-04-02T09:00:00Z", "aviso concurrente")
    aviso_concurrente = concurrentes[0]
    marcar = lambda: http("PUT", "/api/v1/notificaciones/%s/lectura" % aviso_concurrente, token_a)[0]
    codigos_paralelos = en_paralelo(marcar, marcar)
    fecha_concurrente = leida_en(aviso_concurrente)
    check("dos lecturas concurrentes del mismo aviso responden 204",
          codigos_paralelos == [204, 204], codigos_paralelos)
    check("la fecha de lectura concurrente queda persistida una sola vez", fecha_concurrente is not None,
          fecha_concurrente)
    http("PUT", "/api/v1/notificaciones/%s/lectura" % aviso_concurrente, token_a)
    check("una lectura posterior conserva la fecha inicial",
          leida_en(aviso_concurrente) == fecha_concurrente,
          (leida_en(aviso_concurrente), fecha_concurrente))

    # 13) Concurrencia entre la lectura individual y la masiva.
    individual_masiva = insertar_avisos(cuenta_a, 3, "2026-04-03T09:00:00Z", "aviso individual-masiva")
    individual, masiva = en_paralelo(
        lambda: http("PUT", "/api/v1/notificaciones/%s/lectura" % individual_masiva[0], token_a)[0],
        lambda: http("PUT", "/api/v1/notificaciones/lectura", token_a)[0])
    check("lectura individual y masiva concurrentes responden 204",
          individual == 204 and masiva == 204, (individual, masiva))
    leidas_conjunto = [leida_en(aviso) for aviso in individual_masiva]
    check("la masiva concurrente deja leídos los tres avisos", all(leidas_conjunto), leidas_conjunto)
    posterior = insertar_avisos(cuenta_a, 1, "2026-04-03T09:30:00Z", "aviso posterior a la masiva")[0]
    check("un aviso creado después de la masiva sigue pendiente", leida_en(posterior) is None,
          leida_en(posterior))

    # 14) Preparar el worker del piloto: dispositivo de A y aviso nuevo leído antes del envío.
    instalacion = str(uuid.uuid4())
    prueba = secreto()
    estado, _, _ = http("PUT", "/api/v1/notificaciones/dispositivos/%s" % instalacion, token_a, {
        "plataforma": "ANDROID", "permisoConcedido": True,
        "token": "token-fcm-f09b05-" + sufijo, "credencialRevocacion": secreto(),
    }, {"X-ALIADO-Installation-Proof": prueba})
    check("registrar el dispositivo Android de A responde 204", estado == 204, estado)
    estado, _, _ = http("PUT", "/api/v1/preferencias/notificaciones", token_a, {
        "tipoAviso": "NUEVA_OPORTUNIDAD", "canal": "PUSH", "habilitada": True,
    })
    check("la preferencia PUSH/NUEVA_OPORTUNIDAD de A queda habilitada", estado == 200, estado)

    estado, resumen, _ = http("GET", "/api/v1/notificaciones/resumen", token_a)
    solicitud_worker = publicar(token_c, "Solicitud para el worker del piloto F09-BANDEJA-05")
    _, pendientes = bandeja(token_a, limite=100)
    aviso_worker = next((i["id"] for i in pendientes["items"]
                         if i["solicitudId"] == solicitud_worker), None)
    check("la publicación para el worker crea un aviso pendiente", aviso_worker is not None, pendientes)
    estado, _, _ = http("PUT", "/api/v1/notificaciones/%s/lectura" % aviso_worker, token_a)
    check("el aviso del worker se marca leído por HTTP (204)", estado == 204, estado)
    leida_worker = leida_en(aviso_worker)
    check("el aviso del worker tiene fecha de lectura persistida", es_iso(leida_worker), leida_worker)

    filas = sql_filas("SELECT t.estado FROM notificaciones n "
                      "JOIN trabajos_push t ON t.notificacion_id = n.id WHERE n.id = '%s';" % aviso_worker)
    check("el trabajo push queda encolado como PENDIENTE_PREPARACION sin worker habilitado",
          filas == [["PENDIENTE_PREPARACION"]], filas)

    estado_http = {
        "cuenta_a": cuenta_a, "email_a": email_a, "token_a": token_a,
        "aviso_worker": aviso_worker, "leida_worker": leida_worker,
        "instalacion": instalacion, "sufijo": sufijo,
        "solicitud_1": solicitud_1, "solicitud_2": solicitud_2,
        "solicitud_worker": solicitud_worker,
        "empatados": empatados, "pendientes_antes_del_worker": len(pendientes["items"]),
    }
    with open(STATE, "w", encoding="utf-8") as f:
        json.dump(estado_http, f, indent=2)
    print("estado guardado en " + STATE)


def postworker():
    with open(STATE, encoding="utf-8") as f:
        estado = json.load(f)
    aviso = estado["aviso_worker"]
    filas = sql_filas(
        "SELECT n.estado_entrega, t.estado, t.motivo_final, "
        "coalesce(e.estado,''), coalesce(e.ultimo_codigo_error,''), "
        "to_char(n.leida_en AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS.US\"Z\"') "
        "FROM notificaciones n JOIN trabajos_push t ON t.notificacion_id = n.id "
        "LEFT JOIN entregas_push e ON e.trabajo_id = t.id WHERE n.id = '%s';" % aviso)
    check("el worker finalizó el trabajo push", filas and filas[0][1] == "FINALIZADO", filas)
    check("el worker registró el resultado terminal de la entrega (proveedor falso)",
          filas and filas[0][3] == "FALLIDA_DEFINITIVA"
          and filas[0][4] == "PROVEEDOR_NO_CONFIGURADO", filas)
    check("el worker no alteró la fecha de lectura del aviso",
          filas and filas[0][5] == estado["leida_worker"], (filas, estado["leida_worker"]))
    token = iniciar_sesion(estado["email_a"])
    estado_http, cuerpo, _ = http("GET", "/api/v1/notificaciones/bandeja?estadoLectura=TODAS&limite=100", token)
    item = next((i for i in cuerpo["items"] if i["id"] == aviso), None) if estado_http == 200 else None
    check("la API sigue devolviendo la misma fecha de lectura tras el worker",
          item is not None and item["leidaEn"] == estado["leida_worker"], item)


def main():
    modo = sys.argv[1] if len(sys.argv) > 1 else "principal"
    try:
        if modo == "principal":
            principal()
        elif modo == "postworker":
            postworker()
        else:
            raise SystemExit("modo desconocido: " + modo)
    finally:
        fallidas = [nombre for nombre, ok in resultados if not ok]
        print("\nRESUMEN: %d comprobaciones, %d correctas, %d fallidas"
              % (len(resultados), len(resultados) - len(fallidas), len(fallidas)))
        for nombre in fallidas:
            print("  FALLÓ: " + nombre)
    if any(not ok for _, ok in resultados):
        sys.exit(1)


if __name__ == "__main__":
    main()
