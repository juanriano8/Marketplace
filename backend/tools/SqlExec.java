import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Ejecuta una sentencia SQL sobre la base de datos configurada.
 *
 * <p>Existe porque algunas operaciones de mantenimiento no tienen endpoint en la
 * API: por ejemplo, ocultar productos de prueba que quedaron en estado ACTIVE y
 * que la regla de negocio ya no permite rechazar (solo se puede aprobar o
 * rechazar desde PENDING_APPROVAL).</p>
 *
 * <p>Uso:</p>
 * <pre>
 *   java -cp postgresql.jar tools/SqlExec.java host puerto usuario password base "UPDATE ..."
 * </pre>
 *
 * <p>Imprime el numero de filas afectadas. Solo admite una sentencia por
 * invocacion, y no hace commit implicito: si la sentencia falla, no cambia nada.</p>
 */
public class SqlExec {

    public static void main(String[] args) throws Exception {
        if (args.length < 6) {
            System.err.println("Uso: java SqlExec.java <host> <puerto> <usuario> <password> <base> <sql>");
            System.exit(1);
        }

        String host = args[0];
        String puerto = args[1];
        String usuario = args[2];
        String password = args[3];
        String base = args[4];
        String sql = args[5];

        String url = "jdbc:postgresql://" + host + ":" + puerto + "/" + base + "?sslmode=require";

        try (Connection conexion = DriverManager.getConnection(url, usuario, password);
             Statement sentencia = conexion.createStatement()) {

            boolean hayResultado = sentencia.execute(sql);

            if (hayResultado) {
                try (ResultSet rs = sentencia.getResultSet()) {
                    int columnas = rs.getMetaData().getColumnCount();
                    int filas = 0;
                    while (rs.next()) {
                        StringBuilder linea = new StringBuilder();
                        for (int i = 1; i <= columnas; i++) {
                            if (i > 1) {
                                linea.append(" | ");
                            }
                            linea.append(rs.getString(i));
                        }
                        System.out.println(linea);
                        filas++;
                    }
                    System.out.println("Filas devueltas: " + filas);
                }
            } else {
                System.out.println("Filas afectadas: " + sentencia.getUpdateCount());
            }
        }
    }
}
