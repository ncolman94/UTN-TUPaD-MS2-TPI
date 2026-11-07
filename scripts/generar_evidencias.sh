#!/usr/bin/env bash
# Regenera todas las evidencias de la version actual en docs/evidencias/:
# pruebas y cobertura, caracterizacion, cambios intencionales, PMD, metricas y demos.
# Uso: scripts/generar_evidencias.sh   (requiere Java 17+ y Python 3)
set -euo pipefail
cd "$(dirname "$0")/.."
MVN="${MVN:-./mvnw}"
DESTINO=docs/evidencias

$MVN -q -B clean verify pmd:pmd

mkdir -p "$DESTINO/pruebas" "$DESTINO/estado-refactorizado"
python3 scripts/resumen_pruebas.py > "$DESTINO/pruebas/resumen-pruebas-y-cobertura.md"
rm -rf "$DESTINO/pruebas/caracterizacion" "$DESTINO/pruebas/cambios-intencionales"
cp -r target/caracterizacion "$DESTINO/pruebas/caracterizacion"
cp -r target/cambios-intencionales "$DESTINO/pruebas/cambios-intencionales"
sort -o "$DESTINO/pruebas/caracterizacion/resumen.txt" "$DESTINO/pruebas/caracterizacion/resumen.txt"

python3 scripts/resumen_pmd.py target/pmd.xml --detalle > "$DESTINO/estado-refactorizado/pmd-resumen.md"
python3 scripts/metricas.py --json "$DESTINO/estado-refactorizado/metricas.json" \
    > "$DESTINO/estado-refactorizado/metricas.md"
for entrada in "$DESTINO"/entradas/*.txt; do
    scripts/ejecutar_demo.sh "$entrada" "$DESTINO/estado-refactorizado/$(basename "$entrada")"
done
echo "Evidencias actualizadas en $DESTINO"
