#!/usr/bin/env python3
"""Resume el reporte XML de PMD (target/pmd.xml) en una tabla Markdown.

Uso:
  ./mvnw -q compile pmd:pmd
  python3 scripts/resumen_pmd.py target/pmd.xml            # conteo por regla
  python3 scripts/resumen_pmd.py target/pmd.xml --detalle  # ademas, cada hallazgo
"""
import collections
import sys
import xml.etree.ElementTree as ET

NS = {"p": "http://pmd.sourceforge.net/report/2.0.0"}


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return 2
    raiz = ET.parse(sys.argv[1]).getroot()
    por_regla = collections.Counter()
    detalle = []
    for archivo in raiz.findall("p:file", NS):
        nombre = archivo.get("name").split("src/main/java/")[-1]
        for v in archivo.findall("p:violation", NS):
            por_regla[v.get("rule")] += 1
            detalle.append((nombre, int(v.get("beginline")), v.get("rule"), " ".join(v.text.split())))
    print(f"Total de hallazgos PMD: {sum(por_regla.values())}\n")
    print("| Regla | Hallazgos |")
    print("|---|---:|")
    for regla, cantidad in sorted(por_regla.items(), key=lambda x: (-x[1], x[0])):
        print(f"| {regla} | {cantidad} |")
    if "--detalle" in sys.argv:
        print("\n| Archivo | Linea | Regla | Mensaje |")
        print("|---|---:|---|---|")
        for nombre, linea, regla, mensaje in sorted(detalle):
            print(f"| {nombre} | {linea} | {regla} | {mensaje.replace('|', '/')} |")
    return 0


if __name__ == "__main__":
    sys.exit(main())
