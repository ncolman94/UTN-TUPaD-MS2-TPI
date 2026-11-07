#!/usr/bin/env bash
# Ejecuta el sistema con una entrada fija sobre una copia de los datos de ejemplo
# y guarda la salida, para dejar evidencia reproducible del comportamiento.
#
# Uso: scripts/ejecutar_demo.sh <entrada.txt> <salida.txt> [jar]
#   jar: por defecto target/gestor-solicitudes.jar (antes: ./mvnw -q package).
#        Sirve para correr otra version, por ejemplo la del tag estado-inicial:
#          git worktree add ../inicial estado-inicial
#          (cd ../inicial && ./mvnw -q package)
#          scripts/ejecutar_demo.sh entrada.txt salida.txt ../inicial/target/gestor-solicitudes.jar
#
# Los datos de datos/ no se modifican: se trabaja en una carpeta temporal.
set -euo pipefail
cd "$(dirname "$0")/.."
entrada="$(realpath "$1")"
salida="$2"
jar="$(realpath "${3:-target/gestor-solicitudes.jar}")"
version="$(git -C "$(dirname "$jar")" describe --tags --always 2>/dev/null || echo desconocida)"
temporal="$(mktemp -d)"
trap 'rm -rf "$temporal"' EXIT
cp -r datos "$temporal/"
mkdir -p "$(dirname "$salida")"
{
    echo "### Version: $version - ejecutado el $(date '+%d/%m/%Y')"
    echo "### Entrada (una linea por dato ingresado):"
    sed 's/^/>  /; s/^>  $/>  (enter)/' "$entrada"
    echo "### Salida del programa:"
    set +e
    (cd "$temporal" && java -jar "$jar" < "$entrada" 2>&1)
    codigo=$?
    set -e
    echo
    echo "### Codigo de salida: $codigo"
    echo "### datos/solicitudes.csv al terminar:"
    cat "$temporal/datos/solicitudes.csv"
} > "$salida"
echo "Evidencia guardada en $salida"
