#!/usr/bin/env python3
"""Metricas simples de mantenibilidad para el codigo Java de src/main/java.

Mide, por archivo y en total:
  - lineas de codigo (sin lineas en blanco ni comentarios),
  - cantidad de metodos y constructores,
  - largo del metodo mas extenso (en lineas),
  - mayor cantidad de parametros de un metodo,
  - complejidad ciclomatica aproximada del metodo mas complejo
    (1 + if/for/while/case/catch/&&/||/?),
  - literales de texto con nombres de tipo, estado o prioridad
    ("PENDIENTE", "CERTIFICADO", "ALTA", ...): miden la obsesion por primitivos
    y cuantos lugares hay que tocar cuando cambia una regla.

Uso:
  python3 scripts/metricas.py                      # codigo actual
  python3 scripts/metricas.py --ref estado-inicial # codigo de un tag o commit
  python3 scripts/metricas.py --json salida.json   # ademas guarda el resultado

Es un analisis aproximado por expresiones regulares: alcanza para comparar
versiones del mismo proyecto, no reemplaza a una herramienta como PMD.
Solo usa la biblioteca estandar de Python 3.
"""
import argparse
import io
import json
import pathlib
import re
import subprocess
import sys
import tarfile

RAIZ = pathlib.Path(__file__).resolve().parents[1]
CARPETA_FUENTES = "src/main/java"

NOMBRES_DE_REGLA = {
    "CERTIFICADO", "EXAMEN", "EQUIVALENCIA", "REVISION", "BAJA",
    "PENDIENTE", "EN_PROCESO", "RESUELTA", "RECHAZADA",
    "ALTA", "MEDIA",
}
PALABRAS_DE_CONTROL = {"if", "for", "while", "switch", "catch", "synchronized", "return", "new", "else", "try"}

FIRMA_METODO = re.compile(
    r"(?:(?:public|private|protected|static|final|default|synchronized|abstract)\s+)*"
    r"(?:<[^>]+>\s+)?([\w.<>\[\],?]+(?:\s*<[^>]*>)?)\s+(\w+)\s*\(([^()]*)\)\s*"
    r"(?:throws\s+[\w.,\s]+)?\{"
)
FIRMA_CONSTRUCTOR = re.compile(r"(?:public|private|protected)\s+(\w+)\s*\(([^()]*)\)\s*(?:throws\s+[\w.,\s]+)?\{")
DECISIONES = re.compile(r"\bif\b|\bfor\b|\bwhile\b|\bcase\b|\bcatch\b|&&|\|\||\?(?![>,])")


def leer_fuentes(ref):
    """Devuelve {ruta: texto} de los .java de src/main/java (del disco o de un ref de git)."""
    if ref is None:
        base = RAIZ / CARPETA_FUENTES
        return {str(p.relative_to(RAIZ)): p.read_text(encoding="utf-8") for p in sorted(base.rglob("*.java"))}
    tar = subprocess.run(["git", "-C", str(RAIZ), "archive", ref, CARPETA_FUENTES],
                         check=True, capture_output=True).stdout
    fuentes = {}
    with tarfile.open(fileobj=io.BytesIO(tar)) as archivo:
        for miembro in archivo.getmembers():
            if miembro.isfile() and miembro.name.endswith(".java"):
                fuentes[miembro.name] = archivo.extractfile(miembro).read().decode("utf-8")
    return dict(sorted(fuentes.items()))


def sin_comentarios(texto):
    """Quita comentarios conservando los saltos de linea (para no correr las posiciones)."""
    def blanquear(m):
        return re.sub(r"[^\n]", " ", m.group(0))
    return re.sub(r"/\*.*?\*/|//[^\n]*", blanquear, texto, flags=re.S)


def sin_textos(texto):
    """Vacia el contenido de los literales de texto para que no confundan al analisis."""
    return re.sub(r'"(?:\\.|[^"\\])*"', '""', texto)


def fin_de_bloque(texto, inicio):
    """Indice de la llave que cierra el bloque abierto en texto[inicio] == '{'."""
    profundidad = 0
    for i in range(inicio, len(texto)):
        if texto[i] == "{":
            profundidad += 1
        elif texto[i] == "}":
            profundidad -= 1
            if profundidad == 0:
                return i
    return len(texto) - 1


def contar_parametros(lista):
    lista = re.sub(r"<[^>]*>", "", lista).strip()
    return 0 if not lista else len(lista.split(","))


def metodos(codigo):
    encontrados = []
    for patron, es_constructor in ((FIRMA_METODO, False), (FIRMA_CONSTRUCTOR, True)):
        for m in patron.finditer(codigo):
            if es_constructor:
                nombre, parametros = m.group(1), m.group(2)
            else:
                tipo, nombre, parametros = m.group(1), m.group(2), m.group(3)
                if tipo in PALABRAS_DE_CONTROL or nombre in PALABRAS_DE_CONTROL or tipo in {"record", "class", "enum", "interface"}:
                    continue
            if nombre in PALABRAS_DE_CONTROL:
                continue
            apertura = m.end() - 1
            cierre = fin_de_bloque(codigo, apertura)
            cuerpo = codigo[apertura:cierre + 1]
            lineas = codigo[m.start():cierre + 1].count("\n") + 1
            encontrados.append({
                "inicio": m.start(),
                "nombre": nombre,
                "lineas": lineas,
                "parametros": contar_parametros(parametros),
                "complejidad": 1 + len(DECISIONES.findall(cuerpo)),
            })
    unicos = {m["inicio"]: m for m in encontrados}
    return [unicos[k] for k in sorted(unicos)]


def medir(fuentes):
    archivos = []
    for ruta, texto in fuentes.items():
        limpio = sin_comentarios(texto)
        codigo = sin_textos(limpio)
        literales = [l for l in re.findall(r'"((?:\\.|[^"\\])*)"', limpio) if l in NOMBRES_DE_REGLA]
        ms = metodos(codigo)
        mayor = max(ms, key=lambda m: m["lineas"], default=None)
        archivos.append({
            "archivo": ruta.replace(CARPETA_FUENTES + "/", ""),
            "loc": sum(1 for linea in limpio.splitlines() if linea.strip()),
            "metodos": len(ms),
            "metodo_mas_largo": f'{mayor["nombre"]} ({mayor["lineas"]})' if mayor else "-",
            "max_lineas_metodo": mayor["lineas"] if mayor else 0,
            "max_parametros": max((m["parametros"] for m in ms), default=0),
            "max_complejidad": max((m["complejidad"] for m in ms), default=0),
            "literales_de_regla": len(literales),
        })
    total = {
        "archivos": len(archivos),
        "loc": sum(a["loc"] for a in archivos),
        "loc_archivo_mas_grande": max((a["loc"] for a in archivos), default=0),
        "metodos": sum(a["metodos"] for a in archivos),
        "max_lineas_metodo": max((a["max_lineas_metodo"] for a in archivos), default=0),
        "max_parametros": max((a["max_parametros"] for a in archivos), default=0),
        "max_complejidad": max((a["max_complejidad"] for a in archivos), default=0),
        "literales_de_regla": sum(a["literales_de_regla"] for a in archivos),
    }
    return {"archivos": archivos, "total": total}


def imprimir(resultado, titulo):
    print(f"## Metricas - {titulo}\n")
    print("| Archivo | LOC | Metodos | Metodo mas largo (lineas) | Max. parametros | Max. complejidad | Literales de regla |")
    print("|---|---:|---:|---|---:|---:|---:|")
    for a in resultado["archivos"]:
        print(f'| {a["archivo"]} | {a["loc"]} | {a["metodos"]} | {a["metodo_mas_largo"]} | '
              f'{a["max_parametros"]} | {a["max_complejidad"]} | {a["literales_de_regla"]} |')
    t = resultado["total"]
    print(f'| **Total ({t["archivos"]} archivos)** | **{t["loc"]}** | **{t["metodos"]}** | '
          f'**{t["max_lineas_metodo"]}** | **{t["max_parametros"]}** | **{t["max_complejidad"]}** | '
          f'**{t["literales_de_regla"]}** |')


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--ref", help="tag o commit de git a medir (por defecto, el codigo actual)")
    parser.add_argument("--json", help="archivo donde guardar el resultado en JSON")
    args = parser.parse_args()
    resultado = medir(leer_fuentes(args.ref))
    imprimir(resultado, args.ref or "codigo actual")
    if args.json:
        pathlib.Path(args.json).write_text(json.dumps(resultado, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    return 0


if __name__ == "__main__":
    sys.exit(main())
