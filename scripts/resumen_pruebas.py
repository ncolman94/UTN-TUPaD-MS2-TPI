#!/usr/bin/env python3
"""Resume los reportes de JUnit (target/surefire-reports) y de JaCoCo (target/site/jacoco/jacoco.csv).

Uso (despues de ./mvnw verify):
  python3 scripts/resumen_pruebas.py
"""
import collections
import csv
import pathlib
import sys
import xml.etree.ElementTree as ET

RAIZ = pathlib.Path(__file__).resolve().parents[1]
SUREFIRE = RAIZ / "target" / "surefire-reports"
JACOCO = RAIZ / "target" / "site" / "jacoco" / "jacoco.csv"


def resumen_de_pruebas():
    por_clase = collections.OrderedDict()
    for archivo in sorted(SUREFIRE.glob("TEST-*.xml")):
        raiz = ET.parse(archivo).getroot()
        nombre = raiz.get("name").replace("ar.edu.utn.tupad.solicitudes.", "")
        clase = nombre.split("$")[0]
        datos = por_clase.setdefault(clase, [0, 0, 0])
        datos[0] += int(raiz.get("tests"))
        datos[1] += int(raiz.get("failures")) + int(raiz.get("errors"))
        datos[2] += int(raiz.get("skipped"))
    print("## Pruebas automatizadas (JUnit 5)\n")
    print("| Clase de prueba | Casos | Fallidos | Deshabilitados |")
    print("|---|---:|---:|---:|")
    for clase, (casos, fallidos, omitidos) in por_clase.items():
        print(f"| {clase} | {casos} | {fallidos} | {omitidos} |")
    totales = [sum(v[i] for v in por_clase.values()) for i in range(3)]
    print(f"| **Total** | **{totales[0]}** | **{totales[1]}** | **{totales[2]}** |\n")
    return totales[1]


def resumen_de_cobertura():
    filas = list(csv.DictReader(JACOCO.open(encoding="utf-8")))
    print("## Cobertura (JaCoCo)\n")
    print("| Paquete | Clase | Lineas | Ramas |")
    print("|---|---|---:|---:|")

    def porcentaje(cubiertas, faltantes):
        total = cubiertas + faltantes
        return "-" if total == 0 else f"{100 * cubiertas / total:.0f}% ({cubiertas}/{total})"

    for fila in filas:
        paquete = fila["PACKAGE"].replace("ar.edu.utn.tupad.solicitudes", "").lstrip(".") or "(raiz)"
        print(f'| {paquete} | {fila["CLASS"]} | '
              f'{porcentaje(int(fila["LINE_COVERED"]), int(fila["LINE_MISSED"]))} | '
              f'{porcentaje(int(fila["BRANCH_COVERED"]), int(fila["BRANCH_MISSED"]))} |')
    lineas = [sum(int(f[k]) for f in filas) for k in ("LINE_COVERED", "LINE_MISSED")]
    ramas = [sum(int(f[k]) for f in filas) for k in ("BRANCH_COVERED", "BRANCH_MISSED")]
    print(f"| **Total** | | **{porcentaje(*lineas)}** | **{porcentaje(*ramas)}** |")


def main():
    fallidos = resumen_de_pruebas()
    resumen_de_cobertura()
    return 1 if fallidos else 0


if __name__ == "__main__":
    sys.exit(main())
